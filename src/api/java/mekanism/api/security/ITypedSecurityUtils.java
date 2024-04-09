package mekanism.api.security;

import java.util.UUID;
import java.util.function.Function;
import java.util.function.Supplier;
import mekanism.api.annotations.NothingNullByDefault;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.Nullable;

/**
 * Base utility class for interacting with Mekanism's security system using a specific provider type.
 *
 * @see IEntitySecurityUtils
 * @see IItemSecurityUtils
 * @since 10.5.0
 */
@NothingNullByDefault
public interface ITypedSecurityUtils<PROVIDER> {

    /**
     * {@return the owner capability for the given provider or null if the provider doesn't expose an owner capability}
     *
     * @param provider Capability provider to get the capability for.
     */
    @Nullable
    @Contract("null -> null")
    IOwnerObject ownerCapability(@Nullable PROVIDER provider);

    /**
     * {@return the security capability for the given provider or null if the provider doesn't expose a security capability}
     *
     * @param provider Capability provider to get the capability for.
     */
    @Nullable
    @Contract("null -> null")
    ISecurityObject securityCapability(@Nullable PROVIDER provider);

    /**
     * Checks if a player can access the given capability provider; validating that protection is enabled in the config. Additionally, this method also checks to see if
     * operators bypassing security is enabled in the config and if it is, provides access to the player if they are an operator.
     *
     * @param player   Player to check access for.
     * @param provider Capability provider to check access of.
     *
     * @return {@code true} if the player can access the given provider.
     *
     * @implNote This method assumes that if the security is {@link SecurityMode#TRUSTED} and there is a clientside player, then the player can access the
     * {@link ISecurityObject security object}. This is done because the list of trusted players is not currently synced to all clients.
     * @see #canAccess(UUID, Object, boolean)
     * @see ISecurityUtils#canAccess(Player, Object, Function, Function)
     * @see ISecurityUtils#canAccess(UUID, Object, Function, Function, boolean)
     * @see ISecurityUtils#canAccess(Player, Supplier, Supplier)
     * @see ISecurityUtils#canAccess(UUID, Supplier, Supplier, boolean)
     * @see ISecurityUtils#canAccessObject(Player, ISecurityObject)
     * @see ISecurityUtils#canAccessObject(UUID, ISecurityObject, boolean)
     * @see #canAccessOrDisplayError(Player, Object)
     */
    @Contract("_, null -> true")
    default boolean canAccess(Player player, @Nullable PROVIDER provider) {
        if (provider == null) {
            return true;
        }
        return ISecurityUtils.INSTANCE.canAccess(player, provider, this::securityCapability, this::ownerCapability);
    }

    /**
     * Checks if a player can access the given capability provider; validating that protection is enabled in the config.
     *
     * @param player   Player to check access for.
     * @param provider Capability provider to check access of.
     * @param isClient {@code true} if this method is being run clientside.
     *
     * @return {@code true} if the player can access the given provider. If the player is {@code null} this will return {@code true} if the provider's security is
     * {@link SecurityMode#PUBLIC}.
     *
     * @implNote This method assumes that if the security is {@link SecurityMode#TRUSTED} and there is a player and {@code isClient} is {@code true}, then the player can
     * access the {@link ISecurityObject security object}. This is done because the list of trusted players is not currently synced to all clients.
     * @see #canAccess(Player, Object)
     * @see ISecurityUtils#canAccess(Player, Object, Function, Function)
     * @see ISecurityUtils#canAccess(UUID, Object, Function, Function, boolean)
     * @see ISecurityUtils#canAccess(Player, Supplier, Supplier)
     * @see ISecurityUtils#canAccess(UUID, Supplier, Supplier, boolean)
     * @see ISecurityUtils#canAccessObject(Player, ISecurityObject)
     * @see ISecurityUtils#canAccessObject(UUID, ISecurityObject, boolean)
     * @see #canAccessOrDisplayError(Player, Object)
     */
    @Contract("_, null, _ -> true")
    default boolean canAccess(@Nullable UUID player, @Nullable PROVIDER provider, boolean isClient) {
        if (provider == null) {
            return true;
        }
        return ISecurityUtils.INSTANCE.canAccess(player, provider, this::securityCapability, this::ownerCapability, isClient);
    }

    /**
     * Gets the owner of the given provider or {@code null} if there is no owner or the provider doesn't expose an {@link IOwnerObject}.
     *
     * @param provider Capability provider.
     *
     * @return UUID of the provider or {@code null} if there is no owner.
     *
     * @see IOwnerObject#getOwnerUUID()
     */
    @Nullable
    @Contract("null -> null")
    default UUID getOwnerUUID(@Nullable PROVIDER provider) {
        if (provider == null) {
            return null;
        }
        IOwnerObject ownerObject = ownerCapability(provider);
        return ownerObject == null ? null : ownerObject.getOwnerUUID();
    }

    /**
     * Gets the "effective" security mode for a given provider. If no provider is given, or it does not expose a {@link ISecurityObject security object}, then the
     * security will be assumed to be {@link SecurityMode#PUBLIC} <em>unless</em> an {@link IOwnerObject} is exposed, in which case the security will be assumed
     * {@link SecurityMode#PRIVATE} if protection is enabled.
     * <br><br>
     * When a {@link ISecurityObject security object} is exposed; this method is <em>different</em> from just querying {@link ISecurityObject#getSecurityMode()} as this
     * method takes into account whether protection is disabled in the config and whether the owner of the {@link ISecurityObject} has their security frequency configured
     * to override the access level of less restrictive {@link ISecurityObject security objects}.
     *
     * @param provider Capability provider to get the effective security mode of.
     * @param isClient {@code true} if this method is being run clientside.
     *
     * @return Effective security mode.
     *
     * @implNote If the provider is {@code null} or doesn't expose a {@link ISecurityObject security object}, then the returned mode is {@link SecurityMode#PUBLIC}
     * @see ISecurityUtils#getSecurityMode(Supplier, Supplier, boolean)
     * @see ISecurityUtils#getSecurityMode(Object, Function, Function, boolean)
     * @see ISecurityUtils#getEffectiveSecurityMode(ISecurityObject, boolean)
     */
    default SecurityMode getSecurityMode(@Nullable PROVIDER provider, boolean isClient) {
        if (provider == null) {
            return SecurityMode.PUBLIC;
        }
        return ISecurityUtils.INSTANCE.getSecurityMode(provider, this::securityCapability, this::ownerCapability, isClient);
    }

    /**
     * Helper method to check if a given player can access the given capability provider; and if they can't display a server side access error to the player.
     *
     * @param player   Player to check access for.
     * @param provider Capability provider to check access of.
     *
     * @return {@code true} if the player can access the given provider.
     *
     * @implNote This method assumes that if the security is {@link SecurityMode#TRUSTED} and there is a clientside player, then the player can access the
     * {@link ISecurityObject security object}. This is done because the list of trusted players is not currently synced to all clients.
     * @see #canAccess(Player, Object)
     * @see ISecurityUtils#canAccess(Player, Supplier, Supplier)
     * @see ISecurityUtils#canAccess(UUID, Supplier, Supplier, boolean)
     */
    @Contract("_, null -> true")
    default boolean canAccessOrDisplayError(Player player, @Nullable PROVIDER provider) {
        if (canAccess(player, provider)) {
            return true;
        } else if (!player.level().isClientSide) {
            //Display no access from server side
            ISecurityUtils.INSTANCE.displayNoAccess(player);
        }
        return false;
    }
}