package mekanism.generators.common.tile;

import io.netty.buffer.ByteBuf;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import mekanism.common.FluidSlot;
import mekanism.common.MekanismItems;
import mekanism.common.base.FluidHandlerWrapper;
import mekanism.common.base.IFluidHandlerWrapper;
import mekanism.common.base.ISustainedData;
import mekanism.api.TileNetworkList;
import mekanism.common.config.MekanismConfig.generators;
import mekanism.common.util.ChargeUtils;
import mekanism.common.util.ItemDataUtils;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.PipeUtils;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.NonNullList;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidTankInfo;
import net.minecraftforge.fluids.FluidUtil;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fml.common.FMLCommonHandler;

public class TileEntityBioGenerator extends TileEntityGenerator implements IFluidHandlerWrapper, ISustainedData {

    private static final String[] methods = new String[]{"getEnergy", "getOutput", "getMaxEnergy", "getEnergyNeeded",
          "getBioFuel", "getBioFuelNeeded"};
    private static FluidTankInfo[] ALL_TANKS = new FluidTankInfo[0];
    /**
     * The FluidSlot biofuel instance for this generator.
     */
    public FluidSlot bioFuelSlot = new FluidSlot(24000, -1);

    public TileEntityBioGenerator() {
        super("bio", "BioGenerator", 160000, generators.bioGeneration * 2);
        inventory = NonNullList.withSize(2, ItemStack.EMPTY);
    }

    @Override
    public void onUpdate() {
        super.onUpdate();

        if (!inventory.get(0).isEmpty()) {
            ChargeUtils.charge(1, this);

            FluidStack fluid = FluidUtil.getFluidContained(inventory.get(0));

            if (fluid != null && FluidRegistry.isFluidRegistered("bioethanol")) {
                if (fluid.getFluid() == FluidRegistry.getFluid("bioethanol")) {
                    IFluidHandler handler = FluidUtil.getFluidHandler(inventory.get(0));
                    FluidStack drained = handler.drain(bioFuelSlot.MAX_FLUID - bioFuelSlot.fluidStored, true);

                    if (drained != null) {
                        bioFuelSlot.fluidStored += drained.amount;
                    }
                }
            } else {
                int fuel = getFuel(inventory.get(0));

                if (fuel > 0) {
                    int fuelNeeded = bioFuelSlot.MAX_FLUID - bioFuelSlot.fluidStored;

                    if (fuel <= fuelNeeded) {
                        bioFuelSlot.fluidStored += fuel;

                        if (!inventory.get(0).getItem().getContainerItem(inventory.get(0)).isEmpty()) {
                            inventory.set(0, inventory.get(0).getItem().getContainerItem(inventory.get(0)));
                        } else {
                            inventory.get(0).shrink(1);
                        }
                    }
                }
            }
        }

        if (canOperate()) {
            if (!world.isRemote) {
                setActive(true);
            }

            bioFuelSlot.setFluid(bioFuelSlot.fluidStored - 1);
            setEnergy(electricityStored + generators.bioGeneration);
        } else {
            if (!world.isRemote) {
                setActive(false);
            }
        }
    }

    @Override
    public boolean isItemValidForSlot(int slotID, @Nonnull ItemStack itemstack) {
        if (slotID == 0) {
            if (getFuel(itemstack) > 0) {
                return true;
            } else {
                if (FluidRegistry.isFluidRegistered("bioethanol")) {
                    FluidStack fluidContained = FluidUtil.getFluidContained(itemstack);
                    if (fluidContained != null) {
                        return fluidContained.getFluid() == FluidRegistry.getFluid("bioethanol");
                    }
                }

                return false;
            }
        } else if (slotID == 1) {
            return ChargeUtils.canBeCharged(itemstack);
        }

        return true;
    }

    @Override
    public boolean canOperate() {
        return electricityStored < BASE_MAX_ENERGY && bioFuelSlot.fluidStored > 0 && MekanismUtils.canFunction(this);
    }

    @Override
    public void readFromNBT(NBTTagCompound nbtTags) {
        super.readFromNBT(nbtTags);

        bioFuelSlot.fluidStored = nbtTags.getInteger("bioFuelStored");
    }

    @Nonnull
    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound nbtTags) {
        super.writeToNBT(nbtTags);

        nbtTags.setInteger("bioFuelStored", bioFuelSlot.fluidStored);

        return nbtTags;
    }

    public int getFuel(ItemStack itemstack) {
        return itemstack.getItem() == MekanismItems.BioFuel ? 200 : 0;
    }

    /**
     * Gets the scaled fuel level for the GUI.
     *
     * @param i - multiplier
     * @return Scaled fuel level
     */
    public int getScaledFuelLevel(int i) {
        return bioFuelSlot.fluidStored * i / bioFuelSlot.MAX_FLUID;
    }

    @Nonnull
    @Override
    public int[] getSlotsForFace(@Nonnull EnumFacing side) {
        return side == MekanismUtils.getRight(facing) ? new int[]{1} : new int[]{0};
    }

    @Override
    public boolean canSetFacing(int facing) {
        return facing != 0 && facing != 1;
    }

    @Override
    public void handlePacketData(ByteBuf dataStream) {
        super.handlePacketData(dataStream);

        if (FMLCommonHandler.instance().getEffectiveSide().isClient()) {
            bioFuelSlot.fluidStored = dataStream.readInt();
        }
    }

    @Override
    public TileNetworkList getNetworkedData(TileNetworkList data) {
        super.getNetworkedData(data);
        data.add(bioFuelSlot.fluidStored);
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
                return new Object[]{bioFuelSlot.fluidStored};
            case 5:
                return new Object[]{bioFuelSlot.MAX_FLUID - bioFuelSlot.fluidStored};
            default:
                throw new NoSuchMethodException();
        }
    }

    @Override
    public int fill(EnumFacing from, @Nullable FluidStack resource, boolean doFill) {
        if (resource != null && FluidRegistry.isFluidRegistered("bioethanol") && from != facing) {
            if (resource.getFluid() == FluidRegistry.getFluid("bioethanol")) {
                int fuelTransfer;
                int fuelNeeded = bioFuelSlot.MAX_FLUID - bioFuelSlot.fluidStored;
                int attemptTransfer = resource.amount;

                if (attemptTransfer <= fuelNeeded) {
                    fuelTransfer = attemptTransfer;
                } else {
                    fuelTransfer = fuelNeeded;
                }

                if (doFill) {
                    bioFuelSlot.setFluid(bioFuelSlot.fluidStored + fuelTransfer);
                }

                return fuelTransfer;
            }
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
        return fluid != null && fluid.getFluid().equals(FluidRegistry.getFluid("bioethanol"));
    }

    @Override
    public boolean canDrain(EnumFacing from, @Nullable FluidStack fluid) {
        return false;
    }

    @Override
    public FluidTankInfo[] getTankInfo(EnumFacing from) {
        return PipeUtils.EMPTY;
    }

    @Override
    public void writeSustainedData(ItemStack itemStack) {
        ItemDataUtils.setInt(itemStack, "fluidStored", bioFuelSlot.fluidStored);
    }

    @Override
    public void readSustainedData(ItemStack itemStack) {
        bioFuelSlot.setFluid(ItemDataUtils.getInt(itemStack, "fluidStored"));
    }

    @Override
    public boolean hasCapability(@Nonnull Capability<?> capability, EnumFacing side) {
        return (side != facing && capability == CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY) || super
              .hasCapability(capability, side);
    }

    @Override
    public <T> T getCapability(@Nonnull Capability<T> capability, EnumFacing side) {
        if (side != facing && capability == CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY) {
            return (T) new FluidHandlerWrapper(this, side);
        }

        return super.getCapability(capability, side);
    }

    @Override
    public FluidTankInfo[] getAllTanks() {
        return ALL_TANKS;
    }
}
