package de.sciencecode.nick.commands;

import java.io.IOException;
import java.lang.reflect.Field;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import com.mojang.authlib.GameProfile;

import de.sciencecode.nick.main.Main;
import net.minecraft.server.v1_8_R3.PacketPlayOutEntityDestroy;
import net.minecraft.server.v1_8_R3.PacketPlayOutNamedEntitySpawn;
import net.minecraft.server.v1_8_R3.PacketPlayOutPlayerInfo;
import net.minecraft.server.v1_8_R3.PacketPlayOutPlayerInfo.EnumPlayerInfoAction;

public class Nick implements CommandExecutor {
	
	private Field nameField;
	
	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		nameField = getField(GameProfile.class, "name");
		if (!(sender instanceof Player)) {
			sender.sendMessage("§eNick §8| §cDu musst ein Spieler sein!");
			return true;
		}
		Player p = (Player)sender;
		
		if (!p.hasPermission("nick.nick")) {
			p.sendMessage("§eNick §8| §cDazu hast du keine Rechte!");
			return true;
		}
		
		if (args.length != 1) {
			p.sendMessage("§eNick §8| §cSyntax: /nick [Name]");
			return true;
		}
		
		if (args[0].contains("&")) {
			p.sendMessage("§eNick §8| §cNicks unterstützen keine ColorCodes.");
			return true;
		}
		
		String name = args[0];

		if (name.length() > 16) {
			p.sendMessage("§eNick §8| §cDer Nick darf nicht länger als 16 Zeichen sein!");
			return true;
		}
		
		CraftPlayer cp = (CraftPlayer)p;
		
		Main.changeSkin(cp, args[0]);
		Main.database.set(p.getUniqueId().toString(), name);
		save();
		
		try {
			nameField.set(cp.getProfile(), name);
		} catch (IllegalArgumentException | IllegalAccessException e) {
			e.printStackTrace();
		}
		
		PacketPlayOutEntityDestroy destroy = new PacketPlayOutEntityDestroy(cp.getEntityId());
		Main.sendPacket(destroy);
		removeFromTab(cp);
		p.sendMessage("§eNick §8| §aDu bist nun genickt als §e" + args[0].replaceAll("&", "§"));
		
		new BukkitRunnable() {
			
			@Override
			public void run() {
				addToTab(cp);
				PacketPlayOutNamedEntitySpawn spawn = new PacketPlayOutNamedEntitySpawn(cp.getHandle());
				
				for (Player all : Bukkit.getOnlinePlayers()) {
					
					if (!all.getName().equals(cp.getName())) {
					
					((CraftPlayer)all).getHandle().playerConnection.sendPacket(spawn);
					
					}
				}
			}
		}.runTaskLater(Main.instance, 4);
		
		return false;
	}
	
	public void removeFromTab(CraftPlayer cp) {
		PacketPlayOutPlayerInfo tabremove = new PacketPlayOutPlayerInfo(EnumPlayerInfoAction.REMOVE_PLAYER, cp.getHandle());
		Main.sendPacket(tabremove);
	}
	
	public void addToTab(CraftPlayer cp) {
		PacketPlayOutPlayerInfo tabadd = new PacketPlayOutPlayerInfo(EnumPlayerInfoAction.ADD_PLAYER, cp.getHandle());
		Main.sendPacket(tabadd);
	}
	
	private Field getField(Class<?> clazz, String name) {
		
		try {
			Field field = clazz.getDeclaredField(name);
			field.setAccessible(true);
			return field;
		} catch (NoSuchFieldException | SecurityException e) {
			return null;
		}
		
	}
	
	  public void save()
	  {
	    try
	    {
	      Main.database.save(Main.file);
	    }
	    catch (IOException ex)
	    {
	    	ex.printStackTrace();
	    }
	  }

}
