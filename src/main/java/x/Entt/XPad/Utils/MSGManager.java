package x.Entt.XPad.Utils;

import x.Entt.XPad.XP;
import java.util.Map;
import java.util.Set;

public class MSGManager {
    private static XP plugin;
    private static FileHandler fileHandler;

    private static final Map<String, String> idAliases = Map.ofEntries(
            Map.entry("0", "invalid_material"),
            Map.entry("1", "invalid_particle"),
            Map.entry("2", "invalid_sound"),
            Map.entry("3", "no_permission"),
            Map.entry("4", "reloaded"),
            Map.entry("5", "path_not_found"),
            Map.entry("6", "invalid_value_format"),
            Map.entry("7", "apply_edit"),
            Map.entry("8", "existent_pad"),
            Map.entry("9", "created_pad"),
            Map.entry("10", "deleted_pad"),
            Map.entry("11", "pad_not_exist"),
            Map.entry("12", "source_pad_missing"),
            Map.entry("13", "dest_pad_exists"),
            Map.entry("14", "copied_pad"),
            Map.entry("15", "error_copying_pad"),
            Map.entry("16", "created_template"),
            Map.entry("17", "unknown_template"),
            Map.entry("18", "unknown_list_type"),
            Map.entry("19", "path_not_list"),
            Map.entry("20", "added_to_list"),
            Map.entry("21", "removed_from_list"),
            Map.entry("22", "already_in_list"),
            Map.entry("23", "not_in_list")
    );

    private static final Set<String> validIds = Set.copyOf(idAliases.values());

    public static void init(XP pluginInstance) {
        plugin = pluginInstance;
        fileHandler = plugin.getFh();
    }

    public static String getMessage(String id) {
        if (plugin == null || fileHandler == null) {
            return "Plugin not initialized properly";
        }

        String resolvedId = idAliases.getOrDefault(id, id);
        if (!validIds.contains(resolvedId)) {
            return "Message ID not found: " + id;
        }

        String message = fileHandler.getMessages().getString("messages." + resolvedId);
        return message != null ? message : "Missing message: " + resolvedId;
    }
}