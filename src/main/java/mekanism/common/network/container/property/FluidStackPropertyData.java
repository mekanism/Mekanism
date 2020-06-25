package mekanism.common.network.container.property;

import javax.annotation.Nonnull;
import mekanism.common.inventory.container.MekanismContainer;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fluids.FluidStack;

public class FluidStackPropertyData extends PropertyData {

    @Nonnull
    private final FluidStack value;

    public FluidStackPropertyData(short property, @Nonnull FluidStack value) {
        super(PropertyType.FLUID_STACK, property);
        this.value = value;
    }

    @Override
    public void handleWindowProperty(MekanismContainer container) {
        container.handleWindowProperty(getProperty(), value);
    }

    @Override
    public void writeToPacket(PacketBuffer buffer) {
        super.writeToPacket(buffer);
        buffer.writeFluidStack(value);
    }
}