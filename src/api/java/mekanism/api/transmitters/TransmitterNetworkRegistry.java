package mekanism.api.transmitters;

import java.util.Deque;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import javax.annotation.Nullable;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import mekanism.api.Coord4D;
import mekanism.api.MekanismAPI;
import net.minecraft.util.Direction;
import net.minecraft.util.text.ITextComponent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.TickEvent.Phase;
import net.minecraftforge.event.TickEvent.ServerTickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public class TransmitterNetworkRegistry {

    /**
     * Cached value of {@link Direction#values()}. DO NOT MODIFY THIS LIST.
     */
    private static final Direction[] DIRECTIONS = Direction.values();

    private static final TransmitterNetworkRegistry INSTANCE = new TransmitterNetworkRegistry();
    private static boolean loaderRegistered = false;
    private static final Logger logger = LogManager.getLogger("MekanismTransmitters");
    private final Set<DynamicNetwork<?, ?, ?>> networks = new ObjectOpenHashSet<>();
    private final Set<DynamicNetwork<?, ?, ?>> networksToChange = new ObjectOpenHashSet<>();
    private final Set<IGridTransmitter<?, ?, ?>> invalidTransmitters = new ObjectOpenHashSet<>();
    private Map<Coord4D, IGridTransmitter<?, ?, ?>> orphanTransmitters = new Object2ObjectOpenHashMap<>();
    private final Map<Coord4D, IGridTransmitter<?, ?, ?>> newOrphanTransmitters = new Object2ObjectOpenHashMap<>();

    private Map<UUID, DynamicNetwork<?, ?, ?>> clientNetworks = new Object2ObjectOpenHashMap<>();

    public void addClientNetwork(UUID networkID, DynamicNetwork<?, ?, ?> network) {
        if (!clientNetworks.containsKey(networkID)) {
            clientNetworks.put(networkID, network);
        }
    }

    @Nullable
    public DynamicNetwork<?, ?, ?> getClientNetwork(UUID networkID) {
        return clientNetworks.get(networkID);
    }

    public void removeClientNetwork(DynamicNetwork<?, ?, ?> network) {
        clientNetworks.remove(network.getUUID());
    }

    public void clearClientNetworks() {
        clientNetworks.clear();
    }

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

    public static void invalidateTransmitter(IGridTransmitter<?, ?, ?> transmitter) {
        getInstance().invalidTransmitters.add(transmitter);
    }

    public static void registerOrphanTransmitter(IGridTransmitter<?, ?, ?> transmitter) {
        Coord4D coord = transmitter.coord();
        IGridTransmitter<?, ?, ?> previous = getInstance().newOrphanTransmitters.put(coord, transmitter);
        if (previous != null && previous != transmitter) {
            logger.error("Different orphan transmitter was already registered at location! {}", coord.toString());
        }
    }

    public static void registerChangedNetwork(DynamicNetwork<?, ?, ?> network) {
        getInstance().networksToChange.add(network);
    }

    public static TransmitterNetworkRegistry getInstance() {
        return INSTANCE;
    }

    public void registerNetwork(DynamicNetwork<?, ?, ?> network) {
        networks.add(network);
    }

    public void removeNetwork(DynamicNetwork<?, ?, ?> network) {
        networks.remove(network);
        networksToChange.remove(network);
    }

    @SubscribeEvent
    public void onTick(ServerTickEvent event) {
        if (event.phase == Phase.END && event.side.isServer()) {
            tickEnd();
        }
    }

    public void tickEnd() {
        removeInvalidTransmitters();
        assignOrphans();
        commitChanges();
        for (DynamicNetwork<?, ?, ?> net : networks) {
            net.tick();
        }
    }

    public void removeInvalidTransmitters() {
        if (MekanismAPI.debug && !invalidTransmitters.isEmpty()) {
            logger.info("Dealing with {} invalid Transmitters", invalidTransmitters.size());
        }

        for (IGridTransmitter<?, ?, ?> invalid : invalidTransmitters) {
            if (!(invalid.isOrphan() && invalid.isValid())) {
                DynamicNetwork<?, ?, ?> n = invalid.getTransmitterNetwork();
                if (n != null) {
                    n.invalidate(invalid);
                }
            }
        }

        invalidTransmitters.clear();
    }

    public void assignOrphans() {
        orphanTransmitters = new Object2ObjectOpenHashMap<>(newOrphanTransmitters);
        newOrphanTransmitters.clear();

        if (MekanismAPI.debug && !orphanTransmitters.isEmpty()) {
            logger.info("Dealing with {} orphan Transmitters", orphanTransmitters.size());
        }

        for (IGridTransmitter<?, ?, ?> orphanTransmitter : new Object2ObjectOpenHashMap<>(orphanTransmitters).values()) {
            DynamicNetwork<?, ?, ?> network = getNetworkFromOrphan(orphanTransmitter);
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
                        logger.info("No networks found. Creating new network for {} transmitters", finder.connectedTransmitters.size());
                    }
                    network = startOrphan.createEmptyNetwork();
                    break;
                case 1:
                    if (MekanismAPI.debug) {
                        logger.info("Adding {} transmitters to single found network", finder.connectedTransmitters.size());
                    }
                    network = finder.networksFound.iterator().next();
                    break;
                default:
                    if (MekanismAPI.debug) {
                        logger.info("Merging {} networks with {} new transmitters", finder.networksFound.size(), finder.connectedTransmitters.size());
                    }
                    //TODO: Should we take one of the existing network's uuids?
                    network = startOrphan.mergeNetworks(finder.networksFound);
            }

            network.addNewTransmitters(finder.connectedTransmitters);
            return network;
        }
        return null;
    }

    public void commitChanges() {
        for (DynamicNetwork<?, ?, ?> network : networksToChange) {
            network.commit();
        }
        networksToChange.clear();
    }

    @Override
    public String toString() {
        return "Network Registry:\n" + networks;
    }

    public ITextComponent[] toComponents() {
        ITextComponent[] components = new ITextComponent[networks.size()];
        int i = 0;

        for (DynamicNetwork<?, ?, ?> network : networks) {
            components[i++] = network.getTextComponent();
        }
        return components;
    }

    public class OrphanPathFinder<A, N extends DynamicNetwork<A, N, BUFFER>, BUFFER> {

        public IGridTransmitter<A, N, BUFFER> startPoint;

        public Set<Coord4D> iterated = new ObjectOpenHashSet<>();

        public Set<IGridTransmitter<A, N, BUFFER>> connectedTransmitters = new ObjectOpenHashSet<>();
        public Set<N> networksFound = new ObjectOpenHashSet<>();

        private Deque<Coord4D> queue = new LinkedList<>();

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
                IGridTransmitter<A, N, BUFFER> transmitter = (IGridTransmitter<A, N, BUFFER>) orphanTransmitters.get(from);

                if (transmitter.isValid() && transmitter.isOrphan() &&
                    (connectedTransmitters.isEmpty() || connectedTransmitters.stream().anyMatch(existing -> existing.isCompatibleWith(transmitter)))) {
                    connectedTransmitters.add(transmitter);
                    transmitter.setOrphan(false);

                    for (Direction direction : DIRECTIONS) {
                        if (direction.getAxis().isHorizontal() && !transmitter.world().isBlockPresent(from.getPos().offset(direction))) {
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
            if (net != null && net.compatibleWithBuffer(startPoint.getShare())) {
                if (networksFound.isEmpty() || networksFound.iterator().next().isCompatibleWith(net)) {
                    networksFound.add(net);
                }
            }
        }
    }
}