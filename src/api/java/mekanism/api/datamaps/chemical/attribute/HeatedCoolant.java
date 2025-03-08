package mekanism.api.datamaps.chemical.attribute;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import mekanism.api.MekanismAPI;
import mekanism.api.SerializationConstants;
import mekanism.api.chemical.Chemical;
import mekanism.api.chemical.ChemicalStack;
import mekanism.api.chemical.attribute.ChemicalAttributes;
import mekanism.api.text.APILang;
import mekanism.api.text.EnumColor;
import mekanism.api.text.ITooltipHelper;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item.TooltipContext;
import net.minecraft.world.item.TooltipFlag;
import org.jetbrains.annotations.ApiStatus.Internal;

/**
 * A {@link MekanismAPI#CHEMICAL_REGISTRY chemical} data map that allows defining fuel values for a chemical.
 *
 * @param otherVariant    Chemical representing the cooled variant of this heated coolant.
 * @param thermalEnthalpy Defines how much energy one mB of the chemical can store; lower values will cause boilers to require more of the chemical to produce steam. Must
 *                        be greater than zero.
 * @param conductivity    Defines the proportion of this coolant that can be used at an instant to heat up a boiler and in turn convert this coolant to its cool variant.
 *                        This value should be greater than zero, and at most one.
 * @param temperature     Defines the temperature of this heated coolant that is used in calculating the difference between the boiler's heat and the coolant when
 *                        determining how much heat can be extracted at once. This value should be greater than zero, and at most 1,000,000.
 *
 * @since 10.7.11
 */
public record HeatedCoolant(Holder<Chemical> otherVariant, double thermalEnthalpy, double conductivity, double temperature) implements IChemicalCoolant {

    private static final double BASE_COOLING_EFFICIENCY = 0.4;
    private static final double BASE_COOLANT_TEMP = 100_000;
    //TODO: Evaluate if this is a reasonable max and if not or if people need it higher, potentially raise it
    private static final double MAX_COOLANT_TEMP = 1_000_000;

    /**
     * The ID of the data map.
     *
     * @see mekanism.api.datamaps.IMekanismDataMapTypes#heatedChemicalCoolant()
     */
    public static final ResourceLocation ID = ResourceLocation.fromNamespaceAndPath(MekanismAPI.MEKANISM_MODID, "chemical_attribute_heated_coolant");

    /**
     * Codec for serializing and deserializing heated coolants.
     */
    public static final Codec<HeatedCoolant> CODEC = RecordCodecBuilder.create(instance -> IChemicalCoolant.createBaseCodec(instance,
          SerializationConstants.COOL_VARIANT, BASE_COOLING_EFFICIENCY
    ).and(
          Codec.doubleRange(Double.MIN_VALUE, MAX_COOLANT_TEMP).optionalFieldOf(SerializationConstants.TEMPERATURE, BASE_COOLANT_TEMP).forGetter(HeatedCoolant::temperature)
    ).apply(instance, HeatedCoolant::new));

    public HeatedCoolant(Holder<Chemical> otherVariant, double thermalEnthalpy) {
        this(otherVariant, thermalEnthalpy, BASE_COOLING_EFFICIENCY, BASE_COOLANT_TEMP);
    }

    public HeatedCoolant {
        IChemicalCoolant.validateCoolantParams(otherVariant, thermalEnthalpy, conductivity);
        if (temperature <= 0 || temperature > MAX_COOLANT_TEMP) {
            throw new IllegalArgumentException("Coolant attributes must have a temperature greater than zero and at most " + MAX_COOLANT_TEMP + "! Temperature: " + temperature);
        }
    }

    /**
     * Produce the given amount of the cold variant of this coolant.
     *
     * @param amountCooled Amount of heated coolant to cool.
     *
     * @return Chemical stack representing the cooled coolant.
     */
    public ChemicalStack cool(long amountCooled) {
        return new ChemicalStack(otherVariant, amountCooled);
    }

    @Override
    public void collectTooltips(TooltipContext context, List<Component> tooltips, TooltipFlag tooltipFlag) {
        IChemicalCoolant.super.collectTooltips(context, tooltips, tooltipFlag);
        tooltips.add(APILang.CHEMICAL_ATTRIBUTE_COOLANT_TEMPERATURE.translateColored(EnumColor.GRAY, EnumColor.INDIGO, ITooltipHelper.INSTANCE.getTemperatureDisplayShort(temperature)));
    }

    @Internal
    @Override
    @SuppressWarnings("removal")
    public ChemicalAttributes.HeatedCoolant toLegacyAttribute() {
        return new ChemicalAttributes.HeatedCoolant(this);
    }
}
