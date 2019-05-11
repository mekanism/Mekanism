package mekanism.api.transmitters;

import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;
import mekanism.api.Coord4D;
import mekanism.api.MekanismAPI;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.Phase;
import net.minecraftforge.fml.common.gameevent.TickEvent.ServerTickEvent;
import net.minecraftforge.fml.relauncher.Side;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class TransmitterNetworkRegistry {

    private static TransmitterNetworkRegistry INSTANCE = new TransmitterNetworkRegistry();
    private static boolean loaderRegistered = false;
    private static Logger logger = LogManager.getLogger("MekanismTransmitters");
    private Set<DynamicNetwork> networks = new HashSet<>();
    private Set<DynamicNetwork> networksToChange = new HashSet<>();
    private Set<IGridTransmitter> invalidTransmitters = new HashSet<>();
    private Map<Coord4D, IGridTransmitter> orphanTransmitters = new HashMap<>();
    private Map<Coord4D, IGridTransmitter> newOrphanTransmitters = new HashMap<>();

    public static void initiate() {
        if (!loaderRegistered) {
            loaderRegistered = true;
            MinecraftForge.EVENT_BUS.register(INSTANCE);
        }
    }

    public static void reset() {
        getInstance().networks.clear();
        getInstance().networksToChange.clear();
        getInstance().invalidTransmitters.clear();
        getInstance().orphanTransmitters.clear();
        getInstance().newOrphanTransmitters.clear();
    }

    public static void invalidateTransmitter(IGridTransmitter transmitter) {
        getInstance().invalidTransmitters.add(transmitter);
    }

    public static void registerOrphanTransmitter(IGridTransmitter transmitter) {
        Coord4D coord = transmitter.coord();
        IGridTransmitter previous = getInstance().newOrphanTransmitters.put(coord, transmitter);
        if (previous != null && previous != transmitter) {
            logger.error("Different orphan transmitter was already registered at location! {}", coord.toString());
        }
    }

    public static void registerChangedNetwork(DynamicNetwork network) {
        getInstance().networksToChange.add(network);
    }

    public static TransmitterNetworkRegistry getInstance() {
        return INSTANCE;
    }

    public void registerNetwork(DynamicNetwork network) {
        networks.add(network);
    }

    public void removeNetwork(DynamicNetwork network) {
        networks.remove(network);
        networksToChange.remove(network);
    }

    @SubscribeEvent
    public void onTick(ServerTickEvent event) {
        if (event.phase == Phase.END && event.side == Side.SERVER) {
            tickEnd();
        }
    }

    public void tickEnd() {
        removeInvalidTransmitters();
        assignOrphans();
        commitChanges();
        for (DynamicNetwork net : networks) {
            net.tick();
        }
    }

    public void removeInvalidTransmitters() {
        if (MekanismAPI.debug && !invalidTransmitters.isEmpty()) {
            logger.info("Dealing with " + invalidTransmitters.size() + " invalid Transmitters");
        }

        for (IGridTransmitter invalid : invalidTransmitters) {
            if (!(invalid.isOrphan() && invalid.isValid())) {
                DynamicNetwork n = invalid.getTransmitterNetwork();
                if (n != null) {
                    n.invalidate();
                }
            }
        }

        invalidTransmitters.clear();
    }

    public void assignOrphans() {
        orphanTransmitters = new HashMap<>(newOrphanTransmitters);
        newOrphanTransmitters.clear();

        if (MekanismAPI.debug && !orphanTransmitters.isEmpty()) {
            logger.info("Dealing with " + orphanTransmitters.size() + " orphan Transmitters");
        }

        for (IGridTransmitter orphanTransmitter : new HashMap<>(orphanTransmitters).values()) {
            DynamicNetwork network = getNetworkFromOrphan(orphanTransmitter);
            if (network != null) {
                networksToChange.add(network);
                network.register();
            }
        }

        orphanTransmitters.clear();
    }

    public <A, N extends DynamicNetwork<A, N, BUFFER>, BUFFER> DynamicNetwork<A, N, BUFFER> getNetworkFromOrphan(IGridTransmitter<A, N, BUFFER> startOrphan) {
        if (startOrphan.isValid() && startOrphan.isOrphan()) {
            OrphanPathFinder<A, N, BUFFER> finder = new OrphanPathFinder<>(startOrphan);
            finder.start();
            N network;

            switch (finder.networksFound.size()) {
                case 0:
                    if (MekanismAPI.debug) {
                        logger.info("No networks found. Creating new network for " + finder.connectedTransmitters.size() + " transmitters");
                    }
                    network = startOrphan.createEmptyNetwork();
                    break;
                case 1:
                    if (MekanismAPI.debug) {
                        logger.info("Adding " + finder.connectedTransmitters.size() + " transmitters to single found network");
                    }
                    network = finder.networksFound.iterator().next();
                    break;
                default:
                    if (MekanismAPI.debug) {
                        logger.info("Merging " + finder.networksFound.size() + " networks with " + finder.connectedTransmitters.size() + " new transmitters");
                    }
                    network = startOrphan.mergeNetworks(finder.networksFound);
            }

            network.addNewTransmitters(finder.connectedTransmitters);

            if (finder.someNetworksFailed) {
                //At least one network that connection was attempted with is not compatible
                // So inform this transmitter that there was a failed connection attempt
                // so that it can refresh the connections
                startOrphan.connectionFailed();
            }
            return network;
        }
        return null;
    }

    public void commitChanges() {
        for (DynamicNetwork network : networksToChange) {
            network.commit();
        }
        networksToChange.clear();
    }

    @Override
    public String toString() {
        return "Network Registry:\n" + networks;
    }

    public String[] toStrings() {
        String[] strings = new String[networks.size()];
        int i = 0;

        for (DynamicNetwork network : networks) {
            strings[i++] = network.toString();
        }
        return strings;
    }

    public class OrphanPathFinder<A, N extends DynamicNetwork<A, N, BUFFER>, BUFFER> {

        public IGridTransmitter<A, N, BUFFER> startPoint;

        public HashSet<Coord4D> iterated = new HashSet<>();

        public HashSet<IGridTransmitter<A, N, BUFFER>> connectedTransmitters = new HashSet<>();
        public HashSet<N> networksFound = new HashSet<>();

        private Deque<Coord4D> queue = new LinkedList<>();

        public boolean someNetworksFailed;

        public OrphanPathFinder(IGridTransmitter<A, N, BUFFER> start) {
            startPoint = start;
        }

        public void start() {
            if (queue.peek() != null) {
                logger.error("OrphanPathFinder queue was not empty?!");
                queue.clear();
            }
            queue.push(startPoint.coord());
            while (queue.peek() != null) {
                iterate(queue.removeFirst());
            }
        }

        public void iterate(Coord4D from) {
            if (iterated.contains(from)) {
                return;
            }

            iterated.add(from);

            if (orphanTransmitters.containsKey(from)) {
                IGridTransmitter<A, N, BUFFER> transmitter = orphanTransmitters.get(from);

                if (transmitter.isValid() && transmitter.isOrphan() &&
                    (connectedTransmitters.isEmpty() || connectedTransmitters.stream().anyMatch(existing -> existing.isCompatibleWith(transmitter)))) {
                    connectedTransmitters.add(transmitter);
                    transmitter.setOrphan(false);

                    for (EnumFacing direction : EnumFacing.VALUES) {
                        if (direction.getAxis().isHorizontal() && !transmitter.world()
                              .isBlockLoaded(from.getPos().offset(direction))) {
                            continue;
                        }
                        Coord4D directionCoord = transmitter.getAdjacentConnectableTransmitterCoord(direction);
                        if (directionCoord != null && !iterated.contains(directionCoord)) {
                            queue.addLast(directionCoord);
                        }
                    }
                }
            } else {
                addNetworkToIterated(from);
            }
        }

        public void addNetworkToIterated(Coord4D from) {
            N net = startPoint.getExternalNetwork(from);
            //Make sure that there is an external network and that it is compatible with this buffer
            if (net != null && net.compatibleWithBuffer(startPoint.getBuffer())) {
                if (networksFound.isEmpty() || networksFound.iterator().next().isCompatibleWith(net)) {
                    networksFound.add(net);
                } else {
                    //If it a network was found but it is incompatible, then mark we have a failed
                    // network so we can inform the transmitter it should do a tick delayed refresh
                    someNetworksFailed = true;
                }
            }
        }
    }
}