import org.dreambot.api.script.AbstractScript;
import org.dreambot.api.script.Category;
import org.dreambot.api.script.ScriptManifest;
import org.dreambot.api.script.ScriptManager;
import org.lolwat.Core;

import java.util.Arrays;
import java.util.concurrent.atomic.AtomicBoolean;

@ScriptManifest(category = Category.MISC,name = "BotBuddyWrapper",author = "Riboflavin",version = 0.1)
public class BotBuddyWrapper extends AbstractScript {
    private final ScriptManager manager = ScriptManager.getScriptManager();
    private Core core;
    private Thread coreThread;
    private final AtomicBoolean shouldStop = new AtomicBoolean(false);

    @Override
    public void onStart(String ...args) {

        core = new Core();
        coreThread = new Thread(core);
        coreThread.start();

        // Handle script parameters and starting scripts in a separate thread
        new Thread(() -> {
            if (args.length > 0) {
                try {
                    Thread.sleep(3000); // Sleep for 3 seconds
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
                manageScripts(args);
            }
            shouldStop.set(true);
        }).start();
        this.stop();
    }

    private void manageScripts(String[] params) {
        if (params.length > 0) {
            String scriptName = params[0];
            String[] scriptParams = Arrays.copyOfRange(params, 1, params.length);
            //log("Preparing to start script '" + scriptName + "' with parameters: " + Arrays.toString(scriptParams));

            try {
                Thread.sleep(3000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                //log("Thread interrupted while waiting to start script.");
            }

            //log("Starting script '" + scriptName + "' with parameters: " + Arrays.toString(scriptParams));
            manager.start(scriptName, scriptParams);
        }
    }
    @Override
    public int onLoop() {
        if (shouldStop.get()) {
            //log("Stopping BotBuddyWrapper.");
            stop();
        }
        return 1000;
    }
}