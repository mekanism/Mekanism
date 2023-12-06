package mekanism.common.lib.security;

import java.util.Set;
import java.util.UUID;
import mekanism.api.annotations.NothingNullByDefault;
import mekanism.api.security.IBlockSecurityUtils;
import mekanism.api.security.IOwnerObject;
import mekanism.api.security.ISecurityObject;
import mekanism.api.security.ISecurityUtils;
import mekanism.api.security.SecurityMode;
import mekanism.common.capabilities.Capabilities;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.capabilities.BlockCapability;
import net.neoforged.neoforge.common.util.Lazy;
import org.jetbrains.annotations.Nullable;

/**
 * @apiNote Do not instantiate this class directly as it will be done via the service loader. Instead, access instances of this via {@link IBlockSecurityUtils#INSTANCE}
 */
@NothingNullByDefault
public class BlockSecurityUtils implements IBlockSecurityUtils {

    private static final BlockCapability<IOwnerObject, Void> OWNER_CAPABILITY = BlockCapability.createVoid(Capabilities.OWNER_OBJECT_NAME, IOwnerObject.class);
    private static final BlockCapability<ISecurityObject, Void> SECURITY_CAPABILITY = BlockCapability.createVoid(Capabilities.SECURITY_OBJECT_NAME, ISecurityObject.class);

    public static BlockSecurityUtils get() {
        return (BlockSecurityUtils) INSTANCE;
    }

    @Override
    public BlockCapability<IOwnerObject, Void> ownerCapability() {
        return OWNER_CAPABILITY;
    }

    @Override
    public BlockCapability<ISecurityObject, Void> securityCapability() {
        return SECURITY_CAPABILITY;
    }

    @Override
    public boolean canAccess(Player player, Level level, BlockPos pos, @Nullable BlockState state, @Nullable BlockEntity blockEntity) {
        CachingCapabilityLookup lookup = new CachingCapabilityLookup(level, pos, state, blockEntity);
        return ISecurityUtils.INSTANCE.canAccess(player, lookup::securityCapability, lookup::ownerCapability);
    }

    @Override
    public boolean canAccess(@Nullable UUID player, Level level, BlockPos pos, @Nullable BlockState state, @Nullable BlockEntity blockEntity) {
        CachingCapabilityLookup lookup = new CachingCapabilityLookup(level, pos, state, blockEntity);
        return ISecurityUtils.INSTANCE.canAccess(player, lookup::securityCapability, lookup::ownerCapability, level.isClientSide());
    }

    @Override
    public SecurityMode getSecurityMode(Level level, BlockPos pos, @Nullable BlockState state, @Nullable BlockEntity blockEntity) {
        CachingCapabilityLookup lookup = new CachingCapabilityLookup(level, pos, state, blockEntity);
        return ISecurityUtils.INSTANCE.getSecurityMode(lookup::securityCapability, lookup::ownerCapability, level.isClientSide());
    }

    public void securityChanged(Set<Player> playersUsing, Level level, BlockPos pos, @Nullable BlockEntity target, SecurityMode old, SecurityMode mode) {
        SecurityUtils.get().securityChanged(playersUsing, player -> canAccess(player, level, pos, target), old, mode);
    }

    /**
     * Used to allow caching the block state and block entity lookup between security capability and owner capability lookup. That way if the block queried does not
     * expose a security capability at the given position we don't have to do more world lookups when querying if the block exposes an owner capability.
     */
    private static class CachingCapabilityLookup {

        private record BlockTarget(BlockState state, @Nullable BlockEntity blockEntity) {
        }

        private final Level level;
        private final BlockPos pos;
        private final Lazy<BlockTarget> lazyTarget;

        CachingCapabilityLookup(Level level, BlockPos pos, @Nullable BlockState knownState, @Nullable BlockEntity knownBlockEntity) {
            this.level = level;
            this.pos = pos;
            this.lazyTarget = Lazy.of(() -> {
                // Get block state and block entity if they were not provided
                BlockState state = knownState;
                BlockEntity blockEntity = knownBlockEntity;
                //Note: We use the same logic as BlockCapability uses for minimizing level queries here
                if (blockEntity == null) {
                    if (state == null) {
                        state = level.getBlockState(pos);
                    }
                    if (state.hasBlockEntity()) {
                        //Note: We intentionally look up directly rather than using WorldUtils that validates the position is loaded
                        // it is the responsibility of the caller to check if the position they are querying is loaded
                        blockEntity = level.getBlockEntity(pos);
                    }
                } else if (state == null) {
                    state = blockEntity.getBlockState();
                }
                return new BlockTarget(state, blockEntity);
            });
        }

        @Nullable
        ISecurityObject securityCapability() {
            BlockTarget blockTarget = lazyTarget.get();
            return IBlockSecurityUtils.INSTANCE.securityCapability(level, pos, blockTarget.state(), blockTarget.blockEntity());
        }

        @Nullable
        IOwnerObject ownerCapability() {
            BlockTarget blockTarget = lazyTarget.get();
            return IBlockSecurityUtils.INSTANCE.ownerCapability(level, pos, blockTarget.state(), blockTarget.blockEntity());
        }
    }
}