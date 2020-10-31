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
import mekanism.common.lib.inventory.TransitRequest;
import mekanism.common.lib.inventory.TransitRequest.TransitResponse;
import mekanism.common.tile.TileEntityLogisticalSorter;
import mekanism.common.tile.transmitter.TileEntityLogisticalTransporterBase;
import mekanism.common.util.EnumUtils;
import mekanism.common.util.TransporterUtils;
import mekanism.common.util.WorldUtils;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.chunk.IChunk;
import org.apache.commons.lang3.tuple.Pair;

public final class TransporterPathfinder {

    private TransporterPathfinder() {
    }

    private static List<Destination> getPaths(LogisticalTransporterBase start, TransporterStack stack, TransitRequest request, int min) {
        InventoryNetwork network = start.getTransmitterNetwork();
        if (network == null) {
            return Collections.emptyList();
        }
        Long2ObjectMap<IChunk> chunkMap = new Long2ObjectOpenHashMap<>();
        List<AcceptorData> acceptors = network.calculateAcceptors(request, stack, chunkMap);
        List<Destination> paths = new ArrayList<>();
        for (AcceptorData data : acceptors) {
            Destination path = getPath(data, start, stack, min, chunkMap);
            if (path != null) {
                paths.add(path);
            }
        }
        Collections.sort(paths);
        return paths;
    }

    private static boolean checkPath(World world, List<BlockPos> path, TransporterStack stack, Long2ObjectMap<IChunk> chunkMap) {
        for (int i = path.size() - 1; i > 0; i--) {
            TileEntity tile = WorldUtils.getTileEntity(world, chunkMap, path.get(i));
            if (tile instanceof TileEntityLogisticalTransporterBase) {
                EnumColor color = ((TileEntityLogisticalTransporterBase) tile).getTransmitter().getColor();
                if (color != null && color != stack.color) {
                    return false;
                }
            } else {
                return false;
            }
        }
        return true;
    }

    private static Destination getPath(AcceptorData data, LogisticalTransporterBase start, TransporterStack stack, int min, Long2ObjectMap<IChunk> chunkMap) {
        TransitResponse response = data.getResponse();
        if (response.getSendingAmount() >= min) {
            BlockPos dest = data.getLocation();
            CachedPath test = PathfinderCache.getCache(start, dest, data.getSides());
            if (test != null && checkPath(start.getTileWorld(), test.getPath(), stack, chunkMap)) {
                return new Destination(test.getPath(), false, response, test.getCost());
            }
            Pathfinder p = new Pathfinder(new DestChecker() {
                @Override
                public boolean isValid(TransporterStack stack, Direction dir, TileEntity tile) {
                    return TransporterUtils.canInsert(tile, stack.color, response.getStack(), dir, false);
                }
            }, start.getTileWorld(), dest, start.getTilePos(), stack, chunkMap);
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

    public static Destination getNewRRPath(LogisticalTransporterBase start, TransporterStack stack, TransitRequest request, TileEntityLogisticalSorter outputter,
          int min) {
        List<Destination> paths = getPaths(start, stack, request, min);
        Map<BlockPos, Destination> destPaths = new Object2ObjectOpenHashMap<>();
        for (Destination d : paths) {
            BlockPos dest = d.getPath().get(0);
            Destination destination = destPaths.get(dest);
            if (destination == null || destination.getPath().size() < d.getPath().size()) {
                destPaths.put(dest, d);
            }
        }

        List<Destination> destinations = new ArrayList<>(destPaths.values());
        Collections.sort(destinations);
        Destination closest = null;
        if (!destinations.isEmpty()) {
            if (outputter.rrIndex <= destinations.size() - 1) {
                closest = destinations.get(outputter.rrIndex);
                if (outputter.rrIndex == destinations.size() - 1) {
                    outputter.rrIndex = 0;
                } else if (outputter.rrIndex < destinations.size() - 1) {
                    outputter.rrIndex++;
                }
            } else {
                closest = destinations.get(destinations.size() - 1);
                outputter.rrIndex = 0;
            }
        }
        return closest;
    }

    public static Pair<List<BlockPos>, Path> getIdlePath(LogisticalTransporterBase start, TransporterStack stack) {
        Long2ObjectMap<IChunk> chunkMap = new Long2ObjectOpenHashMap<>();
        if (stack.homeLocation != null) {
            Pathfinder p = new Pathfinder(new DestChecker() {
                @Override
                public boolean isValid(TransporterStack stack, Direction side, TileEntity tile) {
                    return TransporterUtils.canInsert(tile, stack.color, stack.itemStack, side, true);
                }
            }, start.getTileWorld(), stack.homeLocation, start.getTilePos(), stack, chunkMap);
            List<BlockPos> path = p.getPath();
            if (path.size() >= 2) {
                return Pair.of(path, Path.HOME);
            }
            stack.homeLocation = null;
        }

        IdlePath d = new IdlePath(start.getTileWorld(), start.getTilePos(), stack);
        Destination dest = d.find(chunkMap);
        if (dest == null) {
            return null;
        }
        return Pair.of(dest.getPath(), dest.getPathType());
    }

    public static class IdlePath {

        private final World world;
        private final BlockPos start;
        private final TransporterStack transportStack;

        public IdlePath(World world, BlockPos obj, TransporterStack stack) {
            this.world = world;
            start = obj;
            transportStack = stack;
        }

        public Destination find(Long2ObjectMap<IChunk> chunkMap) {
            ArrayList<BlockPos> ret = new ArrayList<>();
            ret.add(start);
            TileEntity startTile = WorldUtils.getTileEntity(world, chunkMap, start);
            if (transportStack.idleDir == null) {
                return getDestination(chunkMap, ret, startTile);
            }
            TileEntityLogisticalTransporterBase tile = WorldUtils.getTileEntity(TileEntityLogisticalTransporterBase.class, world, chunkMap,
                  start.offset(transportStack.idleDir));
            if (transportStack.canInsertToTransporter(tile, transportStack.idleDir, startTile)) {
                loopSide(chunkMap, ret, transportStack.idleDir, startTile);
                return new Destination(ret, true, null, 0).setPathType(Path.NONE);
            }
            TransitRequest request = TransitRequest.simple(transportStack.itemStack);
            if (startTile instanceof TileEntityLogisticalTransporterBase) {
                Destination newPath = TransporterPathfinder.getNewBasePath(((TileEntityLogisticalTransporterBase) startTile).getTransmitter(), transportStack, request, 0);
                if (newPath != null && newPath.getResponse() != null) {
                    transportStack.idleDir = null;
                    newPath.setPathType(Path.DEST);
                    return newPath;
                }
            }
            return getDestination(chunkMap, ret, startTile);
        }

        @Nullable
        private Destination getDestination(Long2ObjectMap<IChunk> chunkMap, ArrayList<BlockPos> ret, TileEntity startTile) {
            Direction newSide = findSide(chunkMap);
            if (newSide == null) {
                return null;
            }
            transportStack.idleDir = newSide;
            loopSide(chunkMap, ret, newSide, startTile);
            return new Destination(ret, true, null, 0).setPathType(Path.NONE);
        }

        private void loopSide(Long2ObjectMap<IChunk> chunkMap, List<BlockPos> list, Direction side, TileEntity startTile) {
            TileEntity lastTile = startTile;
            BlockPos pos = start.offset(side);
            TileEntity tile = WorldUtils.getTileEntity(world, chunkMap, pos);
            while (transportStack.canInsertToTransporter(tile, side, lastTile)) {
                lastTile = tile;
                list.add(pos);
                pos = pos.offset(side);
                tile = WorldUtils.getTileEntity(world, chunkMap, pos);
            }
        }

        private Direction findSide(Long2ObjectMap<IChunk> chunkMap) {
            TileEntity startTile = WorldUtils.getTileEntity(world, chunkMap, start);
            if (transportStack.idleDir == null) {
                for (Direction side : EnumUtils.DIRECTIONS) {
                    TileEntityLogisticalTransporterBase tile = WorldUtils.getTileEntity(TileEntityLogisticalTransporterBase.class, world, chunkMap, start.offset(side));
                    if (transportStack.canInsertToTransporter(tile, side, startTile)) {
                        return side;
                    }
                }
            } else {
                Direction opposite = transportStack.idleDir.getOpposite();
                for (Direction side : EnumSet.complementOf(EnumSet.of(opposite))) {
                    TileEntityLogisticalTransporterBase tile = WorldUtils.getTileEntity(TileEntityLogisticalTransporterBase.class, world, chunkMap, start.offset(side));
                    if (transportStack.canInsertToTransporter(tile, side, startTile)) {
                        return side;
                    }
                }
                TileEntityLogisticalTransporterBase tile = WorldUtils.getTileEntity(TileEntityLogisticalTransporterBase.class, world, chunkMap, start.offset(opposite));
                if (transportStack.canInsertToTransporter(tile, opposite, startTile)) {
                    return opposite;
                }
            }
            return null;
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
        public boolean equals(Object dest) {
            return dest instanceof Destination && ((Destination) dest).path.equals(path);
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

        private final Set<BlockPos> openSet, closedSet;
        private final Map<BlockPos, BlockPos> navMap;
        private final Object2DoubleOpenHashMap<BlockPos> gScore, fScore;
        private final BlockPos start;
        private final BlockPos finalNode;
        private final TransporterStack transportStack;
        private final DestChecker destChecker;
        private final World world;
        private double finalScore;
        private Direction side;
        private List<BlockPos> results;

        public Pathfinder(DestChecker checker, World world, BlockPos finishObj, BlockPos startObj, TransporterStack stack, Long2ObjectMap<IChunk> chunkMap) {
            destChecker = checker;
            this.world = world;

            finalNode = finishObj;
            start = startObj;

            transportStack = stack;

            openSet = new ObjectOpenHashSet<>();
            closedSet = new ObjectOpenHashSet<>();

            navMap = new Object2ObjectOpenHashMap<>();

            gScore = new Object2DoubleOpenHashMap<>();
            fScore = new Object2DoubleOpenHashMap<>();

            results = new ArrayList<>();

            find(chunkMap, start);
        }

        public boolean find(Long2ObjectMap<IChunk> chunkMap, BlockPos start) {
            openSet.add(start);
            gScore.put(start, 0D);
            //Note: This is gScore + estimate, but given our gScore starts at zero we just skip getting it back out
            fScore.put(start, WorldUtils.distanceBetween(start, finalNode));
            boolean hasValidDirection = false;
            TileEntity startTile = WorldUtils.getTileEntity(world, chunkMap, start);
            for (Direction direction : EnumUtils.DIRECTIONS) {
                BlockPos neighbor = start.offset(direction);
                TileEntity neighborTile = WorldUtils.getTileEntity(world, chunkMap, neighbor);
                if (transportStack.canInsertToTransporter(neighborTile, direction, startTile)) {
                    //If we can insert into the transporter, mark that we have a valid path we can take
                    hasValidDirection = true;
                    break;
                } else if (isValidDestination(start, startTile, direction, neighbor, neighborTile)) {
                    //Otherwise if we are neighboring our destination, and we can emit to the location or it is going back
                    // to its home location and can connect to it just exit early and return that this is the best path
                    return true;
                }
            }
            if (!hasValidDirection) {
                //If there is no valid direction that the stack can go just exit
                return false;
            }
            double maxSearchDistance = 2 * WorldUtils.distanceBetween(start, finalNode);
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
                //TODO: Look into getting all the "tile" information from the network's transmitter and acceptor caches
                // This should also make it easier to eventually add some sort of "wrapped" support for inventory blocks
                // that do not have TEs. https://github.com/mekanism/Mekanism/issues/6157
                TileEntity currentNodeTile = WorldUtils.getTileEntity(world, chunkMap, currentNode);
                double currentScore = gScore.getDouble(currentNode);
                for (Direction direction : EnumUtils.DIRECTIONS) {
                    BlockPos neighbor = currentNode.offset(direction);
                    TileEntity neighborEntity = WorldUtils.getTileEntity(world, chunkMap, neighbor);
                    if (transportStack.canInsertToTransporter(neighborEntity, direction, currentNodeTile)) {
                        //If the neighbor is a transporter and the stack is valid for it
                        double tentativeG = currentScore + ((TileEntityLogisticalTransporterBase) neighborEntity).getTransmitter().getCost();
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
                    } else if (isValidDestination(currentNode, currentNodeTile, direction, neighbor, neighborEntity)) {
                        //Else if the neighbor is the destination and we can send to it
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
        private boolean isValidDestination(BlockPos start, TileEntity startTile, Direction direction, BlockPos neighbor, TileEntity neighborTile) {
            //Check to make sure that it is the destination
            if (neighbor.equals(finalNode) && destChecker.isValid(transportStack, direction, neighborTile) && startTile instanceof TileEntityLogisticalTransporterBase) {
                TileEntityLogisticalTransporterBase transporter = (TileEntityLogisticalTransporterBase) startTile;
                if (transporter.getTransmitter().canEmitTo(direction) || (finalNode.equals(transportStack.homeLocation) && transporter.getTransmitter().canConnect(direction))) {
                    //If it is and we can emit to it (normal or push mode),
                    // or it is the home location of the stack (it is returning back due to not having been able to get to its destination)
                    // and we can connect to it (normal, push, or pull (should always be pull as otherwise canEmitTo would have been true)),
                    // then this is the proper path so we mark it as so and return true indicating that we found and marked the ideal path
                    side = direction;
                    results = reconstructPath(navMap, start);
                    return true;
                }
            }
            return false;
        }

        private List<BlockPos> reconstructPath(Map<BlockPos, BlockPos> navMap, BlockPos currentNode) {
            List<BlockPos> path = new ArrayList<>();
            path.add(currentNode);
            if (navMap.containsKey(currentNode)) {
                path.addAll(reconstructPath(navMap, navMap.get(currentNode)));
            }
            finalScore = gScore.getDouble(currentNode) + WorldUtils.distanceBetween(currentNode, finalNode);
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

            public boolean isValid(TransporterStack stack, Direction side, TileEntity tile) {
                return false;
            }
        }
    }
}