package universalelectricity.compatibility;

import ic2.api.energy.event.EnergyTileLoadEvent;
import ic2.api.energy.event.EnergyTileUnloadEvent;
import ic2.api.energy.tile.IEnergySink;
import ic2.api.energy.tile.IEnergySource;
import ic2.api.energy.tile.IEnergyTile;
import ic2.api.item.IElectricItemManager;
import ic2.api.item.ISpecialElectricItem;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import net.minecraftforge.common.ForgeDirection;
import net.minecraftforge.common.MinecraftForge;
import universalelectricity.core.electricity.ElectricityPack;
import universalelectricity.core.item.IItemElectric;
import universalelectricity.core.vector.Vector3;
import universalelectricity.prefab.tile.TileEntityElectrical;
import buildcraft.api.power.IPowerReceptor;
import buildcraft.api.power.PowerHandler;
import buildcraft.api.power.PowerHandler.PowerReceiver;
import buildcraft.api.power.PowerHandler.Type;
import cofh.api.energy.IEnergyContainerItem;
import cofh.api.energy.IEnergyHandler;

/**
 * A universal electricity tile used for tiles that consume or produce electricity.
 * 
 * Extend this class or use as a reference for your own implementation of compatible electrical
 * tiles.
 * 
 * @author micdoodle8, Calclavia
 * 
 */
public abstract class TileEntityUniversalElectrical extends TileEntityElectrical implements IEnergySink, IEnergySource, IPowerReceptor, IEnergyHandler
{
	protected boolean isAddedToEnergyNet;
	public PowerHandler bcPowerHandler;
	public Type bcBlockType = Type.MACHINE;
	public float maxInputEnergy = 100;

	/**
	 * Recharges electric item.
	 */
	@Override
	public void recharge(ItemStack itemStack)
	{
		if (itemStack != null)
		{
			if (itemStack.getItem() instanceof IItemElectric)
			{
				super.recharge(itemStack);
			}
			else if (itemStack.getItem() instanceof ISpecialElectricItem)
			{
				ISpecialElectricItem electricItem = (ISpecialElectricItem) itemStack.getItem();
				IElectricItemManager manager = electricItem.getManager(itemStack);
				float energy = Math.max(this.getProvide(ForgeDirection.UNKNOWN) * Compatibility.IC2_RATIO, 0);
				energy = manager.charge(itemStack, (int) (energy * Compatibility.TO_IC2_RATIO), 0, false, false) * Compatibility.IC2_RATIO;
				this.provideElectricity(energy, true);
			}
			else if (itemStack.getItem() instanceof IEnergyContainerItem)
			{
				float forgienEnergyAccepted = ((IEnergyContainerItem) itemStack.getItem()).receiveEnergy(itemStack, (int) (this.getProvide(ForgeDirection.UNKNOWN) * Compatibility.TO_TE_RATIO), false);
				this.provideElectricity(forgienEnergyAccepted * Compatibility.TE_RATIO, true);
			}
		}
	}

	/**
	 * Discharges electric item.
	 */
	@Override
	public void discharge(ItemStack itemStack)
	{
		if (itemStack != null)
		{
			if (itemStack.getItem() instanceof IItemElectric)
			{
				super.discharge(itemStack);
			}
			else if (itemStack.getItem() instanceof ISpecialElectricItem)
			{
				ISpecialElectricItem electricItem = (ISpecialElectricItem) itemStack.getItem();

				if (electricItem.canProvideEnergy(itemStack))
				{
					IElectricItemManager manager = electricItem.getManager(itemStack);
					float energy = Math.max(this.getRequest(ForgeDirection.UNKNOWN) * Compatibility.IC2_RATIO, 0);
					energy = manager.discharge(itemStack, (int) (energy * Compatibility.TO_IC2_RATIO), 0, false, false);
					this.receiveElectricity(energy, true);
				}
			}
			else if (itemStack.getItem() instanceof IEnergyContainerItem)
			{
				float forgienEnergy = ((IEnergyContainerItem) itemStack.getItem()).extractEnergy(itemStack, (int) (this.getRequest(ForgeDirection.UNKNOWN) * Compatibility.TO_TE_RATIO), false);
				this.receiveElectricity(forgienEnergy * Compatibility.TE_RATIO, true);
			}
		}
	}

	@Override
	public void initiate()
	{
		super.initiate();
		this.initBuildCraft();
	}

	@Override
	public void updateEntity()
	{
		super.updateEntity();

		// Register to the IC2 Network
		if (!this.worldObj.isRemote)
		{
			if (!this.isAddedToEnergyNet)
			{
				this.initIC();
			}

			if (this.bcPowerHandler == null)
			{
				this.initBuildCraft();
			}

			if (Compatibility.isBuildcraftLoaded())
			{
				if (this.bcPowerHandler.getEnergyStored() > 0)
				{
					/**
					 * Cheat BuildCraft powerHandler and always empty energy inside of it.
					 */
					this.receiveElectricity(this.bcPowerHandler.getEnergyStored() * Compatibility.BC3_RATIO, true);
					this.bcPowerHandler.setEnergy(0);
				}
			}
		}
	}

	@Override
	public void produce()
	{
		if (!this.worldObj.isRemote)
		{
			for (ForgeDirection outputDirection : this.getOutputDirections())
			{
				if (outputDirection != ForgeDirection.UNKNOWN)
				{
					if (!this.produceUE(outputDirection))
					{
						if (!this.produceThermalExpansion(outputDirection))
						{
							this.produceBuildCraft(outputDirection);
						}
					}

				}
			}
		}
	}

	public boolean produceThermalExpansion(ForgeDirection outputDirection)
	{
		if (!this.worldObj.isRemote && outputDirection != null && outputDirection != ForgeDirection.UNKNOWN)
		{
			float provide = this.getProvide(outputDirection);

			if (this.getEnergyStored() >= provide && provide > 0)
			{
				if (Compatibility.isThermalExpansionLoaded())
				{
					TileEntity tileEntity = new Vector3(this).modifyPositionFromSide(outputDirection).getTileEntity(this.worldObj);

					if (tileEntity instanceof IEnergyHandler)
					{
						IEnergyHandler receiver = (IEnergyHandler) tileEntity;
						int convertedProvide = (int) (provide * Compatibility.TO_TE_RATIO);

						if (receiver.canInterface(outputDirection.getOpposite()) && receiver.receiveEnergy(outputDirection.getOpposite(), convertedProvide, true) > 0)
						{
							int forgienEnergyUsed = receiver.receiveEnergy(outputDirection.getOpposite(), convertedProvide, false);
							this.provideElectricity(forgienEnergyUsed * Compatibility.TE_RATIO, true);
							return true;
						}
					}
				}
			}
		}

		return false;
	}

	public boolean produceBuildCraft(ForgeDirection outputDirection)
	{
		if (!this.worldObj.isRemote && outputDirection != null && outputDirection != ForgeDirection.UNKNOWN)
		{
			float provide = this.getProvide(outputDirection);

			if (this.getEnergyStored() >= provide && provide > 0)
			{
				if (Compatibility.isBuildcraftLoaded())
				{
					TileEntity tileEntity = new Vector3(this).modifyPositionFromSide(outputDirection).getTileEntity(this.worldObj);

					if (tileEntity instanceof IPowerReceptor)
					{
						PowerReceiver receiver = ((IPowerReceptor) tileEntity).getPowerReceiver(outputDirection.getOpposite());

						if (receiver != null)
						{
							if (receiver.powerRequest() > 0)
							{
								float convertedProvide = provide * Compatibility.TO_BC_RATIO;
								float forgienEnergyUsed = receiver.receiveEnergy(this.bcBlockType, convertedProvide, outputDirection.getOpposite());
								this.provideElectricity(forgienEnergyUsed * Compatibility.BC3_RATIO, true);
								return true;
							}
						}
					}
				}
			}
		}

		return false;
	}

	/**
	 * TE Methods
	 */
	public int receiveEnergy(ForgeDirection from, int maxReceive, boolean simulate)
	{
		return (int) (this.receiveElectricity(from, ElectricityPack.getFromWatts(maxReceive * Compatibility.TE_RATIO, this.getVoltage()), !simulate) * Compatibility.TO_TE_RATIO);
	}

	public int extractEnergy(ForgeDirection from, int maxExtract, boolean simulate)
	{
		return (int) (this.provideElectricity(from, ElectricityPack.getFromWatts(maxExtract * Compatibility.TE_RATIO, this.getVoltage()), !simulate).getWatts() * Compatibility.TO_TE_RATIO);
	}

	public boolean canInterface(ForgeDirection from)
	{
		return this.canConnect(from);
	}

	/**
	 * Returns the amount of energy currently stored.
	 */
	public int getEnergyStored(ForgeDirection from)
	{
		return (int) (this.getEnergyStored() * Compatibility.TO_TE_RATIO);
	}

	/**
	 * Returns the maximum amount of energy that can be stored.
	 */
	public int getMaxEnergyStored(ForgeDirection from)
	{
		return (int) (this.getMaxEnergyStored() * Compatibility.TO_TE_RATIO);
	}

	/**
	 * IC2 Methods
	 */
	@Override
	public boolean acceptsEnergyFrom(TileEntity emitter, ForgeDirection direction)
	{
		return this.getInputDirections().contains(direction);
	}

	@Override
	public double getOfferedEnergy()
	{
		return this.getProvide(ForgeDirection.UNKNOWN) * Compatibility.TO_IC2_RATIO;
	}

	@Override
	public void drawEnergy(double amount)
	{
		this.provideElectricity((float) amount * Compatibility.IC2_RATIO, true);
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
		return this.getRequest(ForgeDirection.UNKNOWN) * Compatibility.TO_IC2_RATIO;
	}

	@Override
	public double injectEnergyUnits(ForgeDirection direction, double amount)
	{
		if (this.getInputDirections().contains(direction))
		{
			float convertedEnergy = (float) (amount * Compatibility.IC2_RATIO);
			ElectricityPack toSend = ElectricityPack.getFromWatts(convertedEnergy, this.getVoltage());
			float receive = this.receiveElectricity(direction, toSend, true);

			// Return the difference, since injectEnergy returns left over energy, and
			// receiveElectricity returns energy used.
			return Math.round(amount - (receive * Compatibility.TO_IC2_RATIO));
		}

		return amount;
	}

	@Override
	public boolean emitsEnergyTo(TileEntity receiver, ForgeDirection direction)
	{
		return receiver instanceof IEnergyTile && this.getOutputDirections().contains(direction);
	}

	@Override
	public int getMaxSafeInput()
	{
		return Integer.MAX_VALUE;
	}

	/**
	 * BuildCraft power support
	 */
	public void initBuildCraft()
	{
		if (this.bcPowerHandler == null)
		{
			this.bcPowerHandler = new PowerHandler(this, this.bcBlockType);
		}
		this.bcPowerHandler.configure(0, this.maxInputEnergy, 0, (int) Math.ceil(this.getMaxEnergyStored() * Compatibility.BC3_RATIO));
	}

	@Override
	public PowerReceiver getPowerReceiver(ForgeDirection side)
	{
		this.initBuildCraft();
		return this.bcPowerHandler.getPowerReceiver();
	}

	@Override
	public void doWork(PowerHandler workProvider)
	{

	}

	@Override
	public World getWorld()
	{
		return this.getWorldObj();
	}
}
