package mekanism.api.robit;

import com.mojang.serialization.MapCodec;
import java.util.List;
import mekanism.api.MekanismAPI;
import mekanism.api.annotations.NothingNullByDefault;
import mekanism.api.text.TextComponentUtil;
import net.minecraft.Util;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.common.data.DatapackBuiltinEntriesProvider;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Represents a skin for a robit. Register these during datagen using {@link DatapackBuiltinEntriesProvider}.
 * <br>
 * See also the <a href="https://github.com/mekanism/Mekanism/wiki/Robit-Skins">Mekanism GitHub wiki</a> for the syntax of creating these manually.
 */
@NothingNullByDefault
public interface RobitSkin {

    /**
     * @return the codec which serializes and deserializes this {@link RobitSkin}.
     *
     * @implNote The returned codec should be registered in the {@link MekanismAPI#ROBIT_SKIN_SERIALIZER_REGISTRY robit skin serializer registry}.
     * @since 10.4.0
     */
    MapCodec<? extends RobitSkin> codec();


    /**
     * Gets the location of the custom json model for this skin relative to the base "models" directory. In general, it is probably a good idea to base it off the
     * existing robit model's json except with any small changes this skin requires. For an example of the syntax the default model's location would be
     * {@code mekanism:item/robit}.
     *
     * @return Custom model or {@code null} if the default model should be used.
     *
     * @apiNote This is mostly untested currently so if you run into issues please report them.
     */
    @Nullable
    default ResourceLocation customModel() {
        return null;
    }

    /**
     * Gets the list of textures that will be used for this skin. The textures should be located in the asset location: {@code
     * <namespace>/textures/entity/robit/<path>.png}
     * <br><br>
     * It is <strong>important</strong> that this list has at <strong>least ONE</strong> element in it.
     * <br><br>
     * Every three ticks of the robit being alive if it has moved, the selected texture of this skin is incremented to the next one in the list, and then it repeats from
     * the start. This allows skins to define "movement" changes such as how the Robit's treads appear to be moving in the base skin.
     *
     * @return Unmodifiable list of textures for this skin.
     */
    List<ResourceLocation> textures();

    /**
     * Checks if the given player has access to select this skin.
     *
     * @param player Player to check.
     *
     * @return {@code true} if the player has access.
     *
     * @apiNote Only called on the server
     */
    default boolean isUnlocked(@NotNull Player player) {
        //TODO: Have some skins that are potentially locked as patreon rewards?
        return true;
    }

    /**
     * Helper to get the proper translation key path for a given {@link RobitSkin}.
     *
     * @param key {@link RobitSkin} name.
     *
     * @since 10.4.0
     */
    static String getTranslationKey(ResourceKey<? extends RobitSkin> key) {
        return Util.makeDescriptionId("robit_skin", key.location());
    }

    /**
     * Helper to get a translation component representing the display name of a given {@link RobitSkin}.
     *
     * @param key {@link RobitSkin} name.
     *
     * @since 10.4.0
     */
    static Component getTranslatedName(ResourceKey<? extends RobitSkin> key) {
        return TextComponentUtil.translate(getTranslationKey(key));
    }
}