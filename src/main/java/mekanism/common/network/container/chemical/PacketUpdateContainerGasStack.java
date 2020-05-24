package mekanism.common.network.container.chemical;

import javax.annotation.Nonnull;
import mekanism.api.chemical.ChemicalUtils;
import mekanism.api.chemical.gas.Gas;
import mekanism.api.chemical.gas.GasStack;
import net.minecraft.network.PacketBuffer;

/**
 * Version of {@link net.minecraft.network.play.server.SWindowPropertyPacket} for chemical stacks
 */
public class PacketUpdateContainerGasStack extends PacketUpdateContainerChemicalStack<Gas, GasStack> {

    public PacketUpdateContainerGasStack(short windowId, short property, @Nonnull GasStack value) {
        super(windowId, property, value);
    }

    private PacketUpdateContainerGasStack(PacketBuffer buffer) {
        super(buffer);
    }

    @Override
    protected GasStack readStack(PacketBuffer buffer) {
        return ChemicalUtils.readGasStack(buffer);
    }

    public static PacketUpdateContainerGasStack decode(PacketBuffer buf) {
        return new PacketUpdateContainerGasStack(buf);
    }
}