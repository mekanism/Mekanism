package mekanism.common.content.tank;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import mekanism.api.fluid.IExtendedFluidTank;
import mekanism.api.fluid.IMekanismFluidHandler;
import mekanism.api.inventory.IInventorySlot;
import mekanism.common.base.ContainerEditMode;
import mekanism.common.capabilities.fluid.BasicFluidTank;
import mekanism.common.capabilities.fluid.MultiblockFluidTank;
import mekanism.common.inventory.container.slot.ContainerSlotType;
import mekanism.common.inventory.slot.FluidInventorySlot;
import mekanism.common.inventory.slot.OutputInventorySlot;
import mekanism.common.multiblock.IValveHandler.ValveData;
import mekanism.common.multiblock.MultiblockData;
import mekanism.common.tile.TileEntityDynamicTank;
import net.minecraft.util.Direction;

public class TankMultiblockData extends MultiblockData<TankMultiblockData> implements IMekanismFluidHandler {

    public MultiblockFluidTank<TileEntityDynamicTank> fluidTank;

    public ContainerEditMode editMode = ContainerEditMode.BOTH;
    public Set<ValveData> valves = new ObjectOpenHashSet<>();

    @Nonnull
    private List<IInventorySlot> inventorySlots;
    private List<IExtendedFluidTank> fluidTanks;
    private int tankCapacity;

    public TankMultiblockData(TileEntityDynamicTank tile) {
        fluidTank = MultiblockFluidTank.create(tile, () -> tile.structure == null ? 0 : tile.structure.getTankCapacity(), BasicFluidTank.alwaysTrue);
        fluidTanks = Collections.singletonList(fluidTank);
        inventorySlots = createBaseInventorySlots();
    }

    private List<IInventorySlot> createBaseInventorySlots() {
        List<IInventorySlot> inventorySlots = new ArrayList<>();
        FluidInventorySlot input;
        inventorySlots.add(input = FluidInventorySlot.input(fluidTank, this, 146, 21));
        inventorySlots.add(OutputInventorySlot.at(this, 146, 51));
        input.setSlotType(ContainerSlotType.INPUT);
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

    @Nonnull
    @Override
    public List<IExtendedFluidTank> getFluidTanks(@Nullable Direction side) {
        return fluidTanks;
    }
}