package mekanism.common.tag;

import java.util.function.Consumer;
import java.util.function.Function;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagBuilder;
import net.minecraft.tags.TagEntry;
import net.minecraft.tags.TagKey;

//Based off of TagsProvider.TagAppender but with a few shortcuts for forge registry entries and also a few more helpers and addition of SafeVarargs annotations
public class MekanismTagBuilder<TYPE, BUILDER extends MekanismTagBuilder<TYPE, BUILDER>> {

    protected final TagBuilder builder;
    protected final String modID;

    public MekanismTagBuilder(TagBuilder builder, String modID) {
        this.builder = builder;
        this.modID = modID;
    }

    @SuppressWarnings("unchecked")
    private BUILDER getThis() {
        return (BUILDER) this;
    }

    @SafeVarargs
    public final BUILDER add(TagKey<TYPE>... tags) {
        return apply(builder::addTag, TagKey::location, tags);
    }

    public BUILDER add(TagEntry tag) {
        builder.add(tag);
        return getThis();
    }

    @SafeVarargs
    public final BUILDER add(ResourceKey<TYPE>... keys) {
        return add(ResourceKey::location, keys);
    }

    @SafeVarargs
    public final <T> BUILDER add(Function<T, ResourceLocation> locationGetter, T... elements) {
        return apply(builder::addElement, locationGetter, elements);
    }

    public BUILDER replace() {
        return replace(true);
    }

    public BUILDER replace(boolean value) {
        builder.replace(value);
        return getThis();
    }

    public BUILDER addOptional(ResourceLocation... locations) {
        return addOptional(Function.identity(), locations);
    }

    @SafeVarargs
    public final <T> BUILDER addOptional(Function<T, ResourceLocation> locationGetter, T... elements) {
        return add(TagEntry::optionalElement, locationGetter, elements);
    }

    @SafeVarargs
    public final BUILDER addOptionalTag(TagKey<TYPE>... tags) {
        return addOptionalTag(TagKey::location, tags);
    }

    public BUILDER addOptionalTag(ResourceLocation... locations) {
        return addOptionalTag(Function.identity(), locations);
    }

    @SafeVarargs
    public final <T> BUILDER addOptionalTag(Function<T, ResourceLocation> locationGetter, T... elements) {
        return add(TagEntry::optionalTag, locationGetter, elements);
    }

    @SafeVarargs
    private <T> BUILDER add(Function<ResourceLocation, TagEntry> entryCreator, Function<T, ResourceLocation> locationGetter, T... elements) {
        return apply(rl -> add(entryCreator.apply(rl)), locationGetter, elements);
    }

    public BUILDER remove(ResourceLocation... locations) {
        return remove(Function.identity(), locations);
    }

    @SafeVarargs
    public final <T> BUILDER remove(Function<T, ResourceLocation> locationGetter, T... elements) {
        return apply(rl -> builder.removeElement(rl, modID), locationGetter, elements);
    }

    @SafeVarargs
    public final BUILDER remove(TagKey<TYPE>... tags) {
        for (TagKey<TYPE> tag : tags) {
            builder.removeTag(tag.location(), modID);
        }
        return getThis();
    }

    @SafeVarargs
    protected final <T> BUILDER apply(Consumer<ResourceLocation> consumer, Function<T, ResourceLocation> locationGetter, T... elements) {
        for (T element : elements) {
            consumer.accept(locationGetter.apply(element));
        }
        return getThis();
    }
}