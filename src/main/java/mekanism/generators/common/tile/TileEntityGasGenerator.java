package mekanism.generators.common.tile;

import io.netty.buffer.ByteBuf;
import javax.annotation.Nonnull;
import mekanism.api.TileNetworkList;
import mekanism.api.gas.Gas;
import mekanism.api.gas.GasStack;
import mekanism.api.gas.GasTank;
import mekanism.api.gas.GasTankInfo;
import mekanism.api.gas.IGasHandler;
import mekanism.api.gas.IGasItem;
import mekanism.common.FuelHandler;
import mekanism.common.FuelHandler.FuelGas;
import mekanism.common.base.ISustainedData;
import mekanism.common.capabilities.Capabilities;
import mekanism.common.config.MekanismConfig.general;
import mekanism.common.util.ChargeUtils;
import mekanism.common.util.GasUtils;
import mekanism.common.util.ItemDataUtils;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.TileUtils;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.NonNullList;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.fml.common.FMLCommonHandler;

public class TileEntityGasGenerator extends TileEntityGenerator implements IGasHandler, ISustainedData {

    private static final String[] methods = new String[]{"getEnergy", "getOutput", "getMaxEnergy", "getEnergyNeeded",
          "getGas", "getGasNeeded"};
    /**
     * The maximum amount of gas this block can store.
     */
    public int MAX_GAS = 18000;
    /**
     * The tank this block is storing fuel in.
     */
    public GasTank fuelTank;
    public int burnTicks = 0;
    public int maxBurnTicks;
    public double generationRate = 0;
    public int clientUsed;

    public TileEntityGasGenerator() {
        super("gas", "GasGenerator", general.FROM_H2 * 100, general.FROM_H2 * 2);
        inventory = NonNullList.withSize(2, ItemStack.EMPTY);
        fuelTank = new GasTank(MAX_GAS);
    }

    @Override
    public void onUpdate() {
        super.onUpdate();

        if (!world.isRemote) {
            ChargeUtils.charge(1, this);

            if (!inventory.get(0).isEmpty() && fuelTank.getStored() < MAX_GAS) {
                Gas gasType = null;

                if (fuelTank.getGas() != null) {
                    gasType = fuelTank.getGas().getGas();
                } else if (!inventory.get(0).isEmpty() && inventory.get(0).getItem() instanceof IGasItem) {
                    if (((IGasItem) inventory.get(0).getItem()).getGas(inventory.get(0)) != null) {
                        gasType = ((IGasItem) inventory.get(0).getItem()).getGas(inventory.get(0)).getGas();
                    }
                }

                if (gasType != null && FuelHandler.getFuel(gasType) != null) {
                    GasStack removed = GasUtils.removeGas(inventory.get(0), gasType, fuelTank.getNeeded());
                    boolean isTankEmpty = (fuelTank.getGas() == null);

                    int fuelReceived = fuelTank.receive(removed, true);

                    if (fuelReceived > 0 && isTankEmpty) {
                        output = FuelHandler.getFuel(fuelTank.getGas().getGas()).energyPerTick * 2;
                    }
                }
            }

            boolean operate = canOperate();

            if (operate && getEnergy() + generationRate < getMaxEnergy()) {
                setActive(true);

                FuelGas fuel;

                if (fuelTank.getStored() != 0) {
                    fuel = FuelHandler.getFuel(fuelTank.getGas().getGas());
                    maxBurnTicks = fuel.burnTicks;
                    generationRate = fuel.energyPerTick;
                }

                int toUse = getToUse();

                output = Math.max(general.FROM_H2 * 2, generationRate * getToUse() * 2);

                int total = burnTicks + fuelTank.getStored() * maxBurnTicks;
                total -= toUse;

                setEnergy(getEnergy() + generationRate * toUse);

                if (fuelTank.getStored() > 0) {
                    fuelTank.setGas(new GasStack(fuelTank.getGasType(), total / maxBurnTicks));
                }

                burnTicks = total % maxBurnTicks;
                clientUsed = toUse;
            } else {
                if (!operate) {
                    reset();
                }

                clientUsed = 0;
                setActive(false);
            }
        }
    }

    public void reset() {
        burnTicks = 0;
        maxBurnTicks = 0;
        generationRate = 0;
        output = general.FROM_H2 * 2;
    }

    public int getToUse() {
        if (generationRate == 0 || fuelTank.getGas() == null) {
            return 0;
        }

        int max = (int) Math.ceil(((float) fuelTank.getStored() / (float) fuelTank.getMaxGas()) * 256F);
        max = Math.min((fuelTank.getStored() * maxBurnTicks) + burnTicks, max);
        max = (int) Math.min((getMaxEnergy() - getEnergy()) / generationRate, max);

        return max;
    }

    @Override
    public boolean canExtractItem(int slotID, @Nonnull ItemStack itemstack, @Nonnull EnumFacing side) {
        if (slotID == 1) {
            return ChargeUtils.canBeOutputted(itemstack, true);
        } else if (slotID == 0) {
            return (itemstack.getItem() instanceof IGasItem
                  && ((IGasItem) itemstack.getItem()).getGas(itemstack) == null);
        }

        return false;
    }

    @Override
    public boolean isItemValidForSlot(int slotID, @Nonnull ItemStack itemstack) {
        if (slotID == 0) {
            return itemstack.getItem() instanceof IGasItem && ((IGasItem) itemstack.getItem()).getGas(itemstack) != null
                  &&
                  FuelHandler.getFuel((((IGasItem) itemstack.getItem()).getGas(itemstack).getGas())) != null;
        } else if (slotID == 1) {
            return ChargeUtils.canBeCharged(itemstack);
        }

        return true;
    }

    @Nonnull
    @Override
    public int[] getSlotsForFace(@Nonnull EnumFacing side) {
        return side == MekanismUtils.getRight(facing) ? new int[]{1} : new int[]{0};
    }

    @Override
    public boolean canOperate() {
        return (fuelTank.getStored() > 0 || burnTicks > 0) && MekanismUtils.canFunction(this);
    }

    /**
     * Gets the scaled gas level for the GUI.
     *
     * @param i - multiplier
     * @return Scaled gas level
     */
    public int getScaledGasLevel(int i) {
        return fuelTank.getStored() * i / MAX_GAS;
    }

    @Override
    public String[] getMethods() {
        return methods;
    }

    @Override
    public Object[] invoke(int method, Object[] arguments) throws Exception {
        switch (method) {
            case 0:
                return new Object[]{getEnergy()};
            case 1:
                return new Object[]{output};
            case 2:
                return new Object[]{getMaxEnergy()};
            case 3:
                return new Object[]{getMaxEnergy() - getEnergy()};
            case 4:
                return new Object[]{fuelTank.getStored()};
            case 5:
                return new Object[]{fuelTank.getNeeded()};
            default:
                throw new NoSuchMethodException();
        }
    }

    @Override
    public void handlePacketData(ByteBuf dataStream) {
        super.handlePacketData(dataStream);

        if (FMLCommonHandler.instance().getEffectiveSide().isClient()) {
            TileUtils.readTankData(dataStream, fuelTank);

            generationRate = dataStream.readDouble();
            output = dataStream.readDouble();
            clientUsed = dataStream.readInt();
        }
    }

    @Override
    public TileNetworkList getNetworkedData(TileNetworkList data) {
        super.getNetworkedData(data);

        TileUtils.addTankData(data, fuelTank);

        data.add(generationRate);
        data.add(output);
        data.add(clientUsed);

        return data;
    }

    @Override
    public int receiveGas(EnumFacing side, GasStack stack, boolean doTransfer) {
        boolean isTankEmpty = (fuelTank.getGas() == null);

        if (canReceiveGas(side, stack.getGas()) && (isTankEmpty || fuelTank.getGas().isGasEqual(stack))) {
            int fuelReceived = fuelTank.receive(stack, doTransfer);

            if (doTransfer && isTankEmpty && fuelReceived > 0) {
                output = FuelHandler.getFuel(fuelTank.getGas().getGas()).energyPerTick * 2;
            }

            return fuelReceived;
        }

        return 0;
    }

    @Nonnull
    @Override
    public GasTankInfo[] getTankInfo() {
        return new GasTankInfo[]{fuelTank};
    }

    @Override
    public void readFromNBT(NBTTagCompound nbtTags) {
        super.readFromNBT(nbtTags);

        fuelTank.read(nbtTags.getCompoundTag("fuelTank"));

        boolean isTankEmpty = (fuelTank.getGas() == null);
        FuelGas fuel = (isTankEmpty) ? null : FuelHandler.getFuel(fuelTank.getGas().getGas());

        if (fuel != null) {
            output = fuel.energyPerTick * 2;
        }
    }

    @Nonnull
    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound nbtTags) {
        super.writeToNBT(nbtTags);

        nbtTags.setTag("fuelTank", fuelTank.write(new NBTTagCompound()));

        return nbtTags;
    }

    @Override
    public boolean canReceiveGas(EnumFacing side, Gas type) {
        return FuelHandler.getFuel(type) != null && side != facing;
    }

    @Override
    public GasStack drawGas(EnumFacing side, int amount, boolean doTransfer) {
        return null;
    }

    @Override
    public boolean canDrawGas(EnumFacing side, Gas type) {
        return false;
    }

    @Override
    public boolean hasCapability(@Nonnull Capability<?> capability, EnumFacing side) {
        if (isCapabilityDisabled(capability, side)) {
            return false;
        }
        return capability == Capabilities.GAS_HANDLER_CAPABILITY || super.hasCapability(capability, side);
    }

    @Override
    public <T> T getCapability(@Nonnull Capability<T> capability, EnumFacing side) {
        if (isCapabilityDisabled(capability, side)) {
            return null;
        } else if (capability == Capabilities.GAS_HANDLER_CAPABILITY) {
            return Capabilities.GAS_HANDLER_CAPABILITY.cast(this);
        }

        return super.getCapability(capability, side);
    }

    @Override
    public boolean isCapabilityDisabled(@Nonnull Capability<?> capability, EnumFacing side) {
        if (capability == Capabilities.GAS_HANDLER_CAPABILITY) {
            return side == facing;
        }
        return super.isCapabilityDisabled(capability, side);
    }

    @Override
    public void writeSustainedData(ItemStack itemStack) {
        if (fuelTank != null) {
            ItemDataUtils.setCompound(itemStack, "fuelTank", fuelTank.write(new NBTTagCompound()));
        }
    }

    @Override
    public void readSustainedData(ItemStack itemStack) {
        if (ItemDataUtils.hasData(itemStack, "fuelTank")) {
            fuelTank.read(ItemDataUtils.getCompound(itemStack, "fuelTank"));

            boolean isTankEmpty = (fuelTank.getGas() == null);
            //Update energy output based on any existing fuel in tank
            FuelGas fuel = (isTankEmpty) ? null : FuelHandler.getFuel(fuelTank.getGas().getGas());

            if (fuel != null) {
                output = fuel.energyPerTick * 2;
            }
        }
    }
}
