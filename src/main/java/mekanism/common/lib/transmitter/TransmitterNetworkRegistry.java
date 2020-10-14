package mekanism.common.lib.transmitter;

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import java.util.Deque;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import javax.annotation.Nullable;
import mekanism.api.Coord4D;
import mekanism.api.MekanismAPI;
import mekanism.common.Mekanism;
import mekanism.common.content.network.transmitter.BufferedTransmitter;
import mekanism.common.content.network.transmitter.Transmitter;
import mekanism.common.util.EnumUtils;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.TickEvent.Phase;
import net.minecraftforge.event.TickEvent.ServerTickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public class TransmitterNetworkRegistry {

    private static final TransmitterNetworkRegistry INSTANCE = new TransmitterNetworkRegistry();
    private static boolean loaderRegistered = false;
    private final Set<DynamicNetwork<?, ?, ?>> networks = new ObjectOpenHashSet<>();
    private final Set<DynamicNetwork<?, ?, ?>> networksToChange = new ObjectOpenHashSet<>();
    private final Set<Transmitter<?, ?, ?>> invalidTransmitters = new ObjectOpenHashSet<>();
    private Map<Coord4D, Transmitter<?, ?, ?>> orphanTransmitters = new Object2ObjectOpenHashMap<>();
    private final Map<Coord4D, Transmitter<?, ?, ?>> newOrphanTransmitters = new Object2ObjectOpenHashMap<>();
    private final Map<UUID, DynamicNetwork<?, ?, ?>> clientNetworks = new Object2ObjectOpenHashMap<>();

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

    public static void invalidateTransmitter(Transmitter<?, ?, ?> transmitter) {
        getInstance().invalidTransmitters.add(transmitter);
    }

    public static void registerOrphanTransmitter(Transmitter<?, ?, ?> transmitter) {
        Coord4D coord = Coord4D.get(transmitter.getTransmitterTile());
        Transmitter<?, ?, ?> previous = getInstance().newOrphanTransmitters.put(coord, transmitter);
        if (previous != null && previous != transmitter) {
            Mekanism.logger.error("Different orphan transmitter was already registered at location! {}", coord);
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
            removeInvalidTransmitters();
            assignOrphans();
            commitChanges();
            for (DynamicNetwork<?, ?, ?> net : networks) {
                net.onUpdate();
            }
        }
    }

    private void removeInvalidTransmitters() {
        if (MekanismAPI.debug && !invalidTransmitters.isEmpty()) {
            Mekanism.logger.info("Dealing with {} invalid Transmitters", invalidTransmitters.size());
        }
        for (Transmitter<?, ?, ?> invalid : invalidTransmitters) {
            removeInvalidTransmitter(invalid);
        }
        invalidTransmitters.clear();
    }

    private <NETWORK extends DynamicNetwork<?, NETWORK, TRANSMITTER>, TRANSMITTER extends Transmitter<?, NETWORK, TRANSMITTER>>
    void removeInvalidTransmitter(Transmitter<?, NETWORK, TRANSMITTER> invalid) {
        if (!invalid.isOrphan() || !invalid.isValid()) {
            NETWORK n = invalid.getTransmitterNetwork();
            if (n != null) {
                n.invalidate((TRANSMITTER) invalid);
            }
        }
    }

    private void assignOrphans() {
        orphanTransmitters = new Object2ObjectOpenHashMap<>(newOrphanTransmitters);
        newOrphanTransmitters.clear();

        if (MekanismAPI.debug && !orphanTransmitters.isEmpty()) {
            Mekanism.logger.info("Dealing with {} orphan Transmitters", orphanTransmitters.size());
        }

        for (Transmitter<?, ?, ?> orphanTransmitter : orphanTransmitters.values()) {
            if (orphanTransmitter.isValid() && orphanTransmitter.isOrphan()) {
                OrphanPathFinder<?, ?, ?> finder;
                if (orphanTransmitter instanceof BufferedTransmitter) {
                    finder = new BufferedOrphanPathFinder<>((BufferedTransmitter<?, ?, ?, ?>) orphanTransmitter);
                } else {
                    finder = new OrphanPathFinder<>(orphanTransmitter);
                }
                DynamicNetwork<?, ?, ?> network = getNetworkFromOrphan(finder);
                networksToChange.add(network);
                network.register();
            }
        }

        orphanTransmitters.clear();
    }

    private <ACCEPTOR, NETWORK extends DynamicNetwork<ACCEPTOR, NETWORK, TRANSMITTER>, TRANSMITTER extends Transmitter<ACCEPTOR, NETWORK, TRANSMITTER>>
    DynamicNetwork<ACCEPTOR, NETWORK, TRANSMITTER> getNetworkFromOrphan(OrphanPathFinder<ACCEPTOR, NETWORK, TRANSMITTER> finder) {
        finder.start();
        NETWORK network;
        switch (finder.networksFound.size()) {
            case 0:
                if (MekanismAPI.debug) {
                    Mekanism.logger.info("No networks found. Creating new network for {} transmitters", finder.connectedTransmitters.size());
                }
                network = finder.createEmptyNetwork();
                break;
            case 1:
                if (MekanismAPI.debug) {
                    Mekanism.logger.info("Adding {} transmitters to single found network", finder.connectedTransmitters.size());
                }
                network = finder.networksFound.iterator().next();
                break;
            default:
                if (MekanismAPI.debug) {
                    Mekanism.logger.info("Merging {} networks with {} new transmitters", finder.networksFound.size(), finder.connectedTransmitters.size());
                }
                network = finder.createNetworkByMerging();
        }
        network.addNewTransmitters(finder.connectedTransmitters);
        return network;
    }

    private void commitChanges() {
        Set<DynamicNetwork<?, ?, ?>> networks = new ObjectOpenHashSet<>(networksToChange);
        networksToChange.clear();
        for (DynamicNetwork<?, ?, ?> network : networks) {
            network.commit();
        }
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

    public class OrphanPathFinder<ACCEPTOR, NETWORK extends DynamicNetwork<ACCEPTOR, NETWORK, TRANSMITTER>,
          TRANSMITTER extends Transmitter<ACCEPTOR, NETWORK, TRANSMITTER>> {

        public final Set<TRANSMITTER> connectedTransmitters = new ObjectOpenHashSet<>();
        public final Set<NETWORK> networksFound = new ObjectOpenHashSet<>();
        public final Set<BlockPos> iterated = new ObjectOpenHashSet<>();
        private final Deque<BlockPos> queue = new LinkedList<>();
        public final TRANSMITTER startPoint;
        private final World world;

        protected OrphanPathFinder(Transmitter<ACCEPTOR, NETWORK, TRANSMITTER> start) {
            startPoint = (TRANSMITTER) start;
            world = startPoint.getTileWorld();
        }

        public void start() {
            if (queue.peek() != null) {
                Mekanism.logger.error("OrphanPathFinder queue was not empty?!");
                queue.clear();
            }
            queue.push(startPoint.getTilePos());
            while (queue.peek() != null) {
                iterate(queue.removeFirst());
            }
        }

        public void iterate(BlockPos from) {
            if (iterated.add(from)) {
                Coord4D fromCoord = new Coord4D(from, world);
                if (orphanTransmitters.containsKey(fromCoord)) {
                    Transmitter<?, ?, ?> transmitter = orphanTransmitters.get(fromCoord);
                    if (transmitter.isValid() && transmitter.isOrphan()) {
                        if (connectedTransmitters.isEmpty() || connectedTransmitters.stream().anyMatch(existing -> existing.isValidTransmitter(transmitter))) {
                            connectedTransmitters.add((TRANSMITTER) transmitter);
                            transmitter.setOrphan(false);
                            for (Direction direction : EnumUtils.DIRECTIONS) {
                                if (!direction.getAxis().isHorizontal() || world.isBlockPresent(from.offset(direction))) {
                                    BlockPos directionPos = transmitter.getAdjacentConnectableTransmitterPos(direction);
                                    if (directionPos != null && !iterated.contains(directionPos)) {
                                        queue.addLast(directionPos);
                                    }
                                }
                            }
                        }
                    }
                } else {
                    addNetworkToIterated(from);
                }
            }
        }

        public void addNetworkToIterated(BlockPos from) {
            //Make sure that there is an external network
            NETWORK net = startPoint.getExternalNetwork(from);
            if (net != null) {
                networksFound.add(net);
            }
        }

        public NETWORK createEmptyNetwork() {
            return startPoint.createEmptyNetwork();
        }

        public NETWORK createNetworkByMerging() {
            //TODO: Should we take one of the existing network's uuids?
            return startPoint.createNetworkByMerging(networksFound);
        }
    }

    public class BufferedOrphanPathFinder<ACCEPTOR, NETWORK extends DynamicBufferedNetwork<ACCEPTOR, NETWORK, BUFFER, TRANSMITTER>, BUFFER,
          TRANSMITTER extends BufferedTransmitter<ACCEPTOR, NETWORK, BUFFER, TRANSMITTER>> extends OrphanPathFinder<ACCEPTOR, NETWORK, TRANSMITTER> {

        protected BufferedOrphanPathFinder(BufferedTransmitter<ACCEPTOR, NETWORK, BUFFER, TRANSMITTER> start) {
            super(start);
        }

        @Override
        public void addNetworkToIterated(BlockPos from) {
            NETWORK net = startPoint.getExternalNetwork(from);
            //Make sure that there is an external network and that it is compatible with this buffer
            if (net != null && net.compatibleWithBuffer(startPoint.getShare())) {
                //Note: We need to check against all the networks we have found as if the first network we found is empty
                // then we will be "compatible" with any network
                if (networksFound.isEmpty() || networksFound.stream().allMatch(network -> network.isCompatibleWith(net))) {
                    networksFound.add(net);
                }
            }
        }
    }
}