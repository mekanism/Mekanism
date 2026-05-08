package mekanism.common.content.tank;

import com.mojang.datafixers.util.Either;
import java.util.ArrayList;
import java.util.List;
import mekanism.api.IContentsListener;
import mekanism.api.SerializationConstants;
import mekanism.api.chemical.ChemicalStack;
import mekanism.api.chemical.IChemicalTank;
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
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.neoforged.neoforge.fluids.FluidStack;
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
    private int tankCapacity;
    private long chemicalTankCapacity;
    public float prevScale;

    public TankMultiblockData(TileEntityDynamicTank tile) {
        super(tile);
        IContentsListener saveAndComparator = createSaveAndComparator();
        mergedTank = MergedTank.create(
              VariableCapacityFluidTank.create(this, this::getTankCapacity, ConstantPredicates.alwaysTrue(), saveAndComparator),
              VariableCapacityChemicalTank.create(this, this::getChemicalTankCapacity, ConstantPredicates.alwaysTrue(), saveAndComparator)
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
            //todo shouldn't this use the lowest amount? - Thiakil
            default -> MekanismUtils.getScale(prevScale, 0, getChemicalTankCapacity(), true);
        };
    }

    @ComputerMethod
    public int getTankCapacity() {
        return tankCapacity;
    }

    @ComputerMethod
    public long getChemicalTankCapacity() {
        return chemicalTankCapacity;
    }

    @Override
    public void setVolume(int volume) {
        if (getVolume() != volume) {
            super.setVolume(volume);
            tankCapacity = volume * MekanismConfig.general.dynamicTankFluidPerTank.get();
            chemicalTankCapacity = volume * MekanismConfig.general.dynamicTankChemicalPerTank.get();
        }
    }

    @Override
    protected int getMultiblockRedstoneLevel() {
        long capacity = mergedTank.getCurrentType() == CurrentType.FLUID ? getTankCapacity() : getChemicalTankCapacity();
        return MekanismUtils.redstoneLevelFromContents(getStoredAmount(), capacity);
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

    @ComputerMethod
    Either<ChemicalStack, FluidStack> getStored() {
        return switch (mergedTank.getCurrentType()) {
            case FLUID -> Either.right(getFluidTank().getFluid());
            case CHEMICAL -> Either.left(getChemicalTank().getStack());
            default -> Either.right(FluidStack.EMPTY);
        };
    }

    @ComputerMethod
    double getFilledPercentage() {
        long capacity = mergedTank.getCurrentType() == CurrentType.FLUID ? getTankCapacity() : getChemicalTankCapacity();
        return getStoredAmount() / (double) capacity;
    }
    //End computer related methods
}
