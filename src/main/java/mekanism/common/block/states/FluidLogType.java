package mekanism.common.block.states;

import net.minecraft.util.StringRepresentable;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import org.jetbrains.annotations.NotNull;

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

    @NotNull
    @Override
    public String getSerializedName() {
        return name;
    }
}