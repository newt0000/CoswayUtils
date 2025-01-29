package CoswayUtil;

import org.bukkit.plugin.java.JavaPlugin;

public final class CoswayUtil extends JavaPlugin {

    @Override
    public void onEnable() {
        serverMessage("aw sheit here we go again....");

    }

    @Override
    public void onDisable() {
        serverMessage("im dead, im alive but im dead....");
    }

    public String prefix() {
        return "[Cosway Utility] ";
    }
    public void serverMessage(String msg) {
        getServer().broadcastMessage(prefix()+msg);
    }
}
