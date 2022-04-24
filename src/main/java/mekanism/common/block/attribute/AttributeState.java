package mekanism.common.block.attribute;

import java.util.List;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Property;
import org.jetbrains.annotations.Contract;

public interface AttributeState extends Attribute {

    BlockState copyStateData(BlockState oldState, BlockState newState);

    void fillBlockStateContainer(Block block, List<Property<?>> properties);

    default BlockState getDefaultState(@Nonnull BlockState state) {
        return state;
    }

    @Contract("_, null, _, _, _, _ -> null")
    default BlockState getStateForPlacement(Block block, @Nullable BlockState state, @Nonnull LevelAccessor world, @Nonnull BlockPos pos, @Nullable Player player,
          @Nonnull Direction face) {
        return state;
    }
}
