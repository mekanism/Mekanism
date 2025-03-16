package mekanism.common.integration.crafttweaker.robit;

import com.blamejared.crafttweaker.api.annotation.ZenRegister;
import com.blamejared.crafttweaker_annotations.annotations.NativeTypeRegistration;
import mekanism.api.MekanismAPI;
import mekanism.api.robit.RobitSkin;
import mekanism.common.entity.EntityRobit;
import mekanism.common.integration.crafttweaker.CrTConstants;
import mekanism.common.registries.MekanismRobitSkins;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
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
        return MekanismRobitSkins.lookup(internal.level().registryAccess(), internal.getSkin()).skin();
    }

    /**
     * Gets the name of the skin this Robit currently is using.
     *
     * @return Current skin.
     */
    @ZenCodeType.Method
    @ZenCodeType.Getter("skinName")
    public static ResourceLocation getSkinName(EntityRobit internal) {
        //Note: We perform a lookup in case the skin is invalid and thus would fall back to being the base skin
        return MekanismRobitSkins.lookup(internal.level().registryAccess(), internal.getSkin()).location();
    }

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
    @ZenCodeType.Method
    public static boolean setSkin(EntityRobit internal, RobitSkin skin, @ZenCodeType.Nullable Player player) {
        ResourceKey<Registry<RobitSkin>> registryName = MekanismAPI.ROBIT_SKIN_REGISTRY_NAME;
        ResourceLocation skinName = internal.level().registryAccess().registryOrThrow(registryName).getKeyOrNull(skin);
        if (skinName == null) {
            throw new IllegalArgumentException("Unregistered robit skin");
        }
        return internal.setSkin(ResourceKey.create(registryName, skinName), player);
    }

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
    @ZenCodeType.Method
    public static boolean setSkin(EntityRobit internal, ResourceLocation skin, @ZenCodeType.Nullable Player player) {
        ResourceKey<Registry<RobitSkin>> registryName = MekanismAPI.ROBIT_SKIN_REGISTRY_NAME;
        ResourceKey<RobitSkin> skinKey = ResourceKey.create(registryName, skin);
        if (!internal.level().registryAccess().registryOrThrow(registryName).containsKey(skinKey)) {
            throw new IllegalArgumentException("Unknown robit skin with name: " + skin);
        }
        return internal.setSkin(skinKey, player);
    }
}