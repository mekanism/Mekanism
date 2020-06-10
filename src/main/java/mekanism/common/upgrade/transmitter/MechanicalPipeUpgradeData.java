package mekanism.common.upgrade.transmitter;

import mekanism.common.lib.transmitter.ConnectionType;
import net.minecraftforge.fluids.FluidStack;

public class MechanicalPipeUpgradeData extends TransmitterUpgradeData {

    public final FluidStack contents;

    public MechanicalPipeUpgradeData(boolean redstoneReactive, ConnectionType[] connectionTypes, FluidStack contents) {
        super(redstoneReactive, connectionTypes);
        this.contents = contents;
    }
}