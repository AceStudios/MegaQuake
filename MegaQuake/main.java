package MegaQuake;

import java.awt.Color;
import java.io.File;
import java.util.HashMap;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Sign;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Fireball;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.util.Vector;

public class Main extends JavaPlugin implements Listener{
	public static HashMap<Player, User> user = null;
	public static HashMap<String, Arena> arenas = new HashMap<>();
	public static Plugin plug = null;
	public static File data = null;
	public Boolean checking = false;
	public HashMap<Integer, Player> thrower = new HashMap<>();
	@SuppressWarnings("deprecation")
	public void onEnable(){
		checking = false;
		for(Player p : Bukkit.getOnlinePlayers()){
			p.kickPlayer(Color.red + "Reloading!");
		}
		
		plug = getPlugin(Main.class);
		
		data = getDataFolder();
		
		user = new HashMap<>();
		
		getServer().getPluginManager().registerEvents(this, this);
		
		String[] arena = new String[]{"Quartz Palace", "Prison Quake"};
		for(String a : arena){
			arenas.put(a, new Arena(a));
		}
		if(!checking){
			getServer().getScheduler().runTaskAsynchronously(this, new Check());
			checking = true;
		}
		
	}
	
	@EventHandler
	public void signCreated(SignChangeEvent e){
		if(e.getLine(0).toLowerCase().equals("[quake]")){
			if(e.getPlayer().isOp()){
				String[] arena = new String[]{"Quartz Palace", "Prison Quak"};
				Boolean isArena = false;
				for(String check : arena){
					if(e.getLine(3).equals(check)){
						isArena = true;
					}
				}
				if(isArena){
					SignRefresh ref = new SignRefresh((Sign) e.getBlock().getState(), arenas.get(e.getLine(3)));
					ref.start();
					e.setLine(0, ChatColor.GREEN + "[Quake]");
				}
			}
		}
	}
	
	@EventHandler
	public void onSignClick(PlayerInteractEvent e){
		String[] arena = new String[]{"Quartz Palace", "Prison Quak"};
		Material block = e.getClickedBlock().getType();
		if(block.equals(Material.SIGN) || block.equals(Material.SIGN_POST)){
			e.setCancelled(true);
			Sign s = (Sign) e.getClickedBlock().getState();
			
			Boolean isArena = false;
			for(String check : arena){
				if(s.getLine(3).equals(check)){
					isArena = true;
				}
			}
			
			if(isArena && s.getLine(0).equals(ChatColor.GREEN + "OPEN")){
				Arena selected = arenas.get(s.getLine(3));
				if(selected.getPlayers().size() != 3){
					selected.addPlayer(e.getPlayer());
					e.getPlayer().sendMessage(s.getLine(3));
					user.get(e.getPlayer()).setArena(s.getLine(3));
					e.getPlayer().teleport(selected.getSpawn(0));
					user.get(e.getPlayer()).setArena(s.getLine(3));;
					if(selected.getPlayers().size() != 3){ }
					else{}
				}else{}
			}
		}
	}
	
	@SuppressWarnings("deprecation")
	@EventHandler
	public void onEntityDamageByEntity(EntityDamageByEntityEvent e){
		Entity hit = e.getEntity();
		Entity damager = e.getDamager();
		if(hit instanceof Player && damager instanceof Fireball){
			Player player = (Player) hit;
			Fireball fb = (Fireball) damager;
			Player shooter = thrower.get(fb.getEntityId());
			player.setHealth(0);
			for(Player p : arenas.get(user.get(player)).getPlayers()){
				p.sendMessage(ChatColor.BLUE + shooter.getName() + ChatColor.YELLOW + " has gibbed " + ChatColor.RED + player.getName());
			}
		}
	}
	
	@EventHandler
	public void onQuit(PlayerQuitEvent e){
		user.remove(e.getPlayer());
	}
	
	@EventHandler
	public void onJoin(PlayerJoinEvent e){
		user.put(e.getPlayer(), new User(e.getPlayer()));
	}
	
	@SuppressWarnings("deprecation")
	@EventHandler
    public void onInteract(PlayerInteractEvent e) {
		
        final Player player = e.getPlayer();
        Material hand;
        if (player.getItemInHand() == null) {
        	return;
        }
            hand = player.getItemInHand().getType();
            if (hand.equals(Material.WOOD_HOE)) {
                    if (!user.get(player).isReloading()) {
                    	Location shootLocation = e.getPlayer().getLocation();
                    	Vector directionVector = shootLocation.getDirection().normalize();
                    	double startShift = 2;
                    	Vector shootShiftVector = new Vector(directionVector.getX() * startShift, directionVector.getY() * startShift + 1.5, directionVector.getZ() * startShift);
                    	shootLocation = shootLocation.add(shootShiftVector.getX(), shootShiftVector.getY(), shootShiftVector.getZ());
                    	Fireball fireball = shootLocation.getWorld().spawn(shootLocation, Fireball.class);
                    	fireball.setVelocity(directionVector.multiply(2.0));
                    	fireball.setIsIncendiary(false);
                    	fireball.setShooter(e.getPlayer());
                    	fireball.setYield(0);
                    	thrower.put(fireball.getEntityId(), player);
                    	e.setCancelled(true);
                    	getServer().getScheduler().runTaskAsynchronously(this, new Reload(player));
                    } else {
                            player.sendMessage(ChatColor.RED + "The gun is reloading!");
                    }
            }
    }	
}
