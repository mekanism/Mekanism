package mekanism.common.lib.multiblock;

import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.function.ToIntFunction;
import com.google.common.collect.Sets;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import mekanism.common.lib.math.voxel.BlockPosBuilder;
import mekanism.common.lib.math.voxel.VoxelPlane;
import mekanism.common.lib.multiblock.FormationProtocol.FormationResult;
import mekanism.common.util.MekanismUtils;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3i;
import net.minecraft.world.World;

public class Structure {

    public static final Structure INVALID = new Structure();

    private final Map<BlockPos, IMultiblockBase> nodes = new Object2ObjectOpenHashMap<>();
    private final Map<Axis, TreeMap<Integer, VoxelPlane>> planeMap = new Object2ObjectOpenHashMap<>();

    private int minX, minY, minZ;
    private int maxX, maxY, maxZ;

    private boolean valid;

    private long updateTimestamp;
    private boolean didUpdate;

    private MultiblockData multiblockData;
    private IMultiblock<?> controller;

    private Structure() {}

    public Structure(IMultiblockBase node) {
        init(node);
        valid = true;
    }

    private void init(IMultiblockBase node) {
        nodes.put(node.getTilePos(), node);
        for (Axis axis : Axis.AXES) {
            getAxisMap(axis).put(axis.getCoord(node.getTilePos()), new VoxelPlane(axis, node.getTilePos()));
        }
        if (node instanceof IMultiblock) {
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

    public TreeMap<Integer, VoxelPlane> getAxisMap(Axis axis) {
        return planeMap.computeIfAbsent(axis, k -> new TreeMap<>(Integer::compare));
    }

    public <TILE extends TileEntity & IMultiblockBase> void tick(TILE tile) {
        if (!didUpdate && updateTimestamp == tile.getWorld().getGameTime() - 1) {
            didUpdate = true;
            runUpdate(tile);
        }
        if (!isValid()) {
            validate(tile);
        }
    }

    public <TILE extends TileEntity & IMultiblockBase> FormationResult runUpdate(TILE tile) {
        if (getController() != null && multiblockData == null) {
            if (multiblockData == null) {
                return getController().createFormationProtocol().doUpdate();
            }
        }
        removeMultiblock(tile.getWorld());
        return FormationResult.FAIL;
    }

    public void add(Structure s) {
        if (s != this) {
            if (s.controller != null) {
                controller = s.controller;
            }
            s.nodes.forEach((key, value) -> {
                nodes.put(key, value);
                System.out.println("Setting " + getManager());
                value.setStructure(getManager(), this);
            });
            for (Axis axis : s.planeMap.keySet()) {
                Map<Integer, VoxelPlane> map = getAxisMap(axis);
                s.planeMap.get(axis).forEach((key, value) -> {
                    VoxelPlane p = map.get(key);
                    if (p != null) {
                        p.merge(value);
                    } else {
                        map.put(key, p = value);
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

    private static void validate(IMultiblockBase node) {
        FormationProtocol.explore(node.getTilePos(), pos -> {
            if (pos.equals(node.getTilePos()))
                return true;
            TileEntity tile = MekanismUtils.getTileEntity(node.getTileWorld(), pos);
            if (tile instanceof IMultiblockBase) {
                IMultiblockBase adj = (IMultiblockBase) tile;
                if (isCompatible(node, adj)) {
                    boolean didMerge = false;
                    if (node instanceof IStructuralMultiblock && adj instanceof IStructuralMultiblock) {
                        Set<MultiblockManager<?>> managers = Sets.newHashSet();
                        managers.addAll(((IStructuralMultiblock) node).getStructureMap().keySet());
                        managers.addAll(((IStructuralMultiblock) adj).getStructureMap().keySet());
                        // if both are structural, they should merge all manager structures
                        for (MultiblockManager<?> manager : managers) {
                            didMerge = mergeIfNecessary(node, adj, manager);
                        }
                    } else if (node instanceof IStructuralMultiblock) {
                        // validate from the perspective of the IMultiblock
                        validate(adj);
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

    private static boolean mergeIfNecessary(IMultiblockBase node, IMultiblockBase adj, MultiblockManager<?> manager) {
        // reset the structures if they're invalid
        if (!node.getStructure(manager).isValid())
            node.resetStructure(manager);
        if (!adj.getStructure(manager).isValid())
            adj.resetStructure(manager);
        // only merge if the structures are different
        if (!node.hasStructure(adj.getStructure(manager))) {
            mergeStructures(node, adj, manager);
            return true;
        }
        return false;
    }

    private static void mergeStructures(IMultiblockBase node, IMultiblockBase adj, MultiblockManager<?> manager) {
        Structure nodeStructure = node.getStructure(manager);
        Structure adjStructure = adj.getStructure(manager);
        Structure changed = null;

        // merge into the bigger structure for efficiency
        if (nodeStructure.size() > adjStructure.size()) {
            changed = nodeStructure;
            changed.add(adjStructure);
        } else {
            changed = adjStructure;
            changed.add(nodeStructure);
        }
        // update the changed structure
        changed.updateTimestamp = node.getTileWorld().getGameTime();
        changed.didUpdate = false;
        changed.removeMultiblock(node.getTileWorld());
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

        protected static final Axis[] AXES = values();
    }
}
