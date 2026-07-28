package x.Entt.XPad.CMDs;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.stream.Stream;

import x.Entt.XPad.Utils.FileHandler;
import x.Entt.XPad.Utils.MSG;
import x.Entt.XPad.XP;
import x.Entt.XPad.Utils.Text;

import static x.Entt.XPad.XP.prefix;

public class CMD implements CommandExecutor, TabCompleter {
    private final XP plugin;

    private static final List<String> DIRECTIONS = Arrays.asList("player-view", "single-direction");
    private static final List<String> PARTICLES = Arrays.asList(
            "FLAME", "SMOKE_NORMAL", "SMOKE_LARGE", "HEART", "CLOUD", "SPELL",
            "SPELL_MOB", "SPELL_INSTANT", "SPELL_WITCH", "DRIP_WATER", "DRIP_LAVA",
            "VILLAGER_ANGRY", "VILLAGER_HAPPY", "TOWN_AURA", "NOTE", "PORTAL",
            "ENCHANTMENT_TABLE", "EXPLOSION_NORMAL", "EXPLOSION_LARGE", "EXPLOSION_HUGE",
            "FIREWORKS_SPARK", "WATER_BUBBLE", "WATER_SPLASH", "WATER_WAKE",
            "SUSPENDED", "SUSPENDED_DEPTH", "CRIT", "CRIT_MAGIC", "REDSTONE",
            "SNOWBALL", "SNOW_SHOVEL", "SLIME", "END_ROD", "DRAGON_BREATH"
    );
    private static final List<String> SOUNDS = Arrays.asList(
            "ENTITY_FIREWORK_ROCKET_LAUNCH", "ENTITY_PLAYER_LEVELUP", "ENTITY_ENDERMAN_TELEPORT",
            "ENTITY_EXPERIENCE_ORB_PICKUP", "ENTITY_GENERIC_EXPLODE", "ENTITY_WITHER_SHOOT",
            "BLOCK_ANVIL_LAND", "BLOCK_PISTON_EXTEND", "ENTITY_ARROW_SHOOT"
    );
    private static final List<String> BLOCKS = Arrays.asList(
            "STONE_PRESSURE_PLATE", "HEAVY_WEIGHTED_PRESSURE_PLATE", "LIGHT_WEIGHTED_PRESSURE_PLATE",
            "OAK_PRESSURE_PLATE", "SPRUCE_PRESSURE_PLATE", "BIRCH_PRESSURE_PLATE",
            "JUNGLE_PRESSURE_PLATE", "ACACIA_PRESSURE_PLATE", "DARK_OAK_PRESSURE_PLATE",
            "MANGROVE_PRESSURE_PLATE", "CHERRY_PRESSURE_PLATE", "BAMBOO_PRESSURE_PLATE",
            "CRIMSON_PRESSURE_PLATE", "WARPED_PRESSURE_PLATE",
            "REDSTONE_BLOCK", "EMERALD_BLOCK", "DIAMOND_BLOCK", "GOLD_BLOCK", "IRON_BLOCK",
            "COAL_BLOCK", "LAPIS_BLOCK", "QUARTZ_BLOCK", "COPPER_BLOCK", "NETHERITE_BLOCK",
            "OBSIDIAN", "CRYING_OBSIDIAN", "BEDROCK", "DEEPSLATE", "POLISHED_DEEPSLATE",
            "BLACKSTONE", "POLISHED_BLACKSTONE", "TERRACOTTA", "GLAZED_TERRACOTTA",
            "ANY"
    );

    public CMD(XP plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command cmd, @NotNull String s, String[] args) {
        if (!sender.hasPermission("xpad.op")) {
            sender.sendMessage(Text.color(MSG.getMessage("3").replace("$prefix$", prefix)));
            return true;
        }

        if (args.length == 0) {
            sendHelp(sender);
            return true;
        }

        FileHandler fh = plugin.getFh();

        switch (args[0].toLowerCase()) {
            case "reload" -> {
                fh.loadAllFiles();
                sender.sendMessage(Text.color(MSG.getMessage("4").replace("$prefix$", prefix)));
                return true;
            }

            case "edit" -> handleEditCommand(sender, args);
            case "create" -> handleCreateCommand(sender, args);
            case "delete" -> handleDeleteCommand(sender, args);
            case "list" -> handleListCommand(sender, args);
            case "info" -> handleInfoCommand(sender, args);
            case "copy" -> handleCopyCommand(sender, args);
        }

        sendHelp(sender);
        return true;
    }

    private void sendHelp(CommandSender sender) {
        sender.sendMessage(Text.color("&6&l=== " + prefix + "&6&l==="));
        sender.sendMessage(Text.color("&e/xpad reload &7- Reload configuration"));
        sender.sendMessage(Text.color("&e/xpad edit <id> <property> <value> &7- Edit pad property"));
        sender.sendMessage(Text.color("&e/xpad create <id> &7- Create new pad"));
        sender.sendMessage(Text.color("&e/xpad delete <id> &7- Delete pad"));
        sender.sendMessage(Text.color("&e/xpad copy <from> <to> &7- Copy pad configuration"));
        sender.sendMessage(Text.color("&e/xpad list &7- List all pads"));
        sender.sendMessage(Text.color("&e/xpad info <id> &7- Show pad information"));
        sender.sendMessage(Text.color("&6&l=== " + prefix + "&6&l==="));
    }

    private void handleEditCommand(CommandSender sender, String[] args) {
        FileHandler fh = plugin.getFh();
        FileConfiguration config = fh.getConfig();

        if (args.length < 3) {
            sender.sendMessage(Text.color(prefix + "&c&lUSE: &f/xpad edit <id> <property> <value>"));
            return;
        }

        String firstArg = args[1];
        String path;
        String value;
        if (isNumeric(firstArg)) {
            if (args.length < 4) {
                sender.sendMessage(Text.color(prefix + "&c&lUSE: &f/xpad edit <id> <property> <value>"));
                return;
            }

            String property = args[2];
            value = String.join(" ", Arrays.copyOfRange(args, 3, args.length));
            path = "pads." + firstArg + "." + property;

            if (!config.contains("pads." + firstArg)) {
                sender.sendMessage(Text.color(MSG.getMessage("11").replace("$prefix$", prefix).replace("$pad$", firstArg)));
                return;
            }
        } else {
            path = firstArg;
            value = String.join(" ", Arrays.copyOfRange(args, 2, args.length));
        }

        if (!config.contains(path)) {
            sender.sendMessage(Text.color(MSG.getMessage("5").replace("$prefix$", prefix).replace("$path$", path)));
            return;
        }

        fh.getConfig().set(path, value);
        fh.saveConfig();
        fh.loadAllFiles();
        sender.sendMessage(Text.color(MSG.getMessage("7").replace("$prefix$", prefix).replace("$value$", value).replace("$path$", path)));
    }

    private void handleCreateCommand(CommandSender sender, String[] args) {
        FileHandler fh = plugin.getFh();
        FileConfiguration config = fh.getConfig();

        if (args.length < 2) {
            sender.sendMessage(Text.color(prefix + "&c&lUSE: &f/xpad create <id>"));
            return;
        }

        String padId = args[1];
        String padPath = "pads." + padId;
        if (config.get(padPath) != null) {
            sender.sendMessage(Text.color(MSG.getMessage("8").replace("$prefix$", prefix).replace("$pad$", padId)));
            return;
        }

        createDefaultPad(padId);
        fh.saveConfig();
        sender.sendMessage(Text.color(MSG.getMessage("9").replace("$prefix$", prefix).replace("$pad$", padId)));
        fh.loadConfig();
    }

    private void handleDeleteCommand(CommandSender sender, String[] args) {
        FileHandler fh = plugin.getFh();
        FileConfiguration config = fh.getConfig();

        if (args.length < 2) {
            sender.sendMessage(Text.color(prefix + "&c&lUSE: &f/xpad delete <id>"));
            return;
        }

        String padId = args[1];
        String padPath = "pads." + padId;

        if (!config.contains(padPath)) {
            sender.sendMessage(Text.color(MSG.getMessage("11").replace("$prefix$", prefix).replace("$pad$", padId)));
            return;
        }

        fh.getConfig().set(padPath, null);
        fh.saveConfig();
        fh.loadConfig();
        sender.sendMessage(Text.color(MSG.getMessage("10").replace("$prefix$", prefix).replace("$pad$", padId)));
    }

    private void handleListCommand(CommandSender sender, String[] args) {
        FileHandler fh = plugin.getFh();
        FileConfiguration config = fh.getConfig();

        if (args.length == 1) {
            ConfigurationSection padsSection = config.getConfigurationSection("pads");
            if (padsSection == null || padsSection.getKeys(false).isEmpty()) {
                sender.sendMessage(Text.color(prefix + "&cNo pads configured"));
                return;
            }

            sender.sendMessage(Text.color(prefix + "&6&lConfigured Pads:"));
            for (String padId : padsSection.getKeys(false)) {
                String permission = config.getString("pads." + padId + ".permission", "none");
                int speed = config.getInt("pads." + padId + ".speed", 0);
                sender.sendMessage(Text.color("&e" + padId + " &7- Speed: &f" + speed + " &7| Permission: &f" + permission));
            }
        }

        String listType = args[1].toLowerCase();
        switch (listType) {
            case "pads" -> {
                ConfigurationSection padsSection = config.getConfigurationSection("pads");
                if (padsSection == null || padsSection.getKeys(false).isEmpty()) {
                    sender.sendMessage(Text.color(prefix + "&cNo pads configured"));
                    return;
                }

                sender.sendMessage(Text.color(prefix + "&6&lConfigured Pads:"));
                for (String padId : padsSection.getKeys(false)) {
                    String permission = config.getString("pads." + padId + ".permission", "none");
                    int speed = config.getInt("pads." + padId + ".speed", 0);
                    sender.sendMessage(Text.color("&e" + padId + " &7- Speed: &f" + speed + " &7| Permission: &f" + permission));
                }
            }
            case "worlds" -> {
                List<String> worlds = config.getStringList("world-list.worlds");
                String mode = config.getString("world-list.list-mode", "whitelist");
                sender.sendMessage(Text.color(prefix + "&6World List (&e" + mode + "&6):"));
                if (worlds.isEmpty()) sender.sendMessage(Text.color("&7No worlds configured"));
                else worlds.forEach(world -> sender.sendMessage(Text.color("&e- " + world)));
            }
            case "paths" -> {
                List<String> paths = getAllConfigPaths();
                sender.sendMessage(Text.color(prefix + "&6Available Configuration Paths:"));
                paths.stream().limit(20).forEach(path -> sender.sendMessage(Text.color("&e" + path)));
                if (paths.size() > 20) sender.sendMessage(Text.color("&7... and " + (paths.size() - 20) + " more"));
            }
            default -> {
                sender.sendMessage(Text.color(MSG.getMessage("18").replace("$prefix$", prefix).replace("$type$", listType)));
                sender.sendMessage(Text.color("&7Available: pads, paths, worlds"));
            }
        }
    }

    private void handleInfoCommand(CommandSender sender, String[] args) {
        FileHandler fh = plugin.getFh();
        FileConfiguration config = fh.getConfig();

        if (args.length < 2) {
            sender.sendMessage(Text.color(prefix + "&c&lUSE: &f/xpad info <id>"));
            return;
        }

        String padId = args[1];
        String padPath = "pads." + padId;
        if (!config.contains(padPath)) {
            sender.sendMessage(Text.color(MSG.getMessage("11").replace("$prefix$", prefix).replace("$pad$", padId)));
            return;
        }

        ConfigurationSection padSection = config.getConfigurationSection(padPath);
        if (padSection != null) {
            sender.sendMessage(Text.color(prefix + "&6&lPad Information: &e" + padId));
            sender.sendMessage(Text.color("&eSpeed: &f" + padSection.getInt("speed", 5)));
            sender.sendMessage(Text.color("&eDirection: &f" + padSection.getString("direction", "player-view")));
            sender.sendMessage(Text.color("&eLaunch Plate: &f" + padSection.getString("launch-plate", "STONE_PRESSURE_PLATE")));
            sender.sendMessage(Text.color("&eBottom Block: &f" + padSection.getString("bottom-block", "REDSTONE_BLOCK")));
            sender.sendMessage(Text.color("&eSound: &f" + padSection.getString("sound", "ENTITY_FIREWORK_ROCKET_LAUNCH")));
            sender.sendMessage(Text.color("&eCooldown: &f" + padSection.getInt("cooldown", 2) + "s"));
            sender.sendMessage(Text.color("&ePermission: &f" + padSection.getString("permission", "none")));

            if (padSection.contains("trail")) {
                boolean trailEnabled = padSection.getBoolean("trail.enabled", false);
                String trailType = padSection.getString("trail.type", "FLAME");
                sender.sendMessage(Text.color("&eTrail: &f" + (trailEnabled ? "Enabled (" + trailType + ")" : "Disabled")));
            }

            if (padSection.contains("vector")) {
                Object vectorObj = padSection.get("vector");
                if (vectorObj instanceof String) sender.sendMessage(Text.color("&eVector: &f" + vectorObj));
                else if (vectorObj instanceof List<?> vector) {
                    if (vector.size() >= 2)
                        sender.sendMessage(Text.color("&eVector: &f[" + vector.get(0) + ", " + vector.get(1) + "]"));
                    else sender.sendMessage(Text.color("&eVector: &cInvalid configuration"));
                } else sender.sendMessage(Text.color("&eVector: &cInvalid configuration"));
            }
        }
    }

    private void handleCopyCommand(CommandSender sender, String[] args) {
        FileHandler fh = plugin.getFh();
        FileConfiguration config = fh.getConfig();

        if (args.length < 3) {
            sender.sendMessage(Text.color(prefix + "&c&lUSE: &f/xpad copy <from> <to>"));
            return;
        }

        String fromId = args[1];
        String toId = args[2];
        String fromPath = "pads." + fromId;
        String toPath = "pads." + toId;

        if (!config.contains(fromPath)) {
            sender.sendMessage(Text.color(MSG.getMessage("12").replace("$prefix$", prefix).replace("$from$", fromId)));
            return;
        }

        if (config.contains(toPath)) {
            sender.sendMessage(Text.color(MSG.getMessage("13").replace("$prefix$", prefix).replace("$to$", toId)));
            return;
        }

        ConfigurationSection fromSection = config.getConfigurationSection(fromPath);
        if (fromSection != null) {
            config.createSection(toPath, fromSection.getValues(true));
            config.set(toPath + ".permission", "xpad.use." + toId);
            plugin.saveConfig();
            sender.sendMessage(Text.color(MSG.getMessage("14").replace("$prefix$", prefix).replace("$from$", fromId).replace("$to$", toId)));
        } else {
            sender.sendMessage(Text.color(MSG.getMessage("15").replace("$prefix$", prefix)));
        }
    }

    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command cmd, @NotNull String s, String[] args) {
        List<String> completions = new ArrayList<>();
        FileConfiguration config = plugin.getFh().getConfig();

        if (args.length == 1) {
            String partial = args[0].toLowerCase();
            List<String> options = Arrays.asList("reload", "edit", "create", "delete", "list", "info", "copy", "help");
            completions = options.stream().filter(opt -> opt.startsWith(partial)).toList();
        } else if (args.length == 2) {
            String subCmd = args[0].toLowerCase();
            String partial = args[1].toLowerCase();
            ConfigurationSection padsSection = config.getConfigurationSection("pads");
            Set<String> padIds = padsSection != null ? padsSection.getKeys(false) : new HashSet<>();

            switch (subCmd) {
                case "edit", "delete", "info", "copy" -> completions = padIds.stream()
                        .filter(id -> id.toLowerCase().startsWith(partial))
                        .toList();
                case "create" -> {
                    for (int i = 1; i <= 20; i++) {
                        String id = String.valueOf(i);
                        if (!padIds.contains(id) && id.startsWith(partial)) completions.add(id);
                    }
                }
                case "list" -> {
                    List<String> listTypes = Arrays.asList("pads", "paths", "worlds");
                    completions = listTypes.stream().filter(st -> st.startsWith(partial)).toList();
                }
            }
        } else if (args[0].equalsIgnoreCase("edit") && isNumeric(args[1])) {
            String padId = args[1];
            String prefix = "pads." + padId + ".";
            StringBuilder pathBuilder = new StringBuilder();
            for (int i = 2; i < args.length - 1; i++) pathBuilder.append(args[i]).append(".");
            String pathBase = pathBuilder.toString();
            String currentPartial = args[args.length - 1].toLowerCase();
            String fullPrefix = prefix + pathBase;

            List<String> suggestions = getAllConfigPaths().stream()
                    .filter(p -> p.startsWith(fullPrefix))
                    .map(p -> p.substring(fullPrefix.length()))
                    .map(p -> p.split("\\.")[0])
                    .filter(p -> p.toLowerCase().startsWith(currentPartial))
                    .distinct()
                    .sorted()
                    .toList();

            completions.addAll(suggestions);

            if (fullPrefix.endsWith("particle.") || fullPrefix.contains("trail.type")) {
                completions.addAll(PARTICLES.stream()
                        .filter(p -> p.toLowerCase().startsWith(currentPartial))
                        .toList());
            }
            if (fullPrefix.endsWith("launch-plate.") || fullPrefix.endsWith("bottom-block.")) {
                completions.addAll(BLOCKS.stream()
                        .filter(b -> b.toLowerCase().startsWith(currentPartial))
                        .toList());
            }
            if (fullPrefix.endsWith("sound.")) {
                completions.addAll(SOUNDS.stream()
                        .filter(st -> st.toLowerCase().startsWith(currentPartial))
                        .toList());
            }
            if (fullPrefix.endsWith("direction.")) {
                completions.addAll(DIRECTIONS.stream()
                        .filter(d -> d.toLowerCase().startsWith(currentPartial))
                        .toList());
            }
            if (fullPrefix.endsWith("enabled.")) {
                completions.addAll(Stream.of("true", "false")
                        .filter(st -> st.startsWith(currentPartial))
                        .toList());
            }
            if (fullPrefix.endsWith("permission.")) {
                completions.addAll(Stream.of("xpad.use")
                        .filter(st -> st.startsWith(currentPartial))
                        .toList());
            }
        } else if (args.length == 4 && args[0].equalsIgnoreCase("copy")) {
            String partial = args[3].toLowerCase();
            ConfigurationSection padsSection = config.getConfigurationSection("pads");
            Set<String> existingIds = padsSection != null ? padsSection.getKeys(false) : new HashSet<>();

            for (int i = 1; i <= 20; i++) {
                String id = String.valueOf(i);
                if (!existingIds.contains(id) && id.startsWith(partial)) completions.add(id);
            }
        }
        return completions;
    }

    private List<String> getAllConfigPaths() {
        List<String> paths = new ArrayList<>();
        FileConfiguration config = plugin.getFh().getConfig();
        collectPaths(config, "", paths);
        return paths;
    }

    private void collectPaths(ConfigurationSection section, String pathPrefix, List<String> paths) {
        for (String key : section.getKeys(false)) {
            String fullPath = pathPrefix.isEmpty() ? key : pathPrefix + "." + key;
            paths.add(fullPath);
            ConfigurationSection subsection = section.getConfigurationSection(key);
            if (subsection != null) collectPaths(subsection, fullPath, paths);
        }
    }

    private boolean isNumeric(String str) {
        try {
            Integer.parseInt(str);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    private void createDefaultPad(String padId) {
        FileConfiguration config = plugin.getFh().getConfig();
        String base = "pads." + padId + ".";
        config.set(base + "speed", 5);
        config.set(base + "direction", "player-view");
        config.set(base + "launch-plate", "STONE_PRESSURE_PLATE");
        config.set(base + "bottom-block", "REDSTONE_BLOCK");
        config.set(base + "sound", "ENTITY_FIREWORK_ROCKET_LAUNCH");
        config.set(base + "cooldown", 2);
        config.set(base + "permission", "xpad.use." + padId);
        config.set(base + "trail.enabled", false);
        config.set(base + "trail.type", "FLAME");
        config.set(base + "vector", Arrays.asList(0, 0));
    }
}