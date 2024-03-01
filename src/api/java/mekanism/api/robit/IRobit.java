package mekanism.api.robit;

import mekanism.api.annotations.NothingNullByDefault;
import mekanism.api.security.ISecurityObject;
import net.minecraft.core.GlobalPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.Nullable;

/**
 * Basic interface that Robit's implement to provide basic access to some methods by allowing mods to instance check the entity against this interface.
 */
@NothingNullByDefault
public interface IRobit extends ISecurityObject {

    /**
     * Gets the skin this Robit currently is using.
     *
     * @return Current skin.
     */
    ResourceKey<RobitSkin> getSkin();

    /**
     * Tries to set this Robit's skin to the given skin.
     *
     * @param skin   The skin to set.
     * @param player The player who is trying to set the skin of the robit, or null if the player is unknown.
     *
     * @return {@code true} if the Robit's skin was set, or false if the player does not have security clearance or doesn't have the skin unlocked
     * ({@link RobitSkin#isUnlocked(Player)}).
     *
     * @implNote This method only syncs changes from the server side, so in general should only be called from the server side except for uses internal to the Robit.
     */
    boolean setSkin(ResourceKey<RobitSkin> skin, @Nullable Player player);

    /**
     * Gets the position of the chargepad that the Robit considers to be its home.
     *
     * @return Global position containing the dimension and block position.
     *
     * @since 10.5.2
     */
    @Nullable
    GlobalPos getHome();
}