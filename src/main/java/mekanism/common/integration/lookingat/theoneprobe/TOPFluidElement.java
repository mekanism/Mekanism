package mekanism.common.integration.lookingat.theoneprobe;

import javax.annotation.Nonnull;
import mcjty.theoneprobe.api.IElement;
import mcjty.theoneprobe.api.IElementFactory;
import mekanism.common.Mekanism;
import mekanism.common.integration.lookingat.FluidElement;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.fluids.FluidStack;

public class TOPFluidElement extends FluidElement implements IElement {

    private static final ResourceLocation ID = Mekanism.rl("fluid");

    public TOPFluidElement(@Nonnull FluidStack stored, int capacity) {
        super(stored, capacity);
    }

    @Override
    public void toBytes(FriendlyByteBuf buf) {
        buf.writeFluidStack(stored);
        buf.writeVarInt(capacity);
    }

    @Override
    public ResourceLocation getID() {
        return ID;
    }

    public static class Factory implements IElementFactory {

        @Override
        public TOPFluidElement createElement(FriendlyByteBuf buf) {
            return new TOPFluidElement(buf.readFluidStack(), buf.readVarInt());
        }

        @Override
        public ResourceLocation getId() {
            return ID;
        }
    }
}