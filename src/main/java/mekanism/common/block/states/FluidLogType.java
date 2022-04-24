package mekanism.common.block.states;

import javax.annotation.Nonnull;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;

public enum FluidLogType implements IFluidLogType, StringRepresentable {
    EMPTY("empty", Fluids.EMPTY),
    WATER("water", Fluids.WATER),
    LAVA("lava", Fluids.LAVA);

    private final String name;
    private final Fluid fluid;

    FluidLogType(String name, Fluid fluid) {
        this.name = name;
        this.fluid = fluid;
    }

    @Override
    public Fluid getFluid() {
        return fluid;
    }

    @Nonnull
    @Override
    public String getSerializedName() {
        return name;
    }
}