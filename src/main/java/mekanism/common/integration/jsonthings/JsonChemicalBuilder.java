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
import net.minecraft.resources.Identifier;
import org.jetbrains.annotations.Nullable;

@NothingNullByDefault
public final class JsonChemicalBuilder extends BaseBuilder<Chemical, JsonChemicalBuilder> {

    private final List<Consumer<ChemicalBuilder>> baseData = new ArrayList<>();
    @Nullable
    private Identifier texture;
    @Nullable
    private Integer colorRepresentation;

    public JsonChemicalBuilder(ThingParser<JsonChemicalBuilder> ownerParser, Identifier registryName) {
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

    public JsonChemicalBuilder texture(Identifier texture) {
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
}