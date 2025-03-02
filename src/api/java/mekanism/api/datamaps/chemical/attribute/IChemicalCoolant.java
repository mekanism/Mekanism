package mekanism.api.datamaps.chemical.attribute;

import com.mojang.datafixers.Products;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.mojang.serialization.codecs.RecordCodecBuilder.Mu;
import java.util.List;
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

//TODO - 1.22: Do we want to allow applying coolants to fluids so that we can define water directly that way?
public sealed interface IChemicalCoolant extends IChemicalAttribute permits CooledCoolant, HeatedCoolant {

    /**
     * Gets the thermal enthalpy of this coolant. Thermal Enthalpy defines how much energy one mB of the chemical can store.
     */
    double thermalEnthalpy();

    /**
     * Gets the conductivity of this coolant. 'Conductivity' defines the proportion of a reactor's available heat that can be used at an instant to convert this coolant's
     * cool variant to its heated variant.
     */
    double conductivity();

    Holder<Chemical> otherVariant();

    @Override
    default void collectTooltips(TooltipContext context, List<Component> tooltips, TooltipFlag tooltipFlag) {
        ITooltipHelper tooltipHelper = ITooltipHelper.INSTANCE;
        tooltips.add(APILang.CHEMICAL_ATTRIBUTE_COOLANT_EFFICIENCY.translateColored(EnumColor.GRAY, EnumColor.INDIGO, tooltipHelper.getPercent(conductivity())));
        tooltips.add(APILang.CHEMICAL_ATTRIBUTE_COOLANT_ENTHALPY.translateColored(EnumColor.GRAY, EnumColor.INDIGO,
              tooltipHelper.getEnergyPerMBDisplayShort(MathUtils.clampToLong(thermalEnthalpy()))));
    }

    static <COOLANT extends IChemicalCoolant> Products.P3<Mu<COOLANT>, Holder<Chemical>, Double, Double> createBaseCodec(RecordCodecBuilder.Instance<COOLANT> instance,
          String otherFormName) {
        //TODO - 1.21: Figure out how to prevent the chemical from referencing itself
        return instance.group(
              ChemicalStack.CHEMICAL_NON_EMPTY_HOLDER_CODEC.fieldOf(otherFormName).forGetter(IChemicalCoolant::otherVariant),
              Codec.doubleRange(Double.MIN_VALUE, Double.MAX_VALUE).fieldOf(SerializationConstants.THERMAL_ENTHALPY).forGetter(IChemicalCoolant::thermalEnthalpy),
              Codec.doubleRange(Double.MIN_VALUE, 1).fieldOf(SerializationConstants.CONDUCTIVITY).forGetter(IChemicalCoolant::conductivity)
        );
    }

    //TODO - 1.22: Move the non empty holder check to here
    static void validateCoolantParams(double thermalEnthalpy, double conductivity) {
        if (thermalEnthalpy <= 0) {
            throw new IllegalArgumentException("Coolant attributes must have a thermal enthalpy greater than zero! Thermal Enthalpy: " + thermalEnthalpy);
        } else if (conductivity <= 0 || conductivity > 1) {
            throw new IllegalArgumentException("Coolant attributes must have a conductivity greater than zero and at most one! Conductivity: " + conductivity);
        }
    }
}