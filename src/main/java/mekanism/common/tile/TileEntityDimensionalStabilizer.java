package mekanism.common.tile;

import java.util.HashSet;
import java.util.Set;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import mekanism.api.Action;
import mekanism.api.AutomationType;
import mekanism.api.IContentsListener;
import mekanism.api.NBTConstants;
import mekanism.api.RelativeSide;
import mekanism.api.math.FloatingLong;
import mekanism.common.capabilities.energy.item.FixedUsageEnergyContainer;
import mekanism.common.capabilities.holder.energy.EnergyContainerHelper;
import mekanism.common.capabilities.holder.energy.IEnergyContainerHolder;
import mekanism.common.capabilities.holder.slot.IInventorySlotHolder;
import mekanism.common.capabilities.holder.slot.InventorySlotHelper;
import mekanism.common.config.MekanismConfig;
import mekanism.common.integration.computer.SpecialComputerMethodWrapper.ComputerIInventorySlotWrapper;
import mekanism.common.integration.computer.annotation.WrappingComputerMethod;
import mekanism.common.inventory.slot.EnergyInventorySlot;
import mekanism.common.lib.chunkloading.IChunkLoader;
import mekanism.common.registries.MekanismBlocks;
import mekanism.common.tile.base.SubstanceType;
import mekanism.common.tile.base.TileEntityMekanism;
import mekanism.common.tile.component.TileComponentChunkLoader;
import mekanism.common.util.MekanismUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.state.BlockState;

public class TileEntityDimensionalStabilizer extends TileEntityMekanism implements IChunkLoader {

    public static final int MAX_LOAD_RADIUS = 2;
    public static final int MAX_LOAD_DIAMETER = 2 * MAX_LOAD_RADIUS + 1;

    protected final ChunkLoader chunkLoaderComponent;
    protected final boolean[][] loadingChunks;

    protected boolean chunkloading = false;

    protected FixedUsageEnergyContainer<TileEntityDimensionalStabilizer> energyContainer;
    @WrappingComputerMethod(wrapper = ComputerIInventorySlotWrapper.class, methodNames = "getEnergyItem")
    private EnergyInventorySlot energySlot;

    public TileEntityDimensionalStabilizer(BlockPos pos, BlockState state) {
        super(MekanismBlocks.DIMENSIONAL_STABILIZER, pos, state);

        chunkLoaderComponent = new ChunkLoader(this);
        loadingChunks = new boolean[MAX_LOAD_DIAMETER][MAX_LOAD_DIAMETER];
        loadingChunks[MAX_LOAD_RADIUS][MAX_LOAD_RADIUS] = true;
    }

    public boolean isChunkloadingAt(int x, int z) {
        return loadingChunks[x][z];
    }

    public void toggleChunkloadingAt(int x, int z) {
        loadingChunks[x][z] = !loadingChunks[x][z];
        setChanged(false);
    }

    @Nonnull
    @Override
    protected IEnergyContainerHolder getInitialEnergyContainers(IContentsListener listener) {
        EnergyContainerHelper builder = EnergyContainerHelper.forSide(this::getDirection);
        builder.addContainer(energyContainer = FixedUsageEnergyContainer.input(this, listener));
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
        if (MekanismUtils.canFunction(this)) {
            FloatingLong energyPerTick = energyContainer.getEnergyPerTick();
            if (energyContainer.extract(energyPerTick, Action.SIMULATE, AutomationType.INTERNAL).equals(energyPerTick)) {
                energyContainer.extract(energyPerTick, Action.EXECUTE, AutomationType.INTERNAL);
                setChunkloading(true);
            } else {
                setChunkloading(false);
            }
        }
    }

    private void setChunkloading(boolean value) {
        boolean old = chunkloading;
        chunkloading = value;
        if (old != chunkloading) {
            setChanged(true);
        }
    }

    @Override
    public TileComponentChunkLoader<TileEntityDimensionalStabilizer> getChunkLoader() {
        return chunkLoaderComponent;
    }

    @Override
    public Set<ChunkPos> getChunkSet() {
        Set<ChunkPos> chunkSet = new HashSet<>();
        for (int z = -MAX_LOAD_RADIUS; z <= MAX_LOAD_RADIUS; ++z) {
            for (int x = -MAX_LOAD_RADIUS; x <= MAX_LOAD_RADIUS; ++x) {
                if (loadingChunks[x + MAX_LOAD_RADIUS][z + MAX_LOAD_RADIUS]) {
                    chunkSet.add(new ChunkPos(
                          SectionPos.blockToSectionCoord(worldPosition.getX()) + x,
                          SectionPos.blockToSectionCoord(worldPosition.getZ()) + z)
                    );
                }
            }
        }

        return chunkSet;
    }

    @Override
    public int getRedstoneLevel() {
        return chunkloading ? 15 : 0;
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
    protected void addGeneralPersistentData(@Nonnull CompoundTag nbtTags) {
        super.addGeneralPersistentData(nbtTags);
        byte[] chunksToLoad = new byte[MAX_LOAD_DIAMETER * MAX_LOAD_DIAMETER];
        for (int z = 0; z < MAX_LOAD_DIAMETER; ++z) {
            for (int x = 0; x < MAX_LOAD_DIAMETER; ++x) {
                chunksToLoad[z * MAX_LOAD_DIAMETER + x] = (byte) (loadingChunks[x][z] ? 1 : 0);
            }
        }
        nbtTags.putByteArray(NBTConstants.STABILIZER_CHUNKS_TO_LOAD, chunksToLoad);
    }

    @Override
    protected void loadGeneralPersistentData(@Nonnull CompoundTag nbt) {
        super.loadGeneralPersistentData(nbt);
        byte[] chunksToLoad = nbt.getByteArray(NBTConstants.STABILIZER_CHUNKS_TO_LOAD);
        for (int z = 0; z < MAX_LOAD_DIAMETER; ++z) {
            for (int x = 0; x < MAX_LOAD_DIAMETER; ++x) {
                loadingChunks[x][z] = chunksToLoad[z * MAX_LOAD_DIAMETER + x] == 1;
            }
        }
    }

    @Override
    public void saveAdditional(@Nonnull CompoundTag nbtTags) {
        super.saveAdditional(nbtTags);
        nbtTags.putBoolean(NBTConstants.STABILIZER_CHUNKLOADING, chunkloading);
    }

    @Override
    public void load(@Nonnull CompoundTag nbt) {
        super.load(nbt);
        chunkloading = nbt.getBoolean(NBTConstants.STABILIZER_CHUNKLOADING);
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
            return MekanismConfig.general.allowChunkloading.get() && chunkloading;
        }
    }
}
