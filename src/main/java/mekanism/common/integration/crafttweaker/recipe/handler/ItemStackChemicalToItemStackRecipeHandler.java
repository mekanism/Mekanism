package mekanism.common.integration.crafttweaker.recipe.handler;

import com.blamejared.crafttweaker.api.recipe.component.IDecomposedRecipe;
import com.blamejared.crafttweaker.api.recipe.handler.IRecipeHandler;
import com.blamejared.crafttweaker.api.recipe.manager.base.IRecipeManager;
import java.util.Optional;
import mekanism.api.chemical.Chemical;
import mekanism.api.chemical.ChemicalStack;
import mekanism.api.recipes.ItemStackGasToItemStackRecipe;
import mekanism.api.recipes.MetallurgicInfuserRecipe;
import mekanism.api.recipes.PaintingRecipe;
import mekanism.api.recipes.chemical.ItemStackChemicalToItemStackRecipe;
import mekanism.api.recipes.ingredients.ChemicalStackIngredient;
import mekanism.common.integration.crafttweaker.CrTRecipeComponents;
import mekanism.common.integration.crafttweaker.CrTRecipeComponents.ChemicalRecipeComponent;
import mekanism.common.integration.crafttweaker.recipe.manager.ItemStackChemicalToItemStackRecipeManager;
import net.minecraft.core.RegistryAccess;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeHolder;

public abstract class ItemStackChemicalToItemStackRecipeHandler<RECIPE extends ItemStackChemicalToItemStackRecipe> extends MekanismRecipeHandler<RECIPE> {

    @Override
    public String dumpToCommandString(IRecipeManager<? super RECIPE> manager, RegistryAccess registryAccess, RecipeHolder<RECIPE> recipeHolder) {
        RECIPE recipe = recipeHolder.value();
        return buildCommandString(manager, recipeHolder, recipe.getItemInput(), recipe.getChemicalInput(), recipe.getOutputDefinition());
    }

    @Override
    public <U extends Recipe<?>> boolean doesConflict(IRecipeManager<? super RECIPE> manager, RECIPE recipe, U o) {
        //Only support if the other is an itemstack chemical to itemstack recipe and don't bother checking the reverse as the recipe type's generics
        // ensures that it is of the same type
        if (o instanceof ItemStackChemicalToItemStackRecipe other) {
            //Check chemical ingredients first in case the type doesn't match
            return chemicalIngredientConflicts(recipe.getChemicalInput(), other.getChemicalInput()) &&
                   ingredientConflicts(recipe.getItemInput(), other.getItemInput());
        }
        return false;
    }

    @Override
    public Optional<IDecomposedRecipe> decompose(IRecipeManager<? super RECIPE> manager, RegistryAccess registryAccess, RECIPE recipe) {
        return decompose(recipe.getItemInput(), recipe.getChemicalInput(), recipe.getOutputDefinition());
    }

    @Override
    public Optional<RECIPE> recompose(IRecipeManager<? super RECIPE> m, RegistryAccess registryAccess, IDecomposedRecipe recipe) {
        if (m instanceof ItemStackChemicalToItemStackRecipeManager) {
            ItemStackChemicalToItemStackRecipeManager<Chemical, ChemicalStack, ChemicalStackIngredient, RECIPE> manager =
                  (ItemStackChemicalToItemStackRecipeManager<Chemical, ChemicalStack, ChemicalStackIngredient, RECIPE>) m;
            return Optional.of(manager.makeRecipe(
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
    protected abstract ChemicalRecipeComponent getChemicalComponent();

    @IRecipeHandler.For(ItemStackGasToItemStackRecipe.class)
    public static class ItemStackGasToItemStackRecipeHandler extends ItemStackChemicalToItemStackRecipeHandler<
          ItemStackGasToItemStackRecipe> {

        @Override
        protected ChemicalRecipeComponent getChemicalComponent() {
            return CrTRecipeComponents.CHEMICAL;
        }
    }

    @IRecipeHandler.For(MetallurgicInfuserRecipe.class)
    public static class MetallurgicInfuserRecipeHandler extends ItemStackChemicalToItemStackRecipeHandler<
          MetallurgicInfuserRecipe> {

        @Override
        protected ChemicalRecipeComponent getChemicalComponent() {
            return CrTRecipeComponents.CHEMICAL;
        }
    }

    @IRecipeHandler.For(PaintingRecipe.class)
    public static class PaintingRecipeHandler extends ItemStackChemicalToItemStackRecipeHandler<PaintingRecipe> {

        @Override
        protected ChemicalRecipeComponent getChemicalComponent() {
            return CrTRecipeComponents.CHEMICAL;
        }
    }
}