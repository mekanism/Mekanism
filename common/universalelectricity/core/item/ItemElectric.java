package universalelectricity.core.item;

import java.util.List;

import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;
import universalelectricity.core.electricity.ElectricityDisplay;
import universalelectricity.core.electricity.ElectricityDisplay.ElectricUnit;

/**
 * Extend from this class if your item requires electricity or to be charged. Optionally, you can
 * implement IItemElectric instead.
 * 
 * @author Calclavia
 * 
 */
public abstract class ItemElectric extends Item implements IItemElectric
{
	public ItemElectric(int id)
	{
		super(id);
		this.setMaxStackSize(1);
		this.setMaxDamage(100);
		this.setNoRepair();
	}

	@Override
	public void addInformation(ItemStack itemStack, EntityPlayer entityPlayer, List list, boolean par4)
	{
		String color = "";
		float joules = this.getElectricityStored(itemStack);

		if (joules <= this.getMaxElectricityStored(itemStack) / 3)
		{
			color = "\u00a74";
		}
		else if (joules > this.getMaxElectricityStored(itemStack) * 2 / 3)
		{
			color = "\u00a72";
		}
		else
		{
			color = "\u00a76";
		}

		list.add(color + ElectricityDisplay.getDisplayShort(joules, ElectricUnit.JOULES) + "/" + ElectricityDisplay.getDisplayShort(this.getMaxElectricityStored(itemStack), ElectricUnit.JOULES));
	}

	/**
	 * Makes sure the item is uncharged when it is crafted and not charged. Change this if you do
	 * not want this to happen!
	 */
	@Override
	public void onCreated(ItemStack itemStack, World par2World, EntityPlayer par3EntityPlayer)
	{
		this.setElectricity(itemStack, 0);
	}

	@Override
	public float recharge(ItemStack itemStack, float energy, boolean doReceive)
	{
		float rejectedElectricity = Math.max((this.getElectricityStored(itemStack) + energy) - this.getMaxElectricityStored(itemStack), 0);
		float energyToReceive = energy - rejectedElectricity;

		if (doReceive)
		{
			this.setElectricity(itemStack, this.getElectricityStored(itemStack) + energyToReceive);
		}

		return energyToReceive;
	}

	@Override
	public float discharge(ItemStack itemStack, float energy, boolean doTransfer)
	{
		float energyToTransfer = Math.min(this.getElectricityStored(itemStack), energy);

		if (doTransfer)
		{
			this.setElectricity(itemStack, this.getElectricityStored(itemStack) - energyToTransfer);
		}

		return energyToTransfer;
	}

	@Override
	public float getVoltage(ItemStack itemStack)
	{
		return 120;
	}

	@Override
	public void setElectricity(ItemStack itemStack, float joules)
	{
		// Saves the frequency in the ItemStack
		if (itemStack.getTagCompound() == null)
		{
			itemStack.setTagCompound(new NBTTagCompound());
		}

		float electricityStored = Math.max(Math.min(joules, this.getMaxElectricityStored(itemStack)), 0);
		itemStack.getTagCompound().setFloat("electricity", electricityStored);

		/**
		 * Sets the damage as a percentage to render the bar properly.
		 */
		itemStack.setItemDamage((int) (100 - (electricityStored / getMaxElectricityStored(itemStack)) * 100));
	}

	@Override
	public float getTransfer(ItemStack itemStack)
	{
		return this.getMaxElectricityStored(itemStack) - this.getElectricityStored(itemStack);
	}

	/**
	 * This function is called to get the electricity stored in this item
	 * 
	 * @return - The amount of electricity stored in watts
	 */
	@Override
	public float getElectricityStored(ItemStack itemStack)
	{
		if (itemStack.getTagCompound() == null)
		{
			itemStack.setTagCompound(new NBTTagCompound());
		}

		float electricityStored = itemStack.getTagCompound().getFloat("electricity");

		/**
		 * Sets the damage as a percentage to render the bar properly.
		 */
		itemStack.setItemDamage((int) (100 - (electricityStored / getMaxElectricityStored(itemStack)) * 100));
		return electricityStored;
	}

	@Override
	public void getSubItems(int par1, CreativeTabs par2CreativeTabs, List par3List)
	{
		// Add an uncharged version of the electric item
		par3List.add(ElectricItemHelper.getUncharged(new ItemStack(this)));
		// Add an electric item to the creative list that is fully charged
		ItemStack chargedItem = new ItemStack(this);
		par3List.add(ElectricItemHelper.getWithCharge(chargedItem, this.getMaxElectricityStored(chargedItem)));
	}
}
