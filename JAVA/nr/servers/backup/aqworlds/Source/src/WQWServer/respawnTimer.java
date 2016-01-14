package WQWServer;

import java.util.Timer;
import java.util.TimerTask;

/**
 * Respawns a monster
 * @author XVII
 */


public class respawnTimer {
    static int restime=6000;
    public static void main(final int monsterid, final int monstertype, final int roomtype, final int roomnumb) {

    final Timer timer = new Timer();
     int pingTime = restime;
     timer.schedule(new TimerTask()
       {
            @Override
        public void run() {
            try {
                Main.server.gameServer.room[roomtype][roomnumb].respawnMonsterDo(monsterid);
            } catch (Exception e) {
                Main.server.debug("Exception in respawn timer: "+e.getMessage());
                Main.server.gameServer.room[roomtype][roomnumb].respawnMonsterDo(monsterid);
            }
        }
      },pingTime);

    }
}