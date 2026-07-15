package CoswayUtil;

import net.milkbowl.vault.economy.Economy;
import org.bukkit.ChatColor;
import org.bukkit.attribute.Attribute;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class EconomyRewards implements Listener {

    private final CoswayUtil plugin;
    private final Economy economy;

    private final Map<Player, EntityDamageByEntityEvent> damageCache = new HashMap<>();

    private final DecimalFormat formatter = new DecimalFormat("#.##");


    /*
     Reward message buffer
     */
    private final Map<Player, Map<String, RewardBuffer>> rewardBuffers = new HashMap<>();


    private static class RewardBuffer {

        String entity;
        int count;
        double amount;

        RewardBuffer(String entity) {
            this.entity = entity;
        }
    }



    public EconomyRewards(CoswayUtil plugin) {
        this.plugin = plugin;
        this.economy = plugin.getEconomy();
    }



    @EventHandler
    public void onEntityDamage(EntityDamageByEntityEvent event) {

        if (!(event.getDamager() instanceof Player player)) {
            return;
        }

        damageCache.put(player, event);
    }




    @EventHandler
    public void onEntityDeath(EntityDeathEvent event) {

        Player player = event.getEntity().getKiller();

        if (player == null) {
            return;
        }

        if (isBlacklisted(event.getEntity())) {
            return;
        }


        double reward = calculateReward(player, event.getEntity());


        if (reward <= 0) {
            return;
        }


        economy.depositPlayer(player, reward);



        FileConfiguration config = plugin.getConfig();



        if (config.getBoolean(
                "economy-rewards.messages.enabled",
                true
        )) {


            addRewardMessage(
                    player,
                    getEntityDisplayName(event.getEntity()),
                    event.getEntity().getName(),
                    reward
            );
        }



        damageCache.remove(player);
    }






    private void addRewardMessage(
            Player player,
            String entity,
            String customEntity,
            double reward
    ) {


        rewardBuffers.computeIfAbsent(
                player,
                p -> new HashMap<>()
        );


        Map<String, RewardBuffer> playerBuffer =
                rewardBuffers.get(player);



        RewardBuffer buffer =
                playerBuffer.computeIfAbsent(
                        entity,
                        RewardBuffer::new
                );



        buffer.count++;
        buffer.amount += reward;



        long delay =
                plugin.getConfig()
                        .getLong(
                                "economy-rewards.messages.buffer-time",
                                40
                        );



        new BukkitRunnable() {

            @Override
            public void run() {


                RewardBuffer send =
                        playerBuffer.remove(entity);



                if (send == null) {
                    return;
                }



                String message =
                        plugin.getConfig()
                                .getString(
                                        "economy-rewards.messages.reward",
                                        "&aRewarded $%amount% for killing %count% %entity%"
                                );



                message =
                        message
                                .replace(
                                        "%amount%",
                                        formatter.format(send.amount)
                                )
                                .replace(
                                        "%count%",
                                        String.valueOf(send.count)
                                )
                                .replace(
                                        "%entity%",
                                        send.entity
                                )
                                .replace(
                                        "%customentity%",
                                        customEntity
                                );



                player.sendMessage(
                        ChatColor.translateAlternateColorCodes(
                                '&',
                                message
                        )
                );

            }

        }.runTaskLater(
                plugin,
                delay
        );
    }






    private String getEntityDisplayName(LivingEntity entity) {

        String name = entity.getType().name()
                .toLowerCase()
                .replace("_", " ");


        String[] words = name.split(" ");


        StringBuilder formatted = new StringBuilder();


        for (String word : words) {

            if (!word.isEmpty()) {

                formatted.append(
                        Character.toUpperCase(
                                word.charAt(0)
                        )
                );

                formatted.append(
                        word.substring(1)
                );

                formatted.append(" ");
            }
        }


        return formatted.toString().trim();
    }






    private double calculateReward(Player player, LivingEntity entity) {

        FileConfiguration config = plugin.getConfig();


        double hearts =
                getMaxHealth(entity) / 2.0;


        double base =
                config.getDouble(
                        "economy-rewards.base.reward-per-heart",
                        2.5
                );


        double reward =
                hearts * base;


        double totalIncrease = 0;



        EntityDamageByEntityEvent damageEvent =
                damageCache.get(player);



        if (damageEvent != null &&
                config.getBoolean(
                        "economy-rewards.modifiers.damage.enabled",
                        true
                )) {


            double damageHearts =
                    damageEvent.getFinalDamage() / 2;



            double percent =
                    config.getDouble(
                            "economy-rewards.modifiers.damage.percent-per-heart",
                            2
                    );



            damageHearts =
                    Math.min(
                            damageHearts,
                            hearts * 2
                    );


            totalIncrease +=
                    damageHearts * percent;
        }




        if (config.getBoolean(
                "economy-rewards.modifiers.experience.enabled",
                true
        )) {


            totalIncrease +=
                    player.getLevel()
                            *
                            config.getDouble(
                                    "economy-rewards.modifiers.experience.percent-per-level",
                                    1
                            );
        }




        if (config.getBoolean(
                "economy-rewards.modifiers.critical-hit.enabled",
                true
        )) {


            if (player.getFallDistance() > 0) {

                totalIncrease +=
                        config.getDouble(
                                "economy-rewards.modifiers.critical-hit.percent",
                                10
                        );
            }
        }




        if (damageEvent != null &&
                damageEvent.getDamager() instanceof Projectile) {


            totalIncrease +=
                    config.getDouble(
                            "economy-rewards.modifiers.projectile.percent",
                            5
                    );
        }




        ItemStack weapon =
                player.getInventory().getItemInMainHand();



        if (weapon.containsEnchantment(Enchantment.LOOTING)) {


            int level =
                    weapon.getEnchantmentLevel(
                            Enchantment.LOOTING
                    );


            totalIncrease +=
                    level *
                            config.getDouble(
                                    "economy-rewards.modifiers.looting.percent-per-level",
                                    2
                            );
        }




        if (weapon.containsEnchantment(
                Enchantment.FIRE_ASPECT
        )) {


            totalIncrease +=
                    config.getDouble(
                            "economy-rewards.modifiers.fire-aspect.percent",
                            2
                    );
        }




        if (entity.getCustomName() != null &&
                config.getBoolean(
                        "economy-rewards.modifiers.named-mobs.enabled",
                        true
                )) {


            totalIncrease +=
                    config.getDouble(
                            "economy-rewards.modifiers.named-mobs.percent",
                            20
                    );
        }




        if (entity instanceof Ageable ageable &&
                !ageable.isAdult()) {


            totalIncrease +=
                    config.getDouble(
                            "economy-rewards.modifiers.baby-mobs.percent",
                            15
                    );
        }




        return reward *
                (1 + totalIncrease / 100);
    }





    private double getMaxHealth(LivingEntity entity) {

        if (entity.getAttribute(Attribute.MAX_HEALTH) == null) {
            return 2;
        }

        return entity.getAttribute(Attribute.MAX_HEALTH)
                .getValue();
    }





    private boolean isBlacklisted(LivingEntity entity) {

        List<String> blacklist =
                plugin.getConfig()
                        .getStringList(
                                "economy-rewards.blacklist"
                        );


        return blacklist.contains(
                entity.getType().name()
        );
    }
}