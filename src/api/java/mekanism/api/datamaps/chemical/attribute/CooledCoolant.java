package mekanism.api.datamaps.chemical.attribute;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import mekanism.api.MekanismAPI;
import mekanism.api.SerializationConstants;
import mekanism.api.chemical.Chemical;
import mekanism.api.chemical.ChemicalStack;
import mekanism.api.chemical.attribute.ChemicalAttributes;
import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.ApiStatus.Internal;

/**
 * A {@link MekanismAPI#CHEMICAL_REGISTRY chemical} data map that allows defining fuel values for a chemical.
 *
 * @param otherVariant    Chemical representing the heated variant of this cooled coolant.
 * @param thermalEnthalpy Defines how much energy one mB of the chemical can store; lower values will cause reactors to require more of the chemical to stay cool. Must be
 *                        greater than zero.
 * @param conductivity    Defines the proportion of a reactor's available heat that can be used at an instant to convert this coolant's cool variant to its heated
 *                        variant. This value should be greater than zero, and at most one.
 *
 * @since 10.7.11
 */
public record CooledCoolant(Holder<Chemical> otherVariant, double thermalEnthalpy, double conductivity) implements IChemicalCoolant {

    /**
     * The ID of the data map.
     *
     * @see mekanism.api.datamaps.IMekanismDataMapTypes#cooledChemicalCoolant()
     */
    public static final ResourceLocation ID = ResourceLocation.fromNamespaceAndPath(MekanismAPI.MEKANISM_MODID, "chemical_attribute_cooled_coolant");

    /**
     * Codec for serializing and deserializing cooled coolants.
     */
    public static final Codec<CooledCoolant> CODEC = RecordCodecBuilder.create(instance -> IChemicalCoolant.createBaseCodec(instance,
          SerializationConstants.HOT_VARIANT, 1
    ).apply(instance, CooledCoolant::new));

    public CooledCoolant {
        IChemicalCoolant.validateCoolantParams(otherVariant, thermalEnthalpy, conductivity);
    }

    /**
     * Produce the given amount of the hot variant of this coolant.
     *
     * @param amountHeated Amount of coolant to heat.
     *
     * @return Chemical stack representing the heated coolant.
     */
    public ChemicalStack heat(long amountHeated) {
        return new ChemicalStack(otherVariant, amountHeated);
    }

    @Internal
    @Override
    @SuppressWarnings("removal")
    public ChemicalAttributes.CooledCoolant toLegacyAttribute() {
        return new ChemicalAttributes.CooledCoolant(this);
    }
}
