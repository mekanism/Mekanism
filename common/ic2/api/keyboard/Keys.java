package ic2.api.keyboard;

import net.minecraft.entity.player.EntityPlayer;

public class Keys {
	public static boolean isModeKeyPressed(EntityPlayer aPlayer) {
		return getKeyPressed(aPlayer, "isModeSwitchKeyDown");
	}

	public static boolean isAltKeyPressed(EntityPlayer aPlayer) {
		return getKeyPressed(aPlayer, "isAltKeyDown");
	}

	public static boolean isHudModeKeyPressed(EntityPlayer aPlayer) {
		return getKeyPressed(aPlayer, "isHudModeKeyDown");
	}

	public static boolean isBoostKeyPressed(EntityPlayer aPlayer) {
		return getKeyPressed(aPlayer, "isBoostKeyDown");
	}

	public static boolean isForwardKeyPressed(EntityPlayer aPlayer) {
		return getKeyPressed(aPlayer, "isForwardKeyDown");
	}

	public static boolean isJumpKeyPressed(EntityPlayer aPlayer) {
		return getKeyPressed(aPlayer, "isJumpKeyDown");
	}

	public static boolean isSideInventoryKeyPressed(EntityPlayer aPlayer) {
		return getKeyPressed(aPlayer, "isSideinventoryKeyDown");
	}

	public static boolean isSneakKeyPressed(EntityPlayer aPlayer) {
		return getKeyPressed(aPlayer, "isSneakKeyDown");
	}

	public static boolean getKeyPressed(EntityPlayer aPlayer, String aKeyName) {
		Object tObject = ic2.api.util.ReflectionHelper.callMethod(ic2.api.util.ReflectionHelper.getField(ic2.api.info.Info.ic2ModInstance, "keyboard", false, true), "isModeSwitchKeyDown", false, false, true, aPlayer);
		if (tObject != null && tObject instanceof Boolean) {
			return (Boolean)tObject;
		}
		return false;
	}
}