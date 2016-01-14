package WQWServer;

import java.io.*;
import java.net.*;
import java.sql.*;
import java.util.*;
import java.util.Timer.*;
import java.util.TimerTask.*;

public class ServerConnection extends Thread {
    protected Socket socket;
    protected BufferedReader socketIn;
    protected PrintWriter socketOut;
    protected Server server;
    protected GameServer gameServer;
    protected Functions functions;
    protected String account;
    protected String charname;
    protected int accountid;
    protected int userid;
    protected String ip;
    protected int port;
    protected boolean hasFinalized;
    protected Room playerRoom;
    protected int playerSlot;
    protected int partyID;
    protected int monfighting=0;
    protected int playerlevel;
    protected String gender;
    protected int weaponlevel;
    protected boolean fighting;
    protected boolean onpvp=false;
    protected int monkilled;
    protected int packetsend;
    protected int aacd;
    protected int userrank;
    protected String[] anim=new String[5];
    protected String[] str1=new String[5];
    protected String[] auras = new String[5];
    protected String[] skillfx = new String[5];
    protected String[] friends = new String[20];
    protected String[] drops;
    protected int[] droppercent;
    protected int[] mpcost = new int[5];
    protected int classid;
    protected int[] questsaccepted = new int[20];
    pingTimer ping = new pingTimer();
    Random generator = new Random();

    /**
     * Creates a new instance of RelayServerConnection.
     */
    public ServerConnection(Socket socket, Server server, GameServer _gameServer) {
        this.socket = socket;
        this.server = server;
        this.ip = Main.getip(socket);
        this.port = socket.getPort();
        this.gameServer = _gameServer;
        this.start();
    }

    /**
     * Roots a debug message to the main application.
     */

    protected void debug(String msg) {
        Main.debug("User Connection ("+this.port+") - ", msg);
    }
    protected void write(String label,String msg) {
        Main.addMessage(label + ": ", msg);
    }


    public SocketAddress getRemoteAddress() {
        return this.socket.getRemoteSocketAddress();
    }

    protected String getcmd(String packet)
    {
        if (packet.startsWith("<")) {
            int endArrow = packet.indexOf(">");
            int endSlash = packet.indexOf("/>");
            if (endSlash < 0) {
                endSlash = endArrow + 1;
            }
            if (endSlash < endArrow) {
                return packet.substring(1,endSlash);
            } else {
                return packet.substring(1,endArrow);
            }
        } else if (packet.startsWith("%")) {
            String packet_handled[] = packet.split("%");
            return packet_handled[3];
        }
        return "Error";
    }

    protected void parseCMD(String cmd, String packet)
    {
        try{
            this.packetsend += 1;
            if (this.packetsend > 300) {
                gameServer.kickPlayer(this.account, "the server automatically kicked out due to packet spamming");
            }
            Packet recvPack = new Packet();
            recvPack.setPacket(packet);
            if (cmd.equals("addFriend")) {
                /* Accept friend request */
                recvPack.removeHeader();
                String packet_handled[] = recvPack.getPacket().split("%");
                addFriend(this.account, packet_handled[2], true);
            } else if (cmd.equals("afk")) {
                /* Away from keyboard */
                recvPack.removeHeader();
                String packet_handled[] = recvPack.getPacket().split("%");
                setAFK(Boolean.parseBoolean(packet_handled[2]));
            } else if (cmd.equals("bankFromInv")) {
                /* Take from bank */
                recvPack.removeHeader();
                String packet_handled[] = recvPack.getPacket().split("%");
                bankFromInv(Integer.parseInt(packet_handled[2]),Integer.parseInt(packet_handled[3]));
            } else if (cmd.equals("bankToInv")) {
                /* Take from bank */
                recvPack.removeHeader();
                String packet_handled[] = recvPack.getPacket().split("%");
                bankToInv(Integer.parseInt(packet_handled[2]),Integer.parseInt(packet_handled[3]));
            } else if (cmd.equals("buyBagSlots")) {
                /* Buy bag slots */
                recvPack.removeHeader();
                String packet_handled[] = recvPack.getPacket().split("%");
                buyBagSlots(Integer.parseInt(packet_handled[2]));
            } else if (cmd.equals("buyBankSlots")) {
                /* Buy bank slots */
                recvPack.removeHeader();
                String packet_handled[] = recvPack.getPacket().split("%");
                buyBankSlots(Integer.parseInt(packet_handled[2]));
            } else if (cmd.equals("buyHouseSlots")) {
                /* Buy house slots */
                recvPack.removeHeader();
                String packet_handled[] = recvPack.getPacket().split("%");
                buyHouseSlots(Integer.parseInt(packet_handled[2]));
            } else if (cmd.equals("buyItem")) {
                /* Buy item */
                recvPack.removeHeader();
                String packet_handled[] = recvPack.getPacket().split("%");
                buyItem(Integer.parseInt(packet_handled[2]),Integer.parseInt(packet_handled[3]));
            } else if (cmd.equals("cc")) {
                /* Canned chat */
                recvPack.removeHeader();
                String packet_handled[] = recvPack.getPacket().split("%");
                cannedChat(packet_handled[2]);
            } else if (cmd.equals("cmd")) {
                /* Switch on the cmd command */
                recvPack.removeHeader();
                String packet_handled[] = recvPack.getPacket().split("%");
                String cmdSwitch = packet_handled[2];
                if (cmdSwitch.equals("tfer")) {
                    /* Joining a new room */
                    try {
                        if (packet_handled[4].indexOf("-") > 0) {
                            String room[] = packet_handled[4].split("-");
                            joinRoom(room[0], Integer.parseInt(room[1]), "Enter", "Spawn");
                        } else {
                            if (packet_handled.length > 6) {
                                joinRoom(packet_handled[4], -1, packet_handled[5], packet_handled[6]);
                            } else {
                                joinRoom(packet_handled[4], -1, "Enter", "Spawn");
                            }
                        }
                    } catch(Exception e){
                        serverMsg("Error in joining room (Known Bug) please try joining another room and try again.","warning",false,false);
                    }
                } else if (cmdSwitch.equals("goto")) {
                    /* Goto a player */
                    Packet sendPack = new Packet();
                    debug(packet_handled[3]);
                    if (gameServer.getPlayerID(packet_handled[3]) != -1) {
                        int room[] = gameServer.getPlayerRoom(packet_handled[3]);
                        String roomname = gameServer.getRoomName(room[0]);
                        String[] frames = gameServer.getFramePad(packet_handled[3]);
                        if (roomname.equals("null") == false) {
                            joinRoom(roomname, room[1], frames[0], frames[1]);
                        }
                    } else {
                        sendPack.addString("%xt%warning%-1%User '" + packet_handled[3] + "' could not be found.%");
                        send(sendPack, true);
                    }

                } else if (cmdSwitch.equals("house")) {
                    
                } else if (cmdSwitch.equals("ignoreList")) {

                } else if (cmdSwitch.equals("uopref")) {
                    changeUserSettings(Boolean.parseBoolean(packet_handled[4]), packet_handled[3]);
                } else {
                    userCommand(packet_handled[2]+" "+packet_handled[3]);
                }
            }  else if (cmd.equals("declineFriend")) {
                /* Decline friend request */
                recvPack.removeHeader();
                String packet_handled[] = recvPack.getPacket().split("%");
                requestFriend(packet_handled[2]);
            } else if (cmd.equals("deleteFriend")) {
                /* Delete friend */
                recvPack.removeHeader();
                String packet_handled[] = recvPack.getPacket().split("%");
                deleteFriend(this.account, packet_handled[3], true);
            } else if (cmd.equals("emotea")) {
                /* Emotes */
                recvPack.removeHeader();
                String packet_handled[] = recvPack.getPacket().split("%");
                emoteChat(packet_handled[2]);
            } else if (cmd.equals("equipItem")) {
                /* Equipping */
                recvPack.removeHeader();
                String packet_handled[] = recvPack.getPacket().split("%");
                equipItem(Integer.parseInt(packet_handled[2]), Integer.parseInt(packet_handled[1]));
            } else if (cmd.equals("firstJoin")) {
                sendLobby();
            } else if (cmd.equals("gar")) {
                /* Player attack */
                recvPack.removeHeader();
                String packet_handled[] = recvPack.getPacket().split("%");

                playerAttack(packet_handled[3], Integer.parseInt(packet_handled[2]));

                /*
                String monster[] = skill[1].split(":");
                playerAttack(skill[1],skill[0], Integer.parseInt(packet_handled[2]), monster[0], Integer.parseInt(monster[1]), this.weaponlevel);
                */
            } else if (cmd.equals("getQuests")) {
                /* Get Quests */
                recvPack.removeHeader();
                String packet_handled[] = recvPack.getPacket().split("%");
                String quests = "%";
                for (int i = 2; i < packet_handled.length-1; i++) {
                    quests += packet_handled[i] + "%";
                }
                getQuests(quests);
            } else if (cmd.equals("gp")) {
                /* Switch on the gp command */
                recvPack.removeHeader();
                String packet_handled[] = recvPack.getPacket().split("%");
                String gpSwitch = packet_handled[2];
                if (gpSwitch.equals("pi")) {
                    /* Inviting to a party */
                    partyInvite(packet_handled[3]);
                } else if (gpSwitch.equals("pd")) {
                    /* Declining an invitation */
                    partyDecline(Integer.parseInt(packet_handled[3]));
                } else if (gpSwitch.equals("pa")) {
                    /* Accepting an invitation */
                    partyAccept(Integer.parseInt(packet_handled[3]));
                } else if (gpSwitch.equals("pl")) {
                    /* Leaving a party */
                    partyLeave();
                } else if (gpSwitch.equals("pk")) {
                    /* Kick a player from the party */
                    partyKick(packet_handled[3]);
                } else if (gpSwitch.equals("pp")) {
                    /* Promote a player to leader of the party */
                    partyPromote(packet_handled[3]);
                } else if (gpSwitch.equals("ps")) {
                    /* Summon a player */
                    partySummon(packet_handled[3]);
                } else if (gpSwitch.equals("psd")) {
                    /* Decline a summon */
                    partySummonDecline(packet_handled[3]);
                } else if (gpSwitch.equals("psd")) {
                    /* Accept a summon */
                    partySummonAccept();
                } else {
                    debug("Unknown packet recieved (gp): "+gpSwitch);
                }
            } else if (cmd.equals("hi")) {
                /* Attack disconnect timer */
                playerTimerAttack();
            } else if (cmd.equals("loadBank")) {
                /* Load Bank */
                loadBank();
            } else if (cmd.equals("loadFriendsList")) {
                /* Load Friends List */
                //loadFriends();
            } else if (cmd.equals("loadHouseInventory")) {
                /* Load House Inventory */
                loadHouseInventory();
            } else if (cmd.equals("loadShop")) {
                /* Load Shop */
                recvPack.removeHeader();
                String packet_handled[] = recvPack.getPacket().split("%");
                loadShop(Integer.parseInt(packet_handled[2]));
            } else if (cmd.equals("loadEnhShop")) {
                /* Load Shop */
                recvPack.removeHeader();
                String packet_handled[] = recvPack.getPacket().split("%");
                loadEnhShop(Integer.parseInt(packet_handled[2]));
            } else if (cmd.equals("loadHairShop")) {
                /* Load Hair Shop */
                recvPack.removeHeader();
                String packet_handled[] = recvPack.getPacket().split("%");
                loadHairShop(Integer.parseInt(packet_handled[2]));
            } else if (cmd.equals("msg t='sys'")) {
                /* Switch on the system command */
                recvPack.removeHeader();
                String sysSwitch = recvPack.getXMLSingle("body action");
                if (sysSwitch.equals("verChk")) {
                    /* Sends the version */
                    sendVersion();
                } else if (sysSwitch.equals("login")) {
                    /* Logs the player in */
                    doLogin(recvPack.getCDATA(recvPack.getXML("nick")), recvPack.getCDATA(recvPack.getXML("pword")), true);
                } else {
                    debug("Unknown packet recieved (sys): "+cmd);
                }
            } else if (cmd.equals("message")) {
                /* Send chat data */
                recvPack.removeHeader();
                String packet_handled[] = recvPack.getPacket().split("%");
                if (packet_handled[2].startsWith("!")) {
                    userCommand(packet_handled[2].substring(1,packet_handled[2].length()));
                } else {
                    userChat(Integer.parseInt(packet_handled[1]),packet_handled[2],packet_handled[3]);
                }
            } else if (cmd.equals("me")) {
                /* Send chat data */
                recvPack.removeHeader();
                String packet_handled[] = recvPack.getPacket().split("%");
                if (packet_handled[2].startsWith("!")) {
                    userCommand(packet_handled[2].substring(1,packet_handled[2].length()));
                } else {
                    userChat(Integer.parseInt(packet_handled[1]),packet_handled[2],packet_handled[3]);
                }
            } else if (cmd.equals("moveToCell")) {
                recvPack.removeHeader();
                String packet_handled[] = recvPack.getPacket().split("%");
                moveToCell(packet_handled[2],packet_handled[3]);
            } else if (cmd.equals("mv")) {
                /* Send move data */
                recvPack.removeHeader();
                String packet_handled[] = recvPack.getPacket().split("%");
                userMove(Integer.parseInt(packet_handled[2]),Integer.parseInt(packet_handled[3]),Integer.parseInt(packet_handled[4]), false);
            } else if (cmd.equals("policy-file-request")) {
                /* Send the policy */
                sendPolicy();
            } else if (cmd.equals("resPlayerTimed")) {
                /* Respawn player */
                respawnPlayer();
            } else if (cmd.equals("restRequest")) {
                /* Rest player */
                restPlayer();
            } else if (cmd.equals("retrieveInventory")) {
                /* Get the inventory details */
                sendEnhancementDetails();
                loadBigInventory();
                //getInvent();
            } else if (cmd.equals("PVPQr")) {
                pvpQuery("new");
            } else if (cmd.equals("retrieveUserData")) {
                /* Send user data */
                recvPack.removeHeader();
                String packet_handled[] = recvPack.getPacket().split("%");
                retrieveUserData(Integer.parseInt(packet_handled[2]), true);
            } else if (cmd.equals("retrieveUserDatas")){
                recvPack.removeHeader();
                retrieveUserDatas(recvPack.getPacket());
            } else if (cmd.equals("requestFriend")) {
                /* Add friend request */
                recvPack.removeHeader();
                String packet_handled[] = recvPack.getPacket().split("%");
                requestFriend(packet_handled[2]);
            } else if (cmd.equals("aggroMon")){
                recvPack.removeHeader();
                String packet_handled[] = recvPack.getPacket().split("%");
                if(playerRoom.monsterState[Integer.parseInt(packet_handled[2])] != 0){
                    this.fighting= true;
                    Room room = this.playerRoom;
                    int slot = room.getPlayerSlot(this.account);
                    playerHitTimer(Integer.parseInt(packet_handled[2]), room.frame[slot]);
                }
            } else if (cmd.equals("removeItem")) {
                recvPack.removeHeader();
                String packet_handled[] = recvPack.getPacket().split("%");
                removeItem(Integer.parseInt(packet_handled[2]),Integer.parseInt(packet_handled[3]));
            } else if (cmd.equals("sellItem")) {
                /* Sell item */
                recvPack.removeHeader();
                String packet_handled[] = recvPack.getPacket().split("%");
                sellItem(Integer.parseInt(packet_handled[2]),Integer.parseInt(packet_handled[4]));
            } else if (cmd.equals("unequipItem")) {
                /* Unequipping */
                recvPack.removeHeader();
                String packet_handled[] = recvPack.getPacket().split("%");
                unequipItem(Integer.parseInt(packet_handled[2]), Integer.parseInt(packet_handled[1]));
            } else if (cmd.equals("changeColor")) {
                /* Change Hair/Color */
                recvPack.removeHeader();
                String packet_handled[] = recvPack.getPacket().split("%");
                changeColor(Integer.parseInt(packet_handled[2]), Integer.parseInt(packet_handled[3]),Integer.parseInt(packet_handled[4]),Integer.parseInt(packet_handled[5]));
            } else if (cmd.equals("changeArmorColor")) {
                /* Change Armor Color */
                recvPack.removeHeader();
                String packet_handled[] = recvPack.getPacket().split("%");
                changeArmorColor(Integer.parseInt(packet_handled[2]),Integer.parseInt(packet_handled[3]),Integer.parseInt(packet_handled[4]));
            } else if (cmd.equals("getDrop")) {
                /* Get Item Drop */
                recvPack.removeHeader();
                String packet_handled[] = recvPack.getPacket().split("%");
                getDrop(Integer.parseInt(packet_handled[2]));
            } else if (cmd.equals("enhanceItemShop")) {
                /* Load Shop */
                recvPack.removeHeader();
                String packet_handled[] = recvPack.getPacket().split("%");
                enhanceItem(Integer.parseInt(packet_handled[2]),Integer.parseInt(packet_handled[3]), false);
            } else if (cmd.equals("enhanceItemLocal")) {
                recvPack.removeHeader();
                String packet_handled[] = recvPack.getPacket().split("%");
                enhanceItem(Integer.parseInt(packet_handled[2]),Integer.parseInt(packet_handled[3]), true);
            } else if (cmd.equals("mtcid")){
                recvPack.removeHeader();
                String packet_handled[] = recvPack.getPacket().split("%");
                Packet move = new Packet();
                move.addString("%xt%mtcid%-1%"+packet_handled[2]+"%");
                send(move, true);

                /* Switch Move to Cell by ID for Bludrut Brawl */

                switch(Integer.parseInt(packet_handled[2])){
                    case 30:
                        moveToCell("Morale1C","Top");
                        break;
                    case 29:
                        moveToCell("Enter1","Left");
                        break;
                    case 28:
                        moveToCell("Captain1","Spawn");
                        break;
                    case 27:
                        moveToCell("Morale1B","Right");
                        break;
                    case 26:
                        moveToCell("Morale1C","Right");
                        break;
                    case 25:
                        moveToCell("Morale1C","Left");
                        break;
                    case 24:
                        moveToCell("Morale1A","Right");
                        break;
                    case 23:
                        moveToCell("Morale1B","Left");
                        break;
                    case 22:
                        moveToCell("Crosslower","Right");
                        break;
                    case 21:
                        moveToCell("Resource1A","Right");
                        break;
                    case 20:
                        moveToCell("Resource1B","Left");
                        break;
                    case 19:
                        moveToCell("Crossupper","Right");
                        break;
                    case 18:
                        moveToCell("Resource1A","Left");
                        break;
                    case 17:
                        moveToCell("Crosslower","Middle");
                        break;
                    case 16:
                        moveToCell("Resource0A","Right");
                        break;
                    case 15:
                        moveToCell("Morale1A","Left");
                        break;
                    case 14:
                        moveToCell("Crossupper","Bottom");
                        break;
                    case 13:
                        moveToCell("Morale0A","Right");
                        break;
                    case 12:
                        moveToCell("Crossupper","Right");
                        break;
                    case 11:
                        moveToCell("Resource0B","Right");
                        break;
                    case 10:
                        moveToCell("Resource0A","Left");
                        break;
                    case 9:
                        moveToCell("Crosslower","Left");
                        break;
                    case 8:
                        moveToCell("Morale0B","Right");
                        break;
                    case 7:
                        moveToCell("Morale0A","Left");
                        break;
                    case 6:
                        moveToCell("Morale0C","Right");
                        break;
                    case 5:
                        moveToCell("Morale0C","Left");
                        break;
                    case 4:
                        moveToCell("Morale0B","Left");
                        break;
                    case 3:
                        moveToCell("Captain0","Spawn");
                        break;
                    case 2:
                        moveToCell("Enter0","Right");
                        break;
                    case 1:
                        moveToCell("Morale0C","Top");
                        break;

                    /* Switch move to cell id's command for darkovia pvp brawl
                    case 16:
                        moveToCell("lBoss","Right");
                        break;
                    case 15:
                        moveToCell("Enter0","Top");
                        break;
                    case 14:
                        moveToCell("l1","Right");
                        break;
                    case 13:
                        moveToCell("lBoss","Left");
                        break;
                    case 12:
                        moveToCell("v1","Right");
                        break;
                    case 5:
                        moveToCell("l1","Left");
                        break;
                    case 4:
                        moveToCell("vBoss","Right");
                        break;
                    case 3:
                        moveToCell("v1","Left");
                        break;
                    case 2:
                        moveToCell("Enter1","Top");
                        break;
                    case 1:
                        moveToCell("vBoss","Left");
                        break;
                     */
                    default:
                        break;
                }
            } else if (cmd.equals("PVPIr")){
                recvPack.removeHeader();
                String packet_handled[] = recvPack.getPacket().split("%");
                if(!packet_handled[2].equals("0")){
                    pvpQuery("done");
                    joinRoom("bludrutbrawl",1,"Enter"+gameServer.pvpteam[this.accountid],"Spawn");
                } else {
                    pvpQuery("none");
                }
            } else if(cmd.equals("tryQuestComplete")){
                recvPack.removeHeader();
                String packet_handled[] = recvPack.getPacket().split("%");
                tryQuestComplete(Integer.parseInt(packet_handled[2]),Integer.parseInt(packet_handled[3]));
            } else if(cmd.equals("acceptQuest")){
                recvPack.removeHeader();
                String packet_handled[] = recvPack.getPacket().split("%");
                for (int i = 1; i < 20; i++) {
                    if (this.questsaccepted[i]==0) {
                        questsaccepted[i] = Integer.parseInt(packet_handled[2]);
                        debug("Quest ID: " + packet_handled[2] + " accepted.");
                        return;
                    }
                }
            } else if (cmd.equals("geia")) {
                //recvPack.removeHeader();
                //String packet_handled[] = recvPack.getPacket().split("%");
                sendPotionAnimation();
            } else if (cmd.equals("whisper")) {
                /* Change Color */
                recvPack.removeHeader();
                String packet_handled[] = recvPack.getPacket().split("%");
                whisperChat(packet_handled[2], packet_handled[3]);
            } else if (cmd.equals("Error")) {
                debug("Error in reading packet: "+packet);
            } else {
                debug("Unknown packet recieved: "+cmd);
            }
        } catch(Exception e){
            debug("Error in parsing CMD: "+e.getMessage()+", Cause: "+e.getCause()+", Packet: "+packet);
        }
    }

    /*
     * START COMMAND RESPONSES
     * The following section contains responses for the messages sent to the server by the client
     * Above and below this section are the core functions
     */
    protected void sendPotionAnimation() {
        Packet sendPack = new Packet();
        sendPack.addString("{\"t\":\"xt\",\"b\":{\"r\":-1,\"o\":{\"cmd\":\"seia\",\"o\":{\"nam\":\"Invigorating Nectar\",\"anim\":\"Salute\",\"mp\":0,\"desc\":\"Health potion effect\",\"auras\":[{\"nam\":\"Heal\",\"t\":\"s\",\"dur\":5,\"tgt\":\"f\"}],\"range\":808,\"fx\":\"w\",\"damage\":-8,\"dsrc\":\"SP1\",\"ref\":\"ph1\",\"tgt\":\"f\",\"typ\":\"m\",\"strl\":\"sp_eh1\",\"cd\":0},\"iRes\":1}}}");
        send(sendPack, true);
    }

    protected void tryQuestComplete(int questid, int citemid)
    {
        Packet sendPack = new Packet();
        debug("Completing Quest ID: " + questid);
        try {
            ResultSet rs = Main.sql.doquery("SELECT * FROM wqw_quests WHERE id="+questid);
            if (rs.next()) {
                String sName = rs.getString("sName");
                int iGold = rs.getInt("iGold");
                int iExp = rs.getInt("iExp");
                String rewards[] = rs.getString("oRewards").split(":");
                String t[] = rs.getString("turnin").split(",");
                int qty[] = new int[t.length];
                int itemid[] = new int[t.length];
                for(int b = 0; b < t.length; b++){
                    String xx[] = t[b].split(":");
                    qty[b] = Integer.parseInt(xx[1]);
                    itemid[b] = Integer.parseInt(xx[0]);
                }
                turnInItem(itemid, qty);
                sendPack.addString("{\"t\":\"xt\",\"b\":{\"r\":-1,\"o\":{\"cmd\":\"ccqr\",\"rewardObj\":{\"iCP\":0,\"intGold\":"+iGold+",\"intExp\":"+iExp+",\"typ\":\"q\"},\"bSuccess\":1,\"QuestID\":"+questid+",\"sName\":\""+sName+"\"}}}");
                send(sendPack, true);
                sendPack.clean();
                sendPack.addString(addRewards(iExp,iGold,0,0,"q",this.accountid));
                send(sendPack, true);


                for(int i = 0; i < rewards.length; i++){
                    ResultSet xs = Main.sql.doquery("SELECT * FROM wqw_items WHERE itemid="+Integer.parseInt(rewards[i])+" AND userid="+this.userid);
                    if(!xs.next() && citemid!=-1){
                        dropItem(citemid);
                    } else if(citemid==-1 && !xs.next()) {
                        dropItem(Integer.parseInt(rewards[i]));
                    }
                }
            }
            if(this.playerlevel>=500){
                levelUp();
            }
            for(int e =0; e < this.questsaccepted.length; e++){
                if(this.questsaccepted[e]==questid){
                    this.questsaccepted[e] =0;
                    debug("Quest has been completed ID: " + questid);
                    return;
                }
            }
        } catch (Exception e){
            debug("Error in completing quest: " + e.getMessage());
        }
    }

    protected void changeUserSettings(boolean set, String tobeset)
    {
        if(set){
            Main.sql.doupdate("UPDATE wqw_users SET "+tobeset+"=1 WHERE id="+this.userid);
        } else {
            Main.sql.doupdate("UPDATE wqw_users SET "+tobeset+"=0 WHERE id="+this.userid);
        }
        if(tobeset.equals("bGoto") && set){
               serverMsg("Accepting goto requests.","server",false,false);
        } else if (tobeset.equals("bGoto")&& !set) {
               serverMsg("Blocking goto requests.","warning",false,false);
        }
        if(tobeset.equals("bWhisper")){
               serverMsg("Accepting PMs.","server",false,false);
        } else if (tobeset.equals("bWhisper")&& !set) {
               serverMsg("Ignoring PMs.","warning",false,false);
        }
        if(tobeset.equals("bTT")&& set){
               serverMsg("Ability ToolTips will always show on mouseover.","server",false,false);
        } else if (tobeset.equals("bTT")&& !set) {
            serverMsg("Ability ToolTips will not show mouseover during combat.","warning",false,false);
        }
        if(tobeset.equals("bFriend")&& set){
            serverMsg("Accepting Friend requests.","server",false,false);
        } else if (tobeset.equals("bFriend")&& !set) {
             serverMsg("Ignoring Friend requests.","warning",false,false);
        }
        if(tobeset.equals("bParty")&& set){
              serverMsg("Accepting party invites.","server",false,false);
        } else if (tobeset.equals("bParty")&& !set) {
               serverMsg("Ignoring party invites.","warning",false,false);
        }
    }

    protected void addPvPTeamScore(int teamid, int score)
    {
        Packet sendPack = new Packet();
        if(teamid==0){
            gameServer.lscore+=score;
        } else {
            gameServer.vscore+=score;
        }
        if(gameServer.lscore == 1000 || gameServer.vscore == 1000){
            
        }
        sendPack.addString("{\"t\":\"xt\",\"b\":{\"r\":-1,\"o\":{\"cmd\":\"PVPS\",\"pvpScore\":[{\"v\":"+gameServer.lscore+",\"r\":0,\"m\":0,\"k\":0},{\"v\":"+gameServer.vscore+",\"r\":0,\"m\":0,\"k\":0}]}}}");
        gameServer.writeMapPacket(this.account, sendPack, true, false);
    }

    protected void pvpQuery(String warzone)
    {
        Packet sendPack = new Packet();
        if(warzone.equals("new")){
            sendPack.addString("%xt%server%-1%You joined the Warzone queue for Bludrut Brawl!%");
            send(sendPack, true);
            sendPack.clean();
            sendPack.addString("{\"t\":\"xt\",\"b\":{\"r\":-1,\"o\":{\"cmd\":\"PVPQ\",\"bitSuccess\":1,\"avgWait\":-1,\"warzone\":\"bludrutbrawl\"}}}");
            send(sendPack, true);
            newPvpWarzone();
            onpvp=true;
        } else if (warzone.equals("done")) {
            sendPack.addString("{\"t\":\"xt\",\"b\":{\"r\":-1,\"o\":{\"cmd\":\"PVPQ\",\"bitSuccess\":0}}}");
            send(sendPack, true);
        } else if (warzone.equals("none")) {
            sendPack.addString("%xt%server%-1%You have been removed from the Warzone's queue%");
            send(sendPack, true);
            sendPack.clean();
            sendPack.addString("{\"t\":\"xt\",\"b\":{\"r\":-1,\"o\":{\"cmd\":\"PVPQ\",\"bitSuccess\":0}}}");
            send(sendPack, true);
            onpvp=false;
        }
    }

    protected void newPvpWarzone()
    {
        final Packet sendPack = new Packet();
        Timer timer = new Timer();
        timer.schedule(new TimerTask()
        {
            @Override
            public void run() {
                sendPack.addString("%xt%server%-1%A new Warzone battle has started!%");
                send(sendPack, true);
                sendPack.clean();
                sendPack.addString("{\"t\":\"xt\",\"b\":{\"r\":-1,\"o\":{\"cmd\":\"PVPI\",\"warzone\":\"bludrutbrawl\"}}}");
                send(sendPack, true);
            }
        }, 3000);
    }

    protected void removeItem(int itemid, int deleteid)
    {
        try {
        Packet sendPack = new Packet();
        ResultSet rs = Main.sql.doquery("SELECT * FROM wqw_items WHERE id="+deleteid);
            if (rs.next()) {
                String sES = rs.getString("sES");
                int iQty = rs.getInt("iQty");
                if(sES.equals("None")){
                    Main.sql.doupdate("UPDATE wqw_items SET iQty=iQty-1 WHERE id="+deleteid);
                    if((iQty-1)==1){
                        Main.sql.doupdate("DELETE FROM wqw_items WHERE id="+deleteid);
                    }
                } else {
                    Main.sql.doupdate("DELETE FROM wqw_items WHERE id="+deleteid);
                }
                sendPack.addString("{\"t\":\"xt\",\"b\":{\"r\":-1,\"o\":{\"cmd\":\"removeItem\",\"CharItemID\":"+deleteid+"}}}");
            } else {
                sendPack.addString("{\"t\":\"xt\",\"b\":{\"r\":-1,\"o\":{\"cmd\":\"removeItem\",\"bitSuccess\":0,\"strMessage\":\"Item Does Not Exist\",\"CharItemID\":-1}}}");
            }
        rs.close();
        send(sendPack, true);
        } catch (Exception e){
             debug("Exception in deleting item: " + e.getMessage());
        }
    }

    protected void clearAuras()
    {
        Packet sendPack= new Packet();
        sendPack.addString("{\"t\":\"xt\",\"b\":{\"r\":-1,\"o\":{\"cmd\":\"clearAuras\"}}}");
        send(sendPack,true);
    }

    protected void updateClass()
    {
        try {
            int classXP = 0;
            Packet sendPack = new Packet();
            ResultSet rs = Main.sql.doquery("SELECT * FROM wqw_items WHERE userid="+this.userid+" AND sES='ar' AND equipped=1");
            if (rs.next()) {
                clearAuras();
                classXP = rs.getInt("classXP");
            }
            ResultSet rs2 = Main.sql.doquery("SELECT * FROM wqw_classes WHERE classid="+this.classid);
            if(rs2.next()){
                sendPack.addString("{\"t\":\"xt\",\"b\":{\"r\":-1,\"o\":{\"uid\":"+this.accountid+",\"sStats\":\""+rs2.getString("sStats")+"\",\"iCP\":"+classXP+",\"cmd\":\"updateClass\",\"sDesc\":\""+rs2.getString("sDesc")+"\",\"sClassCat\":\""+rs2.getString("sClassCat")+"\",\"aMRM\":["+rs2.getString("aMRM")+"],\"sClassName\":\""+rs2.getString("className")+"\"}}}");
                send(sendPack,true);
                sendPack.clean();
                sendPack.addString("{\"t\":\"xt\",\"b\":{\"r\":-1,\"o\":{\"uid\":"+this.accountid+",\"iCP\":"+classXP+",\"cmd\":\"updateClass\",\"sClassCat\":\""+rs2.getString("sClassCat")+"\",\"sClassName\":\""+rs2.getString("className")+"\"}}}");
                gameServer.writeMapPacket(this.account, sendPack, true, true);
            }
        }
        catch (Exception e){
            debug("Exception in update class: " + e.getMessage());
        }
    }

    protected void turnInItem(int[] itemid, int qty[])
    {
        Packet sendPack = new Packet();
        try{
            for(int i=0; i < itemid.length; i++){
                ResultSet rs = Main.sql.doquery("SELECT * FROM wqw_items WHERE itemid="+itemid[i]+" AND userid="+this.userid);
                if(rs.next()){
                    int ccqty = rs.getInt("iQty") - qty[i];
                    if(ccqty <=0){
                       Main.sql.doupdate("DELETE FROM wqw_items WHERE itemid="+itemid[i]+" AND userid="+this.userid);
                    } else {
                       Main.sql.doupdate("UPDATE wqw_items SET iQty="+ccqty+" WHERE itemid="+itemid[i]+" AND userid="+ this.userid);
                    }
                }
            }
            sendPack.addString("{\"t\":\"xt\",\"b\":{\"r\":-1,\"o\":{\"cmd\":\"turnIn\",\"sItems\":\"");
            for(int a=0; a<itemid.length; a++){
                if(a!=0){
                    sendPack.addString(",");
                }
                sendPack.addString(itemid[a]+":"+qty[a]);
            }
            sendPack.addString("\"}}}");
            send(sendPack, true);
        } catch (Exception e){
            debug("Error in turning in item:" + e.getMessage());
        }

    }

    protected void enhanceItem(int itemid, int enhanceid, boolean islocal)
    {
        try {
            Packet sendPack = new Packet();
            int EnhDPS=0;
            int iCost=0;
            int EnhRng=0;
            int EnhLvl=0;
            int EnhRty=0;
            int EnhID = 0;
            String EnhName="";
            sendPack.addString("{\"t\":\"xt\",\"b\":{\"r\":-1,\"o\":{");
            ResultSet is = Main.sql.doquery("SELECT * FROM wqw_equipment WHERE itemid="+enhanceid);
            if (is.next()) {
                EnhDPS=is.getInt("iDPS");
                iCost=is.getInt("iCost");
                EnhRng=is.getInt("iRng");
                EnhLvl=is.getInt("iLvl");
                EnhRty=is.getInt("iRty");
                EnhID = is.getInt("EnhID");
                EnhName=is.getString("sName");
            }
            is.close();
            if(!islocal){
                sendPack.addString("\"ItemID\":"+itemid+"," +
                       "\"cmd\":\"enhanceItemShop\",\"EnhID\":" +
                       ""+enhanceid+",\"EnhLvl\":"+EnhLvl+",\"EnhDPS\":"+EnhDPS+",\"bSuccess\":1,\"iCost\":"+iCost+"," +
                       "\"EnhRty\":"+EnhRty+",\"EnhRng\":"+EnhRng+",\"EnhName\":\""+EnhName+"\",\"EnhPID\":"+EnhID+"}}}");
                        Main.sql.doupdate("UPDATE wqw_users SET gold=gold-"+iCost+" WHERE id="+this.userid);
            } else {
                sendPack.addString("\"ItemID\":"+itemid+"," +
                       "\"cmd\":\"enhanceItemLocal\",\"EnhID\":" +
                       ""+enhanceid+",\"EnhLvl\":"+EnhLvl+",\"EnhDPS\":"+EnhDPS+",\"bSuccess\":1,\"iCost\":"+iCost+"," +
                       "\"EnhRty\":"+EnhRty+",\"EnhRng\":"+EnhRng+",\"EnhName\":\""+EnhName+"\",\"EnhPID\":"+EnhID+"}}}");
                ResultSet rs = Main.sql.doquery("SELECT * FROM wqw_items WHERE itemid="+enhanceid+" AND userid="+this.userid);
                if(rs.next()){
                    int eid[] = new int[1];
                    int qty[] = new int[1];
                    eid[0] = enhanceid;
                    qty[0] = 1;
                    turnInItem(eid,qty);
                }
            }
            Main.sql.doupdate("UPDATE wqw_items SET iLvl="+EnhLvl+",EnhID="+EnhID+" WHERE userid="+this.userid+" AND itemid="+itemid);
            send(sendPack, true);
            sendPack.clean();     
        } catch (Exception e) {
            debug("Exception in enhance item: "+e.getMessage()+", itemid: "+itemid+", enhanceid: "+enhanceid);
        }
    }

    protected void loadEnhShop(int shopid)
    {
        try {
            Packet sendPack = new Packet();
            ResultSet rs = Main.sql.doquery("SELECT * FROM wqw_enhshops WHERE shopid="+shopid);
            if (rs.next()) {
                sendPack.addString("{\"t\":\"xt\",\"b\":{\"r\":-1,\"o\":{\"cmd\":\"loadEnhShop\",\"items\":[");
                String[] items = rs.getString("items").split(",");
                int i = items.length;
                int e = 0;
                rs.close();
                while (e < i) {
                    if (e != 0) {
                        sendPack.addString(",");
                    }
                    ResultSet is = Main.sql.doquery("SELECT * FROM wqw_equipment WHERE itemID="+items[e]);
                    if (is.next()) {
                        sendPack.addString("{\"sIcon\":\""+is.getString("sIcon")+"\",\"ItemID\":\""+is.getInt("itemID")+"\",\"iLvl\":\""+is.getInt("iLvl")+"\",\"iEnh\":\""+is.getInt("iEnh")+"\",\"sElmt\":\""+is.getString("sElmt")+"\",\"bTemp\":\""+is.getInt("bTemp")+"\",\"sLink\":\""+is.getString("sLink")+"\",\"bStaff\":\""+is.getInt("bStaff")+"\",\"iRng\":\""+is.getInt("iRng")+"\",\"bCoins\":\""+is.getInt("bCoins")+"\",\"iDPS\":\""+is.getInt("iDPS")+"\",\"sES\":\""+is.getString("sES")+"\",\"iHrs\":\""+is.getInt("iHRS")+"\",\"sFile\":\""+is.getString("sFile")+"\",\"sType\":\""+is.getString("sType")+"\",\"sDesc\":\""+is.getString("sDesc")+"\",\"iStk\":\""+is.getInt("iStk")+"\",\"iEnhCost\":\""+is.getInt("iCost")+"\",\"bUpg\":\""+is.getInt("bUpg")+"\",\"iRty\":\""+is.getInt("iRty")+"\",\"sName\":\""+is.getString("sName")+"\",\"iQty\":\""+is.getInt("iQty")+"\",\"EnhID\":\""+is.getInt("EnhID")+"\"}");
                    }
                    is.close();
                    e++;
                }
                sendPack.addString("]}}}");
                send(sendPack, true);
            }
        } catch (Exception e) {
            debug("Exception in load enhancement shop: "+e.getMessage());
        }
    }

    protected void getDrop(int itemid)
    {
        try {
            Packet sendPack = new Packet();
            boolean doContinue = true;
            int adjustid = 0;
            String sES = "";
            String className="";
            int level = 1;
            int qty = 1;
            String isitem="";
            ResultSet rs = Main.sql.doquery("SELECT * FROM wqw_equipment WHERE itemID="+itemid);
            if (rs.next()) {
                level = rs.getInt("iLvl");
                sES = rs.getString("sES");
                className = rs.getString("sName");
                isitem = rs.getString("sType");
                qty = rs.getInt("iQty");

            } else {
                doContinue = false;
            }
            if (doContinue) {
                rs.close();
                if(sES.equals("ar")){
                    Main.sql.doupdate("INSERT INTO wqw_items (itemid, userid, sES, className, classXP , iLvl, EnhID) VALUES ("+itemid+", "+this.userid+", '"+sES+"', '"+className+"', '0', '0','-1')");
                } else if(isitem.equals("Item") || isitem.equals("Quest Item")){
                    ResultSet es = Main.sql.doquery("SELECT * FROM wqw_items WHERE itemid="+itemid+" AND userid="+this.userid);
                    if(es.next()){
                        Main.sql.doupdate("UPDATE wqw_items SET iQty=iQty+"+qty+" WHERE itemid="+itemid+" AND userid="+this.userid);
                    } else {
                        Main.sql.doupdate("INSERT INTO wqw_items (itemid, userid, sES, iLvl, EnhID) VALUES ("+itemid+", "+this.userid+", '"+sES+"', '"+level+"', '-1')");
                    }
                } else {
                    Main.sql.doupdate("INSERT INTO wqw_items (itemid, userid, sES, iLvl, EnhID) VALUES ("+itemid+", "+this.userid+", '"+sES+"', '0', '-1')");
                }
            }
            if (doContinue) {
                ResultSet es = Main.sql.doquery("SELECT * FROM wqw_items WHERE userid="+this.userid+" AND itemid="+itemid+" AND sES='"+sES+"'");
                if (es.next()) {
                    adjustid = es.getInt("id");
                }
                es.close();
                sendPack.addString("{\"t\":\"xt\",\"b\":{\"r\":-1,\"o\":{\"ItemID\":"+itemid+",\"cmd\":\"getDrop\"," +
                        "\"bBank\":false,\"bitSuccess\":1,\"CharItemID\":");
                sendPack.addInt(adjustid);
                sendPack.addString(",\"iQty\":"+qty+"}}}");
            }
            send(sendPack, true);
        } catch (Exception e) {
            debug("Exception in getDrop(): "+e.getMessage());
        }
    }
    protected void changeArmorColor(int base, int trim, int accessory){
        Main.sql.doupdate("UPDATE wqw_users SET cosColorBase="+base+" WHERE id="+this.userid);
        Main.sql.doupdate("UPDATE wqw_users SET cosColorAccessory="+accessory+" WHERE id="+this.userid);
        Main.sql.doupdate("UPDATE wqw_users SET cosColorTrim="+trim+" WHERE id="+this.userid);
    }
    protected void changeColor(int skincolor, int haircolor, int eyecolor, int hairid){
        debug("Changing hair to id: " + hairid);
        Main.sql.doupdate("UPDATE wqw_users SET plaColorSkin="+skincolor+" WHERE id="+this.userid);
        Main.sql.doupdate("UPDATE wqw_users SET plaColorHair="+haircolor+" WHERE id="+this.userid);
        Main.sql.doupdate("UPDATE wqw_users SET plaColorEyes="+eyecolor+" WHERE id="+this.userid);
        try{
            ResultSet rs = Main.sql.doquery("SELECT * FROM wqw_hairs WHERE hairID="+hairid);
            if (rs.next()) {
                String name = rs.getString("sName");
                String file = rs.getString("sFile");
                rs.close();
                Main.sql.doupdate("UPDATE wqw_users SET hairID="+hairid+" WHERE id="+this.userid);
                Main.sql.doupdate("UPDATE wqw_users SET hairName='"+name+"' WHERE id="+this.userid);
                Main.sql.doupdate("UPDATE wqw_users SET hairFile='"+file+"' WHERE id="+this.userid);
            }
            rs.close();
        } catch(Exception e) {
            debug("Error in changing hair style and colors: " + e.getMessage());
        }
    }
    protected void loadHairShop(int shopid)
    {
        try {
            Packet sendPack = new Packet();
            sendPack.addString("{\"t\":\"xt\",\"b\":{\"r\":-1,\"o\":{\"HairShopID\":\""+shopid+"\",\"cmd\":\"loadHairShop\",\"hair\":[");
            ResultSet rs = Main.sql.doquery("SELECT * FROM wqw_hairshop WHERE id="+shopid);
            if (rs.next()) {
                String[] items = rs.getString("hairs"+this.gender).split(",");
                int i = items.length;
                int e = 0;
                rs.close();
                while (e < i) {
                    if (e != 0) {
                        sendPack.addString(",");
                    }
                    ResultSet is = Main.sql.doquery("SELECT * FROM wqw_hairs WHERE hairID="+items[e]);
                    if (is.next()) {
                        sendPack.addString("{\"sFile\":\""+is.getString("sFile")+"\"" +
                                ",\"HairID\":\""+is.getInt("hairID")+"\"" +
                                ",\"sName\":\""+is.getString("sName")+"\"" +
                                ",\"sGen\":\""+is.getString("sGen")+"\"}");
                    }
                    is.close();
                    e++;
                }
            }
            sendPack.addString("]}}}");
            send(sendPack, true);
        } catch (Exception e) {
            debug("Exception in loadHairShop(): "+e.getMessage());
        }
    }

    protected void addFriend(String thischar, String otherchar, boolean repeat)
    {
        try {
            Packet sendPack = new Packet();
            String[] account2 = new String[1];
            account2[0] = ""+gameServer.userID[gameServer.getPlayerID(thischar)];

            ResultSet rs2 = Main.sql.doquery("SELECT COUNT(*) AS rowcount FROM wqw_friends WHERE userid="+account2[0]);

            ResultSetMetaData rsMetaData = rs2.getMetaData();

            int numberOfColumns = rsMetaData.getColumnCount();

            if (numberOfColumns == 0){
                Main.sql.doupdate("INSERT INTO wqw_friends (userid) VALUES ("+this.userid+")");
            }
            rs2.close();

            if (numberOfColumns < 10) {
                ResultSet rs = Main.sql.doquery("SELECT * FROM wqw_friends WHERE userid="+account2[0]);
                if (rs.next()) {
                    if (!rs.getString("friendid").equals("")) {
                        Main.sql.doupdate("UPDATE wqw_friends SET friendid=CONCAT(friendid, "+"',', "+gameServer.userID[gameServer.getPlayerID(otherchar)]+") WHERE userid="+account2[0]);
                        sendPack.addString("{\"t\":\"xt\",\"b\":{\"r\":-1,\"o\":{\"cmd\":\"addFriend\",\"friend\":");
                        rs.close();
                        ResultSet is = Main.sql.doquery("SELECT * FROM wqw_users WHERE id="+gameServer.userID[gameServer.getPlayerID(otherchar)]);
                        if (is.next()) {
                            sendPack.addString("{\"iLvl\":\""+is.getInt("level")+"\",\"ID\":\""+is.getInt("id")+"\",\"sName\":\""+is.getString("username")+"\",\"sServer\":\""+is.getString("curServer")+"\"}");
                        }
                        sendPack.addString("}}}");
                        gameServer.writePlayerPacket(thischar, sendPack, true);
                        sendPack.clean();
                        sendPack.addString("%xt%server%-1%"+otherchar+" added to your friends list.%");
                        gameServer.writePlayerPacket(thischar, sendPack, true);
                        is.close();
                        updateFriends();
                    } else if(rs.getString("friendid").indexOf(Integer.toString(gameServer.userID[gameServer.getPlayerID(otherchar)]))!=-1) {
                        sendPack.clean();
                        ResultSet is = Main.sql.doquery("SELECT * FROM wqw_users WHERE id="+gameServer.userID[gameServer.getPlayerID(otherchar)]);
                        if (is.next()) {
                            String ochar = is.getString("username");
                            sendPack.addString("%xt%server%-1%"+ochar+" is already added to your friends list.%");
                            gameServer.writePlayerPacket(thischar, sendPack, true);
                        }
                    } else {
                        Main.sql.doupdate("UPDATE wqw_friends SET friendid=CONCAT(friendid, "+gameServer.userID[gameServer.getPlayerID(otherchar)]+") WHERE userid="+account2[0]);
                        
                        rs.close();
                        ResultSet is = Main.sql.doquery("SELECT * FROM wqw_users WHERE id="+gameServer.userID[gameServer.getPlayerID(otherchar)]);
                        sendPack.addString("{\"t\":\"xt\",\"b\":{\"r\":-1,\"o\":{\"cmd\":\"addFriend\",\"friend\":");
                        if (is.next()) {
                            sendPack.addString("{\"iLvl\":\""+is.getInt("level")+"\",\"ID\":\""+is.getInt("id")+"\",\"sName\":\""+is.getString("username")+"\",\"sServer\":\""+is.getString("curServer")+"\"}");
                        }
                        sendPack.addString("}}}");
                        gameServer.writePlayerPacket(thischar, sendPack, true);
                        sendPack.clean();
                        sendPack.addString("%xt%server%-1%"+otherchar+" added to your friends list.%");
                        gameServer.writePlayerPacket(thischar, sendPack, true);
                        is.close();
                        updateFriends();
                    }
                }
            }
            if (repeat) {
                addFriend(otherchar, thischar, false);
            }
            rs2.close();
        } catch (Exception e) {
            debug("Exception in addFriend(): "+e.getMessage());
        }
    }

    protected void addGold(int gold)
    {
        Main.sql.doupdate("UPDATE wqw_users SET gold=gold+"+gold+" WHERE id="+this.userid);
    }

    protected int getXpToLevel(int playerlevel)
    {
        if (playerlevel < 3000) {
            int points = 0;
            for (int lvl=1; lvl <= playerlevel; lvl++)
            {
                points += Math.floor(lvl + 200 * Math.pow(2, lvl / 7.));
            }
            return (int) Math.floor(points / 4);
            //return (250*((playerlevel)/2)*((playerlevel/2)*(playerlevel+1)/playerlevel)*(playerlevel/2)+100);
        }
        return 0;
    }
    protected int getHPwithNecklace()
    {
        try{
             ResultSet it = Main.sql.doquery("SELECT * FROM wqw_items WHERE sES='am' AND equipped=1 AND userid="+this.userid);
             if(it.next()){
                 ResultSet ge = Main.sql.doquery("SELECT sLink FROM wqw_equipment WHERE itemID="+it.getString("itemid"));
                 if(ge.next()){
                     int newhp = gameServer.calculateHP(gameServer.level[accountid]) + Integer.parseInt(ge.getString("sLink"));
                     return newhp;
                 }
             }
        } catch (Exception e){

        }
        return gameServer.hpmax[this.accountid];
    }
   protected void levelUp(){
       Packet sendPack = new Packet();
       try{
            ResultSet rs = Main.sql.doquery("SELECT * FROM wqw_users WHERE id="+this.userid);
            if (rs.next()) {
                int xp = rs.getInt("xp");
                if (xp>=getXpToLevel(this.playerlevel)) {
                    Main.sql.doupdate("UPDATE wqw_users SET level=level+1, xp=0 WHERE id="+userid);
                    this.playerlevel += 1;
                    gameServer.mp[this.accountid] = gameServer.calculateMP(this.playerlevel);
                    gameServer.hp[this.accountid] = gameServer.calculateHP(this.playerlevel);
                    gameServer.hpmax[this.accountid] = gameServer.calculateHP(this.playerlevel);
                    gameServer.mpmax[this.accountid] = gameServer.calculateMP(this.playerlevel);
                    gameServer.level[this.accountid] = this.playerlevel;
                    sendPack.addString("{\"t\":\"xt\",\"b\":{\"r\":-1,\"o\":{\"cmd\":\"levelUp\",\"intExpToLevel\":\""+getXpToLevel(this.playerlevel)+"\",\"intLevel\":\""+this.playerlevel+"\"}}}");
                    send(sendPack, true);
                    sendPack.clean();
                    serverMsg(this.account+" is now level "+this.playerlevel+"!","server",false,true);
                    sendUotls(true,true,true,true,true);
                }
            }
            rs.close();
        }
        catch(Exception err) {
            debug("Error in level up: " + err.getMessage());
        }
       if(this.playerlevel==3000){
                  serverMsg("Parabens! "+this.account+" Voce chegou ao level maximo!","server",true,false);
        }
    }

    protected void changeLevel(int level)
    {
        Packet sendPack = new Packet();
        try {
            if(level <=3000){
                Main.sql.doupdate("UPDATE wqw_users SET level="+level+", xp=0 WHERE id="+this.userid);
                this.playerlevel = level;
                int newhp = getHPwithNecklace();
                gameServer.hp[this.accountid] = gameServer.calculateHP(this.playerlevel);
                gameServer.hpmax[this.accountid] = gameServer.calculateHP(this.playerlevel);
                if(onpvp){
                    gameServer.hp[this.accountid] = newhp;
                    gameServer.hpmax[this.accountid] = newhp;
                }
                gameServer.mp[this.accountid] = gameServer.calculateMP(level);
                gameServer.mpmax[this.accountid] = gameServer.calculateMP(level);
                gameServer.level[this.accountid] = level;
                sendPack.addString("{\"t\":\"xt\",\"b\":{\"r\":-1,\"o\":{\"cmd\":\"levelUp\",\"intExpToLevel\":\""+getXpToLevel(this.playerlevel)+"\",\"intLevel\":\""+this.playerlevel+"\"}}}");
                send(sendPack, true);
                sendPack.clean();
                sendUotls(true,true,true,true,true);
            } else {
                serverMsg("Maximum Level is 3000! You cannot change your level beyond that point.","warning",false,false);
            }
        } catch(Exception e) {
            debug("Error in changing level: " + e.getMessage());
        }
    }
    protected String addRewards(int xpreward, int goldreward, int cpreward, int monstertype, String type, int monsterid)
    {
        Packet sendPack2 = new Packet();
        try {
            int cp = gameServer.getClassPoints(this.userid);
            int rank = gameServer.getRankFromCP(cp);
            cpreward = (cp+cpreward);
            if(rank==-1){
                rank=10;
                cp=302500;
                cpreward=302500;
            }
            Main.sql.doupdate("UPDATE wqw_items SET classXP="+cpreward+" WHERE userid="+userid+" AND equipped=1 AND sES='ar'");
            Main.sql.doupdate("UPDATE wqw_users SET gold=gold+"+goldreward+", xp=xp+"+xpreward+" WHERE id="+userid);
            if (type.equals("m") && goldreward==50000){
                Main.sql.doupdate("UPDATE wqw_users SET coins=coins-"+goldreward+" WHERE id="+userid);
            }
            ResultSet rs = Main.sql.doquery("SELECT * FROM wqw_users WHERE id="+this.userid);
            if (rs.next()) {
                int gold = rs.getInt("gold");
                int xp = rs.getInt("xp");
                    sendPack2.addString("{\"t\":\"xt\",\"b\":{\"r\":-1,\"o\":{\"id\":\""+monsterid+"\",\"iCP\":"+cp+",\"cmd\":\"addGoldExp\",\"intGold\":"+gold+",\"intExp\":"+xp+",\"typ\":\""+type+"\"}}}");
                    if(this.userrank!=rank){
                        loadSkills(this.classid);
                    }
            }
            rs.close();
        } catch (Exception e) {
            debug("Exception in addRewards(): "+e.getMessage());
        }
        return sendPack2.getPacket();
    }

    protected String addMonsterRewards(int monstertype, String type, int monsterid)
    {
        try {
            Random r = new Random();
            int randint = r.nextInt(20);
            int goldreward = 0;
            int xpreward = 0;
            int cpreward = 0;
            ResultSet rs = Main.sql.doquery("SELECT * FROM wqw_settings LIMIT 1");
            if (rs.next()) {
                gameServer.xprate = rs.getInt("xprate");
                gameServer.goldrate = rs.getInt("goldrate");
            }
            rs.close();
            if (type.equals("p")) {
                goldreward = (gameServer.level[monsterid]*50)*gameServer.goldrate + randint*2;
                xpreward = (gameServer.level[monsterid]*35)*gameServer.xprate + randint*4;
                cpreward = xpreward/2 + randint;
            } else {
                ResultSet is = Main.sql.doquery("SELECT * FROM wqw_monsters WHERE MonID="+monstertype);
                if (is.next()) {
                    goldreward = is.getInt("intGold")*gameServer.goldrate;
                    xpreward = is.getInt("intExp")*gameServer.xprate*is.getInt("intLevel");
                    if(xpreward !=0){
                        cpreward =(int) (xpreward/1.5 + randint);
                    } else {
                        cpreward=0;
                    }
                    
                }
            }
            if(this.playerlevel==3000){
                return addRewards(0, goldreward, cpreward, monstertype, type, monsterid);
            }else {
                return addRewards(xpreward, goldreward, cpreward, monstertype, type, monsterid);
            }
        } catch (Exception e) {
            debug("Exception in addMonsterRewards(): "+e.getMessage());
        }
        return "";
    }

    protected void addQuestItem(int itemid)
    {
        Packet sendPack = new Packet();

        sendPack.addString("{\"t\":\"xt\",\"b\":{\"r\":-1,\"o\":{\"cmd\":\"addItems\",\"items\":{\""+itemid+"\":{\"ItemID\":\""+itemid+"\",\"iQty\":1}}}}}");
        send(sendPack, true);
    }

    protected void bankFromInv(int itemid, int adjustid)
    {
        try {
            Packet sendPack = new Packet();
            ResultSet rs = Main.sql.doquery("SELECT * FROM wqw_items WHERE userid="+this.userid+" AND bBank=1");
            int i = 0;
            while (rs.next()) {
                i++;
            }
            rs.close();
            ResultSet is = Main.sql.doquery("SELECT * FROM wqw_users WHERE id="+this.userid);
            if (is.next()) {
                if (is.getInt("slotBank") <= i) {
                    sendPack.addString("%xt%warning%-1%You have the maximum items you can in your bank.%");
                } else {
                    Main.sql.doupdate("UPDATE wqw_items SET bBank=1 WHERE userid="+this.userid+" AND bBank=0 AND id="+adjustid);
                    sendPack.addString("{\"t\":\"xt\",\"b\":{\"r\":-1,\"o\":{\"ItemID\":"+itemid+",\"cmd\":\"bankFromInv\"}}}");
                }
            }
            is.close();
            send(sendPack, true);
        } catch (Exception e) {
            debug("Exception in bankFromInv(): "+e.getMessage());
        }
    }

    protected void bankToInv(int itemid, int adjustid)
    {
        try {
            Packet sendPack = new Packet();
            ResultSet rs = Main.sql.doquery("SELECT * FROM wqw_items WHERE userid="+this.userid+" AND bBank=0");
            int i = 0;
            while (rs.next()) {
                i++;
            }
            rs.close();
            ResultSet is = Main.sql.doquery("SELECT * FROM wqw_users WHERE id="+this.userid);
            if (is.next()) {
                if (is.getInt("slotBag") <= i) {
                    sendPack.addString("%xt%warning%-1%You have the maximum items you can in your inventory.%");
                } else {
                    Main.sql.doupdate("UPDATE wqw_items SET bBank=0 WHERE userid="+this.userid+" AND bBank=1 AND id="+adjustid);
                    sendPack.addString("{\"t\":\"xt\",\"b\":{\"r\":-1,\"o\":{\"ItemID\":"+itemid+",\"cmd\":\"bankToInv\"}}}");
                }
            }
            is.close();
            send(sendPack, true);
        } catch (Exception e) {
            debug("Exception in bankToInv(): "+e.getMessage());
        }
    }

    protected void buyBankSlots(int amount)
    {
        try {
            Packet sendPack = new Packet();
            boolean doContinue = true;
            int coins = 0;
            int curSlots = 0;
            ResultSet is = Main.sql.doquery("SELECT * FROM wqw_users WHERE id="+this.userid);
            if (is.next()) {
                coins = is.getInt("coins");
                curSlots = is.getInt("slotBank");
            }
            if (curSlots > 59) {
                sendPack.addString("{\"t\":\"xt\",\"b\":{\"r\":-1,\"o\":{\"cmd\":\"buyBankSlots\",\"bitSuccess\":0,\"strMessage\":\"You have the maximum bank slots avaliable.\",\"iSlots\":0}}}");
                doContinue = false;
            }
            if (doContinue) {
                if (coins < (200*amount)) {
                    sendPack.addString("{\"t\":\"xt\",\"b\":{\"r\":-1,\"o\":{\"cmd\":\"buyBankSlots\",\"bitSuccess\":0,\"strMessage\":\"You do not have enough coins to buy that many slots.\",\"iSlots\":0}}}");
                } else {
                    Main.sql.doupdate("UPDATE wqw_users SET coins=coins-"+(200*amount)+", slotBank=slotBank+"+amount+" WHERE id="+this.userid);
                    sendPack.addString("{\"t\":\"xt\",\"b\":{\"r\":-1,\"o\":{\"cmd\":\"buyBankSlots\",\"bitSuccess\":1,\"iSlots\":"+amount+"}}}");
                }
            }
            is.close();
            send(sendPack, true);
        } catch (Exception e) {
            debug("Exception in buyBankSlots(): "+e.getMessage());
        }
    }

    protected void buyBagSlots(int amount)
    {
        try {
            Packet sendPack = new Packet();
            boolean doContinue = true;
            int coins = 0;
            int curSlots = 0;
            ResultSet is = Main.sql.doquery("SELECT * FROM wqw_users WHERE id="+this.userid);
            if (is.next()) {
                coins = is.getInt("coins");
                curSlots = is.getInt("slotBag");
            }
            if (curSlots > 64) {
                sendPack.addString("{\"t\":\"xt\",\"b\":{\"r\":-1,\"o\":{\"cmd\":\"buyBagSlots\",\"bitSuccess\":0,\"strMessage\":\"You have the maximum bag slots avaliable.\",\"iSlots\":0}}}");
                doContinue = false;
            }
            if (doContinue) {
                if (coins < (200*amount)) {
                    sendPack.addString("{\"t\":\"xt\",\"b\":{\"r\":-1,\"o\":{\"cmd\":\"buyBagSlots\",\"bitSuccess\":0,\"strMessage\":\"You do not have enough coins to buy that many slots.\",\"iSlots\":0}}}");
                } else {
                    Main.sql.doupdate("UPDATE wqw_users SET coins=coins-"+(200*amount)+", slotBag=slotBag+"+amount+" WHERE id="+this.userid);
                    sendPack.addString("{\"t\":\"xt\",\"b\":{\"r\":-1,\"o\":{\"cmd\":\"buyBagSlots\",\"bitSuccess\":1,\"iSlots\":"+amount+"}}}");
                }
            }
            is.close();
            send(sendPack, true);
        } catch (Exception e) {
            debug("Exception in buyBagSlots(): "+e.getMessage());
        }
    }

    protected void buyHouseSlots(int amount)
    {
        try {
            Packet sendPack = new Packet();
            boolean doContinue = true;
            int coins = 0;
            int curSlots = 0;
            ResultSet is = Main.sql.doquery("SELECT * FROM wqw_users WHERE id="+this.userid);
            if (is.next()) {
                coins = is.getInt("coins");
                curSlots = is.getInt("slotHouse");
            }
            if (curSlots > 29) {
                sendPack.addString("{\"t\":\"xt\",\"b\":{\"r\":-1,\"o\":{\"cmd\":\"buyHouseSlots\",\"bitSuccess\":0,\"strMessage\":\"You have the maximum house slots avaliable.\",\"iSlots\":0}}}");
                doContinue = false;
            }
            if (doContinue) {
                if (coins < (200*amount)) {
                    sendPack.addString("{\"t\":\"xt\",\"b\":{\"r\":-1,\"o\":{\"cmd\":\"buyHouseSlots\",\"bitSuccess\":0,\"strMessage\":\"You do not have enough coins to buy that many slots.\",\"iSlots\":0}}}");
                } else {
                    Main.sql.doupdate("UPDATE wqw_users SET coins=coins-"+(200*amount)+", slotHouse=slotHouse+"+amount+" WHERE id="+this.userid);
                    sendPack.addString("{\"t\":\"xt\",\"b\":{\"r\":-1,\"o\":{\"cmd\":\"buyHouseSlots\",\"bitSuccess\":1,\"iSlots\":"+amount+"}}}");
                }
            }
            is.close();
            send(sendPack, true);
        } catch (Exception e) {
            debug("Exception in buyHouseSlots(): "+e.getMessage());
        }
    }

    protected void buyItem(int itemid, int shopid)
    {
        try {
            Packet sendPack = new Packet();
            boolean doContinue = true;
            int gold = 0;
            int buyprice = 1;
            int adjustid = 0;
            int level = 1;
            int iscoins=0;
            int qty = 1;
            String isitem="";
            String className="";
            String sES = "";
            int EnhID = 0;
            ResultSet rs = Main.sql.doquery("SELECT * FROM wqw_equipment WHERE itemID="+itemid);
            if (rs.next()) {
                buyprice = rs.getInt("iCost");
                level = rs.getInt("iLvl");
                sES = rs.getString("sES");
                className = rs.getString("sName");
                iscoins = rs.getInt("bCoins");
                isitem = rs.getString("sType");
                qty = rs.getInt("iQty");
                EnhID = rs.getInt("EnhID");
            } else {
                sendPack.addString("{\"t\":\"xt\",\"b\":{\"r\":-1,\"o\":{\"cmd\":\"buyItem\",\"bitSuccess\":0,\"strMessage\":\"Item Does Not Exist\",\"CharItemID\":-1}}}");
                doContinue = false;
            }
            if(level>this.playerlevel){
                sendPack.addString("{\"t\":\"xt\",\"b\":{\"r\":-1,\"o\":{\"cmd\":\"buyItem\",\"bitSuccess\":0,\"strMessage\":\"Level requirement not met!\",\"CharItemID\":-1}}}");
                doContinue = false;
            }
            if (doContinue) {
                rs.close();
                ResultSet is = Main.sql.doquery("SELECT * FROM wqw_users WHERE id="+this.userid);
                if (is.next()) {
                    gold = is.getInt("gold");
                }
                is.close();
                if ((gold - buyprice) >= 0) {
                    if(sES.equals("ar")){
                        Main.sql.doupdate("INSERT INTO wqw_items (itemid, userid, sES, className, classXP , iLvl, EnhID) VALUES ("+itemid+", "+this.userid+", '"+sES+"', '"+className+"', '0', '"+level+"','"+EnhID+"')");
                    } else if(isitem.equals("Item") || isitem.equals("Quest Item")){
                        ResultSet es = Main.sql.doquery("SELECT * FROM wqw_items WHERE itemid="+itemid+" AND userid="+this.userid);
                        if(es.next()){
                            Main.sql.doupdate("UPDATE wqw_items SET iQty=iQty+"+qty+" WHERE itemid="+itemid+" AND userid="+this.userid);
                        } else {
                            Main.sql.doupdate("INSERT INTO wqw_items (itemid, userid, sES, iLvl, EnhID) VALUES ("+itemid+", "+this.userid+", '"+sES+"', '"+level+"', '"+EnhID+"')");
                        }
                    } else {
                        Main.sql.doupdate("INSERT INTO wqw_items (itemid, userid, sES, iLvl, EnhID) VALUES ("+itemid+", "+this.userid+", '"+sES+"', '"+level+"', '"+EnhID+"')");
                    }
                    if(iscoins!=1){
                        Main.sql.doupdate("UPDATE wqw_users SET gold="+(gold-buyprice)+" WHERE id="+this.userid);
                    } else {
                        Main.sql.doupdate("UPDATE wqw_users SET coins=coins-"+buyprice+" WHERE id="+this.userid);
                    }
                } else {
                    sendPack.addString("{\"t\":\"xt\",\"b\":{\"r\":-1,\"o\":{\"cmd\":\"buyItem\",\"bitSuccess\":0,\"strMessage\":\"Not Enough Gold\",\"CharItemID\":-1}}}");
                    doContinue = false;
                }
            }
            if (doContinue) {
                ResultSet es = Main.sql.doquery("SELECT * FROM wqw_items WHERE userid="+this.userid+" AND itemid="+itemid+" AND sES='"+sES+"'");
                if (es.next()) {
                    adjustid = es.getInt("id");
                }
                es.close();
                sendPack.addString("{\"t\":\"xt\",\"b\":{\"r\":-1,\"o\":{\"cmd\":\"buyItem\",\"bitSuccess\":1,\"CharItemID\":");
                sendPack.addInt(adjustid);
                sendPack.addString("}}}");
            }
            send(sendPack, true);
        } catch (Exception e) {
            debug("Exception in buyItem(): "+e.getMessage());
        }
    }

    protected void cannedChat(String chat)
    {
        Packet sendPack = new Packet();
        sendPack.addString("%xt%cc%-1%");
        sendPack.addString(chat);
        sendPack.addString("%"+this.account+"%");
        gameServer.writeMapPacket(this.account,sendPack,true,false);
        write("Canned Chat - ", this.account+": "+chat);
    }

    protected void declineFriend(String otherchar)
    {
        Packet sendPack = new Packet();
        sendPack.addString("%xt%server%-1%You declined the friend request.%");
        send(sendPack, true);
        sendPack.clean();
        sendPack.addString("%xt%server%-1%"+this.account+" declined your friend request.%");
        gameServer.writePlayerPacket(otherchar, sendPack, true);
    }

    protected void deleteFriend(String thischar, String otherchar, boolean repeat)
    {
        try {
            Packet sendPack = new Packet();
            String account2 = ""+gameServer.userID[gameServer.getPlayerID(thischar)];
            String account3 = ""+gameServer.userID[gameServer.getPlayerID(otherchar)];
            String[] temp;
            String newFriend = "";
            ResultSet rs = Main.sql.doquery("SELECT * FROM wqw_friends WHERE userid="+account2);
            if (rs.next()) {
                temp = rs.getString("friendid").split(",");
                if (temp[temp.length-1].equals(account3)) {
                    newFriend = rs.getString("friendid").replace(account3,"");
                } else {
                    newFriend = rs.getString("friendid").replace(account3+",","");
                }
                Main.sql.doupdate("UPDATE wqw_friends SET friendid='"+newFriend+"' WHERE userid="+account2);
                sendPack.addString("{\"t\":\"xt\",\"b\":{\"r\":-1,\"o\":{\"cmd\":\"deleteFriend\",\"ID\":"+account3+"}}}");
                gameServer.writePlayerPacket(thischar, sendPack, true);
            }
            if (repeat) {
                deleteFriend(otherchar, thischar, false);
            }
            rs.close();
        } catch (Exception e) {
            debug("Exception in delete friend: "+e.getMessage());
        }
    }
    /*protected void dancePlayer(String username)
    {
    Packet sendPack = new Packet();
    sendPack.addString("%xt%emotea%-1%");
    sendPack.addString("dance");
    sendPack.addString("%"+gameServer.getPlayerID(username)+"%");
    gameServer.writeMapPacket(this.account,sendPack,true,false);
    }*/
    protected void emoteAll(String emote)
    {
        Packet sendPack = new Packet();
        for (int i=0; i < 10; i++) {
            sendPack.clean();
            sendPack.addString("%xt%emotea%-1%");
            sendPack.addString(emote);
            sendPack.addString("%"+i+"%");
            gameServer.writeMapPacket(this.account,sendPack,true,false);
        }
    }
    protected void forceEmote(String[] cmd)
    {
        Packet sendPack = new Packet();
        sendPack.addString("%xt%emotea%-1%");
        sendPack.addString(cmd[0]);
        sendPack.addString("%"+gameServer.getPlayerID(cmd[1])+"%");
        gameServer.writeMapPacket(this.account,sendPack,true,false);
    }
    protected void getCharacterSettings()
    {
        String[] classShort = new String[6];
        classShort[0] = "bParty";
        classShort[1] = "bGoto";
        classShort[2] = "bWhisper";
        classShort[3] = "bTT";
        classShort[4] = "bFriend";
        try {
            int i = 0;
            while (i < 5) {
                ResultSet rs = Main.sql.doquery("SELECT * FROM wqw_users WHERE id="+this.userid);
                if (rs.next()) {
                    	if(classShort[i].equals("bGoto")){
                            if(rs.getInt("bGoto")==1){
                                serverMsg("Accepting goto requests.","server",false,false);
                            } else {
                                serverMsg("Blocking goto requests.","warning",false,false);
                            }
                        } else if(classShort[i].equals("bWhisper")){
                            if(rs.getInt("bWhisper")==1){
                                serverMsg("Accepting PMs.","server",false,false);
                            } else {
                                serverMsg("Ignoring PMs.","warning",false,false);
                            }
                        } else if(classShort[i].equals("bTT")){
                            if(rs.getInt("bTT")==1){
                                serverMsg("Ability ToolTips will always show on mouseover.","server",false,false);
                            } else {
                                serverMsg("Ability ToolTips will not show on mouseover during combat.","warning",false,false);
                            }
                        } else if(classShort[i].equals("bFriend")){
                            if(rs.getInt("bFriend")==1){
                                serverMsg("Accepting Friend requests.","server",false,false);
                            } else {
                                serverMsg("Ignoring Friend requests.","warning",false,false);
                            }
                        } else if(classShort[i].equals("bParty")){
                            if(rs.getInt("bParty")==1){
                                serverMsg("Accepting party invites.","server",false,false);
                            } else {
                                serverMsg("Ignoring party invites.","warning",false,false);
                            }
                        }
                }
                rs.close();
                i++;
            }
        } catch (Exception e) {
            debug("Error getting user settings: " + e.getMessage());
        }
    }
    protected void serverMsg(String msg,String type, boolean global, boolean map)
    {
        Packet sendPack = new Packet();
        sendPack.addString("%xt%"+type+"%-1%"+msg+"%");
        if(global){
            gameServer.writeGlobalPacket(this.account, sendPack, true, false);
        } else if(map){
            gameServer.writeMapPacket(this.account, sendPack, true, false);
        } else {
           send(sendPack,true);
        }
        sendPack.clean();
    }
    
    protected void doLogin(String user, String pass, boolean repeat)
    {
        Packet sendPack = new Packet();
        sendPack.addString("%xt%loginResponse%-1%");
        try {
            ResultSet rs = Main.sql.doquery("SELECT * FROM wqw_users WHERE username='"+user+"' AND password='"+pass+"' AND banned=0 LIMIT 1");
            if (rs.next()) {
                this.userid = rs.getInt("id");
                this.charname = rs.getString("username");
                this.classid = rs.getInt("currentClass");
                this.gender = rs.getString("gender");
                int result = gameServer.adduser(user, socketOut, socket, rs.getInt("level"));
                if (result != -1) {
                    String message = getMessage();
                    while (message.equals("")) {
                        message = getMessage();
                    }
                    String news = getNews();
                    while (news.equals("")) {
                        news = getNews();
                    }
                    sendPack.addString("true%"+result+"%"+user+"%"+message+"%"+pass+"%"+news+"%");
                    gameServer.userID[result] = this.userid;
                    this.accountid = result;
                    this.account = user;
                    this.playerRoom = gameServer.room[gameServer.getPlayerRoom(this.account)[0]][gameServer.getPlayerRoom(this.account)[1]];
                    this.playerSlot = this.playerRoom.getPlayerSlot(this.account);
                    Main.sql.doupdate("UPDATE wqw_users SET curServer='"+Main.serverName+"' WHERE username='"+this.account+"'");
                    ping.main(this);
                } else {
                    sendPack.addString("0%-1%%This server is full, please select another..%");
                }
            } else {
                sendPack.addString("0%-1%%User Data for '"+this.account+"' could not be retrieved. Please contact the MTWorlds staff to resolve the issue.%");
            }
            rs.close();
            getCharacterSettings();
            sendSettings();
            send(sendPack,true);
            //sendCurrentEvent();
            debug("Logged In : "+user);
        } catch (Exception e) {
            debug("Exception in do login: "+e.getMessage());
            if (repeat) {
                doLogin(user, pass, false);
            } else {
                this.finalize();
            }
        }
    }
    protected void sendCurrentEvent()
    {
        Packet sendPack = new Packet();
        sendPack.addString("%xt%server%-1%");
        try{
        ResultSet rs = Main.sql.doquery("SELECT event FROM wqw_settings LIMIT 1");
            if (rs.next()) {
                if(!rs.getString("event").equals("")){
                    sendPack.addString("Current Event! - "+rs.getString("event"));
                }else{
                    sendPack.addString("There are currently no events this time.");
                }
            }
        } catch(Exception err) {
            debug(err.getMessage());
        }
        sendPack.addString("%");
        send(sendPack,true);
    }
    protected void emoteChat(String chat)
    {
        Packet sendPack = new Packet();
        sendPack.addString("%xt%emotea%-1%");
        sendPack.addString(chat);
        sendPack.addString("%"+this.accountid+"%");
        gameServer.writeMapPacket(this.account,sendPack,true,true);
    }

    protected void equipItem(int itemid, int adjustid)
    {
        try {
            Packet sendPack = new Packet();
            sendPack.addString("{\"t\":\"xt\",\"b\":{\"r\":-1,\"o\":{\"uid\":");
            sendPack.addInt(this.accountid);
            ResultSet rs = Main.sql.doquery("SELECT * FROM wqw_equipment WHERE itemID="+itemid);
            if (rs.next()) {
                sendPack.addString(",\"ItemID\":\""+itemid+"\",\"strES\":\""+rs.getString("sES")+"\",\"cmd\":\"equipItem\",\"sFile\":\""+rs.getString("sFile")+"\",\"sLink\":\""+rs.getString("sLink")+"\"");
                if(rs.getString("sES").equals("Weapon")){
                    sendPack.addString(",\"sType\":\""+rs.getString("sType")+"\"");
                }
                sendPack.addString("}}}");
            }
            String type = rs.getString("sES");
            String classname=rs.getString("sName");
            int userclassid=rs.getInt("classID");
            rs.close();

            
            Main.sql.doupdate("UPDATE wqw_items SET equipped=0 WHERE userid="+this.userid+" AND equipped=1 AND sES='"+type+"'");
            Main.sql.doupdate("UPDATE wqw_items SET equipped=1 WHERE userid="+this.userid+" AND itemid="+itemid+" AND equipped=0");
            if (type.equals("Weapon")) {
                ResultSet is = Main.sql.doquery("SELECT * FROM wqw_items WHERE id="+adjustid);
                if (is.next()) {
                    this.weaponlevel = is.getInt("iLvl");
                }
                is.close();
                initAutoAttack();
            }
            gameServer.writeMapPacket(this.account, sendPack, true, false);
            sendPack.clean();
            if (type.equals("ar")) {
                this.classid=userclassid;
                Main.sql.doupdate("UPDATE wqw_users SET currentClass="+this.classid+" WHERE id="+this.userid);
                Main.sql.doupdate("UPDATE wqw_items SET className='"+classname+"' WHERE userid="+this.userid+" AND sES='ar' AND equipped=1");
                updateClass();
                loadSkills(this.classid);
            }
        } catch (Exception e) {
            debug("Exception in equip item: "+e.getMessage()+", itemid: "+itemid+", adjustid: "+adjustid);
        }
    }

    protected String getClassName(int id)
    {
        try {
            ResultSet rs = Main.sql.doquery("SELECT * FROM wqw_items WHERE userid="+id+" AND equipped=1 AND sES='ar'");
            if (rs.next()) {
                return rs.getString("className");
            }
            rs.close();
        } catch (Exception e) {
            debug("Exception in get class points: "+e.getMessage());
        }
        return "Error";
    }

    protected boolean isPotionsEquipped()
    {
        try {
            ResultSet rs = Main.sql.doquery("SELECT * FROM wqw_items WHERE equipped=1 AND itemid=1749 AND userid="+this.userid);
               if (rs.next()) {
                   return true;
               }
        } catch (Exception e){

        }
        return false;
    }

    protected String getItemInfo(int itemid)
    {
        Packet itemPack = new Packet();
        try {
            ResultSet rs = Main.sql.doquery("SELECT * FROM wqw_equipment WHERE itemid="+itemid);
               if (rs.next()) {
                    itemPack.addString("\"sIcon\":\""+rs.getString("sIcon")+"\"," +
                    "\"ItemID\":\""+rs.getInt("itemID")+"\"," +
                    "\"iLvl\":\""+rs.getInt("iLvl")+"\"," +
                    "\"sLink\":\""+rs.getString("sLink")+"\"," +
                    "\"sElmt\":\""+rs.getString("sElmt")+"\"," +
                    "\"bTemp\":\""+rs.getInt("bTemp")+"\"," +
                    "\"bStaff\":\""+rs.getInt("bStaff")+"\"," +
                    "\"iRng\":\""+rs.getInt("iRng")+"\"," +
                    "\"bCoins\":\""+rs.getInt("bCoins")+"\"," +
                    "\"iDPS\":\""+rs.getInt("iDPS")+"\"," +
                    "\"sES\":\""+rs.getString("sES")+"\"," +
                    "\"bitSuccess\":\"1\"," +
                    "\"sType\":\""+rs.getString("sType")+"\"," +
                    "\"sDesc\":\""+rs.getString("sDesc")+"\"," +
                    "\"iStk\":\""+rs.getInt("iStk")+"\"," +
                    "\"iCost\":\""+rs.getInt("iCost")+"\"," +
                    "\"bUpg\":\""+rs.getInt("bUpg")+"\"," +
                    "\"bHouse\":\"0\"," +
                    "\"iRty\":"+rs.getInt("iRty")+"," +
                    "\"sName\":\""+rs.getString("sName")+"\"," +
                    "\"sReqQuests\":\""+rs.getString("sReqQuests")+"\"");
            }
            rs.close();
        } catch (Exception e) {
            debug("Exception in get item info: "+e.getMessage());
        }
        return itemPack.getPacket();
    }

    protected String getEquipment(int id)
    {
        Packet equipPack = new Packet();
        String[] classShort = new String[6];
        classShort[0] = "ar";
        classShort[1] = "ba";
        classShort[2] = "Weapon";
        classShort[3] = "co";
        classShort[4] = "he";
        classShort[5] = "pe";
        try {
            int i = 0;
            while (i < 6) {
                ResultSet rs = Main.sql.doquery("SELECT * FROM wqw_items WHERE userid="+id+" AND equipped=1 AND sES='"+classShort[i]+"'");
                if (rs.next()) {
                    if (i == 0) {
                        equipPack.addString("\""+classShort[i]+"\":{\"ItemID\":\"");
                    } else {
                        equipPack.addString(",\""+classShort[i]+"\":{\"ItemID\":\"");
                    }
                    int itemid = rs.getInt("itemid");
                    equipPack.addInt(itemid);

                    if (id == this.accountid && classShort[i].equals("Weapon")) {
                        this.weaponlevel = rs.getInt("iLvl");
                    }
                    ResultSet es = Main.sql.doquery("SELECT * FROM wqw_equipment WHERE itemid="+itemid+" LIMIT 1");
                    if (es.next()) {
                        equipPack.addString("\",\"sType\":");
                        equipPack.addString("\""+es.getString("sType"));
                        equipPack.addString("\",\"sFile\":\"");
                        equipPack.addString(es.getString("sFile"));
                        equipPack.addString("\",\"sLink\":\"");
                        equipPack.addString(es.getString("sLink"));
                    }
                    equipPack.addString("\"}");
                    es.close();
                }
                rs.close();
                i++;
            }
        } catch (Exception e) {
            debug("Exception in get equipment: "+e.getMessage());
        }
        return equipPack.getPacket();
    }

    protected void getQuests(String quests)
    {
        try {
            Packet sendPack = new Packet();
            sendPack.addString("{\"t\":\"xt\",\"b\":{\"r\":-1,\"o\":{\"cmd\":\"getQuests\",\"quests\":{");
            String packet_handled[] = quests.split("%");

            /* Get each quest info... */

            for (int i = 1; i < packet_handled.length; i++) {
                ResultSet rs = Main.sql.doquery("SELECT * FROM wqw_quests WHERE id="+packet_handled[i]);
                debug("Trying to Load Quest ID: " +packet_handled[i] + " to " + this.account);
                if (!packet_handled[i].equals("undefined") && rs.next()) {

                    String[] oItems;
                    String[] oRewards;
                    String[] turnin;

                    if(i!=1){
                        sendPack.addString(",");
                    }

                    /* Initialize the variables */

                    oItems = rs.getString("oItems").split(":");
                    if(!rs.getString("oRewards").equals("")){
                        oRewards = rs.getString("oRewards").split(":");
                    } else {
                        oRewards=new String[0];
                    }
                    turnin = rs.getString("turnin").split(",");

                    
                    sendPack.addString("\""+rs.getString("id")+"\":{\"sFaction\":\""+rs.getString("sFaction")+"\",\"iLvl\":\""+rs.getString("iLvl")+"\",\"FactionID\":\""+rs.getInt("factionID")+"\",\"iClass\":"+rs.getInt("iClass")+",");
                    sendPack.addString("\"iReqRep\":"+rs.getString("iReqRep")+",\"iValue\":"+rs.getInt("iValue")+",\"bOnce\":\"0\",\"iGold\":"+rs.getInt("iGold")+",");
                    sendPack.addString("\"iRep\":"+rs.getInt("iRep")+",");
                    sendPack.addString("\"bitSuccess\":\"1\",\"sEndText\":\""+rs.getString("sEndText")+"\",\"sDesc\":\""+rs.getString("sDesc")+"\",");
                    sendPack.addString("\"QuestID\":\""+rs.getString("id")+"\",\"bUpg\":\""+rs.getInt("bUpg")+"\",\"iReqCP\":"+rs.getInt("iReqCP")+",\"iSlot\":-1,\"iExp\":"+rs.getInt("iExp")+",\"iWar\":0,\"sName\":\""+rs.getString("sName")+"\",");
                    
                    /* If there are quest item drops, add them */
                    sendPack.addString("\"oRewards\":{");
                    if(!rs.getString("oRewards").equals("")){
                        
                        sendPack.addString("\"items"+rs.getString("rewType")+"\":{");
                        for (int x=0; x < oRewards.length; x++) {
                             if (x != 0) {
                                sendPack.addString(",");
                             }
                             sendPack.addString("\""+oRewards[x]+"\":{");
                             sendPack.addString(getItemInfo(Integer.parseInt(oRewards[x])));
                             sendPack.addString("}");
                        }
                        sendPack.addString("}");
                       
                    }
                     sendPack.addString("},");
                    /* Items to send to finish the quest */

                    sendPack.addString("\"turnin\":[");
                    for (int a=0; a < turnin.length; a++) {
                        if (a != 0) {
                            sendPack.addString(",");
                        }
                        String [] droppart = turnin[a].split(":");
                        sendPack.addString("{\"ItemID\":\""+droppart[0]+"\",\"iQty\":\""+droppart[1]+"\"}");
                    }
                    sendPack.addString("],");

                    /* Required items for turning in quest */

                    sendPack.addString("\"oItems\":");
                    sendPack.addString("{");
                    for (int e=0; e < oItems.length; e++) {
                        if (e != 0) {
                            sendPack.addString(",");
                        }
                        sendPack.addString("\""+oItems[e]+"\":{");
                        sendPack.addString(getItemInfo(Integer.parseInt(oItems[e])));
                        sendPack.addString("}");
                    }
                    sendPack.addString("}}");
                    debug("Quest ID Loaded: "+packet_handled[i] +" to " +this.account);
                }
                rs.close();
            }
            sendPack.addString("}}}}");
            send(sendPack, true);

        } catch (Exception e) {
            debug("Exception in get quests: "+e.getMessage());
        }
        /* Get Quests by Zeroskull */
    }

    protected void getInvent()
    {
        try {
            Packet sendPack = new Packet();
            sendPack.addString("{\"t\":\"xt\",\"b\":{\"r\":-1,\"o\":{\"cmd\":\"initInventory\",\"items\":[");
            ResultSet rs = Main.sql.doquery("SELECT * FROM wqw_items WHERE userid="+this.userid+" AND bBank=0");
            int[] charitemid = new int[40];
            int[] itemid = new int[40];
            int[] equip = new int[40];
            int[] level = new int[40];
            int[] classxp = new int[40];
            int i = 0;
            while (rs.next()) {
                charitemid[i] = rs.getInt("id");
                itemid[i] = rs.getInt("itemid");
                equip[i] = rs.getInt("equipped");
                level[i] = rs.getInt("iLvl");
                if(rs.getString("sES").equals("ar")){
                    classxp[i] = rs.getInt("classXP");
                }
                i++;
            }
            rs.close();
            int e = 0;
            while (e < i) {
                if (e != 0) {
                    sendPack.addString(",");
                }
                ResultSet is = Main.sql.doquery("SELECT * FROM wqw_equipment WHERE itemID="+itemid[e]);
                if (is.next()) {
                    sendPack.addString("{\"ItemID\":\""+is.getInt("itemID")+"\",\"sLink\":\""+is.getString("sLink")+"\",\"sElmt\":\""+is.getString("sElmt")+"\",\"bStaff\":\""+is.getInt("bStaff")+"\",\"iRng\":\""+is.getInt("iRng")+"\",\"iDPS\":\""+is.getInt("iDPS")+"\",\"bCoins\":\""+is.getInt("bCoins")+"\",\"sES\":\""+is.getString("sES")+"\",\"sType\":\""+is.getString("sType")+"\",\"iCost\":\""+is.getInt("iCost")+"\",\"iRty\":\""+is.getInt("iRty")+"\",");
                    if(is.getString("sES").equals("ar")){
                        sendPack.addString("\"iQty\":\""+classxp[e]+"\"");
                    } else {
                        sendPack.addString("\"iQty\":\""+is.getInt("iQty")+"\"");
                    }
                    sendPack.addString(",\"iLvl\":\""+level[e]+"\",\"sIcon\":\""+is.getString("sIcon")+"\",\"iEnh\":\""+level[e]+"\",\"bTemp\":\""+is.getInt("bTemp")+"\",\"CharItemID\":\""+charitemid[e]+"\",\"iHrs\":\""+is.getInt("iHrs")+"\",\"sFile\":\""+is.getString("sFile")+"\",\"iStk\":\""+is.getInt("iStk")+"\",\"sDesc\":\""+is.getString("sDesc")+"\",\"bBank\":\""+0+"\",\"bUpg\":\""+is.getInt("bUpg")+"\",\"bEquip\":\""+equip[e]+"\",\"sName\":\""+is.getString("sName")+"\"}");
                }
                is.close();
                e++;
            }
            sendPack.addString("]}}}");
            send(sendPack, true);
            sendPack.clean();
            sendPack.addString("%xt%server%-1%Character load complete.%");
            send(sendPack, true);
        } catch (Exception e) {
            debug("Exception in get invent: "+e.getMessage());
        }
    }

    protected String getMessage()
    {
        try {
            ResultSet rs = Main.sql.doquery("SELECT message FROM wqw_settings LIMIT 1");
            if (rs.next()) {
                return rs.getString("message");
            }
            rs.close();
        } catch (Exception e) {
            //debug("Exception in get news: "+e.getMessage());
        }
        return "";
    }

    protected String getNews()
    {
        try {
            ResultSet rs = Main.sql.doquery("SELECT * FROM wqw_settings LIMIT 1");
            if (rs.next()) {
                return "sNews="+rs.getString("newsFile")+",sMap="+rs.getString("mapFile")+",sBook="+rs.getString("bookFile");
            }
            rs.close();
        } catch (Exception e) {
            //debug("Exception in get news: "+e.getMessage());
        }
        return "";
    }

    protected void joinRoom(String newroom, int roomnumb, String frame, String pad)
    {
        Packet sendPack = new Packet();
        if(roomnumb > 99999){
            roomnumb = generator.nextInt(99999);
        }
        int oldroom[] = gameServer.getPlayerRoom(this.account);
        if(/*!newroom.equals("darkoviapvp") || */!newroom.equals("bludrutbrawl")) {
            gameServer.hp[this.accountid] = gameServer.calculateHP(this.playerlevel);
            gameServer.hpmax[this.accountid] = gameServer.calculateHP(this.playerlevel);
            sendUotls(true, true, true, true, false);
            onpvp = false;
        } else if (/*newroom.equals("darkoviapvp") || */newroom.equals("bludrutbrawl") && !onpvp) {
            serverMsg("PvP Map 'bludrutbrawl' is only accessible through the pvp interface.", "warning", false, false);
            return;
        } else {
            int newhp = getHPwithNecklace();
            gameServer.hp[this.accountid] = newhp;
            gameServer.hpmax[this.accountid] = newhp;
            sendUotls(true, true, true, true, false);
        }
        if(!gameServer.isStaff(this.account) && newroom.equals("limbo")){
            sendPack.addString("%xt%warning%-1%");
            sendPack.addString("\"limbo\" is a staff only map.");
            sendPack.addString("%");
            send(sendPack, true);
        } else if(!gameServer.isStaff(this.account) && newroom.equals("???!!!")){
            sendPack.addString("%xt%warning%-1%");
            sendPack.addString("\"???\" is a staff only map.");
            sendPack.addString("%");
            send(sendPack, true);
        } else {
            if (gameServer.addToRoom(newroom, roomnumb, this.accountid)) {
                this.playerRoom = gameServer.room[gameServer.getPlayerRoom(this.account)[0]][gameServer.getPlayerRoom(this.account)[1]];
                this.playerSlot = this.playerRoom.getPlayerSlot(this.account);
                this.playerRoom.frame[this.playerSlot] = frame;
                this.playerRoom.pad[this.playerSlot] = pad;
                this.playerRoom.tx[this.playerSlot] = 0;
                this.playerRoom.ty[this.playerSlot] = 0;
                sendPack.addXMLSingle(1,"msg t","sys");
                sendPack.addXMLSingle(1,"body action","userGone","r",""+oldroom[0]);
                sendPack.addXMLSingle(0,"user id",""+this.accountid);
                sendPack.addXMLSingle(2,"body");
                sendPack.addXMLSingle(2,"msg");
                gameServer.writeOtherMapPacket(oldroom, sendPack, true);
                sendPack.clean();
                sendPack.addString("%xt%exitArea%-1%");
                sendPack.addInt(this.accountid);
                sendPack.addString("%"+this.account+"%");
                gameServer.writeOtherMapPacket(oldroom, sendPack, true);
                sendLobby();
            } else {
                sendPack.addString("%xt%warning%-1%\"");
                sendPack.addString(newroom);
                sendPack.addString("\" is not a recognized Map name%");
                send(sendPack, true);
            }
      }
    }

    protected void loadBank()
    {
        try {
            Packet sendPack = new Packet();
            sendPack.addString("{\"t\":\"xt\",\"b\":{\"r\":-1,\"o\":{\"cmd\":\"loadBank\",\"items\":[");
            ResultSet rs = Main.sql.doquery("SELECT * FROM wqw_items WHERE userid="+this.userid+" AND bBank=1");
            int[] charitemid = new int[160];
            int[] itemid = new int[160];
            int[] equip = new int[160];
            int[] level = new int[160];
            int i = 0;
            while (rs.next()) {
                charitemid[i] = rs.getInt("id");
                itemid[i] = rs.getInt("itemid");
                equip[i] = rs.getInt("equipped");
                level[i] = rs.getInt("iLvl");
                i++;
            }
            rs.close();
            int e = 0;
            while (e < i) {
                if (e != 0) {
                    sendPack.addString(",");
                }
                ResultSet is = Main.sql.doquery("SELECT * FROM wqw_equipment WHERE itemID="+itemid[e]);
                if (is.next()) {
                    sendPack.addString("{\"sIcon\":\""+is.getString("sIcon")+"\",\"ItemID\":\""+is.getInt("itemID")+"\",\"iLvl\":\""+level[e]+"\",\"iEnh\":\""+is.getInt("iEnh")+"\",\"sElmt\":\""+is.getString("sElmt")+"\",\"bTemp\":\""+is.getInt("bTemp")+"\",\"sLink\":\""+is.getString("sLink")+"\",\"bStaff\":\""+is.getInt("bStaff")+"\",\"CharItemID\":\""+charitemid[e]+"\",\"iRng\":\""+is.getInt("iRng")+"\",\"bCoins\":\""+is.getInt("bCoins")+"\",\"iDPS\":\""+is.getInt("iDPS")+"\",\"sES\":\""+is.getString("sES")+"\",\"iHrs\":\""+is.getInt("iHRS")+"\",\"sFile\":\""+is.getString("sFile")+"\",\"sType\":\""+is.getString("sType")+"\",\"sDesc\":\""+is.getString("sDesc")+"\",\"iStk\":\""+is.getInt("iStk")+"\",\"iCost\":\""+is.getInt("iCost")+"\",\"bEquip\":\""+equip[e]+"\",\"bUpg\":\""+is.getInt("bUpg")+"\",\"iRty\":\""+is.getInt("iRty")+"\",\"sName\":\""+is.getString("sName")+"\",\"iQty\":\""+is.getInt("iQty")+"\"}");
                }
                is.close();
                e++;
            }
            sendPack.addString("]}}}");
            send(sendPack, true);
        } catch (Exception e) {
            debug("Exception in load bank: "+e.getMessage());
        }
    }

    protected void loadBigInventory(){
        Packet sendPack = new Packet();
        sendPack.addString("{\"t\":\"xt\",\"b\":{\"r\":-1,\"o\":{\"cmd\":\"loadInventoryBig\",\"friends\":[");
        try {
            ResultSet rs = Main.sql.doquery("SELECT * FROM wqw_friends WHERE userid="+this.userid);
            if (rs.next()) {
                if (!rs.getString("friendid").equals("")) {
                    String[] friendslist = rs.getString("friendid").split(",");
                    int i = friendslist.length;
                    int e = 0;
                    rs.close();
                    while (e < i) {
                        if (e != 0) {
                            sendPack.addString(",");
                        }
                        ResultSet is = Main.sql.doquery("SELECT * FROM wqw_users WHERE id="+friendslist[e]);
                        if (is.next()) {
                            this.friends[e] = is.getString("username");
                            sendPack.addString("{\"iLvl\":\""+is.getInt("level")+"\",\"ID\":\""+is.getInt("id")+"\",\"sName\":\""+is.getString("username")+"\",\"sServer\":\""+is.getString("curServer")+"\"}");
                        }
                        is.close();
                        e++;
                    }
                }
            }
            sendPack.addString("],\"items\":[");

            ResultSet rs2 = Main.sql.doquery("SELECT * FROM wqw_items WHERE sES NOT IN('hi','ho') AND userid="+this.userid+" AND bBank=0");
            int[] charitemid = new int[265];
            int[] itemid = new int[265];
            int[] equip = new int[265];
            int[] level = new int[265];
            int[] classxp = new int[265];
            int[] qty = new int[265];
            int[] enhid = new int[265];
            int i = 0;
            while (rs2.next()) {
                charitemid[i] = rs2.getInt("id");
                itemid[i] = rs2.getInt("itemid");
                equip[i] = rs2.getInt("equipped");
                level[i] = rs2.getInt("iLvl");
                enhid[i] = rs2.getInt("EnhID");
                qty[i] = rs2.getInt("iQty");
                if(rs2.getString("sES").equals("ar")){
                    classxp[i] = rs2.getInt("classXP");
                }
                i++;
            }
            rs2.close();
            int e = 0;
            while (e < i) {
                ResultSet is = Main.sql.doquery("SELECT * FROM wqw_equipment WHERE itemID="+itemid[e]);
                if (is.next()) {
                    if (e != 0) {
                        sendPack.addString(",");
                    }
                    sendPack.addString("{\"ItemID\":\""+is.getInt("itemID")+"\",\"sLink\":\""+is.getString("sLink")+"\",\"sElmt\":\""+is.getString("sElmt")+"\",\"bStaff\":\""+is.getInt("bStaff")+"\",\"iRng\":\""+is.getInt("iRng")+"\",\"iDPS\":\""+is.getInt("iDPS")+"\",\"bCoins\":\""+is.getInt("bCoins")+"\",\"sES\":\""+is.getString("sES")+"\",\"sType\":\""+is.getString("sType")+"\",\"iCost\":\""+is.getInt("iCost")+"\",\"iRty\":\""+is.getInt("iRty")+"\",");
                    if(is.getString("sES").equals("ar")){
                        sendPack.addString("\"iQty\":\""+classxp[e]+"\",");
                    } else {
                        sendPack.addString("\"iQty\":\""+qty[e]+"\",");
                    }
                    if(is.getString("sES").equals("Weapon")){
                        sendPack.addString("\"EnhDPS\":\"100\",");
                    }
                    if(is.getString("sType").equals("Enhancement") || is.getString("sType").equals("Necklace") || is.getString("sType").equals("Item") || is.getString("sType").equals("Quest Item") || is.getString("sType").equals("Pet") || is.getString("sType").equals("Armor")){
                            sendPack.addString("\"EnhID\":\"0\",\"PatternID\":\""+enhid[e]+"\",");
                    }
                    
                    if(is.getString("sType").equals("Enhancement") || enhid[e]==-1){
                        sendPack.addString("\"iLvl\":\""+is.getInt("iLvl"));
                    } else {
                        sendPack.addString("\"EnhLvl\":\""+level[e]+"\",\"EnhID\":\"1863\",\"EnhRty\":1,\"EnhPatternID\":\""+enhid[e]);
                    }
                    sendPack.addString("\",\"sIcon\":\""+is.getString("sIcon")+"\",\"bTemp\":\""+is.getInt("bTemp")+"\",\"CharItemID\":\""+charitemid[e]+"\",\"iHrs\":\""+is.getInt("iHrs")+"\",\"sFile\":\""+is.getString("sFile")+"\",\"iStk\":\""+is.getInt("iStk")+"\",\"sDesc\":\""+is.getString("sDesc")+"\",\"bBank\":\""+0+"\",\"bUpg\":\""+is.getInt("bUpg")+"\",\"bEquip\":\""+equip[e]+"\",\"sName\":\""+is.getString("sName")+"\"}");
                }
                is.close();
                e++;
            }

            sendPack.addString("],\"factions\":[],\"hitems\":[");
            ResultSet hs = Main.sql.doquery("SELECT * FROM wqw_items WHERE sES IN('hi','ho') AND userid="+this.userid+" AND bBank=0");
            int x=0;
            int[] hequip = new int[30];
            int[] hcharitemid = new int[30];
            int[] hitemid = new int[30];
            while (hs.next()) {
                hequip[x] = hs.getInt("equipped");
                hcharitemid[x] = hs.getInt("id");
                hitemid[x] = hs.getInt("itemid");
                x++;
            }
            int z = 0;
            while (z < x) {
                ResultSet is = Main.sql.doquery("SELECT * FROM wqw_equipment WHERE itemid="+hitemid[z]);
                if (is.next()) {
                    if (z != 0) {
                        sendPack.addString(",");
                    }
                    sendPack.addString("{\"ItemID\":\""+is.getInt("itemID")+"\",\"sLink\":\""+is.getString("sLink")+"\",\"sElmt\":\""+is.getString("sElmt")+"\",\"bStaff\":\""+is.getInt("bStaff")+"\",\"iRng\":\""+is.getInt("iRng")+"\",\"iDPS\":\""+is.getInt("iDPS")+"\",\"bCoins\":\""+is.getInt("bCoins")+"\",\"sES\":\""+is.getString("sES")+"\",\"sType\":\""+is.getString("sType")+"\",\"iCost\":\""+is.getInt("iCost")+"\",\"iRty\":\""+is.getInt("iRty")+"\",");
                    sendPack.addString("\"iQty\":\"1\",");
                    sendPack.addString("\"iLvl\":\""+is.getInt("iLvl"));
                    sendPack.addString("\",\"sIcon\":\""+is.getString("sIcon")+"\",\"bTemp\":\""+is.getInt("bTemp")+"\",\"CharItemID\":\""+hcharitemid[z]+"\",\"iHrs\":\""+is.getInt("iHrs")+"\",\"sFile\":\""+is.getString("sFile")+"\",\"iStk\":\""+is.getInt("iStk")+"\",\"sDesc\":\""+is.getString("sDesc")+"\",\"bBank\":\""+0+"\",\"bUpg\":\""+is.getInt("bUpg")+"\",\"bEquip\":\""+hequip[z]+"\",\"sName\":\""+is.getString("sName")+"\"}");
                }
                is.close();
                z++;
            }
            sendPack.addString("]}}}");
            send(sendPack, true);
            updateClass();
            loadSkills(this.classid);
            sendPack.clean();
            sendPack.addString("%xt%server%-1%Character load complete.%");
            send(sendPack, true);
            sendOnlineStatus();
        } catch (Exception e) {
            debug("Exception in load big inventory: "+e.getMessage());
        }
    }

    protected void loadHouseInventory()
    {
        Packet sendPack = new Packet();
        sendPack.addString("{\"t\":\"xt\",\"b\":{\"r\":-1,\"o\":{\"cmd\":\"loadHouseInventory\",\"sHouseInfo\":[],\"items\":[]}}}");
        send(sendPack, true);
    }

    protected void loadShop(int shopid)
    {
        try {
            Packet sendPack = new Packet();
            ResultSet rs = Main.sql.doquery("SELECT * FROM wqw_shops WHERE shopid="+shopid);
            if (rs.next()) {
                sendPack.addString("{\"t\":\"xt\",\"b\":{\"r\":-1,\"o\":{\"shopinfo\":{\"strName\":\"");
                sendPack.addString(rs.getString("strName"));
                sendPack.addString("\",\"bitSuccess\":\"1\",\"items\":[");
                String[] items = rs.getString("items").split(",");
                int house = rs.getInt("bhouse");
                int staff = rs.getInt("bStaff");
                String field = rs.getString("sField");
                rs.close();
                for (int e =0; e < items.length; e++) {
                    if (e != 0) {
                        sendPack.addString(",");
                    }
                    ResultSet is = Main.sql.doquery("SELECT * FROM wqw_equipment WHERE itemID="+items[e]);
                    if (is.next()) {
                        if(is.getString("sType").equals("Enhancement") || is.getString("sType").equals("Necklace") || is.getString("sType").equals("Item") || is.getString("sType").equals("Quest Item") || is.getString("sType").equals("Pet") || is.getString("sType").equals("Armor")){
                            sendPack.addString("{\"EnhID\":\"0\",\"PatternID\":\""+is.getInt("EnhID")+"\",\"iLvl\":\""+is.getInt("iLvl")+"\",");
                        } else {
                            sendPack.addString("{\"EnhID\":\"1863\",\"EnhRty\":1,\"EnhPatternID\":\""+is.getInt("iLvl")+"\",\"EnhLvl\":\""+is.getInt("iLvl")+"\",");
                        }
                        if(is.getString("sES").equals("ho")){
                            sendPack.addString("\"bHouse\":\"1\",");
                        }
                        sendPack.addString("\"iQSvalue\":\"0\",\"sFaction\":\""+is.getString("sFaction")+"\",\"ItemID\":\""+is.getInt("itemID")+"\",\"iClass\":\""+is.getInt("iClass")+"\",\"sElmt\":\""+is.getString("sElmt")+"\",\"sLink\":\""+is.getString("sLink")+"\",\"bStaff\":\""+is.getInt("bStaff")+"\",\"iRng\":\""+is.getInt("iRng")+"\",\"iDPS\":\""+is.getInt("iDPS")+"\",\"bCoins\":\""+is.getInt("bCoins")+"\",\"sES\":\""+is.getString("sES")+"\",\"sType\":\""+is.getString("sType")+"\",\"iCost\":\""+is.getInt("iCost")+"\",\"iRty\":\""+is.getInt("iRty")+"\",\"iQty\":\""+is.getInt("iQty")+"\",\"sIcon\":\""+is.getString("sIcon")+"\",\"FactionID\":\""+is.getInt("FactionID")+"\",\"bTemp\":\""+is.getInt("bTemp")+"\",\"iReqRep\":\""+is.getInt("iReqRep")+"\",\"ShopItemID\":\""+(shopid+e)+"\",\"sFile\":\""+is.getString("sFile")+"\",\"iStk\":\""+is.getInt("iStk")+"\",\"sDesc\":\""+is.getString("sDesc")+"\",\"bUpg\":\""+is.getInt("bUpg")+"\",\"bHouse\":\""+house+"\",\"iReqCP\":\""+is.getInt("iReqCP")+"\",\"sName\":\""+is.getString("sName")+"\",\"iQSindex\":\"-1\"}");
                    }
                    is.close();
                }
                sendPack.addString("],\"ShopID\":\"");
                sendPack.addInt(shopid);
                sendPack.addString("\",\"sField\":\""+field+"\",\"bStaff\":\""+staff+"\",\"bHouse\":\""+house);
                sendPack.addString("\",\"iIndex\":\"-1\"},\"cmd\":\"loadShop\"}}}");
                send(sendPack, true);
            }
        } catch (Exception e) {
            debug("Exception in load shop: "+e.getMessage());
        }
    }

    protected void sendOnlineStatus()
    {
        try {
            Packet sendPack = new Packet();
            sendPack.addString("{\"t\":\"xt\",\"b\":{\"r\":-1,\"o\":{\"cmd\":\"updateFriend\",\"friend\":{\"iLvl\":"+gameServer.level[this.accountid]+",\"ID\":\""+this.userid+"\",\"sName\":\""+this.charname+"\",\"sServer\":\""+Main.serverName+"\"}}}}");
            for(int i=0; i < this.friends.length; i++){
                if(gameServer.getPlayerID(this.friends[i].toLowerCase())>0){
                    gameServer.writePlayerPacket(this.friends[i].toLowerCase(), sendPack, true);
                    sendPack.clean();
                    sendPack.addString("%xt%server%-1%"+this.charname+" has logged in.%");
                    write(this.charname,"(Is now Online)");
                    gameServer.writePlayerPacket(this.friends[i].toLowerCase(), sendPack, true);
                }
            }
        } catch (Exception e){
            debug("Error in sending friend online request: "+ e.getMessage());
        }
    }

    protected void updateFriends()
    {
        try {
            ResultSet rs = Main.sql.doquery("SELECT * FROM wqw_friends WHERE userid="+this.userid);
            if (rs.next()) {
                if (!rs.getString("friendid").equals("")) {
                    String[] friendslist = rs.getString("friendid").split(",");
                    rs.close();
                    for (int e = 0; e < friendslist.length; e++) {
                        ResultSet is = Main.sql.doquery("SELECT * FROM wqw_users WHERE id="+friendslist[e]);
                        if (is.next()) {
                            this.friends[e] = is.getString("username");
                        }
                        is.close();
                    }
                }
            }
        } catch (Exception e){
            debug("Error in updating friends array: "+ e.getMessage());
        }
    }

    protected void sendEnhancementDetails()
    {
        Packet sendPack = new Packet();
        sendPack.addString("{\"t\":\"xt\",\"b\":{\"r\":-1,\"o\":{\"cmd\":\"enhp\",\"o\":{\"3\":{\"iWIS\":\"0\",\"sDesc\":\"M2\",\"ID\":\"3\",\"iSTR\":\"30\",\"iLCK\":\"0\",\"sName\":\"Thief\",\"iDEX\":\"45\",\"iEND\":\"25\",\"iINT\":\"0\"},\"2\":{\"iWIS\":\"0\",\"sDesc\":\"M1\",\"ID\":\"2\",\"iSTR\":\"44\",\"iLCK\":\"0\",\"sName\":\"Fighter\",\"iDEX\":\"13\",\"iEND\":\"43\",\"iINT\":\"0\"},\"1\":{\"iWIS\":\"16\",\"sDesc\":\"none\",\"ID\":\"1\",\"iSTR\":\"16\",\"iLCK\":\"0\",\"sName\":\"Adventurer\",\"iDEX\":\"16\",\"iEND\":\"18\",\"iINT\":\"16\"},\"7\":{\"iWIS\":\"15\",\"sDesc\":\"C2\",\"ID\":\"7\",\"iSTR\":\"0\",\"iLCK\":\"0\",\"sName\":\"Healer\",\"iDEX\":\"0\",\"iEND\":\"40\",\"iINT\":\"45\"},\"6\":{\"iWIS\":\"0\",\"sDesc\":\"C1\",\"ID\":\"6\",\"iSTR\":\"0\",\"iLCK\":\"20\",\"sName\":\"Wizard\",\"iDEX\":\"0\",\"iEND\":\"30\",\"iINT\":\"50\"},\"5\":{\"iWIS\":\"0\",\"sDesc\":\"M3\",\"ID\":\"5\",\"iSTR\":\"28\",\"iLCK\":\"0\",\"sName\":\"Hybrid\",\"iDEX\":\"20\",\"iEND\":\"25\",\"iINT\":\"27\"},\"9\":{\"iWIS\":\"0\",\"sDesc\":\"S1\",\"ID\":\"9\",\"iSTR\":\"5\",\"iLCK\":\"70\",\"sName\":\"Lucky\",\"iDEX\":\"10\",\"iEND\":\"15\",\"iINT\":\"0\"}}}}}");
        send(sendPack, true);
    }

    protected void initAutoAttack()
    {
        String weptype = getWeaponInfo("sType");
        String wepname = getWeaponInfo("sName");
        if(weptype.equals("Bow")){
            this.anim[0]="RangedAttack1";
        } else if (weptype.equals("Dagger")){
            this.anim[0]="DuelWield/DaggerAttack1,DuelWield/DaggerAttack2";
        } else if (wepname.equals("No Weapon")){
            this.anim[0]="UnarmedAttack1,UnarmedAttack2,KickAttack,FlipAttack";
        } else {
            this.anim[0]="Attack1,Attack2";
        }
    }

    protected void loadSkills(int classid)
    {
        int cp = gameServer.getClassPoints(this.userid);
        int rank = gameServer.getRankFromCP(cp);
        boolean pot = isPotionsEquipped();
        
        if(rank==-1){
            rank = 10;
        }
        this.userrank = rank;
         try {
            Packet sendPack = new Packet();
            ResultSet rs = Main.sql.doquery("SELECT * FROM wqw_classes WHERE classid="+classid);
            if (rs.next()) {
                sendPack.addString("{\"t\":\"xt\",\"b\":{\"r\":-1,\"o\":{\"cmd\":\"sAct\",\"actions\":{\"passive\":[");
                String[] passives = rs.getString("passives").split(",");
                String[] skills = rs.getString("skills").split(",");
                rs.close();
                for (int x = 0; x < passives.length; x++) {
                    if(x!=0){
                        sendPack.addString(",");
                    }
                    ResultSet es = Main.sql.doquery("SELECT * FROM wqw_passives WHERE id="+passives[x]);
                    if(es.next()){
                        sendPack.addString("{\"icon\":\""+es.getString("icon")+"\",\"ref\":\""+es.getString("ref")+"\",\"nam\":\""+es.getString("name")+"\",\"desc\":\""+es.getString("desc")+"\",");
                        if(rank<=3){
                            sendPack.addString("\"isOK\":false");
                        } else {
                            sendPack.addString("\"isOK\":true");
                        }
                        sendPack.addString(",\"auras\":[{}],\"typ\":\""+es.getString("type")+"\"}");
                    }
                    es.close();
                }
                sendPack.addString("],\"active\":[");
                for(int e =0; e < skills.length; e++){
                    if (e != 0) {
                        sendPack.addString(",");
                    }
                    ResultSet is = Main.sql.doquery("SELECT * FROM wqw_skills WHERE id="+skills[e]);
                    if (is.next()) {
                        this.anim[e]=is.getString("anim");
                        this.str1[e]=is.getString("str1");
                        this.mpcost[e] = is.getInt("mana");
                        this.skillfx[e] = is.getString("fx");
                        if(!is.getString("aura").equals("")){
                            this.auras[e]=is.getString("aura");
                        } else {
                            this.auras[e]="";
                        }
                        if(is.getString("ref").equals("aa")){
                            this.aacd=is.getInt("cd");
                        }
                        sendPack.addString("{\"icon\":\""+is.getString("icon")+"\",\"mp\":"+is.getInt("mana")+",\"nam\":\""+is.getString("name")+"\",\"anim\":\""+is.getString("anim")+"\",\"desc\":\""+is.getString("desc")+"\",");
                        if(rank<=1 && e==2){
                          sendPack.addString("\"isOK\":false,");
                        } else if (rank<=2 && e==3) {
                          sendPack.addString("\"isOK\":false,");
                        } else if (rank<=4 && e==4) {
                          sendPack.addString("\"isOK\":false,");
                        } else {
                          sendPack.addString("\"isOK\":true,");
                        }
                        if(is.getInt("tgtMax")!=0){
                            sendPack.addString("\"tgtMax\":"+is.getInt("tgtMax")+",\"tgtMin\":"+is.getInt("tgtMin")+",");
                        }
                        sendPack.addString("\"range\":"+is.getInt("range")+",\"fx\":\"m\",\"damage\":1,\"dsrc\":\""+is.getString("dsrc")+"\",\"ref\":\""+is.getString("ref")+"\",\"auto\":"+is.getString("auto")+",\"tgt\":\""+is.getString("tgt")+"\",\"typ\":\""+is.getString("typ")+"\",\"strl\":\""+is.getString("str1")+"\",\"cd\":"+is.getInt("cd")+"}");
                    }
                    is.close();
                }
                sendPack.addString(",{\"icon\":\"icu1\",\"nam\":\"Potions\",\"anim\":\"Salute\",\"mp\":0,\"desc\":\"Equip a potion or scroll from your inventory to use it here.\",\"isOK\":true,\"range\":808,\"fx\":\"\",\"ref\":\"i1\",\"tgt\":\"f\",\"typ\":\"i\",\"strl\":\"\",\"cd\":5000}");
                sendPack.addString("]}}}}");
                send(sendPack, true);
                initAutoAttack();
                if(pot){
                    sendPotionAnimation();
                }
            }
            updateStats();
        } catch (Exception e) {
            debug("Exception in load skills: "+e.getMessage());
        }
    }
    protected String getWeaponInfo(String column){
        try {
            ResultSet rs = Main.sql.doquery("SELECT * FROM wqw_items WHERE equipped=1 AND sES='Weapon' AND userid="+this.userid);
            if(rs.next()){
                int itemid = rs.getInt("itemid");
                ResultSet es = Main.sql.doquery("SELECT * FROM wqw_equipment WHERE itemID="+itemid);
                if(es.next()){
                    return es.getString(column);
                }
            }
        } catch(Exception e){

        }
        return "Attack1";
    }
    protected void sendSettings()
    {
        Packet sendPack = new Packet();
        sendPack.addString("{\"t\":\"xt\",\"b\":{\"r\":-1,\"o\":{\"cmd\":\"cvu\",\"o\":{\"PCstRatio\":7.47,\"PChpDelta\":1640,\"PChpBase1\":360,\"baseHit\":0,\"intSPtoDPS\":10,\"resistRating\":17,\"curveExponent\":0.66,\"baseCritValue\":1.5,\"PChpGoal100\":4000,\"intLevelCap\":100,\"baseMiss\":0.1,\"baseParry\":0.03,\"GstBase\":12,\"modRating\":3,\"baseResistValue\":0.7,\"baseBlockValue\":0.7,\"intHPperEND\":5,\"baseHaste\":0,\"baseBlock\":0,\"statsExponent\":1,\"PChpBase100\":2000,\"intAPtoDPS\":10,\"PCstBase\":15,\"baseCrit\":0.05,\"baseEventValue\":0.05,\"GstGoal\":572,\"PChpGoal1\":400,\"GstRatio\":5.6,\"intLevelMax\":100,\"bigNumberBase\":8,\"PCstGoal\":762,\"baseDodge\":0.04,\"PCDPSMod\":0.85}}}}");
        send(sendPack, true);
    }
    protected void updateStats()
    {
        Packet sendPack = new Packet();
        sendPack.addString("{\"t\":\"xt\",\"b\":{\"r\":-1,\"o\":{\"tempSta\":{\"ba\":{\"STR\":9,\"INT\":9,\"DEX\":7,\"END\":8},\"Weapon\":{\"STR\":24,\"DEX\":7,\"END\":23},\"innate\":{\"STR\":61,\"INT\":11,\"DEX\":50,\"WIS\":23,\"LCK\":14,\"END\":68},\"ar\":{\"STR\":14,\"DEX\":4,\"END\":13},\"he\":{\"STR\":12,\"INT\":12,\"DEX\":9,\"END\":11}},\"cmd\":\"stu\",\"sta\":{\"$tdo\":0.115,\"$thi\":0.038500000000000006,\"_cmi\":1,\"$smb\":0,\"_tdo\":0.04,\"_cmo\":1,\"_sem\":0.05,\"$WIS\":23,\"$tha\":0.057749999999999996,\"$tpa\":0.03,\"_cdi\":1,\"_sp\":0,\"$cpo\":1.1,\"_chi\":1,\"$cpi\":1,\"_cdo\":1,\"_tbl\":0,\"_tpa\":0.03,\"_cho\":1,\"$LCK\":14,\"$shb\":0,\"$STR\":120,\"$sem\":0.12000000000000001,\"_ap\":0,\"_sbm\":0.7,\"$cmi\":0.92,\"$cai\":0.9,\"$tbl\":0,\"_srm\":0.7,\"_cai\":1,\"$DEX\":77,\"_STR\":61,\"$ap\":240,\"$cao\":1,\"_DEX\":50,\"$sbm\":0.61,\"$cmc\":1,\"$INT\":32,\"_cpi\":1,\"$chi\":1,\"$cho\":1,\"_INT\":11,\"_scm\":1.5,\"_cao\":1,\"_END\":68,\"_WIS\":23,\"_shb\":0,\"_tre\":0.07,\"$cdo\":1,\"$tcr\":0.16999999999999998,\"$END\":123,\"$cdi\":1,\"_cpo\":1,\"$scm\":1.675,\"_tcr\":0.05,\"_tha\":0,\"_thi\":0,\"$srm\":0.7,\"$cmo\":1,\"$sp\":64,\"_LCK\":14,\"_cmc\":1,\"$tre\":0.07,\"_smb\":0},\"wDPS\":69}}}");
        send(sendPack, true);
    }
    protected void moveToCell(String frame, String pad)
    {
        Packet sendPack = new Packet();
        this.playerRoom.pad[this.playerSlot] = pad;
        this.playerRoom.frame[this.playerSlot] = frame;
        this.playerRoom.tx[this.playerSlot] = 0;
        this.playerRoom.ty[this.playerSlot] = 0;
        this.monfighting = 0;
        this.fighting = false;
        sendPack.addString("%xt%uotls%-1%");
        sendPack.addString(this.account);
        sendPack.addString("%strPad:");
        sendPack.addString(pad);
        sendPack.addString(",tx:0,strFrame:");
        sendPack.addString(frame);
        sendPack.addString(",ty:0%");
        gameServer.writeMapPacket(this.account, sendPack, true, true);
        if(onpvp){
            debug("Sent moveToCell by ID \""+frame+"\",\""+pad+"\" : " + this.account);
        }
    }

    protected void moveToUser(int playerid)
    {
        Room otherroom = gameServer.room[gameServer.getPlayerRoom(gameServer.charName[playerid])[0]][gameServer.getPlayerRoom(gameServer.charName[playerid])[1]];
        int otherslot = otherroom.getPlayerSlot(gameServer.charName[playerid]);
        int newx = otherroom.tx[otherslot];
        int newy = otherroom.ty[otherslot];
        if (otherroom.tx[otherslot] > this.playerRoom.tx[this.playerSlot]) {
            newx -= 96;
        } else {
            newx += 96;
        }
        userMove(newx, newy, 16, true);
    }

    protected void partyAccept(int partyid)
    {
        Packet sendPack = new Packet();
        if (gameServer.partyRoom[this.accountid] == 0) {
            this.partyID = gameServer.addToParty(this.account, partyid);
            sendPack.addString("{\"t\":\"xt\",\"b\":{\"r\":-1,\"o\":{\"cmd\":\"pa\",\"ul\":");
            sendPack.addString(gameServer.party[this.partyID].getPlayers());
            sendPack.addString(",\"owner\":\""+gameServer.party[this.partyID].partyOwner+"\",\"pid\":"+this.partyID+"}}}");
            send(sendPack, true);
            sendPack.clean();
            sendPack.addString("{\"t\":\"xt\",\"b\":{\"r\":-1,\"o\":{\"cmd\":\"pa\",\"ul\":[\"");
            sendPack.addString(this.account);
            sendPack.addString("\"],\"owner\":\""+gameServer.party[this.partyID].partyOwner+"\",\"pid\":"+this.partyID+"}}}");
            gameServer.writePartyPacket(this.account, sendPack, true, true);
        } else {
            sendPack.addString("%xt%warning%-1%You are already in a party.%");
            send(sendPack, true);
        }
        sendPack.clean();
    }

    protected void partyDecline(int partyid)
    {
        Packet sendPack = new Packet();
        sendPack.addString("%xt%server%-1%You have declined the invitation.%");
        send(sendPack, true);
        sendPack.clean();
        sendPack.addString("{\"t\":\"xt\",\"b\":{\"r\":-1,\"o\":{\"cmd\":\"pd\",\"unm\":\"");
        sendPack.addString(this.account);
        sendPack.addString("\"}}}");
        gameServer.writePlayerPacket(gameServer.party[partyid].partyOwner, sendPack, true);
        sendPack.clean();
    }

    protected void partyKick(String otherchar)
    {
        Packet sendPack = new Packet();
        if (gameServer.partyRoom[gameServer.getPlayerID(otherchar)] == gameServer.partyRoom[this.accountid]) {
            sendPack.addString("{\"t\":\"xt\",\"b\":{\"r\":-1,\"o\":{\"cmd\":\"pr\",\"owner\":\""+gameServer.party[this.partyID].partyOwner+"\",\"pid\":"+this.partyID+",\"typ\":\"k\",\"unm\":\""+otherchar+"\"}}}");
            gameServer.writePartyPacket(this.account, sendPack, true, false);
            gameServer.leaveParty(otherchar);
        } else {
            sendPack.addString("%xt%warning%-1%That player is not in your party.%");
            send(sendPack, true);
        }
    }

    protected void partyInvite(String otherchar)
    {
        Packet sendPack = new Packet();
        if (gameServer.partyRoom[this.accountid] == 0) {
            this.partyID = gameServer.addToParty(this.account, 0);
        }
        sendPack.addString("%xt%server%-1%You have invited "+otherchar+" to join your party.%");
        send(sendPack, true);
        sendPack.clean();
        sendPack.addString("{\"t\":\"xt\",\"b\":{\"r\":-1,\"o\":{\"cmd\":\"pi\",\"owner\":\"");
        sendPack.addString(gameServer.party[this.partyID].partyOwner);
        sendPack.addString("\",\"pid\":"+this.partyID+"}}}");
        gameServer.writePlayerPacket(otherchar, sendPack, true);
    }

    protected void partyLeave()
    {
        Packet sendPack = new Packet();
        if (gameServer.partyRoom[this.accountid] != 0) {
            sendPack.addString("{\"t\":\"xt\",\"b\":{\"r\":-1,\"o\":{\"cmd\":\"pr\",\"owner\":\"");
            sendPack.addString(gameServer.party[this.partyID].partyOwner);
            sendPack.addString("\",\"pid\":"+this.partyID+",\"typ\":\"l\",\"unm\":\""+this.account+"\"}}}");
            gameServer.writePartyPacket(this.account, sendPack, true, false);
            gameServer.leaveParty(this.account);
            this.partyID = 0;
        } else {
            sendPack.addString("%xt%warning%-1%You are not in a party.%");
            send(sendPack, true);
        }
    }

    protected void partyPromote(String otherchar)
    {
        Packet sendPack = new Packet();
        if (gameServer.partyRoom[gameServer.getPlayerID(otherchar)] == gameServer.partyRoom[this.accountid]) {
            sendPack.addString("{\"t\":\"xt\",\"b\":{\"r\":-1,\"o\":{\"cmd\":\"pp\",\"owner\":\""+otherchar+"\"}}}");
            gameServer.writePartyPacket(this.account, sendPack, true, false);
            gameServer.party[gameServer.partyRoom[this.accountid]].partyOwner = otherchar;
        } else {
            sendPack.addString("%xt%warning%-1%That player is not in your party.%");
            send(sendPack, true);
        }
    }
    protected void partySummon(String otherchar)
    {
        Packet sendPack = new Packet();
        if (gameServer.partyRoom[gameServer.getPlayerID(otherchar)] == gameServer.partyRoom[this.accountid]) {
            sendPack.addString("%xt%server%-1%You attempt to summon "+otherchar+" to you.%");
            send(sendPack, true);
            sendPack.clean();
            sendPack.addString("{\"t\":\"xt\",\"b\":{\"r\":-1,\"o\":{\"cmd\":\"ps\",\"strF\":\""+this.playerRoom.frame[this.playerSlot]+"\",\"unm\":\""+this.account+"\",\"strP\":\""+this.playerRoom.pad[this.playerSlot]+"\"}}}");
            gameServer.writePlayerPacket(otherchar, sendPack, true);
        } else {
            sendPack.addString("%xt%warning%-1%That player is not in your party.%");
            send(sendPack, true);
        }
    }

    protected void partySummonAccept()
    {
        /*Packet sendPack = new Packet();
        sendPack.addString("%xt%server%-1%You declined the summon.%");
        send(sendPack, true);
        sendPack.clean();
        sendPack.addString("%xt%server%-1%"+this.account+" declined your summon.%");
        gameServer.writePlayerPacket(otherchar, sendPack, true);*/
    }

    protected int getRandomValueFromArray (int[] array) {
        int rnd = generator.nextInt(array.length);
        return array[rnd];
    }

    protected void partySummonDecline(String otherchar)
    {
        Packet sendPack = new Packet();
        sendPack.addString("%xt%server%-1%You declined the summon.%");
        send(sendPack, true);
        sendPack.clean();
        sendPack.addString("%xt%server%-1%"+this.account+" declined your summon.%");
        gameServer.writePlayerPacket(otherchar, sendPack, true);
    }
    protected void dropItem(int itemid)
    {
        Packet sendPack = new Packet();
	try
	{
            sendPack.clean();
            int success=0;
            if (itemid!=0){
            ResultSet rs = Main.sql.doquery("SELECT * FROM wqw_equipment WHERE itemID="+itemid);
            if (rs.next()) {
		sendPack.addString("{\"t\":\"xt\",\"b\":{\"r\":-1,\"o\":{\"cmd\":\"dropItem\",\"items\":" +
		"{\""+itemid+"\":{" +
		"\"sIcon\":\""+rs.getString("sIcon")+"\"," +
		"\"ItemID\":\""+itemid+"\"," +
		"\"iLvl\":\""+rs.getInt("iLvl")+"\"," +
		"\"sLink\":\""+rs.getString("sLink")+"\"," +
		"\"sElmt\":\""+rs.getString("sElmt")+"\"," +
		"\"bTemp\":\""+rs.getInt("bTemp")+"\"," +
		"\"bStaff\":\""+rs.getInt("bStaff")+"\"," +
		"\"iRng\":\""+rs.getInt("iRng")+"\"," +
		"\"bCoins\":\""+rs.getInt("bCoins")+"\"," +
		"\"iDPS\":\""+rs.getInt("iDPS")+"\"," +
		"\"sES\":\""+rs.getString("sES")+"\"," +
		"\"bPTR\":\"0\"," +
		"\"bitSuccess\":\"1\"," +
		"\"EnhID\":-1," +
		"\"sType\":\""+rs.getString("sType")+"\"," +
		"\"sDesc\":\""+rs.getString("sDesc")+"\"," +
		"\"iStk\":\""+rs.getInt("iStk")+"\"," +
		"\"iCost\":\""+rs.getInt("iCost")+"\"," +
		"\"bUpg\":\""+rs.getInt("bUpg")+"\"," +
		"\"bHouse\":\"\"," +
		"\"iRty\":"+rs.getInt("iRty")+"," +
		"\"sName\":\""+rs.getString("sName")+"\"," +
		"\"iQty\":"+rs.getInt("iQty")+"," +
		"\"sReqQuests\":\"\"}}}}}");
		success=1;
		}
		rs.close();
            }
            if(success==1){
		send(sendPack,true);
            }
            sendPack.clean();
	} catch(Exception e){
            debug("Exception in drop item: "+e.getMessage());
	}
    }
    
    protected void playerAttack(String attack, int turn)
    {
        Packet sendPack = new Packet();

        int monsterid[] = new int[5];
        int monsterid2[] = new int[5];
        int damage = (int)(50+(gameServer.level[this.accountid]*gameServer.level[this.accountid]/8)+(gameServer.level[this.accountid])*1.25);
        int damage2 = (int) (60+(gameServer.level[this.accountid]*gameServer.level[this.accountid]/4)+(gameServer.level[this.accountid])*1.25);
        int crit[] = new int[5];
        int dodge[] = new int[5];
        int damage3[] = new int[5];
        String type[] = new String[5];
        String skill[] = new String[5];
        String mToAttack[];
        if(attack.indexOf(",")!=-1){
            mToAttack = attack.split(",");
        } else {
            mToAttack = new String[1];
            mToAttack[0] = attack;
        }
        String hit[] = new String[5];

        boolean addState = false;

        Room room = this.playerRoom;
        int slot = room.getPlayerSlot(this.account);

        for(int i = 0; i < mToAttack.length; i++){
            String[] monsters = mToAttack[i].split(">");
            skill[i] = monsters[0];
            String[] tobehit = monsters[1].split(":");
            monsterid[i] = Integer.parseInt(tobehit[1]);
            monsterid2[i] = Integer.parseInt(tobehit[1]) - 1;
            type[i] = tobehit[0];
            crit [i] = this.generator.nextInt(15);
            dodge[i] = this.generator.nextInt(10);
        }

        for(int e = 0; e < mToAttack.length; e++){
            damage3[e] = damage + this.generator.nextInt(damage2-damage);
            hit[e]="hit";
            if (skill[e].equals("a2")){
                damage3[e]= damage3[e]*2;
                hit[e]="crit";
            }
            if (crit[e] > 13) {
                hit[e] = "crit";
                damage3[e] = damage3[e]*2;
            } else if (dodge[e] > 9) {
                hit[e] = "miss";
                damage3[e] = 0;
            }

            if (!skill[e].equals("i1")) {
                this.fighting = true;
            }
            if (this.playerRoom.monsterState[monsterid2[e]] == 1 && type[e].equals("m")) {
                this.monfighting = mToAttack.length;
                playerHitTimer(monsterid[e], room.frame[slot]);
            } else if (this.playerRoom.monsterState[monsterid2[e]] == 0 && type[e].equals("m")) {
                this.fighting = false;
            }

            if (gameServer.hp[this.accountid] >= 1 && type[e].equals("m") && !skill[e].equals("i1")) {
                if(this.playerRoom.monsterHP[monsterid2[e]]==this.playerRoom.monsterHP[monsterid2[e]]){
                    addState=true;
                }

                this.playerRoom.monsterHP[monsterid2[e]] -= damage3[e];
                    if (this.playerRoom.monsterHP[monsterid2[e]] <= 0 && this.playerRoom.monsterState[monsterid2[e]] >=1 && type[e].equals("m")) {
                        this.playerRoom.monsterHP[monsterid2[e]] = 0;
                        this.playerRoom.monsterMP[monsterid2[e]] = 0;
                        this.monfighting -=1;
                            this.fighting=false;
                        
                        this.playerRoom.respawnMonster(monsterid2[e], this.playerRoom.monsterType[monsterid2[e]]);
                        Main.sql.doupdate("UPDATE wqw_users SET monkill=monkill+1 WHERE username='"+this.account+"'");
                        this.monkilled++;
                        addState = true;
                    }
            } else if (type[e].equals("p") && gameServer.hp[this.accountid] >= 1 && !skill[e].equals("i1")) {
                 if (gameServer.isAlive[monsterid[e]] == true && type[e].equals("p") && monsterid[e]!=this.accountid) {
                    if(gameServer.hp[monsterid[e]]==gameServer.hpmax[monsterid[e]]){
                        addState = true;
                    }
                    gameServer.hp[monsterid[e]] -= damage3[e];
                    if(gameServer.hp[monsterid[e]]<=0){
                        gameServer.hp[monsterid[e]] = 0;
                        gameServer.mp[monsterid[e]] = 0;
                        this.monfighting = 0;
                        this.fighting = false;
                        addPvPTeamScore(gameServer.pvpteam[this.accountid], 50);
                        Main.sql.doupdate("UPDATE wqw_users SET pvpkill=pvpkill+1 WHERE username='"+this.account+"'");
                        gameServer.isAlive[monsterid[e]] = false;
                        addState = true;
                    }
                }
            }
        }

        if(skill[0].equals("aa")){
            sendPack.addString(skillPacket(0, addState ,mToAttack, room.frame[slot], type, hit, damage3,monsterid, turn));
        } else if(skill[0].equals("a1")){
            gameServer.mp[this.accountid] -= mpcost[1];
            sendPack.addString(skillPacket(1, addState ,mToAttack, room.frame[slot], type, hit, damage3,monsterid, turn));
        } else if(skill[0].equals("a2")){
            gameServer.mp[this.accountid] -= mpcost[2];
            sendPack.addString(skillPacket(2, addState ,mToAttack, room.frame[slot], type, hit, damage3,monsterid, turn));
        } else if(skill[0].equals("a3")){
            gameServer.mp[this.accountid] -= mpcost[3];
            sendPack.addString(skillPacket(3, addState ,mToAttack, room.frame[slot], type, hit, damage3,monsterid, turn));
        } else if(skill[0].equals("a4")){
            gameServer.mp[this.accountid] -= mpcost[4];
            sendPack.addString(skillPacket(4, addState ,mToAttack, room.frame[slot], type, hit, damage3,monsterid, turn));
        } else if(skill[0].equals("i1")){
            int hdamage = damage + this.generator.nextInt(damage2-damage);
            if(gameServer.hp[monsterid[0]]<gameServer.hpmax[monsterid[0]]){
                gameServer.hp[monsterid[0]] += hdamage;
            } else {
                gameServer.hp[monsterid[0]]= gameServer.hpmax[monsterid[0]];
            }
            sendPack.addString("{\"t\":\"xt\",\"b\":{\"r\":-1,\"o\":{\"cmd\":\"ct\",\"anims\":[{\"strFrame\":\""+room.frame[slot]+
                    "\",\"cInf\":\"p:"+this.accountid+"\",\"fx\":\"w\",\"animStr\":\"Salute\",\"tInf\":\""+type[0]+":"+monsterid[0]+"\",\"strl\":\"sp_eh1\"}],\"a\":[{\"cInf\":\"p:"+this.accountid+
                    "\",\"cmd\":\"aura+\",\"auras\":[{\"nam\":\"Heal\",\"t\":\"s\",\"dur\":5,\"isNew\":true}],\"tInf\":\""+type[0]+":"+monsterid[0]+"\"}],\"p\":{\""+gameServer.charName[monsterid[0]]+
                    "\":{\"intHP\":"+gameServer.hp[monsterid[0]]+"}},\"sarsa\":[{\"cInf\":\"p:"+this.accountid+
                    "\",\"a\":[{\"hp\":-"+hdamage+",\"tInf\":\""+type[0]+":"+monsterid[0]+"\",\"type\":\"hit\"}],\"actID\":"+turn+",\"iRes\":1}]}}}");
            int pot[] = new int[1];
            int qty[] = new int[1];
            pot[0]=1749;
            qty[0]=1;
            turnInItem(pot,qty);
        }
        gameServer.writeMapPacket(this.account, sendPack, true, false);
    }

    protected String skillPacket(int i,boolean addState,String[] monsters, String frame, String[] type, String[] hit, int[] damage3, int[] monsterid, int turn)
    {
        Packet sendPack = new Packet();

        String returnThis;

        /* Initialize the variables */

        int monsterid2[] = new int[5];
        boolean addMonsters = false;
        for(int x=0; x<monsters.length; x++){
            monsterid2[x] = monsterid[x] - 1;
        }
        returnThis = "{\"t\":\"xt\",\"b\":{\"r\":-1,\"o\":{\"cmd\":\"ct\",\"anims\":[{\"strFrame\":\""+frame+"\",\"cInf\":\"p:"+this.accountid+"\",\"fx\":\""+this.skillfx[i]+"\",\"animStr\":\""+this.anim[i]+"\",\"tInf\":\"";
        /* Adds the player or monster ids to be healed or attack */
        for(int z=0; z<monsters.length; z++){
            if(z!=0){
                returnThis+=",";
            }
            returnThis+=type[z]+":"+monsterid[z];
        }
        returnThis+="\",\"strl\":\""+this.str1[i]+"\"}],";

        /* Checks if skill has an aura or not */

        if(!this.auras[i].equals("")){
            final String[] omg = this.auras[i].split(",");
            int time = Integer.parseInt(omg[1]) * 1000;
            String omgx="none";
            addAura(omg[0], time);
            returnThis += "\"a\":[{\"cInf\":\"p:"+this.accountid+"\",\"cmd\":\"aura+\",\"auras\":[{\"nam\":\""+omg[0]+"\",\"t\":\"s\"";
            if(skillfx[i].equals("m")){
                returnThis+=",\"s\":\"s\",\"cat\":\"stun\"";
            }
            returnThis+= ",\"dur\":"+omg[1]+",\"isNew\":true}],";
            returnThis += "\"tInf\":\""+type[0]+":"+monsterid[0]+"\"}],\"p\":{\""+this.account+"\":{\"intMP\":"+gameServer.mp[this.accountid]+"}},\"sarsa\":[{\"cInf\":\"p:"+this.accountid+"\",\"a\":[{\"hp\":0,\"tInf\":\"p:"+this.accountid+"\",\"type\":\""+omgx+"\"}],\"actID\":"+turn+",\"iRes\":1}]}}}";
        } else {
            returnThis+="\"sarsa\":[{\"cInf\":\"p:"+this.accountid+"\",\"a\":[";

            /* Shows the damage for each monster or player ids */

            for(int a = 0; a<monsters.length; a++){
                if(a!=0){
                    returnThis+=",";
                }
                if(type[a].equals("m")){

                    /* Monster type is found set to true to add monster uotls */

                    addMonsters = true;
                }
                returnThis+="{\"hp\":"+damage3[a]+",\"tInf\":\""+type[a]+":"+monsterid[a]+"\",\"type\":\""+hit[a]+"\"}";  
            }
            returnThis+="],\"actID\":"+turn+",\"iRes\":1}],";

            /* Refreshes all the player ids to their real current hp or level and etc... */

            returnThis+="\"p\":{";
            returnThis += "\""+this.account+"\":{\"intMP\":"+gameServer.mp[this.accountid];
            if(addState && this.fighting) {
                returnThis +=",\"intState\":2";
            } else if (addState) {
                returnThis +=",\"intState\":1";
            }
            returnThis+="}";
            for(int b = 0; b<monsters.length; b++){
                if(type[b].equals("p")){
                    returnThis+=",\""+gameServer.charName[monsterid[b]]+"\":{\"intHP\":"+gameServer.hp[monsterid[b]];
                    if(gameServer.hp[monsterid[b]]<=0){
                        /* Player died, set its state to dead */
                        returnThis+=",\"intMP\":0,\"intState\":0";
                        sendPack.clean();
                        sendPack.addString(addMonsterRewards(0, type[b], monsterid[b]));
                        send(sendPack, true);
                    } else if(addState && this.fighting) {
                        /* Player is fighting, set its state to fighting */
                        returnThis +=",\"intState\":2";
                    }
                    returnThis+="}";
                }
            }
            returnThis+="}";

            /* Refreshes all the monster ids to their real current hp or level and etc... */

            if(addMonsters){
                returnThis+=",\"m\":{";
                for(int b = 0; b<monsters.length; b++){
                    if(b!=0){
                        returnThis+=",";
                    }
                    returnThis+="\""+monsterid[b]+"\":{\"intHP\":";
                    if(this.playerRoom.monsterHP[monsterid2[b]]<=0){
                        returnThis+="0";
                    } else {
                        returnThis+=this.playerRoom.monsterHP[monsterid2[b]];
                    }
                    if(this.playerRoom.monsterHP[monsterid2[b]]<=0 && this.playerRoom.monsterState[monsterid2[b]]!=0){
                        this.playerRoom.monsterState[monsterid2[b]]=0;
                        /* Monster died, set its state to dead */
                        returnThis+=",\"intMP\":0,\"intState\":0";
                        sendPack.clean();
                        sendPack.addString(addMonsterRewards(this.playerRoom.monsterType[monsterid2[b]], type[b], monsterid[b]));
                        send(sendPack, true);
                        int isdropped = generator.nextInt(95);
                        if(this.droppercent[monsterid2[b]] > isdropped){
                            dropItem(Integer.parseInt(this.drops[monsterid2[b]]));
                            //debug("DRopping from monster id: "+(monsterid2[b] + 1));
                        }
                        if(onpvp){
                            addPvPTeamScore(gameServer.pvpteam[this.accountid],25);
                        }
                    }
                    returnThis+="}";
                }
            }
            returnThis+="}}}}";
            if(gameServer.level[this.accountid]!=100){
                levelUp();
            }
        }
        return returnThis;
        /* Skill Packet for v1.0 by Zeroskull */
    }
    protected void addAura(final String aura, int time)
    {
        Packet sendPack = new Packet();
        gameServer.writeMapPacket(this.account, sendPack, true, false);
        Timer timer = new Timer();
        timer.schedule(new TimerTask()
        {
            @Override
            public void run() {
                removeAura(aura);
            }
        }, time);
    }

    protected void removeAura(String name)
    {
        Packet sendPack = new Packet();
        sendPack.addString("{\"t\":\"xt\",\"b\":{\"r\":-1,\"o\":{\"cmd\":\"ct\",\"a\":[{\"cmd\":\"aura-\",\"aura\":{\"nam\":\""+name+"\"},\"tInf\":\"p:"+this.accountid+"\"}]}}}");
        gameServer.writeMapPacket(this.account, sendPack, true, false);
    }

    protected void overKill(int monsterid)
    {
        Packet sendPack = new Packet();

        Room room = this.playerRoom;
        int slot = room.getPlayerSlot(gameServer.charName[monsterid]);
        gameServer.hp[monsterid]=0;
        int mid[] = {monsterid};
        int damage3[] = {999999999};
        String hit[] = {"crit"};
        String type[] = {"p"};
        String mToAttack[] = new String[1];
        sendPack.addString(skillPacket(0, false ,mToAttack, room.frame[slot], type, hit, damage3,mid, 0));
        gameServer.writeMapPacket(this.account, sendPack, true, false);
    }
    protected void playerHitTimer(final int monsterid, final String frame){
        Timer timer = new Timer();
        final int monsterid2 = monsterid - 1;
        int rand = generator.nextInt(500);
        int time = 1000 + rand;
        timer.schedule(new TimerTask()
        {
            @Override
            public void run() {
                if(playerRoom.monsterState[monsterid2] != 0 && gameServer.hp[accountid]>0 && frame.equals(playerRoom.monsterFrame[monsterid2])){
                    playerHit(monsterid);
                    playerHitTimer(monsterid, frame);
                }
            }
        }, time);
    }
    
    protected void playerHit(int monsterid)
    {
        Packet sendPack = new Packet();
        int monsterid2 = monsterid - 1;
        int damage = (int)(30+(this.playerRoom.monsterLevel[monsterid2]*this.playerRoom.monsterLevel[monsterid2]/8)+(this.playerRoom.monsterLevel[monsterid2])*1.25);
        int damage2 = (int) (40+(this.playerRoom.monsterLevel[monsterid2]*this.playerRoom.monsterLevel[monsterid2]/4)+(this.playerRoom.monsterLevel[monsterid2])*1.25);
        int damage3 = damage + this.generator.nextInt(damage2-damage);
        int crit = this.generator.nextInt(20);
        int dodge = this.generator.nextInt(20);
        String hit = "hit";
        if (crit > 15) {
            hit = "crit";
            damage3 = damage3*2;
        } else if (dodge > 18) {
            hit = "dodge";
            damage3 = 0;
        }
        Room room = this.playerRoom;
        gameServer.hp[this.accountid] -= damage3;
        if (gameServer.hp[this.accountid] <= 0) {
            this.fighting = false;
            this.playerRoom.monsterState[monsterid2]=1;
            gameServer.hp[this.accountid] = 0;
            sendPack.addString("{\"t\":\"xt\",\"b\":{\"r\":-1,\"o\":{\"cmd\":\"ct\",\"anims\":[{\"strFrame\":\""+room.monsterFrame[monsterid]+"\",\"cInf\":\"m:"+monsterid+"\",\"fx\":\"m\",\"animStr\":\"Attack1,Attack2\",\"tInf\":\"p:"+this.accountid+"\"}],\"m\":{\""+monsterid+"\":{\"intState\":1}},\"p\":{\""+this.account+"\":{\"intHP\":0,\"intMP\":0,\"intState\":0}},\"sara\":[{\"actionResult\":{\"hp\":"+damage3+",\"cInf\":\"m:"+monsterid+"\",\"tInf\":\"p:"+this.accountid+"\",\"type\":\""+hit+"\"},\"iRes\":1}]}}}");
            gameServer.writeMapPacket(this.account, sendPack, true, false);
            sendPack.clean();
        } else {
            this.playerRoom.monsterState[monsterid2]=2;
            sendPack.addString("{\"t\":\"xt\",\"b\":{\"r\":-1,\"o\":{\"cmd\":\"ct\",\"anims\":[{\"strFrame\":\""+room.monsterFrame[monsterid]+"\",\"cInf\":\"m:"+monsterid+"\",\"fx\":\"m\",\"animStr\":\"Attack1,Attack2\",\"tInf\":\"p:"+this.accountid+"\"}],\"p\":{\""+this.account+"\":{\"intHP\":"+gameServer.hp[this.accountid]+"}},\"sara\":[{\"actionResult\":{\"hp\":"+damage3+",\"cInf\":\"m:"+monsterid+"\",\"tInf\":\"p:"+this.accountid+"\",\"type\":\""+hit+"\"},\"iRes\":1}]}}}");
            gameServer.writeMapPacket(this.account, sendPack, true, false);

            /* Uncomment the commented lines if you want an aqw style of not seeing the damage */

            //send(sendPack, true);
            sendPack.clean();
            //sendPack.addString("{\"t\":\"xt\",\"b\":{\"r\":-1,\"o\":{\"cInf\":\"m:"+monsterid+"\",\"anim\":\"Attack1,Attack2\",\"cmd\":\"anim\",\"fx\":\"m\",\"tInf\":\"p:"+this.accountid+"\",\"strl\":\"\"}}}");
            //gameServer.writeMapPacket(this.account, sendPack, true, false);
        }
    }

    protected void playerTimerAttack()
    {
        Packet sendPack = new Packet();
        sendPack.addString("%xt%hi%-1%");
        send(sendPack, true);
        sendPack.clean();
        //playerHit(this.monfighting);
    }

    protected void respawnPlayer()
    {
        Packet sendPack = new Packet();
        if(onpvp){
            sendPack.addString("%xt%resTimed%-1%Enter"+gameServer.pvpteam[this.accountid]+"%Spawn%");
        } else {
            sendPack.addString("%xt%resTimed%-1%Enter%Spawn%");
        }
        send(sendPack, true);
        this.monfighting = 0;
        gameServer.hp[this.accountid] = gameServer.hpmax[this.accountid];
        gameServer.mp[this.accountid] = gameServer.mpmax[this.accountid];
        gameServer.isAlive[this.accountid] = true;
        sendPack.clean();
        sendPack.addString("{\"t\":\"xt\",\"b\":{\"r\":-1,\"o\":{\"cmd\":\"uotls\",\"o\":{\"intHP\":"+gameServer.hpmax[this.accountid]+",\"intMP\":"+gameServer.mpmax[this.accountid]+",\"intState\":1},\"unm\":\""+this.account+"\"}}}");
        gameServer.writeMapPacket(this.account, sendPack, true, false);
        Main.sql.doupdate("UPDATE wqw_users SET killed=killed+1 WHERE username='"+this.account+"'");
    }

    protected void restPlayer()
    {
        int newhp = getHPwithNecklace();
        if(onpvp){
            gameServer.hpmax[this.accountid] = newhp;
        }
        gameServer.hp[this.accountid] += gameServer.hpmax[this.accountid]/20;
        if (gameServer.hp[this.accountid] > gameServer.hpmax[this.accountid]) {
            gameServer.hp[this.accountid] = gameServer.hpmax[this.accountid];
        }
        gameServer.mp[this.accountid] += gameServer.mpmax[this.accountid]/20;
        if (gameServer.mp[this.accountid] > gameServer.mpmax[this.accountid]) {
            gameServer.mp[this.accountid] = gameServer.mpmax[this.accountid];
        }
        sendUotls(true,false,true,false,false);
    }

    protected void requestFriend(String otherchar)
    {
        Packet sendPack = new Packet();
        sendPack.addString("%xt%server%-1%You have requested "+otherchar+" to be friends.%");
        send(sendPack, true);
        sendPack.clean();
        sendPack.addString("{\"t\":\"xt\",\"b\":{\"r\":-1,\"o\":{\"cmd\":\"requestFriend\",\"unm\":\""+this.account+"\"}}}");
        gameServer.writePlayerPacket(otherchar, sendPack, true);
    }

     boolean isInteger(String input){
        try{
            Integer.parseInt(input);
            return true;
        }
        catch(NumberFormatException nfe){
            return false;
        }
    }
    protected void retrieveUserDatas(String Packet)
    {
        try
        {
            String packet_handled[] = Packet.split("%");
            Packet sendPack = new Packet();
            sendPack.addString("{\"t\":\"xt\",\"b\":{\"r\":-1,\"o\":{\"cmd\":\"initUserDatas\",\"a\":[");
            for (int i = 2; i < packet_handled.length; i++) {
                if(isInteger(packet_handled[i])){
                    if(i != 2){
                        sendPack.addString(",");
                    }

                    sendPack.addString("{");
                    int pID = Integer.parseInt(packet_handled[i]);
                    int id = gameServer.userID[pID];

                    debug("Retrieving user data for uID: "+id+", pID: "+pID+", Character: "+gameServer.charName[pID]);

                    int classPoints = gameServer.getClassPoints(id);
                    String className = getClassName(id);
                    String equipment = getEquipment(id);
                    Room room = this.playerRoom;
                    ResultSet rs = Main.sql.doquery("SELECT * FROM wqw_users WHERE id="+id+" LIMIT 1");

                    if(rs.next()){
                        String userName = rs.getString("username").toLowerCase();
                        int playerslot = room.getPlayerSlot(userName);
                        int playerLevel = rs.getInt("level");
                        if(userName.equals(this.account)){
                            this.playerlevel = playerLevel;
                        }
                        sendPack.addString("\"uid\":");
                        sendPack.addInt(gameServer.getPlayerID(userName));
                        sendPack.addString(",\"strFrame\":");
                        sendPack.addString("\"" + room.frame[playerslot] + "\"");
                        sendPack.addString(",\"strPad\":");
                        sendPack.addString("\"" + room.pad[playerslot] + "\"");
                        sendPack.addString(",\"data\":{\"intColorAccessory\":\"");
                        sendPack.addInt(rs.getInt("cosColorAccessory"));
                        sendPack.addString("\",\"iCP\":");
                        sendPack.addInt(classPoints);
                        sendPack.addString(",\"intLevel\":\"");
                        sendPack.addInt(playerLevel);
                        sendPack.addString("\",\"iBagSlots\":");
                        sendPack.addInt(rs.getInt("slotBag"));
                        sendPack.addString(",\"ig0\":0,\"iUpgDays\":\"-");
                        sendPack.addInt(rs.getInt("upgDays"));
                        sendPack.addString("\",\"intColorBase\":\"");
                        sendPack.addInt(rs.getInt("cosColorBase"));
                        sendPack.addString("\",\"sCountry\":\"US\"");
                        sendPack.addString(",\"iSTR\":\"");
                        sendPack.addInt(rs.getInt("str"));
                        sendPack.addString("\",\"ip0\":0,\"iq0\":0,\"iAge\":\"");
                        sendPack.addInt(rs.getInt("age"));
                        sendPack.addString("\",\"iWIS\":\"");
                        sendPack.addInt(rs.getInt("WIS"));
                        sendPack.addString("\",\"intExpToLevel\":\"");
                        sendPack.addInt(getXpToLevel(playerLevel)); //Calculate this
                        sendPack.addString("\",\"intGold\":");
                        sendPack.addInt(rs.getInt("gold"));
                        sendPack.addString(",\"intMP\":");
                        sendPack.addInt(gameServer.calculateMP(playerLevel)); //Calculate this
                        sendPack.addString(",\"sHouseInfo\":[]");
                        sendPack.addString(",\"iBankSlots\":");
                        sendPack.addInt(rs.getInt("slotBank"));
                        sendPack.addString(",\"iHouseSlots\":");
                        sendPack.addInt(rs.getInt("slotHouse"));
                        sendPack.addString(",\"id0\":0,\"intColorSkin\":\"");
                        sendPack.addInt(rs.getInt("plaColorSkin"));
                        sendPack.addString("\",\"intMPMax\":");
                        sendPack.addInt(gameServer.calculateMP(playerLevel)); //Calculate this
                        sendPack.addString(",\"intHPMax\":");
                        sendPack.addInt(gameServer.calculateHP(playerLevel)); //Calculate this
                        sendPack.addString(",\"dUpgExp\":\"");
                        sendPack.addString("2012-01-20T17:53:00"/*+rs.getString("upgDate")*/);
                        sendPack.addString("\",\"iUpg\":\"");
                        sendPack.addInt(rs.getInt("upgrade"));
                        sendPack.addString("\",\"CharID\":\"");
                        sendPack.addInt(id);
                        sendPack.addString("\",\"strEmail\":\"none\"");
                        sendPack.addString(",\"iINT\":\"");
                        sendPack.addInt(rs.getInt("INT"));
                        sendPack.addString("\",\"intColorTrim\":\"");
                        sendPack.addInt(rs.getInt("cosColorTrim"));
                        sendPack.addString("\",\"lastArea\":\"");
                        sendPack.addString(rs.getString("lastVisited"));
                        sendPack.addString("\",\"iFounder\":\"1\"");
                        sendPack.addString(",\"intDBExp\":");
                        sendPack.addInt(rs.getInt("xp"));
                        sendPack.addString(",\"intExp\":");
                        sendPack.addInt(rs.getInt("xp"));
                        sendPack.addString(",\"UserID\":\"");
                        sendPack.addInt(id);
                        sendPack.addString("\",\"ia1\":\"0\",\"ia0\":0,\"intHP\":");
                        sendPack.addInt(gameServer.calculateHP(playerLevel)); //Calculate this
                        sendPack.addString(",\"dCreated\":\"0000-00-00T00:00:00\"");
                        sendPack.addString(",\"strQuests\":\"ZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZ\",\"bitSuccess\":\"1\",\"strHairName\":\"");
                        sendPack.addString(rs.getString("hairName"));
                        sendPack.addString("\",\"intColorEye\":\"");
                        sendPack.addInt(rs.getInt("plaColorEyes"));
                        sendPack.addString("\",\"iLCK\":\"");
                        sendPack.addInt(rs.getInt("LCK"));
                        sendPack.addString("\",\"eqp\":{");
                        sendPack.addString(equipment);
                        sendPack.addString("},\"iDBCP\":");
                        sendPack.addInt(classPoints);
                        sendPack.addString(",\"intDBGold\":");
                        sendPack.addInt(rs.getInt("gold"));
                        sendPack.addString(",\"strClassName\":\"");
                        sendPack.addString(className);
                        sendPack.addString("\",\"intActivationFlag\":\"");
                        sendPack.addInt(rs.getInt("emailActive"));
                        sendPack.addString("\",\"intAccessLevel\":\"");
                        sendPack.addInt(rs.getInt("access"));
                        sendPack.addString("\",\"strHairFilename\":\"");
                        sendPack.addString(rs.getString("hairFile"));
                        sendPack.addString("\",\"intColorHair\":\"");
                        sendPack.addInt(rs.getInt("plaColorHair"));
                        sendPack.addString("\",\"HairID\":\"");
                        sendPack.addInt(rs.getInt("hairID"));
                        sendPack.addString("\",\"strGender\":\"");
                        sendPack.addString(rs.getString("gender"));
                        sendPack.addString("\",\"strUsername\":\"");
                        sendPack.addString(userName);
                        sendPack.addString("\",\"iDEX\":\"");
                        sendPack.addInt(rs.getInt("DEX"));
                        sendPack.addString("\",\"intCoins\":");
                        sendPack.addInt(rs.getInt("coins"));
                        sendPack.addString(",\"iEND\":\"");
                        sendPack.addInt(rs.getInt("END"));
                        sendPack.addString("\",\"strMapName\":\"");
                        sendPack.addString(room.roomName + "\"");
                    }
                    sendPack.addString("}}");
                    rs.close();
                }
            }
            sendPack.addString("]}}}");
            send(sendPack, true);
            sendPack.clean();
        }
        catch(Exception e)
        {
            debug("Exception in retrieve user datas: "+e.getMessage()+", uid: "+Packet);
        }
    }
    protected void retrieveUserData(int id2, boolean doAgain)
    {
        int uid = id2;
        try {
            int id = gameServer.userID[id2];
            if (id > 0) {
                debug("Attempting to retrieve user data: "+id+", "+gameServer.charName[id2]);
                int cp = gameServer.getClassPoints(id);
                String cn = getClassName(id);
                String equip = getEquipment(id);
                Packet sendPack = new Packet();
                Room room = this.playerRoom; //gameServer.room[gameServer.getPlayerRoom(user)[0]][gameServer.getPlayerRoom(user)[1]];
                ResultSet rs = Main.sql.doquery("SELECT * FROM wqw_users WHERE id="+id);
                if (rs.next()) {
                    String user = rs.getString("username");
                    user = user.toLowerCase();
                    int slot = room.getPlayerSlot(user);
                    int level = rs.getInt("level");
                    if (id == this.userid) {
                        this.playerlevel = level;

                    }
                    sendPack.addString("{\"t\":\"xt\",\"b\":{\"r\":-1,\"o\":{\"uid\":");
                    sendPack.addInt(gameServer.getPlayerID(user));
                    sendPack.addString(",\"strFrame\":\"");
                    sendPack.addString(room.frame[slot]);
                    sendPack.addString("\",\"cmd\":\"initUserData\",\"strPad\":\"");
                    sendPack.addString(room.pad[slot]);
                    sendPack.addString("\",\"data\":{\"intColorAccessory\":\"");
                    sendPack.addInt(rs.getInt("cosColorAccessory"));
                    sendPack.addString("\",\"iCP\":");
                    sendPack.addInt(cp);
                    sendPack.addString(",\"intLevel\":\"");
                    sendPack.addInt(level);
                    sendPack.addString("\",\"iBagSlots\":");
                    sendPack.addInt(rs.getInt("slotBag"));
                    sendPack.addString(",\"ig0\":0,\"iUpgDays\":\"-");
                    sendPack.addInt(rs.getInt("upgDays"));
                    sendPack.addString("\",\"intColorBase\":\"");
                    sendPack.addInt(rs.getInt("cosColorBase"));
                    sendPack.addString("\",\"iSTR\":\"");
                    sendPack.addInt(rs.getInt("str"));
                    sendPack.addString("\",\"ip0\":0,\"iq0\":0,\"iAge\":\"");
                    sendPack.addInt(rs.getInt("age"));
                    sendPack.addString("\",\"iWIS\":\"");
                    sendPack.addInt(rs.getInt("WIS"));
                    sendPack.addString("\",\"intExpToLevel\":\"");
                    sendPack.addInt(getXpToLevel(level)); //Calculate this
                    sendPack.addString("\",\"intGold\":");
                    sendPack.addInt(rs.getInt("gold"));
                    sendPack.addString(",\"intMP\":");
                    sendPack.addInt(gameServer.calculateMP(level)); //Calculate this
                    sendPack.addString(",\"iBankSlots\":");
                    sendPack.addInt(rs.getInt("slotBank"));
                    sendPack.addString(",\"iHouseSlots\":");
                    sendPack.addInt(rs.getInt("slotHouse"));
                    sendPack.addString(",\"id0\":0,\"intColorSkin\":\"");
                    sendPack.addInt(rs.getInt("plaColorSkin"));
                    sendPack.addString("\",\"intMPMax\":");
                    sendPack.addInt(gameServer.calculateMP(level)); //Calculate this
                    sendPack.addString(",\"intHPMax\":");
                    sendPack.addInt(gameServer.calculateHP(level)); //Calculate this
                    sendPack.addString(",\"dUpgExp\":\"");
                    sendPack.addString("2009-01-20T17:53:00"/*+rs.getString("upgDate")*/);
                    sendPack.addString("\",\"iUpg\":\"");
                    sendPack.addInt(rs.getInt("upgrade"));
                    sendPack.addString("\",\"CharID\":\"");
                    sendPack.addInt(id);
                    sendPack.addString("\",\"strClassName\":\"");
                    sendPack.addString(cn);
                    sendPack.addString("\",\"iINT\":\"");
                    sendPack.addInt(rs.getInt("INT"));
                    sendPack.addString("\",\"ItemID\":\"");
                    sendPack.addInt(rs.getInt("currentClass"));
                    sendPack.addString("\",\"lastArea\":\"");
                    sendPack.addString(rs.getString("lastVisited"));
                    sendPack.addString("\",\"intColorTrim\":\"");
                    sendPack.addInt(rs.getInt("cosColorTrim"));
                    sendPack.addString("\",\"intDBExp\":");
                    sendPack.addInt(rs.getInt("xp"));
                    sendPack.addString(",\"intExp\":");
                    sendPack.addInt(rs.getInt("xp"));
                    sendPack.addString(",\"UserID\":\"");
                    sendPack.addInt(id);
                    sendPack.addString("\",\"ia1\":\"0\",\"ia0\":0,\"intHP\":");
                    sendPack.addInt(gameServer.calculateHP(level)); //Calculate this
                    sendPack.addString(",\"strQuests\":\"ZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZ\",\"bitSuccess\":\"1\",\"strHairName\":\"");
                    sendPack.addString(rs.getString("hairName"));
                    sendPack.addString("\",\"intColorEye\":\"");
                    sendPack.addInt(rs.getInt("plaColorEyes"));
                    sendPack.addString("\",\"iLCK\":\"");
                    sendPack.addInt(rs.getInt("LCK"));
                    sendPack.addString("\",\"eqp\":{");
                    sendPack.addString(equip);
                    sendPack.addString("},\"iDBCP\":");
                    sendPack.addInt(cp);
                    sendPack.addString(",\"intDBGold\":");
                    sendPack.addInt(rs.getInt("LCK"));
                    sendPack.addString(",\"intActivationFlag\":\"");
                    sendPack.addInt(rs.getInt("emailActive"));
                    sendPack.addString("\",\"intAccessLevel\":\"");
                    sendPack.addInt(rs.getInt("access"));
                    sendPack.addString("\",\"strHairFilename\":\"");
                    sendPack.addString(rs.getString("hairFile"));
                    sendPack.addString("\",\"intColorHair\":\"");
                    sendPack.addInt(rs.getInt("plaColorHair"));
                    sendPack.addString("\",\"HairID\":\"");
                    sendPack.addInt(rs.getInt("hairID"));
                    sendPack.addString("\",\"strGender\":\"");
                    sendPack.addString(rs.getString("gender"));
                    sendPack.addString("\",\"strUsername\":\"");
                    sendPack.addString(user);
                    sendPack.addString("\",\"iDEX\":\"");
                    sendPack.addInt(rs.getInt("DEX"));
                    sendPack.addString("\",\"intCoins\":");
                    sendPack.addInt(rs.getInt("coins"));
                    sendPack.addString(",\"iEND\":\"");
                    sendPack.addInt(rs.getInt("END"));
                    sendPack.addString("\",\"strMapName\":\"");
                    sendPack.addString(room.roomName);
                    sendPack.addString("\"}}}}");
                    send(sendPack,true);
                    sendPack.clean();
                    rs.close();
                }
            }
        } catch (Exception e) {
            debug("Exception in retrieve user data: "+e.getMessage()+", uid: "+uid);
            try {
                Thread.sleep(200);
            } catch (Exception e2) {
                debug("retrieveUserData sleep failed: "+e2.getMessage());
            }
            if (doAgain) {
                retrieveUserData(uid, false);
            } else {
                if (gameServer.userID[id2] == this.userid) {
                    Packet sendPack = new Packet();
                    sendPack.addString("%xt%logoutWarning%-1%%15%");
                    send(sendPack, true);
                    this.finalize();
                }
            }
        }
    }

    protected void sellItem(int itemid, int adjustid)
    {
        try {
            int sellprice = 0;
            int iscoins = 0;
            int qty = 1;
            String isitem="";
            Packet sendPack = new Packet();
            ResultSet es = Main.sql.doquery("SELECT * FROM wqw_equipment WHERE itemid="+itemid);
            if (es.next()) {
                sellprice = es.getInt("iCost")/4;
                iscoins = es.getInt("bCoins");
                isitem = es.getString("sType");
                qty = es.getInt("iQty");
            }
            es.close();
            ResultSet rs = Main.sql.doquery("SELECT * FROM wqw_items WHERE userid="+this.userid+" AND id="+adjustid+" AND equipped=0 AND itemid="+itemid);
            if (rs.next()) {
                
                if(iscoins!=1){
                    Main.sql.doupdate("UPDATE wqw_users SET gold=gold+"+sellprice+" WHERE id="+this.userid);
                }else{
                    Main.sql.doupdate("UPDATE wqw_users SET coins=coins+"+sellprice+" WHERE id="+this.userid);
                }
                if(isitem.equals("Item") || isitem.equals("Quest Item")){
                    Main.sql.doupdate("UPDATE wqw_items SET iQty=iQty-1 WHERE itemid="+itemid+" AND userid="+this.userid);
                    if(qty==1){
                        Main.sql.doupdate("DELETE FROM wqw_items WHERE id="+adjustid);
                    }
                } else {
                    Main.sql.doupdate("DELETE FROM wqw_items WHERE id="+adjustid);
                }
                sendPack.addString("{\"t\":\"xt\",\"b\":{\"r\":-1,\"o\":{\"cmd\":\"sellItem\",\"intAmount\":"+sellprice+",\"CharItemID\":"+adjustid+",\"bCoins\":\""+iscoins+"\"}}}");
            } else {
                sendPack.addString("{\"t\":\"xt\",\"b\":{\"r\":-1,\"o\":{\"cmd\":\"sellItem\",\"bitSuccess\":0,\"strMessage\":\"Item Does Not Exist\",\"CharItemID\":-1}}}");
            }
            rs.close();
            send(sendPack, true);
        } catch (Exception e) {
            debug("Exception in sell item: "+e.getMessage());
        }
    }

    protected void sendUotls(boolean addhp, boolean addhpmax, boolean addmp, boolean addmpmax, boolean addlevel){
        Packet Pack = new Packet();
        Pack.addString("{\"t\":\"xt\",\"b\":{\"r\":-1,\"o\":{\"cmd\":\"uotls\",\"o\":{");
        for(int i=1; i<5; i++){
            if(i==1 && addhp){
                if(i!=1){ Pack.addString(","); }
                Pack.addString("\"intHP\":"+gameServer.hp[this.accountid]);
            } else if (i==2 && addhpmax){
                if(i!=1){ Pack.addString(","); }
                Pack.addString("\"intHPMax\":"+gameServer.hpmax[this.accountid]);
            } else if (i==3 && addmpmax){
                if(i!=1){ Pack.addString(","); }
                Pack.addString("\"intMPMax\":"+gameServer.mpmax[this.accountid]);
            } else if (i==4 && addmp){
                if(i!=1){ Pack.addString(","); }
                Pack.addString("\"intMP\":"+gameServer.mp[this.accountid]);
            } else if (i==5 && addlevel){
                if(i!=1){ Pack.addString(","); }
                Pack.addString("\"intLevel\":"+gameServer.level[this.accountid]);
            }
        }
        Pack.addString("},\"unm\":\""+this.account+"\"}}}");
        gameServer.writeMapPacket(this.account, Pack, true, false);
    }

    protected void sendArea(boolean doAgain)
    {
        try {
            Main.sql.doupdate("UPDATE wqw_users SET lastVisited='"+this.playerRoom.roomName+"-"+this.playerRoom.roomNumb+"' WHERE id="+this.userid);
            ResultSet is = Main.sql.doquery("SELECT * FROM wqw_maps WHERE id="+this.playerRoom.roomType);
            if (is.next()) {
                String[] mons = is.getString("monsterid").split(",");
                String[] monnumbs = is.getString("monsternumb").split(",");
                String[] monframe = is.getString("monsterframe").split(",");
                String extra = is.getString("sExtra");
                Boolean anymonsters = true;

                if(is.getString("monsternumb").equals("")){
                    anymonsters = false;
                }

                Packet sendPack = new Packet();
                    sendPack.addString("{\"t\":\"xt\",\"b\":{\"r\":-1,\"o\":{\"cmd\":\"moveToArea\",\"areaName\":\"");
                    sendPack.addString(this.playerRoom.roomName+"-"+gameServer.getPlayerRoom(this.account)[1]);
                    sendPack.addString("\",\"intKillCount\":0,\"uoBranch\":[");
                    for (int i = 0; i < 10; i++) {
                        if (!this.playerRoom.roomSlot[i].equals("")) {
                            int playerI = gameServer.getPlayerID(this.playerRoom.roomSlot[i]);
                            if (playerI > 0 && !this.playerRoom.roomSlot[i].equals("")) {
                                if (i != 0) {
                                    sendPack.addString(",");
                                }
                                sendPack.addString(this.playerRoom.getPlayerInfo(i));
                            }
                        }
                    }
                    sendPack.addString("],\"strMapFileName\":\"");
                    sendPack.addString(this.playerRoom.fileName +"\"");
                    if(extra.equals("bPvP")){
                        sendPack.addString(",\"PVPFactions\":[{\"id\":8,\"sName\":\"Legends\"},{\"id\":7,\"sName\":\"Overlords\"}],\"pvpTeam\":"+gameServer.pvpteam[this.accountid]+",\"pvpScore\":[{\"v\":"+gameServer.lscore+",\"r\":0,\"m\":0,\"k\":0},{\"v\":"+gameServer.vscore+",\"r\":0,\"m\":0,\"k\":0}]");
                    }

                    sendPack.addString(",\"mondef\":[");

                    if (anymonsters == true) {
                        /* Initialize Drops */

                        this.drops = new String[monnumbs.length];
                        this.droppercent = new int[monnumbs.length];
                        for(int x = 0; x< monnumbs.length; x++){
                            ResultSet xs = Main.sql.doquery("SELECT * FROM wqw_monsters WHERE MonID="+monnumbs[x]);
                            if (xs.next()) {
                                String[] mondrops = xs.getString("strDrops").split(",");
                                for (int a = 0; a < mondrops.length; a++) {
                                    String [] droppart = mondrops[a].split(":");
                                    this.drops[x] = droppart[0];
                                    this.droppercent[x] = (int) (Double.parseDouble(droppart[1])*100);
                                    debug("Drop Item ID: " + this.drops[x] + " Loaded! Percentage: "+this.droppercent[x] +"%");
                                }
                            }
                        }
                        
                        for (int e = 0; e < mons.length; e++) {
                            if (e != 0) {
                                sendPack.addString(",");
                            }
                            is.close();
                            ResultSet rs = Main.sql.doquery("SELECT * FROM wqw_monsters WHERE MonID="+mons[e]);
                            if (rs.next()) {
                                sendPack.addString("{\"sRace\":\""+rs.getString("sRace")+"\",\"MonID\":\""+rs.getInt("MonID")+"\",\"intMP\":\""+rs.getInt("intMPMax")+"\",\"intLevel\":\""+rs.getInt("intLevel")+"\",\"intMPMax\":"+rs.getInt("intMPMax")+",\"intHP\":\""+rs.getInt("intHPMax")+"\",\"strBehave\":\"walk\",\"intHPMax\":\""+rs.getInt("intHPMax")+"\",\"strElement\":\""+rs.getString("strElement")+"\",\"strLinkage\":\""+rs.getString("strLinkage")+"\"");
                                sendPack.addString(",\"strMonFileName\":\""+rs.getString("strMonFileName")+"\",\"strMonName\":\""+rs.getString("strMonName")+"\"}");
                            }
                            rs.close();
                        }
                    }

                    sendPack.addString("],\"intType\":\"1\",\"monBranch\":[");
                    if (anymonsters == true) {
                    sendPack.addString(this.playerRoom.getMon(monnumbs));
                    }
                    sendPack.addString("],\"wB\":[],\"sExtra\":\""+extra+"\"");
                    sendPack.addString(",\"monmap\":[");

                    if (anymonsters == true) {
                    for (int u = 0; u < monnumbs.length; u++) {
                        if (u != 0) {
                            sendPack.addString(",");
                        }
                        this.playerRoom.monsterFrame[u] = monframe[u];
                        sendPack.addString("{\"MonMapID\":\""+(u+1)+"\",\"strFrame\":\""+monframe[u]+"\",\"intRSS\":\"-1\",\"MonID\":\""+monnumbs[u]+"\",\"bRed\":\"0\"}");
                    }
                    }

                    sendPack.addString("],\"areaId\":");
                    sendPack.addString(""+gameServer.getPlayerRoom(this.account)[0]);
                    sendPack.addString(",\"strMapName\":\"");
                    sendPack.addString(this.playerRoom.roomName);
                    sendPack.addString("\"}}}");
                    send(sendPack,true);
                sendPack.clean();
                sendPack.addString("%xt%server%-1%Movido para \""+this.playerRoom.roomName+"-"+gameServer.getPlayerRoom(this.account)[1]+"\"!%");
                send(sendPack,true);
                sendPack.clean();
                sendPack.addXMLSingle(1,"msg t","sys");
                sendPack.addXMLSingle(1,"body action","uER","r",""+gameServer.getPlayerRoom(this.account)[0]*gameServer.getPlayerRoom(this.account)[1]);
                sendPack.addXMLSingle(1,"u i",""+gameServer.getPlayerID(this.account),"m",""+gameServer.getModerator(gameServer.getPlayerID(this.account)),"s","0","p",""+(gameServer.room[gameServer.getPlayerRoom(this.account)[0]][gameServer.getPlayerRoom(this.account)[1]].getPlayerSlot(this.account)+1));
                sendPack.addXML("n","",1);
                sendPack.addCDATA(this.account);
                sendPack.addXML("n","",2);
                sendPack.addXML("vars","",0);
                sendPack.addXMLSingle(2,"u");
                sendPack.addXMLSingle(2,"body");
                sendPack.addXMLSingle(2,"msg");
                gameServer.writeMapPacket(this.account, sendPack, true, true);
                gameServer.sendPlayerDetails(this.account);
            }
        } catch (Exception e) {
            debug("Exception in send area: "+e.getMessage());
            if (doAgain) {
                sendArea(false);
            } else {
                Packet sendPack = new Packet();
                sendPack.addString("%xt%logoutWarning%-1%%15%");
                send(sendPack, true);
                this.finalize();
            }
        }
    }

    protected void sendLobby()
    {
        Packet sendPack = new Packet();
        sendPack.addXMLSingle(1,"msg t","sys");
        sendPack.addXMLSingle(1,"body action","joinOK","r",""+gameServer.getPlayerRoom(this.account)[0]);
        sendPack.addXMLSingle(0,"pid id",""+(this.playerRoom.getPlayerSlot(this.account)+1));
        sendPack.addXMLSingle(0,"vars");
        sendPack.addXMLSingle(1,"uLs r",""+gameServer.getPlayerRoom(this.account)[0]*gameServer.getPlayerRoom(this.account)[1]);

        for (int e = 0; e < 10; e++) {
            if (!this.playerRoom.roomSlot[e].equals("")) {
                int playerI = gameServer.getPlayerID(this.playerRoom.roomSlot[e]);
                if (playerI > 0 && !this.playerRoom.roomSlot[e].equals("")) {
                    int mod = gameServer.getModerator(playerI);
                    sendPack.addXMLSingle(1,"u i",""+playerI,"m",""+mod,"s","0","p",""+(e+1));
                    sendPack.addXML("n","",1);
                    sendPack.addCDATA(gameServer.getCharname(playerI));
                    sendPack.addXML("n","",2);
                    sendPack.addXML("vars","",0);
                    sendPack.addXMLSingle(2,"u");
                }
            }
        }
        sendPack.addXMLSingle(2,"uLs");
        sendPack.addXMLSingle(2,"body");
        sendPack.addXMLSingle(2,"msg");
        send(sendPack, true);
        sendPack.clean();
        debug("Sent lobby: "+this.account);
        sendArea(true);
    }

    protected void sendPolicy()
    {
        Packet sendPack = new Packet();
        sendPack.addXML("cross-domain-policy","",1);
        sendPack.addXMLSingle(0,"allow-access-from domain","*","to-ports",""+Main.port);
        sendPack.addXML("cross-domain-policy","",2);
        send(sendPack, true);
        debug("Sent policy to: "+this.ip);
        this.finalize();
    }

    protected void sendVersion()
    {
        Packet sendPack = new Packet();
        sendPack.addXMLSingle(1,"msg t","sys");
        sendPack.addXMLSingle(1,"body action","apiOK");
        sendPack.addXMLSingle(2,"body");
        sendPack.addXMLSingle(2,"msg");
        send(sendPack, true);
        debug("Sent version to: "+this.ip);
    }

    protected void setAFK(boolean afk)
    {
        Packet sendPack = new Packet();
        sendPack.addString("%xt%uotls%-1%");
        sendPack.addString(this.account);
        sendPack.addString("%afk:");
        sendPack.addString(afk+"%");
        gameServer.writeMapPacket(this.account,sendPack,true,false);
        sendPack.clean();
        if(afk != this.playerRoom.afk[this.playerSlot]){
            this.playerRoom.afk[this.playerSlot] = afk;
            if (afk) {
                sendPack.addString("%xt%server%-1%You are now Away From Keyboard (AFK).%");
            } else {
                sendPack.addString("%xt%server%-1%You are no longer Away From Keyboard (AFK).%");
            }
            send(sendPack, true);
            sendPack.clean();
        }
        write(this.account,"AFK Set: "+ afk);
    }

    protected void unequipItem(int itemid, int adjustid)
    {
        try {
            Packet sendPack = new Packet();
            sendPack.addString("{\"t\":\"xt\",\"b\":{\"r\":-1,\"o\":{\"uid\":");
            sendPack.addInt(this.accountid);
            ResultSet rs = Main.sql.doquery("SELECT * FROM wqw_equipment WHERE itemID="+itemid);
            if (rs.next()) {
                sendPack.addString(",\"ItemID\":\""+itemid+"\",\"strES\":\""+rs.getString("sES")+"\",\"cmd\":\"unequipItem\"}}}");
            }
            rs.close();
            Main.sql.doupdate("UPDATE wqw_items SET equipped=0 WHERE userid="+this.userid+" AND itemid="+itemid+" AND equipped=1");
            gameServer.writeMapPacket(this.account, sendPack, true, false);
        } catch (Exception e) {
            debug("Exception in equip item: "+e.getMessage());
        }
    }

    protected void userChat(int room, String message, String zone)
    {
        Packet sendPack = new Packet();
        sendPack.addString("%xt%chatm%"+room+"%");



        sendPack.addString(zone+"~"+message);
        if (zone.equals("server")) {
            sendPack.addString("%"+this.account);
        } else {
            sendPack.addString("%"+this.account);
        }
        if (zone.equals("event")) {
            sendPack.addString("%em%"+this.account);
        } else {
            sendPack.addString("%"+this.account);
        }
        sendPack.addString(zone+"~"+message);
        sendPack.addString("%"+this.userid);
        sendPack.addString("%"+room+"%");
        if (zone.equals("party")) {
            if (gameServer.partyRoom[this.accountid] > 0) {
                gameServer.writePartyPacket(this.account,sendPack,true,false);
            } else {
                sendPack.clean();
                sendPack.addString("%xt%warning%-1%You are not in a party.%");
                send(sendPack,true);
            }
        } else if(zone.equals("warning")){
            gameServer.writeGlobalPacket(this.account, sendPack, true, false);
        } else if(zone.equals("server")){
            gameServer.writeGlobalPacket(this.account, sendPack, true, false);
         } else if(zone.equals("trade")){
            gameServer.writeMapPacket(this.account, sendPack, true, false);
        } else {
            gameServer.writeMapPacket(this.account,sendPack,true,false);
        }
        sendPack.clean();
        write(this.account, message);
    }

   


    protected void userCommand(String cmd)
    {
        Packet sendPack = new Packet();
        boolean startMod = true;
        int i = 0;
        byte[] cmdBytes = cmd.getBytes();
        String command = "";
        while (cmdBytes[i] != 0x20 && i < cmd.length() - 1) {
            i++;
        }
        if (i == cmd.length() - 1) {
            command = cmd.substring(0, cmd.length()).toLowerCase();
        } else {
            command = cmd.substring(0, i).toLowerCase();
        }
        
        if (gameServer.getModerator(gameServer.getPlayerID(this.account)) == 0 && gameServer.getVIP(gameServer.getPlayerID(this.account)) == 0 && gameServer.getAdmin(gameServer.getPlayerID(this.account))==0){
            /* Add commands for a normal user here ^_^ */
        } else if (gameServer.getModerator(gameServer.getPlayerID(this.account)) == 1 && startMod == true){
            if (command.equals("ban")){
                debug("Banning: "+cmd.substring(i + 1));
                gameServer.banPlayer(cmd.substring(i + 1), this.account);
            } else if (command.equals("cp")) {
                int cp = Integer.parseInt(cmd.substring(i + 1));
                sendPack.addString(addRewards(0, 0, cp, 0, "m", 0));
                send(sendPack, true);
            } else if (command.equals("heal")) {
                gameServer.hp[this.accountid] = gameServer.hpmax[this.accountid];
                gameServer.mp[this.accountid] = gameServer.mpmax[this.accountid];
                sendUotls(true,false,true,false,false);
            } else if (command.equals("help")) {
                serverMsg("Ban/Unban (user) - Bans/Unbans (user).","server",false,false);
                serverMsg("CP/Gold/XP (i) - Gives you (i) amount of CP/Gold/XP.","server",false,false);
                serverMsg("Heal - Heals you to max HP and MP.","server",false,false);
                serverMsg("Kick (user) - Kicks (user).","server",false,false);
                serverMsg("Announce (Message) - Annouce some messages to all players (free for admin's)","server",false,false);
                serverMsg("Shop (Shop id) - Load any shop you want instantly.","server",false,false);
                serverMsg("Level (amount) Level up to your choice%","server",false,false);
                serverMsg("Mod (message) - Displays (message) in the mod chat style.","server",false,false);
            } else if (command.equals("gold")) {
                int gold = Integer.parseInt(cmd.substring(i + 1));
                sendPack.addString(addRewards(0, gold, 0, 0, "m", 0));
                send(sendPack, true);
            } else if (command.equals("kick")){
                gameServer.kickPlayer(cmd.substring(i + 1), this.account);
            } else if (command.equals("mod")){
                sendPack.addString("%xt%moderator%-1%("+this.account+"): "+cmd.substring(i + 1)+"%");
                gameServer.writeGlobalPacket(this.account, sendPack, true, false);
            } else if (command.equals("me")){
                sendPack.addString("%xt%em%-1%"+this.account+": "+cmd.substring(i + 1)+"%%");
                gameServer.writeMapPacket(this.account, sendPack, true, false);
            } else if (command.equals("em")){
                sendPack.addString("%xt%em%-1%"+this.account+": "+cmd.substring(i + 1)+"%%");
                gameServer.writeMapPacket(this.account, sendPack, true, false);
            } else if (command.equals("xp")) {
                int xp = Integer.parseInt(cmd.substring(i + 1));
                sendPack.addString(addRewards(xp, 0, 0, 0, "m", 0));
                send(sendPack, true);
            } else if (command.equals("unban")) {
                gameServer.unbanPlayer(cmd.substring(i + 1), this.account);
            } else if (command.equals("level")) {
                changeLevel(Integer.parseInt(cmd.substring(i + 1)));
            } else if (command.equals("shop")){
                loadShop(Integer.parseInt(cmd.substring(i + 1)));
            } else if (command.equals("announce")){
                sendPack.addString("%xt%server%-1%(VIP) ("+this.account+"): "+cmd.substring(i + 1)+"%");
                gameServer.writeGlobalPacket(this.account, sendPack, true, false);
            } else {
                sendPack.addString("%xt%server%-1%Invalid command. Use the command help for a list of commands.%");
                send(sendPack, true);
            }
       } else if (gameServer.getVIP(gameServer.getPlayerID(this.account)) == 1){
            if (command.equals("heal")) {
                gameServer.hp[this.accountid] = gameServer.hpmax[this.accountid];
                gameServer.mp[this.accountid] = gameServer.mpmax[this.accountid];
                sendUotls(true,false,true,false,false);
            } else if (command.equals("setpvp")){
                debug("Setting PvP: "+this.account);
                if (this.fighting == false) {
                    gameServer.pvpOn[this.accountid] = !gameServer.pvpOn[this.accountid];
                    sendPack.addString("%xt%server%-1%PvP set to "+gameServer.pvpOn[this.accountid]+".%");
                    send(sendPack, true);
                } else {
                    sendPack.addString("%xt%server%-1%You must wait at least thirty seconds after a battle to change your PvP status.%");
                    send(sendPack, true);
                }
            } else if (command.equals("help")) {
                serverMsg("Heal - Heals you to max HP and MP.","server",false,false);
                serverMsg("Shop (Shop id) - Load any shop you want instantly.","server",false,false);
                serverMsg("Announce (Message) - Annouce some messages to all players (free for vip's)","server",false,false);
            } else if (command.equals("level")) {
                sendPack.clean();
                sendPack.addString("%xt%server%-1%!level command has been removed for the fairness of the game.");
                send(sendPack,true);
            } else if (command.equals("shop")){
                int shopid = Integer.parseInt(cmd.substring(i + 1));
                loadShop(shopid);
            } else if (command.equals("announce")){
                sendPack.addString("%xt%server%-1%(VIP) ("+this.account+"): "+cmd.substring(i + 1)+"%");
                gameServer.writeGlobalPacket(this.account, sendPack, true, false);
            } else {
                sendPack.addString("%xt%server%-1%Invalid command. Use the command help for a list of commands.%");
                send(sendPack, true);
            }
        } else if (gameServer.getAdmin(gameServer.getPlayerID(this.account)) == 1){
            if (command.equals("ban")){
                debug("Banning: "+cmd.substring(i + 1));
                gameServer.banPlayer(cmd.substring(i + 1), this.account);
            } else if (command.equals("setpvp")){
                debug("Setting PvP: "+this.account);
                if (this.fighting == false) {
                    gameServer.pvpOn[this.accountid] = !gameServer.pvpOn[this.accountid];
                    sendPack.addString("%xt%server%-1%PvP set to "+gameServer.pvpOn[this.accountid]+".%");
                    send(sendPack, true);
                } else {
                    sendPack.addString("%xt%server%-1%You must wait at least thirty seconds after a battle to change your PvP status.%");
                    send(sendPack, true);
                }
            } else if (command.equals("cp")) {
                int cp = Integer.parseInt(cmd.substring(i + 1));
                debug("Adding "+cp+" CP to "+this.account);
                sendPack.clean();
                sendPack.addString(addRewards(0, 0, cp, 0, "m", 0));
                send(sendPack, true);
            } else if (command.equals("heal")) {
                gameServer.hp[this.accountid] = gameServer.hpmax[this.accountid];
                gameServer.mp[this.accountid] = gameServer.mpmax[this.accountid];
                sendUotls(true,false,true,false,false);
            } else if (command.equals("help")) {
                serverMsg("Ban/Unban (user) - Bans/Unbans (user).","server",false,false);
                serverMsg("CP/Gold/XP (i) - Gives you (i) amount of CP/Gold/XP.","server",false,false);
                serverMsg("Heal - Heals you to max HP and MP.","server",false,false);
                serverMsg("Kick (user) - Kicks (user).","server",false,false);
                serverMsg("Unmod (user) - Unmods (user).","server",false,false);
                serverMsg("Makemod (user) - Mods a (user).","server",false,false);
                serverMsg("VIP (user) - Grants vip status (user).","server",false,false);
                serverMsg("Unvip (user) - Disable vip status (user).","server",false,false);
                serverMsg("Delete (user) - Deletes (user).","server",false,false);
                serverMsg("Announce (Message) - Annouce some messages to all players (free for admin's)","server",false,false);
                serverMsg("Shop (Shop id) - Load any shop you want instantly.","server",false,false);
                serverMsg("Level (amount) Level up to your choice%","server",false,false);
                serverMsg("Admin (message) - Chat to all as admin in color red.","server",false,false);
                serverMsg("Kill (username) - Kills the player automatically...","server",false,false);
                serverMsg("Mod (message) - Displays (message) in the mod chat style.","server",false,false);
            } else if (command.equals("gold")) {
                int gold = Integer.parseInt(cmd.substring(i + 1));
                debug("Adding "+gold+" gold to "+this.account);
                sendPack.clean();
                sendPack.addString(addRewards(0, gold, 0, 0, "m", 0));
                send(sendPack, true);
            } else if (command.equals("kick")){
                debug("Kicking: "+cmd.substring(i + 1));
                gameServer.kickPlayer(cmd.substring(i + 1), this.account);
            } else if (command.equals("mod")){
                sendPack.clean();
                sendPack.addString("%xt%moderator%-1%("+this.account+"): "+cmd.substring(i + 1)+"%");
                gameServer.writeGlobalPacket(this.account, sendPack, true, false);
            } else if (command.equals("xp")) {
                int xp = Integer.parseInt(cmd.substring(i + 1));
                debug("Adding "+xp+" XP to "+this.account);
                sendPack.clean();
                sendPack.addString(addRewards(xp, 0, 0, 0, "m", 0));
                send(sendPack, true);
            } else if (command.equals("unban")) {
                debug("Unbanning: "+cmd.substring(i + 1));
                gameServer.unbanPlayer(cmd.substring(i + 1), this.account);
            } else if (command.equals("unmod")) {
                gameServer.unmodPlayer(cmd.substring(i + 1), this.account);
            } else if (command.equals("unvip")) {
                gameServer.unvipPlayer(cmd.substring(i + 1), this.account);
            } else if (command.equals("vip")) {
                gameServer.vipPlayer(cmd.substring(i + 1), this.account);
            } else if (command.equals("makemod")) {
                gameServer.modPlayer(cmd.substring(i + 1), this.account);
            } else if (command.equals("delete")) {
                gameServer.deletePlayer(cmd.substring(i + 1), this.account);
            } else if (command.equals("announce")){
                sendPack.addString("%xt%server%-1%(VIP) ("+this.account+"): "+cmd.substring(i + 1)+"%");
                gameServer.writeGlobalPacket(this.account, sendPack, true, false);
            } else if (command.equals("emoteall")){
                emoteAll(cmd.substring(i + 1));
            } else if (command.equals("forceemote")){
                String[] omg = cmd.substring(i + 1).split(",");
                forceEmote(omg);
            } else if (command.equals("shop")){
                int shopid = Integer.parseInt(cmd.substring(i + 1));
                loadShop(shopid);
            } else if (command.equals("kill")){
                overKill(gameServer.getPlayerID(cmd.substring(i + 1)));
            } else if (command.equals("level")) {
                changeLevel(Integer.parseInt(cmd.substring(i + 1)));
            } else {
                sendPack.addString("%xt%server%-1%Invalid command. Use the command help for a list of commands.%");
                send(sendPack, true);
            }
        }
    }

    protected void userMove(int tx, int ty, int speed, boolean cansee)
    {
        if (tx < 0) {
            tx = 0;
        }
        if (ty < 0) {
            ty = 0;
        }
        if (tx > 1000) {
            tx = 1000;
        }
        if (ty > 800) {
            ty = 800;
        }
        Packet sendPack = new Packet();
        sendPack.addString("%xt%uotls%-1%");
        sendPack.addString(this.account);
        sendPack.addString("%sp:"+speed);
        sendPack.addString(",tx:"+tx);
        sendPack.addString(",ty:"+ty);
        sendPack.addString(",strFrame:"+this.playerRoom.frame[this.playerSlot]+"%");
        this.playerRoom.tx[this.playerSlot] = tx;
        this.playerRoom.ty[this.playerSlot] = ty;
        gameServer.writeMapPacket(this.account,sendPack,true,!cansee);
        sendPack.clean();
    }

    protected void whisperChat(String message, String otheruser)
    {
        Packet sendPack = new Packet();
        if (gameServer.getPlayerID(otheruser.toLowerCase()) > 0) {
            sendPack.addString("%xt%whisper%-1%");
            sendPack.addString(message);
            sendPack.addString("%"+this.account+"%"+otheruser+"%0%");
            send(sendPack, true);
            gameServer.writePlayerPacket(otheruser,sendPack,true);
            write(this.account+" > "+otheruser,message);
        } else {
            sendPack.addString("%xt%server%-1%Player "+otheruser+" could not be found%");
            send(sendPack, true);
        }
    }

    /*
     * END COMMAND RESPONSES
     * The above section contains responses for the messages sent to the server by the client
     * Above and below this section are the core functions
     */

    protected void send(Packet pack, boolean addNull)
    {
        String packet = pack.getPacket();
        if (addNull) {
            packet += "\u0000";
        }
        this.socketOut.write(packet);
        this.socketOut.flush();
    }

    protected String read()
    {
        StringBuffer buffer = new StringBuffer();
        int codePoint;
        boolean zeroByteRead = false;
        try {
            do {
                if (!this.hasFinalized) {
                    codePoint = this.socketIn.read();

                    if (codePoint == 0) {
                        zeroByteRead = true;
                    }

                    if (codePoint == -1) {
                        this.finalize();
                    }
                    else if (Character.isValidCodePoint(codePoint)) {
                        buffer.appendCodePoint(codePoint);
                    }
                }
            }
            while (!zeroByteRead);
        } catch (Exception e) {
            debug("Error read: " + e.getMessage() + ", " + e.getCause());
            this.finalize();
        }
        return buffer.toString();
    }

    /**
     * Create a reader and writer for the socket
     */
    @Override
    public void run()
    {
        try {
            this.socketIn = new BufferedReader(new InputStreamReader(this.socket.getInputStream()));
            this.socketOut = new PrintWriter(this.socket.getOutputStream(), true);
            String packet;
            while((packet = read()) != null && !hasFinalized) {
                parseCMD(getcmd(packet),packet);
            }
        }
        catch (Exception e) {
            debug("Exception encountered in run: " + e.getMessage() + ", " + e.getCause());
        }
        this.finalize();
    }

    /**
     * Closes the reader, the writer and the socket.
     */
    @Override
    protected void finalize() {
        try {
            Packet sendPack = new Packet();
            this.hasFinalized = true;
            if(this.userid > 0){
                try {
                    //Finalize this to online users xD

                    Main.sql.doupdate("UPDATE wqw_users SET curServer='Offline' WHERE id="+this.userid);
                    sendPack.addString("{\"t\":\"xt\",\"b\":{\"r\":-1,\"o\":{\"cmd\":\"updateFriend\",\"friend\":{\"iLvl\":"+gameServer.level[this.accountid]+",\"ID\":\""+this.userid+"\",\"sName\":\""+this.charname+"\",\"sServer\":\"Offline\"}}}}");
                    for(int i=0; i < this.friends.length; i++){
                        if(gameServer.getPlayerID(this.friends[i].toLowerCase())>0){
                            gameServer.writePlayerPacket(this.friends[i].toLowerCase(), sendPack, true);
                            sendPack.clean();
                            sendPack.addString("%xt%server%-1%"+this.charname+" has logged out.%");
                            write(charname,"(Is now Offline)");
                            gameServer.writePlayerPacket(this.friends[i].toLowerCase(), sendPack, true);
                        }
                       
                    }

                } catch (Exception e){
                    debug("Exception in finalize sql, userid: "+this.userid+", "+e.getMessage());
                }
            }
            if (this.account != null && !this.account.equals("") && gameServer.getPlayerID(this.account) > 0) {
                gameServer.removeuser(this.account);
            } else {
                this.server.remove(this.getRemoteAddress());
            }
            this.socketOut.close();
            this.socket.close();
            this.socketIn.close();
            stop();
        }
        catch (Exception e) {
            debug("Exception in finalizing: " + e.getMessage());
            gameServer.removeuser(this.account);
            this.server.remove(this.getRemoteAddress());
            stop();
        }
    }
}
