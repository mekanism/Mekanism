package mekanism.common.tags;

import mekanism.api.chemical.Chemical;
import mekanism.api.chemical.ChemicalTags;
import net.minecraft.tags.TagKey;
import net.minecraftforge.common.util.Lazy;
import net.minecraftforge.registries.IForgeRegistry;
import net.minecraftforge.registries.IForgeRegistryEntry;
import net.minecraftforge.registries.tags.ITag;

public record LazyTagLookup<TYPE extends IForgeRegistryEntry<TYPE>>(TagKey<TYPE> key, Lazy<ITag<TYPE>> lazyTag) {

    public static <TYPE extends IForgeRegistryEntry<TYPE>> LazyTagLookup<TYPE> create(IForgeRegistry<TYPE> registry, TagKey<TYPE> key) {
        return new LazyTagLookup<>(key, Lazy.of(() -> TagUtils.manager(registry).getTag(key)));
    }

    public static <CHEMICAL extends Chemical<CHEMICAL>> LazyTagLookup<CHEMICAL> create(ChemicalTags<CHEMICAL> registry, TagKey<CHEMICAL> key) {
        return new LazyTagLookup<>(key, Lazy.of(() -> registry.getManager().orElseThrow().getTag(key)));
    }

    public ITag<TYPE> tag() {
        return lazyTag.get();
    }

    public boolean contains(TYPE element) {
        return tag().contains(element);
    }

    public boolean isEmpty() {
        return tag().isEmpty();
    }
}