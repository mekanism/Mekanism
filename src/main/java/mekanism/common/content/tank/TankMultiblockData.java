package mekanism.common.content.tank;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import mekanism.api.IContentsListener;
import mekanism.api.SerializationConstants;
import mekanism.api.chemical.ChemicalResource;
import mekanism.api.chemical.IChemicalTank;
import mekanism.api.fluid.IFluidTank;
import mekanism.api.functions.ConstantPredicates;
import mekanism.api.inventory.IInventorySlot;
import mekanism.api.resource.IMekanismResourceHandler;
import mekanism.api.resource.LargeResourceStack;
import mekanism.common.component.containers.type.ContainerType;
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
import mekanism.common.inventory.slot.OutputInventorySlot;
import mekanism.common.lib.multiblock.IValveHandler;
import mekanism.common.lib.multiblock.MultiblockData;
import mekanism.common.tile.interfaces.IFluidContainerManager.ContainerEditMode;
import mekanism.common.tile.multiblock.TileEntityDynamicTank;
import mekanism.common.tile.multiblock.TileEntityDynamicValve;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.WorldUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.redstone.Redstone;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.fluid.FluidResource;

public class TankMultiblockData extends MultiblockData implements IValveHandler {

    private final ResourceHandler<FluidResource> directFluidHandler;
    private final ResourceHandler<ChemicalResource> directChemicalHandler;
    @ContainerSync
    public final MergedTank mergedTank;
    @ContainerSync
    @SyntheticComputerMethod(getter = "getContainerEditMode")
    public ContainerEditMode editMode = ContainerEditMode.BOTH;

    @WrappingComputerMethod(wrapper = ComputerIInventorySlotWrapper.class, methodNames = "getInputItem", docPlaceholder = "input slot")
    HybridInventorySlot inputSlot;
    @WrappingComputerMethod(wrapper = ComputerIInventorySlotWrapper.class, methodNames = "getOutputItem", docPlaceholder = "output slot")
    OutputInventorySlot outputSlot;
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
        directFluidHandler = (IMekanismResourceHandler<FluidResource, IFluidTank>) () -> fluidTanks;
        directChemicalHandler = (IMekanismResourceHandler<ChemicalResource, IChemicalTank>) () -> chemicalTanks;
    }

    private List<IInventorySlot> createBaseInventorySlots() {
        List<IInventorySlot> inventorySlots = new ArrayList<>();
        inventorySlots.add(inputSlot = HybridInventorySlot.input(mergedTank, this, 146, 21));
        inventorySlots.add(outputSlot = OutputInventorySlot.at(this, 146, 51));
        inputSlot.setSlotType(ContainerSlotType.INPUT);
        return inventorySlots;
    }

    @Override
    public boolean tick(ServerLevel world) {
        boolean needsPacket = super.tick(world);
        inputSlot.handleTank(outputSlot, editMode, null);
        float scale = getScale();
        if (MekanismUtils.scaleChanged(scale, prevScale)) {
            prevScale = scale;
            needsPacket = true;
        }
        return needsPacket;
    }

    @Override
    protected void updateEjectors(Level world) {
        if (!world.isClientSide()) {
            //Note: We don't need to wrap valve tanks on the client side
            for (Map.Entry<BlockPos, ValveData> entry : valves.entrySet()) {
                TileEntityDynamicValve tile = WorldUtils.getTileEntity(TileEntityDynamicValve.class, world, entry.getKey());
                if (tile != null) {
                    ValveData valve = entry.getValue();
                    valve.resetTanks();
                    valve.addTank(mergedTank.getFluidTank(), true);
                }
            }
        }
    }

    @Override
    protected boolean hasFluidValveHandling() {
        return true;
    }

    @Override
    public void readUpdateTag(ValueInput input) {
        super.readUpdateTag(input);
        prevScale = input.getFloatOr(SerializationConstants.SCALE, prevScale);
        mergedTank.readFromUpdateTag(input);
        readValves(input);
    }

    @Override
    public void writeUpdateTag(ValueOutput output) {
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
            case FLUID -> ContainerType.FLUID.getRedstoneSignalFromContainer(getFluidTank());
            case CHEMICAL -> ContainerType.CHEMICAL.getRedstoneSignalFromContainer(getChemicalTank());
            default -> Redstone.SIGNAL_NONE;
        };
    }

    private long getStoredAmount() {
        return mergedTank.getCurrentContainer().amountAsLong();
    }

    public ResourceHandler<FluidResource> getDirectFluidHandler() {
        return directFluidHandler;
    }

    public ResourceHandler<ChemicalResource> getDirectChemicalHandler() {
        return directChemicalHandler;
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
            //Reset the transfer direction if the edit mode changes so that it can better determine the direction if it has been changed to BOTH
            inputSlot.resetLastTransferDirection(null);
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
    LargeResourceStack<?> getStored() {
        return mergedTank.getCurrentContainer().asStack();
    }

    @ComputerMethod
    double getFilledPercentage() {
        return getStoredAmount() / (double) getTankCapacity();
    }
    //End computer related methods
}
