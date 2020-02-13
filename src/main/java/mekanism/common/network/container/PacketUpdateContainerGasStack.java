package mekanism.common.network.container;

import javax.annotation.Nonnull;
import mekanism.api.gas.GasStack;
import mekanism.common.PacketHandler;
import mekanism.common.inventory.container.MekanismContainer;
import net.minecraft.network.PacketBuffer;

/**
 * Version of {@link net.minecraft.network.play.server.SWindowPropertyPacket} for gas stacks
 */
public class PacketUpdateContainerGasStack extends PacketUpdateContainer<PacketUpdateContainerGasStack> {

    @Nonnull
    private final GasStack value;

    public PacketUpdateContainerGasStack(short windowId, short property, @Nonnull GasStack value) {
        super(windowId, property);
        this.value = value;
    }

    private PacketUpdateContainerGasStack(PacketBuffer buffer) {
        super(buffer);
        this.value = PacketHandler.readGasStack(buffer);
    }

    @Override
    protected void encode(PacketBuffer buffer) {
        super.encode(buffer);
        PacketHandler.writeChemicalStack(buffer, value);
    }

    @Override
    protected void handle(MekanismContainer container, PacketUpdateContainerGasStack message) {
        container.handleWindowProperty(message.property, message.value);
    }

    public static PacketUpdateContainerGasStack decode(PacketBuffer buf) {
        return new PacketUpdateContainerGasStack(buf);
    }
}