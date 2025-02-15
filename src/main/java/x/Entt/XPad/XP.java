package x.Entt.XPad;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.TextComponent;
import net.milkbowl.vault.economy.Economy;

import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

import x.Entt.XPad.CMDs.CMD;
import x.Entt.XPad.Events.Events;
import x.Entt.XPad.Events.OPEvents;
import x.Entt.XPad.Utils.MSG;
import x.Entt.XPad.Utils.Metrics;
import x.Entt.XPad.Utils.Updater;

public class XP extends JavaPlugin{
    int bStatsID = 21579;
    private Metrics metrics;
    private Updater updater;
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

        updater = new Updater(this, 116150);

        logMetrics();

        searchUpdates();

        registerConfig();
        registerEvents();
        registerCommands();
    }

    public void onDisable() {
        Bukkit.getConsoleSender().sendMessage
                (MSG.color(prefix + "&av" + version + " &cDisabled"));

        metrics.shutdown();
    }

    private void logToConsole(String message) {
        Bukkit.getConsoleSender().sendMessage(MSG.color(message));
    }

    private void logMetrics() {
        metrics.addCustomChart(new Metrics.SimplePie("vault_enabled", () -> String.valueOf(getConfig().getBoolean("vault.enabled", false))));

        String launchPlate = getConfig().getString("launch-plate", "STONE_PRESSURE_PLATE");
        String bottomBlock = getConfig().getString("bottom-block", "REDSTONE_BLOCK");
        String combination = launchPlate + " + " + bottomBlock;

        metrics.addCustomChart(new Metrics.AdvancedPie("launcher_combinations", () -> {
            Map<String, Integer> combinations = new HashMap<>();
            combinations.put(combination, 1);
            return combinations;
        }));
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
        File configFile = new File(getDataFolder(), "config.yml");

        if (!configFile.exists()) {
            saveResource("config.yml", false);
        }
    }

    public void registerCommands() {
        Objects.requireNonNull(this.getCommand("xpad")).setExecutor(new CMD(this));
    }

    public void registerEvents() {
        getServer().getPluginManager().registerEvents(new Events(this), this);
        getServer().getPluginManager().registerEvents(new OPEvents(this), this);
    }

    public void searchUpdates() {
        String downloadUrl = "https://www.spigotmc.org/resources/xpad-the-best-jumppads-plugin-1-8-1-21.116150/";
        TextComponent link = new TextComponent(MSG.color("&e&lClick here to download the update!"));
        link.setClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, downloadUrl));

        boolean updateAvailable = false;
        String latestVersion = "unknown";

        try {
            updateAvailable = updater.isUpdateAvailable();
            latestVersion = updater.getLatestVersion();
        } catch (Exception e) {
            logToConsole("&cError checking for updates: " + e.getMessage());
        }

        if (updateAvailable) {
            logToConsole("&2&l===========================================");
            logToConsole("&6&lNEW VERSION AVAILABLE!");
            logToConsole("&e&lCurrent Version: &f" + version);
            logToConsole("&e&lLatest Version: &f" + latestVersion);
            logToConsole("&e&lDownload it here: &f" + downloadUrl);
            logToConsole("&2&l===========================================");

            for (Player player : Bukkit.getOnlinePlayers()) {
                if (player.hasPermission("xpad.op")) {
                    player.sendMessage(MSG.color(prefix + "&e&lA new plugin update is available!"));
                    player.spigot().sendMessage(link);
                }
            }
        }
    }
}