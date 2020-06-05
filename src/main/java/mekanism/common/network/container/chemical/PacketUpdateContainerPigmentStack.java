package mekanism.common.network.container.chemical;

import javax.annotation.Nonnull;
import mekanism.api.chemical.ChemicalUtils;
import mekanism.api.chemical.pigment.PigmentStack;
import net.minecraft.network.PacketBuffer;

/**
 * Version of {@link net.minecraft.network.play.server.SWindowPropertyPacket} for pigment stacks
 */
public class PacketUpdateContainerPigmentStack extends PacketUpdateContainerChemicalStack<PigmentStack> {

    public PacketUpdateContainerPigmentStack(short windowId, short property, @Nonnull PigmentStack value) {
        super(windowId, property, value);
    }

    private PacketUpdateContainerPigmentStack(PacketBuffer buffer) {
        super(buffer);
    }

    @Override
    protected PigmentStack readStack(PacketBuffer buffer) {
        return ChemicalUtils.readPigmentStack(buffer);
    }

    public static PacketUpdateContainerPigmentStack decode(PacketBuffer buf) {
        return new PacketUpdateContainerPigmentStack(buf);
    }
}