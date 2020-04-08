package mekanism.generators.common.tile.fission;

import mekanism.common.multiblock.MultiblockCache;
import mekanism.common.multiblock.MultiblockManager;
import mekanism.common.multiblock.UpdateProtocol;
import mekanism.common.tile.TileEntityMultiblock;
import mekanism.generators.common.MekanismGenerators;
import mekanism.generators.common.content.fission.FissionReactorCache;
import mekanism.generators.common.content.fission.FissionReactorUpdateProtocol;
import mekanism.generators.common.content.fission.SynchronizedFissionReactorData;
import mekanism.generators.common.registries.GeneratorsBlocks;

public class TileEntityFissionReactorCasing extends TileEntityMultiblock<SynchronizedFissionReactorData> {

    public TileEntityFissionReactorCasing() {
        super(GeneratorsBlocks.FISSION_REACTOR_CASING);
    }

    @Override
    protected SynchronizedFissionReactorData getNewStructure() {
        return new SynchronizedFissionReactorData(this);
    }

    @Override
    public MultiblockCache<SynchronizedFissionReactorData> getNewCache() {
        return new FissionReactorCache();
    }

    @Override
    protected UpdateProtocol<SynchronizedFissionReactorData> getProtocol() {
        return new FissionReactorUpdateProtocol(this);
    }

    @Override
    public MultiblockManager<SynchronizedFissionReactorData> getManager() {
        return MekanismGenerators.fissionReactorManager;
    }
}
