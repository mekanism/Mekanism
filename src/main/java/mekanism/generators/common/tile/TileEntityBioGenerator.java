package mekanism.generators.common.tile;

import io.netty.buffer.ByteBuf;

import java.util.ArrayList;

import mekanism.api.MekanismConfig.generators;
import mekanism.api.gas.*;
import mekanism.api.gas.GasTank;
import mekanism.common.base.ISustainedData;
import mekanism.common.util.ChargeUtils;
import mekanism.common.util.MekanismUtils;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.common.util.ForgeDirection;

public class TileEntityBioGenerator extends TileEntityGenerator implements IGasHandler, ITubeConnection, ISustainedData
{
	/** The maximum amount of gas this block can store. */
	public int MAX_GAS = 18000;
	public GasTank fuelTank;


	public TileEntityBioGenerator()
	{
		super("bio", "BioGenerator", 180000, generators.bioGeneration * generators.ethanolMultiplier * 2);
		inventory = new ItemStack[2];
		fuelTank = new GasTank(MAX_GAS);
	}

	@Override
	public void onUpdate() {
		super.onUpdate();

		if (!worldObj.isRemote) {
			ChargeUtils.charge(1, this);

			//Has valid item and capacity for new gas
			if (inventory[0] != null && fuelTank.getStored() < MAX_GAS) {
				Gas gasType = null;

				//Ensures to only accept currently stored gas
				if (fuelTank.getStored() > 0 && fuelTank.getGas() != null)
				{
					gasType = fuelTank.getGas().getGas();
				}
				else if(inventory[0] != null && inventory[0].getItem() instanceof IGasItem)
				{
					GasStack gas = ((IGasItem)inventory[0].getItem()).getGas(inventory[0]);

					//If tank has valid gas in storage, accept gas from container
					if(gas != null && isValidGas(gas.getGas()))
					{
						gasType = gas.getGas();
					}
				}
				//If valid gas, drain Gas container
				if (isValidGas(gasType)) {

					GasStack removed = GasTransmission.removeGas(inventory[0], gasType, fuelTank.getNeeded());
					boolean isTankEmpty = (fuelTank.getGas() == null);

					int fuelReceived = fuelTank.receive(removed, true);
					//If fuel is received and Tank is empty, generate power
					if (fuelReceived > 0 && isTankEmpty) {
						//Distinguish between fuel types
						output = generators.bioGeneration * getMultiplier(gasType) * 2;
					}
				}
			}

			if (canOperate()) {
				if (!worldObj.isRemote) {
					setActive(true);
				}
				output = generators.bioGeneration * getMultiplier(fuelTank.getGas().getGas()) * 2;

				setEnergy(electricityStored + generators.bioGeneration * getMultiplier(fuelTank.getGas().getGas()));
				fuelTank.setGas(new GasStack(fuelTank.getGasType(), fuelTank.getStored() - 1));

			} else {
				//reset
					fuelTank.setGas(null);
					output = generators.bioGeneration * 2;

				if (!worldObj.isRemote) {
					setActive(false);
				}
			}
		}
	}
	@Override
	public boolean isItemValidForSlot(int slotID, ItemStack itemstack)
	{
		if(slotID == 0)
		{
			return itemstack.getItem() instanceof IGasItem && ((IGasItem)itemstack.getItem()).getGas(itemstack) != null &&
					isValidGas((((IGasItem)itemstack.getItem()).getGas(itemstack).getGas()));
		}
		else if(slotID == 1)
		{
			return ChargeUtils.canBeCharged(itemstack);
		}

		return true;
	}

	@Override
	public boolean canExtractItem(int slotID, ItemStack itemstack, int side)
	{
		if(slotID == 1)
		{
			return ChargeUtils.canBeOutputted(itemstack, true);
		}
		else if(slotID == 0)
		{
			return (itemstack.getItem() instanceof IGasItem && ((IGasItem)itemstack.getItem()).getGas(itemstack) == null);
		}

		return false;
	}

	@Override
	public boolean canOperate()
	{
		return electricityStored < BASE_MAX_ENERGY && fuelTank.getStored() > 0 && MekanismUtils.canFunction(this);
	}

	@Override
	public int[] getAccessibleSlotsFromSide(int side)
	{
		return ForgeDirection.getOrientation(side) == MekanismUtils.getRight(facing) ? new int[] {1} : new int[] {0};
	}

	@Override
	public void readFromNBT(NBTTagCompound nbtTags)
	{
		super.readFromNBT(nbtTags);

		fuelTank.read(nbtTags.getCompoundTag("fuelTank"));

		boolean isTankEmpty = (fuelTank.getGas() == null);
		Gas fuel = (isTankEmpty) ? null : fuelTank.getGas().getGas();

		if (fuel != null)
			output = generators.bioGeneration * getMultiplier(fuelTank.getGas().getGas()) * 2;
	}

	@Override
	public void writeToNBT(NBTTagCompound nbtTags)
	{
		super.writeToNBT(nbtTags);

		nbtTags.setTag("fuelTank", fuelTank.write(new NBTTagCompound()));
	}

	public double getMultiplier(Gas gas)
	{
		if (gas == GasRegistry.getGas("bioethanol")) {

			return generators.ethanolMultiplier;
		}
		return 1;
	}

	public int getFuelType()
	{
		if (fuelTank.getStored() > 0) {

			Gas gas = fuelTank.getGas().getGas();

			if (gas != null && isValidGas(gas)) {
				if (gas == GasRegistry.getGas("bioethanol"))
					return 1; //Ethanol
			}
		}
		return 0; //Biogas
	}

	public int getTypeGas()
	{
		if(fuelTank.getGas().getGas() == GasRegistry.getGas("bioethanol"))
		{
			return 1; //Orange
		}
		return 0; //Green
	}

	/**
	 * Gets the scaled fuel level for the GUI.
	 * @param i - multiplier
	 * @return
	 */
	public int getScaledFuelLevel(int i)
	{
		return fuelTank.getStored()*i / MAX_GAS;
	}

	@Override
	public boolean canSetFacing(int facing)
	{
		return facing != 0 && facing != 1;
	}

	@Override
	public void handlePacketData(ByteBuf dataStream)
	{
		super.handlePacketData(dataStream);

		if(worldObj.isRemote)
		{
			if(dataStream.readBoolean())
			{
				fuelTank.setGas(new GasStack(GasRegistry.getGas(dataStream.readInt()), dataStream.readInt()));
			}
			else {
				fuelTank.setGas(null);
			}

			output = dataStream.readDouble();
		}
	}

	public boolean isValidGas(Gas gas)
	{
		if (gas == GasRegistry.getGas("biomass") || gas == GasRegistry.getGas("bioethanol")){
			return true;}
		else
			return false;
	}

	@Override
	public ArrayList getNetworkedData(ArrayList data)
	{
		super.getNetworkedData(data);

		if(fuelTank.getGas() != null)
		{
			data.add(true);
			data.add(fuelTank.getGas().getGas().getID());
			data.add(fuelTank.getStored());
		}
		else {
			data.add(false);
		}

		data.add(output);

		return data;
	}
    private static final String[] methods = new String[] {"getEnergy", "getOutput", "getMaxEnergy", "getEnergyNeeded", "getBioFuel", "getBioFuelNeeded"};

	@Override
	public String[] getMethods()
	{
		return methods;
	}

	@Override
	public Object[] invoke(int method, Object[] arguments) throws Exception
	{
		switch(method)
		{
			case 0:
				return new Object[] {getEnergy()};
			case 1:
				return new Object[] {output};
			case 2:
				return new Object[] {getMaxEnergy()};
			case 3:
				return new Object[] {getMaxEnergy()-getEnergy()};
			case 4:
				return new Object[] {fuelTank.getStored()};
			case 5:
				return new Object[] {fuelTank.getNeeded()};
			default:
				throw new NoSuchMethodException();
		}
	}

	@Override
	public boolean canReceiveGas(ForgeDirection side, Gas type)
	{
		return isValidGas(type) && side != ForgeDirection.getOrientation(facing);
	}

	@Override
	public int receiveGas(ForgeDirection side, GasStack stack, boolean doTransfer) {
		boolean isTankEmpty = (fuelTank.getGas() == null);

		if(canReceiveGas(side, stack.getGas()) && (isTankEmpty || fuelTank.getGas().isGasEqual(stack)))
		{
			int fuelReceived = fuelTank.receive(stack, doTransfer);

			if(doTransfer && isTankEmpty && fuelReceived > 0)
			{
				output = generators.bioGeneration * getMultiplier(stack.getGas()) * 2;
			}

			return fuelReceived;
		}

		return 0;
	}

	@Override
	public int receiveGas(ForgeDirection side, GasStack stack) {
		return 0;
	}

	@Override
	public GasStack drawGas(ForgeDirection side, int amount, boolean doTransfer)
	{
		return null;
	}

	@Override
	public GasStack drawGas(ForgeDirection side, int amount)
	{
		return drawGas(side, amount, true);
	}

	@Override
	public boolean canDrawGas(ForgeDirection side, Gas type)
	{
		return false;
	}

	@Override
	public boolean canTubeConnect(ForgeDirection side)
	{
		return side != ForgeDirection.getOrientation(facing);
	}
	@Override
	public void writeSustainedData(ItemStack itemStack)
	{
		if(fuelTank != null)
		{
			itemStack.stackTagCompound.setTag("fuelTank", fuelTank.write(new NBTTagCompound()));
		}
	}

	@Override
	public void readSustainedData(ItemStack itemStack)
	{
		if(itemStack.stackTagCompound.hasKey("fuelTank"))
		{
			fuelTank.read(itemStack.stackTagCompound.getCompoundTag("fuelTank"));

			boolean isTankEmpty = (fuelTank.getGas() == null);
			//Update energy output based on any existing fuel in tank

			Gas fuel = (isTankEmpty) ? null : fuelTank.getGas().getGas();

			if (fuel != null)
				output = generators.bioGeneration * getMultiplier(fuelTank.getGas().getGas()) * 2;

		}
	}
}