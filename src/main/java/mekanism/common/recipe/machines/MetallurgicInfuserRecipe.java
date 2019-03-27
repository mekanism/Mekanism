package mekanism.common.recipe.machines;

import mekanism.common.InfuseStorage;
import mekanism.common.recipe.inputs.InfusionInput;
import mekanism.common.recipe.outputs.ItemStackOutput;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;

public class MetallurgicInfuserRecipe extends MachineRecipe<InfusionInput, ItemStackOutput, MetallurgicInfuserRecipe> {

    public MetallurgicInfuserRecipe(InfusionInput input, ItemStackOutput output) {
        super(input, output);
    }

    public MetallurgicInfuserRecipe(InfusionInput input, ItemStack output) {
        this(input, new ItemStackOutput(output));
    }

    public boolean inputMatches(NonNullList<ItemStack> inventory, int inputIndex, InfuseStorage infuse) {
        return getInput().use(inventory, inputIndex, infuse, false);
    }

    public boolean canOperate(NonNullList<ItemStack> inventory, int inputIndex, int outputIndex, InfuseStorage infuse) {
        return inputMatches(inventory, inputIndex, infuse) && getOutput().applyOutputs(inventory, outputIndex, false);
    }

    @Override
    public MetallurgicInfuserRecipe copy() {
        return new MetallurgicInfuserRecipe(getInput(), getOutput());
    }

    public void output(NonNullList<ItemStack> inventory, int inputIndex, int outputIndex, InfuseStorage infuseStored) {
        if (getInput().use(inventory, inputIndex, infuseStored, true)) {
            getOutput().applyOutputs(inventory, outputIndex, true);
        }
    }
}
