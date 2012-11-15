package mekanism.common;

import java.util.List;

import ic2.api.IElectricItem;
import mekanism.api.IStorageTank;
import net.minecraft.src.*;

public abstract class ItemStorageTank extends ItemMekanism implements IStorageTank
{
	public int MAX_GAS;
	public int TRANSFER_RATE;
	public int DIVIDER;
	
	public ItemStorageTank(int id, int gas, int rate, int divide)
	{
		super(id);
		DIVIDER = divide;
		MAX_GAS = gas;
		TRANSFER_RATE = rate;
		setMaxStackSize(1);
		setMaxDamage(100);
		setNoRepair();
		setCreativeTab(Mekanism.tabMekanism);
	}
	
	@Override
	public void addInformation(ItemStack itemstack, EntityPlayer entityplayer, List list, boolean flag)
	{
		int gas = getGas(itemstack);
		
		list.add("Stored " + gasType().name + ": " + gas);
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
    	item.setGas(itemstack, item.getGas(itemstack));
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
	public void setGas(ItemStack itemstack, int hydrogen)
	{
		if(itemstack.stackTagCompound == null)
		{
			itemstack.setTagCompound(new NBTTagCompound());
		}
		
		int stored = Math.max(Math.min(hydrogen, MAX_GAS), 0);
		itemstack.stackTagCompound.setInteger("gas", stored);
        itemstack.setItemDamage((MAX_GAS - stored)/DIVIDER);
	}
	
	public ItemStack getEmptyItem()
	{
		ItemStack empty = new ItemStack(this);
		empty.setItemDamage(100);
		return empty;
	}
	
	@Override
	public void getSubItems(int i, CreativeTabs tabs, List list)
	{
		ItemStack empty = new ItemStack(this);
		empty.setItemDamage(100);
		list.add(empty);
		ItemStack charged = new ItemStack(this);
		setGas(charged, ((IStorageTank)charged.getItem()).getMaxGas());
		list.add(charged);
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
	public int addGas(ItemStack itemstack, int amount) 
	{
		int rejects = Math.max((getGas(itemstack) + amount) - MAX_GAS, 0);
		setGas(itemstack, getGas(itemstack) + amount - rejects);
		return rejects;
	}

	@Override
	public int removeGas(ItemStack itemstack, int amount)
	{
		int hydrogenToUse = Math.min(getGas(itemstack), amount);
		setGas(itemstack, getGas(itemstack) - hydrogenToUse);
		return hydrogenToUse;
	}
	
	@Override
	public int getDivider()
	{
		return DIVIDER;
	}
	
	@Override
	public boolean canReceiveGas()
	{
		return true;
	}
	
	@Override
	public boolean canProvideGas()
	{
		return true;
	}
}
