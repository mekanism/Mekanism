package mekanism.common;

import java.util.List;

import ic2.api.IElectricItem;
import mekanism.api.EnumGas;
import mekanism.api.IStorageTank;
import mekanism.api.IEnergyCube.EnumTier;
import net.minecraft.src.*;

public class ItemStorageTank extends ItemMekanism implements IStorageTank
{
	/** The maximum amount of gas this tank can hold. */
	public int MAX_GAS;
	
	/** How fast this tank can transfer gas. */
	public int TRANSFER_RATE;
	
	/** The number that, when the max amount of gas is divided by, will make it equal 100. */
	public int DIVIDER;
	
	public ItemStorageTank(int id, int maxGas, int transferRate, int divide)
	{
		super(id);
		DIVIDER = divide;
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
		int gas = getGas(itemstack);
		
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
    	ItemStorageTank item = ((ItemStorageTank)itemstack.getItem());
    	item.setGas(itemstack, item.getGasType(itemstack), item.getGas(itemstack));
    	item.setGasType(itemstack, item.getGasType(itemstack));
    	
    	if(item.getGas(itemstack) == 0)
    	{
    		item.setGasType(itemstack, EnumGas.NONE);
    	}
    }
	
	@Override
	public int getGas(ItemStack itemstack)
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
		
		itemstack.setItemDamage((MAX_GAS - stored)/DIVIDER);
		return stored;
	}
	
	@Override
	public void setGas(ItemStack itemstack, EnumGas type, int hydrogen)
	{
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
			int stored = Math.max(Math.min(hydrogen, MAX_GAS), 0);
			itemstack.stackTagCompound.setInteger("gas", stored);
	        itemstack.setItemDamage((MAX_GAS - stored)/DIVIDER);
		}
		
		if(getGas(itemstack) == 0)
		{
			setGasType(itemstack, EnumGas.NONE);
		}
	}
	
	public ItemStack getEmptyItem()
	{
		ItemStack empty = new ItemStack(this);
		empty.setItemDamage(100);
		setGasType(empty, EnumGas.NONE);
		return empty;
	}
	
	@Override
	public void getSubItems(int i, CreativeTabs tabs, List list)
	{
		ItemStack empty = new ItemStack(this);
		empty.setItemDamage(100);
		list.add(empty);
		
		for(EnumGas type : EnumGas.values())
		{
			if(type != EnumGas.NONE)
			{
				ItemStack charged = new ItemStack(this);
				setGasType(charged, type);
				setGas(charged, type, ((IStorageTank)charged.getItem()).getMaxGas());
				list.add(charged);
			}
		}
	}
	
	@Override
	public int getMaxGas()
	{
		return MAX_GAS;
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
			int rejects = Math.max((getGas(itemstack) + amount) - MAX_GAS, 0);
			setGas(itemstack, type, getGas(itemstack) + amount - rejects);
			return rejects;
		}
		return amount;
	}

	@Override
	public int removeGas(ItemStack itemstack, EnumGas type, int amount)
	{
		if(getGasType(itemstack) == type)
		{
			int hydrogenToUse = Math.min(getGas(itemstack), amount);
			setGas(itemstack, type, getGas(itemstack) - hydrogenToUse);
			return hydrogenToUse;
		}
		
		return 0;
	}
	
	@Override
	public int getDivider()
	{
		return DIVIDER;
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
