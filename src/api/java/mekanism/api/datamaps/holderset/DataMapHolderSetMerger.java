package mekanism.api.datamaps.holderset;

import com.mojang.datafixers.util.Either;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;
import net.minecraft.core.HolderSet;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.TagKey;
import net.neoforged.neoforge.registries.datamaps.DataMapValueMerger;
import net.neoforged.neoforge.registries.holdersets.OrHolderSet;

//TODO - 26.2: Docs
public final class DataMapHolderSetMerger<REGISTRY, TYPE> implements DataMapValueMerger<REGISTRY, HolderSet<TYPE>> {

    private static final DataMapHolderSetMerger<?, ?> INSTANCE = new DataMapHolderSetMerger<>();

    @SuppressWarnings("unchecked")
    public static <REGISTRY, TYPE> DataMapHolderSetMerger<REGISTRY, TYPE> instance() {
        return (DataMapHolderSetMerger<REGISTRY, TYPE>) INSTANCE;
    }

    private DataMapHolderSetMerger() {
    }

    @Override
    public HolderSet<TYPE> merge(Registry<REGISTRY> registry, Either<TagKey<REGISTRY>, ResourceKey<REGISTRY>> first, HolderSet<TYPE> firstValue,
          Either<TagKey<REGISTRY>, ResourceKey<REGISTRY>> second, HolderSet<TYPE> secondValue) {
        if (firstValue.isImmediatelyResolvable() && secondValue.isImmediatelyResolvable()) {
            //If it is immediately resolvable, just resolve it, calculate the union, and make a direct holderset
            return HolderSet.direct(Stream.of(firstValue, secondValue).flatMap(HolderSet::stream).distinct().toList());
        }
        //Otherwise create an OrHolderSet from both holdersets. If any are already an OrHolderSet, we effectively just add extra parameters to them
        List<HolderSet<TYPE>> holderSets = new ArrayList<>();
        addHolderSets(holderSets, firstValue);
        addHolderSets(holderSets, secondValue);
        return new OrHolderSet<>(List.copyOf(holderSets));
    }

    private void addHolderSets(List<HolderSet<TYPE>> holderSets, HolderSet<TYPE> holderSet) {
        if (holderSet instanceof OrHolderSet<TYPE> orSet) {
            //Reduce how many levels of nesting are necessary
            holderSets.addAll(orSet.getComponents());
        } else {
            holderSets.add(holderSet);
        }
    }
}