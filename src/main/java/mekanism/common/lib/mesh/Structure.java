package mekanism.common.lib.mesh;

import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.function.BiConsumer;
import com.google.common.base.Function;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import mekanism.common.lib.multiblock.MultiblockData;
import mekanism.common.lib.multiblock.MultiblockManager;
import mekanism.common.lib.multiblock.UpdateProtocol.Explorer;
import mekanism.common.util.MekanismUtils;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;

public class Structure {

    public static final Structure INVALID = new Structure();

    private Map<BlockPos, IMeshNode> nodes = new Object2ObjectOpenHashMap<>();
    private Map<Axis, TreeMap<Integer, Plane>> planeMap = new Object2ObjectOpenHashMap<>();

    private boolean valid;

    private long updateTimestamp;
    private boolean didUpdate;

    private MultiblockData multiblock;
    private Set<MultiblockManager<?>> managers = new ObjectOpenHashSet<>();

    private Structure() {}

    public Structure(IMeshNode node) {
        init(node);
        valid = true;
    }

    private void init(IMeshNode node) {
        nodes.put(node.getPos(), node);
        for (Axis axis : Axis.AXES) {
            getAxisMap(axis).put(axis.getCoord(node.getPos()), new Plane(axis, node.getPos()));
        }
    }

    private TreeMap<Integer, Plane> getAxisMap(Axis axis) {
        TreeMap<Integer, Plane> ret = planeMap.get(axis);
        if (ret == null) {
            ret = new TreeMap<>(Integer::compare);
            planeMap.put(axis, ret);
        }
        return ret;
    }

    public <TILE extends TileEntity & IMeshNode> void tick(TILE tile) {
        if (!didUpdate && updateTimestamp == tile.getWorld().getGameTime() - 1) {
            System.out.println(size() + " " + hashCode() + " " + fetchCuboid(new Cuboid(3, 3, 3)));
            didUpdate = true;
        }
        if (!isValid()) {
            validate(tile);
        }
    }

    public void add(IMeshNode node) {
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
        }
    }

    public boolean isValid() {
        return valid;
    }

    public void invalidate() {
        valid = false;
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

    private static <TILE extends TileEntity & IMeshNode> void validate(TILE node) {
        node.resetStructure();
        new Explorer(pos -> {
            if (pos.equals(node.getPos()))
                return true;
            TileEntity tile = MekanismUtils.getTileEntity(node.getWorld(), pos);
            if (tile instanceof IMeshNode) {
                IMeshNode adj = (IMeshNode) tile;
                if (!adj.getStructure().isValid() || node.getStructure().size() > adj.getStructure().size()) {
                    node.getStructure().add(adj);
                } else {
                    adj.getStructure().add(node);
                }
                return true;
            }
            return false;
        }).explore(node.getPos());

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
