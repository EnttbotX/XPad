package x.Entt.XPad.Utils;

import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import x.Entt.XPad.XP;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

public class ConfigUpdater {

    private final XP plugin;

    public ConfigUpdater(XP plugin) {
        this.plugin = plugin;
    }

    public void update() {
        File configFile = new File(plugin.getDataFolder(), "config.yml");

        if (!configFile.exists()) {
            plugin.getFh().saveDefaults();
            return;
        }

        FileConfiguration config = YamlConfiguration.loadConfiguration(configFile);
        String version = config.getString("config-version", "0.0.0");
        if (version.equals("1.4.0")) return;

        String prefix = config.getString("prefix", "&1[&eXPAD&1]&r ");
        Bukkit.getConsoleSender().sendMessage(MSG.color(prefix + "&2Starting the migration of the config to v1.4.0"));

        double speed = config.getDouble("speed", 5.0);
        String direction = config.getString("direction", "player-view");
        String plate = config.getString("launch-plate", "STONE_PRESSURE_PLATE");
        String bottom = config.getString("bottom-block", "ANY");
        String sound = config.getString("sound", "ENTITY_FIREWORK_ROCKET_LAUNCH");
        List<String> worlds = config.getStringList("world-list.worlds");
        String listMode = config.getString("world-list.list-mode", "blacklist");

        boolean sendMsgEnabled = config.getBoolean("actions.send-message.enabled", true);
        List<String> sendMsgMessages = config.getStringList("actions.send-message.message");
        boolean sendTitleEnabled = config.getBoolean("actions.send-title.enabled", false);
        String title = config.getString("actions.send-title.title", "&cLaunched");
        String subtitle = config.getString("actions.send-title.subtitle", "&fbye-bye");
        int titleIn = config.getInt("actions.send-title.in", 10);
        int titleStay = config.getInt("actions.send-title.stay", 40);
        int titleOut = config.getInt("actions.send-title.out", 10);
        boolean executeCmdEnabled = config.getBoolean("actions.execute-cmd.enabled", false);
        List<String> executeCmds = config.getStringList("actions.execute-cmd.execute");

        try (FileWriter writer = new FileWriter(configFile)) {
            writer.write(
                    "#       ___    ___ ________  ________  ________\n" +
                            "#      |\\  \\  /  /|\\   __  \\|\\   __  \\|\\   ___ \\\n" +
                            "#      \\ \\  \\/  / | \\  \\|\\  \\ \\  \\|\\  \\ \\  \\_|\\ \\\n" +
                            "#       \\ \\    / / \\ \\   ____\\ \\   __  \\ \\  \\ \\\\ \\\n" +
                            "#        /     \\/   \\ \\  \\___|\\ \\  \\ \\  \\ \\  \\_\\\\ \\\n" +
                            "#       /  /\\   \\    \\ \\__\\    \\ \\__\\ \\__\\ \\_______\\\n" +
                            "#      /__/ /\\ __\\    \\|__|     \\|__|\\|__|\\|_______|\n" +
                            "#     |__|/  \\|__|        by EnttbotX\n\n" +

                            "config-version: 1.4.0\n\n" +

                            "update-log:\n" +
                            "  enabled: true\n" +
                            "  permission: \"xpad.updater\"\n\n" +

                            "pads:\n" +
                            "  '1':\n" +
                            "    speed: " + speed + "\n" +
                            "    direction: \"" + direction.replace("\"", "\\\"") + "\"\n" +
                            "    launch-plate: \"" + plate.replace("\"", "\\\"") + "\"\n" +
                            "    bottom-block: \"" + bottom.replace("\"", "\\\"") + "\"\n" +
                            "    sound: \"" + sound.replace("\"", "\\\"") + "\"\n" +
                            "    cooldown: 2\n" +
                            "    permission: \"xpad.use.1\"\n" +
                            "    actions:\n" +
                            "      send-message:\n" +
                            "        enabled: " + sendMsgEnabled + "\n" +
                            "        message:\n"
            );
            for (String msg : sendMsgMessages) {
                writer.write("          - \"" + msg.replace("\"", "\\\"") + "\"\n");
            }
            writer.write(
                    "      send-title:\n" +
                            "        enabled: " + sendTitleEnabled + "\n" +
                            "        title: \"" + title.replace("\"", "\\\"") + "\"\n" +
                            "        subtitle: \"" + subtitle.replace("\"", "\\\"") + "\"\n" +
                            "        in: " + titleIn + "\n" +
                            "        stay: " + titleStay + "\n" +
                            "        out: " + titleOut + "\n" +
                            "      execute-cmd:\n" +
                            "        enabled: " + executeCmdEnabled + "\n" +
                            "        execute:\n"
            );
            for (String cmd : executeCmds) {
                writer.write("          - \"" + cmd.replace("\"", "\\\"") + "\"\n");
            }
            writer.write(
                    "    trail:\n" +
                            "      enabled: true\n" +
                            "      type: \"FLAME\"\n\n" +

                            "  '2':\n" +
                            "    speed: 12\n" +
                            "    direction: \"single-direction\"\n" +
                            "    vector: [20, 10]\n" +
                            "    launch-plate: \"STONE_PRESSURE_PLATE\"\n" +
                            "    bottom-block: \"ANY\"\n" +
                            "    sound: \"ENTITY_FIREWORK_ROCKET_LAUNCH\"\n" +
                            "    cooldown: 2\n" +
                            "    permission: \"xpad.use.2\"\n" +
                            "    actions:\n" +
                            "      send-message:\n" +
                            "        enabled: false\n" +
                            "        message:\n" +
                            "          - \"&3Launched!\"\n" +
                            "      send-title:\n" +
                            "        enabled: false\n" +
                            "        title: \"&cLaunched\"\n" +
                            "        subtitle: \"&fbye-bye\"\n" +
                            "        in: 10\n" +
                            "        stay: 5\n" +
                            "        out: 10\n" +
                            "      execute-cmd:\n" +
                            "        enabled: false\n" +
                            "        execute:\n" +
                            "          - \"eco give %player% 100\"\n" +
                            "    trail:\n" +
                            "      enabled: true\n" +
                            "      type: \"FLAME\"\n\n" +

                            "world-list:\n" +
                            "  enabled: true\n" +
                            "  list-mode: \"" + listMode.replace("\"", "\\\"") + "\"\n" +
                            "  worlds:\n"
            );
            for (String w : worlds) {
                writer.write("    - \"" + w.replace("\"", "\\\"") + "\"\n");
            }
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }

        File messagesFile = new File(plugin.getDataFolder(), "messages.yml");
        if (!messagesFile.exists()) {
            try (FileWriter writer = new FileWriter(messagesFile)) {
                writer.write(
                        "#       ___    ___ ________  ________  ________\n" +
                                "#      |\\  \\  /  /|\\   __  \\|\\   __  \\|\\   ___ \\\n" +
                                "#      \\ \\  \\/  / | \\  \\|\\  \\ \\  \\|\\  \\ \\  \\_|\\ \\\n" +
                                "#       \\ \\    / / \\ \\   ____\\ \\   __  \\ \\  \\ \\\\ \\\n" +
                                "#        /     \\/   \\ \\  \\___|\\ \\  \\ \\  \\ \\  \\_\\\\ \\\n" +
                                "#       /  /\\   \\    \\ \\__\\    \\ \\__\\ \\__\\ \\_______\\\n" +
                                "#      /__/ /\\ __\\    \\|__|     \\|__|\\|__|\\|_______|\n" +
                                "#     |__|/  \\|__|        by EnttbotX\n" +
                                "#                      ko-fi.com/enttbotx\n\n" +

                                "config-version: 1.4.0\n\n" +

                                "prefix: \"" + prefix.replace("\"", "\\\"") + "\"\n" +
                                "debug-prefix: \"&e&lXPAD >>&r \"\n\n" +

                                "messages:\n" +
                                "  invalid_material: \"$prefix$ &cInvalid material at $path$. &7Using default: $def$\"\n" +
                                "  invalid_particle: \"$prefix$ &cInvalid particle at $path$. &7Using default: $def$\"\n" +
                                "  invalid_sound: \"$prefix$ &cInvalid sound at $path$: $name$\"\n" +
                                "  no_permission: \"$prefix$ &4You don´t have permissions to do that!\"\n" +
                                "  reloaded: \"$prefix$ &2Has been reloaded!\"\n" +
                                "  path_not_found: \"$prefix$ &4The path $path$ does not exists! &c&lUSE:&f &e/xpad list paths &7to see available paths\"\n" +
                                "  invalid_value_format: \"$prefix$ &4Invalid value format: $value$\"\n" +
                                "  apply_edit: \"$prefix$ &aSuccessfully updated &e$path$&a to &e$value$\"\n" +
                                "  existent_pad: \"$prefix$ &4The pad $pad$ already exists\"\n" +
                                "  created_pad: \"$prefix$ &aCreated new pad: &e$pad$\"\n" +
                                "  created_template: \"$prefix$ &aCreated &e$type$&a template pad: &e$pad$\"\n" +
                                "  deleted_pad: \"$prefix$ &aDeleted pad: &e$pad$\"\n" +
                                "  pad_not_exist: \"$prefix$ &cPad &e$pad$ &cdoes not exist!\"\n" +
                                "  error_reading_pad: \"$prefix$ &cError reading pad configuration\"\n" +
                                "  source_pad_missing: \"$prefix$ &cSource pad &e$from$ &cdoes not exist!\"\n" +
                                "  dest_pad_exists: \"$prefix$ &cDestination pad &e$to$ &calready exists!\"\n" +
                                "  error_copying_pad: \"$prefix$ &cError copying pad configuration\"\n" +
                                "  copied_pad: \"$prefix$ &aCopied pad &e$from$ &ato &e$to$\"\n" +
                                "  unknown_template: \"$prefix$ &cUnknown template: &e$type$\"\n" +
                                "  unknown_list_type: \"$prefix$ &cUnknown list type: &e$type$\"\n" +
                                "  path_not_list: \"$prefix$ &cPath is not a list: &e$path$\"\n" +
                                "  added_to_list: \"$prefix$ &aAdded &e$item$ &ato list at &e$path$\"\n" +
                                "  removed_from_list: \"$prefix$ &aRemoved &e$item$ &afrom list at &e$path$\"\n" +
                                "  already_in_list: \"$prefix$ &eItem already in list: &e$item$\"\n" +
                                "  not_in_list: \"$prefix$ &eItem not found in list: &e$item$\"\n"
                );
            } catch (IOException e) {
                e.printStackTrace();
                return;
            }
        }

        Bukkit.getConsoleSender().sendMessage(MSG.color(prefix + "&2Config updated and migrated to v1.4.0"));
    }
}