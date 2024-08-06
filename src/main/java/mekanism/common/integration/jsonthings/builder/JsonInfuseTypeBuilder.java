package mekanism.common.integration.jsonthings.builder;

import dev.gigaherz.jsonthings.things.parsers.ThingParser;
import mekanism.api.annotations.NothingNullByDefault;
import mekanism.api.chemical.Chemical;
import mekanism.api.chemical.ChemicalBuilder;
import mekanism.common.util.ChemicalUtil;
import net.minecraft.resources.ResourceLocation;

@NothingNullByDefault
public class JsonInfuseTypeBuilder extends JsonChemicalBuilder<JsonInfuseTypeBuilder> {

    public JsonInfuseTypeBuilder(ThingParser<JsonInfuseTypeBuilder> ownerParser, ResourceLocation registryName) {
        super(ownerParser, registryName);
    }

    @Override
    protected String getThingTypeDisplayName() {
        return "Infuse Type";
    }

    @Override
    protected Chemical buildInternal() {
        ChemicalBuilder internal = texture == null ? ChemicalBuilder.infuseType() : ChemicalBuilder.builder(texture);
        applyBaseData(internal);
        return ChemicalUtil.chemical(internal, colorRepresentation);
    }
}