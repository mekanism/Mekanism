package mekanism.common.recipe.lookup.cache;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Predicate;
import javax.annotation.Nullable;
import mekanism.api.chemical.gas.Gas;
import mekanism.api.chemical.gas.GasStack;
import mekanism.api.recipes.RotaryRecipe;
import mekanism.common.recipe.MekanismRecipeType;
import mekanism.common.recipe.lookup.cache.type.ChemicalInputCache;
import mekanism.common.recipe.lookup.cache.type.FluidInputCache;
import net.minecraft.world.level.Level;
import net.minecraftforge.fluids.FluidStack;

/**
 * Similar in concept to {@link SingleInputRecipeCache} except specialized to handle Rotary Condensentrator recipes for the purposes of being able to handle both the gas
 * to fluid and fluid to gas directions.
 */
public class RotaryInputRecipeCache extends AbstractInputRecipeCache<RotaryRecipe> {

    private final ChemicalInputCache<Gas, GasStack, RotaryRecipe> gasInputCache = new ChemicalInputCache<>();
    private final FluidInputCache<RotaryRecipe> fluidInputCache = new FluidInputCache<>();
    private final Set<RotaryRecipe> complexGasInputRecipes = new HashSet<>();
    private final Set<RotaryRecipe> complexFluidInputRecipes = new HashSet<>();

    public RotaryInputRecipeCache(MekanismRecipeType<RotaryRecipe, ?> recipeType) {
        super(recipeType);
    }

    @Override
    public void clear() {
        super.clear();
        gasInputCache.clear();
        fluidInputCache.clear();
        complexGasInputRecipes.clear();
        complexFluidInputRecipes.clear();
    }

    /**
     * Checks if there is a matching recipe that has the given fluid input.
     *
     * @param world World.
     * @param input Recipe input.
     *
     * @return {@code true} if there is a match, {@code false} if there isn't.
     */
    public boolean containsInput(@Nullable Level world, FluidStack input) {
        return containsInput(world, input, RotaryRecipe::getFluidInput, fluidInputCache, complexFluidInputRecipes);
    }

    /**
     * Checks if there is a matching recipe that has the given gas input.
     *
     * @param world World.
     * @param input Recipe input.
     *
     * @return {@code true} if there is a match, {@code false} if there isn't.
     */
    public boolean containsInput(@Nullable Level world, GasStack input) {
        return containsInput(world, input, RotaryRecipe::getGasInput, gasInputCache, complexGasInputRecipes);
    }

    /**
     * Finds the first recipe that matches the given fluid input.
     *
     * @param world World.
     * @param input Recipe input.
     *
     * @return Recipe matching the given fluid input, or {@code null} if no recipe matches.
     */
    @Nullable
    public RotaryRecipe findFirstRecipe(@Nullable Level world, FluidStack input) {
        if (fluidInputCache.isEmpty(input)) {
            //Don't allow empty inputs
            return null;
        }
        initCacheIfNeeded(world);
        Predicate<RotaryRecipe> matchPredicate = recipe -> recipe.test(input);
        RotaryRecipe recipe = fluidInputCache.findFirstRecipe(input, matchPredicate);
        return recipe == null ? findFirstRecipe(complexFluidInputRecipes, matchPredicate) : recipe;
    }

    /**
     * Finds the first recipe that matches the given gas input.
     *
     * @param world World.
     * @param input Recipe input.
     *
     * @return Recipe matching the given gas input, or {@code null} if no recipe matches.
     */
    @Nullable
    public RotaryRecipe findFirstRecipe(@Nullable Level world, GasStack input) {
        if (gasInputCache.isEmpty(input)) {
            //Don't allow empty inputs
            return null;
        }
        initCacheIfNeeded(world);
        Predicate<RotaryRecipe> matchPredicate = recipe -> recipe.test(input);
        RotaryRecipe recipe = gasInputCache.findFirstRecipe(input, matchPredicate);
        return recipe == null ? findFirstRecipe(complexGasInputRecipes, matchPredicate) : recipe;
    }

    @Override
    protected void initCache(List<RotaryRecipe> recipes) {
        for (RotaryRecipe recipe : recipes) {
            if (recipe.hasFluidToGas() && fluidInputCache.mapInputs(recipe, recipe.getFluidInput())) {
                complexFluidInputRecipes.add(recipe);
            }
            if (recipe.hasGasToFluid() && gasInputCache.mapInputs(recipe, recipe.getGasInput())) {
                complexGasInputRecipes.add(recipe);
            }
        }
    }
}