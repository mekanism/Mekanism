package mekanism.common.lib.multiblock;

import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import java.util.EnumMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.NavigableMap;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.function.ToIntFunction;
import mekanism.common.lib.math.voxel.BlockPosBuilder;
import mekanism.common.lib.math.voxel.VoxelPlane;
import mekanism.common.lib.multiblock.FormationProtocol.FormationResult;
import mekanism.common.util.WorldUtils;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Vector3i;
import net.minecraft.world.World;
import net.minecraft.world.chunk.IChunk;

public class Structure {

    public static final Structure INVALID = new Structure();

    private final Map<BlockPos, IMultiblockBase> nodes = new Object2ObjectOpenHashMap<>();

    private final Map<Axis, SortedMap<Integer, VoxelPlane>> minorPlaneMap = new EnumMap<>(Axis.class);
    private final Map<Axis, NavigableMap<Integer, VoxelPlane>> planeMap = new EnumMap<>(Axis.class);

    private boolean valid;

    private long updateTimestamp;
    private boolean didUpdate;

    private MultiblockData multiblockData;
    private IMultiblock<?> controller;

    private Structure() {
    }

    public Structure(IMultiblockBase node) {
        init(node);
        valid = true;
    }

    private void init(IMultiblockBase node) {
        BlockPos pos = node.getTilePos();
        nodes.put(pos, node);
        for (Axis axis : Axis.AXES) {
            getMinorAxisMap(axis).put(axis.getCoord(pos), new VoxelPlane(axis, pos, node instanceof IMultiblock));
        }
        if (node instanceof IMultiblock && (controller == null || ((IMultiblock<?>) node).canBeMaster())) {
            controller = (IMultiblock<?>) node;
        }
    }

    public MultiblockData getMultiblockData() {
        return multiblockData;
    }

    public void setMultiblockData(MultiblockData multiblockData) {
        this.multiblockData = multiblockData;
    }

    public IMultiblock<?> getController() {
        return controller;
    }

    public MultiblockManager<?> getManager() {
        return controller != null && valid ? controller.getManager() : null;
    }

    public IMultiblockBase getTile(BlockPos pos) {
        return nodes.get(pos);
    }

    public SortedMap<Integer, VoxelPlane> getMinorAxisMap(Axis axis) {
        return minorPlaneMap.computeIfAbsent(axis, k -> new TreeMap<>(Integer::compare));
    }

    public NavigableMap<Integer, VoxelPlane> getMajorAxisMap(Axis axis) {
        return planeMap.computeIfAbsent(axis, k -> new TreeMap<>(Integer::compare));
    }

    public void markForUpdate(World world, boolean invalidate) {
        updateTimestamp = world.getGameTime();
        didUpdate = false;
        if (invalidate) {
            invalidate(world);
        } else {
            removeMultiblock(world);
        }
    }

    public <TILE extends TileEntity & IMultiblockBase> void tick(TILE tile) {
        if (!didUpdate && updateTimestamp == tile.getWorld().getGameTime() - 1) {
            didUpdate = true;
            runUpdate(tile);
        }
        if (!isValid()) {
            validate(tile, new Long2ObjectOpenHashMap<>());
        }
    }

    public <TILE extends TileEntity & IMultiblockBase> FormationResult runUpdate(TILE tile) {
        if (getController() != null && multiblockData == null) {
            return getController().createFormationProtocol().doUpdate();
        }
        removeMultiblock(tile.getWorld());
        return FormationResult.FAIL;
    }

    public void add(Structure s) {
        if (s != this) {
            if (s.controller != null && s.controller.canBeMaster()) {
                //If the controller of the other structure isn't null and it can be a master block
                // override our structure's controller
                controller = s.controller;
            }
            //Merge nodes, and update their structure to point to our structure
            MultiblockManager<?> manager = getManager();
            s.nodes.forEach((key, value) -> {
                nodes.put(key, value);
                value.setStructure(manager, this);
            });
            //Iterate through the over the other structure's minor plane map and merge
            // the minor planes into our structure.
            for (Entry<Axis, SortedMap<Integer, VoxelPlane>> entry : s.minorPlaneMap.entrySet()) {
                Axis axis = entry.getKey();
                Map<Integer, VoxelPlane> minorMap = getMinorAxisMap(axis);
                Map<Integer, VoxelPlane> majorMap = getMajorAxisMap(axis);
                entry.getValue().forEach((key, value) -> {
                    VoxelPlane majorPlane = majorMap.get(key);
                    if (majorPlane != null) {
                        //If the major map already has an entry for the position, merge them
                        majorPlane.merge(value);
                        return;
                    }
                    VoxelPlane minorPlane = minorMap.get(key);
                    if (minorPlane == null) {
                        //Otherwise, if the minor map also doesn't have an entry, copy it
                        minorMap.put(key, value);
                    } else {
                        //Otherwise, if the minor map does have an entry, merge them
                        minorPlane.merge(value);
                        //If after merging the planes
                        if (minorPlane.hasFrame() && minorPlane.length() >= 2 && minorPlane.height() >= 2) {
                            // the plane has a frame and is at least two by two
                            // move it from the minor plane map to the major plane map
                            majorMap.put(key, minorPlane);
                            minorMap.remove(key);
                        }
                    }
                });
            }
            //Iterate through the over the other structure's major plane map and merge
            // the major planes into our structure.
            for (Entry<Axis, NavigableMap<Integer, VoxelPlane>> entry : s.planeMap.entrySet()) {
                Axis axis = entry.getKey();
                Map<Integer, VoxelPlane> minorMap = getMinorAxisMap(axis);
                Map<Integer, VoxelPlane> majorMap = getMajorAxisMap(axis);
                entry.getValue().forEach((key, value) -> {
                    VoxelPlane majorPlane = majorMap.get(key);
                    if (majorPlane == null) {
                        //If the major map doesn't have an entry, copy it
                        majorMap.put(key, majorPlane = value);
                        VoxelPlane minorPlane = minorMap.get(key);
                        if (minorPlane != null) {
                            //If however we already have a matching minor plane, we need to then
                            // remove our minor plane and merge it into our new major plane
                            majorPlane.merge(minorPlane);
                            minorMap.remove(key);
                        }
                    } else {
                        //If the major map does have an entry, merge them
                        majorPlane.merge(value);
                    }
                });
            }
        }
    }

    public boolean isValid() {
        return valid;
    }

    public void invalidate(World world) {
        removeMultiblock(world);
        valid = false;
    }

    public void removeMultiblock(World world) {
        if (multiblockData != null) {
            multiblockData.remove(world);
            multiblockData = null;
        }
    }

    public boolean contains(BlockPos pos) {
        return nodes.containsKey(pos);
    }

    public int size() {
        return nodes.size();
    }

    private static void validate(IMultiblockBase node, Long2ObjectMap<IChunk> chunkMap) {
        if (node instanceof IMultiblock) {
            IMultiblock<?> multiblock = (IMultiblock<?>) node;
            if (!multiblock.getStructure().isValid()) {
                // only validate if necessary; this will already be valid if we recursively call validate()
                // from a structural multiblock's perspective
                multiblock.resetStructure(multiblock.getManager());
            }
        } else if (node instanceof IStructuralMultiblock) {
            node.resetStructure(null);
        }
        FormationProtocol.explore(node.getTilePos(), pos -> {
            if (pos.equals(node.getTilePos())) {
                return true;
            }
            TileEntity tile = WorldUtils.getTileEntity(node.getTileWorld(), chunkMap, pos);
            if (tile instanceof IMultiblockBase) {
                IMultiblockBase adj = (IMultiblockBase) tile;
                if (isCompatible(node, adj)) {
                    boolean didMerge = false;
                    if (node instanceof IStructuralMultiblock && adj instanceof IStructuralMultiblock) {
                        Set<MultiblockManager<?>> managers = new HashSet<>();
                        managers.addAll(((IStructuralMultiblock) node).getStructureMap().keySet());
                        managers.addAll(((IStructuralMultiblock) adj).getStructureMap().keySet());
                        // if both are structural, they should merge all manager structures
                        //TODO - 10.1 (or earlier): Figure out what this should be as having it just be
                        // equals seems incorrect. My guess is it should be the commended code down below
                        // but maybe it should be |= instead
                        for (MultiblockManager<?> manager : managers) {
                            didMerge = mergeIfNecessary(node, adj, manager);
                        }
                        /*if (!managers.isEmpty()) {
                            boolean merged = true;
                            for (MultiblockManager<?> manager : managers) {
                                merged &= mergeIfNecessary(node, adj, manager);
                            }
                            didMerge = merged;
                        }*/
                    } else if (node instanceof IStructuralMultiblock) {
                        // validate from the perspective of the IMultiblock
                        if (!hasStructure(node, (IMultiblock<?>) adj)) {
                            validate(adj, chunkMap);
                        }
                        return false;
                    } else if (adj instanceof IStructuralMultiblock) {
                        didMerge = mergeIfNecessary(node, adj, getManager(node));
                    } else { // both are regular IMultiblocks
                        // we know the structures are compatible so managers must be the same for both
                        didMerge = mergeIfNecessary(node, adj, getManager(node));
                    }
                    return didMerge;
                }
            }
            return false;
        });
    }

    private static boolean hasStructure(IMultiblockBase structural, IMultiblock<?> multiblock) {
        return structural.getStructure(multiblock.getManager()) == multiblock.getStructure();
    }

    private static boolean mergeIfNecessary(IMultiblockBase node, IMultiblockBase adj, MultiblockManager<?> manager) {
        // reset the structures if they're invalid
        Structure nodeStructure = node.getStructure(manager);
        if (!nodeStructure.isValid()) {
            nodeStructure = node.resetStructure(manager);
        }
        Structure adjStructure = adj.getStructure(manager);
        if (!adjStructure.isValid()) {
            adjStructure = adj.resetStructure(manager);
        }
        // only merge if the structures are different
        if (!node.hasStructure(adjStructure)) {
            Structure changed;
            // merge into the bigger structure for efficiency
            if (nodeStructure.size() >= adjStructure.size() || (nodeStructure.getManager() != null && adjStructure.getManager() == null)) {
                //Note: We do >= so that if both have size one (a frame and a structural block), then we can
                // properly add the structural to the frame, instead of trying to add the frame to the structural
                // we also make sure this doesn't somehow happen in the future anyways, if there is some edge case
                // that comes up where our node's manager isn't null but the adjacent one is because then we still
                // need to be merging this direction anyways
                changed = nodeStructure;
                changed.add(adjStructure);
            } else {
                changed = adjStructure;
                changed.add(nodeStructure);
            }
            // update the changed structure
            changed.markForUpdate(node.getTileWorld(), false);
            return true;
        }
        return false;
    }

    private static boolean isCompatible(IMultiblockBase node, IMultiblockBase other) {
        MultiblockManager<?> manager = getManager(node), otherManager = getManager(other);
        if (manager != null && otherManager != null) {
            return manager == otherManager;
        } else if (manager == null && otherManager == null) {
            return true;
        } else if (manager == null && node instanceof IStructuralMultiblock) {
            return ((IStructuralMultiblock) node).canInterface(otherManager);
        } else if (otherManager == null && other instanceof IStructuralMultiblock) {
            return ((IStructuralMultiblock) other).canInterface(manager);
        }
        return false;
    }

    private static MultiblockManager<?> getManager(IMultiblockBase node) {
        return node instanceof IMultiblock ? ((IMultiblock<?>) node).getManager() : null;
    }

    public enum Axis {
        X(Vector3i::getX),
        Y(Vector3i::getY),
        Z(Vector3i::getZ);

        private final ToIntFunction<BlockPos> posMapper;

        Axis(ToIntFunction<BlockPos> posMapper) {
            this.posMapper = posMapper;
        }

        public int getCoord(BlockPos pos) {
            return posMapper.applyAsInt(pos);
        }

        public void set(BlockPosBuilder pos, int val) {
            pos.set(this, val);
        }

        public Axis horizontal() {
            return this == X ? Z : X;
        }

        public Axis vertical() {
            return this == Y ? Z : Y;
        }

        public static Axis get(Direction side) {
            return AXES[side.getAxis().ordinal()];
        }

        protected static final Axis[] AXES = values();
    }
}
