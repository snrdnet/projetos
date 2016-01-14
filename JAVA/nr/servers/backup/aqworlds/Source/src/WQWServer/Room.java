package WQWServer;

import java.io.*;
import java.sql.*;

/**
 * @author XVII
 * Rooms
 */
public class Room
{
    public String[] roomSlot = new String[20];
    public int roomType;
    public String roomName;
    public String fileName;
    public String[] pad = new String[50];
    public String[] frame = new String[50];
    public int[] tx = new int[20];
    public int[] ty = new int[20];
    public boolean[] afk = new boolean[20];
    public int roomNumb;
    public int users;
    private PrintWriter[] playerSocket = new PrintWriter[20];
    public String[] monsterBehave = new String[64];
    public int[] monsterHP = new int[64];
    public int[] monsterMP = new int[64];
    public int[] monsterHPMax = new int[64];
    public int[] monsterMPMax = new int[64];
    public int[] monsterState = new int[64];
    public int[] monsterType = new int[64];
    public int[] monsterLevel = new int[64];
    public String[] monsterFrame = new String[64];

    public Room(int type, int numb)
    {
        this.roomType = type;
        this.roomNumb = numb;
        for (int i = 0; i < roomSlot.length; i++) {
            this.roomSlot[i] = "";
            this.pad[i] = "Spawn";
            this.frame[i] = "Enter";
            this.afk[i] = false;
        }
        try {
            ResultSet rs = Main.sql.doquery("SELECT * FROM wqw_maps WHERE id="+type);
            if (rs.next()) {
                this.roomName = rs.getString("name");
                this.fileName = rs.getString("fileName");
                if (!rs.getString("monsternumb").equals("")) {
                    this.monsterBehave = rs.getString("monsternumb").split(",");
                    for (int a = 0; a < this.monsterBehave.length; a++) {
                        ResultSet is = Main.sql.doquery("SELECT * FROM wqw_monsters WHERE MonID="+this.monsterBehave[a]);
                        if (is.next()) {
                            this.monsterHP[a] = is.getInt("intHPMax");
                            this.monsterMP[a] = is.getInt("intMPMax");
                            this.monsterHPMax[a] = is.getInt("intHPMax");
                            this.monsterMPMax[a] = is.getInt("intMPMax");
                            this.monsterLevel[a] = is.getInt("intLevel");
                        }
                        this.monsterType[a] = Integer.parseInt(this.monsterBehave[a]);
                        this.monsterBehave[a] = "walk";
                        this.monsterState[a] = 1;
                    }
                }
            }
            rs.close();
        } catch (Exception e) {
            Main.server.debug("Exception in room: "+e.getMessage());
        }
    }

    protected String getMon(String[] monnumbs)
    {
        Packet sendPack2 = new Packet();
        try {
            for (int o = 0; o < monnumbs.length; o++) {
                ResultSet os = Main.sql.doquery("SELECT * FROM wqw_monsters WHERE MonID="+monnumbs[o]);
                if (os.next()) {
                    if (o != 0) {
                        sendPack2.addString(",");
                    }
                    sendPack2.addString("{\"intHPMax\":"+os.getInt("intHPMax")+",\"iLvl\":"+os.getInt("intLevel")+",\"MonMapID\":"+(o+1)+",\"MonID\":\""+monnumbs[o]+"\",\"intMP\":"+this.monsterMP[o]+",\"wDPS\":"+os.getInt("iDPS")+",\"intState\":"+this.monsterState[o]+",\"intMPMax\":"+os.getInt("intMPMax")+",\"bRed\":\"0\",");
                    if(os.getString("react").contains("0")){
                        sendPack2.addString("\"react\":["+os.getString("react")+"],");
                    }
                    sendPack2.addString("\"intHP\":"+this.monsterHP[o]+"}");
                }
                os.close();
            }
        } catch (Exception e) {
            Main.server.debug("Exception in get monster: "+e.getMessage());
        }
        return sendPack2.getPacket();
    }

    protected String getPlayerInfo(int slot)
    {
        Packet pInfo = new Packet();
        pInfo.addString("{\"uoName\":\""+roomSlot[slot]+"\"");
        pInfo.addString(",\"strUsername\":\""+roomSlot[slot]+"\"");
        pInfo.addString(",\"strFrame\":\""+frame[slot]+"\"");
        pInfo.addString(",\"strPad\":\""+pad[slot]+"\"");
        pInfo.addString(getPlayerSQL(slot));
        pInfo.addString(",\"tx\":"+ty[slot]);
        pInfo.addString(",\"ty\":"+ty[slot]);
        pInfo.addString(",\"afk\":"+afk[slot]);
        pInfo.addString("}");
        //pInfo.addString(",\"vip:"+Main.server.gameServer.getVIP(Main.server.gameServer.getPlayerID(roomSlot[slot])));
        //pInfo.addString("\",\"fd:"+Main.server.gameServer.getFD(Main.server.gameServer.getPlayerID(roomSlot[slot]))+"\"]");
        return pInfo.getPacket();
    }

    protected int getPlayerSlot(String charname)
    {
        for (int i = 0; i < roomSlot.length; i++) {
            if (roomSlot[i].equals(charname)) {
                return i;
            }
        }
        return -1;
    }

    
    protected String getPlayerSQL(int slot)
    {
        Packet sqlInfo = new Packet();
        try {
            ResultSet rs = Main.sql.doquery("SELECT * FROM wqw_users WHERE username='"+this.roomSlot[slot]+"'");
            
            int i = Main.server.gameServer.getPlayerID(this.roomSlot[slot]);
            if (rs.next()) {
                sqlInfo.addString(",\"intState\":1");
                sqlInfo.addString(",\"intLevel\":"+rs.getInt("level"));
                sqlInfo.addString(",\"entID\":"+i);
                sqlInfo.addString(",\"entType\":\"p\"");
                sqlInfo.addString(",\"showHelm\":true");
                sqlInfo.addString(",\"showCloak\":true");
                if(this.roomName.equals("darkoviapvp") || this.roomName.equals("bludrutbrawl")){
                    sqlInfo.addString(",\"pvpTeam\":"+Main.server.gameServer.pvpteam[i]);
                }
                sqlInfo.addString(",\"intHP\":"+Main.server.gameServer.hp[i]);
                sqlInfo.addString(",\"intMP\":"+Main.server.gameServer.mp[i]);
                sqlInfo.addString(",\"intHPMax\":"+Main.server.gameServer.hpmax[i]);
                sqlInfo.addString(",\"intMPMax\":"+Main.server.gameServer.mpmax[i]);
            }
            rs.close();
        } catch (Exception e) {
            Main.server.debug("Exception in room init: "+e.getMessage());
        }
        return sqlInfo.getPacket();
    }

    protected void respawnMonster(int monsterid, int monstertype)
    {
        respawnTimer respawn = new respawnTimer();
        respawn.main(monsterid, monstertype, this.roomType, this.roomNumb);
    }

    protected void respawnMonsterDo(int monsterid)
    {
        try {
            this.monsterHP[monsterid] = this.monsterHPMax[monsterid];
            this.monsterMP[monsterid] = this.monsterMPMax[monsterid];
            this.monsterLevel[monsterid] = this.monsterLevel[monsterid];
            this.monsterState[monsterid] = 1;
            int[] temproom = new int[2];
            temproom[0] = this.roomType;
            temproom[1] = this.roomNumb;
            Packet sendPack = new Packet();
            sendPack.addString("{\"t\":\"xt\",\"b\":{\"r\":-1,\"o\":{\"id\":"+(monsterid+1)+",\"cmd\":\"mtls\",\"o\":{\"intMP\":"+this.monsterMP[monsterid]+",\"intState\":1,\"intHP\":"+this.monsterHP[monsterid]+"}}}}");
            Main.server.gameServer.writeOtherMapPacket(temproom, sendPack, true);
            sendPack.clean();
            sendPack.addString("%xt%respawnMon%-1%"+(monsterid+1)+"%");
            Main.server.gameServer.writeOtherMapPacket(temproom, sendPack, true);
        } catch (Exception e) {
            Main.server.debug("Error in respawning monster: " + e.getMessage());
            respawnMonsterDo(monsterid);
        }
    }

    protected void addPlayer(String charname, PrintWriter socket)
    {
        for (int i = 0; i < roomSlot.length; i++) {
            if (roomSlot[i].equals("") || roomSlot[i] == null) {
                this.roomSlot[i] = charname;
                this.playerSocket[i] = socket;
                this.users++;
                break;
            }
        }
    }

    protected void removePlayer(String charname)
    {
        int ID = getPlayerSlot(charname);
        this.roomSlot[ID] = "";
        this.playerSocket[ID] = null;
        this.pad[ID] = "Spawn";
        this.frame[ID] = "Enter";
        this.afk[ID] = false;
        this.tx[ID] = 0;
        this.ty[ID] = 0;
        this.users--;
    }

    @Override
    protected void finalize()
    {
        for (int e = 0; e < roomSlot.length; e++) {
            removePlayer(this.roomSlot[e]);
        }
    }
}
