package universalelectricity.prefab;

import java.util.ArrayList;
import java.util.Collection;

import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.ForgeSubscribe;
import net.minecraftforge.event.world.ChunkEvent;
import universalelectricity.core.block.INetworkConnection;

/**
 * A helper class to register chunk loading for your wires if you need them to be refreshed upon
 * chunk load. This prevents the need for your wire to be refreshed.
 * 
 * @author Calclavia, Aidancbrady
 * 
 */
public class ConductorChunkInitiate
{
	private static boolean onChunkLoadRegistered = false;

	/**
	 * Registers and initiates Universal Electricity's network loader.
	 */
	public static void register()
	{
		if (!onChunkLoadRegistered)
		{
			try
			{
				MinecraftForge.EVENT_BUS.register(new ConductorChunkInitiate());
				onChunkLoadRegistered = true;
			}
			catch (Exception e)
			{
				e.printStackTrace();
			}
		}
	}

	@ForgeSubscribe
	public void onChunkLoad(ChunkEvent.Load event)
	{
		if (event.getChunk() != null)
		{
			Collection<?> collection = new ArrayList();
			collection.addAll(event.getChunk().chunkTileEntityMap.values());

			for (Object obj : collection)
			{
				if (obj instanceof TileEntity)
				{
					TileEntity tileEntity = (TileEntity) obj;

					if (tileEntity instanceof INetworkConnection)
					{
						((INetworkConnection) tileEntity).refresh();
					}
				}
			}
		}
	}
}
