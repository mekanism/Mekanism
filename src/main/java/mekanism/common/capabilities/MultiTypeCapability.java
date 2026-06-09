package mekanism.common.capabilities;

import java.util.function.BooleanSupplier;
import mekanism.common.util.ItemAccessUtils;
import mekanism.common.util.WorldUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.TypedInstance;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.capabilities.BlockCapability;
import net.neoforged.neoforge.capabilities.BlockCapabilityCache;
import net.neoforged.neoforge.capabilities.EntityCapability;
import net.neoforged.neoforge.capabilities.ItemCapability;
import net.neoforged.neoforge.transfer.access.ItemAccess;
import org.jspecify.annotations.Nullable;

public record MultiTypeCapability<HANDLER>(BlockCapability<HANDLER, @Nullable Direction> block,
                                           ItemCapability<HANDLER, ItemAccess> item,
                                           EntityCapability<HANDLER, ?> entity) {

    public MultiTypeCapability(Identifier name, Class<HANDLER> handlerClass) {
        this(
              BlockCapability.createSided(name, handlerClass),
              ItemCapability.create(name, handlerClass, ItemAccess.class),
              EntityCapability.createVoid(name, handlerClass)
        );
    }

    public boolean is(BlockCapability<?, ?> capability) {
        return capability == block();
    }

    @Nullable
    public HANDLER getQueryOnlyCapability(TypedInstance<Item> instance) {
        return getCapability(ItemAccessUtils.sideEffectFreeAccess(instance));
    }

    @Nullable
    public HANDLER getCapability(ItemAccess access) {//TODO - 26.1: Re-evaluate callers and if any of them need to be oneByOne
        //Note: Safety handling of empty stack is done when looking up the provider inside getCapability's implementation
        return access.getCapability(item());
    }

    @Nullable
    public HANDLER getCapability(@Nullable Entity entity) {
        return entity == null ? null : entity.getCapability(entity(), null);
    }

    @Nullable
    public HANDLER getCapabilityIfLoaded(@Nullable Level level, BlockPos pos, @Nullable Direction side) {
        return getCapabilityIfLoaded(level, pos, null, null, side);
    }

    @Nullable
    public HANDLER getCapabilityIfLoaded(@Nullable Level level, BlockPos pos, @Nullable BlockState state, @Nullable BlockEntity blockEntity,
          @Nullable Direction side) {
        return WorldUtils.getCapability(level, block(), pos, state, blockEntity, side);
    }

    public BlockCapabilityCache<HANDLER, @Nullable Direction> createCache(ServerLevel level, BlockPos pos, @Nullable Direction context) {
        return BlockCapabilityCache.create(block(), level, pos, context);
    }

    public BlockCapabilityCache<HANDLER, @Nullable Direction> createCache(ServerLevel level, BlockPos pos, @Nullable Direction context, BooleanSupplier isValid,
          Runnable invalidationListener) {
        return BlockCapabilityCache.create(block(), level, pos, context, isValid, invalidationListener);
    }

    public BlockCapabilityCache<HANDLER, @Nullable Direction> createCache(ServerLevel level, BlockPos pos, @Nullable Direction context, BooleanSupplier isValid) {
        return createCache(level, pos, context, isValid, () -> {});
    }
}