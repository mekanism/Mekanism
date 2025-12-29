package mekanism.api.chemical;

import java.util.Objects;
import mekanism.api.MekanismAPI;
import mekanism.api.annotations.NothingNullByDefault;
import net.minecraft.resources.Identifier;

@NothingNullByDefault
public class ChemicalBuilder {

    private final Identifier texture;
    private int tint = 0xFFFFFF;

    protected ChemicalBuilder(Identifier texture) {
        this.texture = texture;
    }

    /**
     * Gets the {@link Identifier} representing the texture this chemical will use.
     */
    public Identifier getTexture() {
        return texture;
    }

    /**
     * Sets the tint to apply to this chemical when rendering.
     *
     * @param tint Color in RRGGBB format
     */
    public ChemicalBuilder tint(int tint) {
        this.tint = tint;
        return this;
    }

    /**
     * Gets the tint to apply to this chemical when rendering.
     *
     * @return Tint in RRGGBB format.
     */
    public int getTint() {
        return tint;
    }

    /**
     * Creates a builder for registering a {@link Chemical}, with a given texture.
     *
     * @param texture A {@link Identifier} representing the texture this {@link Chemical} will use.
     *
     * @return A builder for creating a {@link Chemical}.
     *
     * @apiNote The texture will be automatically stitched to the block texture atlas.
     * <br>
     * It is recommended to override {@link Chemical#getColorRepresentation()} if this builder method is not used in combination with {@link #tint(int)} due to the
     * texture not needing tinting.
     */
    public static ChemicalBuilder builder(Identifier texture) {
        return new ChemicalBuilder(Objects.requireNonNull(texture));
    }

    /**
     * Creates a builder for registering a {@link Chemical}, using our default Gas texture.
     *
     * @return A builder for creating a {@link Chemical}.
     */
    public static ChemicalBuilder builder() {
        return builder(Identifier.fromNamespaceAndPath(MekanismAPI.MEKANISM_MODID, "liquid/liquid"));
    }

    /**
     * Creates a builder for registering a {@link Chemical}, using our default clean Slurry texture.
     *
     * @return A builder for creating a {@link Chemical}.
     */
    public static ChemicalBuilder cleanSlurry() {
        return builder(Identifier.fromNamespaceAndPath(MekanismAPI.MEKANISM_MODID, "slurry/clean"));
    }

    /**
     * Creates a builder for registering a {@link Chemical}, using our default dirty Slurry texture.
     *
     * @return A builder for creating a {@link Chemical}.
     */
    public static ChemicalBuilder dirtySlurry() {
        return builder(Identifier.fromNamespaceAndPath(MekanismAPI.MEKANISM_MODID, "slurry/dirty"));
    }

    /**
     * Creates a builder for registering an {@link Chemical}, using our default Infuse Type texture.
     *
     * @return A builder for creating an {@link Chemical}.
     */
    public static ChemicalBuilder infuseType() {
        return builder(Identifier.fromNamespaceAndPath(MekanismAPI.MEKANISM_MODID, "infuse_type/base"));
    }

    /**
     * Creates a builder for registering a {@link Chemical}, using our default Pigment texture.
     *
     * @return A builder for creating a {@link Chemical}.
     */
    public static ChemicalBuilder pigment() {
        return builder(Identifier.fromNamespaceAndPath(MekanismAPI.MEKANISM_MODID, "pigment/base"));
    }
}