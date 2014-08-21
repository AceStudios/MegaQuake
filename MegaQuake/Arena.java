package MegaQuake;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Scanner;
import java.util.concurrent.TimeUnit;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import com.google.common.io.Files;

public class Arena {
	protected HashMap<User, Integer> kills = new HashMap<>();
	File data = Main.data;
	Boolean open = true;
	File arena = null; 
	public Arena(String Arena){
		arena = new File(data + "/" + Arena + "/");
		arena.mkdirs();
	}
	public void addKill(User User){
		int old = kills.get(User);
		int now = old + 1;
		kills.put(User, now);
	}
	public void checkScores(){
		for(Player p : getPlayers()){
			int pKills = kills.get(Main.user.get(p));
			if(pKills == 25){
				for(Player players : getPlayers()){
					players.sendMessage(ChatColor.YELLOW + p.getDisplayName() + " has won!");
				}
			}
		}
	}
	protected void setPlayers(ArrayList<Player> players){
		File file = new File(arena + "players");
		if(file.exists()){
			file.delete();
		}
		String data = "Players:";
		for(Player player: players){
			data = data + "\n" + player.getName();
		}
	}
	public void addPlayer(Player player){
		ArrayList<Player> newPlayers = getPlayers();
		newPlayers.add(player);
		setPlayers(newPlayers);
		int players = newPlayers.size();
		if(players == 4){
			startCountDown();
		}
	}
	public void removePlayer(Player player){
		ArrayList<Player> newPlayers = getPlayers();
		newPlayers.remove(player);
		setPlayers(newPlayers);
	}
	public ArrayList<Player> getPlayers(){
		ArrayList<Player> players = new ArrayList<>();
		File file = new File(arena + "players");
		if(file.exists()){
			try{
				Scanner scanner = new Scanner(file);
				while(scanner.hasNext()){
					Player player = Bukkit.getPlayer(scanner.nextLine());
					if(player != null){
						players.add(player);
					}
				}
				scanner.close();
			}catch(IOException e){
				
			}
		}else{
			
		}
		return players;
	}
	public void startCountDown(){
		JavaPlugin.getProvidingPlugin(Main.class).getServer().getScheduler().runTaskAsynchronously(Main.plug, new Runnable() {
			public void run() {
				for(int x = 10; x != -1; x++){
					for(Player player : getPlayers()){
						player.sendMessage(ChatColor.YELLOW + "Game starting in " + x + "secound(s)!");
					}
					try{
						TimeUnit.SECONDS.sleep(1);
					}catch(InterruptedException e){ }
				}
				start();
			}
		});
	}
	public Location getSpawn(int key){
		int x = 0;
		int y = 0;
		int z = 0;
		File spawn = new File(arena + "lobby.yml");
		if(spawn.exists()){
			try{
				Scanner scanner = new Scanner(spawn);
				while(scanner.hasNext()){
					String l = scanner.nextLine();
					if(l.contains(key + "X: ")){
						x = Integer.parseInt(l.replace(key + "X: ", ""));
					}else if(l.contains(key + "Y: ")){
						y = Integer.parseInt(l.replace(key + "Y: ", ""));
					}else if(l.contains(key + "Z: ")){
						z = Integer.parseInt(l.replace(key + "Z: ", ""));
					}
				}
				scanner.close();
			}catch(IOException e){
				
			}
		}else{
			try{
				spawn.createNewFile();
				String defaultLobby = "spawns(0-3)";
				for(int count = 0; count != 4; count++){
					defaultLobby = defaultLobby + "\n" + count + "X: 0\n" + count + "Y: 0\n" + count + "Z: 0\n";
				}
				Files.write(defaultLobby.getBytes(), spawn);
			}catch(IOException e){ }
		}
		Location loc = new Location(Bukkit.getWorld("quake"), x, y, z);
		return loc;
	}
	private void start(){
		setOpen(false);
		for(int count = 0; count != getPlayers().size(); count++){
			for(Player player : getPlayers()){
				player.sendMessage(ChatColor.YELLOW + "Game has started!");
				player.teleport(getSpawn(count));
			}
		}
	}
	public void setOpen(Boolean value){
		open = value;
	}
}
