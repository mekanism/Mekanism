package mekanism.common.content.tank;

import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import mekanism.api.Coord4D;
import mekanism.api.inventory.IInventorySlot;
import mekanism.common.base.ContainerEditMode;
import mekanism.common.inventory.container.slot.ContainerSlotType;
import mekanism.common.inventory.slot.FluidInventorySlot;
import mekanism.common.inventory.slot.OutputInventorySlot;
import mekanism.common.multiblock.SynchronizedData;
import mekanism.common.tile.TileEntityDynamicTank;
import net.minecraft.util.Direction;
import net.minecraftforge.fluids.FluidStack;

public class SynchronizedTankData extends SynchronizedData<SynchronizedTankData> {

    public DynamicFluidTank fluidTank;

    /**
     * For use by rendering segment
     */
    @Nonnull
    public FluidStack prevFluid = FluidStack.EMPTY;
    public int prevFluidStage = 0;

    public ContainerEditMode editMode = ContainerEditMode.BOTH;
    public Set<ValveData> valves = new ObjectOpenHashSet<>();

    @Nonnull
    private List<IInventorySlot> inventorySlots;

    public SynchronizedTankData(TileEntityDynamicTank tile) {
        fluidTank = new DynamicFluidTank(tile);
        inventorySlots = createBaseInventorySlots();
    }

    private List<IInventorySlot> createBaseInventorySlots() {
        List<IInventorySlot> inventorySlots = new ArrayList<>();
        FluidInventorySlot input;
        inventorySlots.add(input = FluidInventorySlot.input(fluidTank, this, 146, 20));
        inventorySlots.add(OutputInventorySlot.at(this, 146, 51));
        input.setSlotType(ContainerSlotType.INPUT);
        return inventorySlots;
    }

    @Nonnull
    @Override
    public List<IInventorySlot> getInventorySlots(@Nullable Direction side) {
        return inventorySlots;
    }

    public void setInventoryData(@Nonnull List<IInventorySlot> toCopy) {
        for (int i = 0; i < toCopy.size(); i++) {
            if (i < inventorySlots.size()) {
                //Copy it via NBT to ensure that we set it using the "unsafe" method in case there
                // is a problem with the types somehow
                inventorySlots.get(i).deserializeNBT(toCopy.get(i).serializeNBT());
            }
        }
    }

    public boolean needsRenderUpdate() {
        if ((fluidTank.isEmpty() && !prevFluid.isEmpty()) || (!fluidTank.isEmpty() && prevFluid.isEmpty())) {
            return true;
        }
        if (fluidTank.isEmpty()) {
            return false;
        }
        int totalStage = (volHeight - 2) * (TankUpdateProtocol.FLUID_PER_TANK / 100);
        int currentStage = (int) ((fluidTank.getFluidAmount() / (float) (volume * TankUpdateProtocol.FLUID_PER_TANK)) * totalStage);
        boolean stageChanged = currentStage != prevFluidStage;
        prevFluidStage = currentStage;
        return !fluidTank.getFluid().isFluidEqual(prevFluid) || stageChanged;
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