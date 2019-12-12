package mekanism.common.block.states;

import javax.annotation.Nonnull;
import net.minecraft.block.BlockState;
import net.minecraft.block.IWaterLoggable;
import net.minecraft.fluid.Fluid;
import net.minecraft.fluid.IFluidState;
import net.minecraft.state.properties.BlockStateProperties;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IWorld;

//TODO: After evaluating dynamic typed fluid logging if we don't need any special methods, maybe just kill this class in favor of directly using IWaterLoggable
// Note: This will probably actually be useful for overriding some of the methods in IWaterLoggable, such as valid fluids
public interface IStateWaterLogged extends IWaterLoggable {

    /**
     * Overwritten to check against canContainFluid instead of inlining the check to water directly.
     */
    @Override
    default boolean receiveFluid(@Nonnull IWorld world, @Nonnull BlockPos pos, BlockState state, @Nonnull IFluidState fluidState) {
        Fluid fluid = fluidState.getFluid();
        if (canContainFluid(world, pos, state, fluid)) {
            if (!world.isRemote()) {
                world.setBlockState(pos, state.with(BlockStateProperties.WATERLOGGED, true), 3);
                world.getPendingFluidTicks().scheduleTick(pos, fluid, fluid.getTickRate(world));
            }
            return true;
        }
        return false;
    }
}