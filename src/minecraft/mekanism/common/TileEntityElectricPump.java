package mekanism.common;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

import buildcraft.api.core.Position;

import com.google.common.io.ByteArrayDataInput;

import ic2.api.ElectricItem;
import ic2.api.IElectricItem;
import universalelectricity.core.item.ElectricItemHelper;
import universalelectricity.core.item.IItemElectric;
import universalelectricity.core.vector.Vector3;
import universalelectricity.core.vector.VectorHelper;
import mekanism.api.InfusionType;
import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.common.ForgeDirection;
import net.minecraftforge.liquids.ILiquidTank;
import net.minecraftforge.liquids.ITankContainer;
import net.minecraftforge.liquids.LiquidContainerRegistry;
import net.minecraftforge.liquids.LiquidStack;
import net.minecraftforge.liquids.LiquidTank;

public class TileEntityElectricPump extends TileEntityElectricBlock implements ITankContainer, ISustainedTank
{
	/** This pump's tank */
	public LiquidTank liquidTank;
	
	/** The nodes that have full sources near them or in them */
	public Set<BlockWrapper> recurringNodes = new HashSet<BlockWrapper>();
	
	/** The nodes that have already been sucked up, but are held on to in order to remove dead blocks */
	public Set<BlockWrapper> cleaningNodes = new HashSet<BlockWrapper>();
	
	/** Random for this pump */
	public Random random = new Random();
	
	public TileEntityElectricPump()
	{
		super("Electric Pump", 10000);
		liquidTank = new LiquidTank(10000);
		inventory = new ItemStack[3];
	}
	
	@Override
	public void onUpdate()
	{
		if(inventory[2] != null)
		{
			if(electricityStored < MAX_ELECTRICITY)
			{
				setJoules(getJoules() + ElectricItemHelper.dechargeItem(inventory[2], getMaxJoules() - getJoules(), getVoltage()));
				
				if(Mekanism.hooks.IC2Loaded && inventory[2].getItem() instanceof IElectricItem)
				{
					IElectricItem item = (IElectricItem)inventory[2].getItem();
					if(item.canProvideEnergy(inventory[2]))
					{
						double gain = ElectricItem.discharge(inventory[2], (int)((MAX_ELECTRICITY - electricityStored)*Mekanism.TO_IC2), 3, false, false)*Mekanism.FROM_IC2;
						setJoules(electricityStored + gain);
					}
				}
			}
			if(inventory[2].itemID == Item.redstone.itemID && electricityStored+1000 <= MAX_ELECTRICITY)
			{
				setJoules(electricityStored + 1000);
				inventory[2].stackSize--;
				
	            if(inventory[2].stackSize <= 0)
	            {
	                inventory[2] = null;
	            }
			}
		}
		
		if(inventory[0] != null)
		{
			if(liquidTank.getLiquid() != null && liquidTank.getLiquid().amount >= LiquidContainerRegistry.BUCKET_VOLUME)
			{
				if(LiquidContainerRegistry.isEmptyContainer(inventory[0]))
				{
					ItemStack tempStack = LiquidContainerRegistry.fillLiquidContainer(liquidTank.getLiquid(), inventory[0]);
					
					if(tempStack != null)
					{
						if(inventory[1] == null)
						{
							liquidTank.drain(LiquidContainerRegistry.BUCKET_VOLUME, true);
							
							inventory[1] = tempStack;
							inventory[0].stackSize--;
							
							if(inventory[0].stackSize <= 0)
							{
								inventory[0] = null;
							}
						}
						else if(tempStack.isItemEqual(inventory[1]) && tempStack.getMaxStackSize() > inventory[1].stackSize)
						{
							liquidTank.drain(LiquidContainerRegistry.BUCKET_VOLUME, true);
							
							inventory[1].stackSize++;
							inventory[0].stackSize--;
							
							if(inventory[0].stackSize <= 0)
							{
								inventory[0] = null;
							}
						}
					}
				}
			}
		}
		
		if(!worldObj.isRemote && worldObj.getWorldTime() % 20 == 0)
		{
			if(electricityStored >= 100 && (liquidTank.getLiquid() == null || liquidTank.getLiquid().amount+LiquidContainerRegistry.BUCKET_VOLUME <= 10000))
			{
				if(suck(true))
				{
					clean(true);
				}
				else {
					cleaningNodes.clear();
				}
			}
		}
		
		if(liquidTank.getLiquid() != null) 
		{
			for(ForgeDirection orientation : ForgeDirection.VALID_DIRECTIONS) 
			{
				TileEntity tileEntity = VectorHelper.getTileEntityFromSide(worldObj, new Vector3(xCoord, yCoord, zCoord), orientation);

				if(tileEntity instanceof ITankContainer) 
				{
					liquidTank.drain(((ITankContainer)tileEntity).fill(orientation.getOpposite(), liquidTank.getLiquid(), true), true);
					
					if(liquidTank.getLiquid() == null || liquidTank.getLiquid().amount <= 0) 
					{
						break;
					}
				}
			}
		}
	}
	
	public boolean suck(boolean take)
	{
		List<BlockWrapper> tempPumpList = Arrays.asList(recurringNodes.toArray(new BlockWrapper[recurringNodes.size()]));
		Collections.shuffle(tempPumpList);
		
		for(BlockWrapper wrapper : cleaningNodes)
		{
			if(MekanismUtils.isLiquid(worldObj, wrapper.x, wrapper.y, wrapper.z))
			{
				if(liquidTank.getLiquid() == null || MekanismUtils.getLiquid(worldObj, wrapper.x, wrapper.y, wrapper.z).isLiquidEqual(liquidTank.getLiquid()))
				{
					if(take)
					{
						setJoules(electricityStored - 100);
						liquidTank.fill(MekanismUtils.getLiquid(worldObj, wrapper.x, wrapper.y, wrapper.z), true);
						worldObj.setBlockToAir(wrapper.x, wrapper.y, wrapper.z);
					}
					
					return true;
				}
			}
		}
		
		for(BlockWrapper wrapper : tempPumpList)
		{
			if(MekanismUtils.isLiquid(worldObj, wrapper.x, wrapper.y, wrapper.z))
			{
				if(liquidTank.getLiquid() == null || MekanismUtils.getLiquid(worldObj, wrapper.x, wrapper.y, wrapper.z).isLiquidEqual(liquidTank.getLiquid()))
				{
					if(take)
					{
						setJoules(electricityStored - 100);
						liquidTank.fill(MekanismUtils.getLiquid(worldObj, wrapper.x, wrapper.y, wrapper.z), true);
						worldObj.setBlockToAir(wrapper.x, wrapper.y, wrapper.z);
					}
					
					return true;
				}
			}
			else if(MekanismUtils.isDeadLiquid(worldObj, wrapper.x, wrapper.y, wrapper.z))
			{
				if(liquidTank.getLiquid() != null && MekanismUtils.getLiquidId(worldObj, wrapper.x, wrapper.y, wrapper.z) == liquidTank.getLiquid().itemID)
				{
					if(take)
					{
						worldObj.setBlockToAir(wrapper.x, wrapper.y, wrapper.z);
					}
				}
			}
			
			for(ForgeDirection orientation : ForgeDirection.VALID_DIRECTIONS)
			{
				int x = MekanismUtils.getCoords(wrapper, orientation)[0];
				int y = MekanismUtils.getCoords(wrapper, orientation)[1];
				int z = MekanismUtils.getCoords(wrapper, orientation)[2];
				
				if(MekanismUtils.getDistance(BlockWrapper.get(this), new BlockWrapper(x, y, z)) <= 80)
				{
					if(MekanismUtils.isLiquid(worldObj, x, y, z))
					{
						if(liquidTank.getLiquid() == null || MekanismUtils.getLiquid(worldObj, x, y, z).isLiquidEqual(liquidTank.getLiquid()))
						{
							if(take)
							{
								setJoules(electricityStored - 100);
								recurringNodes.add(new BlockWrapper(x, y, z));
								liquidTank.fill(MekanismUtils.getLiquid(worldObj, x, y, z), true);
								worldObj.setBlockToAir(x, y, z);
							}
							
							return true;
						}
					}
					else if(MekanismUtils.isDeadLiquid(worldObj, x, y, z))
					{
						if(liquidTank.getLiquid() != null && MekanismUtils.getLiquidId(worldObj, x, y, z) == liquidTank.getLiquid().itemID)
						{
							if(take)
							{
								worldObj.setBlockToAir(x, y, z);
							}
						}
					}
				}
			}
			
			cleaningNodes.add(wrapper);
			recurringNodes.remove(wrapper);
		}
		
		for(ForgeDirection orientation : ForgeDirection.VALID_DIRECTIONS)
		{
			if(orientation != ForgeDirection.UP)
			{
				int x = MekanismUtils.getCoords(BlockWrapper.get(this), orientation)[0];
				int y = MekanismUtils.getCoords(BlockWrapper.get(this), orientation)[1];
				int z = MekanismUtils.getCoords(BlockWrapper.get(this), orientation)[2];
				
				if(MekanismUtils.isLiquid(worldObj, x, y, z))
				{
					if(liquidTank.getLiquid() == null || MekanismUtils.getLiquid(worldObj, x, y, z).isLiquidEqual(liquidTank.getLiquid()))
					{
						if(take)
						{
							setJoules(electricityStored - 100);
							recurringNodes.add(new BlockWrapper(x, y, z));
							liquidTank.fill(MekanismUtils.getLiquid(worldObj, x, y, z), true);
							worldObj.setBlockToAir(x, y, z);
						}
						
						return true;
					}
				}
				else if(MekanismUtils.isDeadLiquid(worldObj, x, y, z))
				{
					if(liquidTank.getLiquid() != null && MekanismUtils.getLiquidId(worldObj, x, y, z) == liquidTank.getLiquid().itemID)
					{
						if(take)
						{
							worldObj.setBlockToAir(x, y, z);
						}
					}
				}
			}
		}
		
		return false;
	}
	
	public boolean clean(boolean take)
	{
		boolean took = false;
		if(!worldObj.isRemote)
		{
			for(BlockWrapper wrapper : cleaningNodes)
			{
				if(MekanismUtils.isDeadLiquid(worldObj, wrapper.x, wrapper.y, wrapper.z))
				{
					if(liquidTank.getLiquid() != null && MekanismUtils.getLiquidId(worldObj, wrapper.x, wrapper.y, wrapper.z) == liquidTank.getLiquid().itemID)
					{
						took = true;
						if(take)
						{
							worldObj.setBlockToAir(wrapper.x, wrapper.y, wrapper.z);
						}
					}
				}
			}
			
			for(BlockWrapper wrapper : recurringNodes)
			{
				if(MekanismUtils.isDeadLiquid(worldObj, wrapper.x, wrapper.y, wrapper.z))
				{
					if(liquidTank.getLiquid() != null && MekanismUtils.getLiquidId(worldObj, wrapper.x, wrapper.y, wrapper.z) == liquidTank.getLiquid().itemID)
					{
						took = true;
						if(take)
						{
							worldObj.setBlockToAir(wrapper.x, wrapper.y, wrapper.z);
						}
					}
				}
			}
		}
		
		return took;
	}
	
	@Override
	public void handlePacketData(ByteArrayDataInput dataStream)
	{
		super.handlePacketData(dataStream);
		
		try {
			int amount = dataStream.readInt();
			int itemID = dataStream.readInt();
			int itemMeta = dataStream.readInt();
			
			liquidTank.setLiquid(new LiquidStack(itemID, amount, itemMeta));
			
		} catch(Exception e) {}
		
		MekanismUtils.updateBlock(worldObj, xCoord, yCoord, zCoord);
	}
	
	@Override
	public ArrayList getNetworkedData(ArrayList data)
	{
		super.getNetworkedData(data);
		
		if(liquidTank.getLiquid() != null)
		{
			data.add(liquidTank.getLiquid().amount);
			data.add(liquidTank.getLiquid().itemID);
			data.add(liquidTank.getLiquid().itemMeta);
		}
		
		return data;
	}
	
	public int getScaledEnergyLevel(int i)
	{
		return (int)(electricityStored*i / MAX_ELECTRICITY);
	}
	
	public int getScaledLiquidLevel(int i)
	{
		return liquidTank.getLiquid() != null ? liquidTank.getLiquid().amount*i / 10000 : 0;
	}
	
    @Override
    public void writeToNBT(NBTTagCompound nbtTags)
    {
        super.writeToNBT(nbtTags);
        
        if(liquidTank.getLiquid() != null)
        {
        	nbtTags.setTag("liquidTank", liquidTank.writeToNBT(new NBTTagCompound()));
        }
        
        NBTTagList recurringList = new NBTTagList();
        
        for(BlockWrapper wrapper : recurringNodes)
        {
        	NBTTagCompound tagCompound = new NBTTagCompound();
        	wrapper.write(tagCompound);
        	recurringList.appendTag(tagCompound);
        }
        
        if(!recurringList.tagList.isEmpty())
        {
        	nbtTags.setTag("recurringNodes", recurringList);
        }
        
        NBTTagList cleaningList = new NBTTagList();
        
        for(BlockWrapper wrapper : cleaningNodes)
        {
        	NBTTagCompound tagCompound = new NBTTagCompound();
        	wrapper.write(tagCompound);
        	cleaningList.appendTag(tagCompound);
        }
        
        if(!cleaningList.tagList.isEmpty())
        {
        	nbtTags.setTag("cleaningNodes", cleaningList);
        }
    }
    
    @Override
    public void readFromNBT(NBTTagCompound nbtTags)
    {
    	super.readFromNBT(nbtTags);
    	
    	if(nbtTags.hasKey("liquidTank"))
    	{
    		liquidTank.readFromNBT(nbtTags.getCompoundTag("liquidTank"));
    	}
    	
    	if(nbtTags.hasKey("recurringNodes"))
    	{
    		NBTTagList tagList = nbtTags.getTagList("recurringNodes");
    		
    		for(int i = 0; i < tagList.tagCount(); i++)
    		{
    			recurringNodes.add(BlockWrapper.read((NBTTagCompound)tagList.tagAt(i)));
    		}
    	}
    	
    	if(nbtTags.hasKey("cleaningNodes"))
    	{
    		NBTTagList tagList = nbtTags.getTagList("cleaningNodes");
    		
    		for(int i = 0; i < tagList.tagCount(); i++)
    		{
    			cleaningNodes.add(BlockWrapper.read((NBTTagCompound)tagList.tagAt(i)));
    		}
    	}
    }
    
	@Override
	public boolean isStackValidForSlot(int slotID, ItemStack itemstack)
	{
		if(slotID == 1)
		{
			return false;
		}
		else if(slotID == 0)
		{
			return LiquidContainerRegistry.isEmptyContainer(itemstack);
		}
		else if(slotID == 2)
		{
			return (itemstack.getItem() instanceof IElectricItem && ((IElectricItem)itemstack.getItem()).canProvideEnergy(itemstack)) || 
					(itemstack.getItem() instanceof IItemElectric && ((IItemElectric)itemstack.getItem()).getProvideRequest(itemstack).amperes != 0) || 
					itemstack.itemID == Item.redstone.itemID;
		}
		return true;
	}
    
	@Override
	public boolean func_102008_b(int slotID, ItemStack itemstack, int side)
	{
		if(slotID == 2)
		{
			return (itemstack.getItem() instanceof IItemElectric && ((IItemElectric)itemstack.getItem()).getProvideRequest(itemstack).getWatts() == 0) ||
					(itemstack.getItem() instanceof IElectricItem && ((IElectricItem)itemstack.getItem()).canProvideEnergy(itemstack) && 
							(!(itemstack.getItem() instanceof IItemElectric) || 
							((IItemElectric)itemstack.getItem()).getProvideRequest(itemstack).getWatts() == 0));
		}
		else if(slotID == 1)
		{
			return true;
		}
		
		return false;
	}

	@Override
	public int fill(ForgeDirection from, LiquidStack resource, boolean doFill) 
	{
		return 0;
	}

	@Override
	public int fill(int tankIndex, LiquidStack resource, boolean doFill) 
	{
		return 0;
	}

	@Override
	public LiquidStack drain(ForgeDirection from, int maxDrain, boolean doDrain) 
	{
		return drain(0, maxDrain, doDrain);
	}
	
	@Override
	public int[] getSizeInventorySide(int side)
	{
		if(side == 1)
		{
			return new int[] {0};
		}
		else if(side == 0)
		{
			return new int[] {1};
		}
		else {
			return new int[] {2};
		}
	}

	@Override
	public LiquidStack drain(int tankIndex, int maxDrain, boolean doDrain)
	{
		if(tankIndex == 0)
		{
			return liquidTank.drain(maxDrain, doDrain);
		}
		
		return null;
	}

	@Override
	public ILiquidTank[] getTanks(ForgeDirection direction) 
	{
		return new ILiquidTank[] {liquidTank};
	}

	@Override
	public ILiquidTank getTank(ForgeDirection direction, LiquidStack type) 
	{
		return liquidTank;
	}

	@Override
	public void setLiquidStack(LiquidStack liquidStack, Object... data) 
	{
		liquidTank.setLiquid(liquidStack);
	}

	@Override
	public LiquidStack getLiquidStack(Object... data) 
	{
		return liquidTank.getLiquid();
	}

	@Override
	public boolean hasTank(Object... data) 
	{
		return true;
	}
}
