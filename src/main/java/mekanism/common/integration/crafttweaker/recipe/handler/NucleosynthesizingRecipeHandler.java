package mekanism.common.integration.crafttweaker.recipe.handler;

import com.blamejared.crafttweaker.api.recipe.component.BuiltinRecipeComponents;
import com.blamejared.crafttweaker.api.recipe.component.IDecomposedRecipe;
import com.blamejared.crafttweaker.api.recipe.handler.IRecipeHandler;
import com.blamejared.crafttweaker.api.recipe.manager.base.IRecipeManager;
import java.util.Optional;
import mekanism.api.recipes.NucleosynthesizingRecipe;
import mekanism.common.integration.crafttweaker.CrTRecipeComponents;
import mekanism.common.integration.crafttweaker.recipe.manager.NucleosynthesizingRecipeManager;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.Recipe;

@IRecipeHandler.For(NucleosynthesizingRecipe.class)
public class NucleosynthesizingRecipeHandler extends MekanismRecipeHandler<NucleosynthesizingRecipe> {

    @Override
    public String dumpToCommandString(IRecipeManager<? super NucleosynthesizingRecipe> manager, NucleosynthesizingRecipe recipe) {
        return buildCommandString(manager, recipe, recipe.getItemInput(), recipe.getChemicalInput(), recipe.getOutputDefinition(), recipe.getDuration());
    }

    @Override
    public <U extends Recipe<?>> boolean doesConflict(IRecipeManager<? super NucleosynthesizingRecipe> manager, NucleosynthesizingRecipe recipe, U o) {
        //Only support if the other is a nucleosynthesizing recipe and don't bother checking the reverse as the recipe type's generics
        // ensures that it is of the same type
        if (o instanceof NucleosynthesizingRecipe other) {
            return ingredientConflicts(recipe.getItemInput(), other.getItemInput()) && ingredientConflicts(recipe.getChemicalInput(), other.getChemicalInput());
        }
        return false;
    }

    @Override
    public Optional<IDecomposedRecipe> decompose(IRecipeManager<? super NucleosynthesizingRecipe> manager, NucleosynthesizingRecipe recipe) {
        return decompose(recipe.getItemInput(), recipe.getChemicalInput(), recipe.getOutputDefinition(), recipe.getDuration());
    }

    @Override
    public Optional<NucleosynthesizingRecipe> recompose(IRecipeManager<? super NucleosynthesizingRecipe> m, ResourceLocation name, IDecomposedRecipe recipe) {
        if (m instanceof NucleosynthesizingRecipeManager manager) {
            return Optional.of(manager.makeRecipe(name,
                  recipe.getOrThrowSingle(CrTRecipeComponents.ITEM.input()),
                  recipe.getOrThrowSingle(CrTRecipeComponents.GAS.input()),
                  recipe.getOrThrowSingle(CrTRecipeComponents.ITEM.output()),
                  recipe.getOrThrowSingle(BuiltinRecipeComponents.Processing.TIME)
            ));
        }
        return Optional.empty();
    }
}