package WQWServer;

import java.io.*;
import java.net.*;
import java.sql.*;
import java.util.*;

/**
 * @author XVII
 * ServerConnection - Processes packets from a single client.
 */
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
   protected int monfighting = 0;
   protected int playerlevel;
   protected String gender;
   protected int weaponlevel;
   protected boolean fighting;
   protected boolean onpvp = false;
   protected int monkilled;
   protected int packetsend;
   protected int aacd;
   protected int userrank;
   protected String[] anim = new String[5];
   protected String[] str1 = new String[5];
   protected String[] auras = new String[5];
   protected String[] skillfx = new String[5];
   protected String[] friends = new String[20];
   protected String[] drops;
   protected int[] droppercent;
   protected int[] mpcost = new int[5];
   protected int classid;
   protected int[] questsaccepted = new int[20];
   protected int[] skills = new int[5];
   protected int[] passives = new int[2];
   pvpTimer pvpTime = new pvpTimer();
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
        Main.debug("Conexão com (" + this.socket.getRemoteSocketAddress() + ")", msg);
    }
    protected void write(String label, String msg) {
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
        return packet.substring(1, endSlash);
      }
      return packet.substring(1, endArrow);
    }
    if (packet.startsWith("%")) {
      String[] packet_handled = packet.split("%");
      return packet_handled[3];
    }
    return "Error";
  }

  protected void parseCMD(String cmd, String packet)
  {
    try {
      this.packetsend += 1;
      if (this.packetsend > 300) {
        this.gameServer.kickPlayer(this.account, "the server automatically kicked out due to packet spamming");
      }
      Packet recvPack = new Packet();
      recvPack.setPacket(packet);
      if (cmd.equals("addFriend"))
      {
        recvPack.removeHeader();
        String[] packet_handled = recvPack.getPacket().split("%");
        addFriend(this.account, packet_handled[2], true);
      } else if (cmd.equals("afk"))
      {
        recvPack.removeHeader();
        String[] packet_handled = recvPack.getPacket().split("%");
        setAFK(Boolean.parseBoolean(packet_handled[2]));
      } else if (cmd.equals("bankFromInv"))
      {
        recvPack.removeHeader();
        String[] packet_handled = recvPack.getPacket().split("%");
        bankFromInv(Integer.parseInt(packet_handled[2]), Integer.parseInt(packet_handled[3]));
      } else if (cmd.equals("bankToInv"))
      {
        recvPack.removeHeader();
        String[] packet_handled = recvPack.getPacket().split("%");
        bankToInv(Integer.parseInt(packet_handled[2]), Integer.parseInt(packet_handled[3]));
      } else if (cmd.equals("buyBagSlots"))
      {
        recvPack.removeHeader();
        String[] packet_handled = recvPack.getPacket().split("%");
        buyBagSlots(Integer.parseInt(packet_handled[2]));
      } else if (cmd.equals("buyBankSlots"))
      {
        recvPack.removeHeader();
        String[] packet_handled = recvPack.getPacket().split("%");
        buyBankSlots(Integer.parseInt(packet_handled[2]));
      } else if (cmd.equals("buyHouseSlots"))
      {
        recvPack.removeHeader();
        String[] packet_handled = recvPack.getPacket().split("%");
        buyHouseSlots(Integer.parseInt(packet_handled[2]));
      } else if (cmd.equals("buyItem"))
      {
        recvPack.removeHeader();
        String[] packet_handled = recvPack.getPacket().split("%");
        buyItem(Integer.parseInt(packet_handled[2]), Integer.parseInt(packet_handled[3]));
      } else if (cmd.equals("cc"))
      {
        recvPack.removeHeader();
        String[] packet_handled = recvPack.getPacket().split("%");
        cannedChat(packet_handled[2]);
      } else if (cmd.equals("cmd"))
      {
        recvPack.removeHeader();
        String[] packet_handled = recvPack.getPacket().split("%");
        String cmdSwitch = packet_handled[2];
        if (cmdSwitch.equals("tfer"))
        {
          try {
            if (packet_handled[4].indexOf("-") > 0) {
              String[] room = packet_handled[4].split("-");
              joinRoom(room[0], Integer.parseInt(room[1]), "Enter", "Spawn");
            }
            else if (packet_handled.length > 6) {
              joinRoom(packet_handled[4], -1, packet_handled[5], packet_handled[6]);
            } else {
              joinRoom(packet_handled[4], -1, "Enter", "Spawn");
            }
          }
          catch (Exception e) {
            serverMsg("Error in joining room (Known Bug) please try joining another room and try again.", "warning", false, false);
          }
        } else if (cmdSwitch.equals("goto"))
        {
          Packet sendPack = new Packet();
          debug(packet_handled[3]);
          if (this.gameServer.getPlayerID(packet_handled[3]) != -1) {
            int[] room = this.gameServer.getPlayerRoom(packet_handled[3]);
            String roomname = this.gameServer.getRoomName(room[0]);
            String[] frames = this.gameServer.getFramePad(packet_handled[3]);
            if (!roomname.equals("null"))
              joinRoom(roomname, room[1], frames[0], frames[1]);
          }
          else {
            sendPack.addString("%xt%warning%-1%User '" + packet_handled[3] + "' could not be found.%");
            send(sendPack, true);
          }
        }
        else if (!cmdSwitch.equals("house"))
        {
          if (!cmdSwitch.equals("ignoreList"))
          {
            if (cmdSwitch.equals("uopref"))
              changeUserSettings(Boolean.parseBoolean(packet_handled[4]), packet_handled[3]);
            else
              userCommand(packet_handled[2] + " " + packet_handled[3]);
          }
        }
      } else if (cmd.equals("declineFriend"))
      {
        recvPack.removeHeader();
        String[] packet_handled = recvPack.getPacket().split("%");
        requestFriend(packet_handled[2]);
      } else if (cmd.equals("deleteFriend"))
      {
        recvPack.removeHeader();
        String[] packet_handled = recvPack.getPacket().split("%");
        deleteFriend(this.account, packet_handled[3], true);
      } else if (cmd.equals("emotea"))
      {
        recvPack.removeHeader();
        String[] packet_handled = recvPack.getPacket().split("%");
        emoteChat(packet_handled[2]);
      } else if (cmd.equals("equipItem"))
      {
        recvPack.removeHeader();
        String[] packet_handled = recvPack.getPacket().split("%");
        equipItem(Integer.parseInt(packet_handled[2]), Integer.parseInt(packet_handled[1]));
      } else if (cmd.equals("firstJoin")) {
        sendLobby();
      } else if (cmd.equals("gar"))
      {
        recvPack.removeHeader();
        String[] packet_handled = recvPack.getPacket().split("%");

        playerAttack(packet_handled[3], Integer.parseInt(packet_handled[2]));
      } else if (cmd.equals("getQuests"))
      {
        recvPack.removeHeader();
        String[] packet_handled = recvPack.getPacket().split("%");
        String quests = "%";
        for (int i = 2; i < packet_handled.length - 1; i++) {
          quests = quests + packet_handled[i] + "%";
        }
        getQuests(quests);
      } else if (cmd.equals("gp"))
      {
        recvPack.removeHeader();
        String[] packet_handled = recvPack.getPacket().split("%");
        String gpSwitch = packet_handled[2];
        if (gpSwitch.equals("pi"))
        {
          partyInvite(packet_handled[3]);
        } else if (gpSwitch.equals("pd"))
        {
          partyDecline(Integer.parseInt(packet_handled[3]));
        } else if (gpSwitch.equals("pa"))
        {
          partyAccept(Integer.parseInt(packet_handled[3]));
        } else if (gpSwitch.equals("pl"))
        {
          partyLeave();
        } else if (gpSwitch.equals("pk"))
        {
          partyKick(packet_handled[3]);
        } else if (gpSwitch.equals("pp"))
        {
          partyPromote(packet_handled[3]);
        } else if (gpSwitch.equals("ps"))
        {
          partySummon(packet_handled[3]);
        } else if (gpSwitch.equals("psd"))
        {
          partySummonDecline(packet_handled[3]);
        } else if (gpSwitch.equals("psd"))
        {
          partySummonAccept();
        }
        else debug("Unknown packet recieved (gp): " + gpSwitch);
      }
      else if (cmd.equals("hi"))
      {
        playerTimerAttack();
      } else if (cmd.equals("loadBank"))
      {
        loadBank();
      } else if (!cmd.equals("loadFriendsList"))
      {
        if (cmd.equals("loadHouseInventory"))
        {
          loadHouseInventory();
        } else if (cmd.equals("loadShop"))
        {
          recvPack.removeHeader();
          String[] packet_handled = recvPack.getPacket().split("%");
          loadShop(Integer.parseInt(packet_handled[2]));
        } else if (cmd.equals("loadEnhShop"))
        {
          recvPack.removeHeader();
          String[] packet_handled = recvPack.getPacket().split("%");
          loadEnhShop(Integer.parseInt(packet_handled[2]));
        } else if (cmd.equals("loadHairShop"))
        {
          recvPack.removeHeader();
          String[] packet_handled = recvPack.getPacket().split("%");
          loadHairShop(Integer.parseInt(packet_handled[2]));
        } else if (cmd.equals("msg t='sys'"))
        {
          recvPack.removeHeader();
          String sysSwitch = recvPack.getXMLSingle("body action");
          if (sysSwitch.equals("verChk"))
          {
            sendVersion();
          } else if (sysSwitch.equals("login"))
          {
            doLogin(recvPack.getCDATA(recvPack.getXML("nick")), recvPack.getCDATA(recvPack.getXML("pword")), true);
          }
          else debug("Unknown packet recieved (sys): " + cmd);
        }
        else if (cmd.equals("message"))
        {
          recvPack.removeHeader();
          String[] packet_handled = recvPack.getPacket().split("%");
          if (packet_handled[2].startsWith("!"))
            userCommand(packet_handled[2].substring(1, packet_handled[2].length()));
          else
            userChat(Integer.parseInt(packet_handled[1]), packet_handled[2], packet_handled[3]);
        }
        else if (cmd.equals("me"))
        {
          recvPack.removeHeader();
          String[] packet_handled = recvPack.getPacket().split("%");
          if (packet_handled[2].startsWith("!"))
            userCommand(packet_handled[2].substring(1, packet_handled[2].length()));
          else
            userChat(Integer.parseInt(packet_handled[1]), packet_handled[2], packet_handled[3]);
        }
        else if (cmd.equals("moveToCell")) {
          recvPack.removeHeader();
          String[] packet_handled = recvPack.getPacket().split("%");
          moveToCell(packet_handled[2], packet_handled[3]);
        } else if (cmd.equals("mv"))
        {
          recvPack.removeHeader();
          String[] packet_handled = recvPack.getPacket().split("%");
          userMove(Integer.parseInt(packet_handled[2]), Integer.parseInt(packet_handled[3]), Integer.parseInt(packet_handled[4]), false);
        } else if (cmd.equals("policy-file-request"))
        {
          sendPolicy();
        } else if (cmd.equals("resPlayerTimed"))
        {
          respawnPlayer();
        } else if (cmd.equals("restRequest"))
        {
          restPlayer();
        } else if (cmd.equals("retrieveInventory"))
        {
          sendEnhancementDetails();
          loadBigInventory();
        }
        else if (cmd.equals("PVPQr")) {
          pvpQuery("new");
        } else if (cmd.equals("retrieveUserData"))
        {
          recvPack.removeHeader();
          String[] packet_handled = recvPack.getPacket().split("%");
          retrieveUserData(Integer.parseInt(packet_handled[2]), true);
        } else if (cmd.equals("retrieveUserDatas")) {
          recvPack.removeHeader();
          retrieveUserDatas(recvPack.getPacket());
        } else if (cmd.equals("requestFriend"))
        {
          recvPack.removeHeader();
          String[] packet_handled = recvPack.getPacket().split("%");
          requestFriend(packet_handled[2]);
       } else if (cmd.equals("aggroMon"))
       {
           recvPack.removeHeader();
           String[] packet_handled = recvPack.getPacket().split("%");
         if (this.playerRoom.monsterState[Integer.parseInt(packet_handled[2])] != 0) {
           this.fighting = true;
           playerHitTimer(Integer.parseInt(packet_handled[2]));
         }
        } else if (cmd.equals("removeItem")) {
          recvPack.removeHeader();
          String[] packet_handled = recvPack.getPacket().split("%");
          removeItem(Integer.parseInt(packet_handled[2]), Integer.parseInt(packet_handled[3]));
        } else if (cmd.equals("sellItem"))
        {
          recvPack.removeHeader();
          String[] packet_handled = recvPack.getPacket().split("%");
          sellItem(Integer.parseInt(packet_handled[2]), Integer.parseInt(packet_handled[4]));
        } else if (cmd.equals("unequipItem"))
        {
          recvPack.removeHeader();
          String[] packet_handled = recvPack.getPacket().split("%");
          unequipItem(Integer.parseInt(packet_handled[2]), Integer.parseInt(packet_handled[1]));
        } else if (cmd.equals("changeColor"))
        {
          recvPack.removeHeader();
          String[] packet_handled = recvPack.getPacket().split("%");
          changeColor(Integer.parseInt(packet_handled[2]), Integer.parseInt(packet_handled[3]), Integer.parseInt(packet_handled[4]), Integer.parseInt(packet_handled[5]));
        } else if (cmd.equals("changeArmorColor"))
        {
          recvPack.removeHeader();
          String[] packet_handled = recvPack.getPacket().split("%");
          changeArmorColor(Integer.parseInt(packet_handled[2]), Integer.parseInt(packet_handled[3]), Integer.parseInt(packet_handled[4]));
        } else if (cmd.equals("getDrop"))
        {
          recvPack.removeHeader();
          String[] packet_handled = recvPack.getPacket().split("%");
          getDrop(Integer.parseInt(packet_handled[2]));
        } else if (cmd.equals("enhanceItemShop"))
        {
          recvPack.removeHeader();
          String[] packet_handled = recvPack.getPacket().split("%");
          enhanceItem(Integer.parseInt(packet_handled[2]), Integer.parseInt(packet_handled[3]), false);
        } else if (cmd.equals("enhanceItemLocal")) {
          recvPack.removeHeader();
          String[] packet_handled = recvPack.getPacket().split("%");
          enhanceItem(Integer.parseInt(packet_handled[2]), Integer.parseInt(packet_handled[3]), true);
        } else if (cmd.equals("mtcid")) {
          recvPack.removeHeader();
          String[] packet_handled = recvPack.getPacket().split("%");
          Packet move = new Packet();
          move.addString("%xt%mtcid%-1%" + packet_handled[2] + "%");
          send(move, true);

          switch (Integer.parseInt(packet_handled[2])) {
          case 30:
            moveToCell("Morale1C", "Top");
            break;
          case 29:
            moveToCell("Enter1", "Left");
            break;
          case 28:
            moveToCell("Captain1", "Spawn");
            break;
          case 27:
            moveToCell("Morale1B", "Right");
            break;
          case 26:
            moveToCell("Morale1C", "Right");
            break;
          case 25:
            moveToCell("Morale1C", "Left");
            break;
          case 24:
            moveToCell("Morale1A", "Right");
            break;
          case 23:
            moveToCell("Morale1B", "Left");
            break;
          case 22:
            moveToCell("Crosslower", "Right");
            break;
          case 21:
            moveToCell("Resource1A", "Right");
            break;
          case 20:
            moveToCell("Resource1B", "Left");
            break;
          case 19:
            moveToCell("Crossupper", "Right");
            break;
          case 18:
            moveToCell("Resource1A", "Left");
            break;
          case 17:
            moveToCell("Crosslower", "Middle");
            break;
          case 16:
            moveToCell("Resource0A", "Right");
            break;
          case 15:
            moveToCell("Morale1A", "Left");
            break;
          case 14:
            moveToCell("Crossupper", "Bottom");
            break;
          case 13:
            moveToCell("Morale0A", "Right");
            break;
          case 12:
            moveToCell("Crossupper", "Right");
            break;
          case 11:
            moveToCell("Resource0B", "Right");
            break;
          case 10:
            moveToCell("Resource0A", "Left");
            break;
          case 9:
            moveToCell("Crosslower", "Left");
            break;
          case 8:
            moveToCell("Morale0B", "Right");
            break;
          case 7:
            moveToCell("Morale0A", "Left");
            break;
          case 6:
            moveToCell("Morale0C", "Right");
            break;
          case 5:
            moveToCell("Morale0C", "Left");
            break;
          case 4:
            moveToCell("Morale0B", "Left");
            break;
          case 3:
            moveToCell("Captain0", "Spawn");
            break;
          case 2:
            moveToCell("Enter0", "Right");
            break;
          case 1:
            moveToCell("Morale0C", "Top");
            break;
          }

        }
        else if (cmd.equals("PVPIr")) {
          recvPack.removeHeader();
          String[] packet_handled = recvPack.getPacket().split("%");
          if (!packet_handled[2].equals("0")) {
            pvpQuery("done");
            joinRoom("bludrutbrawl", 1, "Enter" + this.gameServer.pvpteam[this.accountid], "Spawn");
          } else {
            pvpQuery("none");
          }
        } else if (cmd.equals("tryQuestComplete")) {
          recvPack.removeHeader();
          String[] packet_handled = recvPack.getPacket().split("%");
          tryQuestComplete(Integer.parseInt(packet_handled[2]), Integer.parseInt(packet_handled[3]));
        } else if (cmd.equals("acceptQuest")) {
          recvPack.removeHeader();
          String[] packet_handled = recvPack.getPacket().split("%");
          for (int i = 1; i < 20; i++)
            if (this.questsaccepted[i] == 0) {
              this.questsaccepted[i] = Integer.parseInt(packet_handled[2]);
              debug("Quest ID: " + packet_handled[2] + " accepted.");
              return;
            }
        }
        else if (cmd.equals("geia"))
        {
          sendPotionAnimation();
        } else if (cmd.equals("whisper"))
        {
          recvPack.removeHeader();
          String[] packet_handled = recvPack.getPacket().split("%");
          whisperChat(packet_handled[2], packet_handled[3]);
        } else if (cmd.equals("Error")) {
          debug("Error in reading packet: " + packet);
        } else {
          debug("Unknown packet recieved: " + cmd);
        }
      }
    } catch (Exception e) {
      debug("Error in parsing CMD: " + e.getMessage() + ", Cause: " + e.getCause() + ", Packet: " + packet);
    }
  }

  protected void sendPotionAnimation()
  {
    Packet sendPack = new Packet();
    sendPack.addString("{\"t\":\"xt\",\"b\":{\"r\":-1,\"o\":{\"cmd\":\"seia\",\"o\":{\"nam\":\"Invigorating Nectar\",\"anim\":\"Salute\",\"mp\":0,\"desc\":\"Health potion effect\",\"auras\":[{\"nam\":\"Heal\",\"t\":\"s\",\"dur\":5,\"tgt\":\"f\"}],\"range\":808,\"fx\":\"w\",\"damage\":-8,\"dsrc\":\"SP1\",\"ref\":\"ph1\",\"tgt\":\"f\",\"typ\":\"m\",\"strl\":\"sp_eh1\",\"cd\":0},\"iRes\":1}}}");
    send(sendPack, true);
  }

  protected void tryQuestComplete(int questid, int citemid)
  {
    Packet sendPack = new Packet();
    debug("Completing Quest ID: " + questid);
    try {
      ResultSet rs = Main.sql.doquery("SELECT * FROM wqw_quests WHERE id=" + questid);
      if (rs.next()) {
        String sName = rs.getString("sName");
        int iGold = rs.getInt("iGold");
        int iExp = rs.getInt("iExp");
        String[] rewards = rs.getString("oRewards").split(":");
        String[] t = rs.getString("turnin").split(",");
        int[] qty = new int[t.length];
        int[] itemid = new int[t.length];
        for (int b = 0; b < t.length; b++) {
          String[] xx = t[b].split(":");
          qty[b] = Integer.parseInt(xx[1]);
          itemid[b] = Integer.parseInt(xx[0]);
        }
        turnInItem(itemid, qty);
        sendPack.addString("{\"t\":\"xt\",\"b\":{\"r\":-1,\"o\":{\"cmd\":\"ccqr\",\"rewardObj\":{\"iCP\":0,\"intGold\":" + iGold + ",\"intExp\":" + iExp + ",\"typ\":\"q\"},\"bSuccess\":1,\"QuestID\":" + questid + ",\"sName\":\"" + sName + "\"}}}");
        send(sendPack, true);
        sendPack.clean();
        sendPack.addString(addRewards(iExp, iGold, 0, 0, "q", this.accountid));
        send(sendPack, true);

        for (int i = 0; i < rewards.length; i++) {
          ResultSet xs = Main.sql.doquery("SELECT * FROM wqw_items WHERE itemid=" + Integer.parseInt(rewards[i]) + " AND userid=" + this.userid);
          if ((!xs.next()) && (citemid != -1))
            dropItem(citemid);
          else if ((citemid == -1) && (!xs.next())) {
            dropItem(Integer.parseInt(rewards[i]));
          }
        }
      }
      if (this.playerlevel >= 500) {
        levelUp();
      }
      for (int e = 0; e < this.questsaccepted.length; e++)
        if (this.questsaccepted[e] == questid) {
          this.questsaccepted[e] = 0;
          debug("Quest has been completed ID: " + questid);
          return;
        }
    }
    catch (Exception e) {
      debug("Error in completing quest: " + e.getMessage());
    }
  }

  protected void changeUserSettings(boolean set, String tobeset)
  {
    if (set)
      Main.sql.doupdate("UPDATE wqw_users SET " + tobeset + "=1 WHERE id=" + this.userid);
    else {
      Main.sql.doupdate("UPDATE wqw_users SET " + tobeset + "=0 WHERE id=" + this.userid);
    }
    if ((tobeset.equals("bGoto")) && (set))
      serverMsg("Aceitando Goto.", "server", false, false);
    else if ((tobeset.equals("bGoto")) && (!set)) {
      serverMsg("Ignorando Goto.", "warning", false, false);
    }
    if (tobeset.equals("bWhisper"))
      serverMsg("Aceitando PMs.", "server", false, false);
    else if ((tobeset.equals("bWhisper")) && (!set)) {
      serverMsg("Ignorando PMs.", "warning", false, false);
    }
    if ((tobeset.equals("bTT")) && (set))
      serverMsg("Ability ToolTips will always show on mouseover.", "server", false, false);
    else if ((tobeset.equals("bTT")) && (!set)) {
      serverMsg("Ability ToolTips will not show mouseover during combat.", "warning", false, false);
    }
    if ((tobeset.equals("bFriend")) && (set))
      serverMsg("Aceitando novos Amigos.", "server", false, false);
    else if ((tobeset.equals("bFriend")) && (!set)) {
      serverMsg("Ignorando novos Amigos.", "warning", false, false);
    }
    if ((tobeset.equals("bParty")) && (set))
      serverMsg("Aceitando Party.", "server", false, false);
    else if ((tobeset.equals("bParty")) && (!set))
      serverMsg("Ignorando Party.", "warning", false, false);
  }

  protected void addPvPTeamScore(int teamid, int score)
  {
    Packet sendPack = new Packet();
    if (teamid == 0)
      this.gameServer.lscore += score;
    else {
      this.gameServer.vscore += score;
    }
    if ((this.gameServer.lscore != 1000) && (this.gameServer.vscore == 1000));
    sendPack.addString("{\"t\":\"xt\",\"b\":{\"r\":-1,\"o\":{\"cmd\":\"PVPS\",\"pvpScore\":[{\"v\":" + this.gameServer.lscore + ",\"r\":0,\"m\":0,\"k\":0},{\"v\":" + this.gameServer.vscore + ",\"r\":0,\"m\":0,\"k\":0}]}}}");
    this.gameServer.writeMapPacket(this.account, sendPack, true, false);
  }

  protected void pvpQuery(String warzone)
  {
    Packet sendPack = new Packet();
    if (warzone.equals("new")) {
      sendPack.addString("%xt%server%-1%Voce foi movido para Warzone Bludrut Brawl !%");
      send(sendPack, true);
      sendPack.clean();
      sendPack.addString("{\"t\":\"xt\",\"b\":{\"r\":-1,\"o\":{\"cmd\":\"PVPQ\",\"bitSuccess\":1,\"avgWait\":-1,\"warzone\":\"bludrutbrawl\"}}}");
      send(sendPack, true);
      newPvpWarzone();
      this.onpvp = true;
    } else if (warzone.equals("done")) {
      sendPack.addString("{\"t\":\"xt\",\"b\":{\"r\":-1,\"o\":{\"cmd\":\"PVPQ\",\"bitSuccess\":0}}}");
      send(sendPack, true);
    } else if (warzone.equals("none")) {
      sendPack.addString("%xt%server%-1%Voce foi removido da Warzone !%");
      send(sendPack, true);
      sendPack.clean();
      sendPack.addString("{\"t\":\"xt\",\"b\":{\"r\":-1,\"o\":{\"cmd\":\"PVPQ\",\"bitSuccess\":0}}}");
      send(sendPack, true);
      this.onpvp = false;
    }
  }

  protected void newPvpWarzone()
  {
    final Packet sendPack = new Packet();
    Timer timer = new Timer();
    timer.schedule(new TimerTask()
    {
      public void run()
      {
        sendPack.addString("%xt%server%-1%Nova Warzone iniciada !%");
        ServerConnection.this.send(sendPack, true);
        sendPack.clean();
        sendPack.addString("{\"t\":\"xt\",\"b\":{\"r\":-1,\"o\":{\"cmd\":\"PVPI\",\"warzone\":\"bludrutbrawl\"}}}");
        ServerConnection.this.send(sendPack, true);
      }
    }
    , 3000L);
  }

  protected void removeItem(int itemid, int deleteid)
  {
    try
    {
      Packet sendPack = new Packet();
      ResultSet rs = Main.sql.doquery("SELECT * FROM wqw_items WHERE id=" + deleteid);
      if (rs.next()) {
        String sES = rs.getString("sES");
        int iQty = rs.getInt("iQty");
        if (sES.equals("None")) {
          Main.sql.doupdate("UPDATE wqw_items SET iQty=iQty-1 WHERE id=" + deleteid);
          if (iQty - 1 == 1)
            Main.sql.doupdate("DELETE FROM wqw_items WHERE id=" + deleteid);
        }
        else {
          Main.sql.doupdate("DELETE FROM wqw_items WHERE id=" + deleteid);
        }
        sendPack.addString("{\"t\":\"xt\",\"b\":{\"r\":-1,\"o\":{\"cmd\":\"removeItem\",\"CharItemID\":" + deleteid + "}}}");
      } else {
        sendPack.addString("{\"t\":\"xt\",\"b\":{\"r\":-1,\"o\":{\"cmd\":\"removeItem\",\"bitSuccess\":0,\"strMessage\":\"Item Does Not Exist\",\"CharItemID\":-1}}}");
      }
      rs.close();
      send(sendPack, true);
    } catch (Exception e) {
      debug("Exception in deleting item: " + e.getMessage());
    }
  }

  protected void clearAuras()
  {
    Packet sendPack = new Packet();
    sendPack.addString("{\"t\":\"xt\",\"b\":{\"r\":-1,\"o\":{\"cmd\":\"clearAuras\"}}}");
    send(sendPack, true);
  }

  protected void updateClass()
  {
    try {
      int classXP = 0;
      Packet sendPack = new Packet();
      ResultSet rs = Main.sql.doquery("SELECT * FROM wqw_items WHERE userid=" + this.userid + " AND sES='ar' AND equipped=1");
      if (rs.next()) {
        clearAuras();
        classXP = rs.getInt("classXP");
      }
      ResultSet rs2 = Main.sql.doquery("SELECT * FROM wqw_classes WHERE classid=" + this.classid);
      if (rs2.next()) {
        sendPack.addString("{\"t\":\"xt\",\"b\":{\"r\":-1,\"o\":{\"uid\":" + this.accountid + ",\"sStats\":\"" + rs2.getString("sStats") + "\",\"iCP\":" + classXP + ",\"cmd\":\"updateClass\",\"sDesc\":\"" + rs2.getString("sDesc") + "\",\"sClassCat\":\"" + rs2.getString("sClassCat") + "\",\"aMRM\":[" + rs2.getString("aMRM") + "],\"sClassName\":\"" + rs2.getString("className") + "\"}}}");
        send(sendPack, true);
        sendPack.clean();
        sendPack.addString("{\"t\":\"xt\",\"b\":{\"r\":-1,\"o\":{\"uid\":" + this.accountid + ",\"iCP\":" + classXP + ",\"cmd\":\"updateClass\",\"sClassCat\":\"" + rs2.getString("sClassCat") + "\",\"sClassName\":\"" + rs2.getString("className") + "\"}}}");
        this.gameServer.writeMapPacket(this.account, sendPack, true, true);
      }
    }
    catch (Exception e) {
      debug("Exception in update class: " + e.getMessage());
    }
  }

  protected void turnInItem(int[] itemid, int[] qty)
  {
    Packet sendPack = new Packet();
    try {
      for (int i = 0; i < itemid.length; i++) {
        ResultSet rs = Main.sql.doquery("SELECT * FROM wqw_items WHERE itemid=" + itemid[i] + " AND userid=" + this.userid);
        if (rs.next()) {
          int ccqty = rs.getInt("iQty") - qty[i];
          if (ccqty <= 0)
            Main.sql.doupdate("DELETE FROM wqw_items WHERE itemid=" + itemid[i] + " AND userid=" + this.userid);
          else {
            Main.sql.doupdate("UPDATE wqw_items SET iQty=" + ccqty + " WHERE itemid=" + itemid[i] + " AND userid=" + this.userid);
          }
        }
      }
      sendPack.addString("{\"t\":\"xt\",\"b\":{\"r\":-1,\"o\":{\"cmd\":\"turnIn\",\"sItems\":\"");
      for (int a = 0; a < itemid.length; a++) {
        if (a != 0) {
          sendPack.addString(",");
        }
        sendPack.addString(itemid[a] + ":" + qty[a]);
      }
      sendPack.addString("\"}}}");
      send(sendPack, true);
    } catch (Exception e) {
      debug("Error in turning in item:" + e.getMessage());
    }
  }

  protected void enhanceItem(int itemid, int enhanceid, boolean islocal)
  {
    try
    {
      Packet sendPack = new Packet();
      int EnhDPS = 0;
      int iCost = 0;
      int EnhRng = 0;
      int EnhLvl = 0;
      int EnhRty = 0;
      int EnhID = 0;
      String EnhName = "";
      sendPack.addString("{\"t\":\"xt\",\"b\":{\"r\":-1,\"o\":{");
      ResultSet is = Main.sql.doquery("SELECT * FROM wqw_equipment WHERE itemid=" + enhanceid);
      if (is.next()) {
        EnhDPS = is.getInt("iDPS");
        iCost = is.getInt("iCost");
        EnhRng = is.getInt("iRng");
        EnhLvl = is.getInt("iLvl");
        EnhRty = is.getInt("iRty");
        EnhID = is.getInt("EnhID");
        EnhName = is.getString("sName");
      }
      is.close();
      if (!islocal) {
        sendPack.addString("\"ItemID\":" + itemid + "," + "\"cmd\":\"enhanceItemShop\",\"EnhID\":" + "" + enhanceid + ",\"EnhLvl\":" + EnhLvl + ",\"EnhDPS\":" + EnhDPS + ",\"bSuccess\":1,\"iCost\":" + iCost + "," + "\"EnhRty\":" + EnhRty + ",\"EnhRng\":" + EnhRng + ",\"EnhName\":\"" + EnhName + "\",\"EnhPID\":" + EnhID + "}}}");

        Main.sql.doupdate("UPDATE wqw_users SET gold=gold-" + iCost + " WHERE id=" + this.userid);
      } else {
        sendPack.addString("\"ItemID\":" + itemid + "," + "\"cmd\":\"enhanceItemLocal\",\"EnhID\":" + "" + enhanceid + ",\"EnhLvl\":" + EnhLvl + ",\"EnhDPS\":" + EnhDPS + ",\"bSuccess\":1,\"iCost\":" + iCost + "," + "\"EnhRty\":" + EnhRty + ",\"EnhRng\":" + EnhRng + ",\"EnhName\":\"" + EnhName + "\",\"EnhPID\":" + EnhID + "}}}");

        ResultSet rs = Main.sql.doquery("SELECT * FROM wqw_items WHERE itemid=" + enhanceid + " AND userid=" + this.userid);
        if (rs.next()) {
          int[] eid = new int[1];
          int[] qty = new int[1];
          eid[0] = enhanceid;
          qty[0] = 1;
          turnInItem(eid, qty);
        }
      }
      Main.sql.doupdate("UPDATE wqw_items SET iLvl=" + EnhLvl + ",EnhID=" + EnhID + " WHERE userid=" + this.userid + " AND itemid=" + itemid);
      send(sendPack, true);
      sendPack.clean();
    } catch (Exception e) {
      debug("Exception in enhance item: " + e.getMessage() + ", itemid: " + itemid + ", enhanceid: " + enhanceid);
    }
  }

  protected void loadEnhShop(int shopid)
  {
    try {
      Packet sendPack = new Packet();
      ResultSet rs = Main.sql.doquery("SELECT * FROM wqw_enhshops WHERE shopid=" + shopid);
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
          ResultSet is = Main.sql.doquery("SELECT * FROM wqw_equipment WHERE itemID=" + items[e]);
          if (is.next()) {
            sendPack.addString("{\"sIcon\":\"" + is.getString("sIcon") + "\",\"ItemID\":\"" + is.getInt("itemID") + "\",\"iLvl\":\"" + is.getInt("iLvl") + "\",\"iEnh\":\"" + is.getInt("iEnh") + "\",\"sElmt\":\"" + is.getString("sElmt") + "\",\"bTemp\":\"" + is.getInt("bTemp") + "\",\"sLink\":\"" + is.getString("sLink") + "\",\"bStaff\":\"" + is.getInt("bStaff") + "\",\"iRng\":\"" + is.getInt("iRng") + "\",\"bCoins\":\"" + is.getInt("bCoins") + "\",\"iDPS\":\"" + is.getInt("iDPS") + "\",\"sES\":\"" + is.getString("sES") + "\",\"iHrs\":\"" + is.getInt("iHRS") + "\",\"sFile\":\"" + is.getString("sFile") + "\",\"sType\":\"" + is.getString("sType") + "\",\"sDesc\":\"" + is.getString("sDesc") + "\",\"iStk\":\"" + is.getInt("iStk") + "\",\"iEnhCost\":\"" + is.getInt("iCost") + "\",\"bUpg\":\"" + is.getInt("bUpg") + "\",\"iRty\":\"" + is.getInt("iRty") + "\",\"sName\":\"" + is.getString("sName") + "\",\"iQty\":\"" + is.getInt("iQty") + "\",\"EnhID\":\"" + is.getInt("EnhID") + "\"}");
          }
          is.close();
          e++;
        }
        sendPack.addString("]}}}");
        send(sendPack, true);
      }
    } catch (Exception e) {
      debug("Exception in load enhancement shop: " + e.getMessage());
    }
  }

  protected void getDrop(int itemid)
  {
    try {
      Packet sendPack = new Packet();
      boolean doContinue = true;
      int adjustid = 0;
      String sES = "";
      String className = "";
      int level = 1;
      int qty = 1;
      String isitem = "";
      ResultSet rs = Main.sql.doquery("SELECT * FROM wqw_equipment WHERE itemID=" + itemid);
      if (rs.next()) {
        level = rs.getInt("iLvl");
        sES = rs.getString("sES");
        className = rs.getString("sName");
        isitem = rs.getString("sType");
        qty = rs.getInt("iQty");
      }
      else {
        doContinue = false;
      }
      if (doContinue) {
        rs.close();
        if (sES.equals("ar")) {
          Main.sql.doupdate("INSERT INTO wqw_items (itemid, userid, sES, className, classXP , iLvl, EnhID) VALUES (" + itemid + ", " + this.userid + ", '" + sES + "', '" + className + "', '0', '0','-1')");
        } else if ((isitem.equals("Item")) || (isitem.equals("Quest Item"))) {
          ResultSet es = Main.sql.doquery("SELECT * FROM wqw_items WHERE itemid=" + itemid + " AND userid=" + this.userid);
          if (es.next())
            Main.sql.doupdate("UPDATE wqw_items SET iQty=iQty+" + qty + " WHERE itemid=" + itemid + " AND userid=" + this.userid);
          else
            Main.sql.doupdate("INSERT INTO wqw_items (itemid, userid, sES, iLvl, EnhID) VALUES (" + itemid + ", " + this.userid + ", '" + sES + "', '" + level + "', '-1')");
        }
        else {
          Main.sql.doupdate("INSERT INTO wqw_items (itemid, userid, sES, iLvl, EnhID) VALUES (" + itemid + ", " + this.userid + ", '" + sES + "', '0', '-1')");
        }
      }
      if (doContinue) {
        ResultSet es = Main.sql.doquery("SELECT * FROM wqw_items WHERE userid=" + this.userid + " AND itemid=" + itemid + " AND sES='" + sES + "'");
        if (es.next()) {
          adjustid = es.getInt("id");
        }
        es.close();
        sendPack.addString("{\"t\":\"xt\",\"b\":{\"r\":-1,\"o\":{\"ItemID\":" + itemid + ",\"cmd\":\"getDrop\"," + "\"bBank\":false,\"bitSuccess\":1,\"CharItemID\":");

        sendPack.addInt(adjustid);
        sendPack.addString(",\"iQty\":" + qty + "}}}");
      }
      send(sendPack, true);
    } catch (Exception e) {
      debug("Exception in getDrop(): " + e.getMessage());
    }
  }

  protected void changeArmorColor(int base, int trim, int accessory) {
    Main.sql.doupdate("UPDATE wqw_users SET cosColorBase=" + base + " WHERE id=" + this.userid);
    Main.sql.doupdate("UPDATE wqw_users SET cosColorAccessory=" + accessory + " WHERE id=" + this.userid);
    Main.sql.doupdate("UPDATE wqw_users SET cosColorTrim=" + trim + " WHERE id=" + this.userid);
  }
  protected void changeColor(int skincolor, int haircolor, int eyecolor, int hairid) {
    debug("Changing hair to id: " + hairid);
    Main.sql.doupdate("UPDATE wqw_users SET plaColorSkin=" + skincolor + " WHERE id=" + this.userid);
    Main.sql.doupdate("UPDATE wqw_users SET plaColorHair=" + haircolor + " WHERE id=" + this.userid);
    Main.sql.doupdate("UPDATE wqw_users SET plaColorEyes=" + eyecolor + " WHERE id=" + this.userid);
    try {
      ResultSet rs = Main.sql.doquery("SELECT * FROM wqw_hairs WHERE hairID=" + hairid);
      if (rs.next()) {
        String name = rs.getString("sName");
        String file = rs.getString("sFile");
        rs.close();
        Main.sql.doupdate("UPDATE wqw_users SET hairID=" + hairid + " WHERE id=" + this.userid);
        Main.sql.doupdate("UPDATE wqw_users SET hairName='" + name + "' WHERE id=" + this.userid);
        Main.sql.doupdate("UPDATE wqw_users SET hairFile='" + file + "' WHERE id=" + this.userid);
      }
      rs.close();
    } catch (Exception e) {
      debug("Error in changing hair style and colors: " + e.getMessage());
    }
  }

  protected void loadHairShop(int shopid) {
    try {
      Packet sendPack = new Packet();
      sendPack.addString("{\"t\":\"xt\",\"b\":{\"r\":-1,\"o\":{\"HairShopID\":\"" + shopid + "\",\"cmd\":\"loadHairShop\",\"hair\":[");
      ResultSet rs = Main.sql.doquery("SELECT * FROM wqw_hairshop WHERE id=" + shopid);
      if (rs.next()) {
        String[] items = rs.getString("hairs" + this.gender).split(",");
        int i = items.length;
        int e = 0;
        rs.close();
        while (e < i) {
          if (e != 0) {
            sendPack.addString(",");
          }
          ResultSet is = Main.sql.doquery("SELECT * FROM wqw_hairs WHERE hairID=" + items[e]);
          if (is.next()) {
            sendPack.addString("{\"sFile\":\"" + is.getString("sFile") + "\"" + ",\"HairID\":\"" + is.getInt("hairID") + "\"" + ",\"sName\":\"" + is.getString("sName") + "\"" + ",\"sGen\":\"" + is.getString("sGen") + "\"}");
          }

          is.close();
          e++;
        }
      }
      sendPack.addString("]}}}");
      send(sendPack, true);
    } catch (Exception e) {
      debug("Exception in loadHairShop(): " + e.getMessage());
    }
  }

  protected void addFriend(String thischar, String otherchar, boolean repeat)
  {
    try {
      Packet sendPack = new Packet();
      String[] account2 = new String[1];
      account2[0] = ("" + this.gameServer.userID[this.gameServer.getPlayerID(thischar)]);

      ResultSet rs2 = Main.sql.doquery("SELECT COUNT(*) AS rowcount FROM wqw_friends WHERE userid=" + account2[0]);

      ResultSetMetaData rsMetaData = rs2.getMetaData();

      int numberOfColumns = rsMetaData.getColumnCount();

      if (numberOfColumns == 0) {
        Main.sql.doupdate("INSERT INTO wqw_friends (userid) VALUES (" + this.userid + ")");
      }
      rs2.close();

      if (numberOfColumns < 10) {
        ResultSet rs = Main.sql.doquery("SELECT * FROM wqw_friends WHERE userid=" + account2[0]);
        if (rs.next()) {
          if (!rs.getString("friendid").equals("")) {
            Main.sql.doupdate("UPDATE wqw_friends SET friendid=CONCAT(friendid, ',', " + this.gameServer.userID[this.gameServer.getPlayerID(otherchar)] + ") WHERE userid=" + account2[0]);
            sendPack.addString("{\"t\":\"xt\",\"b\":{\"r\":-1,\"o\":{\"cmd\":\"addFriend\",\"friend\":");
            rs.close();
            ResultSet is = Main.sql.doquery("SELECT * FROM wqw_users WHERE id=" + this.gameServer.userID[this.gameServer.getPlayerID(otherchar)]);
            if (is.next()) {
              sendPack.addString("{\"iLvl\":\"" + is.getInt("level") + "\",\"ID\":\"" + is.getInt("id") + "\",\"sName\":\"" + is.getString("username") + "\",\"sServer\":\"" + is.getString("curServer") + "\"}");
            }
            sendPack.addString("}}}");
            this.gameServer.writePlayerPacket(thischar, sendPack, true);
            sendPack.clean();
            sendPack.addString("%xt%server%-1%" + otherchar + " added to your friends list.%");
            this.gameServer.writePlayerPacket(thischar, sendPack, true);
            is.close();
            updateFriends();
          } else if (rs.getString("friendid").indexOf(Integer.toString(this.gameServer.userID[this.gameServer.getPlayerID(otherchar)])) != -1) {
            sendPack.clean();
            ResultSet is = Main.sql.doquery("SELECT * FROM wqw_users WHERE id=" + this.gameServer.userID[this.gameServer.getPlayerID(otherchar)]);
            if (is.next()) {
              String ochar = is.getString("username");
              sendPack.addString("%xt%server%-1%" + ochar + " is already added to your friends list.%");
              this.gameServer.writePlayerPacket(thischar, sendPack, true);
            }
          } else {
            Main.sql.doupdate("UPDATE wqw_friends SET friendid=CONCAT(friendid, " + this.gameServer.userID[this.gameServer.getPlayerID(otherchar)] + ") WHERE userid=" + account2[0]);

            rs.close();
            ResultSet is = Main.sql.doquery("SELECT * FROM wqw_users WHERE id=" + this.gameServer.userID[this.gameServer.getPlayerID(otherchar)]);
            sendPack.addString("{\"t\":\"xt\",\"b\":{\"r\":-1,\"o\":{\"cmd\":\"addFriend\",\"friend\":");
            if (is.next()) {
              sendPack.addString("{\"iLvl\":\"" + is.getInt("level") + "\",\"ID\":\"" + is.getInt("id") + "\",\"sName\":\"" + is.getString("username") + "\",\"sServer\":\"" + is.getString("curServer") + "\"}");
            }
            sendPack.addString("}}}");
            this.gameServer.writePlayerPacket(thischar, sendPack, true);
            sendPack.clean();
            sendPack.addString("%xt%server%-1%" + otherchar + " added to your friends list.%");
            this.gameServer.writePlayerPacket(thischar, sendPack, true);
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
      debug("Exception in addFriend(): " + e.getMessage());
    }
  }

  protected void addGold(int gold)
  {
    Main.sql.doupdate("UPDATE wqw_users SET gold=gold+" + gold + " WHERE id=" + this.userid);
  }

  protected int getXpToLevel(int playerlevel)
  {
    if (playerlevel < 1001) {
      int points = 0;
      for (int lvl = 1; lvl <= playerlevel; lvl++)
      {
        points = (int)(points + Math.floor(lvl + 300.0D * Math.pow(2.0D, lvl / 7.0D)));
      }
      return (int)Math.floor(points / 4);
    }

    return 0;
  }

  protected int getHPwithNecklace() {
    try {
      ResultSet it = Main.sql.doquery("SELECT * FROM wqw_items WHERE sES='am' AND equipped=1 AND userid=" + this.userid);
      if (it.next()) {
        ResultSet ge = Main.sql.doquery("SELECT sLink FROM wqw_equipment WHERE itemID=" + it.getString("itemid"));
        if (ge.next()) {
          int newhp = this.gameServer.calculateHP(this.gameServer.level[this.accountid]) + Integer.parseInt(ge.getString("sLink"));
          return newhp;
        }
      }
    }
    catch (Exception e) {
    }
    return this.gameServer.hpmax[this.accountid];
  }
  protected void levelUp() {
    Packet sendPack = new Packet();
    try {
      ResultSet rs = Main.sql.doquery("SELECT * FROM wqw_users WHERE id=" + this.userid);
      if (rs.next()) {
        int xp = rs.getInt("xp");
        if (xp >= getXpToLevel(this.playerlevel)) {
          Main.sql.doupdate("UPDATE wqw_users SET level=level+1, xp=0 WHERE id=" + this.userid);
          this.playerlevel += 1;
          this.gameServer.mp[this.accountid] = this.gameServer.calculateMP(this.playerlevel);
          this.gameServer.hp[this.accountid] = this.gameServer.calculateHP(this.playerlevel);
          this.gameServer.hpmax[this.accountid] = this.gameServer.calculateHP(this.playerlevel);
          this.gameServer.mpmax[this.accountid] = this.gameServer.calculateMP(this.playerlevel);
          this.gameServer.level[this.accountid] = this.playerlevel;
          sendPack.addString("{\"t\":\"xt\",\"b\":{\"r\":-1,\"o\":{\"cmd\":\"levelUp\",\"intExpToLevel\":\"" + getXpToLevel(this.playerlevel) + "\",\"intLevel\":\"" + this.playerlevel + "\"}}}");
          send(sendPack, true);
          sendPack.clean();
          serverMsg(this.account + " Upou para " + this.playerlevel + "!", "server", false, true);
          sendUotls(true, true, true, true, true);
        }
      }
      rs.close();
    }
    catch (Exception err) {
      debug("Error in level up: " + err.getMessage());
    }
    if (this.playerlevel == 1000)
      serverMsg("Parabens ! " + this.account + " Voce alcancou o level maximo !", "server", true, false);
  }

  protected void changeLevel(int level)
  {
    Packet sendPack = new Packet();
    try {
      if (level <= 1000) {
        Main.sql.doupdate("UPDATE wqw_users SET level=" + level + ", xp=0 WHERE id=" + this.userid);
        this.playerlevel = level;
        int newhp = getHPwithNecklace();
        this.gameServer.hp[this.accountid] = this.gameServer.calculateHP(this.playerlevel);
        this.gameServer.hpmax[this.accountid] = this.gameServer.calculateHP(this.playerlevel);
        if (this.onpvp) {
          this.gameServer.hp[this.accountid] = newhp;
          this.gameServer.hpmax[this.accountid] = newhp;
        }
        this.gameServer.mp[this.accountid] = this.gameServer.calculateMP(level);
        this.gameServer.mpmax[this.accountid] = this.gameServer.calculateMP(level);
        this.gameServer.level[this.accountid] = level;
        sendPack.addString("{\"t\":\"xt\",\"b\":{\"r\":-1,\"o\":{\"cmd\":\"levelUp\",\"intExpToLevel\":\"" + getXpToLevel(this.playerlevel) + "\",\"intLevel\":\"" + this.playerlevel + "\"}}}");
        send(sendPack, true);
        sendPack.clean();
        sendUotls(true, true, true, true, true);
        gameServer.removeuser(this.account);
      } else {
        serverMsg("level Limite e 1000 !", "warning", false, false);
      }
    } catch (Exception e) {
      debug("Error in changing level: " + e.getMessage());
    }
  }

  protected String addRewards(int xpreward, int goldreward, int cpreward, int monstertype, String type, int monsterid) {
    Packet sendPack2 = new Packet();
    try {
      int cp = this.gameServer.getClassPoints(this.userid);
      int rank = this.gameServer.getRankFromCP(cp);
      cpreward = cp + cpreward;
      if (rank == -1) {
        rank = 10;
        cp = 302500;
        cpreward = 302500;
      }
      Main.sql.doupdate("UPDATE wqw_items SET classXP=" + cpreward + " WHERE userid=" + this.userid + " AND equipped=1 AND sES='ar'");
      Main.sql.doupdate("UPDATE wqw_users SET gold=gold+" + goldreward + ", xp=xp+" + xpreward + " WHERE id=" + this.userid);
      if ((type.equals("m")) && (goldreward == 50000)) {
        Main.sql.doupdate("UPDATE wqw_users SET coins=coins-" + goldreward + " WHERE id=" + this.userid);
      }
      ResultSet rs = Main.sql.doquery("SELECT * FROM wqw_users WHERE id=" + this.userid);
      if (rs.next()) {
        int gold = rs.getInt("gold");
        int xp = rs.getInt("xp");
        sendPack2.addString("{\"t\":\"xt\",\"b\":{\"r\":-1,\"o\":{\"id\":\"" + monsterid + "\",\"iCP\":" + cp + ",\"cmd\":\"addGoldExp\",\"intGold\":" + gold + ",\"intExp\":" + xp + ",\"typ\":\"" + type + "\"}}}");
        if (this.userrank != rank) {
          loadSkills(this.classid);
        }
      }
      rs.close();
    } catch (Exception e) {
      debug("Exception in addRewards(): " + e.getMessage());
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
        this.gameServer.xprate = rs.getInt("xprate");
        this.gameServer.goldrate = rs.getInt("goldrate");
      }
      rs.close();
      if (type.equals("p")) {
        goldreward = this.gameServer.level[monsterid] * 50 * this.gameServer.goldrate + randint * 2;
        xpreward = this.gameServer.level[monsterid] * 35 * this.gameServer.xprate + randint * 4;
        cpreward = xpreward / 2 + randint;
      } else {
        ResultSet is = Main.sql.doquery("SELECT * FROM wqw_monsters WHERE MonID=" + monstertype);
        if (is.next()) {
          goldreward = is.getInt("intGold") * this.gameServer.goldrate;
          xpreward = is.getInt("intExp") * this.gameServer.xprate * is.getInt("intLevel");
          if (xpreward != 0)
            cpreward = (int)(xpreward / 1.5D + randint);
          else {
            cpreward = 0;
          }
        }
      }

      if (this.playerlevel == 3000) {
        return addRewards(0, goldreward, cpreward, monstertype, type, monsterid);
      }
      return addRewards(xpreward, goldreward, cpreward, monstertype, type, monsterid);
    }
    catch (Exception e) {
      debug("Exception in addMonsterRewards(): " + e.getMessage());
    }
    return "";
  }

  protected void addQuestItem(int itemid)
  {
    Packet sendPack = new Packet();

    sendPack.addString("{\"t\":\"xt\",\"b\":{\"r\":-1,\"o\":{\"cmd\":\"addItems\",\"items\":{\"" + itemid + "\":{\"ItemID\":\"" + itemid + "\",\"iQty\":1}}}}}");
    send(sendPack, true);
  }

  protected void bankFromInv(int itemid, int adjustid)
  {
    try {
      Packet sendPack = new Packet();
      ResultSet rs = Main.sql.doquery("SELECT * FROM wqw_items WHERE userid=" + this.userid + " AND bBank=1");
      int i = 0;
      while (rs.next()) {
        i++;
      }
      rs.close();
      ResultSet is = Main.sql.doquery("SELECT * FROM wqw_users WHERE id=" + this.userid);
      if (is.next()) {
        if (is.getInt("slotBank") <= i) {
          sendPack.addString("%xt%warning%-1%You have the maximum items you can in your bank.%");
        } else {
          Main.sql.doupdate("UPDATE wqw_items SET bBank=1 WHERE userid=" + this.userid + " AND bBank=0 AND id=" + adjustid);
          sendPack.addString("{\"t\":\"xt\",\"b\":{\"r\":-1,\"o\":{\"ItemID\":" + itemid + ",\"cmd\":\"bankFromInv\"}}}");
        }
      }
      is.close();
      send(sendPack, true);
    } catch (Exception e) {
      debug("Exception in bankFromInv(): " + e.getMessage());
    }
  }

  protected void bankToInv(int itemid, int adjustid)
  {
    try {
      Packet sendPack = new Packet();
      ResultSet rs = Main.sql.doquery("SELECT * FROM wqw_items WHERE userid=" + this.userid + " AND bBank=0");
      int i = 0;
      while (rs.next()) {
        i++;
      }
      rs.close();
      ResultSet is = Main.sql.doquery("SELECT * FROM wqw_users WHERE id=" + this.userid);
      if (is.next()) {
        if (is.getInt("slotBag") <= i) {
          sendPack.addString("%xt%warning%-1%You have the maximum items you can in your inventory.%");
        } else {
          Main.sql.doupdate("UPDATE wqw_items SET bBank=0 WHERE userid=" + this.userid + " AND bBank=1 AND id=" + adjustid);
          sendPack.addString("{\"t\":\"xt\",\"b\":{\"r\":-1,\"o\":{\"ItemID\":" + itemid + ",\"cmd\":\"bankToInv\"}}}");
        }
      }
      is.close();
      send(sendPack, true);
    } catch (Exception e) {
      debug("Exception in bankToInv(): " + e.getMessage());
    }
  }

  protected void buyBankSlots(int amount)
  {
    try {
      Packet sendPack = new Packet();
      boolean doContinue = true;
      int coins = 0;
      int curSlots = 0;
      ResultSet is = Main.sql.doquery("SELECT * FROM wqw_users WHERE id=" + this.userid);
      if (is.next()) {
        coins = is.getInt("coins");
        curSlots = is.getInt("slotBank");
      }
      if (curSlots > 59) {
        sendPack.addString("{\"t\":\"xt\",\"b\":{\"r\":-1,\"o\":{\"cmd\":\"buyBankSlots\",\"bitSuccess\":0,\"strMessage\":\"You have the maximum bank slots avaliable.\",\"iSlots\":0}}}");
        doContinue = false;
      }
      if (doContinue) {
        if (coins < 200 * amount) {
          sendPack.addString("{\"t\":\"xt\",\"b\":{\"r\":-1,\"o\":{\"cmd\":\"buyBankSlots\",\"bitSuccess\":0,\"strMessage\":\"You do not have enough coins to buy that many slots.\",\"iSlots\":0}}}");
        } else {
          Main.sql.doupdate("UPDATE wqw_users SET coins=coins-" + 200 * amount + ", slotBank=slotBank+" + amount + " WHERE id=" + this.userid);
          sendPack.addString("{\"t\":\"xt\",\"b\":{\"r\":-1,\"o\":{\"cmd\":\"buyBankSlots\",\"bitSuccess\":1,\"iSlots\":" + amount + "}}}");
        }
      }
      is.close();
      send(sendPack, true);
    } catch (Exception e) {
      debug("Exception in buyBankSlots(): " + e.getMessage());
    }
  }

  protected void buyBagSlots(int amount)
  {
    try {
      Packet sendPack = new Packet();
      boolean doContinue = true;
      int coins = 0;
      int curSlots = 0;
      ResultSet is = Main.sql.doquery("SELECT * FROM wqw_users WHERE id=" + this.userid);
      if (is.next()) {
        coins = is.getInt("coins");
        curSlots = is.getInt("slotBag");
      }
      if (curSlots > 64) {
        sendPack.addString("{\"t\":\"xt\",\"b\":{\"r\":-1,\"o\":{\"cmd\":\"buyBagSlots\",\"bitSuccess\":0,\"strMessage\":\"You have the maximum bag slots avaliable.\",\"iSlots\":0}}}");
        doContinue = false;
      }
      if (doContinue) {
        if (coins < 200 * amount) {
          sendPack.addString("{\"t\":\"xt\",\"b\":{\"r\":-1,\"o\":{\"cmd\":\"buyBagSlots\",\"bitSuccess\":0,\"strMessage\":\"You do not have enough coins to buy that many slots.\",\"iSlots\":0}}}");
        } else {
          Main.sql.doupdate("UPDATE wqw_users SET coins=coins-" + 100 * amount + ", slotBag=slotBag+" + amount + " WHERE id=" + this.userid);
          sendPack.addString("{\"t\":\"xt\",\"b\":{\"r\":-1,\"o\":{\"cmd\":\"buyBagSlots\",\"bitSuccess\":1,\"iSlots\":" + amount + "}}}");
        }
      }
      is.close();
      send(sendPack, true);
    } catch (Exception e) {
      debug("Exception in buyBagSlots(): " + e.getMessage());
    }
  }

  protected void buyHouseSlots(int amount)
  {
    try {
      Packet sendPack = new Packet();
      boolean doContinue = true;
      int coins = 0;
      int curSlots = 0;
      ResultSet is = Main.sql.doquery("SELECT * FROM wqw_users WHERE id=" + this.userid);
      if (is.next()) {
        coins = is.getInt("coins");
        curSlots = is.getInt("slotHouse");
      }
      if (curSlots > 29) {
        sendPack.addString("{\"t\":\"xt\",\"b\":{\"r\":-1,\"o\":{\"cmd\":\"buyHouseSlots\",\"bitSuccess\":0,\"strMessage\":\"You have the maximum house slots avaliable.\",\"iSlots\":0}}}");
        doContinue = false;
      }
      if (doContinue) {
        if (coins < 200 * amount) {
          sendPack.addString("{\"t\":\"xt\",\"b\":{\"r\":-1,\"o\":{\"cmd\":\"buyHouseSlots\",\"bitSuccess\":0,\"strMessage\":\"You do not have enough coins to buy that many slots.\",\"iSlots\":0}}}");
        } else {
          Main.sql.doupdate("UPDATE wqw_users SET coins=coins-" + 200 * amount + ", slotHouse=slotHouse+" + amount + " WHERE id=" + this.userid);
          sendPack.addString("{\"t\":\"xt\",\"b\":{\"r\":-1,\"o\":{\"cmd\":\"buyHouseSlots\",\"bitSuccess\":1,\"iSlots\":" + amount + "}}}");
        }
      }
      is.close();
      send(sendPack, true);
    } catch (Exception e) {
      debug("Exception in buyHouseSlots(): " + e.getMessage());
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
      int iscoins = 0;
      int qty = 1;
      String isitem = "";
      String className = "";
      String sES = "";
      int EnhID = 0;
      ResultSet rs = Main.sql.doquery("SELECT * FROM wqw_equipment WHERE itemID=" + itemid);
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
      if (level > this.playerlevel) {
        sendPack.addString("{\"t\":\"xt\",\"b\":{\"r\":-1,\"o\":{\"cmd\":\"buyItem\",\"bitSuccess\":0,\"strMessage\":\"Level requirement not met!\",\"CharItemID\":-1}}}");
        doContinue = false;
      }
      if (doContinue) {
        rs.close();
        ResultSet is = Main.sql.doquery("SELECT * FROM wqw_users WHERE id=" + this.userid);
        if (is.next()) {
          gold = is.getInt("gold");
        }
        is.close();
        if (gold - buyprice >= 0) {
          if (sES.equals("ar")) {
            Main.sql.doupdate("INSERT INTO wqw_items (itemid, userid, sES, className, classXP , iLvl, EnhID) VALUES (" + itemid + ", " + this.userid + ", '" + sES + "', '" + className + "', '0', '" + level + "','" + EnhID + "')");
          } else if ((isitem.equals("Item")) || (isitem.equals("Quest Item"))) {
            ResultSet es = Main.sql.doquery("SELECT * FROM wqw_items WHERE itemid=" + itemid + " AND userid=" + this.userid);
            if (es.next())
              Main.sql.doupdate("UPDATE wqw_items SET iQty=iQty+" + qty + " WHERE itemid=" + itemid + " AND userid=" + this.userid);
            else
              Main.sql.doupdate("INSERT INTO wqw_items (itemid, userid, sES, iLvl, EnhID) VALUES (" + itemid + ", " + this.userid + ", '" + sES + "', '" + level + "', '" + EnhID + "')");
          }
          else {
            Main.sql.doupdate("INSERT INTO wqw_items (itemid, userid, sES, iLvl, EnhID) VALUES (" + itemid + ", " + this.userid + ", '" + sES + "', '" + level + "', '" + EnhID + "')");
          }
          if (iscoins != 1)
            Main.sql.doupdate("UPDATE wqw_users SET gold=" + (gold - buyprice) + " WHERE id=" + this.userid);
          else
            Main.sql.doupdate("UPDATE wqw_users SET coins=coins-" + buyprice + " WHERE id=" + this.userid);
        }
        else {
          sendPack.addString("{\"t\":\"xt\",\"b\":{\"r\":-1,\"o\":{\"cmd\":\"buyItem\",\"bitSuccess\":0,\"strMessage\":\"Not Enough Gold\",\"CharItemID\":-1}}}");
          doContinue = false;
        }
      }
      if (doContinue) {
        ResultSet es = Main.sql.doquery("SELECT * FROM wqw_items WHERE userid=" + this.userid + " AND itemid=" + itemid + " AND sES='" + sES + "'");
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
      debug("Exception in buyItem(): " + e.getMessage());
    }
  }

  protected void cannedChat(String chat)
  {
    Packet sendPack = new Packet();
    sendPack.addString("%xt%cc%-1%");
    sendPack.addString(chat);
    sendPack.addString("%" + this.account + "%");
    this.gameServer.writeMapPacket(this.account, sendPack, true, false);
    write("Canned Chat - ", this.account + ": " + chat);
  }

  protected void declineFriend(String otherchar)
  {
    Packet sendPack = new Packet();
    sendPack.addString("%xt%server%-1%You declined the friend request.%");
    send(sendPack, true);
    sendPack.clean();
    sendPack.addString("%xt%server%-1%" + this.account + " declined your friend request.%");
    this.gameServer.writePlayerPacket(otherchar, sendPack, true);
  }

  protected void deleteFriend(String thischar, String otherchar, boolean repeat)
  {
    try {
      Packet sendPack = new Packet();
      String account2 = "" + this.gameServer.userID[this.gameServer.getPlayerID(thischar)];
      String account3 = "" + this.gameServer.userID[this.gameServer.getPlayerID(otherchar)];

      String newFriend = "";
      ResultSet rs = Main.sql.doquery("SELECT * FROM wqw_friends WHERE userid=" + account2);
      if (rs.next()) {
        String[] temp = rs.getString("friendid").split(",");
        if (temp[(temp.length - 1)].equals(account3))
          newFriend = rs.getString("friendid").replace(account3, "");
        else {
          newFriend = rs.getString("friendid").replace(account3 + ",", "");
        }
        Main.sql.doupdate("UPDATE wqw_friends SET friendid='" + newFriend + "' WHERE userid=" + account2);
        sendPack.addString("{\"t\":\"xt\",\"b\":{\"r\":-1,\"o\":{\"cmd\":\"deleteFriend\",\"ID\":" + account3 + "}}}");
        this.gameServer.writePlayerPacket(thischar, sendPack, true);
      }
      if (repeat) {
        deleteFriend(otherchar, thischar, false);
      }
      rs.close();
    } catch (Exception e) {
      debug("Exception in delete friend: " + e.getMessage());
    }
  }

  protected void emoteAll(String emote)
  {
    Packet sendPack = new Packet();
    for (int i = 0; i < 10; i++) {
      sendPack.clean();
      sendPack.addString("%xt%emotea%-1%");
      sendPack.addString(emote);
      sendPack.addString("%" + i + "%");
      this.gameServer.writeMapPacket(this.account, sendPack, true, false);
    }
  }

  protected void forceEmote(String[] cmd) {
    Packet sendPack = new Packet();
    sendPack.addString("%xt%emotea%-1%");
    sendPack.addString(cmd[0]);
    sendPack.addString("%" + this.gameServer.getPlayerID(cmd[1]) + "%");
    this.gameServer.writeMapPacket(this.account, sendPack, true, false);
  }

  protected void getCharacterSettings() {
    String[] classShort = new String[6];
    classShort[0] = "bParty";
    classShort[1] = "bGoto";
    classShort[2] = "bWhisper";
    classShort[3] = "bTT";
    classShort[4] = "bFriend";
    try {
      int i = 0;
      while (i < 5) {
        ResultSet rs = Main.sql.doquery("SELECT * FROM wqw_users WHERE id=" + this.userid);
        if (rs.next()) {
          if (classShort[i].equals("bGoto")) {
            if (rs.getInt("bGoto") == 1)
              serverMsg("Aceitando Goto.", "server", false, false);
            else
              serverMsg("Ignorando Goto.", "warning", false, false);
          }
          else if (classShort[i].equals("bWhisper")) {
            if (rs.getInt("bWhisper") == 1)
              serverMsg("Aceitando PMs.", "server", false, false);
            else
              serverMsg("Ignorando PMs.", "warning", false, false);
          }
          else if (classShort[i].equals("bTT")) {
            if (rs.getInt("bTT") == 1)
              serverMsg("Ability ToolTips will always show on mouseover.", "server", false, false);
            else
              serverMsg("Ability ToolTips will not show on mouseover during combat.", "warning", false, false);
          }
          else if (classShort[i].equals("bFriend")) {
            if (rs.getInt("bFriend") == 1)
              serverMsg("Aceitando novos Amigos.", "server", false, false);
            else
              serverMsg("Ignorando novos Amigos.", "warning", false, false);
          }
          else if (classShort[i].equals("bParty")) {
            if (rs.getInt("bParty") == 1)
              serverMsg("Aceitando Party.", "server", false, false);
            else {
              serverMsg("Ignorando Party.", "warning", false, false);
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

  protected void serverMsg(String msg, String type, boolean global, boolean map) {
    Packet sendPack = new Packet();
    sendPack.addString("%xt%" + type + "%-1%" + msg + "%");
    if (global)
      this.gameServer.writeGlobalPacket(this.account, sendPack, true, false);
    else if (map)
      this.gameServer.writeMapPacket(this.account, sendPack, true, false);
    else {
      send(sendPack, true);
    }
    sendPack.clean();
  }
   protected void doLogin(String user, String pass, boolean repeat)
   {
     Packet sendPack = new Packet();
     sendPack.addString("%xt%loginResponse%-1%");
     try {
       ResultSet rs = Main.sql.doquery("SELECT * FROM wqw_users WHERE username='" + user + "' AND password='" + pass + "' AND banned=0 LIMIT 1");
       if (rs.next()) {
         this.userid = rs.getInt("id");
         this.charname = rs.getString("username");
         this.classid = rs.getInt("currentClass");
         this.gender = rs.getString("gender");
         int result = this.gameServer.adduser(user, this.socketOut, this.socket, rs.getInt("level"));
         if (result != -1) {
           String message = getMessage();
           while (message.equals("")) {
             message = getMessage();
           }
           String news = getNews();
           while (news.equals("")) {
             news = getNews();
           }
           sendPack.addString("true%" + result + "%" + user + "%" + message + "%" + pass + "%" + news + "%");
           this.gameServer.userID[result] = this.userid;
           this.accountid = result;
           this.account = user;
           this.playerRoom = this.gameServer.room[this.gameServer.getPlayerRoom(this.account)[0]][this.gameServer.getPlayerRoom(this.account)[1]];
           this.playerSlot = this.playerRoom.getPlayerSlot(this.account);
           Main.sql.doupdate("UPDATE wqw_users SET curServer='" + Main.serverName + "' WHERE username='" + this.account + "'");
           pingTimer.main(this);
         } else {
           sendPack.addString("0%-1%%Este servidor esta cheio, por favor escolha outro..%");
         }
       } else {
         sendPack.addString("0%-1%%Data User '" + this.account + "' não pode ser carregado. Por favor contate a equipe do Neo Rider Quest Worlds para que o erro seja resolvido .%");
      }
       rs.close();
      getCharacterSettings();
       sendSettings();
       send(sendPack, true);

       debug("Loggado : " + user);
     } catch (Exception e) {
       debug("Excepção em dologin: " + e.getMessage());
       if (repeat)
         doLogin(user, pass, false);
        else
           debug("erro finalizado a execução na classe dologin");
         finalize();
         }

   }
    protected void sendCurrentEvent()
  {
    Packet sendPack = new Packet();
    sendPack.addString("%xt%server%-1%");
    try {
      ResultSet rs = Main.sql.doquery("SELECT event FROM wqw_settings LIMIT 1");
      if (rs.next())
        if (!rs.getString("event").equals(""))
          sendPack.addString("Current Event! - " + rs.getString("event"));
        else
          sendPack.addString("There are currently no events this time.");
    }
    catch (Exception err)
    {
      debug(err.getMessage());
    }
    sendPack.addString("%");
    send(sendPack, true);
  }

  protected void emoteChat(String chat) {
    Packet sendPack = new Packet();
    sendPack.addString("%xt%emotea%-1%");
    sendPack.addString(chat);
    sendPack.addString("%" + this.accountid + "%");
    this.gameServer.writeMapPacket(this.account, sendPack, true, true);
  }

  protected void equipItem(int itemid, int adjustid)
  {
    try {
      Packet sendPack = new Packet();
      sendPack.addString("{\"t\":\"xt\",\"b\":{\"r\":-1,\"o\":{\"uid\":");
      sendPack.addInt(this.accountid);
      ResultSet rs = Main.sql.doquery("SELECT * FROM wqw_equipment WHERE itemID=" + itemid);
      if (rs.next()) {
        sendPack.addString(",\"ItemID\":\"" + itemid + "\",\"strES\":\"" + rs.getString("sES") + "\",\"cmd\":\"equipItem\",\"sFile\":\"" + rs.getString("sFile") + "\",\"sLink\":\"" + rs.getString("sLink") + "\"");
        if (rs.getString("sES").equals("Weapon")) {
          sendPack.addString(",\"sType\":\"" + rs.getString("sType") + "\"");
        }
        sendPack.addString("}}}");
      }
      String type = rs.getString("sES");
      String classname = rs.getString("sName");
      int userclassid = rs.getInt("classID");
      rs.close();

      Main.sql.doupdate("UPDATE wqw_items SET equipped=0 WHERE userid=" + this.userid + " AND equipped=1 AND sES='" + type + "'");
      Main.sql.doupdate("UPDATE wqw_items SET equipped=1 WHERE userid=" + this.userid + " AND itemid=" + itemid + " AND equipped=0");
      if (type.equals("Weapon")) {
        ResultSet is = Main.sql.doquery("SELECT * FROM wqw_items WHERE id=" + adjustid);
        if (is.next()) {
          this.weaponlevel = is.getInt("iLvl");
        }
        is.close();
        initAutoAttack();
      }
      this.gameServer.writeMapPacket(this.account, sendPack, true, false);
      sendPack.clean();
      if (type.equals("ar")) {
        this.classid = userclassid;
        Main.sql.doupdate("UPDATE wqw_users SET currentClass=" + this.classid + " WHERE id=" + this.userid);
        Main.sql.doupdate("UPDATE wqw_items SET className='" + classname + "' WHERE userid=" + this.userid + " AND sES='ar' AND equipped=1");
        updateClass();
        loadSkills(this.classid);
      }
    } catch (Exception e) {
      debug("Exception in equip item: " + e.getMessage() + ", itemid: " + itemid + ", adjustid: " + adjustid);
    }
  }

  protected String getClassName(int id)
  {
    try {
      ResultSet rs = Main.sql.doquery("SELECT * FROM wqw_items WHERE userid=" + id + " AND equipped=1 AND sES='ar'");
      if (rs.next()) {
        return rs.getString("className");
      }
      rs.close();
    } catch (Exception e) {
      debug("Exception in get class points: " + e.getMessage());
    }
    return "Error";
  }

  protected boolean isPotionsEquipped()
  {
    try {
      ResultSet rs = Main.sql.doquery("SELECT * FROM wqw_items WHERE equipped=1 AND itemid=1749 AND userid=" + this.userid);
      if (rs.next())
        return true;
    }
    catch (Exception e)
    {
    }
    return false;
  }

  protected String getItemInfo(int itemid)
  {
    Packet itemPack = new Packet();
    try {
      ResultSet rs = Main.sql.doquery("SELECT * FROM wqw_equipment WHERE itemid=" + itemid);
      if (rs.next()) {
        itemPack.addString("\"sIcon\":\"" + rs.getString("sIcon") + "\"," + "\"ItemID\":\"" + rs.getInt("itemID") + "\"," + "\"iLvl\":\"" + rs.getInt("iLvl") + "\"," + "\"sLink\":\"" + rs.getString("sLink") + "\"," + "\"sElmt\":\"" + rs.getString("sElmt") + "\"," + "\"bTemp\":\"" + rs.getInt("bTemp") + "\"," + "\"bStaff\":\"" + rs.getInt("bStaff") + "\"," + "\"iRng\":\"" + rs.getInt("iRng") + "\"," + "\"bCoins\":\"" + rs.getInt("bCoins") + "\"," + "\"iDPS\":\"" + rs.getInt("iDPS") + "\"," + "\"sES\":\"" + rs.getString("sES") + "\"," + "\"bitSuccess\":\"1\"," + "\"sType\":\"" + rs.getString("sType") + "\"," + "\"sDesc\":\"" + rs.getString("sDesc") + "\"," + "\"iStk\":\"" + rs.getInt("iStk") + "\"," + "\"iCost\":\"" + rs.getInt("iCost") + "\"," + "\"bUpg\":\"" + rs.getInt("bUpg") + "\"," + "\"bHouse\":\"0\"," + "\"iRty\":" + rs.getInt("iRty") + "," + "\"sName\":\"" + rs.getString("sName") + "\"," + "\"sReqQuests\":\"" + rs.getString("sReqQuests") + "\"");
      }

      rs.close();
    } catch (Exception e) {
      debug("Exception in get item info: " + e.getMessage());
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
        ResultSet rs = Main.sql.doquery("SELECT * FROM wqw_items WHERE userid=" + id + " AND equipped=1 AND sES='" + classShort[i] + "'");
        if (rs.next()) {
          if (i == 0)
            equipPack.addString("\"" + classShort[i] + "\":{\"ItemID\":\"");
          else {
            equipPack.addString(",\"" + classShort[i] + "\":{\"ItemID\":\"");
          }
          int itemid = rs.getInt("itemid");
          equipPack.addInt(itemid);

          if ((id == this.accountid) && (classShort[i].equals("Weapon"))) {
            this.weaponlevel = rs.getInt("iLvl");
          }
          ResultSet es = Main.sql.doquery("SELECT * FROM wqw_equipment WHERE itemid=" + itemid + " LIMIT 1");
          if (es.next()) {
            equipPack.addString("\",\"sType\":");
            equipPack.addString("\"" + es.getString("sType"));
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
      debug("Exception in get equipment: " + e.getMessage());
    }
    return equipPack.getPacket();
  }

  protected void getQuests(String quests)
  {
    try {
      Packet sendPack = new Packet();
      sendPack.addString("{\"t\":\"xt\",\"b\":{\"r\":-1,\"o\":{\"cmd\":\"getQuests\",\"quests\":{");
      String[] packet_handled = quests.split("%");

      for (int i = 1; i < packet_handled.length; i++) {
        ResultSet rs = Main.sql.doquery("SELECT * FROM wqw_quests WHERE id=" + packet_handled[i]);
        debug("Trying to Load Quest ID: " + packet_handled[i] + " to " + this.account);
        if ((!packet_handled[i].equals("undefined")) && (rs.next()))
        {
          if (i != 1) {
            sendPack.addString(",");
          }

          String[] oItems = rs.getString("oItems").split(":");
          String[] oRewards;
          if (!rs.getString("oRewards").equals(""))
            oRewards = rs.getString("oRewards").split(":");
          else {
            oRewards = new String[0];
          }
          String[] turnin = rs.getString("turnin").split(",");

          sendPack.addString("\"" + rs.getString("id") + "\":{\"sFaction\":\"" + rs.getString("sFaction") + "\",\"iLvl\":\"" + rs.getString("iLvl") + "\",\"FactionID\":\"" + rs.getInt("factionID") + "\",\"iClass\":" + rs.getInt("iClass") + ",");
          sendPack.addString("\"iReqRep\":" + rs.getString("iReqRep") + ",\"iValue\":" + rs.getInt("iValue") + ",\"bOnce\":\"0\",\"iGold\":" + rs.getInt("iGold") + ",");
          sendPack.addString("\"iRep\":" + rs.getInt("iRep") + ",");
          sendPack.addString("\"bitSuccess\":\"1\",\"sEndText\":\"" + rs.getString("sEndText") + "\",\"sDesc\":\"" + rs.getString("sDesc") + "\",");
          sendPack.addString("\"QuestID\":\"" + rs.getString("id") + "\",\"bUpg\":\"" + rs.getInt("bUpg") + "\",\"iReqCP\":" + rs.getInt("iReqCP") + ",\"iSlot\":-1,\"iExp\":" + rs.getInt("iExp") + ",\"iWar\":0,\"sName\":\"" + rs.getString("sName") + "\",");

          sendPack.addString("\"oRewards\":{");
          if (!rs.getString("oRewards").equals(""))
          {
            sendPack.addString("\"items" + rs.getString("rewType") + "\":{");
            for (int x = 0; x < oRewards.length; x++) {
              if (x != 0) {
                sendPack.addString(",");
              }
              sendPack.addString("\"" + oRewards[x] + "\":{");
              sendPack.addString(getItemInfo(Integer.parseInt(oRewards[x])));
              sendPack.addString("}");
            }
            sendPack.addString("}");
          }

          sendPack.addString("},");

          sendPack.addString("\"turnin\":[");
          for (int a = 0; a < turnin.length; a++) {
            if (a != 0) {
              sendPack.addString(",");
            }
            String[] droppart = turnin[a].split(":");
            sendPack.addString("{\"ItemID\":\"" + droppart[0] + "\",\"iQty\":\"" + droppart[1] + "\"}");
          }
          sendPack.addString("],");

          sendPack.addString("\"oItems\":");
          sendPack.addString("{");
          for (int e = 0; e < oItems.length; e++) {
            if (e != 0) {
              sendPack.addString(",");
            }
            sendPack.addString("\"" + oItems[e] + "\":{");
            sendPack.addString(getItemInfo(Integer.parseInt(oItems[e])));
            sendPack.addString("}");
          }
          sendPack.addString("}}");
          debug("Quest ID Loaded: " + packet_handled[i] + " to " + this.account);
        }
        rs.close();
      }
      sendPack.addString("}}}}");
      send(sendPack, true);
    }
    catch (Exception e) {
      debug("Exception in get quests: " + e.getMessage());
    }
  }

  protected void getInvent()
  {
    try
    {
      Packet sendPack = new Packet();
      sendPack.addString("{\"t\":\"xt\",\"b\":{\"r\":-1,\"o\":{\"cmd\":\"initInventory\",\"items\":[");
      ResultSet rs = Main.sql.doquery("SELECT * FROM wqw_items WHERE userid=" + this.userid + " AND bBank=0");
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
        if (rs.getString("sES").equals("ar")) {
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
        ResultSet is = Main.sql.doquery("SELECT * FROM wqw_equipment WHERE itemID=" + itemid[e]);
        if (is.next()) {
          sendPack.addString("{\"ItemID\":\"" + is.getInt("itemID") + "\",\"sLink\":\"" + is.getString("sLink") + "\",\"sElmt\":\"" + is.getString("sElmt") + "\",\"bStaff\":\"" + is.getInt("bStaff") + "\",\"iRng\":\"" + is.getInt("iRng") + "\",\"iDPS\":\"" + is.getInt("iDPS") + "\",\"bCoins\":\"" + is.getInt("bCoins") + "\",\"sES\":\"" + is.getString("sES") + "\",\"sType\":\"" + is.getString("sType") + "\",\"iCost\":\"" + is.getInt("iCost") + "\",\"iRty\":\"" + is.getInt("iRty") + "\",");
          if (is.getString("sES").equals("ar"))
            sendPack.addString("\"iQty\":\"" + classxp[e] + "\"");
          else {
            sendPack.addString("\"iQty\":\"" + is.getInt("iQty") + "\"");
          }
          sendPack.addString(",\"iLvl\":\"" + level[e] + "\",\"sIcon\":\"" + is.getString("sIcon") + "\",\"iEnh\":\"" + level[e] + "\",\"bTemp\":\"" + is.getInt("bTemp") + "\",\"CharItemID\":\"" + charitemid[e] + "\",\"iHrs\":\"" + is.getInt("iHrs") + "\",\"sFile\":\"" + is.getString("sFile") + "\",\"iStk\":\"" + is.getInt("iStk") + "\",\"sDesc\":\"" + is.getString("sDesc") + "\",\"bBank\":\"" + 0 + "\",\"bUpg\":\"" + is.getInt("bUpg") + "\",\"bEquip\":\"" + equip[e] + "\",\"sName\":\"" + is.getString("sName") + "\"}");
        }
        is.close();
        e++;
      }
      sendPack.addString("]}}}");
      send(sendPack, true);
      sendPack.clean();
      sendPack.addString("%xt%server%-1%Char Carregado !%");
      send(sendPack, true);
    } catch (Exception e) {
      debug("Exception in get invent: " + e.getMessage());
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
    }
    catch (Exception e) {
    }
    return "";
  }

  protected String getNews()
  {
    try {
      ResultSet rs = Main.sql.doquery("SELECT * FROM wqw_settings LIMIT 1");
      if (rs.next()) {
        return "sNews=" + rs.getString("newsFile") + ",sMap=" + rs.getString("mapFile") + ",sBook=" + rs.getString("bookFile");
      }
      rs.close();
    }
    catch (Exception e) {
    }
    return "";
  }


  protected void joinRoom(String newroom, int roomnumb, String frame, String pad)
  {
    Packet sendPack = new Packet();
    if (roomnumb > 99999) {
      roomnumb = this.generator.nextInt(99999);
    }
    int[] oldroom = this.gameServer.getPlayerRoom(this.account);
    if (!newroom.equals("bludrutbrawl")) {
      this.gameServer.hp[this.accountid] = this.gameServer.calculateHP(this.playerlevel);
      this.gameServer.hpmax[this.accountid] = this.gameServer.calculateHP(this.playerlevel);
      sendUotls(true, true, true, true, false);
      this.onpvp = false; } else {
      if ((newroom.equals("bludrutbrawl")) && (!this.onpvp)) {
        serverMsg("PvP Map 'bludrutbrawl' is only accessible through the pvp interface.", "warning", false, false);
        return;
      }
      int newhp = getHPwithNecklace();
      this.gameServer.hp[this.accountid] = newhp;
      this.gameServer.hpmax[this.accountid] = newhp;
      sendUotls(true, true, true, true, false);
    }
    if ((!this.gameServer.isStaff(this.account)) && (newroom.equals("limbo"))) {
      sendPack.addString("%xt%warning%-1%");
      sendPack.addString("\"limbo\" is a staff only map.");
      sendPack.addString("%");
      send(sendPack, true);
    } else if ((!this.gameServer.isStaff(this.account)) && (newroom.equals("???!!!"))) {
      sendPack.addString("%xt%warning%-1%");
      sendPack.addString("\"???\" is a staff only map.");
      sendPack.addString("%");
      send(sendPack, true);
    }
    else if (this.gameServer.addToRoom(newroom, roomnumb, this.accountid)) {
      this.playerRoom = this.gameServer.room[this.gameServer.getPlayerRoom(this.account)[0]][this.gameServer.getPlayerRoom(this.account)[1]];
      this.playerSlot = this.playerRoom.getPlayerSlot(this.account);
      this.playerRoom.frame[this.playerSlot] = frame;
      this.playerRoom.pad[this.playerSlot] = pad;
      this.playerRoom.tx[this.playerSlot] = 0;
      this.playerRoom.ty[this.playerSlot] = 0;
      sendPack.addXMLSingle(1, new String[] { "msg t", "sys" });
      sendPack.addXMLSingle(1, new String[] { "body action", "userGone", "r", "" + oldroom[0] });
      sendPack.addXMLSingle(0, new String[] { "user id", "" + this.accountid });
      sendPack.addXMLSingle(2, new String[] { "body" });
      sendPack.addXMLSingle(2, new String[] { "msg" });
      this.gameServer.writeOtherMapPacket(oldroom, sendPack, true);
      sendPack.clean();
      sendPack.addString("%xt%exitArea%-1%");
      sendPack.addInt(this.accountid);
      sendPack.addString("%" + this.account + "%");
      this.gameServer.writeOtherMapPacket(oldroom, sendPack, true);
      sendLobby();
    } else {
      sendPack.addString("%xt%warning%-1%\"");
      sendPack.addString(newroom);
      sendPack.addString("\" is not a recognized Map name%");
      send(sendPack, true);
    }
  }
  protected void loadBank()
  {
    try
    {
      Packet sendPack = new Packet();
      sendPack.addString("{\"t\":\"xt\",\"b\":{\"r\":-1,\"o\":{\"cmd\":\"loadBank\",\"items\":[");
      ResultSet rs = Main.sql.doquery("SELECT * FROM wqw_items WHERE userid=" + this.userid + " AND bBank=1");
      int[] charitemid = new int[' '];
      int[] itemid = new int[' '];
      int[] equip = new int[' '];
      int[] level = new int[' '];
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
        ResultSet is = Main.sql.doquery("SELECT * FROM wqw_equipment WHERE itemID=" + itemid[e]);
        if (is.next()) {
          sendPack.addString("{\"sIcon\":\"" + is.getString("sIcon") + "\",\"ItemID\":\"" + is.getInt("itemID") + "\",\"iLvl\":\"" + level[e] + "\",\"iEnh\":\"" + is.getInt("iEnh") + "\",\"sElmt\":\"" + is.getString("sElmt") + "\",\"bTemp\":\"" + is.getInt("bTemp") + "\",\"sLink\":\"" + is.getString("sLink") + "\",\"bStaff\":\"" + is.getInt("bStaff") + "\",\"CharItemID\":\"" + charitemid[e] + "\",\"iRng\":\"" + is.getInt("iRng") + "\",\"bCoins\":\"" + is.getInt("bCoins") + "\",\"iDPS\":\"" + is.getInt("iDPS") + "\",\"sES\":\"" + is.getString("sES") + "\",\"iHrs\":\"" + is.getInt("iHRS") + "\",\"sFile\":\"" + is.getString("sFile") + "\",\"sType\":\"" + is.getString("sType") + "\",\"sDesc\":\"" + is.getString("sDesc") + "\",\"iStk\":\"" + is.getInt("iStk") + "\",\"iCost\":\"" + is.getInt("iCost") + "\",\"bEquip\":\"" + equip[e] + "\",\"bUpg\":\"" + is.getInt("bUpg") + "\",\"iRty\":\"" + is.getInt("iRty") + "\",\"sName\":\"" + is.getString("sName") + "\",\"iQty\":\"" + is.getInt("iQty") + "\"}");
        }
        is.close();
        e++;
      }
      sendPack.addString("]}}}");
      send(sendPack, true);
    } catch (Exception e) {
      debug("Exception in load bank: " + e.getMessage());
    }
  }

  protected void loadBigInventory() {
    Packet sendPack = new Packet();
    sendPack.addString("{\"t\":\"xt\",\"b\":{\"r\":-1,\"o\":{\"cmd\":\"loadInventoryBig\",\"friends\":[");
    try {
      ResultSet rs = Main.sql.doquery("SELECT * FROM wqw_friends WHERE userid=" + this.userid);
      if ((rs.next()) &&
        (!rs.getString("friendid").equals(""))) {
        String[] friendslist = rs.getString("friendid").split(",");
        int i = friendslist.length;
        int e = 0;
        rs.close();
        while (e < i) {
          if (e != 0) {
            sendPack.addString(",");
          }
          ResultSet is = Main.sql.doquery("SELECT * FROM wqw_users WHERE id=" + friendslist[e]);
          if (is.next()) {
            this.friends[e] = is.getString("username");
            sendPack.addString("{\"iLvl\":\"" + is.getInt("level") + "\",\"ID\":\"" + is.getInt("id") + "\",\"sName\":\"" + is.getString("username") + "\",\"sServer\":\"" + is.getString("curServer") + "\"}");
          }
          is.close();
          e++;
        }
      }

      sendPack.addString("],\"items\":[");

      ResultSet rs2 = Main.sql.doquery("SELECT * FROM wqw_items WHERE sES NOT IN('hi','ho') AND userid=" + this.userid + " AND bBank=0");
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
        if (rs2.getString("sES").equals("ar")) {
          classxp[i] = rs2.getInt("classXP");
        }
        i++;
      }
      rs2.close();
      int e = 0;
      while (e < i) {
        ResultSet is = Main.sql.doquery("SELECT * FROM wqw_equipment WHERE itemID=" + itemid[e]);
        if (is.next()) {
          if (e != 0) {
            sendPack.addString(",");
          }
          sendPack.addString("{\"ItemID\":\"" + is.getInt("itemID") + "\",\"sLink\":\"" + is.getString("sLink") + "\",\"sElmt\":\"" + is.getString("sElmt") + "\",\"bStaff\":\"" + is.getInt("bStaff") + "\",\"iRng\":\"" + is.getInt("iRng") + "\",\"iDPS\":\"" + is.getInt("iDPS") + "\",\"bCoins\":\"" + is.getInt("bCoins") + "\",\"sES\":\"" + is.getString("sES") + "\",\"sType\":\"" + is.getString("sType") + "\",\"iCost\":\"" + is.getInt("iCost") + "\",\"iRty\":\"" + is.getInt("iRty") + "\",");
          if (is.getString("sES").equals("ar"))
            sendPack.addString("\"iQty\":\"" + classxp[e] + "\",");
          else {
            sendPack.addString("\"iQty\":\"" + qty[e] + "\",");
          }
          if (is.getString("sES").equals("Weapon")) {
            sendPack.addString("\"EnhDPS\":\"100\",");
          }
          if ((is.getString("sType").equals("Enhancement")) || (is.getString("sType").equals("Necklace")) || (is.getString("sType").equals("Item")) || (is.getString("sType").equals("Quest Item")) || (is.getString("sType").equals("Pet")) || (is.getString("sType").equals("Armor"))) {
            sendPack.addString("\"EnhID\":\"0\",\"PatternID\":\"" + enhid[e] + "\",");
          }

          if ((is.getString("sType").equals("Enhancement")) || (enhid[e] == -1))
            sendPack.addString("\"iLvl\":\"" + is.getInt("iLvl"));
          else {
            sendPack.addString("\"EnhLvl\":\"" + level[e] + "\",\"EnhID\":\"1863\",\"EnhRty\":1,\"EnhPatternID\":\"" + enhid[e]);
          }
          sendPack.addString("\",\"sIcon\":\"" + is.getString("sIcon") + "\",\"bTemp\":\"" + is.getInt("bTemp") + "\",\"CharItemID\":\"" + charitemid[e] + "\",\"iHrs\":\"" + is.getInt("iHrs") + "\",\"sFile\":\"" + is.getString("sFile") + "\",\"iStk\":\"" + is.getInt("iStk") + "\",\"sDesc\":\"" + is.getString("sDesc") + "\",\"bBank\":\"" + 0 + "\",\"bUpg\":\"" + is.getInt("bUpg") + "\",\"bEquip\":\"" + equip[e] + "\",\"sName\":\"" + is.getString("sName") + "\"}");
        }
        is.close();
        e++;
      }

      sendPack.addString("],\"factions\":[],\"hitems\":[");
      ResultSet hs = Main.sql.doquery("SELECT * FROM wqw_items WHERE sES IN('hi','ho') AND userid=" + this.userid + " AND bBank=0");
      int x = 0;
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
        ResultSet is = Main.sql.doquery("SELECT * FROM wqw_equipment WHERE itemid=" + hitemid[z]);
        if (is.next()) {
          if (z != 0) {
            sendPack.addString(",");
          }
          sendPack.addString("{\"ItemID\":\"" + is.getInt("itemID") + "\",\"sLink\":\"" + is.getString("sLink") + "\",\"sElmt\":\"" + is.getString("sElmt") + "\",\"bStaff\":\"" + is.getInt("bStaff") + "\",\"iRng\":\"" + is.getInt("iRng") + "\",\"iDPS\":\"" + is.getInt("iDPS") + "\",\"bCoins\":\"" + is.getInt("bCoins") + "\",\"sES\":\"" + is.getString("sES") + "\",\"sType\":\"" + is.getString("sType") + "\",\"iCost\":\"" + is.getInt("iCost") + "\",\"iRty\":\"" + is.getInt("iRty") + "\",");
          sendPack.addString("\"iQty\":\"1\",");
          sendPack.addString("\"iLvl\":\"" + is.getInt("iLvl"));
          sendPack.addString("\",\"sIcon\":\"" + is.getString("sIcon") + "\",\"bTemp\":\"" + is.getInt("bTemp") + "\",\"CharItemID\":\"" + hcharitemid[z] + "\",\"iHrs\":\"" + is.getInt("iHrs") + "\",\"sFile\":\"" + is.getString("sFile") + "\",\"iStk\":\"" + is.getInt("iStk") + "\",\"sDesc\":\"" + is.getString("sDesc") + "\",\"bBank\":\"" + 0 + "\",\"bUpg\":\"" + is.getInt("bUpg") + "\",\"bEquip\":\"" + hequip[z] + "\",\"sName\":\"" + is.getString("sName") + "\"}");
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
      debug("Exception in load big inventory: " + e.getMessage());
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
      ResultSet rs = Main.sql.doquery("SELECT * FROM wqw_shops WHERE shopid=" + shopid);
      if (rs.next()) {
        sendPack.addString("{\"t\":\"xt\",\"b\":{\"r\":-1,\"o\":{\"shopinfo\":{\"strName\":\"");
        sendPack.addString(rs.getString("strName"));
        sendPack.addString("\",\"bitSuccess\":\"1\",\"items\":[");
        String[] items = rs.getString("items").split(",");
        int house = rs.getInt("bhouse");
        int staff = rs.getInt("bStaff");
        String field = rs.getString("sField");
        rs.close();
        for (int e = 0; e < items.length; e++) {
          if (e != 0) {
            sendPack.addString(",");
          }
          ResultSet is = Main.sql.doquery("SELECT * FROM wqw_equipment WHERE itemID=" + items[e]);
          if (is.next()) {
            if ((is.getString("sType").equals("Enhancement")) || (is.getString("sType").equals("Necklace")) || (is.getString("sType").equals("Item")) || (is.getString("sType").equals("Quest Item")) || (is.getString("sType").equals("Pet")) || (is.getString("sType").equals("Armor")))
              sendPack.addString("{\"EnhID\":\"0\",\"PatternID\":\"" + is.getInt("EnhID") + "\",\"iLvl\":\"" + is.getInt("iLvl") + "\",");
            else {
              sendPack.addString("{\"EnhID\":\"1863\",\"EnhRty\":1,\"EnhPatternID\":\"" + is.getInt("iLvl") + "\",\"EnhLvl\":\"" + is.getInt("iLvl") + "\",");
            }
            if (is.getString("sES").equals("ho")) {
              sendPack.addString("\"bHouse\":\"1\",");
            }
            sendPack.addString("\"iQSvalue\":\"0\",\"sFaction\":\"" + is.getString("sFaction") + "\",\"ItemID\":\"" + is.getInt("itemID") + "\",\"iClass\":\"" + is.getInt("iClass") + "\",\"sElmt\":\"" + is.getString("sElmt") + "\",\"sLink\":\"" + is.getString("sLink") + "\",\"bStaff\":\"" + is.getInt("bStaff") + "\",\"iRng\":\"" + is.getInt("iRng") + "\",\"iDPS\":\"" + is.getInt("iDPS") + "\",\"bCoins\":\"" + is.getInt("bCoins") + "\",\"sES\":\"" + is.getString("sES") + "\",\"sType\":\"" + is.getString("sType") + "\",\"iCost\":\"" + is.getInt("iCost") + "\",\"iRty\":\"" + is.getInt("iRty") + "\",\"iQty\":\"" + is.getInt("iQty") + "\",\"sIcon\":\"" + is.getString("sIcon") + "\",\"FactionID\":\"" + is.getInt("FactionID") + "\",\"bTemp\":\"" + is.getInt("bTemp") + "\",\"iReqRep\":\"" + is.getInt("iReqRep") + "\",\"ShopItemID\":\"" + (shopid + e) + "\",\"sFile\":\"" + is.getString("sFile") + "\",\"iStk\":\"" + is.getInt("iStk") + "\",\"sDesc\":\"" + is.getString("sDesc") + "\",\"bUpg\":\"" + is.getInt("bUpg") + "\",\"bHouse\":\"" + house + "\",\"iReqCP\":\"" + is.getInt("iReqCP") + "\",\"sName\":\"" + is.getString("sName") + "\",\"iQSindex\":\"-1\"}");
          }
          is.close();
        }
        sendPack.addString("],\"ShopID\":\"");
        sendPack.addInt(shopid);
        sendPack.addString("\",\"sField\":\"" + field + "\",\"bStaff\":\"" + staff + "\",\"bHouse\":\"" + house);
        sendPack.addString("\",\"iIndex\":\"-1\"},\"cmd\":\"loadShop\"}}}");
        send(sendPack, true);
      }
    } catch (Exception e) {
      debug("Exception in load shop: " + e.getMessage());
    }
  }

  protected void sendOnlineStatus()
  {
    try {
      Packet sendPack = new Packet();
      sendPack.addString("{\"t\":\"xt\",\"b\":{\"r\":-1,\"o\":{\"cmd\":\"updateFriend\",\"friend\":{\"iLvl\":" + this.gameServer.level[this.accountid] + ",\"ID\":\"" + this.userid + "\",\"sName\":\"" + this.charname + "\",\"sServer\":\"" + Main.serverName + "\"}}}}");
      for (int i = 0; i < this.friends.length; i++)
        if (this.gameServer.getPlayerID(this.friends[i].toLowerCase()) > 0) {
          this.gameServer.writePlayerPacket(this.friends[i].toLowerCase(), sendPack, true);
          sendPack.clean();
          sendPack.addString("%xt%server%-1%" + this.charname + " has logged in.%");
          write(this.charname, "(Is now Online)");
          this.gameServer.writePlayerPacket(this.friends[i].toLowerCase(), sendPack, true);
        }
    }
    catch (Exception e) {
      debug("Error in sending friend online request: " + e.getMessage());
    }
  }

  protected void updateFriends()
  {
    try {
      ResultSet rs = Main.sql.doquery("SELECT * FROM wqw_friends WHERE userid=" + this.userid);
      if ((rs.next()) &&
        (!rs.getString("friendid").equals(""))) {
        String[] friendslist = rs.getString("friendid").split(",");
        rs.close();
        for (int e = 0; e < friendslist.length; e++) {
          ResultSet is = Main.sql.doquery("SELECT * FROM wqw_users WHERE id=" + friendslist[e]);
          if (is.next()) {
            this.friends[e] = is.getString("username");
          }
          is.close();
        }
      }
    }
    catch (Exception e) {
      debug("Error in updating friends array: " + e.getMessage());
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
    if (weptype.equals("Bow"))
      this.anim[0] = "RangedAttack1";
    else if (weptype.equals("Dagger"))
      this.anim[0] = "DuelWield/DaggerAttack1,DuelWield/DaggerAttack2";
    else if (wepname.equals("No Weapon"))
      this.anim[0] = "UnarmedAttack1,UnarmedAttack2,KickAttack,FlipAttack";
    else
      this.anim[0] = "Attack1,Attack2";
  }

  protected void loadSkills(int classid)
  {
    int cp = this.gameServer.getClassPoints(this.userid);
    int rank = this.gameServer.getRankFromCP(cp);
    boolean pot = isPotionsEquipped();

    if (rank == -1) {
      rank = 10;
    }
    this.userrank = rank;
    try {
      Packet sendPack = new Packet();
      ResultSet rs = Main.sql.doquery("SELECT * FROM wqw_classes WHERE classid=" + classid);
      if (rs.next()) {
        sendPack.addString("{\"t\":\"xt\",\"b\":{\"r\":-1,\"o\":{\"cmd\":\"sAct\",\"actions\":{\"passive\":[");
        String[] passives = rs.getString("passives").split(",");
        String[] skills = rs.getString("skills").split(",");

        rs.close();
        for (int x = 0; x < passives.length; x++) {
          if (x != 0) {
            sendPack.addString(",");
          }
          ResultSet es = Main.sql.doquery("SELECT * FROM wqw_passives WHERE id=" + passives[x]);
          if (es.next()) {
            sendPack.addString("{\"icon\":\"" + es.getString("icon") + "\",\"ref\":\"" + es.getString("ref") + "\",\"nam\":\"" + es.getString("name") + "\",\"desc\":\"" + es.getString("desc") + "\",");
            if (rank <= 3)
              sendPack.addString("\"isOK\":false");
            else {
               
              sendPack.addString("\"isOK\":true");
            }
            sendPack.addString(",\"auras\":[{}],\"typ\":\"" + es.getString("type") + "\"}");
          }
          es.close();
        }
        sendPack.addString("],\"active\":[");
        for (int e = 0; e < skills.length; e++) {
          if (e != 0) {
            sendPack.addString(",");
          }
          ResultSet is = Main.sql.doquery("SELECT * FROM wqw_skills WHERE id=" + skills[e]);
          if (is.next()) {
            this.anim[e] = is.getString("anim");
            this.str1[e] = is.getString("str1");
            this.mpcost[e] = is.getInt("mana");
            this.skillfx[e] = is.getString("fx");
            int skid = Integer.parseInt(skills[e]);
           this.skills[e] = skid;
            if (!is.getString("aura").equals(""))
              this.auras[e] = is.getString("aura");
            else {
              this.auras[e] = "";
            }
            if (is.getString("ref").equals("aa")) {
              this.aacd = is.getInt("cd");
            }
            sendPack.addString("{\"icon\":\"" + is.getString("icon") + "\",\"mp\":" + is.getInt("mana") + ",\"nam\":\"" + is.getString("name") + "\",\"anim\":\"" + is.getString("anim") + "\",\"desc\":\"" + is.getString("desc") + "\",");
            if ((rank <= 1) && (e == 2))
              sendPack.addString("\"isOK\":false,");
            else if ((rank <= 2) && (e == 3))
              sendPack.addString("\"isOK\":false,");
            else if ((rank <= 4) && (e == 4))
              sendPack.addString("\"isOK\":false,");
            else {
              sendPack.addString("\"isOK\":true,");
            }
            if (is.getInt("tgtMax") != 0) {
              sendPack.addString("\"tgtMax\":" + is.getInt("tgtMax") + ",\"tgtMin\":" + is.getInt("tgtMin") + ",");
            }
            sendPack.addString("\"range\":" + is.getInt("range") + ",\"fx\":\"m\",\"damage\":1,\"dsrc\":\"" + is.getString("dsrc") + "\",\"ref\":\"" + is.getString("ref") + "\",\"auto\":" + is.getString("auto") + ",\"tgt\":\"" + is.getString("tgt") + "\",\"typ\":\"" + is.getString("typ") + "\",\"strl\":\"" + is.getString("str1") + "\",\"cd\":" + is.getInt("cd") + "}");
          }
          is.close();
        }
        sendPack.addString(",{\"icon\":\"icu1\",\"nam\":\"Potions\",\"anim\":\"Salute\",\"mp\":0,\"desc\":\"Equip a potion or scroll from your inventory to use it here.\",\"isOK\":true,\"range\":808,\"fx\":\"\",\"ref\":\"i1\",\"tgt\":\"f\",\"typ\":\"i\",\"strl\":\"\",\"cd\":5000}");
        sendPack.addString("]}}}}");
        send(sendPack, true);
        initAutoAttack();
        if (pot) {
          sendPotionAnimation();
        }
      }
      updateStats();
    } catch (Exception e) {
      debug("Exception in load skills: " + e.getMessage());
    }
  }

  protected String getWeaponInfo(String column) {
    try {
      ResultSet rs = Main.sql.doquery("SELECT * FROM wqw_items WHERE equipped=1 AND sES='Weapon' AND userid=" + this.userid);
      if (rs.next()) {
        int itemid = rs.getInt("itemid");
        ResultSet es = Main.sql.doquery("SELECT * FROM wqw_equipment WHERE itemID=" + itemid);
        if (es.next())
          return es.getString(column);
      }
    }
    catch (Exception e)
    {
    }
    return "Attack1";
  }

  protected void sendSettings() {
    Packet sendPack = new Packet();
    sendPack.addString("{\"t\":\"xt\",\"b\":{\"r\":-1,\"o\":{\"cmd\":\"cvu\",\"o\":{\"PCstRatio\":7.47,\"PChpDelta\":1640,\"PChpBase1\":360,\"baseHit\":0,\"intSPtoDPS\":10,\"resistRating\":17,\"curveExponent\":0.66,\"baseCritValue\":1.5,\"PChpGoal100\":4000,\"intLevelCap\":100,\"baseMiss\":0.1,\"baseParry\":0.03,\"GstBase\":12,\"modRating\":3,\"baseResistValue\":0.7,\"baseBlockValue\":0.7,\"intHPperEND\":5,\"baseHaste\":0,\"baseBlock\":0,\"statsExponent\":1,\"PChpBase100\":2000,\"intAPtoDPS\":10,\"PCstBase\":15,\"baseCrit\":0.05,\"baseEventValue\":0.05,\"GstGoal\":572,\"PChpGoal1\":400,\"GstRatio\":5.6,\"intLevelMax\":100,\"bigNumberBase\":8,\"PCstGoal\":762,\"baseDodge\":0.04,\"PCDPSMod\":0.85}}}}");
    send(sendPack, true);
  }

  protected void updateStats() {
    Packet sendPack = new Packet();
    sendPack.addString("{\"t\":\"xt\",\"b\":{\"r\":-1,\"o\":{\"tempSta\":{\"ba\":{\"STR\":9,\"INT\":9,\"DEX\":7,\"END\":8},\"Weapon\":{\"STR\":24,\"DEX\":7,\"END\":23},\"innate\":{\"STR\":61,\"INT\":11,\"DEX\":50,\"WIS\":23,\"LCK\":14,\"END\":68},\"ar\":{\"STR\":14,\"DEX\":4,\"END\":13},\"he\":{\"STR\":12,\"INT\":12,\"DEX\":9,\"END\":11}},\"cmd\":\"stu\",\"sta\":{\"$tdo\":0.115,\"$thi\":0.038500000000000006,\"_cmi\":1,\"$smb\":0,\"_tdo\":0.04,\"_cmo\":1,\"_sem\":0.05,\"$WIS\":23,\"$tha\":0.057749999999999996,\"$tpa\":0.03,\"_cdi\":1,\"_sp\":0,\"$cpo\":1.1,\"_chi\":1,\"$cpi\":1,\"_cdo\":1,\"_tbl\":0,\"_tpa\":0.03,\"_cho\":1,\"$LCK\":14,\"$shb\":0,\"$STR\":120,\"$sem\":0.12000000000000001,\"_ap\":0,\"_sbm\":0.7,\"$cmi\":0.92,\"$cai\":0.9,\"$tbl\":0,\"_srm\":0.7,\"_cai\":1,\"$DEX\":77,\"_STR\":61,\"$ap\":240,\"$cao\":1,\"_DEX\":50,\"$sbm\":0.61,\"$cmc\":1,\"$INT\":32,\"_cpi\":1,\"$chi\":1,\"$cho\":1,\"_INT\":11,\"_scm\":1.5,\"_cao\":1,\"_END\":68,\"_WIS\":23,\"_shb\":0,\"_tre\":0.07,\"$cdo\":1,\"$tcr\":0.16999999999999998,\"$END\":123,\"$cdi\":1,\"_cpo\":1,\"$scm\":1.675,\"_tcr\":0.05,\"_tha\":0,\"_thi\":0,\"$srm\":0.7,\"$cmo\":1,\"$sp\":64,\"_LCK\":14,\"_cmc\":1,\"$tre\":0.07,\"_smb\":0},\"wDPS\":69}}}");
    send(sendPack, true);
  }

  protected void moveToCell(String frame, String pad) {
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
    this.gameServer.writeMapPacket(this.account, sendPack, true, true);
    if (this.onpvp)
      debug("Sent moveToCell by ID \"" + frame + "\",\"" + pad + "\" : " + this.account);
  }

  protected void moveToUser(int playerid)
  {
    Room otherroom = this.gameServer.room[this.gameServer.getPlayerRoom(this.gameServer.charName[playerid])[0]][this.gameServer.getPlayerRoom(this.gameServer.charName[playerid])[1]];
    int otherslot = otherroom.getPlayerSlot(this.gameServer.charName[playerid]);
    int newx = otherroom.tx[otherslot];
    int newy = otherroom.ty[otherslot];
    if (otherroom.tx[otherslot] > this.playerRoom.tx[this.playerSlot])
      newx -= 96;
    else {
      newx += 96;
    }
    userMove(newx, newy, 16, true);
  }

  protected void partyAccept(int partyid)
  {
    Packet sendPack = new Packet();
    if (this.gameServer.partyRoom[this.accountid] == 0) {
      this.partyID = this.gameServer.addToParty(this.account, partyid, false);
      sendPack.addString("{\"t\":\"xt\",\"b\":{\"r\":-1,\"o\":{\"cmd\":\"pa\",\"ul\":");
      sendPack.addString(this.gameServer.party[this.partyID].getPlayers());
      sendPack.addString(",\"owner\":\"" + this.gameServer.party[this.partyID].partyOwner + "\",\"pid\":" + this.partyID + "}}}");
      send(sendPack, true);
      sendPack.clean();
      sendPack.addString("{\"t\":\"xt\",\"b\":{\"r\":-1,\"o\":{\"cmd\":\"pa\",\"ul\":[\"");
      sendPack.addString(this.account);
      sendPack.addString("\"],\"owner\":\"" + this.gameServer.party[this.partyID].partyOwner + "\",\"pid\":" + this.partyID + "}}}");
      this.gameServer.writePartyPacket(this.account, sendPack, true, true);
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
    this.gameServer.writePlayerPacket(this.gameServer.party[partyid].partyOwner, sendPack, true);
    sendPack.clean();
  }

  protected void partyKick(String otherchar)
  {
    Packet sendPack = new Packet();
    if (this.gameServer.partyRoom[this.gameServer.getPlayerID(otherchar)] == this.gameServer.partyRoom[this.accountid]) {
      sendPack.addString("{\"t\":\"xt\",\"b\":{\"r\":-1,\"o\":{\"cmd\":\"pr\",\"owner\":\"" + this.gameServer.party[this.partyID].partyOwner + "\",\"pid\":" + this.partyID + ",\"typ\":\"k\",\"unm\":\"" + otherchar + "\"}}}");
      this.gameServer.writePartyPacket(this.account, sendPack, true, false);
      this.gameServer.leaveParty(otherchar);
    } else {
      sendPack.addString("%xt%warning%-1%That player is not in your party.%");
      send(sendPack, true);
    }
  }

  protected void partyInvite(String otherchar)
  {
    Packet sendPack = new Packet();
    if (this.gameServer.partyRoom[this.accountid] == 0) {
      this.partyID = this.gameServer.addToParty(this.account, 0, true);
    }
    sendPack.addString("%xt%server%-1%You have invited " + otherchar + " to join your party.%");
    send(sendPack, true);
    sendPack.clean();
    sendPack.addString("{\"t\":\"xt\",\"b\":{\"r\":-1,\"o\":{\"cmd\":\"pi\",\"owner\":\"");
    sendPack.addString(this.gameServer.party[this.partyID].partyOwner);
    sendPack.addString("\",\"pid\":" + this.partyID + "}}}");
    this.gameServer.writePlayerPacket(otherchar, sendPack, true);
  }

  protected void partyLeave()
  {
    Packet sendPack = new Packet();
    if (this.gameServer.partyRoom[this.accountid] != 0) {
      sendPack.addString("{\"t\":\"xt\",\"b\":{\"r\":-1,\"o\":{\"cmd\":\"pr\",\"owner\":\"");
      sendPack.addString(this.gameServer.party[this.partyID].partyOwner);
      sendPack.addString("\",\"pid\":" + this.partyID + ",\"typ\":\"l\",\"unm\":\"" + this.account + "\"}}}");
      this.gameServer.writePartyPacket(this.account, sendPack, true, false);
      this.gameServer.leaveParty(this.account);
      this.partyID = 0;
    } else {
      sendPack.addString("%xt%warning%-1%You are not in a party.%");
      send(sendPack, true);
    }
  }

  protected void partyPromote(String otherchar)
  {
    Packet sendPack = new Packet();
    if (this.gameServer.partyRoom[this.gameServer.getPlayerID(otherchar)] == this.gameServer.partyRoom[this.accountid]) {
      sendPack.addString("{\"t\":\"xt\",\"b\":{\"r\":-1,\"o\":{\"cmd\":\"pp\",\"owner\":\"" + otherchar + "\"}}}");
      this.gameServer.writePartyPacket(this.account, sendPack, true, false);
      this.gameServer.party[this.gameServer.partyRoom[this.accountid]].partyOwner = otherchar;
    } else {
      sendPack.addString("%xt%warning%-1%That player is not in your party.%");
      send(sendPack, true);
    }
  }

  protected void partySummon(String otherchar) {
    Packet sendPack = new Packet();
    if (this.gameServer.partyRoom[this.gameServer.getPlayerID(otherchar)] == this.gameServer.partyRoom[this.accountid]) {
      sendPack.addString("%xt%server%-1%You attempt to summon " + otherchar + " to you.%");
      send(sendPack, true);
      sendPack.clean();
      sendPack.addString("{\"t\":\"xt\",\"b\":{\"r\":-1,\"o\":{\"cmd\":\"ps\",\"strF\":\"" + this.playerRoom.frame[this.playerSlot] + "\",\"unm\":\"" + this.account + "\",\"strP\":\"" + this.playerRoom.pad[this.playerSlot] + "\"}}}");
      this.gameServer.writePlayerPacket(otherchar, sendPack, true);
    } else {
      sendPack.addString("%xt%warning%-1%That player is not in your party.%");
      send(sendPack, true);
    }
  }

  protected void partySummonAccept()
  {
  }

  protected int getRandomValueFromArray(int[] array)
  {
    int rnd = this.generator.nextInt(array.length);
    return array[rnd];
  }

  protected void partySummonDecline(String otherchar)
  {
    Packet sendPack = new Packet();
    sendPack.addString("%xt%server%-1%You declined the summon.%");
    send(sendPack, true);
    sendPack.clean();
    sendPack.addString("%xt%server%-1%" + this.account + " declined your summon.%");
    this.gameServer.writePlayerPacket(otherchar, sendPack, true);
  }

  protected void dropItem(int itemid) {
    Packet sendPack = new Packet();
    try
    {
      sendPack.clean();
      int success = 0;
      if (itemid != 0) {
        ResultSet rs = Main.sql.doquery("SELECT * FROM wqw_equipment WHERE itemID=" + itemid);
        if (rs.next()) {
          sendPack.addString("{\"t\":\"xt\",\"b\":{\"r\":-1,\"o\":{\"cmd\":\"dropItem\",\"items\":{\"" + itemid + "\":{" + "\"sIcon\":\"" + rs.getString("sIcon") + "\"," + "\"ItemID\":\"" + itemid + "\"," + "\"iLvl\":\"" + rs.getInt("iLvl") + "\"," + "\"sLink\":\"" + rs.getString("sLink") + "\"," + "\"sElmt\":\"" + rs.getString("sElmt") + "\"," + "\"bTemp\":\"" + rs.getInt("bTemp") + "\"," + "\"bStaff\":\"" + rs.getInt("bStaff") + "\"," + "\"iRng\":\"" + rs.getInt("iRng") + "\"," + "\"bCoins\":\"" + rs.getInt("bCoins") + "\"," + "\"iDPS\":\"" + rs.getInt("iDPS") + "\"," + "\"sES\":\"" + rs.getString("sES") + "\"," + "\"bPTR\":\"0\"," + "\"bitSuccess\":\"1\"," + "\"EnhID\":-1," + "\"sType\":\"" + rs.getString("sType") + "\"," + "\"sDesc\":\"" + rs.getString("sDesc") + "\"," + "\"iStk\":\"" + rs.getInt("iStk") + "\"," + "\"iCost\":\"" + rs.getInt("iCost") + "\"," + "\"bUpg\":\"" + rs.getInt("bUpg") + "\"," + "\"bHouse\":\"\"," + "\"iRty\":" + rs.getInt("iRty") + "," + "\"sName\":\"" + rs.getString("sName") + "\"," + "\"iQty\":" + rs.getInt("iQty") + "," + "\"sReqQuests\":\"\"}}}}}");

          success = 1;
        }
        rs.close();
      }
      if (success == 1) {
        send(sendPack, true);
      }
      sendPack.clean();
    } catch (Exception e) {
      debug("Exception in drop item: " + e.getMessage());
    }
  }
protected void playerAttack(String attack, int turn)
   {
    Packet sendPack = new Packet();
     try {
/* 2550 */       int[] monsterid = new int[15];
/* 2551 */       int[] monsterid2 = new int[15];
/* 2552 */       int damage = (int)(10 + this.gameServer.level[this.accountid] * this.gameServer.level[this.accountid] / 8 + this.gameServer.level[this.accountid] * 1.25D);
/* 2553 */       int damage2 = (int)(50 + this.gameServer.level[this.accountid] * this.gameServer.level[this.accountid] / 4 + this.gameServer.level[this.accountid] * 1.25D);
/* 2554 */       int[] crit = new int[15];
/* 2555 */       int[] dodge = new int[15];
/* 2556 */       int[] miss = new int[15];
/* 2557 */       int[] damage3 = new int[15];
/* 2558 */       String[] type = new String[15];
/* 2559 */       String[] skill = new String[15];
       String[] mToAttack;
/* 2561 */       if (attack.indexOf(",") != -1) {
/* 2562 */         mToAttack = attack.split(",");
       } else {
/* 2564 */         mToAttack = new String[1];
/* 2565 */         mToAttack[0] = attack;
       }
/* 2567 */       String[] hit = new String[15];

/* 2569 */       int skillpos = 0;

/* 2571 */       boolean addState = false;

/* 2573 */       Room room = this.playerRoom;
/* 2574 */       int slot = room.getPlayerSlot(this.account);

/* 2576 */       for (int i = 0; i < mToAttack.length; i++) {
/* 2577 */         String[] monsters = mToAttack[i].split(">");
/* 2578 */         skill[i] = monsters[0];
/* 2579 */         String[] tobehit = monsters[1].split(":");
/* 2580 */         monsterid[i] = Integer.parseInt(tobehit[1]);
/* 2581 */         monsterid2[i] = (Integer.parseInt(tobehit[1]) - 1);
/* 2582 */         type[i] = tobehit[0];

/* 2584 */         crit[i] = this.generator.nextInt(15);
/* 2585 */         dodge[i] = this.generator.nextInt(10);
/* 2586 */         miss[i] = this.generator.nextInt(15);
       }

/* 2589 */       if (skill[0].equals("a1"))
/* 2590 */         skillpos = 1;
/* 2591 */       else if (skill[0].equals("a2"))
/* 2592 */         skillpos = 2;
/* 2593 */       else if (skill[0].equals("a3"))
/* 2594 */         skillpos = 3;
/* 2595 */       else if (skill[0].equals("a4")) {
/* 2596 */         skillpos = 4;
       }

/* 2599 */       this.gameServer.mp[this.accountid] -= this.gameServer.skills.getMpCost(this.skills[skillpos]);

/* 2601 */       for (int l = 0; l < mToAttack.length; l++)
       {
/* 2603 */         double adamage = 1.0D;
/* 2604 */         double pdamage = this.gameServer.auras.getDamage(this.passives[0]) + this.gameServer.auras.getDamage(this.passives[1]);
/* 2605 */         double sdamage = this.gameServer.skills.getDamage(this.skills[skillpos]);

/* 2607 */         String atype = "";
/* 2608 */         boolean iscrit = false;
/* 2609 */         for (int xd = 0; xd < 5; xd++) {
/* 2610 */           if (this.gameServer.skills.getAura(this.skills[xd]) != 0) {
/* 2611 */             int auraid = this.gameServer.skills.getAura(this.skills[xd]);
/* 2612 */             atype = this.gameServer.auras.getType(auraid);
/* 2613 */             if (this.gameServer.auras.isAuraActive(this.accountid, auraid)) {
/* 2614 */               adamage = this.gameServer.auras.getDamage(auraid);
/* 2615 */               iscrit = this.gameServer.auras.isCrit(auraid);
             }
           }
         }

/* 2620 */         if ((!atype.equals("passive")) && (type[l].equals("p")) && (this.playerRoom.safe)) {
/* 2621 */           serverMsg("This map is a safezone, you are not allowed to PvP here.", "server", false, false);
/* 2622 */           return;
         }
/* 2624 */         damage3[l] = (int)(damage + this.generator.nextInt(damage2 - damage) * (adamage + sdamage + pdamage));

/* 2626 */         hit[l] = "hit";
/* 2627 */         if (damage3[l] < 0) {
/* 2628 */           damage3[l] = 0;
         }
/*      */
/* 2631 */         if ((crit[l] > 14) || (iscrit)) {
/* 2632 */           hit[l] = "crit";
/* 2633 */           damage3[l] *= 2;
/* 2634 */         } else if (dodge[l] > 9) {
/* 2635 */           hit[l] = "dodge";
/* 2636 */           damage3[l] = 0;
/* 2637 */         } else if (miss[l] > 11) {
/* 2638 */           hit[l] = "miss";
/* 2639 */           damage3[l] = 0;
/*      */         }
/*      */
/* 2642 */         if (!skill[l].equals("i1")) {
/* 2643 */           this.fighting = true;
/*      */         }
/* 2645 */         if ((this.playerRoom.monsterState[monsterid2[l]] == 1) && (type[l].equals("m"))) {
/* 2646 */           this.monfighting = mToAttack.length;
/* 2647 */           playerHitTimer(monsterid[l]);
/* 2648 */         } else if ((this.playerRoom.monsterState[monsterid2[l]] == 0) && (type[l].equals("m"))) {
/* 2649 */           this.fighting = false;
         }

/* 2652 */         if ((this.gameServer.hp[this.accountid] >= 1) && (type[l].equals("m")) && (!skill[l].equals("i1"))) {
/* 2653 */           if (this.playerRoom.monsterHP[monsterid2[l]] >= this.playerRoom.monsterHP[monsterid2[l]]) {
/* 2654 */             addState = true;
           }

/* 2657 */           this.playerRoom.monsterHP[monsterid2[l]] -= damage3[l];
/* 2658 */           if ((this.playerRoom.monsterHP[monsterid2[l]] <= 0) && (this.playerRoom.monsterState[monsterid2[l]] >= 1) && (type[l].equals("m"))) {
/* 2659 */             this.playerRoom.monsterHP[monsterid2[l]] = 0;
/* 2660 */             this.playerRoom.monsterMP[monsterid2[l]] = 0;
/* 2661 */             this.monfighting -= 1;
/* 2662 */             this.fighting = false;

/* 2664 */             this.playerRoom.respawnMonster(monsterid2[l], this.playerRoom.monsterType[monsterid2[l]]);
/* 2665 */             Main.sql.doupdate("UPDATE wqw_users SET monkill=monkill+1 WHERE username='" + this.account + "'");
/* 2666 */             this.monkilled += 1;
/* 2667 */             addState = true;
           }
         } else {
/* 2669 */           if ((!type[l].equals("p")) || (this.gameServer.hp[this.accountid] < 1) || (skill[l].equals("i1")) ||
/* 2670 */             (this.gameServer.isAlive[monsterid[l]] != true) || (!type[l].equals("p")) || (monsterid[l] == this.accountid)) continue;
/* 2671 */           if (this.gameServer.hp[monsterid[l]] >= this.gameServer.hpmax[monsterid[l]]) {
/* 2672 */             addState = true;
           }
/* 2674 */           this.gameServer.hp[monsterid[l]] -= damage3[l];
/* 2675 */           if (this.gameServer.hp[monsterid[l]] <= 0) {
/* 2676 */             this.gameServer.hp[monsterid[l]] = 0;
/* 2677 */             this.gameServer.mp[monsterid[l]] = 0;
/* 2678 */             this.gameServer.state[monsterid[l]] = 0;
/* 2679 */             this.monfighting = 0;
/* 2680 */             this.fighting = false;
/* 2681 */             addPvPTeamScore(this.gameServer.pvpteam[this.accountid], this.gameServer.level[monsterid[l]] * 2);
/* 2682 */             Main.sql.doupdate("UPDATE wqw_users SET pvpkill=pvpkill+1 WHERE username='" + this.account + "'");
/* 2683 */             this.gameServer.isAlive[monsterid[l]] = false;
/* 2684 */             addState = true;
           }
         }

       }

/* 2690 */       if (skill[0].equals("i1")) {
/* 2691 */         int hdamage = damage + this.generator.nextInt(damage2 - damage);
/* 2692 */         if (this.gameServer.hp[monsterid[0]] < this.gameServer.hpmax[monsterid[0]])
/* 2693 */           this.gameServer.hp[monsterid[0]] += hdamage;
         else {
/* 2695 */           this.gameServer.hp[monsterid[0]] = this.gameServer.hpmax[monsterid[0]];
         }
                sendPack.addString("{\"t\":\"xt\",\"b\":{\"r\":-1,\"o\":{\"cmd\":\"ct\",\"anims\":[{\"strFrame\":\"" + room.frame[slot] + "\",\"cInf\":\"p:" + this.accountid + "\",\"fx\":\"w\",\"animStr\":\"Salute\",\"tInf\":\"" + type[0] + ":" + monsterid[0] + "\",\"strl\":\"sp_eh1\"}],\"a\":[{\"cInf\":\"p:" + this.accountid + "\",\"cmd\":\"aura+\",\"auras\":[{\"nam\":\"Heal\",\"t\":\"s\",\"dur\":5,\"isNew\":true}],\"tInf\":\"" + type[0] + ":" + monsterid[0] + "\"}],\"p\":{\"" + this.gameServer.charName[monsterid[0]] + "\":{\"intHP\":" + this.gameServer.hp[monsterid[0]] + "}},\"sarsa\":[{\"cInf\":\"p:" + this.accountid + "\",\"a\":[{\"hp\":-" + hdamage + ",\"tInf\":\"" + type[0] + ":" + monsterid[0] + "\",\"type\":\"hit\"}],\"actID\":" + turn + ",\"iRes\":1}]}}}");

                   this.gameServer.writeMapPacket(this.account, sendPack, true, false);
/* 2703 */         int[] pot = { 1749 };
/* 2704 */         int[] qty = { 1 };
/* 2705 */         turnInItem(pot, qty);
       } else {
/* 2707 */         sendPack.addString(skillPacket(skillpos, addState, mToAttack, room.frame[slot], type, hit, damage3, monsterid, turn));
/* 2708 */         this.gameServer.writeMapPacket(this.account, sendPack, true, false);
                }
/* 2710 */       if (this.gameServer.level[this.accountid] != 100) {
/* 2711 */         levelUp();
       }

/* 2714 */       if (this.gameServer.state[this.accountid] == 1)
/* 2715 */         autoRestTimer();
     }
     catch (Exception l) {
/* 2718 */       debug("Erro em player Attack: " + l.getMessage() + "causado por:" + l.getCause());
     }
   }
protected void autoRestTimer() {
/* 2907 */     Timer timer = new Timer();
/* 2908 */     timer.schedule(new TimerTask()
     {
       public void run() {
/* 2911 */         if ((ServerConnection.this.gameServer.state[ServerConnection.this.accountid] == 1) && ((ServerConnection.this.gameServer.hp[ServerConnection.this.accountid] < ServerConnection.this.gameServer.hpmax[ServerConnection.this.accountid]) || (ServerConnection.this.gameServer.mp[ServerConnection.this.accountid] < ServerConnection.this.gameServer.mpmax[ServerConnection.this.accountid])))
/* 2912 */           ServerConnection.this.autoRest();
       }
     }
     , 2000L);
   }
protected void autoRest() {
    Packet sendPack = new Packet();
/* 3035 */     if ((this.gameServer.state[this.accountid] == 1) && ((this.gameServer.hp[this.accountid] != this.gameServer.hpmax[this.accountid]) || (this.gameServer.mp[this.accountid] != this.gameServer.mpmax[this.accountid]))) {
/* 3036 */       String returnThis = "";
/* 3037 */       int newhp = getHPwithNecklace();
/* 3038 */       int rand = this.generator.nextInt(50);
/* 3039 */       while (rand < 20) {
/* 3040 */         rand = this.generator.nextInt(50);
       }
/* 3042 */       if (this.gameServer.pvpOn[this.accountid]) {
/* 3043 */         this.gameServer.hpmax[this.accountid] = newhp;
       }
/* 3045 */       this.gameServer.hp[this.accountid] += this.gameServer.hpmax[this.accountid] / rand;
/* 3046 */       if (this.gameServer.hp[this.accountid] > this.gameServer.hpmax[this.accountid]) {
/* 3047 */         this.gameServer.hp[this.accountid] = this.gameServer.hpmax[this.accountid];
       }
/* 3049 */       this.gameServer.mp[this.accountid] += this.gameServer.mpmax[this.accountid] / rand;
/* 3050 */       if (this.gameServer.mp[this.accountid] > this.gameServer.mpmax[this.accountid]) {
/* 3051 */         this.gameServer.mp[this.accountid] = this.gameServer.mpmax[this.accountid];
       }
/* 3053 */       returnThis = returnThis + "{\"t\":\"xt\",\"b\":{\"r\":-1,\"o\":{\"cmd\":\"ct\",";
/* 3054 */       returnThis = returnThis + "\"p\":{";
/* 3055 */       returnThis = returnThis + "\"" + this.account + "\":{\"intMP\":" + this.gameServer.mp[this.accountid] + ",\"intHP\":" + this.gameServer.hp[this.accountid];
/* 3056 */       returnThis = returnThis + "}}}}}";
/* 3057 */       sendPack.addString(returnThis);
/* 3058 */       this.gameServer.writeMapPacket(this.account, sendPack, true, false);
/* 3059 */       autoRestTimer();
     }
   }
 protected void playerHit(int monsterid)
   {
     Packet sendPack = new Packet();
/* 2937 */     int monsterid2 = monsterid - 1;

/* 2939 */     double areduce = 0.0D;
/* 2940 */     double preduce = this.gameServer.auras.getReduction(this.passives[0]) * this.gameServer.auras.getReduction(this.passives[1]);
/* 2941 */     for (int i = 0; i < 5; i++) {
/* 2942 */       if (this.gameServer.skills.getAura(this.skills[i]) != 0) {
/* 2943 */         int auraid = this.gameServer.skills.getAura(this.skills[i]);
/* 2944 */         if (this.gameServer.auras.isAuraActive(this.accountid, auraid)) {
/* 2945 */           areduce = this.gameServer.auras.getReduction(auraid);
         }
       }
     }

/* 2950 */     int damage = (int)(10 + this.playerRoom.monsterLevel[monsterid2] * this.playerRoom.monsterLevel[monsterid2] / 8 + this.playerRoom.monsterLevel[monsterid2] * 1.25D);
/* 2951 */     int damage2 = (int)(50 + this.playerRoom.monsterLevel[monsterid2] * this.playerRoom.monsterLevel[monsterid2] / 4 + this.playerRoom.monsterLevel[monsterid2] * 1.25D);
/* 2952 */     int damage3 = damage + this.generator.nextInt(damage2 - damage);
/* 2953 */     damage3 = (int)(damage3 - damage3 * (areduce + preduce));
/* 2954 */     if (damage3 < 0) {
/* 2955 */       damage3 = 0;
     }
/* 2957 */     int crit = this.generator.nextInt(20);
/* 2958 */     int dodge = this.generator.nextInt(20);
/* 2959 */     String hit = "hit";
/* 2960 */     if (crit > 15) {
/* 2961 */       hit = "crit";
/* 2962 */       damage3 *= 2;
/* 2963 */     } else if (dodge > 18) {
/* 2964 */       hit = "dodge";
/* 2965 */       damage3 = 0;
     }
/* 2967 */     Room room = this.playerRoom;
/* 2968 */     int slot = this.playerRoom.getPlayerSlot(this.account);
/* 2969 */     String frame = this.playerRoom.frame[slot];
/* 2970 */     this.gameServer.hp[this.accountid] -= damage3;
/* 2971 */     if (this.gameServer.hp[this.accountid] <= 0) {
/* 2972 */       this.fighting = false;
/* 2973 */       this.playerRoom.monsterState[monsterid2] = 1;
/* 2974 */       this.gameServer.hp[this.accountid] = 0;
/* 2975 */       this.gameServer.state[this.accountid] = 0;
/* 2976 */       sendPack.addString("{\"t\":\"xt\",\"b\":{\"r\":-1,\"o\":{\"cmd\":\"ct\",\"anims\":[{\"strFrame\":\"" + room.monsterFrame[monsterid2] + "\",\"cInf\":\"m:" + monsterid + "\",\"fx\":\"m\",\"animStr\":\"Attack1,Attack2\",\"tInf\":\"p:" + this.accountid + "\"}],\"m\":{\"" + monsterid + "\":{\"intState\":1}},\"p\":{\"" + this.account + "\":{\"intHP\":0,\"intState\":0}},\"sara\":[{\"actionResult\":{\"hp\":" + damage3 + ",\"cInf\":\"m:" + monsterid + "\",\"tInf\":\"p:" + this.accountid + "\",\"type\":\"" + hit + "\"},\"iRes\":1}]}}}");
/* 2977 */       this.gameServer.writeMapPacket(this.account, sendPack, true, false);
/* 2978 */     } else if ((this.playerRoom.monsterState[monsterid2] != 0) && (this.gameServer.hp[this.accountid] > 0) && (this.playerRoom.monsterFrame[monsterid2].equals(frame))) {
/* 2979 */       this.playerRoom.monsterState[monsterid2] = 2;
/* 2980 */       this.gameServer.state[this.accountid] = 2;
/* 2981 */       sendPack.addString("{\"t\":\"xt\",\"b\":{\"r\":-1,\"o\":{\"cmd\":\"ct\",\"anims\":[{\"strFrame\":\"" + room.monsterFrame[monsterid2] + "\",\"cInf\":\"m:" + monsterid + "\",\"fx\":\"m\",\"animStr\":\"Attack1,Attack2\",\"tInf\":\"p:" + this.accountid + "\"}],\"p\":{\"" + this.account + "\":{\"intHP\":" + this.gameServer.hp[this.accountid] + "}},\"sara\":[{\"actionResult\":{\"hp\":" + damage3 + ",\"cInf\":\"m:" + monsterid + "\",\"tInf\":\"p:" + this.accountid + "\",\"type\":\"" + hit + "\"},\"iRes\":1}]}}}");
/* 2982 */       this.gameServer.writeMapPacket(this.account, sendPack, true, false);
     }
   }

  protected String skillPacket(int i, boolean addState, String[] monsters, String frame, String[] type, String[] hit, int[] damage3, int[] monsterid, int turn)
  {
    Packet sendPack = new Packet();

    int[] monsterid2 = new int[5];
    boolean addMonsters = false;
    for (int x = 0; x < monsters.length; x++) {
      monsterid[x] -= 1;
    }
    String returnThis = "{\"t\":\"xt\",\"b\":{\"r\":-1,\"o\":{\"cmd\":\"ct\",\"anims\":[{\"strFrame\":\"" + frame + "\",\"cInf\":\"p:" + this.accountid + "\",\"fx\":\"" + this.skillfx[i] + "\",\"animStr\":\"" + this.anim[i] + "\",\"tInf\":\"";

    for (int z = 0; z < monsters.length; z++) {
      if (z != 0) {
        returnThis = returnThis + ",";
      }
      returnThis = returnThis + type[z] + ":" + monsterid[z];
    }
    returnThis = returnThis + "\",\"strl\":\"" + this.str1[i] + "\"}],";

    if (!this.auras[i].equals("")) {
      String[] omg = this.auras[i].split(",");
      int time = Integer.parseInt(omg[1]) * 1000;
      String omgx = "none";
      addAura(omg[0], time);
      returnThis = returnThis + "\"a\":[{\"cInf\":\"p:" + this.accountid + "\",\"cmd\":\"aura+\",\"auras\":[{\"nam\":\"" + omg[0] + "\",\"t\":\"s\"";
      if (this.skillfx[i].equals("m")) {
        returnThis = returnThis + ",\"s\":\"s\",\"cat\":\"stun\"";
      }
      returnThis = returnThis + ",\"dur\":" + omg[1] + ",\"isNew\":true}],";
      returnThis = returnThis + "\"tInf\":\"" + type[0] + ":" + monsterid[0] + "\"}],\"p\":{\"" + this.account + "\":{\"intMP\":" + this.gameServer.mp[this.accountid] + "}},\"sarsa\":[{\"cInf\":\"p:" + this.accountid + "\",\"a\":[{\"hp\":0,\"tInf\":\"p:" + this.accountid + "\",\"type\":\"" + omgx + "\"}],\"actID\":" + turn + ",\"iRes\":1}]}}}";
    } else {
      returnThis = returnThis + "\"sarsa\":[{\"cInf\":\"p:" + this.accountid + "\",\"a\":[";

      for (int a = 0; a < monsters.length; a++) {
        if (a != 0) {
          returnThis = returnThis + ",";
        }
        if (type[a].equals("m"))
        {
          addMonsters = true;
        }
        returnThis = returnThis + "{\"hp\":" + damage3[a] + ",\"tInf\":\"" + type[a] + ":" + monsterid[a] + "\",\"type\":\"" + hit[a] + "\"}";
      }
      returnThis = returnThis + "],\"actID\":" + turn + ",\"iRes\":1}],";

      returnThis = returnThis + "\"p\":{";
      returnThis = returnThis + "\"" + this.account + "\":{\"intMP\":" + this.gameServer.mp[this.accountid];
      if ((addState) && (this.fighting))
        returnThis = returnThis + ",\"intState\":2";
      else if (addState) {
        returnThis = returnThis + ",\"intState\":1";
      }
      returnThis = returnThis + "}";
      for (int b = 0; b < monsters.length; b++) {
        if (type[b].equals("p")) {
          returnThis = returnThis + ",\"" + this.gameServer.charName[monsterid[b]] + "\":{\"intHP\":" + this.gameServer.hp[monsterid[b]];
          if (this.gameServer.hp[monsterid[b]] <= 0)
          {
            returnThis = returnThis + ",\"intMP\":0,\"intState\":0";
            sendPack.clean();
            sendPack.addString(addMonsterRewards(0, type[b], monsterid[b]));
            send(sendPack, true);
          } else if ((addState) && (this.fighting))
          {
            returnThis = returnThis + ",\"intState\":2";
          }
          returnThis = returnThis + "}";
        }
      }
      returnThis = returnThis + "}";

      if (addMonsters) {
        returnThis = returnThis + ",\"m\":{";
        for (int b = 0; b < monsters.length; b++) {
          if (b != 0) {
            returnThis = returnThis + ",";
          }
          returnThis = returnThis + "\"" + monsterid[b] + "\":{\"intHP\":";
          if (this.playerRoom.monsterHP[monsterid2[b]] <= 0)
            returnThis = returnThis + "0";
          else {
            returnThis = returnThis + this.playerRoom.monsterHP[monsterid2[b]];
          }
          if ((this.playerRoom.monsterHP[monsterid2[b]] <= 0) && (this.playerRoom.monsterState[monsterid2[b]] != 0)) {
            this.playerRoom.monsterState[monsterid2[b]] = 0;

            returnThis = returnThis + ",\"intMP\":0,\"intState\":0";
            sendPack.clean();
            sendPack.addString(addMonsterRewards(this.playerRoom.monsterType[monsterid2[b]], type[b], monsterid[b]));
            send(sendPack, true);
            int isdropped = this.generator.nextInt(95);
            if (this.droppercent[monsterid2[b]] > isdropped) {
              dropItem(Integer.parseInt(this.drops[monsterid2[b]]));
            }

            if (this.onpvp) {
              addPvPTeamScore(this.gameServer.pvpteam[this.accountid], 25);
            }
          }
          returnThis = returnThis + "}";
        }
      }
      returnThis = returnThis + "}}}}";
      if (this.gameServer.level[this.accountid] != 100) {
        levelUp();
      }
    }
    return returnThis;
  }

  protected void addAura(final String aura, int time)
  {
    Packet sendPack = new Packet();
    this.gameServer.writeMapPacket(this.account, sendPack, true, false);
    Timer timer = new Timer();
    timer.schedule(new TimerTask()
    {
      public void run()
      {
        ServerConnection.this.removeAura(aura);
      }
    }
    , time);
  }

  protected void removeAura(String name)
  {
    Packet sendPack = new Packet();
    sendPack.addString("{\"t\":\"xt\",\"b\":{\"r\":-1,\"o\":{\"cmd\":\"ct\",\"a\":[{\"cmd\":\"aura-\",\"aura\":{\"nam\":\"" + name + "\"},\"tInf\":\"p:" + this.accountid + "\"}]}}}");
    this.gameServer.writeMapPacket(this.account, sendPack, true, false);
  }

  protected void overKill(int monsterid)
  {
    Packet sendPack = new Packet();

    Room room = this.playerRoom;
    int slot = room.getPlayerSlot(this.gameServer.charName[monsterid]);
    this.gameServer.hp[monsterid] = 0;
    int[] mid = { monsterid };
    int[] damage3 = { 999999999 };
    String[] hit = { "crit" };
    String[] type = { "p" };
    String[] mToAttack = new String[1];
    sendPack.addString(skillPacket(0, false, mToAttack, room.frame[slot], type, hit, damage3, mid, 0));
    this.gameServer.writeMapPacket(this.account, sendPack, true, false);
  }
  protected void playerHitTimer(final int monsterid)
   {
/* 2919 */     Timer timer = new Timer();
/* 2920 */     final int monsterid2 = monsterid - 1;
    int rand = this.generator.nextInt(500);
    int time = 1000 + rand;
/* 2923 */     int slot = this.playerRoom.getPlayerSlot(this.account);
/* 2924 */     final String frame = this.playerRoom.frame[slot];
/* 2925 */     timer.schedule(new TimerTask()
     {
       public void run() {
/* 2928 */         if ((ServerConnection.this.playerRoom.monsterState[monsterid2] != 0) && (ServerConnection.this.gameServer.hp[ServerConnection.this.accountid] > 0) && (ServerConnection.this.playerRoom.monsterFrame[monsterid2].equals(frame))) {
/* 2929 */           ServerConnection.this.playerHit(monsterid);
/* 2930 */           ServerConnection.this.playerHitTimer(monsterid);
         }
       }
     }
     , time);
   }

  

  protected void playerTimerAttack()
  {
    Packet sendPack = new Packet();
    sendPack.addString("%xt%hi%-1%");
    send(sendPack, true);
    sendPack.clean();
  }

  protected void respawnPlayer()
  {
    Packet sendPack = new Packet();
    if (this.onpvp)
      sendPack.addString("%xt%resTimed%-1%Enter" + this.gameServer.pvpteam[this.accountid] + "%Spawn%");
    else {
      sendPack.addString("%xt%resTimed%-1%Enter%Spawn%");
    }
    send(sendPack, true);
    this.monfighting = 0;
    this.gameServer.hp[this.accountid] = this.gameServer.hpmax[this.accountid];
    this.gameServer.mp[this.accountid] = this.gameServer.mpmax[this.accountid];
    this.gameServer.isAlive[this.accountid] = true;
    sendPack.clean();
    sendPack.addString("{\"t\":\"xt\",\"b\":{\"r\":-1,\"o\":{\"cmd\":\"uotls\",\"o\":{\"intHP\":" + this.gameServer.hpmax[this.accountid] + ",\"intMP\":" + this.gameServer.mpmax[this.accountid] + ",\"intState\":1},\"unm\":\"" + this.account + "\"}}}");
    this.gameServer.writeMapPacket(this.account, sendPack, true, false);
    Main.sql.doupdate("UPDATE wqw_users SET killed=killed+1 WHERE username='" + this.account + "'");
  }

  protected void restPlayer()
  {
    int newhp = getHPwithNecklace();
    if (this.onpvp) {
      this.gameServer.hpmax[this.accountid] = newhp;
    }
    this.gameServer.hp[this.accountid] += this.gameServer.hpmax[this.accountid] / 20;
    if (this.gameServer.hp[this.accountid] > this.gameServer.hpmax[this.accountid]) {
      this.gameServer.hp[this.accountid] = this.gameServer.hpmax[this.accountid];
    }
    this.gameServer.mp[this.accountid] += this.gameServer.mpmax[this.accountid] / 20;
    if (this.gameServer.mp[this.accountid] > this.gameServer.mpmax[this.accountid]) {
      this.gameServer.mp[this.accountid] = this.gameServer.mpmax[this.accountid];
    }
    sendUotls(true, false, true, false, false);
  }

  protected void requestFriend(String otherchar)
  {
    Packet sendPack = new Packet();
    sendPack.addString("%xt%server%-1%You have requested " + otherchar + " to be friends.%");
    send(sendPack, true);
    sendPack.clean();
    sendPack.addString("{\"t\":\"xt\",\"b\":{\"r\":-1,\"o\":{\"cmd\":\"requestFriend\",\"unm\":\"" + this.account + "\"}}}");
    this.gameServer.writePlayerPacket(otherchar, sendPack, true);
  }

  boolean isInteger(String input) {
    try {
      Integer.parseInt(input);
      return true;
    } catch (NumberFormatException nfe) {
    }
    return false;
  }
    protected void retrieveUserDatas(String Packet)
   {
     try
     {
/* 3124 */       String[] packet_handled = Packet.split("%");
/* 3125 */       Packet sendPack = new Packet();
/* 3126 */       sendPack.addString("{\"t\":\"xt\",\"b\":{\"r\":-1,\"o\":{\"cmd\":\"initUserDatas\",\"a\":[");
/* 3127 */       for (int i = 2; i < packet_handled.length; i++) {
/* 3128 */         if (isInteger(packet_handled[i])) {
/* 3129 */           if (i != 2) {
/* 3130 */             sendPack.addString(",");
           }

/* 3133 */           sendPack.addString("{");
/* 3134 */           int pID = Integer.parseInt(packet_handled[i]);
/* 3135 */           int id = this.gameServer.userID[pID];

/* 3137 */           debug("Retrieving user data for uID: " + id + ", pID: " + pID + ", Character: " + this.gameServer.charName[pID]);
/*      */
/* 3139 */           int classPoints = this.gameServer.getClassPoints(id);
/* 3140 */           String className = getClassName(id);
/* 3141 */           String equipment = getEquipment(id);
/* 3142 */           Room room = this.playerRoom;
/* 3143 */           ResultSet rs = Main.sql.doquery("SELECT * FROM wqw_users WHERE id=" + id + " LIMIT 1");
/*      */
/* 3145 */           if (rs.next()) {
/* 3146 */             String userName = rs.getString("username").toLowerCase();
/* 3147 */             int playerslot = room.getPlayerSlot(userName);
/* 3148 */             int playerLevel = rs.getInt("level");
/* 3149 */             if (userName.equals(this.account)) {
/* 3150 */               this.playerlevel = playerLevel;
/*      */             }
/* 3152 */             sendPack.addString("\"uid\":");
/* 3153 */             sendPack.addInt(this.gameServer.getPlayerID(userName));
/* 3154 */             sendPack.addString(",\"strFrame\":");
/* 3155 */             sendPack.addString("\"" + room.frame[playerslot] + "\"");
/* 3156 */             sendPack.addString(",\"strPad\":");
/* 3157 */             sendPack.addString("\"" + room.pad[playerslot] + "\"");
/* 3158 */             sendPack.addString(",\"data\":{\"intColorAccessory\":\"");
/* 3159 */             sendPack.addInt(rs.getInt("cosColorAccessory"));
/* 3160 */             sendPack.addString("\",\"iCP\":");
/* 3161 */             sendPack.addInt(classPoints);
/* 3162 */             sendPack.addString(",\"intLevel\":\"");
/* 3163 */             sendPack.addInt(playerLevel);
/* 3164 */             sendPack.addString("\",\"iBagSlots\":");
/* 3165 */             sendPack.addInt(rs.getInt("slotBag"));
/* 3166 */             sendPack.addString(",\"ig0\":0,\"iUpgDays\":\"-");
/* 3167 */             sendPack.addInt(rs.getInt("upgDays"));
/* 3168 */             sendPack.addString("\",\"intColorBase\":\"");
/* 3169 */             sendPack.addInt(rs.getInt("cosColorBase"));
/* 3170 */             sendPack.addString("\",\"sCountry\":\"US\"");
/* 3171 */             sendPack.addString(",\"iSTR\":\"");
/* 3172 */             sendPack.addInt(rs.getInt("str"));
/* 3173 */             sendPack.addString("\",\"ip0\":0,\"iq0\":0,\"iAge\":\"");
/* 3174 */             sendPack.addInt(rs.getInt("age"));
/* 3175 */             sendPack.addString("\",\"iWIS\":\"");
/* 3176 */             sendPack.addInt(rs.getInt("WIS"));
/* 3177 */             sendPack.addString("\",\"intExpToLevel\":\"");
/* 3178 */             sendPack.addInt(getXpToLevel(playerLevel));
/* 3179 */             sendPack.addString("\",\"intGold\":");
/* 3180 */             sendPack.addInt(rs.getInt("gold"));
/* 3181 */             sendPack.addString(",\"intMP\":");
/* 3182 */             sendPack.addInt(this.gameServer.calculateMP(playerLevel));
/* 3183 */             sendPack.addString(",\"sHouseInfo\":[]");
/* 3184 */             sendPack.addString(",\"iBankSlots\":");
/* 3185 */             sendPack.addInt(rs.getInt("slotBank"));
/* 3186 */             sendPack.addString(",\"iHouseSlots\":");
/* 3187 */             sendPack.addInt(rs.getInt("slotHouse"));
/* 3188 */             sendPack.addString(",\"id0\":0,\"intColorSkin\":\"");
/* 3189 */             sendPack.addInt(rs.getInt("plaColorSkin"));
/* 3190 */             sendPack.addString("\",\"intMPMax\":");
/* 3191 */             sendPack.addInt(this.gameServer.calculateMP(playerLevel));
/* 3192 */             sendPack.addString(",\"intHPMax\":");
/* 3193 */             sendPack.addInt(this.gameServer.calculateHP(playerLevel));
/* 3194 */             sendPack.addString(",\"dUpgExp\":\"");
/* 3195 */             sendPack.addString("2012-01-20T17:53:00");
/* 3196 */             sendPack.addString("\",\"iUpg\":\"");
/* 3197 */             sendPack.addInt(rs.getInt("upgrade"));
/* 3198 */             sendPack.addString("\",\"CharID\":\"");
/* 3199 */             sendPack.addInt(id);
/* 3200 */             sendPack.addString("\",\"strEmail\":\"none\"");
/* 3201 */             sendPack.addString(",\"iINT\":\"");
/* 3202 */             sendPack.addInt(rs.getInt("INT"));
/* 3203 */             sendPack.addString("\",\"intColorTrim\":\"");
/* 3204 */             sendPack.addInt(rs.getInt("cosColorTrim"));
/* 3205 */             sendPack.addString("\",\"lastArea\":\"");
/* 3206 */             sendPack.addString(rs.getString("lastVisited"));
/* 3207 */             sendPack.addString("\",\"iFounder\":\"1\"");
/* 3208 */             sendPack.addString(",\"intDBExp\":");
/* 3209 */             sendPack.addInt(rs.getInt("xp"));
/* 3210 */             sendPack.addString(",\"intExp\":");
/* 3211 */             sendPack.addInt(rs.getInt("xp"));
/* 3212 */             sendPack.addString(",\"UserID\":\"");
/* 3213 */             sendPack.addInt(id);
/* 3214 */             sendPack.addString("\",\"ia1\":\"0\",\"ia0\":0,\"intHP\":");
/* 3215 */             sendPack.addInt(this.gameServer.calculateHP(playerLevel));
/* 3216 */             sendPack.addString(",\"dCreated\":\"0000-00-00T00:00:00\"");
/* 3217 */             sendPack.addString(",\"strQuests\":\"ZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZ\",\"bitSuccess\":\"1\",\"strHairName\":\"");
/* 3218 */             sendPack.addString(rs.getString("hairName"));
/* 3219 */             sendPack.addString("\",\"intColorEye\":\"");
/* 3220 */             sendPack.addInt(rs.getInt("plaColorEyes"));
/* 3221 */             sendPack.addString("\",\"iLCK\":\"");
/* 3222 */             sendPack.addInt(rs.getInt("LCK"));
/* 3223 */             sendPack.addString("\",\"eqp\":{");
/* 3224 */             sendPack.addString(equipment);
/* 3225 */             sendPack.addString("},\"iDBCP\":");
/* 3226 */             sendPack.addInt(classPoints);
/* 3227 */             sendPack.addString(",\"intDBGold\":");
/* 3228 */             sendPack.addInt(rs.getInt("gold"));
/* 3229 */             sendPack.addString(",\"strClassName\":\"");
/* 3230 */             sendPack.addString(className);
/* 3231 */             sendPack.addString("\",\"intActivationFlag\":\"");
/* 3232 */             sendPack.addInt(rs.getInt("emailActive"));
/* 3233 */             sendPack.addString("\",\"intAccessLevel\":\"");
/* 3234 */             sendPack.addInt(rs.getInt("access"));
/* 3235 */             sendPack.addString("\",\"strHairFilename\":\"");
/* 3236 */             sendPack.addString(rs.getString("hairFile"));
/* 3237 */             sendPack.addString("\",\"intColorHair\":\"");
/* 3238 */             sendPack.addInt(rs.getInt("plaColorHair"));
/* 3239 */             sendPack.addString("\",\"HairID\":\"");
/* 3240 */             sendPack.addInt(rs.getInt("hairID"));
/* 3241 */             sendPack.addString("\",\"strGender\":\"");
/* 3242 */             sendPack.addString(rs.getString("gender"));
/* 3243 */             sendPack.addString("\",\"strUsername\":\"");
/* 3244 */             sendPack.addString(userName);
/* 3245 */             sendPack.addString("\",\"iDEX\":\"");
/* 3246 */             sendPack.addInt(rs.getInt("DEX"));
/* 3247 */             sendPack.addString("\",\"intCoins\":");
/* 3248 */             sendPack.addInt(rs.getInt("coins"));
/* 3249 */             sendPack.addString(",\"iEND\":\"");
/* 3250 */             sendPack.addInt(rs.getInt("END"));
/* 3251 */             sendPack.addString("\",\"strMapName\":\"");
/* 3252 */             sendPack.addString(room.roomName + "\"");
/*      */           }
/* 3254 */           sendPack.addString("}}");
/* 3255 */           rs.close();
/*      */         }
/*      */       }
/* 3258 */       sendPack.addString("]}}}");
/* 3259 */       send(sendPack, true);
/* 3260 */       sendPack.clean();
/*      */     }
/*      */     catch (Exception e)
/*      */     {
       debug("Exception in retrieve user datas: " + e.getMessage() + ", uid: " + Packet);
     }
   }

protected void retrieveUserData(int id2, boolean doAgain) {
/* 3269 */     int uid = id2;
/*      */     try {
/* 3271 */       int id = this.gameServer.userID[id2];
/* 3272 */       if (id > 0) {
/* 3273 */         debug("Attempting to retrieve user data: " + id + ", " + this.gameServer.charName[id2]);
/* 3274 */         int cp = this.gameServer.getClassPoints(id);
/* 3275 */         String cn = getClassName(id);
/* 3276 */         String equip = getEquipment(id);
/* 3277 */         Packet sendPack = new Packet();
/* 3278 */         Room room = this.playerRoom;
/* 3279 */         ResultSet rs = Main.sql.doquery("SELECT * FROM wqw_users WHERE id=" + id);
/* 3280 */         if (rs.next()) {
/* 3281 */           String user = rs.getString("username");
/* 3282 */           user = user.toLowerCase();
/* 3283 */           int slot = room.getPlayerSlot(user);
/* 3284 */           int level = rs.getInt("level");
/* 3285 */           if (id == this.userid) {
/* 3286 */             this.playerlevel = level;
/*      */           }
/*      */
/* 3289 */           sendPack.addString("{\"t\":\"xt\",\"b\":{\"r\":-1,\"o\":{\"uid\":");
/* 3290 */           sendPack.addInt(this.gameServer.getPlayerID(user));
/* 3291 */           sendPack.addString(",\"strFrame\":\"");
/* 3292 */           sendPack.addString(room.frame[slot]);
/* 3293 */           sendPack.addString("\",\"cmd\":\"initUserData\",\"strPad\":\"");
/* 3294 */           sendPack.addString(room.pad[slot]);
/* 3295 */           sendPack.addString("\",\"data\":{\"intColorAccessory\":\"");
/* 3296 */           sendPack.addInt(rs.getInt("cosColorAccessory"));
/* 3297 */           sendPack.addString("\",\"iCP\":");
/* 3298 */           sendPack.addInt(cp);
/* 3299 */           sendPack.addString(",\"intLevel\":\"");
/* 3300 */           sendPack.addInt(level);
/* 3301 */           sendPack.addString("\",\"iBagSlots\":");
/* 3302 */           sendPack.addInt(rs.getInt("slotBag"));
/* 3303 */           sendPack.addString(",\"ig0\":0,\"iUpgDays\":\"-");
/* 3304 */           sendPack.addInt(rs.getInt("upgDays"));
/* 3305 */           sendPack.addString("\",\"intColorBase\":\"");
/* 3306 */           sendPack.addInt(rs.getInt("cosColorBase"));
/* 3307 */           sendPack.addString("\",\"iSTR\":\"");
/* 3308 */           sendPack.addInt(rs.getInt("str"));
/* 3309 */           sendPack.addString("\",\"ip0\":0,\"iq0\":0,\"iAge\":\"");
/* 3310 */           sendPack.addInt(rs.getInt("age"));
/* 3311 */           sendPack.addString("\",\"iWIS\":\"");
/* 3312 */           sendPack.addInt(rs.getInt("WIS"));
/* 3313 */           sendPack.addString("\",\"intExpToLevel\":\"");
/* 3314 */           sendPack.addInt(getXpToLevel(level));
/* 3315 */           sendPack.addString("\",\"intGold\":");
/* 3316 */           sendPack.addInt(rs.getInt("gold"));
/* 3317 */           sendPack.addString(",\"intMP\":");
/* 3318 */           sendPack.addInt(this.gameServer.calculateMP(level));
/* 3319 */           sendPack.addString(",\"iBankSlots\":");
/* 3320 */           sendPack.addInt(rs.getInt("slotBank"));
/* 3321 */           sendPack.addString(",\"iHouseSlots\":");
/* 3322 */           sendPack.addInt(rs.getInt("slotHouse"));
/* 3323 */           sendPack.addString(",\"id0\":0,\"intColorSkin\":\"");
/* 3324 */           sendPack.addInt(rs.getInt("plaColorSkin"));
/* 3325 */           sendPack.addString("\",\"intMPMax\":");
/* 3326 */           sendPack.addInt(this.gameServer.calculateMP(level));
/* 3327 */           sendPack.addString(",\"intHPMax\":");
/* 3328 */           sendPack.addInt(this.gameServer.calculateHP(level));
/* 3329 */           sendPack.addString(",\"dUpgExp\":\"");
/* 3330 */           sendPack.addString("2009-01-20T17:53:00");
/* 3331 */           sendPack.addString("\",\"iUpg\":\"");
/* 3332 */           sendPack.addInt(rs.getInt("upgrade"));
/* 3333 */           sendPack.addString("\",\"CharID\":\"");
/* 3334 */           sendPack.addInt(id);
/* 3335 */           sendPack.addString("\",\"strClassName\":\"");
/* 3336 */           sendPack.addString(cn);
/* 3337 */           sendPack.addString("\",\"iINT\":\"");
/* 3338 */           sendPack.addInt(rs.getInt("INT"));
/* 3339 */           sendPack.addString("\",\"ItemID\":\"");
/* 3340 */           sendPack.addInt(rs.getInt("currentClass"));
/* 3341 */           sendPack.addString("\",\"lastArea\":\"");
/* 3342 */           sendPack.addString(rs.getString("lastVisited"));
/* 3343 */           sendPack.addString("\",\"intColorTrim\":\"");
/* 3344 */           sendPack.addInt(rs.getInt("cosColorTrim"));
/* 3345 */           sendPack.addString("\",\"intDBExp\":");
/* 3346 */           sendPack.addInt(rs.getInt("xp"));
/* 3347 */           sendPack.addString(",\"intExp\":");
/* 3348 */           sendPack.addInt(rs.getInt("xp"));
/* 3349 */           sendPack.addString(",\"UserID\":\"");
/* 3350 */           sendPack.addInt(id);
/* 3351 */           sendPack.addString("\",\"ia1\":\"0\",\"ia0\":0,\"intHP\":");
/* 3352 */           sendPack.addInt(this.gameServer.calculateHP(level));
/* 3353 */           sendPack.addString(",\"strQuests\":\"ZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZ\",\"bitSuccess\":\"1\",\"strHairName\":\"");
/* 3354 */           sendPack.addString(rs.getString("hairName"));
/* 3355 */           sendPack.addString("\",\"intColorEye\":\"");
/* 3356 */           sendPack.addInt(rs.getInt("plaColorEyes"));
/* 3357 */           sendPack.addString("\",\"iLCK\":\"");
/* 3358 */           sendPack.addInt(rs.getInt("LCK"));
/* 3359 */           sendPack.addString("\",\"eqp\":{");
/* 3360 */           sendPack.addString(equip);
/* 3361 */           sendPack.addString("},\"iDBCP\":");
/* 3362 */           sendPack.addInt(cp);
/* 3363 */           sendPack.addString(",\"intDBGold\":");
/* 3364 */           sendPack.addInt(rs.getInt("LCK"));
/* 3365 */           sendPack.addString(",\"intActivationFlag\":\"");
/* 3366 */           sendPack.addInt(rs.getInt("emailActive"));
/* 3367 */           sendPack.addString("\",\"intAccessLevel\":\"");
/* 3368 */           sendPack.addInt(rs.getInt("access"));
/* 3369 */           sendPack.addString("\",\"strHairFilename\":\"");
/* 3370 */           sendPack.addString(rs.getString("hairFile"));
/* 3371 */           sendPack.addString("\",\"intColorHair\":\"");
/* 3372 */           sendPack.addInt(rs.getInt("plaColorHair"));
/* 3373 */           sendPack.addString("\",\"HairID\":\"");
/* 3374 */           sendPack.addInt(rs.getInt("hairID"));
/* 3375 */           sendPack.addString("\",\"strGender\":\"");
/* 3376 */           sendPack.addString(rs.getString("gender"));
/* 3377 */           sendPack.addString("\",\"strUsername\":\"");
/* 3378 */           sendPack.addString(user);
/* 3379 */           sendPack.addString("\",\"iDEX\":\"");
/* 3380 */           sendPack.addInt(rs.getInt("DEX"));
/* 3381 */           sendPack.addString("\",\"intCoins\":");
/* 3382 */           sendPack.addInt(rs.getInt("coins"));
/* 3383 */           sendPack.addString(",\"iEND\":\"");
/* 3384 */           sendPack.addInt(rs.getInt("END"));
/* 3385 */           sendPack.addString("\",\"strMapName\":\"");
/* 3386 */           sendPack.addString(room.roomName);
/* 3387 */           sendPack.addString("\"}}}}");
/* 3388 */           send(sendPack, true);
/* 3389 */           sendPack.clean();
/* 3390 */           rs.close();
/*      */         }
/*      */       }
/*      */     } catch (Exception e) {
/* 3394 */       debug("Exception in retrieve user data: " + e.getMessage() + ", uid: " + uid);
/*      */       try {
/* 3396 */         Thread.sleep(200L);
/*      */       } catch (Exception e2) {
/* 3398 */         debug("retrieveUserData sleep failed: " + e2.getMessage());
/*      */       }
/* 3400 */       if (doAgain) {
/* 3401 */         retrieveUserData(uid, false);
/*      */       }
/* 3403 */       else if (this.gameServer.userID[id2] == this.userid) {
/* 3404 */         Packet sendPack = new Packet();
/* 3405 */         sendPack.addString("%xt%logoutWarning%-1%%15%");
/* 3406 */         send(sendPack, true);
/* 3407 */         finalize();
/*      */       }
/*      */     }
   }

  protected void sellItem(int itemid, int adjustid)
  {
    try
    {
      int sellprice = 0;
      int iscoins = 0;
      int qty = 1;
      String isitem = "";
      Packet sendPack = new Packet();
      ResultSet es = Main.sql.doquery("SELECT * FROM wqw_equipment WHERE itemid=" + itemid);
      if (es.next()) {
        sellprice = es.getInt("iCost") / 4;
        iscoins = es.getInt("bCoins");
        isitem = es.getString("sType");
        qty = es.getInt("iQty");
      }
      es.close();
      ResultSet rs = Main.sql.doquery("SELECT * FROM wqw_items WHERE userid=" + this.userid + " AND id=" + adjustid + " AND equipped=0 AND itemid=" + itemid);
      if (rs.next())
      {
        if (iscoins != 1)
          Main.sql.doupdate("UPDATE wqw_users SET gold=gold+" + sellprice + " WHERE id=" + this.userid);
        else {
          Main.sql.doupdate("UPDATE wqw_users SET coins=coins+" + sellprice + " WHERE id=" + this.userid);
        }
        if ((isitem.equals("Item")) || (isitem.equals("Quest Item"))) {
          Main.sql.doupdate("UPDATE wqw_items SET iQty=iQty-1 WHERE itemid=" + itemid + " AND userid=" + this.userid);
          if (qty == 1)
            Main.sql.doupdate("DELETE FROM wqw_items WHERE id=" + adjustid);
        }
        else {
          Main.sql.doupdate("DELETE FROM wqw_items WHERE id=" + adjustid);
        }
        sendPack.addString("{\"t\":\"xt\",\"b\":{\"r\":-1,\"o\":{\"cmd\":\"sellItem\",\"intAmount\":" + sellprice + ",\"CharItemID\":" + adjustid + ",\"bCoins\":\"" + iscoins + "\"}}}");
      } else {
        sendPack.addString("{\"t\":\"xt\",\"b\":{\"r\":-1,\"o\":{\"cmd\":\"sellItem\",\"bitSuccess\":0,\"strMessage\":\"Item Does Not Exist\",\"CharItemID\":-1}}}");
      }
      rs.close();
      send(sendPack, true);
    } catch (Exception e) {
      debug("Exception in sell item: " + e.getMessage());
    }
  }

  protected void sendUotls(boolean addhp, boolean addhpmax, boolean addmp, boolean addmpmax, boolean addlevel) {
    Packet Pack = new Packet();
    Pack.addString("{\"t\":\"xt\",\"b\":{\"r\":-1,\"o\":{\"cmd\":\"uotls\",\"o\":{");
    for (int i = 1; i < 5; i++) {
      if ((i == 1) && (addhp)) {
        if (i != 1) Pack.addString(",");
        Pack.addString("\"intHP\":" + this.gameServer.hp[this.accountid]);
      } else if ((i == 2) && (addhpmax)) {
        if (i != 1) Pack.addString(",");
        Pack.addString("\"intHPMax\":" + this.gameServer.hpmax[this.accountid]);
      } else if ((i == 3) && (addmpmax)) {
        if (i != 1) Pack.addString(",");
        Pack.addString("\"intMPMax\":" + this.gameServer.mpmax[this.accountid]);
      } else if ((i == 4) && (addmp)) {
        if (i != 1) Pack.addString(",");
        Pack.addString("\"intMP\":" + this.gameServer.mp[this.accountid]);
      } else if ((i == 5) && (addlevel)) {
        if (i != 1) Pack.addString(",");
        Pack.addString("\"intLevel\":" + this.gameServer.level[this.accountid]);
      }
    }
    Pack.addString("},\"unm\":\"" + this.account + "\"}}}");
    this.gameServer.writeMapPacket(this.account, Pack, true, false);
  }

  protected void sendArea(boolean doAgain)
  {
    try {
      Main.sql.doupdate("UPDATE wqw_users SET lastVisited='" + this.playerRoom.roomName + "-" + this.playerRoom.roomNumb + "' WHERE id=" + this.userid);
      ResultSet is = Main.sql.doquery("SELECT * FROM wqw_maps WHERE id=" + this.playerRoom.roomType);
      if (is.next()) {
        String[] mons = is.getString("monsterid").split(",");
        String[] monnumbs = is.getString("monsternumb").split(",");
        String[] monframe = is.getString("monsterframe").split(",");
        String extra = is.getString("sExtra");
        Boolean anymonsters = Boolean.valueOf(true);

        if (is.getString("monsternumb").equals("")) {
          anymonsters = Boolean.valueOf(false);
        }

        Packet sendPack = new Packet();
        sendPack.addString("{\"t\":\"xt\",\"b\":{\"r\":-1,\"o\":{\"cmd\":\"moveToArea\",\"areaName\":\"");
        sendPack.addString(this.playerRoom.roomName + "-" + this.gameServer.getPlayerRoom(this.account)[1]);
        sendPack.addString("\",\"intKillCount\":0,\"uoBranch\":[");
        for (int i = 0; i < 10; i++) {
          if (!this.playerRoom.roomSlot[i].equals("")) {
            int playerI = this.gameServer.getPlayerID(this.playerRoom.roomSlot[i]);
            if ((playerI > 0) && (!this.playerRoom.roomSlot[i].equals(""))) {
              if (i != 0) {
                sendPack.addString(",");
              }
              sendPack.addString(this.playerRoom.getPlayerInfo(i));
            }
          }
        }
        sendPack.addString("],\"strMapFileName\":\"");
        sendPack.addString(this.playerRoom.fileName + "\"");
        if (extra.equals("bPvP")) {
          sendPack.addString(",\"PVPFactions\":[{\"id\":8,\"sName\":\"Legends\"},{\"id\":7,\"sName\":\"Overlords\"}],\"pvpTeam\":" + this.gameServer.pvpteam[this.accountid] + ",\"pvpScore\":[{\"v\":" + this.gameServer.lscore + ",\"r\":0,\"m\":0,\"k\":0},{\"v\":" + this.gameServer.vscore + ",\"r\":0,\"m\":0,\"k\":0}]");
        }

        sendPack.addString(",\"mondef\":[");

        if (anymonsters.booleanValue() == true)
        {
          this.drops = new String[monnumbs.length];
          this.droppercent = new int[monnumbs.length];
          for (int x = 0; x < monnumbs.length; x++) {
            ResultSet xs = Main.sql.doquery("SELECT * FROM wqw_monsters WHERE MonID=" + monnumbs[x]);
            if (xs.next()) {
              String[] mondrops = xs.getString("strDrops").split(",");
              for (int a = 0; a < mondrops.length; a++) {
                String[] droppart = mondrops[a].split(":");
                this.drops[x] = droppart[0];
                this.droppercent[x] = (int)(Double.parseDouble(droppart[1]) * 100.0D);
                debug("Drop Item ID: " + this.drops[x] + " Loaded! Percentage: " + this.droppercent[x] + "%");
              }
            }
          }

          for (int e = 0; e < mons.length; e++) {
            if (e != 0) {
              sendPack.addString(",");
            }
            is.close();
            ResultSet rs = Main.sql.doquery("SELECT * FROM wqw_monsters WHERE MonID=" + mons[e]);
            if (rs.next()) {
              sendPack.addString("{\"sRace\":\"" + rs.getString("sRace") + "\",\"MonID\":\"" + rs.getInt("MonID") + "\",\"intMP\":\"" + rs.getInt("intMPMax") + "\",\"intLevel\":\"" + rs.getInt("intLevel") + "\",\"intMPMax\":" + rs.getInt("intMPMax") + ",\"intHP\":\"" + rs.getInt("intHPMax") + "\",\"strBehave\":\"walk\",\"intHPMax\":\"" + rs.getInt("intHPMax") + "\",\"strElement\":\"" + rs.getString("strElement") + "\",\"strLinkage\":\"" + rs.getString("strLinkage") + "\"");
              sendPack.addString(",\"strMonFileName\":\"" + rs.getString("strMonFileName") + "\",\"strMonName\":\"" + rs.getString("strMonName") + "\"}");
            }
            rs.close();
          }
        }

        sendPack.addString("],\"intType\":\"1\",\"monBranch\":[");
        if (anymonsters.booleanValue() == true) {
          sendPack.addString(this.playerRoom.getMon(monnumbs));
        }
        sendPack.addString("],\"wB\":[],\"sExtra\":\"" + extra + "\"");
        sendPack.addString(",\"monmap\":[");

        if (anymonsters.booleanValue() == true) {
          for (int u = 0; u < monnumbs.length; u++) {
            if (u != 0) {
              sendPack.addString(",");
            }
            this.playerRoom.monsterFrame[u] = monframe[u];
            sendPack.addString("{\"MonMapID\":\"" + (u + 1) + "\",\"strFrame\":\"" + monframe[u] + "\",\"intRSS\":\"-1\",\"MonID\":\"" + monnumbs[u] + "\",\"bRed\":\"0\"}");
          }
        }

        sendPack.addString("],\"areaId\":");
        sendPack.addString("" + this.gameServer.getPlayerRoom(this.account)[0]);
        sendPack.addString(",\"strMapName\":\"");
        sendPack.addString(this.playerRoom.roomName);
        sendPack.addString("\"}}}");
        send(sendPack, true);
        sendPack.clean();
        sendPack.addString("%xt%server%-1%Movido para \"" + this.playerRoom.roomName + "-" + this.gameServer.getPlayerRoom(this.account)[1] + "\"!%");
        send(sendPack, true);
        sendPack.clean();
        sendPack.addXMLSingle(1, new String[] { "msg t", "sys" });
        sendPack.addXMLSingle(1, new String[] { "body action", "uER", "r", "" + this.gameServer.getPlayerRoom(this.account)[0] * this.gameServer.getPlayerRoom(this.account)[1] });
        sendPack.addXMLSingle(1, new String[] { "u i", "" + this.gameServer.getPlayerID(this.account), "m", "" + this.gameServer.getModerator(this.gameServer.getPlayerID(this.account)), "s", "0", "p", "" + (this.gameServer.room[this.gameServer.getPlayerRoom(this.account)[0]][this.gameServer.getPlayerRoom(this.account)[1]].getPlayerSlot(this.account) + 1) });
        sendPack.addXML("n", "", 1);
        sendPack.addCDATA(this.account);
        sendPack.addXML("n", "", 2);
        sendPack.addXML("vars", "", 0);
        sendPack.addXMLSingle(2, new String[] { "u" });
        sendPack.addXMLSingle(2, new String[] { "body" });
        sendPack.addXMLSingle(2, new String[] { "msg" });
        this.gameServer.writeMapPacket(this.account, sendPack, true, true);
        this.gameServer.sendPlayerDetails(this.account);
      }
    } catch (Exception e) {
      debug("Exception in send area: " + e.getMessage());
      if (doAgain) {
        sendArea(false);
      } else {
        Packet sendPack = new Packet();
        sendPack.addString("%xt%logoutWarning%-1%%15%");
        send(sendPack, true);
        finalize();
      }
    }
  }

    protected void sendLobby()
    {
        Packet sendPack = new Packet();
     sendPack.addXMLSingle(1, new String[] { "msg t", "sys" });
     sendPack.addXMLSingle(1, new String[] { "body action", "joinOK", "r", "" + this.gameServer.getPlayerRoom(this.account)[0] });
     sendPack.addXMLSingle(0, new String[] { "pid id", "" + (this.playerRoom.getPlayerSlot(this.account) + 1) });
     sendPack.addXMLSingle(0, new String[] { "vars" });
     sendPack.addXMLSingle(1, new String[] { "uLs r", "" + this.gameServer.getPlayerRoom(this.account)[0] * this.gameServer.getPlayerRoom(this.account)[1] });

        for (int e = 0; e < 10; e++) {
            if (!this.playerRoom.roomSlot[e].equals("")) {
                int playerI = gameServer.getPlayerID(this.playerRoom.roomSlot[e]);
                if (playerI > 0 && !this.playerRoom.roomSlot[e].equals("")) {
                    int mod = gameServer.getModerator(playerI);
                    sendPack.addXMLSingle(1, new String[] { "u i", "" + playerI, "m", "" + mod, "s", "0", "p", "" + (e + 1) });
                    sendPack.addXML("n","",1);
                    sendPack.addCDATA(gameServer.getCharname(playerI));
                    sendPack.addXML("n","",2);
                    sendPack.addXML("vars","",0);
                    sendPack.addXMLSingle(2, new String[] { "u" });
                }
            }
        }
        sendPack.addXMLSingle(2, new String[] { "uLs" });
        sendPack.addXMLSingle(2, new String[] { "body" });
        sendPack.addXMLSingle(2, new String[] { "msg" });
        send(sendPack, true);
        sendPack.clean();
        debug("Sent lobby: "+this.account);
        sendArea(true);
    }

    protected void sendPolicy()
    {
     Packet sendPack = new Packet();
     sendPack.addXML("cross-domain-policy", "", 1);
     sendPack.addXMLSingle(0, new String[] { "allow-access-from domain", "*", "to-ports", "" + Main.port });
     sendPack.addXML("cross-domain-policy", "", 2);
     send(sendPack, true);
     debug("Sent policy to: " + this.ip);
        this.finalize();
    }

    protected void sendVersion()
    {
     Packet sendPack = new Packet();
     sendPack.addXMLSingle(1, new String[] { "msg t", "sys" });
     sendPack.addXMLSingle(1, new String[] { "body action", "apiOK" });
     sendPack.addXMLSingle(2, new String[] { "body" });
     sendPack.addXMLSingle(2, new String[] { "msg" });
     send(sendPack, true);
        debug("Sent version to: "+this.ip);
    }

  protected void setAFK(boolean afk)
  {
    Packet sendPack = new Packet();
    sendPack.addString("%xt%uotls%-1%");
    sendPack.addString(this.account);
    sendPack.addString("%afk:");
    sendPack.addString(afk + "%");
    this.gameServer.writeMapPacket(this.account, sendPack, true, false);
    sendPack.clean();
    if (afk != this.playerRoom.afk[this.playerSlot]) {
      this.playerRoom.afk[this.playerSlot] = afk;
      if (afk)
        sendPack.addString("%xt%server%-1%You are now Away From Keyboard (AFK).%");
      else {
        sendPack.addString("%xt%server%-1%You are no longer Away From Keyboard (AFK).%");
      }
      send(sendPack, true);
      sendPack.clean();
    }
    write(this.account, "AFK Set: " + afk);
  }

  protected void unequipItem(int itemid, int adjustid)
  {
    try {
      Packet sendPack = new Packet();
      sendPack.addString("{\"t\":\"xt\",\"b\":{\"r\":-1,\"o\":{\"uid\":");
      sendPack.addInt(this.accountid);
      ResultSet rs = Main.sql.doquery("SELECT * FROM wqw_equipment WHERE itemID=" + itemid);
      if (rs.next()) {
        sendPack.addString(",\"ItemID\":\"" + itemid + "\",\"strES\":\"" + rs.getString("sES") + "\",\"cmd\":\"unequipItem\"}}}");
      }
      rs.close();
      Main.sql.doupdate("UPDATE wqw_items SET equipped=0 WHERE userid=" + this.userid + " AND itemid=" + itemid + " AND equipped=1");
      this.gameServer.writeMapPacket(this.account, sendPack, true, false);
    } catch (Exception e) {
      debug("Exception in equip item: " + e.getMessage());
    }
  }

  protected void userChat(int room, String message, String zone)
  {
    Packet sendPack = new Packet();
    sendPack.addString("%xt%chatm%" + room + "%");

    sendPack.addString(zone + "~" + message);
    if (zone.equals("server"))
      sendPack.addString("%" + this.account);
    else {
      sendPack.addString("%" + this.account);
    }
    if (zone.equals("event"))
      sendPack.addString("%em%" + this.account);
    else {
      sendPack.addString("%" + this.account);
    }
    sendPack.addString(zone + "~" + message);
    sendPack.addString("%" + this.userid);
    sendPack.addString("%" + room + "%");
    if (zone.equals("party")) {
      if (this.gameServer.partyRoom[this.accountid] > 0) {
        this.gameServer.writePartyPacket(this.account, sendPack, true, false);
      } else {
        sendPack.clean();
        sendPack.addString("%xt%warning%-1%You are not in a party.%");
        send(sendPack, true);
      }
    } else if (zone.equals("warning"))
      this.gameServer.writeGlobalPacket(this.account, sendPack, true, false);
    else if (zone.equals("server"))
      this.gameServer.writeGlobalPacket(this.account, sendPack, true, false);
    else if (zone.equals("trade"))
      this.gameServer.writeMapPacket(this.account, sendPack, true, false);
    else {
      this.gameServer.writeMapPacket(this.account, sendPack, true, false);
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
    while ((cmdBytes[i] != 32) && (i < cmd.length() - 1)) {
      i++;
    }
    if (i == cmd.length() - 1)
      command = cmd.substring(0, cmd.length()).toLowerCase();
    else {
      command = cmd.substring(0, i).toLowerCase();
    }

    if ((this.gameServer.getModerator(this.gameServer.getPlayerID(this.account)) != 0) || (this.gameServer.getVIP(this.gameServer.getPlayerID(this.account)) != 0) || (this.gameServer.getAdmin(this.gameServer.getPlayerID(this.account)) != 0))
    {
      if ((this.gameServer.getModerator(this.gameServer.getPlayerID(this.account)) == 1) && (startMod == true)) {
        if (command.equals("ban")) {
          debug("Banning: " + cmd.substring(i + 1));
          this.gameServer.banPlayer(cmd.substring(i + 1), this.account);
        } else if (command.equals("cp")) {
          int cp = Integer.parseInt(cmd.substring(i + 1));
          sendPack.addString(addRewards(0, 0, cp, 0, "m", 0));
          send(sendPack, true);
        } else if (command.equals("heal")) {
          this.gameServer.hp[this.accountid] = this.gameServer.hpmax[this.accountid];
          this.gameServer.mp[this.accountid] = this.gameServer.mpmax[this.accountid];
          sendUotls(true, false, true, false, false);
        } else if (command.equals("ver")) {
          serverMsg("!Ban/Unban (user) - Bans/Unbans (user).", "server", false, false);
          serverMsg("!CP/Gold/XP (i) - add CP/Gold/XP.", "server", false, false);
          serverMsg("!Heal - Heal HP e MP.", "server", false, false);
          serverMsg("!Kick (user) - Kicks (user).", "server", false, false);
          serverMsg("!post (MSG) - Posta uma MSG para todos os players", "server", false, false);
          serverMsg("!Shop (Shop id) - Load shop.", "server", false, false);
          serverMsg("!Level (amount) Levelup%", "server", false, false);
          serverMsg("!Mod (MSG) - Mod MSG.", "server", false, false);

        } else if (command.equals("gold")) {
          int gold = Integer.parseInt(cmd.substring(i + 1));
          sendPack.addString(addRewards(0, gold, 0, 0, "m", 0));
          send(sendPack, true);
        } else if (command.equals("kick")) {
          this.gameServer.kickPlayer(cmd.substring(i + 1), this.account);
        } else if (command.equals("mod")) {
          sendPack.addString("%xt%moderator%-1%(" + this.account + "): " + cmd.substring(i + 1) + "%");
          this.gameServer.writeGlobalPacket(this.account, sendPack, true, false);
        } else if (command.equals("me")) {
          sendPack.addString("%xt%em%-1%" + this.account + ": " + cmd.substring(i + 1) + "%%");
          this.gameServer.writeMapPacket(this.account, sendPack, true, false);
        } else if (command.equals("em")) {
          sendPack.addString("%xt%em%-1%" + this.account + ": " + cmd.substring(i + 1) + "%%");
          this.gameServer.writeMapPacket(this.account, sendPack, true, false);
        } else if (command.equals("xp")) {
          int xp = Integer.parseInt(cmd.substring(i + 1));
          sendPack.addString(addRewards(xp, 0, 0, 0, "m", 0));
          send(sendPack, true);
        } else if (command.equals("unban")) {
          this.gameServer.unbanPlayer(cmd.substring(i + 1), this.account);
        } else if (command.equals("level")) {
          changeLevel(Integer.parseInt(cmd.substring(i + 1)));
        } else if (command.equals("shop")) {
          loadShop(Integer.parseInt(cmd.substring(i + 1)));
        } else if (command.equals("post")) {
          sendPack.addString("%xt%server%-1%(GM) (" + this.account + "): " + cmd.substring(i + 1) + "%");
          this.gameServer.writeGlobalPacket(this.account, sendPack, true, false);
        } else {
          sendPack.addString("%xt%warning%-1%Comando invalido, digite !ver para ver a lista de comandos.%");
          send(sendPack, true);
        }
      }else if (this.gameServer.getVIP(this.gameServer.getPlayerID(this.account)) == 1) {
         if (command.equals("setpvp")) {
          debug("Setting PvP: " + this.account);
          if (!this.fighting) {
            this.gameServer.pvpOn[this.accountid] = (this.gameServer.pvpOn[this.accountid] == false ? true : false);
            sendPack.addString("%xt%server%-1%PvP set to " + this.gameServer.pvpOn[this.accountid] + ".%");
            send(sendPack, true);
          } else {
            sendPack.addString("%xt%server%-1%You must wait at least thirty seconds after a battle to change your PvP status.%");
            send(sendPack, true);
          }
        } else if (command.equals("ver")) {
          serverMsg("Shop (Shop id) - Load any shop you want instantly.", "server", false, false);
          serverMsg("post (Message) - Annouce some messages to all players (free for vip's)", "server", false, false);
        } else if (command.equals("shop")) {
          int shopid = Integer.parseInt(cmd.substring(i + 1));
          loadShop(shopid);
        } else if (command.equals("post")) {
          sendPack.addString("%xt%server%-1%(VIP) (" + this.account + "): " + cmd.substring(i + 1) + "%");
          this.gameServer.writeGlobalPacket(this.account, sendPack, true, false);
        } else {
          sendPack.addString("%xt%warning%-1%Comando invalido, digite !ver para ver a lista de comandos.%");
          send(sendPack, true);
        }
      }else if (this.gameServer.getAdmin(this.gameServer.getPlayerID(this.account)) == 1)
        if (command.equals("ban")) {
          debug("Banning: " + cmd.substring(i + 1));
          this.gameServer.banPlayer(cmd.substring(i + 1), this.account);
        } else if (command.equals("setpvp")) {
          debug("Setting PvP: " + this.account);
          if (!this.fighting) {
            this.gameServer.pvpOn[this.accountid] = (this.gameServer.pvpOn[this.accountid] == false ? true : false);
            sendPack.addString("%xt%server%-1%PvP set to " + this.gameServer.pvpOn[this.accountid] + ".%");
            send(sendPack, true);
          } else {
            sendPack.addString("%xt%server%-1%You must wait at least thirty seconds after a battle to change your PvP status.%");
            send(sendPack, true);
          }
        } else if (command.equals("cp")) {
          int cp = Integer.parseInt(cmd.substring(i + 1));
          debug("Adding " + cp + " CP to " + this.account);
          sendPack.clean();
          sendPack.addString(addRewards(0, 0, cp, 0, "m", 0));
          send(sendPack, true);
        } else if (command.equals("heal")) {
          this.gameServer.hp[this.accountid] = this.gameServer.hpmax[this.accountid];
          this.gameServer.mp[this.accountid] = this.gameServer.mpmax[this.accountid];
          sendUotls(true, false, true, false, false);
        } else if (command.equals("ver")) {
          serverMsg("!Ban/Unban (user) - Bans/Unbans (user).", "server", false, false);
          serverMsg("!CP/Gold/XP (i) - add CP/Gold/XP.", "server", false, false);
          serverMsg("!Heal - Heal HP e MP.", "server", false, false);
          serverMsg("!Kick (user) - Kick (user).", "server", false, false);
          serverMsg("!Unmod (user) - Unmod (user).", "server", false, false);
          serverMsg("!Makemod (user) - Mod a (user).", "server", false, false);
          serverMsg("!VIP (user) - add vip (user).", "server", false, false);
          serverMsg("!Unvip (user) - remove vip (user).", "server", false, false);
          serverMsg("!Delete (user) - Delete (user).", "server", false, false);
          serverMsg("!Post (MSG) - Annouce some messages to all players (free for admin's)", "server", false, false);
          serverMsg("!Shop (Shop id) - Load any shop you want instantly.", "server", false, false);
          serverMsg("!Level (amount) Level up to your choice%", "server", false, false);
          serverMsg("!Kill (username) - Kills the player automatically...", "server", false, false);
          serverMsg("!Mod (message) - Displays (message) in the mod chat style.", "server", false, false);
        } else if (command.equals("gold")) {
          int gold = Integer.parseInt(cmd.substring(i + 1));
          debug("Adding " + gold + " gold to " + this.account);
          sendPack.clean();
          sendPack.addString(addRewards(0, gold, 0, 0, "m", 0));
          send(sendPack, true);
        } else if (command.equals("kick")) {
          debug("Kicking: " + cmd.substring(i + 1));
          this.gameServer.kickPlayer(cmd.substring(i + 1), this.account);
        } else if (command.equals("mod")) {
          sendPack.clean();
          sendPack.addString("%xt%moderator%-1%(" + this.account + "): " + cmd.substring(i + 1) + "%");
          this.gameServer.writeGlobalPacket(this.account, sendPack, true, false);
        } else if (command.equals("xp")) {
          int xp = Integer.parseInt(cmd.substring(i + 1));
          debug("Adding " + xp + " XP to " + this.account);
          sendPack.clean();
          sendPack.addString(addRewards(xp, 0, 0, 0, "m", 0));
          send(sendPack, true);
        } else if (command.equals("unban")) {
          debug("Unbanning: " + cmd.substring(i + 1));
          this.gameServer.unbanPlayer(cmd.substring(i + 1), this.account);
        } else if (command.equals("unmod")) {
          this.gameServer.unmodPlayer(cmd.substring(i + 1), this.account);
        } else if (command.equals("unvip")) {
          this.gameServer.unvipPlayer(cmd.substring(i + 1), this.account);
        } else if (command.equals("vip")) {
          this.gameServer.vipPlayer(cmd.substring(i + 1), this.account);
        } else if (command.equals("makemod")) {
          this.gameServer.modPlayer(cmd.substring(i + 1), this.account);
        } else if (command.equals("delete")) {
          this.gameServer.deletePlayer(cmd.substring(i + 1), this.account);
        } else if (command.equals("")) {
          sendPack.addString("%xt%server%-1%(VIP) (" + this.account + "): " + cmd.substring(i + 1) + "%");
          this.gameServer.writeGlobalPacket(this.account, sendPack, true, false);
        } else if (command.equals("emoteall")) {
          emoteAll(cmd.substring(i + 1));
        } else if (command.equals("forceemote")) {
          String[] omg = cmd.substring(i + 1).split(",");
          forceEmote(omg);
        } else if (command.equals("shop")) {
          int shopid = Integer.parseInt(cmd.substring(i + 1));
          loadShop(shopid);
        } else if (command.equals("kill")) {
          overKill(this.gameServer.getPlayerID(cmd.substring(i + 1)));
        } else if (command.equals("post")) {
          sendPack.addString("%xt%server%-1%(VIP) (" + this.account + "): " + cmd.substring(i + 1) + "%");
          this.gameServer.writeGlobalPacket(this.account, sendPack, true, false);
        } else if (command.equals("level")) {
          changeLevel(Integer.parseInt(cmd.substring(i + 1)));
        } else {
          sendPack.addString("%xt%warning%-1%Comando invalido, digite !ver para ver a lista de comandos.%");
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
    sendPack.addString("%sp:" + speed);
    sendPack.addString(",tx:" + tx);
    sendPack.addString(",ty:" + ty);
    sendPack.addString(",strFrame:" + this.playerRoom.frame[this.playerSlot] + "%");
    this.playerRoom.tx[this.playerSlot] = tx;
    this.playerRoom.ty[this.playerSlot] = ty;
    this.gameServer.writeMapPacket(this.account, sendPack, true, !cansee);
    sendPack.clean();
  }

  protected void whisperChat(String message, String otheruser)
  {
    Packet sendPack = new Packet();
    if (this.gameServer.getPlayerID(otheruser.toLowerCase()) > 0) {
      sendPack.addString("%xt%whisper%-1%");
      sendPack.addString(message);
      sendPack.addString("%" + this.account + "%" + otheruser + "%0%");
      send(sendPack, true);
      this.gameServer.writePlayerPacket(otheruser, sendPack, true);
      write(this.account + " > " + otheruser, message);
    } else {
      sendPack.addString("%xt%server%-1%Player " + otheruser + " could not be found%");
      send(sendPack, true);
    }
  }

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
            debug("Error (read): " + e.getMessage() + ", " + e.getCause());
            this.finalize();
        }
        return buffer.toString();
    }

    /**
     * Create a reader and writer for the socket
     */
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
            debug("Exception (run): " + e.getMessage() + ", " + e.getCause());
        }
        this.finalize();
    }

    /**
     * Closes the reader, the writer and the socket.
     */
    @Override
    protected void finalize() {
        try {
            this.hasFinalized = true;
            if(this.userid > 0){
                try {
                    Main.sql.doupdate("UPDATE wqw_users SET curServer='' WHERE id="+this.userid);
                } catch (Exception e){
                    debug("Exception (finalize sql), userid: "+this.userid+", "+e.getMessage());
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
            debug("Exception (finalize): " + e.getMessage());
            gameServer.removeuser(this.account);
            this.server.remove(this.getRemoteAddress());
            stop();
        }
    }
}
