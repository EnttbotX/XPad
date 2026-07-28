package x.Entt.XPad.Utils;

import java.io.File;
import java.io.IOException;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import x.Entt.XPad.XP;

public class FileHandler {
    private final XP plugin;
    private FileConfiguration config;
    public File configFile;
    private FileConfiguration messages;
    private File messagesFile;

    public FileHandler(XP plugin) {
        this.plugin = plugin;
    }

    public void saveDefaults() {
        configFile = new File(plugin.getDataFolder(), "config.yml");

        if (!configFile.exists()) {
            plugin.saveResource("config.yml", false);
        }

        messagesFile = new File(plugin.getDataFolder(), "messages.yml");

        if (!messagesFile.exists()) {
            plugin.saveResource("messages.yml", false);
        }
    }

    public void saveConfig() {
        try {
            if (config != null && configFile != null) {
                config.save(configFile);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void loadConfig() {
        String configFileName = "config.yml";
        configFile = new File(plugin.getDataFolder(), configFileName);
        if (!configFile.exists()) {
            plugin.saveResource(configFileName, false);
        }
        config = YamlConfiguration.loadConfiguration(configFile);
    }

    public FileConfiguration getConfig() {
        if (config == null) {
            loadConfig();
        }
        return config;
    }

    public void loadMessages() {
        String messagesFileName = "messages.yml";
        messagesFile = new File(plugin.getDataFolder(), messagesFileName);

        if (!messagesFile.exists()) {
            plugin.saveResource(messagesFileName, false);
        }
        messages = YamlConfiguration.loadConfiguration(messagesFile);
    }

    public void saveMessages() {
        try {
            if (messages != null && messagesFile != null) {
                YamlConfiguration yamlConfig = (YamlConfiguration) messages;
                yamlConfig.save(messagesFile);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void reloadMessages() {
        loadMessages();
    }

    public FileConfiguration getMessages() {
        if (messages == null) {
            loadMessages();
        }
        return messages;
    }

    public void loadAllFiles() {
        loadMessages();
        loadConfig();
    }
}