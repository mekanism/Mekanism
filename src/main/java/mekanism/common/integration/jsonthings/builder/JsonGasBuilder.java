package mekanism.common.integration.jsonthings.builder;

import dev.gigaherz.jsonthings.things.parsers.ThingParser;
import mekanism.api.annotations.NothingNullByDefault;
import mekanism.api.chemical.Chemical;
import mekanism.api.chemical.ChemicalBuilder;
import mekanism.common.util.ChemicalUtil;
import net.minecraft.resources.ResourceLocation;

@NothingNullByDefault
public class JsonGasBuilder extends JsonChemicalBuilder<JsonGasBuilder> {

    public JsonGasBuilder(ThingParser<JsonGasBuilder> ownerParser, ResourceLocation registryName) {
        super(ownerParser, registryName);
    }

    @Override
    protected String getThingTypeDisplayName() {
        return "Gas";
    }

    @Override
    protected Chemical buildInternal() {
        ChemicalBuilder internal = texture == null ? ChemicalBuilder.builder() : ChemicalBuilder.builder(texture);
        applyBaseData(internal);
        return ChemicalUtil.chemical(internal, colorRepresentation);
    }
}