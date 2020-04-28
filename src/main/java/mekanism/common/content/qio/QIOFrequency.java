package mekanism.common.content.qio;

import java.util.UUID;
import mekanism.common.frequency.Frequency;
import mekanism.common.frequency.FrequencyType;
import net.minecraft.tileentity.TileEntity;

public class QIOFrequency extends Frequency {

    public QIOFrequency(String n, UUID uuid) {
        super(FrequencyType.QIO, n, uuid);
    }

    public QIOFrequency() {
        super(FrequencyType.QIO);
    }

    public static class QIOItemTypeData {

    }

    public static class QIOHolderItemData {
        /** The slot ID where the drive referencing this data is contained. */
        private int driveSlot;
    }

    @Override
    public void onDeactivate(TileEntity tile) {
        super.onDeactivate(tile);
    }
}
