package mekanism.common.tile.machine;

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.BiFunction;
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
import mekanism.common.integration.computer.ComputerException;
import mekanism.common.integration.computer.SpecialComputerMethodWrapper.ComputerIInventorySlotWrapper;
import mekanism.common.integration.computer.annotation.ComputerMethod;
import mekanism.common.integration.computer.annotation.SyntheticComputerMethod;
import mekanism.common.integration.computer.annotation.WrappingComputerMethod;
import mekanism.common.inventory.container.MekanismContainer;
import mekanism.common.inventory.slot.EnergyInventorySlot;
import mekanism.common.lib.chunkloading.IChunkLoader;
import mekanism.common.registries.MekanismBlocks;
import mekanism.common.tile.base.SubstanceType;
import mekanism.common.tile.base.TileEntityMekanism;
import mekanism.common.tile.component.TileComponentChunkLoader;
import mekanism.common.tile.interfaces.IHasVisualization;
import mekanism.common.tile.interfaces.ISustainedData;
import mekanism.common.util.MekanismUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class TileEntityDimensionalStabilizer extends TileEntityMekanism implements IChunkLoader, ISustainedData, IHasVisualization {

    public static final int MAX_LOAD_RADIUS = 2;
    public static final int MAX_LOAD_DIAMETER = 2 * MAX_LOAD_RADIUS + 1;
    private static final BiFunction<FloatingLong, TileEntityDimensionalStabilizer, FloatingLong> BASE_ENERGY_CALCULATOR = (base, tile) -> base.multiply(tile.chunksLoaded);

    private final ChunkLoader chunkLoaderComponent;
    private final boolean[][] loadingChunks;
    @SyntheticComputerMethod(getter = "getChunksLoaded")
    private int chunksLoaded = 1;
    private boolean clientRendering;

    private FixedUsageEnergyContainer<TileEntityDimensionalStabilizer> energyContainer;
    @WrappingComputerMethod(wrapper = ComputerIInventorySlotWrapper.class, methodNames = "getEnergyItem")
    private EnergyInventorySlot energySlot;

    public TileEntityDimensionalStabilizer(BlockPos pos, BlockState state) {
        super(MekanismBlocks.DIMENSIONAL_STABILIZER, pos, state);
        addCapabilityResolver(BasicCapabilityResolver.constant(Capabilities.CONFIG_CARD, this));

        chunkLoaderComponent = new ChunkLoader(this);
        loadingChunks = new boolean[MAX_LOAD_DIAMETER][MAX_LOAD_DIAMETER];
        //Center chunk where the stabilizer is, is always loaded (unless none are loaded due to energy or control mode)
        loadingChunks[MAX_LOAD_RADIUS][MAX_LOAD_RADIUS] = true;
    }

    @NotNull
    @Override
    protected IEnergyContainerHolder getInitialEnergyContainers(IContentsListener listener) {
        EnergyContainerHelper builder = EnergyContainerHelper.forSide(this::getDirection);
        builder.addContainer(energyContainer = FixedUsageEnergyContainer.input(this, BASE_ENERGY_CALCULATOR, listener));
        return builder.build();
    }

    @NotNull
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

    @NotNull
    @Override
    public AABB getRenderBoundingBox() {
        if (isClientRendering() && canDisplayVisuals() && level != null) {
            int chunkX = SectionPos.blockToSectionCoord(worldPosition.getX());
            int chunkZ = SectionPos.blockToSectionCoord(worldPosition.getZ());
            ChunkPos minChunk = new ChunkPos(chunkX - MAX_LOAD_RADIUS, chunkZ - MAX_LOAD_RADIUS);
            ChunkPos maxChunk = new ChunkPos(chunkX + MAX_LOAD_RADIUS, chunkZ + MAX_LOAD_RADIUS);
            return new AABB(
                  minChunk.getMinBlockX(),
                  level.getMinBuildHeight(),
                  minChunk.getMinBlockZ(),
                  maxChunk.getMaxBlockX() + 1,
                  level.getMaxBuildHeight(),
                  maxChunk.getMaxBlockZ() + 1
            );
        }
        return super.getRenderBoundingBox();
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
    public void writeSustainedData(CompoundTag dataMap) {
        byte[] chunksToLoad = new byte[MAX_LOAD_DIAMETER * MAX_LOAD_DIAMETER];
        for (int x = 0; x < MAX_LOAD_DIAMETER; x++) {
            for (int z = 0; z < MAX_LOAD_DIAMETER; z++) {
                chunksToLoad[x * MAX_LOAD_DIAMETER + z] = (byte) (isChunkLoadingAt(x, z) ? 1 : 0);
            }
        }
        dataMap.putByteArray(NBTConstants.STABILIZER_CHUNKS_TO_LOAD, chunksToLoad);
    }

    @Override
    public void readSustainedData(CompoundTag dataMap) {
        boolean changed = false;
        int lastChunksLoaded = chunksLoaded;
        byte[] chunksToLoad = dataMap.getByteArray(NBTConstants.STABILIZER_CHUNKS_TO_LOAD);
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

    //Methods relating to IComputerTile
    private int validateDimension(int val, boolean x) throws ComputerException {
        if (val < -MAX_LOAD_RADIUS || val > MAX_LOAD_RADIUS) {
            throw new ComputerException("%s offset '%d' is not in range, must be between %d and %d inclusive.", x ? "X" : "Z", val, -MAX_LOAD_RADIUS, MAX_LOAD_RADIUS);
        }
        //Shift up by max load radius as internally we act starting at zero
        return val + MAX_LOAD_RADIUS;
    }

    @ComputerMethod(nameOverride = "isChunkLoadingAt")
    private boolean computerIsChunkloadingAt(int x, int z) throws ComputerException {
        return isChunkLoadingAt(validateDimension(x, true), validateDimension(z, false));
    }

    @ComputerMethod(nameOverride = "toggleChunkLoadingAt")
    private void computerToggleChunkLoadingAt(int x, int z) throws ComputerException {
        validateSecurityIsPublic();
        toggleChunkLoadingAt(validateDimension(x, true), validateDimension(z, false));
    }

    @ComputerMethod(nameOverride = "setChunkLoadingAt")
    private void computerSetChunkLoadingAt(int x, int z, boolean load) throws ComputerException {
        validateSecurityIsPublic();
        if (setChunkLoadingAt(validateDimension(x, true), validateDimension(z, false), load)) {
            //If it changed we need to mark it as such and update various things
            setChanged(false);
            energyContainer.updateEnergyPerTick();
            //Refresh the chunks that are loaded as it has changed
            getChunkLoader().refreshChunkTickets();
        }
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
