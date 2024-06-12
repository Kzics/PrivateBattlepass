package dev.xalphabet.privatebattlepass;

import me.clip.placeholderapi.PlaceholderAPI;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.List;
import java.util.Map;

public class BattlepassListener implements Listener {
    private final BattlepassSystem battlepassSystem;

    public BattlepassListener(BattlepassSystem battlepassSystem) {
        this.battlepassSystem = battlepassSystem;
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        Player player = (Player) event.getWhoClicked();
        Inventory clickedInventory = event.getClickedInventory();

        if (clickedInventory != null && ChatColor.stripColor(event.getView().getTitle()).startsWith("BATTLEPASS")) {
            event.setCancelled(true); // Cancel the event to prevent item movement

            int slot = event.getRawSlot();
            String[] titleParts = ChatColor.stripColor(event.getView().getTitle()).split(" ");
            int page = Integer.parseInt(titleParts[titleParts.length - 1].split("/")[0].replaceAll("[^0-9]", ""));
            int maxPages = battlepassSystem.getMaxMenus();

            if (slot == 9) { // Previous page
                if (page > 1) {
                    player.openInventory(battlepassSystem.getBattlepassGUI().getInventory(player, page - 1));
                }
            } else if (slot == 17) { // Next page
                if (page < maxPages) {
                    player.openInventory(battlepassSystem.getBattlepassGUI().getInventory(player, page + 1));
                }
            } else {
                // Handle item clicks
                Map<Integer, Map<String, Map<String, Object>>> pages = battlepassSystem.getPages();
                Map<String, Map<String, Object>> rewards = pages.get(page);

                if (rewards != null) {
                    for (Map.Entry<String, Map<String, Object>> entry : rewards.entrySet()) {
                        Map<String, Object> reward = entry.getValue();
                        int rewardSlot = (int) reward.get("slot");

                        if (rewardSlot == slot) {
                            if (checkRequirements(player, (Map<String, Integer>) reward.get("requirements"))) {
                                List<String> commands = (event.isLeftClick()) ? (List<String>) reward.get("leftClickCommands") : (List<String>) reward.get("rightClickCommands");
                                if (commands != null) {
                                    for (String command : commands) {
                                        String parsedCommand = PlaceholderAPI.setPlaceholders(player, command);
                                        player.performCommand(parsedCommand);
                                    }
                                }
                            } else {
                                player.sendMessage(ChatColor.RED + "You do not meet the requirements for this reward.");
                            }
                            break;
                        }
                    }
                }
            }
        }
    }

    private boolean checkRequirements(Player player, Map<String, Integer> requirements) {
        for (Map.Entry<String, Integer> requirement : requirements.entrySet()) {
            String placeholder = requirement.getKey();
            int requiredValue = requirement.getValue();

            String result = PlaceholderAPI.setPlaceholders(player, "%" + placeholder + "%");
            try {
                int actualValue = Integer.parseInt(result);
                if (actualValue < requiredValue) {
                    return false;
                }
            } catch (NumberFormatException e) {
                return false;
            }
        }
        return true;
    }
}
