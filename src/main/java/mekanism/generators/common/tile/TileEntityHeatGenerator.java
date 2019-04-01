package mekanism.generators.common.tile;

import io.netty.buffer.ByteBuf;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import mekanism.api.Coord4D;
import mekanism.api.IHeatTransfer;
import mekanism.common.base.FluidHandlerWrapper;
import mekanism.common.base.IFluidHandlerWrapper;
import mekanism.common.base.ISustainedData;
import mekanism.api.TileNetworkList;
import mekanism.common.capabilities.Capabilities;
import mekanism.common.config.MekanismConfig.generators;
import mekanism.common.util.CapabilityUtils;
import mekanism.common.util.ChargeUtils;
import mekanism.common.util.FluidContainerUtils;
import mekanism.common.util.FluidContainerUtils.FluidChecker;
import mekanism.common.util.HeatUtils;
import mekanism.common.util.ItemDataUtils;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.PipeUtils;
import mekanism.common.util.TileUtils;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityFurnace;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidTank;
import net.minecraftforge.fluids.FluidTankInfo;
import net.minecraftforge.fluids.FluidUtil;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fml.common.FMLCommonHandler;

public class TileEntityHeatGenerator extends TileEntityGenerator implements IFluidHandlerWrapper, ISustainedData,
      IHeatTransfer {

    private static final String[] methods = new String[]{"getEnergy", "getOutput", "getMaxEnergy", "getEnergyNeeded",
          "getFuel", "getFuelNeeded"};
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

    public TileEntityHeatGenerator() {
        super("heat", "HeatGenerator", 160000, generators.heatGeneration * 2);
        inventory = NonNullList.withSize(2, ItemStack.EMPTY);
    }

    @Override
    public void onUpdate() {
        super.onUpdate();

        if (!world.isRemote) {
            ChargeUtils.charge(1, this);

            if (!inventory.get(0).isEmpty()) {
                if (FluidContainerUtils.isFluidContainer(inventory.get(0))) {
                    lavaTank.fill(FluidContainerUtils
                          .extractFluid(lavaTank, this, 0, FluidChecker.check(FluidRegistry.LAVA)), true);
                } else {
                    int fuel = getFuel(inventory.get(0));

                    if (fuel > 0) {
                        int fuelNeeded =
                              lavaTank.getCapacity() - (lavaTank.getFluid() != null ? lavaTank.getFluid().amount : 0);

                        if (fuel <= fuelNeeded) {
                            lavaTank.fill(new FluidStack(FluidRegistry.LAVA, fuel), true);

                            if (!inventory.get(0).getItem().getContainerItem(inventory.get(0)).isEmpty()) {
                                inventory.set(0, inventory.get(0).getItem().getContainerItem(inventory.get(0)));
                            } else {
                                inventory.get(0).shrink(1);
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
                transferHeatTo(generators.heatGeneration);
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
        return electricityStored < BASE_MAX_ENERGY && lavaTank.getFluid() != null && lavaTank.getFluid().amount >= 10
              && MekanismUtils.canFunction(this);
    }

    @Override
    public void readFromNBT(NBTTagCompound nbtTags) {
        super.readFromNBT(nbtTags);

        if (nbtTags.hasKey("lavaTank")) {
            lavaTank.readFromNBT(nbtTags.getCompoundTag("lavaTank"));
        }
    }

    @Nonnull
    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound nbtTags) {
        super.writeToNBT(nbtTags);

        if (lavaTank.getFluid() != null) {
            nbtTags.setTag("lavaTank", lavaTank.writeToNBT(new NBTTagCompound()));
        }

        return nbtTags;
    }

    @Override
    public boolean canExtractItem(int slotID, @Nonnull ItemStack itemstack, @Nonnull EnumFacing side) {
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

        for (EnumFacing side : EnumFacing.VALUES) {
            Coord4D coord = Coord4D.get(this).offset(side);

            if (isLava(coord.getPos())) {
                lavaBoost++;
            }
        }

        if (world.provider.getDimension() == -1) {
            netherBoost = generators.heatGenerationNether;
        }

        return (generators.heatGenerationLava * lavaBoost) + netherBoost;
    }

    private boolean isLava(BlockPos pos) {
        return world.getBlockState(pos).getBlock() == Blocks.LAVA;
    }

    public int getFuel(ItemStack itemstack) {
        return TileEntityFurnace.getItemBurnTime(itemstack) / 2;
    }

    @Nonnull
    @Override
    public int[] getSlotsForFace(@Nonnull EnumFacing side) {
        return side == MekanismUtils.getRight(facing) ? new int[]{1} : new int[]{0};
    }

    /**
     * Gets the scaled fuel level for the GUI.
     *
     * @param i - multiplier
     * @return Scaled fuel level
     */
    public int getScaledFuelLevel(int i) {
        return (lavaTank.getFluid() != null ? lavaTank.getFluid().amount : 0) * i / lavaTank.getCapacity();
    }

    @Override
    public void handlePacketData(ByteBuf dataStream) {
        super.handlePacketData(dataStream);

        if (FMLCommonHandler.instance().getEffectiveSide().isClient()) {
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
    public Object[] invoke(int method, Object[] arguments) throws Exception {
        switch (method) {
            case 0:
                return new Object[]{electricityStored};
            case 1:
                return new Object[]{output};
            case 2:
                return new Object[]{BASE_MAX_ENERGY};
            case 3:
                return new Object[]{(BASE_MAX_ENERGY - electricityStored)};
            case 4:
                return new Object[]{lavaTank.getFluid() != null ? lavaTank.getFluid().amount : 0};
            case 5:
                return new Object[]{
                      lavaTank.getCapacity() - (lavaTank.getFluid() != null ? lavaTank.getFluid().amount : 0)};
            default:
                throw new NoSuchMethodException();
        }
    }

    @Override
    public int fill(EnumFacing from, @Nullable FluidStack resource, boolean doFill) {
        if (resource != null && resource.getFluid() == FluidRegistry.LAVA && from != facing) {
            return lavaTank.fill(resource, doFill);
        }

        return 0;
    }

    @Override
    public FluidStack drain(EnumFacing from, int maxDrain, boolean doDrain) {
        return null;
    }

    @Override
    public FluidStack drain(EnumFacing from, @Nullable FluidStack resource, boolean doDrain) {
        return null;
    }

    @Override
    public boolean canFill(EnumFacing from, @Nullable FluidStack fluid) {
        return fluid != null && fluid.getFluid().equals(FluidRegistry.LAVA) && from != facing;
    }

    @Override
    public boolean canDrain(EnumFacing from, @Nullable FluidStack fluid) {
        return false;
    }

    @Override
    public FluidTankInfo[] getTankInfo(EnumFacing from) {
        if (from == facing) {
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
            ItemDataUtils.setCompound(itemStack, "lavaTank", lavaTank.getFluid().writeToNBT(new NBTTagCompound()));
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
    public double getInsulationCoefficient(EnumFacing side) {
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
    public boolean canConnectHeat(EnumFacing side) {
        return side == EnumFacing.DOWN;
    }

    @Override
    public IHeatTransfer getAdjacent(EnumFacing side) {
        if (canConnectHeat(side)) {
            TileEntity adj = Coord4D.get(this).offset(side).getTileEntity(world);

            if (CapabilityUtils.hasCapability(adj, Capabilities.HEAT_TRANSFER_CAPABILITY, side.getOpposite())) {
                return CapabilityUtils.getCapability(adj, Capabilities.HEAT_TRANSFER_CAPABILITY, side.getOpposite());
            }
        }

        return null;
    }

    @Override
    public boolean hasCapability(@Nonnull Capability<?> capability, EnumFacing side) {
        return capability == Capabilities.HEAT_TRANSFER_CAPABILITY ||
              (side != facing && capability == CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY) || super
              .hasCapability(capability, side);
    }

    @Override
    public <T> T getCapability(@Nonnull Capability<T> capability, EnumFacing side) {
        if (capability == Capabilities.HEAT_TRANSFER_CAPABILITY) {
            return Capabilities.HEAT_TRANSFER_CAPABILITY.cast(this);
        }

        if (side != facing && capability == CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY) {
            return CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY.cast(new FluidHandlerWrapper(this, side));
        }

        return super.getCapability(capability, side);
    }
}
