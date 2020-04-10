package mekanism.common.content.matrix;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import mekanism.api.Coord4D;
import mekanism.api.math.FloatingLong;
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

    private FloatingLong clientLastOutput = FloatingLong.ZERO;
    private FloatingLong clientLastInput = FloatingLong.ZERO;
    private FloatingLong clientEnergy = FloatingLong.ZERO;
    private FloatingLong clientMaxTransfer = FloatingLong.ZERO;
    private FloatingLong clientMaxEnergy = FloatingLong.ZERO;
    private int clientProviders;
    private int clientCells;

    @Nonnull
    private final List<IInventorySlot> inventorySlots;
    @Nonnull
    public final EnergyInventorySlot energyInputSlot;
    @Nonnull
    public final EnergyInventorySlot energyOutputSlot;

    private final boolean remote;

    public SynchronizedMatrixData(TileEntityInductionCasing tile) {
        remote = tile.isRemote();
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

    public FloatingLong getEnergy() {
        return remote ? clientEnergy : energyContainer.getEnergy();
    }

    public void tick() {
        energyContainer.tick();
    }

    public void invalidate() {
        energyContainer.invalidate();
    }

    public FloatingLong getStorageCap() {
        return remote ? clientMaxEnergy : energyContainer.getMaxEnergy();
    }

    public FloatingLong getTransferCap() {
        return remote ? clientMaxTransfer : energyContainer.getMaxTransfer();
    }

    public FloatingLong getLastInput() {
        return remote ? clientLastInput : energyContainer.getLastInput();
    }

    public FloatingLong getLastOutput() {
        return remote ? clientLastOutput : energyContainer.getLastOutput();
    }

    public int getCellCount() {
        return remote ? clientCells : energyContainer.getCells();
    }

    public int getProviderCount() {
        return remote ? clientProviders : energyContainer.getProviders();
    }

    public void setClientEnergy(FloatingLong cachedTotal) {
        this.clientEnergy = cachedTotal;
    }

    public void setClientMaxEnergy(FloatingLong storageCap) {
        this.clientMaxEnergy = storageCap;
    }

    public void setClientMaxTransfer(FloatingLong transferCap) {
        this.clientMaxTransfer = transferCap;
    }

    public void setClientLastInput(FloatingLong lastInput) {
        this.clientLastInput = lastInput;
    }

    public void setClientLastOutput(FloatingLong lastOutput) {
        this.clientLastOutput = lastOutput;
    }

    public void setClientCells(int cells) {
        this.clientCells = cells;
    }

    public void setClientProviders(int providers) {
        this.clientProviders = providers;
    }

    @Nonnull
    @Override
    public List<IEnergyContainer> getEnergyContainers(@Nullable Direction side) {
        return energyContainers;
    }
}