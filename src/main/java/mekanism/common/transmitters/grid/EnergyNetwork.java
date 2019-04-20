package mekanism.common.transmitters.grid;

import java.util.Collection;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import javax.annotation.Nullable;
import mekanism.api.Coord4D;
import mekanism.api.energy.EnergyStack;
import mekanism.api.transmitters.DynamicNetwork;
import mekanism.api.transmitters.IGridTransmitter;
import mekanism.common.base.EnergyAcceptorTarget;
import mekanism.common.base.EnergyAcceptorWrapper;
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

    public double tickEmit(double energyToSend) {
        if (FMLCommonHandler.instance().getEffectiveSide().isClient()) {
            return 0;
        }

        joulesTransmitted = doEmit(energyToSend);
        return joulesTransmitted;
    }

    public double emit(double energyToSend, boolean doEmit) {
        double toUse = Math.min(getEnergyNeeded(), energyToSend);

        if (doEmit) {
            buffer.amount += toUse;
        }

        return energyToSend - toUse;
    }

    /**
     * @return sent
     */
    public double doEmit(double energyToSend) {
        Set<EnergyAcceptorTarget> availableAcceptors = getAcceptorTargets();
        double sent = 0;

        if (!availableAcceptors.isEmpty()) {
            double energyToSplit = energyToSend;
            int toSplitAmong = availableAcceptors.size();
            double amountPer = energyToSplit / toSplitAmong;

            //Simulate addition
            for (EnergyAcceptorTarget target : availableAcceptors) {
                Map<EnumFacing, EnergyAcceptorWrapper> wrappers = target.getWrappers();
                for (Entry<EnumFacing, EnergyAcceptorWrapper> entry : wrappers.entrySet()) {
                    EnumFacing side = entry.getKey();
                    double amountNeeded = entry.getValue().acceptEnergy(side, energyToSend, true);
                    boolean canGive = amountNeeded <= amountPer;
                    //TODO: Make addAmount return 0 if canGive is true, and otherwise return amountNeeded
                    // That way we can run it as target.simulate(side, energyToSend)
                    target.addAmount(side, amountNeeded, canGive);
                    if (canGive) {
                        //If we are giving it, then lower the amount we are checking/splitting
                        energyToSplit -= amountNeeded;
                        toSplitAmong--;
                        amountPer = energyToSplit / toSplitAmong;
                    }
                }
            }
            //TODO: Second set of ones that have things still with needed?
            // Or just loop over all and have boolean lookup to see if there are needed ones

            //TODO: Make this more efficient as it is the least efficient part of it
            // Custom datastructure sorted by value??
            boolean amountPerChanged = true;
            while (amountPerChanged) {
                amountPerChanged = false;
                double amountPerLast = amountPer;
                //TODO: Recalc the amountPerChanged between one target to the next?
                for (EnergyAcceptorTarget target : availableAcceptors) {
                    //TODO: have target keep track of moving needed to given?
                    Iterator<Entry<EnumFacing, Double>> iterator = target.getNeededIterator();
                    while (iterator.hasNext()) {
                        Entry<EnumFacing, Double> needInfo = iterator.next();
                        Double amountNeeded = needInfo.getValue();
                        if (amountNeeded <= amountPer) {
                            target.addGiven(needInfo.getKey(), amountNeeded);
                            //Remove it as it no longer valid
                            iterator.remove();
                            //Adjust the energy split
                            energyToSplit -= amountNeeded;
                            toSplitAmong--;
                            amountPer = energyToSplit / toSplitAmong;
                            if (!amountPerChanged && amountPer != amountPerLast) {
                                //We changed our amount so set it back to true so that we know we need
                                // to loop over things again
                                amountPerChanged = true;
                            }
                        }
                        //Continue checking this iteration of it in case we happen to be
                        // getting things in a bad order so that we don't recheck
                        // the same values many times
                    }
                }
            }

            //Give them all the energy we calculated they deserve/want
            for (EnergyAcceptorTarget target : availableAcceptors) {
                sent += target.sendGivenWithDefault(amountPer);
            }
        }

        return sent;
    }

    @Override
    public Set<Pair<Coord4D, EnergyAcceptorWrapper>> getAcceptors(Object data) {
        Set<Pair<Coord4D, EnergyAcceptorWrapper>> toReturn = new HashSet<>();

        if (FMLCommonHandler.instance().getEffectiveSide().isClient()) {
            return toReturn;
        }

        for (Coord4D coord : possibleAcceptors.keySet()) {
            EnumSet<EnumFacing> sides = acceptorDirections.get(coord);

            if (sides == null || sides.isEmpty()) {
                continue;
            }

            TileEntity tile = coord.getTileEntity(getWorld());

            for (EnumFacing side : sides) {
                EnergyAcceptorWrapper acceptor = EnergyAcceptorWrapper.get(tile, side);

                if (acceptor != null) {
                    if (acceptor.canReceiveEnergy(side) && acceptor.needsEnergy(side)) {
                        toReturn.add(Pair.of(coord, acceptor));
                        break;
                    }
                }
            }
        }

        return toReturn;
    }

    public Set<EnergyAcceptorTarget> getAcceptorTargets() {
        Set<EnergyAcceptorTarget> toReturn = new HashSet<>();

        if (FMLCommonHandler.instance().getEffectiveSide().isClient()) {
            return toReturn;
        }

        for (Coord4D coord : possibleAcceptors.keySet()) {
            EnumSet<EnumFacing> sides = acceptorDirections.get(coord);
            if (sides == null || sides.isEmpty()) {
                continue;
            }
            TileEntity tile = coord.getTileEntity(getWorld());
            if (tile == null) {
                continue;
            }
            EnergyAcceptorTarget target = new EnergyAcceptorTarget(coord);
            for (EnumFacing side : sides) {
                EnergyAcceptorWrapper acceptor = EnergyAcceptorWrapper.get(tile, side);
                if (acceptor != null && acceptor.canReceiveEnergy(side) && acceptor.needsEnergy(side)) {
                    target.addSide(side, acceptor);
                }
            }
            if (target.hasAcceptors()) {
                toReturn.add(target);
            }
        }

        return toReturn;
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
                buffer.amount -= tickEmit(buffer.amount);
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
