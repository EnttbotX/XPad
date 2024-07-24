package x.Entt.XPad;

import java.io.File;
import java.util.Objects;

import net.milkbowl.vault.economy.Economy;

import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

import x.Entt.XPad.CMDs.CMD;
import x.Entt.XPad.Events.Events;
import x.Entt.XPad.Utils.MSG;
import x.Entt.XPad.Utils.Metrics;
import x.Entt.XPad.Utils.UpdateLogger;

public class XPad extends JavaPlugin{

    int bStatsID = 21579;
    private Metrics metrics;
    public static String prefix;
    public static Economy econ = null;
    private final String version = getDescription().getVersion();

    public void onEnable(){
        metrics = new Metrics(this, bStatsID);

        if (getConfig().getString("prefix") != null) {
            prefix = getConfig().getString("prefix");
        } else {
            prefix = "&1[XPAD]";
        }

        Bukkit.getConsoleSender().sendMessage
                (MSG.color(prefix + "&av" + version + " &2Enabled!"));

        if (getConfig().getBoolean("Vault.enabled") && !setupEconomy()) {
            FileConfiguration config = getConfig();
            getLogger().severe(MSG.color(prefix + "&cVault not found, deactivating it for the plugin"));
            config.set("Vault.enabled", false);
            return;
        }

        updateLog();
        registerConfig();
        registerEvents();
        registerCommands();
    }

    public void onDisable() {
        Bukkit.getConsoleSender().sendMessage
                (MSG.color(prefix + "&av" + version + " &cDisabled"));
    }

    private boolean setupEconomy() {
        RegisteredServiceProvider<Economy> economyProvider = getServer().getServicesManager().getRegistration(net.milkbowl.vault.economy.Economy.class);
        if (economyProvider != null) {
            econ = (Economy) ((RegisteredServiceProvider<?>) economyProvider).getProvider();
        } else {
            Bukkit.getConsoleSender().sendMessage(MSG.color(prefix + "&cEconomyProvider is null"));
        }

        return (econ != null);
    }

    public void registerConfig() {
        FileConfiguration config = getConfig();
        File configFile = new File(getDataFolder(), "config.yml");

        if (!configFile.exists()) {
            config.options().copyDefaults(true);
            saveConfig();
        }
    }

    public void registerCommands() {
        Objects.requireNonNull(this.getCommand("xpad")).setExecutor(new CMD(this));
    }

    public void registerEvents() {
        getServer().getPluginManager().registerEvents(new Events(this), this);
    }

    private void updateLog() {
        int spigotID = 116150;
        UpdateLogger updateLogger = new UpdateLogger(this, spigotID);

        try {
            if (updateLogger.isUpdateAvailable()) {
                getLogger().info(MSG.color(prefix + "&cThere is a new update of the plugin"));
            } else {
                getLogger().info(MSG.color(prefix + "&2Plugin updated!"));
            }
        } catch (Exception e) {
            getLogger().warning(MSG.color(prefix + "&4&lError searching newer versions: " + e.getMessage()));
        }
    }
}