package mekanism.common.transmitters.grid;

import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import mekanism.api.Action;
import mekanism.api.Coord4D;
import mekanism.api.energy.IEnergyContainer;
import mekanism.api.energy.IMekanismStrictEnergyHandler;
import mekanism.api.energy.IStrictEnergyHandler;
import mekanism.api.transmitters.DynamicNetwork;
import mekanism.api.transmitters.IGridTransmitter;
import mekanism.common.MekanismLang;
import mekanism.common.integration.EnergyCompatUtils;
import mekanism.common.base.target.EnergyAcceptorTarget;
import mekanism.common.capabilities.energy.BasicEnergyContainer;
import mekanism.common.capabilities.energy.VariableCapacityEnergyContainer;
import mekanism.common.util.EmitUtils;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.text.EnergyDisplay;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.world.chunk.IChunk;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.Event;

public class EnergyNetwork extends DynamicNetwork<IStrictEnergyHandler, EnergyNetwork, Double> implements IMekanismStrictEnergyHandler {

    private final List<IEnergyContainer> energyContainers;
    public final VariableCapacityEnergyContainer energyContainer;

    public double clientEnergyScale;
    private double lastPowerScale;
    private double joulesTransmitted;
    private double jouleBufferLastTick;

    public EnergyNetwork() {
        energyContainer = VariableCapacityEnergyContainer.create(this::getCapacityAsDouble, BasicEnergyContainer.alwaysTrue, BasicEnergyContainer.alwaysTrue, this);
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
    public void adoptTransmittersAndAcceptorsFrom(EnergyNetwork net) {
        if (isRemote()) {
            if (!net.energyContainer.isEmpty() && net.clientEnergyScale > clientEnergyScale) {
                clientEnergyScale = net.clientEnergyScale;
                energyContainer.setEnergy(net.getBuffer());
                net.clientEnergyScale = 0;
                net.energyContainer.setEmpty();
            }
        } else if (!net.energyContainer.isEmpty()) {
            energyContainer.setEnergy(energyContainer.getEnergy() + net.getBuffer());
            net.energyContainer.setEmpty();
        }
        super.adoptTransmittersAndAcceptorsFrom(net);
    }

    public static double round(double d) {
        return Math.round(d * 10_000) / 10_000;
    }

    @Nonnull
    @Override
    public Double getBuffer() {
        return energyContainer.getEnergy();
    }

    @Override
    public void absorbBuffer(IGridTransmitter<IStrictEnergyHandler, EnergyNetwork, Double> transmitter) {
        Double energy = transmitter.getBuffer();
        if (energy != null && energy > 0) {
            energyContainer.setEnergy(energyContainer.getEnergy() + energy);
        }
    }

    @Override
    public void clampBuffer() {
        if (!energyContainer.isEmpty()) {
            double capacity = getCapacityAsDouble();
            if (energyContainer.getEnergy() > capacity) {
                energyContainer.setEnergy(capacity);
            }
        }
    }

    private double tickEmit(double energyToSend) {
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
                IStrictEnergyHandler handler = EnergyCompatUtils.get(tile, side);
                if (handler != null && handler.insertEnergy(1, Action.SIMULATE) < 1) {
                    target.addHandler(side, handler);
                }
            }
            int curHandlers = target.getHandlers().size();
            if (curHandlers > 0) {
                targets.add(target);
                totalHandlers += curHandlers;
            }
        }
        return EmitUtils.sendToAcceptors(targets, totalHandlers, energyToSend);
    }

    @Override
    public String toString() {
        return "[EnergyNetwork] " + transmitters.size() + " transmitters, " + possibleAcceptors.size() + " acceptors.";
    }

    @Override
    public void onUpdate() {
        super.onUpdate();
        clearJoulesTransmitted();

        double currentPowerScale = getPowerScale();
        if (!isRemote()) {
            if (Math.abs(currentPowerScale - lastPowerScale) > 0.01 || (currentPowerScale != lastPowerScale && (currentPowerScale == 0 || currentPowerScale == 1))) {
                needsUpdate = true;
            }
            if (needsUpdate) {
                MinecraftForge.EVENT_BUS.post(new EnergyTransferEvent(this, currentPowerScale));
                lastPowerScale = currentPowerScale;
                needsUpdate = false;
            }
            if (buffer.amount > 0) {
                joulesTransmitted = tickEmit(buffer.amount);
                buffer.amount -= joulesTransmitted;
            }
        }
    }

    public double getPowerScale() {
        return Math.max(jouleBufferLastTick == 0 ? 0 : Math.min(Math.ceil(Math.log10(getPower()) * 2) / 10, 1),
              getCapacityAsDouble() == 0 ? 0 : energyContainer.getEnergy() / getCapacityAsDouble());
    }

    public void clearJoulesTransmitted() {
        jouleBufferLastTick = buffer.amount;
        joulesTransmitted = 0;
    }

    public double getPower() {
        return jouleBufferLastTick * 20;
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
        return MekanismLang.GENERIC_PER_TICK.translate(EnergyDisplay.of(joulesTransmitted));
    }

    @Override
    public ITextComponent getTextComponent() {
        return MekanismLang.NETWORK_DESCRIPTION.translate(MekanismLang.ENERGY_NETWORK, transmitters.size(), possibleAcceptors.size());
    }

    @Override
    public List<IEnergyContainer> getEnergyContainers(@Nullable Direction side) {
        return energyContainers;
    }

    @Override
    public void onContentsChanged() {
        //TODO: Do we want to mark the network as dirty
    }

    public static class EnergyTransferEvent extends Event {

        public final EnergyNetwork energyNetwork;

        public final double power;

        public EnergyTransferEvent(EnergyNetwork network, double currentPower) {
            energyNetwork = network;
            power = currentPower;
        }
    }
}