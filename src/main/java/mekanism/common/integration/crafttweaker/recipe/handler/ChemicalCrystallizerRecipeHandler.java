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
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.Recipe;

@IRecipeHandler.For(ChemicalCrystallizerRecipe.class)
public class ChemicalCrystallizerRecipeHandler extends MekanismRecipeHandler<ChemicalCrystallizerRecipe> {

    @Override
    public String dumpToCommandString(IRecipeManager<? super ChemicalCrystallizerRecipe> manager, ChemicalCrystallizerRecipe recipe) {
        return buildCommandString(manager, recipe, recipe.getInput(), recipe.getOutputDefinition());
    }

    @Override
    public <U extends Recipe<?>> boolean doesConflict(IRecipeManager<? super ChemicalCrystallizerRecipe> manager, ChemicalCrystallizerRecipe recipe, U o) {
        //Only support if the other is a chemical crystallizer recipe and don't bother checking the reverse as the recipe type's generics
        // ensures that it is of the same type
        return o instanceof ChemicalCrystallizerRecipe other && chemicalIngredientConflicts(recipe.getInput(), other.getInput());
    }

    @Override
    public Optional<IDecomposedRecipe> decompose(IRecipeManager<? super ChemicalCrystallizerRecipe> manager, ChemicalCrystallizerRecipe recipe) {
        return decompose(recipe.getInput(), recipe.getOutputDefinition());
    }

    @Override
    public Optional<ChemicalCrystallizerRecipe> recompose(IRecipeManager<? super ChemicalCrystallizerRecipe> m, ResourceLocation name, IDecomposedRecipe recipe) {
        if (m instanceof ChemicalCrystallizerRecipeManager manager) {
            ChemicalStackIngredient<?, ?> input = CrTRecipeComponents.CHEMICAL_COMPONENTS.stream()
                  .map(chemicalComponent -> CrTUtils.getSingleIfPresent(recipe, chemicalComponent.input()))
                  .filter(Optional::isPresent)
                  .map(Optional::get)
                  .findFirst()
                  .orElseThrow(() -> new IllegalArgumentException("No chemical input ingredient provided."));
            return Optional.of(manager.makeRecipe(name,
                  input,
                  recipe.getOrThrowSingle(CrTRecipeComponents.ITEM.output())
            ));
        }
        return Optional.empty();
    }
}