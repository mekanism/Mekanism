package mekanism.common.lib.multiblock;

import java.util.Map;
import java.util.TreeMap;
import java.util.function.BiConsumer;
import com.google.common.base.Function;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import mekanism.common.lib.multiblock.IMultiblockBase.UpdateType;
import mekanism.common.lib.multiblock.UpdateProtocol.FormationResult;
import mekanism.common.util.MekanismUtils;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class Structure {

    public static final Structure INVALID = new Structure();
    private static final Cuboid MIN_BOUNDS = new Cuboid(3, 3, 3);

    private Map<BlockPos, IMultiblockBase> nodes = new Object2ObjectOpenHashMap<>();
    private Map<Axis, TreeMap<Integer, Plane>> planeMap = new Object2ObjectOpenHashMap<>();

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
        nodes.put(node.getPos(), node);
        for (Axis axis : Axis.AXES) {
            getAxisMap(axis).put(axis.getCoord(node.getPos()), new Plane(axis, node.getPos()));
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

    private TreeMap<Integer, Plane> getAxisMap(Axis axis) {
        TreeMap<Integer, Plane> ret = planeMap.get(axis);
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
            Cuboid cuboid = fetchCuboid(MIN_BOUNDS);
            if (cuboid != null && multiblockData == null) {
                return getController().runUpdate(UpdateType.NORMAL, cuboid);
            } else {
                removeMultiblock(tile.getWorld());
            }
        }
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
                Map<Integer, Plane> map = getAxisMap(axis);
                s.planeMap.get(axis).entrySet().forEach(entry -> {
                    Plane p = map.get(entry.getKey());
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

    public Cuboid fetchCuboid(Cuboid minBounds) {
        Cuboid prev = null;
        for (Axis axis : Axis.AXES) {
            TreeMap<Integer, Plane> map = getAxisMap(axis);
            Map.Entry<Integer, Plane> first = map.firstEntry(), last = map.lastEntry();
            if (first == null || !first.getValue().equals(last.getValue()) || !first.getValue().isFull()) {
                return null;
            }
            Cuboid cuboid = Cuboid.from(first.getValue(), last.getValue(), first.getKey(), last.getKey());
            // if this is the first axial cuboid check, make sure we meet the min bounds
            if (prev == null && !cuboid.greaterOrEqual(minBounds)) {
                return null;
            }
            // if this isn't the first axial cuboid check, make sure
            if (prev != null && !prev.equals(cuboid)) {
                return null;
            }
            prev = cuboid;
        }
        return prev;
    }

    private static <TILE extends TileEntity & IMultiblockBase> void validate(TILE node) {
        node.resetStructure();
        UpdateProtocol.explore(node.getPos(), pos -> {
            if (pos.equals(node.getPos()))
                return true;
            TileEntity tile = MekanismUtils.getTileEntity(node.getWorld(), pos);
            if (tile instanceof IMultiblockBase) {
                IMultiblockBase adj = (IMultiblockBase) tile;
                if (!adj.getStructure().isValid() || node.getStructure().size() > adj.getStructure().size()) {
                    node.getStructure().add(adj);
                } else {
                    adj.getStructure().add(node);
                }
                return true;
            }
            return false;
        });

        node.getStructure().updateTimestamp = node.getWorld().getGameTime();
        node.getStructure().didUpdate = false;
    }

    public enum Axis {
        X(pos -> pos.getX(), (pos, val) -> pos.x = val),
        Y(pos -> pos.getY(), (pos, val) -> pos.y = val),
        Z(pos -> pos.getZ(), (pos, val) -> pos.z = val);

        private Function<BlockPos, Integer> posMapper;
        private BiConsumer<BlockPosBuilder, Integer> setter;

        private Axis(Function<BlockPos, Integer> posMapper, BiConsumer<BlockPosBuilder, Integer> setter) {
            this.posMapper = posMapper;
            this.setter = setter;
        }

        public int getCoord(BlockPos pos) {
            return posMapper.apply(pos);
        }

        public void set(BlockPosBuilder pos, int val) {
            setter.accept(pos, val);
        }

        public Axis horizontal() {
            return this == X ? Z : X;
        }

        public Axis vertical() {
            return this == Y ? Z : Y;
        }

        protected static final Axis[] AXES = values();
    }

    public static class BlockPosBuilder {

        protected int x, y, z;

        public BlockPos build() {
            return new BlockPos(x, y, z);
        }
    }
}
