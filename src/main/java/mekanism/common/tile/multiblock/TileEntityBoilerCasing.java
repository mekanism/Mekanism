package mekanism.common.tile.multiblock;

import javax.annotation.Nonnull;
import mekanism.api.providers.IBlockProvider;
import mekanism.common.Mekanism;
import mekanism.common.capabilities.holder.heat.IHeatCapacitorHolder;
import mekanism.common.content.boiler.BoilerMultiblockData;
import mekanism.common.lib.multiblock.MultiblockManager;
import mekanism.common.registries.MekanismBlocks;
import mekanism.common.tile.base.SubstanceType;
import mekanism.common.tile.prefab.TileEntityMultiblock;

public class TileEntityBoilerCasing extends TileEntityMultiblock<BoilerMultiblockData> {

    public TileEntityBoilerCasing() {
        this(MekanismBlocks.BOILER_CASING);
    }

    public TileEntityBoilerCasing(IBlockProvider blockProvider) {
        super(blockProvider);
    }

    @Nonnull
    @Override
    public BoilerMultiblockData createMultiblock() {
        return new BoilerMultiblockData(this);
    }

    @Override
    public MultiblockManager<BoilerMultiblockData> getManager() {
        return Mekanism.boilerManager;
    }

    @Nonnull
    @Override
    protected IHeatCapacitorHolder getInitialHeatCapacitors() {
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