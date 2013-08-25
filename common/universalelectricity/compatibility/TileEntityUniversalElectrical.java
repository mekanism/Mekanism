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
import thermalexpansion.api.item.IChargeableItem;
import universalelectricity.core.electricity.ElectricityPack;
import universalelectricity.core.item.IItemElectric;
import universalelectricity.core.vector.Vector3;
import universalelectricity.prefab.tile.TileEntityElectrical;
import buildcraft.api.power.IPowerReceptor;
import buildcraft.api.power.PowerHandler;
import buildcraft.api.power.PowerHandler.PowerReceiver;
import buildcraft.api.power.PowerHandler.Type;

/**
 * A universal electricity tile used for tiles that consume or produce electricity.
 * 
 * Extend this class or use as a reference for your own implementation of compatible electrical
 * tiles.
 * 
 * @author micdoodle8, Calclavia
 * 
 */
public abstract class TileEntityUniversalElectrical extends TileEntityElectrical implements IEnergySink, IEnergySource, IPowerReceptor
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
			else if (itemStack.getItem() instanceof IChargeableItem)
			{
				float accepted = ((IChargeableItem) itemStack.getItem()).receiveEnergy(itemStack, this.getProvide(ForgeDirection.UNKNOWN) * Compatibility.BC3_RATIO, true);
				this.provideElectricity(accepted, true);
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
			else if (itemStack.getItem() instanceof IChargeableItem)
			{
				float given = ((IChargeableItem) itemStack.getItem()).transferEnergy(itemStack, this.getRequest(ForgeDirection.UNKNOWN) * Compatibility.BC3_RATIO, true);
				this.receiveElectricity(given, true);
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
				this.produceUE(outputDirection);
				this.produceBuildCraft(outputDirection);
			}
		}
	}

	public void produceBuildCraft(ForgeDirection outputDirection)
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
							float bc3Provide = provide * Compatibility.TO_BC_RATIO;
							float energyUsed = Math.min(receiver.receiveEnergy(this.bcBlockType, bc3Provide, outputDirection.getOpposite()), bc3Provide);
							this.provideElectricity((bc3Provide - (energyUsed * Compatibility.TO_BC_RATIO)), true);
						}
					}
				}
			}
		}
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
		return Math.ceil(this.getRequest(ForgeDirection.UNKNOWN) * Compatibility.TO_IC2_RATIO);
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
