package mekanism.common.tile.machine;

import java.util.HashSet;
import java.util.Set;
import mekanism.api.Action;
import mekanism.api.AutomationType;
import mekanism.api.IContentsListener;
import mekanism.api.RelativeSide;
import mekanism.api.SerializationConstants;
import mekanism.api.functions.LongObjectToLongFunction;
import mekanism.api.math.MathUtils;
import mekanism.common.attachments.StabilizedChunks;
import mekanism.common.attachments.containers.ContainerType;
import mekanism.common.capabilities.energy.FixedUsageEnergyContainer;
import mekanism.common.capabilities.holder.energy.EnergyContainerHelper;
import mekanism.common.capabilities.holder.energy.IEnergyContainerHolder;
import mekanism.common.capabilities.holder.slot.IInventorySlotHolder;
import mekanism.common.capabilities.holder.slot.InventorySlotHelper;
import mekanism.common.config.MekanismConfig;
import mekanism.common.integration.computer.ComputerException;
import mekanism.common.integration.computer.SpecialComputerMethodWrapper.ComputerIInventorySlotWrapper;
import mekanism.common.integration.computer.annotation.ComputerMethod;
import mekanism.common.integration.computer.annotation.SyntheticComputerMethod;
import mekanism.common.integration.computer.annotation.WrappingComputerMethod;
import mekanism.common.inventory.container.MekanismContainer;
import mekanism.common.inventory.slot.EnergyInventorySlot;
import mekanism.common.lib.chunkloading.IChunkLoader;
import mekanism.common.registries.MekanismBlocks;
import mekanism.common.registries.MekanismDataComponents;
import mekanism.common.tile.base.TileEntityMekanism;
import mekanism.common.tile.component.TileComponentChunkLoader;
import mekanism.common.tile.interfaces.IHasVisualization;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.SectionPos;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.redstone.Redstone;
import org.jetbrains.annotations.NotNull;

public class TileEntityDimensionalStabilizer extends TileEntityMekanism implements IChunkLoader, IHasVisualization {

    public static final int MAX_LOAD_RADIUS = 2;
    public static final int MAX_LOAD_DIAMETER = 2 * MAX_LOAD_RADIUS + 1;
    private static final String COMPUTER_RANGE_STR = "Range: [-" + MAX_LOAD_RADIUS + ", " + MAX_LOAD_RADIUS + "]";
    private static final String COMPUTER_RANGE_RAD = "Range: [1, " + MAX_LOAD_RADIUS + "]";
    private static final LongObjectToLongFunction<TileEntityDimensionalStabilizer> BASE_ENERGY_CALCULATOR = (base, tile) -> MathUtils.multiplyClamped(base, tile.chunksLoaded);

    private final ChunkLoader chunkLoaderComponent;
    private final boolean[][] loadingChunks;
    @SyntheticComputerMethod(getter = "getChunksLoaded", getterDescription = "Get the number of chunks being loaded.")
    private int chunksLoaded = 1;
    private boolean clientRendering;

    private FixedUsageEnergyContainer<TileEntityDimensionalStabilizer> energyContainer;
    @WrappingComputerMethod(wrapper = ComputerIInventorySlotWrapper.class, methodNames = "getEnergyItem", docPlaceholder = "energy slot")
    EnergyInventorySlot energySlot;

    public TileEntityDimensionalStabilizer(BlockPos pos, BlockState state) {
        super(MekanismBlocks.DIMENSIONAL_STABILIZER, pos, state);

        chunkLoaderComponent = new ChunkLoader(this);
        loadingChunks = new boolean[MAX_LOAD_DIAMETER][MAX_LOAD_DIAMETER];
        //Center chunk where the stabilizer is, is always loaded (unless none are loaded due to energy or control mode)
        loadingChunks[MAX_LOAD_RADIUS][MAX_LOAD_RADIUS] = true;
    }

    @NotNull
    @Override
    protected IEnergyContainerHolder getInitialEnergyContainers(IContentsListener listener) {
        EnergyContainerHelper builder = EnergyContainerHelper.forSide(facingSupplier);
        builder.addContainer(energyContainer = FixedUsageEnergyContainer.input(this, BASE_ENERGY_CALCULATOR, listener));
        return builder.build();
    }

    @NotNull
    @Override
    protected IInventorySlotHolder getInitialInventory(IContentsListener listener) {
        InventorySlotHelper builder = InventorySlotHelper.forSide(facingSupplier);
        builder.addSlot(energySlot = EnergyInventorySlot.fillOrConvert(energyContainer, this::getLevel, listener, 143, 35), RelativeSide.BACK);
        return builder.build();
    }

    @Override
    protected boolean onUpdateServer() {
        boolean sendUpdatePacket = super.onUpdateServer();
        energySlot.fillContainerOrConvert();
        //Only attempt to use power if chunk loading isn't disabled in the config
        if (MekanismConfig.general.allowChunkloading.get() && canFunction()) {
            long energyPerTick = energyContainer.getEnergyPerTick();
            if (energyContainer.extract(energyPerTick, Action.SIMULATE, AutomationType.INTERNAL) == energyPerTick) {
                energyContainer.extract(energyPerTick, Action.EXECUTE, AutomationType.INTERNAL);
                setActive(true);
            } else {
                setActive(false);
            }
        } else {
            setActive(false);
        }
        return sendUpdatePacket;
    }

    public boolean isChunkLoadingAt(int x, int z) {
        return loadingChunks[x][z];
    }

    public void toggleChunkLoadingAt(int x, int z) {
        //Validate x and z are valid as this is called from a packet
        if (x >= 0 && x < MAX_LOAD_DIAMETER && z >= 0 && z < MAX_LOAD_DIAMETER) {
            if (setChunkLoadingAt(x, z, !isChunkLoadingAt(x, z))) {
                setChanged(false);
                energyContainer.updateEnergyPerTick();
                //Refresh the chunks that are loaded as it has changed
                getChunkLoader().refreshChunkTickets();
            }
        }
    }

    public void adjustChunkLoadingRadius(int radius, boolean load) {
        //Validate radius as this is called from a packet
        if (radius > 0 && radius <= MAX_LOAD_RADIUS) {
            boolean changed = false;
            for (int x = -radius; x <= radius; x++) {
                boolean skipInner = x > -radius && x < radius;
                int actualX = x + MAX_LOAD_RADIUS;
                for (int z = -radius; z <= radius; z += skipInner ? 2 * radius : 1) {
                    if (setChunkLoadingAt(actualX, z + MAX_LOAD_RADIUS, load)) {
                        changed = true;
                    }
                }
            }
            if (changed) {
                //If something actually changed, then save the changes and update the needed energy and chunk tickets
                // in theory from packet something will always change, but in case there is a desync or in case this
                // is done via a computer mod on already set chunks, don't actually update anything
                setChanged(false);
                energyContainer.updateEnergyPerTick();
                //Refresh the chunks that are loaded as it has changed
                getChunkLoader().refreshChunkTickets();
            }
        }
    }

    private boolean setChunkLoadingAt(int x, int z, boolean load) {
        if (x == MAX_LOAD_RADIUS && z == MAX_LOAD_RADIUS) {
            //Center chunk where the stabilizer is, is always loaded (unless none are loaded due to energy or control mode)
            // so just skip and return we don't need to update if that is the position that someone attempts to change
            return false;
        } else if (isChunkLoadingAt(x, z) != load) {
            loadingChunks[x][z] = load;
            if (load) {
                chunksLoaded++;
            } else {
                chunksLoaded--;
            }
            return true;
        }
        return false;
    }

    @Override
    public TileComponentChunkLoader<TileEntityDimensionalStabilizer> getChunkLoader() {
        return chunkLoaderComponent;
    }

    @Override
    public Set<ChunkPos> getChunkSet() {
        Set<ChunkPos> chunkSet = new HashSet<>();
        int chunkX = SectionPos.blockToSectionCoord(worldPosition.getX());
        int chunkZ = SectionPos.blockToSectionCoord(worldPosition.getZ());
        for (int x = -MAX_LOAD_RADIUS; x <= MAX_LOAD_RADIUS; x++) {
            for (int z = -MAX_LOAD_RADIUS; z <= MAX_LOAD_RADIUS; z++) {
                if (isChunkLoadingAt(x + MAX_LOAD_RADIUS, z + MAX_LOAD_RADIUS)) {
                    chunkSet.add(new ChunkPos(chunkX + x, chunkZ + z));
                }
            }
        }
        return chunkSet;
    }

    @Override
    public int getRedstoneLevel() {
        return getActive() ? Redstone.SIGNAL_MAX : Redstone.SIGNAL_NONE;
    }

    @Override
    protected boolean makesComparatorDirty(ContainerType<?, ?, ?> type) {
        return false;
    }

    @Override
    public int getCurrentRedstoneLevel() {
        //We don't cache the redstone level for the dimensional stabilizer
        return getRedstoneLevel();
    }

    @Override
    public boolean isClientRendering() {
        return clientRendering;
    }

    @Override
    public void toggleClientRendering() {
        this.clientRendering = !clientRendering;
    }

    @Override
    public void addContainerTrackers(MekanismContainer container) {
        super.addContainerTrackers(container);
        container.trackArray(loadingChunks);
    }

    @Override
    public void writeSustainedData(HolderLookup.Provider provider, CompoundTag dataMap) {
        super.writeSustainedData(provider, dataMap);
        byte[] chunksToLoad = new byte[MAX_LOAD_DIAMETER * MAX_LOAD_DIAMETER];
        for (int x = 0; x < MAX_LOAD_DIAMETER; x++) {
            for (int z = 0; z < MAX_LOAD_DIAMETER; z++) {
                chunksToLoad[x * MAX_LOAD_DIAMETER + z] = (byte) (isChunkLoadingAt(x, z) ? 1 : 0);
            }
        }
        dataMap.putByteArray(SerializationConstants.STABILIZER_CHUNKS_TO_LOAD, chunksToLoad);
    }

    @Override
    public void readSustainedData(HolderLookup.Provider provider, @NotNull CompoundTag dataMap) {
        super.readSustainedData(provider, dataMap);
        boolean changed = false;
        int lastChunksLoaded = chunksLoaded;
        byte[] chunksToLoad = dataMap.getByteArray(SerializationConstants.STABILIZER_CHUNKS_TO_LOAD);
        if (chunksToLoad.length != MAX_LOAD_DIAMETER * MAX_LOAD_DIAMETER) {
            //If it is the wrong size dummy it to all zeros so things get set to false as we don't know
            // where to position our values
            chunksToLoad = new byte[MAX_LOAD_DIAMETER * MAX_LOAD_DIAMETER];
        }
        for (int x = 0; x < MAX_LOAD_DIAMETER; x++) {
            for (int z = 0; z < MAX_LOAD_DIAMETER; z++) {
                changed |= setChunkLoadingAt(x, z, chunksToLoad[x * MAX_LOAD_DIAMETER + z] == 1);
            }
        }
        if (changed) {
            if (chunksLoaded != lastChunksLoaded) {
                //If the number of chunks loaded is different we need to update our energy to use
                energyContainer.updateEnergyPerTick();
            }
            if (hasLevel()) {
                //Refresh the chunks that are loaded as it has changed
                getChunkLoader().refreshChunkTickets();
            }
        }
    }

    @Override
    protected void applyImplicitComponents(@NotNull BlockEntity.DataComponentInput input) {
        super.applyImplicitComponents(input);
        //TODO - 1.20.4: Deduplicate this and the from nbt
        boolean changed = false;
        int lastChunksLoaded = chunksLoaded;
        StabilizedChunks chunksToLoad = input.getOrDefault(MekanismDataComponents.STABILIZER_CHUNKS, StabilizedChunks.NONE);
        if (chunksToLoad == StabilizedChunks.NONE) {
            //Skip if we don't have any data
            return;
        }
        for (int x = 0; x < MAX_LOAD_DIAMETER; x++) {
            for (int z = 0; z < MAX_LOAD_DIAMETER; z++) {
                changed |= setChunkLoadingAt(x, z, chunksToLoad.loaded(x * MAX_LOAD_DIAMETER + z));
            }
        }
        if (changed) {
            if (chunksLoaded != lastChunksLoaded) {
                //If the number of chunks loaded is different we need to update our energy to use
                energyContainer.updateEnergyPerTick();
            }
            if (hasLevel()) {
                //Refresh the chunks that are loaded as it has changed
                getChunkLoader().refreshChunkTickets();
            }
        }
    }

    @Override
    protected void collectImplicitComponents(@NotNull DataComponentMap.Builder builder) {
        super.collectImplicitComponents(builder);
        builder.set(MekanismDataComponents.STABILIZER_CHUNKS, StabilizedChunks.create(this));
    }

    @Override
    public void configurationDataSet() {
        super.configurationDataSet();
        //Refresh the chunk tickets as they may have changed
        getChunkLoader().refreshChunkTickets();
    }

    public FixedUsageEnergyContainer<TileEntityDimensionalStabilizer> getEnergyContainer() {
        return energyContainer;
    }

    //Methods relating to IComputerTile
    private int validateDimension(int val, boolean x) throws ComputerException {
        if (val < -MAX_LOAD_RADIUS || val > MAX_LOAD_RADIUS) {
            throw new ComputerException("%s offset '%d' is not in range, must be between %d and %d inclusive.", x ? "X" : "Z", val, -MAX_LOAD_RADIUS, MAX_LOAD_RADIUS);
        }
        //Shift up by max load radius as internally we act starting at zero
        return val + MAX_LOAD_RADIUS;
    }

    @ComputerMethod(nameOverride = "isChunkLoadingAt", methodDescription = "Check if the Dimensional Stabilizer is configured to load a the specified relative chunk position at x,y (Stabilizer is at 0,0). " + COMPUTER_RANGE_STR)
    boolean computerIsChunkloadingAt(int x, int z) throws ComputerException {
        return isChunkLoadingAt(validateDimension(x, true), validateDimension(z, false));
    }

    @ComputerMethod(nameOverride = "toggleChunkLoadingAt", requiresPublicSecurity = true, methodDescription = "Toggle loading the specified relative chunk at the relative x,y position (Stabilizer is at 0,0). Just like clicking the button in the GUI. " + COMPUTER_RANGE_STR)
    void computerToggleChunkLoadingAt(int x, int z) throws ComputerException {
        validateSecurityIsPublic();
        toggleChunkLoadingAt(validateDimension(x, true), validateDimension(z, false));
    }

    @ComputerMethod(nameOverride = "setChunkLoadingAt", requiresPublicSecurity = true, methodDescription = "Set if the Dimensional Stabilizer is configured to load a the specified relative position (Stabilizer is at 0,0). True = load the chunk, false = don't load the chunk. " + COMPUTER_RANGE_STR)
    void computerSetChunkLoadingAt(int x, int z, boolean load) throws ComputerException {
        validateSecurityIsPublic();
        if (setChunkLoadingAt(validateDimension(x, true), validateDimension(z, false), load)) {
            //If it changed we need to mark it as such and update various things
            setChanged(false);
            energyContainer.updateEnergyPerTick();
            //Refresh the chunks that are loaded as it has changed
            getChunkLoader().refreshChunkTickets();
        }
    }

    private void validateRadius(int radius) throws ComputerException {
        if (radius <= 0 || radius > MAX_LOAD_RADIUS) {
            throw new ComputerException("Radius '%d' is not in range, must be between 1 and %d inclusive.", radius, MAX_LOAD_RADIUS);
        }
    }

    @ComputerMethod(requiresPublicSecurity = true, methodDescription = "Sets the chunks in the specified radius to be loaded. The chunk the Stabilizer is in is always loaded. " + COMPUTER_RANGE_RAD)
    void enableChunkLoadingFor(int radius) throws ComputerException {
        validateSecurityIsPublic();
        validateRadius(radius);
        adjustChunkLoadingRadius(radius, true);
    }

    @ComputerMethod(requiresPublicSecurity = true, methodDescription = "Sets the chunks in the specified radius to not be kept loaded. The chunk the Stabilizer is in is always loaded. " + COMPUTER_RANGE_RAD)
    void disableChunkLoadingFor(int radius) throws ComputerException {
        validateSecurityIsPublic();
        validateRadius(radius);
        adjustChunkLoadingRadius(radius, false);
    }
    //End methods IComputerTile

    private class ChunkLoader extends TileComponentChunkLoader<TileEntityDimensionalStabilizer> {

        public ChunkLoader(TileEntityDimensionalStabilizer tile) {
            super(tile, true);
        }

        @Override
        public boolean canOperate() {
            return MekanismConfig.general.allowChunkloading.get() && getActive();
        }
    }
}
