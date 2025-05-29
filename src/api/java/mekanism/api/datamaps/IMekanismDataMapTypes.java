package mekanism.api.datamaps;

import mekanism.api.MekanismAPI;
import mekanism.api.chemical.Chemical;
import mekanism.api.datamaps.chemical.ChemicalSolidTag;
import mekanism.api.datamaps.chemical.attribute.ChemicalFuel;
import mekanism.api.datamaps.chemical.attribute.ChemicalRadioactivity;
import mekanism.api.datamaps.chemical.attribute.CooledCoolant;
import mekanism.api.datamaps.chemical.attribute.HeatedCoolant;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.damagesource.DamageType;
import net.neoforged.neoforge.registries.datamaps.DataMapType;
import org.jetbrains.annotations.Nullable;

/**
 * Helper class to provide access to Mekanism's data map types.
 *
 * @since 10.7.11
 */
public interface IMekanismDataMapTypes {

    /**
     * Provides access to Mekanism's data map types.
     */
    IMekanismDataMapTypes INSTANCE = MekanismAPI.getService(IMekanismDataMapTypes.class);

    /**
     * Helper to get data from a holder. This method supports both reference and direct holders.
     *
     * @param registryAccess Registry access to look up the reference if a direct holder was provided.
     * @param registryName   Name of the registry that contains the holder.
     * @param holder         Holder to query.
     * @param type           Type of data to lookup.
     *
     * @return Absorption values or null if there are no absorption values defined for the damage type.
     */
    @Nullable
    <TYPE, DATA> DATA getData(RegistryAccess registryAccess, ResourceKey<? extends Registry<? extends TYPE>> registryName, Holder<TYPE> holder, DataMapType<TYPE, DATA> type);

    /**
     * The {@linkplain DamageType} data map that defines how much of a particular damage type the MekaSuit can absorb.
     * <p>
     * The location of this data map is {@code mekanism/data_maps/damage_type/mekasuit_absorption.json}, and the values are objects with 1 field:
     * <ul>
     * <li>{@code absorption}, a float between zero and one inclusive - defines the ratio of the given damage type the MekaSuit can absorb</li>
     * </ul>
     *
     * The use of a float as the value is also possible, though discouraged in case more options are added in the future.
     *
     * @implNote This data map is not synced to the client.
     */
    DataMapType<DamageType, MekaSuitAbsorption> mekaSuitAbsorption();

    /**
     * Helper to get the MekaSuit absorption data from a damage type holder. This method supports both reference and direct holders.
     *
     * @param registryAccess Registry access to look up the damage type if a direct holder was provided.
     * @param holder         Damage Type.
     *
     * @return Absorption values or null if there are no absorption values defined for the damage type.
     */
    @Nullable
    default MekaSuitAbsorption getMekaSuitAbsorption(RegistryAccess registryAccess, Holder<DamageType> holder) {
        return getData(registryAccess, Registries.DAMAGE_TYPE, holder, mekaSuitAbsorption());
    }

    /**
     * The {@linkplain Chemical} data map that defines how radioactive a chemical is.
     * <p>
     * The location of this data map is {@code mekanism/data_maps/mekanism/chemical/chemical_solid_tag.json}, and the values are objects with 1 field:
     * <ul>
     * <li>{@code representation}, an item tag key - the item representations of a chemical for display in a chemical crystallizer</li>
     * </ul>
     *
     * The use of a tag key as the value is also possible, though discouraged in case more options are added in the future.
     */
    DataMapType<Chemical, ChemicalSolidTag> chemicalSolidTag();

    /**
     * The {@linkplain Chemical} data map that defines fuel properties of a chemical.
     * <p>
     * The location of this data map is {@code mekanism/data_maps/mekanism/chemical/chemical_attribute_fuel.json}, and the values are objects with 2 fields:
     * <ul>
     * <li>{@code burn_time}, a positive integer - how long the fuel will burn, in ticks</li>
     * <li>{@code energy}, a positive long - how much energy will the fuel produce each tick of its burn</li>
     * </ul>
     */
    DataMapType<Chemical, ChemicalFuel> chemicalFuel();

    /**
     * The {@linkplain Chemical} data map that defines how radioactive a chemical is.
     * <p>
     * The location of this data map is {@code mekanism/data_maps/mekanism/chemical/chemical_attribute_radioactivity.json}, and the values are objects with 1 field:
     * <ul>
     * <li>{@code radioactivity}, a double greater than or equal the baseline radiation - radioactivity of the chemical measured in Sv/h</li>
     * </ul>
     *
     * The use of a double as the value is also possible, though discouraged in case more options are added in the future.
     */
    DataMapType<Chemical, ChemicalRadioactivity> chemicalRadioactivity();

    /**
     * The {@linkplain Chemical} data map that defines coolant properties of a chemical.
     * <p>
     * The location of this data map is {@code mekanism/data_maps/mekanism/chemical/chemical_attribute_cooled_coolant.json}, and the values are objects with 3 fields:
     * <ul>
     * <li>{@code hot_variant}, a chemical holder - the registry name of the hot variant of this coolant</li>
     * <li>{@code thermal_enthalpy}, a positive double - the amount of energy one mB of the chemical can store; lower values will cause reactors to require more of the
     * chemical to stay cool</li>
     * <li>{@code conductivity}, a positive double that is at most one - the proportion of a reactor's available heat that can be used at an instant to convert this
     * coolant's cool variant to its heated variant</li>
     * </ul>
     *
     * @apiNote While having the coolant reference itself as a target works, it is highly discouraged.
     */
    DataMapType<Chemical, CooledCoolant> cooledChemicalCoolant();

    /**
     * The {@linkplain Chemical} data map that defines heated coolant properties of a chemical.
     * <p>
     * The location of this data map is {@code mekanism/data_maps/mekanism/chemical/chemical_attribute_heated_coolant.json}, and the values are objects with four fields:
     * <ul>
     * <li>{@code cool_variant}, a chemical holder - the registry name of the cold variant of this coolant</li>
     * <li>{@code thermal_enthalpy}, a positive double - the amount of energy one mB of the chemical can store; lower values will cause boilers to require more of the
     * chemical to produce steam</li>
     * <li>{@code conductivity}, a positive double that is at most one; optional, defaults to 0.4 - the proportion of this coolant that can be used at an instant to heat up a boiler and in turn
     * convert this coolant to its cool variant</li>
     * <li>{@code temperature}, a positive double that is at most 1,000,000; optional, defaults to 100,000 - the temperature of this heated coolant that is used in calculating the difference between the boiler's heat and the coolant when determining how much heat can be extracted at once</li>
     * </ul>
     *
     * @apiNote While having the coolant reference itself as a target works, it is highly discouraged.
     */
    DataMapType<Chemical, HeatedCoolant> heatedChemicalCoolant();
}