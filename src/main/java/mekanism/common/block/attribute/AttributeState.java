package mekanism.common.block.attribute;

import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Property;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface AttributeState extends Attribute {

    BlockState copyStateData(BlockState oldState, BlockState newState);

    void fillBlockStateContainer(Block block, List<Property<?>> properties);

    default BlockState getDefaultState(@NotNull BlockState state) {
        return state;
    }

    @Contract("null, _, _, _, _ -> null")
    default BlockState getStateForPlacement(@Nullable BlockState state, @NotNull LevelAccessor world, @NotNull BlockPos pos, @Nullable Player player,
          @NotNull Direction face) {
        return state;
    }
}
