package org.botbuddy;

import org.dreambot.api.script.AbstractScript;
import org.dreambot.api.script.Category;
import org.dreambot.api.script.ScriptManager;
import org.dreambot.api.script.ScriptManifest;
import org.dreambot.api.utilities.Logger;

import java.util.Arrays;

@ScriptManifest(category = Category.MISC, name = "BotBuddyWrapper", author = "lolwat", version = 2.0)
public class BotBuddyWrapper extends AbstractScript {
    private String scriptName;
    private String[] params;

    @Override
    public void onStart(String... args) {
        try {
            if (args.length < 1) {
                ScriptManager.getScriptManager().stop();
                return;
            }

            this.scriptName = args[0];
            this.params = args.length > 1 ? Arrays.copyOfRange(args, 1, args.length) : new String[0];

            new Thread(() -> {
                while (true) {
                    try {
                        // --- Place your background logic here ---
                        Logger.log("Background logic running...");
                        // ----------------------------------------

                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        break;
                    } catch (Exception ex) {
                        Logger.error("BotBuddy error: " + ex.getMessage());
                    }
                }
            }, "BotBuddy-Background").start();

            // Delay and start the specified script
            new Thread(() -> {
                try {
                    Thread.sleep(100);
                } catch (InterruptedException ignored) {}
                ScriptManager.getScriptManager().start(scriptName, params);
            }).start();

            ScriptManager.getScriptManager().stop();

        } catch (Exception e) {
            Logger.error("Error during onStart: " + e.getMessage());
            ScriptManager.getScriptManager().stop();
        }
    }

    @Override
    public int onLoop() {

        return 1000;
    }

}