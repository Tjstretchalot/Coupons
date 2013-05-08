package me.timothy.coupons;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.java.JavaPlugin;

public final class Coupons extends JavaPlugin {
	public static final String COUPON_LIST = "coupon.list";
	public static final String COUPON_LIST_OTHER = "coupon.listother";
	public static final String COUPON_VALIDATE = "coupon.validate";
	public static final String COUPON_ADD = "coupon.add";
	public static final String COUPON_GIFT = "coupon.gift";
	public static final String COUPON_GIFT_USED = "coupon.giftused";
	private static final String COUPON_REMOVE = "coupon.remove";

	@Override
	public void onEnable() {
		Communications.setPlugin(this);
		File couponsFolder = new File("plugins/Coupons");
		if(!couponsFolder.exists()) {
			couponsFolder.mkdirs();
		}
		CouponsConfig.onEnable(this);
		getServer().getPluginManager().registerEvents(new PlayerConnectionListener(), this);
		Communications.sendMessage(Communications.ENABLED);
		saveConfig();
	}

	@Override
	public void onDisable() {
		Communications.sendMessage(Communications.DISABLED);
	}

	@Override
	public boolean onCommand(CommandSender sender, Command command,
			String label, String[] args) {
		if(command.getName().equals("coupon")) {
			if(args.length < 1) {
				sendUsage(sender);
				return true;
			}
			
			if(args[0].equals("list") && args.length > 1) {
				args[0] = "listother";
			}

			switch(args[0]) {
			case "list":
				if(!sender.hasPermission(COUPON_LIST)) {
					Communications.sendMessage(Communications.NO_PERMISSION, sender);
					return true;
				}
				CouponsCommands.onListCommand(sender, args);
				break;
			case "listother":
				if(!sender.hasPermission(COUPON_LIST_OTHER)) {
					Communications.sendMessage(Communications.NO_PERMISSION, sender);
					return true;
				}
				CouponsCommands.onListOtherCommand(sender, args);
				break;
			case "validate":
				if(!sender.hasPermission(COUPON_VALIDATE)) {
					Communications.sendMessage(Communications.NO_PERMISSION, sender);
					return true;
				}
				CouponsCommands.onValidateCommand(sender, args);
				break;
			case "add":
				if(!sender.hasPermission(COUPON_ADD)) {
					Communications.sendMessage(Communications.NO_PERMISSION, sender);
					return true;
				}
				CouponsCommands.onAddCommand(sender, args);
				break;
			case "gift":
				if(!sender.hasPermission(COUPON_GIFT)) {
					Communications.sendMessage(Communications.NO_PERMISSION, sender);
					return true;
				}
				CouponsCommands.onGiftCommand(sender, args);
				break;
			case "remove":
				if(!sender.hasPermission(COUPON_REMOVE)) {
					Communications.sendMessage(Communications.NO_PERMISSION, sender);
					return true;
				}
				CouponsCommands.onRemoveCommand(sender, args);
				break;
			default:
				sendUsage(sender);
				break;
			}
			return true;
		}
		return false;
	}

	public static void sendUsage(CommandSender sender) {
		boolean b = false;
		if(sender.hasPermission(COUPON_LIST)) {
			sender.sendMessage("/coupon list (to view holding/owned coupons)");
			b = true;
		}
		if(sender.hasPermission(COUPON_LIST_OTHER)) {
			sender.sendMessage("/coupon list [name] (to view holding/owned coupons of a specific player)");
			b = true;
		}
		if(sender.hasPermission(COUPON_VALIDATE)) {
			sender.sendMessage("/coupon validate [coupon]");
			b = true;
		}
		if(sender.hasPermission(COUPON_ADD)) {
			sender.sendMessage("/coupon add [playername] [coupon] (the command for admins to hand out a coupon)");
			b = true;
		}
		if(sender.hasPermission(COUPON_GIFT)) {
			sender.sendMessage("/coupon gift [playername] [coupon] (to give away an owned coupon as a gift)");
			b = true;
		}
		if(sender.hasPermission(COUPON_REMOVE)) {
			sender.sendMessage("/coupon remove [playername] [coupon] (to remove a used/held coupon)");
			b = true;
		}
		if(!b) {
			sender.sendMessage(ChatColor.RED + "You have no access to this plugin");
		}
	}

	@Override
	public void reloadConfig() {
		super.reloadConfig();

		CouponsConfig.reload();
	}

	@Override
	public void saveConfig() {
		if(Files.exists(Paths.get("plugins/Coupons", "config.yml"))) {
			super.saveConfig();
		}else {
			try(InputStream inStream = getResource("config.yml")) {
				Files.copy(inStream, Paths.get("plugins/Coupons", "config.yml"));
				
			}catch(IOException ex) {
				Communications.sendMessage(Communications.COUPONS_SAVE_CONFIG_FAIL, 
						new NamedString("error", ex.getMessage()));
			}

		}

		CouponsConfig.save();
	}


}
