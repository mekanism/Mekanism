package mekanism.common.recipe.lookup.cache;

import java.util.EnumMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import mekanism.api.chemical.Chemical;
import mekanism.api.chemical.ChemicalStack;
import mekanism.api.chemical.ChemicalType;
import mekanism.api.chemical.merged.BoxedChemicalStack;
import mekanism.api.recipes.ChemicalCrystallizerRecipe;
import mekanism.api.recipes.ingredients.ChemicalStackIngredient;
import mekanism.common.recipe.MekanismRecipeType;
import mekanism.common.recipe.lookup.cache.type.ChemicalInputCache;
import mekanism.common.util.ChemicalUtil;
import mekanism.common.util.EnumUtils;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

/**
 * Similar in concept to {@link SingleInputRecipeCache} except specialized to handle Chemical Crystallizer recipes for the purposes of being able to better handle the
 * boxed chemical stack inputs.
 */
public class ChemicalCrystallizerInputRecipeCache extends AbstractInputRecipeCache<ChemicalCrystallizerRecipe> {

    private final Map<ChemicalType, ChemicalInputCache<?, ?, ChemicalCrystallizerRecipe>> typeBasedCache = new EnumMap<>(ChemicalType.class);
    private final Map<ChemicalType, Set<ChemicalCrystallizerRecipe>> typeBasedComplexRecipes = new EnumMap<>(ChemicalType.class);

    public ChemicalCrystallizerInputRecipeCache(MekanismRecipeType<?, ChemicalCrystallizerRecipe, ?> recipeType) {
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
    public boolean containsInput(@Nullable Level world, BoxedChemicalStack input) {
        if (input.isEmpty()) {
            //Don't allow empty inputs
            return false;
        }
        initCacheIfNeeded(world);
        ChemicalType type = input.getChemicalType();
        if (containsInput(type, input.getChemicalStack())) {
            return true;
        }
        for (ChemicalCrystallizerRecipe recipe : typeBasedComplexRecipes.get(type)) {
            if (recipe.testType(input)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Checks if there is a matching recipe that has the given input.
     *
     * @param world World.
     * @param input Recipe input.
     *
     * @return {@code true} if there is a match, {@code false} if there isn't.
     */
    public <CHEMICAL extends Chemical<CHEMICAL>, STACK extends ChemicalStack<CHEMICAL>> boolean containsInput(@Nullable Level world, CHEMICAL input) {
        if (input.isEmptyType()) {
            //Don't allow empty inputs
            return false;
        }
        initCacheIfNeeded(world);
        ChemicalType type = ChemicalType.getTypeFor(input);
        STACK stack = ChemicalUtil.withAmount(input, 1);
        if (containsInput(type, stack)) {
            return true;
        }
        for (ChemicalCrystallizerRecipe recipe : typeBasedComplexRecipes.get(type)) {
            if (recipe.testType(stack)) {
                return true;
            }
        }
        return false;
    }

    @SuppressWarnings("unchecked")
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
    public ChemicalCrystallizerRecipe findFirstRecipe(@Nullable Level world, BoxedChemicalStack input) {
        if (input.isEmpty()) {
            //Don't allow empty inputs
            return null;
        }
        initCacheIfNeeded(world);
        return findFirstRecipe(input.getChemicalType(), input.getChemicalStack());
    }

    @Nullable
    @SuppressWarnings("unchecked")
    private <CHEMICAL extends Chemical<CHEMICAL>, STACK extends ChemicalStack<CHEMICAL>> ChemicalCrystallizerRecipe findFirstRecipe(ChemicalType type, STACK stack) {
        ChemicalInputCache<CHEMICAL, STACK, ChemicalCrystallizerRecipe> cache = (ChemicalInputCache<CHEMICAL, STACK, ChemicalCrystallizerRecipe>) typeBasedCache.get(type);
        ChemicalCrystallizerRecipe recipe = findFirstRecipe(stack, cache.getRecipes(stack));
        return recipe == null ? findFirstRecipe(stack, typeBasedComplexRecipes.get(type)) : recipe;
    }

    @Nullable
    @SuppressWarnings("unchecked")
    private <CHEMICAL extends Chemical<CHEMICAL>, STACK extends ChemicalStack<CHEMICAL>> ChemicalCrystallizerRecipe findFirstRecipe(STACK input, Iterable<ChemicalCrystallizerRecipe> recipes) {
        for (ChemicalCrystallizerRecipe recipe : recipes) {
            if (((ChemicalStackIngredient<CHEMICAL, STACK, ?>) recipe.getInput()).test(input)) {
                return recipe;
            }
        }
        return null;
    }

    @Override
    protected void initCache(List<RecipeHolder<ChemicalCrystallizerRecipe>> recipes) {
        for (RecipeHolder<ChemicalCrystallizerRecipe> recipeHolder : recipes) {
            ChemicalCrystallizerRecipe recipe = recipeHolder.value();
            ChemicalStackIngredient<?, ?, ?> ingredient = recipe.getInput();
            ChemicalType type = ChemicalType.getTypeFor(ingredient);
            if (mapInputs(recipe, type, ingredient)) {
                typeBasedComplexRecipes.get(type).add(recipe);
            }
        }
    }

    @SuppressWarnings("unchecked")
    private <CHEMICAL extends Chemical<CHEMICAL>, STACK extends ChemicalStack<CHEMICAL>, INGREDIENT extends ChemicalStackIngredient<CHEMICAL, STACK, ?>> boolean mapInputs(
          ChemicalCrystallizerRecipe recipe, ChemicalType type, INGREDIENT ingredient) {
        return ((ChemicalInputCache<CHEMICAL, STACK, ChemicalCrystallizerRecipe>) typeBasedCache.get(type)).mapInputs(recipe, ingredient);
    }
}