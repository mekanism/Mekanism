package obsidian.api;

import net.minecraft.src.*;

/**
 * Use this class's 'getItem()' method to retrieve ItemStacks from the 'ObsidianIngots'
 * class.
 * @author AidanBrady
 *
 */
public final class ItemRetriever 
{
	/** The 'ObsidianIngots' class that items and blocks are retrieved from. */
	private static Class ObsidianIngots;
	
	/**
	 * Attempts to retrieve an ItemStack of an item or block with the declared identifier.
	 * 
	 * ObsidianIngots identifiers follow an easy-to-remember pattern.  All identifiers
	 * are identical to the String returned by 'getItemName().'  None include spaces, 
	 * and always make sure you start with a capital letter. The name that shows up
	 * in-game can be stripped down to identifier form by removing spaces and all non-
	 * alphabetic characters (,./'=-_). Below is an example:
	 * 
	 * ItemStack enrichedAlloy = ItemRetriever.getItem("EnrichedAlloy");
	 * 
	 * The same also works for blocks.
	 * 
	 * ItemStack refinedObsidian = ItemRetriever.getItem("RefinedObsidian");
	 * 
	 * Make sure you run this in or after FMLPostInitializationEvent runs, because most
	 * items are registered when FMLInitializationEvent runs. However, some items ARE 
	 * registered later in order to hook into other mods. In a rare circumstance you may
	 * have to add "after:ObsidianIngots" in the @Mod 'dependencies' annotation.
	 * 
	 * Note that you will be able to retrieve items that Obsidian Ingots has retrieved
	 * from other mods. In other words, if IC2 is installed, 'getItem("GoldDust")' will
	 * return IndustrialCraft gold dust.
	 * 
	 * @param identifier - a String to be searched in the 'ObsidianIngots' class
	 * @return an ItemStack of the declared identifier, otherwise null.
	 */
	public static ItemStack getItem(String identifier)
	{
		try {
			if(ObsidianIngots == null)
			{
				ObsidianIngots = Class.forName("net.uberkat.obsidian.common.ObsidianIngots");
			}
			
			Object ret = ObsidianIngots.getField(identifier).get(null);
			
			if(ret instanceof Item)
			{
				return new ItemStack((Item)ret, 1);
			}
			else {
				return null;
			}
		} catch(Exception e) {
			System.err.println("[ObsidianIngots] Error retrieving item with identifier '" + identifier + "': " + e.getMessage());
			return null;
		}
	}
}
