package mekanism.common.content.tank;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import mekanism.api.chemical.gas.BasicGasTank;
import mekanism.api.chemical.gas.IGasTank;
import mekanism.api.chemical.gas.IMekanismGasHandler;
import mekanism.api.fluid.IExtendedFluidTank;
import mekanism.api.fluid.IMekanismFluidHandler;
import mekanism.api.inventory.IInventorySlot;
import mekanism.common.base.ContainerEditMode;
import mekanism.common.capabilities.chemical.MultiblockGasTank;
import mekanism.common.capabilities.fluid.BasicFluidTank;
import mekanism.common.capabilities.fluid.MultiblockFluidTank;
import mekanism.common.inventory.container.slot.ContainerSlotType;
import mekanism.common.inventory.slot.HybridInventorySlot;
import mekanism.common.multiblock.MultiblockData;
import mekanism.common.tile.TileEntityDynamicTank;
import mekanism.common.util.MekanismUtils;
import net.minecraft.util.Direction;

public class TankMultiblockData extends MultiblockData<TankMultiblockData> implements IMekanismFluidHandler, IMekanismGasHandler {

    public MultiblockFluidTank<TileEntityDynamicTank> fluidTank;
    public MultiblockGasTank<TileEntityDynamicTank> gasTank;

    public ContainerEditMode editMode = ContainerEditMode.BOTH;

    @Nonnull
    private List<IInventorySlot> inventorySlots;
    private List<IExtendedFluidTank> fluidTanks;
    private List<IGasTank> gasTanks;
    private int tankCapacity;

    public TankMultiblockData(TileEntityDynamicTank tile) {
        fluidTank = MultiblockFluidTank.create(tile, () -> tile.structure == null ? 0 : tile.structure.getTankCapacity(), BasicFluidTank.alwaysTrueBi,
              (stack, automationType) -> gasTank.isEmpty(), BasicFluidTank.alwaysTrue, null);
        fluidTanks = Collections.singletonList(fluidTank);
        gasTank = MultiblockGasTank.create(tile, () -> tile.structure == null ? 0 : tile.structure.getTankCapacity(), BasicGasTank.alwaysTrueBi,
              (stack, automationType) -> fluidTank.isEmpty(), BasicGasTank.alwaysTrue, null, null);
        gasTanks = Collections.singletonList(gasTank);
        inventorySlots = createBaseInventorySlots();
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

    @Nonnull
    @Override
    public List<IInventorySlot> getInventorySlots(@Nullable Direction side) {
        return inventorySlots;
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

    @Nonnull
    @Override
    public List<IExtendedFluidTank> getFluidTanks(@Nullable Direction side) {
        return fluidTanks;
    }

    @Nonnull
    @Override
    public List<IGasTank> getGasTanks(@Nullable Direction side) {
        return gasTanks;
    }
}