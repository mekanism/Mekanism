package mekanism.common.content.tank;

import java.util.ArrayList;
import java.util.List;
import mekanism.api.IContentsListener;
import mekanism.api.NBTConstants;
import mekanism.api.chemical.ChemicalTankBuilder;
import mekanism.api.chemical.gas.IGasTank;
import mekanism.api.chemical.infuse.IInfusionTank;
import mekanism.api.chemical.pigment.IPigmentTank;
import mekanism.api.chemical.slurry.ISlurryTank;
import mekanism.api.fluid.IExtendedFluidTank;
import mekanism.api.inventory.IInventorySlot;
import mekanism.common.capabilities.chemical.multiblock.MultiblockChemicalTankBuilder;
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
import mekanism.common.tile.multiblock.TileEntityDynamicTank;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.NBTUtils;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.Level;
import net.minecraftforge.fluids.FluidStack;

public class TankMultiblockData extends MultiblockData implements IValveHandler {

    @ContainerSync
    public final MergedTank mergedTank;
    @ContainerSync
    @SyntheticComputerMethod(getter = "getContainerEditMode")
    public ContainerEditMode editMode = ContainerEditMode.BOTH;

    @WrappingComputerMethod(wrapper = ComputerIInventorySlotWrapper.class, methodNames = "getInputItem")
    private HybridInventorySlot inputSlot;
    @WrappingComputerMethod(wrapper = ComputerIInventorySlotWrapper.class, methodNames = "getOutputItem")
    private HybridInventorySlot outputSlot;
    private int tankCapacity;
    private long chemicalTankCapacity;
    public float prevScale;

    public TankMultiblockData(TileEntityDynamicTank tile) {
        super(tile);
        IContentsListener saveAndComparator = createSaveAndComparator();
        mergedTank = MergedTank.create(
              VariableCapacityFluidTank.create(this, this::getTankCapacity, BasicFluidTank.alwaysTrue, saveAndComparator),
              MultiblockChemicalTankBuilder.GAS.create(this, this::getChemicalTankCapacity, ChemicalTankBuilder.GAS.alwaysTrue, saveAndComparator),
              MultiblockChemicalTankBuilder.INFUSION.create(this, this::getChemicalTankCapacity, ChemicalTankBuilder.INFUSION.alwaysTrue, saveAndComparator),
              MultiblockChemicalTankBuilder.PIGMENT.create(this, this::getChemicalTankCapacity, ChemicalTankBuilder.PIGMENT.alwaysTrue, saveAndComparator),
              MultiblockChemicalTankBuilder.SLURRY.create(this, this::getChemicalTankCapacity, ChemicalTankBuilder.SLURRY.alwaysTrue, saveAndComparator)
        );
        fluidTanks.add(mergedTank.getFluidTank());
        gasTanks.add(mergedTank.getGasTank());
        infusionTanks.add(mergedTank.getInfusionTank());
        pigmentTanks.add(mergedTank.getPigmentTank());
        slurryTanks.add(mergedTank.getSlurryTank());
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
        CurrentType type = mergedTank.getCurrentType();
        if (type == CurrentType.EMPTY) {
            inputSlot.handleTank(outputSlot, editMode);
            inputSlot.drainChemicalTanks();
            outputSlot.fillChemicalTanks();
        } else if (type == CurrentType.FLUID) {
            inputSlot.handleTank(outputSlot, editMode);
        } else { //Chemicals
            inputSlot.drainChemicalTank(type);
            outputSlot.fillChemicalTank(type);
        }
        float scale = getScale();
        if (scale != prevScale) {
            prevScale = scale;
            needsPacket = true;
        }
        return needsPacket;
    }

    @Override
    public void readUpdateTag(CompoundTag tag) {
        super.readUpdateTag(tag);
        NBTUtils.setFloatIfPresent(tag, NBTConstants.SCALE, scale -> prevScale = scale);
        mergedTank.readFromUpdateTag(tag);
        readValves(tag);
    }

    @Override
    public void writeUpdateTag(CompoundTag tag) {
        super.writeUpdateTag(tag);
        tag.putFloat(NBTConstants.SCALE, prevScale);
        mergedTank.addToUpdateTag(tag);
        writeValves(tag);
    }

    private float getScale() {
        return switch (mergedTank.getCurrentType()) {
            case FLUID -> MekanismUtils.getScale(prevScale, getFluidTank());
            case GAS -> MekanismUtils.getScale(prevScale, getGasTank());
            case INFUSION -> MekanismUtils.getScale(prevScale, getInfusionTank());
            case PIGMENT -> MekanismUtils.getScale(prevScale, getPigmentTank());
            case SLURRY -> MekanismUtils.getScale(prevScale, getSlurryTank());
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
        super.setVolume(volume);
        tankCapacity = getVolume() * MekanismConfig.general.dynamicTankFluidPerTank.get();
        chemicalTankCapacity = getVolume() * MekanismConfig.general.dynamicTankChemicalPerTank.get();
    }

    @Override
    protected int getMultiblockRedstoneLevel() {
        long capacity = mergedTank.getCurrentType() == CurrentType.FLUID ? getTankCapacity() : getChemicalTankCapacity();
        return MekanismUtils.redstoneLevelFromContents(getStoredAmount(), capacity);
    }

    private long getStoredAmount() {
        return switch (mergedTank.getCurrentType()) {
            case FLUID -> getFluidTank().getFluidAmount();
            case GAS -> getGasTank().getStored();
            case INFUSION -> getInfusionTank().getStored();
            case PIGMENT -> getPigmentTank().getStored();
            case SLURRY -> getSlurryTank().getStored();
            default -> 0;
        };
    }

    public IExtendedFluidTank getFluidTank() {
        return mergedTank.getFluidTank();
    }

    public IGasTank getGasTank() {
        return mergedTank.getGasTank();
    }

    public IInfusionTank getInfusionTank() {
        return mergedTank.getInfusionTank();
    }

    public IPigmentTank getPigmentTank() {
        return mergedTank.getPigmentTank();
    }

    public ISlurryTank getSlurryTank() {
        return mergedTank.getSlurryTank();
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
    private void incrementContainerEditMode() {
        setContainerEditMode(editMode.getNext());
    }

    @ComputerMethod
    private void decrementContainerEditMode() {
        setContainerEditMode(editMode.getPrevious());
    }

    @ComputerMethod
    private Object getStored() {
        return switch (mergedTank.getCurrentType()) {
            case FLUID -> getFluidTank().getFluid();
            case GAS -> getGasTank().getStack();
            case INFUSION -> getInfusionTank().getStack();
            case PIGMENT -> getPigmentTank().getStack();
            case SLURRY -> getSlurryTank().getStack();
            default -> FluidStack.EMPTY;
        };
    }

    @ComputerMethod
    private double getFilledPercentage() {
        long capacity = mergedTank.getCurrentType() == CurrentType.FLUID ? getTankCapacity() : getChemicalTankCapacity();
        return getStoredAmount() / (double) capacity;
    }
    //End computer related methods
}