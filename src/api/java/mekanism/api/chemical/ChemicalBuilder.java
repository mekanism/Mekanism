package mekanism.api.chemical;

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import java.util.Collections;
import java.util.Map;
import java.util.Objects;
import mekanism.api.MekanismAPI;
import mekanism.api.annotations.NothingNullByDefault;
import mekanism.api.chemical.attribute.ChemicalAttribute;
import mekanism.api.datamaps.chemical.ChemicalSolidTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import org.jetbrains.annotations.Nullable;

@NothingNullByDefault
public class ChemicalBuilder {

    private final ResourceLocation texture;
    private int tint = 0xFFFFFF;

    @SuppressWarnings("removal")
    @Deprecated(forRemoval = true, since = "10.7.11")
    private final Map<Class<? extends ChemicalAttribute>, ChemicalAttribute> attributeMap = new Object2ObjectOpenHashMap<>();
    @Nullable
    @Deprecated(forRemoval = true, since = "10.7.11")
    private TagKey<Item> oreTag;
    @Deprecated(forRemoval = true, since = "10.7.11")
    private boolean isGaseous = false;

    protected ChemicalBuilder(ResourceLocation texture) {
        this.texture = texture;
    }

    /**
     * Adds a {@link ChemicalAttribute} to the set of attributes this chemical has.
     *
     * @param attribute Attribute to add.
     *
     * @deprecated Prefer adding attributes via data map.
     */
    @SuppressWarnings("removal")
    @Deprecated(forRemoval = true, since = "10.7.11")
    public ChemicalBuilder with(ChemicalAttribute attribute) {
        attributeMap.put(attribute.getClass(), attribute);
        return this;
    }

    /**
     * Gets the attributes this chemical will have.
     */
    @SuppressWarnings("removal")
    @Deprecated(forRemoval = true, since = "10.7.11")
    public Map<Class<? extends ChemicalAttribute>, ChemicalAttribute> getAttributeMap() {
        return Collections.unmodifiableMap(attributeMap);
    }

    /**
     * Gets the {@link ResourceLocation} representing the texture this chemical will use.
     */
    public ResourceLocation getTexture() {
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
     * Sets the tag that represents the ore that goes with this {@link Chemical}.
     *
     * @param oreTagLocation {@link ResourceLocation} of the item tag representing the ore.
     *
     * @deprecated Please see {@link ChemicalSolidTag}
     */
    @Deprecated(forRemoval = true, since = "10.7.11")
    public ChemicalBuilder ore(ResourceLocation oreTagLocation) {
        return ore(ItemTags.create(Objects.requireNonNull(oreTagLocation)));
    }

    /**
     * Sets the tag that represents the ore that goes with this {@link Chemical}.
     *
     * @param oreTag Tag representing the ore.
     *
     * @deprecated Please see {@link ChemicalSolidTag}
     */
    @Deprecated(forRemoval = true, since = "10.7.11")
    public ChemicalBuilder ore(TagKey<Item> oreTag) {
        this.oreTag = Objects.requireNonNull(oreTag);
        return this;
    }

    /**
     * Gets the item tag that represents the ore that goes with this {@link Chemical}.
     *
     * @deprecated Please see {@link ChemicalSolidTag}
     */
    @Nullable
    @Deprecated(forRemoval = true, since = "10.7.11")
    public TagKey<Item> getOreTag() {
        return oreTag;
    }

    /**
     * Set this chemical should render as a gas. Omit to leave as fluid-like
     *
     * @since 10.7.0
     * @deprecated Please add your chemicals to {@link mekanism.api.MekanismAPITags.Chemicals#GASEOUS}
     */
    @Deprecated(forRemoval = true, since = "10.7.11")
    public ChemicalBuilder gaseous() {
        this.isGaseous = true;
        return this;
    }

    /**
     * {@return whether this chemical should render as a gas or more like a fluid}
     *
     * @since 10.7.0
     *
     * @deprecated Please add your chemicals to {@link mekanism.api.MekanismAPITags.Chemicals#GASEOUS}
     */
    @Deprecated(forRemoval = true, since = "10.7.11")
    public boolean isGaseous() {
        return isGaseous;
    }

    /**
     * Creates a builder for registering a {@link Chemical}, with a given texture.
     *
     * @param texture A {@link ResourceLocation} representing the texture this {@link Chemical} will use.
     *
     * @return A builder for creating a {@link Chemical}.
     *
     * @apiNote The texture will be automatically stitched to the block texture atlas.
     * <br>
     * It is recommended to override {@link Chemical#getColorRepresentation()} if this builder method is not used in combination with {@link #tint(int)} due to the
     * texture not needing tinting.
     */
    public static ChemicalBuilder builder(ResourceLocation texture) {
        return new ChemicalBuilder(Objects.requireNonNull(texture));
    }

    /**
     * Creates a builder for registering a {@link Chemical}, using our default Gas texture.
     *
     * @return A builder for creating a {@link Chemical}.
     */
    public static ChemicalBuilder builder() {
        return builder(ResourceLocation.fromNamespaceAndPath(MekanismAPI.MEKANISM_MODID, "liquid/liquid"));
    }

    /**
     * Creates a builder for registering a {@link Chemical}, using our default clean Slurry texture.
     *
     * @return A builder for creating a {@link Chemical}.
     */
    public static ChemicalBuilder cleanSlurry() {
        return builder(ResourceLocation.fromNamespaceAndPath(MekanismAPI.MEKANISM_MODID, "slurry/clean"));
    }

    /**
     * Creates a builder for registering a {@link Chemical}, using our default dirty Slurry texture.
     *
     * @return A builder for creating a {@link Chemical}.
     */
    public static ChemicalBuilder dirtySlurry() {
        return builder(ResourceLocation.fromNamespaceAndPath(MekanismAPI.MEKANISM_MODID, "slurry/dirty"));
    }

    /**
     * Creates a builder for registering an {@link Chemical}, using our default Infuse Type texture.
     *
     * @return A builder for creating an {@link Chemical}.
     */
    public static ChemicalBuilder infuseType() {
        return builder(ResourceLocation.fromNamespaceAndPath(MekanismAPI.MEKANISM_MODID, "infuse_type/base"));
    }

    /**
     * Creates a builder for registering a {@link Chemical}, using our default Pigment texture.
     *
     * @return A builder for creating a {@link Chemical}.
     */
    public static ChemicalBuilder pigment() {
        return builder(ResourceLocation.fromNamespaceAndPath(MekanismAPI.MEKANISM_MODID, "pigment/base"));
    }
}