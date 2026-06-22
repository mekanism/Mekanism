package mekanism.api.chemical;

import com.mojang.serialization.MapCodec;
import java.util.Objects;
import mekanism.api.MekanismAPI;
import net.minecraft.resources.Identifier;
import net.minecraft.util.ARGB;
import net.neoforged.fml.loading.FMLEnvironment;

/// Simple chemical implementation.
///
/// @param icon                Texture to use as an icon for rendering.
/// @param tint                ARGB color to use in tinting this chemical.
/// @param colorRepresentation ARGB color to use when representing this chemical for uses like durability bars.
///
/// @since 10.8.0
public record BasicChemical(Identifier icon, int tint, int colorRepresentation) implements Chemical {

    /// @param icon Texture to use as an icon for rendering.
    /// @param tint ARGB color to use in tinting this chemical, and as the [color representation][Chemical#colorRepresentation()].
    public BasicChemical(Identifier icon, int tint) {
        this(icon, tint, tint);
    }

    public BasicChemical {
        Objects.requireNonNull(icon, "Icon cannot be null");
        tint = validateColor(tint);
        colorRepresentation = validateColor(colorRepresentation);
    }

    private static int validateColor(int color) {
        if (ARGB.alpha(color) == 0) {
            if (FMLEnvironment.isProduction()) {
                return ARGB.opaque(color);
            }
            throw new IllegalArgumentException("Chemical tint should include alpha.");
        }
        return color;
    }

    private static final Identifier BASE_ICON = Identifier.fromNamespaceAndPath(MekanismAPI.MEKANISM_MODID, "mek_liquid/liquid");
    private static final Identifier CLEAN_SLURRY_ICON = Identifier.fromNamespaceAndPath(MekanismAPI.MEKANISM_MODID, "mek_chemical/slurry/clean");
    private static final Identifier DIRTY_SLURRY_ICON = Identifier.fromNamespaceAndPath(MekanismAPI.MEKANISM_MODID, "mek_chemical/slurry/dirty");
    private static final Identifier INFUSE_TYPE_ICON = Identifier.fromNamespaceAndPath(MekanismAPI.MEKANISM_MODID, "mek_chemical/infuse_type/base");
    private static final Identifier PIGMENT_ICON = Identifier.fromNamespaceAndPath(MekanismAPI.MEKANISM_MODID, "mek_chemical/pigment/base");

    /// Creates a basic chemical that uses the default chemical texture.
    ///
    /// @param tint ARGB color to use in tinting this chemical, and as the [color representation][Chemical#colorRepresentation()].
    public static BasicChemical defaultIcon(int tint) {
        return defaultIcon(tint, tint);
    }

    /// Creates a basic chemical that uses the default chemical texture.
    ///
    /// @param tint                ARGB color to use in tinting this chemical.
    /// @param colorRepresentation ARGB color to use when representing this chemical for uses like durability bars.
    public static BasicChemical defaultIcon(int tint, int colorRepresentation) {
        return new BasicChemical(BASE_ICON, tint, colorRepresentation);
    }

    /// Creates a basic chemical that uses the default slurry texture.
    ///
    /// @param clean `true` to use the default clean slurry texture, `false` to use the default dirty slurry texture.
    /// @param tint  ARGB color to use in tinting this chemical, and as the [color representation][Chemical#colorRepresentation()].
    public static BasicChemical slurry(boolean clean, int tint) {
        return slurry(clean, tint, tint);
    }

    /// Creates a basic chemical that uses the default slurry texture.
    ///
    /// @param clean               `true` to use the default clean slurry texture, `false` to use the default dirty slurry texture.
    /// @param tint                ARGB color to use in tinting this chemical.
    /// @param colorRepresentation ARGB color to use when representing this chemical for uses like durability bars.
    public static BasicChemical slurry(boolean clean, int tint, int colorRepresentation) {
        return new BasicChemical(clean ? CLEAN_SLURRY_ICON : DIRTY_SLURRY_ICON, tint, colorRepresentation);
    }

    /// Creates a basic chemical that uses the default infuse type texture.
    ///
    /// @param tint ARGB color to use in tinting this chemical, and as the [color representation][Chemical#colorRepresentation()].
    public static BasicChemical infuseType(int tint) {
        return infuseType(tint, tint);
    }

    /// Creates a basic chemical that uses the default infuse type texture.
    ///
    /// @param tint                ARGB color to use in tinting this chemical.
    /// @param colorRepresentation ARGB color to use when representing this chemical for uses like durability bars.
    public static BasicChemical infuseType(int tint, int colorRepresentation) {
        return new BasicChemical(INFUSE_TYPE_ICON, tint, colorRepresentation);
    }

    /// Creates a basic chemical that uses the default pigment texture.
    ///
    /// @param tint ARGB color to use in tinting this chemical, and as the [color representation][Chemical#colorRepresentation()].
    public static BasicChemical pigment(int tint) {
        return pigment(tint, tint);
    }

    /// Creates a basic chemical that uses the default pigment texture.
    ///
    /// @param tint                ARGB color to use in tinting this chemical.
    /// @param colorRepresentation ARGB color to use when representing this chemical for uses like durability bars.
    public static BasicChemical pigment(int tint, int colorRepresentation) {
        return new BasicChemical(PIGMENT_ICON, tint, colorRepresentation);
    }

    @Override
    public MapCodec<? extends Chemical> codec() {
        return ChemicalSerializationHelper.NETWORK_CODEC;
    }
}