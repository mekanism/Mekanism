package mekanism.api.security;

import java.util.UUID;
import java.util.function.Function;
import java.util.function.Supplier;
import mekanism.api.MekanismAPI;
import mekanism.api.annotations.NothingNullByDefault;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.Nullable;

/**
 * Utility class for interacting with Mekanism's security system.
 *
 * @see ISecurityUtils#INSTANCE
 * @see ITypedSecurityUtils
 * @see IBlockSecurityUtils
 * @since 10.2.1
 */
@NothingNullByDefault
public interface ISecurityUtils {

    /**
     * Provides access to Mekanism's implementation of {@link ISecurityUtils}.
     *
     * @since 10.4.0
     */
    ISecurityUtils INSTANCE = MekanismAPI.getService(ISecurityUtils.class);

    /**
     * Checks if a player can access the given capability provider; validating that protection is enabled in the config. Additionally, this method also checks to see if
     * operators bypassing security is enabled in the config and if it is, provides access to the player if they are an operator.
     *
     * @param player           Player to check access for.
     * @param securityProvider Supplier to get the security capability to check. Can return {@code null} if there is no security.
     * @param ownerProvider    Supplier to get the owner capability to check. Can return {@code null} if there is no owner.
     *
     * @return {@code true} if the player can access the given provider.
     *
     * @implNote This method assumes that if the security is {@link SecurityMode#TRUSTED} and there is a clientside player, then the player can access the
     * {@link ISecurityObject security object}. This is done because the list of trusted players is not currently synced to all clients.
     * @see #canAccess(UUID, Supplier, Supplier, boolean)
     * @see #canAccessObject(Player, ISecurityObject)
     * @see #canAccessObject(UUID, ISecurityObject, boolean)
     * @since 10.5.0
     */
    boolean canAccess(Player player, Supplier<@Nullable ISecurityObject> securityProvider, Supplier<@Nullable IOwnerObject> ownerProvider);

    /**
     * Checks if a player can access the given capability provider; validating that protection is enabled in the config. Additionally, this method also checks to see if
     * operators bypassing security is enabled in the config and if it is, provides access to the player if they are an operator.
     *
     * @param player           Player to check access for.
     * @param provider         The provider to get the capabilities for.
     * @param securityProvider Supplier to get the security capability to check. Can return {@code null} if there is no security.
     * @param ownerProvider    Supplier to get the owner capability to check. Can return {@code null} if there is no owner.
     *
     * @return {@code true} if the player can access the given provider.
     *
     * @implNote This method assumes that if the security is {@link SecurityMode#TRUSTED} and there is a clientside player, then the player can access the
     * {@link ISecurityObject security object}. This is done because the list of trusted players is not currently synced to all clients.
     * @see #canAccess(UUID, Supplier, Supplier, boolean)
     * @see #canAccessObject(Player, ISecurityObject)
     * @see #canAccessObject(UUID, ISecurityObject, boolean)
     * @since 10.5.18
     */
    <PROVIDER> boolean canAccess(Player player, PROVIDER provider, Function<PROVIDER, @Nullable ISecurityObject> securityProvider,
          Function<PROVIDER, @Nullable IOwnerObject> ownerProvider);

    /**
     * Checks if a player can access the given security object; validating that protection is enabled in the config. Additionally, this method also checks to see if
     * operators bypassing security is enabled in the config and if it is, provides access to the player if they are an operator.
     *
     * @param player         Player to check access for.
     * @param securityObject Security object to check access of.
     *
     * @return {@code true} if the player can access the given security object.
     *
     * @implNote This method assumes that if the security is {@link SecurityMode#TRUSTED} and there is a clientside player, then the player can access the
     * {@link ISecurityObject security object}. This is done because the list of trusted players is not currently synced to all clients.
     * @see #canAccess(Player, Supplier, Supplier)
     * @see #canAccess(UUID, Supplier, Supplier, boolean)
     * @see #canAccessObject(UUID, ISecurityObject, boolean)
     */
    boolean canAccessObject(Player player, ISecurityObject securityObject);

    /**
     * Checks if a player can access the given capability provider; validating that protection is enabled in the config.
     *
     * @param player           Player to check access for.
     * @param securityProvider Supplier to get the security capability to check. Can return {@code null} if there is no security.
     * @param ownerProvider    Supplier to get the owner capability to check. Can return {@code null} if there is no owner.
     * @param isClient         {@code true} if this method is being run clientside.
     *
     * @return {@code true} if the player can access the given provider. If the player is {@code null} this will return {@code true} if the provider's security is
     * {@link SecurityMode#PUBLIC}.
     *
     * @implNote This method assumes that if the security is {@link SecurityMode#TRUSTED} and there is a player and {@code isClient} is {@code true}, then the player can
     * access the {@link ISecurityObject security object}. This is done because the list of trusted players is not currently synced to all clients.
     * @see #canAccess(Player, Supplier, Supplier)
     * @see #canAccessObject(Player, ISecurityObject)
     * @see #canAccessObject(UUID, ISecurityObject, boolean)
     * @since 10.5.0
     */
    boolean canAccess(@Nullable UUID player, Supplier<@Nullable ISecurityObject> securityProvider, Supplier<@Nullable IOwnerObject> ownerProvider, boolean isClient);

    /**
     * Checks if a player can access the given capability provider; validating that protection is enabled in the config.
     *
     * @param player           Player to check access for.
     * @param provider         The provider to get the capabilities for.
     * @param securityProvider Supplier to get the security capability to check. Can return {@code null} if there is no security.
     * @param ownerProvider    Supplier to get the owner capability to check. Can return {@code null} if there is no owner.
     * @param isClient         {@code true} if this method is being run clientside.
     *
     * @return {@code true} if the player can access the given provider. If the player is {@code null} this will return {@code true} if the provider's security is
     * {@link SecurityMode#PUBLIC}.
     *
     * @implNote This method assumes that if the security is {@link SecurityMode#TRUSTED} and there is a player and {@code isClient} is {@code true}, then the player can
     * access the {@link ISecurityObject security object}. This is done because the list of trusted players is not currently synced to all clients.
     * @see #canAccess(Player, Supplier, Supplier)
     * @see #canAccessObject(Player, ISecurityObject)
     * @see #canAccessObject(UUID, ISecurityObject, boolean)
     * @since 10.5.18
     */
    <PROVIDER> boolean canAccess(@Nullable UUID player, PROVIDER provider, Function<PROVIDER, @Nullable ISecurityObject> securityProvider,
          Function<PROVIDER, @Nullable IOwnerObject> ownerProvider, boolean isClient);

    /**
     * Checks if a player can access the given security object; validating that protection is enabled in the config.
     *
     * @param player         Player to check access for.
     * @param securityObject Security object to check access of.
     * @param isClient       {@code true} if this method is being run clientside.
     *
     * @return {@code true} if the player can access the given security object. If the player is {@code null} this will return {@code true} if the object's security is
     * {@link SecurityMode#PUBLIC}.
     *
     * @implNote This method assumes that if the security is {@link SecurityMode#TRUSTED} and there is a player and {@code isClient} is {@code true}, then the player can
     * access the {@link ISecurityObject security object}. This is done because the list of trusted players is not currently synced to all clients.
     * @see #canAccess(Player, Supplier, Supplier)
     * @see #canAccess(UUID, Supplier, Supplier, boolean)
     * @see #canAccessObject(Player, ISecurityObject)
     */
    boolean canAccessObject(@Nullable UUID player, ISecurityObject securityObject, boolean isClient);

    /**
     * Helper to check if a given override security mode is more restrictive than the base mode. The cases where this is true are:
     * <br>
     * If the base is {@link SecurityMode#PUBLIC} and the override is either {@link SecurityMode#TRUSTED} or {@link SecurityMode#PRIVATE}.
     * <br>
     * If the base is {@link SecurityMode#TRUSTED} and the override is {@link SecurityMode#PRIVATE}.
     *
     * @param base       Base security mode.
     * @param overridden Override security mode.
     *
     * @return {@code true} if the override security mode is more restrictive than the base security mode.
     */
    boolean moreRestrictive(SecurityMode base, SecurityMode overridden);

    /**
     * Gets the "effective" security mode for a given provider. If no provider is given, or it does not expose a {@link ISecurityObject security object}, then the
     * security will be assumed to be {@link SecurityMode#PUBLIC} <em>unless</em> an {@link IOwnerObject} is exposed, in which case the security will be assumed
     * {@link SecurityMode#PRIVATE} if protection is enabled.
     * <br><br>
     * When a {@link ISecurityObject security object} is exposed; this method is <em>different</em> from just querying {@link ISecurityObject#getSecurityMode()} as this
     * method takes into account whether protection is disabled in the config and whether the owner of the {@link ISecurityObject} has their security frequency configured
     * to override the access level of less restrictive {@link ISecurityObject security objects}.
     *
     * @param securityProvider Supplier to get the security capability to check. Can return {@code null} if there is no security.
     * @param ownerProvider    Supplier to get the owner capability to check. Can return {@code null} if there is no owner.
     * @param isClient         {@code true} if this method is being run clientside.
     *
     * @return Effective security mode.
     *
     * @implNote If the provider is {@code null} or doesn't expose a {@link ISecurityObject security object}, then the returned mode is {@link SecurityMode#PUBLIC}
     * @see #getEffectiveSecurityMode(ISecurityObject, boolean)
     * @since 10.5.0
     */
    SecurityMode getSecurityMode(Supplier<@Nullable ISecurityObject> securityProvider, Supplier<@Nullable IOwnerObject> ownerProvider, boolean isClient);

    /**
     * Gets the "effective" security mode for a given provider. If no provider is given, or it does not expose a {@link ISecurityObject security object}, then the
     * security will be assumed to be {@link SecurityMode#PUBLIC} <em>unless</em> an {@link IOwnerObject} is exposed, in which case the security will be assumed
     * {@link SecurityMode#PRIVATE} if protection is enabled.
     * <br><br>
     * When a {@link ISecurityObject security object} is exposed; this method is <em>different</em> from just querying {@link ISecurityObject#getSecurityMode()} as this
     * method takes into account whether protection is disabled in the config and whether the owner of the {@link ISecurityObject} has their security frequency configured
     * to override the access level of less restrictive {@link ISecurityObject security objects}.
     *
     * @param provider         The provider to get the capabilities for.
     * @param securityProvider Supplier to get the security capability to check. Can return {@code null} if there is no security.
     * @param ownerProvider    Supplier to get the owner capability to check. Can return {@code null} if there is no owner.
     * @param isClient         {@code true} if this method is being run clientside.
     *
     * @return Effective security mode.
     *
     * @implNote If the provider is {@code null} or doesn't expose a {@link ISecurityObject security object}, then the returned mode is {@link SecurityMode#PUBLIC}
     * @see #getEffectiveSecurityMode(ISecurityObject, boolean)
     * @since 10.5.18
     */
    <PROVIDER> SecurityMode getSecurityMode(PROVIDER provider, Function<PROVIDER, @Nullable ISecurityObject> securityProvider,
          Function<PROVIDER, @Nullable IOwnerObject> ownerProvider, boolean isClient);

    /**
     * Gets the "effective" security mode for a given object. This is <em>different</em> from just querying {@link ISecurityObject#getSecurityMode()} as this method takes
     * into account whether protection is disabled in the config and whether the owner of the {@link ISecurityObject} has their security frequency configured to override
     * the access level of less restrictive {@link ISecurityObject security objects}.
     *
     * @param securityObject Security object to get the effective security mode of.
     * @param isClient       {@code true} if this method is being run clientside.
     *
     * @return Effective security mode.
     *
     * @see #getSecurityMode(Supplier, Supplier, boolean)
     */
    SecurityMode getEffectiveSecurityMode(ISecurityObject securityObject, boolean isClient);

    /**
     * Displays an error message to the given player indicating that the player does not have access to what they were trying to do.
     *
     * @param player Player to send the access error to.
     */
    void displayNoAccess(Player player);
}