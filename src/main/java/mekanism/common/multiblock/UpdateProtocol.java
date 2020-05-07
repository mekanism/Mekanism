package mekanism.common.multiblock;

import java.util.ArrayList;
import java.util.Deque;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import mekanism.api.Coord4D;
import mekanism.common.multiblock.IValveHandler.ValveData;
import mekanism.common.tile.TileEntityMultiblock;
import mekanism.common.util.EnumUtils;
import mekanism.common.util.MekanismUtils;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;

public abstract class UpdateProtocol<T extends SynchronizedData<T>> {

    /**
     * The multiblock nodes that have already been iterated over.
     */
    public Set<Coord4D> iteratedNodes = new ObjectOpenHashSet<>();

    public Set<Coord4D> innerNodes = new ObjectOpenHashSet<>();

    /**
     * The structures found, all connected by some nodes to the pointer.
     */
    public T structureFound = null;

    /**
     * The original block the calculation is getting run from.
     */
    public TileEntityMultiblock<T> pointer;

    public UpdateProtocol(TileEntityMultiblock<T> tile) {
        pointer = tile;
    }

    /**
     * Recursively loops through each node connected to the given TileEntity.
     *
     * @param coord - coord to start with
     * @param queue - the queue to add next nodes to to avoid recursion
     */
    public void loopThrough(Coord4D coord, Deque<Coord4D> queue) {
        int origX = coord.x, origY = coord.y, origZ = coord.z;
        if (isCorner(origX, origY, origZ)) {
            int xmin = 0, xmax = 0, ymin = 0, ymax = 0, zmin = 0, zmax = 0;
            if (isViableNode(origX + 1, origY, origZ)) {
                xmax = findViableNode(coord, 1, 0, 0);
            } else {
                xmin = findViableNode(coord, -1, 0, 0);
            }
            if (isViableNode(origX, origY + 1, origZ)) {
                ymax = findViableNode(coord, 0, 1, 0);
            } else {
                ymin = findViableNode(coord, 0, -1, 0);
            }
            if (isViableNode(origX, origY, origZ + 1)) {
                zmax = findViableNode(coord, 0, 0, 1);
            } else {
                zmin = findViableNode(coord, 0, 0, -1);
            }

            Set<Coord4D> locations = new ObjectOpenHashSet<>();
            Set<ValveData> valves = new ObjectOpenHashSet<>();
            boolean isValid = true;

            int minX = origX + xmin, maxX = origX + xmax;
            int minY = origY + ymin, maxY = origY + ymax;
            int minZ = origZ + zmin, maxZ = origZ + zmax;
            outer:
            for (int x = xmin; x <= xmax; x++) {
                for (int y = ymin; y <= ymax; y++) {
                    for (int z = zmin; z <= zmax; z++) {
                        BlockPos pos = new BlockPos(origX + x, origY + y, origZ + z);
                        if (x == xmin || x == xmax || y == ymin || y == ymax || z == zmin || z == zmax) {
                            CasingType type = getCasingType(pos);
                            if (!isViableNode(pos) || isFramePos(coord.translate(x, y, z), minX, maxX, minY, maxY, minZ, maxZ) && !type.isFrame()) {
                                //If it is not a valid node or if it is supposed to be a frame but is invalid
                                // then we are not valid over all
                                isValid = false;
                                break outer;
                            } else {
                                locations.add(coord.translate(x, y, z));
                                if (type.isValve()) {
                                    ValveData data = new ValveData();
                                    data.location = coord.translate(x, y, z);
                                    data.side = getSide(data.location, minX, maxX, minY, maxY, minZ, maxZ);
                                    valves.add(data);
                                }
                            }
                        } else if (!isValidInnerNode(pos)) {
                            isValid = false;
                            break outer;
                        } else if (!pointer.getWorld().isAirBlock(pos)) {
                            innerNodes.add(new Coord4D(pos, pointer.getWorld().getDimension().getType()));
                        }
                    }
                }
            }

            if (isValid) {
                //Check the boolean values before performing other calculations
                int length = Math.abs(xmax - xmin) + 1;
                int height = Math.abs(ymax - ymin) + 1;
                int width = Math.abs(zmax - zmin) + 1;
                if (length <= 18 && height <= 18 && width <= 18) {
                    T structure = getNewStructure();
                    structure.locations = locations;
                    structure.valves = valves;
                    structure.volLength = length;
                    structure.volHeight = height;
                    structure.volWidth = width;
                    structure.setVolume(structure.volLength * structure.volHeight * structure.volWidth);
                    structure.renderLocation = coord.translate(0, 1, 0);
                    structure.minLocation = coord.translate(xmin, ymin, zmin);
                    structure.maxLocation = coord.translate(xmax, ymax, zmax);

                    if (structure.volLength >= 3 && structure.volHeight >= 3 && structure.volWidth >= 3) {
                        onStructureCreated(structure, origX, origY, origZ, xmin, xmax, ymin, ymax, zmin, zmax);
                        if (structure.locations.contains(Coord4D.get(pointer)) && isCorrectCorner(coord, minX, minY, minZ)) {
                            if (canForm(structure)) {
                                structureFound = structure;
                                return;
                            }
                        }
                    }
                }
            }
        }

        innerNodes.clear();
        iteratedNodes.add(coord);

        if (iteratedNodes.size() > 2048) {
            return;
        }

        for (Direction side : EnumUtils.DIRECTIONS) {
            Coord4D sideCoord = coord.offset(side);
            if (isViableNode(sideCoord.getPos()) && !iteratedNodes.contains(sideCoord)) {
                queue.addLast(sideCoord);
            }
        }
    }

    protected boolean canForm(T structure) {
        return true;
    }

    public Direction getSide(Coord4D obj, int xmin, int xmax, int ymin, int ymax, int zmin, int zmax) {
        if (obj.x == xmin) {
            return Direction.WEST;
        } else if (obj.x == xmax) {
            return Direction.EAST;
        } else if (obj.y == ymin) {
            return Direction.DOWN;
        } else if (obj.y == ymax) {
            return Direction.UP;
        } else if (obj.z == zmin) {
            return Direction.NORTH;
        } else if (obj.z == zmax) {
            return Direction.SOUTH;
        }
        return null;
    }

    protected boolean isValidInnerNode(BlockPos pos) {
        return pointer.getWorld().isAirBlock(pos);
    }

    /**
     * Helper method for reducing duplicate code in loopThrough.
     *
     * @param orig   Starting position
     * @param xShift Direction x is being changed, 1 is increasing, 0 means not changing, -1 means decreasing. Only one of xShift, yShift, and zShift should not be 0
     *               during any call. A value of 1 also implies that it is a viable node so we start checking at 1 instead of 0.
     * @param yShift Direction y is being changed, 1 is increasing, 0 means not changing, -1 means decreasing. Only one of xShift, yShift, and zShift should not be 0
     *               during any call. A value of 1 also implies that it is a viable node so we start checking at 1 instead of 0.
     * @param zShift Direction z is being changed, 1 is increasing, 0 means not changing, -1 means decreasing. Only one of xShift, yShift, and zShift should not be 0
     *               during any call. A value of 1 also implies that it is a viable node so we start checking at 1 instead of 0.
     *
     * @return x, y, or z depending on which one is not zero.
     */
    private int findViableNode(Coord4D orig, int xShift, int yShift, int zShift) {
        int x = xShift == 1 ? 1 : 0;
        int y = yShift == 1 ? 1 : 0;
        int z = zShift == 1 ? 1 : 0;
        while (isViableNode(orig.x + x + xShift, orig.y + y + yShift, orig.z + z + zShift)) {
            x += xShift;
            y += yShift;
            z += zShift;
        }
        return x != 0 ? x : y != 0 ? y : z;
    }

    private boolean isCorner(int x, int y, int z) {
        return (!isViableNode(x + 1, y, z) || !isViableNode(x - 1, y, z)) &&
               (!isViableNode(x, y + 1, z) || !isViableNode(x, y - 1, z)) &&
               (!isViableNode(x, y, z + 1) || !isViableNode(x, y, z - 1));
    }

    /**
     * @param x - x coordinate
     * @param y - y coordinate
     * @param z - z coordinate
     *
     * @return Whether or not the block at the specified location is a viable node for a multiblock structure.
     */
    public boolean isViableNode(int x, int y, int z) {
        TileEntity tile = MekanismUtils.getTileEntity(pointer.getWorld(), new BlockPos(x, y, z));
        if (tile instanceof IStructuralMultiblock && ((IStructuralMultiblock) tile).canInterface(pointer)) {
            return true;
        }
        return MultiblockManager.areEqual(tile, pointer);

    }

    /**
     * @param pos - coordinates
     *
     * @return Whether or not the block at the specified location is a viable node for a multiblock structure.
     */
    public boolean isViableNode(BlockPos pos) {
        return isViableNode(pos.getX(), pos.getY(), pos.getZ());
    }

    /**
     * @param obj  - location to check
     * @param xmin - minimum x value
     * @param ymin - minimum y value
     * @param zmin - minimum z value
     *
     * @return If the block at the specified location is on the minimum of all angles of this multiblock structure, and the one to use for the actual calculation.
     */
    private boolean isCorrectCorner(Coord4D obj, int xmin, int ymin, int zmin) {
        return obj.x == xmin && obj.y == ymin && obj.z == zmin;
    }

    /**
     * @param obj  - location to check
     * @param xmin - minimum x value
     * @param xmax - maximum x value
     * @param ymin - minimum y value
     * @param ymax - maximum y value
     * @param zmin - minimum z value
     * @param zmax - maximum z value
     *
     * @return Whether or not the block at the specified location is considered a frame on the multiblock structure.
     */
    private boolean isFramePos(Coord4D obj, int xmin, int xmax, int ymin, int ymax, int zmin, int zmax) {
        boolean xMatches = obj.x == xmin || obj.x == xmax;
        boolean yMatches = obj.y == ymin || obj.y == ymax;
        boolean zMatches = obj.z == zmin || obj.z == zmax;
        return xMatches && yMatches || xMatches && zMatches || yMatches && zMatches;
    }

    protected abstract CasingType getCasingType(BlockPos pos);

    protected abstract MultiblockCache<T> getNewCache();

    protected abstract T getNewStructure();

    protected abstract MultiblockManager<T> getManager();

    protected abstract void mergeCaches(List<ItemStack> rejectedItems, MultiblockCache<T> cache, MultiblockCache<T> merge);

    protected void onFormed() {
        for (Coord4D coord : structureFound.internalLocations) {
            TileEntityInternalMultiblock tile = MekanismUtils.getTileEntity(TileEntityInternalMultiblock.class, pointer.getWorld(), coord.getPos());
            if (tile != null) {
                tile.setMultiblock(structureFound.inventoryID);
            }
        }
    }

    protected void onStructureCreated(T structure, int origX, int origY, int origZ, int xmin, int xmax, int ymin, int ymax, int zmin, int zmax) {
    }

    protected void onStructureDestroyed(T structure) {
        for (Coord4D coord : structure.internalLocations) {
            killInnerNode(coord);
        }
    }

    private void killInnerNode(Coord4D coord) {
        TileEntityInternalMultiblock tile = MekanismUtils.getTileEntity(TileEntityInternalMultiblock.class, pointer.getWorld(), coord.getPos());
        if (tile != null) {
            tile.setMultiblock(null);
        }
    }

    /**
     * Runs the protocol and updates all nodes that make a part of the multiblock.
     */
    public void doUpdate() {
        Deque<Coord4D> pathingQueue = new LinkedList<>();
        pathingQueue.add(Coord4D.get(pointer));
        while (pathingQueue.peek() != null) {
            Coord4D next = pathingQueue.removeFirst();
            if (!iteratedNodes.contains(next)) {
                loopThrough(next, pathingQueue);
            }
        }

        if (structureFound != null) {
            for (Coord4D coord : iteratedNodes) {
                if (!structureFound.locations.contains(coord)) {
                    for (Coord4D newCoord : iteratedNodes) {
                        TileEntity tile = MekanismUtils.getTileEntity(pointer.getWorld(), newCoord.getPos());
                        if (tile instanceof TileEntityMultiblock) {
                            ((TileEntityMultiblock<?>) tile).removeStructure();
                        } else if (tile instanceof IStructuralMultiblock) {
                            ((IStructuralMultiblock) tile).setController(null);
                        }
                    }
                    for (Coord4D newCoord : innerNodes) {
                        killInnerNode(newCoord);
                    }
                    return;
                }
            }

            List<UUID> idsFound = new ArrayList<>();
            for (Coord4D obj : structureFound.locations) {
                TileEntity tile = MekanismUtils.getTileEntity(pointer.getWorld(), obj.getPos());
                if (tile instanceof TileEntityMultiblock && ((TileEntityMultiblock<?>) tile).cachedID != null) {
                    idsFound.add(((TileEntityMultiblock<?>) tile).cachedID);
                }
            }

            MultiblockCache<T> cache = getNewCache();
            MultiblockManager<T> manager = getManager();
            UUID idToUse = null;
            if (idsFound.isEmpty()) {
                idToUse = manager.getUniqueInventoryID();
            } else {
                List<ItemStack> rejectedItems = new ArrayList<>();
                for (UUID id : idsFound) {
                    if (manager.inventories.get(id) != null) {
                        if (cache == null) {
                            cache = manager.pullInventory(pointer.getWorld(), id);
                        } else {
                            mergeCaches(rejectedItems, cache, manager.pullInventory(pointer.getWorld(), id));
                        }
                        idToUse = id;
                    }
                }
                //TODO someday: drop all items in rejectedItems
                //TODO seriously this needs to happen soon
                //TODO perhaps drop from pointer?
            }

            cache.apply(structureFound);
            structureFound.inventoryID = idToUse;
            structureFound.onCreated();

            onFormed();

            List<IStructuralMultiblock> structures = new ArrayList<>();
            Coord4D toUse = null;

            for (Coord4D obj : structureFound.locations) {
                TileEntity tile = MekanismUtils.getTileEntity(pointer.getWorld(), obj.getPos());
                if (tile instanceof TileEntityMultiblock) {
                    ((TileEntityMultiblock<T>) tile).structure = structureFound;
                    if (toUse == null) {
                        toUse = obj;
                    }
                } else if (tile instanceof IStructuralMultiblock) {
                    structures.add((IStructuralMultiblock) tile);
                }
            }

            //Remove all structural multiblocks from locations, set controllers
            for (IStructuralMultiblock node : structures) {
                node.setController(toUse);
                structureFound.locations.remove(Coord4D.get((TileEntity) node));
            }
        } else {
            for (Coord4D coord : iteratedNodes) {
                TileEntity tile = MekanismUtils.getTileEntity(pointer.getWorld(), coord.getPos());
                if (tile instanceof TileEntityMultiblock) {
                    TileEntityMultiblock<T> tileEntity = (TileEntityMultiblock<T>) tile;
                    if (tileEntity.structure != null && !tileEntity.structure.destroyed) {
                        onStructureDestroyed(tileEntity.structure);
                        tileEntity.structure.destroyed = true;
                    }
                    tileEntity.removeStructure();
                } else if (tile instanceof IStructuralMultiblock) {
                    ((IStructuralMultiblock) tile).setController(null);
                }
            }
            for (Coord4D coord : innerNodes) {
                killInnerNode(coord);
            }
        }
    }

    public static abstract class NodeChecker {

        public abstract boolean isValid(final Coord4D coord);

        public boolean shouldContinue(int iterated) {
            return true;
        }
    }

    public static class NodeCounter {

        public Set<Coord4D> iterated = new ObjectOpenHashSet<>();

        public NodeChecker checker;

        public NodeCounter(NodeChecker c) {
            checker = c;
        }

        public void loop(Coord4D pos) {
            iterated.add(pos);

            if (!checker.shouldContinue(iterated.size())) {
                return;
            }

            for (Direction side : EnumUtils.DIRECTIONS) {
                Coord4D coord = pos.offset(side);

                if (!iterated.contains(coord) && checker.isValid(coord)) {
                    loop(coord);
                }
            }
        }

        public int calculate(Coord4D coord) {
            if (!checker.isValid(coord)) {
                return 0;
            }
            loop(coord);
            return iterated.size();
        }
    }

    public enum CasingType {
        FRAME,
        VALVE,
        OTHER,
        INVALID;

        boolean isFrame() {
            return this == FRAME;
        }

        boolean isValve() {
            return this == VALVE;
        }
    }
}
