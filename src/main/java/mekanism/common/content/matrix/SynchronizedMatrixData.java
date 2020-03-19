package mekanism.common.content.matrix;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import mekanism.api.Coord4D;
import mekanism.api.energy.IEnergyContainer;
import mekanism.api.energy.IMekanismStrictEnergyHandler;
import mekanism.api.inventory.IInventorySlot;
import mekanism.common.inventory.container.slot.SlotOverlay;
import mekanism.common.inventory.slot.EnergyInventorySlot;
import mekanism.common.multiblock.SynchronizedData;
import mekanism.common.tile.TileEntityInductionCasing;
import mekanism.common.tile.TileEntityInductionCell;
import mekanism.common.tile.TileEntityInductionProvider;
import net.minecraft.util.Direction;

public class SynchronizedMatrixData extends SynchronizedData<SynchronizedMatrixData> implements IMekanismStrictEnergyHandler {

    @Nonnull
    private final List<IEnergyContainer> energyContainers;
    @Nonnull
    private final MatrixEnergyContainer energyContainer;

    private int clientProviders;
    private int clientCells;

    @Nonnull
    private final List<IInventorySlot> inventorySlots;
    @Nonnull
    public final EnergyInventorySlot energyInputSlot;
    @Nonnull
    public final EnergyInventorySlot energyOutputSlot;

    public SynchronizedMatrixData(TileEntityInductionCasing tile) {
        energyContainers = Collections.singletonList(energyContainer = new MatrixEnergyContainer(tile));
        inventorySlots = new ArrayList<>();
        inventorySlots.add(energyInputSlot = EnergyInventorySlot.drain(energyContainer, this, 146, 20));
        inventorySlots.add(energyOutputSlot = EnergyInventorySlot.fillOrConvert(energyContainer, tile::getWorld, this, 146, 51));
        energyInputSlot.setSlotOverlay(SlotOverlay.PLUS);
        energyOutputSlot.setSlotOverlay(SlotOverlay.MINUS);
    }

    @Nonnull
    @Override
    public List<IInventorySlot> getInventorySlots(@Nullable Direction side) {
        return inventorySlots;
    }

    public void setInventoryData(@Nonnull List<IInventorySlot> toCopy) {
        for (int i = 0; i < toCopy.size(); i++) {
            if (i < inventorySlots.size()) {
                //Copy it via NBT to ensure that we set it using the "unsafe" method in case there is a problem with the types somehow
                inventorySlots.get(i).deserializeNBT(toCopy.get(i).serializeNBT());
            }
        }
    }

    public void addCell(Coord4D coord, TileEntityInductionCell cell) {
        energyContainer.addCell(coord, cell);
    }

    public void addProvider(Coord4D coord, TileEntityInductionProvider provider) {
        energyContainer.addProvider(coord, provider);
    }

    @Nonnull
    public MatrixEnergyContainer getEnergyContainer() {
        return energyContainer;
    }

    public double getEnergy() {
        return energyContainer.getEnergy();
    }

    public void tick() {
        energyContainer.tick();
    }

    public double getStorageCap() {
        return energyContainer.getMaxEnergy();
    }

    public double getTransferCap() {
        return transferCap;
    }

    public double getLastInput() {
        return lastInput;
    }

    public double getLastOutput() {
        return lastOutput;
    }

    public int getCellCount() {
        return cells.isEmpty() ? clientCells : cells.size();
    }

    public int getProviderCount() {
        return providers.isEmpty() ? clientProviders : providers.size();
    }

    /**
     * @apiNote Only call this from the client when syncing values
     */
    public void setCachedTotal(double cachedTotal) {
        this.cachedTotal = cachedTotal;
    }

    /**
     * @apiNote Only call this from the client when syncing values
     */
    public void setStorageCap(double storageCap) {
        this.storageCap = storageCap;
    }

    /**
     * @apiNote Only call this from the client when syncing values
     */
    public void setTransferCap(double transferCap) {
        this.transferCap = transferCap;
    }

    /**
     * @apiNote Only call this from the client when syncing values
     */
    public void setLastInput(double lastInput) {
        this.lastInput = lastInput;
    }

    /**
     * @apiNote Only call this from the client when syncing values
     */
    public void setLastOutput(double lastOutput) {
        this.lastOutput = lastOutput;
    }

    /**
     * @apiNote Only call this from the client when syncing values
     */
    public void setClientCells(int clientCells) {
        this.clientCells = clientCells;
    }

    /**
     * @apiNote Only call this from the client when syncing values
     */
    public void setClientProviders(int clientProviders) {
        this.clientProviders = clientProviders;
    }

    @Override
    public List<IEnergyContainer> getEnergyContainers(@Nullable Direction side) {
        return energyContainers;
    }
}