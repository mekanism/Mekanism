package mekanism.common.recipe.lookup.cache;

import java.util.EnumMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Predicate;
import javax.annotation.Nullable;
import mekanism.api.chemical.Chemical;
import mekanism.api.chemical.ChemicalStack;
import mekanism.api.chemical.ChemicalType;
import mekanism.api.chemical.merged.BoxedChemicalStack;
import mekanism.api.recipes.ChemicalCrystallizerRecipe;
import mekanism.api.recipes.inputs.chemical.IChemicalStackIngredient;
import mekanism.common.recipe.MekanismRecipeType;
import mekanism.common.recipe.lookup.cache.type.ChemicalInputCache;
import mekanism.common.util.EnumUtils;
import net.minecraft.world.World;

/**
 * Similar in concept to {@link SingleInputRecipeCache} except specialized to handle Chemical Crystallizer recipes for the purposes of being able to better handle the
 * boxed chemical stack inputs.
 */
public class ChemicalCrystallizerInputRecipeCache extends AbstractInputRecipeCache<ChemicalCrystallizerRecipe> {

    private final Map<ChemicalType, ChemicalInputCache<?, ?, ChemicalCrystallizerRecipe>> typeBasedCache = new EnumMap<>(ChemicalType.class);
    private final Map<ChemicalType, Set<ChemicalCrystallizerRecipe>> typeBasedComplexRecipes = new EnumMap<>(ChemicalType.class);

    public ChemicalCrystallizerInputRecipeCache(MekanismRecipeType<ChemicalCrystallizerRecipe, ?> recipeType) {
        super(recipeType);
        for (ChemicalType chemicalType : EnumUtils.CHEMICAL_TYPES) {
            typeBasedCache.put(chemicalType, new ChemicalInputCache<>());
            typeBasedComplexRecipes.put(chemicalType, new HashSet<>());
        }
    }

    @Override
    public void clear() {
        super.clear();
        for (ChemicalType chemicalType : EnumUtils.CHEMICAL_TYPES) {
            typeBasedCache.get(chemicalType).clear();
            typeBasedComplexRecipes.get(chemicalType).clear();
        }
    }

    /**
     * Checks if there is a matching recipe that has the given input.
     *
     * @param world World.
     * @param input Recipe input.
     *
     * @return {@code true} if there is a match, {@code false} if there isn't.
     */
    public boolean containsInput(@Nullable World world, BoxedChemicalStack input) {
        if (input.isEmpty()) {
            //Don't allow empty inputs
            return false;
        }
        initCacheIfNeeded(world);
        ChemicalType type = input.getChemicalType();
        return containsInput(type, input.getChemicalStack()) || typeBasedComplexRecipes.get(type).stream().anyMatch(recipe -> recipe.testType(input));
    }

    /**
     * Checks if there is a matching recipe that has the given input.
     *
     * @param world World.
     * @param input Recipe input.
     *
     * @return {@code true} if there is a match, {@code false} if there isn't.
     */
    public <CHEMICAL extends Chemical<CHEMICAL>, STACK extends ChemicalStack<CHEMICAL>> boolean containsInput(@Nullable World world, CHEMICAL input) {
        if (input.isEmptyType()) {
            //Don't allow empty inputs
            return false;
        }
        initCacheIfNeeded(world);
        ChemicalType type = ChemicalType.getTypeFor(input);
        STACK stack = (STACK) input.getStack(1);
        return containsInput(type, stack) || typeBasedComplexRecipes.get(type).stream().anyMatch(recipe -> recipe.testType(stack));
    }

    private <CHEMICAL extends Chemical<CHEMICAL>, STACK extends ChemicalStack<CHEMICAL>> boolean containsInput(ChemicalType type, STACK stack) {
        return ((ChemicalInputCache<CHEMICAL, STACK, ChemicalCrystallizerRecipe>) typeBasedCache.get(type)).contains(stack);
    }

    /**
     * Finds the first recipe that matches the given input.
     *
     * @param world World.
     * @param input Recipe input.
     *
     * @return Recipe matching the given input, or {@code null} if no recipe matches.
     */
    @Nullable
    public ChemicalCrystallizerRecipe findFirstRecipe(@Nullable World world, BoxedChemicalStack input) {
        if (input.isEmpty()) {
            //Don't allow empty inputs
            return null;
        }
        initCacheIfNeeded(world);
        return findFirstRecipe(input.getChemicalType(), input.getChemicalStack());
    }

    @Nullable
    private <CHEMICAL extends Chemical<CHEMICAL>, STACK extends ChemicalStack<CHEMICAL>> ChemicalCrystallizerRecipe findFirstRecipe(ChemicalType type, STACK stack) {
        Predicate<ChemicalCrystallizerRecipe> matchPredicate = recipe -> ((IChemicalStackIngredient<CHEMICAL, STACK>) recipe.getInput()).test(stack);
        ChemicalInputCache<CHEMICAL, STACK, ChemicalCrystallizerRecipe> cache = (ChemicalInputCache<CHEMICAL, STACK, ChemicalCrystallizerRecipe>) typeBasedCache.get(type);
        ChemicalCrystallizerRecipe recipe = cache.findFirstRecipe(stack, matchPredicate);
        return recipe == null ? findFirstRecipe(typeBasedComplexRecipes.get(type), matchPredicate) : recipe;
    }

    @Override
    protected void initCache(List<ChemicalCrystallizerRecipe> recipes) {
        for (ChemicalCrystallizerRecipe recipe : recipes) {
            IChemicalStackIngredient<?, ?> ingredient = recipe.getInput();
            ChemicalType type = ChemicalType.getTypeFor(ingredient);
            if (mapInputs(recipe, type, ingredient)) {
                typeBasedComplexRecipes.get(type).add(recipe);
            }
        }
    }

    private <CHEMICAL extends Chemical<CHEMICAL>, STACK extends ChemicalStack<CHEMICAL>, INGREDIENT extends IChemicalStackIngredient<CHEMICAL, STACK>> boolean mapInputs(
          ChemicalCrystallizerRecipe recipe, ChemicalType type, INGREDIENT ingredient) {
        return ((ChemicalInputCache<CHEMICAL, STACK, ChemicalCrystallizerRecipe>) typeBasedCache.get(type)).mapInputs(recipe, ingredient);
    }
}