package x.Entt.XPad;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.TextComponent;

import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
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

        MSGManager.init(this);
        fh.saveDefaults();
        fh.loadAllFiles();
        prefix = fh.getMessages().getString("prefix", "&1[&eXPAD&1]&r ");

        logMetrics();

        if(fh.getConfig().getBoolean("update-log.enabled")) {
            searchUpdates();
        }

        registerConfig();
        registerEvents();
        registerCommands();

        Bukkit.getConsoleSender().sendMessage
                (MSG.color(prefix + "&av" + version + " &2Enabled!"));
    }

    public void onDisable() {
        metrics.shutdown();

        Bukkit.getConsoleSender().sendMessage
                (MSG.color(prefix + "&av" + version + " &cDisabled"));
    }

    private void logMetrics() {
        String launchPlate = getConfig().getString("launch-plate", "UNKNOWN");
        String bottomBlock = getConfig().getString("bottom-block", "UNKNOWN");
        String combination = launchPlate + " + " + bottomBlock;

        metrics.addCustomChart(new Metrics.AdvancedPie("launcher_combinations", () -> {
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

    public void searchUpdates() {
        String downloadUrl = "https://www.spigotmc.org/resources/xpad-the-best-jumppads-plugin-1-8-1-21.116150/";
        TextComponent link = new TextComponent(MSG.color("&e&lClick here to download the update!"));
        link.setClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, downloadUrl));

        boolean updateAvailable = false;
        String latestVersion = "unknown";

        try {
            updater = new Updater(this, 116150);
            updateAvailable = updater.isUpdateAvailable();
            latestVersion = updater.getLatestVersion();
        } catch (Exception e) {
            Bukkit.getConsoleSender().sendMessage(MSG.color("&cError checking for updates: " + e.getMessage()));
        }

        if (updateAvailable) {
            Bukkit.getConsoleSender().sendMessage("");
            Bukkit.getConsoleSender().sendMessage(MSG.color("&2&l============= " + prefix + "&2&l============="));
            Bukkit.getConsoleSender().sendMessage(MSG.color("&6&lNEW VERSION AVAILABLE!"));
            Bukkit.getConsoleSender().sendMessage(MSG.color("&e&lCurrent Version: &f" + version));
            Bukkit.getConsoleSender().sendMessage(MSG.color("&e&lLatest Version: &f" + latestVersion));
            Bukkit.getConsoleSender().sendMessage(MSG.color("&e&lDownload it here: &f" + downloadUrl));
            Bukkit.getConsoleSender().sendMessage(MSG.color("&2&l============= " + prefix + "&2&l============="));
            Bukkit.getConsoleSender().sendMessage("");

            for (Player player : Bukkit.getOnlinePlayers()) {
                if (player.hasPermission("xpad.op")) {
                    player.sendMessage(MSG.color(prefix + "&e&lA new plugin update is available!"));
                    player.spigot().sendMessage(link);
                }
            }
        }
    }

    public void cleanupPads() {
        ConfigurationSection padsSection = this.getConfig().getConfigurationSection("pads");
        if (padsSection != null) {
            for (String key : padsSection.getKeys(false)) {
                if (padsSection.get(key) == null) {
                    this.getConfig().set("pads." + key, null);
                }
            }
            this.saveConfig();
        }
    }

    public FileHandler getFh() {
        return fh;
    }
}