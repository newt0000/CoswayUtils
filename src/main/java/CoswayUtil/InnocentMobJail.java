package CoswayUtil;

import net.milkbowl.vault.economy.Economy;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.command.*;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.*;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.player.*;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.util.*;

public class InnocentMobJail implements Listener, CommandExecutor {

    private final CoswayUtil plugin;
    private final Economy economy;

    private final NamespacedKey jailKey;

    private final Map<UUID, Map<Location, Material>> jailedBlocks = new HashMap<>();
    private final Map<UUID, BukkitRunnable> jailTimers = new HashMap<>();

    public InnocentMobJail(CoswayUtil plugin, Economy economy){
        this.plugin = plugin;
        this.economy = economy;
        this.jailKey = new NamespacedKey(plugin, "jailed");
    }

    private boolean isJailed(Player player){
        return player.getPersistentDataContainer()
                .getOrDefault(jailKey, PersistentDataType.BYTE, (byte)0) == 1;
    }

    private void setJailed(Player player, boolean state){
        player.getPersistentDataContainer().set(
                jailKey,
                PersistentDataType.BYTE,
                state ? (byte)1 : (byte)0
        );
    }

    @EventHandler
    public void onEntityDeath(EntityDeathEvent event){

        Player killer = event.getEntity().getKiller();
        if(killer == null)return;

        String mob = event.getEntity().getType().name().toLowerCase();

        ConfigurationSection section =
                plugin.getConfig().getConfigurationSection("innocents."+mob);

        if(section == null)return;

        double fine = section.getDouble("fine",0);
        String time = section.getString("time","30s");

        if(fine > 0)
            economy.withdrawPlayer(killer,fine);

        jailPlayer(killer,parseTime(time));

        sendMessage(
                killer,
                plugin.getConfig()
                        .getString(
                                "innocents.messages.jailed",
                                "&cYou killed an innocent %victim%! Jailed for %time%"
                        )
                        .replace("%time%",time)
                        .replace("%fine%",String.format("%.2f",fine))
                        .replace("%victim%",
                                event.getEntity().getType().name()
                                        .toLowerCase()
                                        .replace("_"," "))
        );
    }

    private void jailPlayer(Player player,long duration){

        if(isJailed(player)){
            return;
        }

        setJailed(player,true);

        UUID uuid = player.getUniqueId();

        Location center =
                player.getLocation()
                        .getBlock()
                        .getLocation();

        Map<Location,Material> changed = new HashMap<>();

        for(int x=-2;x<=2;x++){
            for(int y=0;y<=3;y++){
                for(int z=-2;z<=2;z++){

                    Block block =
                            center.clone()
                                    .add(x,y,z)
                                    .getBlock();

                    if(block.getType()==Material.AIR){

                        changed.put(
                                block.getLocation(),
                                Material.AIR
                        );

                        block.setType(Material.TINTED_GLASS);
                    }
                }
            }
        }

        jailedBlocks.put(uuid,changed);

        Location jail =
                center.clone()
                        .add(0.5,1,0.5);

        player.teleport(jail);

        applyJailEffects(player);

        BukkitRunnable timer = new BukkitRunnable(){

            @Override
            public void run(){

                if(player.isOnline() && isJailed(player))
                    releasePlayer(player);
            }
        };

        jailTimers.put(uuid,timer);

        timer.runTaskLater(plugin,duration);
    }

    private void applyJailEffects(Player player){

        if(!isJailed(player))
            return;

        player.addPotionEffect(
                new PotionEffect(
                        PotionEffectType.MINING_FATIGUE,
                        Integer.MAX_VALUE,
                        10,
                        false,
                        false,
                        false
                )
        );

        player.setVelocity(new Vector(0,0,0));
    }
    @EventHandler
    public void onMove(PlayerMoveEvent event){

        Player player=event.getPlayer();

        if(!isJailed(player))
            return;

        Location from=event.getFrom();
        Location to=event.getTo();

        if(to==null)
            return;

        if(to.getX()!=from.getX()
                ||to.getY()!=from.getY()
                ||to.getZ()!=from.getZ()){

            Location lock=from.clone();

            lock.setYaw(to.getYaw());
            lock.setPitch(to.getPitch());

            event.setTo(lock);
        }

        applyJailEffects(player);
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event){

        Player player=event.getPlayer();

        if(!isJailed(player))
            return;

        new BukkitRunnable(){

            @Override
            public void run(){

                if(isJailed(player))
                    applyJailEffects(player);

            }

        }.runTaskLater(plugin,20);
    }

    private void releasePlayer(Player player){

        UUID uuid=player.getUniqueId();

        setJailed(player,false);

        BukkitRunnable timer=jailTimers.remove(uuid);

        if(timer!=null)
            timer.cancel();

        Map<Location,Material> blocks=jailedBlocks.remove(uuid);

        if(blocks!=null){
            for(Map.Entry<Location,Material> entry:blocks.entrySet()){

                Block block=entry.getKey().getBlock();

                if(block.getType()==Material.TINTED_GLASS){
                    block.setType(entry.getValue());
                }
            }
        }

// remove any remaining jail glass roof/walls around player
        Location center=player.getLocation().getBlock().getLocation();

        for(int x=-3;x<=3;x++){
            for(int y=-1;y<=4;y++){
                for(int z=-3;z<=3;z++){

                    Block block=center.clone()
                            .add(x,y,z)
                            .getBlock();

                    if(block.getType()==Material.TINTED_GLASS){
                        block.setType(Material.AIR);
                    }
                }
            }
        }

        player.removePotionEffect(
                PotionEffectType.MINING_FATIGUE
        );

        player.setVelocity(
                new Vector(0,0,0)
        );

        Location exit=player.getLocation();

        while(exit.getBlock().getType().isSolid())
            exit.add(0,1,0);

        player.teleport(
                exit.add(0.5,0,0.5)
        );

        player.getWorld().playSound(
                player.getLocation(),
                Sound.BLOCK_CHAIN_BREAK,
                1,
                1
        );

        player.getWorld().playSound(
                player.getLocation(),
                Sound.ENTITY_PLAYER_LEVELUP,
                1,
                1
        );

        sendMessage(
                player,
                "&aYou have been released from jail."
        );
    }

    @Override
    public boolean onCommand(
            CommandSender sender,
            Command command,
            String label,
            String[] args
    ){

        if(command.getName().equalsIgnoreCase("unjail")){

            if(args.length<1){

                sender.sendMessage(
                        ChatColor.RED+
                                "/unjail <player>"
                );

                return true;
            }

            Player target=Bukkit.getPlayer(args[0]);

            if(target==null){

                sender.sendMessage(
                        ChatColor.RED+
                                "Player not found."
                );

                return true;
            }

            releasePlayer(target);

            sender.sendMessage(
                    ChatColor.GREEN+
                            "Released "+
                            target.getName()
            );

            return true;
        }


        if(command.getName().equalsIgnoreCase("jail")){

            if(args.length<3){

                sender.sendMessage(
                        ChatColor.RED+
                                "/jail <player> <fine> <time>"
                );

                return true;
            }

            Player target=Bukkit.getPlayer(args[0]);

            if(target==null){

                sender.sendMessage(
                        ChatColor.RED+
                                "Player not found."
                );

                return true;
            }

            double fine=Double.parseDouble(args[1]);

            if(fine>0)
                economy.withdrawPlayer(target,fine);

            jailPlayer(
                    target,
                    parseTime(args[2])
            );

            sender.sendMessage(
                    ChatColor.GREEN+
                            "Jailed "+
                            target.getName()
            );

            return true;
        }

        return false;
    }
    private void sendMessage(Player player,String msg){

        player.sendMessage(
                ChatColor.translateAlternateColorCodes(
                        '&',
                        msg
                )
        );
    }

    private long parseTime(String input){

        long total=0;
        String number="";

        for(char c:input.toCharArray()){

            if(Character.isDigit(c)){

                number+=c;

            }else if(!number.isEmpty()){

                long amount=Long.parseLong(number);

                switch(c){

                    case 'd':
                        total+=amount*20L*60*60*24;
                        break;

                    case 'h':
                        total+=amount*20L*60*60;
                        break;

                    case 'm':
                        total+=amount*20L*60;
                        break;

                    case 's':
                        total+=amount*20L;
                        break;
                }

                number="";
            }
        }

        return total;
    }
}