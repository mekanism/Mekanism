package mekanism.common.integration.lookingat.theoneprobe;

import mcjty.theoneprobe.api.IElement;
import mcjty.theoneprobe.api.IElementFactory;
import mekanism.common.integration.lookingat.EnergyElement;
import mekanism.common.integration.lookingat.LookingAtUtils;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;

public class TOPEnergyElement extends EnergyElement implements IElement {

    public TOPEnergyElement(EnergyElement element) {
        super(element.getEnergy(), element.getMaxEnergy());
    }

    @Override
    public void toBytes(RegistryFriendlyByteBuf buf) {
        STREAM_CODEC.encode(buf, this);
    }

    public static class Factory implements IElementFactory {

        @Override
        public TOPEnergyElement createElement(RegistryFriendlyByteBuf buf) {
            return new TOPEnergyElement(STREAM_CODEC.decode(buf));
        }

        @Override
        public ResourceLocation getId() {
            return LookingAtUtils.ENERGY;
        }
    }
}