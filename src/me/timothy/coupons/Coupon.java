package me.timothy.coupons;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import ru.tehkode.permissions.PermissionUser;
import ru.tehkode.permissions.bukkit.PermissionsEx;

public class Coupon {
	public String name;
	public List<String> permissions;
	
	public Coupon(String name, List<String> perms) {
		this.name = name;
		this.permissions = perms;
	}

	public void sendInfo(String indent, CommandSender sender) {
		StringBuilder res = new StringBuilder(indent).append(name).append(" - ");
		if(permissions.size() == 1) {
			res.append(permissions.get(0));
		}else if(permissions.size() > 1) {
			synchronized(permissions) {
				for(String str : permissions) {
					res.append("\n  ").append(indent).append(str);
				}
			}
		}else {
			res.append("none");
		}
		
		String[] spl = res.toString().split("\n");
		for(String str : spl) {
			sender.sendMessage(str);
		}
	}
	
	public List<String> applyPermissions(Player player) {
		List<String> applied = new ArrayList<>();
		PermissionUser permsUser = PermissionsEx.getUser(player);
		for(String perm : permissions) {
			if(!permsUser.has(perm)) {
				permsUser.addPermission(perm);
				applied.add(perm);
			}
		}
		return applied;
	}
	
	public void removePermissions(Player player, List<String> applied) {
		PermissionUser permsUser = PermissionsEx.getUser(player);
		for(String perm : permissions) {
			permsUser.removePermission(perm);
		}
	}
}
