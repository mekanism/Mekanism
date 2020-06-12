package mekanism.common.upgrade.transmitter;

import mekanism.api.chemical.merged.BoxedChemicalStack;
import mekanism.common.lib.transmitter.ConnectionType;

public class PressurizedTubeUpgradeData extends TransmitterUpgradeData {

    public final BoxedChemicalStack contents;

    public PressurizedTubeUpgradeData(boolean redstoneReactive, ConnectionType[] connectionTypes, BoxedChemicalStack contents) {
        super(redstoneReactive, connectionTypes);
        this.contents = contents;
    }
}