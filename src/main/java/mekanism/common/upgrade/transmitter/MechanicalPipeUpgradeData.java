package mekanism.common.upgrade.transmitter;

import mekanism.api.container.LargeResourceStack;
import mekanism.common.lib.transmitter.ConnectionType;
import net.neoforged.neoforge.transfer.fluid.FluidResource;

public class MechanicalPipeUpgradeData extends TransmitterUpgradeData {

    public final LargeResourceStack<FluidResource> contents;

    public MechanicalPipeUpgradeData(boolean redstoneReactive, ConnectionType[] connectionTypes, LargeResourceStack<FluidResource> contents) {
        super(redstoneReactive, connectionTypes);
        this.contents = contents;
    }
}