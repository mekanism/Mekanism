package mekanism.common.lib.transmitter;

import it.unimi.dsi.fastutil.longs.Long2BooleanMap;
import it.unimi.dsi.fastutil.longs.Long2BooleanMaps;
import it.unimi.dsi.fastutil.longs.Long2BooleanRBTreeMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectRBTreeMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import java.util.Collection;
import java.util.Deque;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import mekanism.api.MekanismAPI;
import mekanism.common.Mekanism;
import mekanism.common.content.network.transmitter.Transmitter;
import mekanism.common.lib.collection.LongMultimap;
import mekanism.common.tile.transmitter.TileEntityTransmitter;
import mekanism.common.util.EnumUtils;
import mekanism.common.util.WorldUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ChunkMap;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.level.ChunkTicketLevelUpdatedEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;
import org.jetbrains.annotations.Nullable;
import org.jspecify.annotations.NonNull;

@EventBusSubscriber(modid = Mekanism.MODID)
public class TransmitterNetworkRegistry {

    private static final Map<ResourceKey<Level>, DimData> dimensionStore = new HashMap<>();
    private static final Set<DynamicNetwork<?, ?, ?>> networks = new ObjectOpenHashSet<>();
    private static final Map<UUID, DynamicNetwork<?, ?, ?>> clientNetworks = new Object2ObjectOpenHashMap<>();
    private static Set<Transmitter<?, ?, ?>> invalidTransmitters = new ObjectOpenHashSet<>();
    private static Set<DynamicNetwork<?, ?, ?>> networksToChange = new ObjectOpenHashSet<>();

    public static void addClientNetwork(UUID networkID, DynamicNetwork<?, ?, ?> network) {
        if (!clientNetworks.containsKey(networkID)) {
            clientNetworks.put(networkID, network);
        }
    }

    @Nullable
    public static DynamicNetwork<?, ?, ?> getClientNetwork(UUID networkID) {
        return clientNetworks.get(networkID);
    }

    public static void removeClientNetwork(DynamicNetwork<?, ?, ?> network) {
        clientNetworks.remove(network.getUUID());
    }

    public static void clearClientNetworks() {
        clientNetworks.clear();
    }

    public static void reset() {
        networks.clear();
        networksToChange.clear();
        invalidTransmitters.clear();
        dimensionStore.clear();
    }

    private static DimData dimData(ResourceKey<Level> dimension) {
        return dimensionStore.computeIfAbsent(dimension, _ -> new DimData());
    }

    @Nullable
    private static DimData dimDataOrNull(ResourceKey<Level> dimension) {
        return dimensionStore.get(dimension);
    }

    public static void trackTransmitter(Transmitter<?, ?, ?> transmitter) {
        dimData(transmitter.getDimension()).trackTransmitter(transmitter);
    }

    public static void untrackTransmitter(Transmitter<?, ?, ?> transmitter) {
        DimData dimData = dimDataOrNull(transmitter.getDimension());
        if (dimData != null) {
            dimData.untrackTransmitter(transmitter);
        }
    }

    public static void invalidateTransmitter(Transmitter<?, ?, ?> transmitter) {
        invalidTransmitters.add(transmitter);
        DimData dimData = dimDataOrNull(transmitter.getDimension());
        if (dimData != null) {
            dimData.invalidateTransmitter(transmitter);
        }
    }

    public static void registerOrphanTransmitter(Transmitter<?, ?, ?> transmitter) {
        if (!invalidTransmitters.remove(transmitter)) {
            //If we weren't an invalid transmitter, then we need to add it as a new orphan, otherwise removing it is good enough
            // as if it was an orphan before it still will be one, and if it wasn't then it still will be part of the network it
            // was in.
            dimData(transmitter.getDimension()).registerOrphanTransmitter(transmitter);
        }
    }

    public static void registerChangedNetwork(DynamicNetwork<?, ?, ?> network) {
        networksToChange.add(network);
    }

    public static void registerNetwork(DynamicNetwork<?, ?, ?> network) {
        networks.add(network);
    }

    public static void removeNetwork(DynamicNetwork<?, ?, ?> network) {
        networks.remove(network);
        networksToChange.remove(network);
    }

    @SubscribeEvent
    public static void onTick(ServerTickEvent.Post event) {
        handleChangedChunks();
        removeInvalidTransmitters();
        assignOrphans();
        commitChanges();
        if (event.getServer().tickRateManager().runsNormally()) {
            for (DynamicNetwork<?, ?, ?> net : networks) {
                net.onUpdate();
            }
        }
    }

    @SubscribeEvent
    public static void onTicketLevelChange(ChunkTicketLevelUpdatedEvent event) {
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
        DimData dimData = dimDataOrNull(event.getLevel().dimension());
        if (dimData != null) {
            dimData.onTicketLevelChange(event.getChunkPos(), loaded);
        }
    }

    private static void handleChangedChunks() {
        dimensionStore.values().forEach(DimData::handleChangedChunks);
    }

    private static void removeInvalidTransmitters() {
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

    private static <NETWORK extends DynamicNetwork<?, NETWORK, TRANSMITTER>, TRANSMITTER extends Transmitter<?, NETWORK, TRANSMITTER>>
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

    private static void assignOrphans() {
        dimensionStore.values().forEach(DimData::assignOrphans);
    }

    private static void commitChanges() {
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

    public static Component[] toComponents() {
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
            world = startPoint.getLevel();
            transmitterValidator = startPoint.getNewOrphanValidator();
        }

        NETWORK getNetworkFromOrphan(Long2ObjectMap<Transmitter<?, ?, ?>> orphanTransmitters) {
            //Calculate the network
            if (queue.peek() != null) {
                Mekanism.logger.error("OrphanPathFinder queue was not empty?!");
                queue.clear();
            }
            queue.push(startPoint.getBlockPos());
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

        private void iterate(Long2ObjectMap<Transmitter<?, ?, ?>> orphanTransmitters, BlockPos from) {
            if (iterated.add(from)) {
                Transmitter<?, ?, ?> transmitter = orphanTransmitters.get(from.asLong());
                if (transmitter != null) {
                    if (transmitter.isValid() && transmitter.isOrphan() && startPoint.supportsTransmissionType(transmitter) &&
                        transmitterValidator.isTransmitterCompatible(transmitter)) {
                        connectedTransmitters.add((TRANSMITTER) transmitter);
                        transmitter.setOrphan(false);
                        BlockPos.MutableBlockPos directionPos = new BlockPos.MutableBlockPos();
                        for (Direction direction : EnumUtils.DIRECTIONS) {
                            directionPos.setWithOffset(from, direction);
                            if (!iterated.contains(directionPos)) {
                                TileEntityTransmitter tile = WorldUtils.getTileEntity(TileEntityTransmitter.class, world, chunkMap, directionPos);
                                if (tile != null && transmitter.isValidTransmitterBasic(tile, direction)) {
                                    queue.addLast(directionPos.immutable());
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

    private TransmitterNetworkRegistry() {
    }

    private static class DimData {

        private final LongMultimap<Transmitter<?, ?, ?>> transmitters = new LongMultimap<>();
        private /*chunkpos*/ Long2BooleanMap changedTicketChunks = newL2BMap();
        private /*blockpos*/ Long2ObjectMap<Transmitter<?, ?, ?>> newOrphanTransmitters = newL2OMap();

        private static @NonNull Long2ObjectRBTreeMap<Transmitter<?, ?, ?>> newL2OMap() {
            return new Long2ObjectRBTreeMap<>();
        }

        private static @NonNull Long2BooleanMap newL2BMap() {
            return new Long2BooleanRBTreeMap();
        }

        void trackTransmitter(Transmitter<?, ?, ?> transmitter) {
            transmitters.put(ChunkPos.pack(transmitter.getBlockPos()), transmitter);
        }

        void untrackTransmitter(Transmitter<?, ?, ?> transmitter) {
            transmitters.remove(ChunkPos.pack(transmitter.getBlockPos()), transmitter);
        }

        void onTicketLevelChange(long chunkPos, boolean loaded) {
            if (transmitters.containsKey(chunkPos)) {
                //Only track it if we have any transmitters in that chunk
                if (changedTicketChunks.getOrDefault(chunkPos, loaded) != loaded) {
                    //If we are watching the chunk and the loaded state isn't what we already had it as,
                    // then remove it as it didn't actually change. In theory in all cases this is equivalent
                    // to just checking if changeTicketChunks contains chunk, but is slightly more accurate
                    // in case for some reason we get two load or unload notifications in a row
                    changedTicketChunks.remove(chunkPos);
                } else {
                    // Otherwise, make sure the map is aware of the change
                    changedTicketChunks.put(chunkPos, loaded);
                }
            }
        }

        void handleChangedChunks() {
            if (!changedTicketChunks.isEmpty()) {
                Long2BooleanMap changed = changedTicketChunks;
                changedTicketChunks = newL2BMap();
                if (MekanismAPI.debug) {
                    Mekanism.logger.info("Dealing with {} changed chunks", changed.size());
                }
                for (var iterator = Long2BooleanMaps.fastIterator(changed); iterator.hasNext(); ) {
                    Long2BooleanMap.Entry entry = iterator.next();
                    long chunk = entry.getLongKey();
                    boolean loaded = entry.getBooleanValue();
                    Collection<Transmitter<?, ?, ?>> chunkTransmitters = transmitters.get(chunk);
                    int transmitterCount;
                    if (chunkTransmitters != null) {//TODO - 26.1: Is this supposed to be able to be null, or is this check masking a bug?
                        transmitterCount = chunkTransmitters.size();
                        for (Transmitter<?, ?, ?> transmitter : chunkTransmitters) {
                            transmitter.getTransmitterTile().chunkAccessibilityChange(loaded);
                        }
                    } else {
                        transmitterCount = 0;
                    }
                    if (MekanismAPI.debug) {
                        Mekanism.logger.info("{} {} transmitters in chunk: {}, {}", loaded ? "Loaded" : "Unloaded", transmitterCount, ChunkPos.getX(chunk), ChunkPos.getZ(chunk));
                    }
                }
            }
        }

        void invalidateTransmitter(Transmitter<?, ?, ?> transmitter) {
            long coord = transmitter.getWorldPositionLong();
            Transmitter<?, ?, ?> removed = newOrphanTransmitters.remove(coord);
            if (removed != null && removed != transmitter) {
                Mekanism.logger.error("Different orphan transmitter was registered at location during removal! {}", blockToStr(coord));
                newOrphanTransmitters.put(coord, transmitter);//put it back? TODO: work out if this is correct, probably continues the error
            }
        }

        void registerOrphanTransmitter(Transmitter<?, ?, ?> transmitter) {
            long pos = transmitter.getWorldPositionLong();
            Transmitter<?, ?, ?> previous = newOrphanTransmitters.put(pos, transmitter);
            if (previous != null && previous != transmitter && previous.isValid()) {
                Mekanism.logger.error("Different orphan transmitter was already registered at location! {}", blockToStr(pos));
            }
        }

        private void assignOrphans() {
            if (!newOrphanTransmitters.isEmpty()) {
                Long2ObjectMap<Transmitter<?, ?, ?>> orphanTransmitters = newOrphanTransmitters;
                newOrphanTransmitters = newL2OMap();
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

        static String blockToStr(long blockPos) {
            return String.format("x=%d, y=%d, z=%d", BlockPos.getX(blockPos), BlockPos.getY(blockPos), BlockPos.getZ(blockPos));
        }
    }
}