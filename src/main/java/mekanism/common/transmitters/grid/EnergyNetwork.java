package mekanism.common.transmitters.grid;

import java.util.Collection;
import java.util.EnumSet;
import java.util.HashMap;
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
        double sent = 0;

        Set<EnergyAcceptorTarget> availableAcceptors = getAcceptorTargets();

        //TODO: Safety checks for if sent goes above energyToSend
        // Or if something becomes negative??
        if (!availableAcceptors.isEmpty()) {
            double energyToSplit = energyToSend;
            int toSplitAmong = availableAcceptors.size();
            double amountPer = energyToSplit / toSplitAmong;
            //Simulate it
            Map<EnergyAcceptorWrapper, Double> needed = new HashMap<>();
            Map<EnergyAcceptorWrapper, Double> given = new HashMap<>();

            Map<EnergyAcceptorWrapper, EnumFacing> wrapperSides = new HashMap<>();
            //TODO: Keep track of EnergyAcceptorTarget longer
            // The target could handle *needed*, *given* against a smaller EnumFacing map
            // instead of having such large maps with big object keys.
            // The code would be a bit of a mess for removing/changing it over
            // We will need to loop over each one and then over each one's needed map

            for (EnergyAcceptorTarget target : availableAcceptors) {
                Map<EnumFacing, EnergyAcceptorWrapper> wrappers = target.getWrappers();
                for (Entry<EnumFacing, EnergyAcceptorWrapper> entry : wrappers.entrySet()) {
                    EnergyAcceptorWrapper acceptor = entry.getValue();

                    //Build up a map of all acceptors to which side we are interacting with them from
                    //TODO: Maybe improve efficiency by keeping track of target longer??
                    wrapperSides.put(acceptor, entry.getKey());

                    double amountNeeded = acceptor.acceptEnergy(entry.getKey(), energyToSend, true);
                    //Precheck so we don't have to loop as much below if we sanity check amount
                    //TODO: Finish comment
                    if (amountNeeded <= amountPer) {
                        given.put(acceptor, amountNeeded);
                        energyToSplit -= amountNeeded;
                        toSplitAmong--;
                        amountPer = energyToSplit / toSplitAmong;
                    } else {
                        needed.put(acceptor, amountNeeded);
                    }
                }
            }

            //TODO: Make this more efficient as it is the least efficient part of it
            // Custom datastructure sorted by value??
            boolean amountPerChanged = true;
            while (amountPerChanged) {
                amountPerChanged = false;
                Iterator<Entry<EnergyAcceptorWrapper, Double>> iterator = needed.entrySet().iterator();
                while (iterator.hasNext()) {
                    Entry<EnergyAcceptorWrapper, Double> needInfo = iterator.next();
                    Double amountNeeded = needInfo.getValue();
                    if (amountNeeded <= amountPer) {
                        given.put(needInfo.getKey(), amountNeeded);
                        //Remove it as it no longer valid
                        iterator.remove();
                        //Adjust the energy split
                        energyToSplit -= amountNeeded;
                        toSplitAmong--;
                        amountPer = energyToSplit / toSplitAmong;
                        //We changed our amount so set it back to true
                        amountPerChanged = true;
                    }
                    //Continue checking this iteration of it in case we happen to be
                    // getting things in a bad order so that we don't recheck
                    // the same values many times
                }
            }



            //If needed is not empty then we add the remaining ones at being given a fair split
            // of the remaining energy
            for (EnergyAcceptorWrapper wrapper : needed.keySet()) {
                given.put(wrapper, amountPer);
            }

            //Give them all the energy we calculated they deserve/want
            for (Entry<EnergyAcceptorWrapper, Double> giveInfo : given.entrySet()) {
                EnergyAcceptorWrapper acceptor = giveInfo.getKey();
                Double amount = giveInfo.getValue();
                //Give it power and add how much actually got accepted instead of how much
                // we attempted to give it
                if (wrapperSides.containsKey(acceptor)) {
                    sent += acceptor.acceptEnergy(wrapperSides.get(acceptor), amount, false);
                } else {
                    //TODO: Log error
                }
            }
        }

        /*List<Pair<Coord4D, EnergyAcceptorWrapper>> availableAcceptorsOLD = new ArrayList<>(getAcceptors(null));

        if (!availableAcceptorsOLD.isEmpty()) {
            //TODO: Loop over acceptors TWICE.
            // First time simulate using the max value for that acceptor type
            // - Probably should use energyToSend as upper limit
            // Then use math to calculate a fair balance for giving it to the different acceptors
            // Second time loop over it and give each acceptor their calculated amount
            // This way we don't have cases where we are giving a machine more energy per tick
            // than it wants

            //TODO: Algorithm
            // toSend = energyToSend / numAcceptors
            // if energyWanted < toSend then
            // - adjust toSend
            // - check remaining acceptors
            // - recheck all the acceptors that we didn't mark as wanting less power
            //   than we are willing to give them
            // - Once toSend has been checked against all acceptors at least once without
            //   changing then we evenly give the remaining (the new value of toSend) to
            //   each acceptor

            int divider = availableAcceptorsOLD.size();
            double remaining = energyToSend % divider;
            double sending = (energyToSend - remaining) / divider;
            //TODO: Rewrite EnergyAcceptorWrapper? Given it may be sided
            // This actually probably is already handled and is the reason it is a set/list
            // instead of a map

            for (Pair<Coord4D, EnergyAcceptorWrapper> pair : availableAcceptorsOLD) {
                EnergyAcceptorWrapper acceptor = pair.getRight();
                double currentSending = sending + remaining;
                EnumSet<EnumFacing> sides = acceptorDirections.get(pair.getLeft());

                if (sides == null || sides.isEmpty()) {
                    continue;
                }

                //TODO: Except if above is true about it really being different Wrappers...
                // Then why does it use the same AcceptorWrapper down here
                // Better datastructure might be something like:
                // Map<Coord4D, Map<EnumFacing, EnergyAcceptorWrapper>>
                // Or maybe something that stores sidedness
                // Map<Coord4D, MultiSidedEnergyAcceptorWrapper>
                // and have that store the different ones it can use so that we then
                // can have it so that we don't need multiple references to ones that
                // do not care about the side?

                //TODO: This really would probably be better as something like
                // Map<SidedCoord4D, EnergyAcceptorWrapper>
                // AMAZINGLY I think it actually works properly given the pair does not compare
                // the values so for now it may be fine until implementing the optimization of ignoring sidedness?
                // EXCEPT does it really...? Because we check the acceptorDirections to see which sides
                // we can connect to it from, BUT we use the same acceptor regardless instead of
                // getting the one that is sided
                //TODO: If we instead were to getAcceptors (at least for energy) to
                // Map<Coord4D, Map<EnumFacing, EnergyAcceptorWrapper>> and then get the correct
                // acceptors based on that...

                //TODO: If we do have a MultiSidedEnergyAcceptorWrapper/EnergyAcceptorTarget
                // then we should have it also keep track of how many "sides" it *has* even
                // if the impl disregards sidedness. This way we can then batch how much
                // energy we actually send into one call rather than doing it as multiple
                // NOTE: If it DOES have sidedness we need to give each side the amount of
                // energy we cannot batch it all into one side in case it does not accept
                // more than a specific amount of power from that side
                for (EnumFacing side : sides) {
                    double prev = sent;

                    sent += acceptor.acceptEnergy(side, currentSending, false);

                    if (sent > prev) {
                        break;
                    }
                }
            }
        }*/

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
