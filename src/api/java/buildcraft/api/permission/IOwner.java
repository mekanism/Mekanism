package buildcraft.api.permission;

import java.util.UUID;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.server.MinecraftServer;

/** Defines an owner -- this is an actual player in the world, or an offline player. */
public interface IOwner {
    /** Gets the player, if they actually exist on the server */
    @Nullable
    EntityPlayer getPlayer(MinecraftServer server);

    /** Gets the player's UUID */
    @Nonnull
    UUID getPlayerUUID();

    /** Gets the player's name. */
    @Nonnull
    String getPlayerName();
}
