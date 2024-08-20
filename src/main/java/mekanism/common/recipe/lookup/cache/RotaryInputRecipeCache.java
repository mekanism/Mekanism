package mekanism.common.recipe.lookup.cache;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import mekanism.api.chemical.ChemicalStack;
import mekanism.api.recipes.RotaryRecipe;
import mekanism.common.recipe.MekanismRecipeType;
import mekanism.common.recipe.lookup.cache.type.ChemicalInputCache;
import mekanism.common.recipe.lookup.cache.type.FluidInputCache;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.fluids.FluidStack;
import org.jetbrains.annotations.Nullable;

/**
 * Similar in concept to {@link SingleInputRecipeCache} except specialized to handle Rotary Condensentrator recipes for the purposes of being able to handle both the gas
 * to fluid and fluid to chemical directions.
 */
public class RotaryInputRecipeCache extends AbstractInputRecipeCache<RotaryRecipe> {

    private final ChemicalInputCache<RotaryRecipe> chemicalInputCache = new ChemicalInputCache<>();
    private final FluidInputCache<RotaryRecipe> fluidInputCache = new FluidInputCache<>();
    private final Set<RotaryRecipe> complexChemicalInputRecipes = new HashSet<>();
    private final Set<RotaryRecipe> complexFluidInputRecipes = new HashSet<>();

    public RotaryInputRecipeCache(MekanismRecipeType<?, RotaryRecipe, ?> recipeType) {
        super(recipeType);
    }

    @Override
    public void clear() {
        super.clear();
        chemicalInputCache.clear();
        fluidInputCache.clear();
        complexChemicalInputRecipes.clear();
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
     * Checks if there is a matching recipe that has the given chemical input.
     *
     * @param world World.
     * @param input Recipe input.
     *
     * @return {@code true} if there is a match, {@code false} if there isn't.
     */
    public boolean containsInput(@Nullable Level world, ChemicalStack input) {
        return containsInput(world, input, RotaryRecipe::getChemicalInput, chemicalInputCache, complexChemicalInputRecipes);
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
        RotaryRecipe recipe = findFirstRecipe(input, fluidInputCache.getRecipes(input));
        return recipe == null ? findFirstRecipe(input, complexFluidInputRecipes) : recipe;
    }

    @Nullable
    private RotaryRecipe findFirstRecipe(FluidStack input, Iterable<RotaryRecipe> recipes) {
        for (RotaryRecipe recipe : recipes) {
            if (recipe.test(input)) {
                return recipe;
            }
        }
        return null;
    }

    /**
     * Finds the first recipe that matches the given chemical input.
     *
     * @param world World.
     * @param input Recipe input.
     *
     * @return Recipe matching the given chemical input, or {@code null} if no recipe matches.
     */
    @Nullable
    public RotaryRecipe findFirstRecipe(@Nullable Level world, ChemicalStack input) {
        if (chemicalInputCache.isEmpty(input)) {
            //Don't allow empty inputs
            return null;
        }
        initCacheIfNeeded(world);
        RotaryRecipe recipe = findFirstRecipe(input, chemicalInputCache.getRecipes(input));
        return recipe == null ? findFirstRecipe(input, complexChemicalInputRecipes) : recipe;
    }

    @Nullable
    private RotaryRecipe findFirstRecipe(ChemicalStack input, Iterable<RotaryRecipe> recipes) {
        for (RotaryRecipe recipe : recipes) {
            if (recipe.test(input)) {
                return recipe;
            }
        }
        return null;
    }

    @Override
    protected void initCache(List<RecipeHolder<RotaryRecipe>> recipes) {
        for (RecipeHolder<RotaryRecipe> recipeHolder : recipes) {
            RotaryRecipe recipe = recipeHolder.value();
            if (recipe.hasFluidToChemical() && fluidInputCache.mapInputs(recipe, recipe.getFluidInput())) {
                complexFluidInputRecipes.add(recipe);
            }
            if (recipe.hasChemicalToFluid() && chemicalInputCache.mapInputs(recipe, recipe.getChemicalInput())) {
                complexChemicalInputRecipes.add(recipe);
            }
        }
    }
}