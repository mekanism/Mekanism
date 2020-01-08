package mekanism.common.upgrade.transmitter;

import mekanism.common.tile.transmitter.TileEntitySidedPipe.ConnectionType;

public class UniversalCableUpgradeData extends TransmitterUpgradeData {

    public final double buffer;

    public UniversalCableUpgradeData(boolean redstoneReactive, ConnectionType[] connectionTypes, double buffer) {
        super(redstoneReactive, connectionTypes);
        this.buffer = buffer;
    }
}