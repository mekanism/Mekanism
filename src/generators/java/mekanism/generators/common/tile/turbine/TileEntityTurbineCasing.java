package mekanism.generators.common.tile.turbine;

import javax.annotation.Nonnull;
import mekanism.api.providers.IBlockProvider;
import mekanism.common.lib.multiblock.MultiblockManager;
import mekanism.common.tile.interfaces.IHasGasMode;
import mekanism.common.tile.prefab.TileEntityMultiblock;
import mekanism.generators.common.MekanismGenerators;
import mekanism.generators.common.content.turbine.TurbineMultiblockData;
import mekanism.generators.common.registries.GeneratorsBlocks;

public class TileEntityTurbineCasing extends TileEntityMultiblock<TurbineMultiblockData> implements IHasGasMode {

    public TileEntityTurbineCasing() {
        this(GeneratorsBlocks.TURBINE_CASING);
    }

    public TileEntityTurbineCasing(IBlockProvider blockProvider) {
        super(blockProvider);
    }

    @Override
    public void nextMode(int tank) {
        if (tank == 0) {
            TurbineMultiblockData multiblock = getMultiblock();
            multiblock.dumpMode = multiblock.dumpMode.getNext();
        }
    }

    @Nonnull
    @Override
    public TurbineMultiblockData createMultiblock() {
        return new TurbineMultiblockData(this);
    }

    @Override
    public MultiblockManager<TurbineMultiblockData> getManager() {
        return MekanismGenerators.turbineManager;
    }
}