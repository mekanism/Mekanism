package mekanism.common.lib.multiblock;

import mekanism.common.lib.math.Cuboid;
import mekanism.common.lib.math.IShape;
import mekanism.common.lib.multiblock.UpdateProtocol.FormationResult;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;

public class CuboidStructureValidator implements IStructureValidator {

    private static final Cuboid MIN_CUBOID = new Cuboid(3, 3, 3);

    private Cuboid cuboid;

    public CuboidStructureValidator(Structure structure) {
        cuboid = structure.fetchCuboid(MIN_CUBOID);
    }

    @Override
    public FormationResult validate(UpdateProtocol<?> protocol, UpdateProtocol<?>.ValidationContext ctx) {
        BlockPos min = cuboid.getMinPos(), max = cuboid.getMaxPos();
        for (int x = min.getX(); x <= max.getX(); x++) {
            for (int y = min.getY(); y <= max.getY(); y++) {
                for (int z = min.getZ(); z <= max.getZ(); z++) {
                    BlockPos pos = new BlockPos(x, y, z);
                    if (x == min.getX() || x == max.getX() || y == min.getY() || y == max.getY() || z == min.getZ() || z == max.getZ()) {
                        FormationResult ret = ctx.validateFrame(pos, cuboid.isEdge(pos));
                        if (!ret.isFormed()) {
                            return ret;
                        }
                    } else {
                        FormationResult ret = ctx.validateInner(pos);
                        if (!ret.isFormed()) {
                            return ret;
                        }
                    }
                }
            }
        }
        return FormationResult.SUCCESS;
    }

    @Override
    public Direction getSide(BlockPos pos) {
        return cuboid.getSide(pos);
    }

    @Override
    public IShape getShape() {
        return cuboid;
    }

    @Override
    public boolean isValid() {
        return cuboid != null;
    }
}
