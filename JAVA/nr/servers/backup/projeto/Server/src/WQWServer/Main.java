 package WQWServer;
 
 import java.io.FileInputStream;
 import java.net.InetAddress;
 import java.net.Socket;
 import java.sql.ResultSet;
 import java.util.Properties;
 import java.util.Timer;
 import javax.swing.JButton;
 
 public class Main
 {
   public static final boolean DEBUG = true;
   public static Server server;
   public static ServerGUI gui;
   public static int port = 5588;
   public static String serverIP;
   public static String serverName;
   protected static Properties serverConf = new Properties();
   protected static Properties serverCheck = new Properties();
   public static int version = 1;
   public static SQLDatabase sql;
   protected static boolean active = false;
 
   public static void debug(String label, String msg)
   {
     if (gui != null)
       gui.write(label + msg);
   }
 
   public static void addMessage(String label, String msg)
   {
     if (gui != null)
       gui.writeChat(label + msg);
   }
 
   public static String getip(Socket sock)
   {
     String s = sock.getInetAddress().toString();
     return s.substring(0, 0) + s.substring(1);
   }
 
   private static void loadconfigs()
   {
     try
     {
       FileInputStream fin = new FileInputStream("configs/server.conf");
       serverConf.load(fin);
       fin.close();
     }
     catch (Exception ex)
     {
       ex.printStackTrace();
     }
     serverIP = serverConf.getProperty("Server_IP");
   }
 
   private static boolean loadcheck()
   {
     sql = new SQLDatabase("Server");
     sql.start();
     String access = "";
     try
     {
       ResultSet rs = sql.doquery("SELECT accesscode FROM wqw_settings");
       if (rs.next()) {
         access = rs.getString("accesscode");
       }
     }
     catch (Exception ex)
     {
      ex.printStackTrace();
     }
     if (!access.equals("epicfailureisbad")) {
       debug("Autenticação - ", "Accesso bloqueado!");
       return false;
     }
     debug("Autenticação - ", "Acesso Permitido!");
     return true;
   }
 
   public static void main(String[] args)
   {
     try
     {
       gui = new ServerGUI(server);
       gui.setTitle("Neo Rider Quest Worlds Emulador - Versão 1.0.0.9");
       gui.setLocationRelativeTo(null);
       gui.setVisible(true);
 
       gui.jButton1.setEnabled(false);
       gui.jButton2.setEnabled(false);
       gui.jButton3.setEnabled(false);
       gui.jButton6.setEnabled(false);
       gui.jButton8.setEnabled(false);
       gui.jButton5.setEnabled(false);
       gui.jButton9.setEnabled(false);
       gui.jButton10.setEnabled(false);
       gui.jButton11.setEnabled(false);
     }
     catch (Exception e)
     {
       debug("Main", "Excepção (main)" + e.getMessage());
     }
   }
 
   protected static void startServer() {
     try {
       if ((!active) && (loadcheck())) {
         loadconfigs();
 
         server = new Server(port);
         server.start();
         server.lobbyinit();
 
         boolean stop = false;
 
         ResultSet is = sql.doquery("SELECT version FROM wqw_settings");
         if (is.next()) {
           int ver = is.getInt("version");
           if (ver != version) {
             debug("Noticia - ", "Seu cliente não é compatível.");
             stopServer(false);
             stop = true;
           } else {
             sql.doupdate("UPDATE wqw_servers SET online=1 WHERE ip='" + serverIP + "'");
           }
         } else {
           debug("Noticia - ", "impossível localizar a tabela de configurações no DB.");
           stopServer(false);
           stop = true;
         }
         is.close();
 
         if (!stop) {
           ResultSet rs = sql.doquery("SELECT * FROM wqw_servers WHERE ip='" + serverIP + "'");
           if (rs.next()) {
             serverName = rs.getString("name");
             sql.doupdate("UPDATE wqw_servers SET online=1, count=0 WHERE ip='" + serverIP + "'");
           } else {
             serverName = "Test";
           }
           rs.close();
           active = true;
           debug("Servidor - ", "Servidor iniciado (Porta " + port + ")");
           gui.setClientCount("Servidor Online - 0 Clientes");
           gui.newTimer();
 
           gui.jButton1.setEnabled(true);
           gui.jButton2.setEnabled(true);
           gui.jButton3.setEnabled(true);
 
           gui.jButton6.setEnabled(true);
           gui.jButton8.setEnabled(true);
           gui.jButton7.setEnabled(false);
           gui.jButton5.setEnabled(true);
           gui.jButton9.setEnabled(true);
           gui.jButton10.setEnabled(true);
           gui.jButton11.setEnabled(true);
         }
       } else {
         debug("Autenticação - ", "Falha de identificação");
         gui.jButton7.setEnabled(false);
       }
     } catch (Exception e) {
       debug("Main", "Excepção (main, startServer)" + e.getMessage());
     }
   }
 
   protected static void stopServer(boolean temp) {
     try {
       if (active) {
         Packet sendPack = new Packet();
         if (temp)
           sendPack.addString("%xt%logoutWarning%-1%%5%");
         else {
           sendPack.addString("%xt%logoutWarning%-1%%5%");
         }
         Server.gameServer.writeGlobalPacket("", sendPack, true, false);
         gui.setClientCount("Servidor Offline");
         active = false;
         server.finalize();
         sql.doupdate("UPDATE wqw_users SET curServer='Offline'");
         sql.doupdate("UPDATE wqw_servers SET online=0, count=0 WHERE name='" + serverName + "'");
         server = null;
         sql = null;
         gui.timer.cancel();
         gui.jButton1.setEnabled(false);
         gui.jButton2.setEnabled(false);
         gui.jButton3.setEnabled(false);
         gui.jButton6.setEnabled(false);
         gui.jButton8.setEnabled(false);
         gui.jButton7.setEnabled(true);
         gui.jButton5.setEnabled(false);
         gui.jButton9.setEnabled(false);
         gui.jButton10.setEnabled(false);
         gui.jButton11.setEnabled(false);
         debug("Servidor - ", "Servidor Parado");
       }
     } catch (Exception e) {
       debug("Main", "Excepção (main, stopServer) " + e.getMessage());
       server = null;
       sql = null;
     }
   }
 }