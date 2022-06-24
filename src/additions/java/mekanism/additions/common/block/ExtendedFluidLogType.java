package mekanism.additions.common.block;

import mekanism.common.block.states.IFluidLogType;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import org.jetbrains.annotations.NotNull;

public enum ExtendedFluidLogType implements IFluidLogType, StringRepresentable {
    EMPTY("empty", Fluids.EMPTY),
    LAVA("lava", Fluids.LAVA);

    private final String name;
    private final Fluid fluid;

    ExtendedFluidLogType(String name, Fluid fluid) {
        this.name = name;
        this.fluid = fluid;
    }

    @Override
    public Fluid getFluid() {
        return fluid;
    }

    @NotNull
    @Override
    public String getSerializedName() {
        return name;
    }
}