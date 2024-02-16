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
    private final Lock managerLock = new ReentrantLock();

    public ScriptLaunch(ScriptManager manager, String scriptName, String[] params) {
        this.manager = manager;
        this.scriptName = scriptName;
        this.params = params;
    }

    @Override
    public synchronized void run() {
        managerLock.lock();
        try {

            ensureScriptStopped();
            while (true) {
                if (!manager.isRunning()) {
                    if (scriptName != null) {
                        log("Attempting to start next script: " + scriptName + " with params: " + Arrays.toString(params));
                        manager.start(scriptName, params);
                    } else {
                        log("No script name was provided.");
                    }
                }
                try {
                    Thread.sleep(5000); // Wait for 5 seconds before checking if the script is running
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    return;
                }
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
                        Thread.sleep(5000); // Checking every 5 seconds if the script has stopped
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