package mekanism.induction.common.wire;

import ic2.api.energy.event.EnergyTileLoadEvent;
import ic2.api.energy.event.EnergyTileUnloadEvent;
import ic2.api.energy.tile.IEnergySink;
import ic2.api.energy.tile.IEnergyTile;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.common.ForgeDirection;
import net.minecraftforge.common.MinecraftForge;
import universalelectricity.compatibility.Compatibility;
import universalelectricity.core.electricity.ElectricityPack;
import universalelectricity.core.vector.Vector3;
import universalelectricity.core.vector.VectorHelper;
import cofh.api.energy.IEnergyHandler;

public abstract class PartUniversalConductor extends PartConductor implements IEnergySink, IEnergyHandler
{
	protected boolean isAddedToEnergyNet;

	@Override
	public boolean isValidAcceptor(TileEntity tile)
	{
		if(tile instanceof IEnergyTile)
		{
			return true;
		}
		else if(tile instanceof IEnergyHandler)
		{
			return true;
		}

		return super.isValidAcceptor(tile);
	}

	@Override
	public boolean isConnectionPrevented(TileEntity tile, ForgeDirection side)
	{
		if(tile instanceof IEnergyHandler)
		{
			return !((IEnergyHandler)tile).canInterface(side);
		}
		
		return super.isConnectionPrevented(tile, side);
	}

	@Override
	public void onWorldJoin()
	{
		super.onWorldJoin();
		
		if(!world().isRemote)
		{
			if(!isAddedToEnergyNet)
			{
				initIC();
			}
		}
	}

	@Override
	public void onAdded()
	{
		super.onAdded();
		
		if(!world().isRemote)
		{
			if(!isAddedToEnergyNet)
			{
				initIC();
			}
		}
	}

	@Override
	public void onChunkLoad()
	{
		super.onChunkLoad();
		
		if(!world().isRemote)
		{
			if(!isAddedToEnergyNet)
			{
				initIC();
			}
		}
	}

	@Override
	public void onWorldSeparate()
	{
		unloadTileIC2();
		super.onWorldSeparate();
	}

	@Override
	public void onChunkUnload()
	{
		unloadTileIC2();
		super.onChunkUnload();
	}

	@Override
	public void onRemoved() {}

	@Override
	public void preRemove()
	{
		unloadTileIC2();
		super.preRemove();
	}

	protected void initIC()
	{
		if(Compatibility.isIndustrialCraft2Loaded())
		{
			MinecraftForge.EVENT_BUS.post(new EnergyTileLoadEvent((IEnergyTile) tile()));
		}

		isAddedToEnergyNet = true;
	}

	private void unloadTileIC2()
	{
		if(isAddedToEnergyNet && world() != null)
		{
			if(Compatibility.isIndustrialCraft2Loaded())
			{
				MinecraftForge.EVENT_BUS.post(new EnergyTileUnloadEvent((IEnergyTile) tile()));
			}

			isAddedToEnergyNet = false;
		}
	}

	@Override
	public double demandedEnergyUnits()
	{
		if(getNetwork() == null)
		{
			return 0.0;
		}

		return getNetwork().getRequest(tile()).getWatts() * Compatibility.TO_IC2_RATIO;
	}

	@Override
	public double injectEnergyUnits(ForgeDirection directionFrom, double amount)
	{
		TileEntity tile = VectorHelper.getTileEntityFromSide(world(), new Vector3(tile()), directionFrom);
		ElectricityPack pack = ElectricityPack.getFromWatts((float) (amount * Compatibility.IC2_RATIO), 120);
		return getNetwork().produce(pack, tile(), tile) * Compatibility.TO_IC2_RATIO;
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

	/**
	 * Thermal Expansion Functions
	 */
	@Override
	public int receiveEnergy(ForgeDirection from, int maxReceive, boolean simulate)
	{
		ElectricityPack pack = ElectricityPack.getFromWatts(maxReceive * Compatibility.TE_RATIO, 1);
		float request = getMaxEnergyStored(from);

		if(!simulate)
		{
			if(request > 0)
			{
				return (int) (maxReceive - (getNetwork().produce(pack, new Vector3(tile()).modifyPositionFromSide(from).getTileEntity(world())) * Compatibility.TO_TE_RATIO));
			}

			return 0;
		}

		return (int)Math.min(maxReceive, request * Compatibility.TO_TE_RATIO);
	}

	@Override
	public int extractEnergy(ForgeDirection from, int maxExtract, boolean simulate)
	{
		return 0;
	}

	@Override
	public boolean canInterface(ForgeDirection from)
	{
		return true;
	}

	@Override
	public int getEnergyStored(ForgeDirection from)
	{
		return 0;
	}

	@Override
	public int getMaxEnergyStored(ForgeDirection from)
	{
		return (int)Math.round(getNetwork().getRequest(new Vector3(tile()).modifyPositionFromSide(from).getTileEntity(world())).getWatts() * Compatibility.TO_TE_RATIO);
	}
}
