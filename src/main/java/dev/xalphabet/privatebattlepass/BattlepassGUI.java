package dev.xalphabet.privatebattlepass;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.scheduler.BukkitRunnable;
import me.clip.placeholderapi.PlaceholderAPI;

import java.util.*;

public class BattlepassGUI implements Listener {
    private final BattlepassSystem battlepassSystem;

    public BattlepassGUI(BattlepassSystem battlepassSystem) {
        this.battlepassSystem = battlepassSystem;
    }

    public Inventory getInventory(Player player, int page) {
        Map<Integer, Map<String, Map<String, Object>>> pages = battlepassSystem.getPages();
        Map<String, Map<String, Object>> rewards = pages.get(page);

        Inventory inventory = Bukkit.createInventory(null, 27, "BATTLEPASS - (" + page + "/" + battlepassSystem.getMaxMenus() + ")");

        if (rewards != null) {
            for (Map.Entry<String, Map<String, Object>> entry : rewards.entrySet()) {
                String rewardName = entry.getKey();
                Map<String, Object> reward = entry.getValue();
                Material material = Material.valueOf((String) reward.get("material"));
                int slot = (int) reward.get("slot");
                String displayName = ChatColor.translateAlternateColorCodes('&', (String) reward.get("itemDisplayName"));
                List<String> lore = parseLore(reward.get("itemLore"), player);

                ItemStack item = new ItemStack(material);
                ItemMeta meta = item.getItemMeta();
                if (meta != null) {
                    meta.setDisplayName(displayName);
                    meta.setLore(lore);
                    item.setItemMeta(meta);
                }

                inventory.setItem(slot, item);
            }
        }

        // Add navigation and decorative items
        addControlItems(inventory, page);

        // Start dynamic update
        startDynamicUpdate(player, inventory);

        return inventory;
    }

    private List<String> parseLore(Object loreObj, Player player) {
        List<String> loreLines = new ArrayList<>();
        if (loreObj instanceof List) {
            loreLines = (List<String>) loreObj;
        } else if (loreObj instanceof String) {
            String loreString = ((String) loreObj).replace("\\n", "\n");
            loreLines = Arrays.asList(loreString.split("\n"));
        }

        List<String> parsedLore = new ArrayList<>();
        for (String line : loreLines) {
            parsedLore.add(ChatColor.translateAlternateColorCodes('&', PlaceholderAPI.setPlaceholders(player, line)));
        }
        return parsedLore;
    }

    private void startDynamicUpdate(Player player, Inventory inventory) {
        new BukkitRunnable() {
            @Override
            public void run() {
                if (player.getOpenInventory().getTopInventory().equals(inventory)) {
                    for (int i = 0; i < 9; i++) {
                        inventory.setItem(i, createRandomGlassPane());
                    }
                    for (int i = 18; i < 27; i++) {
                        inventory.setItem(i, createRandomGlassPane());
                    }
                    inventory.setItem(10, createRandomGlassPane());
                    inventory.setItem(16, createRandomGlassPane());
                } else {
                    this.cancel();
                }
            }
        }.runTaskTimer(battlepassSystem.getPlugin(), 0, 20);
    }

    private ItemStack createRandomGlassPane() {
        Material[] glassPaneColors = {
                Material.BLACK_STAINED_GLASS_PANE,
                Material.BLUE_STAINED_GLASS_PANE,
                Material.GREEN_STAINED_GLASS_PANE,
                Material.CYAN_STAINED_GLASS_PANE,
                Material.RED_STAINED_GLASS_PANE,
                Material.MAGENTA_STAINED_GLASS_PANE,
                Material.YELLOW_STAINED_GLASS_PANE,
                Material.LIGHT_GRAY_STAINED_GLASS_PANE,
                Material.GRAY_STAINED_GLASS_PANE,
                Material.LIGHT_BLUE_STAINED_GLASS_PANE,
                Material.LIME_STAINED_GLASS_PANE,
                Material.PINK_STAINED_GLASS_PANE,
                Material.PURPLE_STAINED_GLASS_PANE,
                Material.ORANGE_STAINED_GLASS_PANE,
                Material.WHITE_STAINED_GLASS_PANE
        };

        Material randomColor = glassPaneColors[new Random().nextInt(glassPaneColors.length)];
        ItemStack glassPane = new ItemStack(randomColor);
        ItemMeta meta = glassPane.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(ChatColor.GRAY + "");
            glassPane.setItemMeta(meta);
        }
        return glassPane;
    }

    private void addControlItems(Inventory inventory, int page) {
        // Previous page arrow
        ItemStack previousPage = new ItemStack(Material.ARROW);
        ItemMeta previousMeta = previousPage.getItemMeta();
        if (previousMeta != null) {
            previousMeta.setDisplayName(ChatColor.YELLOW + "Previous Page");
            previousPage.setItemMeta(previousMeta);
        }
        inventory.setItem(9, previousPage);

        // Next page arrow
        ItemStack nextPage = new ItemStack(Material.ARROW);
        ItemMeta nextMeta = nextPage.getItemMeta();
        if (nextMeta != null) {
            nextMeta.setDisplayName(ChatColor.YELLOW + "Next Page");
            nextPage.setItemMeta(nextMeta);
        }
        inventory.setItem(17, nextPage);

        // Gray stained glass panes
        for (int i = 0; i < 9; i++) {
            inventory.setItem(i, createRandomGlassPane());
        }
        for (int i = 18; i < 27; i++) {
            inventory.setItem(i, createRandomGlassPane());
        }

        // Remove barriers
        for (int i = 0; i < inventory.getSize(); i++) {
            if (inventory.getItem(i) == null) {
                inventory.setItem(i, createRandomGlassPane());
            }
        }
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        Player player = (Player) event.getWhoClicked();
        Inventory clickedInventory = event.getClickedInventory();

        if (clickedInventory != null && clickedInventory.equals(event.getView().getTopInventory()) && event.getView().getTitle().startsWith("BATTLEPASS")) {
            event.setCancelled(true); // Cancel the event to prevent item movement

            int slot = event.getSlot();
            ItemStack clickedItem = event.getCurrentItem();
            if (clickedItem != null && clickedItem.getType() != Material.AIR) {
                for (Map<String, Map<String, Object>> rewardMap : battlepassSystem.getPages().values()) {
                    for (Map.Entry<String, Map<String, Object>> rewardEntry : rewardMap.entrySet()) {
                        Map<String, Object> reward = rewardEntry.getValue();
                        int rewardSlot = (int) reward.get("slot");
                        if (rewardSlot == slot) {
                            boolean requirementsMet = checkRequirements(player, (Map<String, Integer>) reward.get("requirements"));

                            if (requirementsMet) {
                                Bukkit.getLogger().info(player.getName() + " met all requirements for " + rewardEntry.getKey());
                                player.sendMessage(ChatColor.GREEN + "You have met the requirements for this reward!");
                                if (event.isLeftClick()) {
                                    executeCommands(player, (List<String>) reward.get("leftClickCommands"));
                                    Bukkit.getLogger().info("[Not Secure] [" + player.getName() + "] Left clicked the reward!");
                                } else if (event.isRightClick()) {
                                    executeCommands(player, (List<String>) reward.get("rightClickCommands"));
                                    Bukkit.getLogger().info("[Not Secure] [" + player.getName() + "] Right clicked the reward!");
                                }
                            } else {
                                Bukkit.getLogger().info(player.getName() + " did not meet the requirements for " + rewardEntry.getKey());
                                player.sendMessage(ChatColor.RED + "You do not meet the requirements for this reward.");
                            }
                            return;
                        }
                    }
                }
            }
        }
    }

    private boolean checkRequirements(Player player, Map<String, Integer> requirements) {
        for (Map.Entry<String, Integer> reqEntry : requirements.entrySet()) {
            String placeholder = reqEntry.getKey();
            int requiredValue = reqEntry.getValue();
            String formattedPlaceholder = "%" + placeholder + "%"; // Ensure placeholders are enclosed with % symbols
            String placeholderValue = PlaceholderAPI.setPlaceholders(player, formattedPlaceholder);

            int actualValue;
            try {
                actualValue = Integer.parseInt(placeholderValue);
            } catch (NumberFormatException e) {
                actualValue = 0;
            }

            // Log the placeholder, required value, and actual value for debugging
            Bukkit.getLogger().info("Checking requirement: " + placeholder + " required: " + requiredValue + " actual: " + actualValue);

            if (actualValue < requiredValue) {
                return false;
            }
        }
        return true;
    }

    private void executeCommands(Player player, List<String> commands) {
        if (commands != null && !commands.isEmpty()) {
            for (String command : commands) {
                Bukkit.dispatchCommand(Bukkit.getConsoleSender(), command.replace("%player%", player.getName()));
            }
        }
    }
}
