package mekanism.api;

import net.minecraft.item.ItemStack;

/**
 * The types of infuse currently available in Mekanism.
 * @author AidanBrady
 *
 */
public class InfusionType 
{
	//depricated
	/*COAL("COAL"),
	TIN("TIN"),
	DIAMOND("DIAMOND"),
	BIO("BIO"),
	NONE("NONE");*/
	
	
	/** ItemStack of an item used as infuse, used only for recepie viewer now */
	public ItemStack item;
	
	/** The name of this infusion */
	public String name;
	
	/**
     * Use this to add new infusion types
     * @param recipe The recipe index to get the result for.
     * @param item ItemStack of an item used for this infusion, used only for recepie viewer now
     */
	public InfusionType(String s, ItemStack i)
	{
		item = i;
		name = s;
	}
}
