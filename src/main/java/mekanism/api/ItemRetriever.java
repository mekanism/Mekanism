package mekanism.api;

import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

/**
 * Use this class's 'getItem()' method to retrieve ItemStacks from the 'Mekanism'
 * class.
 * @author AidanBrady
 *
 */
public final class ItemRetriever
{
	/** The 'MekanismItems' class that items are retrieved from. */
	private static Class MekanismItems;
	
	/** The 'MekanismBlocks' class that blocks are retrieved from. */
	private static Class MekanismBlocks;

	/**
	 * Attempts to retrieve an ItemStack of an item with the declared identifier.
	 *
	 * Mekanism identifiers follow an easy-to-remember pattern.  All identifiers
	 * are identical to the String returned by 'getItemName().'  None include spaces,
	 * and all start with a capital letter. The name that shows up in-game can be
	 * stripped down to identifier form by removing spaces and all non-alphabetic
	 * characters (,./'=-_). Below is an example:
	 *
	 * ItemStack enrichedAlloy = ItemRetriever.getItem("EnrichedAlloy");
	 *
	 * Note that for items or blocks that have specific metadata you will need to create
	 * a new ItemStack with that specified value, as this will only return an ItemStack
	 * with the meta value '0.'
	 *
	 * Make sure you run this in or after FMLPostInitializationEvent runs, because most
	 * items are registered when FMLInitializationEvent runs. However, some items ARE
	 * registered later in order to hook into other mods. In a rare circumstance you may
	 * have to add "after:Mekanism" in the @Mod 'dependencies' annotation.
	 *
	 * @param identifier - a String to be searched in the 'MekanismItems' class
	 * @return an ItemStack of the declared identifier, otherwise null.
	 */
	public static ItemStack getItem(String identifier)
	{
		try {
			if(MekanismItems == null)
			{
				MekanismItems = Class.forName("mekanism.common.MekanismItems");
			}

			Object ret = MekanismItems.getField(identifier).get(null);

			if(ret instanceof Item)
			{
				return new ItemStack((Item)ret, 1);
			}
			else {
				return null;
			}
		} catch(Exception e) {
			System.err.println("Error retrieving item with identifier '" + identifier + "': " + e.getMessage());
			return null;
		}
	}
	
	/**
	 * Attempts to retrieve an ItemStack of a block with the declared identifier.
	 *
	 * Mekanism identifiers follow an easy-to-remember pattern.  All identifiers
	 * are identical to the String returned by 'getItemName().'  None include spaces,
	 * and all start with a capital letter. The name that shows up in-game can be
	 * stripped down to identifier form by removing spaces and all non-alphabetic
	 * characters (,./'=-_). Below is an example:
	 *
	 * ItemStack enrichedAlloy = ItemRetriever.getItem("EnrichedAlloy");
	 *
	 * Note that for items or blocks that have specific metadata you will need to create
	 * a new ItemStack with that specified value, as this will only return an ItemStack
	 * with the meta value '0.'
	 *
	 * Make sure you run this in or after FMLPostInitializationEvent runs, because most
	 * items are registered when FMLInitializationEvent runs. However, some items ARE
	 * registered later in order to hook into other mods. In a rare circumstance you may
	 * have to add "after:Mekanism" in the @Mod 'dependencies' annotation.
	 *
	 * @param identifier - a String to be searched in the 'MekanismBlocks' class
	 * @return an ItemStack of the declared identifier, otherwise null.
	 */
	public static ItemStack getBlock(String identifier)
	{
		try {
			if(MekanismBlocks == null)
			{
				MekanismBlocks = Class.forName("mekanism.common.MekanismBlocks");
			}

			Object ret = MekanismBlocks.getField(identifier).get(null);

			if(ret instanceof Block)
			{
				return new ItemStack((Block)ret, 1);
			}
			else {
				return null;
			}
		} catch(Exception e) {
			System.err.println("Error retrieving block with identifier '" + identifier + "': " + e.getMessage());
			return null;
		}
	}
}
