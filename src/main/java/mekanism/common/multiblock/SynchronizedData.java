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
import mekanism.common.multiblock.IValveHandler.ValveData;
import mekanism.common.tile.TileEntityMultiblock;
import mekanism.common.util.EnumUtils;
import mekanism.common.util.MekanismUtils;
import net.minecraft.util.Direction;
import net.minecraft.world.World;

public abstract class SynchronizedData<T extends SynchronizedData<T>> implements IMekanismInventory {

    public Set<Coord4D> locations = new ObjectOpenHashSet<>();
    public Set<ValveData> valves = new ObjectOpenHashSet<>();
    public Set<Coord4D> internalLocations = new ObjectOpenHashSet<>();

    public int volLength, volWidth, volHeight;
    private int volume;

    public UUID inventoryID;

    public boolean didTick;

    public boolean hasRenderer;

    @Nullable//may be null if structure has not been fully sent
    public Coord4D renderLocation;

    public Coord4D minLocation, maxLocation;

    public boolean destroyed;

    private int currentRedstoneLevel;

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
    public void onContentsChanged() {
    }

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
        SynchronizedData<T> data = (SynchronizedData<T>) obj;
        if (!data.locations.equals(locations)) {
            return false;
        }
        if (data.volLength != volLength || data.volWidth != volWidth || data.volHeight != volHeight) {
            return false;
        }
        return data.getVolume() == getVolume();
    }

    public int getVolume() {
        return volume;
    }

    public void setVolume(int volume) {
        this.volume = volume;
    }

    public void onCreated() {
        forceUpdateComparatorLevel();
    }

    // Only call from the server
    public void markDirtyComparator(World world) {
        int newRedstoneLevel = getMultiblockRedstoneLevel();
        if (newRedstoneLevel != currentRedstoneLevel) {
            //Update the comparator value if it changed
            currentRedstoneLevel = newRedstoneLevel;
            //And inform all the valves that the level they should be supplying changed
            notifyAllUpdateComparator(world);
        }
    }

    public void notifyAllUpdateComparator(World world) {
        for (ValveData valve : valves) {
            TileEntityMultiblock<?> tile = MekanismUtils.getTileEntity(TileEntityMultiblock.class, world, valve.location.getPos());
            if (tile != null) {
                tile.markDirtyComparator();
            }
        }
    }

    public void forceUpdateComparatorLevel() {
        currentRedstoneLevel = getMultiblockRedstoneLevel();
    }

    protected abstract int getMultiblockRedstoneLevel();

    public int getCurrentRedstoneLevel() {
        return currentRedstoneLevel;
    }
}