package mekanism.common.integration.jsonthings;

import com.google.gson.JsonObject;
import dev.gigaherz.jsonthings.things.builders.BaseBuilder;
import dev.gigaherz.jsonthings.things.parsers.ThingParser;
import dev.gigaherz.jsonthings.util.parse.JParse;
import dev.gigaherz.jsonthings.util.parse.function.ObjValueFunction;
import dev.gigaherz.jsonthings.util.parse.value.Any;
import java.util.function.Consumer;
import java.util.function.IntConsumer;
import mekanism.api.MekanismAPI;
import mekanism.api.annotations.NothingNullByDefault;
import mekanism.api.chemical.Chemical;
import mekanism.common.Mekanism;
import net.minecraft.resources.Identifier;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.RegisterEvent;

@NothingNullByDefault
public class JsonChemicalParser extends ThingParser<Chemical, JsonChemicalBuilder> {

    public JsonChemicalParser(IEventBus bus) {
        super(GSON, Mekanism.MODID + "/chemical");
        bus.addListener(this::register);
    }

    private void register(RegisterEvent event) {
        event.register(MekanismAPI.CHEMICAL_REGISTRY_NAME, helper -> {
            LOGGER.info("Started registering Chemical things, errors about unexpected registry domains are harmless...");
            processAndConsumeErrors(getThingType(), getBuilders(), thing -> helper.register(thing.getRegistryName(), thing.get()), BaseBuilder::getRegistryName);
            LOGGER.info("Done processing thingpack Chemical.");
        });
    }

    @Override
    protected JsonChemicalBuilder processThing(Identifier key, JsonObject data, Consumer<JsonChemicalBuilder> builderModification) {
        JsonChemicalBuilder builder = new JsonChemicalBuilder(this, key);
        JParse.begin(data)
              .ifKey("texture", val -> val.string().map(Identifier::parse).handle(builder::texture))
              .ifKey("tint", val -> processColor(val, builder::tint))
              .ifKey("color_representation", val -> processColor(val, builder::colorRepresentation));
        builderModification.accept(builder);
        return builder;
    }

    private static void processColor(Any val, IntConsumer colorSetter) {
        val.ifObj(obj -> obj.map((ObjValueFunction<Integer>) ThingParser::parseColor).handle(colorSetter::accept))
              .ifArray(arr -> arr.mapWhole(ThingParser::parseColor).handle(colorSetter::accept))
              .ifString(str -> str.map(ThingParser::parseColor).handle(colorSetter::accept))
              .ifInteger(i -> i.handle(colorSetter))
              .typeError();
    }
}