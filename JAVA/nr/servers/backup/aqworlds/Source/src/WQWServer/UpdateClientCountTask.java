/*
 * UpdateClientCountTask.java
 * This file checks how many clients are waiting for login information.
 */

package WQWServer;

import java.util.*;

/**
 * @author XVII
 * UpdateClientCountTask updates the amount of connected clients.
 */
public class UpdateClientCountTask extends TimerTask {
    protected int count;

    
    /**
     * Updates the label with the number of connected clients.
     */
    @Override
    public void run() {
        this.count = Main.server.getClientCount();
        String msg = "Servidor Online - " + this.count + " Cliente" + ((this.count != 1) ? "s" : "");
        Main.gui.setClientCount(msg);
    }   
}
