 package WQWServer;
 
 import java.util.TimerTask;
 
 public class UpdateClientCountTask extends TimerTask
 {
   protected int count; 
   public void run()
   {
     this.count = Main.server.getClientCount();
     String msg = new StringBuilder().append("Servidor Online - ").append(this.count).append(" Cliente").append(this.count != 1 ? "s" : "").toString();
     Main.gui.setClientCount(msg);
   }
 }