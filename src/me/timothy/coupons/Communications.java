package me.timothy.coupons;

import java.util.logging.Level;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

public class Communications {
	private static final String MESSAGES_PREFIX = "messages.";
	private static final String PLAYER_MESSAGES_PREFIX = MESSAGES_PREFIX
			+ "player.";
	public static final String DISABLED = "disabled";
	public static final String ENABLED = "enabled";
	public static final String DEFAULT_COUPONS_CONFIG_FAIL = "create-config-fail";
	public static final String COUPONS_SAVE_CONFIG_FAIL = "save-config-fail";
	public static final String INVALID_COUPON = "invalid-coupon";
	public static final String PLAYER_AND_CONSOLE_ONLY = "player-and-console-only";
	public static final String PLAYER_ONLY = "player-only";
	public static final String NOT_HOLDING_THAT_COUPON = "not-holding-that-coupon";
	public static final String VALIDATE_COUPON = "validate-coupon";
	public static final String PLAYER_NOT_FOUND = "player-not-found";
	public static final String ADD_COUPON = "add-coupon";
	public static final String RECIEVE_COUPON = "recieve-coupon";
	public static final String GIFT_COUPON = "gift-coupon";
	public static final String RECIEVE_GIFT_COUPON = "recieve-gift-coupon";
	public static final String REMOVE_COUPON = "remove-coupon";
	public static final String NO_PERMISSION = "no-permission";
	
	private static JavaPlugin plugin;

	public static void sendMessage(String configMessage, Object... other) {
		String realMessage = plugin.getConfig().getString(
				MESSAGES_PREFIX + configMessage);
		if(realMessage == null) {
			plugin.getLogger().severe("Invalid config message '" + configMessage + "' used");
			return;
		}
		realMessage = realMessage.replace("\\u00A7", "\u00A7");

		Level level = Level.INFO;
		if (realMessage.startsWith("fine")) {
			realMessage = realMessage.substring(4);
			level = Level.FINE;
		} else if (realMessage.startsWith("warn")) {
			realMessage = realMessage.substring(4);
			level = Level.WARNING;
		} else if (realMessage.startsWith("severe")) {
			realMessage = realMessage.substring(6);
			level = Level.SEVERE;
		}
		ChatColor.stripColor(realMessage);
		realMessage = replaceObjects(realMessage, other);
		plugin.getLogger().log(level, realMessage);

	}

	public static void sendMessage(String configMessage, CommandSender sender,
			Object... other) {
		String realMessage = plugin.getConfig().getString(
				PLAYER_MESSAGES_PREFIX + configMessage);
		realMessage = realMessage.replace("\\u00A7", "\u00A7");
		realMessage = replaceObjects(realMessage, other);
		sender.sendMessage(realMessage);
	}

	public static void setPlugin(JavaPlugin plugin) {
		Communications.plugin = plugin;
	}

	private static String replaceObjects(String realMessage, Object[] other) {
		for (Object o : other) {
			if (o instanceof Player) {
				realMessage = realMessage.replace("<player>",
						((Player) o).getName());
			} else if (o instanceof NamedString) {
				NamedString nn = (NamedString) o;
				realMessage = realMessage.replace("<" + nn.getName() + ">",
						nn.getValue());
			} else {
				realMessage.replace("<" + o.getClass().getSimpleName() + ">",
						o.toString());
			}
		}
		return realMessage;
	}
}
