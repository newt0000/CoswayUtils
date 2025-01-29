package CoswayUtil;

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
        serverMessage("aw sheit here we go again....");

    }

    @Override
    public void onDisable() {
        serverMessage("im dead, im alive but im dead....");
    }
    public boolean onCommand(@NotNull CommandSender sender, Command cmd, @NotNull String label, String[] args) {
        if (cmd.getName().equalsIgnoreCase("scale") && sender instanceof Player) {
        Player player = (Player) sender;
        if (args.length < 1) {
            return false;
            } else {
            setScale(player, Float.parseFloat(args[1]));
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
                10, // Max distance to check
                entity -> entity != player // Ignore the player themselves
        );

        if (result != null && result.getHitEntity() != null) {
            Entity entity = result.getHitEntity();

            try {
                entity.getClass().getMethod("setScale", float.class).invoke(entity, value);
            } catch (Exception e) {
                player.sendMessage("§cThis entity does not support scaling!");
                e.printStackTrace();
            }
        } else {
            player.sendMessage("§cNo entity found in sight!");
        }
    }
    public void serverMessage(String msg) {
        getServer().broadcastMessage(prefix()+msg);
    }
}
