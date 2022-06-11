package mekanism.common.integration.lookingat.theoneprobe;

import mcjty.theoneprobe.api.IElement;
import mcjty.theoneprobe.api.IElementFactory;
import mekanism.api.math.FloatingLong;
import mekanism.common.integration.lookingat.EnergyElement;
import mekanism.common.integration.lookingat.LookingAtUtils;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;

public class TOPEnergyElement extends EnergyElement implements IElement {

    public TOPEnergyElement(FloatingLong energy, FloatingLong maxEnergy) {
        super(energy, maxEnergy);
    }

    @Override
    public void toBytes(FriendlyByteBuf buf) {
        energy.writeToBuffer(buf);
        maxEnergy.writeToBuffer(buf);
    }

    @Override
    public ResourceLocation getID() {
        return LookingAtUtils.ENERGY;
    }

    public static class Factory implements IElementFactory {

        @Override
        public TOPEnergyElement createElement(FriendlyByteBuf buf) {
            return new TOPEnergyElement(FloatingLong.readFromBuffer(buf), FloatingLong.readFromBuffer(buf));
        }

        @Override
        public ResourceLocation getId() {
            return LookingAtUtils.ENERGY;
        }
    }
}