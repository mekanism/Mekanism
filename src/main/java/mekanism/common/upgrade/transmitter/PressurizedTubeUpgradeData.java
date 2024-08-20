package mekanism.common.upgrade.transmitter;

import mekanism.api.chemical.ChemicalStack;
import mekanism.common.lib.transmitter.ConnectionType;

public class PressurizedTubeUpgradeData extends TransmitterUpgradeData {

    public final ChemicalStack contents;

    public PressurizedTubeUpgradeData(boolean redstoneReactive, ConnectionType[] connectionTypes, ChemicalStack contents) {
        super(redstoneReactive, connectionTypes);
        this.contents = contents;
    }
}