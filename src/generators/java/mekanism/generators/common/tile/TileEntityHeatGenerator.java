package mekanism.generators.common.tile;

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import java.util.Map;
import java.util.Optional;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import mekanism.api.Action;
import mekanism.api.Coord4D;
import mekanism.api.IHeatTransfer;
import mekanism.api.RelativeSide;
import mekanism.api.sustained.ISustainedData;
import mekanism.common.base.FluidHandlerWrapper;
import mekanism.common.base.IFluidHandlerWrapper;
import mekanism.common.capabilities.Capabilities;
import mekanism.common.capabilities.holder.slot.IInventorySlotHolder;
import mekanism.common.capabilities.holder.slot.InventorySlotHelper;
import mekanism.common.inventory.container.MekanismContainer;
import mekanism.common.inventory.container.sync.SyncableDouble;
import mekanism.common.inventory.container.sync.SyncableFluidStack;
import mekanism.common.inventory.slot.EnergyInventorySlot;
import mekanism.common.inventory.slot.FuelInventorySlot;
import mekanism.common.util.CapabilityUtils;
import mekanism.common.util.EnumUtils;
import mekanism.common.util.FluidContainerUtils;
import mekanism.common.util.FluidContainerUtils.FluidChecker;
import mekanism.common.util.HeatUtils;
import mekanism.common.util.ItemDataUtils;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.PipeUtils;
import mekanism.generators.common.config.MekanismGeneratorsConfig;
import mekanism.generators.common.registries.GeneratorsBlocks;
import net.minecraft.fluid.Fluids;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tags.FluidTags;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.ForgeHooks;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.IFluidTank;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandler.FluidAction;
import net.minecraftforge.fluids.capability.templates.FluidTank;

public class TileEntityHeatGenerator extends TileEntityGenerator implements IFluidHandlerWrapper, ISustainedData, IHeatTransfer {

    private static final String[] methods = new String[]{"getEnergy", "getOutput", "getMaxEnergy", "getEnergyNeeded", "getFuel", "getFuelNeeded"};
    /**
     * The FluidTank for this generator.
     */
    public FluidTank lavaTank = new FluidTank(24_000);
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
                //TODO: Do we want to at some point try to move this logic into the FuelSlot itself?
                if (FluidContainerUtils.isFluidContainer(fuelStack)) {
                    lavaTank.fill(FluidContainerUtils.extractFluid(lavaTank, fuelSlot, FluidChecker.check(Fluids.LAVA)), FluidAction.EXECUTE);
                } else {
                    int fuel = getFuel(fuelStack);
                    if (fuel > 0) {
                        int fuelNeeded = lavaTank.getSpace();
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

    @Override
    public void read(CompoundNBT nbtTags) {
        super.read(nbtTags);
        if (nbtTags.contains("lavaTank")) {
            lavaTank.readFromNBT(nbtTags.getCompound("lavaTank"));
        }
    }

    @Nonnull
    @Override
    public CompoundNBT write(CompoundNBT nbtTags) {
        super.write(nbtTags);
        if (!lavaTank.isEmpty()) {
            nbtTags.put("lavaTank", lavaTank.writeToNBT(new CompoundNBT()));
        }
        return nbtTags;
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
                return new Object[]{lavaTank.getSpace()};
            default:
                throw new NoSuchMethodException();
        }
    }

    @Override
    public int fill(Direction from, @Nonnull FluidStack resource, FluidAction fluidAction) {
        return lavaTank.fill(resource, fluidAction);
    }

    @Override
    public boolean canFill(Direction from, @Nonnull FluidStack fluid) {
        return fluid.getFluid().equals(Fluids.LAVA) && from != getDirection();
    }

    @Override
    public IFluidTank[] getTankInfo(Direction from) {
        if (from == getDirection()) {
            return PipeUtils.EMPTY;
        }
        return new IFluidTank[]{lavaTank};
    }

    @Override
    public IFluidTank[] getAllTanks() {
        return getTankInfo(null);
    }

    @Override
    public void writeSustainedData(ItemStack itemStack) {
        if (!lavaTank.isEmpty()) {
            ItemDataUtils.setCompound(itemStack, "lavaTank", lavaTank.getFluid().writeToNBT(new CompoundNBT()));
        }
    }

    @Override
    public void readSustainedData(ItemStack itemStack) {
        lavaTank.setFluid(FluidStack.loadFluidStackFromNBT(ItemDataUtils.getCompound(itemStack, "lavaTank")));
    }

    @Override
    public Map<String, String> getTileDataRemap() {
        Map<String, String> remap = new Object2ObjectOpenHashMap<>();
        remap.put("lavaTank", "lavaTank");
        return remap;
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

    @Nonnull
    @Override
    public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> capability, @Nullable Direction side) {
        if (capability == Capabilities.HEAT_TRANSFER_CAPABILITY && side == Direction.DOWN) {
            return Capabilities.HEAT_TRANSFER_CAPABILITY.orEmpty(capability, LazyOptional.of(() -> this));
        }
        if (capability == CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY && side != getDirection()) {
            return CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY.orEmpty(capability, LazyOptional.of(() -> new FluidHandlerWrapper(this, side)));
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
        container.track(SyncableFluidStack.create(lavaTank));
    }
}