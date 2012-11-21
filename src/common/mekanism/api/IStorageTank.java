package mekanism.api;

import net.minecraft.src.*;

/**
 * Implement this in your item class if it can store or transfer hydrogen.
 * @author AidanBrady
 *
 */
public interface IStorageTank
{
	/**
	 * Gets the amount of gas the item has from NBT storage.
	 * @param itemstack
	 * @return amount of hydrogen
	 */
	public int getGas(ItemStack itemstack);
	
	/**
	 * Sets the hydrogen the item has with NBT.
	 * @param itemstack
	 * @param energy
	 */
	public void setGas(ItemStack itemstack, int hydrogen);
	
	/**
	 * Gets the maximum amount of hydrogen this item can hold.
	 * @return maximum hydrogen
	 */
	public int getMaxGas();
	
	/**
	 * Gets the rate of transfer this item can handle.
	 * @return
	 */
	public int getRate();
	
	/**
	 * Charges the item with the defined amount of hydrogen.
	 * @param itemstack
	 * @param amount
	 * @return leftover hydrogen
	 */
	public int addGas(ItemStack itemstack, int amount);
	
	/**
	 * Removes the defined amount of hydrogen from the item.
	 * @param itemstack
	 * @param amount
	 * @return hydrogen discharged
	 */
	public int removeGas(ItemStack itemstack, int amount);
	
	/**
	 * Gets the divider that gets that returns the max damage as 100.
	 * @return divider
	 */
	public int getDivider();
	
	/**
	 * Whether or not this energized item be given hydrogen.
	 * @return if the item be charged
	 */
	public boolean canReceiveGas();
	
	/**
	 * Whether or not this energized item can give a hydrogen receiver hydrogen.
	 * @return if the item can charge
	 */
	public boolean canProvideGas();
	
	public EnumGas gasType();
	
	public static enum EnumGas 
	{
		NONE("None"),
		OXYGEN("Oxygen"),
		HYDROGEN("Hydrogen");
		
		public String name;
		
		public static EnumGas getFromName(String gasName)
		{
			for(EnumGas gas : values())
			{
				if(gasName.contains(gas.name))
				{
					return gas;
				}
			}
			
			System.out.println("[Mekanism] Invalid gas identifier when retrieving with name.");
			return NONE;
		}
		
		private EnumGas(String s)
		{
			name = s;
		}
	}
}
