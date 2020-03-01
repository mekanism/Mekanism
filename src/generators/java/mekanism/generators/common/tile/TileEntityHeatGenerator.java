package mekanism.generators.common.tile;

import java.util.Optional;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import mekanism.api.Action;
import mekanism.api.Coord4D;
import mekanism.api.IHeatTransfer;
import mekanism.api.RelativeSide;
import mekanism.common.capabilities.Capabilities;
import mekanism.common.capabilities.fluid.BasicFluidTank;
import mekanism.common.capabilities.holder.fluid.FluidTankHelper;
import mekanism.common.capabilities.holder.fluid.IFluidTankHolder;
import mekanism.common.capabilities.holder.slot.IInventorySlotHolder;
import mekanism.common.capabilities.holder.slot.InventorySlotHelper;
import mekanism.common.inventory.container.MekanismContainer;
import mekanism.common.inventory.container.sync.SyncableDouble;
import mekanism.common.inventory.slot.EnergyInventorySlot;
import mekanism.common.inventory.slot.FuelInventorySlot;
import mekanism.common.util.CapabilityUtils;
import mekanism.common.util.EnumUtils;
import mekanism.common.util.HeatUtils;
import mekanism.common.util.MekanismUtils;
import mekanism.generators.common.config.MekanismGeneratorsConfig;
import mekanism.generators.common.registries.GeneratorsBlocks;
import net.minecraft.fluid.Fluids;
import net.minecraft.item.ItemStack;
import net.minecraft.tags.FluidTags;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.ForgeHooks;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidUtil;
import net.minecraftforge.fluids.capability.IFluidHandlerItem;

public class TileEntityHeatGenerator extends TileEntityGenerator implements IHeatTransfer {

    private static final String[] methods = new String[]{"getEnergy", "getOutput", "getMaxEnergy", "getEnergyNeeded", "getFuel", "getFuelNeeded"};
    /**
     * The FluidTank for this generator.
     */
    public BasicFluidTank lavaTank;
    private double temperature = 0;
    private double thermalEfficiency = 0.5D;
    private double invHeatCapacity = 1;
    private double heatToAbsorb = 0;
    private double producingEnergy;
    private double lastTransferLoss;
    private double lastEnvironmentLoss;

    private FuelInventorySlot fuelSlot;
    private EnergyInventorySlot energySlot;

    public TileEntityHeatGenerator() {
        super(GeneratorsBlocks.HEAT_GENERATOR, MekanismGeneratorsConfig.generators.heatGeneration.get() * 2);
    }

    @Nonnull
    @Override
    protected IFluidTankHolder getInitialFluidTanks() {
        FluidTankHelper builder = FluidTankHelper.forSide(this::getDirection);
        builder.addTank(lavaTank = BasicFluidTank.create(24_000, this), RelativeSide.LEFT, RelativeSide.RIGHT, RelativeSide.BACK, RelativeSide.TOP,
              RelativeSide.BOTTOM);
        return builder.build();
    }

    @Nonnull
    @Override
    protected IInventorySlotHolder getInitialInventory() {
        InventorySlotHelper builder = InventorySlotHelper.forSide(this::getDirection);
        //TODO: See if this can be cleaned up/optimized
        builder.addSlot(fuelSlot = FuelInventorySlot.forFuel(this::getFuel, fluidStack -> fluidStack.getFluid().isIn(FluidTags.LAVA), this, 17, 35),
              RelativeSide.FRONT, RelativeSide.LEFT, RelativeSide.BACK, RelativeSide.TOP, RelativeSide.BOTTOM);
        builder.addSlot(energySlot = EnergyInventorySlot.charge(this, 143, 35), RelativeSide.RIGHT);
        return builder.build();
    }

    @Override
    public void onUpdate() {
        super.onUpdate();
        if (!isRemote()) {
            energySlot.charge(this);
            ItemStack fuelStack = fuelSlot.getStack();
            if (!fuelStack.isEmpty()) {
                //TODO: FluidHandler - Move this to the FuelSlot, as we are basically modifying the slot's stack
                Optional<IFluidHandlerItem> fluidHandlerItem = MekanismUtils.toOptional(FluidUtil.getFluidHandler(fuelStack));
                if (fluidHandlerItem.isPresent()) {
                    IFluidHandlerItem handler = fluidHandlerItem.get();
                    FluidStack fluidStack = handler.drain(Integer.MAX_VALUE, FluidAction.SIMULATE);
                    if (!fluidStack.isEmpty() && fluidStack.getFluid().isIn(FluidTags.LAVA)) {
                        lavaTank.fill(handler.drain(lavaTank.getNeeded(), FluidAction.EXECUTE), FluidAction.EXECUTE);
                    }
                    //TODO: FluidHandler - Check if we want to be doing this when ret is empty
                    fuelSlot.setStack(handler.getContainer());
                } else {
                    int fuel = getFuel(fuelStack);
                    if (fuel > 0) {
                        int fuelNeeded = lavaTank.getNeeded();
                        if (fuel <= fuelNeeded) {
                            lavaTank.fill(new FluidStack(Fluids.LAVA, fuel), FluidAction.EXECUTE);
                            ItemStack containerItem = fuelStack.getItem().getContainerItem(fuelStack);
                            if (!containerItem.isEmpty()) {
                                fuelSlot.setStack(containerItem);
                            } else {
                                if (fuelSlot.shrinkStack(1, Action.EXECUTE) != 1) {
                                    //TODO: Print error that something went wrong
                                }
                            }
                        }
                    }
                }
            }

            double prev = getEnergy();
            transferHeatTo(getBoost());
            if (canOperate()) {
                setActive(true);
                lavaTank.drain(10, FluidAction.EXECUTE);
                transferHeatTo(MekanismGeneratorsConfig.generators.heatGeneration.get());
            } else {
                setActive(false);
            }

            double[] loss = simulateHeat();
            applyTemperatureChange();
            lastTransferLoss = loss[0];
            lastEnvironmentLoss = loss[1];
            producingEnergy = getEnergy() - prev;
        }
    }

    @Override
    public boolean canOperate() {
        return getEnergy() < getBaseStorage() && lavaTank.getFluidAmount() >= 10 && MekanismUtils.canFunction(this);
    }

    private double getBoost() {
        int lavaBoost = 0;
        double netherBoost = 0D;
        for (Direction side : EnumUtils.DIRECTIONS) {
            Coord4D coord = Coord4D.get(this).offset(side);
            if (isLava(coord.getPos())) {
                lavaBoost++;
            }
        }
        World world = getWorld();
        if (world != null && world.getDimension().isNether()) {
            netherBoost = MekanismGeneratorsConfig.generators.heatGenerationNether.get();
        }
        return (MekanismGeneratorsConfig.generators.heatGenerationLava.get() * lavaBoost) + netherBoost;
    }

    private boolean isLava(BlockPos pos) {
        World world = getWorld();
        return world != null && world.getFluidState(pos).isTagged(FluidTags.LAVA);
    }

    private int getFuel(ItemStack stack) {
        return ForgeHooks.getBurnTime(stack) / 2;
    }

    @Override
    public String[] getMethods() {
        return methods;
    }

    @Override
    public Object[] invoke(int method, Object[] arguments) throws NoSuchMethodException {
        switch (method) {
            case 0:
                return new Object[]{getEnergy()};
            case 1:
                return new Object[]{output};
            case 2:
                return new Object[]{getBaseStorage()};
            case 3:
                return new Object[]{getBaseStorage() - getEnergy()};
            case 4:
                return new Object[]{lavaTank.getFluidAmount()};
            case 5:
                return new Object[]{lavaTank.getNeeded()};
            default:
                throw new NoSuchMethodException();
        }
    }

    @Override
    public double getTemp() {
        return temperature;
    }

    @Override
    public double getInverseConductionCoefficient() {
        return 1;
    }

    @Override
    public double getInsulationCoefficient(Direction side) {
        return side == Direction.DOWN ? 0 : 10000;
    }

    @Override
    public void transferHeatTo(double heat) {
        heatToAbsorb += heat;
    }

    @Override
    public double[] simulateHeat() {
        if (getTemp() > 0) {
            double carnotEfficiency = getTemp() / (getTemp() + IHeatTransfer.AMBIENT_TEMP);
            double heatLost = thermalEfficiency * getTemp();
            double workDone = heatLost * carnotEfficiency;
            transferHeatTo(-heatLost);
            setEnergy(getEnergy() + workDone);
        }
        return HeatUtils.simulate(this);
    }

    @Override
    public double applyTemperatureChange() {
        temperature += invHeatCapacity * heatToAbsorb;
        heatToAbsorb = 0;
        return temperature;
    }

    @Nullable
    @Override
    public IHeatTransfer getAdjacent(Direction side) {
        if (side == Direction.DOWN) {
            TileEntity adj = MekanismUtils.getTileEntity(getWorld(), pos.down());
            Optional<IHeatTransfer> capability = MekanismUtils.toOptional(CapabilityUtils.getCapability(adj, Capabilities.HEAT_TRANSFER_CAPABILITY, side.getOpposite()));
            if (capability.isPresent()) {
                return capability.get();
            }
        }
        return null;
    }

    @Override
    public boolean isCapabilityDisabled(@Nonnull Capability<?> capability, @Nullable Direction side) {
        if (capability == Capabilities.HEAT_TRANSFER_CAPABILITY && side == Direction.DOWN) {
            return true;
        }
        return super.isCapabilityDisabled(capability, side);
    }

    @Nonnull
    @Override
    public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> capability, @Nullable Direction side) {
        if (isCapabilityDisabled(capability, side)) {
            return LazyOptional.empty();
        } else if (capability == Capabilities.HEAT_TRANSFER_CAPABILITY) {
            return Capabilities.HEAT_TRANSFER_CAPABILITY.orEmpty(capability, LazyOptional.of(() -> this));
        }
        return super.getCapability(capability, side);
    }

    public double getProducingEnergy() {
        return producingEnergy;
    }

    public double getLastTransferLoss() {
        return lastTransferLoss;
    }

    public double getLastEnvironmentLoss() {
        return lastEnvironmentLoss;
    }

    @Override
    public int getRedstoneLevel() {
        return MekanismUtils.redstoneLevelFromContents(lavaTank.getFluidAmount(), lavaTank.getCapacity());
    }

    @Override
    public void addContainerTrackers(MekanismContainer container) {
        super.addContainerTrackers(container);
        container.track(SyncableDouble.create(this::getProducingEnergy, value -> producingEnergy = value));
        container.track(SyncableDouble.create(this::getLastTransferLoss, value -> lastTransferLoss = value));
        container.track(SyncableDouble.create(this::getLastEnvironmentLoss, value -> lastEnvironmentLoss = value));
    }
}