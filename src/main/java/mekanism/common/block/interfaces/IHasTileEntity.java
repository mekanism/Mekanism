package mekanism.common.block.interfaces;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import mekanism.common.registration.impl.TileEntityTypeRegistryObject;
import mekanism.common.util.WorldUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

public interface IHasTileEntity<TILE extends BlockEntity> extends EntityBlock {

    TileEntityTypeRegistryObject<? extends TILE> getTileType();

    default TILE createDummyBlockEntity() {
        return createDummyBlockEntity(((Block) this).defaultBlockState());
    }

    default TILE createDummyBlockEntity(@Nonnull BlockState state) {
        return newBlockEntity(BlockPos.ZERO, state);
    }

    @Override
    default TILE newBlockEntity(@Nonnull BlockPos pos, @Nonnull BlockState state) {
        return getTileType().get().create(pos, state);
    }

    @Nullable
    @Override
    default <T extends BlockEntity> BlockEntityTicker<T> getTicker(@Nonnull Level level, @Nonnull BlockState state, @Nonnull BlockEntityType<T> blockEntityType) {
        TileEntityTypeRegistryObject<? extends TILE> type = getTileType();
        return blockEntityType == type.get() ? (BlockEntityTicker<T>) type.getTicker(level.isClientSide) : null;
    }

    default boolean triggerBlockEntityEvent(@Nonnull BlockState state, Level level, BlockPos pos, int id, int param) {
        BlockEntity blockEntity = WorldUtils.getTileEntity(level, pos);
        return blockEntity != null && blockEntity.triggerEvent(id, param);
    }
}