package mekanism.common.multiblock;

import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import mekanism.api.Coord4D;
import mekanism.api.chemical.gas.IGasTank;
import mekanism.api.chemical.gas.IMekanismGasHandler;
import mekanism.api.energy.IEnergyContainer;
import mekanism.api.energy.IMekanismStrictEnergyHandler;
import mekanism.api.fluid.IExtendedFluidTank;
import mekanism.api.fluid.IMekanismFluidHandler;
import mekanism.api.heat.IHeatCapacitor;
import mekanism.api.inventory.IInventorySlot;
import mekanism.api.inventory.IMekanismInventory;
import mekanism.common.capabilities.heat.ITileHeatHandler;
import mekanism.common.inventory.container.sync.dynamic.ContainerSync;
import mekanism.common.multiblock.IValveHandler.ValveData;
import mekanism.common.tile.prefab.TileEntityMultiblock;
import mekanism.common.util.EnumUtils;
import mekanism.common.util.MekanismUtils;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public abstract class MultiblockData implements IMekanismInventory, IMekanismFluidHandler, IMekanismGasHandler,
      IMekanismStrictEnergyHandler, ITileHeatHandler {

    public Set<Coord4D> locations = new ObjectOpenHashSet<>();
    public Set<Coord4D> internalLocations = new ObjectOpenHashSet<>();
    public Set<ValveData> valves = new ObjectOpenHashSet<>();

    @ContainerSync(tag = "stats")
    public int volLength, volWidth, volHeight;

    @ContainerSync(getter = "getVolume", setter = "setVolume")
    private int volume;

    public UUID inventoryID;

    public boolean didUpdateThisTick;

    public boolean hasRenderer;

    @Nullable//may be null if structure has not been fully sent
    public Coord4D renderLocation;

    public Coord4D minLocation, maxLocation;

    public boolean destroyed;
    @ContainerSync
    private boolean formed;

    private int currentRedstoneLevel;

    protected final List<IInventorySlot> inventorySlots = new ArrayList<>();
    protected final List<IExtendedFluidTank> fluidTanks = new ArrayList<>();
    protected final List<IGasTank> gasTanks = new ArrayList<>();
    protected final List<IEnergyContainer> energyContainers = new ArrayList<>();
    protected final List<IHeatCapacitor> heatCapacitors = new ArrayList<>();

    /**
     * Tick the multiblock.
     * @return if we need an update packet
     */
    public boolean tick(World world) {
        for (ValveData data : valves) {
            if (data.activeTicks > 0) {
                data.activeTicks--;
            }
            if (data.activeTicks > 0 != data.prevActive) {
                return true;
            }
            data.prevActive = data.activeTicks > 0;
        }
        return false;
    }

    @Nonnull
    @Override
    public List<IInventorySlot> getInventorySlots(@Nullable Direction side) {
        return isFormed() ? inventorySlots : Collections.emptyList();
    }

    @Nonnull
    @Override
    public List<IExtendedFluidTank> getFluidTanks(@Nullable Direction side) {
        return isFormed() ? fluidTanks : Collections.emptyList();
    }

    @Nonnull
    @Override
    public List<IGasTank> getGasTanks(@Nullable Direction side) {
        return isFormed() ? gasTanks : Collections.emptyList();
    }

    @Nonnull
    @Override
    public List<IEnergyContainer> getEnergyContainers(@Nullable Direction side) {
        return isFormed() ? energyContainers : Collections.emptyList();
    }

    @Nonnull
    @Override
    public List<IHeatCapacitor> getHeatCapacitors(Direction side) {
        return isFormed() ? heatCapacitors : Collections.emptyList();
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
        MultiblockData data = (MultiblockData) obj;
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

    public boolean isFormed() {
        return formed;
    }

    public void setFormed(boolean formed) {
        this.formed = formed;
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
        if (!isFormed()) return;
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

    public enum BlockLocation {
        INSIDE,
        OUTSIDE,
        WALLS;
    }
}