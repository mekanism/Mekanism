package mekanism.common;

import java.util.ArrayList;

import mekanism.common.tileentity.TileEntityContainerBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;

import com.google.common.io.ByteArrayDataInput;

public class TileComponentUpgrade implements ITileComponent
{
	/** How long it takes this machine to install an upgrade. */
	public static int UPGRADE_TICKS_REQUIRED = 40;
	
	/** The inventory slot the upgrade slot of this component occupies. */
	private int upgradeSlot;
	
	/** How many upgrade ticks have progressed. */
	public int upgradeTicks;
	
	/** This machine's speed multiplier. */
	public int speedMultiplier;
	
	/** This machine's energy multiplier. */
	public int energyMultiplier;
	
	/** TileEntity implementing this component. */
	public TileEntityContainerBlock tileEntity;
	
	public TileComponentUpgrade(TileEntityContainerBlock tile, int slot)
	{
		tileEntity = tile;
		upgradeSlot = slot;
		
		tile.components.add(this);
	}
	
	@Override
	public void tick()
	{
		if(!tileEntity.worldObj.isRemote)
		{
			if(tileEntity.inventory[upgradeSlot] != null)
			{
				if(tileEntity.inventory[upgradeSlot].isItemEqual(new ItemStack(Mekanism.EnergyUpgrade)) && energyMultiplier < 8)
				{
					if(upgradeTicks < UPGRADE_TICKS_REQUIRED)
					{
						upgradeTicks++;
					}
					else if(upgradeTicks == UPGRADE_TICKS_REQUIRED)
					{
						upgradeTicks = 0;
						energyMultiplier++;
						
						tileEntity.inventory[upgradeSlot].stackSize--;
						
						if(tileEntity.inventory[upgradeSlot].stackSize == 0)
						{
							tileEntity.inventory[upgradeSlot] = null;
						}
					}
				}
				else if(tileEntity.inventory[upgradeSlot].isItemEqual(new ItemStack(Mekanism.SpeedUpgrade)) && speedMultiplier < 8)
				{
					if(upgradeTicks < UPGRADE_TICKS_REQUIRED)
					{
						upgradeTicks++;
					}
					else if(upgradeTicks == UPGRADE_TICKS_REQUIRED)
					{
						upgradeTicks = 0;
						speedMultiplier++;
						
						tileEntity.inventory[upgradeSlot].stackSize--;
						
						if(tileEntity.inventory[upgradeSlot].stackSize == 0)
						{
							tileEntity.inventory[upgradeSlot] = null;
						}
					}
				}
				else {
					upgradeTicks = 0;
				}
			}
			else {
				upgradeTicks = 0;
			}
		}
	}
	
	public int getScaledUpgradeProgress(int i)
	{
		return upgradeTicks*i / UPGRADE_TICKS_REQUIRED;
	}
	
	@Override
	public void read(NBTTagCompound nbtTags)
	{
    	speedMultiplier = nbtTags.getInteger("speedMultiplier");
    	energyMultiplier = nbtTags.getInteger("energyMultiplier");
	}
	
	@Override
	public void read(ByteArrayDataInput dataStream)
	{
		speedMultiplier = dataStream.readInt();
		energyMultiplier = dataStream.readInt();
		upgradeTicks = dataStream.readInt();
	}
	
	@Override
	public void write(NBTTagCompound nbtTags)
	{
        nbtTags.setInteger("speedMultiplier", speedMultiplier);
        nbtTags.setInteger("energyMultiplier", energyMultiplier);
	}
	
	@Override
	public void write(ArrayList data)
	{
		data.add(speedMultiplier);
		data.add(energyMultiplier);
		data.add(upgradeTicks);
	}
}
