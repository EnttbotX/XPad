package x.Entt.XPad.CMDs;

import x.Entt.XPad.XP;
import x.Entt.XPad.Events.Events;
import x.Entt.XPad.Utils.MSG;
import static x.Entt.XPad.XP.prefix;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public class CMD implements CommandExecutor, TabCompleter {
    private final XP plugin;

    public CMD(XP plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String s, String[] args) {
        //console
        if (!(sender instanceof Player)) {
            if (args.length >= 1) {
                if (args[0].equalsIgnoreCase("reload")) {
                    plugin.reloadConfig();
                    sender.sendMessage(MSG.color(prefix + "&2XPad config has been reloaded."));
                    Events.resetWarnings();
                    return true;
                }
            }

            sender.sendMessage(MSG.color(prefix + "&cConsole Commands: &f/xpad reload."));
            return true;
        }

        Player player = (Player) sender;

        if (sender.hasPermission("xpad.op")) {
            if (args.length >= 1) {
                if (args[0].equalsIgnoreCase("reload")) {
                    plugin.reloadConfig();
                    player.sendMessage(MSG.color(prefix + "&2XPad config has been reloaded."));
                    Events.resetWarnings();
                    return true;
                }
            }
        } else {
            sender.sendMessage
                    (MSG.color(prefix + "&cYou don't have permissions to use this command"));
        }

        return false;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command cmd, String s, String[] args) {
        List<String> completer = new ArrayList<>();

        if (args.length == 1) {
            String arg0 = args[0];

            String[] words = new String[]{"reload"};
            for (String search : words) {
                if (search.startsWith(arg0)) {
                    completer.add(search);
                }
            }
        }
        return completer;
    }
}