package dev.xalphabet.privatebattlepass;

import me.clip.placeholderapi.PlaceholderAPI;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.*;

public class BattlepassCommands implements CommandExecutor {
    private final BattlepassSystem battlepassSystem;
    private final PrivateBattlepass plugin;

    public BattlepassCommands(BattlepassSystem battlepassSystem, PrivateBattlepass plugin) {
        this.battlepassSystem = battlepassSystem;
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (label.equalsIgnoreCase("battlepass")) {
            if (sender instanceof Player) {
                Player player = (Player) sender;
                int page = 1;
                if (args.length > 0) {
                    try {
                        page = Integer.parseInt(args[0]);
                    } catch (NumberFormatException e) {
                        player.sendMessage(ChatColor.RED + "Invalid page number. Showing page 1.");
                    }
                }
                player.openInventory(battlepassSystem.getBattlepassGUI().getInventory(player, page));
                return true;
            }
        } else if (label.equalsIgnoreCase("addreward")) {
            if (args.length < 8) {
                sender.sendMessage(ChatColor.RED + "Usage: /addreward <page> <rewardName> <material> <slot> <displayName> <lore> <leftClickCommand> <rightClickCommand> [<requirementKey1> <requirementValue1> ...]");
                return false;
            }

            try {
                List<String> combinedArgsList = combineQuotedArgs(args);
                String[] combinedArgs = combinedArgsList.toArray(new String[0]);

                int page = Integer.parseInt(combinedArgs[0]);
                String rewardName = combinedArgs[1];
                String materialString = combinedArgs[2].toUpperCase();
                Material material;
                try {
                    material = Material.valueOf(materialString);
                } catch (IllegalArgumentException e) {
                    sender.sendMessage(ChatColor.RED + "Invalid material: " + materialString);
                    return false;
                }
                int slot = Integer.parseInt(combinedArgs[3]);

                if (battlepassSystem.isSlotOccupied(page, slot)) {
                    sender.sendMessage(ChatColor.RED + "Slot " + slot + " is already occupied on page " + page + ".");
                    return false;
                }

                String displayName = ChatColor.translateAlternateColorCodes('&', combinedArgs[4]);
                String lore = combinedArgs[5].replace("\\n", "\n");

                String leftClickCommand = combinedArgs[6];
                String rightClickCommand = combinedArgs[7];

                List<String> leftClickCommands = new ArrayList<>();
                leftClickCommands.add(leftClickCommand);

                List<String> rightClickCommands = new ArrayList<>();
                rightClickCommands.add(rightClickCommand);

                Map<String, Integer> requirements = parseRequirements(Arrays.copyOfRange(combinedArgs, 8, combinedArgs.length));

                StringBuilder formattedLore = new StringBuilder(lore);
                for (Map.Entry<String, Integer> entry : requirements.entrySet()) {
                    formattedLore.append("\n&bRequirement: ").append(entry.getKey()).append("/").append(entry.getValue());
                }

                battlepassSystem.addReward(page, rewardName, material, slot, displayName, formattedLore.toString(), leftClickCommands, rightClickCommands, requirements);
                sender.sendMessage(ChatColor.GOLD + "" + ChatColor.BOLD + "[Battlepass++] " + ChatColor.BOLD + "" + ChatColor.WHITE + "Reward '" + rewardName + "' added to page " + page + " successfully!");

                return true;
            } catch (NumberFormatException e) {
                sender.sendMessage(ChatColor.RED + "Invalid number format in arguments.");
                return false;
            }
        }
        return false;
    }

    private List<String> combineQuotedArgs(String[] args) {
        List<String> result = new ArrayList<>();
        StringBuilder currentArg = new StringBuilder();
        boolean inQuotes = false;

        for (String arg : args) {
            if (arg.startsWith("\"")) {
                inQuotes = true;
                currentArg.append(arg.substring(1));
            } else if (arg.endsWith("\"") && inQuotes) {
                inQuotes = false;
                currentArg.append(" ").append(arg, 0, arg.length() - 1);
                result.add(currentArg.toString());
                currentArg.setLength(0);
            } else if (inQuotes) {
                currentArg.append(" ").append(arg);
            } else {
                result.add(arg);
            }
        }

        if (currentArg.length() > 0) {
            result.add(currentArg.toString());
        }

        return result;
    }

    private Map<String, Integer> parseRequirements(String[] args) {
        Map<String, Integer> requirements = new HashMap<>();
        if (args.length % 2 != 0) {
            return requirements;
        }
        for (int i = 0; i < args.length; i += 2) {
            String key = args[i];
            try {
                int value = Integer.parseInt(args[i + 1]);
                requirements.put(key, value);
            } catch (NumberFormatException ignored) {
            }
        }
        return requirements;
    }

    private boolean checkRequirements(Player player, Map<String, Integer> requirements) {
        for (Map.Entry<String, Integer> requirement : requirements.entrySet()) {
            String placeholder = requirement.getKey();
            int requiredValue = requirement.getValue();

            String actualValueString = PlaceholderAPI.setPlaceholders(player, "%" + placeholder + "%");
            int actualValue;
            try {
                actualValue = Integer.parseInt(actualValueString);
            } catch (NumberFormatException e) {
                actualValue = 0;
            }

            plugin.getLogger().info("Checking requirement: " + placeholder + " required: " + requiredValue + " actual: " + actualValue);

            if (actualValue < requiredValue) {
                plugin.getLogger().info(player.getName() + " did not meet the requirements for " + placeholder);
                return false;
            }
        }
        return true;
    }
}
