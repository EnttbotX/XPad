package x.Entt.XPad.Events;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.util.Vector;

import x.Entt.XPad.Utils.UpdateLogger;
import x.Entt.XPad.XPad;
import x.Entt.XPad.Utils.MSG;

import java.util.List;
import java.util.Objects;

import static x.Entt.XPad.XPad.econ;
import static x.Entt.XPad.XPad.prefix;

public class Events implements Listener {
    private final XPad plugin;

    public Events(XPad instance) {
        this.plugin = instance;
    }

    private static boolean plateWarn = false;
    private static boolean blockWarn = false;
    private static boolean lowSpeedWarn = false;
    private static boolean highSpeedWarn = false;
    private static boolean effectWarn = false;

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        Player player = event.getPlayer();
        Location loc = player.getLocation();
        String plateName = plugin.getConfig().getString("launch-plate");
        assert plateName != null;
        boolean isPlate = plateName.toLowerCase().contains("plate");
        Material cfgPlate = Material.matchMaterial(plateName);
        Material cfgBlock = Material.matchMaterial(Objects.requireNonNull(plugin.getConfig().getString("bottom-block")));
        Material bottomBlock = Objects.requireNonNull(loc.getWorld()).getBlockAt(loc).getRelative(0, isPlate ? -1 : -2, 0).getType();
        Material plate = loc.getWorld().getBlockAt(loc).getRelative(0, isPlate ? 0 : -1, 0).getType();
        int speed = plugin.getConfig().getInt("speed");
        String effectName = plugin.getConfig().getString("effect");
        Particle effect = null;

        if (cfgPlate == null) {
            if (!plateWarn) {
                plugin.getLogger().warning("The plate config was improperly set. Defaulting to Stone Pressure Plate.");
                plateWarn = true;
            }
            cfgPlate = Material.STONE_PRESSURE_PLATE;
        }

        if (cfgBlock == null) {
            if (!blockWarn) {
                plugin.getLogger().warning("The block config was improperly set. Defaulting to Redstone Block.");
                blockWarn = true;
            }
            cfgBlock = Material.REDSTONE_BLOCK;
        }

        if (speed < 1) {
            if (!lowSpeedWarn) {
                plugin.getLogger().warning("The speed was set to a value below 1. Defaulting to 1.");
                lowSpeedWarn = true;
            }
            speed = 1;
        } else if (speed > 18) {
            if (!highSpeedWarn) {
                plugin.getLogger().warning("The speed was set to a value above the maximum of 18. Defaulting to 18.");
                highSpeedWarn = true;
            }
            speed = 18;
        }

        if (effectName != null) {
            try {
                effect = Particle.valueOf(effectName);
            } catch (IllegalArgumentException e) {
                if (!effectWarn) {
                    plugin.getLogger().warning("The effect config was improperly set or invalid. No effect will be used.");
                    effectWarn = true;
                }
            }
        }

        if (player.hasPermission("xpad.launch") && bottomBlock == cfgBlock && plate == cfgPlate) {
            player.setVelocity(player.getLocation().getDirection().multiply(speed));
            player.setVelocity(new Vector(player.getVelocity().getX(), 1.0D, player.getVelocity().getZ()));
            String soundName = plugin.getConfig().getString("sound");
            if (soundName != null) {
                try {
                    player.playSound(player.getLocation(), Sound.valueOf(soundName), 1.0F, 1.0F);
                } catch (IllegalArgumentException e) {
                    plugin.getLogger().warning("Invalid sound name in config: " + soundName);
                }
            }

            if (effect != null) {
                player.getWorld().spawnParticle(effect, player.getLocation(), 100);
            }

            if (plugin.getConfig().getBoolean("actions.send-message.enabled")) {
                List<String> messages = plugin.getConfig().getStringList("actions.send-message.message");
                player.sendMessage(MSG.color(String.join("\n", messages)));
            }

            if (plugin.getConfig().getBoolean("actions.send-title.enabled")) {
                player.sendTitle(
                        Objects.requireNonNull(plugin.getConfig().getString("actions.send-title.title")).replace("&", "§"),
                        Objects.requireNonNull(plugin.getConfig().getString("actions.send-title.subtitle")).replace("&", "§"),
                        plugin.getConfig().getInt("actions.send-title.in"),
                        plugin.getConfig().getInt("actions.send-title.stay"),
                        plugin.getConfig().getInt("actions.send-title.out")
                );
            }

            if (plugin.getConfig().getBoolean("vault.enabled")) {
                econ.withdrawPlayer(player, plugin.getConfig().getInt("vault.use_cost"));
                econ.depositPlayer(player, plugin.getConfig().getInt("vault.use_gain"));
            }
        }
    }

    @EventHandler
    public void onPlayerDamage(EntityDamageEvent event) {
        Entity entity = event.getEntity();
        if (entity instanceof Player && entity.hasPermission("xpad.launch")) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = (Player) event.getPlayer();

        int spigotID = 116150;

        UpdateLogger updateLogger = new UpdateLogger(plugin, spigotID);
        try {
            if (updateLogger.isUpdateAvailable()) {
                player.sendMessage(MSG.color(prefix + "&cThere is a new update of the plugin"));
            }
        } catch (Exception e) {
            player.sendMessage(MSG.color(prefix + "&4&lError searching newer versions: " + e.getMessage()));
        }
    }

    public static void resetWarnings() {
        plateWarn = blockWarn = lowSpeedWarn = highSpeedWarn = effectWarn = false;
    }
}