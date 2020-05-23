package mekanism.common.lib.multiblock;

import java.util.Map;
import java.util.TreeMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import java.util.function.ToIntFunction;
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

    private boolean valid;

    private long updateTimestamp;
    private boolean didUpdate;

    private MultiblockData multiblockData;
    private final Map<MultiblockManager<?>, IMultiblock<?>> controllers = new Object2ObjectOpenHashMap<>();

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
            IMultiblock<?> multiblockTile = (IMultiblock<?>) node;
            controllers.put(multiblockTile.getManager(), multiblockTile);
        }
    }

    public MultiblockData getMultiblockData() {
        return multiblockData;
    }

    public void setMultiblockData(MultiblockData multiblockData) {
        this.multiblockData = multiblockData;
    }

    public IMultiblock<?> getController() {
        return controllers.size() == 1 ? controllers.values().iterator().next() : null;
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
        if (getController() != null) {
            IStructureValidator validator = getController().validateStructure();
            if (validator.precheck() && multiblockData == null) {
                return getController().getFormationProtocol().doUpdate(validator);
            }
        }
        removeMultiblock(tile.getWorld());
        return FormationResult.FAIL;
    }

    public void add(IMultiblockBase node) {
        if (!node.getStructure().isValid()) {
            node.resetStructure();
        }
        Structure s = node.getStructure();
        if (s != this) {
            s.nodes.forEach((key, value) -> {
                nodes.put(key, value);
                value.setStructure(this);
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

            controllers.putAll(s.controllers);
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

    private static <TILE extends TileEntity & IMultiblockBase> void validate(TILE node) {
        node.resetStructure();
        FormationProtocol.explore(node.getPos(), pos -> {
            if (pos.equals(node.getPos()))
                return true;
            TileEntity tile = MekanismUtils.getTileEntity(node.getWorld(), pos);
            if (tile instanceof IMultiblockBase) {
                IMultiblockBase adj = (IMultiblockBase) tile;
                if (adj.getStructure() != node.getStructure()) {
                    if (!adj.getStructure().isValid() || node.getStructure().size() > adj.getStructure().size()) {
                        node.getStructure().add(adj);
                    } else {
                        adj.getStructure().add(node);
                    }
                    return true;
                }
            }
            return false;
        });

        node.getStructure().updateTimestamp = node.getWorld().getGameTime();
        node.getStructure().didUpdate = false;
        node.getStructure().removeMultiblock(node.getWorld());
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
