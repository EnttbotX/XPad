package x.Entt.XPad.Events;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

import x.Entt.XPad.Utils.MSG;
import x.Entt.XPad.Utils.Updater;
import x.Entt.XPad.XP;

import static x.Entt.XPad.XP.prefix;

public class OPEvents implements Listener {
    private final XP plugin;

    public OPEvents(XP plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();

        int spigotID = 116150;

        Updater updater = new Updater(plugin, spigotID);
        try {
            if (updater.isUpdateAvailable()) {
                if (player.hasPermission("xpad.op")) {
                    player.sendMessage(MSG.color(prefix + "&cThere is a new update of the plugin"));
                }
            }
        } catch (Exception e) {
            if (player.hasPermission("xpad.op")) {
                player.sendMessage(MSG.color(prefix + "&4&lError searching newer versions: " + e.getMessage()));
            }
        }
    }
}