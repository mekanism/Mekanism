package mekanism.api.chemical;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import mekanism.api.MekanismAPI;
import mekanism.api.MekanismRegistries;
import mekanism.api.datamaps.IMekanismDataMapTypes;
import mekanism.api.datamaps.chemical.ChemicalSolidTag;
import mekanism.api.datamaps.chemical.attribute.ChemicalFuel;
import mekanism.api.datamaps.chemical.attribute.ChemicalRadioactivity;
import mekanism.api.datamaps.chemical.attribute.CooledCoolant;
import mekanism.api.datamaps.chemical.attribute.HeatedCoolant;
import mekanism.api.text.IHasTranslationKey;
import net.minecraft.core.Holder;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.TypedInstance;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemInstance;
import net.neoforged.neoforge.registries.datamaps.DataMapType;
import org.jspecify.annotations.Nullable;

/// @since 10.8.0
public interface ChemicalInstance extends TypedInstance<Chemical>, IHasTranslationKey {//TODO - 26.2: Docs

    String FIELD_ID = ItemInstance.FIELD_ID;

    /// A standard codec for non-empty Chemical holders.
    Codec<Holder<Chemical>> CHEMICAL_HOLDER_CODEC = ChemicalSerializationHelper.REFERENCE_CODEC.validate(
          chemical -> chemical.is(MekanismAPI.EMPTY_CHEMICAL_KEY) ? DataResult.error(() -> "Chemical must not be mekanism:empty") : DataResult.success(chemical)
    );

    /// A stream codec which can be used to encode and decode chemical holders over the network.
    StreamCodec<RegistryFriendlyByteBuf, Holder<Chemical>> CHEMICAL_HOLDER_STREAM_CODEC = ByteBufCodecs.holderRegistry(MekanismRegistries.Keys.CHEMICAL);

    @Override
    default String getTranslationKey() {
        //Wrapper to get translation key of the chemical type easier
        return Chemical.getTranslationKey(typeHolder().getKey());
    }

    /// Helper to check if this chemical is radioactive without having to look it up from the attributes.
    ///
    /// @return `true` if this chemical is radioactive.
    default boolean isRadioactive(@Nullable RegistryAccess registryAccess) {
        return getRadioactivityAttribute(registryAccess) != null;
    }

    /// {@return radiation level of this chemical, or zero if it is not radioactive}.
    default double getRadioactivity(@Nullable RegistryAccess registryAccess) {
        ChemicalRadioactivity radioactivity = getRadioactivityAttribute(registryAccess);
        return radioactivity == null ? 0 : radioactivity.radioactivity();
    }

    default int fuelEnergyDensity(@Nullable RegistryAccess registryAccess) {
        ChemicalFuel fuel = getFuel(registryAccess);
        return fuel == null ? 0 : fuel.energyDensity();
    }

    @Nullable
    default ChemicalRadioactivity getRadioactivityAttribute(@Nullable RegistryAccess registryAccess) {
        return getData(registryAccess, IMekanismDataMapTypes.INSTANCE.chemicalRadioactivity());
    }

    @Nullable
    default ChemicalFuel getFuel(@Nullable RegistryAccess registryAccess) {
        return getData(registryAccess, IMekanismDataMapTypes.INSTANCE.chemicalFuel());
    }

    @Nullable
    default CooledCoolant getCooledCoolant(@Nullable RegistryAccess registryAccess) {
        return getData(registryAccess, IMekanismDataMapTypes.INSTANCE.cooledChemicalCoolant());
    }

    @Nullable
    default HeatedCoolant getHeatedCoolant(@Nullable RegistryAccess registryAccess) {
        return getData(registryAccess, IMekanismDataMapTypes.INSTANCE.heatedChemicalCoolant());
    }

    @Nullable
    default ChemicalSolidTag getSolidTag(@Nullable RegistryAccess registryAccess) {
        return getData(registryAccess, IMekanismDataMapTypes.INSTANCE.chemicalSolidTag());
    }

    @Nullable
    default <DATA> DATA getData(@Nullable RegistryAccess registryAccess, DataMapType<Chemical, DATA> type) {
        return IMekanismDataMapTypes.INSTANCE.getData(registryAccess, MekanismRegistries.Keys.CHEMICAL, typeHolder(), type);
    }
}