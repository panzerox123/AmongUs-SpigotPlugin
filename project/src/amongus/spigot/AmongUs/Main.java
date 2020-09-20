package amongus.spigot.AmongUs;

import org.apache.commons.lang.ArrayUtils;
import org.bukkit.*;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitScheduler;

import java.net.http.WebSocket;
import java.util.Collection;
import java.util.Random;

public class Main extends JavaPlugin {
    Player imposter;
    Player crew[];
    Player all_online[];
    @Override
    public void onEnable(){
        new EventListener(this);
    }

    @Override
    public void onDisable(){
        //Shutdown
        //Plugin disable
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if(label.equalsIgnoreCase("init")){
            if(sender instanceof  Player){
                Player player = (Player) sender;
                if(player.hasPermission("init.use")){
                    Bukkit.broadcastMessage(ChatColor.BLUE + "" + ChatColor.BOLD + "Initialising Among Us . . .");
                    Location player_location = player.getLocation();
                    setBoundary(player_location.getX(), player_location.getY() ,player_location.getZ());
                    initPlayers();
                    equipPlayers();
                } else {
                    player.sendMessage("Nope. Can't use it.");
                }
            } else {
                Bukkit.broadcastMessage(ChatColor.BLUE + "" + ChatColor.BOLD + "Initialising Among Us . . .");
                setBoundary(0.0,0.0,0.0);
                initPlayers();
                equipPlayers();
            }
            return true;
        }

        return super.onCommand(sender, command, label, args);
    }

    private boolean setBoundary(double sender_x,double sender_y, double sender_z){
        World world = Bukkit.getWorld("world");
        WorldBorder border = world.getWorldBorder();
        border.setSize(200.0);
        border.setCenter(sender_x, sender_z);
        world.setSpawnLocation((int)sender_x,(int)sender_y,(int)sender_z);
        world.setDifficulty(Difficulty.PEACEFUL);
        return  true;
    }

    private boolean initPlayers(){
        final Collection<? extends Player> online = Bukkit.getOnlinePlayers();
        Player players[] = new Player[online.size()];
        int index = 0;
        for(Player player: online) {
            players[index] = player;
            index++;
        }
        this.all_online = players;
        int imposter_index = new Random().nextInt(players.length);
        //Bukkit.broadcastMessage(players.length + "" + imposter_index+ players[0].getDisplayName());
        this.imposter = players[imposter_index];
        this.crew = (Player[]) ArrayUtils.removeElement(players, imposter_index);
        for(int i = 0; i < all_online.length; i++){
            if(all_online[i] == imposter) all_online[i].sendMessage(ChatColor.RED + "You are the imposter >:) ...");
            else all_online[i].sendMessage(ChatColor.GREEN + "You are innocent :) ...");
        }
        return true;
    }

    private Boolean equipPlayers() {
        ItemStack crew_inv[] = {new ItemStack(Material.DIAMOND_AXE, 1), new ItemStack(Material.DIAMOND_PICKAXE, 1), new ItemStack(Material.DIAMOND_SHOVEL, 1)};
        for (Player player : crew) {
            player.getInventory().clear();
            player.getInventory().setContents(crew_inv);
            player.setPlayerListHeader("Innocent");
            player.setHealth(20);
            player.teleport(Bukkit.getWorld("world").getSpawnLocation());
        }
        imposter.getInventory().setContents(crew_inv);
        ItemStack super_sword = new ItemStack(Material.DIAMOND_SWORD);
        super_sword.addUnsafeEnchantment(Enchantment.DAMAGE_ALL, 1000);
        imposter.getInventory().addItem(super_sword);
        imposter.setHealth(20);
        imposter.teleport(Bukkit.getWorld("world").getSpawnLocation());
        return true;
    }
}

class EventListener implements Listener {
    Main plugin;
    Player imposter;
    EventListener(Main plugin){
        plugin.getServer().getPluginManager().registerEvents(this,plugin);
        this.plugin = plugin;
    }
    @EventHandler
    public boolean onPlayerDeath(PlayerDeathEvent event){
        this.imposter = plugin.imposter;
        Player dead_player = event.getEntity();
        event.setKeepInventory(true);
        if(dead_player != imposter){
            event.setDeathMessage(ChatColor.RED+""+ChatColor.BOLD+dead_player.getDisplayName()+" was killed...  :(");
            dead_player.setGameMode(GameMode.SPECTATOR);
            imposter.getInventory().remove(Material.DIAMOND_SWORD);
            imposter.sendMessage(ChatColor.BLUE +"Sword cooldown started...");
            BukkitScheduler cooldown = Bukkit.getServer().getScheduler();
            cooldown.scheduleSyncDelayedTask(this.plugin, new Runnable(){
                @Override
                public void run(){
                    imposter.sendMessage(ChatColor.BLUE + "Cooldown Over...");
                    ItemStack super_sword = new ItemStack(Material.DIAMOND_SWORD,1);
                    super_sword.addUnsafeEnchantment(Enchantment.DAMAGE_ALL, 1000);
                    imposter.getInventory().addItem(super_sword);
                }
            },200);
        } else {
            event.setDeathMessage(ChatColor.DARK_GREEN+""+ChatColor.BOLD+dead_player.getDisplayName()+" was the imposter... !!! :)");
        }
        return true;
    }
}
