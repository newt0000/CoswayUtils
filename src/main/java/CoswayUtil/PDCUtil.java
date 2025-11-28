package CoswayUtil;

import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * Utility class for working with Persistent Data Containers.
 * Call PDCUtil.init(plugin) in your onEnable() before using.
 */
public final class PDCUtil {

    private static JavaPlugin plugin;

    private PDCUtil() {
        // static-only utility
    }

    /**
     * Must be called once from your main plugin's onEnable().
     */
    public static void init(JavaPlugin pluginInstance) {
        plugin = pluginInstance;
    }

    private static NamespacedKey key(String rawKey) {
        if (plugin == null) {
            throw new IllegalStateException("PDCUtil not initialized. Call PDCUtil.init(plugin) in onEnable().");
        }
        // Lowercase to keep keys consistent
        return new NamespacedKey(plugin, rawKey.toLowerCase());
    }

    // ---------- String helpers ----------

    public static void setString(Player player, String key, String value) {
        PersistentDataContainer pdc = player.getPersistentDataContainer();
        pdc.set(key(key), PersistentDataType.STRING, value);
    }

    public static String getString(Player player, String key) {
        PersistentDataContainer pdc = player.getPersistentDataContainer();
        return pdc.get(key(key), PersistentDataType.STRING);
    }

    public static boolean hasString(Player player, String key) {
        PersistentDataContainer pdc = player.getPersistentDataContainer();
        return pdc.has(key(key), PersistentDataType.STRING);
    }

    public static void remove(Player player, String key) {
        PersistentDataContainer pdc = player.getPersistentDataContainer();
        pdc.remove(key(key));
    }

    // You can add more helpers later:
    // setInt/getInt, setDouble/getDouble, etc.

}
