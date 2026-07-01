package mekanism.api.chemical;

import com.mojang.serialization.MapCodec;
import mekanism.api.text.TextComponentUtil;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.util.Util;
import net.minecraft.world.level.Level;
import org.jspecify.annotations.Nullable;

public interface Chemical {

    /// @return the codec which serializes and deserializes this [Chemical].
    ///
    /// @implNote The returned codec should be registered in the [chemical serializer registry][mekanism.api.MekanismRegistries#CHEMICAL_SERIALIZERS].
    /// @since 10.8.0
    MapCodec<? extends Chemical> codec();

    /// Gets the resource location of the icon associated with this Chemical.
    ///
    /// @return The resource location of the icon
    ///
    /// @since 10.8.0
    Identifier icon();

    /// Get the tint for rendering the chemical
    ///
    /// @return int representation of color in ARGB format
    ///
    /// @since 10.8.0
    int tint();//TODO - 26.2: Should we try to move the icon and tint to a model/separate file that can be specified/overridden by a resource pack?

    //TODO - 26.2: Implement this and make chemicals not be full bright by default
    default int lightLevel() {
        return Level.MAX_BRIGHTNESS;
    }

    /// Get the color representation used for displaying in things like durability bars of chemical tanks.
    ///
    /// @return int representation of color in ARGB format
    ///
    /// @since 10.8.0
    default int colorRepresentation() {
        return tint();
    }

    /// Helper to get the proper translation key path for a given [Chemical].
    ///
    /// @param key [Chemical] name.
    ///
    /// @since 10.8.0
    static String getTranslationKey(@Nullable ResourceKey<? extends Chemical> key) {
        return Util.makeDescriptionId("chemical", key == null ? null : key.identifier());
    }

    /// Helper to get a translation component representing the display name of a given [Chemical].
    ///
    /// @param key [Chemical] name.
    ///
    /// @since 10.4.0
    static Component getTranslatedName(ResourceKey<? extends Chemical> key) {
        return TextComponentUtil.translate(getTranslationKey(key));
    }
}