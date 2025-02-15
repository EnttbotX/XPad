package x.Entt.XPad.Events;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.util.Vector;
import x.Entt.XPad.XP;
import x.Entt.XPad.Utils.MSG;

import java.util.*;

import static x.Entt.XPad.XP.econ;

public class Events implements Listener {
    private final XP plugin;
    private final Map<Player, Boolean> playerUsingPad = new HashMap<>();

    public Events(XP instance) {
        this.plugin = instance;
    }

    private static boolean plateWarn, blockWarn, lowSpeedWarn, highSpeedWarn, effectWarn;

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        Player player = event.getPlayer();
        if (!isPlayerInValidWorld(player)) return;

        Location loc = player.getLocation();
        Material cfgPlate = getMaterialOrDefault("launch-plate", Material.STONE_PRESSURE_PLATE, plateWarn);
        Material cfgBlock = getMaterialOrDefault("bottom-block", Material.REDSTONE_BLOCK, blockWarn);
        boolean isPlate = plugin.getConfig().getString("launch-plate", "").toLowerCase().contains("plate");

        Material bottomBlock = Objects.requireNonNull(loc.getWorld()).getBlockAt(loc).getRelative(0, isPlate ? -1 : -2, 0).getType();
        Material plate = loc.getWorld().getBlockAt(loc).getRelative(0, isPlate ? 0 : -1, 0).getType();
        int speed = getSpeed();
        Particle effect = getEffect();
        String direction = plugin.getConfig().getString("direction", "player-view").toLowerCase();

        boolean validBottomBlock = cfgBlock != null ? bottomBlock == cfgBlock : "any".equalsIgnoreCase(plugin.getConfig().getString("bottom-block"));
        if (player.hasPermission("xpad.launch") && validBottomBlock && plate == cfgPlate) {
            launchPlayer(player, speed, direction, effect);
            playerUsingPad.put(player, true);
        } else {
            playerUsingPad.put(player, false);
        }
    }

    private boolean isPlayerInValidWorld(Player player) {
        if (!plugin.getConfig().getBoolean("world-list.enabled")) return true;

        List<String> worlds = plugin.getConfig().getStringList("world-list.worlds");
        String worldName = player.getWorld().getName();
        String listMode = plugin.getConfig().getString("world-list.list-mode", "whitelist").toLowerCase();

        return listMode.equals("whitelist") == worlds.contains(worldName);
    }

    private Material getMaterialOrDefault(String configPath, Material defaultMaterial, boolean warnFlag) {
        String materialName = plugin.getConfig().getString(configPath);
        if (materialName == null) return defaultMaterial;

        Material material = Material.matchMaterial(materialName);
        if (material == null) {
            if (!warnFlag) {
                plugin.getLogger().warning("Invalid material in config: " + configPath + ". Defaulting to " + defaultMaterial);
            }
            return defaultMaterial;
        }
        return material;
    }

    private int getSpeed() {
        int speed = plugin.getConfig().getInt("speed");
        if (speed < 1) {
            if (!lowSpeedWarn) plugin.getLogger().warning("Speed below 1. Defaulting to 1.");
            return 1;
        } else if (speed > 18) {
            if (!highSpeedWarn) plugin.getLogger().warning("Speed above 18. Defaulting to 18.");
            return 18;
        }
        return speed;
    }

    private Particle getEffect() {
        String effectName = plugin.getConfig().getString("effect");
        if (effectName != null) {
            try {
                return Particle.valueOf(effectName.toUpperCase());
            } catch (IllegalArgumentException e) {
                if (!effectWarn) {
                    plugin.getLogger().warning("Invalid effect in config: " + effectName + ". No effect will be used.");
                    effectWarn = true;
                }
            }
        }
        return null;
    }

    private void launchPlayer(Player player, int speed, String direction, Particle effect) {
        Vector launchVector = direction.equals("player-view")
                ? player.getLocation().getDirection().multiply(speed)
                : new Vector(0, 1, 0).multiply(speed);

        player.setVelocity(launchVector);
        playSound(player);
        if (effect != null) player.getWorld().spawnParticle(effect, player.getLocation(), 100);
        sendActions(player);
    }

    private void playSound(Player player) {
        String soundName = plugin.getConfig().getString("sound");
        if (soundName != null) {
            try {
                player.playSound(player.getLocation(), Sound.valueOf(soundName.toUpperCase()), 1.0F, 1.0F);
            } catch (IllegalArgumentException e) {
                plugin.getLogger().warning("Invalid sound name in config: " + soundName);
            }
        }
    }

    private void sendActions(Player player) {
        if (plugin.getConfig().getBoolean("actions.send-message.enabled")) {
            List<String> messages = plugin.getConfig().getStringList("actions.send-message.message");
            if (!messages.isEmpty()) {
                player.sendMessage(MSG.color(String.join("\n", messages)));
            }
        }

        if (plugin.getConfig().getBoolean("actions.send-title.enabled")) {
            String title = plugin.getConfig().getString("actions.send-title.title", "");
            String subtitle = plugin.getConfig().getString("actions.send-title.subtitle", "");

            if (!title.isEmpty() || !subtitle.isEmpty()) {
                player.sendTitle(
                        title.replace("&", "§"),
                        subtitle.replace("&", "§"),
                        plugin.getConfig().getInt("actions.send-title.in", 10),
                        plugin.getConfig().getInt("actions.send-title.stay", 40),
                        plugin.getConfig().getInt("actions.send-title.out", 10)
                );
            }
        }

        if (plugin.getConfig().getBoolean("vault.enabled") && econ != null) {
            econ.withdrawPlayer(player, plugin.getConfig().getInt("vault.use_cost", 0));
            econ.depositPlayer(player, plugin.getConfig().getInt("vault.use_gain", 0));
        }
    }

    @EventHandler
    public void onPlayerDamage(EntityDamageEvent event) {
        if (event.getEntity() instanceof Player player
                && event.getCause() == EntityDamageEvent.DamageCause.FALL
                && playerUsingPad.getOrDefault(player, false)) {
            event.setCancelled(true);
        }
    }

    public static void resetWarnings() {
        plateWarn = blockWarn = lowSpeedWarn = highSpeedWarn = effectWarn = false;
    }
}