package mekanism.common.integration.lookingat.theoneprobe;

import mcjty.theoneprobe.api.IElement;
import mcjty.theoneprobe.api.IElementFactory;
import mekanism.common.integration.lookingat.EnergyElement;
import mekanism.common.integration.lookingat.LookingAtUtils;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.resources.Identifier;

public class TOPEnergyElement extends EnergyElement implements IElement {

    public TOPEnergyElement(EnergyElement element) {
        super(element.getEnergy(), element.getMaxEnergy());
    }

    @Override
    public void toBytes(RegistryFriendlyByteBuf buffer) {
        STREAM_CODEC.encode(buffer, this);
    }

    public static class Factory implements IElementFactory {

        @Override
        public TOPEnergyElement createElement(RegistryFriendlyByteBuf buffer) {
            return new TOPEnergyElement(STREAM_CODEC.decode(buffer));
        }

        @Override
        public Identifier getId() {
            return LookingAtUtils.ENERGY;
        }
    }
}