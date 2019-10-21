package mekanism.generators.common.tile;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import mekanism.api.Action;
import mekanism.api.Coord4D;
import mekanism.api.IHeatTransfer;
import mekanism.api.RelativeSide;
import mekanism.api.TileNetworkList;
import mekanism.api.sustained.ISustainedData;
import mekanism.common.base.FluidHandlerWrapper;
import mekanism.common.base.IComparatorSupport;
import mekanism.common.base.IFluidHandlerWrapper;
import mekanism.common.capabilities.Capabilities;
import mekanism.common.inventory.slot.EnergyInventorySlot;
import mekanism.common.inventory.slot.FuelInventorySlot;
import mekanism.common.inventory.slot.holder.IInventorySlotHolder;
import mekanism.common.inventory.slot.holder.InventorySlotHelper;
import mekanism.common.util.CapabilityUtils;
import mekanism.common.util.ChargeUtils;
import mekanism.common.util.EnumUtils;
import mekanism.common.util.FluidContainerUtils;
import mekanism.common.util.FluidContainerUtils.FluidChecker;
import mekanism.common.util.HeatUtils;
import mekanism.common.util.ItemDataUtils;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.PipeUtils;
import mekanism.common.util.TileUtils;
import mekanism.generators.common.GeneratorsBlock;
import mekanism.generators.common.config.MekanismGeneratorsConfig;
import net.minecraft.block.Blocks;
import net.minecraft.fluid.Fluids;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.PacketBuffer;
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

public class TileEntityHeatGenerator extends TileEntityGenerator implements IFluidHandlerWrapper, ISustainedData, IHeatTransfer, IComparatorSupport {

    private static final String[] methods = new String[]{"getEnergy", "getOutput", "getMaxEnergy", "getEnergyNeeded", "getFuel", "getFuelNeeded"};
    /**
     * The FluidTank for this generator.
     */
    public FluidTank lavaTank = new FluidTank(24000);
    public double temperature = 0;
    public double thermalEfficiency = 0.5D;
    public double invHeatCapacity = 1;
    public double heatToAbsorb = 0;
    public double producingEnergy;
    public double lastTransferLoss;
    public double lastEnvironmentLoss;
    private int currentRedstoneLevel;

    private FuelInventorySlot fuelSlot;
    private EnergyInventorySlot energySlot;

    public TileEntityHeatGenerator() {
        super(GeneratorsBlock.HEAT_GENERATOR, MekanismGeneratorsConfig.generators.heatGeneration.get() * 2);
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
            ChargeUtils.charge(energySlot.getStack(), this);
            ItemStack fuelStack = fuelSlot.getStack();
            if (!fuelStack.isEmpty()) {
                if (FluidContainerUtils.isFluidContainer(fuelStack)) {
                    lavaTank.fill(FluidContainerUtils.extractFluid(lavaTank, fuelSlot, FluidChecker.check(Fluids.LAVA)), FluidAction.EXECUTE);
                } else {
                    int fuel = getFuel(fuelStack);
                    if (fuel > 0) {
                        int fuelNeeded = lavaTank.getCapacity() - lavaTank.getFluid().getAmount();
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

            World world = getWorld();
            if (world != null) {
                int newRedstoneLevel = getRedstoneLevel();
                if (newRedstoneLevel != currentRedstoneLevel) {
                    world.updateComparatorOutputLevel(pos, getBlockType());
                    currentRedstoneLevel = newRedstoneLevel;
                }
            }
        }
    }

    @Override
    public boolean canOperate() {
        return getEnergy() < getBaseStorage() && lavaTank.getFluid().getAmount() >= 10 && MekanismUtils.canFunction(this);
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
        if (!lavaTank.getFluid().isEmpty()) {
            nbtTags.put("lavaTank", lavaTank.writeToNBT(new CompoundNBT()));
        }
        return nbtTags;
    }

    public double getBoost() {
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
        //TODO: FluidTags.LAVA?
        return world != null && world.getBlockState(pos).getBlock() == Blocks.LAVA;
    }

    public int getFuel(ItemStack stack) {
        return ForgeHooks.getBurnTime(stack) / 2;
    }

    /**
     * Gets the scaled fuel level for the GUI.
     *
     * @param i - multiplier
     *
     * @return Scaled fuel level
     */
    public int getScaledFuelLevel(int i) {
        return lavaTank.getFluid().getAmount() * i / lavaTank.getCapacity();
    }

    @Override
    public void handlePacketData(PacketBuffer dataStream) {
        super.handlePacketData(dataStream);

        if (isRemote()) {
            producingEnergy = dataStream.readDouble();

            lastTransferLoss = dataStream.readDouble();
            lastEnvironmentLoss = dataStream.readDouble();

            TileUtils.readTankData(dataStream, lavaTank);
        }
    }

    @Override
    public TileNetworkList getNetworkedData(TileNetworkList data) {
        super.getNetworkedData(data);
        data.add(producingEnergy);
        data.add(lastTransferLoss);
        data.add(lastEnvironmentLoss);
        TileUtils.addTankData(data, lavaTank);
        return data;
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
                return new Object[]{lavaTank.getFluid().getAmount()};
            case 5:
                return new Object[]{lavaTank.getCapacity() - lavaTank.getFluid().getAmount()};
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
        if (!lavaTank.getFluid().isEmpty()) {
            ItemDataUtils.setCompound(itemStack, "lavaTank", lavaTank.getFluid().writeToNBT(new CompoundNBT()));
        }
    }

    @Override
    public void readSustainedData(ItemStack itemStack) {
        lavaTank.setFluid(FluidStack.loadFluidStackFromNBT(ItemDataUtils.getCompound(itemStack, "lavaTank")));
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
            return CapabilityUtils.getCapabilityHelper(adj, Capabilities.HEAT_TRANSFER_CAPABILITY, side.getOpposite()).getValue();
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

    @Override
    public int getRedstoneLevel() {
        return MekanismUtils.redstoneLevelFromContents(lavaTank.getFluidAmount(), lavaTank.getCapacity());
    }
}