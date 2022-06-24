package mekanism.common.tag;

import com.mojang.datafixers.util.Either;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagBuilder;
import net.minecraft.tags.TagEntry;
import net.minecraft.tags.TagKey;
import net.minecraftforge.registries.IForgeRegistry;

//Based off of TagsProvider.TagAppender but with a few shortcuts for forge registry entries and also a few more helpers and addition of SafeVarargs annotations
public class ForgeRegistryTagBuilder<TYPE> {

    private final Either<IForgeRegistry<TYPE>, Registry<TYPE>> registry;
    private final TagBuilder builder;
    private final String modID;

    public ForgeRegistryTagBuilder(Either<IForgeRegistry<TYPE>, Registry<TYPE>> registry, TagBuilder builder, String modID) {
        this.registry = registry;
        this.builder = builder;
        this.modID = modID;
    }

    @SafeVarargs
    public final ForgeRegistryTagBuilder<TYPE> add(Supplier<TYPE>... elements) {
        return addTyped(Supplier::get, elements);
    }

    private ResourceLocation getKey(TYPE element) {
        return registry.map(r -> r.getKey(element), r -> r.getKey(element));
    }

    @SafeVarargs
    public final ForgeRegistryTagBuilder<TYPE> add(TYPE... elements) {
        return add(this::getKey, elements);
    }

    @SafeVarargs
    public final <T> ForgeRegistryTagBuilder<TYPE> addTyped(Function<T, TYPE> converter, T... elements) {
        return add(converter.andThen(this::getKey), elements);
    }

    @SafeVarargs
    public final ForgeRegistryTagBuilder<TYPE> add(TagKey<TYPE>... tags) {
        return apply(builder::addTag, TagKey::location, tags);
    }

    public ForgeRegistryTagBuilder<TYPE> add(TagEntry tag) {
        builder.add(tag);
        return this;
    }

    @SafeVarargs
    public final ForgeRegistryTagBuilder<TYPE> add(ResourceKey<TYPE>... keys) {
        return add(ResourceKey::location, keys);
    }

    @SafeVarargs
    public final <T> ForgeRegistryTagBuilder<TYPE> add(Function<T, ResourceLocation> locationGetter, T... elements) {
        return apply(builder::addElement, locationGetter, elements);
    }

    public ForgeRegistryTagBuilder<TYPE> replace() {
        return replace(true);
    }

    public ForgeRegistryTagBuilder<TYPE> replace(boolean value) {
        builder.replace(value);
        return this;
    }

    @SafeVarargs
    public final ForgeRegistryTagBuilder<TYPE> addOptional(TYPE... elements) {
        return addOptional(this::getKey, elements);
    }

    public ForgeRegistryTagBuilder<TYPE> addOptional(ResourceLocation... locations) {
        return addOptional(Function.identity(), locations);
    }

    @SafeVarargs
    public final <T> ForgeRegistryTagBuilder<TYPE> addOptional(Function<T, ResourceLocation> locationGetter, T... elements) {
        return add(TagEntry::optionalElement, locationGetter, elements);
    }

    @SafeVarargs
    public final ForgeRegistryTagBuilder<TYPE> addOptionalTag(TagKey<TYPE>... tags) {
        return addOptionalTag(TagKey::location, tags);
    }

    public ForgeRegistryTagBuilder<TYPE> addOptionalTag(ResourceLocation... locations) {
        return addOptionalTag(Function.identity(), locations);
    }

    @SafeVarargs
    public final <T> ForgeRegistryTagBuilder<TYPE> addOptionalTag(Function<T, ResourceLocation> locationGetter, T... elements) {
        return add(TagEntry::optionalTag, locationGetter, elements);
    }

    @SafeVarargs
    private <T> ForgeRegistryTagBuilder<TYPE> add(Function<ResourceLocation, TagEntry> entryCreator, Function<T, ResourceLocation> locationGetter, T... elements) {
        return apply(rl -> add(entryCreator.apply(rl)), locationGetter, elements);
    }

    @SafeVarargs
    public final ForgeRegistryTagBuilder<TYPE> remove(TYPE... elements) {
        return remove(this::getKey, elements);
    }

    public ForgeRegistryTagBuilder<TYPE> remove(ResourceLocation... locations) {
        return remove(Function.identity(), locations);
    }

    @SafeVarargs
    public final <T> ForgeRegistryTagBuilder<TYPE> remove(Function<T, ResourceLocation> locationGetter, T... elements) {
        return apply(rl -> builder.removeElement(rl, modID), locationGetter, elements);
    }

    @SafeVarargs
    public final ForgeRegistryTagBuilder<TYPE> remove(TagKey<TYPE>... tags) {
        for (TagKey<TYPE> tag : tags) {
            builder.removeTag(tag.location(), modID);
        }
        return this;
    }

    @SafeVarargs
    private <T> ForgeRegistryTagBuilder<TYPE> apply(Consumer<ResourceLocation> consumer, Function<T, ResourceLocation> locationGetter, T... elements) {
        for (T element : elements) {
            consumer.accept(locationGetter.apply(element));
        }
        return this;
    }
}