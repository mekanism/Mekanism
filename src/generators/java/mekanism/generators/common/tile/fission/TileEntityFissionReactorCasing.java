package mekanism.generators.common.tile.fission;

import mekanism.api.Coord4D;
import mekanism.api.providers.IBlockProvider;
import mekanism.common.capabilities.heat.ITileHeatHandler;
import mekanism.common.multiblock.MultiblockManager;
import mekanism.common.multiblock.UpdateProtocol;
import mekanism.common.tile.TileEntityMultiblock;
import mekanism.generators.common.MekanismGenerators;
import mekanism.generators.common.content.fission.FissionReactorUpdateProtocol;
import mekanism.generators.common.content.fission.SynchronizedFissionReactorData;
import mekanism.generators.common.registries.GeneratorsBlocks;
import net.minecraft.tileentity.TileEntity;

public class TileEntityFissionReactorCasing extends TileEntityMultiblock<SynchronizedFissionReactorData> {

    public float prevWaterScale;
    public float prevSteamScale;

    public TileEntityFissionReactorCasing() {
        super(GeneratorsBlocks.FISSION_REACTOR_CASING);
    }

    public TileEntityFissionReactorCasing(IBlockProvider blockProvider) {
        super(blockProvider);
    }

    @Override
    protected void onUpdateServer() {
        super.onUpdateServer();
        if (structure != null && isRendering) {
            boolean needsPacket = false;
            // burn reactor fuel, create energy
            if (structure.active) {
                structure.burnFuel();
            }
            // handle coolant heating (water -> steam)
            structure.handleCoolant();
            // external heat dissipation
            structure.lastEnvironmentLoss = structure.simulateEnvironment();
            // adjacent heat transfer
            structure.lastAdjacentLoss = 0;
            for (Coord4D coord : structure.portLocations) {
                TileEntity tile = world.getTileEntity(coord.getPos());
                if (tile instanceof ITileHeatHandler) {
                    structure.lastAdjacentLoss += ((ITileHeatHandler) tile).simulateAdjacent();
                }
            }
            // update temperature
            structure.update(null);
            structure.handleDamage();

            if (needsPacket) {
                sendUpdatePacket();
            }
            markDirty(false);
        }
    }

    @Override
    public SynchronizedFissionReactorData getNewStructure() {
        return new SynchronizedFissionReactorData(this);
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
