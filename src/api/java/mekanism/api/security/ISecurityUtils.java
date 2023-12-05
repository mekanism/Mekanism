package mekanism.api.security;

import java.util.List;
import java.util.ServiceLoader;
import java.util.UUID;
import java.util.function.Function;
import java.util.function.Supplier;
import mekanism.api.annotations.NothingNullByDefault;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.Nullable;

/**
 * Utility class for interacting with Mekanism's security system.
 *
 * @see ISecurityUtils#INSTANCE
 * @since 10.2.1
 */
@NothingNullByDefault
public interface ISecurityUtils {

    /**
     * Provides access to Mekanism's implementation of {@link ISecurityUtils}.
     *
     * @since 10.4.0
     */
    ISecurityUtils INSTANCE = ServiceLoader.load(ISecurityUtils.class).findFirst().orElseThrow(() -> new IllegalStateException("No valid ServiceImpl for ISecurityUtils found"));

    /**
     * Checks if a player can access the given capability provider; validating that protection is enabled in the config. Additionally, this method also checks to see if
     * operators bypassing security is enabled in the config and if it is provides access to the player if they are an operator.
     *
     * @param player   Player to check access for.
     * @param provider Capability provider to check access of.
     *
     * @return {@code true} if the player can access the given provider.
     *
     * @implNote This method assumes that if the security is {@link SecurityMode#TRUSTED} and there is a clientside player, then the player can access the
     * {@link ISecurityObject security object}. This is done because the list of trusted players is not currently synced to all clients.
     * @see #canAccess(UUID, Object, boolean)
     * @see #canAccessObject(Player, ISecurityObject)
     * @see #canAccessObject(UUID, ISecurityObject, boolean)
     * @see #canAccessOrDisplayError(Player, Object)
     */
    boolean canAccess(Player player, Supplier<@Nullable ISecurityObject> securityProvider, Supplier<@Nullable IOwnerObject> ownerProvider);

    @Contract("_, null, _, _ -> true")
    default <OBJECT> boolean canAccess(Player player, @Nullable OBJECT provider, Function<OBJECT, @Nullable ISecurityObject> securityProvider,
          Function<OBJECT, @Nullable IOwnerObject> ownerProvider) {
        if (provider == null) {
            return true;
        }
        return canAccess(player, () -> securityProvider.apply(provider), () -> ownerProvider.apply(provider));
    }

    @Contract("_, null -> true")
    boolean canAccess(Player player, @Nullable Entity provider);

    //TODO - 1.20.2: List in docs the valid "types" for the provider
    boolean canAccess(Player player, ItemStack provider);

    boolean canAccess(Player player, Level level, BlockPos pos, @Nullable BlockEntity tile);

    boolean canAccess(@Nullable UUID player, Level level, BlockPos pos, @Nullable BlockEntity tile);

    /**
     * Checks if a player can access the given security object; validating that protection is enabled in the config. Additionally, this method also checks to see if
     * operators bypassing security is enabled in the config and if it is provides access to the player if they are an operator.
     *
     * @param player         Player to check access for.
     * @param securityObject Security object to check access of.
     *
     * @return {@code true} if the player can access the given security object.
     *
     * @implNote This method assumes that if the security is {@link SecurityMode#TRUSTED} and there is a clientside player, then the player can access the
     * {@link ISecurityObject security object}. This is done because the list of trusted players is not currently synced to all clients.
     * @see #canAccess(Player, Object)
     * @see #canAccess(UUID, Object, boolean)
     * @see #canAccessObject(UUID, ISecurityObject, boolean)
     * @see #canAccessOrDisplayError(Player, Object)
     */
    boolean canAccessObject(Player player, ISecurityObject securityObject);

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
     * @see #canAccessObject(Player, ISecurityObject)
     * @see #canAccessObject(UUID, ISecurityObject, boolean)
     * @see #canAccessOrDisplayError(Player, Object)
     */
    boolean canAccess(@Nullable UUID player, ItemStack provider, boolean isClient);//TODO: helpers for other types?

    boolean canAccess(@Nullable UUID player, Supplier<@Nullable ISecurityObject> securityProvider, Supplier<@Nullable IOwnerObject> ownerProvider, boolean isClient);

    default <OBJECT> boolean canAccess(@Nullable UUID player, @Nullable OBJECT provider, Function<OBJECT, @Nullable ISecurityObject> securityProvider,
          Function<OBJECT, @Nullable IOwnerObject> ownerProvider, boolean isClient) {
        if (provider == null) {
            return true;
        }
        return canAccess(player, () -> securityProvider.apply(provider), () -> ownerProvider.apply(provider), isClient);
    }

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
     * @see #canAccess(Player, Object)
     * @see #canAccess(UUID, Object, boolean)
     * @see #canAccessObject(Player, ISecurityObject)
     * @see #canAccessOrDisplayError(Player, Object)
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
     * Gets the owner of the given stack or {@code null} if there is no owner or the stack doesn't expose an {@link IOwnerObject}.
     *
     * @param stack Capability provider.
     *
     * @return UUID of the provider or {@code null} if there is no owner.
     *
     * @see IOwnerObject#getOwnerUUID()
     */
    @Nullable
    UUID getOwnerUUID(ItemStack stack);

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
     * @implNote If the provider is {@code null} or doesn't expose a {@link ISecurityObject security object}, then
     * @see #getEffectiveSecurityMode(ISecurityObject, boolean)
     */
    SecurityMode getSecurityMode(Supplier<@Nullable ISecurityObject> securityProvider, Supplier<@Nullable IOwnerObject> ownerProvider, boolean isClient);

    SecurityMode getSecurityMode(Level level, BlockPos pos, @Nullable BlockEntity tile);

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
     * @see #getSecurityMode(Object, boolean)
     */
    SecurityMode getEffectiveSecurityMode(ISecurityObject securityObject, boolean isClient);

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
     */
    @Contract("_, null -> true")
    default boolean canAccessOrDisplayError(Player player, @Nullable Entity provider) {
        return canAccessOrDisplayError(player, canAccess(player, provider));
    }

    default boolean canAccessOrDisplayError(Player player, ItemStack provider) {
        return canAccessOrDisplayError(player, canAccess(player, provider));
    }

    default boolean canAccessOrDisplayError(Player player, Level level, BlockPos pos, @Nullable BlockEntity tile) {
        return canAccessOrDisplayError(player, canAccess(player, level, pos, tile));
    }

    private boolean canAccessOrDisplayError(Player player, boolean canAccess) {
        if (canAccess) {
            return true;
        } else if (!player.level().isClientSide) {
            //Display no access from server side
            displayNoAccess(player);
        }
        return false;
    }

    /**
     * Displays an error message to the given player indicating that the player does not have access to what they were trying to do.
     *
     * @param player Player to send the access error to.
     */
    void displayNoAccess(Player player);

    /**
     * Adds any owner and security data that the given stack has to the passed in list of tooltips. If the stack does not expose an owner or any security data then the
     * corresponding data will not be added.
     *
     * @param stack   Stack to retrieve the owner and any security data from.
     * @param tooltip List of components to add extra tooltips to.
     *
     * @apiNote While this method won't crash if called on the server it won't render quite right due to not having access to the player, so it is best to only call this
     * on the client.
     */
    void addSecurityTooltip(ItemStack stack, List<Component> tooltip);
}