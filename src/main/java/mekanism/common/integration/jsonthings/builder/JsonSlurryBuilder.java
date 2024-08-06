package mekanism.common.integration.jsonthings.builder;

import dev.gigaherz.jsonthings.things.parsers.ThingParser;
import mekanism.api.annotations.NothingNullByDefault;
import mekanism.api.chemical.Chemical;
import mekanism.api.chemical.ChemicalBuilder;
import mekanism.common.util.ChemicalUtil;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;

@NothingNullByDefault
public class JsonSlurryBuilder extends JsonChemicalBuilder<JsonSlurryBuilder> {

    @Nullable
    private Boolean clean;

    public JsonSlurryBuilder(ThingParser<JsonSlurryBuilder> ownerParser, ResourceLocation registryName) {
        super(ownerParser, registryName);
    }

    /**
     * Sets the tag that represents the ore that goes with this {@link Chemical}.
     *
     * @param oreTag {@link ResourceLocation} of the item tag representing the ore.
     */
    public JsonSlurryBuilder ore(ResourceLocation oreTag) {
        return baseData(builder -> builder.ore(oreTag));
    }

    @Override
    public JsonSlurryBuilder texture(ResourceLocation texture) {
        if (clean != null) {
            //Note: This should never happen as we do texture before checking clean, but still
            throw new IllegalStateException("Texture cannot be used in combination with clean");
        }
        return super.texture(texture);
    }

    public JsonSlurryBuilder clean(boolean clean) {
        if (texture != null) {
            throw new IllegalStateException("Clean cannot be used in combination with specifying an explicit texture");
        }
        this.clean = clean;
        return this;
    }

    @Override
    protected String getThingTypeDisplayName() {
        return "Slurry";
    }

    @Override
    protected Chemical buildInternal() {
        ChemicalBuilder internal;
        if (texture == null) {
            if (clean == null) {
                throw new IllegalStateException("Slurry " + getRegistryName() + " didn't have a texture or fallback texture (whether it is clean or not) specified");
            }
            internal = clean ? ChemicalBuilder.cleanSlurry() : ChemicalBuilder.dirtySlurry();
        } else {
            internal = ChemicalBuilder.builder(texture);
        }
        applyBaseData(internal);
        return ChemicalUtil.chemical(internal, colorRepresentation);
    }
}