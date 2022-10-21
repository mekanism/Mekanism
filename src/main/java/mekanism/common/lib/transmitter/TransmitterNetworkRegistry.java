package mekanism.common.lib.transmitter;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2BooleanMap;
import it.unimi.dsi.fastutil.objects.Object2BooleanOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import java.util.Collection;
import java.util.Deque;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import mekanism.api.Chunk3D;
import mekanism.api.Coord4D;
import mekanism.api.MekanismAPI;
import mekanism.common.Mekanism;
import mekanism.common.content.network.transmitter.Transmitter;
import mekanism.common.tile.transmitter.TileEntityTransmitter;
import mekanism.common.util.EnumUtils;
import mekanism.common.util.WorldUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ChunkMap;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.TickEvent.Phase;
import net.minecraftforge.event.TickEvent.ServerTickEvent;
import net.minecraftforge.event.level.ChunkTicketLevelUpdatedEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import org.jetbrains.annotations.Nullable;

public class TransmitterNetworkRegistry {

    private static final TransmitterNetworkRegistry INSTANCE = new TransmitterNetworkRegistry();
    private static boolean loaderRegistered = false;
    private final Multimap<Chunk3D, Transmitter<?, ?, ?>> transmitters = HashMultimap.create();
    private Object2BooleanMap<Chunk3D> changedTicketChunks = new Object2BooleanOpenHashMap<>();
    private final Set<DynamicNetwork<?, ?, ?>> networks = new ObjectOpenHashSet<>();
    private final Map<UUID, DynamicNetwork<?, ?, ?>> clientNetworks = new Object2ObjectOpenHashMap<>();
    private Map<Coord4D, Transmitter<?, ?, ?>> newOrphanTransmitters = new Object2ObjectOpenHashMap<>();
    private Set<Transmitter<?, ?, ?>> invalidTransmitters = new ObjectOpenHashSet<>();
    private Set<DynamicNetwork<?, ?, ?>> networksToChange = new ObjectOpenHashSet<>();

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
        getInstance().newOrphanTransmitters.clear();
        getInstance().transmitters.clear();
        getInstance().changedTicketChunks.clear();
    }

    public static void trackTransmitter(Transmitter<?, ?, ?> transmitter) {
        getInstance().transmitters.put(transmitter.getTileChunk(), transmitter);
    }

    public static void untrackTransmitter(Transmitter<?, ?, ?> transmitter) {
        getInstance().transmitters.remove(transmitter.getTileChunk(), transmitter);
    }

    public static void invalidateTransmitter(Transmitter<?, ?, ?> transmitter) {
        getInstance().invalidTransmitters.add(transmitter);
    }

    public static void registerOrphanTransmitter(Transmitter<?, ?, ?> transmitter) {
        if (!getInstance().invalidTransmitters.remove(transmitter)) {
            //If we weren't an invalid transmitter, then we need to add it as a new orphan, otherwise removing it is good enough
            // as if it was an orphan before it still will be one, and if it wasn't then it still will be part of the network it
            // was in.
            Coord4D coord = transmitter.getTileCoord();
            Transmitter<?, ?, ?> previous = getInstance().newOrphanTransmitters.put(coord, transmitter);
            if (previous != null && previous != transmitter) {
                Mekanism.logger.error("Different orphan transmitter was already registered at location! {}", coord);
            }
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
            handleChangedChunks();
            removeInvalidTransmitters();
            assignOrphans();
            commitChanges();
            for (DynamicNetwork<?, ?, ?> net : networks) {
                net.onUpdate();
            }
        }
    }

    @SubscribeEvent
    public void onTicketLevelChange(ChunkTicketLevelUpdatedEvent event) {
        int newTicketLevel = event.getNewTicketLevel();
        int oldTicketLevel = event.getOldTicketLevel();
        boolean loaded;
        if (oldTicketLevel > ChunkMap.MAX_VIEW_DISTANCE && newTicketLevel <= ChunkMap.MAX_VIEW_DISTANCE) {
            //Went from "unloaded" to loaded
            loaded = true;
        } else if (newTicketLevel > ChunkMap.MAX_VIEW_DISTANCE && oldTicketLevel <= ChunkMap.MAX_VIEW_DISTANCE) {
            //Went from loaded to "unloaded"
            loaded = false;
        } else {
            //Load type stayed the same, just exit
            return;
        }
        Chunk3D chunk = new Chunk3D(event.getLevel().dimension(), event.getChunkPos());
        if (transmitters.containsKey(chunk)) {
            //Only track it if we have any transmitters in that chunk
            if (changedTicketChunks.getOrDefault(chunk, loaded) != loaded) {
                //If we are watching the chunk and the loaded state isn't what we already had it as,
                // then remove it as it didn't actually change. In theory in all cases this is equivalent
                // to just checking if changeTicketChunks contains chunk, but is slightly more accurate
                // in case for some reason we get two load or unload notifications in a row
                changedTicketChunks.removeBoolean(chunk);
            } else {
                // Otherwise, make sure the map is aware of the change
                changedTicketChunks.put(chunk, loaded);
            }
        }
    }

    private void handleChangedChunks() {
        if (!changedTicketChunks.isEmpty()) {
            Object2BooleanMap<Chunk3D> changed = changedTicketChunks;
            changedTicketChunks = new Object2BooleanOpenHashMap<>();
            if (MekanismAPI.debug) {
                Mekanism.logger.info("Dealing with {} changed chunks", changed.size());
            }
            for (Object2BooleanMap.Entry<Chunk3D> entry : changed.object2BooleanEntrySet()) {
                Chunk3D chunk = entry.getKey();
                boolean loaded = entry.getBooleanValue();
                Collection<Transmitter<?, ?, ?>> chunkTransmitters = transmitters.get(chunk);
                for (Transmitter<?, ?, ?> transmitter : chunkTransmitters) {
                    transmitter.getTransmitterTile().chunkAccessibilityChange(loaded);
                }
                if (MekanismAPI.debug) {
                    Mekanism.logger.info("{} {} transmitters in chunk: {}, {}", loaded ? "Loaded" : "Unloaded", chunkTransmitters.size(), chunk.x, chunk.z);
                }
            }
        }
    }

    private void removeInvalidTransmitters() {
        if (!invalidTransmitters.isEmpty()) {
            //Ensure we copy the invalid transmitters, so that when we iterate and remove invalid ones
            // and add still valid ones as orphans, we actually add them as orphans rather than try
            // removing them as invalid and find out they are invalid
            Set<Transmitter<?, ?, ?>> toInvalidate = invalidTransmitters;
            invalidTransmitters = new ObjectOpenHashSet<>();
            if (MekanismAPI.debug) {
                Mekanism.logger.info("Dealing with {} invalid Transmitters", toInvalidate.size());
            }
            for (Transmitter<?, ?, ?> invalid : toInvalidate) {
                removeInvalidTransmitter(invalid);
            }
        }
    }

    private <NETWORK extends DynamicNetwork<?, NETWORK, TRANSMITTER>, TRANSMITTER extends Transmitter<?, NETWORK, TRANSMITTER>>
    void removeInvalidTransmitter(Transmitter<?, NETWORK, TRANSMITTER> invalid) {
        if (!invalid.isOrphan() || !invalid.isValid()) {
            NETWORK n = invalid.getTransmitterNetwork();
            if (n != null) {
                n.invalidate((TRANSMITTER) invalid);
                if (!invalid.isValid()) {
                    //If the transmitter isn't valid, then we need to make sure we clear the network from it
                    // after invalidating the network, so that we can make sure that if this transmitter somehow
                    // gets revived, then it will be able to be properly handled as an orphan.
                    invalid.setTransmitterNetwork(null, false);
                }
            }
        }
    }

    private void assignOrphans() {
        if (!newOrphanTransmitters.isEmpty()) {
            Map<Coord4D, Transmitter<?, ?, ?>> orphanTransmitters = newOrphanTransmitters;
            newOrphanTransmitters = new Object2ObjectOpenHashMap<>();
            if (MekanismAPI.debug) {
                Mekanism.logger.info("Dealing with {} orphan Transmitters", orphanTransmitters.size());
            }

            for (Transmitter<?, ?, ?> orphanTransmitter : orphanTransmitters.values()) {
                if (orphanTransmitter.isValid() && orphanTransmitter.isOrphan()) {
                    OrphanPathFinder<?, ?, ?> finder = new OrphanPathFinder<>(orphanTransmitter);
                    networksToChange.add(finder.getNetworkFromOrphan(orphanTransmitters));
                }
            }
        }
    }

    private void commitChanges() {
        if (!networksToChange.isEmpty()) {
            Set<DynamicNetwork<?, ?, ?>> networks = networksToChange;
            networksToChange = new ObjectOpenHashSet<>();
            for (DynamicNetwork<?, ?, ?> network : networks) {
                network.commit();
            }
        }
    }

    @Override
    public String toString() {
        return "Network Registry:\n" + networks;
    }

    public Component[] toComponents() {
        Component[] components = new Component[networks.size()];
        int i = 0;
        for (DynamicNetwork<?, ?, ?> network : networks) {
            components[i++] = network.getTextComponent();
        }
        return components;
    }

    public static class OrphanPathFinder<ACCEPTOR, NETWORK extends DynamicNetwork<ACCEPTOR, NETWORK, TRANSMITTER>,
          TRANSMITTER extends Transmitter<ACCEPTOR, NETWORK, TRANSMITTER>> {

        private final CompatibleTransmitterValidator<ACCEPTOR, NETWORK, TRANSMITTER> transmitterValidator;
        private final Set<TRANSMITTER> connectedTransmitters = new ObjectOpenHashSet<>();
        private final Long2ObjectMap<ChunkAccess> chunkMap = new Long2ObjectOpenHashMap<>();
        private final Set<NETWORK> networksFound = new ObjectOpenHashSet<>();
        private final Set<BlockPos> iterated = new ObjectOpenHashSet<>();
        private final Deque<BlockPos> queue = new LinkedList<>();
        private final TRANSMITTER startPoint;
        private final Level world;

        OrphanPathFinder(Transmitter<ACCEPTOR, NETWORK, TRANSMITTER> start) {
            startPoint = (TRANSMITTER) start;
            world = startPoint.getTileWorld();
            transmitterValidator = startPoint.getNewOrphanValidator();
        }

        NETWORK getNetworkFromOrphan(Map<Coord4D, Transmitter<?, ?, ?>> orphanTransmitters) {
            //Calculate the network
            if (queue.peek() != null) {
                Mekanism.logger.error("OrphanPathFinder queue was not empty?!");
                queue.clear();
            }
            queue.push(startPoint.getTilePos());
            while (queue.peek() != null) {
                iterate(orphanTransmitters, queue.removeFirst());
            }
            //Create the network or grab the found ones
            NETWORK network;
            if (networksFound.size() == 1) {
                if (MekanismAPI.debug) {
                    Mekanism.logger.info("Adding {} transmitters to single found network", connectedTransmitters.size());
                }
                network = networksFound.iterator().next();
            } else {
                if (MekanismAPI.debug) {
                    if (networksFound.isEmpty()) {
                        Mekanism.logger.info("No networks found. Creating new network for {} transmitters", connectedTransmitters.size());
                    } else {
                        Mekanism.logger.info("Merging {} networks with {} new transmitters", networksFound.size(), connectedTransmitters.size());
                    }
                }
                //TODO: Should we take one of the existing network's uuids if there is one?
                network = startPoint.createNetworkByMerging(networksFound);
            }
            network.addNewTransmitters(connectedTransmitters, transmitterValidator);
            return network;
        }

        private void iterate(Map<Coord4D, Transmitter<?, ?, ?>> orphanTransmitters, BlockPos from) {
            if (iterated.add(from)) {
                Coord4D fromCoord = new Coord4D(from, world);
                Transmitter<?, ?, ?> transmitter = orphanTransmitters.get(fromCoord);
                if (transmitter != null) {
                    if (transmitter.isValid() && transmitter.isOrphan() && startPoint.supportsTransmissionType(transmitter) &&
                        transmitterValidator.isTransmitterCompatible(transmitter)) {
                        connectedTransmitters.add((TRANSMITTER) transmitter);
                        transmitter.setOrphan(false);
                        for (Direction direction : EnumUtils.DIRECTIONS) {
                            BlockPos directionPos = from.relative(direction);
                            if (!iterated.contains(directionPos)) {
                                TileEntityTransmitter tile = WorldUtils.getTileEntity(TileEntityTransmitter.class, world, chunkMap, directionPos);
                                if (tile != null && transmitter.isValidTransmitterBasic(tile, direction)) {
                                    queue.addLast(directionPos);
                                }
                            }
                        }
                    }
                } else {
                    TileEntityTransmitter tile = WorldUtils.getTileEntity(TileEntityTransmitter.class, world, chunkMap, from);
                    if (tile != null && startPoint.supportsTransmissionType(tile)) {
                        NETWORK net = (NETWORK) tile.getTransmitter().getTransmitterNetwork();
                        //Make sure that there is an external network
                        if (net != null && transmitterValidator.isNetworkCompatible(net)) {
                            networksFound.add(net);
                        }
                    }
                }
            }
        }
    }
}