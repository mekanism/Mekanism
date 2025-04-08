package mekanism.common.content.transporter;

import it.unimi.dsi.fastutil.longs.Long2DoubleOpenHashMap;
import it.unimi.dsi.fastutil.longs.Long2LongMap;
import it.unimi.dsi.fastutil.longs.Long2LongOpenHashMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.longs.LongArrayList;
import it.unimi.dsi.fastutil.longs.LongList;
import it.unimi.dsi.fastutil.longs.LongLists;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import it.unimi.dsi.fastutil.longs.LongSet;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import mekanism.api.text.EnumColor;
import mekanism.common.content.network.InventoryNetwork;
import mekanism.common.content.network.InventoryNetwork.AcceptorData;
import mekanism.common.content.network.transmitter.LogisticalTransporterBase;
import mekanism.common.content.transporter.PathfinderCache.CachedPath;
import mekanism.common.content.transporter.TransporterStack.Path;
import mekanism.common.lib.SidedBlockPos;
import mekanism.common.lib.inventory.IAdvancedTransportEjector;
import mekanism.common.lib.inventory.TransitRequest;
import mekanism.common.lib.inventory.TransitRequest.TransitResponse;
import mekanism.common.lib.transmitter.ConnectionType;
import mekanism.common.util.EnumUtils;
import mekanism.common.util.TransporterUtils;
import mekanism.common.util.WorldUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.GlobalPos;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.chunk.ChunkAccess;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class TransporterPathfinder {

    private TransporterPathfinder() {
    }

    private static List<Destination> getPaths(LogisticalTransporterBase start, TransporterStack stack, TransitRequest request, int min,
          Map<GlobalPos, Set<TransporterStack>> additionalFlowingStacks) {
        InventoryNetwork network = start.getTransmitterNetwork();
        if (network == null) {
            return Collections.emptyList();
        }
        Long2ObjectMap<ChunkAccess> chunkMap = new Long2ObjectOpenHashMap<>();
        List<AcceptorData> acceptors = network.calculateAcceptors(request, stack, chunkMap, additionalFlowingStacks, start);
        List<Destination> paths = new ArrayList<>();
        for (AcceptorData data : acceptors) {
            Destination path = getPath(network, data, start, stack, min, chunkMap);
            if (path != null) {
                paths.add(path);
            }
        }
        Collections.sort(paths);
        return paths;
    }

    public static boolean checkPath(InventoryNetwork network, LongList path, TransporterStack stack) {
        for (int i = path.size() - 1; i > 0; i--) {
            LogisticalTransporterBase transmitter = network.getTransmitter(path.getLong(i));
            if (transmitter == null) {
                return false;
            }
            EnumColor color = transmitter.getColor();
            if (color != null && color != stack.color) {
                return false;
            }
        }
        return true;
    }

    @Nullable
    private static Destination getPath(InventoryNetwork network, AcceptorData data, LogisticalTransporterBase start, TransporterStack stack, int min,
          Long2ObjectMap<ChunkAccess> chunkMap) {
        TransitResponse response = data.getResponse();
        if (response.getSendingAmount() >= min) {
            BlockPos dest = data.getLocation();
            CachedPath test = PathfinderCache.getCache(start, dest, data.getSides());
            if (test != null && checkPath(network, test.path(), stack)) {
                return new Destination(test, response);
            }
            Pathfinder p = new Pathfinder(network, start.getLevel(), dest, start.getBlockPos(), stack, response.getStack(),
                  (level, pos, tile, s, resp, side) -> TransporterUtils.canInsert(level, pos, tile, s.color, resp, side, false));
            p.find(chunkMap);
            if (p.hasPath()) {
                return new Destination(PathfinderCache.addCachedPath(start, dest, p), response);
            }
        }
        return null;
    }

    @Nullable
    public static Destination getNewBasePath(LogisticalTransporterBase start, TransporterStack stack, TransitRequest request, int min) {
        return getNewBasePath(start, stack, request, min, Collections.emptyMap());
    }

    @Nullable
    public static Destination getNewBasePath(LogisticalTransporterBase start, TransporterStack stack, TransitRequest request, int min,
          Map<GlobalPos, Set<TransporterStack>> additionalFlowingStacks) {
        List<Destination> paths = getPaths(start, stack, request, min, additionalFlowingStacks);
        if (paths.isEmpty()) {
            return null;
        }
        return paths.getFirst();
    }

    @Nullable
    public static Destination getNewRRPath(LogisticalTransporterBase start, TransporterStack stack, TransitRequest request, IAdvancedTransportEjector outputter,
          int min) {
        List<Destination> destinations = getPaths(start, stack, request, min, Collections.emptyMap());
        int destinationCount = destinations.size();
        if (destinationCount == 0) {
            return null;
        }
        if (destinationCount > 1) {
            SidedBlockPos rrTarget = outputter.getRoundRobinTarget();
            if (rrTarget != null) {
                //If we have more than one destination and have a "next" round-robin target stored
                // go through the different destinations and find one that matches
                for (int i = 0; i < destinationCount; i++) {
                    Destination destination = destinations.get(i);
                    LongList path = destination.getPath();
                    long pos = path.getLong(0);
                    if (rrTarget.pos() == pos) {
                        Direction sideOfDest = WorldUtils.sideDifference(path.getLong(1), pos);
                        if (rrTarget.side() == sideOfDest) {
                            //When we find one that matches
                            if (i == destinationCount - 1) {
                                // if we are the last element mark that the next target is the first one
                                // Note: We do this rather than just setting it to null so that if more
                                // targets get added we still continue in the place we are expecting
                                outputter.setRoundRobinTarget(destinations.getFirst());
                            } else {
                                // Otherwise, if we are not the last element mark the next target as
                                // the next destination
                                outputter.setRoundRobinTarget(destinations.get(i + 1));
                            }
                            //We return our matching destination instead of the next one and using rrTarget to
                            // keep track of what destination we did last as then if we filled it up we would
                            // not be able to find a match the next iteration and thus be forced to reset
                            return destination;
                        }
                    }
                }
                //If we could not find our target anywhere, just fallback and reset to the start of the list
                // this should only happen if the destination gets broken/removed before we send to it
            }
        }
        Destination destination = destinations.get(0);
        if (destinationCount > 1) {
            outputter.setRoundRobinTarget(destinations.get(1));
        } else {
            outputter.setRoundRobinTarget(destination);
        }
        return destination;
    }

    public record IdlePathData(LongList path, Path type) {
    }

    @Nullable
    public static IdlePathData getIdlePath(LogisticalTransporterBase start, TransporterStack stack) {
        InventoryNetwork network = start.getTransmitterNetwork();
        if (network == null) {
            return null;
        }
        if (stack.homeLocation != Long.MAX_VALUE) {
            Long2ObjectMap<ChunkAccess> chunkMap = new Long2ObjectOpenHashMap<>();
            //We are idling use the base stack
            Pathfinder p = new Pathfinder(network, start.getLevel(), BlockPos.of(stack.homeLocation), start.getBlockPos(), stack, stack.itemStack,
                  (level, pos, tile, s, resp, side) -> TransporterUtils.canInsert(level, pos, tile, s.color, resp, side, true));
            p.find(chunkMap);
            if (p.hasPath()) {
                return new IdlePathData(p.getPath(), Path.HOME);
            }
            stack.homeLocation = Long.MAX_VALUE;
        }

        IdlePath d = new IdlePath(network, start.getBlockPos(), stack);
        Destination dest = d.find();
        if (dest == null) {
            return null;
        }
        return new IdlePathData(dest.getPath(), dest.getPathType());
    }

    public static class IdlePath {

        private final InventoryNetwork network;
        private final BlockPos start;
        private final TransporterStack transportStack;

        public IdlePath(InventoryNetwork network, BlockPos start, TransporterStack stack) {
            this.network = network;
            this.start = start;
            transportStack = stack;
        }

        public Destination find() {
            LongList ret = new LongArrayList();
            ret.add(start.asLong());
            LogisticalTransporterBase startTransmitter = network.getTransmitter(start);
            if (transportStack.idleDir == null) {
                return getDestination(ret, startTransmitter);
            }
            LogisticalTransporterBase transmitter = network.getTransmitter(start.relative(transportStack.idleDir));
            if (transportStack.canInsertToTransporter(transmitter, transportStack.idleDir, startTransmitter)) {
                loopSide(ret, transportStack.idleDir, startTransmitter);
                return createDestination(ret);
            }
            TransitRequest request = TransitRequest.simple(transportStack.itemStack);
            if (startTransmitter != null) {
                Destination newPath = getNewBasePath(startTransmitter, transportStack, request, 0);
                if (newPath != null && newPath.getResponse() != null) {
                    transportStack.idleDir = null;
                    newPath.setPathType(Path.DEST);
                    return newPath;
                }
            }
            return getDestination(ret, startTransmitter);
        }

        @Nullable
        private Destination getDestination(LongList ret, @Nullable LogisticalTransporterBase startTransmitter) {
            Direction newSide = findSide(startTransmitter);
            if (newSide == null) {
                if (startTransmitter != null) {
                    //If we have an idle dir, use that as the "closest" side, otherwise use the side we are closest
                    // to of the current path
                    Direction sideClosest = transportStack.idleDir == null ? transportStack.getSide(startTransmitter) : transportStack.idleDir;
                    //Check all the sides except the one we currently are closest to, as if we only are connected to one side
                    // then we want to fail
                    for (Direction side : EnumSet.complementOf(EnumSet.of(sideClosest))) {
                        if (startTransmitter.getConnectionType(side) != ConnectionType.NONE) {
                            //If we are connected to a side, idle towards it, the path not pointing at a transmitter
                            // is gracefully handled
                            transportStack.idleDir = side;
                            ret.add(WorldUtils.relativePos(start.asLong(), side));
                            return createDestination(ret);
                        }
                    }
                }
                return null;
            }
            transportStack.idleDir = newSide;
            loopSide(ret, newSide, startTransmitter);
            return createDestination(ret);
        }

        private Destination createDestination(LongList ret) {
            LongList path = new LongArrayList(ret);
            Collections.reverse(path);
            return new Destination(LongLists.unmodifiable(path), null, 0);
        }

        private void loopSide(LongList list, Direction side, @Nullable LogisticalTransporterBase startTransmitter) {
            LogisticalTransporterBase lastTransmitter = startTransmitter;
            BlockPos pos = start.relative(side);
            LogisticalTransporterBase transmitter = network.getTransmitter(pos);
            while (transportStack.canInsertToTransporter(transmitter, side, lastTransmitter)) {
                lastTransmitter = transmitter;
                list.add(pos.asLong());
                pos = pos.relative(side);
                transmitter = network.getTransmitter(pos);
            }
        }

        private Direction findSide(@Nullable LogisticalTransporterBase startTransmitter) {
            if (transportStack.idleDir == null) {
                for (Direction side : EnumUtils.DIRECTIONS) {
                    if (canInsertToTransporter(side, startTransmitter)) {
                        return side;
                    }
                }
            } else {
                Direction opposite = transportStack.idleDir.getOpposite();
                for (Direction side : EnumSet.complementOf(EnumSet.of(opposite))) {
                    if (canInsertToTransporter(side, startTransmitter)) {
                        return side;
                    }
                }
                if (canInsertToTransporter(opposite, startTransmitter)) {
                    return opposite;
                }
            }
            return null;
        }

        private boolean canInsertToTransporter(Direction from, @Nullable LogisticalTransporterBase startTransmitter) {
            return transportStack.canInsertToTransporter(network.getTransmitter(start.relative(from)), from, startTransmitter);
        }
    }

    public static class Destination implements Comparable<Destination> {

        private final TransitResponse response;
        private final LongList path;
        private final int cachedHash;
        private final double score;
        private Path pathType = Path.NONE;

        public Destination(CachedPath path, TransitResponse ret) {
            this(path.path(), ret, path.cost());
        }

        /**
         * @apiNote Expects list to be unmodifiable/immutable (at the very least not mutated after being passed).
         */
        public Destination(LongList path, TransitResponse ret, double gScore) {
            this.path = path;
            this.cachedHash = this.path.hashCode();
            this.response = ret;
            this.score = gScore;
        }

        public Destination setPathType(Path type) {
            pathType = type;
            return this;
        }

        @Override
        public int hashCode() {
            return cachedHash;
        }

        @Override
        public boolean equals(Object o) {
            return o instanceof Destination other && other.path.equals(path);
        }

        @Override
        public int compareTo(@NotNull Destination dest) {
            if (score < dest.score) {
                return -1;
            } else if (score > dest.score) {
                return 1;
            }
            return path.size() - dest.path.size();
        }

        public TransitResponse getResponse() {
            return response;
        }

        public Path getPathType() {
            return pathType;
        }

        public LongList getPath() {
            return path;
        }
    }

    public static class Pathfinder {

        private final LongSet openSet = new LongOpenHashSet();
        private final LongSet closedSet = new LongOpenHashSet();
        private final Long2LongMap navMap = new Long2LongOpenHashMap();
        private final Long2DoubleOpenHashMap gScore = new Long2DoubleOpenHashMap();
        private final Long2DoubleOpenHashMap fScore = new Long2DoubleOpenHashMap();
        private final InventoryNetwork network;
        private final BlockPos start;
        private final BlockPos finalNode;
        private final long finalNodeLong;
        private final TransporterStack transportStack;
        private final ItemStack data;
        private final DestChecker destChecker;
        private final Level world;
        private double finalScore;
        private Direction side;
        private LongList results = new LongArrayList();

        public Pathfinder(InventoryNetwork network, Level world, BlockPos finalNode, BlockPos start, TransporterStack stack, ItemStack data, DestChecker checker) {
            destChecker = checker;
            this.network = network;
            this.world = world;
            this.finalNode = finalNode;
            this.finalNodeLong = finalNode.asLong();
            this.start = start;
            transportStack = stack;
            this.data = data;
        }

        public boolean find(Long2ObjectMap<ChunkAccess> chunkMap) {
            openSet.add(start.asLong());
            gScore.put(start.asLong(), 0D);
            //Note: This is gScore + estimate, but given our gScore starts at zero we just skip getting it back out
            double totalDistance = WorldUtils.distanceBetween(start, finalNode);
            fScore.put(start.asLong(), totalDistance);
            boolean hasValidDirection = false;
            LogisticalTransporterBase startTransmitter = network.getTransmitter(start);
            BlockPos.MutableBlockPos neighbor = new BlockPos.MutableBlockPos();
            for (Direction direction : EnumUtils.DIRECTIONS) {
                neighbor.setWithOffset(start, direction);
                LogisticalTransporterBase neighborTransmitter = network.getTransmitter(neighbor);
                if (transportStack.canInsertToTransporter(neighborTransmitter, direction, startTransmitter)) {
                    //If we can insert into the transporter, mark that we have a valid path we can take
                    hasValidDirection = true;
                    break;
                } else if (isValidDestination(start, startTransmitter, direction, neighbor, chunkMap)) {
                    //Otherwise, if we are neighboring our destination, and we can emit to the location, or it is going back
                    // to its home location and can connect to it just exit early and return that this is the best path
                    return true;
                }
            }
            if (!hasValidDirection) {
                //If there is no valid direction that the stack can go just exit
                return false;
            }
            //If the blocks are very close together, allow for path finding up to four blocks away
            double maxSearchDistance = Math.max(2 * totalDistance, 4);
            while (!openSet.isEmpty()) {
                long currentNodeLong = Long.MAX_VALUE;
                double lowestFScore = 0;
                for (long node : openSet) {
                    if (currentNodeLong == Long.MAX_VALUE || fScore.get(node) < lowestFScore) {
                        currentNodeLong = node;
                        lowestFScore = fScore.get(node);
                    }
                }
                if (currentNodeLong == Long.MAX_VALUE) {
                    //If we have no current node, then exit
                    break;
                }
                BlockPos currentNode = BlockPos.of(currentNodeLong);
                //Remove the current node from unchecked and add it to checked
                openSet.remove(currentNodeLong);
                closedSet.add(currentNodeLong);
                if (WorldUtils.distanceBetween(start, currentNode) > maxSearchDistance) {
                    //If it is too far away for us to keep considering then continue on and see if we have another path that may be valid
                    // Even if it currently has a bit higher of a score
                    continue;
                }
                LogisticalTransporterBase currentNodeTransmitter = network.getTransmitter(currentNode);
                double currentScore = gScore.get(currentNodeLong);
                for (Direction direction : EnumUtils.DIRECTIONS) {
                    neighbor.setWithOffset(currentNode, direction);
                    long neighborLong = neighbor.asLong();
                    LogisticalTransporterBase neighborTransmitter = network.getTransmitter(neighbor);
                    if (transportStack.canInsertToTransporter(neighborTransmitter, direction, currentNodeTransmitter)) {
                        //If the neighbor is a transporter and the stack is valid for it
                        double tentativeG = currentScore + neighborTransmitter.getCost();
                        if (closedSet.contains(neighborLong) && tentativeG >= gScore.get(neighborLong)) {
                            continue;
                        }
                        if (!openSet.contains(neighborLong) || tentativeG < gScore.get(neighborLong)) {
                            navMap.put(neighborLong, currentNodeLong);
                            gScore.put(neighborLong, tentativeG);
                            //Put the gScore plus estimate in the final score
                            fScore.put(neighborLong, tentativeG + WorldUtils.distanceBetween(neighbor, finalNode));
                            openSet.add(neighborLong);
                        }
                    } else if (isValidDestination(currentNode, currentNodeTransmitter, direction, neighbor, chunkMap)) {
                        //Else if the neighbor is the destination, and we can send to it
                        return true;
                    }
                }
            }
            return false;
        }

        /**
         * Checks if we have a valid connection to the destination and are able to emit to it. If we are this updates the side and results to the proper values.
         *
         * @return True if we found a valid connection to the destination and can insert into it, false otherwise
         */
        private boolean isValidDestination(BlockPos start, @Nullable LogisticalTransporterBase startTransporter, Direction direction, BlockPos neighbor,
              Long2ObjectMap<ChunkAccess> chunkMap) {
            //Check to make sure that it is the destination
            if (startTransporter != null && neighbor.equals(finalNode)) {
                BlockEntity neighborTile = WorldUtils.getTileEntity(world, chunkMap, neighbor);
                if (destChecker.isValid(world, neighbor, neighborTile, transportStack, data, direction)) {
                    if (startTransporter.canEmitTo(direction) || (finalNodeLong == transportStack.homeLocation && startTransporter.canConnect(direction))) {
                        //If it is, and we can emit to it (normal or push mode),
                        // or it is the home location of the stack (it is returning due to not having been able to get to its destination) and
                        // we can connect to it (normal, push, or pull (should always be pull as otherwise canEmitTo would have been true)),
                        // then this is the proper path, so we mark it as so and return true indicating that we found and marked the ideal path
                        side = direction;
                        results = reconstructPath(navMap, start.asLong());
                        finalScore = gScore.get(start.asLong()) + WorldUtils.distanceBetween(start, finalNode);
                        return true;
                    }
                }
            }
            return false;
        }

        private LongList reconstructPath(Long2LongMap navMap, long nextNode) {
            LongList path = new LongArrayList();
            do {
                path.add(nextNode);
                nextNode = navMap.getOrDefault(nextNode, Long.MAX_VALUE);
            } while (nextNode != Long.MAX_VALUE);
            return path;
        }

        public boolean hasPath() {
            return !results.isEmpty();
        }

        public LongList getPath() {
            LongList path = new LongArrayList(results.size() + 1);
            path.add(finalNode.asLong());
            path.addAll(results);
            return path;
        }

        public double getFinalScore() {
            return finalScore;
        }

        public Direction getSide() {
            return side;
        }

        @FunctionalInterface
        public interface DestChecker {

            boolean isValid(Level level, BlockPos pos, @Nullable BlockEntity tile, TransporterStack stack, ItemStack data, Direction side);
        }
    }

}