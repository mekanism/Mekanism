package mekanism.common.upgrade.transmitter;

import mekanism.api.math.FloatingLong;
import mekanism.common.tile.transmitter.TileEntitySidedPipe.ConnectionType;

public class ThermodynamicConductorUpgradeData extends TransmitterUpgradeData {

    public final FloatingLong heat;

    public ThermodynamicConductorUpgradeData(boolean redstoneReactive, ConnectionType[] connectionTypes, FloatingLong heat) {
        super(redstoneReactive, connectionTypes);
        this.heat = heat;
    }
}