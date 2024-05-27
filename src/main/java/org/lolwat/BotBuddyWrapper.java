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
    private Thread mapThread;
    private ScriptServer scriptServer;
    private final ThreadPoolExecutor executor = (ThreadPoolExecutor) Executors.newFixedThreadPool(1);

    @Override
    public void onStart(String... args) {
        stopLock.lock();
        try {
            Integer port = null;
            String scriptName = null;
            String[] params = null;

            if (args.length > 0) {
                try {
                    port = Integer.parseInt(args[0]);
                    if (args.length > 1) {
                        scriptName = args[1];
                        params = Arrays.copyOfRange(args, 2, args.length);
                    }
                } catch (NumberFormatException e) {
                    scriptName = args[0];
                    params = Arrays.copyOfRange(args, 1, args.length);
                }
            }

            Core core = new Core();
            coreThread = new Thread(core, "CoreThread");
            coreThread.start();

            //Map map = new Map();
            //mapThread = new Thread(map, "MapThread");
            //mapThread.start();

            if (port != null) {
                scriptServer = new ScriptServer(port);
                new Thread(scriptServer, "ScriptServerThread").start();
            }

            if (scriptName != null) {
                nextScriptName = scriptName;
                nextScriptParams = params;
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
        ScriptLaunch scriptLaunch = new ScriptLaunch(nextScriptName, nextScriptParams, scriptServer);
        executor.execute(scriptLaunch);
    }
}