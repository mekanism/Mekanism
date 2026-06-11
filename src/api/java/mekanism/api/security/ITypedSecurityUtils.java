package mekanism.api.security;

import java.util.UUID;
import java.util.function.Function;
import java.util.function.Supplier;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.Contract;
import org.jspecify.annotations.Nullable;

/// Base utility class for interacting with Mekanism's security system using a specific provider type.
///
/// @see IEntitySecurityUtils
/// @see IItemSecurityUtils
/// @since 10.5.0
public interface ITypedSecurityUtils<PROVIDER> {

    /// {@return the owner capability for the given provider or null if the provider doesn't expose an owner capability}
    ///
    /// @param provider Capability provider to get the capability for.
    @Nullable
    @Contract("null -> null")
    IOwnerObject ownerCapability(@Nullable PROVIDER provider);

    /// {@return the security capability for the given provider or null if the provider doesn't expose a security capability}
    ///
    /// @param provider Capability provider to get the capability for.
    @Nullable
    @Contract("null -> null")
    ISecurityObject securityCapability(@Nullable PROVIDER provider);

    /// Checks if a player can access the given capability provider; validating that protection is enabled in the config. Additionally, this method also checks to see if
    /// operators bypassing security is enabled in the config and if it is, provides access to the player if they are an operator.
    ///
    /// @param player   Player to check access for.
    /// @param provider Capability provider to check access of.
    ///
    /// @return `true` if the player can access the given provider.
    ///
    /// @implNote This method assumes that if the security is [SecurityMode#TRUSTED] and there is a clientside player, then the player can access the [`security
    /// object`][ISecurityObject]. This is done because the list of trusted players is not currently synced to all clients.
    /// @see #canAccess(UUID, Object, boolean)
    /// @see ISecurityUtils#canAccess(Player, Object, Function, Function)
    /// @see ISecurityUtils#canAccess(UUID, Object, Function, Function, boolean)
    /// @see ISecurityUtils#canAccess(Player, Supplier, Supplier)
    /// @see ISecurityUtils#canAccess(UUID, Supplier, Supplier, boolean)
    /// @see ISecurityUtils#canAccessObject(Player, ISecurityObject)
    /// @see ISecurityUtils#canAccessObject(UUID, ISecurityObject, boolean)
    /// @see #canAccessOrDisplayError(Player, Object)
    @Contract("_, null -> true")
    default boolean canAccess(Player player, @Nullable PROVIDER provider) {
        if (provider == null) {
            return true;
        }
        return ISecurityUtils.INSTANCE.canAccess(player, provider, this::securityCapability, this::ownerCapability);
    }

    /// Checks if a player can access the given capability provider; validating that protection is enabled in the config.
    ///
    /// @param player         Player to check access for.
    /// @param provider       Capability provider to check access of.
    /// @param isClient`true` if this method is being run clientside.
    ///
    /// @return `true` if the player can access the given provider. If the player is `null` this will return `true` if the provider's security is [SecurityMode#PUBLIC].
    ///
    /// @implNote This method assumes that if the security is [SecurityMode#TRUSTED] and there is a player and `isClient` is `true`, then the player can access the
    /// [`security object`][ISecurityObject]. This is done because the list of trusted players is not currently synced to all clients.
    /// @see #canAccess(Player, Object)
    /// @see ISecurityUtils#canAccess(Player, Object, Function, Function)
    /// @see ISecurityUtils#canAccess(UUID, Object, Function, Function, boolean)
    /// @see ISecurityUtils#canAccess(Player, Supplier, Supplier)
    /// @see ISecurityUtils#canAccess(UUID, Supplier, Supplier, boolean)
    /// @see ISecurityUtils#canAccessObject(Player, ISecurityObject)
    /// @see ISecurityUtils#canAccessObject(UUID, ISecurityObject, boolean)
    /// @see #canAccessOrDisplayError(Player, Object)
    @Contract("_, null, _ -> true")
    default boolean canAccess(@Nullable UUID player, @Nullable PROVIDER provider, boolean isClient) {
        if (provider == null) {
            return true;
        }
        return ISecurityUtils.INSTANCE.canAccess(player, provider, this::securityCapability, this::ownerCapability, isClient);
    }

    /// Gets the owner of the given provider or `null` if there is no owner or the provider doesn't expose an [IOwnerObject].
    ///
    /// @param provider Capability provider.
    ///
    /// @return UUID of the provider or `null` if there is no owner.
    ///
    /// @see IOwnerObject#getOwnerUUID()
    @Nullable
    @Contract("null -> null")
    default UUID getOwnerUUID(@Nullable PROVIDER provider) {
        if (provider == null) {
            return null;
        }
        IOwnerObject ownerObject = ownerCapability(provider);
        return ownerObject == null ? null : ownerObject.getOwnerUUID();
    }

    /// Gets the "effective" security mode for a given provider. If no provider is given, or it does not expose a [`security object`][ISecurityObject], then the
    /// security will be assumed to be [SecurityMode#PUBLIC] _unless_ an [IOwnerObject] is exposed, in which case the security will be assumed
    /// [SecurityMode#PRIVATE] if protection is enabled.
    ///
    /// When a [`security object`][ISecurityObject] is exposed; this method is _different_ from just querying [ISecurityObject#getSecurityMode()] as this
    /// method takes into account whether protection is disabled in the config and whether the owner of the [ISecurityObject] has their security frequency configured
    /// to override the access level of less restrictive [`security objects`][ISecurityObject].
    ///
    /// @param provider Capability provider to get the effective security mode of.
    /// @param isClient`true` if this method is being run clientside.
    ///
    /// @return Effective security mode.
    ///
    /// @implNote If the provider is `null` or doesn't expose a [`security object`][ISecurityObject], then the returned mode is [SecurityMode#PUBLIC]
    /// @see ISecurityUtils#getSecurityMode(Supplier, Supplier, boolean)
    /// @see ISecurityUtils#getSecurityMode(Object, Function, Function, boolean)
    /// @see ISecurityUtils#getEffectiveSecurityMode(ISecurityObject, boolean)
    default SecurityMode getSecurityMode(@Nullable PROVIDER provider, boolean isClient) {
        if (provider == null) {
            return SecurityMode.PUBLIC;
        }
        return ISecurityUtils.INSTANCE.getSecurityMode(provider, this::securityCapability, this::ownerCapability, isClient);
    }

    /// Helper method to check if a given player can access the given capability provider; and if they can't display a server side access error to the player.
    ///
    /// @param player   Player to check access for.
    /// @param provider Capability provider to check access of.
    ///
    /// @return `true` if the player can access the given provider.
    ///
    /// @implNote This method assumes that if the security is [SecurityMode#TRUSTED] and there is a clientside player, then the player can access the
    /// [`security object`][ISecurityObject]. This is done because the list of trusted players is not currently synced to all clients.
    /// @see #canAccess(Player, Object)
    /// @see ISecurityUtils#canAccess(Player, Supplier, Supplier)
    /// @see ISecurityUtils#canAccess(UUID, Supplier, Supplier, boolean)
    @Contract("_, null -> true")
    default boolean canAccessOrDisplayError(Player player, @Nullable PROVIDER provider) {
        if (canAccess(player, provider)) {
            return true;
        } else if (!player.level().isClientSide()) {
            //Display no access from server side
            ISecurityUtils.INSTANCE.displayNoAccess(player);
        }
        return false;
    }
}