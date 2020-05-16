package mekanism.common.content.teleporter;

import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import java.util.Set;
import java.util.UUID;
import mekanism.api.Coord4D;
import mekanism.common.lib.frequency.Frequency;
import mekanism.common.lib.frequency.FrequencyType;
import net.minecraft.tileentity.TileEntity;

public class TeleporterFrequency extends Frequency {

    private Set<Coord4D> activeCoords = new ObjectOpenHashSet<>();

    public TeleporterFrequency(String n, UUID uuid) {
        super(FrequencyType.TELEPORTER, n, uuid);
    }

    public TeleporterFrequency() {
        super(FrequencyType.TELEPORTER);
    }

    public Set<Coord4D> getActiveCoords() {
        return activeCoords;
    }

    @Override
    public void update(TileEntity tile) {
        super.update(tile);
        activeCoords.add(Coord4D.get(tile));
    }

    @Override
    public void onDeactivate(TileEntity tile) {
        super.onDeactivate(tile);
        activeCoords.remove(Coord4D.get(tile));
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
