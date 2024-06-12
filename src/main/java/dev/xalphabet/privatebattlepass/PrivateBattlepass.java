package dev.xalphabet.privatebattlepass;

import org.bukkit.Material;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

public class PrivateBattlepass extends JavaPlugin {
    private BattlepassSystem battlepassSystem;

    @Override
    public void onEnable() {
        Logger logger = getLogger();
        logger.info("Your plugin has been enabled!");

        int maxMenus = getConfig().getInt("maxMenus", 10);

        String baseDirectory = getDataFolder().getPath();
        battlepassSystem = new BattlepassSystem(this, baseDirectory, maxMenus, logger);

        // Register commands
        BattlepassCommands battlepassCommands = new BattlepassCommands(battlepassSystem);
        getCommand("battlepass").setExecutor(battlepassCommands);
        getCommand("addreward").setExecutor(battlepassCommands);

        // Register event listeners
        getServer().getPluginManager().registerEvents(new BattlepassListener(battlepassSystem), this);
        getServer().getPluginManager().registerEvents(battlepassSystem.getBattlepassGUI(), this);

        // Add example reward
        addExampleReward();
    }

    public BattlepassSystem getBattlepassSystem() {
        return battlepassSystem;
    }

    @Override
    public void onDisable() {
        getLogger().info("BattlepassPlusPlus has been disabled!");
    }

    public void addExampleReward() {
        int page = 1;
        String rewardName = "ExampleReward";
        Material material = Material.DIAMOND;
        int slot = 11;
        String displayName = "&6Test Reward: &lpage_1.yml";
        String lore = "&bThis is a test reward\n&bIt demonstrates the features of the plugin";

        Map<String, Integer> requirements = new HashMap<>();
        requirements.put("%statistic_mob_kills%", 5);
        requirements.put("%statistic_player_kills%", 10);
        requirements.put("%statistic_deaths%", 3);
        requirements.put("%statistic_damage_taken%", 50);
        requirements.put("%statistic_damage_dealt%", 100);

        for (Map.Entry<String, Integer> entry : requirements.entrySet()) {
            lore += "\n&bRequirement: " + entry.getKey() + "/" + entry.getValue();
        }

        String leftClickCommand = "say Left clicked the test reward!";
        String rightClickCommand = "say Right clicked the test reward!";

        List<String> leftClickCommands = new ArrayList<>();
        leftClickCommands.add(leftClickCommand);

        List<String> rightClickCommands = new ArrayList<>();
        rightClickCommands.add(rightClickCommand);

        battlepassSystem.addReward(page, rewardName, material, slot, displayName, lore, leftClickCommands, rightClickCommands, requirements);
    }
}
