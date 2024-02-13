package org.lolwat;

import org.dreambot.api.randoms.RandomEvent;
import org.dreambot.api.script.AbstractScript;
import org.dreambot.api.script.Category;
import org.dreambot.api.script.ScriptManifest;
import org.dreambot.api.script.ScriptManager;

import java.util.Arrays;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

@ScriptManifest(category = Category.MISC, name = "BotBuddyWrapper", author = "Riboflavin", version = 1.0)
public class BotBuddyWrapper extends AbstractScript {
    private final ScriptManager manager = ScriptManager.getScriptManager();
    private Core core;
    private Thread coreThread;
    private final AtomicBoolean shouldStop = new AtomicBoolean(false);
    private final Lock stopLock = new ReentrantLock();
    private final String instanceId = UUID.randomUUID().toString();
    private String nextScriptName = null;
    private String[] nextScriptParams = null;

    @Override
    public void onStart(String... args) {
        super.onStart();
        getRandomManager().disableSolver(RandomEvent.LOGIN);

        log("Starting BotBuddyWrapper instance: " + instanceId);
        core = new Core();
        coreThread = new Thread(core, "CoreThread-" + instanceId);
        coreThread.start();

        // Parse arguments for the next script
        if (args.length > 0) {
            nextScriptName = args[0];
            nextScriptParams = Arrays.copyOfRange(args, 1, args.length);
        }
    }

    private final long startTime = System.currentTimeMillis(); // Store start time

    @Override
    public int onLoop() {
        // Check if 2 seconds have passed since the script started
        if (System.currentTimeMillis() - startTime > 2000) {
            shouldStop.set(true); // Signal to stop the script
        }

        if (shouldStop.get()) {
            safelyStopScript();
            return -1; // Signal to stop the script immediately
        }

        // Normal operation
        return 1000; // Delay for next loop iteration (1 second here for example)
    }


    @Override
    public void onExit() {
        safelyStopScript();
        // Optionally start the next script if specified
        if (nextScriptName != null) {
            startNextScript();
        }
    }

    private void safelyStopScript() {
        stopLock.lock();
        try {
            shouldStop.set(true);
            if (coreThread != null && coreThread.isAlive()) {
                coreThread.interrupt();
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            stopLock.unlock();
        }
    }

    private void startNextScript() {
        if (nextScriptName != null) {
            // Create and start the script launch thread
            ScriptLaunch scriptLaunch = new ScriptLaunch(manager, nextScriptName, nextScriptParams);
            Thread launchThread = new Thread(scriptLaunch, "ScriptLaunchThread");
            launchThread.start();
        }
    }
}
