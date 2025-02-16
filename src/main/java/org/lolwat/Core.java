package org.lolwat;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import org.dreambot.api.Client;
import org.dreambot.api.methods.container.impl.Inventory;
import org.dreambot.api.methods.container.impl.bank.Bank;
import org.dreambot.api.methods.quest.Quests;
import org.dreambot.api.methods.settings.PlayerSettings;
import org.dreambot.api.methods.skills.Skill;
import org.dreambot.api.methods.skills.Skills;
import org.dreambot.api.methods.widget.Widget;
import org.dreambot.api.methods.widget.Widgets;
import org.dreambot.api.methods.world.Worlds;
import org.dreambot.api.wrappers.interactive.Player;
import org.dreambot.api.methods.interactive.Players;
import org.dreambot.api.wrappers.items.Item;
import org.dreambot.api.wrappers.widgets.WidgetChild;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

import static org.dreambot.api.utilities.Logger.log;

//TODO Handle muling "BB_MULE: {\"internalId\": \"\", \"internalAccount\": \"\", \"world\": \"\", \"location\": \"\", \"items\":{}, \"master\": \"\"}"
//TODO Limit 10 accounts per world, optionally per host
//TODO Add check for stuckness
//TODO add live bank and inventory
//TODO listen for rare item drops and report to user/org


public class Core implements Runnable {
    private long lastBankGP = -1;
    private long lastTotalGP = -1;
    private int lastTotalLevel = -1;
    private int lastQuestPoints = -1;
    private final Map<Skill, Integer> lastSkillLevels = new HashMap<>();
    private boolean hasChanged = false;
    private boolean isFirstOutput = true;
    private boolean isBankChecked = false;

    @Override
    public void run() {
        while (true) {
            if (Bank.isOpen()) {
                checkForBankChanges();
                isBankChecked = true;
            } else {
                checkForChanges();
            }

            if ((hasChanged || isFirstOutput) && isBankChecked) {
                logInformation();
                hasChanged = false;
                isFirstOutput = false;
            }

            try {
                Thread.sleep(1000); // Run core every 1 seconds
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }

    private void checkForBankChanges() {
        long currentBankGP = getBankGP() + getBankPlatinumTokens();
        long currentInventoryGP = getInventoryGP() + getInventoryPlatinumTokens();
        long currentTotalGP = currentBankGP + currentInventoryGP;

        if (currentTotalGP != lastTotalGP) {
            lastTotalGP = currentTotalGP;
            lastBankGP = currentBankGP;
            hasChanged = true;
        }
    }

    private void checkForChanges() {

        int currentTotalLevel = getTotalLevel();
        if (currentTotalLevel != lastTotalLevel) {
            lastTotalLevel = currentTotalLevel;
            hasChanged = true;
        }

        int currentQuestPoints = getQuestPoints();
        if (currentQuestPoints != lastQuestPoints) {
            lastQuestPoints = currentQuestPoints;
            hasChanged = true;
        }

        for (Skill skill : Skill.values()) {
            int currentLevel = Skills.getRealLevel(skill);
            Integer lastLevel = lastSkillLevels.get(skill);
            if (lastLevel == null || currentLevel != lastLevel) {
                lastSkillLevels.put(skill, currentLevel);
                hasChanged = true;
            }
        }
    }

    private void logInformation() {
        String displayName = Players.getLocal().getName();
        String accountType = Client.isMembers() ? "P2P" : "F2P";
        int world = Worlds.getCurrentWorld();
        int membershipDaysLeft = PlayerSettings.getConfig(1780);

        JsonObject jsonOutput = new JsonObject();
        jsonOutput.addProperty("BB_DISPLAYNAME", displayName);
        jsonOutput.addProperty("BB_TYPE", accountType);
        jsonOutput.addProperty("BB_MEM_DAYS_LEFT", membershipDaysLeft);
        jsonOutput.addProperty("BB_WORLD", world);
        jsonOutput.addProperty("BB_GP", Math.max(lastTotalGP, 0));
        jsonOutput.addProperty("BB_TTL", lastTotalLevel);
        jsonOutput.addProperty("BB_QP", lastQuestPoints);

        JsonObject statsJson = new JsonObject();
        for (Map.Entry<Skill, Integer> entry : lastSkillLevels.entrySet()) {
            statsJson.addProperty(entry.getKey().getName(), entry.getValue());
        }
        jsonOutput.add("BB_STATS", statsJson);

        Gson gson = new Gson();
        log("BB_OUTPUT: " + gson.toJson(jsonOutput));
    }

    private long getBankPlatinumTokens() {
        Item platinumTokens = Bank.get("Platinum token");
        return platinumTokens != null ? platinumTokens.getAmount() * 1000L : 0L;
    }

    private long getInventoryPlatinumTokens() {
        Item platinumTokens = Inventory.get("Platinum token");
        return platinumTokens != null ? platinumTokens.getAmount() * 1000L : 0L;
    }

    private long getBankGP() {
        if (!Bank.isOpen()) {
            return lastBankGP;
        }
        Item coins = Bank.get("Coins");
        return coins != null ? coins.getAmount() : 0L;
    }

    private long getInventoryGP() {
        Item coins = Inventory.get("Coins");
        return coins != null ? coins.getAmount() : 0L;
    }

    private int getTotalLevel() {
        return Skills.getTotalLevel();
    }

    private int getQuestPoints() {
        return Quests.getQuestPoints();
    }
}