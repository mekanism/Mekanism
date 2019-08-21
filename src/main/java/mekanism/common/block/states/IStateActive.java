package mekanism.common.block.states;

import javax.annotation.Nonnull;
import net.minecraft.block.BlockState;

//TODO: Should/Can IActiveSate be merged with this overriding this. (Will look at when moving some TileEntity stuff into blocks/block states more directly)
public interface IStateActive {

    default boolean isActive(BlockState state) {
        return state.get(BlockStateHelper.activeProperty);
    }

    default BlockState setActive(@Nonnull BlockState state, boolean active) {
        return state.with(BlockStateHelper.activeProperty, active);
    }
}