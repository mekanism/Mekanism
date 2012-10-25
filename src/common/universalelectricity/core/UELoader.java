package universalelectricity.core;

import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.ForgeSubscribe;
import net.minecraftforge.event.world.WorldEvent.Load;
import net.minecraftforge.event.world.WorldEvent.Unload;
import universalelectricity.electricity.ElectricityManager;
import universalelectricity.electricity.ElectricityManagerTicker;
import cpw.mods.fml.common.FMLLog;
import cpw.mods.fml.common.Loader;
import cpw.mods.fml.common.Side;
import cpw.mods.fml.common.registry.TickRegistry;

/**
 * A class used to load Universal Electricity and make it work.
 * @author Calclavia
 *
 */
public class UELoader
{
	public static final UELoader INSTANCE = new UELoader();

	public static boolean isInitialized = false;

	public void initiate()
	{
		if (!isInitialized)
		{
			TickRegistry.registerTickHandler(new ElectricityManagerTicker(), Side.SERVER);
			ElectricityManager.instance = new ElectricityManager();
			MinecraftForge.EVENT_BUS.register(this);

			if (UniversalElectricity.BC3_RATIO <= 0 || !Loader.isModLoaded("BuildCraft|Core"))
			{
				FMLLog.fine("Disabled Buildcraft electricity conversion!");
			}
			else
			{
				FMLLog.fine("Buildcraft conversion ratio: " + UniversalElectricity.BC3_RATIO);
			}

			if (UniversalElectricity.IC2_RATIO <= 0 || !Loader.isModLoaded("IC2"))
			{
				FMLLog.fine("Disabled Industrialcraft electricity conversion!");
			}
			else
			{
				FMLLog.fine("IC2 conversion ratio: " + UniversalElectricity.IC2_RATIO);
			}

			FMLLog.finest("Universal Electricity v" + UniversalElectricity.VERSION + " successfully loaded!");

			isInitialized = true;
		}
	}

	@ForgeSubscribe
	public void onWorldLoad(Load event)
	{
		ElectricityManagerTicker.inGameTicks = 0;
	}

	@ForgeSubscribe
	public void onWorldUnload(Unload event)
	{
		ElectricityManager.instance = new ElectricityManager();
	}
}
