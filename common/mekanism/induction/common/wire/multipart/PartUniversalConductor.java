package mekanism.induction.common.wire.multipart;

import ic2.api.energy.event.EnergyTileLoadEvent;
import ic2.api.energy.event.EnergyTileUnloadEvent;
import ic2.api.energy.tile.IEnergySink;
import ic2.api.energy.tile.IEnergyTile;

import java.util.HashSet;
import java.util.Set;

import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import net.minecraftforge.common.ForgeDirection;
import net.minecraftforge.common.MinecraftForge;
import universalelectricity.compatibility.Compatibility;
import universalelectricity.core.electricity.ElectricityPack;
import universalelectricity.core.vector.Vector3;
import universalelectricity.core.vector.VectorHelper;
import buildcraft.api.power.IPowerReceptor;
import buildcraft.api.power.PowerHandler;
import buildcraft.api.power.PowerHandler.PowerReceiver;
import buildcraft.api.power.PowerHandler.Type;

public abstract class PartUniversalConductor extends PartConductor implements IEnergySink, IPowerReceptor
{

	protected boolean isAddedToEnergyNet;
	public PowerHandler powerHandler;
	public float buildcraftBuffer = Compatibility.BC3_RATIO * 50;

	public PartUniversalConductor()
	{
		this.powerHandler = new PowerHandler(this, Type.PIPE);
		this.powerHandler.configure(0, this.buildcraftBuffer, this.buildcraftBuffer, this.buildcraftBuffer * 2);
		this.powerHandler.configurePowerPerdition(0, 0);
	}

	@Override
	public boolean isValidAcceptor(TileEntity tile)
	{
		if (Compatibility.isIndustrialCraft2Loaded() && tile instanceof IEnergyTile)
		{
			return true;
		}
		else if (Compatibility.isBuildcraftLoaded() && tile instanceof IPowerReceptor)
		{
			return true;
		}
		return super.isValidAcceptor(tile);
	}

	@Override
	public void onWorldJoin()
	{
		super.onWorldJoin();
		if (!this.world().isRemote)
		{
			if (!this.isAddedToEnergyNet)
			{
				this.initIC();
			}
		}
	}

	@Override
	public void onAdded()
	{
		super.onAdded();
		if (!this.world().isRemote)
		{
			if (!this.isAddedToEnergyNet)
			{
				this.initIC();
			}
		}
	}

	@Override
	public void onChunkLoad()
	{
		super.onChunkLoad();
		if (!this.world().isRemote)
		{
			if (!this.isAddedToEnergyNet)
			{
				this.initIC();
			}
		}
	}

	@Override
	public void onWorldSeparate()
	{
		this.unloadTileIC2();
		super.onWorldSeparate();
	}

	@Override
	public void onChunkUnload()
	{
		this.unloadTileIC2();
		super.onChunkUnload();
	}

	@Override
	public void onRemoved()
	{
	}

	@Override
	public void preRemove()
	{
		this.unloadTileIC2();
		super.preRemove();
	}

	protected void initIC()
	{
		if (Compatibility.isIndustrialCraft2Loaded())
		{
			MinecraftForge.EVENT_BUS.post(new EnergyTileLoadEvent((IEnergyTile) tile()));
		}

		this.isAddedToEnergyNet = true;
	}

	private void unloadTileIC2()
	{
		if (this.isAddedToEnergyNet && this.world() != null)
		{
			if (Compatibility.isIndustrialCraft2Loaded())
			{
				MinecraftForge.EVENT_BUS.post(new EnergyTileUnloadEvent((IEnergyTile) tile()));
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

		return this.getNetwork().getRequest(tile()).getWatts() * Compatibility.TO_IC2_RATIO;
	}

	@Override
	public double injectEnergyUnits(ForgeDirection directionFrom, double amount)
	{
		TileEntity tile = VectorHelper.getTileEntityFromSide(this.world(), new Vector3(tile()), directionFrom);
		ElectricityPack pack = ElectricityPack.getFromWatts((float) (amount * Compatibility.IC2_RATIO), 120);
		return this.getNetwork().produce(pack, tile(), tile) * Compatibility.TO_IC2_RATIO;
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
	 * BuildCraft functions
	 */
	@Override
	public PowerReceiver getPowerReceiver(ForgeDirection side)
	{
		return this.powerHandler.getPowerReceiver();
	}

	@Override
	public void doWork(PowerHandler workProvider)
	{
		Set<TileEntity> ignoreTiles = new HashSet<TileEntity>();
		ignoreTiles.add(tile());

		for (ForgeDirection direction : ForgeDirection.VALID_DIRECTIONS)
		{
			TileEntity tile = new Vector3(tile()).modifyPositionFromSide(direction).getTileEntity(this.world());
			ignoreTiles.add(tile);
		}

		ElectricityPack pack = ElectricityPack.getFromWatts(workProvider.useEnergy(0, this.getNetwork().getRequest(tile()).getWatts() * Compatibility.TO_BC_RATIO, true) * Compatibility.BC3_RATIO, 120);
		this.getNetwork().produce(pack, ignoreTiles.toArray(new TileEntity[0]));
	}

	@Override
	public World getWorld()
	{
		return world();
	}
}
