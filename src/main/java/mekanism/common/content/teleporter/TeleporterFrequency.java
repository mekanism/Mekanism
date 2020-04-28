package mekanism.common.content.teleporter;

import java.util.UUID;
import mekanism.api.Coord4D;
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

    public Coord4D getClosestCoords(Coord4D coord) {
        Coord4D closest = null;
        for (Coord4D iterCoord : activeCoords) {
            if (iterCoord.equals(coord)) {
                continue;
            }
            if (closest == null) {
                closest = iterCoord;
                continue;
            }

            if (coord.dimension != closest.dimension && coord.dimension == iterCoord.dimension) {
                closest = iterCoord;
            } else if (coord.dimension != closest.dimension || coord.dimension == iterCoord.dimension) {
                if (coord.distanceTo(closest) > coord.distanceTo(iterCoord)) {
                    closest = iterCoord;
                }
            }
        }
        return closest;
    }
}
