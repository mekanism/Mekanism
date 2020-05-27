package mekanism.common.lib.multiblock;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.function.BooleanSupplier;
import java.util.function.Supplier;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import mekanism.api.Action;
import mekanism.api.NBTConstants;
import mekanism.api.chemical.gas.IGasTank;
import mekanism.api.chemical.gas.IMekanismGasHandler;
import mekanism.api.chemical.infuse.IInfusionTank;
import mekanism.api.chemical.infuse.IMekanismInfusionHandler;
import mekanism.api.chemical.pigment.IMekanismPigmentHandler;
import mekanism.api.chemical.pigment.IPigmentTank;
import mekanism.api.chemical.slurry.IMekanismSlurryHandler;
import mekanism.api.chemical.slurry.ISlurryTank;
import mekanism.api.energy.IEnergyContainer;
import mekanism.api.energy.IMekanismStrictEnergyHandler;
import mekanism.api.fluid.IExtendedFluidTank;
import mekanism.api.fluid.IMekanismFluidHandler;
import mekanism.api.heat.IHeatCapacitor;
import mekanism.api.inventory.IInventorySlot;
import mekanism.api.inventory.IMekanismInventory;
import mekanism.common.capabilities.heat.ITileHeatHandler;
import mekanism.common.inventory.container.sync.dynamic.ContainerSync;
import mekanism.common.lib.math.voxel.IShape;
import mekanism.common.lib.math.voxel.VoxelCuboid;
import mekanism.common.lib.multiblock.IValveHandler.ValveData;
import mekanism.common.lib.multiblock.MultiblockCache.CacheSubstance;
import mekanism.common.tile.prefab.TileEntityInternalMultiblock;
import mekanism.common.tile.prefab.TileEntityMultiblock;
import mekanism.common.util.EnumUtils;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.NBTUtils;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.NBTUtil;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class MultiblockData implements IMekanismInventory, IMekanismFluidHandler, IMekanismGasHandler, IMekanismInfusionHandler, IMekanismPigmentHandler,
      IMekanismSlurryHandler, IMekanismStrictEnergyHandler, ITileHeatHandler {

    public Set<BlockPos> locations = new ObjectOpenHashSet<>();
    public Set<BlockPos> internalLocations = new ObjectOpenHashSet<>();
    public Set<ValveData> valves = new ObjectOpenHashSet<>();

    @ContainerSync(getter = "getVolume", setter = "setVolume")
    private int volume;

    public UUID inventoryID;

    public boolean hasMaster;

    @Nullable//may be null if structure has not been fully sent
    public BlockPos renderLocation;

    private VoxelCuboid bounds = new VoxelCuboid(0, 0, 0);

    @ContainerSync
    private boolean formed;

    private int currentRedstoneLevel;

    private final BooleanSupplier remoteSupplier;
    private final Supplier<World> worldSupplier;

    protected final List<IInventorySlot> inventorySlots = new ArrayList<>();
    protected final List<IExtendedFluidTank> fluidTanks = new ArrayList<>();
    protected final List<IGasTank> gasTanks = new ArrayList<>();
    protected final List<IInfusionTank> infusionTanks = new ArrayList<>();
    protected final List<IPigmentTank> pigmentTanks = new ArrayList<>();
    protected final List<ISlurryTank> slurryTanks = new ArrayList<>();
    protected final List<IEnergyContainer> energyContainers = new ArrayList<>();
    protected final List<IHeatCapacitor> heatCapacitors = new ArrayList<>();

    public MultiblockData(TileEntity tile) {
        remoteSupplier = () -> tile.getWorld().isRemote();
        worldSupplier = tile::getWorld;
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

    public boolean setShape(IShape shape) {
        if (shape instanceof VoxelCuboid) {
            VoxelCuboid cuboid = (VoxelCuboid) shape;
            bounds = cuboid;
            renderLocation = cuboid.getMinPos().offset(Direction.UP);
            setVolume(bounds.length() * bounds.width() * bounds.height());
            return true;
        }
        return false;
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
        if (shouldCap(CacheSubstance.INFUSION)) {
            for (IInfusionTank tank : getInfusionTanks(null)) {
                tank.setStackSize(Math.min(tank.getStored(), tank.getCapacity()), Action.EXECUTE);
            }
        }
        if (shouldCap(CacheSubstance.PIGMENT)) {
            for (IPigmentTank tank : getPigmentTanks(null)) {
                tank.setStackSize(Math.min(tank.getStored(), tank.getCapacity()), Action.EXECUTE);
            }
        }
        if (shouldCap(CacheSubstance.SLURRY)) {
            for (ISlurryTank tank : getSlurryTanks(null)) {
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

    protected boolean isRemote() {
        return remoteSupplier.getAsBoolean();
    }

    protected World getWorld() {
        return worldSupplier.get();
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

    public void readUpdateTag(CompoundNBT tag) {
        NBTUtils.setIntIfPresent(tag, NBTConstants.VOLUME, value -> setVolume(value));
        NBTUtils.setBlockPosIfPresent(tag, NBTConstants.RENDER_LOCATION, value -> renderLocation = value);
        bounds = new VoxelCuboid(NBTUtil.readBlockPos(tag.getCompound(NBTConstants.MIN)),
                                 NBTUtil.readBlockPos(tag.getCompound(NBTConstants.MAX)));
        if (tag.hasUniqueId(NBTConstants.INVENTORY_ID)) {
            inventoryID = tag.getUniqueId(NBTConstants.INVENTORY_ID);
        } else {
            inventoryID = null;
        }
    }

    public void writeUpdateTag(CompoundNBT tag) {
        tag.putInt(NBTConstants.VOLUME, getVolume());
        tag.put(NBTConstants.RENDER_LOCATION, NBTUtil.writeBlockPos(renderLocation));
        tag.put(NBTConstants.MIN, NBTUtil.writeBlockPos(bounds.getMinPos()));
        tag.put(NBTConstants.MAX, NBTUtil.writeBlockPos(bounds.getMaxPos()));
        if (inventoryID != null) {
            tag.putUniqueId(NBTConstants.INVENTORY_ID, inventoryID);
        }
    }

    public int length() {
        return bounds.length();
    }

    public int width() {
        return bounds.width();
    }

    public int height() {
        return bounds.height();
    }

    public BlockPos getMinPos() {
        return bounds.getMinPos();
    }

    public BlockPos getMaxPos() {
        return bounds.getMaxPos();
    }

    public VoxelCuboid getBounds() {
        return bounds;
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
    public List<IInfusionTank> getInfusionTanks(@Nullable Direction side) {
        return isFormed() ? infusionTanks : Collections.emptyList();
    }

    @Nonnull
    @Override
    public List<IPigmentTank> getPigmentTanks(@Nullable Direction side) {
        return isFormed() ? pigmentTanks : Collections.emptyList();
    }

    @Nonnull
    @Override
    public List<ISlurryTank> getSlurryTanks(@Nullable Direction side) {
        return isFormed() ? slurryTanks : Collections.emptyList();
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

    public Collection<ValveData> getValveData() {
        return valves;
    }

    @Override
    public void onContentsChanged() {
    }

    @Override
    public int hashCode() {
        int code = 1;
        code = 31 * code + locations.hashCode();
        code = 31 * code + bounds.hashCode();
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
        if (!data.bounds.equals(bounds)) {
            return false;
        }
        return data.getVolume() == getVolume();
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
}