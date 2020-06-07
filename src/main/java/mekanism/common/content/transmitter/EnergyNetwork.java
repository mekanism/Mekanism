package mekanism.common.content.transmitter;

import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import mekanism.api.Action;
import mekanism.api.Coord4D;
import mekanism.api.energy.IEnergyContainer;
import mekanism.api.energy.IMekanismStrictEnergyHandler;
import mekanism.api.energy.IStrictEnergyHandler;
import mekanism.api.inventory.AutomationType;
import mekanism.api.math.FloatingLong;
import mekanism.common.MekanismLang;
import mekanism.common.capabilities.energy.BasicEnergyContainer;
import mekanism.common.capabilities.energy.VariableCapacityEnergyContainer;
import mekanism.common.distribution.target.EnergyAcceptorTarget;
import mekanism.common.distribution.target.EnergyTransmitterSaveTarget;
import mekanism.common.integration.energy.EnergyCompatUtils;
import mekanism.common.lib.transmitter.DynamicNetwork;
import mekanism.common.tile.transmitter.TileEntityTransmitter;
import mekanism.common.util.EmitUtils;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.text.EnergyDisplay;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.world.chunk.IChunk;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.Event;

public class EnergyNetwork extends DynamicNetwork<IStrictEnergyHandler, EnergyNetwork, FloatingLong> implements IMekanismStrictEnergyHandler {

    private final List<IEnergyContainer> energyContainers;
    public final VariableCapacityEnergyContainer energyContainer;

    public float energyScale;
    private FloatingLong prevTransferAmount = FloatingLong.ZERO;
    private FloatingLong floatingLongCapacity = FloatingLong.ZERO;

    public EnergyNetwork() {
        energyContainer = VariableCapacityEnergyContainer.create(this::getCapacityAsFloatingLong, BasicEnergyContainer.alwaysTrue, BasicEnergyContainer.alwaysTrue, this);
        energyContainers = Collections.singletonList(energyContainer);
    }

    public EnergyNetwork(UUID networkID) {
        super(networkID);
        energyContainer = VariableCapacityEnergyContainer.create(this::getCapacityAsFloatingLong, BasicEnergyContainer.alwaysTrue, BasicEnergyContainer.alwaysTrue, this);
        energyContainers = Collections.singletonList(energyContainer);
    }

    public EnergyNetwork(Collection<EnergyNetwork> networks) {
        this();
        for (EnergyNetwork net : networks) {
            if (net != null) {
                adoptTransmittersAndAcceptorsFrom(net);
                net.deregister();
            }
        }
        register();
    }

    @Override
    protected void forceScaleUpdate() {
        if (!energyContainer.isEmpty() && !energyContainer.getMaxEnergy().isZero()) {
            energyScale = Math.min(1, energyContainer.getEnergy().divide(energyContainer.getMaxEnergy()).floatValue());
        } else {
            energyScale = 0;
        }
    }

    @Override
    public void adoptTransmittersAndAcceptorsFrom(EnergyNetwork net) {
        FloatingLong oldCapacity = getCapacityAsFloatingLong();
        super.adoptTransmittersAndAcceptorsFrom(net);
        //Merge the energy scales
        FloatingLong ourScale = energyScale == 0 ? FloatingLong.ZERO : oldCapacity.multiply(energyScale);
        FloatingLong theirScale = net.energyScale == 0 ? FloatingLong.ZERO : net.getCapacityAsFloatingLong().multiply(net.energyScale);
        FloatingLong capacity = getCapacityAsFloatingLong();
        energyScale = Math.min(1, capacity.isZero() ? 0 : ourScale.add(theirScale).divide(getCapacityAsFloatingLong()).floatValue());
        if (!isRemote() && !net.energyContainer.isEmpty()) {
            energyContainer.setEnergy(energyContainer.getEnergy().add(net.getBuffer()));
            net.energyContainer.setEmpty();
        }
    }

    @Nonnull
    @Override
    public FloatingLong getBuffer() {
        return energyContainer.getEnergy();
    }

    @Override
    public void absorbBuffer(TileEntityTransmitter<IStrictEnergyHandler, EnergyNetwork, FloatingLong> transmitter) {
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
    protected synchronized void updateCapacity(TileEntityTransmitter<IStrictEnergyHandler, EnergyNetwork, FloatingLong> transmitter) {
        floatingLongCapacity = floatingLongCapacity.plusEqual(transmitter.getCapacityAsFloatingLong());
        capacity = floatingLongCapacity.longValue();
    }

    @Override
    public synchronized void updateCapacity() {
        FloatingLong sum = FloatingLong.ZERO;
        for (TileEntityTransmitter<IStrictEnergyHandler, EnergyNetwork, FloatingLong> transmitter : transmitters) {
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
    protected void updateSaveShares(@Nullable TileEntityTransmitter<?, ?, ?> triggerTransmitter) {
        super.updateSaveShares(triggerTransmitter);
        int size = transmittersSize();
        if (size > 0) {
            //Just pretend we are always accessing it from the north
            Direction side = Direction.NORTH;
            Set<EnergyTransmitterSaveTarget> saveTargets = new ObjectOpenHashSet<>(size);
            for (TileEntityTransmitter<IStrictEnergyHandler, EnergyNetwork, FloatingLong> transmitter : transmitters) {
                EnergyTransmitterSaveTarget saveTarget = new EnergyTransmitterSaveTarget();
                saveTarget.addHandler(side, transmitter);
                saveTargets.add(saveTarget);
            }
            EmitUtils.sendToAcceptors(saveTargets, size, energyContainer.getEnergy().copy());
            for (EnergyTransmitterSaveTarget saveTarget : saveTargets) {
                saveTarget.saveShare(side);
            }
        }
    }

    private FloatingLong tickEmit(FloatingLong energyToSend) {
        Set<EnergyAcceptorTarget> targets = new ObjectOpenHashSet<>();
        int totalHandlers = 0;
        Long2ObjectMap<IChunk> chunkMap = new Long2ObjectOpenHashMap<>();
        for (Coord4D coord : possibleAcceptors) {
            EnumSet<Direction> sides = acceptorDirections.get(coord);
            if (sides == null || sides.isEmpty()) {
                continue;
            }
            TileEntity tile = MekanismUtils.getTileEntity(getWorld(), chunkMap, coord);
            if (tile == null) {
                continue;
            }
            EnergyAcceptorTarget target = new EnergyAcceptorTarget();
            for (Direction side : sides) {
                IStrictEnergyHandler handler = EnergyCompatUtils.getStrictEnergyHandler(tile, side);
                if (handler != null && handler.insertEnergy(energyToSend, Action.SIMULATE).smallerThan(energyToSend)) {
                    target.addHandler(side, handler);
                }
            }
            int curHandlers = target.getHandlers().size();
            if (curHandlers > 0) {
                targets.add(target);
                totalHandlers += curHandlers;
            }
        }
        return EmitUtils.sendToAcceptors(targets, totalHandlers, energyToSend.copy());
    }

    @Override
    public String toString() {
        return "[EnergyNetwork] " + transmitters.size() + " transmitters, " + possibleAcceptors.size() + " acceptors.";
    }

    @Override
    public void onUpdate() {
        super.onUpdate();
        if (!isRemote()) {
            float scale = computeContentScale();
            if (scale != energyScale) {
                energyScale = scale;
                needsUpdate = true;
            }
            if (needsUpdate) {
                MinecraftForge.EVENT_BUS.post(new EnergyTransferEvent(this, energyScale));
                needsUpdate = false;
            }
            if (energyContainer.isEmpty()) {
                prevTransferAmount = FloatingLong.ZERO;
            } else {
                prevTransferAmount = tickEmit(energyContainer.getEnergy());
                energyContainer.extract(prevTransferAmount, Action.EXECUTE, AutomationType.INTERNAL);
            }
        }
    }

    public float computeContentScale() {
        float scale = (float) energyContainer.getEnergy().divideToLevel(energyContainer.getMaxEnergy());
        float ret = Math.max(energyScale, scale);
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
    public ITextComponent getTextComponent() {
        return MekanismLang.NETWORK_DESCRIPTION.translate(MekanismLang.ENERGY_NETWORK, transmitters.size(), possibleAcceptors.size());
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

    public static class EnergyTransferEvent extends Event {

        public final EnergyNetwork energyNetwork;
        public final float energyScale;

        public EnergyTransferEvent(EnergyNetwork network, float scale) {
            energyNetwork = network;
            energyScale = scale;
        }
    }
}