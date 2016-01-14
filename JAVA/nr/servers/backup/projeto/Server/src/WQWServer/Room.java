/*     */ package WQWServer;
/*     */ 
/*     */ import java.io.PrintWriter;
/*     */ import java.sql.ResultSet;
/*     */ 
/*     */ public class Room
/*     */ {
/*  12 */   public String[] roomSlot = new String[20];
/*     */   public int roomType;
/*     */   public String roomName;
/*     */   public String fileName;
/*  16 */   public String[] pad = new String[50];
/*  17 */   public String[] frame = new String[50];
/*  18 */   public int[] tx = new int[20];
/*  19 */   public int[] ty = new int[20];
/*  20 */   public boolean[] afk = new boolean[20];
/*     */   public int roomNumb;
/*     */   public int users;
/*  23 */   private PrintWriter[] playerSocket = new PrintWriter[20];
/*  24 */   public String[] monsterBehave = new String[64];
/*  25 */   public int[] monsterHP = new int[64];
/*  26 */   public int[] monsterMP = new int[64];
/*  27 */   public int[] monsterHPMax = new int[64];
/*  28 */   public int[] monsterMPMax = new int[64];
/*  29 */   public int[] monsterState = new int[64];
/*  30 */   public int[] monsterType = new int[64];
/*  31 */   public int[] monsterLevel = new int[64];
/*  32 */   public String[] monsterFrame = new String[64];
public boolean safe = false;
/*     */ 
/*     */   public Room(int type, int numb)
/*     */   {
/*  36 */     this.roomType = type;
/*  37 */     this.roomNumb = numb;
/*  38 */     for (int i = 0; i < this.roomSlot.length; i++) {
/*  39 */       this.roomSlot[i] = "";
/*  40 */       this.pad[i] = "Spawn";
/*  41 */       this.frame[i] = "Enter";
/*  42 */       this.afk[i] = false;
/*     */     }
/*     */     try {
/*  45 */       ResultSet rs = Main.sql.doquery("SELECT * FROM wqw_maps WHERE id=" + type);
/*  46 */       if (rs.next()) {
/*  47 */         this.roomName = rs.getString("name");
/*  48 */         this.fileName = rs.getString("fileName");
/*  49 */         if (!rs.getString("monsternumb").equals("")) {
/*  50 */           this.monsterBehave = rs.getString("monsternumb").split(",");
/*  51 */           for (int a = 0; a < this.monsterBehave.length; a++) {
/*  52 */             ResultSet is = Main.sql.doquery("SELECT * FROM wqw_monsters WHERE MonID=" + this.monsterBehave[a]);
/*  53 */             if (is.next()) {
/*  54 */               this.monsterHP[a] = is.getInt("intHPMax");
/*  55 */               this.monsterMP[a] = is.getInt("intMPMax");
/*  56 */               this.monsterHPMax[a] = is.getInt("intHPMax");
/*  57 */               this.monsterMPMax[a] = is.getInt("intMPMax");
/*  58 */               this.monsterLevel[a] = is.getInt("intLevel");
/*     */             }
/*  60 */             this.monsterType[a] = Integer.parseInt(this.monsterBehave[a]);
/*  61 */             this.monsterBehave[a] = "walk";
/*  62 */             this.monsterState[a] = 1;
/*     */           }
/*     */         }
/*     */       }
/*  66 */       rs.close();
/*     */     } catch (Exception e) {
/*  68 */       Main.server.debug("Exception in room: " + e.getMessage());
/*     */     }
/*     */   }
/*     */ 
/*     */   protected String getMon(String[] monnumbs)
/*     */   {
/*  74 */     Packet sendPack2 = new Packet();
/*     */     try {
/*  76 */       for (int o = 0; o < monnumbs.length; o++) {
/*  77 */         ResultSet os = Main.sql.doquery("SELECT * FROM wqw_monsters WHERE MonID=" + monnumbs[o]);
/*  78 */         if (os.next()) {
/*  79 */           if (o != 0) {
/*  80 */             sendPack2.addString(",");
/*     */           }
/*  82 */           sendPack2.addString("{\"intHPMax\":" + os.getInt("intHPMax") + ",\"iLvl\":" + os.getInt("intLevel") + ",\"MonMapID\":" + (o + 1) + ",\"MonID\":\"" + monnumbs[o] + "\",\"intMP\":" + this.monsterMP[o] + ",\"wDPS\":" + os.getInt("iDPS") + ",\"intState\":" + this.monsterState[o] + ",\"intMPMax\":" + os.getInt("intMPMax") + ",\"bRed\":\"0\",");
/*  83 */           if (os.getString("react").contains("0")) {
/*  84 */             sendPack2.addString("\"react\":[" + os.getString("react") + "],");
/*     */           }
/*  86 */           sendPack2.addString("\"intHP\":" + this.monsterHP[o] + "}");
/*     */         }
/*  88 */         os.close();
/*     */       }
/*     */     } catch (Exception e) {
/*  91 */       Main.server.debug("Exception in get monster: " + e.getMessage());
/*     */     }
/*  93 */     return sendPack2.getPacket();
/*     */   }
/*     */ 
/*     */   protected String getPlayerInfo(int slot)
/*     */   {
/*  98 */     Packet pInfo = new Packet();
/*  99 */     pInfo.addString("{\"uoName\":\"" + this.roomSlot[slot] + "\"");
/* 100 */     pInfo.addString(",\"strUsername\":\"" + this.roomSlot[slot] + "\"");
/* 101 */     pInfo.addString(",\"strFrame\":\"" + this.frame[slot] + "\"");
/* 102 */     pInfo.addString(",\"strPad\":\"" + this.pad[slot] + "\"");
/* 103 */     pInfo.addString(getPlayerSQL(slot));
/* 104 */     pInfo.addString(",\"tx\":" + this.ty[slot]);
/* 105 */     pInfo.addString(",\"ty\":" + this.ty[slot]);
/* 106 */     pInfo.addString(",\"afk\":" + this.afk[slot]);
/* 107 */     pInfo.addString("}");
/*     */ 
/* 110 */     return pInfo.getPacket();
/*     */   }
/*     */ 
/*     */   protected int getPlayerSlot(String charname)
/*     */   {
/* 115 */     for (int i = 0; i < this.roomSlot.length; i++) {
/* 116 */       if (this.roomSlot[i].equals(charname)) {
/* 117 */         return i;
/*     */       }
/*     */     }
/* 120 */     return -1;
/*     */   }
/*     */ 
/*     */   protected String getPlayerSQL(int slot)
/*     */   {
/* 126 */     Packet sqlInfo = new Packet();
/*     */     try {
/* 128 */       ResultSet rs = Main.sql.doquery("SELECT * FROM wqw_users WHERE username='" + this.roomSlot[slot] + "'");
/*     */ 
/* 130 */       int i = Server.gameServer.getPlayerID(this.roomSlot[slot]);
/* 131 */       if (rs.next()) {
/* 132 */         sqlInfo.addString(",\"intState\":1");
/* 133 */         sqlInfo.addString(",\"intLevel\":" + rs.getInt("level"));
/* 134 */         sqlInfo.addString(",\"entID\":" + i);
/* 135 */         sqlInfo.addString(",\"entType\":\"p\"");
/* 136 */         sqlInfo.addString(",\"showHelm\":true");
/* 137 */         sqlInfo.addString(",\"showCloak\":true");
/* 138 */         if ((this.roomName.equals("darkoviapvp")) || (this.roomName.equals("bludrutbrawl"))) {
/* 139 */           sqlInfo.addString(",\"pvpTeam\":" + Server.gameServer.pvpteam[i]);
/*     */         }
/* 141 */         sqlInfo.addString(",\"intHP\":" + Server.gameServer.hp[i]);
/* 142 */         sqlInfo.addString(",\"intMP\":" + Server.gameServer.mp[i]);
/* 143 */         sqlInfo.addString(",\"intHPMax\":" + Server.gameServer.hpmax[i]);
/* 144 */         sqlInfo.addString(",\"intMPMax\":" + Server.gameServer.mpmax[i]);
/*     */       }
/* 146 */       rs.close();
/*     */     } catch (Exception e) {
/* 148 */       Main.server.debug("Exception in room init: " + e.getMessage());
/*     */     }
/* 150 */     return sqlInfo.getPacket();
/*     */   }
/*     */ 
 protected void respawnMonster(int monsterid, int monstertype)
    {
        respawnTimer respawn = new respawnTimer();
        respawn.main(monsterid, monstertype, this.roomType, this.roomNumb);
    }

    protected void respawnMonsterDo(int monsterid)
    {
        try {
            ResultSet is = Main.sql.doquery("SELECT * FROM wqw_monsters WHERE MonID="+this.monsterType[monsterid]);
            if (is.next()) {
                this.monsterHP[monsterid] = is.getInt("intHPMax");
                this.monsterMP[monsterid] = is.getInt("intMPMax");
                this.monsterLevel[monsterid] = is.getInt("intLevel");
            }
            this.monsterState[monsterid] = 1;
            int[] temproom = new int[2];
            temproom[0] = this.roomType;
            temproom[1] = this.roomNumb;
            Packet sendPack = new Packet();
            sendPack.addString("%xt%mtls%-1%"+(monsterid+1)+"%intHP:"+this.monsterHP[monsterid]+",intMP:"+this.monsterMP[monsterid]+",intState:1%");
            Main.server.gameServer.writeOtherMapPacket(temproom, sendPack, true);
            sendPack.clean();
            sendPack.addString("%xt%respawnMon%-1%"+(monsterid+1)+"%");
            Main.server.gameServer.writeOtherMapPacket(temproom, sendPack, true);
            is.close();
        } catch (Exception e) {
            this.monsterHP[monsterid] = this.monsterHPMax[monsterid];
            this.monsterMP[monsterid] = this.monsterMPMax[monsterid];
            this.monsterState[monsterid] = 1;
            Main.server.debug("Exception in respawn monster: "+e.getMessage());
            this.monsterState[monsterid] = 1;
            int[] temproom = new int[2];
            temproom[0] = this.roomType;
            temproom[1] = this.roomNumb;
            Packet sendPack = new Packet();
            sendPack.addString("%xt%mtls%-1%"+(monsterid+1)+"%intHP:"+this.monsterHP[monsterid]+",intMP:"+this.monsterMP[monsterid]+",intState:1%");
            Main.server.gameServer.writeOtherMapPacket(temproom, sendPack, true);
            sendPack.clean();
            sendPack.addString("%xt%respawnMon%-1%"+(monsterid+1)+"%");
            Main.server.gameServer.writeOtherMapPacket(temproom, sendPack, true);
        }
    }
/*     */ 
/*     */   protected void addPlayer(String charname, PrintWriter socket)
/*     */   {
/* 183 */     for (int i = 0; i < this.roomSlot.length; i++)
/* 184 */       if ((this.roomSlot[i].equals("")) || (this.roomSlot[i] == null)) {
/* 185 */         this.roomSlot[i] = charname;
/* 186 */         this.playerSocket[i] = socket;
/* 187 */         this.users += 1;
/* 188 */         break;
/*     */       }
/*     */   }
/*     */ 
/*     */   protected void removePlayer(String charname)
/*     */   {
/* 195 */     int ID = getPlayerSlot(charname);
/* 196 */     this.roomSlot[ID] = "";
/* 197 */     this.playerSocket[ID] = null;
/* 198 */     this.pad[ID] = "Spawn";
/* 199 */     this.frame[ID] = "Enter";
/* 200 */     this.afk[ID] = false;
/* 201 */     this.tx[ID] = 0;
/* 202 */     this.ty[ID] = 0;
/* 203 */     this.users -= 1;
/*     */   }
/*     */ 
/*     */   protected void finalize()
/*     */   {
/* 209 */     for (int e = 0; e < this.roomSlot.length; e++)
/* 210 */       removePlayer(this.roomSlot[e]);
/*     */   }
/*     */ }