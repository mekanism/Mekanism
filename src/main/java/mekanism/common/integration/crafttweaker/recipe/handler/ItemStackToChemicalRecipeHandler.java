package mekanism.common.integration.crafttweaker.recipe.handler;

import com.blamejared.crafttweaker.api.recipe.component.IDecomposedRecipe;
import com.blamejared.crafttweaker.api.recipe.handler.IRecipeHandler;
import com.blamejared.crafttweaker.api.recipe.manager.base.IRecipeManager;
import java.util.Optional;
import mekanism.api.chemical.Chemical;
import mekanism.api.chemical.ChemicalStack;
import mekanism.api.chemical.gas.Gas;
import mekanism.api.chemical.gas.GasStack;
import mekanism.api.chemical.infuse.InfuseType;
import mekanism.api.chemical.infuse.InfusionStack;
import mekanism.api.chemical.pigment.Pigment;
import mekanism.api.chemical.pigment.PigmentStack;
import mekanism.api.recipes.ItemStackToGasRecipe;
import mekanism.api.recipes.ItemStackToInfuseTypeRecipe;
import mekanism.api.recipes.ItemStackToPigmentRecipe;
import mekanism.api.recipes.chemical.ItemStackToChemicalRecipe;
import mekanism.common.integration.crafttweaker.CrTRecipeComponents;
import mekanism.common.integration.crafttweaker.CrTRecipeComponents.ChemicalRecipeComponent;
import mekanism.common.integration.crafttweaker.chemical.ICrTChemicalStack;
import mekanism.common.integration.crafttweaker.chemical.ICrTChemicalStack.ICrTGasStack;
import mekanism.common.integration.crafttweaker.chemical.ICrTChemicalStack.ICrTInfusionStack;
import mekanism.common.integration.crafttweaker.chemical.ICrTChemicalStack.ICrTPigmentStack;
import mekanism.common.integration.crafttweaker.recipe.manager.ItemStackToChemicalRecipeManager;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.Recipe;

public abstract class ItemStackToChemicalRecipeHandler<CHEMICAL extends Chemical<CHEMICAL>, STACK extends ChemicalStack<CHEMICAL>,
      CRT_STACK extends ICrTChemicalStack<CHEMICAL, STACK, CRT_STACK>, RECIPE extends ItemStackToChemicalRecipe<CHEMICAL, STACK>> extends MekanismRecipeHandler<RECIPE> {

    @Override
    public String dumpToCommandString(IRecipeManager<? super RECIPE> manager, RECIPE recipe) {
        return buildCommandString(manager, recipe, recipe.getInput(), recipe.getOutputDefinition());
    }

    @Override
    public <U extends Recipe<?>> boolean doesConflict(IRecipeManager<? super RECIPE> manager, RECIPE recipe, U other) {
        //Only support if the other is an itemstack to chemical recipe and don't bother checking the reverse as the recipe type's generics
        // ensures that it is of the same type
        return recipeIsInstance(other) && ingredientConflicts(recipe.getInput(), ((ItemStackToChemicalRecipe<?, ?>) other).getInput());
    }

    @Override
    public Optional<IDecomposedRecipe> decompose(IRecipeManager<? super RECIPE> manager, RECIPE recipe) {
        return decompose(recipe.getInput(), recipe.getOutputDefinition());
    }

    @Override
    public Optional<RECIPE> recompose(IRecipeManager<? super RECIPE> m, ResourceLocation name, IDecomposedRecipe recipe) {
        if (m instanceof ItemStackToChemicalRecipeManager) {
            ItemStackToChemicalRecipeManager<CHEMICAL, STACK, CRT_STACK, RECIPE> manager = (ItemStackToChemicalRecipeManager<CHEMICAL, STACK, CRT_STACK, RECIPE>) m;
            return Optional.of(manager.makeRecipe(name,
                  recipe.getOrThrowSingle(CrTRecipeComponents.ITEM.input()),
                  recipe.getOrThrowSingle(getChemicalComponent().output())
            ));
        }
        return Optional.empty();
    }

    /**
     * @return Chemical component for recomposing recipes.
     */
    protected abstract ChemicalRecipeComponent<CHEMICAL, STACK, ?, CRT_STACK> getChemicalComponent();

    /**
     * @return if the other recipe the correct class type.
     */
    protected abstract boolean recipeIsInstance(Recipe<?> other);

    @IRecipeHandler.For(ItemStackToGasRecipe.class)
    public static class ItemStackToGasRecipeHandler extends ItemStackToChemicalRecipeHandler<Gas, GasStack, ICrTGasStack, ItemStackToGasRecipe> {

        @Override
        protected ChemicalRecipeComponent<Gas, GasStack, ?, ICrTGasStack> getChemicalComponent() {
            return CrTRecipeComponents.GAS;
        }

        @Override
        protected boolean recipeIsInstance(Recipe<?> other) {
            return other instanceof ItemStackToGasRecipe;
        }
    }

    @IRecipeHandler.For(ItemStackToInfuseTypeRecipe.class)
    public static class ItemStackToInfuseTypeRecipeHandler extends ItemStackToChemicalRecipeHandler<InfuseType, InfusionStack, ICrTInfusionStack, ItemStackToInfuseTypeRecipe> {

        @Override
        protected ChemicalRecipeComponent<InfuseType, InfusionStack, ?, ICrTInfusionStack> getChemicalComponent() {
            return CrTRecipeComponents.INFUSION;
        }

        @Override
        protected boolean recipeIsInstance(Recipe<?> other) {
            return other instanceof ItemStackToInfuseTypeRecipe;
        }
    }

    @IRecipeHandler.For(ItemStackToPigmentRecipe.class)
    public static class ItemStackToPigmentRecipeHandler extends ItemStackToChemicalRecipeHandler<Pigment, PigmentStack, ICrTPigmentStack, ItemStackToPigmentRecipe> {

        @Override
        protected ChemicalRecipeComponent<Pigment, PigmentStack, ?, ICrTPigmentStack> getChemicalComponent() {
            return CrTRecipeComponents.PIGMENT;
        }

        @Override
        protected boolean recipeIsInstance(Recipe<?> other) {
            return other instanceof ItemStackToPigmentRecipe;
        }
    }
}