package mekanism.common.upgrade.transmitter;

import mekanism.api.chemical.gas.GasStack;
import mekanism.common.tile.transmitter.TileEntitySidedPipe.ConnectionType;

public class PressurizedTubeUpgradeData extends TransmitterUpgradeData {

    public final GasStack contents;

    public PressurizedTubeUpgradeData(boolean redstoneReactive, ConnectionType[] connectionTypes, GasStack contents) {
        super(redstoneReactive, connectionTypes);
        this.contents = contents;
    }
}