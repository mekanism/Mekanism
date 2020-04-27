package mekanism.common.content.teleporter;

import java.util.UUID;
import mekanism.common.frequency.Frequency;
import mekanism.common.frequency.FrequencyType;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.PacketBuffer;

public class TeleporterFrequency extends Frequency {

    public TeleporterFrequency(String n, UUID uuid) {
        super(FrequencyType.TELEPORTER, n, uuid);
    }

    public TeleporterFrequency(CompoundNBT nbtTags, boolean fromUpdate) {
        super(FrequencyType.TELEPORTER, nbtTags, fromUpdate);
    }

    public TeleporterFrequency(PacketBuffer dataStream) {
        super(FrequencyType.TELEPORTER, dataStream);
    }
}
