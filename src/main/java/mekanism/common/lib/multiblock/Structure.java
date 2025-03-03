package mekanism.common.lib.multiblock;

import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectMaps;
import it.unimi.dsi.fastutil.ints.Int2ObjectRBTreeMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectSortedMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectIterator;
import java.util.EnumMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.ToIntFunction;
import mekanism.common.lib.math.voxel.BlockPosBuilder;
import mekanism.common.lib.math.voxel.VoxelPlane;
import mekanism.common.lib.multiblock.FormationProtocol.FormationResult;
import mekanism.common.util.WorldUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Vec3i;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.chunk.ChunkAccess;

public class Structure {

    public static final Structure INVALID = new Structure();

    private final Map<BlockPos, IMultiblockBase> nodes = new Object2ObjectOpenHashMap<>();

    private final Map<Axis, Int2ObjectSortedMap<VoxelPlane>> minorPlaneMap = new EnumMap<>(Axis.class);
    private final Map<Axis, Int2ObjectSortedMap<VoxelPlane>> planeMap = new EnumMap<>(Axis.class);

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
        BlockPos pos = node.getBlockPos();
        nodes.put(pos, node);
        for (Axis axis : Axis.AXES) {
            getMinorAxisMap(axis).put(axis.getCoord(pos), new VoxelPlane(axis, pos, node instanceof IMultiblock));
        }
        if (node instanceof IMultiblock<?> multiblock && (getController() == null || multiblock.canBeMaster())) {
            controller = multiblock;
        }
    }

    public MultiblockData getMultiblockData() {
        return multiblockData;
    }

    public void setMultiblockData(MultiblockData multiblockData) {
        boolean changed = this.multiblockData != multiblockData;
        this.multiblockData = multiblockData;
        if (changed) {
            //If the multiblock changed, then reset the formed status so that we don't potentially end up with multiple masters
            for (IMultiblockBase node : this.nodes.values()) {
                node.resetForFormed();
            }
        }
    }

    public IMultiblock<?> getController() {
        return controller;
    }

    public MultiblockManager<?> getManager() {
        return getController() != null && valid ? getController().getManager() : null;
    }

    public IMultiblockBase getTile(BlockPos pos) {
        return nodes.get(pos);
    }

    public Int2ObjectSortedMap<VoxelPlane> getMinorAxisMap(Axis axis) {
        return minorPlaneMap.computeIfAbsent(axis, k -> new Int2ObjectRBTreeMap<>());
    }

    public Int2ObjectSortedMap<VoxelPlane> getMajorAxisMap(Axis axis) {
        return planeMap.computeIfAbsent(axis, k -> new Int2ObjectRBTreeMap<>());
    }

    public void markForUpdate(Level world, boolean invalidate) {
        updateTimestamp = world.getGameTime();
        didUpdate = false;
        if (invalidate) {
            invalidate(world);
        } else {
            removeMultiblock(world);
        }
    }

    public <TILE extends BlockEntity & IMultiblockBase> void doImmediateUpdate(TILE tile, boolean tryValidate) {
        //Pretend it got marked for update last tick so that when we call tick it will update
        updateTimestamp = tile.getLevel().getGameTime() - 1;
        didUpdate = false;
        invalidate(tile.getLevel());
        tick(tile, tryValidate);
    }

    public <TILE extends BlockEntity & IMultiblockBase> void tick(TILE tile, boolean tryValidate) {
        if (!didUpdate && updateTimestamp == tile.getLevel().getGameTime() - 1) {
            didUpdate = true;
            runUpdate(tile);
        }
        if (tryValidate && !isValid()) {
            validate(tile, new Long2ObjectOpenHashMap<>());
        }
    }

    public <TILE extends BlockEntity & IMultiblockBase> FormationResult runUpdate(TILE tile) {
        if (getController() != null && multiblockData == null) {
            return getController().createFormationProtocol().doUpdate();
        }
        removeMultiblock(tile.getLevel());
        return FormationResult.FAIL;
    }

    public void add(Structure s) {
        if (s != this) {
            if (s.getController() != null && s.getController().canBeMaster() && (getController() == null || !getController().canBeMaster())) {
                //If the controller of the other structure isn't null, and it can be a master block override our structure's controller
                // if our structure's controller is only the controller because of lack of a better and more proper one
                controller = s.getController();
            }
            //Merge nodes, and update their structure to point to our structure
            MultiblockManager<?> manager = getManager();
            for (Map.Entry<BlockPos, IMultiblockBase> e : s.nodes.entrySet()) {
                IMultiblockBase v = e.getValue();
                nodes.put(e.getKey(), v);
                v.setStructure(manager, this);
            }
            //Iterate through the over the other structure's minor plane map and merge
            // the minor planes into our structure.
            for (Map.Entry<Axis, Int2ObjectSortedMap<VoxelPlane>> entry : s.minorPlaneMap.entrySet()) {
                Axis axis = entry.getKey();
                Int2ObjectSortedMap<VoxelPlane> minorMap = getMinorAxisMap(axis);
                Int2ObjectSortedMap<VoxelPlane> majorMap = getMajorAxisMap(axis);
                for (ObjectIterator<Int2ObjectMap.Entry<VoxelPlane>> iterator = Int2ObjectMaps.fastIterator(entry.getValue()); iterator.hasNext(); ) {
                    Int2ObjectMap.Entry<VoxelPlane> e = iterator.next();
                    int key = e.getIntKey();
                    VoxelPlane value = e.getValue();
                    VoxelPlane majorPlane = majorMap.get(key);
                    if (majorPlane != null) {
                        //If the major map already has an entry for the position, merge them
                        majorPlane.merge(value);
                        continue;
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
                }
            }
            //Iterate through the over the other structure's major plane map and merge
            // the major planes into our structure.
            for (Map.Entry<Axis, Int2ObjectSortedMap<VoxelPlane>> entry : s.planeMap.entrySet()) {
                Axis axis = entry.getKey();
                Int2ObjectSortedMap<VoxelPlane> minorMap = getMinorAxisMap(axis);
                Int2ObjectSortedMap<VoxelPlane> majorMap = getMajorAxisMap(axis);
                for (ObjectIterator<Int2ObjectMap.Entry<VoxelPlane>> iterator = Int2ObjectMaps.fastIterator(entry.getValue()); iterator.hasNext(); ) {
                    Int2ObjectMap.Entry<VoxelPlane> e = iterator.next();
                    int key = e.getIntKey();
                    VoxelPlane value = e.getValue();
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
                }
            }
        }
    }

    public boolean isValid() {
        return valid;
    }

    public void invalidate(Level world) {
        removeMultiblock(world);
        valid = false;
    }

    public void removeMultiblock(Level world) {
        if (multiblockData != null) {
            multiblockData.remove(world, this);
            multiblockData = null;
        }
    }

    public boolean contains(BlockPos pos) {
        return nodes.containsKey(pos);
    }

    public int size() {
        return nodes.size();
    }

    private static void validate(IMultiblockBase node, Long2ObjectMap<ChunkAccess> chunkMap) {
        if (node instanceof IMultiblock<?> multiblock) {
            if (!multiblock.getStructure().isValid()) {
                // only validate if necessary; this will already be valid if we recursively call validate()
                // from a structural multiblock's perspective
                multiblock.resetStructure(multiblock.getManager());
            }
        } else if (node instanceof IStructuralMultiblock) {
            node.resetStructure(null);
        }
        FormationProtocol.explore(node.getLevel(), chunkMap, node.getBlockPos(), node, (level, chunks, start, n, pos) -> {
            if (pos.equals(start)) {
                return true;
            }
            BlockEntity tile = WorldUtils.getTileEntity(level, chunks, pos);
            if (tile instanceof IMultiblockBase adj && isCompatible(n, adj)) {
                boolean didMerge = false;
                if (n instanceof IStructuralMultiblock structuralN && adj instanceof IStructuralMultiblock structuralAdj) {
                    Set<MultiblockManager<?>> managers = new HashSet<>(structuralN.getStructureMap().keySet());
                    managers.addAll(structuralAdj.getStructureMap().keySet());
                    // if both are structural, they try to merge all manager structures
                    for (MultiblockManager<?> manager : managers) {
                        didMerge |= mergeIfNecessary(n, adj, manager, true);
                    }
                } else if (n instanceof IStructuralMultiblock) {
                    // validate from the perspective of the IMultiblock
                    if (!hasStructure(n, (IMultiblock<?>) adj)) {
                        validate(adj, chunks);
                    }
                    return false;
                } else if (adj instanceof IStructuralMultiblock) {
                    didMerge = mergeIfNecessary(n, adj, getManager(n), false);
                } else { // both are regular IMultiblocks
                    // we know the structures are compatible so managers must be the same for both
                    didMerge = mergeIfNecessary(n, adj, getManager(n), false);
                }
                return didMerge;
            }
            return false;
        });
    }

    private static boolean hasStructure(IMultiblockBase structural, IMultiblock<?> multiblock) {
        return structural.getStructure(multiblock.getManager()) == multiblock.getStructure();
    }

    private static boolean mergeIfNecessary(IMultiblockBase node, IMultiblockBase adj, MultiblockManager<?> manager, boolean bothStructural) {
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
            if (bothStructural) {
                //If at least one is formed, but they aren't both formed then don't merge them
                // as it means we are placing a structural multiblock against another structural one that is already formed,
                // so it has to be outside the bounds
                if ((nodeStructure.getMultiblockData() == null) == (adjStructure.getMultiblockData() != null)) {
                    return false;
                }
            }
            Structure changed;
            // merge into the bigger structure for efficiency
            if (nodeStructure.size() >= adjStructure.size() || (nodeStructure.getManager() != null && adjStructure.getManager() == null)) {
                //Note: We do >= so that if both have size of one (a frame and a structural block), then we can
                // properly add the structural to the frame, instead of trying to add the frame to the structural
                // we also make sure this doesn't somehow happen in the future anyway, if there is some edge case
                // that comes up where our node's manager isn't null but the adjacent one is because then we still
                // need to be merging this direction anyway
                changed = nodeStructure;
                changed.add(adjStructure);
            } else {
                changed = adjStructure;
                changed.add(nodeStructure);
            }
            // update the changed structure
            changed.markForUpdate(node.getLevel(), false);
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
        } else if (manager == null && node instanceof IStructuralMultiblock multiblock) {
            return multiblock.canInterface(otherManager);
        } else if (otherManager == null && other instanceof IStructuralMultiblock multiblock) {
            return multiblock.canInterface(manager);
        }
        return false;
    }

    private static MultiblockManager<?> getManager(IMultiblockBase node) {
        return node instanceof IMultiblock<?> multiblock ? multiblock.getManager() : null;
    }

    public enum Axis {
        X(Vec3i::getX),
        Y(Vec3i::getY),
        Z(Vec3i::getZ);

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

        static final Axis[] AXES = values();
    }
}
