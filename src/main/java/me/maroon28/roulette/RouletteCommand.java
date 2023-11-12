package me.maroon28.roulette;

import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;
import redempt.redlib.itemutils.ItemUtils;

public class RouletteCommand implements CommandExecutor {
    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!sender.hasPermission("roulette.get")) {
            sender.sendMessage("No permission!");
            return true;
        }

        if (!args[0].equals("get")) {
            sender.sendMessage("Please use /roulette get!");
            return true;
        }
        if (sender instanceof Player player) {
            ItemStack itemStack = RoulettePlugin.getInstance().getRouletteConfig().getRouletteItem();
            ItemUtils.addPersistentTag(itemStack, new NamespacedKey(RoulettePlugin.getInstance(), "roulette-chest"), PersistentDataType.BOOLEAN, true);
            ItemUtils.give(player, itemStack, 1);
            player.sendMessage("Roulette chest successfully given!");
            return true;
        }

        return false;
    }

}
