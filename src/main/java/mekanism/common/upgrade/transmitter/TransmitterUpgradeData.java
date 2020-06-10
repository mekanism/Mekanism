package mekanism.common.upgrade.transmitter;

import mekanism.common.lib.transmitter.ConnectionType;
import mekanism.common.upgrade.IUpgradeData;

public class TransmitterUpgradeData implements IUpgradeData {

    public final boolean redstoneReactive;
    public final ConnectionType[] connectionTypes;

    public TransmitterUpgradeData(boolean redstoneReactive, ConnectionType[] connectionTypes) {
        this.redstoneReactive = redstoneReactive;
        this.connectionTypes = connectionTypes;
    }
}