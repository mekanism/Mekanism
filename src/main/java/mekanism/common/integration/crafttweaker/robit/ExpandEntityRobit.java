package mekanism.common.integration.crafttweaker.robit;

import com.blamejared.crafttweaker.api.annotation.ZenRegister;
import com.blamejared.crafttweaker_annotations.annotations.NativeTypeRegistration;
import mekanism.api.MekanismRegistries;
import mekanism.api.robit.RobitSkin;
import mekanism.common.entity.EntityRobit;
import mekanism.common.integration.crafttweaker.CrTConstants;
import mekanism.common.registries.MekanismRobitSkins;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.player.Player;
import org.jspecify.annotations.Nullable;
import org.openzen.zencode.java.ZenCodeType;

@ZenRegister
@NativeTypeRegistration(value = EntityRobit.class, zenCodeName = CrTConstants.CLASS_ROBIT)
public class ExpandEntityRobit {

    /// Gets the skin this Robit currently is using.
    ///
    /// @return Current skin.
    @ZenCodeType.Method
    @ZenCodeType.Getter("skin")
    public static RobitSkin getSkin(EntityRobit internal) {
        return MekanismRobitSkins.lookup(internal.level().registryAccess(), internal.getSkinId()).skin();
    }

    /// Gets the name of the skin this Robit currently is using.
    ///
    /// @return Current skin.
    @ZenCodeType.Method
    @ZenCodeType.Getter("skinName")
    public static Identifier getSkinName(EntityRobit internal) {
        //Note: We perform a lookup in case the skin is invalid and thus would fall back to being the base skin
        return MekanismRobitSkins.lookup(internal.level().registryAccess(), internal.getSkinId()).identifier();
    }

    /// Tries to set this Robit's skin to the given skin.
    ///
    /// @param skin   The skin to set.
    /// @param player The player who is trying to set the skin of the robit, or null if the player is unknown.
    ///
    /// @return `true` if the Robit's skin was set, or false if the player does not have security clearance or doesn't have the skin unlocked
    /// ([RobitSkin#isUnlocked(Player)]).
    ///
    /// @implNote This method only syncs changes from the server side, so in general should only be called from the server side except for uses internal to the Robit.
    @ZenCodeType.Method
    public static boolean setSkin(EntityRobit internal, RobitSkin skin, @Nullable @ZenCodeType.Nullable Player player) {
        Identifier skinName = internal.level().registryAccess().lookupOrThrow(MekanismRegistries.Keys.ROBIT_SKINS).getKeyOrNull(skin);
        if (skinName == null) {
            throw new IllegalArgumentException("Unregistered robit skin");
        }
        return internal.setSkin(ResourceKey.create(MekanismRegistries.Keys.ROBIT_SKINS, skinName), player);
    }

    /// Tries to set this Robit's skin to the given skin.
    ///
    /// @param skin   The skin to set.
    /// @param player The player who is trying to set the skin of the robit, or null if the player is unknown.
    ///
    /// @return `true` if the Robit's skin was set, or false if the player does not have security clearance or doesn't have the skin unlocked
    /// ([RobitSkin#isUnlocked(Player)]).
    ///
    /// @implNote This method only syncs changes from the server side, so in general should only be called from the server side except for uses internal to the Robit.
    @ZenCodeType.Method
    public static boolean setSkin(EntityRobit internal, Identifier skin, @Nullable @ZenCodeType.Nullable Player player) {
        ResourceKey<RobitSkin> skinKey = ResourceKey.create(MekanismRegistries.Keys.ROBIT_SKINS, skin);
        if (!internal.level().registryAccess().lookupOrThrow(MekanismRegistries.Keys.ROBIT_SKINS).containsKey(skinKey)) {
            throw new IllegalArgumentException("Unknown robit skin with name: " + skin);
        }
        return internal.setSkin(skinKey, player);
    }
}