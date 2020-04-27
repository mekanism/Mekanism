package mekanism.common.content.qio;

import mekanism.common.frequency.FrequencyType;
import mekanism.common.registries.MekanismBlocks;
import mekanism.common.tile.base.TileEntityMekanism;

public class TileEntityQIODriveArray extends TileEntityMekanism implements IQIODriveHolder {

    private QIOFrequency frequency;

    public TileEntityQIODriveArray() {
        super(MekanismBlocks.QIO_DRIVE_ARRAY);
        frequencyComponent.track(FrequencyType.QIO, true, true, true);
    }

    @Override
    public void onUpdateServer() {
        super.onUpdateServer();
    }
}
