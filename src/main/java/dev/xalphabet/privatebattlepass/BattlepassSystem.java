package dev.xalphabet.privatebattlepass;

import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

public class BattlepassSystem {
    private final JavaPlugin plugin;
    private final BattlepassGUI battlepassGUI;
    private final String baseDirectory;
    private final int maxMenus;
    private final Logger logger;
    private final Map<Integer, Map<String, Map<String, Object>>> pages = new HashMap<>();

    public BattlepassSystem(JavaPlugin plugin, String baseDirectory, int maxMenus, Logger logger) {
        this.plugin = plugin;
        this.baseDirectory = baseDirectory;
        this.maxMenus = maxMenus;
        this.logger = logger;

        this.battlepassGUI = new BattlepassGUI(this); // Initialize battlepassGUI
        loadPages();
    }

    public void addReward(int page, String rewardName, Material material, int slot, String displayName, String lore, List<String> leftClickCommands, List<String> rightClickCommands, Map<String, Integer> requirements) {
        if (!pages.containsKey(page)) {
            pages.put(page, new HashMap<>());
        }

        Map<String, Object> reward = new HashMap<>();
        reward.put("material", material.toString());
        reward.put("slot", slot);
        reward.put("itemDisplayName", displayName);
        reward.put("itemLore", lore);
        reward.put("leftClickCommands", leftClickCommands);
        reward.put("rightClickCommands", rightClickCommands);
        reward.put("requirements", requirements);

        pages.get(page).put(rewardName, reward);
        savePage(page);
    }

    public boolean isSlotOccupied(int page, int slot) {
        Map<String, Map<String, Object>> rewards = pages.get(page);
        if (rewards != null) {
            for (Map<String, Object> reward : rewards.values()) {
                if ((int) reward.get("slot") == slot) {
                    return true;
                }
            }
        }
        return false;
    }

    public Map<Integer, Map<String, Map<String, Object>>> getPages() {
        return pages;
    }

    public int getMaxMenus() {
        return maxMenus;
    }

    public JavaPlugin getPlugin() {
        return plugin;
    }

    public BattlepassGUI getBattlepassGUI() {
        return battlepassGUI;
    }

    private void loadPages() {
        for (int i = 1; i <= maxMenus; i++) {
            File pageFile = new File(baseDirectory, "page_" + i + ".yml");
            if (pageFile.exists()) {
                YamlConfiguration config = YamlConfiguration.loadConfiguration(pageFile);
                ConfigurationSection rewardsSection = config.getConfigurationSection("rewards");
                if (rewardsSection != null) {
                    Map<String, Map<String, Object>> rewardsMap = new HashMap<>();
                    for (String key : rewardsSection.getKeys(false)) {
                        rewardsMap.put(key, rewardsSection.getConfigurationSection(key).getValues(false));
                    }
                    pages.put(i, rewardsMap);
                }
            }
        }
    }

    private void savePage(int page) {
        File pageFile = new File(baseDirectory, "page_" + page + ".yml");
        YamlConfiguration config = new YamlConfiguration();
        config.createSection("rewards", pages.get(page));
        try {
            config.save(pageFile);
        } catch (IOException e) {
            logger.severe("Could not save page " + page + ": " + e.getMessage());
        }
    }
}
