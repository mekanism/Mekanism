package mekanism.common.tile.component;

import io.netty.buffer.ByteBuf;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import mekanism.common.ITileComponent;
import mekanism.common.IUpgradeItem;
import mekanism.common.Upgrade;
import mekanism.common.tile.TileEntityContainerBlock;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraftforge.common.util.Constants.NBT;

public class TileComponentUpgrade implements ITileComponent
{
	/** How long it takes this machine to install an upgrade. */
	public static int UPGRADE_TICKS_REQUIRED = 40;
	
	private Map<Upgrade, Integer> upgrades = new HashMap<Upgrade, Integer>();
	
	private Set<Upgrade> supported = new HashSet<Upgrade>();

	/** The inventory slot the upgrade slot of this component occupies. */
	private int upgradeSlot;

	/** How many upgrade ticks have progressed. */
	public int upgradeTicks;

	/** TileEntity implementing this component. */
	public TileEntityContainerBlock tileEntity;

	public TileComponentUpgrade(TileEntityContainerBlock tile, int slot)
	{
		tileEntity = tile;
		upgradeSlot = slot;
		
		setSupported(Upgrade.SPEED);
		setSupported(Upgrade.ENERGY);

		tile.components.add(this);
	}

	@Override
	public void tick()
	{
		if(!tileEntity.getWorldObj().isRemote)
		{
			if(tileEntity.inventory[upgradeSlot] != null && tileEntity.inventory[upgradeSlot].getItem() instanceof IUpgradeItem)
			{
				Upgrade type = ((IUpgradeItem)tileEntity.inventory[upgradeSlot].getItem()).getUpgradeType(tileEntity.inventory[upgradeSlot]);
				
				if(supports(type) && getUpgrades(type) < type.getMax())
				{
					if(upgradeTicks < UPGRADE_TICKS_REQUIRED)
					{
						upgradeTicks++;
					}
					else if(upgradeTicks == UPGRADE_TICKS_REQUIRED)
					{
						upgradeTicks = 0;
						addUpgrade(type);

						tileEntity.inventory[upgradeSlot].stackSize--;

						if(tileEntity.inventory[upgradeSlot].stackSize == 0)
						{
							tileEntity.inventory[upgradeSlot] = null;
						}

						tileEntity.markDirty();
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
	
	public int getUpgradeSlot()
	{
		return upgradeSlot;
	}

	public int getScaledUpgradeProgress(int i)
	{
		return upgradeTicks*i / UPGRADE_TICKS_REQUIRED;
	}
	
	public int getUpgrades(Upgrade upgrade)
	{
		return upgrades.get(upgrade);
	}
	
	public void addUpgrade(Upgrade upgrade)
	{
		upgrades.put(upgrade, Math.min(upgrade.getMax(), upgrades.get(upgrade)+1));
	}
	
	public void setUpgrades(Upgrade upgrade, int amount)
	{
		upgrades.put(upgrade, amount);
	
		if(upgrades.get(upgrade) == 0)
		{
			upgrades.remove(upgrade);
		}
	}
	
	public void removeUpgrade(Upgrade upgrade)
	{
		upgrades.put(upgrade, Math.max(0, upgrades.get(upgrade)-1));
		
		if(upgrades.get(upgrade) == 0)
		{
			upgrades.remove(upgrade);
		}
	}
	
	public void setSupported(Upgrade upgrade)
	{
		supported.add(upgrade);
	}
	
	public boolean supports(Upgrade upgrade)
	{
		return supported.contains(upgrade);
	}
	
	public NBTTagCompound getTagFor(Upgrade upgrade)
	{
		NBTTagCompound compound = new NBTTagCompound();
		
		compound.setInteger("type", upgrade.ordinal());
		compound.setInteger("amount", getUpgrades(upgrade));
		
		return compound;
	}

	@Override
	public void read(ByteBuf dataStream)
	{
		upgrades.clear();
		
		int amount = dataStream.readInt();
		
		for(int i = 0; i < amount; i++)
		{
			upgrades.put(Upgrade.values()[dataStream.readInt()], dataStream.readInt());
		}
		
		upgradeTicks = dataStream.readInt();
	}
	
	@Override
	public void write(ArrayList data)
	{
		data.add(upgrades.size());
		
		for(Map.Entry<Upgrade, Integer> entry : upgrades.entrySet())
		{
			data.add(entry.getKey().ordinal());
			data.add(entry.getValue());
		}
		
		data.add(upgradeTicks);
	}
	
	@Override
	public void read(NBTTagCompound nbtTags)
	{
		NBTTagList list = nbtTags.getTagList("upgrades", NBT.TAG_COMPOUND);
		
		for(int tagCount = 0; tagCount < list.tagCount(); tagCount++)
		{
			NBTTagCompound compound = (NBTTagCompound)list.getCompoundTagAt(tagCount);
			
			Upgrade upgrade = Upgrade.values()[compound.getInteger("type")];
			upgrades.put(upgrade, compound.getInteger("amount"));
		}
	}

	@Override
	public void write(NBTTagCompound nbtTags)
	{
		NBTTagList list = new NBTTagList();
		
		for(Upgrade upgrade : upgrades.keySet())
		{
			list.appendTag(getTagFor(upgrade));
		}
		
		nbtTags.setTag("upgrades", list);
	}
}
