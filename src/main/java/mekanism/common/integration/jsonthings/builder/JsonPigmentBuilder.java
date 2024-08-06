package mekanism.common.integration.jsonthings.builder;

import dev.gigaherz.jsonthings.things.parsers.ThingParser;
import mekanism.api.annotations.NothingNullByDefault;
import mekanism.api.chemical.Chemical;
import mekanism.api.chemical.ChemicalBuilder;
import mekanism.common.util.ChemicalUtil;
import net.minecraft.resources.ResourceLocation;

@NothingNullByDefault
public class JsonPigmentBuilder extends JsonChemicalBuilder<JsonPigmentBuilder> {

    public JsonPigmentBuilder(ThingParser<JsonPigmentBuilder> ownerParser, ResourceLocation registryName) {
        super(ownerParser, registryName);
    }

    @Override
    protected String getThingTypeDisplayName() {
        return "Pigment";
    }

    @Override
    protected Chemical buildInternal() {
        ChemicalBuilder internal = texture == null ? ChemicalBuilder.pigment() : ChemicalBuilder.builder(texture);
        applyBaseData(internal);
        return ChemicalUtil.chemical(internal, colorRepresentation);
    }
}