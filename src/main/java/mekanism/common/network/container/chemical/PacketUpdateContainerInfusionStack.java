package mekanism.common.network.container.chemical;

import javax.annotation.Nonnull;
import mekanism.api.chemical.ChemicalUtils;
import mekanism.api.chemical.infuse.InfuseType;
import mekanism.api.chemical.infuse.InfusionStack;
import net.minecraft.network.PacketBuffer;

/**
 * Version of {@link net.minecraft.network.play.server.SWindowPropertyPacket} for infusion stacks
 */
public class PacketUpdateContainerInfusionStack extends PacketUpdateContainerChemicalStack<InfuseType, InfusionStack> {

    public PacketUpdateContainerInfusionStack(short windowId, short property, @Nonnull InfusionStack value) {
        super(windowId, property, value);
    }

    private PacketUpdateContainerInfusionStack(PacketBuffer buffer) {
        super(buffer);
    }

    @Override
    protected InfusionStack readStack(PacketBuffer buffer) {
        return ChemicalUtils.readInfusionStack(buffer);
    }

    public static PacketUpdateContainerInfusionStack decode(PacketBuffer buf) {
        return new PacketUpdateContainerInfusionStack(buf);
    }
}