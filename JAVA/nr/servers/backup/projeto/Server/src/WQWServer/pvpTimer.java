 package WQWServer;
 
 import java.util.Timer;
 import java.util.TimerTask;
 
 public class pvpTimer
 {
    public static void main(final ServerConnection client) {

    final Timer timer = new Timer();

    int pingTime = 30000;
     timer.schedule(new TimerTask()
       {
       public void run()
       {
         try {
           client.fighting = false;
           client.monfighting = 0;
         } catch (Exception e) {
           Main.server.debug("Exception in pvp timer: " + e.getMessage());
         }
       }
     }
     , pingTime);
   }
 }
