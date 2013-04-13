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
	 * Retrieve the registered Compressor recipes.
	 *
	 * @return Recipe list as a list of map entries, the key is the input and the value is the output
	 */
	@SuppressWarnings("unchecked")
	public static List<Map.Entry<ItemStack, ItemStack> > getCompressorRecipes() {
		if (TileEntityCompressor_recipes == null) {
			try {
				TileEntityCompressor_recipes = (List<Map.Entry<ItemStack, ItemStack> >) Class.forName(getPackage() + ".core.block.machine.tileentity.TileEntityCompressor").getField("recipes").get(null);
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
		}

		return TileEntityCompressor_recipes;
	}

	/**
	 * Add a Compressor recipe.
	 *
	 * @param input Input
	 * @param output Output
	 */
	public static void addCompressorRecipe(ItemStack input, ItemStack output) {
		getCompressorRecipes().add(new AbstractMap.SimpleEntry<ItemStack, ItemStack>(input, output));
	}


	/**
	 * Get the Compressor output for an input item.
	 *
	 * @param input input item
	 * @param adjustInput remove the processing requirements from input
	 * @return Output item as an independent stack
	 */
	public static ItemStack getCompressorOutputFor(ItemStack input, boolean adjustInput) {
		return getOutputFor(input, adjustInput, getCompressorRecipes());
	}

	/**
	 * Retrieve the registered Extractor recipes.
	 *
	 * @return Recipe list as a list of map entries, the key is the input and the value is the output
	 */
	@SuppressWarnings("unchecked")
	public static List<Map.Entry<ItemStack, ItemStack> > getExtractorRecipes() {
		if (TileEntityExtractor_recipes == null) {
			try {
				TileEntityExtractor_recipes = (List<Map.Entry<ItemStack, ItemStack> >) Class.forName(getPackage() + ".core.block.machine.tileentity.TileEntityExtractor").getField("recipes").get(null);
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
		}

		return TileEntityExtractor_recipes;
	}

	/**
	 * Add a Extractor recipe.
	 *
	 * @param input Input
	 * @param output Output
	 */
	public static void addExtractorRecipe(ItemStack input, ItemStack output) {
		getExtractorRecipes().add(new AbstractMap.SimpleEntry<ItemStack, ItemStack>(input, output));
	}


	/**
	 * Get the Extractor output for an input item.
	 *
	 * @param input input item
	 * @param adjustInput remove the processing requirements from input
	 * @return Output item as an independent stack
	 */
	public static ItemStack getExtractorOutputFor(ItemStack input, boolean adjustInput) {
		return getOutputFor(input, adjustInput, getExtractorRecipes());
	}

	/**
	 * Retrieve the registered Macerator recipes.
	 *
	 * @return Recipe list as a list of map entries, the key is the input and the value is the output
	 */
	@SuppressWarnings("unchecked")
	public static List<Map.Entry<ItemStack, ItemStack> > getMaceratorRecipes() {
		if (TileEntityMacerator_recipes == null) {
			try {
				TileEntityMacerator_recipes = (List<Map.Entry<ItemStack, ItemStack> >) Class.forName(getPackage() + ".core.block.machine.tileentity.TileEntityMacerator").getField("recipes").get(null);
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
		}

		return TileEntityMacerator_recipes;
	}

	/**
	 * Add a Macerator recipe.
	 *
	 * @param input Input
	 * @param output Output
	 */
	public static void addMaceratorRecipe(ItemStack input, ItemStack output) {
		getMaceratorRecipes().add(new AbstractMap.SimpleEntry<ItemStack, ItemStack>(input, output));
	}


	/**
	 * Get the Macerator output for an input item.
	 *
	 * @param input input item
	 * @param adjustInput remove the processing requirements from input
	 * @return Output item as an independent stack
	 */
	public static ItemStack getMaceratorOutputFor(ItemStack input, boolean adjustInput) {
		return getOutputFor(input, adjustInput, getMaceratorRecipes());
	}


	private static ItemStack getOutputFor(ItemStack input, boolean adjustInput, List<Map.Entry<ItemStack, ItemStack> > recipeList) {
		assert input != null;

		for (Map.Entry<ItemStack, ItemStack> entry: recipeList) {
			if (entry.getKey().isItemEqual(input) && input.stackSize >= entry.getKey().stackSize) {
				if (adjustInput) input.stackSize -= entry.getKey().stackSize;

				return entry.getValue().copy();
			}
		}

		return null;
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
	 * Retrieve the registered Scrap Box drops.
	 *
	 * @return Drops as a list of item stack and float (chance) pairs
	 */
	@SuppressWarnings("unchecked")
	public static List<Map.Entry<ItemStack,Float>> getScrapboxDrops() {
		try {
			return (List<Map.Entry<ItemStack,Float>>) Class.forName(getPackage() + ".core.item.ItemScrapbox").getMethod("getDropList").invoke(null);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	/*
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

	/**
	 * Add an item stack to the Scrap Box drops.
	 *
	 * @param dropItem item stack to add
	 * @param chance chance for the item to drop, see the code comments for reference values
	 */
	public static void addScrapboxDrop(ItemStack dropItem, float chance) {
		try {
			Class.forName(getPackage() + ".core.item.ItemScrapbox").getMethod("addDrop", ItemStack.class, float.class).invoke(null, dropItem, chance);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * Add an item to the Scrap Box drops.
	 *
	 * @param dropItem item to add
	 * @param chance chance for the item to drop, see the code comments for reference values
	 */
	public static void addScrapboxDrop(Item dropItem, float chance) {
		addScrapboxDrop(new ItemStack(dropItem, 1), chance);
	}

	/**
	 * Add a block to the Scrap Box drops.
	 *
	 * @param dropItem item to add
	 * @param chance chance for the item to drop, see the code comments for reference values
	 */
	public static void addScrapboxDrop(Block dropItem, float chance) {
		addScrapboxDrop(new ItemStack(dropItem), chance);
	}

	/**
	 * Retrieve the registered Mass Fabricator amplifiers.
	 *
	 * @return Amplifiers as a list of item stack and integer (amplifier value) pairs
	 */
	@SuppressWarnings("unchecked")
	public static List<Map.Entry<ItemStack, Integer> > getMatterAmplifiers() {
		if (TileEntityMatter_amplifiers == null) {
			try {
				TileEntityMatter_amplifiers = (List<Map.Entry<ItemStack, Integer> >) Class.forName(getPackage() + ".core.block.machine.tileentity.TileEntityMatter").getField("amplifiers").get(null);
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
		}

		return TileEntityMatter_amplifiers;
	}

	/**
	 * Add an item stack to the Mass Fabricator amplifiers.
	 *
	 * @param amplifierItem item stack to add
	 * @param value amplifier value for the item, scrap is 5000
	 */
	public static void addMatterAmplifier(ItemStack amplifierItem, int value) {
		getMatterAmplifiers().add(new AbstractMap.SimpleEntry<ItemStack,Integer>(amplifierItem, value));
	}

	/**
	 * Add an item to the Mass Fabricator amplifiers.
	 *
	 * @param amplifierItem item to add
	 * @param value amplifier value for the item, scrap is 5000
	 */
	public static void addMatterAmplifier(Item amplifierItem, int value) {
		addMatterAmplifier(new ItemStack(amplifierItem, 1, -1), value);
	}

	/**
	 * Add a block to the Mass Fabricator amplifiers.
	 *
	 * @param amplifierItem item to add
	 * @param value amplifier value for the item, scrap is 5000
	 */
	public static void addMatterAmplifier(Block amplifierItem, int value) {
		addMatterAmplifier(new ItemStack(amplifierItem, 1, -1), value);
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

