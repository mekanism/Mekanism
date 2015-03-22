package mekanism.generators.common.tile;

import java.util.ArrayList;

import mekanism.api.MekanismConfig.general;
import mekanism.api.gas.Gas;
import mekanism.api.gas.GasRegistry;
import mekanism.api.gas.GasStack;
import mekanism.api.gas.GasTank;
import mekanism.api.gas.GasTransmission;
import mekanism.api.gas.IGasHandler;
import mekanism.api.gas.IGasItem;
import mekanism.api.gas.ITubeConnection;
import mekanism.common.FuelHandler;
import mekanism.common.FuelHandler.FuelGas;
import mekanism.common.Mekanism;
import mekanism.common.base.ISustainedData;
import mekanism.common.util.ChargeUtils;
import mekanism.common.util.MekanismUtils;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.common.util.ForgeDirection;
import cpw.mods.fml.common.Optional.Method;

import io.netty.buffer.ByteBuf;

import dan200.computercraft.api.lua.ILuaContext;
import dan200.computercraft.api.lua.LuaException;
import dan200.computercraft.api.peripheral.IComputerAccess;

public class TileEntityGasGenerator extends TileEntityGenerator implements IGasHandler, ITubeConnection, ISustainedData
{
	/** The maximum amount of gas this block can store. */
	public int MAX_GAS = 18000;

	/** The tank this block is storing fuel in. */
	public GasTank fuelTank;

	public int burnTicks = 0;
	public double generationRate = 0;

	public TileEntityGasGenerator()
	{
		super("gas", "GasGenerator", general.FROM_H2*100, general.FROM_H2*2);
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
					if(((IGasItem)inventory[0].getItem()).getGas(inventory[0]) != null)
					{
						gasType = ((IGasItem)inventory[0].getItem()).getGas(inventory[0]).getGas();
					}
				}
				
				if(gasType != null && FuelHandler.getFuel(gasType) != null)
				{
					GasStack removed = GasTransmission.removeGas(inventory[0], gasType, fuelTank.getNeeded());
					boolean isTankEmpty = (fuelTank.getGas() == null);
					
					int fuelReceived = fuelTank.receive(removed, true);
					
					if(fuelReceived > 0 && isTankEmpty)
					{
						output = FuelHandler.getFuel(fuelTank.getGas().getGas()).energyPerTick * 2;
					}
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
					
					if(fuel != null)
					{
						burnTicks = fuel.burnTicks - 1;
						generationRate = fuel.energyPerTick;
						fuelTank.draw(1, true);
						setEnergy(getEnergy() + generationRate);
					}
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
					FuelHandler.getFuel((((IGasItem)itemstack.getItem()).getGas(itemstack).getGas())) != null;
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
		return getEnergy() < getMaxEnergy() && fuelTank.getStored() > 0 && MekanismUtils.canFunction(this);
	}

	/**
	 * Gets the scaled gas level for the GUI.
	 * @param i - multiplier
	 * @return
	 */
	public int getScaledGasLevel(int i)
	{
		return fuelTank.getStored()*i / MAX_GAS;
	}

	@Override
	@Method(modid = "ComputerCraft")
	public String[] getMethodNames()
	{
		return new String[] {"getStored", "getOutput", "getMaxEnergy", "getEnergyNeeded", "getGas", "getGasNeeded"};
	}

	@Override
	@Method(modid = "ComputerCraft")
	public Object[] callMethod(IComputerAccess computer, ILuaContext context, int method, Object[] arguments) throws LuaException, InterruptedException
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
				Mekanism.logger.error("Attempted to call unknown method with computer ID " + computer.getID());
				return null;
		}
	}

	@Override
	public void handlePacketData(ByteBuf dataStream)
	{
		super.handlePacketData(dataStream);
		
		if(dataStream.readBoolean())
		{
			fuelTank.setGas(new GasStack(GasRegistry.getGas(dataStream.readInt()), dataStream.readInt()));
		}
		else {
			fuelTank.setGas(null);
		}
		
		generationRate = dataStream.readDouble();
		output = dataStream.readDouble();
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
		
		data.add(generationRate);
		data.add(output);
		
		return data;
	}

	@Override
	public int receiveGas(ForgeDirection side, GasStack stack, boolean doTransfer)
	{
		boolean isTankEmpty = (fuelTank.getGas() == null);
		
		if(isTankEmpty || fuelTank.getGas().isGasEqual(stack))
		{
			int fuelReceived = fuelTank.receive(stack, true);
			
			if(isTankEmpty && fuelReceived > 0) 
			{
				output = FuelHandler.getFuel(fuelTank.getGas().getGas()).energyPerTick*2;
			}
			
			return fuelReceived;
		}

		return 0;
	}

	@Override
	public int receiveGas(ForgeDirection side, GasStack stack)
	{
		return receiveGas(side, stack, true);
	}

	@Override
	public void readFromNBT(NBTTagCompound nbtTags)
	{
		super.readFromNBT(nbtTags);

		fuelTank.read(nbtTags.getCompoundTag("fuelTank"));
		
		boolean isTankEmpty = (fuelTank.getGas() == null);
		FuelGas fuel = (isTankEmpty) ? null : FuelHandler.getFuel(fuelTank.getGas().getGas());
		
		if(fuel != null) 
		{
			output = fuel.energyPerTick * 2;
		}
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
			FuelGas fuel = (isTankEmpty) ? null : FuelHandler.getFuel(fuelTank.getGas().getGas());
			
			if(fuel != null) 
			{
				output = fuel.energyPerTick * 2;
			}
		}
	}
}
