package org.apache.log4j.contrib;

import java.io.PrintStream;

import org.apache.catalina.Lifecycle;
import org.apache.catalina.LifecycleEvent;
import org.apache.catalina.LifecycleListener;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;

public class ServerLifecycleListener implements LifecycleListener {
    static boolean is_redirected = false;

    public ServerLifecycleListener() {
    }

    /**
     * Primary entry point for startup and shutdown events.
     *
     * @param event The event that has occurred
     */

    public void lifecycleEvent(LifecycleEvent event) {
        if (Lifecycle.BEFORE_INIT_EVENT.equals(event.getType())) {
            if (!is_redirected) {
                is_redirected = true;

                System.setErr(new PrintStream(new LoggingOutputStream(Logger.getLogger("stderr"), Level.WARN), true));
                System.setOut(new PrintStream(new LoggingOutputStream(Logger.getLogger("stdout"), Level.INFO), true));
            }
        }
    }
}
