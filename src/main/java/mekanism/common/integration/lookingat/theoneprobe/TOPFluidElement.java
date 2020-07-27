package mekanism.common.integration.lookingat.theoneprobe;

import javax.annotation.Nonnull;
import mcjty.theoneprobe.api.IElement;
import mekanism.common.integration.lookingat.FluidElement;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fluids.FluidStack;

public class TOPFluidElement extends FluidElement implements IElement {

    public TOPFluidElement(@Nonnull FluidStack stored, int capacity) {
        super(stored, capacity);
    }

    public TOPFluidElement(PacketBuffer buf) {
        this(buf.readFluidStack(), buf.readVarInt());
    }

    @Override
    public void toBytes(PacketBuffer buf) {
        buf.writeFluidStack(stored);
        buf.writeVarInt(capacity);
    }

    @Override
    public int getID() {
        return TOPProvider.FLUID_ELEMENT_ID;
    }
}