package mekanism.common.tag;

import java.util.Collection;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Stream;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagBuilder;
import net.minecraft.tags.TagKey;

//Based off of TagsProvider.TagAppender but with a few shortcuts for things like holders and also a few more helpers and addition of SafeVarargs annotations
public class MekanismTagBuilder<TYPE> {

    private final Function<Holder<TYPE>, ResourceLocation> holderToName = holder -> Objects.requireNonNull(holder.getKey()).location();
    private final Consumer<ResourceLocation> elementAdder;
    private final Consumer<ResourceLocation> elementRemover;
    private final Consumer<ResourceLocation> optionalElementAdder;
    private final Consumer<ResourceLocation> tagAdder;
    private final Consumer<ResourceLocation> tagRemover;
    private final Consumer<ResourceLocation> optionalTagAdder;
    private final TagBuilder builder;

    public MekanismTagBuilder(TagBuilder builder) {
        this.builder = builder;
        this.elementAdder = this.builder::addElement;
        this.elementRemover = this.builder::removeElement;
        this.optionalElementAdder = this.builder::addOptionalElement;
        this.tagAdder = this.builder::addTag;
        this.tagRemover = this.builder::removeTag;
        this.optionalTagAdder = this.builder::addOptionalTag;
    }

    @SafeVarargs
    public final MekanismTagBuilder<TYPE> add(TagKey<TYPE>... tags) {
        return apply(tagAdder, TagKey::location, tags);
    }

    @SafeVarargs//TODO: Move away from having to have any intrinsics, this mostly exists as a convenience method for when we need to add vanilla entries
    public final MekanismTagBuilder<TYPE> addIntrinsic(Registry<TYPE> registry, TYPE... elements) {
        return apply(elementAdder, element -> Objects.requireNonNull(registry.getKeyOrNull(element)), elements);
    }

    @SafeVarargs
    public final MekanismTagBuilder<TYPE> add(ResourceKey<TYPE>... keys) {
        return apply(elementAdder, ResourceKey::location, keys);
    }

    @SafeVarargs
    public final MekanismTagBuilder<TYPE> add(Holder<TYPE>... elements) {
        return apply(elementAdder, holderToName, elements);
    }

    public final MekanismTagBuilder<TYPE> add(Stream<? extends Holder<TYPE>> elements) {
        return add(elements.toList());
    }

    public MekanismTagBuilder<TYPE> add(Collection<? extends Holder<TYPE>> elements) {
        return apply(elementAdder, holderToName, elements);
    }

    public MekanismTagBuilder<TYPE> replace() {
        return replace(true);
    }

    public MekanismTagBuilder<TYPE> replace(boolean value) {
        builder.replace(value);
        return this;
    }

    public MekanismTagBuilder<TYPE> addOptional(ResourceLocation... locations) {
        return apply(optionalElementAdder, locations);
    }

    @SafeVarargs
    public final MekanismTagBuilder<TYPE> addOptional(TagKey<TYPE>... tags) {
        return apply(optionalTagAdder, TagKey::location, tags);
    }

    public MekanismTagBuilder<TYPE> addOptionalTag(ResourceLocation... locations) {
        return apply(optionalTagAdder, locations);
    }

    @SafeVarargs
    public final MekanismTagBuilder<TYPE> remove(Holder<TYPE>... elements) {
        return apply(elementRemover, holderToName, elements);
    }

    @SafeVarargs
    public final MekanismTagBuilder<TYPE> remove(TagKey<TYPE>... tags) {
        return apply(tagRemover, TagKey::location, tags);
    }

    @SafeVarargs
    private <T, V> MekanismTagBuilder<TYPE> apply(Consumer<V> consumer, Function<T, V> locationGetter, T... elements) {
        for (T element : elements) {
            consumer.accept(locationGetter.apply(element));
        }
        return this;
    }

    @SafeVarargs
    private <T> MekanismTagBuilder<TYPE> apply(Consumer<T> consumer, T... elements) {
        for (T element : elements) {
            consumer.accept(element);
        }
        return this;
    }

    private <T, V> MekanismTagBuilder<TYPE> apply(Consumer<V> consumer, Function<T, V> locationGetter, Collection<? extends T> elements) {
        for (T element : elements) {
            consumer.accept(locationGetter.apply(element));
        }
        return this;
    }
}