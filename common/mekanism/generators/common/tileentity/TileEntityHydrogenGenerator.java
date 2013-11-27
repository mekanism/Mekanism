package mekanism.generators.common.tileentity;

import java.util.ArrayList;

import mekanism.api.gas.Gas;
import mekanism.api.gas.GasRegistry;
import mekanism.api.gas.GasStack;
import mekanism.api.gas.GasUtils;
import mekanism.api.gas.IGasAcceptor;
import mekanism.api.gas.IGasItem;
import mekanism.api.gas.IGasStorage;
import mekanism.api.gas.ITubeConnection;
import mekanism.common.util.ChargeUtils;
import mekanism.common.util.MekanismUtils;
import mekanism.generators.common.MekanismGenerators;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.common.ForgeDirection;

import com.google.common.io.ByteArrayDataInput;

import dan200.computer.api.IComputerAccess;
import dan200.computer.api.ILuaContext;

public class TileEntityHydrogenGenerator extends TileEntityGenerator implements IGasAcceptor, IGasStorage, ITubeConnection
{
	/** The maximum amount of hydrogen this block can store. */
	public int MAX_HYDROGEN = 18000;
	
	/** The amount of hydrogen this block is storing. */
	public int hydrogenStored;
	
	public TileEntityHydrogenGenerator()
	{
		super("Hydrogen Generator", 40000, MekanismGenerators.hydrogenGeneration*2);
		inventory = new ItemStack[2];
	}
	
	@Override
	public void onUpdate()
	{
		super.onUpdate();
		
		if(!worldObj.isRemote)
		{
			ChargeUtils.charge(1, this);
			
			if(inventory[0] != null && hydrogenStored < MAX_HYDROGEN)
			{
				GasStack removed = GasUtils.removeGas(inventory[0], GasRegistry.getGas("hydrogen"), getMaxGas()-hydrogenStored);
				setGas(new GasStack(GasRegistry.getGas("hydrogen"), hydrogenStored - (removed != null ? removed.amount : 0)));
			}
			
			if(canOperate())
			{
				setActive(true);
				
				hydrogenStored-=2;
				setEnergy(electricityStored + MekanismGenerators.hydrogenGeneration);
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
					((IGasItem)itemstack.getItem()).getGas(itemstack).getGas() == GasRegistry.getGas("hydrogen");
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
	public GasStack getGas(Object... data) 
	{
		if(hydrogenStored == 0)
		{
			return null;
		}
		
		return new GasStack(GasRegistry.getGas("hydrogen"), hydrogenStored);
	}

	@Override
	public void setGas(GasStack stack, Object... data) 
	{
		if(stack == null)
		{
			hydrogenStored = 0;
		}
		else if(stack.getGas() == GasRegistry.getGas("hydrogen"))
		{
			hydrogenStored = Math.max(Math.min(stack.amount, getMaxGas()), 0);
		}
		
		MekanismUtils.saveChunk(this);
	}
	
	@Override
	public boolean canOperate()
	{
		return electricityStored < MAX_ELECTRICITY && hydrogenStored-2 >= 0 && MekanismUtils.canFunction(this);
	}
	
	/**
	 * Gets the scaled hydrogen level for the GUI.
	 * @param i - multiplier
	 * @return
	 */
	public int getScaledHydrogenLevel(int i)
	{
		return hydrogenStored*i / MAX_HYDROGEN;
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
				return new Object[] {hydrogenStored};
			case 5:
				return new Object[] {MAX_HYDROGEN-hydrogenStored};
			default:
				System.err.println("[Mekanism] Attempted to call unknown method with computer ID " + computer.getID());
				return null;
		}
	}

	@Override
	public void handlePacketData(ByteArrayDataInput dataStream)
	{
		super.handlePacketData(dataStream);
		hydrogenStored = dataStream.readInt();
	}
	
	@Override
	public ArrayList getNetworkedData(ArrayList data)
	{
		super.getNetworkedData(data);
		data.add(hydrogenStored);
		return data;
	}
	
	@Override
	public float getVoltage()
	{
		return 240;
	}

	@Override
	public int receiveGas(GasStack stack) 
	{
		if(stack.getGas() == GasRegistry.getGas("hydrogen"))
		{
			int stored = getGas() != null ? getGas().amount : 0;
			int toUse = Math.min(getMaxGas()-stored, stack.amount);
			
			setGas(new GasStack(stack.getGas(), stored + toUse));
	    	
	    	return toUse;
		}
		
		return 0;
	}
	
	@Override
    public void readFromNBT(NBTTagCompound nbtTags)
    {
        super.readFromNBT(nbtTags);

        hydrogenStored = nbtTags.getInteger("hydrogenStored");
    }

	@Override
    public void writeToNBT(NBTTagCompound nbtTags)
    {
        super.writeToNBT(nbtTags);
        
        nbtTags.setInteger("hydrogenStored", hydrogenStored);
    }

	@Override
	public boolean canReceiveGas(ForgeDirection side, Gas type) 
	{
		return type == GasRegistry.getGas("hydrogen") && side != ForgeDirection.getOrientation(facing);
	}

	@Override
	public boolean canTubeConnect(ForgeDirection side) 
	{
		return side != ForgeDirection.getOrientation(facing);
	}

	@Override
	public int getMaxGas(Object... data) 
	{
		return MAX_HYDROGEN;
	}
}
