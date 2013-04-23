package mekanism.generators.common;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import ic2.api.ElectricItem;
import ic2.api.IElectricItem;
import mekanism.common.ChargeUtils;
import mekanism.common.LiquidSlot;
import mekanism.common.Mekanism;
import mekanism.common.MekanismUtils;
import mekanism.common.PacketHandler;
import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntityFurnace;
import net.minecraftforge.common.ForgeDirection;
import net.minecraftforge.liquids.ILiquidTank;
import net.minecraftforge.liquids.ITankContainer;
import net.minecraftforge.liquids.LiquidContainerRegistry;
import net.minecraftforge.liquids.LiquidStack;
import net.minecraftforge.liquids.LiquidTank;
import universalelectricity.core.item.ElectricItemHelper;
import universalelectricity.core.item.IItemElectric;

import com.google.common.io.ByteArrayDataInput;

import dan200.computer.api.IComputerAccess;

public class TileEntityHeatGenerator extends TileEntityGenerator implements ITankContainer
{
	/** The LiquidSlot fuel instance for this generator. */
	public LiquidTank lavaTank = new LiquidTank(24000);
	
	/** The amount of electricity this machine can produce with a unit of fuel. */
	public final int GENERATION = 80;
	
	public TileEntityHeatGenerator()
	{
		super("Heat Generator", 160000, 160);
		inventory = new ItemStack[2];
	}
	
	@Override
	public void onUpdate()
	{
		super.onUpdate();
		
		if(!worldObj.isRemote)
		{
			ChargeUtils.charge(1, this);
			
			if(inventory[0] != null)
			{
				LiquidStack liquid = LiquidContainerRegistry.getLiquidForFilledItem(inventory[0]);
				
				if(liquid != null && liquid.itemID == Block.lavaStill.blockID)
				{
					if(lavaTank.getLiquid() == null || lavaTank.getLiquid().amount+liquid.amount <= lavaTank.getCapacity())
					{
						lavaTank.fill(liquid, true);
						
						if(inventory[0].isItemEqual(new ItemStack(Item.bucketLava)))
						{
							inventory[0] = new ItemStack(Item.bucketEmpty);
						}
						else {
							inventory[0].stackSize--;
							
							if(inventory[0].stackSize == 0)
							{
								inventory[0] = null;
							}
						}
					}
				}
				else {
					int fuel = getFuel(inventory[0]);
					if(fuel > 0)
					{
						int fuelNeeded = lavaTank.getCapacity() - (lavaTank.getLiquid() != null ? lavaTank.getLiquid().amount : 0);
						if(fuel <= fuelNeeded)
						{
							lavaTank.fill(new LiquidStack(Block.lavaStill.blockID, fuel), true);
							inventory[0].stackSize--;
						}
						
						if(inventory[0].stackSize == 0)
						{
							inventory[0] = null;
						}
					}
				}
			}
			
			setEnergy(electricityStored + getEnvironmentBoost());
			
			if(canOperate())
			{	
				setActive(true);
				
				lavaTank.drain(10, true);
				setEnergy(electricityStored + GENERATION);
			}
			else {
				setActive(false);
			}
		}
	}
	
	@Override
	public boolean isStackValidForSlot(int slotID, ItemStack itemstack)
	{
		if(slotID == 0)
		{
			return getFuel(itemstack) > 0 || (LiquidContainerRegistry.getLiquidForFilledItem(itemstack) != null && LiquidContainerRegistry.getLiquidForFilledItem(itemstack).itemID == Block.lavaStill.blockID);
		}
		else if(slotID == 1)
		{
			return itemstack.getItem() instanceof IElectricItem || 
					(itemstack.getItem() instanceof IItemElectric && ((IItemElectric)itemstack.getItem()).getReceiveRequest(itemstack).amperes != 0);
		}
		
		return true;
	}
	
	@Override
	public boolean canOperate()
	{
		return electricityStored < MAX_ELECTRICITY && lavaTank.getLiquid() != null && lavaTank.getLiquid().amount >= 10;
	}
	
	@Override
    public void readFromNBT(NBTTagCompound nbtTags)
    {
        super.readFromNBT(nbtTags);
        
        if(nbtTags.hasKey("lavaTank"))
        {
        	lavaTank.readFromNBT(nbtTags.getCompoundTag("lavaTank"));
        }
    }

	@Override
    public void writeToNBT(NBTTagCompound nbtTags)
    {
        super.writeToNBT(nbtTags);
        
        if(lavaTank.getLiquid() != null)
        {
        	nbtTags.setTag("lavaTank", lavaTank.writeToNBT(new NBTTagCompound()));
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
			return LiquidContainerRegistry.isEmptyContainer(itemstack);
		}
		
		return false;
	}
	
	@Override
	public int getEnvironmentBoost()
	{
		int boost = 0;
		
		if(worldObj.getBlockId(xCoord+1, yCoord, zCoord) == 10 || worldObj.getBlockId(xCoord+1, yCoord, zCoord) == 11)
			boost+=5;
		if(worldObj.getBlockId(xCoord-1, yCoord, zCoord) == 10 || worldObj.getBlockId(xCoord-1, yCoord, zCoord) == 11)
			boost+=5;
		if(worldObj.getBlockId(xCoord, yCoord+1, zCoord) == 10 || worldObj.getBlockId(xCoord, yCoord+1, zCoord) == 11)
			boost+=5;
		if(worldObj.getBlockId(xCoord, yCoord-1, zCoord) == 10 || worldObj.getBlockId(xCoord, yCoord-1, zCoord) == 11)
			boost+=5;
		if(worldObj.getBlockId(xCoord, yCoord, zCoord+1) == 10 || worldObj.getBlockId(xCoord, yCoord, zCoord+1) == 11)
			boost+=5;
		if(worldObj.getBlockId(xCoord, yCoord, zCoord-1) == 10 || worldObj.getBlockId(xCoord, yCoord, zCoord-1) == 11)
			boost+=5;
		
		return boost;
	}

	public int getFuel(ItemStack itemstack)
	{
		if(itemstack.itemID == Item.bucketLava.itemID)
		{
			return 1000;
		}
		
		return TileEntityFurnace.getItemBurnTime(itemstack);
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
	
	/**
	 * Gets the scaled fuel level for the GUI.
	 * @param i - multiplier
	 * @return
	 */
	public int getScaledFuelLevel(int i)
	{
		return (lavaTank.getLiquid() != null ? lavaTank.getLiquid().amount : 0)*i / lavaTank.getCapacity();
	}
	
	@Override
	public void handlePacketData(ByteArrayDataInput dataStream)
	{
		super.handlePacketData(dataStream);
		
		int amount = dataStream.readInt();
		if(amount != 0)
		{
			lavaTank.setLiquid(new LiquidStack(Block.lavaStill.blockID, amount, 0));
		}
	}
	
	@Override
	public ArrayList getNetworkedData(ArrayList data)
	{
		super.getNetworkedData(data);
		
		if(lavaTank.getLiquid() != null)
		{
			data.add(lavaTank.getLiquid().amount);
		}
		else {
			data.add(0);
		}
		
		return data;
	}
	
	@Override
	public String[] getMethodNames() 
	{
		return new String[] {"getStored", "getOutput", "getMaxEnergy", "getEnergyNeeded", "getFuel", "getFuelNeeded"};
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
				return new Object[] {lavaTank.getLiquid() != null ? lavaTank.getLiquid().amount : 0};
			case 5:
				return new Object[] {lavaTank.getCapacity()-(lavaTank.getLiquid() != null ? lavaTank.getLiquid().amount : 0)};
			default:
				System.err.println("[Mekanism] Attempted to call unknown method with computer ID " + computer.getID());
				return null;
		}
	}

	@Override
	public int fill(ForgeDirection from, LiquidStack resource, boolean doFill) 
	{
		if(from != ForgeDirection.getOrientation(facing))
		{
			return fill(0, resource, doFill);
		}
		
		return 0;
	}

	@Override
	public int fill(int tankIndex, LiquidStack resource, boolean doFill) 
	{
		if(resource.itemID == Block.lavaStill.blockID && tankIndex == 0)
		{
			return lavaTank.fill(resource, doFill);
		}
		
		return 0;
	}

	@Override
	public LiquidStack drain(ForgeDirection from, int maxDrain, boolean doDrain) 
	{
		return null;
	}

	@Override
	public LiquidStack drain(int tankIndex, int maxDrain, boolean doDrain) 
	{
		return null;
	}

	@Override
	public ILiquidTank[] getTanks(ForgeDirection direction) 
	{
		return new ILiquidTank[] {lavaTank};
	}
	
	@Override
	public ILiquidTank getTank(ForgeDirection direction, LiquidStack type)
	{
		return lavaTank;
	}
}
