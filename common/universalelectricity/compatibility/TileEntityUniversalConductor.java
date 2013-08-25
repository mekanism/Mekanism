package universalelectricity.compatibility;

import ic2.api.energy.event.EnergyTileLoadEvent;
import ic2.api.energy.event.EnergyTileUnloadEvent;
import ic2.api.energy.tile.IEnergyAcceptor;
import ic2.api.energy.tile.IEnergyEmitter;
import ic2.api.energy.tile.IEnergySink;
import ic2.api.energy.tile.IEnergyTile;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.common.ForgeDirection;
import net.minecraftforge.common.MinecraftForge;
import universalelectricity.core.block.IConnector;
import universalelectricity.core.electricity.ElectricityPack;
import universalelectricity.core.vector.Vector3;
import universalelectricity.core.vector.VectorHelper;
import universalelectricity.prefab.tile.TileEntityConductor;
import buildcraft.api.power.IPowerReceptor;

/**
 * A universal conductor class.
 * 
 * Extend this class or use as a reference for your own implementation of compatible conductor
 * tiles.
 * 
 * TODO: Need working BuildCraft support!
 * 
 * @author micdoodle8
 * 
 */
public abstract class TileEntityUniversalConductor extends TileEntityConductor implements IEnergySink
{
    protected boolean isAddedToEnergyNet;
    
	@Override
	public TileEntity[] getAdjacentConnections()
	{
		if (this.adjacentConnections == null)
		{
			this.adjacentConnections = new TileEntity[6];

			for (byte i = 0; i < 6; i++)
			{
				ForgeDirection side = ForgeDirection.getOrientation(i);
				TileEntity tileEntity = VectorHelper.getTileEntityFromSide(this.worldObj, new Vector3(this), side);

				if (tileEntity instanceof IConnector)
				{
					if (((IConnector) tileEntity).canConnect(side.getOpposite()))
					{
						this.adjacentConnections[i] = tileEntity;
					}
				}
				else if (Compatibility.isIndustrialCraft2Loaded() && tileEntity instanceof IEnergyTile)
				{
					if (tileEntity instanceof IEnergyAcceptor)
					{
						if (((IEnergyAcceptor) tileEntity).acceptsEnergyFrom(this, side.getOpposite()))
						{
							this.adjacentConnections[i] = tileEntity;
							continue;
						}
					}

					if (tileEntity instanceof IEnergyEmitter)
	                {
	                    if (((IEnergyEmitter) tileEntity).emitsEnergyTo(tileEntity, side.getOpposite()))
	                    {
                            this.adjacentConnections[i] = tileEntity;
                            continue;
	                    }
	                }
					
					this.adjacentConnections[i] = tileEntity;
				}
				else if (Compatibility.isBuildcraftLoaded() && tileEntity instanceof IPowerReceptor)
				{
					this.adjacentConnections[i] = tileEntity;
				}
			}
		}

		return this.adjacentConnections;
	}

    @Override
    public boolean canUpdate()
    {
        return !this.isAddedToEnergyNet;
    }

    @Override
    public void updateEntity()
    {
        if (!this.worldObj.isRemote)
        {
            if (!this.isAddedToEnergyNet)
            {
                this.initIC();
            }
        }
    }

    @Override
    public void invalidate()
    {
        this.unloadTileIC2();
        super.invalidate();
    }

    @Override
    public void onChunkUnload()
    {
        this.unloadTileIC2();
        super.onChunkUnload();
    }

    protected void initIC()
    {
        if (Compatibility.isIndustrialCraft2Loaded())
        {
            MinecraftForge.EVENT_BUS.post(new EnergyTileLoadEvent(this));
        }

        this.isAddedToEnergyNet = true;
    }

    private void unloadTileIC2()
    {
        if (this.isAddedToEnergyNet && this.worldObj != null)
        {
            if (Compatibility.isIndustrialCraft2Loaded())
            {
                MinecraftForge.EVENT_BUS.post(new EnergyTileUnloadEvent(this));
            }

            this.isAddedToEnergyNet = false;
        }
    }

    @Override
    public double demandedEnergyUnits()
    {
        if (this.getNetwork() == null)
        {
            return 0.0;
        }
        
        return this.getNetwork().getRequest(this).getWatts() * Compatibility.TO_IC2_RATIO;
    }

    @Override
    public double injectEnergyUnits(ForgeDirection directionFrom, double amount)
    {
        TileEntity tile = VectorHelper.getTileEntityFromSide(this.worldObj, new Vector3(this), directionFrom);
        return this.getNetwork().produce(ElectricityPack.getFromWatts((float) (amount * Compatibility.IC2_RATIO), 120.0F / 1000.0F), this, tile);
    }

    @Override
    public int getMaxSafeInput()
    {
        return Integer.MAX_VALUE;
    }

    @Override
    public boolean acceptsEnergyFrom(TileEntity emitter, ForgeDirection direction)
    {
        return true;
    }
}
