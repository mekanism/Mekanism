package mekanism.common.capabilities;

import java.util.function.BooleanSupplier;
import mekanism.common.util.WorldUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.capabilities.BlockCapability;
import net.neoforged.neoforge.capabilities.BlockCapabilityCache;
import net.neoforged.neoforge.capabilities.EntityCapability;
import net.neoforged.neoforge.capabilities.ItemCapability;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface IMultiTypeCapability<HANDLER, ITEM_HANDLER extends HANDLER> {

    BlockCapability<HANDLER, @Nullable Direction> block();

    ItemCapability<ITEM_HANDLER, Void> item();

    EntityCapability<HANDLER, ?> entity();

    default boolean is(BlockCapability<?, ?> capability) {
        return capability == block();
    }

    @Nullable
    default ITEM_HANDLER getCapability(ItemStack stack) {
        //Note: Safety handling of empty stack is done when looking up the provider inside getCapability's implementation
        return stack.getCapability(item());
    }

    /**
     * @apiNote Only use this helper if you don't actually need the capability, otherwise prefer using {@link #getCapability(ItemStack)} and null checking.
     */
    default boolean hasCapability(ItemStack stack) {
        return getCapability(stack) != null;
    }

    @Nullable
    default HANDLER getCapability(@Nullable Entity entity) {
        return entity == null ? null : entity.getCapability(entity(), null);
    }

    @Nullable
    default HANDLER getCapabilityIfLoaded(@Nullable Level level, @NotNull BlockPos pos, @Nullable Direction side) {
        return getCapabilityIfLoaded(level, pos, null, null, side);
    }

    @Nullable
    default HANDLER getCapabilityIfLoaded(@Nullable Level level, @NotNull BlockPos pos, @Nullable BlockState state, @Nullable BlockEntity blockEntity,
          @Nullable Direction side) {
        return WorldUtils.getCapability(level, block(), pos, state, blockEntity, side);
    }

    default BlockCapabilityCache<HANDLER, @Nullable Direction> createCache(ServerLevel level, BlockPos pos, @Nullable Direction context) {
        return BlockCapabilityCache.create(block(), level, pos, context);
    }

    default BlockCapabilityCache<HANDLER, @Nullable Direction> createCache(ServerLevel level, BlockPos pos, @Nullable Direction context, BooleanSupplier isValid,
          Runnable invalidationListener) {
        return BlockCapabilityCache.create(block(), level, pos, context, isValid, invalidationListener);
    }

    default BlockCapabilityCache<HANDLER, @Nullable Direction> createCache(ServerLevel level, BlockPos pos, @Nullable Direction context, BooleanSupplier isValid) {
        return BlockCapabilityCache.create(block(), level, pos, context, isValid, () -> {});
    }
}