package mekanism.common.tile;

import javax.annotation.Nonnull;
import mekanism.api.math.FloatingLong;
import mekanism.api.providers.IBlockProvider;
import mekanism.common.Mekanism;
import mekanism.common.content.matrix.MatrixUpdateProtocol;
import mekanism.common.content.matrix.SynchronizedMatrixData;
import mekanism.common.inventory.container.MekanismContainer;
import mekanism.common.inventory.container.sync.SyncableFloatingLong;
import mekanism.common.inventory.container.sync.SyncableInt;
import mekanism.common.multiblock.MultiblockManager;
import mekanism.common.registries.MekanismBlocks;
import mekanism.common.tile.prefab.TileEntityMultiblock;
import net.minecraftforge.items.CapabilityItemHandler;

public class TileEntityInductionCasing extends TileEntityMultiblock<SynchronizedMatrixData> {

    public TileEntityInductionCasing() {
        this(MekanismBlocks.INDUCTION_CASING);
        //Disable item handler caps if we are the induction casing, don't disable it for the subclassed port though
        addDisabledCapabilities(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY);
    }

    public TileEntityInductionCasing(IBlockProvider blockProvider) {
        super(blockProvider);
    }

    @Override
    protected void onUpdateServer() {
        super.onUpdateServer();
        if (structure != null && isRendering) {
            //We tick the structure before adding/draining from the slots, so that we make sure they get
            // first "pickings" at attempting to get or give power, without having to worry about the
            // rate limit of the structure being used up by the ports
            structure.tick();
            structure.energyInputSlot.drainContainer();
            structure.energyOutputSlot.fillContainerOrConvert();
        }
    }

    @Nonnull
    @Override
    public SynchronizedMatrixData getNewStructure() {
        return new SynchronizedMatrixData(this);
    }

    @Override
    protected MatrixUpdateProtocol getProtocol() {
        return new MatrixUpdateProtocol(this);
    }

    @Override
    public MultiblockManager<SynchronizedMatrixData> getManager() {
        return Mekanism.matrixManager;
    }

    public FloatingLong getEnergy() {
        //Uses post queue as that is the actual total we just haven't saved it yet
        return structure == null ? FloatingLong.ZERO : structure.getEnergy();
    }

    public FloatingLong getMaxEnergy() {
        return structure == null ? FloatingLong.ZERO : structure.getStorageCap();
    }

    public FloatingLong getLastInput() {
        return structure == null ? FloatingLong.ZERO : structure.getLastInput();
    }

    public FloatingLong getLastOutput() {
        return structure == null ? FloatingLong.ZERO : structure.getLastOutput();
    }

    public FloatingLong getTransferCap() {
        return structure == null ? FloatingLong.ZERO : structure.getTransferCap();
    }

    public int getCellCount() {
        return structure == null ? 0 : structure.getCellCount();
    }

    public int getProviderCount() {
        return structure == null ? 0 : structure.getProviderCount();
    }

    @Override
    public void addContainerTrackers(MekanismContainer container) {
        super.addContainerTrackers(container);
        container.track(SyncableFloatingLong.create(this::getEnergy, value -> {
            if (structure != null) {
                structure.setClientEnergy(value);
            }
        }));
        container.track(SyncableFloatingLong.create(this::getMaxEnergy, value -> {
            if (structure != null) {
                structure.setClientMaxEnergy(value);
            }
        }));
        container.track(SyncableFloatingLong.create(this::getLastInput, value -> {
            if (structure != null) {
                structure.setClientLastInput(value);
            }
        }));
        container.track(SyncableFloatingLong.create(this::getLastOutput, value -> {
            if (structure != null) {
                structure.setClientLastOutput(value);
            }
        }));
    }

    public void addStatsTabContainerTrackers(MekanismContainer container) {
        container.track(SyncableFloatingLong.create(() -> structure == null ? FloatingLong.ZERO : structure.getTransferCap(), value -> {
            if (structure != null) {
                structure.setClientMaxTransfer(value);
            }
        }));
        container.track(SyncableInt.create(() -> structure == null ? 0 : structure.volHeight, value -> {
            if (structure != null) {
                structure.volHeight = value;
            }
        }));
        container.track(SyncableInt.create(() -> structure == null ? 0 : structure.volWidth, value -> {
            if (structure != null) {
                structure.volWidth = value;
            }
        }));
        container.track(SyncableInt.create(() -> structure == null ? 0 : structure.volLength, value -> {
            if (structure != null) {
                structure.volLength = value;
            }
        }));
        container.track(SyncableInt.create(() -> structure == null ? 0 : structure.getCellCount(), value -> {
            if (structure != null) {
                structure.setClientCells(value);
            }
        }));
        container.track(SyncableInt.create(() -> structure == null ? 0 : structure.getProviderCount(), value -> {
            if (structure != null) {
                structure.setClientProviders(value);
            }
        }));
    }
}