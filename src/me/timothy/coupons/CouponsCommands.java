package me.timothy.coupons;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class CouponsCommands {

	public static void onListCommand(CommandSender sender, String[] args) {
		if(sender != Bukkit.getConsoleSender()) {
			printHolding(sender, sender.getName());
			printUsed(sender, sender.getName());
		}else {
			print(CouponsConfig.getAllCoupons(), sender, "All Coupons: ");
		}
	}

	public static void onListOtherCommand(CommandSender sender, String[] args) {
		if(args.length < 2) {
			Coupons.sendUsage(sender);
		}
		printHolding(sender, args[1]);
		printUsed(sender, args[1]);
	}

	private static void printHolding(CommandSender sender, String name) {
		List<Coupon> coupons = new ArrayList<>();
		coupons = CouponsConfig.getHoldingCoupons(name);

		print(coupons, sender, "Holding: ");
	}

	private static void print(List<Coupon> coupons, CommandSender sender, String string) {
		sender.sendMessage(string);
		synchronized(coupons) {
			for(Coupon c : coupons) {
				c.sendInfo("  ", sender);
			}
		}
	}

	private static void printUsed(CommandSender sender, String name) {
		List<Coupon> coupons = new ArrayList<>();
		coupons = CouponsConfig.getUsedCoupons(name);

		print(coupons, sender, "Used: ");
	}

	public static void onValidateCommand(CommandSender sender, String[] args) {
		if(!(sender instanceof Player)) {
			Communications.sendMessage(Communications.PLAYER_ONLY, sender, new NamedString("player", sender.getName()));
			return;
		}

		if(args.length < 2) {
			Coupons.sendUsage(sender);
			return;
		}

		Player player = (Player) sender;
		List<Coupon> holding = CouponsConfig.getHoldingCoupons(player.getName());

		Coupon coup = null;
		synchronized(holding) {
			for(Coupon c : holding) {
				if(c.name.equals(args[1])) {
					coup = c;
					break;
				}
			}
		}
		if(coup == null) {
			Communications.sendMessage(Communications.NOT_HOLDING_THAT_COUPON, sender,
					new NamedString("player", sender.getName()),
					new NamedString("coupon", args[1]));
		}

		CouponsConfig.validate(player.getName(), coup);
		Communications.sendMessage(Communications.VALIDATE_COUPON, sender,
				new NamedString("player", sender.getName()),
				new NamedString("coupon", args[1]));
		Communications.sendMessage(Communications.VALIDATE_COUPON,
				new NamedString("player", sender.getName()),
				new NamedString("coupon", args[1]));
	}

	public static void onAddCommand(CommandSender sender, String[] arguments) {
		if(arguments.length < 3) {
			Coupons.sendUsage(sender);
			return;
		}
		String recieverName = arguments[1];
		String couponName = arguments[2];
		if(!CouponsConfig.hasCoupon(couponName)) {
			Coupons.sendUsage(sender);
			return;
		}

		Player sendTo = Bukkit.getServer().getPlayer(recieverName);
		if(sendTo == null) {
			Communications.sendMessage(Communications.PLAYER_NOT_FOUND, sender,
					new NamedString("player", recieverName));
			return;
		}
		recieverName = sendTo.getName();

		CouponsConfig.add(recieverName, couponName);
		if(sender != Bukkit.getConsoleSender()) {
			Communications.sendMessage(Communications.ADD_COUPON, sender,
					new NamedString("player", sender.getName()),
					new NamedString("coupon", couponName),
					new NamedString("reciever", recieverName));
		}
		Communications.sendMessage(Communications.ADD_COUPON,
				new NamedString("player", sender.getName()),
				new NamedString("coupon", couponName),
				new NamedString("reciever", recieverName));
		Communications.sendMessage(Communications.RECIEVE_COUPON, sendTo,
				new NamedString("sender", sender.getName()),
				new NamedString("coupon", couponName),
				new NamedString("reciever", recieverName));
	}

	public static void onGiftCommand(CommandSender sender, String[] arguments) {
		if(arguments.length < 3) {
			Coupons.sendUsage(sender);
			return;
		}
		String recieverName = arguments[1];
		String couponName = arguments[2];
		if(!CouponsConfig.hasCoupon(couponName)) {
			Coupons.sendUsage(sender);
			return;
		}

		Player reciever = Bukkit.getServer().getPlayer(recieverName);
		if(reciever == null) {
			Communications.sendMessage(Communications.PLAYER_NOT_FOUND, sender,
					new NamedString("player", recieverName));
			return;
		}

		Coupon sendersCoupon = null;
		List<Coupon> holding = CouponsConfig.getHoldingCoupons(sender.getName());
		synchronized(holding) {
			for(Coupon c : holding) {
				if(c.name.equals(couponName)) {
					sendersCoupon = c;
					break;
				}
			}
		}

		if(sendersCoupon == null) {
			if(sender.hasPermission(Coupons.COUPON_GIFT_USED)) {
				List<Coupon> used = CouponsConfig.getUsedCoupons(sender.getName());
				synchronized(used) {
					for(Coupon c : used) {
						if(c.name.equals(couponName)) {
							sendersCoupon = c;
							break;
						}
					}
				}
			}

			if(sendersCoupon == null) {
				Communications.sendMessage(Communications.NOT_HOLDING_THAT_COUPON, sender,
						new NamedString("player", sender.getName()),
						new NamedString("coupon", couponName));
				return;
			}

			CouponsConfig.gift(sender.getName(), reciever.getName(), sendersCoupon.name);
			if(sender != Bukkit.getConsoleSender()) {
				Communications.sendMessage(Communications.GIFT_COUPON, sender,
						new NamedString("player", sender.getName()),
						new NamedString("coupon", couponName),
						new NamedString("reciever", reciever.getName()));
			}
			Communications.sendMessage(Communications.GIFT_COUPON,
					new NamedString("player", sender.getName()),
					new NamedString("coupon", couponName),
					new NamedString("reciever", reciever.getName()));
			Communications.sendMessage(Communications.RECIEVE_GIFT_COUPON, sender,
					new NamedString("sender", sender.getName()),
					new NamedString("coupon", couponName),
					new NamedString("reciever", reciever.getName()));
		}
	}

	public static void onRemoveCommand(CommandSender sender, String[] arguments) {
		if(arguments.length < 3) {
			Coupons.sendUsage(sender);
			return;
		}
		String loserName = arguments[1];
		String couponName = arguments[2];
		if(!CouponsConfig.hasCoupon(couponName)) {
			Coupons.sendUsage(sender);
			return;
		}

		Player sendTo = Bukkit.getServer().getPlayer(loserName);
		if(sendTo == null) {
			Communications.sendMessage(Communications.PLAYER_NOT_FOUND, sender,
					new NamedString("player", loserName));
			return;
		}
		loserName = sendTo.getName();

		CouponsConfig.removeCoupon(loserName, couponName);
		if(sender != Bukkit.getConsoleSender()) {
			Communications.sendMessage(Communications.REMOVE_COUPON, sender,
					new NamedString("remover", sender.getName()),
					new NamedString("removee", loserName),
					new NamedString("coupon", couponName));
		}
		Communications.sendMessage(Communications.REMOVE_COUPON,
				new NamedString("remover", sender.getName()),
				new NamedString("removee", loserName),
				new NamedString("coupon", couponName));
		Communications.sendMessage(Communications.REMOVE_COUPON, sendTo,
				new NamedString("remover", sender.getName()),
				new NamedString("removee", loserName),
				new NamedString("coupon", couponName));
	}
}
