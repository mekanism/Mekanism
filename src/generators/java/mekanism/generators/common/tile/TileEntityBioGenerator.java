package mekanism.generators.common.tile;

import mekanism.api.AutomationType;
import mekanism.api.IContentsListener;
import mekanism.api.RelativeSide;
import mekanism.api.SerializationConstants;
import mekanism.api.fluid.IFluidTank;
import mekanism.api.inventory.IInventorySlot;
import mekanism.common.component.containers.type.ContainerType;
import mekanism.common.component.containers.type.IContainerType;
import mekanism.common.capabilities.fluid.BasicFluidTank;
import mekanism.common.capabilities.fluid.VariableCapacityFluidTank;
import mekanism.common.capabilities.holder.container.IContainerHolder;
import mekanism.common.capabilities.holder.container.MekContainerHelper;
import mekanism.common.integration.computer.SpecialComputerMethodWrapper.ComputerFluidTankWrapper;
import mekanism.common.integration.computer.SpecialComputerMethodWrapper.ComputerIInventorySlotWrapper;
import mekanism.common.integration.computer.annotation.WrappingComputerMethod;
import mekanism.common.inventory.slot.EnergyInventorySlot;
import mekanism.common.tags.MekanismTags;
import mekanism.common.util.MekanismUtils;
import mekanism.generators.common.GeneratorTags;
import mekanism.generators.common.config.MekanismGeneratorsConfig;
import mekanism.generators.common.registries.GeneratorsBlocks;
import mekanism.generators.common.registries.GeneratorsFluids;
import mekanism.generators.common.slot.FluidFuelInventorySlot;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.neoforged.neoforge.transfer.item.ItemResource;
import net.neoforged.neoforge.transfer.transaction.Transaction;
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
        super(GeneratorsBlocks.BIO_GENERATOR, pos, state);
    }

    private static int biofuelFromItem(@NotNull ItemResource itemType) {
        if (itemType.is(MekanismTags.Items.FUELS_BIO)) {
            return MekanismGeneratorsConfig.generators.bioFuelPerItem.getAsInt();
        } else if (itemType.is(MekanismTags.Items.FUELS_BLOCK_BIO)) {
            return 9 * MekanismGeneratorsConfig.generators.bioFuelPerItem.getAsInt();
        }
        return 0;
    }

    @NotNull
    @Override
    protected IContainerHolder<IFluidTank> getInitialFluidTanks(IContentsListener listener) {
        MekContainerHelper<IFluidTank> builder = MekContainerHelper.forSide(facingSupplier);
        builder.addContainer(bioFuelTank = VariableCapacityFluidTank.input(MekanismGeneratorsConfig.generators.bioTankCapacity,
                    fluidStack -> fluidStack.is(GeneratorTags.Fluids.BIOETHANOL), listener), RelativeSide.LEFT, RelativeSide.RIGHT,
              RelativeSide.BACK, RelativeSide.TOP, RelativeSide.BOTTOM);
        return builder.build();
    }

    @NotNull
    @Override
    protected IContainerHolder<IInventorySlot> getInitialInventory(IContentsListener listener) {
        MekContainerHelper<IInventorySlot> builder = MekContainerHelper.forSide(facingSupplier);
        builder.addContainer(fuelSlot = FluidFuelInventorySlot.forFuel(bioFuelTank, TileEntityBioGenerator::biofuelFromItem, GeneratorsFluids.BIOETHANOL,
                    listener, 17, 35), RelativeSide.FRONT, RelativeSide.LEFT, RelativeSide.BACK, RelativeSide.TOP,
              RelativeSide.BOTTOM);
        builder.addContainer(energySlot = EnergyInventorySlot.drain(energyContainer(), listener, 143, 35), RelativeSide.RIGHT);
        return builder.build();
    }

    @Override
    protected boolean onUpdateServer() {
        boolean sendUpdatePacket = super.onUpdateServer();
        energySlot.drainContainerIntoSlot(null);
        fuelSlot.fillOrBurn(null);
        boolean isActive = false;
        if (canFunction() && !bioFuelTank.isEmpty()) {
            try (Transaction transaction = Transaction.openRoot()) {
                int toGenerate = MekanismGeneratorsConfig.generators.bioGeneration.get();
                //If we can insert all the energy we would generate, and can extract 1 mB of fuel
                if (energyContainer().insert(toGenerate, transaction, AutomationType.INTERNAL) == toGenerate &&
                    bioFuelTank.extract(bioFuelTank.resource(), 1, transaction, AutomationType.INTERNAL) == 1) {
                    //Then mark the generator as active and commit the changes
                    isActive = true;
                    transaction.commit();
                    float fluidScale = MekanismUtils.getScale(lastFluidScale, bioFuelTank);
                    if (MekanismUtils.scaleChanged(fluidScale, lastFluidScale)) {
                        lastFluidScale = fluidScale;
                        sendUpdatePacket = true;
                    }
                }
            }
        }
        setActive(isActive);
        return sendUpdatePacket;
    }

    @Override
    public void writeReducedUpdatedTag(@NotNull ValueOutput output) {
        super.writeReducedUpdatedTag(output);
        //TODO - 26.1: Do we want to further trim this and similar cases by skipping adding the fluid key if the tank is empty?
        output.putChild(SerializationConstants.FLUID, bioFuelTank);
    }

    @Override
    public void handleUpdateTag(@NotNull ValueInput input) {
        super.handleUpdateTag(input);
        input.readChild(SerializationConstants.FLUID, bioFuelTank);
    }

    @Override
    public int getRedstoneLevel() {
        return ContainerType.FLUID.getRedstoneSignalFromContainer(bioFuelTank);
    }

    @Override
    protected boolean makesComparatorDirty(IContainerType<?, ?> type) {
        return type == ContainerType.FLUID;
    }

    //Methods relating to IComputerTile
    @Override
    int getProductionRate() {
        return getActive() ? MekanismGeneratorsConfig.generators.bioGeneration.get() : 0;
    }
    //End methods IComputerTile
}