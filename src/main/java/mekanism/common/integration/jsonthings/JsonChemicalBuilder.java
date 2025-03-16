package mekanism.common.integration.jsonthings;

import dev.gigaherz.jsonthings.things.builders.BaseBuilder;
import dev.gigaherz.jsonthings.things.parsers.ThingParser;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import mekanism.api.annotations.NothingNullByDefault;
import mekanism.api.chemical.Chemical;
import mekanism.api.chemical.ChemicalBuilder;
import mekanism.common.util.ChemicalUtil;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;

@NothingNullByDefault
public final class JsonChemicalBuilder extends BaseBuilder<Chemical, JsonChemicalBuilder> {

    private final List<Consumer<ChemicalBuilder>> baseData = new ArrayList<>();
    @Nullable
    private ResourceLocation texture;
    @Nullable
    private Integer colorRepresentation;

    public JsonChemicalBuilder(ThingParser<JsonChemicalBuilder> ownerParser, ResourceLocation registryName) {
        super(ownerParser, registryName);
    }

    @Override
    protected String getThingTypeDisplayName() {
        return "Chemical";
    }

    @Override
    protected Chemical buildInternal() {
        ChemicalBuilder internal = texture == null ? ChemicalBuilder.builder() : ChemicalBuilder.builder(texture);
        for (Consumer<ChemicalBuilder> base : baseData) {
            base.accept(internal);
        }
        return ChemicalUtil.chemical(internal, colorRepresentation);
    }

    public JsonChemicalBuilder texture(ResourceLocation texture) {
        if (this.texture != null) {
            throw new IllegalStateException("Specified multiple textures");
        }
        this.texture = texture;
        return this;
    }

    private JsonChemicalBuilder baseData(Consumer<ChemicalBuilder> base) {
        baseData.add(base);
        return this;
    }

    /**
     * Sets the tag that represents the ore that goes with this {@link Chemical}.
     *
     * @param oreTag {@link ResourceLocation} of the item tag representing the ore.
     */
    @SuppressWarnings("removal")
    @Deprecated(forRemoval = true, since = "10.7.11")
    public JsonChemicalBuilder ore(ResourceLocation oreTag) {
        return baseData(builder -> builder.ore(oreTag));
    }

    /**
     * Set this chemical should render as a gas. Omit to leave as fluid-like
     */
    @SuppressWarnings("removal")
    @Deprecated(forRemoval = true, since = "10.7.11")
    public JsonChemicalBuilder gaseous() {
        return baseData(ChemicalBuilder::gaseous);
    }

    /**
     * Sets the tint to apply to this chemical when rendering.
     *
     * @param tint Color in RRGGBB format
     */
    public JsonChemicalBuilder tint(int tint) {
        return baseData(builder -> builder.tint(tint));
    }

    /**
     * Sets the color representation to apply to this chemical when used for things like durability bars. Mostly for use in combination with custom textures that are not
     * tinted.
     *
     * @param color Color in RRGGBB format
     */
    public JsonChemicalBuilder colorRepresentation(int color) {
        colorRepresentation = color;
        return this;
    }

    /**
     * Adds a {@link mekanism.api.chemical.attribute.ChemicalAttribute} to the set of attributes this chemical has.
     *
     * @param attribute Attribute to add.
     */
    @SuppressWarnings("removal")
    public JsonChemicalBuilder with(mekanism.api.chemical.attribute.ChemicalAttribute attribute) {
        return baseData(builder -> builder.with(attribute));
    }
}