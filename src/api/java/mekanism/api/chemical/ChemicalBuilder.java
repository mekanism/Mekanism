package mekanism.api.chemical;

import java.util.Objects;
import mekanism.api.MekanismAPI;
import net.minecraft.resources.Identifier;
import net.minecraft.util.ARGB;
import net.minecraft.util.CommonColors;
import net.neoforged.fml.loading.FMLEnvironment;

public class ChemicalBuilder {

    private final Identifier texture;
    private int tint = CommonColors.WHITE;

    protected ChemicalBuilder(Identifier texture) {
        this.texture = texture;
    }

    /// Gets the [Identifier] representing the texture this chemical will use.
    public Identifier getTexture() {
        return texture;
    }

    /// Sets the tint to apply to this chemical when rendering.
    ///
    /// @param tint Color in AARRGGBB format
    public ChemicalBuilder tint(int tint) {
        if (ARGB.alpha(tint) == 0) {
            if (FMLEnvironment.isProduction()) {
                tint = ARGB.opaque(tint);
            } else {
                throw new IllegalArgumentException("Chemical tint should now includes alpha.");
            }
        }
        this.tint = tint;
        return this;
    }

    /// Gets the tint to apply to this chemical when rendering.
    ///
    /// @return Tint in AARRGGBB format.
    public int getTint() {
        return tint;
    }

    /// Creates a builder for registering a [Chemical], with a given texture.
    ///
    /// @param texture A [Identifier] representing the texture this [Chemical] will use.
    ///
    /// @return A builder for creating a [Chemical].
    ///
    /// @apiNote The texture will be automatically stitched to the block texture atlas.
    ///
    /// It is recommended to override [Chemical#getColorRepresentation()] if this builder method is not used in combination with [#tint(int)] due to the texture not
    /// needing tinting.
    public static ChemicalBuilder builder(Identifier texture) {
        return new ChemicalBuilder(Objects.requireNonNull(texture));
    }

    /// Creates a builder for registering a [Chemical], using our default Gas texture.
    ///
    /// @return A builder for creating a [Chemical].
    public static ChemicalBuilder builder() {
        return builder(Identifier.fromNamespaceAndPath(MekanismAPI.MEKANISM_MODID, "mek_liquid/liquid"));
    }

    /// Creates a builder for registering a [Chemical], using our default clean Slurry texture.
    ///
    /// @return A builder for creating a [Chemical].
    public static ChemicalBuilder cleanSlurry() {
        return builder(Identifier.fromNamespaceAndPath(MekanismAPI.MEKANISM_MODID, "mek_chemical/slurry/clean"));
    }

    /// Creates a builder for registering a [Chemical], using our default dirty Slurry texture.
    ///
    /// @return A builder for creating a [Chemical].
    public static ChemicalBuilder dirtySlurry() {
        return builder(Identifier.fromNamespaceAndPath(MekanismAPI.MEKANISM_MODID, "mek_chemical/slurry/dirty"));
    }

    /// Creates a builder for registering an [Chemical], using our default Infuse Type texture.
    ///
    /// @return A builder for creating an [Chemical].
    public static ChemicalBuilder infuseType() {
        return builder(Identifier.fromNamespaceAndPath(MekanismAPI.MEKANISM_MODID, "mek_chemical/infuse_type/base"));
    }

    /// Creates a builder for registering a [Chemical], using our default Pigment texture.
    ///
    /// @return A builder for creating a [Chemical].
    public static ChemicalBuilder pigment() {
        return builder(Identifier.fromNamespaceAndPath(MekanismAPI.MEKANISM_MODID, "mek_chemical/pigment/base"));
    }
}