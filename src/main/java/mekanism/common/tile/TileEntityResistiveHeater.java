package mekanism.common.tile;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import mekanism.api.IHeatTransfer;
import mekanism.api.TileNetworkList;
import mekanism.common.Mekanism;
import mekanism.common.MekanismBlock;
import mekanism.common.capabilities.Capabilities;
import mekanism.common.config.MekanismConfig;
import mekanism.common.integration.computer.IComputerIntegration;
import mekanism.common.inventory.IInventorySlotHolder;
import mekanism.common.inventory.InventorySlotHelper;
import mekanism.common.inventory.slot.EnergyInventorySlot;
import mekanism.common.tile.base.TileEntityMekanism;
import mekanism.common.util.CapabilityUtils;
import mekanism.common.util.ChargeUtils;
import mekanism.common.util.HeatUtils;
import mekanism.common.util.MekanismUtils;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.PacketBuffer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;

public class TileEntityResistiveHeater extends TileEntityMekanism implements IHeatTransfer, IComputerIntegration {

    private static final String[] methods = new String[]{"getEnergy", "getMaxEnergy", "getTemperature", "setEnergyUsage"};
    public double energyUsage = 100;
    private double temperature;
    public double heatToAbsorb = 0;
    //TODO: Figure out sound
    public float soundScale = 1;
    public double lastEnvironmentLoss;

    public TileEntityResistiveHeater() {
        super(MekanismBlock.RESISTIVE_HEATER);
    }

    @Nonnull
    @Override
    protected IInventorySlotHolder getInitialInventory() {
        InventorySlotHelper.Builder builder = InventorySlotHelper.Builder.forSide(this::getDirection);
        builder.addSlot(EnergyInventorySlot.discharge(15, 35));
        return builder.build();
    }

    @Override
    public void onUpdate() {
        if (!isRemote()) {
            boolean packet = false;
            ChargeUtils.discharge(0, this);
            double toUse = 0;
            if (MekanismUtils.canFunction(this)) {
                toUse = Math.min(getEnergy(), energyUsage);
                heatToAbsorb += toUse / MekanismConfig.general.energyPerHeat.get();
                setEnergy(getEnergy() - toUse);
            }

            setActive(toUse > 0);
            double[] loss = simulateHeat();
            applyTemperatureChange();
            lastEnvironmentLoss = loss[1];
            float newSoundScale = (float) Math.max(0, toUse / 1E5);
            if (Math.abs(newSoundScale - soundScale) > 0.01) {
                packet = true;
            }

            soundScale = newSoundScale;
            if (packet) {
                Mekanism.packetHandler.sendUpdatePacket(this);
            }
        }
    }

    @Override
    public boolean canReceiveEnergy(Direction side) {
        return side == getLeftSide() || side == getRightSide();
    }

    @Override
    public void read(CompoundNBT nbtTags) {
        super.read(nbtTags);
        energyUsage = nbtTags.getDouble("energyUsage");
        temperature = nbtTags.getDouble("temperature");
        setMaxEnergy(energyUsage * 400);
    }

    @Nonnull
    @Override
    public CompoundNBT write(CompoundNBT nbtTags) {
        super.write(nbtTags);
        nbtTags.putDouble("energyUsage", energyUsage);
        nbtTags.putDouble("temperature", temperature);
        return nbtTags;
    }

    @Override
    public void handlePacketData(PacketBuffer dataStream) {
        if (!isRemote()) {
            energyUsage = MekanismUtils.convertToJoules(dataStream.readInt());
            setMaxEnergy(energyUsage * 400);
            return;
        }

        super.handlePacketData(dataStream);
        if (isRemote()) {
            energyUsage = dataStream.readDouble();
            temperature = dataStream.readDouble();
            setMaxEnergy(dataStream.readDouble());
            soundScale = dataStream.readFloat();
            lastEnvironmentLoss = dataStream.readDouble();
        }
    }

    @Override
    public TileNetworkList getNetworkedData(TileNetworkList data) {
        super.getNetworkedData(data);

        data.add(energyUsage);
        data.add(temperature);
        data.add(getMaxEnergy());
        data.add(soundScale);

        data.add(lastEnvironmentLoss);
        return data;
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

    @Override
    public boolean lightUpdate() {
        return true;
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
                return new Object[]{getMaxEnergy()};
            case 2:
                return new Object[]{temperature};
            case 3:
                if (arguments.length == 1) {
                    if (arguments[0] instanceof Double) {
                        energyUsage = (Double) arguments[0];
                        return new Object[]{"Set energy usage."};
                    }
                }
                return new Object[]{"Invalid parameters."};
            default:
                throw new NoSuchMethodException();
        }
    }
}