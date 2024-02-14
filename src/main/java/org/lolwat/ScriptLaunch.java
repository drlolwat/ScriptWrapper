package org.lolwat;

import org.dreambot.api.script.ScriptManager;
import static org.dreambot.api.utilities.Logger.log;

import java.util.Random;
import java.util.Arrays;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class ScriptLaunch implements Runnable {
    private final ScriptManager manager;
    private final String scriptName;
    private final String[] params;
    private final Random random = new Random();
    private final Lock managerLock = new ReentrantLock();

    public ScriptLaunch(ScriptManager manager, String scriptName, String[] params) {
        this.manager = manager;
        this.scriptName = scriptName;
        this.params = params;
    }

    @Override
    public synchronized void run() {
        int delay = random.nextInt(5001);

        try {
            log("Staggering script launch with a delay of " + delay + " ms.");
            Thread.sleep(delay); // Wait for the randomly determined delay
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return;
        }

        managerLock.lock();
        try {
            ensureScriptStopped();
            if (!manager.isRunning() && scriptName != null) {
                log("Attempting to start next script: " + scriptName + " with params: " + Arrays.toString(params));
                manager.start(scriptName, params);
            } else {
                log("Unable to start the next script. The script manager may still be running, or no script name was provided.");
            }
        } finally {
            managerLock.unlock();
        }
    }

    private void ensureScriptStopped() {
        managerLock.lock();
        try {
            if (manager.isRunning()) {
                log("A script is still running. Attempting to stop it.");
                manager.stop();

                try {
                    while (manager.isRunning()) {
                        Thread.sleep(500); // Checking every half second if the script has stopped
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    log("Interrupted while waiting for scripts to stop.");
                }
                log("All scripts stopped successfully.");
            }
        } finally {
            managerLock.unlock();
        }
    }
}
