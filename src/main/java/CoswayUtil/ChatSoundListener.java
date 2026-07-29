package CoswayUtil;

import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import io.papermc.paper.event.player.AsyncChatEvent;

public class ChatSoundListener implements Listener {

    private final CoswayUtil plugin;

    public ChatSoundListener(CoswayUtil plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onChat(AsyncChatEvent event) {

        Player sender = event.getPlayer();

        for (Player player : sender.getWorld().getPlayers()) {

            if (player == sender)
                continue;

            player.playSound(
                    player.getLocation(),
                    Sound.BLOCK_NOTE_BLOCK_CHIME,
                    1.0f,
                    1.2f
            );
        }
    }
}