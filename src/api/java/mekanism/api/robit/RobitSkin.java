package mekanism.api.robit;

import com.mojang.serialization.MapCodec;
import java.util.List;
import mekanism.api.MekanismRegistries;
import mekanism.api.text.TextComponentUtil;
import net.minecraft.client.resources.model.ResolvableModel;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.common.data.DatapackBuiltinEntriesProvider;
import org.jspecify.annotations.Nullable;

/// Represents a skin for a robit. Register these during datagen using [DatapackBuiltinEntriesProvider].
///
/// See also the <a href="https://github.com/mekanism/Mekanism/wiki/Robit-Skins">Mekanism GitHub wiki</a> for the syntax of creating these manually.
public interface RobitSkin {

    /// @return the codec which serializes and deserializes this [RobitSkin].
    ///
    /// @implNote The returned codec should be registered in [robit skin serializer registry][MekanismRegistries#ROBIT_SKIN_SERIALIZERS].
    /// @since 10.4.0
    MapCodec<? extends RobitSkin> codec();


    /// Gets the location of the custom json model for this skin relative to the base "models" directory.
    ///
    /// The model **must** reside in your `assets/<namespace>/models/robit/` folder **or** be otherwise registered with
    /// [ResolvableModel.Resolver#markDependency(Identifier)]
    ///
    /// In general, it is probably a good idea to base it on the existing robit model's json.
    ///
    /// Custom models should use a texture reference of `#robit` which will be replaced as per the documentation on [#textures()].
    ///
    /// For example, the syntax for the default model's location would be `mekanism:robit/robit`.
    ///
    /// @return Custom model or `null` if the default model should be used.
    ///
    @Nullable
    default Identifier customModel() {
        return null;
    }

    /// Gets the list of textures that will be used for this skin.
    ///
    /// If this list is empty, the `#robit` texture reference will be replaced with the default texture.
    ///
    /// The textures should be located in the asset location: `<namespace>/textures/entity/robit/<path>.png`
    ///
    /// The textures **must** be stitched into the Robit atlas (`mekanism:entity/robit`) if they are not in the above path.
    ///
    /// It is **important** that this list has at **least ONE** element in it.
    ///
    /// Every three ticks of the robit being alive if it has moved, the selected texture of this skin is incremented to the next one in the list, and then it repeats from
    /// the start. This allows skins to define "movement" changes such as how the Robit's treads appear to be moving in the base skin.
    ///
    /// @return Unmodifiable list of textures for this skin.
    List<Identifier> textures();

    /// Checks if the given player has access to select this skin.
    ///
    /// @param player Player to check.
    ///
    /// @return `true` if the player has access.
    ///
    /// @apiNote Only called on the server
    default boolean isUnlocked(Player player) {
        //TODO: Have some skins that are potentially locked as patreon rewards?
        return true;
    }

    /// Helper to get the proper translation key path for a given [RobitSkin].
    ///
    /// @param key [RobitSkin] name.
    ///
    /// @since 10.4.0
    static String getTranslationKey(ResourceKey<? extends RobitSkin> key) {
        return key.identifier().toLanguageKey("robit_skin");
    }

    /// Helper to get a translation component representing the display name of a given [RobitSkin].
    ///
    /// @param key [RobitSkin] name.
    ///
    /// @since 10.4.0
    static Component getTranslatedName(ResourceKey<? extends RobitSkin> key) {
        return TextComponentUtil.translate(getTranslationKey(key));
    }
}