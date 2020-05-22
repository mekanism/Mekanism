package mekanism.common.lib.multiblock;

import java.util.Map;
import java.util.TreeMap;
import com.google.common.base.Function;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import mekanism.common.lib.math.BlockPosBuilder;
import mekanism.common.lib.math.VoxelPlane;
import mekanism.common.lib.multiblock.FormationProtocol.FormationResult;
import mekanism.common.util.MekanismUtils;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class Structure {

    public static final Structure INVALID = new Structure();

    private Map<BlockPos, IMultiblockBase> nodes = new Object2ObjectOpenHashMap<>();
    private Map<Axis, TreeMap<Integer, VoxelPlane>> planeMap = new Object2ObjectOpenHashMap<>();

    private boolean valid;

    private long updateTimestamp;
    private boolean didUpdate;

    private MultiblockData multiblockData;
    private Map<MultiblockManager<?>, IMultiblock<?>> controllers = new Object2ObjectOpenHashMap<>();

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
        TreeMap<Integer, VoxelPlane> ret = planeMap.get(axis);
        if (ret == null) {
            ret = new TreeMap<>(Integer::compare);
            planeMap.put(axis, ret);
        }
        return ret;
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
            s.nodes.entrySet().forEach(entry -> {
                nodes.put(entry.getKey(), entry.getValue());
                entry.getValue().setStructure(this);
            });

            for (Axis axis : s.planeMap.keySet()) {
                Map<Integer, VoxelPlane> map = getAxisMap(axis);
                s.planeMap.get(axis).entrySet().forEach(entry -> {
                    VoxelPlane p = map.get(entry.getKey());
                    if (p != null) {
                        p.merge(entry.getValue());
                    } else {
                        p = entry.getValue();
                        map.put(entry.getKey(), entry.getValue());
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
        X(pos -> pos.getX()),
        Y(pos -> pos.getY()),
        Z(pos -> pos.getZ());

        private Function<BlockPos, Integer> posMapper;

        private Axis(Function<BlockPos, Integer> posMapper) {
            this.posMapper = posMapper;
        }

        public int getCoord(BlockPos pos) {
            return posMapper.apply(pos);
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
