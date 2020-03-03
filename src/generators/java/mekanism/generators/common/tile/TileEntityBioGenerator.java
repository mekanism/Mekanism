package mekanism.generators.common.tile;

import javax.annotation.Nonnull;
import mekanism.api.Action;
import mekanism.api.RelativeSide;
import mekanism.api.TileNetworkList;
import mekanism.common.capabilities.fluid.BasicFluidTank;
import mekanism.common.capabilities.holder.fluid.FluidTankHelper;
import mekanism.common.capabilities.holder.fluid.IFluidTankHolder;
import mekanism.common.capabilities.holder.slot.IInventorySlotHolder;
import mekanism.common.capabilities.holder.slot.InventorySlotHelper;
import mekanism.common.inventory.slot.EnergyInventorySlot;
import mekanism.common.tags.MekanismTags;
import mekanism.common.util.MekanismUtils;
import mekanism.generators.common.GeneratorTags;
import mekanism.generators.common.config.MekanismGeneratorsConfig;
import mekanism.generators.common.registries.GeneratorsBlocks;
import mekanism.generators.common.registries.GeneratorsFluids;
import mekanism.generators.common.slot.FluidFuelInventorySlot;
import net.minecraft.network.PacketBuffer;

public class TileEntityBioGenerator extends TileEntityGenerator {

    private static final int MAX_FLUID = 24_000;

    public BasicFluidTank bioFuelTank;
    private FluidFuelInventorySlot fuelSlot;
    private EnergyInventorySlot energySlot;

    public TileEntityBioGenerator() {
        super(GeneratorsBlocks.BIO_GENERATOR, MekanismGeneratorsConfig.generators.bioGeneration.get() * 2);
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
        builder.addSlot(energySlot = EnergyInventorySlot.charge(this, 143, 35), RelativeSide.RIGHT);
        return builder.build();
    }

    @Override
    public void onUpdate() {
        super.onUpdate();
        energySlot.charge(this);
        fuelSlot.fillOrBurn();
        if (canOperate()) {
            if (!isRemote()) {
                setActive(true);
            }
            if (bioFuelTank.shrinkStack(1, Action.EXECUTE) != 1) {
                //TODO: Print error that something went wrong
            }
            setEnergy(getEnergy() + MekanismGeneratorsConfig.generators.bioGeneration.get());
        } else if (!isRemote()) {
            setActive(false);
        }
    }

    @Override
    public boolean canOperate() {
        return getEnergy() < getBaseStorage() && !bioFuelTank.isEmpty() && MekanismUtils.canFunction(this);
    }

    @Override
    public void handlePacketData(PacketBuffer dataStream) {
        super.handlePacketData(dataStream);
        if (isRemote()) {
            bioFuelTank.setStack(dataStream.readFluidStack());
        }
    }

    @Override
    public TileNetworkList getNetworkedData(TileNetworkList data) {
        super.getNetworkedData(data);
        //Note: We still have to sync bio fuel as it is used in rendering the tile
        data.add(bioFuelTank.getFluid());
        return data;
    }
}