package mekanism.common.integration.jsonthings.parser;

import mekanism.api.MekanismAPI;
import mekanism.api.annotations.NothingNullByDefault;
import mekanism.api.chemical.ChemicalType;
import mekanism.common.integration.jsonthings.builder.JsonPigmentBuilder;
import net.neoforged.bus.api.IEventBus;

@NothingNullByDefault
public class JsonPigmentParser extends SimpleJsonChemicalParser<JsonPigmentBuilder> {

    public JsonPigmentParser(IEventBus bus) {
        super(bus, ChemicalType.PIGMENT, "Pigment", MekanismAPI.CHEMICAL_REGISTRY_NAME, JsonPigmentBuilder::new);
    }
}