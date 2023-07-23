package mekanism.common.integration.jsonthings.parser;

import mekanism.api.MekanismAPI;
import mekanism.api.annotations.NothingNullByDefault;
import mekanism.api.chemical.ChemicalType;
import mekanism.api.chemical.pigment.Pigment;
import mekanism.api.chemical.pigment.PigmentBuilder;
import mekanism.common.integration.jsonthings.builder.JsonPigmentBuilder;
import net.minecraftforge.eventbus.api.IEventBus;

@NothingNullByDefault
public class JsonPigmentParser extends SimpleJsonChemicalParser<Pigment, PigmentBuilder, JsonPigmentBuilder> {

    public JsonPigmentParser(IEventBus bus) {
        super(bus, ChemicalType.PIGMENT, "Pigment", MekanismAPI.PIGMENT_REGISTRY_NAME, JsonPigmentBuilder::new);
    }
}