package org.lolwat;

import org.dreambot.api.methods.container.impl.bank.Bank;
import org.dreambot.api.methods.quest.Quests;
import org.dreambot.api.methods.skills.Skill;
import org.dreambot.api.methods.skills.Skills;
import org.dreambot.api.wrappers.items.Item;

import static org.dreambot.api.utilities.Logger.log;

public class Core implements Runnable{
    @Override
    public void run() {
        //log("Core thread started");
        while (true) {
            logInformation();
            try {
                Thread.sleep(60000); // Run core every minute
                //log("Core thread running");
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }

    //TODO - output all info to log only if any of the values have changed
    private void logInformation() {
        log("BB_GP: " + getBankGP());
        log("BB_TTL: " + getTotalLevel());
        log("BB_QP: " + getQuestPoints());
        log("BB_STATS: " + getStats());
    }

    //TODO - add bank window listener to update bank gp and run core thread on bank open?
    private int getBankGP() {
        if (Bank.isOpen()) {
            Item coins = Bank.get("Coins");
            return coins != null ? coins.getAmount() : 0;
        }
        return 0;
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
