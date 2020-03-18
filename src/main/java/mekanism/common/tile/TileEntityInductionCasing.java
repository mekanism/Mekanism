package mekanism.common.tile;

import java.util.List;
import javax.annotation.Nonnull;
import mekanism.api.energy.IStrictEnergyStorage;
import mekanism.api.inventory.IInventorySlot;
import mekanism.api.providers.IBlockProvider;
import mekanism.common.Mekanism;
import mekanism.common.content.matrix.MatrixCache;
import mekanism.common.content.matrix.MatrixUpdateProtocol;
import mekanism.common.content.matrix.SynchronizedMatrixData;
import mekanism.common.inventory.container.MekanismContainer;
import mekanism.common.inventory.container.sync.SyncableDouble;
import mekanism.common.inventory.container.sync.SyncableInt;
import mekanism.common.inventory.slot.EnergyInventorySlot;
import mekanism.common.multiblock.MultiblockManager;
import mekanism.common.registries.MekanismBlocks;
import mekanism.common.registries.MekanismTileEntityTypes;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.items.CapabilityItemHandler;

public class TileEntityInductionCasing extends TileEntityMultiblock<SynchronizedMatrixData> implements IStrictEnergyStorage {

    public TileEntityInductionCasing() {
        this(MekanismBlocks.INDUCTION_CASING);
    }

    public TileEntityInductionCasing(IBlockProvider blockProvider) {
        super(blockProvider);
    }

    @Override
    protected void onUpdateServer() {
        super.onUpdateServer();
        if (structure != null && isRendering) {
            structure.tick(getWorld());
            List<IInventorySlot> inventorySlots = getInventorySlots(null);
            ((EnergyInventorySlot) inventorySlots.get(0)).drainContainer();
            ((EnergyInventorySlot) inventorySlots.get(1)).fillContainerOrConvert();
        }
    }

    @Override
    public boolean isCapabilityDisabled(@Nonnull Capability<?> capability, Direction side) {
        //Disable item handler caps if we are the induction casing, don't disable it for the subclassed port though
        if (capability == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY && getType() == MekanismTileEntityTypes.INDUCTION_CASING.getTileEntityType()) {
            return true;
        }
        return super.isCapabilityDisabled(capability, side);
    }

    @Override
    public ActionResultType onActivate(PlayerEntity player, Hand hand, ItemStack stack) {
        if (structure == null) {
            return ActionResultType.PASS;
        }
        return openGui(player);
    }

    @Nonnull
    @Override
    protected SynchronizedMatrixData getNewStructure() {
        return new SynchronizedMatrixData();
    }

    @Override
    public MatrixCache getNewCache() {
        return new MatrixCache();
    }

    @Override
    protected MatrixUpdateProtocol getProtocol() {
        return new MatrixUpdateProtocol(this);
    }

    @Override
    public MultiblockManager<SynchronizedMatrixData> getManager() {
        return Mekanism.matrixManager;
    }

    @Override
    public double getEnergy() {
        //Uses post queue as that is the actual total we just haven't saved it yet
        return structure != null ? structure.getEnergyPostQueue() : 0;
    }

    @Override
    public void setEnergy(double energy) {
        if (structure != null) {
            structure.queueSetEnergy(Math.max(Math.min(energy, getMaxEnergy()), 0));
        }
    }

    public double addEnergy(double energy, boolean simulate) {
        return structure != null ? structure.queueEnergyAddition(energy, simulate) : 0;
    }

    public double removeEnergy(double energy, boolean simulate) {
        return structure != null ? structure.queueEnergyRemoval(energy, simulate) : 0;
    }

    @Override
    public double getMaxEnergy() {
        return structure != null ? structure.getStorageCap() : 0;
    }

    public double getLastInput() {
        return structure != null ? structure.getLastInput() : 0;
    }

    public double getLastOutput() {
        return structure != null ? structure.getLastOutput() : 0;
    }

    public double getTransferCap() {
        return structure != null ? structure.getTransferCap() : 0;
    }

    public int getCellCount() {
        return structure != null ? structure.getCellCount() : 0;
    }

    public int getProviderCount() {
        return structure != null ? structure.getProviderCount() : 0;
    }

    @Override
    public void addContainerTrackers(MekanismContainer container) {
        super.addContainerTrackers(container);
        container.track(SyncableDouble.create(() -> structure == null ? 0 : structure.getEnergy(), value -> {
            if (structure != null) {
                structure.setCachedTotal(value);
            }
        }));
        container.track(SyncableDouble.create(() -> structure == null ? 0 : structure.getStorageCap(), value -> {
            if (structure != null) {
                structure.setStorageCap(value);
            }
        }));
        container.track(SyncableDouble.create(() -> structure == null ? 0 : structure.getLastInput(), value -> {
            if (structure != null) {
                structure.setLastInput(value);
            }
        }));
        container.track(SyncableDouble.create(() -> structure == null ? 0 : structure.getLastOutput(), value -> {
            if (structure != null) {
                structure.setLastOutput(value);
            }
        }));
    }

    public void addStatsTabContainerTrackers(MekanismContainer container) {
        container.track(SyncableDouble.create(() -> structure == null ? 0 : structure.getTransferCap(), value -> {
            if (structure != null) {
                structure.setTransferCap(value);
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