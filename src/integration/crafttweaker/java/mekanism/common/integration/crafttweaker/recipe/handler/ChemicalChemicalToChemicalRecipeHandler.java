package mekanism.common.integration.crafttweaker.recipe.handler;

import com.blamejared.crafttweaker.api.recipe.component.IDecomposedRecipe;
import com.blamejared.crafttweaker.api.recipe.handler.IRecipeHandler;
import com.blamejared.crafttweaker.api.recipe.manager.base.IRecipeManager;
import java.util.Optional;
import mekanism.api.recipes.ChemicalChemicalToChemicalRecipe;
import mekanism.api.recipes.ingredients.ChemicalStackIngredient;
import mekanism.common.integration.crafttweaker.CrTRecipeComponents;
import mekanism.common.integration.crafttweaker.CrTUtils;
import mekanism.common.integration.crafttweaker.CrTUtils.UnaryTypePair;
import mekanism.common.integration.crafttweaker.recipe.manager.ChemicalChemicalToChemicalRecipeManager;
import net.minecraft.core.RegistryAccess;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeHolder;

@IRecipeHandler.For(ChemicalChemicalToChemicalRecipe.class)
public class ChemicalChemicalToChemicalRecipeHandler extends MekanismRecipeHandler<ChemicalChemicalToChemicalRecipe> {

    @Override
    public String dumpToCommandString(IRecipeManager<? super ChemicalChemicalToChemicalRecipe> manager, RegistryAccess registryAccess,
          RecipeHolder<ChemicalChemicalToChemicalRecipe> recipeHolder) {
        ChemicalChemicalToChemicalRecipe recipe = recipeHolder.value();
        return buildCommandString(manager, recipeHolder, recipe.getLeftInput(), recipe.getRightInput(), recipe.getOutputDefinition());
    }

    @Override
    public <U extends Recipe<?>> boolean doesConflict(IRecipeManager<? super ChemicalChemicalToChemicalRecipe> manager,
          ChemicalChemicalToChemicalRecipe recipe, U o) {
        if (o instanceof ChemicalChemicalToChemicalRecipe other) {
            return (chemicalIngredientConflicts(recipe.getLeftInput(), other.getLeftInput()) &&
                    chemicalIngredientConflicts(recipe.getRightInput(), other.getRightInput())) ||
                   (chemicalIngredientConflicts(recipe.getLeftInput(), other.getRightInput()) &&
                    chemicalIngredientConflicts(recipe.getRightInput(), other.getLeftInput()));
        }
        return false;
    }

    @Override
    public Optional<IDecomposedRecipe> decompose(IRecipeManager<? super ChemicalChemicalToChemicalRecipe> manager, RegistryAccess registryAccess,
          ChemicalChemicalToChemicalRecipe recipe) {
        return decompose(recipe.getLeftInput(), recipe.getRightInput(), recipe.getOutputDefinition());
    }

    @Override
    public Optional<ChemicalChemicalToChemicalRecipe> recompose(IRecipeManager<? super ChemicalChemicalToChemicalRecipe> m, RegistryAccess registryAccess, IDecomposedRecipe recipe) {
        if (m instanceof ChemicalChemicalToChemicalRecipeManager manager) {
            UnaryTypePair<ChemicalStackIngredient> inputs = CrTUtils.getPair(recipe, CrTRecipeComponents.CHEMICAL.input());
            return Optional.of(manager.makeRecipe(
                  inputs.a(),
                  inputs.b(),
                  recipe.getOrThrowSingle(CrTRecipeComponents.CHEMICAL.output())
            ));
        }
        return Optional.empty();
    }
}