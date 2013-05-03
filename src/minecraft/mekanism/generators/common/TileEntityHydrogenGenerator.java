package mekanism.generators.common;

import ic2.api.item.IElectricItem;

import java.util.ArrayList;

import mekanism.api.EnumGas;
import mekanism.api.IGasAcceptor;
import mekanism.api.IGasStorage;
import mekanism.api.IStorageTank;
import mekanism.api.ITubeConnection;
import mekanism.common.ChargeUtils;
import mekanism.common.MekanismUtils;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.common.ForgeDirection;
import universalelectricity.core.item.IItemElectric;

import com.google.common.io.ByteArrayDataInput;

import dan200.computer.api.IComputerAccess;

public class TileEntityHydrogenGenerator extends TileEntityGenerator implements IGasAcceptor, IGasStorage, ITubeConnection
{
	/** The maximum amount of hydrogen this block can store. */
	public int MAX_HYDROGEN = 18000;
	
	/** The amount of hydrogen this block is storing. */
	public int hydrogenStored;
	
	public TileEntityHydrogenGenerator()
	{
		super("Hydrogen Generator", 40000, 400);
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
				if(inventory[0].getItem() instanceof IStorageTank)
				{
					IStorageTank item = (IStorageTank)inventory[0].getItem();
					
					if(item.canProvideGas(inventory[0], EnumGas.HYDROGEN) && item.getGasType(inventory[0]) == EnumGas.HYDROGEN)
					{
						int received = 0;
						int hydrogenNeeded = MAX_HYDROGEN - hydrogenStored;
						if(item.getRate() <= hydrogenNeeded)
						{
							received = item.removeGas(inventory[0], EnumGas.HYDROGEN, item.getRate());
						}
						else if(item.getRate() > hydrogenNeeded)
						{
							received = item.removeGas(inventory[0], EnumGas.HYDROGEN, hydrogenNeeded);
						}
						
						setGas(EnumGas.HYDROGEN, hydrogenStored + received);
					}
				}
			}
			
			if(canOperate())
			{
				setActive(true);
				
				hydrogenStored-=2;
				setEnergy(electricityStored + 200);
			}
			else {
				setActive(false);
			}
		}
	}
	
	@Override
	public boolean func_102008_b(int slotID, ItemStack itemstack, int side)
	{
		if(slotID == 1)
		{
			return (itemstack.getItem() instanceof IItemElectric && ((IItemElectric)itemstack.getItem()).getReceiveRequest(itemstack).getWatts() == 0) ||
					(itemstack.getItem() instanceof IElectricItem && (!(itemstack.getItem() instanceof IItemElectric) || 
							((IItemElectric)itemstack.getItem()).getReceiveRequest(itemstack).getWatts() == 0));
		}
		else if(slotID == 0)
		{
			return (itemstack.getItem() instanceof IStorageTank && ((IStorageTank)itemstack.getItem()).getGas(EnumGas.NONE, itemstack) == 0);
		}
		
		return false;
	}
	
	@Override
	public boolean isStackValidForSlot(int slotID, ItemStack itemstack)
	{
		if(slotID == 0)
		{
			return itemstack.getItem() instanceof IStorageTank && ((IStorageTank)itemstack.getItem()).getGasType(itemstack) == EnumGas.HYDROGEN;
		}
		else if(slotID == 1)
		{
			return itemstack.getItem() instanceof IElectricItem || 
					(itemstack.getItem() instanceof IItemElectric && ((IItemElectric)itemstack.getItem()).getReceiveRequest(itemstack).amperes != 0);
		}
		
		return true;
	}
	
	@Override
	public int[] getSizeInventorySide(int side)
	{
		return ForgeDirection.getOrientation(side) == MekanismUtils.getRight(facing) ? new int[] {1} : new int[] {0};
	}
	
	@Override
	public int getStartInventorySide(ForgeDirection side) 
	{
		if(side == MekanismUtils.getRight(facing))
		{
			return 1;
		}
		
		return 0;
	}

	@Override
	public int getSizeInventorySide(ForgeDirection side)
	{
		return 1;
	}
    
    @Override
	public void setGas(EnumGas type, int amount, Object... data)
	{
		if(type == EnumGas.HYDROGEN)
		{
			hydrogenStored = Math.max(Math.min(amount, MAX_HYDROGEN), 0);
		}
	}
    
	@Override
	public int getGas(EnumGas type, Object... data)
	{
		if(type == EnumGas.HYDROGEN)
		{
			return hydrogenStored;
		}
		
		return 0;
	}
	
	@Override
	public boolean canOperate()
	{
		return electricityStored < MAX_ELECTRICITY && hydrogenStored-10 > -1;
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
	public Object[] callMethod(IComputerAccess computer, int method, Object[] arguments) throws Exception 
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
	public int getEnvironmentBoost() 
	{
		int boost = 1;
		
		if(yCoord > 64 && yCoord < 80)
		{
			boost = 2;
		}
		else if(yCoord > 80 && yCoord < 96)
		{
			boost = 3;
		}
		else if(yCoord > 96)
		{
			boost = 4;
		}
		return boost;
	}
	
	@Override
	public double getVoltage()
	{
		return 240;
	}

	@Override
	public int transferGasToAcceptor(int amount, EnumGas type)
	{
		if(type == EnumGas.HYDROGEN)
		{
	    	int rejects = 0;
	    	int neededHydrogen = MAX_HYDROGEN-hydrogenStored;
	    	if(amount <= neededHydrogen)
	    	{
	    		hydrogenStored += amount;
	    	}
	    	else if(amount > neededHydrogen)
	    	{
	    		hydrogenStored += neededHydrogen;
	    		rejects = amount-neededHydrogen;
	    	}
	    	
	    	return rejects;
		}
		
		return amount;
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
	public boolean canReceiveGas(ForgeDirection side, EnumGas type) 
	{
		return type == EnumGas.HYDROGEN && side != ForgeDirection.getOrientation(facing);
	}

	@Override
	public boolean canTubeConnect(ForgeDirection side) 
	{
		return side != ForgeDirection.getOrientation(facing);
	}

	@Override
	public int getMaxGas(EnumGas type, Object... data) 
	{
		if(type == EnumGas.HYDROGEN)
		{
			return MAX_HYDROGEN;
		}
		
		return 0;
	}
}
