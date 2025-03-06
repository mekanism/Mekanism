package mekanism.api.datamaps.chemical.attribute;

import com.mojang.datafixers.Products;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.mojang.serialization.codecs.RecordCodecBuilder.Mu;
import java.util.List;
import mekanism.api.MekanismAPI;
import mekanism.api.SerializationConstants;
import mekanism.api.chemical.Chemical;
import mekanism.api.chemical.ChemicalStack;
import mekanism.api.math.MathUtils;
import mekanism.api.text.APILang;
import mekanism.api.text.EnumColor;
import mekanism.api.text.ITooltipHelper;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item.TooltipContext;
import net.minecraft.world.item.TooltipFlag;

/**
 * Represents the base information that coolants keep track of.
 *
 * @since 10.7.11
 */
public sealed interface IChemicalCoolant extends IChemicalAttribute permits CooledCoolant, HeatedCoolant {
    //TODO - 1.22: Do we want to allow applying coolants to fluids so that we can define water directly that way?

    /**
     * Gets the thermal enthalpy of this coolant. Thermal Enthalpy defines how much energy one mB of the chemical can store.
     */
    double thermalEnthalpy();

    /**
     * Gets the conductivity of this coolant. 'Conductivity' defines the proportion of a reactor's available heat that can be used at an instant to convert this coolant's
     * cool variant to its heated variant.
     */
    double conductivity();

    /**
     * Gets the other chemical this coolant transforms into after it undergoes a temperature change.
     */
    Holder<Chemical> otherVariant();

    @Override
    default void collectTooltips(TooltipContext context, List<Component> tooltips, TooltipFlag tooltipFlag) {
        ITooltipHelper tooltipHelper = ITooltipHelper.INSTANCE;
        tooltips.add(APILang.CHEMICAL_ATTRIBUTE_COOLANT_EFFICIENCY.translateColored(EnumColor.GRAY, EnumColor.INDIGO, tooltipHelper.getPercent(conductivity())));
        tooltips.add(APILang.CHEMICAL_ATTRIBUTE_COOLANT_ENTHALPY.translateColored(EnumColor.GRAY, EnumColor.INDIGO,
              tooltipHelper.getEnergyPerMBDisplayShort(MathUtils.clampToLong(thermalEnthalpy()))));
    }

    static <COOLANT extends IChemicalCoolant> Products.P3<Mu<COOLANT>, Holder<Chemical>, Double, Double> createBaseCodec(RecordCodecBuilder.Instance<COOLANT> instance,
          String otherFormName, double defaultConductivity) {
        return instance.group(
              ChemicalStack.CHEMICAL_NON_EMPTY_HOLDER_CODEC.fieldOf(otherFormName).forGetter(IChemicalCoolant::otherVariant),
              Codec.doubleRange(Double.MIN_VALUE, Double.MAX_VALUE).fieldOf(SerializationConstants.THERMAL_ENTHALPY).forGetter(IChemicalCoolant::thermalEnthalpy),
              Codec.doubleRange(Double.MIN_VALUE, 1).optionalFieldOf(SerializationConstants.CONDUCTIVITY, defaultConductivity).forGetter(IChemicalCoolant::conductivity)
        );
    }

    /**
     * Validates that the parameters are valid as values in coolants.
     *
     * @param otherVariant Must not represent the empty chemical.
     * @param thermalEnthalpy Must be greater than zero.
     * @param conductivity    This value should be greater than zero, and at most one.
     *
     * @throws IllegalArgumentException If thermal enthalpy or conductivity are invalid values.
     */
    static void validateCoolantParams(Holder<Chemical> otherVariant, double thermalEnthalpy, double conductivity) {
        if (otherVariant.is(MekanismAPI.EMPTY_CHEMICAL_KEY)) {
            throw new IllegalArgumentException("Coolants can not be made that point to the empty chemical");
        } else if (thermalEnthalpy <= 0) {
            throw new IllegalArgumentException("Coolant attributes must have a thermal enthalpy greater than zero! Thermal Enthalpy: " + thermalEnthalpy);
        } else if (conductivity <= 0 || conductivity > 1) {
            throw new IllegalArgumentException("Coolant attributes must have a conductivity greater than zero and at most one! Conductivity: " + conductivity);
        }
    }
}