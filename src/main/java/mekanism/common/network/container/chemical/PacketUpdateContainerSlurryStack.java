package mekanism.common.network.container.chemical;

import javax.annotation.Nonnull;
import mekanism.api.chemical.ChemicalUtils;
import mekanism.api.chemical.slurry.Slurry;
import mekanism.api.chemical.slurry.SlurryStack;
import net.minecraft.network.PacketBuffer;

/**
 * Version of {@link net.minecraft.network.play.server.SWindowPropertyPacket} for slurry stacks
 */
public class PacketUpdateContainerSlurryStack extends PacketUpdateContainerChemicalStack<Slurry, SlurryStack> {

    public PacketUpdateContainerSlurryStack(short windowId, short property, @Nonnull SlurryStack value) {
        super(windowId, property, value);
    }

    private PacketUpdateContainerSlurryStack(PacketBuffer buffer) {
        super(buffer);
    }

    @Override
    protected SlurryStack readStack(PacketBuffer buffer) {
        return ChemicalUtils.readSlurryStack(buffer);
    }

    public static PacketUpdateContainerSlurryStack decode(PacketBuffer buf) {
        return new PacketUpdateContainerSlurryStack(buf);
    }
}