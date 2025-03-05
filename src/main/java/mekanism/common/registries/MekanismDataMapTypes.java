package mekanism.common.registries;

import java.util.Optional;
import mekanism.api.MekanismAPI;
import mekanism.api.chemical.Chemical;
import mekanism.api.datamaps.chemical.ChemicalSolidTag;
import mekanism.api.datamaps.IMekanismDataMapTypes;
import mekanism.api.datamaps.MekaSuitAbsorption;
import mekanism.api.datamaps.chemical.attribute.ChemicalFuel;
import mekanism.api.datamaps.chemical.attribute.ChemicalRadioactivity;
import mekanism.api.datamaps.chemical.attribute.CooledCoolant;
import mekanism.api.datamaps.chemical.attribute.HeatedCoolant;
import mekanism.common.Mekanism;
import mekanism.common.registration.impl.DataMapTypeRegister;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.damagesource.DamageType;
import net.neoforged.neoforge.registries.datamaps.DataMapType;
import org.jetbrains.annotations.Nullable;

public class MekanismDataMapTypes implements IMekanismDataMapTypes {

    public static final DataMapTypeRegister REGISTER = new DataMapTypeRegister(Mekanism.MODID);

    private static final DataMapType<DamageType, MekaSuitAbsorption> MEKA_SUIT_ABSORPTION = REGISTER.registerSimple(MekaSuitAbsorption.ID, Registries.DAMAGE_TYPE, MekaSuitAbsorption.CODEC);
    private static final DataMapType<Chemical, ChemicalSolidTag> CHEMICAL_SOLID_TAG = REGISTER.registerSynced(ChemicalSolidTag.ID, MekanismAPI.CHEMICAL_REGISTRY_NAME,
          ChemicalSolidTag.CODEC, ChemicalSolidTag.SOLID_TAG_CODEC);

    //Chemical Attributes
    private static final DataMapType<Chemical, ChemicalFuel> CHEMICAL_FUEL = REGISTER.registerSimpleSynced(ChemicalFuel.ID, MekanismAPI.CHEMICAL_REGISTRY_NAME, ChemicalFuel.CODEC);
    private static final DataMapType<Chemical, ChemicalRadioactivity> CHEMICAL_RADIOACTIVITY = REGISTER.registerSynced(ChemicalRadioactivity.ID, MekanismAPI.CHEMICAL_REGISTRY_NAME,
          ChemicalRadioactivity.CODEC, ChemicalRadioactivity.RADIOACTIVITY_CODEC);
    private static final DataMapType<Chemical, CooledCoolant> COOLED_CHEMICAL_COOLANT = REGISTER.registerSimpleSynced(CooledCoolant.ID, MekanismAPI.CHEMICAL_REGISTRY_NAME, CooledCoolant.CODEC);
    private static final DataMapType<Chemical, HeatedCoolant> HEATED_CHEMICAL_COOLANT = REGISTER.registerSimpleSynced(HeatedCoolant.ID, MekanismAPI.CHEMICAL_REGISTRY_NAME, HeatedCoolant.CODEC);

    @Override
    public DataMapType<DamageType, MekaSuitAbsorption> mekaSuitAbsorption() {
        return MEKA_SUIT_ABSORPTION;
    }

    @Override
    public DataMapType<Chemical, ChemicalSolidTag> chemicalSolidTag() {
        return CHEMICAL_SOLID_TAG;
    }

    @Override
    public DataMapType<Chemical, ChemicalFuel> chemicalFuel() {
        return CHEMICAL_FUEL;
    }

    @Override
    public DataMapType<Chemical, ChemicalRadioactivity> chemicalRadioactivity() {
        return CHEMICAL_RADIOACTIVITY;
    }

    @Override
    public DataMapType<Chemical, CooledCoolant> cooledChemicalCoolant() {
        return COOLED_CHEMICAL_COOLANT;
    }

    @Override
    public DataMapType<Chemical, HeatedCoolant> heatedChemicalCoolant() {
        return HEATED_CHEMICAL_COOLANT;
    }

    @Nullable
    @Override
    public <TYPE, DATA> DATA getData(RegistryAccess registryAccess, ResourceKey<? extends Registry<? extends TYPE>> registryName, Holder<TYPE> holder, DataMapType<TYPE, DATA> type) {
        if (holder.kind() == Holder.Kind.REFERENCE) {
            //Reference holders can query data map values
            return holder.getData(type);
        }
        Optional<Registry<TYPE>> registry = registryAccess.registry(registryName);
        //noinspection OptionalIsPresent - Capturing lambda
        if (registry.isPresent()) {
            return registry.get().wrapAsHolder(holder.value()).getData(type);
        }
        return null;
    }
}
