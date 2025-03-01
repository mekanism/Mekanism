package mekanism.common.registries;

import java.util.Optional;
import mekanism.api.MekanismAPI;
import mekanism.api.chemical.Chemical;
import mekanism.api.datamaps.ChemicalOreTag;
import mekanism.api.datamaps.IMekanismDataMapTypes;
import mekanism.api.datamaps.MekaSuitAbsorption;
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
    private static final DataMapType<Chemical, ChemicalOreTag> CHEMICAL_ORE_TAG = REGISTER.register(ChemicalOreTag.ID, MekanismAPI.CHEMICAL_REGISTRY_NAME, ChemicalOreTag.CODEC,
          builder -> builder.synced(ChemicalOreTag.CODEC, true));

    @Override
    public DataMapType<DamageType, MekaSuitAbsorption> mekaSuitAbsorption() {
        return MEKA_SUIT_ABSORPTION;
    }

    @Override
    public DataMapType<Chemical, ChemicalOreTag> chemicalOreTag() {
        return CHEMICAL_ORE_TAG;
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
