package mekanism.generators.common.tile.turbine;

import mekanism.api.Coord4D;
import mekanism.common.multiblock.TileEntityInternalMultiblock;
import mekanism.generators.common.content.turbine.SynchronizedTurbineData;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.common.util.ForgeDirection;

public class TileEntityRotationalComplex extends TileEntityInternalMultiblock
{	
	@Override
	public void setMultiblock(String id)
	{
		if(id == null && multiblockUUID != null)
		{
			SynchronizedTurbineData.clientRotationMap.remove(multiblockUUID);
		}
		
		super.setMultiblock(id);
		
		Coord4D coord = Coord4D.get(this).getFromSide(ForgeDirection.DOWN);
		TileEntity tile = coord.getTileEntity(worldObj);
		
		if(tile instanceof TileEntityTurbineRotor)
		{
			((TileEntityTurbineRotor)tile).updateRotors();
		}
	}
}
