package mekanism.common.content.tank;

import java.util.ArrayList;
import java.util.List;
import mekanism.api.chemical.gas.BasicGasTank;
import mekanism.api.inventory.IInventorySlot;
import mekanism.common.base.ContainerEditMode;
import mekanism.common.capabilities.chemical.MultiblockGasTank;
import mekanism.common.capabilities.fluid.BasicFluidTank;
import mekanism.common.capabilities.fluid.MultiblockFluidTank;
import mekanism.common.inventory.container.slot.ContainerSlotType;
import mekanism.common.inventory.container.sync.dynamic.ContainerSync;
import mekanism.common.inventory.slot.HybridInventorySlot;
import mekanism.common.multiblock.MultiblockData;
import mekanism.common.tile.TileEntityDynamicTank;
import mekanism.common.util.MekanismUtils;
import net.minecraft.world.World;

public class TankMultiblockData extends MultiblockData {

    @ContainerSync public MultiblockFluidTank<TankMultiblockData> fluidTank;
    @ContainerSync public MultiblockGasTank<TankMultiblockData> gasTank;

    @ContainerSync
    public ContainerEditMode editMode = ContainerEditMode.BOTH;

    private int tankCapacity;
    public float prevScale;

    public TankMultiblockData(TileEntityDynamicTank tile) {
        fluidTank = MultiblockFluidTank.create(this, tile, () -> getTankCapacity(), BasicFluidTank.alwaysTrueBi,
              (stack, automationType) -> gasTank.isEmpty(), BasicFluidTank.alwaysTrue, null);
        fluidTanks.add(fluidTank);
        gasTank = MultiblockGasTank.create(this, tile, () -> getTankCapacity(), BasicGasTank.alwaysTrueBi,
              (stack, automationType) -> fluidTank.isEmpty(), BasicGasTank.alwaysTrue, null, null);
        gasTanks.add(gasTank);
        inventorySlots.addAll(createBaseInventorySlots());
    }

    private List<IInventorySlot> createBaseInventorySlots() {
        List<IInventorySlot> inventorySlots = new ArrayList<>();
        HybridInventorySlot input, output;
        inventorySlots.add(input = HybridInventorySlot.inputOrDrain(gasTank, fluidTank, this, 146, 21));
        inventorySlots.add(output = HybridInventorySlot.outputOrFill(gasTank, fluidTank, this, 146, 51));
        input.setSlotType(ContainerSlotType.INPUT);
        output.setSlotType(ContainerSlotType.OUTPUT);
        return inventorySlots;
    }

    @Override
    public boolean tick(World world) {
        boolean needsPacket = super.tick(world);
        //TODO: No magic numbers??
        HybridInventorySlot inputSlot = (HybridInventorySlot) inventorySlots.get(0);
        HybridInventorySlot outputSlot = (HybridInventorySlot) inventorySlots.get(1);
        inputSlot.handleTank(outputSlot, editMode);
        inputSlot.drainGasTank();
        outputSlot.fillGasTank();
        float scale = Math.max(MekanismUtils.getScale(prevScale, fluidTank), MekanismUtils.getScale(prevScale, gasTank));
        if (scale != prevScale) {
            needsPacket = true;
            prevScale = scale;
        }
        return needsPacket;
    }

    public int getTankCapacity() {
        return tankCapacity;
    }

    @Override
    public void setVolume(int volume) {
        super.setVolume(volume);
        tankCapacity = getVolume() * TankUpdateProtocol.FLUID_PER_TANK;
    }

    @Override
    protected int getMultiblockRedstoneLevel() {
        return MekanismUtils.redstoneLevelFromContents(fluidTank.getFluidAmount(), fluidTank.getCapacity());
    }
}