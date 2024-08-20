package mekanism.common.integration.crafttweaker.recipe.handler;

import com.blamejared.crafttweaker.api.recipe.component.IDecomposedRecipe;
import com.blamejared.crafttweaker.api.recipe.handler.IRecipeHandler;
import com.blamejared.crafttweaker.api.recipe.manager.base.IRecipeManager;
import java.util.Optional;
import mekanism.api.recipes.ChemicalCrystallizerRecipe;
import mekanism.api.recipes.ingredients.ChemicalStackIngredient;
import mekanism.common.integration.crafttweaker.CrTRecipeComponents;
import mekanism.common.integration.crafttweaker.CrTUtils;
import mekanism.common.integration.crafttweaker.recipe.manager.ChemicalCrystallizerRecipeManager;
import net.minecraft.core.RegistryAccess;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeHolder;

@IRecipeHandler.For(ChemicalCrystallizerRecipe.class)
public class ChemicalCrystallizerRecipeHandler extends MekanismRecipeHandler<ChemicalCrystallizerRecipe> {

    @Override
    public String dumpToCommandString(IRecipeManager<? super ChemicalCrystallizerRecipe> manager, RegistryAccess registryAccess,
          RecipeHolder<ChemicalCrystallizerRecipe> recipeHolder) {
        ChemicalCrystallizerRecipe recipe = recipeHolder.value();
        return buildCommandString(manager, recipeHolder, recipe.getInput(), recipe.getOutputDefinition());
    }

    @Override
    public <U extends Recipe<?>> boolean doesConflict(IRecipeManager<? super ChemicalCrystallizerRecipe> manager, ChemicalCrystallizerRecipe recipe, U o) {
        //Only support if the other is a chemical crystallizer recipe and don't bother checking the reverse as the recipe type's generics
        // ensures that it is of the same type
        return o instanceof ChemicalCrystallizerRecipe other && chemicalIngredientConflicts(recipe.getInput(), other.getInput());
    }

    @Override
    public Optional<IDecomposedRecipe> decompose(IRecipeManager<? super ChemicalCrystallizerRecipe> manager, RegistryAccess registryAccess, ChemicalCrystallizerRecipe recipe) {
        return decompose(recipe.getInput(), recipe.getOutputDefinition());
    }

    @Override
    public Optional<ChemicalCrystallizerRecipe> recompose(IRecipeManager<? super ChemicalCrystallizerRecipe> m, RegistryAccess registryAccess, IDecomposedRecipe recipe) {
        if (m instanceof ChemicalCrystallizerRecipeManager manager) {
            Optional<? extends ChemicalStackIngredient> found = CrTUtils.getSingleIfPresent(recipe, CrTRecipeComponents.CHEMICAL.input());
            ChemicalStackIngredient input = found.orElseThrow(() -> new IllegalArgumentException("No chemical input ingredient provided."));
            return Optional.of(manager.makeRecipe(input, recipe.getOrThrowSingle(CrTRecipeComponents.ITEM.output())));
        }
        return Optional.empty();
    }
}