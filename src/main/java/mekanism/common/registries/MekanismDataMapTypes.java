package mekanism.common.registries;

import java.util.List;
import java.util.Optional;
import mekanism.api.MekanismRegistries;
import mekanism.api.chemical.Chemical;
import mekanism.api.datamaps.IMekanismDataMapTypes;
import mekanism.api.datamaps.MekaSuitAbsorption;
import mekanism.api.datamaps.chemical.ChemicalSolidTag;
import mekanism.api.datamaps.chemical.attribute.ChemicalFuel;
import mekanism.api.datamaps.chemical.attribute.ChemicalRadioactivity;
import mekanism.api.datamaps.chemical.attribute.CooledCoolant;
import mekanism.api.datamaps.chemical.attribute.HeatedCoolant;
import mekanism.api.datamaps.chemical.attribute.IChemicalAttribute;
import mekanism.api.datamaps.holderset.DataMapHolderSetRemover;
import mekanism.api.gear.ModuleData;
import mekanism.common.Mekanism;
import mekanism.common.registration.impl.DataMapTypeRegister;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.item.Item;
import net.neoforged.neoforge.registries.datamaps.AdvancedDataMapType;
import net.neoforged.neoforge.registries.datamaps.DataMapType;
import org.jspecify.annotations.Nullable;

public class MekanismDataMapTypes implements IMekanismDataMapTypes {

    public static final DataMapTypeRegister REGISTER = new DataMapTypeRegister(Mekanism.MODID);

    private static final DataMapType<DamageType, MekaSuitAbsorption> MEKA_SUIT_ABSORPTION = REGISTER.registerSimple(MekaSuitAbsorption.ID, Registries.DAMAGE_TYPE, MekaSuitAbsorption.CODEC);
    private static final AdvancedDataMapType<Item, HolderSet<ModuleData<?>>, DataMapHolderSetRemover<Item, ModuleData<?>>> SUPPORTED_MODULES = REGISTER.registerSyncedHolderSet(
          Mekanism.rl("supported_modules"), Registries.ITEM, MekanismRegistries.Keys.MODULES, MekanismRegistries.MODULES.holderByNameCodec());

    private static final DataMapType<Chemical, ChemicalSolidTag> CHEMICAL_SOLID_TAG = REGISTER.registerSynced(ChemicalSolidTag.ID, MekanismRegistries.Keys.CHEMICAL,
          ChemicalSolidTag.CODEC, ChemicalSolidTag.SOLID_TAG_CODEC);

    //Chemical Attributes
    private static final DataMapType<Chemical, ChemicalFuel> CHEMICAL_FUEL = REGISTER.registerSimpleSynced(ChemicalFuel.ID, MekanismRegistries.Keys.CHEMICAL, ChemicalFuel.CODEC);
    private static final DataMapType<Chemical, ChemicalRadioactivity> CHEMICAL_RADIOACTIVITY = REGISTER.registerSynced(ChemicalRadioactivity.ID, MekanismRegistries.Keys.CHEMICAL,
          ChemicalRadioactivity.CODEC, ChemicalRadioactivity.RADIOACTIVITY_CODEC);
    private static final DataMapType<Chemical, CooledCoolant> COOLED_CHEMICAL_COOLANT = REGISTER.registerSimpleSynced(CooledCoolant.ID, MekanismRegistries.Keys.CHEMICAL, CooledCoolant.CODEC);
    private static final DataMapType<Chemical, HeatedCoolant> HEATED_CHEMICAL_COOLANT = REGISTER.registerSimpleSynced(HeatedCoolant.ID, MekanismRegistries.Keys.CHEMICAL, HeatedCoolant.CODEC);
    //TODO - 26.2: Figure out how to make this extensible. Maybe just loop after the data map registry finishes being populated and do instance checks?
    private static final List<DataMapType<Chemical, ? extends IChemicalAttribute>> ATTRIBUTE_TYPES = List.of(
          CHEMICAL_RADIOACTIVITY,
          CHEMICAL_FUEL,
          COOLED_CHEMICAL_COOLANT,
          HEATED_CHEMICAL_COOLANT
    );

    @Override
    public DataMapType<DamageType, MekaSuitAbsorption> mekaSuitAbsorption() {
        return MEKA_SUIT_ABSORPTION;
    }

    @Override
    public AdvancedDataMapType<Item, HolderSet<ModuleData<?>>, DataMapHolderSetRemover<Item, ModuleData<?>>> supportedModules() {
        return SUPPORTED_MODULES;
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
    public <TYPE, DATA> DATA getData(@Nullable RegistryAccess registryAccess, ResourceKey<? extends Registry<? extends TYPE>> registryName, Holder<TYPE> holder, DataMapType<TYPE, DATA> type) {
        if (holder.kind() == Holder.Kind.REFERENCE) {
            //Reference holders can query data map values
            return holder.getData(type);
        } else if (registryAccess == null) {
            return null;
        }
        Optional<Registry<TYPE>> registry = registryAccess.lookup(registryName);
        //noinspection OptionalIsPresent - Capturing lambda
        if (registry.isPresent()) {
            return registry.get().wrapAsHolder(holder.value()).getData(type);
        }
        return null;
    }

    @Override
    public List<DataMapType<Chemical, ? extends IChemicalAttribute>> chemicalAttributeTypes() {
        return ATTRIBUTE_TYPES;
    }
}
