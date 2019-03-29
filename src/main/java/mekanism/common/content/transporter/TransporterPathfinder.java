package mekanism.common.content.transporter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
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

    public static List<Destination> getPaths(ILogisticalTransporter start, TransporterStack stack,
          TransitRequest request, int min) {
        List<Destination> paths = new ArrayList<>();
        InventoryNetwork network = start.getTransmitterNetwork();

        if (network == null) {
            return paths;
        }

        List<AcceptorData> acceptors = network.calculateAcceptors(request, stack.color);

        for (AcceptorData entry : acceptors) {
            DestChecker checker = new DestChecker() {
                @Override
                public boolean isValid(TransporterStack stack, EnumFacing dir, TileEntity tile) {
                    return InventoryUtils.canInsert(tile, stack.color, entry.response.getStack(), dir, false);
                }
            };

            Destination d = getPath(checker, entry.sides, start, entry.location, stack, entry.response, min);

            if (d != null) {
                paths.add(d);
            }
        }

        Collections.sort(paths);

        return paths;
    }

    public static boolean checkPath(World world, List<Coord4D> path, TransporterStack stack) {
        for (int i = path.size() - 1; i > 0; i--) {
            TileEntity tile = path.get(i).getTileEntity(world);

            if (!CapabilityUtils.hasCapability(tile, Capabilities.LOGISTICAL_TRANSPORTER_CAPABILITY, null)) {
                return false;
            }

            ILogisticalTransporter transporter = CapabilityUtils
                  .getCapability(tile, Capabilities.LOGISTICAL_TRANSPORTER_CAPABILITY, null);

            if (transporter == null || (transporter.getColor() != null && transporter.getColor() != stack.color)) {
                return false;
            }
        }

        return true;
    }

    public static Destination getPath(DestChecker checker, EnumSet<EnumFacing> sides, ILogisticalTransporter start,
          Coord4D dest, TransporterStack stack, TransitResponse response, int min) {
        if (response.getStack().getCount() >= min) {
            List<Coord4D> test = PathfinderCache.getCache(start.coord(), dest, sides);

            if (test != null && checkPath(start.world(), test, stack)) {
                return new Destination(test, false, response, 0).calculateScore(start.world());
            }

            Pathfinder p = new Pathfinder(checker, start.world(), dest, start.coord(), stack);

            if (p.getPath().size() >= 2) {
                PathfinderCache.cachedPaths.put(new PathData(start.coord(), dest, p.side), p.getPath());

                return new Destination(p.getPath(), false, response, p.finalScore);
            }
        }

        return null;
    }

    public static Destination getNewBasePath(ILogisticalTransporter start, TransporterStack stack,
          TransitRequest request, int min) {
        List<Destination> paths = getPaths(start, stack, request, min);

        if (paths.isEmpty()) {
            return null;
        }

        return paths.get(0);
    }

    public static Destination getNewRRPath(ILogisticalTransporter start, TransporterStack stack, TransitRequest request,
          TileEntityLogisticalSorter outputter, int min) {
        List<Destination> paths = getPaths(start, stack, request, min);

        Map<Coord4D, Destination> destPaths = new HashMap<>();

        for (Destination d : paths) {
            if (destPaths.get(d.path.get(0)) == null || destPaths.get(d.path.get(0)).path.size() < d.path.size()) {
                destPaths.put(d.path.get(0), d);
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

        if (closest == null) {
            return null;
        }

        return closest;
    }

    public static Pair<List<Coord4D>, Path> getIdlePath(ILogisticalTransporter start, TransporterStack stack) {
        if (stack.homeLocation != null) {
            DestChecker checker = new DestChecker() {
                @Override
                public boolean isValid(TransporterStack stack, EnumFacing side, TileEntity tile) {
                    return InventoryUtils.canInsert(tile, stack.color, stack.itemStack, side, true);
                }
            };

            Pathfinder p = new Pathfinder(checker, start.world(), stack.homeLocation, start.coord(), stack);
            List<Coord4D> path = p.getPath();

            if (path.size() >= 2) {
                return Pair.of(path, Path.HOME);
            } else {
                stack.homeLocation = null;
            }
        }

        IdlePath d = new IdlePath(start.world(), start.coord(), stack);
        Destination dest = d.find();

        if (dest == null) {
            return null;
        }

        return Pair.of(dest.path, dest.pathType);
    }

    public static class IdlePath {

        public World worldObj;

        public Coord4D start;

        public TransporterStack transportStack;

        public IdlePath(World world, Coord4D obj, TransporterStack stack) {
            worldObj = world;
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
            } else {
                TileEntity tile = start.offset(transportStack.idleDir).getTileEntity(worldObj);

                if (transportStack.canInsertToTransporter(tile, transportStack.idleDir)) {
                    loopSide(ret, transportStack.idleDir);

                    return new Destination(ret, true, null, 0).setPathType(Path.NONE);
                } else {
                    TransitRequest request = TransitRequest.getFromTransport(transportStack);
                    Destination newPath = TransporterPathfinder.getNewBasePath(CapabilityUtils
                          .getCapability(start.getTileEntity(worldObj), Capabilities.LOGISTICAL_TRANSPORTER_CAPABILITY,
                                null), transportStack, request, 0);

                    if (newPath != null && newPath.response != null) {
                        transportStack.idleDir = null;
                        newPath.setPathType(Path.DEST);

                        return newPath;
                    } else {
                        EnumFacing newSide = findSide();

                        if (newSide == null) {
                            return null;
                        }

                        transportStack.idleDir = newSide;
                        loopSide(ret, newSide);

                        return new Destination(ret, true, null, 0).setPathType(Path.NONE);
                    }
                }
            }
        }

        private void loopSide(List<Coord4D> list, EnumFacing side) {
            int count = 1;

            while (true) {
                Coord4D coord = start.offset(side, count);

                if (transportStack.canInsertToTransporter(coord.getTileEntity(worldObj), side)) {
                    list.add(coord);
                    count++;
                } else {
                    break;
                }
            }
        }

        private EnumFacing findSide() {
            if (transportStack.idleDir == null) {
                for (EnumFacing side : EnumFacing.VALUES) {
                    TileEntity tile = start.offset(side).getTileEntity(worldObj);

                    if (transportStack.canInsertToTransporter(tile, side)) {
                        return side;
                    }
                }
            } else {
                for (EnumFacing side : EnumSet.complementOf(EnumSet.of(transportStack.idleDir.getOpposite()))) {
                    TileEntity tile = start.offset(side).getTileEntity(worldObj);

                    if (transportStack.canInsertToTransporter(tile, side)) {
                        return side;
                    }
                }

                TileEntity tile = start.offset(transportStack.idleDir.getOpposite()).getTileEntity(worldObj);

                if (transportStack.canInsertToTransporter(tile, transportStack.idleDir.getOpposite())) {
                    return transportStack.idleDir.getOpposite();
                }
            }

            return null;
        }
    }

    public static class Destination implements Comparable<Destination> {

        public List<Coord4D> path;
        public Path pathType;
        public TransitResponse response;
        public double score;

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
                TileEntity tile = location.getTileEntity(world);

                if (CapabilityUtils.hasCapability(tile, Capabilities.LOGISTICAL_TRANSPORTER_CAPABILITY, null)) {
                    score += CapabilityUtils.getCapability(tile, Capabilities.LOGISTICAL_TRANSPORTER_CAPABILITY, null)
                          .getCost();
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
            } else {
                return path.size() - dest.path.size();
            }
        }
    }

    public static class Pathfinder {

        public final Set<Coord4D> openSet, closedSet;

        public final HashMap<Coord4D, Coord4D> navMap;

        public final HashMap<Coord4D, Double> gScore, fScore;

        public final Coord4D start;

        public final Coord4D finalNode;

        public final TransporterStack transportStack;

        public final DestChecker destChecker;

        public double finalScore;

        public EnumFacing side;

        public ArrayList<Coord4D> results;

        private World worldObj;

        public Pathfinder(DestChecker checker, World world, Coord4D finishObj, Coord4D startObj,
              TransporterStack stack) {
            destChecker = checker;
            worldObj = world;

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

            for (EnumFacing direction : EnumFacing.VALUES) {
                Coord4D neighbor = start.offset(direction);

                if (!transportStack.canInsertToTransporter(neighbor.getTileEntity(worldObj), direction) && (
                      !neighbor.equals(finalNode) || !destChecker
                            .isValid(transportStack, direction, neighbor.getTileEntity(worldObj)))) {
                    blockCount++;
                }
            }

            if (blockCount >= 6) {
                return false;
            }

            double maxSearchDistance = start.distanceTo(finalNode) * 2;
            ArrayList<EnumFacing> directionsToCheck = new ArrayList<>();
            Coord4D[] neighbors = new Coord4D[EnumFacing.VALUES.length];
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

                TileEntity currentNodeTile = currentNode.getTileEntity(worldObj);
                ILogisticalTransporter currentNodeTransporter = null;
                if (currentNodeTile.hasCapability(Capabilities.LOGISTICAL_TRANSPORTER_CAPABILITY, null)) {
                    currentNodeTransporter =
                          CapabilityUtils
                                .getCapability(currentNodeTile, Capabilities.LOGISTICAL_TRANSPORTER_CAPABILITY, null);
                }

                directionsToCheck.clear();
                for (EnumFacing direction : EnumFacing.VALUES) {
                    Coord4D neighbor = currentNode.offset(direction);
                    neighbors[direction.ordinal()] = neighbor;
                    TileEntity neighborEntity = neighbor.getTileEntity(worldObj);
                    neighborEntities[direction.ordinal()] = neighborEntity;
                    if (currentNodeTransporter == null || currentNodeTransporter.canEmitTo(neighborEntity, direction)) {
                        directionsToCheck.add(direction);
                    }
                }

                for (EnumFacing direction : directionsToCheck) {
                    Coord4D neighbor = neighbors[direction.ordinal()];

                    if (transportStack.canInsertToTransporter(neighborEntities[direction.ordinal()], direction)) {
                        TileEntity tile = neighborEntities[direction.ordinal()];
                        double tentativeG = gScore.get(currentNode) + CapabilityUtils
                              .getCapability(tile, Capabilities.LOGISTICAL_TRANSPORTER_CAPABILITY,
                                    direction.getOpposite()).getCost();

                        if (closedSet.contains(neighbor)) {
                            if (tentativeG >= gScore.get(neighbor)) {
                                continue;
                            }
                        }

                        if (!openSet.contains(neighbor) || tentativeG < gScore.get(neighbor)) {
                            navMap.put(neighbor, currentNode);
                            gScore.put(neighbor, tentativeG);
                            fScore.put(neighbor, gScore.get(neighbor) + getEstimate(neighbor, finalNode));
                            openSet.add(neighbor);
                        }
                    } else if (neighbor.equals(finalNode) && destChecker
                          .isValid(transportStack, direction, neighborEntities[direction.ordinal()])) {
                        side = direction;
                        results = reconstructPath(navMap, currentNode);
                        return true;
                    }
                }
            }

            return false;
        }

        private ArrayList<Coord4D> reconstructPath(HashMap<Coord4D, Coord4D> naviMap, Coord4D currentNode) {
            ArrayList<Coord4D> path = new ArrayList<>();

            path.add(currentNode);

            if (naviMap.containsKey(currentNode)) {
                path.addAll(reconstructPath(naviMap, naviMap.get(currentNode)));
            }

            finalScore = gScore.get(currentNode) + currentNode.distanceTo(finalNode);

            return path;
        }

        public ArrayList<Coord4D> getPath() {
            ArrayList<Coord4D> path = new ArrayList<>();
            path.add(finalNode);
            path.addAll(results);

            return path;
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
