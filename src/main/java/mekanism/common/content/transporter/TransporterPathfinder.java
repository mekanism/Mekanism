package mekanism.common.content.transporter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import javax.annotation.Nonnull;
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
import mekanism.common.util.InventoryUtils;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.world.World;
import org.apache.commons.lang3.tuple.Pair;

public final class TransporterPathfinder {

    public static List<Destination> getPaths(ILogisticalTransporter start, TransporterStack stack, TransitRequest request, int min) {
        InventoryNetwork network = start.getTransmitterNetwork();
        if (network == null) {
            return Collections.emptyList();
        }
        List<AcceptorData> acceptors = network.calculateAcceptors(request, stack);
        return acceptors.stream().map(data -> getPath(data, start, stack, min)).filter(Objects::nonNull).sorted().collect(Collectors.toList());
    }

    private static boolean checkPath(World world, List<Coord4D> path, TransporterStack stack) {
        for (int i = path.size() - 1; i > 0; i--) {
            TileEntity tile = path.get(i).getTileEntity(world);
            ILogisticalTransporter transporter = CapabilityUtils.getCapability(tile, Capabilities.LOGISTICAL_TRANSPORTER_CAPABILITY, null);
            if (transporter == null || (transporter.getColor() != null && transporter.getColor() != stack.color)) {
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
                public boolean isValid(TransporterStack stack, EnumFacing dir, TileEntity tile) {
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
                public boolean isValid(TransporterStack stack, EnumFacing side, TileEntity tile) {
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
                EnumFacing newSide = findSide();
                if (newSide == null) {
                    return null;
                }
                transportStack.idleDir = newSide;
                loopSide(ret, newSide);
                return new Destination(ret, true, null, 0).setPathType(Path.NONE);
            }
            TileEntity tile = start.offset(transportStack.idleDir).getTileEntity(world);
            if (transportStack.canInsertToTransporter(tile, transportStack.idleDir)) {
                loopSide(ret, transportStack.idleDir);
                return new Destination(ret, true, null, 0).setPathType(Path.NONE);
            }
            TransitRequest request = TransitRequest.getFromTransport(transportStack);
            Destination newPath = TransporterPathfinder.getNewBasePath(CapabilityUtils.getCapability(start.getTileEntity(world),
                  Capabilities.LOGISTICAL_TRANSPORTER_CAPABILITY, null), transportStack, request, 0);

            if (newPath != null && newPath.getResponse() != null) {
                transportStack.idleDir = null;
                newPath.setPathType(Path.DEST);
                return newPath;
            }
            EnumFacing newSide = findSide();
            if (newSide == null) {
                return null;
            }
            transportStack.idleDir = newSide;
            loopSide(ret, newSide);
            return new Destination(ret, true, null, 0).setPathType(Path.NONE);
        }

        private void loopSide(List<Coord4D> list, EnumFacing side) {
            int count = 1;
            while (true) {
                Coord4D coord = start.offset(side, count);
                if (!transportStack.canInsertToTransporter(coord.getTileEntity(world), side)) {
                    break;
                }
                list.add(coord);
                count++;
            }
        }

        private EnumFacing findSide() {
            if (transportStack.idleDir == null) {
                for (EnumFacing side : EnumFacing.values()) {
                    TileEntity tile = start.offset(side).getTileEntity(world);
                    if (transportStack.canInsertToTransporter(tile, side)) {
                        return side;
                    }
                }
            } else {
                EnumFacing opposite = transportStack.idleDir.getOpposite();
                for (EnumFacing side : EnumSet.complementOf(EnumSet.of(opposite))) {
                    TileEntity tile = start.offset(side).getTileEntity(world);
                    if (transportStack.canInsertToTransporter(tile, side)) {
                        return side;
                    }
                }
                TileEntity tile = start.offset(opposite).getTileEntity(world);
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
                ILogisticalTransporter capability = CapabilityUtils.getCapability(location.getTileEntity(world), Capabilities.LOGISTICAL_TRANSPORTER_CAPABILITY, null);
                if (capability != null) {
                    score += capability.getCost();
                }
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
        private EnumFacing side;
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
            fScore.put(start, gScore.get(start) + getEstimate(start, finalNode));

            int blockCount = 0;

            for (EnumFacing direction : EnumFacing.values()) {
                Coord4D neighbor = start.offset(direction);
                if (!transportStack.canInsertToTransporter(neighbor.getTileEntity(world), direction) &&
                    (!neighbor.equals(finalNode) || !destChecker.isValid(transportStack, direction, neighbor.getTileEntity(world)))) {
                    blockCount++;
                }
            }
            if (blockCount >= 6) {
                return false;
            }

            double maxSearchDistance = start.distanceTo(finalNode) * 2;
            List<EnumFacing> directionsToCheck = new ArrayList<>();
            Coord4D[] neighbors = new Coord4D[EnumFacing.values().length];
            TileEntity[] neighborEntities = new TileEntity[neighbors.length];
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

                TileEntity currentNodeTile = currentNode.getTileEntity(world);
                ILogisticalTransporter currentNodeTransporter = CapabilityUtils.getCapability(currentNodeTile, Capabilities.LOGISTICAL_TRANSPORTER_CAPABILITY, null);
                directionsToCheck.clear();
                for (EnumFacing direction : EnumFacing.values()) {
                    Coord4D neighbor = currentNode.offset(direction);
                    neighbors[direction.ordinal()] = neighbor;
                    TileEntity neighborEntity = neighbor.getTileEntity(world);
                    neighborEntities[direction.ordinal()] = neighborEntity;
                    if (currentNodeTransporter == null || currentNodeTransporter.canEmitTo(neighborEntity, direction) ||
                        (neighbor.equals(finalNode) && destChecker.isValid(transportStack, direction, neighborEntities[direction.ordinal()]))) {
                        directionsToCheck.add(direction);
                    }
                }

                double currentScore = gScore.get(currentNode);
                for (EnumFacing direction : directionsToCheck) {
                    Coord4D neighbor = neighbors[direction.ordinal()];
                    TileEntity neighborEntity = neighborEntities[direction.ordinal()];
                    if (transportStack.canInsertToTransporter(neighborEntity, direction)) {
                        double tentativeG = currentScore + CapabilityUtils.getCapability(neighborEntity, Capabilities.LOGISTICAL_TRANSPORTER_CAPABILITY, direction.getOpposite()).getCost();
                        if (closedSet.contains(neighbor) && tentativeG >= gScore.get(neighbor)) {
                            continue;
                        }

                        if (!openSet.contains(neighbor) || tentativeG < gScore.get(neighbor)) {
                            navMap.put(neighbor, currentNode);
                            gScore.put(neighbor, tentativeG);
                            fScore.put(neighbor, gScore.get(neighbor) + getEstimate(neighbor, finalNode));
                            openSet.add(neighbor);
                        }
                    } else if (neighbor.equals(finalNode) && destChecker.isValid(transportStack, direction, neighborEntity)) {
                        side = direction;
                        results = reconstructPath(navMap, currentNode);
                        return true;
                    }
                }
            }
            return false;
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

        public EnumFacing getSide() {
            return side;
        }

        private double getEstimate(Coord4D start, Coord4D target2) {
            return start.distanceTo(target2);
        }

        public static class DestChecker {

            public boolean isValid(TransporterStack stack, EnumFacing side, TileEntity tile) {
                return false;
            }
        }
    }
}