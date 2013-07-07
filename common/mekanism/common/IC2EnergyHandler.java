package mekanism.common;

import ic2.api.energy.event.EnergyTileSourceEvent;
import ic2.api.energy.tile.IEnergySource;

import java.util.ArrayList;

import mekanism.api.Object3D;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.common.ForgeDirection;
import net.minecraftforge.event.ForgeSubscribe;

/**
 * This here is the core implementation of IC2 into Universal Cable.  Thanks to Player's hard work at making the EnergyNet
 * an event-based system, this is possible.
 * @author AidanBrady
 *
 */
public class IC2EnergyHandler 
{
	@ForgeSubscribe
	public void handleEnergy(EnergyTileSourceEvent event)
	{
		if(!event.world.isRemote)
		{
			TileEntity tileEntity = (TileEntity)event.energyTile;
			ArrayList<TileEntity> ignoredTiles = new ArrayList<TileEntity>();
			
			for(ForgeDirection orientation : ForgeDirection.VALID_DIRECTIONS)
			{
				TileEntity tile = Object3D.get(tileEntity).getFromSide(orientation).getTileEntity(event.world);
				
				if(tile != null)
				{
					if(tileEntity instanceof IEnergySource)
					{
						IEnergySource source = (IEnergySource)tileEntity;
						
						if(!source.emitsEnergyTo(tile, MekanismUtils.toIC2Direction(orientation)))
						{
							ignoredTiles.add(tile);
						}
					}
				}
			}
			
			event.amount = (int)(CableUtils.emitEnergyFromAllSides(event.amount*Mekanism.FROM_IC2, tileEntity, ignoredTiles)*Mekanism.TO_IC2);
		}
	}
}