package mekanism.common;

import java.util.List;

import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagFloat;
import net.minecraft.world.World;
import universalelectricity.core.electricity.ElectricInfo;
import universalelectricity.core.electricity.ElectricInfo.ElectricUnit;
import universalelectricity.core.implement.IItemElectric;

public class ItemEnergized extends ItemMekanism implements IItemElectric
{
	/** The maximum amount of energy this item can hold. */
	public double MAX_ELECTRICITY;
	
	/** How fast this item can transfer energy. */
	public double VOLTAGE;
	
	/** The number that, when the max amount of energy is divided by, will make it equal 100. */
	public int DIVIDER;
	
	public ItemEnergized(int id, double maxElectricity, double voltage, int divider)
	{
		super(id);
		DIVIDER = divider;
		MAX_ELECTRICITY = maxElectricity;
		VOLTAGE = voltage;
		setMaxStackSize(1);
		setMaxDamage(100);
		setNoRepair();
		setCreativeTab(Mekanism.tabMekanism);
	}
	
	@Override
	public void addInformation(ItemStack itemstack, EntityPlayer entityplayer, List list, boolean flag)
	{
		double energy = getJoules(itemstack);
		
		list.add("Stored Energy: " + ElectricInfo.getDisplayShort(energy, ElectricUnit.JOULES));
	}
	
	@Override
	public void onCreated(ItemStack itemstack, World world, EntityPlayer entityplayer)
	{
		itemstack = getUnchargedItem();
	}
	
	@Override
    public void onUpdate(ItemStack itemstack, World world, Entity entity, int i, boolean flag)
    {
    	ItemEnergized item = ((ItemEnergized)itemstack.getItem());
    	item.setJoules(item.getJoules(itemstack), itemstack);
    }
	
	public ItemStack getUnchargedItem()
	{
		ItemStack charged = new ItemStack(this);
		charged.setItemDamage(100);
		return charged;
	}
	
	@Override
	public void getSubItems(int i, CreativeTabs tabs, List list)
	{
		ItemStack discharged = new ItemStack(this);
		discharged.setItemDamage(100);
		list.add(discharged);
		ItemStack charged = new ItemStack(this);
		setJoules(((IItemElectric)charged.getItem()).getMaxJoules(), charged);
		list.add(charged);
	}

	@Override
	public double getJoules(Object... data)
	{
		if (data[0] instanceof ItemStack)
		{
			ItemStack itemStack = (ItemStack) data[0];

			if (itemStack.stackTagCompound == null) 
			{ 
				return 0; 
			}
			
			double electricityStored = 0;
			
			if (itemStack.stackTagCompound.getTag("electricity") instanceof NBTTagFloat)
			{
				electricityStored = itemStack.stackTagCompound.getFloat("electricity");
			}
			else
			{
				electricityStored = itemStack.stackTagCompound.getDouble("electricity");
			}
			
			itemStack.setItemDamage((int)(MAX_ELECTRICITY - electricityStored)/DIVIDER);
			return electricityStored;
		}

		return -1;
	}

	@Override
	public void setJoules(double wattHours, Object... data)
	{
		if (data[0] instanceof ItemStack)
		{
			ItemStack itemStack = (ItemStack) data[0];

			if (itemStack.stackTagCompound == null)
			{
				itemStack.setTagCompound(new NBTTagCompound());
			}

			double electricityStored = Math.max(Math.min(wattHours, getMaxJoules()), 0);
			itemStack.stackTagCompound.setDouble("electricity", electricityStored);
			itemStack.setItemDamage((int)(MAX_ELECTRICITY - electricityStored)/DIVIDER);
		}
	}

	@Override
	public double getMaxJoules(Object... data)
	{
		return MAX_ELECTRICITY;
	}

	@Override
	public double getVoltage() 
	{
		return VOLTAGE;
	}

	@Override
	public double onReceive(double amps, double voltage, ItemStack itemStack)
	{
		double rejectedElectricity = Math.max((getJoules(itemStack) + ElectricInfo.getJoules(amps, voltage, 1)) - getMaxJoules(), 0);
		setJoules(getJoules(itemStack) + ElectricInfo.getJoules(amps, voltage, 1) - rejectedElectricity, itemStack);
		return rejectedElectricity;
	}

	@Override
	public double onUse(double joulesNeeded, ItemStack itemStack)
	{
		double electricityToUse = Math.min(getJoules(itemStack), joulesNeeded);
		setJoules(getJoules(itemStack) - electricityToUse, itemStack);
		return electricityToUse;
	}

	@Override
	public boolean canReceiveElectricity()
	{
		return true;
	}

	@Override
	public boolean canProduceElectricity()
	{
		return true;
	}
}
