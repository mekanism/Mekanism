package mekanism.common.upgrade.transmitter;

import mekanism.common.tile.transmitter.TileEntitySidedPipe.ConnectionType;

public class ThermodynamicConductorUpgradeData extends TransmitterUpgradeData {

    public final double temperature;

    public ThermodynamicConductorUpgradeData(boolean redstoneReactive, ConnectionType[] connectionTypes, double temperature) {
        super(redstoneReactive, connectionTypes);
        this.temperature = temperature;
    }
}