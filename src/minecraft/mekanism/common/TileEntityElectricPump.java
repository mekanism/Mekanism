package mekanism.common;

import java.util.ArrayList;

import com.google.common.io.ByteArrayDataInput;

import ic2.api.ElectricItem;
import ic2.api.IElectricItem;
import universalelectricity.core.item.ElectricItemHelper;
import mekanism.api.InfusionType;
import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.liquids.LiquidContainerRegistry;
import net.minecraftforge.liquids.LiquidStack;
import net.minecraftforge.liquids.LiquidTank;

public class TileEntityElectricPump extends TileEntityElectricBlock
{
	public LiquidTank liquidTank;
	
	public TileEntityElectricPump()
	{
		super("Electric Pump", 16000);
		liquidTank = new LiquidTank(16000);
		inventory = new ItemStack[3];
		liquidTank.setLiquid(new LiquidStack(Block.waterStill.blockID, 8000, 0));
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
			if(liquidTank.getLiquid() != null && liquidTank.getLiquid().amount >= 1000)
			{
				if(LiquidContainerRegistry.isEmptyContainer(inventory[0]))
				{
					ItemStack tempStack = LiquidContainerRegistry.fillLiquidContainer(liquidTank.getLiquid(), inventory[0]);
					
					if(tempStack != null)
					{
						if(inventory[1] == null)
						{
							liquidTank.drain(1000, true);
							
							inventory[1] = tempStack;
							inventory[0].stackSize--;
							
							if(inventory[0].stackSize <= 0)
							{
								inventory[0] = null;
							}
						}
						else if(tempStack.isItemEqual(inventory[1]) && tempStack.getMaxStackSize() > inventory[1].stackSize)
						{
							liquidTank.drain(1000, true);
							
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
		return liquidTank.getLiquid() != null ? liquidTank.getLiquid().amount*i / 16000 : 0;
	}
	
    @Override
    public void writeToNBT(NBTTagCompound nbtTags)
    {
        super.writeToNBT(nbtTags);
        
        if(liquidTank.getLiquid() != null)
        {
        	nbtTags.setTag("liquidTank", liquidTank.getLiquid().writeToNBT(new NBTTagCompound()));
        }
    }
    
    @Override
    public void readFromNBT(NBTTagCompound nbtTags)
    {
    	super.readFromNBT(nbtTags);
    	
    	if(nbtTags.hasKey("liquidTank"))
    	{
    		//liquidTank.setLiquid(LiquidStack.loadLiquidStackFromNBT(nbtTags));
    	}
    	
    	liquidTank.setLiquid(new LiquidStack(Block.waterStill.blockID, 8000, 0));
    }
}
