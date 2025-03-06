package mekanism.common.integration.jsonthings;

import com.google.gson.JsonObject;
import dev.gigaherz.jsonthings.things.builders.BaseBuilder;
import dev.gigaherz.jsonthings.things.parsers.ThingParseException;
import dev.gigaherz.jsonthings.things.parsers.ThingParser;
import dev.gigaherz.jsonthings.util.parse.JParse;
import dev.gigaherz.jsonthings.util.parse.function.ObjValueFunction;
import dev.gigaherz.jsonthings.util.parse.value.Any;
import dev.gigaherz.jsonthings.util.parse.value.ObjValue;
import java.util.function.Consumer;
import java.util.function.IntConsumer;
import mekanism.api.MekanismAPI;
import mekanism.api.annotations.NothingNullByDefault;
import mekanism.api.chemical.Chemical;
import mekanism.api.chemical.attribute.ChemicalAttributes;
import mekanism.api.radiation.IRadiationManager;
import mekanism.common.Mekanism;
import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.RegisterEvent;
import org.jetbrains.annotations.Nullable;

@NothingNullByDefault
public class JsonChemicalParser extends ThingParser<JsonChemicalBuilder> {

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
    @SuppressWarnings("removal")
    protected JsonChemicalBuilder processThing(ResourceLocation key, JsonObject data, Consumer<JsonChemicalBuilder> builderModification) {
        JsonChemicalBuilder builder = new JsonChemicalBuilder(this, key);
        JParse.begin(data)
              .ifKey("texture", val -> val.string().map(ResourceLocation::parse).handle(builder::texture))
              .ifKey("ore", val -> val.string().map(ResourceLocation::parse).handle(builder::ore))
              .ifKey("tint", val -> processColor(val, builder::tint))
              .ifKey("color_representation", val -> processColor(val, builder::colorRepresentation))
              .ifKey("attributes", val -> processAttribute(builder, val.obj()));
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

    @SuppressWarnings("removal")
    private void processAttribute(JsonChemicalBuilder builder, ObjValue rawAttribute) {
        //Note: We chain ifKeys here as while there shouldn't be an overlap as it doesn't make sense, there is also nothing wrong
        // with allowing multiple attribute types to be defined in each block
        rawAttribute.ifKey("radioactivity", attribute -> attribute.doubleValue()
              .min(IRadiationManager.INSTANCE.minRadiationMagnitude())
              .handle(radioactivity -> builder.with(new ChemicalAttributes.Radiation(radioactivity)))
        ).ifKey("coolant", attribute -> {
            ObjValue coolant = attribute.obj();
            boolean hasCooledGas = coolant.hasKey("cooled_gas");
            boolean hasHeatedGas = coolant.hasKey("heated_gas");
            if (hasCooledGas == hasHeatedGas) {
                //Error out if we are missing a cooled or heated gas or if both are declared
                if (hasCooledGas) {
                    throw new ThingParseException("Coolants cannot declare both a cooled and heated gas");
                }
                throw new ThingParseException("Coolants must have either a 'cooled_gas' or a 'heated_gas'");
            }
            CoolantData coolantData = new CoolantData();
            coolant.key("thermal_enthalpy", thermalEnthalpy -> thermalEnthalpy.doubleValue().handle(enthalpy -> coolantData.thermalEnthalpy = enthalpy))
                  .key("conductivity", conductivity -> conductivity.doubleValue().handle(c -> coolantData.conductivity = c))
                  .key(hasCooledGas ? "cooled_gas" : "heated_gas", gas -> gas.string().map(ResourceLocation::parse).handle(g -> coolantData.gas = g));
            //We know we set it in one of the two cases
            Holder<Chemical> otherVariantHolder = DeferredHolder.create(MekanismAPI.CHEMICAL_REGISTRY_NAME, coolantData.gas);
            if (otherVariantHolder.is(MekanismAPI.EMPTY_CHEMICAL_KEY)) {
                throw new ThingParseException("Coolants cannot be created pointing to the empty chemical");
            }
            if (hasCooledGas) {
                builder.with(new ChemicalAttributes.HeatedCoolant(otherVariantHolder, coolantData.thermalEnthalpy));
            } else {
                builder.with(new ChemicalAttributes.CooledCoolant(otherVariantHolder, coolantData.thermalEnthalpy, coolantData.conductivity));
            }
        }).ifKey("fuel", attribute -> {
            FuelData fuelData = new FuelData();
            attribute.obj()
                  .key("burn_ticks", burnTicks -> burnTicks.intValue().min(1).handle(ticks -> fuelData.burnTicks = ticks))
                  .key("energy_density", energyDensity -> energyDensity
                        .ifString(string -> string.map(Long::parseLong).handle(fuelData::setEnergyDensity))
                        .ifLong(l -> l.min(1).handle(fuelData::setEnergyDensity))
                        .typeError()
                  );
            builder.with(new ChemicalAttributes.Fuel(fuelData.burnTicks, fuelData.energyDensity));
        });
    }

    private static class CoolantData {

        @Nullable
        private ResourceLocation gas;
        private double thermalEnthalpy;
        private double conductivity;
    }

    private static class FuelData {

        private long energyDensity = 0;
        private int burnTicks;

        private void setEnergyDensity(long energyDensity) {
            this.energyDensity = energyDensity;
        }
    }
}