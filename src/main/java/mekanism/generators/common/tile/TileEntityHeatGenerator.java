package mekanism.generators.common.tile;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import mekanism.api.Coord4D;
import mekanism.api.IHeatTransfer;
import mekanism.api.TileNetworkList;
import mekanism.common.base.FluidHandlerWrapper;
import mekanism.common.base.IComparatorSupport;
import mekanism.common.base.IFluidHandlerWrapper;
import mekanism.common.base.ISustainedData;
import mekanism.common.capabilities.Capabilities;
import mekanism.common.config.MekanismConfig;
import mekanism.common.util.CapabilityUtils;
import mekanism.common.util.ChargeUtils;
import mekanism.common.util.FluidContainerUtils;
import mekanism.common.util.FluidContainerUtils.FluidChecker;
import mekanism.common.util.HeatUtils;
import mekanism.common.util.ItemDataUtils;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.PipeUtils;
import mekanism.common.util.TileUtils;
import mekanism.generators.common.GeneratorsBlock;
import net.minecraft.block.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.PacketBuffer;
import net.minecraft.tileentity.FurnaceTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidTank;
import net.minecraftforge.fluids.FluidTankInfo;
import net.minecraftforge.fluids.FluidUtil;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;

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

    public TileEntityHeatGenerator() {
        super(GeneratorsBlock.HEAT_GENERATOR, MekanismConfig.current().generators.heatGeneration.val() * 2);
    }

    @Override
    public void onUpdate() {
        super.onUpdate();

        if (!world.isRemote) {
            ChargeUtils.charge(1, this);
            if (!getInventory().get(0).isEmpty()) {
                if (FluidContainerUtils.isFluidContainer(getInventory().get(0))) {
                    lavaTank.fill(FluidContainerUtils.extractFluid(lavaTank, this, 0, FluidChecker.check(FluidRegistry.LAVA)), true);
                } else {
                    int fuel = getFuel(getInventory().get(0));
                    if (fuel > 0) {
                        int fuelNeeded = lavaTank.getCapacity() - (lavaTank.getFluid() != null ? lavaTank.getFluid().amount : 0);
                        if (fuel <= fuelNeeded) {
                            lavaTank.fill(new FluidStack(FluidRegistry.LAVA, fuel), true);
                            if (!getInventory().get(0).getItem().getContainerItem(getInventory().get(0)).isEmpty()) {
                                getInventory().set(0, getInventory().get(0).getItem().getContainerItem(getInventory().get(0)));
                            } else {
                                getInventory().get(0).shrink(1);
                            }
                        }
                    }
                }
            }

            double prev = getEnergy();
            transferHeatTo(getBoost());
            if (canOperate()) {
                setActive(true);
                lavaTank.drain(10, true);
                transferHeatTo(MekanismConfig.current().generators.heatGeneration.val());
            } else {
                setActive(false);
            }

            double[] loss = simulateHeat();
            applyTemperatureChange();
            lastTransferLoss = loss[0];
            lastEnvironmentLoss = loss[1];
            producingEnergy = getEnergy() - prev;

            int newRedstoneLevel = getRedstoneLevel();
            if (newRedstoneLevel != currentRedstoneLevel) {
                world.updateComparatorOutputLevel(pos, getBlockType());
                currentRedstoneLevel = newRedstoneLevel;
            }
        }
    }

    @Override
    public boolean isItemValidForSlot(int slotID, @Nonnull ItemStack itemstack) {
        if (slotID == 0) {
            if (getFuel(itemstack) > 0) {
                return true;
            }
            FluidStack fluidContained = FluidUtil.getFluidContained(itemstack);
            return fluidContained != null && fluidContained.getFluid() == FluidRegistry.LAVA;
        } else if (slotID == 1) {
            return ChargeUtils.canBeCharged(itemstack);
        }
        return true;
    }

    @Override
    public boolean canOperate() {
        return getEnergy() < getBaseStorage() && lavaTank.getFluid() != null && lavaTank.getFluid().amount >= 10 && MekanismUtils.canFunction(this);
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
        if (lavaTank.getFluid() != null) {
            nbtTags.put("lavaTank", lavaTank.writeToNBT(new CompoundNBT()));
        }
        return nbtTags;
    }

    @Override
    public boolean canExtractItem(int slotID, @Nonnull ItemStack itemstack, @Nonnull Direction side) {
        if (slotID == 1) {
            return ChargeUtils.canBeOutputted(itemstack, true);
        } else if (slotID == 0) {
            return FluidUtil.getFluidContained(itemstack) == null;
        }
        return false;
    }

    public double getBoost() {
        int lavaBoost = 0;
        double netherBoost = 0D;
        for (Direction side : Direction.values()) {
            Coord4D coord = Coord4D.get(this).offset(side);
            if (isLava(coord.getPos())) {
                lavaBoost++;
            }
        }
        if (world.getDimension().isNether()) {
            netherBoost = MekanismConfig.current().generators.heatGenerationNether.val();
        }
        return (MekanismConfig.current().generators.heatGenerationLava.val() * lavaBoost) + netherBoost;
    }

    private boolean isLava(BlockPos pos) {
        return world.getBlockState(pos).getBlock() == Blocks.LAVA;
    }

    public int getFuel(ItemStack itemstack) {
        return FurnaceTileEntity.getItemBurnTime(itemstack) / 2;
    }

    @Nonnull
    @Override
    public int[] getSlotsForFace(@Nonnull Direction side) {
        return side == getRightSide() ? new int[]{1} : new int[]{0};
    }

    /**
     * Gets the scaled fuel level for the GUI.
     *
     * @param i - multiplier
     *
     * @return Scaled fuel level
     */
    public int getScaledFuelLevel(int i) {
        return (lavaTank.getFluid() != null ? lavaTank.getFluid().amount : 0) * i / lavaTank.getCapacity();
    }

    @Override
    public void handlePacketData(PacketBuffer dataStream) {
        super.handlePacketData(dataStream);

        if (world.isRemote) {
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
                return new Object[]{lavaTank.getFluid() != null ? lavaTank.getFluid().amount : 0};
            case 5:
                return new Object[]{lavaTank.getCapacity() - (lavaTank.getFluid() != null ? lavaTank.getFluid().amount : 0)};
            default:
                throw new NoSuchMethodException();
        }
    }

    @Override
    public int fill(Direction from, @Nonnull FluidStack resource, boolean doFill) {
        return lavaTank.fill(resource, doFill);
    }

    @Override
    public boolean canFill(Direction from, @Nonnull FluidStack fluid) {
        return fluid.getFluid().equals(FluidRegistry.LAVA) && from != getDirection();
    }

    @Override
    public FluidTankInfo[] getTankInfo(Direction from) {
        if (from == getDirection()) {
            return PipeUtils.EMPTY;
        }
        return new FluidTankInfo[]{lavaTank.getInfo()};
    }

    @Override
    public FluidTankInfo[] getAllTanks() {
        return getTankInfo(null);
    }

    @Override
    public void writeSustainedData(ItemStack itemStack) {
        if (lavaTank.getFluid() != null) {
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
        return canConnectHeat(side) ? 0 : 10000;
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

    @Override
    public boolean canConnectHeat(Direction side) {
        return side == Direction.DOWN;
    }

    @Nullable
    @Override
    public IHeatTransfer getAdjacent(Direction side) {
        if (canConnectHeat(side)) {
            TileEntity adj = Coord4D.get(this).offset(side).getTileEntity(world);
            return CapabilityUtils.getCapabilityHelper(adj, Capabilities.HEAT_TRANSFER_CAPABILITY, side.getOpposite()).getValue();
        }
        return null;
    }

    @Nonnull
    @Override
    public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> capability, @Nullable Direction side) {
        if (capability == Capabilities.HEAT_TRANSFER_CAPABILITY) {
            return Capabilities.HEAT_TRANSFER_CAPABILITY.orEmpty(capability, LazyOptional.of(() -> this));
        }
        if (side != getDirection() && capability == CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY) {
            return CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY.orEmpty(capability, LazyOptional.of(() -> new FluidHandlerWrapper(this, side)));
        }
        return super.getCapability(capability, side);
    }

    @Override
    public int getRedstoneLevel() {
        return MekanismUtils.redstoneLevelFromContents(lavaTank.getFluidAmount(), lavaTank.getCapacity());
    }
}