package mekanism.common.integration.jsonthings.builder;

import dev.gigaherz.jsonthings.things.parsers.ThingParser;
import mekanism.api.annotations.NothingNullByDefault;
import mekanism.api.chemical.infuse.InfuseType;
import mekanism.api.chemical.infuse.InfuseTypeBuilder;
import mekanism.common.util.ChemicalUtil;
import net.minecraft.resources.ResourceLocation;

@NothingNullByDefault
public class JsonInfuseTypeBuilder extends JsonChemicalBuilder<InfuseType, InfuseTypeBuilder, JsonInfuseTypeBuilder> {

    public JsonInfuseTypeBuilder(ThingParser<JsonInfuseTypeBuilder> ownerParser, ResourceLocation registryName) {
        super(ownerParser, registryName);
    }

    @Override
    protected String getThingTypeDisplayName() {
        return "Infuse Type";
    }

    @Override
    protected InfuseType buildInternal() {
        InfuseTypeBuilder internal = texture == null ? InfuseTypeBuilder.builder() : InfuseTypeBuilder.builder(texture);
        applyBaseData(internal);
        return ChemicalUtil.infuseType(internal, colorRepresentation);
    }
}