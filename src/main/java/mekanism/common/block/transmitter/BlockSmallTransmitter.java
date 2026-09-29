package mekanism.common.block.transmitter;

import java.util.Map;
import mekanism.common.content.blocktype.BlockTypeTile;
import mekanism.common.lib.transmitter.ConnectionType;
import mekanism.common.tile.transmitter.TileEntityTransmitter;
import mekanism.common.util.VoxelShapeUtils;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jspecify.annotations.Nullable;

public class BlockSmallTransmitter<TILE extends TileEntityTransmitter> extends BlockTransmitter<TILE> {

    private static final Map<Direction, VoxelShape> SIDES = VoxelShapeUtils.rotateAllInitialDown(box(5, 0, 5, 11, 5, 11));
    private static final Map<Direction, VoxelShape> SIDES_PULL = VoxelShapeUtils.rotateAllInitialDown(VoxelShapeUtils.combine(
          box(5, 4, 5, 11, 5, 11),
          box(6, 2, 6, 10, 4, 10),
          box(4, 0, 4, 12, 2, 12)
    ));
    private static final Map<Direction, VoxelShape> SIDES_PUSH = VoxelShapeUtils.rotateAllInitialDown(VoxelShapeUtils.combine(
          box(5, 3, 5, 11, 5, 11),
          box(6, 1, 6, 10, 3, 10),
          box(7, 0, 7, 9, 1, 9)
    ));
    public static final VoxelShape CENTER = box(5, 5, 5, 11, 11, 11);

    public static VoxelShape getSideForType(ConnectionType type, Direction side) {
        if (type == ConnectionType.PUSH) {
            return SIDES_PUSH.get(side);
        } else if (type == ConnectionType.PULL) {
            return SIDES_PULL.get(side);
        } //else normal
        return SIDES.get(side);
    }

    public BlockSmallTransmitter(BlockTypeTile<TILE> type, BlockBehaviour.Properties properties) {
        this(type, properties, null);
    }

    public BlockSmallTransmitter(BlockTypeTile<TILE> type, BlockBehaviour.Properties properties, @Nullable MapColor mapColor) {
        super(type, properties, mapColor);
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