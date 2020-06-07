package mekanism.common.upgrade.transmitter;

import mekanism.api.energy.IEnergyContainer;
import mekanism.common.lib.transmitter.ConnectionType;

public class UniversalCableUpgradeData extends TransmitterUpgradeData {

    public final IEnergyContainer buffer;

    public UniversalCableUpgradeData(boolean redstoneReactive, ConnectionType[] connectionTypes, IEnergyContainer buffer) {
        super(redstoneReactive, connectionTypes);
        this.buffer = buffer;
    }
}