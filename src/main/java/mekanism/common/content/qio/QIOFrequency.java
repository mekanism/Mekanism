package mekanism.common.content.qio;

import java.util.UUID;
import mekanism.common.frequency.Frequency;
import mekanism.common.frequency.FrequencyType;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.PacketBuffer;

public class QIOFrequency extends Frequency {

    public static final String QIO = "qio";

    public QIOFrequency(String n, UUID uuid) {
        super(FrequencyType.INVENTORY, n, uuid);
    }

    public QIOFrequency(CompoundNBT nbtTags, boolean fromUpdate) {
        super(FrequencyType.INVENTORY, nbtTags, fromUpdate);
    }

    public QIOFrequency(PacketBuffer dataStream) {
        super(FrequencyType.INVENTORY, dataStream);
    }

    public static class QIOItemTypeData {

    }

    public static class QIOHolderItemData {
        /** The slot ID where the drive referencing this data is contained. */
        private int driveSlot;
    }
}
