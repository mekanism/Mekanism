package mekanism.common.tile;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import mekanism.api.Action;
import mekanism.api.IHeatTransfer;
import mekanism.api.NBTConstants;
import mekanism.api.RelativeSide;
import mekanism.api.math.FloatingLong;
import mekanism.api.inventory.AutomationType;
import mekanism.common.base.ITileNetwork;
import mekanism.common.capabilities.Capabilities;
import mekanism.common.capabilities.energy.MachineEnergyContainer;
import mekanism.common.capabilities.energy.ResistiveHeaterEnergyContainer;
import mekanism.common.capabilities.holder.energy.EnergyContainerHelper;
import mekanism.common.capabilities.holder.energy.IEnergyContainerHolder;
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

    private float soundScale = 1;
    private double temperature;
    public double heatToAbsorb = 0;
    public double lastEnvironmentLoss;

    private ResistiveHeaterEnergyContainer energyContainer;
    private EnergyInventorySlot energySlot;

    public TileEntityResistiveHeater() {
        super(MekanismBlocks.RESISTIVE_HEATER);
    }

    @Nonnull
    @Override
    protected IEnergyContainerHolder getInitialEnergyContainers() {
        EnergyContainerHelper builder = EnergyContainerHelper.forSide(this::getDirection);
        builder.addContainer(energyContainer = ResistiveHeaterEnergyContainer.input(this), RelativeSide.LEFT, RelativeSide.RIGHT);
        return builder.build();
    }

    @Nonnull
    @Override
    protected IInventorySlotHolder getInitialInventory() {
        InventorySlotHelper builder = InventorySlotHelper.forSide(this::getDirection);
        builder.addSlot(energySlot = EnergyInventorySlot.fillOrConvert(energyContainer, this::getWorld, this, 15, 35));
        return builder.build();
    }

    @Override
    protected void onUpdateServer() {
        super.onUpdateServer();
        energySlot.fillContainerOrConvert();
        FloatingLong toUse = FloatingLong.ZERO;
        if (MekanismUtils.canFunction(this)) {
            toUse = energyContainer.extract(energyContainer.getEnergyPerTick(), Action.SIMULATE, AutomationType.INTERNAL);
            if (!toUse.isZero()) {
                heatToAbsorb += toUse.divide(MekanismConfig.general.energyPerHeat.get()).doubleValue();
                energyContainer.extract(toUse, Action.EXECUTE, AutomationType.INTERNAL);
            }
        }
        setActive(!toUse.isZero());
        double[] loss = simulateHeat();
        applyTemperatureChange();
        lastEnvironmentLoss = loss[1];
        float newSoundScale = toUse.divide(100_000).floatValue();
        if (Math.abs(newSoundScale - soundScale) > 0.01) {
            soundScale = newSoundScale;
            sendUpdatePacket();
        }
    }

    @Override
    public void read(CompoundNBT nbtTags) {
        super.read(nbtTags);
        temperature = nbtTags.getDouble(NBTConstants.TEMPERATURE);
    }

    @Nonnull
    @Override
    public CompoundNBT write(CompoundNBT nbtTags) {
        super.write(nbtTags);
        nbtTags.putDouble(NBTConstants.TEMPERATURE, temperature);
        return nbtTags;
    }

    @Override
    public void handlePacketData(PacketBuffer dataStream) {
        if (!isRemote()) {
            energyContainer.updateEnergyUsage(FloatingLong.readFromBuffer(dataStream));
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

    public MachineEnergyContainer<TileEntityResistiveHeater> getEnergyContainer() {
        return energyContainer;
    }

    @Override
    public void addContainerTrackers(MekanismContainer container) {
        super.addContainerTrackers(container);
        container.track(SyncableDouble.create(this::getTemp, value -> temperature = value));
        container.track(SyncableDouble.create(() -> lastEnvironmentLoss, value -> lastEnvironmentLoss = value));
    }

    @Nonnull
    @Override
    public CompoundNBT getReducedUpdateTag() {
        CompoundNBT updateTag = super.getReducedUpdateTag();
        updateTag.putFloat(NBTConstants.SOUND_SCALE, soundScale);
        return updateTag;
    }

    @Override
    public void handleUpdateTag(@Nonnull CompoundNBT tag) {
        super.handleUpdateTag(tag);
        NBTUtils.setFloatIfPresent(tag, NBTConstants.SOUND_SCALE, value -> soundScale = value);
    }
}