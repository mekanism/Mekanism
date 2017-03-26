package buildcraft.api.permission;

import com.mojang.authlib.GameProfile;

/** Defines an entity or tile that is owned by a specific player. */
public interface IPlayerOwned {
    GameProfile getOwner();
}
