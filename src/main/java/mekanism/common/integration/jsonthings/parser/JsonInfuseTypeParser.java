package mekanism.common.integration.jsonthings.parser;

import mekanism.api.MekanismAPI;
import mekanism.api.annotations.NothingNullByDefault;
import mekanism.api.chemical.ChemicalType;
import mekanism.api.chemical.infuse.InfuseType;
import mekanism.api.chemical.infuse.InfuseTypeBuilder;
import mekanism.common.integration.jsonthings.builder.JsonInfuseTypeBuilder;
import net.minecraftforge.eventbus.api.IEventBus;

@NothingNullByDefault
public class JsonInfuseTypeParser extends SimpleJsonChemicalParser<InfuseType, InfuseTypeBuilder, JsonInfuseTypeBuilder> {

    public JsonInfuseTypeParser(IEventBus bus) {
        super(bus, ChemicalType.INFUSION, "Infuse Type", MekanismAPI.INFUSE_TYPE_REGISTRY_NAME, JsonInfuseTypeBuilder::new);
    }
}