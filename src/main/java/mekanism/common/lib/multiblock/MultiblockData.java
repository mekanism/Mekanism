package mekanism.common.lib.multiblock;

import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.function.Supplier;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import mekanism.api.Action;
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
import mekanism.common.lib.multiblock.IValveHandler.ValveData;
import mekanism.common.lib.multiblock.MultiblockCache.CacheSubstance;
import mekanism.common.tile.prefab.TileEntityInternalMultiblock;
import mekanism.common.tile.prefab.TileEntityMultiblock;
import mekanism.common.util.EnumUtils;
import mekanism.common.util.MekanismUtils;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class MultiblockData implements IMekanismInventory, IMekanismFluidHandler, IMekanismGasHandler,
      IMekanismStrictEnergyHandler, ITileHeatHandler {

    public Set<BlockPos> locations = new ObjectOpenHashSet<>();
    public Set<BlockPos> internalLocations = new ObjectOpenHashSet<>();
    public Set<ValveData> valves = new ObjectOpenHashSet<>();

    @ContainerSync(tag = "stats")
    public int length, width, height;

    @ContainerSync(getter = "getVolume", setter = "setVolume")
    private int volume;

    public UUID inventoryID;

    public boolean didUpdateThisTick;

    public boolean hasRenderer;

    @Nullable//may be null if structure has not been fully sent
    public BlockPos renderLocation;

    public BlockPos minLocation, maxLocation;

    @ContainerSync
    private boolean formed;

    private int currentRedstoneLevel;

    private final Supplier<World> worldSupplier;

    protected final List<IInventorySlot> inventorySlots = new ArrayList<>();
    protected final List<IExtendedFluidTank> fluidTanks = new ArrayList<>();
    protected final List<IGasTank> gasTanks = new ArrayList<>();
    protected final List<IEnergyContainer> energyContainers = new ArrayList<>();
    protected final List<IHeatCapacitor> heatCapacitors = new ArrayList<>();

    public MultiblockData(TileEntity tile) {
        worldSupplier = () -> tile.getWorld();
    }

    /**
     * Tick the multiblock.
     *
     * @return if we need an update packet
     */
    public boolean tick(World world) {
        boolean ret = false;
        for (ValveData data : valves) {
            data.activeTicks = Math.max(0, data.activeTicks - 1);
            if (data.activeTicks > 0 != data.prevActive) {
                ret = true;
            }
            data.prevActive = data.activeTicks > 0;
        }
        return ret;
    }

    public boolean buildStructure(BlockPos min, BlockPos max) {
        minLocation = min;
        maxLocation = max;
        renderLocation = min.offset(Direction.UP);
        length = Math.abs(max.getX() - min.getX()) + 1;
        height = Math.abs(max.getY() - min.getY()) + 1;
        width = Math.abs(max.getZ() - min.getZ()) + 1;
        setVolume(length * width * height);
        return length >= 3 && length <= UpdateProtocol.MAX_SIZE && height >= 3 && height <= UpdateProtocol.MAX_SIZE && width >= 3 && width <= UpdateProtocol.MAX_SIZE;
    }

    public void onCreated(World world) {
        for (BlockPos pos : internalLocations) {
            TileEntityInternalMultiblock tile = MekanismUtils.getTileEntity(TileEntityInternalMultiblock.class, world, pos);
            if (tile != null) {
                tile.setMultiblock(inventoryID);
            }
        }

        if (shouldCap(CacheSubstance.FLUID)) {
            for (IExtendedFluidTank tank : getFluidTanks(null)) {
                tank.setStackSize(Math.min(tank.getFluidAmount(), tank.getCapacity()), Action.EXECUTE);
            }
        }
        if (shouldCap(CacheSubstance.GAS)) {
            for (IGasTank tank : getGasTanks(null)) {
                tank.setStackSize(Math.min(tank.getStored(), tank.getCapacity()), Action.EXECUTE);
            }
        }
        if (shouldCap(CacheSubstance.ENERGY)) {
            for (IEnergyContainer container : getEnergyContainers(null)) {
                container.setEnergy(container.getEnergy().min(container.getMaxEnergy()));
            }
        }

        forceUpdateComparatorLevel();
    }

    protected boolean shouldCap(CacheSubstance type) {
        return true;
    }

    public void remove(World world) {
        for (BlockPos pos : internalLocations) {
            TileEntityInternalMultiblock tile = MekanismUtils.getTileEntity(TileEntityInternalMultiblock.class, world, pos);
            if (tile != null) {
                tile.setMultiblock(null);
            }
        }
        inventoryID = null;
        formed = false;
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

    public Set<Direction> getDirectionsToEmit(BlockPos pos) {
        Set<Direction> directionsToEmit = EnumSet.noneOf(Direction.class);
        for (Direction direction : EnumUtils.DIRECTIONS) {
            if (!locations.contains(pos.offset(direction))) {
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
        code = 31 * code + length;
        code = 31 * code + width;
        code = 31 * code + height;
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
        if (data.length != length || data.width != width || data.height != height) {
            return false;
        }
        return data.getVolume() == getVolume();
    }

    public BlockLocation getBlockLocation(BlockPos pos) {
        if (pos.getX() > minLocation.getX() && pos.getX() < maxLocation.getX() &&
            pos.getY() > minLocation.getY() && pos.getY() < maxLocation.getY() &&
            pos.getZ() > minLocation.getZ() && pos.getZ() < maxLocation.getZ()) {
            return BlockLocation.INSIDE;
        } else if (pos.getX() < minLocation.getX() || pos.getX() > maxLocation.getX() ||
                   pos.getY() < minLocation.getY() || pos.getY() > maxLocation.getY() ||
                   pos.getZ() < minLocation.getZ() || pos.getZ() > maxLocation.getZ()) {
            return BlockLocation.OUTSIDE;
        }
        return BlockLocation.WALLS;
    }

    public World getWorld() {
        return worldSupplier.get();
    }

    public boolean isRemote() {
        return getWorld().isRemote();
    }

    public boolean isFormed() {
        return formed;
    }

    public void setFormedForce(boolean formed) {
        this.formed = formed;
    }

    public int getVolume() {
        return volume;
    }

    public void setVolume(int volume) {
        this.volume = volume;
    }

    // Only call from the server
    public void markDirtyComparator(World world) {
        if (!isFormed()) {
            return;
        }
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
            TileEntityMultiblock<?> tile = MekanismUtils.getTileEntity(TileEntityMultiblock.class, world, valve.location);
            if (tile != null) {
                tile.markDirtyComparator();
            }
        }
    }

    public void forceUpdateComparatorLevel() {
        currentRedstoneLevel = getMultiblockRedstoneLevel();
    }

    protected int getMultiblockRedstoneLevel() {
        return 0;
    }

    public int getCurrentRedstoneLevel() {
        return currentRedstoneLevel;
    }

    public enum BlockLocation {
        INSIDE,
        OUTSIDE,
        WALLS;
    }
}