package mekanism.api.datamaps;

import java.util.ServiceLoader;
import mekanism.api.MekanismAPI;
import mekanism.api.chemical.Chemical;
import mekanism.api.datamaps.chemical.attribute.ChemicalFuel;
import mekanism.api.datamaps.chemical.ChemicalOreTag;
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
    IMekanismDataMapTypes INSTANCE = ServiceLoader.load(IMekanismDataMapTypes.class).findFirst().orElseThrow(() -> new IllegalStateException("No valid ServiceImpl for IMekanismDataMapTypes found"));

    //TODO - 1.21: Docs for this and all other classes in this package and subpackages
    @Nullable
    <TYPE, DATA> DATA getData(RegistryAccess registryAccess, ResourceKey<? extends Registry<? extends TYPE>> registryName, Holder<TYPE> holder, DataMapType<TYPE, DATA> type);

    DataMapType<DamageType, MekaSuitAbsorption> mekaSuitAbsorption();

    @Nullable
    default MekaSuitAbsorption getMekaSuitAbsorption(RegistryAccess registryAccess, Holder<DamageType> holder) {
        return getData(registryAccess, Registries.DAMAGE_TYPE, holder, mekaSuitAbsorption());
    }

    DataMapType<Chemical, ChemicalOreTag> chemicalOreTag();

    @Nullable
    default ChemicalOreTag getChemicalOreTag(RegistryAccess registryAccess, Holder<Chemical> holder) {
        return getData(registryAccess, MekanismAPI.CHEMICAL_REGISTRY_NAME, holder, chemicalOreTag());
    }

    DataMapType<Chemical, ChemicalFuel> chemicalFuel();

    @Nullable
    default ChemicalFuel getChemicalFuel(RegistryAccess registryAccess, Holder<Chemical> holder) {
        return getData(registryAccess, MekanismAPI.CHEMICAL_REGISTRY_NAME, holder, chemicalFuel());
    }

    DataMapType<Chemical, ChemicalRadioactivity> chemicalRadioactivity();

    @Nullable
    default ChemicalRadioactivity getChemicalRadioactivity(RegistryAccess registryAccess, Holder<Chemical> holder) {
        return getData(registryAccess, MekanismAPI.CHEMICAL_REGISTRY_NAME, holder, chemicalRadioactivity());
    }

    DataMapType<Chemical, CooledCoolant> cooledChemicalCoolant();

    @Nullable
    default CooledCoolant getCooledChemicalCoolant(RegistryAccess registryAccess, Holder<Chemical> holder) {
        return getData(registryAccess, MekanismAPI.CHEMICAL_REGISTRY_NAME, holder, cooledChemicalCoolant());
    }

    DataMapType<Chemical, HeatedCoolant> heatedChemicalCoolant();

    @Nullable
    default HeatedCoolant getHeatedChemicalCoolant(RegistryAccess registryAccess, Holder<Chemical> holder) {
        return getData(registryAccess, MekanismAPI.CHEMICAL_REGISTRY_NAME, holder, heatedChemicalCoolant());
    }
}