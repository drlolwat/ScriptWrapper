import org.dreambot.api.script.AbstractScript;
import org.dreambot.api.script.Category;
import org.dreambot.api.script.ScriptManifest;
import org.dreambot.api.script.ScriptManager;
import org.lolwat.Core;

import java.util.Arrays;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

@ScriptManifest(category = Category.MISC, name = "BotBuddyWrapper", author = "Riboflavin", version = 0.1)
public class BotBuddyWrapper extends AbstractScript {
    private final ScriptManager manager = ScriptManager.getScriptManager();
    private Core core;
    private Thread coreThread;
    private final AtomicBoolean shouldStop = new AtomicBoolean(false);
    private final Lock stopLock = new ReentrantLock();

    @Override
    public void onStart(String... args) {
        core = new Core();
        coreThread = new Thread(core, "CoreThread");
        coreThread.start();

        new Thread(() -> {
            if (args.length > 0) {
                manageScripts(args);
            }
            safelyStopScript();
        }, "ScriptManagerThread").start();
    }

    private void safelyStopScript() {
        stopLock.lock();
        try {
            shouldStop.set(true);
            if (coreThread != null && coreThread.isAlive()) {
                coreThread.interrupt();
                coreThread.join();
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } finally {
            stopLock.unlock();
        }
        // This call to stop() might be redundant if onLoop() is responsible for stopping the script.
        // Consider removing it if your logic in onLoop() is sufficient.
        stop();
    }

    private void manageScripts(String[] params) {
        if (params.length > 0 && !manager.isRunning()) {
            String scriptName = params[0];
            String[] scriptParams = Arrays.copyOfRange(params, 1, params.length);

            try {
                // Additional delay if necessary
                Thread.sleep(5000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }

            manager.start(scriptName, scriptParams);
        }
    }

    @Override
    public int onLoop() {
        if (shouldStop.get()) {
            safelyStopScript();
            return -1; // Stop the script immediately
        }
        return 1000; // Continue the loop with a delay of 1 second
    }

    @Override
    public void onExit() {
        // Ensure resources are cleaned up properly when the script exits
        safelyStopScript();
    }
}
