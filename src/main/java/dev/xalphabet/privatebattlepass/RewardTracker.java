package dev.xalphabet.privatebattlepass;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;
import org.bukkit.configuration.file.YamlConfiguration;

public class RewardTracker {
    private String identifier;
    private String baseDirectory;
    private Set<String> claimedRewards;

    public RewardTracker(String baseDirectory) {
        this.baseDirectory = baseDirectory;
        this.claimedRewards = new HashSet<>();
        this.identifier = identifier;

        loadClaimedRewards();
    }

    private void loadClaimedRewards() {
        Path claimedRewardsPath = Paths.get(baseDirectory, "claimed_rewards.yml");
        if (Files.exists(claimedRewardsPath)) {
            YamlConfiguration config = YamlConfiguration.loadConfiguration(claimedRewardsPath.toFile());
            claimedRewards.addAll(config.getStringList("claimedRewards"));
        }
    }

    public boolean claimReward(String playerName, String rewardName) {
        String key = playerName + ":" + rewardName;
        if (claimedRewards.contains(key)) {
            return false;
        } else {
            claimedRewards.add(key);
            saveClaimedRewards();
            return true;
        }
    }

    public void markRewardClaimed(String playerName, String rewardName) {
        String key = playerName + ":" + rewardName;
        claimedRewards.add(key);
        saveClaimedRewards();
    }

    private void saveClaimedRewards() {
        Path claimedRewardsPath = Paths.get(baseDirectory, "claimed_rewards.yml");
        YamlConfiguration config = new YamlConfiguration();
        config.set("claimedRewards", new ArrayList<>(claimedRewards));
        try {
            config.save(claimedRewardsPath.toFile());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
