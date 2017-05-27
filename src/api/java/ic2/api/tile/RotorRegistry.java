package ic2.api.tile;

import static net.minecraftforge.fml.relauncher.Side.CLIENT;

import net.minecraft.tileentity.TileEntity;

import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.ModContainer;
import net.minecraftforge.fml.relauncher.SideOnly;

import ic2.api.info.Info;

/**
 * Helper class for registering {@link IRotorProvider}s to provide the default windmill renderer
 */
@SideOnly(CLIENT)
public class RotorRegistry {
	/**
	 * Method to register a tile entity with the default IC2 windmill TESH
	 * @param clazz {@link TileEntity} that implements {@link IRotorProvider}
	 * @param <T> Type checking to ensure only {@link IRotorProvider} implementing classes are registered
	 */
	public static <T extends TileEntity & IRotorProvider> void registerRotorProvider(Class<T> clazz) {
		if (INSTANCE != null) INSTANCE.registerRotorProvider(clazz);
	}

	/**
	 * Sets the internal Registry instance.
	 * ONLY IC2 CAN DO THIS!!!!!!!
	 */
	public static void setInstance(IRotorRegistry i) {
		ModContainer mc = Loader.instance().activeModContainer();
		if (mc == null || !Info.MOD_ID.equals(mc.getModId())) {
			throw new IllegalAccessError("Only IC2 can set the instance");
		} else {
			INSTANCE = i;
		}
	}

	private static IRotorRegistry INSTANCE;

	public static interface IRotorRegistry {
		public <T extends TileEntity & IRotorProvider> void registerRotorProvider(Class<T> clazz);
	}
}