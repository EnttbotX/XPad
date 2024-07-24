package x.Entt.XPad.Utils;

import net.md_5.bungee.api.ChatColor;

public class MSG {
    public static String color(String message) {
        return ChatColor.translateAlternateColorCodes('&', message);
    }
}