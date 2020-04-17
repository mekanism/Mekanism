package mekanism.common.multiblock;

import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import mekanism.api.Coord4D;
import mekanism.api.inventory.IInventorySlot;
import mekanism.api.inventory.IMekanismInventory;
import mekanism.common.util.EnumUtils;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;

public abstract class MultiblockData<T extends MultiblockData<T>> implements IMekanismInventory {

    public Set<Coord4D> locations = new ObjectOpenHashSet<>();
    public Set<Coord4D> internalLocations = new ObjectOpenHashSet<>();

    public int volLength;
    public int volWidth;
    public int volHeight;

    private int volume;

    public UUID inventoryID;

    public boolean didTick;

    public boolean hasRenderer;

    @Nullable//may be null if structure has not been fully sent
    public Coord4D renderLocation;

    public Coord4D minLocation;
    public Coord4D maxLocation;

    public boolean destroyed;

    @Nonnull
    @Override
    public List<IInventorySlot> getInventorySlots(@Nullable Direction side) {
        return Collections.emptyList();
    }

    public Set<Direction> getDirectionsToEmit(Coord4D coord) {
        Set<Direction> directionsToEmit = EnumSet.noneOf(Direction.class);
        for (Direction direction : EnumUtils.DIRECTIONS) {
            if (!locations.contains(coord.offset(direction))) {
                directionsToEmit.add(direction);
            }
        }
        return directionsToEmit;
    }

    @Override
    public void onContentsChanged() {}

    @Override
    public int hashCode() {
        int code = 1;
        code = 31 * code + locations.hashCode();
        code = 31 * code + volLength;
        code = 31 * code + volWidth;
        code = 31 * code + volHeight;
        code = 31 * code + getVolume();
        return code;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null || obj.getClass() != getClass()) {
            return false;
        }
        MultiblockData<T> data = (MultiblockData<T>) obj;
        if (!data.locations.equals(locations)) {
            return false;
        }
        if (data.volLength != volLength || data.volWidth != volWidth || data.volHeight != volHeight) {
            return false;
        }
        return data.getVolume() == getVolume();
    }

    public BlockLocation getBlockLocation(BlockPos pos) {
        if (pos.getX() > minLocation.x && pos.getX() < maxLocation.x &&
            pos.getY() > minLocation.y && pos.getY() < maxLocation.y &&
            pos.getZ() > minLocation.z && pos.getZ() < maxLocation.z) {
            return BlockLocation.INSIDE;
        } else if (pos.getX() < minLocation.x || pos.getX() > maxLocation.x ||
                   pos.getY() < minLocation.y || pos.getY() > maxLocation.y ||
                   pos.getZ() < minLocation.z || pos.getZ() > maxLocation.z) {
            return BlockLocation.OUTSIDE;
        }
        return BlockLocation.WALLS;
    }

    public int getVolume() {
        return volume;
    }

    public void setVolume(int volume) {
        this.volume = volume;
    }

    public void onCreated() {}

    public enum BlockLocation {
        INSIDE,
        OUTSIDE,
        WALLS;
    }
}