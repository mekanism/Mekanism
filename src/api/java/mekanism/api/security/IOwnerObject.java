package mekanism.api.security;

import java.util.Objects;
import java.util.UUID;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Expose this as a capability on items, entities, or block entities to represent it is an object that can be "owned".
 *
 * @apiNote The exposed capability should not care about side, and in general will be interacted with via the null side.
 * <br><br>
 * It is assumed that exposed {@link ISecurityObject security objects} always will have a corresponding owner object exposed, but it is not safe to assume that just
 * because an exposed owner object is an instance of an {@link ISecurityObject} that the object actually has security. The only way to know for certain if it does is by
 * checking if the provider exposes a security capability.
 * @since 10.2.1
 */
public interface IOwnerObject {

    /**
     * Gets the owner of this object.
     *
     * @return UUID of this object's owner or {@code null} if the object is not currently owned.
     */
    @Nullable
    UUID getOwnerUUID();

    /**
     * Attempts to look up the last known name for the owner.
     *
     * @return Name of the owner or {@code null} if there is no owner or the name is unknown.
     */
    @Nullable
    String getOwnerName();

    /**
     * Sets the owner of this object to the given user.
     *
     * @param owner Owner or {@code null} to remove the current owner.
     *
     * @apiNote This method should not be called by addons unless it is on one of your own objects; for example to transfer the set owner from an item stack to an entity
     * when placing an entity.
     */
    void setOwnerUUID(@Nullable UUID owner);

    /**
     * Helper method to check if the given player is the owner of this object.
     *
     * @param player Player to check.
     *
     * @return {@code true} if the player is the owner, {@code false} if the player isn't the owner or there is no owner currently set.
     */
    default boolean ownerMatches(@NotNull Player player) {
        Objects.requireNonNull(player, "Player may not be null.");
        return player.getUUID().equals(getOwnerUUID());
    }
}