/*      */ package WQWServer;
/*      */
/*      */ import java.io.IOException;
/*      */ import java.io.PrintWriter;
/*      */ import java.net.Socket;
/*      */ import java.sql.ResultSet;
/*      */ import java.util.logging.Level;
/*      */ import java.util.logging.Logger;
/*      */
/*      */ public class GameServer
/*      */ {
/*   15 */   Functions functions = new Functions();
/*   16 */   private int users = 0;
/*   17 */   public String[] charName = new String[50];
/*   18 */   private PrintWriter[] userSocket = new PrintWriter[50];
/*   19 */   public Socket[] playerSocket = new Socket[50];
/*   20 */   public int[][] charRoom = new int[50][2];
/*   21 */   public int[] partyRoom = new int[50];
/*   22 */   public int[] pvpRoom = new int[2];
/*   23 */   public int[] hp = new int[50];
/*   24 */   public int[] mp = new int[50];
/*   25 */   public int[] hpmax = new int[50];
/*   26 */   public int[] mpmax = new int[50];
/*   27 */   public int[] state = new int[50];
/*   28 */   public int xprate = 1;
/*   29 */   public int goldrate = 1;
/*   30 */   public int[] userID = new int[50];
/*   31 */   public int[] level = new int[50];
             public int[] pvpteam = new int[256];
             public int vscore = 0;
             public int lscore = 0;
/*   33 */   public boolean[] isAlive = new boolean[50];
/*   34 */   public boolean[] pvpOn = new boolean[50];
/*   35 */   public int[] arrRanks = new int[15];
/*   36 */   public Room[][] room = new Room[256][256];
/*   37 */   public Party[] party = new Party[50];
/*   38 */   public SkillHandler skills = new SkillHandler();
/*   39 */   public AuraHandler auras = new AuraHandler();
/*      */
/*      */   protected void init()
/*      */   {
/*      */     try
/*      */     {
/*   52 */       ResultSet rs = Main.sql.doquery("SELECT * FROM wqw_settings LIMIT 1");
/*   53 */       if (rs.next()) {
/*   54 */         this.xprate = rs.getInt("xprate");
/*   55 */         this.goldrate = rs.getInt("goldrate");
/*   56 */         debug("Current Gold Rates: " + Integer.toString(this.goldrate));
/*   57 */         debug("Current XP Rates: " + Integer.toString(this.xprate));
/*      */       }
/*   59 */       rs.close();
/*   60 */       initArrRep();
/*   61 */       this.skills.Init();
/*   62 */       this.auras.Init();
/*      */     } catch (Exception e) {
/*   66 */       debug("Exception in get rates: " + e.getMessage());
/*      */     }
/*      */   }
/*      */
/*      */   protected void initArrRep() {
/*   71 */     int i = 1;
/*   72 */     while (i < 10)
/*      */     {
/*   74 */       int _loc_1 = (int)(Math.pow(i + 1, 3.0D) * 100.0D);
/*   75 */       if (i > 1)
/*   76 */         this.arrRanks[i] = (_loc_1 + this.arrRanks[(i - 1)]);
/*      */       else {
/*   78 */         this.arrRanks[i] = (_loc_1 + 100);
/*      */       }
/*   80 */       i++;
/*      */     }
/*      */   }
/*      */
/*      */   public String cleanStr(String text)
/*      */   {
/*   87 */     text = text.replace("&#", "");
/*   88 */     text = text.replace("#038:#", "");
/*   89 */     if (text.indexOf("%") > -1)
/*   90 */       text = text.replace("%", "#037:");
/*   91 */     else if (text.indexOf("#037:") > -1)
/*   92 */       text = text.replace("#037", "%");
/*   93 */     if (text.indexOf("&") > -1)
/*   94 */       text = text.replace("&", "#038:");
/*   95 */     else if (text.indexOf("#038:") > -1)
/*   96 */       text = text.replace("#038:", "&");
/*   97 */     if (text.indexOf("<") > -1)
/*   98 */       text = text.replace("<", "#060:");
/*   99 */     else if (text.indexOf("#060:") > -1)
/*  100 */       text = text.replace("#060:", "&lt;");
/*  101 */     if (text.indexOf(">") > -1)
/*  102 */       text = text.replace(">", "#062:");
/*  103 */     else if (text.indexOf("#062:") > -1)
/*  104 */       text = text.replace("#062:", "&gt;");
/*  105 */     return text;
/*      */   }
/*      */
/*      */   protected int getRankFromCP(int cp) {
/*  109 */     int i = 1;
/*  110 */     while (i < 10)
/*      */     {
/*  112 */       if (this.arrRanks[i] >= cp) {
/*  113 */         return i;
/*      */       }
/*  115 */       i++;
/*      */     }
/*  117 */     return -1;
/*      */   }
/*      */
/*      */   protected void debug(String msg) {
/*  121 */     Main.debug("Game Server - ", msg);
/*      */   }
/*      */
/*      */   protected int calculateHP(int level)
/*      */   {
/*  133 */     return 850 + ((level + 1) * 49 + 17);
/*      */   }
/*      */
/*      */   protected int calculateMP(int level)
/*      */   {
/*  138 */     return 350;
/*      */   }
/*      */
  protected boolean addToRoom(String newroom, int roomnumb, int playernumb)
    {
        int roomid = getRoomID(newroom);
        if (roomid == 0) {
            return false;
        }
        if (roomnumb > 0) {
            if (this.room[roomid][roomnumb] == null) {
                this.room[this.charRoom[playernumb][0]][this.charRoom[playernumb][1]].removePlayer(this.charName[playernumb]);
                if (this.room[this.charRoom[playernumb][0]][this.charRoom[playernumb][1]].users < 1) {
                    this.room[this.charRoom[playernumb][0]][this.charRoom[playernumb][1]] = null;
                }
                this.room[roomid][roomnumb] = new Room(roomid, roomnumb);
                this.room[roomid][roomnumb].addPlayer(this.charName[playernumb], this.userSocket[playernumb]);
                this.charRoom[playernumb][0] = roomid;
                this.charRoom[playernumb][1] = roomnumb;
                sendJoinRoom(this.charName[playernumb]);

                return true;
            } else if (this.room[4][roomnumb].users < 10) {
                this.room[this.charRoom[playernumb][0]][this.charRoom[playernumb][1]].removePlayer(this.charName[playernumb]);
                if (this.room[this.charRoom[playernumb][0]][this.charRoom[playernumb][1]].users < 1) {
                    this.room[this.charRoom[playernumb][0]][this.charRoom[playernumb][1]] = null;
                }
                this.room[roomid][roomnumb].addPlayer(this.charName[playernumb], this.userSocket[playernumb]);
                this.charRoom[playernumb][0] = roomid;
                this.charRoom[playernumb][1] = roomnumb;
                sendJoinRoom(this.charName[playernumb]);
                return true;
            } else {
                roomnumb++;
                addToRoom(newroom, roomnumb, playernumb);
            }
        }
        for (int e = 1; e < 256; e++) {
            if (this.room[roomid][e] == null) {
                this.room[this.charRoom[playernumb][0]][this.charRoom[playernumb][1]].removePlayer(this.charName[playernumb]);
                if (this.room[this.charRoom[playernumb][0]][this.charRoom[playernumb][1]].users < 1) {
                    this.room[this.charRoom[playernumb][0]][this.charRoom[playernumb][1]] = null;
                }
                this.room[roomid][e] = new Room(roomid, e);
                this.room[roomid][e].addPlayer(this.charName[playernumb], this.userSocket[playernumb]);
                this.charRoom[playernumb][0] = roomid;
                this.charRoom[playernumb][1] = e;
                sendJoinRoom(this.charName[playernumb]);
                return true;
            } else if (this.room[4][e].users < 10) {
                this.room[this.charRoom[playernumb][0]][this.charRoom[playernumb][1]].removePlayer(this.charName[playernumb]);
                if (this.room[this.charRoom[playernumb][0]][this.charRoom[playernumb][1]].users < 1) {
                    this.room[this.charRoom[playernumb][0]][this.charRoom[playernumb][1]] = null;
                }
                this.room[roomid][e].addPlayer(this.charName[playernumb], this.userSocket[playernumb]);
                this.charRoom[playernumb][0] = roomid;
                this.charRoom[playernumb][1] = e;
                sendJoinRoom(this.charName[playernumb]);
                return true;
            } else {
                roomnumb++;
                addToRoom(newroom, roomnumb, playernumb);
                debug("Exepção em add to room");
            }
        }
        return false;
    }

  public int adduser(String username, PrintWriter sockout, Socket socket, int level)
    {
        if (getPlayerID(username) > 0 ) {
            kickPlayer(username, "the server for multiple logins.");
        }
        for (int i = 1; i < 256; i++) {
            if (this.charName[i] == null || this.charName[i].equals("")) {
                this.charName[i] = username;
                this.userSocket[i] = sockout;
                this.playerSocket[i] = socket;
                this.hp[i] = 700+((level+1)*20);
                this.mp[i] = 19+level;
                this.hpmax[i] = 700+((level+1)*20);
                this.mpmax[i] = 19+level;
                this.level[i] = level;
                this.isAlive[i] = true;
                this.pvpOn[i] = true;
        if (i % 2 == 0)
          this.pvpteam[i] = 0;
        else if (i % 2 != 0) {
          this.pvpteam[i] = 1;
        }
                for (int e = 1; e < 256; e++) {
                    if (this.room[4][e] == null) {
                        this.room[4][e] = new Room(4, e);
                        this.room[4][e].addPlayer(username, sockout);
                        this.charRoom[i][0] = 4;
                        this.charRoom[i][1] = e;
                        break;
                    } else if (this.room[4][e].users < 10) {
                        this.room[4][e].addPlayer(username, sockout);
                        this.charRoom[i][0] = 4;
                        this.charRoom[i][1] = e;
                        break;
                    }
                }
                this.users++;
                Main.sql.doupdate("UPDATE wqw_servers SET count=count+1 WHERE name='"+Main.serverName+"'");
                return i;
            }
        }
        return -1;
    }

/*      */
/*      */   protected int getPartyID(String charname)
/*      */   {
/*  295 */     int playernumb = getPlayerID(charname);
/*  296 */     if (this.partyRoom[playernumb] > 0) {
/*  297 */       return this.partyRoom[playernumb];
/*      */     }
/*  299 */     for (int e = 1; e < 50; e++) {
/*  300 */       if (this.party[e] != null) {
/*  301 */         return e;
/*      */       }
/*      */     }
/*  304 */     return 0;
/*      */   }
/*      */
/*      */   protected int addToParty(String charname, int partyid, boolean newParty)
/*      */   {
/*  309 */     int playernumb = getPlayerID(charname);
/*  310 */     if (partyid > 0) {
/*  311 */       if ((this.party[partyid] == null) && (newParty)) {
/*  312 */         this.party[partyid] = new Party(partyid);
/*  313 */         this.party[partyid].addPlayer(this.charName[playernumb], this.userSocket[playernumb]);
/*  314 */         this.partyRoom[playernumb] = partyid;
/*  315 */         return partyid;
/*  316 */       }if ((this.party[partyid].users < 10) && (!newParty)) {
/*  317 */         this.party[partyid].addPlayer(this.charName[playernumb], this.userSocket[playernumb]);
/*  318 */         this.partyRoom[playernumb] = partyid;
/*  319 */         return partyid;
/*      */       }
/*      */     }
/*  322 */     for (int e = 1; e < 50; e++) {
/*  323 */       if ((this.party[e] == null) && (newParty)) {
/*  324 */         this.party[e] = new Party(e);
/*  325 */         this.party[e].addPlayer(this.charName[playernumb], this.userSocket[playernumb]);
/*  326 */         this.partyRoom[playernumb] = e;
/*  327 */         return e;
/*  328 */       }if ((this.party[e].users < 10) && (!newParty)) {
/*  329 */         this.party[e] = new Party(e);
/*  330 */         this.party[e].addPlayer(this.charName[playernumb], this.userSocket[playernumb]);
/*  331 */         this.partyRoom[playernumb] = e;
/*  332 */         return e;
/*      */       }
/*      */     }
/*  335 */     return 0;
/*      */   }
/*      */
/*      */   protected boolean checkInUse(Socket socket, boolean terminate)
/*      */   {
/*  340 */     for (int i = 1; i < 50; i++) {
/*  341 */       if ((this.playerSocket[i] == null) ||
/*  342 */         (!Main.getip(this.playerSocket[i]).equals(Main.getip(socket)))) continue;
/*  343 */       if (terminate == true) {
/*  344 */         removeUserByID(i);
/*      */       }
/*  346 */       return true;
/*      */     }
/*      */
/*  350 */     return false;
/*      */   }
/*      */
/*      */
/*      */   protected String getCharnameSocket(Socket socket)
/*      */   {
/*  368 */     for (int i = 1; i < 50; i++) {
/*  369 */       if ((this.playerSocket[i] != null) &&
/*  370 */         (Main.getip(this.playerSocket[i]).equals(Main.getip(socket)))) {
/*  371 */         return this.charName[i];
/*      */       }
/*      */     }
/*  374 */     return "";
/*      */   }
/*      */
/*      */   protected void leaveParty(String charname)
/*      */   {
/*  379 */     int playernumb = getPlayerID(charname);
/*  380 */     this.party[this.partyRoom[playernumb]].removePlayer(charname);
/*  381 */     if (this.party[this.partyRoom[playernumb]].users < 2) {
/*  382 */       for (int i = 1; i < 50; i++) {
/*  383 */         if ((this.partyRoom[i] == this.partyRoom[playernumb]) && (i != playernumb)) {
/*  384 */           Packet sendPack = new Packet();
/*  385 */           sendPack.addString("{\"t\":\"xt\",\"b\":{\"r\":-1,\"o\":{\"cmd\":\"pc\",\"pid\":" + this.partyRoom[i] + "}}}");
/*  386 */           writePartyPacket(this.charName[i], sendPack, true, false);
/*  387 */           this.party[this.partyRoom[i]].removePlayer(this.charName[i]);
/*  388 */           this.partyRoom[i] = 0;
/*      */         }
/*      */       }
/*  391 */       this.party[this.partyRoom[playernumb]] = null;
/*      */     }
/*  393 */     this.partyRoom[playernumb] = 0;
/*      */   }
/*      */
/*      */   public void removeuser(String charname)
/*      */   {
/*  398 */     int i = getPlayerID(charname);
/*  399 */     if ((i >= 1) && (i <= 49)) {
/*  400 */       Main.server.remove(this.playerSocket[i].getRemoteSocketAddress());
/*  401 */       this.room[this.charRoom[i][0]][this.charRoom[i][1]].removePlayer(charname);
/*  402 */       Packet sendPack = new Packet();
/*  403 */       sendPack.addString("%xt%exitArea%-1%");
/*  404 */       sendPack.addInt(getPlayerID(this.charName[i]));
/*  405 */       sendPack.addString("%" + this.charName[i] + "%");
/*  406 */       writeMapPacket(this.charName[i], sendPack, true, true);
/*  407 */       sendPack.clean();
/*  408 */       sendPack.addXMLSingle(1, new String[] { "msg t", "sys" });
/*  409 */       sendPack.addXMLSingle(1, new String[] { "body action", "userGone", "r", "" + getPlayerRoom(this.charName[i])[0] * getPlayerRoom(this.charName[i])[1] });
/*  410 */       sendPack.addXMLSingle(0, new String[] { "user id", "" + getPlayerID(this.charName[i]) });
/*  411 */       sendPack.addXMLSingle(2, new String[] { "body" });
/*  412 */       sendPack.addXMLSingle(2, new String[] { "msg" });
/*  413 */       writeMapPacket(this.charName[i], sendPack, true, true);
/*  414 */       if (this.partyRoom[i] > 0) {
/*  415 */         sendPack.clean();
/*  416 */         sendPack.addString("{\"t\":\"xt\",\"b\":{\"r\":-1,\"o\":{\"cmd\":\"pr\",\"owner\":\"");
/*  417 */         sendPack.addString(this.party[this.partyRoom[i]].partyOwner);
/*  418 */         sendPack.addString("\",\"pid\":" + this.partyRoom[i] + ",\"typ\":\"l\",\"unm\":\"" + this.charName[i] + "\"}}}");
/*  419 */         writePartyPacket(this.charName[i], sendPack, true, false);
/*  420 */         for (int p = 0; p < 10; p++)
/*  421 */           if (((this.party[this.partyRoom[i]].partySlot[p] != null) || (!this.party[this.partyRoom[i]].partySlot[p].equals(""))) && (!this.party[this.partyRoom[i]].partySlot[p].equals(this.charName[i]))) {
/*  422 */             sendPack.clean();
/*  423 */             sendPack.addString("{\"t\":\"xt\",\"b\":{\"r\":-1,\"o\":{\"cmd\":\"pp\",\"owner\":\"" + this.party[this.partyRoom[i]].partySlot[p] + "\"}}}");
/*  424 */             writePartyPacket(this.charName[i], sendPack, true, false);
/*  425 */             this.party[this.partyRoom[i]].partyOwner = this.party[this.partyRoom[i]].partySlot[p];
/*  426 */             leaveParty(this.charName[i]);
/*  427 */             break;
/*      */           }
/*      */       }
/*      */       try
/*      */       {
/*  432 */         this.playerSocket[i].close();
/*      */       } catch (IOException ex) {
/*  434 */         Logger.getLogger(GameServer.class.getName()).log(Level.SEVERE, null, ex);
/*      */       }
/*  436 */       this.charName[i] = "";
/*  437 */       this.userSocket[i] = null;
/*  438 */       this.playerSocket[i] = null;
/*  439 */       this.charRoom[i][0] = 0;
/*  440 */       this.charRoom[i][1] = 0;
/*  441 */       this.hp[i] = 0;
/*  442 */       this.mp[i] = 0;
/*  443 */       this.state[i] = 0;
/*  444 */       this.hpmax[i] = 0;
/*  445 */       this.mpmax[i] = 0;
/*  446 */       this.users -= 1;
/*  447 */       Main.sql.doupdate("UPDATE wqw_servers SET count=count-1 WHERE name='" + Main.serverName + "'");
/*  448 */       debug("User removed: " + charname);
/*      */     } else {
/*  450 */       Main.server.remove(this.playerSocket[i].getRemoteSocketAddress());
/*  451 */       this.room[this.charRoom[i][0]][this.charRoom[i][1]].removePlayer(charname);
/*  452 */       this.users -= 1;
/*  453 */       Main.sql.doupdate("UPDATE wqw_servers SET count=count-1 WHERE name='" + Main.serverName + "'");
/*      */     }
/*      */   }
/*      */
/*      */   public void removeUserByID(int ID) {
/*  458 */     int i = ID;
/*  459 */     String charname = getCharname(ID);
/*  460 */     if ((i >= 1) && (i <= 49)) {
/*  461 */       Main.server.remove(this.playerSocket[i].getRemoteSocketAddress());
/*  462 */       this.room[this.charRoom[i][0]][this.charRoom[i][1]].removePlayer(charname);
/*  463 */       Packet sendPack = new Packet();
/*  464 */       sendPack.addString("%xt%exitArea%-1%");
/*  465 */       sendPack.addInt(getPlayerID(this.charName[i]));
/*  466 */       sendPack.addString("%" + this.charName[i] + "%");
/*  467 */       writeMapPacket(this.charName[i], sendPack, true, true);
/*  468 */       sendPack.clean();
/*  469 */       sendPack.addXMLSingle(1, new String[] { "msg t", "sys" });
/*  470 */       sendPack.addXMLSingle(1, new String[] { "body action", "userGone", "r", "" + getPlayerRoom(this.charName[i])[0] * getPlayerRoom(this.charName[i])[1] });
/*  471 */       sendPack.addXMLSingle(0, new String[] { "user id", "" + getPlayerID(this.charName[i]) });
/*  472 */       sendPack.addXMLSingle(2, new String[] { "body" });
/*  473 */       sendPack.addXMLSingle(2, new String[] { "msg" });
/*  474 */       writeMapPacket(this.charName[i], sendPack, true, true);
/*  475 */       if (this.partyRoom[i] > 0) {
/*  476 */         sendPack.clean();
/*  477 */         sendPack.addString("{\"t\":\"xt\",\"b\":{\"r\":-1,\"o\":{\"cmd\":\"pr\",\"owner\":\"");
/*  478 */         sendPack.addString(this.party[this.partyRoom[i]].partyOwner);
/*  479 */         sendPack.addString("\",\"pid\":" + this.partyRoom[i] + ",\"typ\":\"l\",\"unm\":\"" + this.charName[i] + "\"}}}");
/*  480 */         writeMapPacket(this.charName[i], sendPack, true, true);
/*  481 */         for (int p = 0; p < 10; p++)
/*  482 */           if (((this.party[this.partyRoom[i]].partySlot[p] != null) || (!this.party[this.partyRoom[i]].partySlot[p].equals(""))) && (!this.party[this.partyRoom[i]].partySlot[p].equals(this.charName[i]))) {
/*  483 */             sendPack.clean();
/*  484 */             sendPack.addString("{\"t\":\"xt\",\"b\":{\"r\":-1,\"o\":{\"cmd\":\"pp\",\"owner\":\"" + this.party[this.partyRoom[i]].partySlot[p] + "\"}}}");
/*  485 */             writePartyPacket(this.charName[i], sendPack, true, false);
/*  486 */             this.party[this.partyRoom[i]].partyOwner = this.party[this.partyRoom[i]].partySlot[p];
/*  487 */             leaveParty(this.charName[i]);
/*  488 */             break;
/*      */           }
/*      */       }
/*      */       try
/*      */       {
/*  493 */         this.playerSocket[i].close();
/*      */       } catch (IOException ex) {
/*  495 */         Logger.getLogger(GameServer.class.getName()).log(Level.SEVERE, null, ex);
/*      */       }
/*  497 */       this.charName[i] = "";
/*  498 */       this.userSocket[i] = null;
/*  499 */       this.playerSocket[i] = null;
/*  500 */       this.charRoom[i][0] = 0;
/*  501 */       this.charRoom[i][1] = 0;
/*  502 */       this.hp[i] = 0;
/*  503 */       this.mp[i] = 0;
/*  504 */       this.state[i] = 0;
/*  505 */       this.hpmax[i] = 0;
/*  506 */       this.mpmax[i] = 0;
/*  507 */       this.users -= 1;
/*  508 */       Main.sql.doupdate("UPDATE wqw_servers SET count=count-1 WHERE name='" + Main.serverName + "'");
/*  509 */       debug("User removed: " + charname);
/*      */     } else {
/*  511 */       Main.server.remove(this.playerSocket[i].getRemoteSocketAddress());
/*  512 */       this.room[this.charRoom[i][0]][this.charRoom[i][1]].removePlayer(charname);
/*  513 */       this.users -= 1;
/*  514 */       Main.sql.doupdate("UPDATE wqw_servers SET count=count-1 WHERE name='" + Main.serverName + "'");
/*      */     }
/*      */   }
/*      */
/*      */   protected void sendJoinRoom(String username)
/*      */   {
/*  520 */     Packet sendPack = new Packet();
/*  521 */     sendPack.addXMLSingle(1, new String[] { "msg t", "sys" });
/*  522 */     sendPack.addXMLSingle(1, new String[] { "body action", "uER", "r", "" + getPlayerRoom(username)[0] * getPlayerRoom(username)[1] });
/*  523 */     sendPack.addXMLSingle(1, new String[] { "u i", "" + getPlayerID(username), "m", "" + (getModerator(getPlayerID(username)) + getAdmin(getPlayerID(username))), "s", "0", "p", "" + (this.room[getPlayerRoom(username)[0]][getPlayerRoom(username)[1]].getPlayerSlot(username) + 1) });
/*  524 */     sendPack.addXML("n", "", 1);
/*  525 */     sendPack.addCDATA(username);
/*  526 */     sendPack.addXML("n", "", 2);
/*  527 */     sendPack.addXML("vars", "", 0);
/*  528 */     sendPack.addXMLSingle(2, new String[] { "u" });
/*  529 */     sendPack.addXMLSingle(2, new String[] { "body" });
/*  530 */     sendPack.addXMLSingle(2, new String[] { "msg" });
/*  531 */     writeMapPacket(username, sendPack, true, true);
/*  532 */     sendPlayerDetails(username);
/*      */   }
/*      */
/*      */   protected String getCharname(int ID)
/*      */   {
/*  537 */     return this.charName[ID];
/*      */   }
/*      */
/*      */   protected int getLevel(String charname)
/*      */   {
/*      */     try {
/*  543 */       ResultSet rs = Main.sql.doquery("SELECT * FROM wqw_users WHERE username='" + charname + "'");
/*  544 */       if (rs.next()) {
/*  545 */         return rs.getInt("level");
/*      */       }
/*  547 */       rs.close();
/*      */     } catch (Exception e) {
/*  549 */       debug("Exception in get level: " + e.getMessage());
/*      */     }
/*  551 */     return 0;
/*      */   }
/*      */
/*      */   protected String getCharnameByUserID(int userid)
/*      */   {
/*      */     try {
/*  557 */       ResultSet rs = Main.sql.doquery("SELECT * FROM wqw_users WHERE id=" + userid);
/*  558 */       if (rs.next()) {
/*  559 */         return rs.getString("username").toLowerCase();
/*      */       }
/*  561 */       rs.close();
/*      */     } catch (Exception e) {
/*  563 */       debug("Exception in get level: " + e.getMessage());
/*      */     }
/*  565 */     return "";
/*      */   }
/*      */
/*      */   protected int getModerator(int ID)
/*      */   {
/*  570 */     int userid = this.userID[ID];
/*      */     try {
/*  572 */       ResultSet rs = Main.sql.doquery("SELECT * FROM wqw_users WHERE id=" + userid);
/*  573 */       if (rs.next()) {
/*  574 */         return rs.getInt("moderator");
/*      */       }
/*  576 */       rs.close();
/*      */     } catch (Exception e) {
/*  578 */       return -1;
/*      */     }
/*  580 */     return 0;
/*      */   }
/*      */
/*      */   protected int getAdmin(int ID)
/*      */   {
/*  585 */     if (ID > 0) {
/*  586 */       int userid = this.userID[ID];
/*      */       try {
/*  588 */         ResultSet rs = Main.sql.doquery("SELECT * FROM wqw_users WHERE id=" + userid);
/*  589 */         if (rs.next()) {
/*  590 */           return rs.getInt("admin");
/*      */         }
/*  592 */         rs.close();
/*      */       } catch (Exception e) {
/*  594 */         return -1;
/*      */       }
/*  596 */       return 0;
/*      */     }
/*  598 */     return -1;
/*      */   }
/*      */
/*      */   protected int getAccess(int ID)
/*      */   {
/*  603 */     int userid = this.userID[ID];
/*      */     try {
/*  605 */       ResultSet rs = Main.sql.doquery("SELECT * FROM wqw_users WHERE id=" + userid);
/*  606 */       if (rs.next()) {
/*  607 */         return rs.getInt("access");
/*      */       }
/*  609 */       rs.close();
/*      */     } catch (Exception e) {
/*  611 */       return -1;
/*      */     }
/*  613 */     return 0;
/*      */   }
/*      */
/*      */   protected int getVIP(int ID)
/*      */   {
/*  618 */     int userid = this.userID[ID];
/*      */     try {
/*  620 */       ResultSet rs = Main.sql.doquery("SELECT * FROM wqw_users WHERE id=" + userid);
/*  621 */       if (rs.next()) {
/*  622 */         return rs.getInt("vip");
/*      */       }
/*  624 */       rs.close();
/*      */     } catch (Exception e) {
/*  626 */       return -1;
/*      */     }
/*  628 */     return 0;
/*      */   }
/*      */
/*      */   protected int getUserID(String Charname)
/*      */   {
/*      */     try {
/*  634 */       ResultSet rs = Main.sql.doquery("SELECT * FROM wqw_users WHERE username='" + Charname + "'");
/*  635 */       if (rs.next()) {
/*  636 */         return rs.getInt("id");
/*      */       }
/*  638 */       rs.close();
/*      */     } catch (Exception e) {
/*  640 */       return -1;
/*      */     }
/*  642 */     return 0;
/*      */   }
/*      */
/*      */   protected int getFD(int ID)
/*      */   {
/*  647 */     int userid = this.userID[ID];
/*      */     try {
/*  649 */       ResultSet rs = Main.sql.doquery("SELECT * FROM wqw_users WHERE id=" + userid);
/*  650 */       if (rs.next()) {
/*  651 */         return rs.getInt("fd");
/*      */       }
/*  653 */       rs.close();
/*      */     } catch (Exception e) {
/*  655 */       return -1;
/*      */     }
/*  657 */     return 0;
/*      */   }
/*      */
 protected int getPlayerID(String charname)
  {
    int result = -1;
    int i = 1;
    do {
      if ((this.charName[i] != null) && (this.charName[i].equals(charname)) && (!this.charName[i].equals(""))) {
        result = i;
        break;
      }
      i++;
    }while (i < 255);
    return result;
  }

   protected int[] getPlayerRoom(String charname)
    {
        int ID = getPlayerID(charname);
        int result[] = new int[2];
        result[0] = this.charRoom[ID][0];
        result[1] = this.charRoom[ID][1];
        return result;
    }

     protected int getRoomID(String roomname)
    {
        try {
            ResultSet rs = Main.sql.doquery("SELECT id FROM wqw_maps WHERE name='"+roomname+"' LIMIT 1");
            if (rs.next()) {
                return rs.getInt("id");
            }
            rs.close();
        } catch (Exception e) {
            debug("Exception in get room id: "+e.getMessage());
        }
        return 0;
    }
    protected String getRoomName(int roomid)
    {
        try {
            ResultSet rs = Main.sql.doquery("SELECT name FROM wqw_maps WHERE id='"+roomid+"' LIMIT 1");
            if (rs.next()) {
                return rs.getString("name");
            }
            rs.close();
        } catch (Exception e) {
            debug("Exception in get room name: "+e.getMessage());
        }
        return "null";
    }

  protected String[] getFramePad(String charname)
  {
    String[] frame = new String[2];
    Room cRoom = this.room[getPlayerRoom(charname)[0]][getPlayerRoom(charname)[1]];
    int slot = cRoom.getPlayerSlot(charname);
    frame[0] = cRoom.frame[slot];
    frame[1] = cRoom.pad[slot];
    return frame;
  }

  protected void sendPlayerDetails(String charname)
  {
    Packet sendPack = new Packet();
    Room cRoom = this.room[getPlayerRoom(charname)[0]][getPlayerRoom(charname)[1]];
    int slot = cRoom.getPlayerSlot(charname);
    int i = getPlayerID(charname);

    sendPack.addString("{\"t\":\"xt\",\"b\":{\"r\":-1,\"o\":{\"cmd\":\"uotls\",\"o\":{");
    sendPack.addString("\"strFrame\":\"" + cRoom.frame[slot] + "\"");
    sendPack.addString(",\"intMP\":" + this.mp[i]);
    sendPack.addString(",\"intLevel\":" + getLevel(charname));
    sendPack.addString(",\"entID\":" + i);
    sendPack.addString(",\"strPad\":\"" + cRoom.pad[slot] + "\"");
    sendPack.addString(",\"intMPMax\":" + this.mpmax[i]);
    sendPack.addString(",\"intHP\":" + this.hp[i]);
    sendPack.addString(",\"afk\":" + cRoom.afk[slot]);
    sendPack.addString(",\"intHPMax\":" + this.hpmax[i]);
    sendPack.addString(",\"ty\":" + cRoom.ty[slot]);
    sendPack.addString(",\"tx\":" + cRoom.tx[slot]);
    sendPack.addString(",\"intState\":1");
    sendPack.addString(",\"entType\":\"p\"");
    sendPack.addString(",\"showCloak\":true");
    sendPack.addString(",\"showHelm\":true");
    sendPack.addString(",\"strUsername\":\"" + charname + "\"");
    sendPack.addString(",\"pvpTeam\":" + this.pvpteam[i] + "");
    sendPack.addString(",\"uoName\":\"" + charname + "\"}");
    sendPack.addString(",\"unm\":\"" + charname + "\"}}}");

    writeMapPacket(charname, sendPack, true, true);
  }
/*      */
/*      */    public void writeGlobalPacket(String charname, Packet pack, boolean addNull, boolean notme)
    {
        int i = 1;
        int charI = getPlayerID(charname);
        String packet = pack.getPacket();
        if (addNull) {
            packet += "\u0000";
        }
        do {
            if (!(notme == true && i == charI) && this.userSocket[i] != null) {
                this.userSocket[i].write(packet);
                this.userSocket[i].flush();
            }
            i++;
        } while (i < 255);
    }

    public void writeOtherMapPacket(int[] room, Packet pack, boolean addNull)
    {
        int i = 1;
        String packet = pack.getPacket();
        if (addNull) {
            packet += "\u0000";
        }
        do {
            if ((this.charRoom[i][0] == room[0]) && (this.charRoom[i][1] == room[1])) {
                this.userSocket[i].write(packet);
                this.userSocket[i].flush();
            }
            i++;
        } while (i < 255);
    }

    public void writeMapPacket(String charname, Packet pack, boolean addNull, boolean notme)
    {
        int i = 1;
        int charI = getPlayerID(charname);
        String packet = pack.getPacket();
        if (addNull) {
            packet += "\u0000";
        }
        do {
            if (!(notme == true && i == charI) && (this.charRoom[i][0] == this.charRoom[charI][0]) && (this.charRoom[i][1] == this.charRoom[charI][1])) {
                this.userSocket[i].write(packet);
                this.userSocket[i].flush();
            }
            i++;
        } while (i < 255);
    }

  public void writePartyPacket(String charname, Packet pack, boolean addNull, boolean notme)
  {
    int i = 1;
    int charI = getPlayerID(charname);
    String packet = pack.getPacket();
    if (addNull)
      packet = packet + "";
    do
    {
      if (((notme != true) || (i != charI)) && (this.partyRoom[i] == this.partyRoom[charI])) {
        this.userSocket[i].write(packet);
        this.userSocket[i].flush();
      }
      i++;
    }while (i < 255);
  }

  public void writePlayerPacket(String charname, Packet pack, boolean addNull)
  {
    int i = getPlayerID(charname);
    String packet = pack.getPacket();
    if (addNull) {
      packet = packet + "";
    }
    this.userSocket[i].write(packet);
    this.userSocket[i].flush();
  }
protected void kickPlayer(String charname, String thisname)
  {
    try {
      Packet sendPack = new Packet();
      charname = charname.toLowerCase();
      thisname = thisname.toLowerCase();
      if (getAdmin(getPlayerID(charname)) < 0) {
        if ((getAdmin(getPlayerID(charname)) > 0) || (!thisname.equals("the server")) || (!thisname.equals("the server for multiple logins"))) {
          sendPack.addString("%xt%server%-1%" + thisname + " has attempted to kick " + charname + " and has been kicked and unmodded for insubordination.%");
          writeGlobalPacket(charname, sendPack, true, false);
          unmodPlayer(thisname, charname);
          kickPlayer(thisname, charname);
        }
      } else {
        int num = getPlayerID(charname);
        sendPack.addString("%xt%logoutWarning%-1%You have been kicked by " + thisname + "%45%");
        writePlayerPacket(charname, sendPack, true);
        sendPack.clean();
        sendPack.addString("%xt%loginResponse%-1%0%-1%%You have been kicked by " + thisname + "%");
        writePlayerPacket(charname, sendPack, true);
        if (num > 0) {
          Main.sql.doupdate("UPDATE wqw_users SET curServer='Offline' WHERE username='" + charname + "'");
          if (this.userSocket[num] != null) {
            Main.server.remove(this.playerSocket[num].getRemoteSocketAddress());
            removeuser(charname);
          } else {
            removeuser(charname);
          }
          sendPack.clean();
          sendPack.addString("%xt%warning%-1%" + charname + " has been kicked by " + thisname + ".%");
          writeGlobalPacket(charname, sendPack, true, false);
        }
      }
    } catch (Exception e) {
      debug("Exception in kick player: " + e.getMessage());
    }
  }
/*      */
/*      */   protected boolean isStaff(String charname) {
/*  857 */     if (getModerator(getPlayerID(charname)) > 0) {
/*  858 */       return true;
/*      */     }
/*  860 */     return getAdmin(getPlayerID(charname)) > 0;
/*      */   }
/*      */
/*      */   protected int getClassPoints(int id)
/*      */   {
/*      */     try
/*      */     {
/*  868 */       ResultSet rs = Main.sql.doquery("SELECT * FROM wqw_items WHERE sES='ar' AND equipped=1 AND userid=" + id);
/*  869 */       if (rs.next()) {
/*  870 */         return rs.getInt("classXP");
/*      */       }
/*  872 */       rs.close();
/*      */     } catch (Exception e) {
/*  874 */       debug("Exception in get class points: " + e.getMessage());
/*  875 */       return getClassPoints(id);
/*      */     }
/*      */
/*  878 */     return -1;
/*      */   }
/*      */
/*      */   protected void banPlayer(String charname, String thisname)
/*      */   {
/*      */     try {
/*  884 */       Packet sendPack = new Packet();
/*  885 */       charname = charname.toLowerCase();
/*  886 */       thisname = thisname.toLowerCase();
/*  887 */       if (((!thisname.contains("server")) || (getAdmin(getPlayerID(thisname)) < 0)) &&
/*  888 */         (getPlayerID(thisname) > 0) &&
/*  889 */         (getAdmin(getPlayerID(charname)) < 0)) {
/*  890 */         sendPack.addString("%xt%server%-1%" + thisname + " has attempted to ban " + charname + " and has been gannonbanned for insubordination.%");
/*  891 */         writeGlobalPacket(charname, sendPack, true, true);
/*  892 */         kickPlayer(thisname, charname);
/*  893 */         return;
/*      */       }
/*      */
/*  897 */       Main.sql.doupdate("UPDATE wqw_users SET banned=1 WHERE username='" + charname + "'");
/*  898 */       sendPack.addString("%xt%warning%-1%" + charname + " has been deactivated by " + thisname + ".%");
/*  899 */       writeGlobalPacket(charname, sendPack, true, true);
/*  900 */       debug("Player \"" + charname + "\" has been deactivated! (By " + thisname + ")");
/*  901 */       kickPlayer(charname, thisname);
/*      */     }
/*      */     catch (Exception e) {
/*  904 */       debug("Exception in ban player: " + e.getMessage());
/*      */     }
/*      */   }
/*      */
/*      */   protected void unbanPlayer(String charname, String thisname)
/*      */   {
/*      */     try {
/*  911 */       Packet sendPack = new Packet();
/*  912 */       Main.sql.doupdate("UPDATE wqw_users SET banned=0 WHERE username='" + charname + "'");
/*  913 */       sendPack.addString("%xt%server%-1%" + charname + " has been activated by " + thisname + ".%");
/*  914 */       writeGlobalPacket(thisname, sendPack, true, true);
/*  915 */       debug("Player \"" + charname + "\" has been activated (By " + thisname + ")");
/*      */     } catch (Exception e) {
/*  917 */       debug("Exception in unban player: " + e.getMessage());
/*      */     }
/*      */   }
/*      */
/*      */   protected void unmodPlayer(String charname, String thisname) {
/*      */     try {
/*  923 */       Packet sendPack = new Packet();
/*  924 */       Main.sql.doupdate("UPDATE wqw_users SET moderator=0,access=5,upgDays=-1 WHERE username='" + charname + "'");
/*  925 */       sendPack.addString("%xt%warning%-1%" + charname + " has been demodded by " + thisname + ".%");
/*  926 */       writeGlobalPacket(thisname,  sendPack, true, true);
/*  927 */       debug("Player \"" + charname + "\" has been demmoded! (By " + thisname + ")");
/*      */     } catch (Exception e) {
/*  929 */       debug("Exception in demodding player: " + e.getMessage());
/*      */     }
/*      */   }
/*      */
/*      */   protected void modPlayer(String charname, String thisname) {
/*      */     try {
/*  935 */       Packet sendPack = new Packet();
/*  936 */       Main.sql.doupdate("UPDATE wqw_users SET moderator=1,access=50,upgDays=0 WHERE username='" + charname + "'");
/*  937 */       sendPack.addString("%xt%server%-1%" + charname + " has been promoted to a moderator by " + thisname + ".%");
/*  938 */       writeGlobalPacket(thisname, sendPack, true, true);
/*  939 */       debug("Player \"" + charname + "\" has promoted to a moderator! (By " + thisname + ")");
/*      */     } catch (Exception e) {
/*  941 */       debug("Exception in modding player: " + e.getMessage());
/*      */     }
/*      */   }
/*      */
/*      */   protected void memPlayer(String charname, String thisname)
/*      */   {
/*      */     try {
/*  948 */       Packet sendPack = new Packet();
/*  949 */       Main.sql.doupdate("UPDATE wqw_users SET upgDays=0 WHERE username='" + charname + "'");
/*  950 */       sendPack.addString("%xt%server%-1%" + charname + " has been upgraded by " + thisname + ".%");
/*  951 */       writeGlobalPacket(thisname, sendPack, true, true);
/*  952 */       debug("Player \"" + charname + "\" has been upgraded! (By " + thisname + ")");
/*      */     } catch (Exception e) {
/*  954 */       debug("Exception in modding player: " + e.getMessage());
/*      */     }
/*      */   }
/*      */
/*      */   protected void vipPlayer(String charname, String thisname) {
/*      */     try {
/*  960 */       Packet sendPack = new Packet();
/*  961 */       Main.sql.doupdate("UPDATE wqw_users SET vip=1,access=30,upgDays=0 WHERE username='" + charname + "'");
/*  962 */       sendPack.addString("%xt%server%-1%" + charname + " has been promoted to a VIP by " + thisname + ".%");
/*  963 */       writeGlobalPacket(thisname, sendPack, true, true);
/*  964 */       debug("Player \"" + charname + "\" has been promoted to a VIP! (By " + thisname + ")");
/*      */     } catch (Exception e) {
/*  966 */       debug("Exception in modding player: " + e.getMessage());
/*      */     }
/*      */   }
/*      */
/*      */   protected void unvipPlayer(String charname, String thisname) {
/*      */     try {
/*  972 */       Packet sendPack = new Packet();
/*  973 */       Main.sql.doupdate("UPDATE wqw_users SET vip=0,access=5,upgDays=-1 WHERE username='" + charname + "'");
/*  974 */       sendPack.addString("%xt%server%-1%" + charname + " has been removed from VIP status by " + thisname + ".%");
/*  975 */       writeGlobalPacket(thisname, sendPack, true, true);
/*  976 */       debug("Player \"" + charname + "\" has been removed from vip status! (By " + thisname + ")");
/*      */     } catch (Exception e) {
/*  978 */       debug("Exception in modding player: " + e.getMessage());
/*      */     }
/*      */   }
/*      */
/*      */   protected void deletePlayer(String charname, String thisname) {
/*      */     try {
/*  984 */       Packet sendPack = new Packet();
/*  985 */       int id = 0;
/*  986 */       ResultSet rs = Main.sql.doquery("SELECT * FROM wqw_users WHERE username='" + charname + "'");
/*  987 */       if (rs.next()) {
/*  988 */         id = rs.getInt("id");
/*      */       }
/*  990 */       rs.close();
/*  991 */       Main.sql.doupdate("DELETE FROM `wqw_users` WHERE `username` = '" + charname + "'");
/*  992 */       Main.sql.doupdate("DELETE FROM `wqw_items` WHERE `userid` = " + id);
/*  993 */       sendPack.addString("%xt%warning%-1%" + charname + " has been deleted by " + thisname + ".%");
/*  994 */       writeGlobalPacket(thisname, sendPack, true, true);
/*  995 */       debug("Player \"" + charname + "\" has been deleted! (By " + thisname + ")");
/*      */     } catch (Exception e) {
/*  997 */       debug("Exception in deleting player: " + e.getMessage());
/*      */     }
/*      */   }
/*      */
/*      */   protected void finalize()
/*      */   {
/* 1004 */     for (int e = 0; e < 50; e++)
/*      */     {
/* 1006 */       if (this.party[e] != null) {
/* 1007 */         this.party[e].finalize();
/* 1008 */         this.party[e] = null;
/*      */       }
/*      */
/* 1011 */       for (int a = 0; a < 50; a++) {
/* 1012 */         if (this.room[e][a] != null) {
/* 1013 */           this.room[e][a].finalize();
/* 1014 */           this.room[e][a] = null;
/*      */         }
/*      */       }
/*      */
/* 1018 */       if (this.charName[e] != null)
/* 1019 */         removeuser(this.charName[e]);
/*      */     }
    }
}
/*      */ 