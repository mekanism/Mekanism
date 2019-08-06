package mekanism.common.recipe.outputs;

import net.minecraft.nbt.CompoundNBT;

public abstract class MachineOutput<OUTPUT extends MachineOutput<OUTPUT>> {

    public abstract OUTPUT copy();

    public abstract void load(CompoundNBT nbtTags);
}