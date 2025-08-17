package mekanism.common.integration.energy;

import java.util.Objects;
import java.util.function.BooleanSupplier;
import mekanism.api.energy.IStrictEnergyHandler;
import mekanism.api.functions.ConstantPredicates;
import mekanism.common.util.WorldUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.capabilities.ICapabilityInvalidationListener;
import org.jetbrains.annotations.Nullable;

/**
 * A cache for block capabilities, to be used to track capabilities at a specific position, with a specific context.
 *
 * <p>The cache is invalidated when the level is notified of a change via {@link Level#invalidateCapabilities(BlockPos)}.
 *
 * <p>Instances are automatically cleared by the garbage collector when they are no longer in use.
 *
 * @implNote This is a modified copy of NeoForge's {@link net.neoforged.neoforge.capabilities.BlockCapabilityCache}
 */
public class BlockEnergyCapabilityCache {

    /**
     * Creates a new cache instance and registers it to the level.
     *
     * @param level   the level
     * @param pos     the position
     * @param context extra context for the query
     */
    public static BlockEnergyCapabilityCache create(ServerLevel level, BlockPos pos, @Nullable Direction context) {
        return create(level, pos, context, ConstantPredicates.ALWAYS_TRUE, () -> {
        });
    }

    /**
     * Creates a new cache instance with an invalidation listener, and registers it to the level.
     *
     * <p>A few details regarding the system:
     * <ul>
     * <li>Calling {@link #getCapability()} from the invalidation listener is not supported,
     * as the block being invalidated might not be ready to be queried again yet.
     * If you receive an invalidation notification, you should wait for some time
     * (e.g. until your own tick) before checking {@link #getCapability()} again.</li>
     * <li>In general, do not perform any level access for the listener.
     * The listener itself might be in a chunk that is being unloaded, for example.</li>
     * <li>The listener does not receive notifications before {@link #getCapability()} is called.
     * After each invalidation, {@link #getCapability()} must be called again to enable further notifications.</li>
     * </ul>
     *
     * @param level                the level
     * @param pos                  the position
     * @param context              extra context for the query
     * @param isValid              a function to check if the listener still wants to receive notifications. A typical example would be {@code () -> !this.isRemoved()}
     *                             for a block entity that should not receive invalidation notifications anymore once it is removed.
     * @param invalidationListener the invalidation listener. Will be called whenever the capability of the cache might have changed.
     */
    public static BlockEnergyCapabilityCache create(ServerLevel level, BlockPos pos, @Nullable Direction context, BooleanSupplier isValid, Runnable invalidationListener) {
        Objects.requireNonNull(isValid);
        Objects.requireNonNull(invalidationListener);
        pos = pos.immutable();

        BlockEnergyCapabilityCache cache = new BlockEnergyCapabilityCache(level, pos, context, isValid, invalidationListener);
        level.registerCapabilityListener(pos, cache.listener);
        return cache;
    }

    private final ServerLevel level;
    private final BlockPos pos;
    @Nullable
    private final Direction context;

    /**
     * {@code true} if notifications received by the cache will be forwarded to {@link #listener}. By default and after each invalidation, this is set to {@code false}.
     * Calling {@link #getCapability()} sets it to {@code true}.
     */
    private boolean cacheValid = false;
    @Nullable
    private IStrictEnergyHandler cachedCap = null;

    private boolean canQuery = true;
    private final ICapabilityInvalidationListener listener;

    private BlockEnergyCapabilityCache(ServerLevel level, BlockPos pos, @Nullable Direction context, BooleanSupplier isValid, Runnable invalidationListener) {
        this.level = level;
        this.pos = pos;
        this.context = context;
        this.listener = () -> {
            if (!cacheValid) {
                // already invalidated, just check if the cache should be removed
                return isValid.getAsBoolean();
            }

            // disable queries for now
            canQuery = false;
            // mark cached cap as invalid
            cacheValid = false;
            // clear cached cap
            cachedCap = null;

            if (isValid.getAsBoolean()) {
                // notify
                invalidationListener.run();
                // re-enable queries
                canQuery = true;
                return true;
            } else {
                // not valid anymore: keep queries disabled and return false
                return false;
            }
        };
    }

    public ServerLevel level() {
        return level;
    }

    public BlockPos pos() {
        return pos;
    }

    @Nullable
    public Direction context() {
        return context;
    }

    /**
     * Gets the capability instance, or {@code null} if the capability is not present.
     *
     * <p>If {@linkplain #pos() the target position} is not loaded, this method will return {@code null}.
     */
    @Nullable
    public IStrictEnergyHandler getCapability() {
        if (!canQuery) {
            throw new IllegalStateException("Do not call getCapability on an invalid cache or from the invalidation listener!");
        }
        //TODO - 1.20.4: Do we need to update/invalidate the cached value if one of the configs related to conversion blacklisting is changed?
        if (!cacheValid) {
            if (WorldUtils.isBlockLoaded(level, pos)) {
                BlockState state = level.getBlockState(pos);
                BlockEntity blockEntity = state.hasBlockEntity() ? level.getBlockEntity(pos) : null;
                cachedCap = EnergyCompatUtils.getStrictEnergyHandler(level, pos, state, blockEntity, context);
            } else {
                // If the position is not loaded, return no capability for now.
                // The cache will be invalidated when the chunk is loaded.
                cachedCap = null;
            }
            cacheValid = true;
        }
        return cachedCap;
    }
}