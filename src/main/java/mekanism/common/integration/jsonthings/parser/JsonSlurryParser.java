package mekanism.common.integration.jsonthings.parser;

import com.google.gson.JsonObject;
import java.util.function.Consumer;
import mekanism.api.MekanismAPI;
import mekanism.api.annotations.NothingNullByDefault;
import mekanism.api.chemical.ChemicalType;
import mekanism.api.chemical.slurry.Slurry;
import mekanism.api.chemical.slurry.SlurryBuilder;
import mekanism.common.integration.jsonthings.builder.JsonSlurryBuilder;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.bus.api.IEventBus;

@NothingNullByDefault
public class JsonSlurryParser extends JsonChemicalParser<Slurry, SlurryBuilder, JsonSlurryBuilder> {

    public JsonSlurryParser(IEventBus bus) {
        super(bus, ChemicalType.SLURRY, "Slurry", MekanismAPI.SLURRY_REGISTRY_NAME);
    }

    @Override
    protected JsonSlurryBuilder processThing(ResourceLocation key, JsonObject data, Consumer<JsonSlurryBuilder> builderModification) {
        JsonSlurryBuilder builder = new JsonSlurryBuilder(this, key);
        parseCommon(data, builder)
              .ifKey("clean", val -> val.bool().handle(builder::clean))
              .ifKey("ore", val -> val.string().map(ResourceLocation::parse).handle(builder::ore))
        ;
        builderModification.accept(builder);
        return builder;
    }
}