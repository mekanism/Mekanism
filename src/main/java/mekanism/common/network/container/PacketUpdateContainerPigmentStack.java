package mekanism.common.network.container;

import javax.annotation.Nonnull;
import mekanism.api.chemical.ChemicalUtils;
import mekanism.api.chemical.pigment.PigmentStack;
import mekanism.common.inventory.container.MekanismContainer;
import net.minecraft.network.PacketBuffer;

/**
 * Version of {@link net.minecraft.network.play.server.SWindowPropertyPacket} for pigment stacks
 */
public class PacketUpdateContainerPigmentStack extends PacketUpdateContainer<PacketUpdateContainerPigmentStack> {

    @Nonnull
    private final PigmentStack value;

    public PacketUpdateContainerPigmentStack(short windowId, short property, @Nonnull PigmentStack value) {
        super(windowId, property);
        this.value = value;
    }

    private PacketUpdateContainerPigmentStack(PacketBuffer buffer) {
        super(buffer);
        this.value = ChemicalUtils.readPigmentStack(buffer);
    }

    @Override
    protected void encode(PacketBuffer buffer) {
        super.encode(buffer);
        ChemicalUtils.writeChemicalStack(buffer, value);
    }

    @Override
    protected void handle(MekanismContainer container, PacketUpdateContainerPigmentStack message) {
        container.handleWindowProperty(message.property, message.value);
    }

    public static PacketUpdateContainerPigmentStack decode(PacketBuffer buf) {
        return new PacketUpdateContainerPigmentStack(buf);
    }
}