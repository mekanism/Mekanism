package ic2.api.energy.event;

import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.event.world.WorldEvent;

import ic2.api.energy.tile.IEnergyTile;

public class EnergyTileEvent extends WorldEvent {
	public final IEnergyTile energyTile;
	
	public EnergyTileEvent(IEnergyTile energyTile) {
		super(((TileEntity) energyTile).worldObj);
		
		this.energyTile = energyTile;
	}
}

