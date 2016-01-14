 package WQWServer;

 import java.net.ServerSocket;
 import java.net.Socket;
 import java.net.SocketAddress;
 import java.util.Vector;

 public class Server extends Thread
 {
   protected int port;
   protected ServerSocket serverSocket;
   protected boolean listening;
   protected Vector<ServerConnection> clientConnections;
   public static GameServer gameServer = new GameServer();

   public Server(int serverPort)
   {
     this.port = serverPort;
     this.listening = false;
     this.clientConnections = new Vector();
   }

   public int getPort()
   {
     return this.port;
   }

   public boolean getListening()
   {
     return this.listening;
   }

   public int getClientCount() {
     return this.clientConnections.size();
   }

   protected void debug(String msg)
   {
     Main.debug("Server - ", msg);
   }

   protected void lobbyinit() {
     gameServer.init();
   }

   public boolean remove(SocketAddress remoteAddress)
   {
     try
     {
       for (int i = 0; i < this.clientConnections.size(); i++) {
         ServerConnection client = (ServerConnection)this.clientConnections.get(i);
         if (client.getRemoteAddress().equals(remoteAddress)) {
           this.clientConnections.remove(i);
           return true;
         }
       }
     }
     catch (Exception e) {
       debug("Exception (remove): " + e.getMessage());
     }
     return false;
   }
  


   public void run()
   {
     try
     {
       this.serverSocket = new ServerSocket(this.port);
       this.listening = true;

       while (this.listening) {
         Socket socket = this.serverSocket.accept();

         debug("Cliente connectado: " + socket.getRemoteSocketAddress());
         ServerConnection socketConnection = new ServerConnection(socket, this, gameServer);
         this.clientConnections.add(socketConnection);
       }
     }
     catch (Exception e)
     {
       if (!e.getMessage().equals("socket closed"))
         debug("Exception (run): " + e.getMessage());
     }
   }

@Override   protected void finalize()
   {
     try
     {
       this.listening = false;
       for (int i = 0; i < this.clientConnections.size(); i++) {
         ServerConnection client = (ServerConnection)this.clientConnections.get(i);
         if (client != null) {
           client.finalize();
           client = null;
         }
      }
       gameServer.finalize();
       this.serverSocket.close();
     }
     catch (Exception e) {
       debug("Exception (finalize): " + e.getMessage());
     }
   }
 }
