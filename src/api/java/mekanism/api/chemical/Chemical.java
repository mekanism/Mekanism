package mekanism.api.chemical;

import mekanism.api.text.TextComponentUtil;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.util.Util;
import org.jspecify.annotations.Nullable;

public interface Chemical {

    /// @return the chemical serializer which serializes and deserializes this [Chemical].
    ///
    /// @implNote The returned serializer should be registered in the [chemical serializer registry][mekanism.api.MekanismRegistries#CHEMICAL_SERIALIZERS].
    /// @since 10.8.0
    ChemicalSerializer serializer();

    /// Gets the resource location of the icon associated with this Chemical.
    ///
    /// @return The resource location of the icon
    ///
    /// @implSpec It is expected that this atlas will be added to the [block atlas][net.minecraft.data.AtlasIds#BLOCKS]. This must be done via a sprite source provider or
    /// by placing the texture within one of the following textures subdirectories `mek_liquid/` or `mek_chemical/`
    /// @since 10.8.0
    Identifier icon();

    /// Get the tint for rendering the chemical
    ///
    /// @return int representation of color in ARGB format
    ///
    /// @since 10.8.0
    int tint();//TODO - 26.2: Should we try to move the icon and tint to a model/separate file that can be specified/overridden by a resource pack?

    /// Returns the light level emitted by the chemical. As chemicals cannot be placed into the world, examples of use cases are: emissivity in multiblocks, or light
    /// level from within a FramedBlock.
    ///
    /// @return A value between `[0, 15]` representing the light level emitted by the chemical.
    ///
    /// @since 10.8.0
    default int lightLevel() {
        return 0;
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