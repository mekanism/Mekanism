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
import mekanism.api.inventory.AutomationType;
import mekanism.api.math.FloatingLong;
import mekanism.api.transmitters.DynamicNetwork;
import mekanism.api.transmitters.IGridTransmitter;
import mekanism.common.MekanismLang;
import mekanism.common.base.target.EnergyAcceptorTarget;
import mekanism.common.capabilities.energy.BasicEnergyContainer;
import mekanism.common.capabilities.energy.VariableCapacityEnergyContainer;
import mekanism.common.integration.EnergyCompatUtils;
import mekanism.common.tile.transmitter.TileEntityTransmitter;
import mekanism.common.tile.transmitter.TileEntityUniversalCable;
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

    //TODO: Improve this mess, it will be easier to first rewrite the gas and fluid network though to have the scale calculations
    // happen on the server and only bother sending sync/update packets to the client when something changes similar to how
    // it is done for fluid tanks. And then we can copy the code to here and modify it to use our proper scale calculations
    public double energyScale;
    private FloatingLong prevTransferAmount = FloatingLong.ZERO;
    private FloatingLong floatingLongCapacity = FloatingLong.ZERO;

    public EnergyNetwork() {
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
        energyScale = getScale();
        register();
    }

    @Override
    public void adoptTransmittersAndAcceptorsFrom(EnergyNetwork net) {
        if (isRemote()) {
            if (!net.energyContainer.isEmpty() && net.energyScale > energyScale) {
                energyScale = net.energyScale;
                energyContainer.setEnergy(net.getBuffer());
                net.energyScale = 0;
                net.energyContainer.setEmpty();
            }
        } else if (!net.energyContainer.isEmpty()) {
            energyContainer.setEnergy(energyContainer.getEnergy().add(net.getBuffer()));
            net.energyContainer.setEmpty();
        }
        super.adoptTransmittersAndAcceptorsFrom(net);
    }

    @Nonnull
    @Override
    public FloatingLong getBuffer() {
        return energyContainer.getEnergy();
    }

    @Override
    public void absorbBuffer(IGridTransmitter<IStrictEnergyHandler, EnergyNetwork, FloatingLong> transmitter) {
        FloatingLong energy = transmitter.getBuffer();
        if (energy != null && !energy.isEmpty()) {
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
    public synchronized void updateCapacity() {
        floatingLongCapacity = FloatingLong.getNewZero();
        for (IGridTransmitter<IStrictEnergyHandler, EnergyNetwork, FloatingLong> transmitter : transmitters) {
            if (transmitter instanceof TileEntityTransmitter) {
                TileEntity tileEntity = ((TileEntityTransmitter<?, ?, ?>) transmitter).getTileEntity();
                if (tileEntity instanceof TileEntityUniversalCable) {
                    //TODO: Add a way to get this via the IGridTransmitter
                    floatingLongCapacity.plusEqual(((TileEntityUniversalCable) tileEntity).getCableCapacity());
                } else {
                    floatingLongCapacity.plusEqual(FloatingLong.create(transmitter.getCapacity()));
                }
            } else {
                floatingLongCapacity.plusEqual(FloatingLong.create(transmitter.getCapacity()));
            }
        }
        capacity = floatingLongCapacity.intValue();
    }

    @Nonnull
    public FloatingLong getCapacityAsFloatingLong() {
        return floatingLongCapacity;
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
                if (handler != null && handler.insertEnergy(FloatingLong.ONE, Action.SIMULATE).smallerThan(FloatingLong.ONE)) {
                    target.addHandler(side, handler);
                }
            }
            int curHandlers = target.getHandlers().size();
            if (curHandlers > 0) {
                targets.add(target);
                totalHandlers += curHandlers;
            }
        }
        //TODO: Evaluate copying this
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
            prevTransferAmount = FloatingLong.ZERO;
            double currentPowerScale = getScale();
            if (Math.abs(currentPowerScale - energyScale) > 0.01 || (currentPowerScale != energyScale && (currentPowerScale == 0 || currentPowerScale == 1))) {
                needsUpdate = true;
            }
            if (needsUpdate) {
                MinecraftForge.EVENT_BUS.post(new EnergyTransferEvent(this, currentPowerScale));
                energyScale = currentPowerScale;
                needsUpdate = false;
            }
            if (!energyContainer.isEmpty()) {
                prevTransferAmount = tickEmit(energyContainer.getEnergy());
                energyContainer.extract(prevTransferAmount, Action.EXECUTE, AutomationType.INTERNAL);
            }
        }
    }

    public double getScale() {
        if (energyContainer.isEmpty() || energyContainer.getMaxEnergy().isEmpty()) {
            return 0;
        }
        //TODO: Figure this out better
        //return Math.max(Math.min(Math.ceil(Math.log10(energyContainer.getEnergy() * 20) * 2) / 10, 1), energyContainer.getEnergy() / energyContainer.getMaxEnergy());
        return FloatingLong.ONE.max(energyContainer.getEnergy().divide(energyContainer.getMaxEnergy())).doubleValue();
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