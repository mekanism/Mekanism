package mekanism.common.content.tank;

import java.util.ArrayList;
import java.util.List;
import mekanism.api.IContentsListener;
import mekanism.api.SerializationConstants;
import mekanism.api.chemical.IChemicalTank;
import mekanism.api.resource.LargeResourceStack;
import mekanism.api.fluid.IFluidTank;
import mekanism.api.functions.ConstantPredicates;
import mekanism.api.inventory.IInventorySlot;
import mekanism.common.capabilities.chemical.VariableCapacityChemicalTank;
import mekanism.common.capabilities.fluid.VariableCapacityFluidTank;
import mekanism.common.capabilities.merged.MergedTank;
import mekanism.common.capabilities.merged.MergedTank.CurrentType;
import mekanism.common.config.MekanismConfig;
import mekanism.common.integration.computer.SpecialComputerMethodWrapper.ComputerIInventorySlotWrapper;
import mekanism.common.integration.computer.annotation.ComputerMethod;
import mekanism.common.integration.computer.annotation.SyntheticComputerMethod;
import mekanism.common.integration.computer.annotation.WrappingComputerMethod;
import mekanism.common.inventory.container.slot.ContainerSlotType;
import mekanism.common.inventory.container.sync.dynamic.ContainerSync;
import mekanism.common.inventory.slot.HybridInventorySlot;
import mekanism.common.lib.multiblock.IValveHandler;
import mekanism.common.lib.multiblock.MultiblockData;
import mekanism.common.tile.interfaces.IFluidContainerManager.ContainerEditMode;
import mekanism.common.tile.multiblock.TileEntityDynamicTank;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.ResourceUtils;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.redstone.Redstone;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.jetbrains.annotations.NotNull;

public class TankMultiblockData extends MultiblockData implements IValveHandler {

    @ContainerSync
    public final MergedTank mergedTank;
    @ContainerSync
    @SyntheticComputerMethod(getter = "getContainerEditMode")
    public ContainerEditMode editMode = ContainerEditMode.BOTH;

    @WrappingComputerMethod(wrapper = ComputerIInventorySlotWrapper.class, methodNames = "getInputItem", docPlaceholder = "input slot")
    HybridInventorySlot inputSlot;
    @WrappingComputerMethod(wrapper = ComputerIInventorySlotWrapper.class, methodNames = "getOutputItem", docPlaceholder = "output slot")
    HybridInventorySlot outputSlot;
    private long tankCapacity;
    public float prevScale;

    public TankMultiblockData(TileEntityDynamicTank tile) {
        super(tile);
        IContentsListener saveAndComparator = createSaveAndComparator();
        mergedTank = MergedTank.create(
              VariableCapacityFluidTank.create(this, this::getTankCapacity, ConstantPredicates.alwaysTrue(), saveAndComparator),
              VariableCapacityChemicalTank.create(this, this::getTankCapacity, ConstantPredicates.alwaysTrue(), saveAndComparator)
        );
        fluidTanks.add(mergedTank.getFluidTank());
        chemicalTanks.add(mergedTank.getChemicalTank());
        inventorySlots.addAll(createBaseInventorySlots());
    }

    private List<IInventorySlot> createBaseInventorySlots() {
        List<IInventorySlot> inventorySlots = new ArrayList<>();
        inventorySlots.add(inputSlot = HybridInventorySlot.inputOrDrain(mergedTank, this, 146, 21));
        inventorySlots.add(outputSlot = HybridInventorySlot.outputOrFill(mergedTank, this, 146, 51));
        inputSlot.setSlotType(ContainerSlotType.INPUT);
        outputSlot.setSlotType(ContainerSlotType.OUTPUT);
        return inventorySlots;
    }

    @Override
    public boolean tick(ServerLevel world) {
        boolean needsPacket = super.tick(world);
        CurrentType type = mergedTank.getCurrentType();
        if (type == CurrentType.EMPTY) {
            inputSlot.handleTank(outputSlot, editMode);
            inputSlot.drainChemicalTank();//todo will this do anything if empty??
            outputSlot.fillChemicalTank();
        } else if (type == CurrentType.FLUID) {
            inputSlot.handleTank(outputSlot, editMode);
        } else { //Chemicals
            inputSlot.drainChemicalTank();
            outputSlot.fillChemicalTank();
        }
        float scale = getScale();
        if (MekanismUtils.scaleChanged(scale, prevScale)) {
            prevScale = scale;
            needsPacket = true;
        }
        return needsPacket;
    }

    @Override
    public void readUpdateTag(@NotNull ValueInput input) {
        super.readUpdateTag(input);
        prevScale = input.getFloatOr(SerializationConstants.SCALE, prevScale);
        mergedTank.readFromUpdateTag(input);
        readValves(input);
    }

    @Override
    public void writeUpdateTag(@NotNull ValueOutput output) {
        super.writeUpdateTag(output);
        output.putFloat(SerializationConstants.SCALE, prevScale);
        mergedTank.addToUpdateTag(output);
        writeValves(output);
    }

    private float getScale() {
        return switch (mergedTank.getCurrentType()) {
            case FLUID -> MekanismUtils.getScale(prevScale, getFluidTank());
            case CHEMICAL -> MekanismUtils.getScale(prevScale, getChemicalTank());
            default -> MekanismUtils.getScale(prevScale, 0, getTankCapacity(), true);
        };
    }

    @ComputerMethod
    public long getTankCapacity() {
        return tankCapacity;
    }

    @Override
    public void setVolume(int volume) {
        if (getVolume() != volume) {
            super.setVolume(volume);
            tankCapacity = volume * MekanismConfig.general.dynamicTankFluidPerTank.get();
        }
    }

    @Override
    protected int getMultiblockRedstoneLevel() {
        return switch (mergedTank.getCurrentType()) {
            case FLUID -> ResourceUtils.getRedstoneSignalFromContainer(getFluidTank());
            case CHEMICAL -> ResourceUtils.getRedstoneSignalFromContainer(getChemicalTank());
            default -> Redstone.SIGNAL_NONE;
        };
    }

    private long getStoredAmount() {
        return switch (mergedTank.getCurrentType()) {
            case FLUID -> getFluidTank().amountAsLong();
            case CHEMICAL -> getChemicalTank().amountAsLong();
            default -> 0;
        };
    }

    public IFluidTank getFluidTank() {
        return mergedTank.getFluidTank();
    }

    public IChemicalTank getChemicalTank() {
        return mergedTank.getChemicalTank();
    }

    public boolean isEmpty() {
        return mergedTank.getCurrentType() == CurrentType.EMPTY;
    }

    @ComputerMethod
    public void setContainerEditMode(ContainerEditMode mode) {
        if (editMode != mode) {
            editMode = mode;
            markDirty();
        }
    }

    //Computer related methods
    @ComputerMethod
    void incrementContainerEditMode() {
        setContainerEditMode(editMode.getNext());
    }

    @ComputerMethod
    void decrementContainerEditMode() {
        setContainerEditMode(editMode.getPrevious());
    }

    //@ComputerMethod//TODO - 26.1: Add a wrapper type for this
    LargeResourceStack<?> getStored() {
        return switch (mergedTank.getCurrentType()) {
            case FLUID -> getFluidTank().asStack();
            case CHEMICAL -> getChemicalTank().asStack();
            default -> LargeResourceStack.EMPTY_FLUID_STACK;
        };
    }

    @ComputerMethod
    double getFilledPercentage() {
        return getStoredAmount() / (double) getTankCapacity();
    }
    //End computer related methods
}
