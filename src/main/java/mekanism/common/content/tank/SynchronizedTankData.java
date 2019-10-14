package mekanism.common.content.tank;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.annotation.Nonnull;
import mekanism.api.Coord4D;
import mekanism.api.inventory.slot.IInventorySlot;
import mekanism.common.inventory.slot.FluidInventorySlot;
import mekanism.common.inventory.slot.OutputInventorySlot;
import mekanism.common.multiblock.SynchronizedData;
import mekanism.common.util.FluidContainerUtils.ContainerEditMode;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Direction;
import net.minecraft.util.NonNullList;
import net.minecraftforge.fluids.FluidStack;

public class SynchronizedTankData extends SynchronizedData<SynchronizedTankData> {

    @Nonnull
    public FluidStack fluidStored = FluidStack.EMPTY;

    /**
     * For use by rendering segment
     */
    @Nonnull
    public FluidStack prevFluid = FluidStack.EMPTY;
    public int prevFluidStage = 0;

    public ContainerEditMode editMode = ContainerEditMode.BOTH;
    public Set<ValveData> valves = new HashSet<>();

    @Nonnull
    private List<IInventorySlot> inventorySlots;

    public SynchronizedTankData() {
        //TODO: Look into some way of allowing slot position to be set differently if needed
        inventorySlots = new ArrayList<>();
        inventorySlots.add(FluidInventorySlot.input(fluidTank, 146, 20));
        inventorySlots.add(OutputInventorySlot.at(146, 51));
    }

    @Nonnull
    @Override
    public List<IInventorySlot> getInventorySlots() {
        return inventorySlots;
    }

    public void setInventoryData(@Nonnull List<IInventorySlot> toCopy) {
        inventorySlots = toCopy;
    }

    public boolean needsRenderUpdate() {
        if ((fluidStored.isEmpty() && !prevFluid.isEmpty()) || (!fluidStored.isEmpty() && prevFluid.isEmpty())) {
            return true;
        }
        if (fluidStored.isEmpty()) {
            return false;
        }
        int totalStage = (volHeight - 2) * (TankUpdateProtocol.FLUID_PER_TANK / 100);
        int currentStage = (int) ((fluidStored.getAmount() / (float) (volume * TankUpdateProtocol.FLUID_PER_TANK)) * totalStage);
        boolean stageChanged = currentStage != prevFluidStage;
        prevFluidStage = currentStage;
        return (fluidStored.getFluid() != prevFluid.getFluid()) || stageChanged;
    }

    public static class ValveData {

        public Direction side;
        public Coord4D location;

        public boolean prevActive;
        public int activeTicks;

        public void onTransfer() {
            activeTicks = 30;
        }

        @Override
        public int hashCode() {
            int code = 1;
            code = 31 * code + side.ordinal();
            code = 31 * code + location.hashCode();
            return code;
        }

        @Override
        public boolean equals(Object obj) {
            return obj instanceof ValveData && ((ValveData) obj).side == side && ((ValveData) obj).location.equals(location);
        }
    }
}