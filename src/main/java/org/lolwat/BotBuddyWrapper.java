package org.lolwat;

import org.dreambot.api.script.AbstractScript;
import org.dreambot.api.script.Category;
import org.dreambot.api.script.ScriptManifest;
import org.dreambot.api.script.ScriptManager;
import org.dreambot.api.utilities.Sleep;

import java.util.Arrays;
import java.util.concurrent.atomic.AtomicBoolean;

@ScriptManifest(category = Category.MISC, name = "BotBuddyWrapper", author = "Riboflavin", version = 0.1)
public class BotBuddyWrapper extends AbstractScript {
    private final ScriptManager manager = ScriptManager.getScriptManager();
    private Core core;
    private Thread coreThread;
    private final AtomicBoolean shouldStop = new AtomicBoolean(false);

    @Override
    public synchronized void onStart(String ...args) {

        core = new Core();
        coreThread = new Thread(core);
        coreThread.start();

        // Handle script parameters and starting scripts in a separate thread
        new Thread(() -> {
            if (args.length > 0) {
                try {
                    Thread.sleep(300);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
                manager.stop();
                Sleep.sleepUntil(() -> !manager.isRunning(), 10000);
                if (manager.getState().equals(ScriptManager.State.STOP)) {
                    manageScripts(args);
                }
            }
            shouldStop.set(true);
        }).start();
        this.stop();
    }

    private synchronized void manageScripts(String[] params) {
        if (params.length > 0) {
            String scriptName = params[0];
            String[] scriptParams = Arrays.copyOfRange(params, 1, params.length);

            try {
                Thread.sleep(3000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }

            manager.start(scriptName, scriptParams);
        }
    }

    @Override
    public int onLoop() {
        if (shouldStop.get()) {
            stop();
        }
        return 1000;
    }
}