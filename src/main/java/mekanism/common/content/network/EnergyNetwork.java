package mekanism.common.content.network;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import mekanism.api.Action;
import mekanism.api.energy.IEnergyContainer;
import mekanism.api.energy.IMekanismStrictEnergyHandler;
import mekanism.api.energy.IStrictEnergyHandler;
import mekanism.api.inventory.AutomationType;
import mekanism.api.math.FloatingLong;
import mekanism.common.MekanismLang;
import mekanism.common.capabilities.energy.BasicEnergyContainer;
import mekanism.common.capabilities.energy.VariableCapacityEnergyContainer;
import mekanism.common.content.network.distribution.EnergyAcceptorTarget;
import mekanism.common.content.network.distribution.EnergyTransmitterSaveTarget;
import mekanism.common.content.network.transmitter.UniversalCable;
import mekanism.common.lib.transmitter.DynamicBufferedNetwork;
import mekanism.common.util.EmitUtils;
import mekanism.common.util.text.EnergyDisplay;
import net.minecraft.util.Direction;
import net.minecraft.util.text.ITextComponent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.util.LazyOptional;

public class EnergyNetwork extends DynamicBufferedNetwork<IStrictEnergyHandler, EnergyNetwork, FloatingLong, UniversalCable> implements IMekanismStrictEnergyHandler {

    private final List<IEnergyContainer> energyContainers;
    public final VariableCapacityEnergyContainer energyContainer;
    private FloatingLong prevTransferAmount = FloatingLong.ZERO;
    private FloatingLong floatingLongCapacity = FloatingLong.ZERO;

    public EnergyNetwork(UUID networkID) {
        super(networkID);
        energyContainer = VariableCapacityEnergyContainer.create(this::getCapacityAsFloatingLong, BasicEnergyContainer.alwaysTrue, BasicEnergyContainer.alwaysTrue, this);
        energyContainers = Collections.singletonList(energyContainer);
    }

    public EnergyNetwork(Collection<EnergyNetwork> networks) {
        this(UUID.randomUUID());
        adoptAllAndRegister(networks);
    }

    @Override
    protected void forceScaleUpdate() {
        if (!energyContainer.isEmpty() && !energyContainer.getMaxEnergy().isZero()) {
            currentScale = Math.min(1, energyContainer.getEnergy().divide(energyContainer.getMaxEnergy()).floatValue());
        } else {
            currentScale = 0;
        }
    }

    @Override
    public List<UniversalCable> adoptTransmittersAndAcceptorsFrom(EnergyNetwork net) {
        FloatingLong oldCapacity = getCapacityAsFloatingLong();
        List<UniversalCable> transmittersToUpdate = super.adoptTransmittersAndAcceptorsFrom(net);
        //Merge the energy scales
        FloatingLong ourScale = currentScale == 0 ? FloatingLong.ZERO : oldCapacity.multiply(currentScale);
        FloatingLong theirScale = net.currentScale == 0 ? FloatingLong.ZERO : net.getCapacityAsFloatingLong().multiply(net.currentScale);
        FloatingLong capacity = getCapacityAsFloatingLong();
        currentScale = Math.min(1, capacity.isZero() ? 0 : ourScale.add(theirScale).divide(getCapacityAsFloatingLong()).floatValue());
        if (!isRemote() && !net.energyContainer.isEmpty()) {
            energyContainer.setEnergy(energyContainer.getEnergy().add(net.getBuffer()));
            net.energyContainer.setEmpty();
        }
        return transmittersToUpdate;
    }

    @Nonnull
    @Override
    public FloatingLong getBuffer() {
        return energyContainer.getEnergy();
    }

    @Override
    public void absorbBuffer(UniversalCable transmitter) {
        FloatingLong energy = transmitter.releaseShare();
        if (!energy.isZero()) {
            energyContainer.setEnergy(energyContainer.getEnergy().add(energy));
        }
    }

    @Override
    public void clampBuffer() {
        if (!energyContainer.isEmpty()) {
            FloatingLong capacity = getCapacityAsFloatingLong();
            if (energyContainer.getEnergy().greaterThan(capacity)) {
                energyContainer.setEnergy(capacity);
            }
        }
    }

    @Override
    protected synchronized void updateCapacity(UniversalCable transmitter) {
        floatingLongCapacity = floatingLongCapacity.plusEqual(transmitter.getCapacityAsFloatingLong());
        capacity = floatingLongCapacity.longValue();
    }

    @Override
    public synchronized void updateCapacity() {
        FloatingLong sum = FloatingLong.ZERO;
        for (UniversalCable transmitter : transmitters) {
            sum = sum.plusEqual(transmitter.getCapacityAsFloatingLong());
        }
        if (!floatingLongCapacity.equals(sum)) {
            floatingLongCapacity = sum;
            capacity = floatingLongCapacity.longValue();
        }
    }

    @Nonnull
    public FloatingLong getCapacityAsFloatingLong() {
        return floatingLongCapacity;
    }

    @Override
    protected void updateSaveShares(@Nullable UniversalCable triggerTransmitter) {
        super.updateSaveShares(triggerTransmitter);
        if (!isEmpty()) {
            EnergyTransmitterSaveTarget saveTarget = new EnergyTransmitterSaveTarget(transmitters);
            EmitUtils.sendToAcceptors(saveTarget, energyContainer.getEnergy().copy());
            saveTarget.saveShare();
        }
    }

    private FloatingLong tickEmit(FloatingLong energyToSend) {
        Collection<Map<Direction, LazyOptional<IStrictEnergyHandler>>> acceptorValues = acceptorCache.getAcceptorValues();
        EnergyAcceptorTarget target = new EnergyAcceptorTarget(acceptorValues.size() * 2);
        for (Map<Direction, LazyOptional<IStrictEnergyHandler>> acceptors : acceptorValues) {
            for (LazyOptional<IStrictEnergyHandler> lazyAcceptor : acceptors.values()) {
                lazyAcceptor.ifPresent(acceptor -> {
                    if (acceptor.insertEnergy(energyToSend, Action.SIMULATE).smallerThan(energyToSend)) {
                        target.addHandler(acceptor);
                    }
                });
            }
        }
        return EmitUtils.sendToAcceptors(target, energyToSend.copy());
    }

    @Override
    public String toString() {
        return "[EnergyNetwork] " + transmittersSize() + " transmitters, " + getAcceptorCount() + " acceptors.";
    }

    @Override
    public void onUpdate() {
        super.onUpdate();
        if (needsUpdate) {
            MinecraftForge.EVENT_BUS.post(new EnergyTransferEvent(this));
            needsUpdate = false;
        }
        if (energyContainer.isEmpty()) {
            prevTransferAmount = FloatingLong.ZERO;
        } else {
            prevTransferAmount = tickEmit(energyContainer.getEnergy());
            energyContainer.extract(prevTransferAmount, Action.EXECUTE, AutomationType.INTERNAL);
        }
    }

    @Override
    protected float computeContentScale() {
        float scale = (float) energyContainer.getEnergy().divideToLevel(energyContainer.getMaxEnergy());
        float ret = Math.max(currentScale, scale);
        if (!prevTransferAmount.isZero() && ret < 1) {
            ret = Math.min(1, ret + 0.02F);
        } else if (prevTransferAmount.isZero() && ret > 0) {
            ret = Math.max(scale, ret - 0.02F);
        }
        return ret;
    }

    @Override
    public ITextComponent getNeededInfo() {
        return EnergyDisplay.of(energyContainer.getNeeded()).getTextComponent();
    }

    @Override
    public ITextComponent getStoredInfo() {
        return EnergyDisplay.of(energyContainer.getEnergy()).getTextComponent();
    }

    @Override
    public ITextComponent getFlowInfo() {
        return MekanismLang.GENERIC_PER_TICK.translate(EnergyDisplay.of(prevTransferAmount));
    }

    @Override
    public Object getNetworkReaderCapacity() {
        return getCapacityAsFloatingLong();
    }

    @Override
    public ITextComponent getTextComponent() {
        return MekanismLang.NETWORK_DESCRIPTION.translate(MekanismLang.ENERGY_NETWORK, transmittersSize(), getAcceptorCount());
    }

    @Nonnull
    @Override
    public List<IEnergyContainer> getEnergyContainers(@Nullable Direction side) {
        return energyContainers;
    }

    @Override
    public void onContentsChanged() {
        markDirty();
    }

    public static class EnergyTransferEvent extends TransferEvent<EnergyNetwork> {

        public EnergyTransferEvent(EnergyNetwork network) {
            super(network);
        }
    }
}