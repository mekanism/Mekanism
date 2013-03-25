package mekanism.common;

import universalelectricity.core.vector.Vector3;
import universalelectricity.core.vector.VectorHelper;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.common.ForgeDirection;
import net.minecraftforge.event.ForgeSubscribe;
import ic2.api.energy.event.EnergyTileSourceEvent;
import ic2.api.energy.tile.IEnergyConductor;

public class IC2EnergyHandler 
{
	@ForgeSubscribe
	public void handleEnergy(EnergyTileSourceEvent event)
	{
		TileEntity tileEntity = (TileEntity)event.energyTile;
		
		for(ForgeDirection orientation : ForgeDirection.VALID_DIRECTIONS)
		{
			TileEntity tile = VectorHelper.getTileEntityFromSide(tileEntity.worldObj, new Vector3(tileEntity.xCoord, tileEntity.yCoord, tileEntity.zCoord), orientation);
			
			if(tile instanceof IEnergyConductor)
			{
				return;
			}
		}
		
		event.amount = (int)(MekanismUtils.emitEnergyFromAllSides(event.amount*Mekanism.FROM_IC2, tileEntity)*Mekanism.TO_IC2);
	}
}
