package mekanism.common.block.transmitter;

import java.util.function.UnaryOperator;
import mekanism.common.content.blocktype.BlockTypeTile;
import mekanism.common.lib.transmitter.ConnectionType;
import mekanism.common.tile.transmitter.TileEntityTransmitter;
import mekanism.common.util.EnumUtils;
import mekanism.common.util.VoxelShapeUtils;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.phys.shapes.VoxelShape;

public class BlockLargeTransmitter<TILE extends TileEntityTransmitter> extends BlockTransmitter<TILE> {

    private static final VoxelShape[] SIDES = new VoxelShape[EnumUtils.DIRECTIONS.length];
    private static final VoxelShape[] SIDES_PULL = new VoxelShape[EnumUtils.DIRECTIONS.length];
    private static final VoxelShape[] SIDES_PUSH = new VoxelShape[EnumUtils.DIRECTIONS.length];
    public static final VoxelShape CENTER = box(4, 4, 4, 12, 12, 12);

    static {
        VoxelShapeUtils.setShape(box(4, 0, 4, 12, 4, 12), SIDES, true);
        VoxelShapeUtils.setShape(VoxelShapeUtils.combine(
              box(4, 3, 4, 12, 4, 12),
              box(5, 2, 5, 11, 3, 11),
              box(3, 0, 3, 13, 2, 13)
        ), SIDES_PULL, true);
        VoxelShapeUtils.setShape(VoxelShapeUtils.combine(
              box(4, 3, 4, 12, 4, 12),
              box(5, 1, 5, 11, 3, 11),
              box(6, 0, 6, 10, 1, 10)
        ), SIDES_PUSH, true);
    }

    public static VoxelShape getSideForType(ConnectionType type, Direction side) {
        if (type == ConnectionType.PUSH) {
            return SIDES_PUSH[side.ordinal()];
        } else if (type == ConnectionType.PULL) {
            return SIDES_PULL[side.ordinal()];
        } //else normal
        return SIDES[side.ordinal()];
    }

    public BlockLargeTransmitter(BlockTypeTile<TILE> type) {
        super(type);
    }

    public BlockLargeTransmitter(BlockTypeTile<TILE> type, UnaryOperator<BlockBehaviour.Properties> propertiesModifier) {
        super(type, propertiesModifier);
    }

    @Override
    protected VoxelShape getCenter() {
        return CENTER;
    }

    @Override
    protected VoxelShape getSide(ConnectionType type, Direction side) {
        return getSideForType(type, side);
    }
}