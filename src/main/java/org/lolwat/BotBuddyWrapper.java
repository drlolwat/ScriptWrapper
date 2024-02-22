package org.lolwat;

import org.dreambot.api.randoms.RandomEvent;
import org.dreambot.api.randoms.RandomManager;
import org.dreambot.api.script.AbstractScript;
import org.dreambot.api.script.Category;
import org.dreambot.api.script.ScriptManifest;
import org.dreambot.api.script.ScriptManager;
import org.dreambot.core.Instance;

import java.util.Arrays;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;

@ScriptManifest(category = Category.MISC, name = "BotBuddyWrapper", author = "Riboflavin", version = 1.0)
public class BotBuddyWrapper extends AbstractScript {
    private final ScriptManager manager = Instance.getInstance().getScriptManager();
    private final AtomicBoolean shouldStop = new AtomicBoolean(false);
    private final Lock stopLock = new ReentrantLock();
    private final String instanceId = UUID.randomUUID().toString();
    private String nextScriptName = null;
    private String[] nextScriptParams = null;
    private Thread coreThread;
    private long startTime = System.currentTimeMillis();
    private final ThreadPoolExecutor executor = (ThreadPoolExecutor) Executors.newFixedThreadPool(5);

    public synchronized void disableLoginManager() {
        getRandomManager().disableSolver(RandomEvent.LOGIN);
    }

    public synchronized void enableLoginManager() {
        getRandomManager().enableSolver(RandomEvent.LOGIN);
    }

    @Override
    public void onStart(String... args) {
        stopLock.lock();
        try {
            super.onStart();
            disableLoginManager();

            //log("Starting BotBuddyWrapper instance: " + instanceId);
            Core core = new Core();
            coreThread = new Thread(core, "CoreThread-" + instanceId);
            coreThread.start();

            if (args.length > 0) {
                nextScriptName = args[0];
                nextScriptParams = Arrays.copyOfRange(args, 1, args.length);
            }

            if (nextScriptName != null) {
                startNextScript();
            }
        } finally {
            stopLock.unlock();
        }
    }

    @Override
    public int onLoop() {
        stopLock.lock();
        try {
            shouldStop.set(true);

            if (shouldStop.get()) {
                return -1;
            }
            return 1000;
        } finally {
            stopLock.unlock();
        }
    }

    @Override
    public void onExit() {
        stopLock.lock();
        try {
            enableLoginManager();
        } finally {
            stopLock.unlock();
        }
    }

    private void startNextScript() {
        ScriptLaunch scriptLaunch = new ScriptLaunch(nextScriptName, nextScriptParams);
        executor.execute(scriptLaunch);
    }
}