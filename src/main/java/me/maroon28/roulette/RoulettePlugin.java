package me.maroon28.roulette;

import org.bukkit.plugin.java.JavaPlugin;

public final class RoulettePlugin extends JavaPlugin {
    private static RoulettePlugin PLUGIN;
    public RoulettePlugin() {
        PLUGIN = this;
    }

    public static RoulettePlugin getInstance() {
        return PLUGIN;
    }

    @Override
    public void onEnable() {
        getServer().getPluginManager().registerEvents(new RouletteListener(), this);
        // the base roulette structure
        saveResource("roulette-structure.dat", false);
    }

}
