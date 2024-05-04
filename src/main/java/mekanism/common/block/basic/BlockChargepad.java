package mekanism.common.block.basic;

import mekanism.common.block.prefab.BlockTile.BlockTileModel;
import mekanism.common.content.blocktype.BlockTypeTile;
import mekanism.common.registries.MekanismBlockTypes;
import mekanism.common.tile.TileEntityChargepad;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.EntityCollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.NotNull;

public class BlockChargepad extends BlockTileModel<TileEntityChargepad, BlockTypeTile<TileEntityChargepad>> {

    private static final VoxelShape BASE = box(0, 0, 0, 16, 1, 16);

    public BlockChargepad() {
        super(MekanismBlockTypes.CHARGEPAD, properties -> properties.mapColor(MapColor.COLOR_GRAY));
    }

    @NotNull
    @Override
    protected VoxelShape getCollisionShape(@NotNull BlockState state, @NotNull BlockGetter level, @NotNull BlockPos pos, @NotNull CollisionContext context) {
        if (!(context instanceof EntityCollisionContext entityCollisionContext) || !(entityCollisionContext.getEntity() instanceof Projectile)) {
            return BASE;
        }
        return super.getCollisionShape(state, level, pos, context);
    }
}