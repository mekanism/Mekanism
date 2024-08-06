package mekanism.common.integration.crafttweaker.recipe.handler;

import com.blamejared.crafttweaker.api.recipe.component.IDecomposedRecipe;
import com.blamejared.crafttweaker.api.recipe.handler.IRecipeHandler;
import com.blamejared.crafttweaker.api.recipe.manager.base.IRecipeManager;
import java.util.Optional;
import mekanism.api.recipes.ChemicalInfuserRecipe;
import mekanism.api.recipes.PigmentMixingRecipe;
import mekanism.api.recipes.chemical.ChemicalChemicalToChemicalRecipe;
import mekanism.api.recipes.ingredients.ChemicalStackIngredient;
import mekanism.common.integration.crafttweaker.CrTRecipeComponents;
import mekanism.common.integration.crafttweaker.CrTRecipeComponents.ChemicalRecipeComponent;
import mekanism.common.integration.crafttweaker.CrTUtils;
import mekanism.common.integration.crafttweaker.CrTUtils.UnaryTypePair;
import mekanism.common.integration.crafttweaker.recipe.manager.ChemicalChemicalToChemicalRecipeManager;
import net.minecraft.core.RegistryAccess;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeHolder;

public abstract class ChemicalChemicalToChemicalRecipeHandler<
      RECIPE extends ChemicalChemicalToChemicalRecipe> extends MekanismRecipeHandler<RECIPE> {

    @Override
    public String dumpToCommandString(IRecipeManager<? super RECIPE> manager, RegistryAccess registryAccess, RecipeHolder<RECIPE> recipeHolder) {
        RECIPE recipe = recipeHolder.value();
        return buildCommandString(manager, recipeHolder, recipe.getLeftInput(), recipe.getRightInput(), recipe.getOutputDefinition());
    }

    @Override
    public <U extends Recipe<?>> boolean doesConflict(IRecipeManager<? super RECIPE> manager, RECIPE recipe, U other) {
        //Only support if the other is a chemical chemical to chemical recipe and don't bother checking the reverse as the recipe type's generics
        // ensures that it is of the same type
        if (recipeIsInstance(other)) {
            ChemicalChemicalToChemicalRecipe otherRecipe = (ChemicalChemicalToChemicalRecipe) other;
            return (chemicalIngredientConflicts(recipe.getLeftInput(), otherRecipe.getLeftInput()) &&
                    chemicalIngredientConflicts(recipe.getRightInput(), otherRecipe.getRightInput())) ||
                   (chemicalIngredientConflicts(recipe.getLeftInput(), otherRecipe.getRightInput()) &&
                    chemicalIngredientConflicts(recipe.getRightInput(), otherRecipe.getLeftInput()));
        }
        return false;
    }

    @Override
    public Optional<IDecomposedRecipe> decompose(IRecipeManager<? super RECIPE> manager, RegistryAccess registryAccess, RECIPE recipe) {
        return decompose(recipe.getLeftInput(), recipe.getRightInput(), recipe.getOutputDefinition());
    }

    @Override
    public Optional<RECIPE> recompose(IRecipeManager<? super RECIPE> m, RegistryAccess registryAccess, IDecomposedRecipe recipe) {
        if (m instanceof ChemicalChemicalToChemicalRecipeManager) {
            ChemicalChemicalToChemicalRecipeManager<RECIPE> manager =
                  (ChemicalChemicalToChemicalRecipeManager<RECIPE>) m;
            UnaryTypePair<ChemicalStackIngredient> inputs = CrTUtils.getPair(recipe, getChemicalComponent().input());
            return Optional.of(manager.makeRecipe(
                  inputs.a(),
                  inputs.b(),
                  recipe.getOrThrowSingle(getChemicalComponent().output())
            ));
        }
        return Optional.empty();
    }

    /**
     * @return Chemical component for recomposing recipes.
     */
    protected abstract ChemicalRecipeComponent getChemicalComponent();

    /**
     * @return if the other recipe the correct class type.
     */
    protected abstract boolean recipeIsInstance(Recipe<?> other);

    @IRecipeHandler.For(ChemicalInfuserRecipe.class)
    public static class ChemicalInfuserRecipeHandler extends ChemicalChemicalToChemicalRecipeHandler<ChemicalInfuserRecipe> {

        @Override
        protected ChemicalRecipeComponent getChemicalComponent() {
            return CrTRecipeComponents.CHEMICAL;
        }

        @Override
        protected boolean recipeIsInstance(Recipe<?> other) {
            return other instanceof ChemicalInfuserRecipe;
        }
    }

    @IRecipeHandler.For(PigmentMixingRecipe.class)
    public static class PigmentMixingRecipeHandler extends ChemicalChemicalToChemicalRecipeHandler<
          PigmentMixingRecipe> {

        @Override
        protected ChemicalRecipeComponent getChemicalComponent() {
            return CrTRecipeComponents.CHEMICAL;
        }

        @Override
        protected boolean recipeIsInstance(Recipe<?> other) {
            return other instanceof PigmentMixingRecipe;
        }
    }
}