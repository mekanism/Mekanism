package mekanism.common.integration.jsonthings.parser;

import mekanism.api.MekanismAPI;
import mekanism.api.annotations.NothingNullByDefault;
import mekanism.common.integration.jsonthings.builder.JsonInfuseTypeBuilder;
import net.neoforged.bus.api.IEventBus;

@NothingNullByDefault
public class JsonInfuseTypeParser extends SimpleJsonChemicalParser<JsonInfuseTypeBuilder> {

    public JsonInfuseTypeParser(IEventBus bus) {
        super(bus, "Infuse Type", MekanismAPI.CHEMICAL_REGISTRY_NAME, JsonInfuseTypeBuilder::new);
    }
}