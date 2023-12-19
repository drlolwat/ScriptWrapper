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

public class Core implements Runnable{
    private int lastBankGP = -1;
    private int lastTotalLevel = -1;
    private int lastQuestPoints = -1;
    private final Map<Skill, Integer> lastSkillLevels = new HashMap<>();
    @Override
    public void run() {
        //log("Core thread started");
        while (true) {
            if (checkForChanges()) {
                logInformation();
            }
            try {
                Thread.sleep(5000); // Run core every 5 seconds
                //log("Core thread running");
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }

    private boolean checkForChanges() {
        boolean hasChanged = false;
        int currentBankGP = getBankGP();
        int currentTotalLevel = getTotalLevel();
        int currentQuestPoints = getQuestPoints();

        if (currentBankGP != lastBankGP || currentTotalLevel != lastTotalLevel || currentQuestPoints != lastQuestPoints) {
            hasChanged = true;
        }

    for (Skill skill : Skill.values()) {
        int currentLevel = Skills.getRealLevel(skill);
        Integer lastLevel = lastSkillLevels.get(skill);
        if (lastLevel == null || currentLevel != lastLevel){
            lastSkillLevels.put(skill, currentLevel);
            hasChanged = true;
        }
    }

    lastBankGP = currentBankGP;
    lastTotalLevel = currentTotalLevel;
    lastQuestPoints = currentQuestPoints;

    return hasChanged;
    }
    //TODO cleanup logs so that money moving operations don't cause output
    private void logInformation() {
        int totalGP = getBankGP() + getInventoryGP();
        log("BB_GP: " + totalGP);
        log("BB_TTL: " + getTotalLevel());
        log("BB_QP: " + getQuestPoints());
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

    private String getStats() {
        StringBuilder stats = new StringBuilder("{");
        for (Skill skill : Skill.values()) {
            int level = Skills.getRealLevel(skill);
            stats.append("\"").append(skill.getName()).append("\": ").append(level).append(", ");
        }
        stats.setLength(stats.length() - 2); // Remove the last comma and space
        stats.append("}");
        return stats.toString();
    }
}
