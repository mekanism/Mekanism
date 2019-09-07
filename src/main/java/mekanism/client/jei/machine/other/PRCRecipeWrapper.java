package mekanism.client.jei.machine.other;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import mekanism.api.annotations.NonNull;
import mekanism.api.gas.GasStack;
import mekanism.api.recipes.PressurizedReactionRecipe;
import mekanism.client.jei.MekanismJEI;
import mekanism.client.jei.machine.MekanismRecipeWrapper;
import mezz.jei.api.ingredients.IIngredients;
import mezz.jei.api.ingredients.VanillaTypes;
import net.minecraft.item.ItemStack;
import org.apache.commons.lang3.tuple.Pair;

public class PRCRecipeWrapper extends MekanismRecipeWrapper<PressurizedReactionRecipe> {

    public PRCRecipeWrapper(PressurizedReactionRecipe recipe) {
        super(recipe);
    }

    @Override
    public void getIngredients(IIngredients ingredients) {
        ingredients.setInputLists(VanillaTypes.ITEM, Collections.singletonList(Arrays.asList(recipe.getInputSolid().getMatchingStacks())));
        ingredients.setInputLists(VanillaTypes.FLUID, Collections.singletonList(recipe.getInputFluid().getRepresentations()));
        ingredients.setInputLists(MekanismJEI.TYPE_GAS, Collections.singletonList(recipe.getGasInput().getRepresentations()));
        @NonNull Pair<List<@NonNull ItemStack>, @NonNull GasStack> outputDefinition = recipe.getOutputDefinition();
        ingredients.setOutputLists(VanillaTypes.ITEM, Collections.singletonList(outputDefinition.getLeft()));
        ingredients.setOutput(MekanismJEI.TYPE_GAS, outputDefinition.getRight());
    }
}