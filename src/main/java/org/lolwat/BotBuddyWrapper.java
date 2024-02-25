package org.lolwat;

import org.dreambot.api.script.AbstractScript;
import org.dreambot.api.script.Category;
import org.dreambot.api.script.ScriptManifest;

import java.util.Arrays;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;

@ScriptManifest(category = Category.MISC, name = "BotBuddyWrapper", author = "Riboflavin", version = 1.0)
public class BotBuddyWrapper extends AbstractScript {
    private final AtomicBoolean shouldStop = new AtomicBoolean(false);
    private final Lock stopLock = new ReentrantLock();
    private String nextScriptName = null;
    private String[] nextScriptParams = null;
    private Thread coreThread;
    private final ThreadPoolExecutor executor = (ThreadPoolExecutor) Executors.newFixedThreadPool(1);

    @Override
    public void onStart(String... args) {
        stopLock.lock();
        try {

            Core core = new Core();
            coreThread = new Thread(core, "CoreThread");
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

    private void startNextScript() {
        ScriptLaunch scriptLaunch = new ScriptLaunch(nextScriptName, nextScriptParams);
        executor.execute(scriptLaunch);
    }
}