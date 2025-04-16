package mekanism.common.content.tank;

import com.mojang.datafixers.util.Either;
import java.util.ArrayList;
import java.util.List;
import mekanism.api.IContentsListener;
import mekanism.api.SerializationConstants;
import mekanism.api.chemical.BasicChemicalTank;
import mekanism.api.chemical.ChemicalStack;
import mekanism.api.chemical.IChemicalHandler;
import mekanism.api.chemical.IChemicalTank;
import mekanism.api.fluid.IExtendedFluidTank;
import mekanism.api.functions.ConstantPredicates;
import mekanism.api.inventory.IInventorySlot;
import mekanism.common.block.attribute.AttributeStateCommonValveMode;
import mekanism.common.capabilities.chemical.VariableCapacityChemicalTank;
import mekanism.common.capabilities.fluid.BasicFluidTank;
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
import mekanism.common.tile.multiblock.TileEntityDynamicValve;
import mekanism.common.tile.multiblock.TileEntityDynamicTank;
import mekanism.common.util.ChemicalUtil;
import mekanism.common.util.FluidUtils;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.NBTUtils;
import mekanism.common.util.WorldUtils;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.IFluidTank;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;

public class TankMultiblockData extends MultiblockData implements IValveHandler {

    @ContainerSync
    public final MergedTank mergedTank;

    // currently used for valves in output mode to prevent backfeeding
    private final List<IExtendedFluidTank> fakeOutputFluidTank = List.of(BasicFluidTank.output(0, null));
    private final List<IChemicalTank> fakeOutputChemicalTank = List.of(BasicChemicalTank.output(0, null));

    //TODO figure out a more graceful way to do this
    private final List<AdvancedCapabilityOutputTarget<IChemicalHandler, AttributeStateCommonValveMode.CommonValveMode>> chemicalOutputTargets = new ArrayList<>();
    private final List<AdvancedCapabilityOutputTarget<IFluidHandler, AttributeStateCommonValveMode.CommonValveMode>> fluidOutputTargets = new ArrayList<>();

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
    public boolean tick(Level world) {
        boolean needsPacket = super.tick(world);

        // main update
        CurrentType type = mergedTank.getCurrentType();

        if (type == CurrentType.EMPTY) {
            // empty tank behavior
            inputSlot.handleTank(outputSlot, editMode);
            inputSlot.drainChemicalTank();//todo will this do anything if empty??
            outputSlot.fillChemicalTank();

        } else if (type == CurrentType.FLUID) {
            // FLUID tank behavior
            inputSlot.handleTank(outputSlot, editMode);

            // output valves
            if (!fluidOutputTargets.isEmpty()) {
                FluidUtils.emit(getActiveOutputs(fluidOutputTargets, AttributeStateCommonValveMode.CommonValveMode.OUTPUT), mergedTank.getFluidTank());
            }

        } else {
            // CHEMICAL tank behavior
            inputSlot.drainChemicalTank();
            outputSlot.fillChemicalTank();

            // output valves
            if (!chemicalOutputTargets.isEmpty()) {
                ChemicalUtil.emit(getActiveOutputs(chemicalOutputTargets, AttributeStateCommonValveMode.CommonValveMode.OUTPUT), mergedTank.getChemicalTank());
            }
        }

        // scale
        float scale = getScale();
        if (MekanismUtils.scaleChanged(scale, prevScale)) {
            prevScale = scale;
            needsPacket = true;
        }
        return needsPacket;
    }

    @Override
    protected void updateEjectors(Level world) {
        chemicalOutputTargets.clear();
        fluidOutputTargets.clear();
        for (ValveData valve : valves) {
            TileEntityDynamicValve tile = WorldUtils.getTileEntity(TileEntityDynamicValve.class, world, valve.location);
            if (tile != null) {
                tile.addValveTargetCapability(chemicalOutputTargets, fluidOutputTargets, valve.side);
            }
        }
    }

    public List<IExtendedFluidTank> getValveModeFluidTanks(AttributeStateCommonValveMode.CommonValveMode valveMode)
    {
        return valveMode == AttributeStateCommonValveMode.CommonValveMode.INPUT ?
                this.fluidTanks : this.fakeOutputFluidTank;
    }

    public List<IChemicalTank> getValveModeChemicalTanks(AttributeStateCommonValveMode.CommonValveMode valveMode)
    {
        return valveMode == AttributeStateCommonValveMode.CommonValveMode.INPUT ?
                this.chemicalTanks : this.fakeOutputChemicalTank;
    }

    @Override
    public void readUpdateTag(CompoundTag tag, HolderLookup.Provider provider) {
        super.readUpdateTag(tag, provider);
        NBTUtils.setFloatIfPresent(tag, SerializationConstants.SCALE, scale -> prevScale = scale);
        mergedTank.readFromUpdateTag(provider, tag);
        readValves(tag);
    }

    @Override
    public void writeUpdateTag(CompoundTag tag, HolderLookup.Provider provider) {
        super.writeUpdateTag(tag, provider);
        tag.putFloat(SerializationConstants.SCALE, prevScale);
        mergedTank.addToUpdateTag(provider, tag);
        writeValves(tag);
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
            case FLUID -> getFluidTank().getFluidAmount();
            case CHEMICAL -> getChemicalTank().getStored();
            default -> 0;
        };
    }

    public IExtendedFluidTank getFluidTank() {
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
