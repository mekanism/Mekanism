package mekanism.common.network.container.property;

import javax.annotation.Nonnull;
import mekanism.api.chemical.ChemicalUtils;
import mekanism.api.chemical.slurry.SlurryStack;
import mekanism.common.inventory.container.MekanismContainer;
import mekanism.common.network.container.PacketUpdateContainerSlurryStack;
import net.minecraft.network.PacketBuffer;

public class SlurryStackPropertyData extends PropertyData {

    @Nonnull
    private final SlurryStack value;

    public SlurryStackPropertyData(short property, @Nonnull SlurryStack value) {
        super(PropertyType.SLURRY_STACK, property);
        this.value = value;
    }

    @Override
    public PacketUpdateContainerSlurryStack getSinglePacket(short windowId) {
        return new PacketUpdateContainerSlurryStack(windowId, getProperty(), value);
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