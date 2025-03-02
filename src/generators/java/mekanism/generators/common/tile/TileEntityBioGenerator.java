package mekanism.generators.common.tile;

import mekanism.api.Action;
import mekanism.api.AutomationType;
import mekanism.api.IContentsListener;
import mekanism.api.RelativeSide;
import mekanism.api.SerializationConstants;
import mekanism.common.attachments.containers.ContainerType;
import mekanism.common.capabilities.fluid.BasicFluidTank;
import mekanism.common.capabilities.fluid.VariableCapacityFluidTank;
import mekanism.common.capabilities.holder.fluid.FluidTankHelper;
import mekanism.common.capabilities.holder.fluid.IFluidTankHolder;
import mekanism.common.capabilities.holder.slot.IInventorySlotHolder;
import mekanism.common.capabilities.holder.slot.InventorySlotHelper;
import mekanism.common.integration.computer.SpecialComputerMethodWrapper.ComputerFluidTankWrapper;
import mekanism.common.integration.computer.SpecialComputerMethodWrapper.ComputerIInventorySlotWrapper;
import mekanism.common.integration.computer.annotation.WrappingComputerMethod;
import mekanism.common.inventory.slot.EnergyInventorySlot;
import mekanism.common.tags.MekanismTags;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.NBTUtils;
import mekanism.generators.common.GeneratorTags;
import mekanism.generators.common.config.MekanismGeneratorsConfig;
import mekanism.generators.common.registries.GeneratorsBlocks;
import mekanism.generators.common.registries.GeneratorsFluids;
import mekanism.generators.common.slot.FluidFuelInventorySlot;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;

public class TileEntityBioGenerator extends TileEntityGenerator {

    @WrappingComputerMethod(wrapper = ComputerFluidTankWrapper.class, methodNames = {"getBioFuel", "getBioFuelCapacity", "getBioFuelNeeded",
                                                                                     "getBioFuelFilledPercentage"}, docPlaceholder = "biofuel tank")
    public BasicFluidTank bioFuelTank;
    @WrappingComputerMethod(wrapper = ComputerIInventorySlotWrapper.class, methodNames = "getFuelItem", docPlaceholder = "fuel slot")
    FluidFuelInventorySlot fuelSlot;
    @WrappingComputerMethod(wrapper = ComputerIInventorySlotWrapper.class, methodNames = "getEnergyItem", docPlaceholder = "energy item")
    EnergyInventorySlot energySlot;
    private float lastFluidScale;

    public TileEntityBioGenerator(BlockPos pos, BlockState state) {
        super(GeneratorsBlocks.BIO_GENERATOR, pos, state, MekanismGeneratorsConfig.generators.bioGeneration);
    }

    @NotNull
    @Override
    protected IFluidTankHolder getInitialFluidTanks(IContentsListener listener) {
        FluidTankHelper builder = FluidTankHelper.forSide(facingSupplier);
        builder.addTank(bioFuelTank = VariableCapacityFluidTank.input(MekanismGeneratorsConfig.generators.bioTankCapacity,
                    fluidStack -> fluidStack.is(GeneratorTags.Fluids.BIOETHANOL), listener), RelativeSide.LEFT, RelativeSide.RIGHT,
              RelativeSide.BACK, RelativeSide.TOP, RelativeSide.BOTTOM);
        return builder.build();
    }

    @NotNull
    @Override
    protected IInventorySlotHolder getInitialInventory(IContentsListener listener) {
        InventorySlotHelper builder = InventorySlotHelper.forSide(facingSupplier);
        builder.addSlot(fuelSlot = FluidFuelInventorySlot.forFuel(bioFuelTank, stack -> stack.is(MekanismTags.Items.FUELS_BIO) ? 200 : stack.is(MekanismTags.Items.FUELS_BLOCK_BIO) ? 200 * 9 : 0,
                    GeneratorsFluids.BIOETHANOL::asStack, listener, 17, 35), RelativeSide.FRONT, RelativeSide.LEFT, RelativeSide.BACK, RelativeSide.TOP,
              RelativeSide.BOTTOM);
        builder.addSlot(energySlot = EnergyInventorySlot.drain(getEnergyContainer(), listener, 143, 35), RelativeSide.RIGHT);
        return builder.build();
    }

    @Override
    protected boolean onUpdateServer() {
        boolean sendUpdatePacket = super.onUpdateServer();
        energySlot.drainContainer();
        fuelSlot.fillOrBurn();
        if (canFunction() && !bioFuelTank.isEmpty() &&
            getEnergyContainer().insert(MekanismGeneratorsConfig.generators.bioGeneration.get(), Action.SIMULATE, AutomationType.INTERNAL) == 0L) {
            setActive(true);
            MekanismUtils.logMismatchedStackSize(bioFuelTank.shrinkStack(1, Action.EXECUTE), 1);
            getEnergyContainer().insert(MekanismGeneratorsConfig.generators.bioGeneration.get(), Action.EXECUTE, AutomationType.INTERNAL);
            float fluidScale = MekanismUtils.getScale(lastFluidScale, bioFuelTank);
            if (MekanismUtils.scaleChanged(fluidScale, lastFluidScale)) {
                lastFluidScale = fluidScale;
                sendUpdatePacket = true;
            }
        } else {
            setActive(false);
        }
        return sendUpdatePacket;
    }

    @NotNull
    @Override
    public CompoundTag getReducedUpdateTag(@NotNull HolderLookup.Provider provider) {
        CompoundTag updateTag = super.getReducedUpdateTag(provider);
        updateTag.put(SerializationConstants.FLUID, bioFuelTank.serializeNBT(provider));
        return updateTag;
    }

    @Override
    public void handleUpdateTag(@NotNull CompoundTag tag, @NotNull HolderLookup.Provider provider) {
        super.handleUpdateTag(tag, provider);
        NBTUtils.setCompoundIfPresent(tag, SerializationConstants.FLUID, nbt -> bioFuelTank.deserializeNBT(provider, nbt));
    }

    @Override
    public int getRedstoneLevel() {
        return MekanismUtils.redstoneLevelFromContents(bioFuelTank.getFluidAmount(), bioFuelTank.getCapacity());
    }

    @Override
    protected boolean makesComparatorDirty(ContainerType<?, ?, ?> type) {
        return type == ContainerType.FLUID;
    }

    //Methods relating to IComputerTile
    @Override
    long getProductionRate() {
        return getActive() ? MekanismGeneratorsConfig.generators.bioGeneration.get() : 0L;
    }
    //End methods IComputerTile
}