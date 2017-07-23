package ic2.api.info;

import net.minecraft.potion.Potion;
import net.minecraft.util.DamageSource;

import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.LoaderState;

public class Info {
	public static IInfoProvider itemInfo;
	public static Object ic2ModInstance;

	/**
	 * Damage Sources used by IC2.
	 * Getting assigned in preload.
	 */
	public static DamageSource DMG_ELECTRIC, DMG_NUKE_EXPLOSION, DMG_RADIATION;

	/**
	 * Potions used by IC2.
	 * Getting assigned in preload.
	 */
	public static Potion POTION_RADIATION;

	public static boolean isIc2Available() {
		if (ic2Available != null) return ic2Available;

		boolean loaded = Loader.isModLoaded(MOD_ID);

		if (Loader.instance().hasReachedState(LoaderState.CONSTRUCTING)) {
			ic2Available = loaded;
		}

		return loaded;
	}

	public static String MOD_ID = "ic2";
	private static Boolean ic2Available = null;
}