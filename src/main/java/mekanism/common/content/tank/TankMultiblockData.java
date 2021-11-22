package mekanism.common.content.tank;

import java.util.ArrayList;
import java.util.List;
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
import mekanism.common.capabilities.fluid.MultiblockFluidTank;
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
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.world.World;
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
        mergedTank = MergedTank.create(
              MultiblockFluidTank.create(this, tile, this::getTankCapacity, BasicFluidTank.alwaysTrue),
              MultiblockChemicalTankBuilder.GAS.create(this, tile, this::getChemicalTankCapacity, ChemicalTankBuilder.GAS.alwaysTrue),
              MultiblockChemicalTankBuilder.INFUSION.create(this, tile, this::getChemicalTankCapacity, ChemicalTankBuilder.INFUSION.alwaysTrue),
              MultiblockChemicalTankBuilder.PIGMENT.create(this, tile, this::getChemicalTankCapacity, ChemicalTankBuilder.PIGMENT.alwaysTrue),
              MultiblockChemicalTankBuilder.SLURRY.create(this, tile, this::getChemicalTankCapacity, ChemicalTankBuilder.SLURRY.alwaysTrue)
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
    public boolean tick(World world) {
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
    public void readUpdateTag(CompoundNBT tag) {
        super.readUpdateTag(tag);
        NBTUtils.setFloatIfPresent(tag, NBTConstants.SCALE, scale -> prevScale = scale);
        mergedTank.readFromUpdateTag(tag);
        readValves(tag);
    }

    @Override
    public void writeUpdateTag(CompoundNBT tag) {
        super.writeUpdateTag(tag);
        tag.putFloat(NBTConstants.SCALE, prevScale);
        mergedTank.addToUpdateTag(tag);
        writeValves(tag);
    }

    private float getScale() {
        switch (mergedTank.getCurrentType()) {
            case FLUID:
                return MekanismUtils.getScale(prevScale, getFluidTank());
            case GAS:
                return MekanismUtils.getScale(prevScale, getGasTank());
            case INFUSION:
                return MekanismUtils.getScale(prevScale, getInfusionTank());
            case PIGMENT:
                return MekanismUtils.getScale(prevScale, getPigmentTank());
            case SLURRY:
                return MekanismUtils.getScale(prevScale, getSlurryTank());
        }
        return MekanismUtils.getScale(prevScale, 0, getChemicalTankCapacity(), true);
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
        switch (mergedTank.getCurrentType()) {
            case FLUID:
                return getFluidTank().getFluidAmount();
            case GAS:
                return getGasTank().getStored();
            case INFUSION:
                return getInfusionTank().getStored();
            case PIGMENT:
                return getPigmentTank().getStored();
            case SLURRY:
                return getSlurryTank().getStored();
        }
        return 0;
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
        switch (mergedTank.getCurrentType()) {
            case FLUID:
                return getFluidTank().getFluid();
            case GAS:
                return getGasTank().getStack();
            case INFUSION:
                return getInfusionTank().getStack();
            case PIGMENT:
                return getPigmentTank().getStack();
            case SLURRY:
                return getSlurryTank().getStack();
        }
        return FluidStack.EMPTY;
    }

    @ComputerMethod
    private double getFilledPercentage() {
        long capacity = mergedTank.getCurrentType() == CurrentType.FLUID ? getTankCapacity() : getChemicalTankCapacity();
        return getStoredAmount() / (double) capacity;
    }
    //End computer related methods
}