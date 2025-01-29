package CoswayUtil;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.util.RayTraceResult;
import org.bukkit.util.Vector;

import org.jetbrains.annotations.NotNull;

public final class CoswayUtil extends JavaPlugin {

    @Override
    public void onEnable() {
        serverMessage(ColorKey("&aaw sheit here we go again...."));

    }

    @Override
    public void onDisable() {
        serverMessage(ColorKey("&cim dead, im alive but im dead...."));
    }
    public boolean onCommand(@NotNull CommandSender sender, Command cmd, @NotNull String label, String[] args) {
        if (cmd.getName().equalsIgnoreCase("scale") && sender instanceof Player) {
        Player player = (Player) sender;
        if (args.length < 1) {
            return false;
            } else {
            setScale(player, Float.parseFloat(args[0]));
        }
        }
        return true;
    }

    public String prefix() {
        return "[Cosway Utility] ";
    }
    public void setScale(Player player, float value) {
        RayTraceResult result = player.getWorld().rayTraceEntities(
                player.getEyeLocation(),
                player.getEyeLocation().getDirection(),
                10,
                entity -> entity instanceof Player == false // Ignore the player
        );

        if (result != null && result.getHitEntity() != null) {
            Entity entity = result.getHitEntity();
            String entityUUID = entity.getUniqueId().toString();

            String command = "attribute " + entityUUID + " minecraft:scale base set " + value;
            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), command);

            player.sendMessage("§aSet scale of " + entity.getName() + " to " + value);
        } else {
            player.sendMessage("§cNot Looking at any entity!");
        }
    }
    public String ColorKey(String t) {
        char searchChar = '&';  // Character to search for
        char replacementChar = '§';  // Character to replace with
        StringBuilder sb = new StringBuilder(t);
        // Search for the character in the StringBuilder
        int index = sb.indexOf(String.valueOf(searchChar));

        // Replace the character if found
        for (int j = 0; j < sb.length(); j++) {
            if (sb.charAt(j) == searchChar) {
                sb.setCharAt(j, replacementChar);
            }
        }
        return sb.toString();
    }
    public void serverMessage(String msg) {
        getServer().broadcastMessage(prefix()+msg);
    }
}
