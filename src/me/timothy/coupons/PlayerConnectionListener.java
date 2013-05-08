package me.timothy.coupons;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public class PlayerConnectionListener implements Listener {
	@EventHandler(ignoreCancelled=true)
	public void onConnect(PlayerJoinEvent event) {
		Player player = event.getPlayer();
		
		CouponsConfig.applyPermissions(player);
	}
	
	@EventHandler(ignoreCancelled=true)
	public void onQuit(PlayerQuitEvent event) {
		Player player = event.getPlayer();
		
		CouponsConfig.removePermissions(player);
	}
}
