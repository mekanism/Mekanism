package universalelectricity.prefab.tile;

import java.util.EnumSet;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.common.ForgeDirection;
import universalelectricity.core.block.IElectrical;
import universalelectricity.core.block.IElectricalStorage;
import universalelectricity.core.electricity.ElectricityHelper;
import universalelectricity.core.electricity.ElectricityPack;
import universalelectricity.core.grid.IElectricityNetwork;
import universalelectricity.core.item.ElectricItemHelper;
import universalelectricity.core.vector.Vector3;
import universalelectricity.core.vector.VectorHelper;

public abstract class TileEntityElectrical extends TileEntityAdvanced implements IElectrical, IElectricalStorage
{
	public float energyStored = 0;

	/**
	 * Recharges electric item.
	 */
	public void recharge(ItemStack itemStack)
	{
		this.setEnergyStored(this.getEnergyStored() - ElectricItemHelper.chargeItem(itemStack, this.getProvide(ForgeDirection.UNKNOWN)));
	}

	/**
	 * Discharges electric item.
	 */
	public void discharge(ItemStack itemStack)
	{
		this.setEnergyStored(this.getEnergyStored() + ElectricItemHelper.dischargeItem(itemStack, this.getRequest(ForgeDirection.UNKNOWN)));
	}

	/**
	 * Called to produce the potential electricity inside this block.
	 */
	public void produce()
	{
		if (!this.worldObj.isRemote)
		{
			for (ForgeDirection outputDirection : this.getOutputDirections())
			{
				this.produceUE(outputDirection);
			}
		}
	}

	/**
	 * Produces UE power towards a specific direction.
	 * 
	 * @param outputDirection - The output direction.
	 */
	public boolean produceUE(ForgeDirection outputDirection)
	{
		if (!this.worldObj.isRemote && outputDirection != null && outputDirection != ForgeDirection.UNKNOWN)
		{
			float provide = this.getProvide(outputDirection);

			if (provide > 0)
			{
                TileEntity outputTile = VectorHelper.getConnectorFromSide(this.worldObj, new Vector3(this), outputDirection);
                IElectricityNetwork outputNetwork = ElectricityHelper.getNetworkFromTileEntity(outputTile, outputDirection);
                if (outputNetwork != null)
                {
                    ElectricityPack powerRequest = outputNetwork.getRequest(this);

                    if (powerRequest.getWatts() > 0)
                    {
                        ElectricityPack sendPack = ElectricityPack.min(ElectricityPack.getFromWatts(this.getEnergyStored(), this.getVoltage()), ElectricityPack.getFromWatts(provide, this.getVoltage()));
                        float rejectedPower = outputNetwork.produce(sendPack, this);
                        this.provideElectricity(sendPack.getWatts() - rejectedPower, true);                    
                        return true;
                    }
                }
                else if (outputTile instanceof IElectrical)
                {
                    float requestedEnergy = ((IElectrical) outputTile).getRequest(outputDirection.getOpposite());
                    
                    if (requestedEnergy > 0)
                    {
                        ElectricityPack sendPack = ElectricityPack.min(ElectricityPack.getFromWatts(this.getEnergyStored(), this.getVoltage()), ElectricityPack.getFromWatts(provide, this.getVoltage()));
                        float acceptedEnergy = ((IElectrical) outputTile).receiveElectricity(outputDirection.getOpposite(), sendPack, true);
                        this.setEnergyStored(this.getEnergyStored() - acceptedEnergy);
                        return true;
                    }
                }
			}
		}

		return false;
	}

	/**
	 * The electrical input direction.
	 * 
	 * @return The direction that electricity is entered into the tile. Return null for no input. By
	 * default you can accept power from all sides.
	 */
	public EnumSet<ForgeDirection> getInputDirections()
	{
		return EnumSet.allOf(ForgeDirection.class);
	}

	/**
	 * The electrical output direction.
	 * 
	 * @return The direction that electricity is output from the tile. Return null for no output. By
	 * default it will return an empty EnumSet.
	 */
	public EnumSet<ForgeDirection> getOutputDirections()
	{
		return EnumSet.noneOf(ForgeDirection.class);
	}

	@Override
	public float receiveElectricity(ForgeDirection from, ElectricityPack receive, boolean doReceive)
	{
		if (this.getInputDirections().contains(from))
		{
			return this.receiveElectricity(receive, doReceive);
		}

		return 0;
	}

	@Override
	public ElectricityPack provideElectricity(ForgeDirection from, ElectricityPack request, boolean doProvide)
	{
		if (this.getOutputDirections().contains(from))
		{
			return this.provideElectricity(request, doProvide);
		}

		return new ElectricityPack();
	}

	/**
	 * A non-side specific version of receiveElectricity for you to optionally use it internally.
	 */
	public float receiveElectricity(ElectricityPack receive, boolean doReceive)
	{
		if (receive != null)
		{
			float prevEnergyStored = this.getEnergyStored();
			float newStoredEnergy = Math.min(this.getEnergyStored() + receive.getWatts(), this.getMaxEnergyStored());

			if (doReceive)
			{
				this.setEnergyStored(newStoredEnergy);
			}

			return Math.max(newStoredEnergy - prevEnergyStored, 0);
		}

		return 0;
	}

	public float receiveElectricity(float energy, boolean doReceive)
	{
		return this.receiveElectricity(ElectricityPack.getFromWatts(energy, this.getVoltage()), doReceive);
	}

	/**
	 * A non-side specific version of provideElectricity for you to optionally use it internally.
	 */
	public ElectricityPack provideElectricity(ElectricityPack request, boolean doProvide)
	{
		if (request != null)
		{
			float requestedEnergy = Math.min(request.getWatts(), this.energyStored);

			if (doProvide)
			{
				this.setEnergyStored(this.energyStored - requestedEnergy);
			}

			return ElectricityPack.getFromWatts(requestedEnergy, this.getVoltage());
		}

		return new ElectricityPack();
	}

	public ElectricityPack provideElectricity(float energy, boolean doProvide)
	{
		return this.provideElectricity(ElectricityPack.getFromWatts(energy, this.getVoltage()), doProvide);
	}

	@Override
	public void setEnergyStored(float energy)
	{
		this.energyStored = Math.max(Math.min(energy, this.getMaxEnergyStored()), 0);
	}

	@Override
	public float getEnergyStored()
	{
		return this.energyStored;
	}

	@Override
	public boolean canConnect(ForgeDirection direction)
	{
		if (direction == null || direction.equals(ForgeDirection.UNKNOWN))
		{
			return false;
		}

		return this.getInputDirections().contains(direction) || this.getOutputDirections().contains(direction);
	}

	@Override
	public float getVoltage()
	{
		return 0.120F;
	}

	@Override
	public void readFromNBT(NBTTagCompound nbt)
	{
		super.readFromNBT(nbt);
		this.energyStored = nbt.getFloat("energyStored");
	}

	@Override
	public void writeToNBT(NBTTagCompound nbt)
	{
		super.writeToNBT(nbt);
		nbt.setFloat("energyStored", this.energyStored);
	}
}
