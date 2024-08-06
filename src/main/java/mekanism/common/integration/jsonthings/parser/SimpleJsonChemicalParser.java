package mekanism.common.integration.jsonthings.parser;

import com.google.gson.JsonObject;
import dev.gigaherz.jsonthings.things.parsers.ThingParser;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import mekanism.api.annotations.NothingNullByDefault;
import mekanism.api.chemical.Chemical;
import mekanism.api.chemical.ChemicalType;
import mekanism.common.integration.jsonthings.builder.JsonChemicalBuilder;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.bus.api.IEventBus;

@NothingNullByDefault
public class SimpleJsonChemicalParser<
      THING_BUILDER extends JsonChemicalBuilder<THING_BUILDER>> extends JsonChemicalParser<THING_BUILDER> {

    private final BiFunction<ThingParser<THING_BUILDER>, ResourceLocation, THING_BUILDER> builderFunction;

    SimpleJsonChemicalParser(IEventBus bus, ChemicalType chemicalType, String thingType, ResourceKey<? extends Registry<Chemical>> registryKey,
          BiFunction<ThingParser<THING_BUILDER>, ResourceLocation, THING_BUILDER> builderFunction) {
        super(bus, chemicalType, thingType, registryKey);
        this.builderFunction = builderFunction;
    }

    @Override
    protected THING_BUILDER processThing(ResourceLocation key, JsonObject data, Consumer<THING_BUILDER> builderModification) {
        THING_BUILDER builder = builderFunction.apply(this, key);
        parseCommon(data, builder);
        builderModification.accept(builder);
        return builder;
    }
}