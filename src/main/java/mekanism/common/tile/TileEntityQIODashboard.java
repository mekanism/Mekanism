package mekanism.common.tile;

import mekanism.common.content.qio.IQIOFrequencyHolder;
import mekanism.common.frequency.FrequencyType;
import mekanism.common.registries.MekanismBlocks;
import mekanism.common.tile.base.TileEntityMekanism;

public class TileEntityQIODashboard extends TileEntityMekanism implements IQIOFrequencyHolder {

    public TileEntityQIODashboard() {
        super(MekanismBlocks.QIO_DASHBOARD);
        frequencyComponent.track(FrequencyType.QIO, true, true, true);
    }
}