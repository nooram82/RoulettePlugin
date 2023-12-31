package me.maroon28.roulette;

import eu.okaeri.configs.ConfigManager;
import eu.okaeri.configs.yaml.bukkit.YamlBukkitConfigurer;
import eu.okaeri.configs.yaml.bukkit.serdes.SerdesBukkit;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.logging.Level;

public final class RoulettePlugin extends JavaPlugin {
    private static RoulettePlugin PLUGIN;
    private RouletteConfig config;

    public RoulettePlugin() {
        PLUGIN = this;
    }

    public static RoulettePlugin getInstance() {
        return PLUGIN;
    }

    @Override
    public void onEnable() {
        getServer().getPluginManager().registerEvents(new RouletteListener(), this);
        getCommand("roulette").setExecutor(new RouletteCommand());
        loadConfig();
    }

    private void loadConfig() {
        try {
            this.config = ConfigManager.create(RouletteConfig.class, (it) -> {
                it.withConfigurer(new YamlBukkitConfigurer(), new SerdesBukkit());
                it.withBindFile(new File(this.getDataFolder(), "config.yml"));
                it.saveDefaults();
                it.load(true);
            });
        } catch (Exception exception) {
            this.getLogger().log(Level.SEVERE, "Error loading config.yml", exception);
            getServer().getPluginManager().disablePlugin(this);
            return;
        }
    }

    @NotNull
    public RouletteConfig getRouletteConfig() {
        return config;
    }
}
