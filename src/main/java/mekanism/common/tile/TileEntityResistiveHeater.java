package mekanism.common.tile;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import mekanism.api.IHeatTransfer;
import mekanism.api.NBTConstants;
import mekanism.common.base.ITileNetwork;
import mekanism.common.capabilities.Capabilities;
import mekanism.common.capabilities.holder.slot.IInventorySlotHolder;
import mekanism.common.capabilities.holder.slot.InventorySlotHelper;
import mekanism.common.config.MekanismConfig;
import mekanism.common.inventory.container.MekanismContainer;
import mekanism.common.inventory.container.sync.SyncableDouble;
import mekanism.common.inventory.slot.EnergyInventorySlot;
import mekanism.common.registries.MekanismBlocks;
import mekanism.common.tile.base.TileEntityMekanism;
import mekanism.common.util.CapabilityUtils;
import mekanism.common.util.HeatUtils;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.NBTUtils;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.PacketBuffer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;

public class TileEntityResistiveHeater extends TileEntityMekanism implements IHeatTransfer, ITileNetwork {

    public double energyUsage = 100;
    private double temperature;
    public double heatToAbsorb = 0;
    public float soundScale = 1;
    public double lastEnvironmentLoss;

    private EnergyInventorySlot energySlot;

    public TileEntityResistiveHeater() {
        super(MekanismBlocks.RESISTIVE_HEATER);
    }

    @Nonnull
    @Override
    protected IInventorySlotHolder getInitialInventory() {
        InventorySlotHelper builder = InventorySlotHelper.forSide(this::getDirection);
        builder.addSlot(energySlot = EnergyInventorySlot.discharge(this, 15, 35));
        return builder.build();
    }

    @Override
    protected void onUpdateServer() {
        super.onUpdateServer();
        energySlot.discharge(this);
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
        float newSoundScale = (float) Math.max(0, toUse / 100_000);
        if (Math.abs(newSoundScale - soundScale) > 0.01) {
            soundScale = newSoundScale;
            sendUpdatePacket();
        }
    }

    @Override
    public boolean canReceiveEnergy(Direction side) {
        return side == getLeftSide() || side == getRightSide();
    }

    @Override
    public void read(CompoundNBT nbtTags) {
        super.read(nbtTags);
        energyUsage = nbtTags.getDouble(NBTConstants.ENERGY_USAGE);
        temperature = nbtTags.getDouble(NBTConstants.TEMPERATURE);
        setMaxEnergy(energyUsage * 400);
    }

    @Nonnull
    @Override
    public CompoundNBT write(CompoundNBT nbtTags) {
        super.write(nbtTags);
        nbtTags.putDouble(NBTConstants.ENERGY_USAGE, energyUsage);
        nbtTags.putDouble(NBTConstants.TEMPERATURE, temperature);
        return nbtTags;
    }

    @Override
    public void handlePacketData(PacketBuffer dataStream) {
        if (!isRemote()) {
            energyUsage = MekanismUtils.convertToJoules(dataStream.readInt());
            setMaxEnergy(energyUsage * 400);
        }
    }

    @Override
    public float getVolume() {
        return (float) Math.sqrt(soundScale);
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
        return MekanismUtils.toOptional(CapabilityUtils.getCapability(adj, Capabilities.HEAT_TRANSFER_CAPABILITY, side.getOpposite())).orElse(null);
    }

    @Nonnull
    @Override
    public <T> LazyOptional<T> getCapabilityIfEnabled(@Nonnull Capability<T> capability, @Nullable Direction side) {
        if (capability == Capabilities.HEAT_TRANSFER_CAPABILITY) {
            return Capabilities.HEAT_TRANSFER_CAPABILITY.orEmpty(capability, LazyOptional.of(() -> this));
        }
        return super.getCapabilityIfEnabled(capability, side);
    }

    @Override
    public boolean lightUpdate() {
        return true;
    }

    @Override
    public void addContainerTrackers(MekanismContainer container) {
        super.addContainerTrackers(container);
        container.track(SyncableDouble.create(() -> energyUsage, value -> energyUsage = value));
        container.track(SyncableDouble.create(this::getTemp, value -> temperature = value));
        container.track(SyncableDouble.create(() -> lastEnvironmentLoss, value -> lastEnvironmentLoss = value));
    }

    @Nonnull
    @Override
    public CompoundNBT getUpdateTag() {
        CompoundNBT updateTag = super.getUpdateTag();
        updateTag.putFloat(NBTConstants.SOUND_SCALE, soundScale);
        return updateTag;
    }

    @Override
    public void handleUpdateTag(@Nonnull CompoundNBT tag) {
        super.handleUpdateTag(tag);
        NBTUtils.setFloatIfPresent(tag, NBTConstants.SOUND_SCALE, value -> soundScale = value);
    }
}