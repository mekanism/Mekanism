package mekanism.api.security;

import java.util.Objects;
import java.util.UUID;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;
import org.jspecify.annotations.Nullable;

/// Expose this as a capability on items, entities, or block entities to represent it is an object that can be "owned".
///
/// @apiNote The exposed capability should not care about side, and in general will be interacted with via the null side.
///
/// It is assumed that exposed [`security objects`][ISecurityObject] always will have a corresponding owner object exposed, but it is not safe to assume that just because
/// an exposed owner object is an instance of an [ISecurityObject] that the object actually has security. The only way to know for certain if it does is by checking if
/// the provider exposes a security capability.
/// @since 10.2.1
public interface IOwnerObject {

    /// Gets the owner of this object.
    ///
    /// @return UUID of this object's owner or `null` if the object is not currently owned.
    @Nullable
    UUID getOwnerUUID();

    /// Attempts to look up the last known name for the owner.
    ///
    /// @return Name of the owner or `null` if there is no owner or the name is unknown.
    @Nullable
    String getOwnerName();

    /// Sets the owner of this object to the given user.
    ///
    /// @param owner       Owner or `null` to remove the current owner.
    /// @param transaction The current transaction context if any that this operation is part of.
    ///
    /// @implNote Only implementations for items currently support rolling back via the passed in transaction.
    /// @apiNote This method should not be called by addons unless it is on one of your own objects; for example to transfer the set owner from an item stack to an entity
    /// when placing an entity.
    /// @since 10.8.0
    void setOwnerUUID(@Nullable UUID owner, @Nullable TransactionContext transaction);

    /// Helper method to check if the given player is the owner of this object.
    ///
    /// @param player Player to check.
    ///
    /// @return `true` if the player is the owner, `false` if the player isn't the owner or there is no owner currently set.
    default boolean ownerMatches(Player player) {
        Objects.requireNonNull(player, "Player may not be null.");
        return player.getUUID().equals(getOwnerUUID());
    }
}