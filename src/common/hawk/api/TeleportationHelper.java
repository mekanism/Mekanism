
package hawk.api;

import java.util.List;
import java.util.Random;
import net.minecraft.server.MinecraftServer;
import net.minecraft.src.Entity;
import net.minecraft.src.EntityPlayer;
import net.minecraft.src.EntityPlayerMP;
import net.minecraft.src.ServerConfigurationManager;
import net.minecraft.src.World;
import net.minecraftforge.common.DimensionManager;

/**
 * 
 * 
 * 
 * @author Elusivehawk
 */
public class TeleportationHelper
{
	private List<EndiumTeleporterCoords> coordsList;
	private static TeleportationHelper INSTANCE;
	
	public TeleportationHelper()
	{
		INSTANCE = this;
	}
	
	public boolean registerCoords(EndiumTeleporterCoords coords)
	{
		for (EndiumTeleporterCoords otherCoords : this.coordsList)
		{
			if (otherCoords.isEqual(coords.symA(), coords.symB(), coords.symC()))
			{
				return false;
			}
			else
			{
				continue;
			}
			
		}
		
		this.coordsList.add(coords);
		return true;
	}
	
	public boolean removeCoords(EndiumTeleporterCoords coords)
	{
		return this.coordsList.remove(coords);
	}
	
	public EndiumTeleporterCoords getCoordsFromSymbols(int sym1, int sym2, int sym3)
	{
		for (EndiumTeleporterCoords coords : this.coordsList)
		{
			if (coords.isEqual(sym1, sym2, sym3))
			{
				return coords;
			}
			
		}
		
		return null;
	}
	
	public void teleportEntity(Entity entity, EndiumTeleporterCoords coords)
	{
		if (new Random().nextInt(100) > 10)
		{
			entity.travelToTheEnd(entity.dimension);
			return;
		}
		
		if (!entity.worldObj.isRemote)
		{
			if (coords.dimension() != entity.dimension)
			{
				ServerConfigurationManager manager = MinecraftServer.getServer().getConfigurationManager();
				
				if (entity instanceof EntityPlayerMP)
				{
					manager.transferPlayerToDimension(((EntityPlayerMP)entity), coords.dimension());
					
				}
				else
				{
					manager.func_82448_a(entity, coords.dimension(), DimensionManager.getWorld(entity.dimension), DimensionManager.getWorld(coords.dimension()));
					
				}
				
			}
			
			entity.setPosition(coords.x() - 0.5, coords.y(), coords.z() - 0.5);
			
		}
		
	}
	
	public static TeleportationHelper instance()
	{
		return INSTANCE;
	}
	
}
