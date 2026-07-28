package x.Entt.XPad;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.TextComponent;

import org.bstats.bukkit.Metrics;
import org.bstats.charts.AdvancedPie;
import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.plugin.java.JavaPlugin;

import x.Entt.XPad.CMDs.CMD;
import x.Entt.XPad.Events.Events;
import x.Entt.XPad.Utils.*;

public class XP extends JavaPlugin{
    int bStatsID = 21579;
    private FileHandler fh;
    private Metrics metrics;
    private Updater updater;
    public static String prefix;
    private ConfigUpdater confUpdater;
    private final String version = getDescription().getVersion();

    public void onEnable(){
        metrics = new Metrics(this, bStatsID);
        updater = new Updater(this, 116150);
        fh = new FileHandler(this);
        cleanupPads();

        confUpdater = new ConfigUpdater(this);
        confUpdater.update();

        MSG.init(this);
        fh.saveDefaults();
        fh.loadAllFiles();
        prefix = fh.getMessages().getString("prefix", "&1[&eXPAD&1]&r ");

        logMetrics();

        if(fh.getConfig().getBoolean("updater.enabled")) {
            updater.searchUpdates();
        }

        registerConfig();
        registerEvents();
        registerCommands();

        Bukkit.getConsoleSender().sendMessage
                (Text.color(prefix + "&av" + version + " &2Enabled!"));
    }

    public void onDisable() {
        metrics.shutdown();

        Bukkit.getConsoleSender().sendMessage
                (Text.color(prefix + "&av" + version + " &cDisabled"));
    }

    private void logMetrics() {
        String launchPlate = getConfig().getString("launch-plate", "UNKNOWN");
        String bottomBlock = getConfig().getString("bottom-block", "UNKNOWN");
        String combination = launchPlate + " + " + bottomBlock;

        metrics.addCustomChart(new AdvancedPie("launcher_combinations", () -> {
            Map<String, Integer> combinations = new HashMap<>();
            combinations.put(combination, 1);
            return combinations;
        }));
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
    }

    public void cleanupPads() {
        FileHandler fh = this.getFh();
        ConfigurationSection padsSection = fh.getConfig().getConfigurationSection("pads");

        if (padsSection != null) {
            for (String key : padsSection.getKeys(false)) {
                if (padsSection.get(key) == null) {
                    fh.getConfig().set("pads." + key, null);
                }
            }
            fh.saveConfig();
        }
    }

    public Updater getUpdater() {
        return updater;
    }

    public FileHandler getFh() {
        return fh;
    }
}