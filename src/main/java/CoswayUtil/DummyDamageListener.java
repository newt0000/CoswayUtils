package CoswayUtil;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.entity.Mannequin;

public class DummyDamageListener implements Listener {

    private final DummyManager dummyManager;


    public DummyDamageListener(DummyManager dummyManager) {
        this.dummyManager = dummyManager;
    }


    @EventHandler
    public void onDamage(EntityDamageEvent event) {

        if (!(event.getEntity() instanceof Mannequin mannequin)) {
            return;
        }


        if (!dummyManager.isDummy(mannequin)) {
            return;
        }


        if (mannequin.isInvulnerable()) {

            event.setCancelled(true);
        }
    }
}