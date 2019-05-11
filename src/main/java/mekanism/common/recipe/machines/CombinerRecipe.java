package mekanism.common.recipe.machines;

import mekanism.common.recipe.inputs.DoubleMachineInput;
import mekanism.common.recipe.outputs.ItemStackOutput;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;

public class CombinerRecipe extends DoubleMachineRecipe<CombinerRecipe> {

    public CombinerRecipe(DoubleMachineInput input, ItemStackOutput output) {
        super(input, output);
    }

    public CombinerRecipe(ItemStack input, ItemStack extra, ItemStack output) {
        super(input, extra, output);
    }

    /**
     * @deprecated Replaced by {@link #CombinerRecipe(ItemStack, ItemStack, ItemStack)}. May be removed with Minecraft 1.13.
     */
    @Deprecated
    public CombinerRecipe(ItemStack input, ItemStack output) {
        super(input, new ItemStack(Blocks.COBBLESTONE), output);
    }

    @Override
    public CombinerRecipe copy() {
        return new CombinerRecipe(getInput().copy(), getOutput().copy());
    }
}
