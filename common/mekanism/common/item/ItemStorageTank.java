package mekanism.common.item;

import java.util.List;

import mekanism.api.IStorageTank;
import mekanism.api.gas.EnumGas;
import mekanism.common.Mekanism;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;

public class ItemStorageTank extends ItemMekanism implements IStorageTank
{
	/** The maximum amount of gas this tank can hold. */
	public int MAX_GAS;
	
	/** How fast this tank can transfer gas. */
	public int TRANSFER_RATE;
	
	public ItemStorageTank(int id, int maxGas, int transferRate)
	{
		super(id);
		MAX_GAS = maxGas;
		TRANSFER_RATE = transferRate;
		setMaxStackSize(1);
		setMaxDamage(100);
		setNoRepair();
		setCreativeTab(Mekanism.tabMekanism);
	}
	
	@Override
	public void addInformation(ItemStack itemstack, EntityPlayer entityplayer, List list, boolean flag)
	{
		int gas = getGas(getGasType(itemstack), itemstack);
		
		if(getGasType(itemstack) == EnumGas.NONE)
		{
			list.add("No gas stored.");
		}
		else {
			list.add("Stored " + getGasType(itemstack).name + ": " + gas);
		}
	}
	
	@Override
	public void onCreated(ItemStack itemstack, World world, EntityPlayer entityplayer)
	{
		itemstack = getEmptyItem();
	}
	
	@Override
    public void onUpdate(ItemStack itemstack, World world, Entity entity, int i, boolean flag)
    {
    	if(getGasType(itemstack) != EnumGas.NONE && getGas(getGasType(itemstack), itemstack) == 0)
    	{
    		setGasType(itemstack, EnumGas.NONE);
    	}
    }
	
	@Override
	public int getGas(EnumGas type, Object... data)
	{
		if(data[0] instanceof ItemStack)
		{
			ItemStack itemstack = (ItemStack)data[0];
			
			if(getGasType(itemstack) == type || type == EnumGas.NONE)
			{
				if(itemstack.stackTagCompound == null)
				{
					return 0;
				}
				
				int stored = 0;
				
				if(itemstack.stackTagCompound.getTag("gas") != null)
				{
					stored = itemstack.stackTagCompound.getInteger("gas");
				}
				
				itemstack.setItemDamage((int)(Math.abs((((float)stored/MAX_GAS)*100)-100)));
				return stored;
			}
		}
		
		return 0;
	}
	
	@Override
	public void setGas(EnumGas type, int amount, Object... data)
	{
		if(data[0] instanceof ItemStack)
		{
			ItemStack itemstack = (ItemStack)data[0];
			
			if(itemstack.stackTagCompound == null)
			{
				itemstack.setTagCompound(new NBTTagCompound());
			}
			
			if(getGasType(itemstack) == EnumGas.NONE)
			{
				setGasType(itemstack, type);
			}
			
			if(getGasType(itemstack) == type)
			{
				int stored = Math.max(Math.min(amount, MAX_GAS), 0);
				itemstack.stackTagCompound.setInteger("gas", stored);
				itemstack.setItemDamage((int)(Math.abs((((float)stored/MAX_GAS)*100)-100)));
			}
			
			if(getGas(getGasType(itemstack), itemstack) == 0)
			{
				setGasType(itemstack, EnumGas.NONE);
			}
		}
	}
	
	public ItemStack getEmptyItem()
	{
		ItemStack empty = new ItemStack(this);
		setGasType(empty, EnumGas.NONE);
		empty.setItemDamage(100);
		return empty;
	}
	
	@Override
	public void getSubItems(int i, CreativeTabs tabs, List list)
	{
		ItemStack empty = new ItemStack(this);
		setGasType(empty, EnumGas.NONE);
		empty.setItemDamage(100);
		list.add(empty);
		
		for(EnumGas type : EnumGas.values())
		{
			if(type != EnumGas.NONE)
			{
				ItemStack filled = new ItemStack(this);
				setGasType(filled, type);
				setGas(type, ((IStorageTank)filled.getItem()).getMaxGas(type, filled), filled);
				list.add(filled);
			}
		}
	}
	
	@Override
	public int getMaxGas(EnumGas type, Object... data)
	{
		if(data[0] instanceof ItemStack)
		{
			ItemStack itemStack = (ItemStack)data[0];
			IStorageTank tank = (IStorageTank)itemStack.getItem();
			
			if(getGasType(itemStack) == EnumGas.NONE || getGasType(itemStack) == type || type == EnumGas.NONE)
			{
				return MAX_GAS;
			}
		}
		
		return 0;
	}

	@Override
	public int getRate() 
	{
		return TRANSFER_RATE;
	}

	@Override
	public int addGas(ItemStack itemstack, EnumGas type, int amount) 
	{
		if(getGasType(itemstack) == type || getGasType(itemstack) == EnumGas.NONE)
		{
			int rejects = Math.max((getGas(getGasType(itemstack), itemstack) + amount) - MAX_GAS, 0);
			setGas(type, getGas(type, itemstack) + amount - rejects, itemstack);
			return rejects;
		}
		
		return amount;
	}

	@Override
	public int removeGas(ItemStack itemstack, EnumGas type, int amount)
	{
		if(getGasType(itemstack) == type)
		{
			int gasToUse = Math.min(getGas(type, itemstack), amount);
			setGas(type, getGas(type, itemstack) - gasToUse, itemstack);
			return gasToUse;
		}
		
		return 0;
	}
	
	@Override
	public boolean canReceiveGas(ItemStack itemstack, EnumGas type)
	{
		return getGasType(itemstack) == type || getGasType(itemstack) == EnumGas.NONE;
	}
	
	@Override
	public boolean canProvideGas(ItemStack itemstack, EnumGas type)
	{
		return getGasType(itemstack) == type;
	}

	@Override
	public EnumGas getGasType(ItemStack itemstack) 
	{
		if(itemstack.stackTagCompound == null) 
		{ 
			return EnumGas.NONE; 
		}
		
		if(itemstack.stackTagCompound.getString("type") == null)
		{
			return EnumGas.NONE;
		}
		
		return EnumGas.getFromName(itemstack.stackTagCompound.getString("gasType"));
	}

	@Override
	public void setGasType(ItemStack itemstack, EnumGas type) 
	{
		if(itemstack.stackTagCompound == null)
		{
			itemstack.setTagCompound(new NBTTagCompound());
		}

		itemstack.stackTagCompound.setString("gasType", type.name);
	}
}
