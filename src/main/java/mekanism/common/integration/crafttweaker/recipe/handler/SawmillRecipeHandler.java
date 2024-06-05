package mekanism.common.integration.crafttweaker.recipe.handler;

import com.blamejared.crafttweaker.api.ingredient.IIngredientWithAmount;
import com.blamejared.crafttweaker.api.item.IItemStack;
import com.blamejared.crafttweaker.api.recipe.component.DecomposedRecipeBuilder;
import com.blamejared.crafttweaker.api.recipe.component.IDecomposedRecipe;
import com.blamejared.crafttweaker.api.recipe.handler.IRecipeHandler;
import com.blamejared.crafttweaker.api.recipe.manager.base.IRecipeManager;
import java.util.List;
import java.util.Optional;
import mekanism.api.recipes.SawmillRecipe;
import mekanism.common.integration.crafttweaker.CrTRecipeComponents;
import mekanism.common.integration.crafttweaker.CrTUtils;
import mekanism.common.integration.crafttweaker.recipe.manager.SawmillRecipeManager;
import net.minecraft.core.RegistryAccess;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeHolder;

@IRecipeHandler.For(SawmillRecipe.class)
public class SawmillRecipeHandler extends MekanismRecipeHandler<SawmillRecipe> {

    @Override
    public String dumpToCommandString(IRecipeManager<? super SawmillRecipe> manager, RegistryAccess registryAccess, RecipeHolder<SawmillRecipe> recipeHolder) {
        SawmillRecipe recipe = recipeHolder.value();
        //Note: We take advantage of the fact that if we have a recipe we have at least one output and that we can skip parameters
        // as if they were optional
        boolean hasSecondary = recipe.getSecondaryChance() > 0;
        List<ItemStack> mainOutputDefinition = recipe.getMainOutputDefinition();
        return buildCommandString(manager, recipeHolder, recipe.getInput(),
              mainOutputDefinition.isEmpty() ? SKIP_OPTIONAL_PARAM : mainOutputDefinition,
              hasSecondary ? recipe.getSecondaryOutputDefinition() : SKIP_OPTIONAL_PARAM,
              hasSecondary ? recipe.getSecondaryChance() : SKIP_OPTIONAL_PARAM
        );
    }

    @Override
    public <U extends Recipe<?>> boolean doesConflict(IRecipeManager<? super SawmillRecipe> manager, SawmillRecipe recipe, U o) {
        //Only support if the other is a sawmill recipe and don't bother checking the reverse as the recipe type's generics
        // ensures that it is of the same type
        return o instanceof SawmillRecipe other && ingredientConflicts(recipe.getInput(), other.getInput());
    }

    @Override
    public Optional<IDecomposedRecipe> decompose(IRecipeManager<? super SawmillRecipe> manager, RegistryAccess registryAccess, SawmillRecipe recipe) {
        List<ItemStack> mainOutputDefinition = recipe.getMainOutputDefinition();
        if (mainOutputDefinition.size() > 1) {
            return Optional.empty();
        }
        List<ItemStack> secondaryOutputDefinition = recipe.getSecondaryOutputDefinition();
        if (secondaryOutputDefinition.size() > 1 || secondaryOutputDefinition.isEmpty() == recipe.getSecondaryChance() > 0) {
            //Multiple secondary outputs, or
            // invalid recipe:
            // secondary outputs is empty and chance greater than zero, or
            // secondary outputs is not empty and chance is zero
            return Optional.empty();
        } else if (mainOutputDefinition.isEmpty() && secondaryOutputDefinition.isEmpty()) {
            //No output, invalid recipe
            return Optional.empty();
        }
        DecomposedRecipeBuilder builder = IDecomposedRecipe.builder()
              .with(CrTRecipeComponents.ITEM.input(), CrTUtils.toCrT(recipe.getInput()));
        if (mainOutputDefinition.isEmpty()) {
            //Only has a secondary output
            builder.with(CrTRecipeComponents.ITEM.output(), CrTUtils.convertItems(secondaryOutputDefinition))
                  .with(CrTRecipeComponents.CHANCE, recipe.getSecondaryChance());
        } else if (secondaryOutputDefinition.isEmpty()) {
            //Only has a main output
            builder.with(CrTRecipeComponents.ITEM.output(), CrTUtils.convertItems(mainOutputDefinition));
        } else {
            //Has both main and secondary outputs
            builder.with(CrTRecipeComponents.ITEM.output(), CrTUtils.convertItems(List.of(mainOutputDefinition.getFirst(), secondaryOutputDefinition.getFirst())))
                  .with(CrTRecipeComponents.CHANCE, recipe.getSecondaryChance());
        }
        return Optional.of(builder.build());
    }

    @Override
    public Optional<SawmillRecipe> recompose(IRecipeManager<? super SawmillRecipe> m, RegistryAccess registryAccess, IDecomposedRecipe recipe) {
        if (m instanceof SawmillRecipeManager manager) {
            IIngredientWithAmount input = recipe.getOrThrowSingle(CrTRecipeComponents.ITEM.input());
            List<IItemStack> outputs = recipe.get(CrTRecipeComponents.ITEM.output());
            if (outputs == null || outputs.isEmpty() || outputs.size() > 2) {
                throw new IllegalArgumentException("Incorrect number of outputs specified. Must be either one or two outputs, and have a secondary chance if two.");
            }
            double chance = CrTUtils.getSingleIfPresent(recipe, CrTRecipeComponents.CHANCE).orElse(0D);
            if (chance == 0 && outputs.size() == 2) {
                //Primary and secondary output but no chance of secondary
                throw new IllegalArgumentException("No chance of specified secondary output.");
            }
            IItemStack output = outputs.get(0);
            IItemStack secondaryOutput;
            if (outputs.size() == 1) {
                if (chance > 1 && chance < 2) {
                    //If there is only a single output and the chance is between one and two,
                    // then use it as a primary and a chance based secondary
                    chance -= 1;
                    secondaryOutput = output.copy();
                } else {
                    secondaryOutput = IItemStack.empty();
                }
            } else {
                secondaryOutput = outputs.get(1);
            }
            if (secondaryOutput.isEmpty()) {
                if (chance == 0 || chance == 1) {
                    //Only has a main output (or only secondary with 100% chance, so we can treat it as a main output)
                    return Optional.of(manager.makeRecipe(
                          input,
                          output
                    ));
                }
                //Only has a secondary output
                return Optional.of(manager.makeRecipe(
                      input,
                      output,
                      chance
                ));
            }
            return Optional.of(manager.makeRecipe(
                  input,
                  output,
                  secondaryOutput,
                  chance
            ));
        }
        return Optional.empty();
    }
}