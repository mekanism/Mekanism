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
import mekanism.api.recipes.ItemStackGasToItemStackRecipe;
import mekanism.api.recipes.MetallurgicInfuserRecipe;
import mekanism.api.recipes.PaintingRecipe;
import mekanism.api.recipes.chemical.ItemStackChemicalToItemStackRecipe;
import mekanism.api.recipes.ingredients.ChemicalStackIngredient;
import mekanism.api.recipes.ingredients.ChemicalStackIngredient.GasStackIngredient;
import mekanism.api.recipes.ingredients.ChemicalStackIngredient.InfusionStackIngredient;
import mekanism.api.recipes.ingredients.ChemicalStackIngredient.PigmentStackIngredient;
import mekanism.common.integration.crafttweaker.CrTRecipeComponents;
import mekanism.common.integration.crafttweaker.CrTRecipeComponents.ChemicalRecipeComponent;
import mekanism.common.integration.crafttweaker.recipe.manager.ItemStackChemicalToItemStackRecipeManager;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.Recipe;

public abstract class ItemStackChemicalToItemStackRecipeHandler<CHEMICAL extends Chemical<CHEMICAL>, STACK extends ChemicalStack<CHEMICAL>,
      INGREDIENT extends ChemicalStackIngredient<CHEMICAL, STACK>, RECIPE extends ItemStackChemicalToItemStackRecipe<CHEMICAL, STACK, INGREDIENT>>
      extends MekanismRecipeHandler<RECIPE> {

    @Override
    public String dumpToCommandString(IRecipeManager<? super RECIPE> manager, RECIPE recipe) {
        return buildCommandString(manager, recipe, recipe.getItemInput(), recipe.getChemicalInput(), recipe.getOutputDefinition());
    }

    @Override
    public <U extends Recipe<?>> boolean doesConflict(IRecipeManager<? super RECIPE> manager, RECIPE recipe, U o) {
        //Only support if the other is an itemstack chemical to itemstack recipe and don't bother checking the reverse as the recipe type's generics
        // ensures that it is of the same type
        if (o instanceof ItemStackChemicalToItemStackRecipe<?, ?, ?> other) {
            //Check chemical ingredients first in case the type doesn't match
            return chemicalIngredientConflicts(recipe.getChemicalInput(), other.getChemicalInput()) &&
                   ingredientConflicts(recipe.getItemInput(), other.getItemInput());
        }
        return false;
    }

    @Override
    public Optional<IDecomposedRecipe> decompose(IRecipeManager<? super RECIPE> manager, RECIPE recipe) {
        return decompose(recipe.getItemInput(), recipe.getChemicalInput(), recipe.getOutputDefinition());
    }

    @Override
    public Optional<RECIPE> recompose(IRecipeManager<? super RECIPE> m, ResourceLocation name, IDecomposedRecipe recipe) {
        if (m instanceof ItemStackChemicalToItemStackRecipeManager) {
            ItemStackChemicalToItemStackRecipeManager<CHEMICAL, STACK, INGREDIENT, RECIPE> manager =
                  (ItemStackChemicalToItemStackRecipeManager<CHEMICAL, STACK, INGREDIENT, RECIPE>) m;
            return Optional.of(manager.makeRecipe(name,
                  recipe.getOrThrowSingle(CrTRecipeComponents.ITEM.input()),
                  recipe.getOrThrowSingle(getChemicalComponent().input()),
                  recipe.getOrThrowSingle(CrTRecipeComponents.ITEM.output())
            ));
        }
        return Optional.empty();
    }

    /**
     * @return Chemical component for recomposing recipes.
     */
    protected abstract ChemicalRecipeComponent<CHEMICAL, STACK, INGREDIENT, ?> getChemicalComponent();

    @IRecipeHandler.For(ItemStackGasToItemStackRecipe.class)
    public static class ItemStackGasToItemStackRecipeHandler extends ItemStackChemicalToItemStackRecipeHandler<Gas, GasStack, GasStackIngredient,
          ItemStackGasToItemStackRecipe> {

        @Override
        protected ChemicalRecipeComponent<Gas, GasStack, GasStackIngredient, ?> getChemicalComponent() {
            return CrTRecipeComponents.GAS;
        }
    }

    @IRecipeHandler.For(MetallurgicInfuserRecipe.class)
    public static class MetallurgicInfuserRecipeHandler extends ItemStackChemicalToItemStackRecipeHandler<InfuseType, InfusionStack, InfusionStackIngredient,
          MetallurgicInfuserRecipe> {

        @Override
        protected ChemicalRecipeComponent<InfuseType, InfusionStack, InfusionStackIngredient, ?> getChemicalComponent() {
            return CrTRecipeComponents.INFUSION;
        }
    }

    @IRecipeHandler.For(PaintingRecipe.class)
    public static class PaintingRecipeHandler extends ItemStackChemicalToItemStackRecipeHandler<Pigment, PigmentStack, PigmentStackIngredient, PaintingRecipe> {

        @Override
        protected ChemicalRecipeComponent<Pigment, PigmentStack, PigmentStackIngredient, ?> getChemicalComponent() {
            return CrTRecipeComponents.PIGMENT;
        }
    }
}