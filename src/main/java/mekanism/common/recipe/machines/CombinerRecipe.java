package mekanism.common.recipe.machines;

import mekanism.common.recipe.inputs.DoubleMachineInput;
import mekanism.common.recipe.outputs.ItemStackOutput;
import net.minecraft.item.ItemStack;

public class CombinerRecipe extends DoubleMachineRecipe<CombinerRecipe> {

    public CombinerRecipe(DoubleMachineInput input, ItemStackOutput output) {
        super(input, output);
    }

    public CombinerRecipe(ItemStack input, ItemStack extra, ItemStack output) {
        super(input, extra, output);
    }

    @Override
    public CombinerRecipe copy() {
        return new CombinerRecipe(getInput().copy(), getOutput().copy());
    }
}