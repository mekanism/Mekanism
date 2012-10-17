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
 * REQUIRED
 * Extend from this class if your item requires electricity or to be charged.
 * @author Calclavia
 *
 */
public abstract class ItemElectric extends Item implements IItemElectric
{
    public ItemElectric(int id, CreativeTabs tabs)
    {
        super(id);
        this.setMaxStackSize(1);
        this.setMaxDamage((int)this.getMaxWattHours());
        this.setNoRepair();
        this.setCreativeTab(tabs);
    }
    
    public ItemElectric(int id)
    {
        this(id, CreativeTabs.tabTools);
    }

    /**
     * Allows items to add custom lines of information to the mouseover description. If you want to add more
     * information to your item, you can super.addInformation() to keep the electiricty info in the item info bar.
     */
    @Override
    public void addInformation(ItemStack par1ItemStack, List par2List)
    {
        String color = "";
        double watts = this.getWattHours(par1ItemStack);

        if (watts <= this.getMaxWattHours() / 3)
        {
            color = "\u00a74";
        }
        else if (watts > this.getMaxWattHours() * 2 / 3)
        {
            color = "\u00a72";
        }
        else
        {
            color = "\u00a76";
        }

        par2List.add(color + ElectricInfo.getDisplay(ElectricInfo.getWattHours(watts), ElectricUnit.WATT_HOUR) + " - " + Math.round((watts / this.getMaxWattHours()) * 100) + "%");
    }

    /**
     * Make sure you super this method!
     */
    @Override
    public void onUpdate(ItemStack par1ItemStack, World par2World, Entity par3Entity, int par4, boolean par5)
    {
    	//Makes sure the damage is set correctly for this electric item!
    	ItemElectric item = ((ItemElectric)par1ItemStack.getItem());
    	item.setWattHours(item.getWattHours(par1ItemStack), par1ItemStack);
    }
    
    /**
     * Makes sure the item is uncharged when it is crafted and not charged.
     * Change this if you do not want this to happen!
     */
    @Override
    public void onCreated(ItemStack par1ItemStack, World par2World, EntityPlayer par3EntityPlayer)
    {
    	par1ItemStack = this.getUnchargedItemStack();
    }

    /**
     * Called when this item is being "recharged" or receiving electricity.
     * @param wattHourReceive - The amount of watt hours this item is receiving.
     * @param itemStack - The ItemStack of this item
     * @return Return the rejected electricity from this item
     */
    public double onReceiveElectricity(double wattHourReceive, ItemStack itemStack)
    {
        double rejectedElectricity = Math.max((this.getWattHours(itemStack) + wattHourReceive) - this.getMaxWattHours(), 0);
        this.setWattHours(this.getWattHours(itemStack) + wattHourReceive - rejectedElectricity, itemStack);
        return rejectedElectricity;
    }

    /**
     * Called when this item's electricity is being used.
     * @param wattHourRequest - The amount of electricity requested from this item
     * @param itemStack - The ItemStack of this item
     * @return The electricity that is given to the requester
     */
    public double onUseElectricity(double wattHourRequest, ItemStack itemStack)
    {
        double electricityToUse = Math.min(this.getWattHours(itemStack), wattHourRequest);
        this.setWattHours(this.getWattHours(itemStack) - electricityToUse, itemStack);
        return electricityToUse;
    }

    /**
     * @return Returns true or false if this consumer can receive electricity at this given tick or moment.
     */
    public boolean canReceiveElectricity()
    {
        return true;
    }

    /**
     * Can this item give out electricity when placed in an tile entity? Electric items like batteries
     * should be able to produce electricity (if they are rechargable).
     * @return - True or False.
     */
    public boolean canProduceElectricity()
    {
        return false;
    }

    /**
     * This function sets the electriicty. Do not directly call this function.
     * Try to use onReceiveElectricity or onUseElectricity instead.
     * @param wattHours - The amount of electricity in joules
     */
    @Override
    public void setWattHours(double wattHours, Object... data)
    {
    	if(data[0] instanceof ItemStack)
    	{
    		ItemStack itemStack = (ItemStack)data[0];
    		
    		//Saves the frequency in the itemstack
            if (itemStack.stackTagCompound == null)
            {
                itemStack.setTagCompound(new NBTTagCompound());
            }

            double electricityStored = Math.max(Math.min(wattHours, this.getMaxWattHours()), 0);
            itemStack.stackTagCompound.setDouble("electricity", electricityStored);
            itemStack.setItemDamage((int)(getMaxWattHours() - electricityStored));
    	}
    }

    /**
     * This function is called to get the electricity stored in this item
     * @return - The amount of electricity stored in watts
     */
    @Override
    public double getWattHours(Object... data)
    {
    	if(data[0] instanceof ItemStack)
    	{
    		ItemStack itemStack = (ItemStack)data[0];

    		if(itemStack.stackTagCompound == null)
	        {
	            return 0;
	        }
    		double electricityStored=0;
            if (itemStack.stackTagCompound.getTag("electricity") instanceof NBTTagFloat){
            	electricityStored = itemStack.stackTagCompound.getFloat("electricity");
            }else{
                electricityStored = itemStack.stackTagCompound.getDouble("electricity");
            }
	        itemStack.setItemDamage((int)(getMaxWattHours() - electricityStored));
	        return electricityStored;
    	}
    	
    	return -1;
    }

    /**
     * This function is called to get the maximum transfer rate this electric item can receive per tick
     * @return - The amount of electricity maximum capacity
     */
    public abstract double getTransferRate();

    /**
     * Gets the voltage of this item
     * @return The Voltage of this item
     */
    public abstract double getVoltage();

    /**
     * Returns an uncharged version of the electric item. Use this if you want
     * the crafting recipe to use a charged version of the electric item
     * instead of an empty version of the electric item
     * @return The ItemStack of a fully charged electric item
     */
    public ItemStack getUnchargedItemStack()
    {
        ItemStack chargedItem = new ItemStack(this);
        chargedItem.setItemDamage((int) this.getMaxWattHours());
        return chargedItem;
    }

    @Override
    public void getSubItems(int par1, CreativeTabs par2CreativeTabs, List par3List)
    {
    	//Add an uncharged version of the electric item
        ItemStack unchargedItem = new ItemStack(this, 1);
        unchargedItem.setItemDamage((int) this.getMaxWattHours());
        par3List.add(unchargedItem);
        //Add an electric item to the creative list that is fully charged
        ItemStack chargedItem = new ItemStack(this, 1);
        this.setWattHours(((IItemElectric)chargedItem.getItem()).getMaxWattHours(), chargedItem);
        par3List.add(chargedItem);
    }
}
