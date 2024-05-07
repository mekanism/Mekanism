package mekanism.common.integration.lookingat.theoneprobe;

import mcjty.theoneprobe.api.IElement;
import mcjty.theoneprobe.api.IElementFactory;
import mekanism.common.integration.lookingat.ChemicalElement;
import mekanism.common.integration.lookingat.LookingAtUtils;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;

public class TOPChemicalElement extends ChemicalElement implements IElement {

    protected TOPChemicalElement(ChemicalElement element) {
        super(element.getStored(), element.getCapacity());
    }

    @Override
    public void toBytes(RegistryFriendlyByteBuf buffer) {
        STREAM_CODEC.encode(buffer, this);
    }

    public static class ChemicalElementFactory implements IElementFactory {

        @Override
        public TOPChemicalElement createElement(RegistryFriendlyByteBuf buffer) {
            return new TOPChemicalElement(STREAM_CODEC.decode(buffer));
        }

        @Override
        public ResourceLocation getId() {
            return LookingAtUtils.CHEMICAL;
        }
    }
}