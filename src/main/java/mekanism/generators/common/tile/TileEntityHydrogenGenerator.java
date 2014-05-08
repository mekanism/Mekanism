package mekanism.generators.common.tile;

import java.util.ArrayList;

import mekanism.api.gas.FuelHandler;
import mekanism.api.gas.FuelHandler.FuelGas;
import mekanism.api.gas.Gas;
import mekanism.api.gas.GasRegistry;
import mekanism.api.gas.GasStack;
import mekanism.api.gas.GasTank;
import mekanism.api.gas.GasTransmission;
import mekanism.api.gas.IGasHandler;
import mekanism.api.gas.IGasItem;
import mekanism.api.gas.ITubeConnection;
import mekanism.common.Mekanism;
import mekanism.common.util.ChargeUtils;
import mekanism.common.util.MekanismUtils;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.common.util.ForgeDirection;

import com.google.common.io.ByteArrayDataInput;

import dan200.computercraft.api.peripheral.IComputerAccess;
import dan200.computercraft.api.lua.ILuaContext;

public class TileEntityHydrogenGenerator extends TileEntityGenerator implements IGasHandler, ITubeConnection
{
	/** The maximum amount of gas this block can store. */
	public int MAX_GAS = 18000;

	/** The tank this block is storing fuel in. */
	public GasTank fuelTank;

	public int burnTicks = 0;
	public double generationRate = 0;

	public TileEntityHydrogenGenerator()
	{
		super("HydrogenGenerator", Mekanism.FROM_H2*100, Mekanism.FROM_H2*2);
		inventory = new ItemStack[2];
		fuelTank = new GasTank(MAX_GAS);
	}

	@Override
	public void onUpdate()
	{
		super.onUpdate();

		if(!worldObj.isRemote)
		{
			ChargeUtils.charge(1, this);

			if(inventory[0] != null && fuelTank.getStored() < MAX_GAS)
			{
				Gas gasType = null;
				if(fuelTank.getGas() != null)
				{
					gasType = fuelTank.getGas().getGas();
				}
				else if(inventory[0] != null && inventory[0].getItem() instanceof IGasItem)
				{
					gasType = ((IGasItem)inventory[0].getItem()).getGas(inventory[0]).getGas();
				}
				if(gasType != null)
				{
					GasStack removed = GasTransmission.removeGas(inventory[0], gasType, MAX_GAS-fuelTank.getStored());
					fuelTank.receive(removed, true);
				}
			}

			if(canOperate())
			{
				setActive(true);

				if(burnTicks > 0)
				{
					burnTicks--;
					setEnergy(electricityStored + generationRate);
				}
				else if(fuelTank.getStored() > 0)
				{
					FuelGas fuel = FuelHandler.getFuel(fuelTank.getGas().getGas());
					burnTicks = fuel.burnTicks - 1;
					generationRate = fuel.energyPerTick;
					fuelTank.draw(1, true);
					setEnergy(electricityStored + generationRate);
				}
				else {
					burnTicks = 0;
					generationRate = 0;
				}
			}
			else {
				setActive(false);
			}
		}
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
	public boolean isItemValidForSlot(int slotID, ItemStack itemstack)
	{
		if(slotID == 0)
		{
			return itemstack.getItem() instanceof IGasItem && ((IGasItem)itemstack.getItem()).getGas(itemstack) != null &&
					(((IGasItem)itemstack.getItem()).getGas(itemstack).getGas() == GasRegistry.getGas("hydrogen") ||
					((IGasItem)itemstack.getItem()).getGas(itemstack).getGas() == GasRegistry.getGas("ethene"));
		}
		else if(slotID == 1)
		{
			return ChargeUtils.canBeCharged(itemstack);
		}

		return true;
	}

	@Override
	public int[] getAccessibleSlotsFromSide(int side)
	{
		return ForgeDirection.getOrientation(side) == MekanismUtils.getRight(facing) ? new int[] {1} : new int[] {0};
	}

	@Override
	public boolean canOperate()
	{
		return electricityStored < MAX_ELECTRICITY && fuelTank.getStored() > 0 && MekanismUtils.canFunction(this);
	}

	/**
	 * Gets the scaled hydrogen level for the GUI.
	 * @param i - multiplier
	 * @return
	 */
	public int getScaledHydrogenLevel(int i)
	{
		return fuelTank.getStored()*i / MAX_GAS;
	}

	@Override
	public String[] getMethodNames()
	{
		return new String[] {"getStored", "getOutput", "getMaxEnergy", "getEnergyNeeded", "getHydrogen", "getHydrogenNeeded"};
	}

	@Override
	public Object[] callMethod(IComputerAccess computer, ILuaContext context, int method, Object[] arguments) throws Exception
	{
		switch(method)
		{
			case 0:
				return new Object[] {electricityStored};
			case 1:
				return new Object[] {output};
			case 2:
				return new Object[] {MAX_ELECTRICITY};
			case 3:
				return new Object[] {(MAX_ELECTRICITY-electricityStored)};
			case 4:
				return new Object[] {fuelTank.getStored()};
			case 5:
				return new Object[] {fuelTank.getNeeded()};
			default:
				System.err.println("[Mekanism] Attempted to call unknown method with computer ID " + computer.getID());
				return null;
		}
	}

	@Override
	public void handlePacketData(ByteArrayDataInput dataStream)
	{
		super.handlePacketData(dataStream);
		fuelTank.setGas(new GasStack(dataStream.readInt(), dataStream.readInt()));
		generationRate = dataStream.readDouble();
	}

	@Override
	public ArrayList getNetworkedData(ArrayList data)
	{
		super.getNetworkedData(data);
		data.add(fuelTank.getGas() == null ? 0 : fuelTank.getGas().getGas().getID());
		data.add(fuelTank.getStored());
		data.add(generationRate);
		return data;
	}

	@Override
	public int receiveGas(ForgeDirection side, GasStack stack)
	{
		if(fuelTank.getGas() == null || fuelTank.getGas().isGasEqual(stack))
		{
			return fuelTank.receive(stack, true);
		}

		return 0;
	}

	@Override
	public void readFromNBT(NBTTagCompound nbtTags)
	{
		super.readFromNBT(nbtTags);

		fuelTank.read(nbtTags.getCompoundTag("fuelTank"));
	}

	@Override
	public void writeToNBT(NBTTagCompound nbtTags)
	{
		super.writeToNBT(nbtTags);

		nbtTags.setTag("fuelTank", fuelTank.write(new NBTTagCompound()));
	}

	@Override
	public boolean canReceiveGas(ForgeDirection side, Gas type)
	{
		return FuelHandler.getFuel(type) != null && side != ForgeDirection.getOrientation(facing);
	}

	@Override
	public GasStack drawGas(ForgeDirection side, int amount)
	{
		return null;
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
}
