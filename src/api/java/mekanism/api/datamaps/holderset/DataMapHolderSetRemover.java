package mekanism.api.datamaps.holderset;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.core.Registry;
import net.minecraft.resources.HolderSetCodec;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.TagKey;
import net.neoforged.neoforge.registries.datamaps.DataMapValueRemover;

/// Implementation of [DataMapValueRemover] that handles removing elements that match a given [holder set][HolderSet] from a data map of [holder sets][HolderSet].
///
/// @param toRemove   Holders that should be removed from the value of the data map.
/// @param <REGISTRY> Registry the data map targets.
/// @param <TYPE>     Holder type.
///
/// @since 10.8.0
public record DataMapHolderSetRemover<REGISTRY, TYPE>(HolderSet<TYPE> toRemove) implements DataMapValueRemover<REGISTRY, HolderSet<TYPE>> {

    /// Codec for creating a data map value remover for removing from [holder set][HolderSet] values.
    public static <REGISTRY, TYPE> Codec<DataMapHolderSetRemover<REGISTRY, TYPE>> codec(ResourceKey<? extends Registry<TYPE>> registryKey, Codec<Holder<TYPE>> elementCodec) {
        return HolderSetCodec.create(registryKey, elementCodec, false).xmap(DataMapHolderSetRemover::new, DataMapHolderSetRemover::toRemove);
    }

    @Override
    public Optional<HolderSet<TYPE>> remove(HolderSet<TYPE> value, Registry<REGISTRY> registry, Either<TagKey<REGISTRY>, ResourceKey<REGISTRY>> source, REGISTRY object) {
        List<Holder<TYPE>> result = new ArrayList<>();
        for (Holder<TYPE> holder : value) {
            if (!toRemove.contains(holder)) {
                result.add(holder);
            }
        }
        return result.isEmpty() ? Optional.empty() : Optional.of(HolderSet.direct(result));
    }
}