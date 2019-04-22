package mekanism.common.transmitters.grid;

import java.util.Collection;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.Set;
import javax.annotation.Nullable;
import mekanism.api.Coord4D;
import mekanism.api.energy.EnergyStack;
import mekanism.api.transmitters.DynamicNetwork;
import mekanism.api.transmitters.IGridTransmitter;
import mekanism.common.base.EnergyAcceptorWrapper;
import mekanism.common.base.target.EnergyAcceptorTarget;
import mekanism.common.util.EmitUtils;
import mekanism.common.util.MekanismUtils;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.eventhandler.Event;
import org.apache.commons.lang3.tuple.Pair;

public class EnergyNetwork extends DynamicNetwork<EnergyAcceptorWrapper, EnergyNetwork, EnergyStack> {

    public double clientEnergyScale = 0;
    public EnergyStack buffer = new EnergyStack(0);
    private double lastPowerScale = 0;
    private double joulesTransmitted = 0;
    private double jouleBufferLastTick = 0;

    public EnergyNetwork() {
    }

    public EnergyNetwork(Collection<EnergyNetwork> networks) {
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
        if (net.jouleBufferLastTick > jouleBufferLastTick || net.clientEnergyScale > clientEnergyScale) {
            clientEnergyScale = net.clientEnergyScale;
            jouleBufferLastTick = net.jouleBufferLastTick;
            joulesTransmitted = net.joulesTransmitted;
            lastPowerScale = net.lastPowerScale;
        }

        buffer.amount += net.buffer.amount;
        super.adoptTransmittersAndAcceptorsFrom(net);
    }

    public static double round(double d) {
        return Math.round(d * 10000) / 10000;
    }

    @Nullable
    public EnergyStack getBuffer() {
        return buffer;
    }

    @Override
    public void absorbBuffer(IGridTransmitter<EnergyAcceptorWrapper, EnergyNetwork, EnergyStack> transmitter) {
        EnergyStack energy = transmitter.getBuffer();
        buffer.amount += energy.amount;
        energy.amount = 0;
    }

    @Override
    public void clampBuffer() {
        if (buffer.amount > getCapacity()) {
            buffer.amount = getCapacity();
        }

        if (buffer.amount < 0) {
            buffer.amount = 0;
        }
    }

    @Override
    protected void updateMeanCapacity() {
        int numCables = transmitters.size();
        double reciprocalSum = 0;

        for (IGridTransmitter<EnergyAcceptorWrapper, EnergyNetwork, EnergyStack> cable : transmitters) {
            reciprocalSum += 1.0 / (double) cable.getCapacity();
        }

        meanCapacity = (double) numCables / reciprocalSum;
    }

    public double getEnergyNeeded() {
        if (FMLCommonHandler.instance().getEffectiveSide().isClient()) {
            return 0;
        }

        return getCapacity() - buffer.amount;
    }

    private double tickEmit(double energyToSend) {
        if (FMLCommonHandler.instance().getEffectiveSide().isClient()) {
            return 0;
        }

        Set<EnergyAcceptorTarget> targets = new HashSet<>();
        int totalHandlers = 0;
        for (Coord4D coord : possibleAcceptors.keySet()) {
            EnumSet<EnumFacing> sides = acceptorDirections.get(coord);
            if (sides == null || sides.isEmpty()) {
                continue;
            }
            TileEntity tile = coord.getTileEntity(getWorld());
            if (tile == null) {
                continue;
            }
            EnergyAcceptorTarget target = new EnergyAcceptorTarget();
            for (EnumFacing side : sides) {
                EnergyAcceptorWrapper acceptor = EnergyAcceptorWrapper.get(tile, side);
                if (acceptor != null && acceptor.canReceiveEnergy(side) && acceptor.needsEnergy(side)) {
                    target.addHandler(side, acceptor);
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

    public double emit(double energyToSend, boolean doEmit) {
        double toUse = Math.min(getEnergyNeeded(), energyToSend);

        if (doEmit) {
            buffer.amount += toUse;
        }

        return energyToSend - toUse;
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

        if (FMLCommonHandler.instance().getEffectiveSide().isServer()) {
            if (Math.abs(currentPowerScale - lastPowerScale) > 0.01 || (currentPowerScale != lastPowerScale && (
                  currentPowerScale == 0 || currentPowerScale == 1))) {
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
              getCapacity() == 0 ? 0 : buffer.amount / getCapacity());
    }

    public void clearJoulesTransmitted() {
        jouleBufferLastTick = buffer.amount;
        joulesTransmitted = 0;
    }

    public double getPower() {
        return jouleBufferLastTick * 20;
    }

    @Override
    public String getNeededInfo() {
        return MekanismUtils.getEnergyDisplay(getEnergyNeeded());
    }

    @Override
    public String getStoredInfo() {
        return MekanismUtils.getEnergyDisplay(buffer.amount);
    }

    @Override
    public String getFlowInfo() {
        return MekanismUtils.getEnergyDisplay(joulesTransmitted) + "/t";
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
