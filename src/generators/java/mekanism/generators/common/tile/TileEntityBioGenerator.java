package mekanism.generators.common.tile;

import javax.annotation.Nonnull;
import mekanism.api.Action;
import mekanism.api.NBTConstants;
import mekanism.api.RelativeSide;
import mekanism.api.inventory.AutomationType;
import mekanism.common.capabilities.fluid.BasicFluidTank;
import mekanism.common.capabilities.holder.fluid.FluidTankHelper;
import mekanism.common.capabilities.holder.fluid.IFluidTankHolder;
import mekanism.common.capabilities.holder.slot.IInventorySlotHolder;
import mekanism.common.capabilities.holder.slot.InventorySlotHelper;
import mekanism.common.inventory.slot.EnergyInventorySlot;
import mekanism.common.tags.MekanismTags;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.NBTUtils;
import mekanism.generators.common.GeneratorTags;
import mekanism.generators.common.config.MekanismGeneratorsConfig;
import mekanism.generators.common.registries.GeneratorsBlocks;
import mekanism.generators.common.registries.GeneratorsFluids;
import mekanism.generators.common.slot.FluidFuelInventorySlot;
import net.minecraft.block.BlockState;
import net.minecraft.nbt.CompoundNBT;

public class TileEntityBioGenerator extends TileEntityGenerator {

    private static final int MAX_FLUID = 24_000;

    public BasicFluidTank bioFuelTank;
    private FluidFuelInventorySlot fuelSlot;
    private EnergyInventorySlot energySlot;
    private float lastFluidScale;

    public TileEntityBioGenerator() {
        super(GeneratorsBlocks.BIO_GENERATOR, MekanismGeneratorsConfig.generators.bioGeneration.get().multiply(2));
    }

    @Nonnull
    @Override
    protected IFluidTankHolder getInitialFluidTanks() {
        FluidTankHelper builder = FluidTankHelper.forSide(this::getDirection);
        builder.addTank(bioFuelTank = BasicFluidTank.create(MAX_FLUID, fluidStack -> fluidStack.getFluid().isIn(GeneratorTags.Fluids.BIOETHANOL), this),
              RelativeSide.LEFT, RelativeSide.RIGHT, RelativeSide.BACK, RelativeSide.TOP, RelativeSide.BOTTOM);
        return builder.build();
    }

    @Nonnull
    @Override
    protected IInventorySlotHolder getInitialInventory() {
        InventorySlotHelper builder = InventorySlotHelper.forSide(this::getDirection);
        builder.addSlot(fuelSlot = FluidFuelInventorySlot.forFuel(bioFuelTank, stack -> stack.getItem().isIn(MekanismTags.Items.FUELS_BIO) ? 200 : 0,
              GeneratorsFluids.BIOETHANOL::getFluidStack, this, 17, 35),
              RelativeSide.FRONT, RelativeSide.LEFT, RelativeSide.BACK, RelativeSide.TOP, RelativeSide.BOTTOM);
        builder.addSlot(energySlot = EnergyInventorySlot.drain(getEnergyContainer(), this, 143, 35), RelativeSide.RIGHT);
        return builder.build();
    }

    @Override
    protected void onUpdateServer() {
        super.onUpdateServer();
        energySlot.drainContainer();
        fuelSlot.fillOrBurn();
        if (MekanismUtils.canFunction(this) && !bioFuelTank.isEmpty() &&
            getEnergyContainer().insert(MekanismGeneratorsConfig.generators.bioGeneration.get(), Action.SIMULATE, AutomationType.INTERNAL).isZero()) {
            setActive(true);
            MekanismUtils.logMismatchedStackSize(bioFuelTank.shrinkStack(1, Action.EXECUTE), 1);
            getEnergyContainer().insert(MekanismGeneratorsConfig.generators.bioGeneration.get(), Action.EXECUTE, AutomationType.INTERNAL);
            float fluidScale = MekanismUtils.getScale(lastFluidScale, bioFuelTank);
            if (fluidScale != lastFluidScale) {
                lastFluidScale = fluidScale;
                sendUpdatePacket();
            }
        } else {
            setActive(false);
        }
    }

    @Nonnull
    @Override
    public CompoundNBT getReducedUpdateTag() {
        CompoundNBT updateTag = super.getReducedUpdateTag();
        updateTag.put(NBTConstants.FLUID_STORED, bioFuelTank.serializeNBT());
        return updateTag;
    }

    @Override
    public void handleUpdateTag(BlockState state, @Nonnull CompoundNBT tag) {
        super.handleUpdateTag(state, tag);
        NBTUtils.setCompoundIfPresent(tag, NBTConstants.FLUID_STORED, nbt -> bioFuelTank.deserializeNBT(nbt));
    }
}