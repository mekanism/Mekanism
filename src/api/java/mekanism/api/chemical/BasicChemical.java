package mekanism.api.chemical;

import java.util.Objects;
import mekanism.api.MekanismAPI;
import mekanism.api.MekanismRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.util.ARGB;
import net.minecraft.util.CommonColors;
import net.minecraft.world.level.Level;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.neoforge.registries.DeferredHolder;
import org.jetbrains.annotations.Range;

/// Simple chemical implementation.
///
/// @param icon                Texture to use as an icon for rendering.
/// @param tint                ARGB color to use in tinting this chemical.
/// @param colorRepresentation ARGB color to use when representing this chemical for uses like durability bars.
///
/// @since 10.8.0
public record BasicChemical(Identifier icon, int tint, int colorRepresentation, @Range(from = 0, to = 15) int lightLevel) implements Chemical {

    private static final DeferredHolder<ChemicalSerializer, ChemicalSerializer> SERIALIZER = DeferredHolder.create(MekanismRegistries.Keys.CHEMICAL_SERIALIZERS,
          Identifier.fromNamespaceAndPath(MekanismAPI.MEKANISM_MODID, "basic"));

    public BasicChemical {
        Objects.requireNonNull(icon, "Icon cannot be null");
        validateLightLevel(lightLevel);
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

    private static void validateLightLevel(int lightLevel) {
        if (lightLevel < 0 || lightLevel > Level.MAX_BRIGHTNESS) {
            throw new IllegalArgumentException("Light level must be between 0 and " + Level.MAX_BRIGHTNESS);
        }
    }

    private static final Identifier BASE_ICON = Identifier.fromNamespaceAndPath(MekanismAPI.MEKANISM_MODID, "mek_liquid/liquid");
    private static final Identifier CLEAN_SLURRY_ICON = Identifier.fromNamespaceAndPath(MekanismAPI.MEKANISM_MODID, "mek_chemical/slurry/clean");
    private static final Identifier DIRTY_SLURRY_ICON = Identifier.fromNamespaceAndPath(MekanismAPI.MEKANISM_MODID, "mek_chemical/slurry/dirty");
    private static final Identifier INFUSE_TYPE_ICON = Identifier.fromNamespaceAndPath(MekanismAPI.MEKANISM_MODID, "mek_chemical/infuse_type/base");
    private static final Identifier PIGMENT_ICON = Identifier.fromNamespaceAndPath(MekanismAPI.MEKANISM_MODID, "mek_chemical/pigment/base");

    /// Creates a basic chemical builder that uses the default chemical texture.
    public static BasicChemical.Builder builder() {
        return builder(BASE_ICON);
    }

    /// Creates a basic chemical that uses the default clean slurry texture.
    public static BasicChemical.Builder cleanSlurry() {
        return builder(CLEAN_SLURRY_ICON);
    }

    /// Creates a basic chemical that uses the default dirty slurry texture.
    public static BasicChemical.Builder dirtySlurry() {
        return builder(DIRTY_SLURRY_ICON);
    }

    /// Creates a basic chemical builder that uses the default infuse type texture.
    public static BasicChemical.Builder infuseType() {
        return builder(INFUSE_TYPE_ICON);
    }

    /// Creates a basic chemical builder that uses the default pigment texture.
    public static BasicChemical.Builder pigment() {
        return builder(PIGMENT_ICON);
    }

    /// Creates a basic chemical builder that uses the specified chemical texture.
    ///
    /// @param icon Texture to use for this chemical.
    public static BasicChemical.Builder builder(Identifier icon) {
        return new BasicChemical.Builder(icon);
    }

    @Override
    public ChemicalSerializer serializer() {
        return SERIALIZER.get();
    }

    public static class Builder {

        private final Identifier icon;
        private int tint = CommonColors.WHITE;
        private int colorRepresentation = CommonColors.WHITE;
        private int lightLevel = 0;

        private Builder(Identifier icon) {
            this.icon = Objects.requireNonNull(icon, "Icon cannot be null");
        }

        /// @param tint ARGB color to use in tinting this chemical, and as the [color representation][Chemical#colorRepresentation()].
        ///
        /// @apiNote If a different color representation is desired, call [#colorRepresentation()] after this method.
        public Builder tint(int tint) {
            this.tint = validateColor(tint);
            return colorRepresentation(tint);
        }

        /// @param colorRepresentation ARGB color to use when representing this chemical for uses like durability bars.
        ///
        /// @apiNote As this method will get overridden by calling [#tint()], it is important to call this method after [#tint()] if a different value is desired.
        public Builder colorRepresentation(int colorRepresentation) {
            this.colorRepresentation = validateColor(colorRepresentation);
            return this;
        }

        /// @param lightLevel A value between `[0, 15]` representing the light level emitted by the chemical. As chemicals cannot be placed into the world, examples of
        /// use cases are: emissivity in multiblocks, or light level from within a FramedBlock.
        public Builder lightLevel(@Range(from = 0, to = 15) int lightLevel) {
            //TODO - 26.2: Define light levels for our various chemicals
            validateLightLevel(lightLevel);
            this.lightLevel = lightLevel;
            return this;
        }

        /// {@return the chemical with the specified properties}
        public BasicChemical build() {
            return new BasicChemical(icon, tint, colorRepresentation, lightLevel);
        }

        private static int validateColor(int color) {
            //Note: Unlike BasicChemical#validateColor, this doesn't support having invalid values in production
            if (ARGB.alpha(color) == 0) {
                throw new IllegalArgumentException("Chemical tint should include alpha.");
            }
            return color;
        }

    }
}