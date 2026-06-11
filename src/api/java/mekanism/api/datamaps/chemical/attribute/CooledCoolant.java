package mekanism.api.datamaps.chemical.attribute;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import mekanism.api.MekanismAPI;
import mekanism.api.SerializationConstants;
import mekanism.api.chemical.Chemical;
import mekanism.api.chemical.ChemicalResource;
import net.minecraft.core.Holder;
import net.minecraft.resources.Identifier;

/// A [`chemical`][MekanismAPI#CHEMICAL_REGISTRY] data map that allows defining fuel values for a chemical.
///
/// @param otherVariant    Chemical representing the heated variant of this cooled coolant.
/// @param thermalEnthalpy Defines how much energy one mB of the chemical can store; lower values will cause reactors to require more of the chemical to stay cool. Must
/// be greater than zero.
/// @param conductivity    Defines the proportion of a reactor's available heat that can be used at an instant to convert this coolant's cool variant to its heated
/// variant. This value should be greater than zero, and at most one.
///
/// @since 10.7.11
public record CooledCoolant(Holder<Chemical> otherVariant, double thermalEnthalpy, double conductivity) implements IChemicalCoolant {

    /// The ID of the data map.
    ///
    /// @see mekanism.api.datamaps.IMekanismDataMapTypes#cooledChemicalCoolant()
    public static final Identifier ID = Identifier.fromNamespaceAndPath(MekanismAPI.MEKANISM_MODID, "chemical_attribute_cooled_coolant");

    /// Codec for serializing and deserializing cooled coolants.
    public static final Codec<CooledCoolant> CODEC = RecordCodecBuilder.create(instance -> IChemicalCoolant.createBaseCodec(instance,
          SerializationConstants.HOT_VARIANT, 1
    ).apply(instance, CooledCoolant::new));

    public CooledCoolant {
        IChemicalCoolant.validateCoolantParams(otherVariant, thermalEnthalpy, conductivity);
    }

    /// {@return a chemical resource representing the type of the heated coolant}
    public ChemicalResource heat() {
        return ChemicalResource.of(otherVariant);
    }
}
