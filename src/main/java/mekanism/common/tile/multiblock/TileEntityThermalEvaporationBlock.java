package mekanism.common.tile.multiblock;

import javax.annotation.Nonnull;
import mekanism.api.NBTConstants;
import mekanism.api.providers.IBlockProvider;
import mekanism.common.Mekanism;
import mekanism.common.content.evaporation.EvaporationMultiblockData;
import mekanism.common.inventory.container.MekanismContainer;
import mekanism.common.inventory.container.sync.dynamic.SyncMapper;
import mekanism.common.lib.multiblock.MultiblockManager;
import mekanism.common.registries.MekanismBlocks;
import mekanism.common.tile.prefab.TileEntityMultiblock;
import mekanism.common.util.NBTUtils;
import net.minecraft.block.Block;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.math.BlockPos;

public class TileEntityThermalEvaporationBlock extends TileEntityMultiblock<EvaporationMultiblockData> {

    public TileEntityThermalEvaporationBlock() {
        this(MekanismBlocks.THERMAL_EVAPORATION_BLOCK);
    }

    public TileEntityThermalEvaporationBlock(IBlockProvider provider) {
        super(provider);
    }

    @Override
    public void onNeighborChange(Block block, BlockPos neighborPos) {
        super.onNeighborChange(block, neighborPos);
        if (!isRemote()) {
            if (getMultiblock().isFormed()) {
                if (getMultiblock().isSolarSpot(neighborPos)) {
                    getMultiblock().updateSolars(getWorld());
                }
            }
        }
    }

    @Nonnull
    @Override
    public CompoundNBT getReducedUpdateTag() {
        CompoundNBT updateTag = super.getReducedUpdateTag();
        if (getMultiblock().isFormed() && isMaster) {
            updateTag.put(NBTConstants.FLUID_STORED, getMultiblock().inputTank.getFluid().writeToNBT(new CompoundNBT()));
            updateTag.putFloat(NBTConstants.SCALE, getMultiblock().prevScale);
        }
        return updateTag;
    }

    @Override
    public void handleUpdateTag(@Nonnull CompoundNBT tag) {
        super.handleUpdateTag(tag);
        if (isMaster && getMultiblock().isFormed()) {
            NBTUtils.setFluidStackIfPresent(tag, NBTConstants.FLUID_STORED, fluid -> getMultiblock().inputTank.setStack(fluid));
            NBTUtils.setFloatIfPresent(tag, NBTConstants.SCALE, scale -> getMultiblock().prevScale = scale);
        }
    }

    @Override
    public void addContainerTrackers(MekanismContainer container) {
        super.addContainerTrackers(container);
        SyncMapper.setup(container, getMultiblock().getClass(), this::getMultiblock, "stats");
    }

    @Override
    public EvaporationMultiblockData createMultiblock() {
        return new EvaporationMultiblockData(this);
    }

    @Override
    public MultiblockManager<EvaporationMultiblockData> getManager() {
        return Mekanism.evaporationManager;
    }
}