package mekanism.common.integration.jsonthings.parser;

import com.google.gson.JsonObject;
import dev.gigaherz.jsonthings.things.builders.BaseBuilder;
import dev.gigaherz.jsonthings.things.parsers.ThingParser;
import dev.gigaherz.jsonthings.util.parse.JParse;
import dev.gigaherz.jsonthings.util.parse.function.ObjValueFunction;
import dev.gigaherz.jsonthings.util.parse.value.Any;
import dev.gigaherz.jsonthings.util.parse.value.ObjValue;
import java.util.function.IntConsumer;
import mekanism.api.annotations.NothingNullByDefault;
import mekanism.api.chemical.Chemical;
import mekanism.api.chemical.ChemicalBuilder;
import mekanism.api.chemical.ChemicalType;
import mekanism.common.Mekanism;
import mekanism.common.integration.jsonthings.builder.JsonChemicalBuilder;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.RegisterEvent;

@NothingNullByDefault
public abstract class JsonChemicalParser<CHEMICAL extends Chemical<CHEMICAL>, BUILDER extends ChemicalBuilder<CHEMICAL, BUILDER>,
      THING_BUILDER extends JsonChemicalBuilder<CHEMICAL, BUILDER, THING_BUILDER>> extends ThingParser<THING_BUILDER> {

    private final ResourceKey<? extends Registry<CHEMICAL>> registryKey;
    private final String thingType;

    protected JsonChemicalParser(IEventBus bus, ChemicalType chemicalType, String thingType, ResourceKey<? extends Registry<CHEMICAL>> registryKey) {
        super(GSON, Mekanism.MODID + "/" + chemicalType.getSerializedName());
        this.thingType = thingType;
        this.registryKey = registryKey;
        bus.addListener(this::register);
    }

    private void register(RegisterEvent event) {
        event.register(registryKey, helper -> {
            LOGGER.info("Started registering {} things, errors about unexpected registry domains are harmless...", thingType);
            processAndConsumeErrors(getThingType(), getBuilders(), thing -> helper.register(thing.getRegistryName(), thing.get()), BaseBuilder::getRegistryName);
            LOGGER.info("Done processing thingpack {}.", thingType);
        });
    }

    protected static void processColor(Any val, IntConsumer colorSetter) {
        val.ifObj(obj -> obj.map((ObjValueFunction<Integer>) ThingParser::parseColor).handle(colorSetter::accept))
              .ifArray(arr -> arr.mapWhole(ThingParser::parseColor).handle(colorSetter::accept))
              .ifString(str -> str.map(ThingParser::parseColor).handle(colorSetter::accept))
              .ifInteger(i -> i.handle(colorSetter))
              .typeError();
    }

    protected ObjValue parseCommon(JsonObject data, THING_BUILDER builder) {
        return JParse.begin(data)
              .ifKey("texture", val -> val.string().map(ResourceLocation::parse).handle(builder::texture))
              .ifKey("tint", val -> processColor(val, builder::tint))
              .ifKey("color_representation", val -> processColor(val, builder::colorRepresentation))
              .ifKey("attributes", val -> processAttribute(builder, val.obj()));
    }

    protected void processAttribute(THING_BUILDER builder, ObjValue rawAttribute) {
    }
}