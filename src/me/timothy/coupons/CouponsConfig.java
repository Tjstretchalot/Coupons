package me.timothy.coupons;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

public class CouponsConfig {
	private static Map<Player, Map<Coupon, List<String>>> appliedPermissions;
	private static Path saveLocation;
	private static FileConfiguration configuration;
	
	private static List<Coupon> allCoupons;
	private static JavaPlugin plugin;
	public static void onEnable(JavaPlugin plugin) {
		appliedPermissions = new HashMap<>();
		CouponsConfig.plugin = plugin;
	}

	private static void createDefaultSaveLocation() {
		try(BufferedWriter out = new BufferedWriter(new FileWriter(saveLocation.toFile()))) {
			out.write("users:");
			out.newLine();
			out.write("  Notch:");
			out.newLine();
			out.write("    used:");
			out.newLine();
			out.write("      - kick");
			out.newLine();
			out.write("    holding:");
			out.newLine();
			out.write("      - ban");
			out.newLine();
			out.newLine();
			out.write("coupons:");
			out.newLine();
			out.write("  kick:");
			out.newLine();
			out.write("    - bukkit.command.kick");
			out.newLine();
			out.write("    - essentials.kick");
			out.newLine();
			out.write("  ban:");
			out.newLine();
			out.write("    - bukkit.command.ban");
			out.newLine();
			out.write("    - bukkit.command.kick");
			out.flush();
		}catch(IOException ex) {
			Communications.sendMessage(Communications.DEFAULT_COUPONS_CONFIG_FAIL, 
					new NamedString("error", ex.getMessage()));
		}
	}
	

	public static void reload() {
		saveLocation = Paths.get("plugins/Coupons", "coupons.yml");
		if(!Files.exists(saveLocation)) {
			createDefaultSaveLocation();
		}
		configuration = YamlConfiguration.loadConfiguration(saveLocation.toFile());
		configuration = YamlConfiguration.loadConfiguration(saveLocation.toFile());
		
		allCoupons = Collections.synchronizedList(new ArrayList<Coupon>());
		
		Set<String> keys = configuration.getKeys(true); 
		synchronized(keys) {
			for(String str : keys) {
				if(!str.startsWith("coupons"))
					continue;
				if(!configuration.isList(str))
					continue;
				
				String couponName = str.substring(8); // remove coupons.
				List<String> tmp = configuration.getStringList(str);
				List<String> permissions = new ArrayList<String>();
				for(int i = 0; i < tmp.size(); i++)
					permissions.add("");
				Collections.copy(permissions, tmp);
				allCoupons.add(new Coupon(couponName, permissions));
			}
		}
	}

	public static void save() {
		try {
			if(Files.exists(saveLocation)) {
				configuration.save(saveLocation.toFile());
			}
		} catch (IOException ex) {
			Communications.sendMessage(Communications.COUPONS_SAVE_CONFIG_FAIL, 
					new NamedString("error", ex.getMessage()));
		}
	}

	public static List<Coupon> getAllCoupons() {
		return allCoupons;
	}

	public static List<Coupon> getUsedCoupons(String name) {
		final String expectedLoc = "users." + name + ".used";
		return getCoupons(name, expectedLoc);
	}
	
	public static List<Coupon> getHoldingCoupons(String name) {
		final String expectedLoc = "users." + name + ".holding";
		return getCoupons(name, expectedLoc);
	}

	private static List<Coupon> getCoupons(final String name, final String expectedLoc) {
		List<Coupon> result = new ArrayList<>();
		if(!configuration.contains(expectedLoc))
			return result;
		if(!configuration.isList(expectedLoc))
			return result;
		
		List<String> list = configuration.getStringList(expectedLoc);
		for(String str : list) {
			if(!hasCoupon(str)) {
				Communications.sendMessage(Communications.INVALID_COUPON,
						new NamedString("couponname", str));
				continue;
			}
			result.add(getCoupon(str));
		}
		return result;
	}

	public static boolean hasCoupon(String str) {
		for(Coupon c : allCoupons) {
			if(c.name.equals(str))
				return true;
		}
		return false;
	}
	
	public static Coupon getCoupon(String str) {
		for(Coupon c : allCoupons) {
			if(c.name.equals(str))
				return c;
		}
		return null;
	}

	public static void validate(String name, Coupon coup) {
		String expectedLoc = "users." + name + ".holding";
		if(!configuration.contains(expectedLoc))
			return;
		if(!configuration.isList(expectedLoc))
			return;
		List<String> vals = configuration.getStringList(expectedLoc);
		vals.remove(coup.name);
		configuration.set(expectedLoc, vals);
		expectedLoc = "users." + name + ".used";
		if(!configuration.contains(expectedLoc)) {
			List<String> tmp = new ArrayList<>();
			tmp.add(coup.name);
			configuration.set(expectedLoc, tmp);
			save();
			return;
		}
		if(!configuration.isList(expectedLoc))
			return;
		vals = configuration.getStringList(expectedLoc);
		vals.add(coup.name);
		applyPermission(plugin.getServer().getPlayer(name), coup);
		configuration.set(expectedLoc, vals);
		save();
	}

	public static void add(String name, String coup) {
		String expectedLoc = "users." + name + ".holding";
		if(!configuration.contains(expectedLoc)) {
			List<String> tmp = new ArrayList<>();
			tmp.add(coup);
			configuration.set(expectedLoc, tmp);
			save();
			return;
		}
		if(!configuration.isList(expectedLoc))
			return;
		List<String> vals = configuration.getStringList(expectedLoc);
		vals.add(coup);
		configuration.set(expectedLoc, vals);
		save();
	}

	public static void gift(String sender, String reciever, String coupon) {
		String expectedLoc = "users." + sender + ".holding";
		if(!configuration.contains(expectedLoc))
			return;
		if(!configuration.isList(expectedLoc))
			return;
		boolean usedCoup = false;
		List<String> vals = configuration.getStringList(expectedLoc);
		if(!vals.contains(coupon)) {
			expectedLoc = "users." + sender + ".used";
			if(!configuration.contains(expectedLoc))
				return;
			if(!configuration.isList(expectedLoc))
				return;
			vals = configuration.getStringList(expectedLoc);
			usedCoup = true;
		}
		if(!vals.contains(coupon)) {
			return;
		}
		if(usedCoup) {
			Player player = plugin.getServer().getPlayer(sender);
			removePermission(player, coupon);
		}
		vals.remove(coupon);
		
		configuration.set(expectedLoc, vals);
		expectedLoc = "users." + reciever + ".holding";
		if(!configuration.contains(expectedLoc)) {
			List<String> tmp = new ArrayList<>();
			tmp.add(coupon);
			configuration.set(expectedLoc, tmp);
			save();
			return;
		}
		if(!configuration.isList(expectedLoc))
			return;
		vals = configuration.getStringList(expectedLoc);
		vals.add(coupon);
		configuration.set(expectedLoc, vals);
		Coupon asCoupon = getCoupon(coupon);
		Player player = plugin.getServer().getPlayer(reciever);
		applyPermission(player, asCoupon);
		configuration.set(expectedLoc, vals);
		save();
	}
	
	public static void removeCoupon(String loserName, String coupon) {
		String expectedLoc = "users." + loserName + ".holding";
		if(!configuration.contains(expectedLoc))
			return;
		if(!configuration.isList(expectedLoc))
			return;
		boolean usedCoup = false;
		List<String> vals = configuration.getStringList(expectedLoc);
		if(!vals.contains(coupon)) {
			expectedLoc = "users." + loserName + ".used";
			if(!configuration.contains(expectedLoc))
				return;
			if(!configuration.isList(expectedLoc))
				return;
			vals = configuration.getStringList(expectedLoc);
			usedCoup = true;
		}
		if(!vals.contains(coupon)) {
			return;
		}
		if(usedCoup) {
			Player player = plugin.getServer().getPlayer(loserName);
			removePermission(player, coupon);
		}
		
		vals.remove(coupon);
		configuration.set(expectedLoc, vals);
		save();
	}

	public static void applyPermissions(Player player) {
		List<Coupon> usedCoupons = getUsedCoupons(player.getName());
		Map<Coupon, List<String>> applied = new HashMap<>();
		
		for(Coupon c : usedCoupons) {
			applied.put(c, c.applyPermissions(player));
		}
		appliedPermissions.put(player, applied);
	}
	
	public static void applyPermission(Player player, Coupon coupon) {
		Map<Coupon, List<String>> applied = appliedPermissions.get(player);
		Set<Coupon> keys = applied.keySet();
		
		if(keys.contains(coupon))
			return;
		
		applied.put(coupon, coupon.applyPermissions(player));
	}
	
	public static void removePermissions(Player player) {
		Map<Coupon, List<String>> applied = appliedPermissions.get(player);
		
		Set<Coupon> keys = applied.keySet();
		for(Coupon c : keys) {
			c.removePermissions(player, applied.get(c));
		}
		
		appliedPermissions.remove(player);
	}
	
	public static void removePermission(Player player, Coupon coupon) {
		Map<Coupon, List<String>> applied = appliedPermissions.get(player);
		Set<Coupon> keys = applied.keySet();
		
		if(!keys.contains(coupon))
			return;
		
		coupon.removePermissions(player, applied.get(coupon));
		applied.remove(coupon);
	}
	
	public static void removePermission(Player player, String coupon) {
		Map<Coupon, List<String>> applied = appliedPermissions.get(player);
		Set<Coupon> keys = applied.keySet();
		
		Coupon theCoupon = null;
		for(Coupon c : keys) {
			if(c.name.equals(coupon)) {
				theCoupon = c;
			}
		}
		
		if(theCoupon == null)
			return;
		theCoupon.removePermissions(player, applied.get(theCoupon));
		applied.remove(theCoupon);
	}
}
