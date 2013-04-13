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
import universalelectricity.core.electricity.ElectricityPack;

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
		double joules = this.getJoules(itemStack);

		if (joules <= this.getMaxJoules(itemStack) / 3)
		{
			color = "\u00a74";
		}
		else if (joules > this.getMaxJoules(itemStack) * 2 / 3)
		{
			color = "\u00a72";
		}
		else
		{
			color = "\u00a76";
		}

		list.add(color + ElectricityDisplay.getDisplay(joules, ElectricUnit.JOULES) + "/" + ElectricityDisplay.getDisplay(this.getMaxJoules(itemStack), ElectricUnit.JOULES));
	}

	/**
	 * Makes sure the item is uncharged when it is crafted and not charged. Change this if you do
	 * not want this to happen!
	 */
	@Override
	public void onCreated(ItemStack itemStack, World par2World, EntityPlayer par3EntityPlayer)
	{
		itemStack = ElectricItemHelper.getUncharged(itemStack);
	}

	@Override
	public ElectricityPack onReceive(ElectricityPack electricityPack, ItemStack itemStack)
	{
		double rejectedElectricity = Math.max((this.getJoules(itemStack) + electricityPack.getWatts()) - this.getMaxJoules(itemStack), 0);
		double joulesToStore = electricityPack.getWatts() - rejectedElectricity;
		this.setJoules(this.getJoules(itemStack) + joulesToStore, itemStack);
		return ElectricityPack.getFromWatts(joulesToStore, this.getVoltage(itemStack));
	}

	@Override
	public ElectricityPack onProvide(ElectricityPack electricityPack, ItemStack itemStack)
	{
		double electricityToUse = Math.min(this.getJoules(itemStack), electricityPack.getWatts());
		this.setJoules(this.getJoules(itemStack) - electricityToUse, itemStack);
		return ElectricityPack.getFromWatts(electricityToUse, this.getVoltage(itemStack));
	}

	@Override
	public ElectricityPack getReceiveRequest(ItemStack itemStack)
	{
		return ElectricityPack.getFromWatts(Math.min(this.getMaxJoules(itemStack) - this.getJoules(itemStack), this.getTransferRate(itemStack)), this.getVoltage(itemStack));
	}

	@Override
	public ElectricityPack getProvideRequest(ItemStack itemStack)
	{
		return ElectricityPack.getFromWatts(Math.min(this.getJoules(itemStack), this.getTransferRate(itemStack)), this.getVoltage(itemStack));
	}

	public double getTransferRate(ItemStack itemStack)
	{
		return this.getMaxJoules(itemStack) * 0.01;
	}

	/**
	 * This function sets the electriicty. Do not directly call this function. Try to use
	 * onReceiveElectricity or onUseElectricity instead.
	 * 
	 * @param joules - The amount of electricity in joules
	 */
	@Override
	public void setJoules(double joules, ItemStack itemStack)
	{
		// Saves the frequency in the ItemStack
		if (itemStack.getTagCompound() == null)
		{
			itemStack.setTagCompound(new NBTTagCompound());
		}

		double electricityStored = Math.max(Math.min(joules, this.getMaxJoules(itemStack)), 0);
		itemStack.getTagCompound().setDouble("electricity", electricityStored);

		/**
		 * Sets the damage as a percentage to render the bar properly.
		 */
		itemStack.setItemDamage((int) (100 - (electricityStored / getMaxJoules(itemStack)) * 100));
	}

	/**
	 * This function is called to get the electricity stored in this item
	 * 
	 * @return - The amount of electricity stored in watts
	 */
	@Override
	public double getJoules(ItemStack itemStack)
	{
		if (itemStack.getTagCompound() == null)
		{
			return 0;
		}

		double electricityStored = itemStack.getTagCompound().getDouble("electricity");

		/**
		 * Sets the damage as a percentage to render the bar properly.
		 */
		itemStack.setItemDamage((int) (100 - (electricityStored / getMaxJoules(itemStack)) * 100));
		return electricityStored;
	}

	@Override
	public void getSubItems(int par1, CreativeTabs par2CreativeTabs, List par3List)
	{
		// Add an uncharged version of the electric item
		par3List.add(ElectricItemHelper.getUncharged(new ItemStack(this)));
		// Add an electric item to the creative list that is fully charged
		ItemStack chargedItem = new ItemStack(this);
		par3List.add(ElectricItemHelper.getWithCharge(chargedItem, this.getMaxJoules(chargedItem)));
	}
}
