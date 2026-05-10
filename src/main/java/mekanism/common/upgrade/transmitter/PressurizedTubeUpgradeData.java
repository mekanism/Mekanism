package mekanism.common.upgrade.transmitter;

import mekanism.api.chemical.ChemicalResource;
import mekanism.api.container.LargeResourceStack;
import mekanism.common.lib.transmitter.ConnectionType;

public class PressurizedTubeUpgradeData extends TransmitterUpgradeData {

    public final LargeResourceStack<ChemicalResource> contents;

    public PressurizedTubeUpgradeData(boolean redstoneReactive, ConnectionType[] connectionTypes, LargeResourceStack<ChemicalResource> contents) {
        super(redstoneReactive, connectionTypes);
        this.contents = contents;
    }
}