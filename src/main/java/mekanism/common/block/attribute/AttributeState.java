package mekanism.common.block.attribute;

import java.util.List;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.state.IProperty;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IWorld;
import org.jetbrains.annotations.Contract;

public abstract class AttributeState implements Attribute {

    public abstract BlockState copyStateData(BlockState oldState, BlockState newState);

    public abstract void fillBlockStateContainer(Block block, List<IProperty<?>> properties);

    public BlockState getDefaultState(@Nonnull BlockState state) {
        return state;
    }

    @Contract("_, null, _, _, _, _ -> null")
    public BlockState getStateForPlacement(Block block, @Nullable BlockState state, @Nonnull IWorld world, @Nonnull BlockPos pos, @Nullable PlayerEntity player, @Nonnull Direction face) {
        return state;
    }
}
