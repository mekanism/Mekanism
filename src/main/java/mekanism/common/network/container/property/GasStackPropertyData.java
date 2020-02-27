package mekanism.common.network.container.property;

import javax.annotation.Nonnull;
import mekanism.api.chemical.ChemicalUtils;
import mekanism.api.chemical.gas.GasStack;
import mekanism.common.inventory.container.MekanismContainer;
import mekanism.common.network.container.PacketUpdateContainerGasStack;
import net.minecraft.network.PacketBuffer;

public class GasStackPropertyData extends PropertyData {

    @Nonnull
    private final GasStack value;

    public GasStackPropertyData(short property, @Nonnull GasStack value) {
        super(PropertyType.GAS_STACK, property);
        this.value = value;
    }

    @Override
    public PacketUpdateContainerGasStack getSinglePacket(short windowId) {
        return new PacketUpdateContainerGasStack(windowId, getProperty(), value);
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