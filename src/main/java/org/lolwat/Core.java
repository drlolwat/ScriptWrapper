package org.lolwat;

import org.dreambot.api.Client;
import org.dreambot.api.methods.container.impl.Inventory;
import org.dreambot.api.methods.container.impl.bank.Bank;
import org.dreambot.api.methods.quest.Quests;
import org.dreambot.api.methods.settings.PlayerSettings;
import org.dreambot.api.methods.skills.Skill;
import org.dreambot.api.methods.skills.Skills;
import org.dreambot.api.methods.widget.Widget;
import org.dreambot.api.methods.widget.Widgets;
import org.dreambot.api.wrappers.items.Item;
import org.dreambot.api.wrappers.widgets.WidgetChild;

import java.util.HashMap;
import java.util.Map;

import static org.dreambot.api.utilities.Logger.log;

//TODO Handle muling "BB_MULE: {\"internalId\": \"\", \"internalAccount\": \"\", \"world\": \"\", \"location\": \"\", \"items\":{}, \"master\": \"\"}"
//TODO Limit 10 accounts per world, optionally per host
//TODO Add check for stuckness
//TODO add live bank and inventory
//TODO listen for rare item drops and report to user/org

public class Core implements Runnable {
    private int lastBankGP = -1;
    private int lastInventoryGP = -1;
    private int lastTotalGP = -1;
    private int lastTotalLevel = -1;
    private int lastQuestPoints = -1;
    private final Map<Skill, Integer> lastSkillLevels = new HashMap<>();

    @Override
    public void run() {
        while (true) {
            if (Bank.isOpen()) {
                if (checkForBankChanges()) {
                    logInformation();
                }
            }
            try {
                Thread.sleep(1000); // Run core every 1 seconds
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }
    
    private boolean checkForBankChanges() {
        boolean hasChanged = false;

        int currentBankGP = getBankGP();
        int currentInventoryGP = getInventoryGP();
        int currentTotalGP = currentBankGP + currentInventoryGP;

        if (currentTotalGP != lastTotalGP) {
            lastTotalGP = currentTotalGP;
            lastBankGP = currentBankGP;
            lastInventoryGP = currentInventoryGP;
            hasChanged = true;
        }

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

        return hasChanged;
    }

    private void logInformation() {
        String displayName = "";
        Widget w = Widgets.getWidget(162);
        if (w != null) {
            WidgetChild c = w.getChild(55);
            displayName = c.getText().split(":")[0].trim();
        }

        String accountType = Client.isMembers() ? "P2P" : "F2P";
        int world = Client.getCurrentWorld();
        int membershipDaysLeft = PlayerSettings.getConfig(1780);

        StringBuilder jsonOutput = new StringBuilder();
        jsonOutput.append("{");
        jsonOutput.append("\"BB_DISPLAYNAME\": \"").append(displayName).append("\", ");
        jsonOutput.append("\"BB_TYPE\": \"").append(accountType).append("\", ");
        jsonOutput.append("\"BB_MEM_DAYS_LEFT\": ").append(membershipDaysLeft).append(", ");
        jsonOutput.append("\"BB_WORLD\": ").append(world).append(", ");
        jsonOutput.append("\"BB_GP\": ").append((long)lastTotalGP).append(", ");
        jsonOutput.append("\"BB_TTL\": ").append(lastTotalLevel).append(", ");
        jsonOutput.append("\"BB_QP\": ").append(lastQuestPoints).append(", ");
        jsonOutput.append(getStatsJson());
        jsonOutput.append("}");

        log("BB_OUTPUT: " + jsonOutput.toString());
    }


    private String getStatsJson() {
        StringBuilder statsJson = new StringBuilder("\"BB_STATS\": {");
        for (Map.Entry<Skill, Integer> entry : lastSkillLevels.entrySet()) {
            statsJson.append("\"").append(entry.getKey().getName()).append("\": ")
                    .append(entry.getValue()).append(", ");
        }
        if (!lastSkillLevels.isEmpty()) {
            statsJson.setLength(statsJson.length() - 2); // Remove the last comma and space
        }
        statsJson.append("}");
        return statsJson.toString();
    }

    private int getBankGP() {
        Item coins = Bank.get("Coins");
        return coins != null ? coins.getAmount() : 0;
    }

    private int getInventoryGP() {
        Item coins = Inventory.get("Coins");
        return coins != null ? coins.getAmount() : 0;
    }

    private int getTotalLevel() {
        return Skills.getTotalLevel();
    }

    private int getQuestPoints() {
        return Quests.getQuestPoints();
    }
}
