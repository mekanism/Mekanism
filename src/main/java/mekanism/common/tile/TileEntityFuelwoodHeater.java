package mekanism.common.tile;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import mekanism.api.IHeatTransfer;
import mekanism.api.TileNetworkList;
import mekanism.common.MekanismBlock;
import mekanism.common.base.IActiveState;
import mekanism.common.capabilities.Capabilities;
import mekanism.common.config.MekanismConfig;
import mekanism.common.tile.base.TileEntityMekanism;
import mekanism.common.util.CapabilityUtils;
import mekanism.common.util.HeatUtils;
import mekanism.common.util.MekanismUtils;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.PacketBuffer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;

public class TileEntityFuelwoodHeater extends TileEntityMekanism implements IHeatTransfer, IActiveState {

    public double temperature;
    public double heatToAbsorb = 0;

    public int burnTime;
    public int maxBurnTime;

    public double lastEnvironmentLoss;

    public TileEntityFuelwoodHeater() {
        super(MekanismBlock.FUELWOOD_HEATER);
    }

    @Override
    public void onUpdate() {
        if (!isRemote()) {
            boolean burning = false;
            if (burnTime > 0) {
                burnTime--;
                burning = true;
            } else {
                ItemStack stack = getStackInSlot(0);
                if (!stack.isEmpty()) {
                    maxBurnTime = burnTime = stack.getBurnTime() / 2;
                    if (burnTime > 0) {
                        ItemStack preShrunk = stack.copy();
                        stack.shrink(1);
                        if (stack.getCount() == 0) {
                            getInventory().set(0, preShrunk.getItem().getContainerItem(preShrunk));
                        }
                        burning = true;
                    }
                }
            }
            if (burning) {
                heatToAbsorb += MekanismConfig.general.heatPerFuelTick.get();
            }
            double[] loss = simulateHeat();
            applyTemperatureChange();
            lastEnvironmentLoss = loss[1];
            setActive(burning);
        }
    }

    @Override
    public void read(CompoundNBT nbtTags) {
        super.read(nbtTags);
        temperature = nbtTags.getDouble("temperature");
        burnTime = nbtTags.getInt("burnTime");
        maxBurnTime = nbtTags.getInt("maxBurnTime");
    }

    @Nonnull
    @Override
    public CompoundNBT write(CompoundNBT nbtTags) {
        super.write(nbtTags);
        nbtTags.putDouble("temperature", temperature);
        nbtTags.putInt("burnTime", burnTime);
        nbtTags.putInt("maxBurnTime", maxBurnTime);
        return nbtTags;
    }

    @Override
    public void handlePacketData(PacketBuffer dataStream) {
        super.handlePacketData(dataStream);
        if (isRemote()) {
            temperature = dataStream.readDouble();
            burnTime = dataStream.readInt();
            maxBurnTime = dataStream.readInt();
            lastEnvironmentLoss = dataStream.readDouble();
        }
    }

    @Override
    public TileNetworkList getNetworkedData(TileNetworkList data) {
        super.getNetworkedData(data);
        data.add(temperature);
        data.add(burnTime);
        data.add(maxBurnTime);
        data.add(lastEnvironmentLoss);
        return data;
    }

    @Nonnull
    @Override
    public int[] getSlotsForFace(@Nonnull Direction side) {
        return new int[]{0};
    }

    @Override
    public boolean isItemValidForSlot(int slotID, @Nonnull ItemStack stack) {
        return stack.getBurnTime() > 0;
    }

    @Override
    public boolean lightUpdate() {
        return true;
    }

    @Override
    public double getTemp() {
        return temperature;
    }

    @Override
    public double getInverseConductionCoefficient() {
        return 5;
    }

    @Override
    public double getInsulationCoefficient(Direction side) {
        return 1000;
    }

    @Override
    public void transferHeatTo(double heat) {
        heatToAbsorb += heat;
    }

    @Override
    public double[] simulateHeat() {
        return HeatUtils.simulate(this);
    }

    @Override
    public double applyTemperatureChange() {
        temperature += heatToAbsorb;
        heatToAbsorb = 0;
        return temperature;
    }

    @Nullable
    @Override
    public IHeatTransfer getAdjacent(Direction side) {
        TileEntity adj = MekanismUtils.getTileEntity(getWorld(), getPos().offset(side));
        return CapabilityUtils.getCapabilityHelper(adj, Capabilities.HEAT_TRANSFER_CAPABILITY, side.getOpposite()).getValue();
    }

    @Nonnull
    @Override
    public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> capability, @Nullable Direction side) {
        if (capability == Capabilities.HEAT_TRANSFER_CAPABILITY) {
            return Capabilities.HEAT_TRANSFER_CAPABILITY.orEmpty(capability, LazyOptional.of(() -> this));
        }
        return super.getCapability(capability, side);
    }
}