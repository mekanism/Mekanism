package ic2.api;

import java.lang.reflect.Method;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;

/**
 * Allows for charging, discharging and using electric items (IElectricItem).
 *
 * The charge or remaining capacity of an item can be determined by calling charge/discharge with
 * ignoreTransferLimit and simulate set to true.
 */
public final class ElectricItem {
	/**
	 * Charge an item with a specified amount of energy
	 *
	 * @param itemStack electric item's stack
	 * @param amount amount of energy to charge in EU
	 * @param tier tier of the charging device, has to be at least as high as the item to charge
	 * @param ignoreTransferLimit ignore the transfer limit specified by getTransferLimit()
	 * @param simulate don't actually change the item, just determine the return value
	 * @return Energy transferred into the electric item
	 */
	public static int charge(ItemStack itemStack, int amount, int tier, boolean ignoreTransferLimit, boolean simulate) {
		try {
			if (ElectricItem_charge == null) ElectricItem_charge = Class.forName(getPackage() + ".core.item.ElectricItem").getMethod("charge", ItemStack.class, Integer.TYPE, Integer.TYPE, Boolean.TYPE, Boolean.TYPE);

			return (Integer) ElectricItem_charge.invoke(null, itemStack, amount, tier, ignoreTransferLimit, simulate);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * Discharge an item by a specified amount of energy
	 *
	 * @param itemStack electric item's stack
	 * @param amount amount of energy to charge in EU
	 * @param tier tier of the discharging device, has to be at least as high as the item to discharge
	 * @param ignoreTransferLimit ignore the transfer limit specified by getTransferLimit()
	 * @param simulate don't actually discharge the item, just determine the return value
	 * @return Energy retrieved from the electric item
	 */
	public static int discharge(ItemStack itemStack, int amount, int tier, boolean ignoreTransferLimit, boolean simulate) {
		try {
			if (ElectricItem_discharge == null) ElectricItem_discharge = Class.forName(getPackage() + ".core.item.ElectricItem").getMethod("discharge", ItemStack.class, Integer.TYPE, Integer.TYPE, Boolean.TYPE, Boolean.TYPE);

			return (Integer) ElectricItem_discharge.invoke(null, itemStack, amount, tier, ignoreTransferLimit, simulate);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * Determine if the specified electric item has at least a specific amount of EU.
	 * This is supposed to be used in the item code during operation, for example if you want to implement your own electric item.
	 * BatPacks are not taken into account.
	 *
	 * @param itemStack electric item's stack
	 * @param amount minimum amount of energy required
	 * @return true if there's enough energy
	 */
	public static boolean canUse(ItemStack itemStack, int amount) {
		try {
			if (ElectricItem_canUse == null) ElectricItem_canUse = Class.forName(getPackage() + ".core.item.ElectricItem").getMethod("canUse", ItemStack.class, Integer.TYPE);

			return (Boolean) ElectricItem_canUse.invoke(null, itemStack, amount);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * Try to retrieve a specific amount of energy from an Item, and if applicable, a BatPack.
	 * This is supposed to be used in the item code during operation, for example if you want to implement your own electric item.
	 *
	 * @param itemStack electric item's stack
	 * @param amount amount of energy to discharge in EU
	 * @param player player holding the item
	 * @return true if the operation succeeded
	 */
	public static boolean use(ItemStack itemStack, int amount, EntityPlayer player) {
		try {
			if (ElectricItem_use == null) ElectricItem_use = Class.forName(getPackage() + ".core.item.ElectricItem").getMethod("use", ItemStack.class, Integer.TYPE, EntityPlayer.class);

			return (Boolean) ElectricItem_use.invoke(null, itemStack, amount, player);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * Charge an item from the BatPack a player is wearing.
	 * This is supposed to be used in the item code during operation, for example if you want to implement your own electric item.
	 * use() already contains this functionality.
	 *
	 * @param itemStack electric item's stack
	 * @param player player holding the item
	 */
	public static void chargeFromArmor(ItemStack itemStack, EntityPlayer player) {
		try {
			if (ElectricItem_chargeFromArmor == null) ElectricItem_chargeFromArmor = Class.forName(getPackage() + ".core.item.ElectricItem").getMethod("chargeFromArmor", ItemStack.class, EntityPlayer.class);

			ElectricItem_chargeFromArmor.invoke(null, itemStack, player);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * Get the base IC2 package name, used internally.
	 *
	 * @return IC2 package name, if unable to be determined defaults to ic2
	 */
	private static String getPackage() {
		Package pkg = ElectricItem.class.getPackage();
		if (pkg != null) return pkg.getName().substring(0, pkg.getName().lastIndexOf('.'));
		else return "ic2";
	}

	private static Method ElectricItem_charge;
	private static Method ElectricItem_discharge;
	private static Method ElectricItem_canUse;
	private static Method ElectricItem_use;
	private static Method ElectricItem_chargeFromArmor;
}

