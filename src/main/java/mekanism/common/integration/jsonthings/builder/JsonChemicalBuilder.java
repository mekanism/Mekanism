package mekanism.common.integration.jsonthings.builder;

import dev.gigaherz.jsonthings.things.builders.BaseBuilder;
import dev.gigaherz.jsonthings.things.parsers.ThingParser;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import mekanism.api.annotations.NothingNullByDefault;
import mekanism.api.chemical.Chemical;
import mekanism.api.chemical.ChemicalBuilder;
import mekanism.api.chemical.attribute.ChemicalAttribute;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;

@NothingNullByDefault
public abstract class JsonChemicalBuilder<CHEMICAL extends Chemical<CHEMICAL>, BUILDER extends ChemicalBuilder<CHEMICAL, BUILDER>,
      THING_BUILDER extends JsonChemicalBuilder<CHEMICAL, BUILDER, THING_BUILDER>> extends BaseBuilder<CHEMICAL, THING_BUILDER> {

    private final List<Consumer<BUILDER>> baseData = new ArrayList<>();
    @Nullable
    protected ResourceLocation texture;
    @Nullable
    protected Integer colorRepresentation;

    protected JsonChemicalBuilder(ThingParser<THING_BUILDER> ownerParser, ResourceLocation registryName) {
        super(ownerParser, registryName);
    }

    protected void applyBaseData(BUILDER builder) {
        for (Consumer<BUILDER> base : baseData) {
            base.accept(builder);
        }
    }

    @SuppressWarnings("unchecked")
    private THING_BUILDER self() {
        return (THING_BUILDER) this;
    }

    public THING_BUILDER texture(ResourceLocation texture) {
        this.texture = texture;
        return self();
    }

    protected THING_BUILDER baseData(Consumer<BUILDER> base) {
        baseData.add(base);
        return self();
    }

    /**
     * Sets the tint to apply to this chemical when rendering.
     *
     * @param tint Color in RRGGBB format
     */
    public THING_BUILDER tint(int tint) {
        return baseData(builder -> builder.tint(tint));
    }

    /**
     * Sets the color representation to apply to this chemical when used for things like durability bars. Mostly for use in combination with custom textures that are not
     * tinted.
     *
     * @param color Color in RRGGBB format
     */
    public THING_BUILDER colorRepresentation(int color) {
        colorRepresentation = color;
        return self();
    }

    /**
     * Adds a {@link ChemicalAttribute} to the set of attributes this chemical has.
     *
     * @param attribute Attribute to add.
     */
    public THING_BUILDER with(ChemicalAttribute attribute) {
        return baseData(builder -> builder.with(attribute));
    }
}