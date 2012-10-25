package universalelectricity.prefab;

import java.util.List;

import net.minecraft.src.CreativeTabs;
import net.minecraft.src.Entity;
import net.minecraft.src.EntityPlayer;
import net.minecraft.src.Item;
import net.minecraft.src.ItemStack;
import net.minecraft.src.NBTTagCompound;
import net.minecraft.src.NBTTagFloat;
import net.minecraft.src.World;
import universalelectricity.electricity.ElectricInfo;
import universalelectricity.electricity.ElectricInfo.ElectricUnit;
import universalelectricity.implement.IItemElectric;

/**
 * Extend from this class if your item requires electricity or to be charged.
 * Optionally, you can implement IItemElectric instead.
 * 
 * @author Calclavia
 * 
 */
public abstract class ItemElectric extends Item implements IItemElectric
{
	public ItemElectric(int id, CreativeTabs tabs)
	{
		super(id);
		this.setMaxStackSize(1);
		this.setMaxDamage((int) this.getMaxJoules());
		this.setNoRepair();
		this.setCreativeTab(tabs);
	}

	public ItemElectric(int id)
	{
		this(id, CreativeTabs.tabTools);
	}

	/**
	 * Allows items to add custom lines of information to the mouseover
	 * description. If you want to add more information to your item, you can
	 * super.addInformation() to keep the electiricty info in the item info bar.
	 */
	@Override
	public void addInformation(ItemStack par1ItemStack, EntityPlayer par2EntityPlayer, List par3List, boolean par4)
	{
		String color = "";
		double joules = this.getJoules(par1ItemStack);

		if (joules <= this.getMaxJoules() / 3)
		{
			color = "\u00a74";
		}
		else if (joules > this.getMaxJoules() * 2 / 3)
		{
			color = "\u00a72";
		}
		else
		{
			color = "\u00a76";
		}

		par3List.add(color + ElectricInfo.getDisplay(joules, ElectricUnit.JOULES) + " - " + Math.round((joules / this.getMaxJoules()) * 100) + "%");
	}

	/**
	 * Make sure you super this method!
	 */
	@Override
	public void onUpdate(ItemStack par1ItemStack, World par2World, Entity par3Entity, int par4, boolean par5)
	{
		// Makes sure the damage is set correctly for this electric item!
		ItemElectric item = ((ItemElectric) par1ItemStack.getItem());
		item.setJoules(item.getJoules(par1ItemStack), par1ItemStack);
	}

	/**
	 * Makes sure the item is uncharged when it is crafted and not charged.
	 * Change this if you do not want this to happen!
	 */
	@Override
	public void onCreated(ItemStack par1ItemStack, World par2World, EntityPlayer par3EntityPlayer)
	{
		par1ItemStack = this.getUncharged();
	}

	@Override
	public double onReceive(double amps, double voltage, ItemStack itemStack)
	{
		double rejectedElectricity = Math.max((this.getJoules(itemStack) + ElectricInfo.getJoules(amps, voltage, 1)) - this.getMaxJoules(), 0);
		this.setJoules(this.getJoules(itemStack) + ElectricInfo.getJoules(amps, voltage, 1) - rejectedElectricity, itemStack);
		return rejectedElectricity;
	}

	@Override
	public double onUse(double joulesNeeded, ItemStack itemStack)
	{
		double electricityToUse = Math.min(this.getJoules(itemStack), joulesNeeded);
		this.setJoules(this.getJoules(itemStack) - electricityToUse, itemStack);
		return electricityToUse;
	}

	public boolean canReceiveElectricity()
	{
		return true;
	}

	public boolean canProduceElectricity()
	{
		return false;
	}

	/**
	 * This function sets the electriicty. Do not directly call this function.
	 * Try to use onReceiveElectricity or onUseElectricity instead.
	 * 
	 * @param wattHours
	 *            - The amount of electricity in joules
	 */
	@Override
	public void setJoules(double wattHours, Object... data)
	{
		if (data[0] instanceof ItemStack)
		{
			ItemStack itemStack = (ItemStack) data[0];

			// Saves the frequency in the itemstack
			if (itemStack.stackTagCompound == null)
			{
				itemStack.setTagCompound(new NBTTagCompound());
			}

			double electricityStored = Math.max(Math.min(wattHours, this.getMaxJoules()), 0);
			itemStack.stackTagCompound.setDouble("electricity", electricityStored);
			itemStack.setItemDamage((int) (getMaxJoules() - electricityStored));
		}
	}

	/**
	 * This function is called to get the electricity stored in this item
	 * 
	 * @return - The amount of electricity stored in watts
	 */
	@Override
	public double getJoules(Object... data)
	{
		if (data[0] instanceof ItemStack)
		{
			ItemStack itemStack = (ItemStack) data[0];

			if (itemStack.stackTagCompound == null) { return 0; }
			double electricityStored = 0;
			if (itemStack.stackTagCompound.getTag("electricity") instanceof NBTTagFloat)
			{
				electricityStored = itemStack.stackTagCompound.getFloat("electricity");
			}
			else
			{
				electricityStored = itemStack.stackTagCompound.getDouble("electricity");
			}
			itemStack.setItemDamage((int) (getMaxJoules() - electricityStored));
			return electricityStored;
		}

		return -1;
	}

	/**
	 * Returns an uncharged version of the electric item. Use this if you want
	 * the crafting recipe to use a charged version of the electric item instead
	 * of an empty version of the electric item
	 * 
	 * @return The ItemStack of a fully charged electric item
	 */
	public ItemStack getUncharged()
	{
		ItemStack chargedItem = new ItemStack(this);
		chargedItem.setItemDamage((int) this.getMaxJoules());
		return chargedItem;
	}
	
	public static ItemStack getUncharged(ItemStack itemStack)
	{
		if(itemStack.getItem() instanceof IItemElectric)
		{
			ItemStack chargedItem = itemStack.copy();
			chargedItem.setItemDamage((int)((IItemElectric)itemStack.getItem()).getMaxJoules());
			return chargedItem;
		}
		
		return null;
	}

	@Override
	public void getSubItems(int par1, CreativeTabs par2CreativeTabs, List par3List)
	{
		// Add an uncharged version of the electric item
		ItemStack unchargedItem = new ItemStack(this, 1);
		unchargedItem.setItemDamage((int) this.getMaxJoules());
		par3List.add(unchargedItem);
		// Add an electric item to the creative list that is fully charged
		ItemStack chargedItem = new ItemStack(this, 1);
		this.setJoules(((IItemElectric) chargedItem.getItem()).getMaxJoules(), chargedItem);
		par3List.add(chargedItem);
	}
}
