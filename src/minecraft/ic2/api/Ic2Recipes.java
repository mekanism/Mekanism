package ic2.api;

import java.lang.reflect.Array;
import java.util.AbstractMap;
import java.util.List;
import java.util.Map;

import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

/**
 * Provides access to Compressor, Extractor and Macerator recipes, as well as charge-aware recipes
 * and the Recycler blacklist.
 *
 * The recipes are only valid after IC2 has been loaded and are metadata and stack size sensitive,
 * for example you can create a recipe to compress 3 wooden planks into 2 sticks.
 */
public final class Ic2Recipes {
	/**
	 * Add a charge-aware shaped crafting recipe.
	 */
	public static void addCraftingRecipe(ItemStack result, Object... args) {
		try {
			Class.forName(getPackage() + ".core.AdvRecipe").getMethod("addAndRegister", ItemStack.class, Array.newInstance(Object.class, 0).getClass()).invoke(null, result, args);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * Add a charge-aware shapeless crafting recipe.
	 */
	public static void addShapelessCraftingRecipe(ItemStack result, Object... args) {
		try {
			Class.forName(getPackage() + ".core.AdvShapelessRecipe").getMethod("addAndRegister", ItemStack.class, Array.newInstance(Object.class, 0).getClass()).invoke(null, result, args);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
	
	/**
	 * Retrieve the registered Recycler blacklist items.
	 *
	 * @return Blacklist
	 */
	@SuppressWarnings("unchecked")
	public static List<ItemStack> getRecyclerBlacklist() {
		if (TileEntityRecycler_blacklist == null) {
			try {
				TileEntityRecycler_blacklist = (List<ItemStack>) Class.forName(getPackage() + ".core.block.machine.tileentity.TileEntityRecycler").getField("blacklist").get(null);
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
		}

		return TileEntityRecycler_blacklist;
	}

	/**
	 * Add an item stack to the Recycler blacklist.
	 *
	 * @param newBlacklistedItem item stack to add
	 */
	public static void addRecyclerBlacklistItem(ItemStack newBlacklistedItem) {
		getRecyclerBlacklist().add(newBlacklistedItem);
	}

	/**
	 * Add an item to the Recycler blacklist.
	 *
	 * @param newBlacklistedItem item to add
	 */
	public static void addRecyclerBlacklistItem(Item newBlacklistedItem) {
		addRecyclerBlacklistItem(new ItemStack(newBlacklistedItem, 1, -1));
	}

	/**
	 * Add a block to the Recycler blacklist.
	 *
	 * @param newBlacklistedBlock block to add
	 */
	public static void addRecyclerBlacklistItem(Block newBlacklistedBlock) {
		addRecyclerBlacklistItem(new ItemStack(newBlacklistedBlock, 1, -1));
	}


	/**
	 * Determine if an item is in the Recycler blacklist.
	 *
	 * @param itemStack item to check
	 * @return Whether the item is blacklisted or not
	 */
	public static boolean isRecyclerInputBlacklisted(ItemStack itemStack) {
		for (ItemStack blackItem: getRecyclerBlacklist()) {
			if (itemStack.isItemEqual(blackItem)) return true;
		}

		return false;
	}

	/**
	 * Get the base IC2 package name, used internally.
	 *
	 * @return IC2 package name, if unable to be determined defaults to ic2
	 */
	private static String getPackage() {
		Package pkg = Ic2Recipes.class.getPackage();
		if (pkg != null) return pkg.getName().substring(0, pkg.getName().lastIndexOf('.'));
		else return "ic2";
	}

	private static List<Map.Entry<ItemStack, ItemStack> > TileEntityCompressor_recipes;
	private static List<Map.Entry<ItemStack, ItemStack> > TileEntityExtractor_recipes;
	private static List<Map.Entry<ItemStack, ItemStack> > TileEntityMacerator_recipes;
	private static List<ItemStack> TileEntityRecycler_blacklist;
	private static List<Map.Entry<ItemStack, Integer> > TileEntityMatter_amplifiers;
}

