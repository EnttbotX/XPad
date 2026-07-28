package x.Entt.XPad.Utils;

import java.util.List;
import java.util.stream.Collectors;

import me.clip.placeholderapi.PlaceholderAPI;

import net.md_5.bungee.api.ChatColor;

import org.bukkit.Bukkit;

public class Text {
    public static String color(String message) {
        if (isPlaceholderAPIAvailable()) {
            message = PlaceholderAPI.setPlaceholders(null, message);
        }

        return ChatColor.translateAlternateColorCodes('&', message);
    }

    public static List<String> colorList(List<String> lines) {
        return lines.stream()
                .map(Text::color)
                .collect(Collectors.toList());
    }

    public static void log(String message) {
        Bukkit.getConsoleSender().sendMessage(color(message));
    }

    private static boolean isPlaceholderAPIAvailable() {
        try {
            Class.forName("me.clip.placeholderapi.PlaceholderAPI");
            return true;
        } catch (ClassNotFoundException e) {
            return false;
        }
    }
}