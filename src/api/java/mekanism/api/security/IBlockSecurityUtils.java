package mekanism.api.security;

import java.util.UUID;
import java.util.function.Supplier;
import mekanism.api.MekanismAPI;
import mekanism.api.annotations.NothingNullByDefault;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.capabilities.BlockCapability;
import org.jetbrains.annotations.Nullable;

/**
 * Utility class for interacting with Mekanism's security system when applied to blocks and block entities.
 *
 * @see IBlockSecurityUtils#INSTANCE
 * @since 10.5.0
 */
@NothingNullByDefault
public interface IBlockSecurityUtils {

    /**
     * Provides access to Mekanism's implementation of {@link IBlockSecurityUtils}.
     *
     * @since 10.5.0
     */
    IBlockSecurityUtils INSTANCE = MekanismAPI.getService(IBlockSecurityUtils.class);

    /**
     * {@return the block capability representing owner objects}
     */
    BlockCapability<IOwnerObject, Void> ownerCapability();

    /**
     * {@return the owner capability for the block at the given location or null if the block doesn't expose an owner capability}
     *
     * @param level Level to query.
     * @param pos   Position in level.
     */
    @Nullable
    default IOwnerObject ownerCapability(Level level, BlockPos pos) {
        return ownerCapability(level, pos, null, null);
    }

    /**
     * {@return the owner capability for the block at the given location or null if the block doesn't expose an owner capability}
     *
     * @param level       Level to query.
     * @param pos         Position in level.
     * @param blockEntity The block entity, if known, or null if unknown.
     */
    @Nullable
    default IOwnerObject ownerCapability(Level level, BlockPos pos, @Nullable BlockEntity blockEntity) {
        return ownerCapability(level, pos, null, blockEntity);
    }

    /**
     * {@return the owner capability for the block at the given location or null if the block doesn't expose an owner capability}
     *
     * @param level       Level to query.
     * @param pos         Position in level.
     * @param state       The block state, if known, or null if unknown.
     * @param blockEntity The block entity, if known, or null if unknown.
     */
    @Nullable
    default IOwnerObject ownerCapability(Level level, BlockPos pos, @Nullable BlockState state, @Nullable BlockEntity blockEntity) {
        return level.getCapability(ownerCapability(), pos, state, blockEntity, null);
    }

    /**
     * {@return the block capability representing security objects}
     */
    BlockCapability<ISecurityObject, Void> securityCapability();

    /**
     * {@return the security capability for the block at the given location or null if the block doesn't expose a security capability}
     *
     * @param level Level to query.
     * @param pos   Position in level.
     */
    @Nullable
    default ISecurityObject securityCapability(Level level, BlockPos pos) {
        return securityCapability(level, pos, null, null);
    }

    /**
     * {@return the security capability for the block at the given location or null if the block doesn't expose a security capability}
     *
     * @param level       Level to query.
     * @param pos         Position in level.
     * @param blockEntity The block entity, if known, or null if unknown.
     */
    @Nullable
    default ISecurityObject securityCapability(Level level, BlockPos pos, @Nullable BlockEntity blockEntity) {
        return securityCapability(level, pos, null, blockEntity);
    }

    /**
     * {@return the security capability for the block at the given location or null if the block doesn't expose a security capability}
     *
     * @param level       Level to query.
     * @param pos         Position in level.
     * @param state       The block state, if known, or null if unknown.
     * @param blockEntity The block entity, if known, or null if unknown.
     */
    @Nullable
    default ISecurityObject securityCapability(Level level, BlockPos pos, @Nullable BlockState state, @Nullable BlockEntity blockEntity) {
        return level.getCapability(securityCapability(), pos, state, blockEntity, null);
    }

    /**
     * Checks if a player can access the block at the given location; validating that protection is enabled in the config. Additionally, this method also checks to see if
     * operators bypassing security is enabled in the config and if it is, provides access to the player if they are an operator.
     *
     * @param player Player to check access for.
     * @param level  Level to query.
     * @param pos    Position in level.
     *
     * @return {@code true} if the player can access the block.
     *
     * @implNote This method assumes that if the security is {@link SecurityMode#TRUSTED} and there is a clientside player, then the player can access the
     * {@link ISecurityObject security object}. This is done because the list of trusted players is not currently synced to all clients.
     * @see #canAccess(Player, Level, BlockPos, BlockEntity)
     * @see #canAccess(Player, Level, BlockPos, BlockState, BlockEntity)
     * @see #canAccess(UUID, Level, BlockPos)
     * @see #canAccess(UUID, Level, BlockPos, BlockEntity)
     * @see #canAccess(UUID, Level, BlockPos, BlockState, BlockEntity)
     * @see ISecurityUtils#canAccess(Player, Supplier, Supplier)
     * @see ISecurityUtils#canAccess(UUID, Supplier, Supplier, boolean)
     * @see ISecurityUtils#canAccessObject(Player, ISecurityObject)
     * @see ISecurityUtils#canAccessObject(UUID, ISecurityObject, boolean)
     * @see #canAccessOrDisplayError(Player, Level, BlockPos)
     * @see #canAccessOrDisplayError(Player, Level, BlockPos, BlockEntity)
     * @see #canAccessOrDisplayError(Player, Level, BlockPos, BlockState, BlockEntity)
     */
    default boolean canAccess(Player player, Level level, BlockPos pos) {
        return canAccess(player, level, pos, null, null);
    }

    /**
     * Checks if a player can access the block at the given location; validating that protection is enabled in the config. Additionally, this method also checks to see if
     * operators bypassing security is enabled in the config and if it is, provides access to the player if they are an operator.
     *
     * @param player      Player to check access for.
     * @param level       Level to query.
     * @param pos         Position in level.
     * @param blockEntity The block entity, if known, or null if unknown.
     *
     * @return {@code true} if the player can access the block.
     *
     * @implNote This method assumes that if the security is {@link SecurityMode#TRUSTED} and there is a clientside player, then the player can access the
     * {@link ISecurityObject security object}. This is done because the list of trusted players is not currently synced to all clients.
     * @see #canAccess(Player, Level, BlockPos)
     * @see #canAccess(Player, Level, BlockPos, BlockState, BlockEntity)
     * @see #canAccess(UUID, Level, BlockPos)
     * @see #canAccess(UUID, Level, BlockPos, BlockEntity)
     * @see #canAccess(UUID, Level, BlockPos, BlockState, BlockEntity)
     * @see ISecurityUtils#canAccess(Player, Supplier, Supplier)
     * @see ISecurityUtils#canAccess(UUID, Supplier, Supplier, boolean)
     * @see ISecurityUtils#canAccessObject(Player, ISecurityObject)
     * @see ISecurityUtils#canAccessObject(UUID, ISecurityObject, boolean)
     * @see #canAccessOrDisplayError(Player, Level, BlockPos)
     * @see #canAccessOrDisplayError(Player, Level, BlockPos, BlockEntity)
     * @see #canAccessOrDisplayError(Player, Level, BlockPos, BlockState, BlockEntity)
     */
    default boolean canAccess(Player player, Level level, BlockPos pos, @Nullable BlockEntity blockEntity) {
        return canAccess(player, level, pos, null, blockEntity);
    }

    /**
     * Checks if a player can access the block at the given location; validating that protection is enabled in the config. Additionally, this method also checks to see if
     * operators bypassing security is enabled in the config and if it is, provides access to the player if they are an operator.
     *
     * @param player      Player to check access for.
     * @param level       Level to query.
     * @param pos         Position in level.
     * @param state       The block state, if known, or null if unknown.
     * @param blockEntity The block entity, if known, or null if unknown.
     *
     * @return {@code true} if the player can access the block.
     *
     * @implNote This method assumes that if the security is {@link SecurityMode#TRUSTED} and there is a clientside player, then the player can access the
     * {@link ISecurityObject security object}. This is done because the list of trusted players is not currently synced to all clients.
     * @see #canAccess(Player, Level, BlockPos)
     * @see #canAccess(Player, Level, BlockPos, BlockEntity)
     * @see #canAccess(UUID, Level, BlockPos)
     * @see #canAccess(UUID, Level, BlockPos, BlockEntity)
     * @see #canAccess(UUID, Level, BlockPos, BlockState, BlockEntity)
     * @see ISecurityUtils#canAccess(Player, Supplier, Supplier)
     * @see ISecurityUtils#canAccess(UUID, Supplier, Supplier, boolean)
     * @see ISecurityUtils#canAccessObject(Player, ISecurityObject)
     * @see ISecurityUtils#canAccessObject(UUID, ISecurityObject, boolean)
     * @see #canAccessOrDisplayError(Player, Level, BlockPos)
     * @see #canAccessOrDisplayError(Player, Level, BlockPos, BlockEntity)
     * @see #canAccessOrDisplayError(Player, Level, BlockPos, BlockState, BlockEntity)
     */
    boolean canAccess(Player player, Level level, BlockPos pos, @Nullable BlockState state, @Nullable BlockEntity blockEntity);

    /**
     * Checks if a player can access the block at the given location; validating that protection is enabled in the config.
     *
     * @param player Player to check access for.
     * @param level  Level to query.
     * @param pos    Position in level.
     *
     * @return {@code true} if the player can access the block at the given location. If the player is {@code null} this will return {@code true} if the block's security
     * is {@link SecurityMode#PUBLIC}.
     *
     * @implNote This method assumes that if the security is {@link SecurityMode#TRUSTED} and there is a player and {@code isClient} is {@code true}, then the player can
     * access the {@link ISecurityObject security object}. This is done because the list of trusted players is not currently synced to all clients.
     * @see #canAccess(Player, Level, BlockPos)
     * @see #canAccess(Player, Level, BlockPos, BlockEntity)
     * @see #canAccess(Player, Level, BlockPos, BlockState, BlockEntity)
     * @see #canAccess(UUID, Level, BlockPos, BlockEntity)
     * @see #canAccess(UUID, Level, BlockPos, BlockState, BlockEntity)
     * @see ISecurityUtils#canAccess(Player, Supplier, Supplier)
     * @see ISecurityUtils#canAccess(UUID, Supplier, Supplier, boolean)
     * @see ISecurityUtils#canAccessObject(Player, ISecurityObject)
     * @see ISecurityUtils#canAccessObject(UUID, ISecurityObject, boolean)
     * @see #canAccessOrDisplayError(Player, Level, BlockPos)
     * @see #canAccessOrDisplayError(Player, Level, BlockPos, BlockEntity)
     * @see #canAccessOrDisplayError(Player, Level, BlockPos, BlockState, BlockEntity)
     */
    default boolean canAccess(@Nullable UUID player, Level level, BlockPos pos) {
        return canAccess(player, level, pos, null, null);
    }

    /**
     * Checks if a player can access the block at the given location; validating that protection is enabled in the config.
     *
     * @param player      Player to check access for.
     * @param level       Level to query.
     * @param pos         Position in level.
     * @param blockEntity The block entity, if known, or null if unknown.
     *
     * @return {@code true} if the player can access the block at the given location. If the player is {@code null} this will return {@code true} if the block's security
     * is {@link SecurityMode#PUBLIC}.
     *
     * @implNote This method assumes that if the security is {@link SecurityMode#TRUSTED} and there is a player and {@code isClient} is {@code true}, then the player can
     * access the {@link ISecurityObject security object}. This is done because the list of trusted players is not currently synced to all clients.
     * @see #canAccess(Player, Level, BlockPos)
     * @see #canAccess(Player, Level, BlockPos, BlockEntity)
     * @see #canAccess(Player, Level, BlockPos, BlockState, BlockEntity)
     * @see #canAccess(UUID, Level, BlockPos)
     * @see #canAccess(UUID, Level, BlockPos, BlockState, BlockEntity)
     * @see ISecurityUtils#canAccess(Player, Supplier, Supplier)
     * @see ISecurityUtils#canAccess(UUID, Supplier, Supplier, boolean)
     * @see ISecurityUtils#canAccessObject(Player, ISecurityObject)
     * @see ISecurityUtils#canAccessObject(UUID, ISecurityObject, boolean)
     * @see #canAccessOrDisplayError(Player, Level, BlockPos)
     * @see #canAccessOrDisplayError(Player, Level, BlockPos, BlockEntity)
     * @see #canAccessOrDisplayError(Player, Level, BlockPos, BlockState, BlockEntity)
     */
    default boolean canAccess(@Nullable UUID player, Level level, BlockPos pos, @Nullable BlockEntity blockEntity) {
        return canAccess(player, level, pos, null, blockEntity);
    }

    /**
     * Checks if a player can access the block at the given location; validating that protection is enabled in the config.
     *
     * @param player      Player to check access for.
     * @param level       Level to query.
     * @param pos         Position in level.
     * @param state       The block state, if known, or null if unknown.
     * @param blockEntity The block entity, if known, or null if unknown.
     *
     * @return {@code true} if the player can access the block at the given location. If the player is {@code null} this will return {@code true} if the block's security
     * is {@link SecurityMode#PUBLIC}.
     *
     * @implNote This method assumes that if the security is {@link SecurityMode#TRUSTED} and there is a player and {@code isClient} is {@code true}, then the player can
     * access the {@link ISecurityObject security object}. This is done because the list of trusted players is not currently synced to all clients.
     * @see #canAccess(Player, Level, BlockPos)
     * @see #canAccess(Player, Level, BlockPos, BlockEntity)
     * @see #canAccess(Player, Level, BlockPos, BlockState, BlockEntity)
     * @see #canAccess(UUID, Level, BlockPos)
     * @see #canAccess(UUID, Level, BlockPos, BlockEntity)
     * @see ISecurityUtils#canAccess(Player, Supplier, Supplier)
     * @see ISecurityUtils#canAccess(UUID, Supplier, Supplier, boolean)
     * @see ISecurityUtils#canAccessObject(Player, ISecurityObject)
     * @see ISecurityUtils#canAccessObject(UUID, ISecurityObject, boolean)
     * @see #canAccessOrDisplayError(Player, Level, BlockPos)
     * @see #canAccessOrDisplayError(Player, Level, BlockPos, BlockEntity)
     * @see #canAccessOrDisplayError(Player, Level, BlockPos, BlockState, BlockEntity)
     */
    boolean canAccess(@Nullable UUID player, Level level, BlockPos pos, @Nullable BlockState state, @Nullable BlockEntity blockEntity);

    /**
     * Gets the owner of the block at the given location or {@code null} if there is no owner or the block doesn't expose an {@link IOwnerObject}.
     *
     * @param level Level to query.
     * @param pos   Position in level.
     *
     * @return UUID of the block's owner or {@code null} if there is no owner.
     *
     * @see IOwnerObject#getOwnerUUID()
     * @see #getOwnerUUID(Level, BlockPos, BlockEntity)
     * @see #getOwnerUUID(Level, BlockPos, BlockState, BlockEntity)
     */
    @Nullable
    default UUID getOwnerUUID(Level level, BlockPos pos) {
        return getOwnerUUID(level, pos, null, null);
    }

    /**
     * Gets the owner of the block at the given location or {@code null} if there is no owner or the block doesn't expose an {@link IOwnerObject}.
     *
     * @param level       Level to query.
     * @param pos         Position in level.
     * @param blockEntity The block entity, if known, or null if unknown.
     *
     * @return UUID of the block's owner or {@code null} if there is no owner.
     *
     * @see IOwnerObject#getOwnerUUID()
     * @see #getOwnerUUID(Level, BlockPos)
     * @see #getOwnerUUID(Level, BlockPos, BlockState, BlockEntity)
     */
    @Nullable
    default UUID getOwnerUUID(Level level, BlockPos pos, @Nullable BlockEntity blockEntity) {
        return getOwnerUUID(level, pos, null, blockEntity);
    }

    /**
     * Gets the owner of the block at the given location or {@code null} if there is no owner or the block doesn't expose an {@link IOwnerObject}.
     *
     * @param level       Level to query.
     * @param pos         Position in level.
     * @param state       The block state, if known, or null if unknown.
     * @param blockEntity The block entity, if known, or null if unknown.
     *
     * @return UUID of the block's owner or {@code null} if there is no owner.
     *
     * @see IOwnerObject#getOwnerUUID()
     * @see #getOwnerUUID(Level, BlockPos)
     * @see #getOwnerUUID(Level, BlockPos, BlockEntity)
     */
    @Nullable
    default UUID getOwnerUUID(Level level, BlockPos pos, @Nullable BlockState state, @Nullable BlockEntity blockEntity) {
        IOwnerObject ownerObject = level.getCapability(ownerCapability(), pos, state, blockEntity, null);
        return ownerObject == null ? null : ownerObject.getOwnerUUID();
    }

    /**
     * Gets the "effective" security mode for a block at the given location. If it does not expose a {@link ISecurityObject security object}, then the security will be
     * assumed to be {@link SecurityMode#PUBLIC} <em>unless</em> an {@link IOwnerObject} is exposed, in which case the security will be assumed
     * {@link SecurityMode#PRIVATE} if protection is enabled.
     * <br><br>
     * When a {@link ISecurityObject security object} is exposed; this method is <em>different</em> from just querying {@link ISecurityObject#getSecurityMode()} as this
     * method takes into account whether protection is disabled in the config and whether the owner of the {@link ISecurityObject} has their security frequency configured
     * to override the access level of less restrictive {@link ISecurityObject security objects}.
     *
     * @param level Level to query.
     * @param pos   Position in level.
     *
     * @return Effective security mode.
     *
     * @implNote If the block doesn't expose a {@link ISecurityObject security object}, then the returned mode is {@link SecurityMode#PUBLIC}
     * @see #getSecurityMode(Level, BlockPos, BlockEntity)
     * @see #getSecurityMode(Level, BlockPos, BlockState, BlockEntity)
     * @see ISecurityUtils#getSecurityMode(Supplier, Supplier, boolean)
     * @see ISecurityUtils#getEffectiveSecurityMode(ISecurityObject, boolean)
     */
    default SecurityMode getSecurityMode(Level level, BlockPos pos) {
        return getSecurityMode(level, pos, null, null);
    }

    /**
     * Gets the "effective" security mode for a block at the given location. If it does not expose a {@link ISecurityObject security object}, then the security will be
     * assumed to be {@link SecurityMode#PUBLIC} <em>unless</em> an {@link IOwnerObject} is exposed, in which case the security will be assumed
     * {@link SecurityMode#PRIVATE} if protection is enabled.
     * <br><br>
     * When a {@link ISecurityObject security object} is exposed; this method is <em>different</em> from just querying {@link ISecurityObject#getSecurityMode()} as this
     * method takes into account whether protection is disabled in the config and whether the owner of the {@link ISecurityObject} has their security frequency configured
     * to override the access level of less restrictive {@link ISecurityObject security objects}.
     *
     * @param level       Level to query.
     * @param pos         Position in level.
     * @param blockEntity The block entity, if known, or null if unknown.
     *
     * @return Effective security mode.
     *
     * @implNote If the block doesn't expose a {@link ISecurityObject security object}, then the returned mode is {@link SecurityMode#PUBLIC}
     * @see #getSecurityMode(Level, BlockPos)
     * @see #getSecurityMode(Level, BlockPos, BlockState, BlockEntity)
     * @see ISecurityUtils#getSecurityMode(Supplier, Supplier, boolean)
     * @see ISecurityUtils#getEffectiveSecurityMode(ISecurityObject, boolean)
     */
    default SecurityMode getSecurityMode(Level level, BlockPos pos, @Nullable BlockEntity blockEntity) {
        return getSecurityMode(level, pos, null, blockEntity);
    }

    /**
     * Gets the "effective" security mode for a block at the given location. If it does not expose a {@link ISecurityObject security object}, then the security will be
     * assumed to be {@link SecurityMode#PUBLIC} <em>unless</em> an {@link IOwnerObject} is exposed, in which case the security will be assumed
     * {@link SecurityMode#PRIVATE} if protection is enabled.
     * <br><br>
     * When a {@link ISecurityObject security object} is exposed; this method is <em>different</em> from just querying {@link ISecurityObject#getSecurityMode()} as this
     * method takes into account whether protection is disabled in the config and whether the owner of the {@link ISecurityObject} has their security frequency configured
     * to override the access level of less restrictive {@link ISecurityObject security objects}.
     *
     * @param level       Level to query.
     * @param pos         Position in level.
     * @param state       The block state, if known, or null if unknown.
     * @param blockEntity The block entity, if known, or null if unknown.
     *
     * @return Effective security mode.
     *
     * @implNote If the block doesn't expose a {@link ISecurityObject security object}, then the returned mode is {@link SecurityMode#PUBLIC}
     * @see #getSecurityMode(Level, BlockPos)
     * @see #getSecurityMode(Level, BlockPos, BlockEntity)
     * @see ISecurityUtils#getSecurityMode(Supplier, Supplier, boolean)
     * @see ISecurityUtils#getEffectiveSecurityMode(ISecurityObject, boolean)
     */
    SecurityMode getSecurityMode(Level level, BlockPos pos, @Nullable BlockState state, @Nullable BlockEntity blockEntity);

    /**
     * Helper method to check if a given player can access the block at the given location; and if they can't display a server side access error to the player.
     *
     * @param player Player to check access for.
     * @param level  Level to query.
     * @param pos    Position in level.
     *
     * @return {@code true} if the player can access the block at the given location.
     *
     * @implNote This method assumes that if the security is {@link SecurityMode#TRUSTED} and there is a clientside player, then the player can access the
     * {@link ISecurityObject security object}. This is done because the list of trusted players is not currently synced to all clients.
     * @see #canAccess(Player, Level, BlockPos)
     */
    default boolean canAccessOrDisplayError(Player player, Level level, BlockPos pos) {
        return canAccessOrDisplayError(player, level, pos, null, null);
    }

    /**
     * Helper method to check if a given player can access the block at the given location; and if they can't display a server side access error to the player.
     *
     * @param player      Player to check access for.
     * @param level       Level to query.
     * @param pos         Position in level.
     * @param blockEntity The block entity, if known, or null if unknown.
     *
     * @return {@code true} if the player can access the block at the given location.
     *
     * @implNote This method assumes that if the security is {@link SecurityMode#TRUSTED} and there is a clientside player, then the player can access the
     * {@link ISecurityObject security object}. This is done because the list of trusted players is not currently synced to all clients.
     * @see #canAccess(Player, Level, BlockPos)
     */
    default boolean canAccessOrDisplayError(Player player, Level level, BlockPos pos, @Nullable BlockEntity blockEntity) {
        return canAccessOrDisplayError(player, level, pos, null, blockEntity);
    }

    /**
     * Helper method to check if a given player can access the block at the given location; and if they can't display a server side access error to the player.
     *
     * @param player      Player to check access for.
     * @param level       Level to query.
     * @param pos         Position in level.
     * @param state       The block state, if known, or null if unknown.
     * @param blockEntity The block entity, if known, or null if unknown.
     *
     * @return {@code true} if the player can access the block at the given location.
     *
     * @implNote This method assumes that if the security is {@link SecurityMode#TRUSTED} and there is a clientside player, then the player can access the
     * {@link ISecurityObject security object}. This is done because the list of trusted players is not currently synced to all clients.
     * @see #canAccess(Player, Level, BlockPos)
     */
    default boolean canAccessOrDisplayError(Player player, Level level, BlockPos pos, @Nullable BlockState state, @Nullable BlockEntity blockEntity) {
        if (canAccess(player, level, pos, state, blockEntity)) {
            return true;
        } else if (!level.isClientSide) {
            //Display no access from server side
            ISecurityUtils.INSTANCE.displayNoAccess(player);
        }
        return false;
    }
}