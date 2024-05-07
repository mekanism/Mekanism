package mekanism.common.integration.lookingat.theoneprobe;

import mcjty.theoneprobe.api.IElement;
import mcjty.theoneprobe.api.IElementFactory;
import mekanism.common.integration.lookingat.FluidElement;
import mekanism.common.integration.lookingat.LookingAtUtils;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;

public class TOPFluidElement extends FluidElement implements IElement {

    public TOPFluidElement(FluidElement element) {
        super(element.getStored(), element.getCapacity());
    }

    @Override
    public void toBytes(RegistryFriendlyByteBuf buffer) {
        STREAM_CODEC.encode(buffer, this);
    }

    public static class Factory implements IElementFactory {

        @Override
        public TOPFluidElement createElement(RegistryFriendlyByteBuf buffer) {
            return new TOPFluidElement(STREAM_CODEC.decode(buffer));
        }

        @Override
        public ResourceLocation getId() {
            return LookingAtUtils.FLUID;
        }
    }
}