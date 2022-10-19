package mekanism.common.integration.crafttweaker.recipe.handler;

import com.blamejared.crafttweaker.api.recipe.component.IDecomposedRecipe;
import com.blamejared.crafttweaker.api.recipe.handler.IRecipeHandler;
import com.blamejared.crafttweaker.api.recipe.manager.base.IRecipeManager;
import java.util.Optional;
import mekanism.api.recipes.CombinerRecipe;
import mekanism.api.recipes.ingredients.ItemStackIngredient;
import mekanism.common.integration.crafttweaker.CrTRecipeComponents;
import mekanism.common.integration.crafttweaker.CrTUtils;
import mekanism.common.integration.crafttweaker.CrTUtils.UnaryTypePair;
import mekanism.common.integration.crafttweaker.recipe.manager.CombinerRecipeManager;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.Recipe;

@IRecipeHandler.For(CombinerRecipe.class)
public class CombinerRecipeHandler extends MekanismRecipeHandler<CombinerRecipe> {

    @Override
    public String dumpToCommandString(IRecipeManager<? super CombinerRecipe> manager, CombinerRecipe recipe) {
        return buildCommandString(manager, recipe, recipe.getMainInput(), recipe.getExtraInput(), recipe.getOutputDefinition());
    }

    @Override
    public <U extends Recipe<?>> boolean doesConflict(IRecipeManager<? super CombinerRecipe> manager, CombinerRecipe recipe, U o) {
        //Only support if the other is a combiner recipe and don't bother checking the reverse as the recipe type's generics
        // ensures that it is of the same type
        if (o instanceof CombinerRecipe other) {
            return ingredientConflicts(recipe.getMainInput(), other.getMainInput()) &&
                   ingredientConflicts(recipe.getExtraInput(), other.getExtraInput());
        }
        return false;
    }

    @Override
    public Optional<IDecomposedRecipe> decompose(IRecipeManager<? super CombinerRecipe> manager, CombinerRecipe recipe) {
        return decompose(recipe.getMainInput(), recipe.getExtraInput(), recipe.getOutputDefinition());
    }

    @Override
    public Optional<CombinerRecipe> recompose(IRecipeManager<? super CombinerRecipe> m, ResourceLocation name, IDecomposedRecipe recipe) {
        if (m instanceof CombinerRecipeManager manager) {
            UnaryTypePair<ItemStackIngredient> inputs = CrTUtils.getPair(recipe, CrTRecipeComponents.ITEM.input());
            return Optional.of(manager.makeRecipe(name,
                  inputs.a(),
                  inputs.b(),
                  recipe.getOrThrowSingle(CrTRecipeComponents.ITEM.output())
            ));
        }
        return Optional.empty();
    }
}