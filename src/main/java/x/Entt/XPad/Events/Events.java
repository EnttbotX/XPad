package x.Entt.XPad.Events;

import org.bukkit.*;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.util.Vector;

import x.Entt.XPad.Utils.FileHandler;
import x.Entt.XPad.Utils.MSGManager;
import x.Entt.XPad.Utils.Updater;
import x.Entt.XPad.Utils.MSG;
import x.Entt.XPad.XP;

import java.util.*;
import java.util.concurrent.TimeUnit;

import static x.Entt.XPad.XP.prefix;

public class Events implements Listener {
    private final XP plugin;
    private final Map<UUID, Long> padCooldowns = new HashMap<>();
    private final Set<UUID> playersOnPad = new HashSet<>();

    private static final Set<String> materialWarned = new HashSet<>();
    private static final Set<String> particleWarned = new HashSet<>();
    private static final Set<String> soundWarned = new HashSet<>();

    public Events(XP plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        FileHandler fh = plugin.getFh();
        FileConfiguration config = fh.getConfig();
        
        if (event.getFrom().getBlock().equals(Objects.requireNonNull(event.getTo()).getBlock())) {
            return;
        }

        Player player = event.getPlayer();
        if (!isPlayerInValidWorld(player)) return;

        Location loc = player.getLocation();
        ConfigurationSection padsSection = config.getConfigurationSection("pads");

        if (padsSection == null) return;

        for (String padID : padsSection.getKeys(false)) {
            String basePath = "pads." + padID;

            if (isPlayerOnPad(loc, basePath)) {
                handlePadActivation(player, basePath, padID);
                return;
            }
        }

        playersOnPad.remove(player.getUniqueId());
    }

    private boolean isPlayerOnPad(Location loc, String basePath) {
        FileHandler fh = plugin.getFh();
        FileConfiguration config = fh.getConfig();
        Material plateMat = getMaterial(basePath + ".launch-plate", "STONE_PRESSURE_PLATE");
        if (plateMat == null) {
            plugin.getLogger().warning("Invalid Jump-pad material in pad: " + basePath);
            return false;
        }

        boolean isPlate = plateMat.name().contains("_PLATE");
        Material currentPlate = loc.getBlock().getRelative(0, isPlate ? 0 : -1, 0).getType();

        String blockStr = config.getString(basePath + ".bottom-block", "REDSTONE_BLOCK");
        Material currentBottom = loc.getBlock().getRelative(0, isPlate ? -1 : -2, 0).getType();

        boolean validBottom = "ANY".equalsIgnoreCase(blockStr) ||
                currentBottom == getMaterial(basePath + ".bottom-block", "REDSTONE_BLOCK");

        return plateMat == currentPlate && validBottom;
    }

    private void handlePadActivation(Player player, String basePath, String padID) {
        FileHandler fh = plugin.getFh();
        FileConfiguration config = fh.getConfig();
        String permission = config.getString(basePath + ".permission", "xpad.use." + padID);
        if (!player.hasPermission(permission)) return;

        long currentTime = System.currentTimeMillis();
        long lastUsed = padCooldowns.getOrDefault(player.getUniqueId(), 0L);
        long cooldown = TimeUnit.SECONDS.toMillis(config.getInt(basePath + ".cooldown", 2));

        if (currentTime - lastUsed < cooldown) return;
        padCooldowns.put(player.getUniqueId(), currentTime);

        int speed = config.getInt(basePath + ".speed", 5);
        String direction = config.getString(basePath + ".direction", "player-view");

        Particle trail = config.getBoolean(basePath + ".trail.enabled", false) ?
                getParticle(basePath + ".trail.type", "FLAME") : null;
        Sound sound = getSound(basePath + ".sound", "ENTITY_FIREWORK_ROCKET_LAUNCH");

        launchPlayer(player, speed, direction, basePath);

        if (sound != null) {
            player.playSound(player.getLocation(), sound, 1.0f, 1.0f);
        }
        if (trail != null) {
            player.getWorld().spawnParticle(trail, player.getLocation(), 100);
        }

        executePadActions(player, basePath + ".actions");

        playersOnPad.add(player.getUniqueId());
    }

    private void launchPlayer(Player player, int speed, String direction, String basePath) {
        Vector velocity;
        FileHandler fh = plugin.getFh();
        FileConfiguration config = fh.getConfig();

        switch (direction.toLowerCase()) {
            case "player-view":
                velocity = player.getLocation().getDirection().normalize().multiply(speed);
                break;
            case "single-direction":
                Object vectorObj = config.get(basePath + ".vector");
                int yaw = 0;
                int pitch = 45;

                if (vectorObj instanceof String) {
                    String vectorStr = ((String)vectorObj).replace("[", "").replace("]", "");
                    String[] parts = vectorStr.split(",");
                    if (parts.length == 2) {
                        try {
                            yaw = Integer.parseInt(parts[0].trim());
                            pitch = Integer.parseInt(parts[1].trim());
                        } catch (NumberFormatException e) {
                            plugin.getLogger().warning("Invalid vector format in pad: " + basePath);
                        }
                    }
                } else if (vectorObj instanceof List) {
                    List<?> vector = (List<?>) vectorObj;
                    if (vector.size() >= 2) {
                        yaw = ((Number)vector.get(0)).intValue();
                        pitch = ((Number)vector.get(1)).intValue();
                    }
                }

                yaw = Math.max(-180, Math.min(180, yaw));
                pitch = Math.max(-90, Math.min(90, pitch));

                Location dirLoc = new Location(player.getWorld(), 0, 0, 0, yaw, pitch);
                velocity = dirLoc.getDirection().normalize().multiply(speed);
                break;
            default:
                velocity = new Vector(0, speed, 0);
        }

        player.setVelocity(velocity);
    }

    private void executePadActions(Player player, String actionsPath) {
        FileHandler fh = plugin.getFh();
        FileConfiguration config = fh.getConfig();
        ConfigurationSection actions = config.getConfigurationSection(actionsPath);
        if (actions == null) return;

        if (actions.getBoolean("send-message.enabled", false)) {
            List<String> messages = actions.getStringList("send-message.message");
            if (!messages.isEmpty()) {
                messages.forEach(msg -> player.sendMessage(MSG.color(msg)));
            }
        }

        if (actions.getBoolean("send-title.enabled", false)) {
            player.sendTitle(
                    MSG.color(actions.getString("send-title.title", "")),
                    MSG.color(actions.getString("send-title.subtitle", "")),
                    actions.getInt("send-title.in", 10),
                    actions.getInt("send-title.stay", 40),
                    actions.getInt("send-title.out", 10)
            );
        }

        if (actions.getBoolean("execute-cmd.enabled", false)) {
            actions.getStringList("execute-cmd.execute").forEach(cmd ->
                    Bukkit.dispatchCommand(
                            Bukkit.getConsoleSender(),
                            cmd.replace("%player%", player.getName())
                    )
            );
        }
    }

    private boolean isPlayerInValidWorld(Player player) {
        FileHandler fh = plugin.getFh();
        FileConfiguration config = fh.getConfig();
        if (!config.getBoolean("world-list.enabled", true)) return true;

        List<String> worlds = config.getStringList("world-list.worlds");
        String mode = config.getString("world-list.list-mode", "whitelist").toLowerCase();

        return mode.equals("whitelist") == worlds.contains(player.getWorld().getName());
    }

    @EventHandler
    public void onPlayerDamage(EntityDamageEvent event) {
        if (event.getEntity() instanceof Player player &&
                event.getCause() == EntityDamageEvent.DamageCause.FALL &&
                playersOnPad.contains(player.getUniqueId())) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void updater(PlayerJoinEvent event) {
        FileHandler fh = plugin.getFh();
        Player player = event.getPlayer();
        if (fh.getConfig().getBoolean("update-log.enabled")) {
            int spigotID = 116150;
            Updater updater = new Updater(plugin, spigotID);
            if (player.hasPermission(fh.getConfig().getString("update-log.permission", "xpad.updater"))) {
                try {
                    if (updater.isUpdateAvailable()) {
                        player.sendMessage(MSG.color(prefix + "&cThere is a new update of the plugin"));
                    }
                } catch (Exception e) {
                    player.sendMessage(MSG.color(prefix + "&4&lError searching newer versions: " + e.getMessage()));
                }
            }
        }
    }

    private Material getMaterial(String path, String def) {
        FileHandler fh = plugin.getFh();
        FileConfiguration config = fh.getConfig();
        String name = config.getString(path, def).toUpperCase();
        Material mat = Material.matchMaterial(name);
        if (mat == null && materialWarned.add(path)) {
            plugin.getLogger().warning(MSG.color(MSGManager.getMessage("1")
                    .replace("$path$", path)
                    .replace("$def$", def)
                    .replace("$prefix$", prefix)));
            return Material.matchMaterial(def);
        }
        return mat;
    }

    private Particle getParticle(String path, String def) {
        FileHandler fh = plugin.getFh();
        FileConfiguration config = fh.getConfig();
        String name = config.getString(path, def).toUpperCase();
        try {
            return Particle.valueOf(name);
        } catch (IllegalArgumentException e) {
            if (particleWarned.add(path)) {
                plugin.getLogger().warning(MSG.color(MSGManager.getMessage("2")
                        .replace("$path$", path)
                        .replace("$def$", def)
                        .replace("$prefix$", prefix)));
            }
            return null;
        }
    }

    private Sound getSound(String path, String def) {
        FileHandler fh = plugin.getFh();
        FileConfiguration config = fh.getConfig();
        String name = config.getString(path, def).toUpperCase();
        try {
            return Sound.valueOf(name);
        } catch (IllegalArgumentException e) {
            if (soundWarned.add(path)) {
                plugin.getLogger().warning(MSG.color(MSGManager.getMessage("3")
                        .replace("$path$", path)
                        .replace("$name$", name)
                        .replace("$prefix$", prefix)));
            }
            return null;
        }
    }

    public static void resetWarnings() {
        materialWarned.clear();
        particleWarned.clear();
        soundWarned.clear();
    }
}