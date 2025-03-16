package mekanism.common.integration.crafttweaker.content.builder;

import com.blamejared.crafttweaker.api.annotation.ZenRegister;
import com.blamejared.crafttweaker.api.tag.type.KnownTag;
import mekanism.api.chemical.Chemical;
import mekanism.api.chemical.ChemicalBuilder;
import mekanism.api.chemical.attribute.ChemicalAttribute;
import mekanism.common.Mekanism;
import mekanism.common.integration.crafttweaker.CrTConstants;
import mekanism.common.integration.crafttweaker.content.CrTContentUtils;
import mekanism.common.util.ChemicalUtil;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import org.jetbrains.annotations.Nullable;
import org.openzen.zencode.java.ZenCodeType;

@ZenRegister(loaders = CrTConstants.CONTENT_LOADER)
@ZenCodeType.Name(CrTConstants.CLASS_BUILDER_CHEMICAL)
public class CrTChemicalBuilder {

    private final ChemicalBuilder builder;
    @Nullable
    private Integer colorRepresentation;

    private CrTChemicalBuilder(ChemicalBuilder builder) {
        this.builder = builder;
    }

    /**
     * Adds an attribute to the set of attributes this chemical has.
     *
     * @param attribute Attribute to add.
     */
    @ZenCodeType.Method
    @SuppressWarnings("removal")
    @Deprecated(forRemoval = true, since = "10.7.11")
    public CrTChemicalBuilder with(ChemicalAttribute attribute) {
        builder.with(attribute);
        return this;
    }

    /**
     * Sets the tint to apply to this chemical when rendering.
     *
     * @param tint Color in RRGGBB format
     */
    @ZenCodeType.Method
    public CrTChemicalBuilder tint(int tint) {
        builder.tint(tint);
        return this;
    }

    /**
     * Sets the color representation to apply to this chemical when used for things like durability bars. Mostly for use in combination with custom textures that are not
     * tinted.
     *
     * @param color Color in RRGGBB format
     */
    @ZenCodeType.Method
    public CrTChemicalBuilder colorRepresentation(int color) {
        colorRepresentation = color;
        return this;
    }

    /**
     * Create a chemical from this builder with the given name.
     *
     * @param name Registry name for the chemical.
     */
    @ZenCodeType.Method
    public void build(String name) {
        build(Mekanism.hooks.craftTweaker.rl(name));
    }

    /**
     * Create a chemical from this builder with the given name.
     *
     * @param registryName Registry name for the chemical.
     */
    protected void build(ResourceLocation registryName) {
        Chemical chemical = ChemicalUtil.chemical(builder, colorRepresentation);
        CrTContentUtils.queueChemicalForRegistration(registryName, chemical);
    }

    /**
     * Sets the tag that represents the ore that goes with this slurry {@link Chemical}.
     *
     * @param oreTagLocation {@link ResourceLocation} of the item tag representing the ore.
     */
    @ZenCodeType.Method
    @SuppressWarnings("removal")
    @Deprecated(forRemoval = true, since = "10.7.11")
    public CrTChemicalBuilder ore(ResourceLocation oreTagLocation) {
        builder.ore(oreTagLocation);
        return this;
    }

    /**
     * Sets the tag that represents the ore that goes with this slurry {@link Chemical}.
     *
     * @param oreTag Tag representing the ore.
     *
     * @implNote We add the tag by its internal id instead of getting the internal tag as the internal tag would currently be {@code null} when this gets called, as the
     * collection is empty, and the goal of this method is to let the slurry have a {@link net.minecraft.tags.TagKey}
     */
    @ZenCodeType.Method
    @SuppressWarnings("removal")
    @Deprecated(forRemoval = true, since = "10.7.11")
    public CrTChemicalBuilder ore(KnownTag<Item> oreTag) {
        builder.ore(oreTag.getTagKey());
        return this;
    }

    /**
     * Set this chemical should render as a gas. Omit to leave as fluid-like.
     */
    @ZenCodeType.Method
    @SuppressWarnings("removal")
    @Deprecated(forRemoval = true, since = "10.7.11")
    public CrTChemicalBuilder gaseous() {
        builder.gaseous();
        return this;
    }

    /**
     * Creates a builder for registering a custom {@link Chemical}.
     *
     * @param textureLocation If present the {@link ResourceLocation} representing the texture this {@link Chemical} will use, otherwise defaults to our default Gas texture.
     *
     * @return A builder for creating a custom {@link Chemical}.
     *
     * @apiNote If a custom texture is used it is recommended to override to use {@link #colorRepresentation(int)} if this builder method is not being used in combination
     * with {@link #tint(int)} due to the texture not needing tinting.
     */
    @ZenCodeType.Method
    public static CrTChemicalBuilder builder(@ZenCodeType.Optional ResourceLocation textureLocation) {
        return new CrTChemicalBuilder(textureLocation == null ? ChemicalBuilder.builder() : ChemicalBuilder.builder(textureLocation));
    }

    /**
     * Creates a builder for registering a custom {@link Chemical} with the default infuse type texture.
     *
     * @return A builder for creating a custom {@link Chemical}.
     */
    @ZenCodeType.Method
    public static CrTChemicalBuilder infuseType() {
        return new CrTChemicalBuilder(ChemicalBuilder.infuseType());
    }

    /**
     * Creates a builder for registering a custom {@link Chemical} with the default pigment texture.
     *
     * @return A builder for creating a custom {@link Chemical}.
     */
    @ZenCodeType.Method
    public static CrTChemicalBuilder pigment() {
        return new CrTChemicalBuilder(ChemicalBuilder.pigment());
    }

    /**
     * Creates a builder for registering a custom {@link Chemical}, using our default clean Slurry texture.
     *
     * @return A builder for creating a custom {@link Chemical}.
     */
    @ZenCodeType.Method
    public static CrTChemicalBuilder clean() {
        return new CrTChemicalBuilder(ChemicalBuilder.cleanSlurry());
    }

    /**
     * Creates a builder for registering a custom {@link Chemical}, using our default dirty Slurry texture.
     *
     * @return A builder for creating a custom {@link Chemical}.
     */
    @ZenCodeType.Method
    public static CrTChemicalBuilder dirty() {
        return new CrTChemicalBuilder(ChemicalBuilder.dirtySlurry());
    }
}