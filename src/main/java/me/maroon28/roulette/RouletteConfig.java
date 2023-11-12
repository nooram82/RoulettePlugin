package me.maroon28.roulette;

import eu.okaeri.configs.OkaeriConfig;
import eu.okaeri.configs.annotation.Comment;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import redempt.redlib.itemutils.ItemUtils;

public class RouletteConfig extends OkaeriConfig {

    @Comment("Sets the roulette animation speed in ticks")
    private int tickDelay = 5;
    @Comment("Roulette item")
    private ItemStack rouletteItem;
    {
        this.rouletteItem = new ItemStack(Material.CHEST);
        ItemUtils.setName(rouletteItem, ChatColor.GOLD +  "Roulette Chest");
        ItemUtils.setLore(rouletteItem, "", "Place down to build a roulette!", "");
    }

    private String host = "";
    private String database = "";
    private String username = "";
    private String password = "";
    private int port = 3306;

    public int getTickDelay() {
        return tickDelay;
    }

    public ItemStack getRouletteItem() {
        return rouletteItem;
    }

    public String getHost() {
        return host;
    }

    public String getDatabase() {
        return database;
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    public int getPort() {
        return port;
    }
}
