package mekanism.common.integration.crafttweaker.recipe.handler;

import com.blamejared.crafttweaker.api.recipe.component.IDecomposedRecipe;
import com.blamejared.crafttweaker.api.recipe.handler.IRecipeHandler;
import com.blamejared.crafttweaker.api.recipe.manager.base.IRecipeManager;
import java.util.Optional;
import mekanism.api.chemical.Chemical;
import mekanism.api.chemical.ChemicalStack;
import mekanism.api.chemical.gas.Gas;
import mekanism.api.chemical.gas.GasStack;
import mekanism.api.chemical.pigment.Pigment;
import mekanism.api.chemical.pigment.PigmentStack;
import mekanism.api.recipes.ChemicalInfuserRecipe;
import mekanism.api.recipes.PigmentMixingRecipe;
import mekanism.api.recipes.chemical.ChemicalChemicalToChemicalRecipe;
import mekanism.api.recipes.ingredients.ChemicalStackIngredient;
import mekanism.api.recipes.ingredients.ChemicalStackIngredient.GasStackIngredient;
import mekanism.api.recipes.ingredients.ChemicalStackIngredient.PigmentStackIngredient;
import mekanism.common.integration.crafttweaker.CrTRecipeComponents;
import mekanism.common.integration.crafttweaker.CrTRecipeComponents.ChemicalRecipeComponent;
import mekanism.common.integration.crafttweaker.CrTUtils;
import mekanism.common.integration.crafttweaker.CrTUtils.UnaryTypePair;
import mekanism.common.integration.crafttweaker.chemical.ICrTChemicalStack;
import mekanism.common.integration.crafttweaker.chemical.ICrTChemicalStack.ICrTGasStack;
import mekanism.common.integration.crafttweaker.chemical.ICrTChemicalStack.ICrTPigmentStack;
import mekanism.common.integration.crafttweaker.recipe.manager.ChemicalChemicalToChemicalRecipeManager;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.Recipe;

public abstract class ChemicalChemicalToChemicalRecipeHandler<CHEMICAL extends Chemical<CHEMICAL>, STACK extends ChemicalStack<CHEMICAL>,
      INGREDIENT extends ChemicalStackIngredient<CHEMICAL, STACK>, CRT_STACK extends ICrTChemicalStack<CHEMICAL, STACK, CRT_STACK>,
      RECIPE extends ChemicalChemicalToChemicalRecipe<CHEMICAL, STACK, INGREDIENT>> extends MekanismRecipeHandler<RECIPE> {

    @Override
    public String dumpToCommandString(IRecipeManager<? super RECIPE> manager, RECIPE recipe) {
        return buildCommandString(manager, recipe, recipe.getLeftInput(), recipe.getRightInput(), recipe.getOutputDefinition());
    }

    @Override
    public <U extends Recipe<?>> boolean doesConflict(IRecipeManager<? super RECIPE> manager, RECIPE recipe, U other) {
        //Only support if the other is a chemical chemical to chemical recipe and don't bother checking the reverse as the recipe type's generics
        // ensures that it is of the same type
        if (recipeIsInstance(other)) {
            ChemicalChemicalToChemicalRecipe<?, ?, ?> otherRecipe = (ChemicalChemicalToChemicalRecipe<?, ?, ?>) other;
            return (chemicalIngredientConflicts(recipe.getLeftInput(), otherRecipe.getLeftInput()) &&
                    chemicalIngredientConflicts(recipe.getRightInput(), otherRecipe.getRightInput())) ||
                   (chemicalIngredientConflicts(recipe.getLeftInput(), otherRecipe.getRightInput()) &&
                    chemicalIngredientConflicts(recipe.getRightInput(), otherRecipe.getLeftInput()));
        }
        return false;
    }

    @Override
    public Optional<IDecomposedRecipe> decompose(IRecipeManager<? super RECIPE> manager, RECIPE recipe) {
        return decompose(recipe.getLeftInput(), recipe.getRightInput(), recipe.getOutputDefinition());
    }

    @Override
    public Optional<RECIPE> recompose(IRecipeManager<? super RECIPE> m, ResourceLocation name, IDecomposedRecipe recipe) {
        if (m instanceof ChemicalChemicalToChemicalRecipeManager) {
            ChemicalChemicalToChemicalRecipeManager<CHEMICAL, STACK, INGREDIENT, CRT_STACK, RECIPE> manager =
                  (ChemicalChemicalToChemicalRecipeManager<CHEMICAL, STACK, INGREDIENT, CRT_STACK, RECIPE>) m;
            UnaryTypePair<INGREDIENT> inputs = CrTUtils.getPair(recipe, getChemicalComponent().input());
            return Optional.of(manager.makeRecipe(name,
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
    protected abstract ChemicalRecipeComponent<CHEMICAL, STACK, INGREDIENT, CRT_STACK> getChemicalComponent();

    /**
     * @return if the other recipe the correct class type.
     */
    protected abstract boolean recipeIsInstance(Recipe<?> other);

    @IRecipeHandler.For(ChemicalInfuserRecipe.class)
    public static class ChemicalInfuserRecipeHandler extends ChemicalChemicalToChemicalRecipeHandler<Gas, GasStack, GasStackIngredient, ICrTGasStack, ChemicalInfuserRecipe> {

        @Override
        protected ChemicalRecipeComponent<Gas, GasStack, GasStackIngredient, ICrTGasStack> getChemicalComponent() {
            return CrTRecipeComponents.GAS;
        }

        @Override
        protected boolean recipeIsInstance(Recipe<?> other) {
            return other instanceof ChemicalInfuserRecipe;
        }
    }

    @IRecipeHandler.For(PigmentMixingRecipe.class)
    public static class PigmentMixingRecipeHandler extends ChemicalChemicalToChemicalRecipeHandler<Pigment, PigmentStack, PigmentStackIngredient, ICrTPigmentStack,
          PigmentMixingRecipe> {

        @Override
        protected ChemicalRecipeComponent<Pigment, PigmentStack, PigmentStackIngredient, ICrTPigmentStack> getChemicalComponent() {
            return CrTRecipeComponents.PIGMENT;
        }

        @Override
        protected boolean recipeIsInstance(Recipe<?> other) {
            return other instanceof PigmentMixingRecipe;
        }
    }
}