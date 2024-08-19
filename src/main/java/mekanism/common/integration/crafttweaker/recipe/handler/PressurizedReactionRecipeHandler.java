package mekanism.common.integration.crafttweaker.recipe.handler;

import com.blamejared.crafttweaker.api.item.IItemStack;
import com.blamejared.crafttweaker.api.recipe.component.BuiltinRecipeComponents;
import com.blamejared.crafttweaker.api.recipe.component.IDecomposedRecipe;
import com.blamejared.crafttweaker.api.recipe.handler.IRecipeHandler;
import com.blamejared.crafttweaker.api.recipe.manager.base.IRecipeManager;
import java.util.List;
import java.util.Optional;
import mekanism.api.chemical.ChemicalStack;
import mekanism.api.recipes.PressurizedReactionRecipe;
import mekanism.api.recipes.PressurizedReactionRecipe.PressurizedReactionRecipeOutput;
import mekanism.common.integration.crafttweaker.CrTRecipeComponents;
import mekanism.common.integration.crafttweaker.CrTUtils;
import mekanism.common.integration.crafttweaker.chemical.ICrTChemicalStack;
import mekanism.common.integration.crafttweaker.recipe.manager.PressurizedReactionRecipeManager;
import net.minecraft.core.RegistryAccess;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeHolder;

@IRecipeHandler.For(PressurizedReactionRecipe.class)
public class PressurizedReactionRecipeHandler extends MekanismRecipeHandler<PressurizedReactionRecipe> {

    @Override
    public String dumpToCommandString(IRecipeManager<? super PressurizedReactionRecipe> manager, RegistryAccess registryAccess,
          RecipeHolder<PressurizedReactionRecipe> recipeHolder) {
        ItemStack itemOutput;
        ChemicalStack chemicalOutput;
        PressurizedReactionRecipe recipe = recipeHolder.value();
        List<PressurizedReactionRecipeOutput> outputs = recipe.getOutputDefinition();
        if (outputs.isEmpty()) {
            //Validate it isn't empty, which shouldn't be possible
            itemOutput = ItemStack.EMPTY;
            chemicalOutput = ChemicalStack.EMPTY;
        } else {
            //Outputs sometimes are as lists, try wrapping them into a single element
            // eventually we may want to try listing them all somehow?
            PressurizedReactionRecipeOutput output = outputs.getFirst();
            itemOutput = output.item();
            chemicalOutput = output.chemical();
        }
        //Note: We can handle skipping optional params like this because only one output should be empty at a time
        // if there is only a single output, which means we can safely skip the other
        return buildCommandString(manager, recipeHolder, recipe.getInputSolid(), recipe.getInputFluid(), recipe.getInputChemical(), recipe.getDuration(),
              itemOutput.isEmpty() ? SKIP_OPTIONAL_PARAM : itemOutput, chemicalOutput.isEmpty() ? SKIP_OPTIONAL_PARAM : chemicalOutput,
              recipe.getEnergyRequired() == 0L ? SKIP_OPTIONAL_PARAM : recipe.getEnergyRequired()
        );
    }

    @Override
    public <U extends Recipe<?>> boolean doesConflict(IRecipeManager<? super PressurizedReactionRecipe> manager, PressurizedReactionRecipe recipe, U o) {
        //Only support if the other is a reaction recipe and don't bother checking the reverse as the recipe type's generics
        // ensures that it is of the same type
        if (o instanceof PressurizedReactionRecipe other) {
            return ingredientConflicts(recipe.getInputSolid(), other.getInputSolid()) &&
                   ingredientConflicts(recipe.getInputFluid(), other.getInputFluid()) &&
                   ingredientConflicts(recipe.getInputChemical(), other.getInputChemical());
        }
        return false;
    }

    @Override
    public Optional<IDecomposedRecipe> decompose(IRecipeManager<? super PressurizedReactionRecipe> manager, RegistryAccess registryAccess, PressurizedReactionRecipe recipe) {
        return decompose(recipe.getInputSolid(), recipe.getInputFluid(), recipe.getInputChemical(), recipe.getDuration(), recipe.getOutputDefinition(),
              recipe.getEnergyRequired());
    }

    @Override
    public Optional<PressurizedReactionRecipe> recompose(IRecipeManager<? super PressurizedReactionRecipe> m, RegistryAccess registryAccess, IDecomposedRecipe recipe) {
        if (m instanceof PressurizedReactionRecipeManager manager) {
            Optional<IItemStack> optionalOutputItem = CrTUtils.getSingleIfPresent(recipe, CrTRecipeComponents.ITEM.output());
            ItemStack outputItem;
            ChemicalStack outputChemical;
            if (optionalOutputItem.isPresent()) {
                outputItem = optionalOutputItem.get().getImmutableInternal();
                outputChemical = CrTUtils.getSingleIfPresent(recipe, CrTRecipeComponents.CHEMICAL.output())
                      .map(ICrTChemicalStack::getImmutableInternal)
                      .orElse(ChemicalStack.EMPTY);
            } else {
                outputItem = ItemStack.EMPTY;
                outputChemical = recipe.getOrThrowSingle(CrTRecipeComponents.CHEMICAL.output()).getImmutableInternal();
            }
            return Optional.of(manager.makeRecipe(
                  recipe.getOrThrowSingle(CrTRecipeComponents.ITEM.input()),
                  recipe.getOrThrowSingle(CrTRecipeComponents.FLUID.input()),
                  recipe.getOrThrowSingle(CrTRecipeComponents.CHEMICAL.input()),
                  recipe.getOrThrowSingle(BuiltinRecipeComponents.Processing.TIME),
                  outputItem,
                  outputChemical,
                  CrTUtils.getSingleIfPresent(recipe, CrTRecipeComponents.ENERGY).orElse(0L)
            ));
        }
        return Optional.empty();
    }
}