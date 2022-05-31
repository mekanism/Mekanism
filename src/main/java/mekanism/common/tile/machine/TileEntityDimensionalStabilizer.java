package mekanism.common.tile.machine;

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.BiFunction;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import mekanism.api.Action;
import mekanism.api.AutomationType;
import mekanism.api.IContentsListener;
import mekanism.api.NBTConstants;
import mekanism.api.RelativeSide;
import mekanism.api.math.FloatingLong;
import mekanism.common.capabilities.Capabilities;
import mekanism.common.capabilities.energy.FixedUsageEnergyContainer;
import mekanism.common.capabilities.holder.energy.EnergyContainerHelper;
import mekanism.common.capabilities.holder.energy.IEnergyContainerHolder;
import mekanism.common.capabilities.holder.slot.IInventorySlotHolder;
import mekanism.common.capabilities.holder.slot.InventorySlotHelper;
import mekanism.common.capabilities.resolver.BasicCapabilityResolver;
import mekanism.common.config.MekanismConfig;
import mekanism.common.integration.computer.SpecialComputerMethodWrapper.ComputerIInventorySlotWrapper;
import mekanism.common.integration.computer.annotation.WrappingComputerMethod;
import mekanism.common.inventory.container.MekanismContainer;
import mekanism.common.inventory.slot.EnergyInventorySlot;
import mekanism.common.lib.chunkloading.IChunkLoader;
import mekanism.common.registries.MekanismBlocks;
import mekanism.common.tile.base.SubstanceType;
import mekanism.common.tile.base.TileEntityMekanism;
import mekanism.common.tile.component.TileComponentChunkLoader;
import mekanism.common.tile.interfaces.ISustainedData;
import mekanism.common.util.MekanismUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.state.BlockState;

public class TileEntityDimensionalStabilizer extends TileEntityMekanism implements IChunkLoader, ISustainedData {

    public static final int MAX_LOAD_RADIUS = 2;
    public static final int MAX_LOAD_DIAMETER = 2 * MAX_LOAD_RADIUS + 1;
    private static final BiFunction<FloatingLong, TileEntityDimensionalStabilizer, FloatingLong> BASE_ENERGY_CALCULATOR =
          (base, tile) -> base.multiply(tile.chunksLoaded);

    private final ChunkLoader chunkLoaderComponent;
    private final boolean[][] loadingChunks;
    //TODO: Expose as a computer method getChunksLoaded
    private int chunksLoaded = 1;

    private FixedUsageEnergyContainer<TileEntityDimensionalStabilizer> energyContainer;
    @WrappingComputerMethod(wrapper = ComputerIInventorySlotWrapper.class, methodNames = "getEnergyItem")
    private EnergyInventorySlot energySlot;

    public TileEntityDimensionalStabilizer(BlockPos pos, BlockState state) {
        super(MekanismBlocks.DIMENSIONAL_STABILIZER, pos, state);
        addCapabilityResolver(BasicCapabilityResolver.constant(Capabilities.CONFIG_CARD_CAPABILITY, this));

        chunkLoaderComponent = new ChunkLoader(this);
        loadingChunks = new boolean[MAX_LOAD_DIAMETER][MAX_LOAD_DIAMETER];
        //Center chunk where the stabilizer is, is always loaded (unless none are loaded due to energy or control mode)
        loadingChunks[MAX_LOAD_RADIUS][MAX_LOAD_RADIUS] = true;
        //TODO: Strictly speaking center chunk should always be loaded if at least one other chunk is loaded
        // due to the fact that we need to be using energy. This isn't currently the case but it should be made to be
        //TODO: Visuals button on GUI and then either do something like F3+G or sort of like the digital miner?
    }

    @Nonnull
    @Override
    protected IEnergyContainerHolder getInitialEnergyContainers(IContentsListener listener) {
        EnergyContainerHelper builder = EnergyContainerHelper.forSide(this::getDirection);
        builder.addContainer(energyContainer = FixedUsageEnergyContainer.input(this, BASE_ENERGY_CALCULATOR, listener));
        return builder.build();
    }

    @Nonnull
    @Override
    protected IInventorySlotHolder getInitialInventory(IContentsListener listener) {
        InventorySlotHelper builder = InventorySlotHelper.forSide(this::getDirection);
        builder.addSlot(energySlot = EnergyInventorySlot.fillOrConvert(energyContainer, this::getLevel, listener, 143, 35), RelativeSide.BACK);
        return builder.build();
    }

    @Override
    protected void onUpdateServer() {
        super.onUpdateServer();
        energySlot.fillContainerOrConvert();
        //Only attempt to use power if chunk loading isn't disabled in the config
        if (MekanismConfig.general.allowChunkloading.get() && MekanismUtils.canFunction(this)) {
            FloatingLong energyPerTick = energyContainer.getEnergyPerTick();
            if (energyContainer.extract(energyPerTick, Action.SIMULATE, AutomationType.INTERNAL).equals(energyPerTick)) {
                energyContainer.extract(energyPerTick, Action.EXECUTE, AutomationType.INTERNAL);
                setActive(true);
            } else {
                setActive(false);
            }
        } else {
            setActive(false);
        }
    }

    //TODO: Expose as a computer method
    public boolean isChunkloadingAt(int x, int z) {
        return loadingChunks[x][z];
    }

    //TODO: Expose as a computer method that requires it to be public
    public void toggleChunkloadingAt(int x, int z) {
        //Validate x and z are valid as this is called from a packet
        if (x >= 0 && x < MAX_LOAD_DIAMETER && z >= 0 && z < MAX_LOAD_DIAMETER) {
            if (setChunkLoadingAt(x, z, !isChunkloadingAt(x, z))) {
                setChanged(false);
                energyContainer.updateEnergyPerTick();
                //Refresh the chunks that are loaded as it has changed
                getChunkLoader().refreshChunkTickets();
            }
        }
    }

    //TODO: Expose as a computer method that requires it to be public and validates positions and marks for refresh and the like
    // Also probably should have it use negatives in computer land so it is offset compared to where we actually are
    private boolean setChunkLoadingAt(int x, int z, boolean load) {
        if (x == MAX_LOAD_RADIUS && z == MAX_LOAD_RADIUS) {
            //Center chunk where the stabilizer is, is always loaded (unless none are loaded due to energy or control mode)
            // so just skip and return we don't need to update if that is the position that someone attempts to change
            return false;
        } else if (isChunkloadingAt(x, z) != load) {
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
                if (isChunkloadingAt(x + MAX_LOAD_RADIUS, z + MAX_LOAD_RADIUS)) {
                    chunkSet.add(new ChunkPos(chunkX + x, chunkZ + z));
                }
            }
        }
        return chunkSet;
    }

    @Override
    public int getRedstoneLevel() {
        return getActive() ? 15 : 0;
    }

    @Override
    protected boolean makesComparatorDirty(@Nullable SubstanceType type) {
        return false;
    }

    @Override
    public int getCurrentRedstoneLevel() {
        //We don't cache the redstone level for the dimensional stabilizer
        return getRedstoneLevel();
    }

    @Override
    public void addContainerTrackers(MekanismContainer container) {
        super.addContainerTrackers(container);
        container.trackArray(loadingChunks);
    }

    @Override
    protected void addGeneralPersistentData(@Nonnull CompoundTag nbt) {
        super.addGeneralPersistentData(nbt);
        writeChunksToLoad(nbt);
    }

    @Override
    protected void loadGeneralPersistentData(@Nonnull CompoundTag nbt) {
        super.loadGeneralPersistentData(nbt);
        readChunksToLoad(nbt);
    }

    private void writeChunksToLoad(@Nonnull CompoundTag nbtTags) {
        byte[] chunksToLoad = new byte[MAX_LOAD_DIAMETER * MAX_LOAD_DIAMETER];
        for (int x = 0; x < MAX_LOAD_DIAMETER; x++) {
            for (int z = 0; z < MAX_LOAD_DIAMETER; z++) {
                chunksToLoad[x * MAX_LOAD_DIAMETER + z] = (byte) (isChunkloadingAt(x, z) ? 1 : 0);
            }
        }
        nbtTags.putByteArray(NBTConstants.STABILIZER_CHUNKS_TO_LOAD, chunksToLoad);
    }

    private boolean readChunksToLoad(@Nonnull CompoundTag nbt) {
        boolean changed = false;
        int lastChunksLoaded = chunksLoaded;
        byte[] chunksToLoad = nbt.getByteArray(NBTConstants.STABILIZER_CHUNKS_TO_LOAD);
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
        if (chunksLoaded != lastChunksLoaded) {
            //If the number of chunks loaded is different we need to update our energy to use
            energyContainer.updateEnergyPerTick();
        }
        return changed;
    }

    @Override
    public void writeSustainedData(CompoundTag dataMap) {
        writeChunksToLoad(dataMap);
    }

    @Override
    public void readSustainedData(CompoundTag dataMap) {
        if (readChunksToLoad(dataMap)) {
            //Refresh the chunks that are loaded as it has changed
            getChunkLoader().refreshChunkTickets();
        }
    }

    @Override
    public Map<String, String> getTileDataRemap() {
        Map<String, String> remap = new Object2ObjectOpenHashMap<>();
        remap.put(NBTConstants.STABILIZER_CHUNKS_TO_LOAD, NBTConstants.STABILIZER_CHUNKS_TO_LOAD);
        return remap;
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

    private class ChunkLoader extends TileComponentChunkLoader<TileEntityDimensionalStabilizer> {

        public ChunkLoader(TileEntityDimensionalStabilizer tile) {
            super(tile);
        }

        @Override
        public boolean canOperate() {
            return MekanismConfig.general.allowChunkloading.get() && getActive();
        }
    }
}
