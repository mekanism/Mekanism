package mekanism.common.lib.multiblock;

import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.function.BiPredicate;
import java.util.function.BooleanSupplier;
import java.util.function.Predicate;
import java.util.function.Supplier;
import mekanism.api.AutomationType;
import mekanism.api.IContentsListener;
import mekanism.api.SerializationConstants;
import mekanism.api.chemical.IChemicalTank;
import mekanism.api.energy.IEnergyContainer;
import mekanism.api.fluid.IFluidTank;
import mekanism.api.heat.HeatAPI;
import mekanism.api.heat.IHeatCapacitor;
import mekanism.api.inventory.IInventorySlot;
import mekanism.common.capabilities.heat.ITileHeatHandler;
import mekanism.common.component.containers.type.ContainerType;
import mekanism.common.integration.computer.annotation.ComputerMethod;
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
import mekanism.common.util.WorldUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.UUIDUtil;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.redstone.Redstone;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.neoforged.neoforge.capabilities.BlockCapabilityCache;
import org.jspecify.annotations.Nullable;

public class MultiblockData implements IMultiblockContents, ITileHeatHandler, IContentsListener {

    public Set<BlockPos> locations = new ObjectOpenHashSet<>();
    /// @apiNote This set is only used for purposes of caching all known valid inner blocks of a multiblock structure, for use in checking if we need to revalidate the
    /// multiblock when something changes, cases we want to skip are inner nodes just changing state (for example, super heating elements being activated) This set is not
    /// synced or checked anywhere (for things like equals) as it is only used on the server and isn't part of the structure's information. It also is not the most
    /// accurate of checks that get done against this as there is no way to tell if the state actually changed or if the block changed entirely, but assuming no one is
    /// replacing the blocks inside a multiblock (which is unsupported) it will handle it fine, and we can easily special-case it becoming air as having been "broken"
    public Set<BlockPos> internalLocations = new ObjectOpenHashSet<>();
    public Map<BlockPos, ValveData> valves = new HashMap<>();

    @ContainerSync(getter = "getVolume", setter = "setVolume")
    private int volume;

    @Nullable
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

    private final Supplier<@Nullable Level> worldSupplier;

    protected final List<IInventorySlot> inventorySlots = new ArrayList<>();
    protected final List<IFluidTank> fluidTanks = new ArrayList<>();
    protected final List<IChemicalTank> chemicalTanks = new ArrayList<>();

    private final BiPredicate<Object, AutomationType> formedBiPred = (_, _) -> isFormed();
    private final BiPredicate<Object, AutomationType> notExternalFormedBiPred = (_, automationType) -> !automationType.isExternal() && isFormed();

    private boolean dirty;

    public MultiblockData(BlockEntity tile) {
        worldSupplier = tile::getLevel;
    }

    @SuppressWarnings("unchecked")
    public <T> BiPredicate<T, AutomationType> formedBiPred() {
        return (BiPredicate<T, AutomationType>) formedBiPred;
    }

    @SuppressWarnings("unchecked")
    public <T> BiPredicate<T, AutomationType> notExternalFormedBiPred() {
        return (BiPredicate<T, AutomationType>) notExternalFormedBiPred;
    }

    protected IContentsListener createSaveAndComparator() {
        return createSaveAndComparator(this);
    }

    protected IContentsListener createSaveAndComparator(IContentsListener contentsListener) {
        return () -> {
            contentsListener.onContentsChanged();
            Level level = getLevel();
            if (level != null && !level.isClientSide()) {
                markDirtyComparator(level);
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

    /// Returns true if the multiblock's gui can be accessed via structural multiblocks, false otherwise. An example this may be false for would be on a thermal
    /// evaporation plant.
    public boolean allowsStructuralGuiAccess(TileEntityStructuralMultiblock multiblock) {
        return true;
    }

    /// Tick the multiblock.
    ///
    /// @return if we need an update packet
    public boolean tick(ServerLevel world) {
        boolean needsPacket = false;
        for (ValveData data : valves.values()) {
            needsPacket |= data.tick();
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
        int seaLevel = world.getSeaLevel();
        double sum = 0;
        for (BlockPos pos : positions) {
            sum += world.getBiome(pos).value().getTemperature(pos, seaLevel);
        }
        return sum / positions.length;
    }

    public boolean setShape(@Nullable IShape shape) {
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
                internalMultiblock.setMultiblock(world, this);
            }
        }
        for (BlockPos pos : locations) {
            BlockEntity tile = WorldUtils.getTileEntity(world, pos);
            if (tile instanceof IStructuralMultiblock structuralMultiblock) {
                structuralMultiblock.multiblockFormed(this);
            }
        }

        if (shouldCache(MultiblockCache.FLUID)) {
            for (IFluidTank tank : getFluidTanks()) {
                ContainerType.FLUID.clampContents(tank, null);
            }
        }
        if (shouldCache(MultiblockCache.CHEMICAL)) {
            for (IChemicalTank tank : getChemicalTanks()) {
                ContainerType.CHEMICAL.clampContents(tank, null);
            }
        }
        if (shouldCache(MultiblockCache.ENERGY)) {
            IEnergyContainer container = getEnergyContainer();
            if (container != null) {
                ContainerType.ENERGY.clampContents(container, null);
            }
        }
        updateEjectors(world);
        forceUpdateComparatorLevel();
    }

    protected void updateEjectors(Level world) {
    }

    protected boolean isRemote() {
        Level level = getLevel();
        return level == null || level.isClientSide();
    }

    @Nullable
    public Level getLevel() {
        return worldSupplier.get();
    }

    protected boolean shouldCache(CacheSubstance<?> type) {
        return true;
    }

    public void remove(LevelReader world, Structure oldStructure) {
        for (BlockPos pos : internalLocations) {
            BlockEntity tile = WorldUtils.getTileEntity(world, pos);
            if (tile instanceof IInternalMultiblock internalMultiblock) {
                internalMultiblock.setMultiblock(world, null);
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

    public void readUpdateTag(ValueInput input) {
        input.getInt(SerializationConstants.VOLUME).ifPresent(this::setVolume);
        input.read(SerializationConstants.RENDER_LOCATION, BlockPos.CODEC).ifPresent(value -> renderLocation = value);
        Optional<BlockPos> minPos = input.read(SerializationConstants.MIN, BlockPos.CODEC);
        Optional<BlockPos> maxPos = input.read(SerializationConstants.MAX, BlockPos.CODEC);
        if (minPos.isPresent() && maxPos.isPresent()) {
            bounds = new VoxelCuboid(minPos.get(), maxPos.get());
        }
        inventoryID = input.read(SerializationConstants.INVENTORY_ID, UUIDUtil.CODEC).orElse(null);
    }

    public void writeUpdateTag(ValueOutput output) {
        output.putInt(SerializationConstants.VOLUME, getVolume());
        //In theory this shouldn't be null here but check it anyway
        output.storeNullable(SerializationConstants.RENDER_LOCATION, BlockPos.CODEC, renderLocation);
        output.store(SerializationConstants.MIN, BlockPos.CODEC, bounds.getMinPos());
        output.store(SerializationConstants.MAX, BlockPos.CODEC, bounds.getMaxPos());
        output.storeNullable(SerializationConstants.INVENTORY_ID, UUIDUtil.CODEC, inventoryID);
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

    /// Checks if this multiblock is formed and the given position is insides the bounds of this multiblock
    public <T extends MultiblockData> boolean isPositionInsideBounds(Structure structure, BlockGetter level, BlockPos pos) {
        if (isFormed()) {
            VoxelCuboid bounds = getBounds();
            CuboidRelative relativeLocation = bounds.getRelativeLocation(pos);
            if (relativeLocation == CuboidRelative.INSIDE) {
                return true;
            } else if (relativeLocation.isWall()) {
                //If we are in the wall check if we are really an inner position. For example evap towers
                MultiblockManager<T> manager = (MultiblockManager<T>) structure.getManager();
                MultiblockType<T> multiblockType = (MultiblockType<T>) structure.multiblockType();
                if (manager != null && multiblockType != null) {
                    IStructureValidator<T> validator = multiblockType.createValidator();
                    if (validator instanceof CuboidStructureValidator<T> cuboidValidator) {
                        validator.init(level, manager, multiblockType, structure);
                        cuboidValidator.loadCuboid(bounds);
                        return cuboidValidator.getStructureRequirement(pos, bounds) == StructureRequirement.INNER;
                    }
                }
            }
        }
        return false;
    }

    /// Checks if this multiblock is formed and the given position is insides the bounds of this multiblock
    public boolean isPositionOutsideBounds(BlockPos pos) {
        return isFormed() && getBounds().getRelativeLocation(pos) == CuboidRelative.OUTSIDE;
    }

    @Nullable
    public Direction getOutsideSide(BlockPos pos) {
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

    @Override
    public List<IInventorySlot> getInventorySlots() {
        return isFormed() || isRemote() ? inventorySlots : Collections.emptyList();
    }

    @Override
    public List<IFluidTank> getFluidTanks() {
        return isFormed() || isRemote() ? fluidTanks : Collections.emptyList();
    }

    protected boolean hasFluidValveHandling() {
        return false;
    }

    public List<IFluidTank> getValveFluidTanks(BlockPos pos) {
        if (!hasFluidValveHandling() || isRemote()) {
            //Note: The client doesn't need to do any handling relating to valves, so just bypass valve handling for it
            return getFluidTanks();
        } else if (isFormed()) {
            ValveData valve = valves.get(pos);
            if (valve == null) {
                //Just return all as we don't have any specific valve wrapping
                return fluidTanks;
            }
            return valve.getValveTanks();
        }
        return Collections.emptyList();
    }

    @Override
    public List<IChemicalTank> getChemicalTanks() {
        return isFormed() || isRemote() ? chemicalTanks : Collections.emptyList();
    }

    @Nullable
    protected IEnergyContainer energyContainer() {
        return null;
    }

    @Nullable
    @Override
    public IEnergyContainer getEnergyContainer() {
        return isFormed() || isRemote() ? energyContainer() : null;
    }

    @Nullable
    protected IHeatCapacitor heatCapacitor() {
        return null;
    }

    @Nullable
    @Override
    public IHeatCapacitor getHeatCapacitor() {
        return isFormed() || isRemote() ? heatCapacitor() : null;
    }

    @Nullable
    @Override
    public IHeatCapacitor getHeatCapacitor(@Nullable Direction side) {
        return getHeatCapacitor();
    }

    public boolean isKnownLocation(BlockPos pos) {
        return locations.contains(pos) || internalLocations.contains(pos);
    }

    public Map<BlockPos, ValveData> getValveData() {
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
    public boolean equals(@Nullable Object obj) {
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
        for (BlockPos valvePos: valves.keySet()) {
            TileEntityMultiblock<?> tile = WorldUtils.getTileEntity(TileEntityMultiblock.class, world, valvePos);
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

    protected <CACHE> List<CACHE> getActiveOutputs(List<? extends OutputTarget<CACHE, @Nullable Void>> outputs) {
        return getActiveOutputs(outputs, null);
    }

    protected <CACHE, DATA extends @Nullable Object> List<CACHE> getActiveOutputs(List<? extends OutputTarget<CACHE, DATA>> outputs, DATA data) {
        if (outputs.isEmpty()) {
            return Collections.emptyList();
        }
        //TODO: Try to somehow cache which ones can currently output?
        List<CACHE> targets = new ArrayList<>(outputs.size());
        for (OutputTarget<CACHE, DATA> target : outputs) {
            if (target.canOutput(data)) {
                targets.add(target.cache());
            }
        }
        return targets;
    }

    public record CapabilityOutputTarget<TYPE>(BlockCapabilityCache<TYPE, @Nullable Direction> cache, BooleanSupplier isActive) implements OutputTarget<BlockCapabilityCache<TYPE, @Nullable Direction>, @Nullable Void> {

        @Override
        public boolean canOutput(@Nullable Void unused) {
            return isActive.getAsBoolean();
        }
    }

    public record AdvancedCapabilityOutputTarget<TYPE, DATA>(BlockCapabilityCache<TYPE, @Nullable Direction> cache, Predicate<DATA> isActive) implements OutputTarget<BlockCapabilityCache<TYPE, @Nullable Direction>, DATA> {

        @Override
        public boolean canOutput(DATA data) {
            return isActive.test(data);
        }
    }

    protected interface OutputTarget<CACHE, DATA extends @Nullable Object> {

        CACHE cache();

        boolean canOutput(DATA data);
    }
}