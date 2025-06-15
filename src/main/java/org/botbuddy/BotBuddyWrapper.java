package org.botbuddy;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import org.dreambot.api.Client;
import org.dreambot.api.methods.container.impl.Inventory;
import org.dreambot.api.methods.container.impl.bank.Bank;
import org.dreambot.api.methods.interactive.Players;
import org.dreambot.api.methods.quest.Quests;
import org.dreambot.api.methods.settings.PlayerSettings;
import org.dreambot.api.methods.skills.Skill;
import org.dreambot.api.methods.skills.Skills;
import org.dreambot.api.methods.world.Worlds;
import org.dreambot.api.script.AbstractScript;
import org.dreambot.api.script.Category;
import org.dreambot.api.script.ScriptManager;
import org.dreambot.api.script.ScriptManifest;
import org.dreambot.api.utilities.Logger;

import java.util.Arrays;
import java.util.HashMap;

@ScriptManifest(
        category = Category.UTILITY,
        name = "BotBuddyWrapper",
        author = "lolwat",
        version = 2.0,
        image = "https://api.botbuddy.net/WatScripts.png")
public class BotBuddyWrapper extends AbstractScript {
    private String scriptName;
    private String[] params;
    private long bankCoins;
    private long inventoryCoins;
    private int questPoints;
    private int totalLevel;
    private String characterName;
    private String accountType;
    private int memberDaysLeft;
    private int currentWorld;
    private HashMap<Skill, Integer> skills;

    @Override
    public void onStart(String... args) {
        try {
            if (args.length < 1) {
                ScriptManager.getScriptManager().stop();
                return;
            }

            this.scriptName = args[0];
            this.params = args.length > 1 ? Arrays.copyOfRange(args, 1, args.length) : new String[0];

            this.bankCoins = 0;
            this.inventoryCoins = 0;
            this.questPoints = 0;
            this.totalLevel = 0;
            this.skills = new HashMap<>();

            new Thread(() -> {
                while (true) {
                    try {
                        if(!Client.isLoggedIn()) {
                            Thread.sleep(1000);
                            continue;
                        }

                        if(Bank.isOpen() || Bank.isCached()) {
                            this.bankCoins = Bank.count("Coins");
                        }

                        this.inventoryCoins = Inventory.count("Coins");
                        this.questPoints = Quests.getQuestPoints();
                        this.totalLevel = Skills.getTotalLevel();
                        this.characterName = Players.getLocal().getName();
                        this.accountType = Worlds.getCurrent().isMembers() ? "P2P" : "F2P";
                        this.memberDaysLeft = PlayerSettings.getConfig(1780);
                        this.currentWorld = Worlds.getCurrentWorld();
                        this.skills.clear();

                        for (Skill skill : Skill.values()) {
                            this.skills.put(skill, Skills.getRealLevel(skill));
                        }

                        JsonObject jsonOutput = getJsonObject();
                        JsonObject statsJson = new JsonObject();
                        for (Skill skill : skills.keySet()) {
                            statsJson.addProperty(skill.getName(), skills.get(skill));
                        }

                        jsonOutput.add("BB_STATS", statsJson);

                        Gson gson = new Gson();
                        Logger.log("BB_OUTPUT: " + gson.toJson(jsonOutput));

                        Thread.sleep(60000);
                    } catch (InterruptedException e) {
                        break;
                    } catch (Exception ignored) {}
                }
            }, "BotBuddy-Background").start();
            new Thread(() -> {
                try {
                    Thread.sleep(500);
                } catch (InterruptedException ignored) {}
                ScriptManager.getScriptManager().start(scriptName, params);
            }).start();
            ScriptManager.getScriptManager().stop();
        } catch (Exception e) {
            Logger.error("Error during onStart: " + e.getMessage());
            ScriptManager.getScriptManager().stop();
        }
    }

    private JsonObject getJsonObject() {
        JsonObject jsonOutput = new JsonObject();
        jsonOutput.addProperty("BB_DISPLAYNAME", characterName);
        jsonOutput.addProperty("BB_TYPE", accountType);
        jsonOutput.addProperty("BB_MEM_DAYS_LEFT", memberDaysLeft);
        jsonOutput.addProperty("BB_WORLD", currentWorld);
        jsonOutput.addProperty("BB_GP", Math.max(bankCoins + inventoryCoins, 0));
        jsonOutput.addProperty("BB_TTL", totalLevel);
        jsonOutput.addProperty("BB_QP", questPoints);
        return jsonOutput;
    }

    @Override
    public int onLoop() {
        return 150;
    }
}