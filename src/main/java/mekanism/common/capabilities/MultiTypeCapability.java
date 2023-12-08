package mekanism.common.capabilities;

import java.util.function.BooleanSupplier;
import mekanism.common.util.WorldUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
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

public record MultiTypeCapability<HANDLER>(BlockCapability<HANDLER, @Nullable Direction> block,
                                           ItemCapability<HANDLER, Void> item,
                                           EntityCapability<HANDLER, ?> entity) {

    public MultiTypeCapability(ResourceLocation name, Class<HANDLER> handlerClass) {
        this(
              BlockCapability.createSided(name, handlerClass),
              ItemCapability.createVoid(name, handlerClass),
              EntityCapability.createVoid(name, handlerClass)
        );
    }

    public boolean is(BlockCapability<?, ?> capability) {
        return capability == block();
    }

    @Nullable
    public HANDLER getCapability(ItemStack stack) {
        //Note: Safety handling of empty stack is done when looking up the provider inside getCapability's implementation
        return stack.getCapability(item());
    }

    /**
     * @apiNote Only use this helper if you don't actually need the capability, otherwise prefer using {@link #getCapability(ItemStack)} and null checking.
     */
    public boolean hasCapability(ItemStack stack) {
        return getCapability(stack) != null;
    }

    @Nullable
    public HANDLER getCapability(@Nullable Entity entity) {
        return entity == null ? null : entity.getCapability(entity(), null);
    }

    @Nullable
    public HANDLER getCapabilityIfLoaded(@Nullable Level level, @NotNull BlockPos pos, @Nullable Direction side) {
        return getCapabilityIfLoaded(level, pos, null, null, side);
    }

    @Nullable
    public HANDLER getCapabilityIfLoaded(@Nullable Level level, @NotNull BlockPos pos, @Nullable BlockState state, @Nullable BlockEntity blockEntity,
          @Nullable Direction side) {
        return WorldUtils.getCapability(level, block(), pos, state, blockEntity, side);
    }

    public BlockCapabilityCache<HANDLER, @Nullable Direction> createCache(ServerLevel level, BlockPos pos, @Nullable Direction context, BooleanSupplier isValid,
          Runnable invalidationListener) {
        return BlockCapabilityCache.create(block(), level, pos, context, isValid, invalidationListener);
    }
}