package de.sciencecode.nick.listeners;

import java.io.IOException;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;

import de.sciencecode.nick.main.Main;

public class OnQuit implements Listener {
	
	@EventHandler
	public void onQuit(PlayerQuitEvent e) {
		Player p = e.getPlayer();
		
		if (Main.database.contains(p.getUniqueId().toString())) {
		
		Main.database.set(p.getUniqueId().toString(), null);
		save();
		
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
