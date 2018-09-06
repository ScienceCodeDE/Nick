package de.sciencecode.nick.main;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;

import de.sciencecode.nick.commands.Nick;
import de.sciencecode.nick.listeners.ChatListener;
import de.sciencecode.nick.listeners.OnQuit;
import de.sciencecode.nick.utils.GameProfileFetcher;
import de.sciencecode.nick.utils.UUIDFetcher;
import net.minecraft.server.v1_8_R3.Packet;
import net.minecraft.server.v1_8_R3.PacketPlayOutEntityDestroy;
import net.minecraft.server.v1_8_R3.PacketPlayOutNamedEntitySpawn;
import net.minecraft.server.v1_8_R3.PacketPlayOutPlayerInfo;
import net.minecraft.server.v1_8_R3.PacketPlayOutPlayerInfo.EnumPlayerInfoAction;

public class Main extends JavaPlugin {
	
	public static Main instance;
	public static FileConfiguration database;
	public static File file;
	
	public void onEnable() {
		
		instance = this;
		
		System.out.println("--------------------");
		System.out.println("Nick ~ Aktiviert");
		System.out.println("--------------------");

		
		registerCommands();
		registerListeners();
		setupFiles();
		
	}
	
	public void onDisable() {
		
		System.out.println("--------------------");
		System.out.println("Nick ~ Deaktiviert");
		System.out.println("--------------------");
		
	}
	
	public void registerCommands() {
		getCommand("nick").setExecutor(new Nick());
	}
	
	public void registerListeners() {
		getServer().getPluginManager().registerEvents(new ChatListener(), this);
		getServer().getPluginManager().registerEvents(new OnQuit(), this);

	}
	
	  private void setupFiles()
	  {
	    if (!Main.instance.getDataFolder().exists()) {
	      Main.instance.getDataFolder().mkdirs();
	    }
	    file = new File(Main.instance.getDataFolder(), "database.yml");
	    if (!file.exists()) {
	      try
	      {
	        file.createNewFile();
	      }
	      catch (IOException ex)
	      {
	        Bukkit.getPluginManager().disablePlugin(this);
	      }
	    }
	    database = YamlConfiguration.loadConfiguration(file);
	  }
	
	public static void changeSkin(CraftPlayer cp, String nameFromPlayer) {
		GameProfile skingp = cp.getProfile();
		
			try {
				skingp = GameProfileFetcher.fetch(UUIDFetcher.getUUID(nameFromPlayer));
			} catch (IOException e) {
				cp.sendMessage("§eSkin §8| §cKonnte Skin nicht laden.");
			}

		Collection<Property> props = skingp.getProperties().get("textures");
		cp.getProfile().getProperties().removeAll("textures");
		
		cp.getProfile().getProperties().putAll("textures", props);
		cp.sendMessage("§eSkin §8| §aDu hast nun den Skin von §e" + nameFromPlayer);
		
		PacketPlayOutEntityDestroy destroy = new PacketPlayOutEntityDestroy(cp.getEntityId());
		sendPacket(destroy);
		
		PacketPlayOutPlayerInfo tabremove = new PacketPlayOutPlayerInfo(EnumPlayerInfoAction.REMOVE_PLAYER, cp.getHandle());
		sendPacket(tabremove);	
						
		new BukkitRunnable() {
			
			@Override
			public void run() {
								
				PacketPlayOutPlayerInfo tabadd = new PacketPlayOutPlayerInfo(EnumPlayerInfoAction.ADD_PLAYER, cp.getHandle());
				sendPacket(tabadd);
			
				PacketPlayOutNamedEntitySpawn spawn = new PacketPlayOutNamedEntitySpawn(cp.getHandle());
				
				for (Player all : Bukkit.getOnlinePlayers()) {
					
					if (!all.getName().equals(cp.getName())) {
					
					((CraftPlayer)all).getHandle().playerConnection.sendPacket(spawn);
					
					}
				}
				
			}
		}.runTaskLater(Main.instance, 4);
		
		new BukkitRunnable() {
			
			@Override
			public void run() {
				if (cp.getWorld().getName().equals("world")) {
					cp.teleport(Bukkit.getWorld("world_nether").getSpawnLocation());
					cp.teleport(Bukkit.getWorld("world").getSpawnLocation());
				} else {
					cp.teleport(Bukkit.getWorld("world").getSpawnLocation());
				}
			}
		}.runTaskLater(Main.instance, 2L);
		
	}
	
	@SuppressWarnings("rawtypes")
	public static void sendPacket(Packet packet) {
		
		for (Player all : Bukkit.getOnlinePlayers()) {
			
			((CraftPlayer)all).getHandle().playerConnection.sendPacket(packet);
		}
		
	}

}
