package mekanism.common.lib.multiblock;

import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.function.BiPredicate;
import java.util.function.BooleanSupplier;
import java.util.function.Predicate;
import java.util.function.Supplier;
import mekanism.api.Action;
import mekanism.api.AutomationType;
import mekanism.api.IContentsListener;
import mekanism.api.SerializationConstants;
import mekanism.api.chemical.IChemicalTank;
import mekanism.api.chemical.IMekanismChemicalHandler;
import mekanism.api.energy.IEnergyContainer;
import mekanism.api.energy.IMekanismStrictEnergyHandler;
import mekanism.api.fluid.IExtendedFluidTank;
import mekanism.api.fluid.IMekanismFluidHandler;
import mekanism.api.heat.HeatAPI;
import mekanism.api.heat.IHeatCapacitor;
import mekanism.api.inventory.IInventorySlot;
import mekanism.api.inventory.IMekanismInventory;
import mekanism.common.capabilities.heat.ITileHeatHandler;
import mekanism.common.integration.computer.annotation.ComputerMethod;
import mekanism.common.integration.energy.BlockEnergyCapabilityCache;
import mekanism.common.inventory.container.sync.dynamic.ContainerSync;
import mekanism.common.lib.math.voxel.IShape;
import mekanism.common.lib.math.voxel.VoxelCuboid;
import mekanism.common.lib.math.voxel.VoxelCuboid.CuboidRelative;
import mekanism.common.lib.multiblock.FormationProtocol.StructureRequirement;
import mekanism.common.lib.multiblock.IValveHandler.ValveData;
import mekanism.common.lib.multiblock.MultiblockCache.CacheSubstance;
import mekanism.common.tile.prefab.TileEntityMultiblock;
import mekanism.common.tile.prefab.TileEntityStructuralMultiblock;
import mekanism.common.util.EnumUtils;
import mekanism.common.util.NBTUtils;
import mekanism.common.util.WorldUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.redstone.Redstone;
import net.neoforged.neoforge.capabilities.BlockCapabilityCache;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class MultiblockData implements IMekanismInventory, IMekanismFluidHandler, IMekanismStrictEnergyHandler, ITileHeatHandler, IMekanismChemicalHandler {

    public Set<BlockPos> locations = new ObjectOpenHashSet<>();
    /**
     * @apiNote This set is only used for purposes of caching all known valid inner blocks of a multiblock structure, for use in checking if we need to revalidate the
     * multiblock when something changes, cases we want to skip are inner nodes just changing state (for example, super heating elements being activated) This set is not
     * synced or checked anywhere (for things like equals) as it is only used on the server and isn't part of the structure's information. It also is not the most
     * accurate of checks that get done against this as there is no way to tell if the state actually changed or if the block changed entirely, but assuming no one is
     * replacing the blocks inside a multiblock (which is unsupported) it will handle it fine, and we can easily special-case it becoming air as having been "broken"
     */
    public Set<BlockPos> internalLocations = new ObjectOpenHashSet<>();
    public Set<ValveData> valves = new ObjectOpenHashSet<>();

    @ContainerSync(getter = "getVolume", setter = "setVolume")
    private int volume;

    public UUID inventoryID;

    public boolean hasMaster;

    @Nullable//may be null if structure has not been fully sent
    public BlockPos renderLocation;

    @ContainerSync
    private VoxelCuboid bounds = new VoxelCuboid(0, 0, 0);

    @ContainerSync
    private boolean formed;
    public boolean recheckStructure;

    private int currentRedstoneLevel = Redstone.SIGNAL_NONE;

    private final BooleanSupplier remoteSupplier;
    private final Supplier<Level> worldSupplier;

    protected final List<IInventorySlot> inventorySlots = new ArrayList<>();
    protected final List<IExtendedFluidTank> fluidTanks = new ArrayList<>();
    protected final List<IChemicalTank> chemicalTanks = new ArrayList<>();
    protected final List<IEnergyContainer> energyContainers = new ArrayList<>();
    protected final List<IHeatCapacitor> heatCapacitors = new ArrayList<>();

    private final BiPredicate<Object, @NotNull AutomationType> formedBiPred = (t, automationType) -> isFormed();
    private final BiPredicate<Object, @NotNull AutomationType> notExternalFormedBiPred = (t, automationType) -> automationType != AutomationType.EXTERNAL && isFormed();

    private boolean dirty;

    public MultiblockData(BlockEntity tile) {
        remoteSupplier = () -> tile.getLevel().isClientSide();
        worldSupplier = tile::getLevel;
    }

    @SuppressWarnings("unchecked")
    public <T> BiPredicate<T, @NotNull AutomationType> formedBiPred() {
        return (BiPredicate<T, @NotNull AutomationType>) formedBiPred;
    }

    @SuppressWarnings("unchecked")
    public <T> BiPredicate<T, @NotNull AutomationType> notExternalFormedBiPred() {
        return (BiPredicate<T, @NotNull AutomationType>) notExternalFormedBiPred;
    }

    protected IContentsListener createSaveAndComparator() {
        return createSaveAndComparator(this);
    }

    protected IContentsListener createSaveAndComparator(IContentsListener contentsListener) {
        return () -> {
            contentsListener.onContentsChanged();
            if (!isRemote()) {
                markDirtyComparator(getLevel());
            }
        };
    }

    public boolean isDirty() {
        return dirty;
    }

    public void resetDirty() {
        dirty = false;
    }

    public void markDirty() {
        dirty = true;
    }

    /**
     * Returns true if the multiblock's gui can be accessed via structural multiblocks, false otherwise. An example this may be false for would be on a thermal
     * evaporation plant.
     */
    public boolean allowsStructuralGuiAccess(TileEntityStructuralMultiblock multiblock) {
        return true;
    }

    /**
     * Tick the multiblock.
     *
     * @return if we need an update packet
     */
    public boolean tick(Level world) {
        boolean needsPacket = false;
        for (ValveData data : valves) {
            data.activeTicks = Math.max(0, data.activeTicks - 1);
            if (data.activeTicks > 0 != data.prevActive) {
                needsPacket = true;
            }
            data.prevActive = data.activeTicks > 0;
        }
        return needsPacket;
    }

    protected double calculateAverageAmbientTemperature(Level world) {
        //Take a rough average of the biome temperature by calculating the average of all the corners of the multiblock
        BlockPos min = getMinPos();
        BlockPos max = getMaxPos();
        return HeatAPI.getAmbientTemp(getBiomeTemp(world,
              min,
              new BlockPos(max.getX(), min.getY(), min.getZ()),
              new BlockPos(min.getX(), min.getY(), max.getZ()),
              new BlockPos(max.getX(), min.getY(), max.getZ()),
              new BlockPos(min.getX(), max.getY(), min.getZ()),
              new BlockPos(max.getX(), max.getY(), min.getZ()),
              new BlockPos(min.getX(), max.getY(), max.getZ()),
              max
        ));
    }

    private static double getBiomeTemp(Level world, BlockPos... positions) {
        if (positions.length == 0) {
            throw new IllegalArgumentException("No positions given.");
        }
        double sum = 0;
        for (BlockPos pos : positions) {
            sum += world.getBiome(pos).value().getTemperature(pos);
        }
        return sum / positions.length;
    }

    public boolean setShape(IShape shape) {
        if (shape instanceof VoxelCuboid cuboid) {
            bounds = cuboid;
            renderLocation = cuboid.getMinPos().relative(Direction.UP);
            setVolume(bounds.length() * bounds.width() * bounds.height());
            return true;
        }
        return false;
    }

    public void onCreated(Level world) {
        for (BlockPos pos : internalLocations) {
            BlockEntity tile = WorldUtils.getTileEntity(world, pos);
            if (tile instanceof IInternalMultiblock internalMultiblock) {
                internalMultiblock.setMultiblock(this);
            }
        }
        for (BlockPos pos : locations) {
            BlockEntity tile = WorldUtils.getTileEntity(world, pos);
            if (tile instanceof IStructuralMultiblock structuralMultiblock) {
                structuralMultiblock.multiblockFormed(this);
            }
        }

        if (shouldCap(CacheSubstance.FLUID)) {
            for (IExtendedFluidTank tank : getFluidTanks(null)) {
                tank.setStackSize(Math.min(tank.getFluidAmount(), tank.getCapacity()), Action.EXECUTE);
            }
        }
        if (shouldCap(CacheSubstance.CHEMICAL)) {
            for (IChemicalTank tank : getChemicalTanks(null)) {
                tank.setStackSize(Math.min(tank.getStored(), tank.getCapacity()), Action.EXECUTE);
            }
        }
        if (shouldCap(CacheSubstance.ENERGY)) {
            for (IEnergyContainer container : getEnergyContainers(null)) {
                container.setEnergy(Math.min(container.getEnergy(), container.getMaxEnergy()));
            }
        }
        updateEjectors(world);
        forceUpdateComparatorLevel();
    }

    protected void updateEjectors(Level world) {
    }

    protected boolean isRemote() {
        return remoteSupplier.getAsBoolean();
    }

    public Level getLevel() {
        return worldSupplier.get();
    }

    protected boolean shouldCap(CacheSubstance<?, ?> type) {
        return true;
    }

    public void remove(Level world, Structure oldStructure) {
        for (BlockPos pos : internalLocations) {
            BlockEntity tile = WorldUtils.getTileEntity(world, pos);
            if (tile instanceof IInternalMultiblock internalMultiblock) {
                internalMultiblock.setMultiblock(null);
            }
        }
        for (BlockPos pos : locations) {
            BlockEntity tile = WorldUtils.getTileEntity(world, pos);
            if (tile instanceof IStructuralMultiblock structuralMultiblock) {
                structuralMultiblock.multiblockUnformed(oldStructure);
            }
        }
        inventoryID = null;
        formed = false;
        recheckStructure = false;
    }

    public void meltdownHappened(Level world) {
    }

    public void readUpdateTag(CompoundTag tag, HolderLookup.Provider provider) {
        NBTUtils.setIntIfPresent(tag, SerializationConstants.VOLUME, this::setVolume);
        NBTUtils.setBlockPosIfPresent(tag, SerializationConstants.RENDER_LOCATION, value -> renderLocation = value);
        Optional<BlockPos> minPos = NbtUtils.readBlockPos(tag, SerializationConstants.MIN);
        Optional<BlockPos> maxPos = NbtUtils.readBlockPos(tag, SerializationConstants.MAX);
        if (minPos.isPresent() && maxPos.isPresent()) {
            bounds = new VoxelCuboid(minPos.get(), maxPos.get());
        }
        NBTUtils.setUUIDIfPresentElse(tag, SerializationConstants.INVENTORY_ID, value -> inventoryID = value, () -> inventoryID = null);
    }

    public void writeUpdateTag(CompoundTag tag, HolderLookup.Provider provider) {
        tag.putInt(SerializationConstants.VOLUME, getVolume());
        if (renderLocation != null) {//In theory this shouldn't be null here but check it anyway
            tag.put(SerializationConstants.RENDER_LOCATION, NbtUtils.writeBlockPos(renderLocation));
        }
        tag.put(SerializationConstants.MIN, NbtUtils.writeBlockPos(bounds.getMinPos()));
        tag.put(SerializationConstants.MAX, NbtUtils.writeBlockPos(bounds.getMaxPos()));
        if (inventoryID != null) {
            tag.putUUID(SerializationConstants.INVENTORY_ID, inventoryID);
        }
    }

    @ComputerMethod(nameOverride = "getLength")
    public int length() {
        return bounds.length();
    }

    @ComputerMethod(nameOverride = "getWidth")
    public int width() {
        return bounds.width();
    }

    @ComputerMethod(nameOverride = "getHeight")
    public int height() {
        return bounds.height();
    }

    @ComputerMethod
    public BlockPos getMinPos() {
        return bounds.getMinPos();
    }

    @ComputerMethod
    public BlockPos getMaxPos() {
        return bounds.getMaxPos();
    }

    public VoxelCuboid getBounds() {
        return bounds;
    }

    /**
     * Checks if this multiblock is formed and the given position is insides the bounds of this multiblock
     */
    public <T extends MultiblockData> boolean isPositionInsideBounds(@NotNull Structure structure, @NotNull BlockPos pos) {
        if (isFormed()) {
            CuboidRelative relativeLocation = getBounds().getRelativeLocation(pos);
            if (relativeLocation == CuboidRelative.INSIDE) {
                return true;
            } else if (relativeLocation.isWall()) {
                //If we are in the wall check if we are really an inner position. For example evap towers
                MultiblockManager<T> manager = (MultiblockManager<T>) structure.getManager();
                if (manager != null) {
                    IStructureValidator<T> validator = manager.createValidator();
                    if (validator instanceof CuboidStructureValidator<T> cuboidValidator) {
                        validator.init(getLevel(), manager, structure);
                        cuboidValidator.loadCuboid(getBounds());
                        return cuboidValidator.getStructureRequirement(pos) == StructureRequirement.INNER;
                    }
                }
            }
        }
        return false;
    }

    /**
     * Checks if this multiblock is formed and the given position is insides the bounds of this multiblock
     */
    public boolean isPositionOutsideBounds(@NotNull BlockPos pos) {
        return isFormed() && getBounds().getRelativeLocation(pos) == CuboidRelative.OUTSIDE;
    }

    @Nullable
    public Direction getOutsideSide(@NotNull BlockPos pos) {
        if (isFormed()) {
            VoxelCuboid bounds = getBounds();
            BlockPos.MutableBlockPos mutable = new BlockPos.MutableBlockPos();
            for (Direction direction : EnumUtils.DIRECTIONS) {
                mutable.setWithOffset(pos, direction);
                if (bounds.getRelativeLocation(mutable) == CuboidRelative.OUTSIDE) {
                    return direction;
                }
            }
        }
        return null;
    }

    @NotNull
    @Override
    public List<IInventorySlot> getInventorySlots(@Nullable Direction side) {
        return isFormed() || isRemote() ? inventorySlots : Collections.emptyList();
    }

    @NotNull
    @Override
    public List<IExtendedFluidTank> getFluidTanks(@Nullable Direction side) {
        return isFormed() || isRemote() ? fluidTanks : Collections.emptyList();
    }

    @NotNull
    @Override
    public List<IChemicalTank> getChemicalTanks(@Nullable Direction side) {
        return isFormed() || isRemote() ? chemicalTanks : Collections.emptyList();
    }

    @NotNull
    @Override
    public List<IEnergyContainer> getEnergyContainers(@Nullable Direction side) {
        return isFormed() || isRemote() ? energyContainers : Collections.emptyList();
    }

    @NotNull
    @Override
    public List<IHeatCapacitor> getHeatCapacitors(Direction side) {
        return isFormed() || isRemote() ? heatCapacitors : Collections.emptyList();
    }

    public boolean isKnownLocation(BlockPos pos) {
        return locations.contains(pos) || internalLocations.contains(pos);
    }

    public Collection<ValveData> getValveData() {
        return valves;
    }

    @Override
    public void onContentsChanged() {
        markDirty();
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
    public void markDirtyComparator(Level world) {
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

    public void notifyAllUpdateComparator(Level world) {
        for (ValveData valve : valves) {
            TileEntityMultiblock<?> tile = WorldUtils.getTileEntity(TileEntityMultiblock.class, world, valve.location);
            if (tile != null) {
                tile.markDirtyComparator();
            }
        }
    }

    public void forceUpdateComparatorLevel() {
        currentRedstoneLevel = getMultiblockRedstoneLevel();
    }

    protected int getMultiblockRedstoneLevel() {
        return Redstone.SIGNAL_NONE;
    }

    public int getCurrentRedstoneLevel() {
        return currentRedstoneLevel;
    }

    protected <CACHE> List<CACHE> getActiveOutputs(List<? extends OutputTarget<CACHE, Void>> outputs) {
        return getActiveOutputs(outputs, null);
    }

    protected <CACHE, DATA> List<CACHE> getActiveOutputs(List<? extends OutputTarget<CACHE, DATA>> outputs, DATA data) {
        //TODO: Try to somehow cache which ones can currently output?
        List<CACHE> targets = new ArrayList<>(outputs.size());
        for (OutputTarget<CACHE, DATA> target : outputs) {
            if (target.canOutput(data)) {
                targets.add(target.cache());
            }
        }
        return targets;
    }

    public record EnergyOutputTarget(BlockEnergyCapabilityCache cache, BooleanSupplier isActive) implements OutputTarget<BlockEnergyCapabilityCache, Void> {

        @Override
        public boolean canOutput(Void unused) {
            return isActive.getAsBoolean();
        }
    }

    public record CapabilityOutputTarget<TYPE>(BlockCapabilityCache<TYPE, @Nullable Direction> cache, BooleanSupplier isActive) implements OutputTarget<BlockCapabilityCache<TYPE, @Nullable Direction>, Void> {

        @Override
        public boolean canOutput(Void unused) {
            return isActive.getAsBoolean();
        }
    }

    public record AdvancedCapabilityOutputTarget<TYPE, DATA>(BlockCapabilityCache<TYPE, @Nullable Direction> cache, Predicate<DATA> isActive) implements OutputTarget<BlockCapabilityCache<TYPE, @Nullable Direction>, DATA> {

        @Override
        public boolean canOutput(DATA data) {
            return isActive.test(data);
        }
    }

    protected interface OutputTarget<CACHE, DATA> {

        CACHE cache();

        boolean canOutput(DATA data);
    }
}