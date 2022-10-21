package mekanism.common.tile.multiblock;

import mekanism.api.IContentsListener;
import mekanism.api.providers.IBlockProvider;
import mekanism.common.Mekanism;
import mekanism.common.capabilities.heat.CachedAmbientTemperature;
import mekanism.common.capabilities.holder.heat.IHeatCapacitorHolder;
import mekanism.common.content.boiler.BoilerMultiblockData;
import mekanism.common.lib.multiblock.MultiblockManager;
import mekanism.common.registries.MekanismBlocks;
import mekanism.common.tile.base.SubstanceType;
import mekanism.common.tile.prefab.TileEntityMultiblock;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;

public class TileEntityBoilerCasing extends TileEntityMultiblock<BoilerMultiblockData> {

    public TileEntityBoilerCasing(BlockPos pos, BlockState state) {
        this(MekanismBlocks.BOILER_CASING, pos, state);
    }

    public TileEntityBoilerCasing(IBlockProvider blockProvider, BlockPos pos, BlockState state) {
        super(blockProvider, pos, state);
    }

    @NotNull
    @Override
    public BoilerMultiblockData createMultiblock() {
        return new BoilerMultiblockData(this);
    }

    @Override
    public MultiblockManager<BoilerMultiblockData> getManager() {
        return Mekanism.boilerManager;
    }

    @NotNull
    @Override
    protected IHeatCapacitorHolder getInitialHeatCapacitors(IContentsListener listener, CachedAmbientTemperature ambientTemperature) {
        return side -> getMultiblock().getHeatCapacitors(side);
    }

    @Override
    public boolean persists(SubstanceType type) {
        //Do not handle heat when it comes to syncing it/saving this tile to disk
        if (type == SubstanceType.HEAT) {
            return false;
        }
        return super.persists(type);
    }
}