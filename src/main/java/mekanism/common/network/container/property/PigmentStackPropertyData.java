package mekanism.common.network.container.property;

import javax.annotation.Nonnull;
import mekanism.api.chemical.ChemicalUtils;
import mekanism.api.chemical.pigment.PigmentStack;
import mekanism.common.inventory.container.MekanismContainer;
import mekanism.common.network.container.PacketUpdateContainerPigmentStack;
import net.minecraft.network.PacketBuffer;

public class PigmentStackPropertyData extends PropertyData {

    @Nonnull
    private final PigmentStack value;

    public PigmentStackPropertyData(short property, @Nonnull PigmentStack value) {
        super(PropertyType.PIGMENT_STACK, property);
        this.value = value;
    }

    @Override
    public PacketUpdateContainerPigmentStack getSinglePacket(short windowId) {
        return new PacketUpdateContainerPigmentStack(windowId, getProperty(), value);
    }

    @Override
    public void handleWindowProperty(MekanismContainer container) {
        container.handleWindowProperty(getProperty(), value);
    }

    @Override
    public void writeToPacket(PacketBuffer buffer) {
        super.writeToPacket(buffer);
        ChemicalUtils.writeChemicalStack(buffer, value);
    }
}