package de.sciencecode.nick.listeners;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

import de.sciencecode.nick.main.Main;

public class ChatListener implements Listener {
	
	@EventHandler
	public void onChat(AsyncPlayerChatEvent e) {
		Player p = e.getPlayer();
		
		if (Main.database.contains(p.getUniqueId().toString())) {
			e.setFormat("<" + Main.database.getString(p.getUniqueId().toString()) + "> " + e.getMessage());
		}

	}

}
