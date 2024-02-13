package org.lolwat;

import org.dreambot.api.script.ScriptManager;

import java.util.Arrays;

import static org.dreambot.api.utilities.Logger.log;

public class ScriptLaunch implements Runnable {
    private final ScriptManager manager;
    private final String scriptName;
    private final String[] params;

    public ScriptLaunch(ScriptManager manager, String scriptName, String[] params) {
        this.manager = manager;
        this.scriptName = scriptName;
        this.params = params;
    }

    @Override
    public void run() {
        try {
            // Wait a brief moment to ensure the previous script has fully stopped
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return;
        }

        if (!manager.isRunning() && scriptName != null) {
            log("Attempting to start next script: " + scriptName + " with params: " + Arrays.toString(params));
            manager.start(scriptName, params);
        } else {
            log("Unable to start the next script. The script manager may still be running, or no script name was provided.");
        }
    }
}
