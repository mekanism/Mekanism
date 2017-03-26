package ic2.api.recipe;

/**
 * General recipe registry.
 *
 * <p>IMachineRecipeManager.addRecipe takes a metadata parameter, the expected contents are specified
 * below as "recipe meta". The metadata NBT itself should be null for "no recipe meta" or a
 * NBTTagCompound with keys matching the names and value types mentioned below.
 *
 * <p>Example: Compressor recipe, Sand -> Glass:
 * <pre>{@code
 * Recipes.compressor.addRecipe(new RecipeInputItemStack(new ItemStack(Blocks.SAND)), null, false, new ItemStack(Blocks.GLASS));
 * }</pre>
 *
 * <p>Example: Metal former rolling recipe, Platinum ingot (oredict) -> Platinum plate:
 * <pre>{@code
 * Recipes.metalformerRolling.addRecipe(new RecipeInputOreDict("ingotPlatinum"), null, false, myPlatinumPlateStack)
 * }</pre>
 *
 * <p>Example: Mass fabricator amplifier, nether stars amplifying 1M EU:
 * <pre>{@code
 * NBTTagCompound meta = new NBTTagCompound();
 * meta.setInteger("amplification", 1000000);
 * Recipes.matterAmplifier.addRecipe(new RecipeInputItemStack(new ItemStack(Items.NETHER_STAR)), meta, false);
 * }</pre>
 *
 */
public class Recipes {
	/**
	 * Convenience factory for various recipe input variants.
	 */
	public static IRecipeInputFactory inputFactory;

	/**
	 * Recipe manager for furnace recipes.
	 *
	 * <p>No recipe meta.
	 *
	 * @deprecated currently unused/not implemented, uses the vanilla furnace recipe list
	 */
	@Deprecated
	public static IMachineRecipeManager furnace;

	/**
	 * Recipe manager for macerator recipes.
	 *
	 * <p>No recipe meta.
	 */
	public static IMachineRecipeManager macerator;

	/**
	 * Recipe manager for extractor recipes.
	 *
	 * <p>No recipe meta.
	 */
	public static IMachineRecipeManager extractor;

	/**
	 * Recipe manager for compressor recipes.
	 *
	 * <p>No recipe meta.
	 */
	public static IMachineRecipeManager compressor;

	/**
	 * Recipe manager for thermal centrifuge recipes.
	 *
	 * <p>Recipe meta:
	 * <ul>
	 * <li>int minHeat: minimum heat level required
	 * </ul>
	 */
	public static IMachineRecipeManager centrifuge;

	/**
	 * Recipe manager for blast cutter recipes.
	 *
	 * <p>Recipe meta:
	 * <ul>
	 * <li>int hardness: minimum blade hardness (3=iron, 6=steel, 9=diamond)
	 * </ul>
	 */
	public static IMachineRecipeManager blockcutter;

	/**
	 * Recipe manager for blast furnace recipes.
	 *
	 * <p>Recipe meta:
	 * <ul>
	 * <li>int fluid: input fluid amount per cycle (in mB)
	 * <li>int duration: process duration (in ticks)
	 * </ul>
	 */
	public static IMachineRecipeManager blastfurnace;

	/**
	 * Recipe manager for recycler recipes.
	 *
	 * <p>No recipe meta.
	 *
	 * @note the implementation is currently immutable, any non-blacklisted item will be accepted,
	 * chance checking is up to the caller.
	 */
	public static IMachineRecipeManager recycler;

	/**
	 * Recipe manager for metal former extrusion mode recipes.
	 *
	 * <p>No recipe meta.
	 */
	public static IMachineRecipeManager metalformerExtruding;

	/**
	 * Recipe manager for metal former cutting mode recipes.
	 *
	 * <p>No recipe meta.
	 */
	public static IMachineRecipeManager metalformerCutting;

	/**
	 * Recipe manager for metal former rolling mode recipes.
	 *
	 * <p>No recipe meta.
	 */
	public static IMachineRecipeManager metalformerRolling;

	/**
	 * Recipe manager for ore washing plant recipes.
	 *
	 * <p>Recipe meta:
	 * <ul>
	 * <li>int amount: input fluid amount per cycle (in mB)
	 * </ul>
	 */
	public static IMachineRecipeManager oreWashing;
	public static ICannerBottleRecipeManager cannerBottle;
	public static ICannerEnrichRecipeManager cannerEnrich;
	public static IElectrolyzerRecipeManager electrolyzer;
	public static IFermenterRecipeManager fermenter;

	/**
	 * Recipe manager for uu mass fabricator (matter gen) recipes.
	 *
	 * <p>Recipe meta:
	 * <ul>
	 * <li>int amplification: amount of eu to be amplified per item (fixed +5x)
	 * </ul>
	 *
	 * <p>Reference values:
	 * <ul>
	 * <li>5000: Scrap
	 * <li>45000: Scrapbox
	 * </ul>
	 */
	public static IMachineRecipeManager matterAmplifier;
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
	public static IScrapboxManager scrapboxDrops;
	public static IListRecipeManager recyclerBlacklist;
	/**
	 * Do not add anything to this Whitelist. This is for Configuration only.
	 * You may need this if you have an own Recycler in your Mod, just to check if something can be recycled. but don't add anything to this List
	 */
	public static IListRecipeManager recyclerWhitelist;
	public static ICraftingRecipeManager advRecipes;

	public static ISemiFluidFuelManager semiFluidGenerator;
	public static IFluidHeatManager fluidHeatGenerator;
	/**
	 * Used by the Liquid Heat Exchanger to cool down liquids and determine the amount of hu generated for every mb.
	 */
	public static ILiquidHeatExchangerManager liquidCooldownManager;
	/**
	 * Opposite of {@link #liquidCooldownManager}. This is for Liquids that can be heated up again.
	 */
	public static ILiquidHeatExchangerManager liquidHeatupManager;
}
