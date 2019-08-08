package mekanism.generators.common.tile;

import javax.annotation.Nonnull;
import mekanism.api.TileNetworkList;
import mekanism.common.FluidSlot;
import mekanism.common.MekanismItem;
import mekanism.common.base.FluidHandlerWrapper;
import mekanism.common.base.IComparatorSupport;
import mekanism.common.base.IFluidHandlerWrapper;
import mekanism.common.base.ISustainedData;
import mekanism.common.config.MekanismConfig;
import mekanism.common.util.ChargeUtils;
import mekanism.common.util.ItemDataUtils;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.PipeUtils;
import mekanism.generators.common.GeneratorsBlock;
import net.minecraft.inventory.container.Container;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.Direction;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidTankInfo;
import net.minecraftforge.fluids.FluidUtil;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandler;

public class TileEntityBioGenerator extends TileEntityGenerator implements IFluidHandlerWrapper, ISustainedData, IComparatorSupport {

    private static final String[] methods = new String[]{"getEnergy", "getOutput", "getMaxEnergy", "getEnergyNeeded", "getBioFuel", "getBioFuelNeeded"};
    private static FluidTankInfo[] ALL_TANKS = new FluidTankInfo[0];
    /**
     * The FluidSlot biofuel instance for this generator.
     */
    public FluidSlot bioFuelSlot = new FluidSlot(24000, -1);

    private int currentRedstoneLevel;

    public TileEntityBioGenerator() {
        super(GeneratorsBlock.BIO_GENERATOR, MekanismConfig.current().generators.bioGeneration.val() * 2);
    }

    @Override
    public void onUpdate() {
        super.onUpdate();

        if (!getInventory().get(0).isEmpty()) {
            ChargeUtils.charge(1, this);
            FluidStack fluid = FluidUtil.getFluidContained(getInventory().get(0));
            if (fluid != null && FluidRegistry.isFluidRegistered("bioethanol")) {
                if (fluid.getFluid() == FluidRegistry.getFluid("bioethanol")) {
                    IFluidHandler handler = FluidUtil.getFluidHandler(getInventory().get(0));
                    FluidStack drained = handler.drain(bioFuelSlot.MAX_FLUID - bioFuelSlot.fluidStored, true);
                    if (drained != null) {
                        bioFuelSlot.fluidStored += drained.amount;
                    }
                }
            } else {
                int fuel = getFuel(getInventory().get(0));
                if (fuel > 0) {
                    int fuelNeeded = bioFuelSlot.MAX_FLUID - bioFuelSlot.fluidStored;
                    if (fuel <= fuelNeeded) {
                        bioFuelSlot.fluidStored += fuel;
                        if (!getInventory().get(0).getItem().getContainerItem(getInventory().get(0)).isEmpty()) {
                            getInventory().set(0, getInventory().get(0).getItem().getContainerItem(getInventory().get(0)));
                        } else {
                            getInventory().get(0).shrink(1);
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
            setEnergy(getEnergy() + MekanismConfig.current().generators.bioGeneration.val());
        } else if (!world.isRemote) {
            setActive(false);
        }
        if (!world.isRemote) {
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
            } else if (FluidRegistry.isFluidRegistered("bioethanol")) {
                FluidStack fluidContained = FluidUtil.getFluidContained(itemstack);
                if (fluidContained != null) {
                    return fluidContained.getFluid() == FluidRegistry.getFluid("bioethanol");
                }
            }
            return false;
        } else if (slotID == 1) {
            return ChargeUtils.canBeCharged(itemstack);
        }
        return true;
    }

    @Override
    public boolean canOperate() {
        return getEnergy() < getBaseStorage() && bioFuelSlot.fluidStored > 0 && MekanismUtils.canFunction(this);
    }

    @Override
    public void read(CompoundNBT nbtTags) {
        super.read(nbtTags);
        bioFuelSlot.fluidStored = nbtTags.getInt("bioFuelStored");
    }

    @Nonnull
    @Override
    public CompoundNBT write(CompoundNBT nbtTags) {
        super.write(nbtTags);
        nbtTags.putInt("bioFuelStored", bioFuelSlot.fluidStored);
        return nbtTags;
    }

    public int getFuel(ItemStack itemstack) {
        return MekanismItem.BIO_FUEL.itemMatches(itemstack) ? 200 : 0;
    }

    /**
     * Gets the scaled fuel level for the GUI.
     *
     * @param i - multiplier
     *
     * @return Scaled fuel level
     */
    public int getScaledFuelLevel(int i) {
        return bioFuelSlot.fluidStored * i / bioFuelSlot.MAX_FLUID;
    }

    @Nonnull
    @Override
    public int[] getSlotsForFace(@Nonnull Direction side) {
        return side == getRightSide() ? new int[]{1} : new int[]{0};
    }

    @Override
    public void handlePacketData(PacketBuffer dataStream) {
        super.handlePacketData(dataStream);
        if (world.isRemote) {
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
                return new Object[]{bioFuelSlot.fluidStored};
            case 5:
                return new Object[]{bioFuelSlot.MAX_FLUID - bioFuelSlot.fluidStored};
            default:
                throw new NoSuchMethodException();
        }
    }

    @Override
    public int fill(Direction from, @Nonnull FluidStack resource, boolean doFill) {
        int fuelNeeded = bioFuelSlot.MAX_FLUID - bioFuelSlot.fluidStored;
        int fuelTransfer = Math.min(resource.amount, fuelNeeded);
        if (doFill) {
            bioFuelSlot.setFluid(bioFuelSlot.fluidStored + fuelTransfer);
        }
        return fuelTransfer;
    }

    @Override
    public boolean canFill(Direction from, @Nonnull FluidStack fluid) {
        return from != getDirection() && fluid.getFluid() == FluidRegistry.getFluid("bioethanol");
    }

    @Override
    public FluidTankInfo[] getTankInfo(Direction from) {
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
    public boolean hasCapability(@Nonnull Capability<?> capability, Direction side) {
        return (side != getDirection() && capability == CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY) || super.hasCapability(capability, side);
    }

    @Override
    public <T> T getCapability(@Nonnull Capability<T> capability, Direction side) {
        if (side != getDirection() && capability == CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY) {
            return CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY.cast(new FluidHandlerWrapper(this, side));
        }
        return super.getCapability(capability, side);
    }

    @Override
    public FluidTankInfo[] getAllTanks() {
        return ALL_TANKS;
    }

    @Override
    public int getRedstoneLevel() {
        return Container.calcRedstoneFromInventory(this);
    }
}