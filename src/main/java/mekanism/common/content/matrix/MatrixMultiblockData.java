package mekanism.common.content.matrix;

import javax.annotation.Nonnull;
import mekanism.api.math.FloatingLong;
import mekanism.common.integration.computer.SpecialComputerMethodWrapper.ComputerIInventorySlotWrapper;
import mekanism.common.integration.computer.annotation.ComputerMethod;
import mekanism.common.integration.computer.annotation.WrappingComputerMethod;
import mekanism.common.inventory.container.slot.SlotOverlay;
import mekanism.common.inventory.container.sync.dynamic.ContainerSync;
import mekanism.common.inventory.slot.EnergyInventorySlot;
import mekanism.common.lib.multiblock.MultiblockCache.CacheSubstance;
import mekanism.common.lib.multiblock.MultiblockData;
import mekanism.common.tile.multiblock.TileEntityInductionCasing;
import mekanism.common.tile.multiblock.TileEntityInductionCell;
import mekanism.common.tile.multiblock.TileEntityInductionProvider;
import mekanism.common.util.MekanismUtils;
import net.minecraft.world.World;

public class MatrixMultiblockData extends MultiblockData {

    @Nonnull
    private final MatrixEnergyContainer energyContainer;

    @ContainerSync(getter = "getLastOutput")
    private FloatingLong clientLastOutput = FloatingLong.ZERO;
    @ContainerSync(getter = "getLastInput")
    private FloatingLong clientLastInput = FloatingLong.ZERO;

    @ContainerSync(getter = "getEnergy")
    private FloatingLong clientEnergy = FloatingLong.ZERO;

    @ContainerSync(tags = "stats", getter = "getTransferCap")
    private FloatingLong clientMaxTransfer = FloatingLong.ZERO;

    @ContainerSync(getter = "getStorageCap")
    private FloatingLong clientMaxEnergy = FloatingLong.ZERO;

    @ContainerSync(tags = "stats", getter = "getProviderCount")
    private int clientProviders;
    @ContainerSync(tags = "stats", getter = "getCellCount")
    private int clientCells;

    @Nonnull
    @WrappingComputerMethod(wrapper = ComputerIInventorySlotWrapper.class, methodNames = "getInputItem")
    private final EnergyInventorySlot energyInputSlot;
    @Nonnull
    @WrappingComputerMethod(wrapper = ComputerIInventorySlotWrapper.class, methodNames = "getOutputItem")
    private final EnergyInventorySlot energyOutputSlot;

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

    @Nonnull
    public MatrixEnergyContainer getEnergyContainer() {
        return energyContainer;
    }

    public FloatingLong getEnergy() {
        return isRemote() ? clientEnergy : energyContainer.getEnergy();
    }

    @Override
    public boolean tick(World world) {
        boolean ret = super.tick(world);
        energyContainer.tick();
        // We tick the main energy container before adding/draining from the slots, so that we make sure
        // they get first "pickings" at attempting to get or give power, without having to worry about the
        // rate limit of the structure being used up by the ports
        energyInputSlot.drainContainer();
        energyOutputSlot.fillContainerOrConvert();
        if (!getLastInput().isZero() || !getLastOutput().isZero()) {
            // If the stored energy changed, update the comparator
            markDirtyComparator(world);
        }
        return ret;
    }

    @Override
    public void remove(World world) {
        energyContainer.invalidate();
        super.remove(world);
    }

    public FloatingLong getStorageCap() {
        return isRemote() ? clientMaxEnergy : energyContainer.getMaxEnergy();
    }

    @ComputerMethod
    public FloatingLong getTransferCap() {
        return isRemote() ? clientMaxTransfer : energyContainer.getMaxTransfer();
    }

    @ComputerMethod
    public FloatingLong getLastInput() {
        return isRemote() ? clientLastInput : energyContainer.getLastInput();
    }

    @ComputerMethod
    public FloatingLong getLastOutput() {
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