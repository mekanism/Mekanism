package mekanism.common.content.matrix;

import java.util.ArrayList;
import java.util.List;
import mekanism.common.integration.computer.SpecialComputerMethodWrapper.ComputerIInventorySlotWrapper;
import mekanism.common.integration.computer.annotation.ComputerMethod;
import mekanism.common.integration.computer.annotation.WrappingComputerMethod;
import mekanism.common.inventory.container.slot.SlotOverlay;
import mekanism.common.inventory.container.sync.dynamic.ContainerSync;
import mekanism.common.inventory.slot.EnergyInventorySlot;
import mekanism.common.lib.multiblock.IValveHandler.ValveData;
import mekanism.common.lib.multiblock.MultiblockCache.CacheSubstance;
import mekanism.common.lib.multiblock.MultiblockData;
import mekanism.common.lib.multiblock.Structure;
import mekanism.common.tile.multiblock.TileEntityInductionCasing;
import mekanism.common.tile.multiblock.TileEntityInductionCell;
import mekanism.common.tile.multiblock.TileEntityInductionPort;
import mekanism.common.tile.multiblock.TileEntityInductionProvider;
import mekanism.common.util.CableUtils;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.WorldUtils;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;

public class MatrixMultiblockData extends MultiblockData {

    public static final String STATS_TAB = "stats";

    private final List<EnergyOutputTarget> energyOutputTargets = new ArrayList<>();
    @NotNull
    private final MatrixEnergyContainer energyContainer;

    @ContainerSync(getter = "getLastOutput")
    private long clientLastOutput = 0L;
    @ContainerSync(getter = "getLastInput")
    private long clientLastInput = 0L;

    @ContainerSync(getter = "getEnergy")
    private long clientEnergy = 0L;

    @ContainerSync(tags = STATS_TAB, getter = "getTransferCap")
    private long clientMaxTransfer = 0L;

    @ContainerSync(getter = "getStorageCap")
    private long clientMaxEnergy = 0L;

    @ContainerSync(tags = STATS_TAB, getter = "getProviderCount")
    private int clientProviders;
    @ContainerSync(tags = STATS_TAB, getter = "getCellCount")
    private int clientCells;

    @NotNull
    @WrappingComputerMethod(wrapper = ComputerIInventorySlotWrapper.class, methodNames = "getInputItem", docPlaceholder = "input slot")
    final EnergyInventorySlot energyInputSlot;
    @NotNull
    @WrappingComputerMethod(wrapper = ComputerIInventorySlotWrapper.class, methodNames = "getOutputItem", docPlaceholder = "output slot")
    final EnergyInventorySlot energyOutputSlot;

    public MatrixMultiblockData(TileEntityInductionCasing tile) {
        super(tile);
        energyContainers.add(energyContainer = new MatrixEnergyContainer(this));
        inventorySlots.add(energyInputSlot = EnergyInventorySlot.drain(energyContainer, this, 146, 21));
        inventorySlots.add(energyOutputSlot = EnergyInventorySlot.fillOrConvert(energyContainer, tile::getLevel, this, 146, 51));
        energyInputSlot.setSlotOverlay(SlotOverlay.PLUS);
        energyOutputSlot.setSlotOverlay(SlotOverlay.MINUS);
    }

    @Override
    protected int getMultiblockRedstoneLevel() {
        return MekanismUtils.redstoneLevelFromContents(getEnergy(), getStorageCap());
    }

    @Override
    protected boolean shouldCap(CacheSubstance<?, ?> type) {
        return type != CacheSubstance.ENERGY;
    }

    public void addCell(TileEntityInductionCell cell) {
        energyContainer.addCell(cell.getBlockPos(), cell);
    }

    public void addProvider(TileEntityInductionProvider provider) {
        energyContainer.addProvider(provider.getBlockPos(), provider);
    }

    @NotNull
    public MatrixEnergyContainer getEnergyContainer() {
        return energyContainer;
    }

    public long getEnergy() {
        return isRemote() ? clientEnergy : energyContainer.getEnergy();
    }

    @Override
    public boolean tick(Level world) {
        boolean ret = super.tick(world);
        energyContainer.tick();
        // We tick the main energy container before adding/draining from the slots, so that we make sure
        // they get first "pickings" at attempting to get or give power, without having to worry about the
        // rate limit of the structure being used up by the ports
        energyInputSlot.drainContainer();
        energyOutputSlot.fillContainerOrConvert();
        if (!energyOutputTargets.isEmpty() && !energyContainer.isEmpty()) {
            CableUtils.emit(getActiveOutputs(energyOutputTargets), energyContainer, energyContainer.getMaxTransfer());
        }
        if (getLastInput() != 0L || getLastOutput() != 0L) {
            // If the stored energy changed, update the comparator
            markDirtyComparator(world);
        }
        return ret;
    }

    @Override
    public void remove(Level world, Structure oldStructure) {
        energyContainer.invalidate();
        super.remove(world, oldStructure);
    }

    @Override
    protected void updateEjectors(Level world) {
        energyOutputTargets.clear();
        for (ValveData valve : valves) {
            TileEntityInductionPort tile = WorldUtils.getTileEntity(TileEntityInductionPort.class, world, valve.location);
            if (tile != null) {
                tile.addEnergyTargetCapability(energyOutputTargets, valve.side);
            }
        }
    }

    public long getStorageCap() {
        return isRemote() ? clientMaxEnergy : energyContainer.getMaxEnergy();
    }

    @ComputerMethod
    public long getTransferCap() {
        return isRemote() ? clientMaxTransfer : energyContainer.getMaxTransfer();
    }

    @ComputerMethod
    public long getLastInput() {
        return isRemote() ? clientLastInput : energyContainer.getLastInput();
    }

    @ComputerMethod
    public long getLastOutput() {
        return isRemote() ? clientLastOutput : energyContainer.getLastOutput();
    }

    @ComputerMethod(nameOverride = "getInstalledCells")
    public int getCellCount() {
        return isRemote() ? clientCells : energyContainer.getCells();
    }

    @ComputerMethod(nameOverride = "getInstalledProviders")
    public int getProviderCount() {
        return isRemote() ? clientProviders : energyContainer.getProviders();
    }
}
