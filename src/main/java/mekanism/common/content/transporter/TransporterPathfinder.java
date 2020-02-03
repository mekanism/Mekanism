package mekanism.common.content.transporter;

import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import mekanism.api.Coord4D;
import mekanism.common.base.ILogisticalTransporter;
import mekanism.common.capabilities.Capabilities;
import mekanism.common.content.transporter.PathfinderCache.PathData;
import mekanism.common.content.transporter.TransitRequest.TransitResponse;
import mekanism.common.content.transporter.TransporterPathfinder.Pathfinder.DestChecker;
import mekanism.common.content.transporter.TransporterStack.Path;
import mekanism.common.tile.TileEntityLogisticalSorter;
import mekanism.common.transmitters.grid.InventoryNetwork;
import mekanism.common.transmitters.grid.InventoryNetwork.AcceptorData;
import mekanism.common.util.CapabilityUtils;
import mekanism.common.util.EnumUtils;
import mekanism.common.util.InventoryUtils;
import mekanism.common.util.MekanismUtils;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.chunk.ChunkStatus;
import net.minecraft.world.chunk.IChunk;
import org.apache.commons.lang3.tuple.Pair;

public final class TransporterPathfinder {

    public static List<Destination> getPaths(ILogisticalTransporter start, TransporterStack stack, TransitRequest request, int min) {
        InventoryNetwork network = start.getTransmitterNetwork();
        if (network == null) {
            return Collections.emptyList();
        }
        List<AcceptorData> acceptors = network.calculateAcceptors(request, stack);
        List<Destination> paths = new ArrayList<>();
        for (AcceptorData data : acceptors) {
            Destination path = getPath(data, start, stack, min);
            if (path != null) {
                paths.add(path);
            }
        }
        Collections.sort(paths);
        return paths;
    }

    private static boolean checkPath(World world, List<Coord4D> path, TransporterStack stack) {
        for (int i = path.size() - 1; i > 0; i--) {
            TileEntity tile = MekanismUtils.getTileEntity(world, path.get(i).getPos());
            Optional<ILogisticalTransporter> capability = MekanismUtils.toOptional(CapabilityUtils.getCapability(tile, Capabilities.LOGISTICAL_TRANSPORTER_CAPABILITY, null));
            if (capability.isPresent()) {
                ILogisticalTransporter transporter = capability.get();
                if (transporter.getColor() != null && transporter.getColor() != stack.color) {
                    return false;
                }
            } else {
                return false;
            }
        }
        return true;
    }

    private static Destination getPath(AcceptorData data, ILogisticalTransporter start, TransporterStack stack, int min) {
        TransitResponse response = data.getResponse();
        if (response.getSendingAmount() >= min) {
            Coord4D dest = data.getLocation();
            List<Coord4D> test = PathfinderCache.getCache(start.coord(), dest, data.getSides());
            if (test != null && checkPath(start.world(), test, stack)) {
                return new Destination(test, false, response, 0).calculateScore(start.world());
            }
            Pathfinder p = new Pathfinder(new DestChecker() {
                @Override
                public boolean isValid(TransporterStack stack, Direction dir, TileEntity tile) {
                    return InventoryUtils.canInsert(tile, stack.color, response.getStack(), dir, false);
                }
            }, start.world(), dest, start.coord(), stack);
            List<Coord4D> path = p.getPath();
            if (path.size() >= 2) {
                PathfinderCache.addCachedPath(new PathData(start.coord(), dest, p.getSide()), path);
                return new Destination(path, false, response, p.finalScore);
            }
        }
        return null;
    }

    @Nullable
    public static Destination getNewBasePath(ILogisticalTransporter start, TransporterStack stack, TransitRequest request, int min) {
        List<Destination> paths = getPaths(start, stack, request, min);
        if (paths.isEmpty()) {
            return null;
        }
        return paths.get(0);
    }

    public static Destination getNewRRPath(ILogisticalTransporter start, TransporterStack stack, TransitRequest request, TileEntityLogisticalSorter outputter, int min) {
        List<Destination> paths = getPaths(start, stack, request, min);
        Map<Coord4D, Destination> destPaths = new HashMap<>();
        for (Destination d : paths) {
            Coord4D dest = d.getPath().get(0);
            Destination destination = destPaths.get(dest);
            if (destination == null || destination.getPath().size() < d.getPath().size()) {
                destPaths.put(dest, d);
            }
        }

        List<Destination> dests = new ArrayList<>(destPaths.values());
        Collections.sort(dests);
        Destination closest = null;
        if (!dests.isEmpty()) {
            if (outputter.rrIndex <= dests.size() - 1) {
                closest = dests.get(outputter.rrIndex);
                if (outputter.rrIndex == dests.size() - 1) {
                    outputter.rrIndex = 0;
                } else if (outputter.rrIndex < dests.size() - 1) {
                    outputter.rrIndex++;
                }
            } else {
                closest = dests.get(dests.size() - 1);
                outputter.rrIndex = 0;
            }
        }
        return closest;
    }

    public static Pair<List<Coord4D>, Path> getIdlePath(ILogisticalTransporter start, TransporterStack stack) {
        if (stack.homeLocation != null) {
            Pathfinder p = new Pathfinder(new DestChecker() {
                @Override
                public boolean isValid(TransporterStack stack, Direction side, TileEntity tile) {
                    return InventoryUtils.canInsert(tile, stack.color, stack.itemStack, side, true);
                }
            }, start.world(), stack.homeLocation, start.coord(), stack);
            List<Coord4D> path = p.getPath();
            if (path.size() >= 2) {
                return Pair.of(path, Path.HOME);
            }
            stack.homeLocation = null;
        }

        IdlePath d = new IdlePath(start.world(), start.coord(), stack);
        Destination dest = d.find();
        if (dest == null) {
            return null;
        }
        return Pair.of(dest.getPath(), dest.getPathType());
    }

    public static class IdlePath {

        private World world;
        private Coord4D start;
        private TransporterStack transportStack;

        public IdlePath(World world, Coord4D obj, TransporterStack stack) {
            this.world = world;
            start = obj;
            transportStack = stack;
        }

        public Destination find() {
            ArrayList<Coord4D> ret = new ArrayList<>();
            ret.add(start);
            if (transportStack.idleDir == null) {
                Direction newSide = findSide();
                if (newSide == null) {
                    return null;
                }
                transportStack.idleDir = newSide;
                loopSide(ret, newSide);
                return new Destination(ret, true, null, 0).setPathType(Path.NONE);
            }
            TileEntity tile = MekanismUtils.getTileEntity(world, start.offset(transportStack.idleDir).getPos());
            if (transportStack.canInsertToTransporter(tile, transportStack.idleDir)) {
                loopSide(ret, transportStack.idleDir);
                return new Destination(ret, true, null, 0).setPathType(Path.NONE);
            }
            TransitRequest request = TransitRequest.getFromTransport(transportStack);
            Optional<ILogisticalTransporter> capability = MekanismUtils.toOptional(CapabilityUtils.getCapability(MekanismUtils.getTileEntity(world, start.getPos()),
                  Capabilities.LOGISTICAL_TRANSPORTER_CAPABILITY, null));
            if (capability.isPresent()) {
                Destination newPath = TransporterPathfinder.getNewBasePath(capability.get(), transportStack, request, 0);
                if (newPath != null && newPath.getResponse() != null) {
                    transportStack.idleDir = null;
                    newPath.setPathType(Path.DEST);
                    return newPath;
                }
            }
            Direction newSide = findSide();
            if (newSide == null) {
                return null;
            }
            transportStack.idleDir = newSide;
            loopSide(ret, newSide);
            return new Destination(ret, true, null, 0).setPathType(Path.NONE);
        }

        private void loopSide(List<Coord4D> list, Direction side) {
            int count = 1;
            while (true) {
                Coord4D coord = start.offset(side, count);
                if (!transportStack.canInsertToTransporter(MekanismUtils.getTileEntity(world, coord.getPos()), side)) {
                    break;
                }
                list.add(coord);
                count++;
            }
        }

        private Direction findSide() {
            BlockPos startPos = start.getPos();
            if (transportStack.idleDir == null) {
                for (Direction side : EnumUtils.DIRECTIONS) {
                    TileEntity tile = MekanismUtils.getTileEntity(world, startPos.offset(side));
                    if (transportStack.canInsertToTransporter(tile, side)) {
                        return side;
                    }
                }
            } else {
                Direction opposite = transportStack.idleDir.getOpposite();
                for (Direction side : EnumSet.complementOf(EnumSet.of(opposite))) {
                    TileEntity tile = MekanismUtils.getTileEntity(world, startPos.offset(side));
                    if (transportStack.canInsertToTransporter(tile, side)) {
                        return side;
                    }
                }
                TileEntity tile = MekanismUtils.getTileEntity(world, startPos.offset(opposite));
                if (transportStack.canInsertToTransporter(tile, opposite)) {
                    return opposite;
                }
            }
            return null;
        }
    }

    public static class Destination implements Comparable<Destination> {

        private List<Coord4D> path;
        private Path pathType;
        private TransitResponse response;
        private double score;

        public Destination(List<Coord4D> list, boolean inv, TransitResponse ret, double gScore) {
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

        public Destination calculateScore(World world) {
            score = 0;
            for (Coord4D location : path) {
                CapabilityUtils.getCapability(MekanismUtils.getTileEntity(world, location.getPos()), Capabilities.LOGISTICAL_TRANSPORTER_CAPABILITY, null)
                      .ifPresent(transporter -> score += transporter.getCost());
            }
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

        public List<Coord4D> getPath() {
            return path;
        }
    }

    public static class Pathfinder {

        private final Set<Coord4D> openSet, closedSet;
        private final Map<Coord4D, Coord4D> navMap;
        private final Map<Coord4D, Double> gScore, fScore;
        private final Coord4D start;
        private final Coord4D finalNode;
        private final TransporterStack transportStack;
        private final DestChecker destChecker;

        private double finalScore;
        private Direction side;
        private List<Coord4D> results;
        private World world;

        public Pathfinder(DestChecker checker, World world, Coord4D finishObj, Coord4D startObj, TransporterStack stack) {
            destChecker = checker;
            this.world = world;

            finalNode = finishObj;
            start = startObj;

            transportStack = stack;

            openSet = new HashSet<>();
            closedSet = new HashSet<>();

            navMap = new HashMap<>();

            gScore = new HashMap<>();
            fScore = new HashMap<>();

            results = new ArrayList<>();

            find(start);
        }

        public boolean find(Coord4D start) {
            openSet.add(start);
            gScore.put(start, 0D);
            //Note: This is gScore + estimate, but given our gScore starts at zero we just skip getting it back out
            fScore.put(start, getEstimate(start, finalNode));
            //TODO: Optimize canConnect check for transporters to not get the world/chunk as much at least in cases we may already even know the tile
            boolean hasValidDirection = false;
            Map<Long, IChunk> chunkMap = new Long2ObjectOpenHashMap<>();
            for (Direction direction : EnumUtils.DIRECTIONS) {
                Coord4D neighbor = start.offset(direction);
                TileEntity neighborTile = getTileEntity(chunkMap, neighbor);
                if (transportStack.canInsertToTransporter(neighborTile, direction) || (neighbor.equals(finalNode) && destChecker.isValid(transportStack, direction, neighborTile))) {
                    //If we can insert into the transporter or the neighbor is the destination, mark that we have a valid note
                    //TODO: We may want to check if we actually have a connection to the neighbor for the final check?
                    hasValidDirection = true;
                    break;
                }
            }
            if (!hasValidDirection) {
                //If there is no valid direction that the stack can go just exit
                return false;
            }

            //TODO: The max search distance is what needs to be adjusted somehow if we want to fix the "bug" with some poor paths
            // not being valid. The bigger issue is that there may be a path that fits but if there is a branch leading nowhere
            // that is too long it then seems to break for some reason
            double maxSearchDistance = 2 * start.distanceTo(finalNode);
            while (!openSet.isEmpty()) {
                Coord4D currentNode = null;
                double lowestFScore = 0;
                for (Coord4D node : openSet) {
                    if (currentNode == null || fScore.get(node) < lowestFScore) {
                        currentNode = node;
                        lowestFScore = fScore.get(node);
                    }
                }
                if (currentNode == null || start.distanceTo(currentNode) > maxSearchDistance) {
                    break;
                }

                openSet.remove(currentNode);
                closedSet.add(currentNode);
                @Nullable
                TileEntity currentNodeTile = null;
                double currentScore = gScore.get(currentNode);
                for (Direction direction : EnumUtils.DIRECTIONS) {
                    Coord4D neighbor = currentNode.offset(direction);
                    TileEntity neighborEntity = getTileEntity(chunkMap, neighbor);
                    if (transportStack.canInsertToTransporter(neighborEntity, direction)) {
                        //If the neighbor is a transporter and the stack is valid for it
                        double tentativeG = currentScore;
                        Optional<ILogisticalTransporter> capability = MekanismUtils.toOptional(CapabilityUtils.getCapability(neighborEntity,
                              Capabilities.LOGISTICAL_TRANSPORTER_CAPABILITY, direction.getOpposite()));
                        //TODO: FIXME - the score calculations are incorrect for some things so causes it to not actually even start transmitting
                        // at least for a long transmission
                        // Something is going on with the estimates (I believe for how it calculates the path) that if there is a potential path
                        // that goes too far/is too long then it gives up
                        if (capability.isPresent()) {
                            tentativeG += capability.get().getCost();
                        }
                        if (closedSet.contains(neighbor) && tentativeG >= gScore.get(neighbor)) {
                            continue;
                        }
                        if (!openSet.contains(neighbor) || tentativeG < gScore.get(neighbor)) {
                            navMap.put(neighbor, currentNode);
                            gScore.put(neighbor, tentativeG);
                            //Put the gScore plus estimate in the final score
                            fScore.put(neighbor, tentativeG + getEstimate(neighbor, finalNode));
                            openSet.add(neighbor);
                        }
                    } else if (neighbor.equals(finalNode) && destChecker.isValid(transportStack, direction, neighborEntity)) {
                        //Else if the neighbor is the destination
                        if (currentNodeTile == null) {
                            //Lazy get the tile
                            currentNodeTile = getTileEntity(chunkMap, currentNode);
                        }
                        Optional<ILogisticalTransporter> currentNodeTransporter = MekanismUtils.toOptional(CapabilityUtils.getCapability(currentNodeTile,
                              Capabilities.LOGISTICAL_TRANSPORTER_CAPABILITY, null));
                        //TODO: Figure out if currentNodeTransporter can ever not be present (if so we may need to handle it)
                        if (currentNodeTransporter.isPresent()) {
                            ILogisticalTransporter transporter = currentNodeTransporter.get();
                            //TODO: Check if canEmitTo broken if it is a pipe connected to a pipe and they are both on "pull"
                            if (transporter.canEmitTo(neighborEntity, direction) || (finalNode.equals(transportStack.homeLocation) && transporter.canConnect(direction))) {
                                //And we can emit to the location or it is going back to its home location and can connect to it
                                side = direction;
                                results = reconstructPath(navMap, currentNode);
                                return true;
                            }
                        }
                    }
                }
            }
            return false;
        }

        //TODO: Decide if we want to move This to a util method in MekanismUtils so that it can be used if we have a local cache of chunks
        private TileEntity getTileEntity(Map<Long, IChunk> chunkMap, Coord4D coord) {
            BlockPos pos = coord.getPos();
            int chunkX = pos.getX() >> 4;
            int chunkZ = pos.getZ() >> 4;
            long combinedChunk = (((long) chunkX) << 32) | (chunkZ & 0xFFFFFFFFL);
            //We get the chunk rather than the world so we can cache the chunk improving the overall
            // performance for retrieving a bunch of chunks in the general vicinity
            IChunk chunk = chunkMap.get(combinedChunk);
            if (chunk == null) {
                //Get the chunk but don't force load it
                chunk = world.getChunk(chunkX, chunkZ, ChunkStatus.FULL, false);
                if (chunk != null) {
                    chunkMap.put(combinedChunk, chunk);
                }
            }
            //Get the tile entity using the chunk we found/had cached
            return MekanismUtils.getTileEntity(chunk, pos);
        }

        private List<Coord4D> reconstructPath(Map<Coord4D, Coord4D> naviMap, Coord4D currentNode) {
            List<Coord4D> path = new ArrayList<>();
            path.add(currentNode);
            if (naviMap.containsKey(currentNode)) {
                path.addAll(reconstructPath(naviMap, naviMap.get(currentNode)));
            }
            finalScore = gScore.get(currentNode) + currentNode.distanceTo(finalNode);
            return path;
        }

        public List<Coord4D> getPath() {
            List<Coord4D> path = new ArrayList<>();
            path.add(finalNode);
            path.addAll(results);
            return path;
        }

        public Direction getSide() {
            return side;
        }

        private double getEstimate(Coord4D start, Coord4D target2) {
            return start.distanceTo(target2);
        }

        public static class DestChecker {

            public boolean isValid(TransporterStack stack, Direction side, TileEntity tile) {
                return false;
            }
        }
    }
}