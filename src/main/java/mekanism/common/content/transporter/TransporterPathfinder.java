package mekanism.common.content.transporter;

import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2DoubleOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import mekanism.api.text.EnumColor;
import mekanism.common.content.network.InventoryNetwork;
import mekanism.common.content.network.InventoryNetwork.AcceptorData;
import mekanism.common.content.network.transmitter.LogisticalTransporterBase;
import mekanism.common.content.transporter.PathfinderCache.CachedPath;
import mekanism.common.content.transporter.PathfinderCache.PathData;
import mekanism.common.content.transporter.TransporterPathfinder.Pathfinder.DestChecker;
import mekanism.common.content.transporter.TransporterStack.Path;
import mekanism.common.lib.SidedBlockPos;
import mekanism.common.lib.inventory.TransitRequest;
import mekanism.common.lib.inventory.TransitRequest.TransitResponse;
import mekanism.common.lib.transmitter.ConnectionType;
import mekanism.common.tile.TileEntityLogisticalSorter;
import mekanism.common.util.EnumUtils;
import mekanism.common.util.TransporterUtils;
import mekanism.common.util.WorldUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.chunk.ChunkAccess;

public final class TransporterPathfinder {

    private TransporterPathfinder() {
    }

    private static List<Destination> getPaths(LogisticalTransporterBase start, TransporterStack stack, TransitRequest request, int min) {
        InventoryNetwork network = start.getTransmitterNetwork();
        if (network == null) {
            return Collections.emptyList();
        }
        Long2ObjectMap<ChunkAccess> chunkMap = new Long2ObjectOpenHashMap<>();
        List<AcceptorData> acceptors = network.calculateAcceptors(request, stack, chunkMap);
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

    private static boolean checkPath(InventoryNetwork network, List<BlockPos> path, TransporterStack stack) {
        for (int i = path.size() - 1; i > 0; i--) {
            LogisticalTransporterBase transmitter = network.getTransmitter(path.get(i));
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
                return new Destination(test.path(), false, response, test.cost());
            }
            Pathfinder p = new Pathfinder(new DestChecker() {
                @Override
                public boolean isValid(TransporterStack stack, Direction side, BlockEntity tile) {
                    return TransporterUtils.canInsert(tile, stack.color, response.getStack(), side, false);
                }
            }, network, start.getTileWorld(), dest, start.getTilePos(), stack);
            p.find(chunkMap);
            List<BlockPos> path = p.getPath();
            if (path.size() >= 2) {
                PathfinderCache.addCachedPath(start, new PathData(start.getTilePos(), dest, p.getSide()), path, p.finalScore);
                return new Destination(path, false, response, p.finalScore);
            }
        }
        return null;
    }

    @Nullable
    public static Destination getNewBasePath(LogisticalTransporterBase start, TransporterStack stack, TransitRequest request, int min) {
        List<Destination> paths = getPaths(start, stack, request, min);
        if (paths.isEmpty()) {
            return null;
        }
        return paths.get(0);
    }

    @Nullable
    public static Destination getNewRRPath(LogisticalTransporterBase start, TransporterStack stack, TransitRequest request, TileEntityLogisticalSorter outputter,
          int min) {
        List<Destination> destinations = getPaths(start, stack, request, min);
        int destinationCount = destinations.size();
        if (destinationCount == 0) {
            return null;
        }
        if (destinationCount > 1 && outputter.rrTarget != null) {
            //If we have more than one destination and have a "next" round-robin target stored
            // go through the different destinations and find one that matches
            for (int i = 0; i < destinationCount; i++) {
                Destination destination = destinations.get(i);
                List<BlockPos> path = destination.getPath();
                BlockPos pos = path.get(0);
                if (outputter.rrTarget.pos().equals(pos)) {
                    Direction sideOfDest = WorldUtils.sideDifference(path.get(1), pos);
                    if (outputter.rrTarget.side() == sideOfDest) {
                        //When we find one that matches
                        if (i == destinationCount - 1) {
                            // if we are the last element mark that the next target is the first one
                            // Note: We do this rather than just setting it to null so that if more
                            // targets get added we still continue in the place we are expecting
                            outputter.rrTarget = SidedBlockPos.get(destinations.get(0));
                        } else {
                            // Otherwise, if we are not the last element mark the next target as
                            // the next destination
                            outputter.rrTarget = SidedBlockPos.get(destinations.get(i + 1));
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
        Destination destination = destinations.get(0);
        if (destinationCount > 1) {
            outputter.rrTarget = SidedBlockPos.get(destinations.get(1));
        } else {
            outputter.rrTarget = SidedBlockPos.get(destination);
        }
        return destination;
    }

    public record IdlePathData(List<BlockPos> path, Path type) {
    }

    @Nullable
    public static IdlePathData getIdlePath(LogisticalTransporterBase start, TransporterStack stack) {
        InventoryNetwork network = start.getTransmitterNetwork();
        if (network == null) {
            return null;
        }
        if (stack.homeLocation != null) {
            Long2ObjectMap<ChunkAccess> chunkMap = new Long2ObjectOpenHashMap<>();
            Pathfinder p = new Pathfinder(new DestChecker() {
                @Override
                public boolean isValid(TransporterStack stack, Direction side, BlockEntity tile) {
                    return TransporterUtils.canInsert(tile, stack.color, stack.itemStack, side, true);
                }
            }, network, start.getTileWorld(), stack.homeLocation, start.getTilePos(), stack);
            p.find(chunkMap);
            List<BlockPos> path = p.getPath();
            if (path.size() >= 2) {
                return new IdlePathData(path, Path.HOME);
            }
            stack.homeLocation = null;
        }

        IdlePath d = new IdlePath(network, start.getTilePos(), stack);
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
            ArrayList<BlockPos> ret = new ArrayList<>();
            ret.add(start);
            LogisticalTransporterBase startTransmitter = network.getTransmitter(start);
            if (transportStack.idleDir == null) {
                return getDestination(ret, startTransmitter);
            }
            LogisticalTransporterBase transmitter = network.getTransmitter(start.relative(transportStack.idleDir));
            if (transportStack.canInsertToTransporter(transmitter, transportStack.idleDir, startTransmitter)) {
                loopSide(ret, transportStack.idleDir, startTransmitter);
                return new Destination(ret, true, null, 0).setPathType(Path.NONE);
            }
            TransitRequest request = TransitRequest.simple(transportStack.itemStack);
            if (startTransmitter != null) {
                Destination newPath = TransporterPathfinder.getNewBasePath(startTransmitter, transportStack, request, 0);
                if (newPath != null && newPath.getResponse() != null) {
                    transportStack.idleDir = null;
                    newPath.setPathType(Path.DEST);
                    return newPath;
                }
            }
            return getDestination(ret, startTransmitter);
        }

        @Nullable
        private Destination getDestination(List<BlockPos> ret, @Nullable LogisticalTransporterBase startTransmitter) {
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
                            ret.add(start.relative(side));
                            return new Destination(ret, true, null, 0).setPathType(Path.NONE);
                        }
                    }
                }
                return null;
            }
            transportStack.idleDir = newSide;
            loopSide(ret, newSide, startTransmitter);
            return new Destination(ret, true, null, 0).setPathType(Path.NONE);
        }

        private void loopSide(List<BlockPos> list, Direction side, @Nullable LogisticalTransporterBase startTransmitter) {
            LogisticalTransporterBase lastTransmitter = startTransmitter;
            BlockPos pos = start.relative(side);
            LogisticalTransporterBase transmitter = network.getTransmitter(pos);
            while (transportStack.canInsertToTransporter(transmitter, side, lastTransmitter)) {
                lastTransmitter = transmitter;
                list.add(pos);
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
        private final List<BlockPos> path;
        private final double score;
        private Path pathType;

        public Destination(List<BlockPos> list, boolean inv, TransitResponse ret, double gScore) {
            path = new ArrayList<>(list);
            if (inv) {
                Collections.reverse(path);
            }
            response = ret;
            score = gScore;
        }

        public Destination setPathType(Path type) {
            pathType = type;
            return this;
        }

        @Override
        public int hashCode() {
            int code = 1;
            code = 31 * code + path.hashCode();
            return code;
        }

        @Override
        public boolean equals(Object o) {
            return o instanceof Destination other && other.path.equals(path);
        }

        @Override
        public int compareTo(@Nonnull Destination dest) {
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

        public List<BlockPos> getPath() {
            return path;
        }
    }

    public static class Pathfinder {

        private final Set<BlockPos> openSet = new ObjectOpenHashSet<>();
        private final Set<BlockPos> closedSet = new ObjectOpenHashSet<>();
        private final Map<BlockPos, BlockPos> navMap = new Object2ObjectOpenHashMap<>();
        private final Object2DoubleOpenHashMap<BlockPos> gScore = new Object2DoubleOpenHashMap<>();
        private final Object2DoubleOpenHashMap<BlockPos> fScore = new Object2DoubleOpenHashMap<>();
        private final InventoryNetwork network;
        private final BlockPos start;
        private final BlockPos finalNode;
        private final TransporterStack transportStack;
        private final DestChecker destChecker;
        private final Level world;
        private double finalScore;
        private Direction side;
        private List<BlockPos> results = new ArrayList<>();

        public Pathfinder(DestChecker checker, InventoryNetwork network, Level world, BlockPos finalNode, BlockPos start, TransporterStack stack) {
            destChecker = checker;
            this.network = network;
            this.world = world;
            this.finalNode = finalNode;
            this.start = start;
            transportStack = stack;
        }

        public boolean find(Long2ObjectMap<ChunkAccess> chunkMap) {
            openSet.add(start);
            gScore.put(start, 0D);
            //Note: This is gScore + estimate, but given our gScore starts at zero we just skip getting it back out
            double totalDistance = WorldUtils.distanceBetween(start, finalNode);
            fScore.put(start, totalDistance);
            boolean hasValidDirection = false;
            LogisticalTransporterBase startTransmitter = network.getTransmitter(start);
            for (Direction direction : EnumUtils.DIRECTIONS) {
                BlockPos neighbor = start.relative(direction);
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
                BlockPos currentNode = null;
                double lowestFScore = 0;
                for (BlockPos node : openSet) {
                    if (currentNode == null || fScore.getDouble(node) < lowestFScore) {
                        currentNode = node;
                        lowestFScore = fScore.getDouble(node);
                    }
                }
                if (currentNode == null) {
                    //If we have no current node, then exit
                    break;
                }
                //Remove the current node from unchecked and add it to checked
                openSet.remove(currentNode);
                closedSet.add(currentNode);
                if (WorldUtils.distanceBetween(start, currentNode) > maxSearchDistance) {
                    //If it is too far away for us to keep considering then continue on and see if we have another path that may be valid
                    // Even if it currently has a bit higher of a score
                    continue;
                }
                LogisticalTransporterBase currentNodeTransmitter = network.getTransmitter(currentNode);
                double currentScore = gScore.getDouble(currentNode);
                for (Direction direction : EnumUtils.DIRECTIONS) {
                    BlockPos neighbor = currentNode.relative(direction);
                    LogisticalTransporterBase neighborTransmitter = network.getTransmitter(neighbor);
                    if (transportStack.canInsertToTransporter(neighborTransmitter, direction, currentNodeTransmitter)) {
                        //If the neighbor is a transporter and the stack is valid for it
                        double tentativeG = currentScore + neighborTransmitter.getCost();
                        if (closedSet.contains(neighbor) && tentativeG >= gScore.getDouble(neighbor)) {
                            continue;
                        }
                        if (!openSet.contains(neighbor) || tentativeG < gScore.getDouble(neighbor)) {
                            navMap.put(neighbor, currentNode);
                            gScore.put(neighbor, tentativeG);
                            //Put the gScore plus estimate in the final score
                            fScore.put(neighbor, tentativeG + WorldUtils.distanceBetween(neighbor, finalNode));
                            openSet.add(neighbor);
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
                if (neighborTile != null && destChecker.isValid(transportStack, direction, neighborTile)) {
                    if (startTransporter.canEmitTo(direction) || (finalNode.equals(transportStack.homeLocation) && startTransporter.canConnect(direction))) {
                        //If it is, and we can emit to it (normal or push mode),
                        // or it is the home location of the stack (it is returning due to not having been able to get to its destination) and
                        // we can connect to it (normal, push, or pull (should always be pull as otherwise canEmitTo would have been true)),
                        // then this is the proper path, so we mark it as so and return true indicating that we found and marked the ideal path
                        side = direction;
                        results = reconstructPath(navMap, start);
                        finalScore = gScore.getDouble(start) + WorldUtils.distanceBetween(start, finalNode);
                        return true;
                    }
                }
            }
            return false;
        }

        private List<BlockPos> reconstructPath(Map<BlockPos, BlockPos> navMap, BlockPos nextNode) {
            List<BlockPos> path = new ArrayList<>();
            while (nextNode != null) {
                path.add(nextNode);
                nextNode = navMap.get(nextNode);
            }
            return path;
        }

        public List<BlockPos> getPath() {
            List<BlockPos> path = new ArrayList<>();
            path.add(finalNode);
            path.addAll(results);
            return path;
        }

        public Direction getSide() {
            return side;
        }

        public static class DestChecker {

            public boolean isValid(TransporterStack stack, Direction side, BlockEntity tile) {
                return false;
            }
        }
    }
}