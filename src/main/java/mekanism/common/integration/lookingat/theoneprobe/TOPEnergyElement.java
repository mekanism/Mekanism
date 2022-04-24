package mekanism.common.integration.lookingat.theoneprobe;

import mcjty.theoneprobe.api.IElement;
import mcjty.theoneprobe.api.IElementFactory;
import mekanism.api.math.FloatingLong;
import mekanism.common.Mekanism;
import mekanism.common.integration.lookingat.EnergyElement;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;

public class TOPEnergyElement extends EnergyElement implements IElement {

    private static final ResourceLocation ID = Mekanism.rl("energy");

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
        return ID;
    }

    public static class Factory implements IElementFactory {

        @Override
        public TOPEnergyElement createElement(FriendlyByteBuf buf) {
            return new TOPEnergyElement(FloatingLong.readFromBuffer(buf), FloatingLong.readFromBuffer(buf));
        }

        @Override
        public ResourceLocation getId() {
            return ID;
        }
    }
}