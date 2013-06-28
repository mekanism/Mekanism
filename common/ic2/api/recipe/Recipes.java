package ic2.api.recipe;

import net.minecraft.item.ItemStack;

/**
 * General recipe registry.
 * 
 * @author Richard
 */
public class Recipes {
	public static IMachineRecipeManager<ItemStack> macerator;
	public static IMachineRecipeManager<ItemStack> extractor;
	public static IMachineRecipeManager<ItemStack> compressor;
	
	/**
	 * Reference amplifier values:
	 * 
	 * 5000: Scrap
	 * 45000: Scrapbox
	 */
	public static IMachineRecipeManager<Integer> matterAmplifier;
	/**
	 * Reference scrap box chance values:
	 *
	 * 0.1: Diamond
	 * 0.5: Cake, Gold Helmet, Iron Ore, Gold Ore
	 * 1.0: Wooden tools, Soul Sand, Sign, Leather, Feather, Bone
	 * 1.5: Apple, Bread
	 * 2.0: Netherrack, Rotten Flesh
	 * 3.0: Grass, Gravel
	 * 4.0: Stick
	 * 5.0: Dirt, Wooden Hoe
	 */
	public static IMachineRecipeManager<Float> scrapboxDrops;
	public static IListRecipeManager recyclerBlacklist;
	
	public static ICraftingRecipeManager advRecipes;
}
