
package net.uberkat.obsidian.hawk.common;

import hawk.api.EndiumTeleporterCoords;
import hawk.api.TeleportationHelper;

import java.util.List;

import dan200.computer.api.IComputerAccess;
import net.minecraft.src.Entity;
import net.minecraftforge.common.ForgeDirection;

public class TileEntityTeleporter extends TileEntityDamagableMachine
{
	public EndiumTeleporterCoords coords;
	
	public TileEntityTeleporter()
	{
		super("hawk/Teleporter.ogg", "Teleporter", "/gui/hawk/Teleporter.png", 200, 0, 100000);
	}
	
	public boolean isReadyToTeleport()
	{
		return energyStored == MAX_ENERGY && worldObj.isBlockIndirectlyGettingPowered(xCoord, yCoord, zCoord) && coords != null;
	}
	
	public void teleportEntity(Entity entity)
	{
		TeleportationHelper.instance().teleportEntity(entity, coords);
		energyStored = 0;
	}

	public boolean canReceiveFromSide(ForgeDirection side)
	{
		return side.ordinal() == 0;
	}

	public boolean canOperate() 
	{
		return false;
	}

	public void operate() 
	{
		
	}

	public List getRecipes()
	{
		return null;
	}

	public String[] getMethodNames() 
	{
		return null;
	}

	public Object[] callMethod(IComputerAccess computer, int method, Object[] arguments) throws Exception
	{
		return null;
	}

	public void sendPacket()
	{
		//Nothing to see here
	}

	public void sendPacketWithRange()
	{
		//Nothing to see here either.
	}
}
