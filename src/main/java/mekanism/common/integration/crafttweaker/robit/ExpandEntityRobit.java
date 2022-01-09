package mekanism.common.integration.crafttweaker.robit;

import com.blamejared.crafttweaker.api.annotation.ZenRegister;
import com.blamejared.crafttweaker_annotations.annotations.NativeTypeRegistration;
import mekanism.api.robit.RobitSkin;
import mekanism.common.entity.EntityRobit;
import mekanism.common.integration.crafttweaker.CrTConstants;
import net.minecraft.world.entity.player.Player;
import org.openzen.zencode.java.ZenCodeType;

@ZenRegister
@NativeTypeRegistration(value = EntityRobit.class, zenCodeName = CrTConstants.CLASS_ROBIT)
public class ExpandEntityRobit {

    /**
     * Gets the skin this Robit currently is using.
     *
     * @return Current skin.
     */
    @ZenCodeType.Method
    @ZenCodeType.Getter("skin")
    public static RobitSkin getSkin(EntityRobit internal) {
        return internal.getSkin();
    }

    /**
     * Tries to set this Robit's skin to the given skin.
     *
     * @param skin   The skin to set.
     * @param player The player who is trying to set the skin of the robit, or null if the player is unknown.
     *
     * @return {@code true} if the Robit's skin was set, or false if the player does not have security clearance or doesn't have the skin unlocked ({@link
     * RobitSkin#isUnlocked(Player)}).
     *
     * @implNote This method only syncs changes from the server side, so in general should only be called from the server side except for uses internal to the Robit.
     */
    @ZenCodeType.Method
    public static boolean setSkin(EntityRobit internal, RobitSkin skin, @ZenCodeType.Nullable Player player) {
        return internal.setSkin(skin, player);
    }
}