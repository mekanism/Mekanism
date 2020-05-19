package mekanism.generators.common.tile.fusion;

import javax.annotation.Nonnull;
import mekanism.api.NBTConstants;
import mekanism.api.providers.IBlockProvider;
import mekanism.common.inventory.container.MekanismContainer;
import mekanism.common.inventory.container.sync.dynamic.SyncMapper;
import mekanism.common.lib.multiblock.FormationProtocol;
import mekanism.common.lib.multiblock.IStructureValidator;
import mekanism.common.lib.multiblock.MultiblockManager;
import mekanism.common.tile.prefab.TileEntityMultiblock;
import mekanism.common.util.NBTUtils;
import mekanism.generators.common.MekanismGenerators;
import mekanism.generators.common.content.fusion.FusionReactorMultiblockData;
import mekanism.generators.common.content.fusion.FusionReactorStructureValidator;
import mekanism.generators.common.content.fusion.FusionReactorUpdateProtocol;
import mekanism.generators.common.registries.GeneratorsBlocks;
import net.minecraft.nbt.CompoundNBT;

public class TileEntityFusionReactorBlock extends TileEntityMultiblock<FusionReactorMultiblockData> {

    public TileEntityFusionReactorBlock() {
        this(GeneratorsBlocks.FUSION_REACTOR_FRAME);
    }

    public TileEntityFusionReactorBlock(IBlockProvider blockProvider) {
        super(blockProvider);
    }

    @Override
    public FusionReactorMultiblockData createMultiblock() {
        return new FusionReactorMultiblockData(this);
    }

    @Override
    public FormationProtocol<FusionReactorMultiblockData> getFormationProtocol() {
        return new FusionReactorUpdateProtocol(this);
    }

    @Override
    public MultiblockManager<FusionReactorMultiblockData> getManager() {
        return MekanismGenerators.fusionReactorManager;
    }

    @Override
    public IStructureValidator validateStructure() {
        return new FusionReactorStructureValidator(getStructure());
    }

    @Nonnull
    @Override
    public CompoundNBT getReducedUpdateTag() {
        CompoundNBT updateTag = super.getReducedUpdateTag();
        if (getMultiblock().isFormed() && isRendering) {
            updateTag.putDouble(NBTConstants.PLASMA_TEMP, getMultiblock().getPlasmaTemp());
            updateTag.putBoolean(NBTConstants.BURNING, getMultiblock().isBurning());
        }
        return updateTag;
    }

    @Override
    public void handleUpdateTag(@Nonnull CompoundNBT tag) {
        super.handleUpdateTag(tag);
        if (getMultiblock().isFormed() && isRendering) {
            NBTUtils.setDoubleIfPresent(tag, NBTConstants.PLASMA_TEMP, getMultiblock()::setLastPlasmaTemp);
            NBTUtils.setBooleanIfPresent(tag, NBTConstants.BURNING, getMultiblock()::setBurning);
        }
    }

    public void setInjectionRateFromPacket(int rate) {
        if (getMultiblock().isFormed()) {
            getMultiblock().setInjectionRate(Math.min(FusionReactorMultiblockData.MAX_INJECTION, Math.max(0, rate - (rate % 2))));
            markDirty(false);
        }
    }

    public void addFuelTabContainerTrackers(MekanismContainer container) {
        SyncMapper.setup(container, FusionReactorMultiblockData.class, () -> getMultiblock(), "fuel");
    }

    public void addHeatTabContainerTrackers(MekanismContainer container) {
        SyncMapper.setup(container, FusionReactorMultiblockData.class, () -> getMultiblock(), "heat");
    }
}