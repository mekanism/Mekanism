package mekanism.common.integration.crafttweaker.content.builder;

import com.blamejared.crafttweaker.api.annotation.ZenRegister;
import com.blamejared.crafttweaker.api.tag.type.KnownTag;
import mekanism.api.chemical.Chemical;
import mekanism.api.chemical.ChemicalBuilder;
import mekanism.api.chemical.attribute.ChemicalAttribute;
import mekanism.common.integration.crafttweaker.CrTConstants;
import mekanism.common.integration.crafttweaker.CrTUtils;
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
    protected Integer colorRepresentation;

    public CrTChemicalBuilder(ChemicalBuilder builder) {
        this.builder = builder;
    }

    /**
     * Adds an attribute to the set of attributes this chemical has.
     *
     * @param attribute Attribute to add.
     */
    @ZenCodeType.Method
    public CrTChemicalBuilder with(ChemicalAttribute attribute) {
        getInternal().with(attribute);
        return self();
    }

    /**
     * Sets the tint to apply to this chemical when rendering.
     *
     * @param tint Color in RRGGBB format
     */
    @ZenCodeType.Method
    public CrTChemicalBuilder tint(int tint) {
        getInternal().tint(tint);
        return self();
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
        return self();
    }

    /**
     * Create a chemical from this builder with the given name.
     *
     * @param name Registry name for the chemical.
     */
    @ZenCodeType.Method
    public void build(String name) {
        build(CrTUtils.rl(name));
    }

    /**
     * Create a chemical from this builder with the given name.
     *
     * @param registryName Registry name for the chemical.
     */
    protected void build(ResourceLocation registryName) {
        Chemical gas = ChemicalUtil.chemical(getInternal(), colorRepresentation);
        CrTContentUtils.queueChemicalForRegistration(registryName, gas);
    }

    /**
     * Gets the internal {@link ChemicalBuilder}
     */
    protected ChemicalBuilder getInternal() {
        return builder;
    }

    protected CrTChemicalBuilder self() {
        return (CrTChemicalBuilder) this;
    }

    /**
     * Sets the tag that represents the ore that goes with this slurry {@link Chemical}.
     *
     * @param oreTagLocation {@link ResourceLocation} of the item tag representing the ore.
     */
    @ZenCodeType.Method
    public CrTChemicalBuilder ore(ResourceLocation oreTagLocation) {
        getInternal().ore(oreTagLocation);
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
    public CrTChemicalBuilder ore(KnownTag<Item> oreTag) {
        getInternal().ore(oreTag.getTagKey());
        return this;
    }

    @ZenCodeType.Method
    public static CrTChemicalBuilder builder(@ZenCodeType.Optional ResourceLocation textureLocation) {
        return new CrTChemicalBuilder(textureLocation == null ? ChemicalBuilder.builder() : ChemicalBuilder.builder(textureLocation));
    }

    /**
     * Creates a builder for registering a custom {@link Chemical}.
     *
     * @param textureLocation If present the {@link ResourceLocation} representing the texture this {@link Chemical} will use, otherwise defaults to our default Infuse
     *                        Type texture.
     *
     * @return A builder for creating a custom {@link Chemical}.
     *
     * @apiNote If a custom texture is used it is recommended to override to use {@link #colorRepresentation(int)} if this builder method is not being used in combination
     * with {@link #tint(int)} due to the texture not needing tinting.
     */
    @ZenCodeType.Method
    public static CrTChemicalBuilder infuseType(@ZenCodeType.Optional ResourceLocation textureLocation) {
        return new CrTChemicalBuilder(textureLocation == null ? ChemicalBuilder.infuseType() : ChemicalBuilder.builder(textureLocation));
    }

    /**
     * Creates a builder for registering a custom {@link Chemical}.
     *
     * @param textureLocation If present the {@link ResourceLocation} representing the texture this {@link Chemical} will use, otherwise defaults to our default
     *                        {@link Chemical} texture.
     *
     * @return A builder for creating a custom {@link Chemical}.
     *
     * @apiNote If a custom texture is used it is recommended to override to use {@link #colorRepresentation(int)} if this builder method is not being used in combination
     * with {@link #tint(int)} due to the texture not needing tinting.
     */
    @ZenCodeType.Method
    public static CrTChemicalBuilder pigment(@ZenCodeType.Optional ResourceLocation textureLocation) {
        return new CrTChemicalBuilder(textureLocation == null ? ChemicalBuilder.pigment() : ChemicalBuilder.builder(textureLocation));
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