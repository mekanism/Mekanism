package mekanism.common.recipe.machines;

import mekanism.api.gas.Gas;
import mekanism.common.recipe.inputs.AdvancedMachineInput;
import mekanism.common.recipe.outputs.ItemStackOutput;
import net.minecraft.item.ItemStack;

public class InjectionRecipe extends AdvancedMachineRecipe<InjectionRecipe> {

    public InjectionRecipe(AdvancedMachineInput input, ItemStackOutput output) {
        super(input, output);
    }

    public InjectionRecipe(ItemStack input, Gas gas, ItemStack output) {
        super(input, gas, output);
    }

    @Override
    public InjectionRecipe copy() {
        return new InjectionRecipe(getInput().copy(), getOutput().copy());
    }
}
