package mekanism.common.block.attribute;

import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Property;
import org.jspecify.annotations.Nullable;

public interface AttributeState extends Attribute {

    BlockState copyStateData(BlockState oldState, BlockState newState);

    void fillBlockStateContainer(Block block, List<Property<?>> properties);

    default BlockState getDefaultState(BlockState state) {
        return state;
    }

    default BlockState getStateForPlacement(BlockState state, LevelAccessor world, BlockPos pos, @Nullable Player player, Direction face) {
        return state;
    }
}
