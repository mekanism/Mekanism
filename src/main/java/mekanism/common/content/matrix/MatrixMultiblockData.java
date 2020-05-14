package mekanism.common.content.matrix;

import java.util.function.BooleanSupplier;
import javax.annotation.Nonnull;
import mekanism.api.Coord4D;
import mekanism.api.math.FloatingLong;
import mekanism.common.inventory.container.slot.SlotOverlay;
import mekanism.common.inventory.container.sync.dynamic.ContainerSync;
import mekanism.common.inventory.slot.EnergyInventorySlot;
import mekanism.common.multiblock.MultiblockData;
import mekanism.common.tile.TileEntityInductionCasing;
import mekanism.common.tile.TileEntityInductionCell;
import mekanism.common.tile.TileEntityInductionProvider;
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

    @ContainerSync(tag = "stats", getter = "getTransferCap")
    private FloatingLong clientMaxTransfer = FloatingLong.ZERO;

    @ContainerSync(getter = "getStorageCap")
    private FloatingLong clientMaxEnergy = FloatingLong.ZERO;

    @ContainerSync(tag = "stats", getter = "getProviderCount")
    private int clientProviders;
    @ContainerSync(tag = "stats", getter = "getCellCount")
    private int clientCells;

    @Nonnull
    public final EnergyInventorySlot energyInputSlot;
    @Nonnull
    public final EnergyInventorySlot energyOutputSlot;

    private final BooleanSupplier remote;

    public MatrixMultiblockData(TileEntityInductionCasing tile) {
        remote = () -> tile.isRemote();
        energyContainers.add(energyContainer = new MatrixEnergyContainer(this));
        inventorySlots.add(energyInputSlot = EnergyInventorySlot.drain(energyContainer, this, 146, 21));
        inventorySlots.add(energyOutputSlot = EnergyInventorySlot.fillOrConvert(energyContainer, tile::getWorld, this, 146, 51));
        energyInputSlot.setSlotOverlay(SlotOverlay.PLUS);
        energyOutputSlot.setSlotOverlay(SlotOverlay.MINUS);
    }

    @Override
    protected int getMultiblockRedstoneLevel() {
        return MekanismUtils.redstoneLevelFromContents(getEnergy(), getStorageCap());
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
        return remote.getAsBoolean() ? clientEnergy : energyContainer.getEnergy();
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

    public void invalidate() {
        energyContainer.invalidate();
    }

    public FloatingLong getStorageCap() {
        return remote.getAsBoolean() ? clientMaxEnergy : energyContainer.getMaxEnergy();
    }

    public FloatingLong getTransferCap() {
        return remote.getAsBoolean() ? clientMaxTransfer : energyContainer.getMaxTransfer();
    }

    public FloatingLong getLastInput() {
        return remote.getAsBoolean() ? clientLastInput : energyContainer.getLastInput();
    }

    public FloatingLong getLastOutput() {
        return remote.getAsBoolean() ? clientLastOutput : energyContainer.getLastOutput();
    }

    public int getCellCount() {
        return remote.getAsBoolean() ? clientCells : energyContainer.getCells();
    }

    public int getProviderCount() {
        return remote.getAsBoolean() ? clientProviders : energyContainer.getProviders();
    }
}