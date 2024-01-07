package org.lolwat;

import org.dreambot.api.methods.container.impl.Inventory;
import org.dreambot.api.methods.container.impl.bank.Bank;
import org.dreambot.api.methods.quest.Quests;
import org.dreambot.api.methods.skills.Skill;
import org.dreambot.api.methods.skills.Skills;
import org.dreambot.api.wrappers.items.Item;

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
                Thread.sleep(500); // Run core every 0.5 seconds
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
        log("BB_GP: " + lastTotalGP);
        log("BB_TTL: " + lastTotalLevel);
        log("BB_QP: " + lastQuestPoints);
        logStats();
    }

    private void logStats() {
        StringBuilder stats = new StringBuilder("{");
        for (Map.Entry<Skill, Integer> entry : lastSkillLevels.entrySet()) {
            stats.append("\"").append(entry.getKey().getName()).append("\": ").append(entry.getValue()).append(", ");
        }
        stats.setLength(stats.length() - 2); // Remove the last comma and space
        stats.append("}");
        log("BB_STATS: " + stats);
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
