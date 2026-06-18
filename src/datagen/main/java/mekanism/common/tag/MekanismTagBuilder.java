package mekanism.common.tag;

import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Stream;
import mekanism.api.text.EnumColorCollection;
import net.minecraft.core.Holder;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.TagBuilder;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.block.ColorCollection;

//Based off of TagsProvider.TagAppender but with a few shortcuts for things like holders and also a few more helpers and addition of SafeVarargs annotations
public class MekanismTagBuilder<TYPE> {

    private final Function<Holder<TYPE>, Identifier> holderToName = holder -> Objects.requireNonNull(holder.getKey()).identifier();
    private final Consumer<Identifier> elementAdder;
    private final Consumer<Identifier> elementRemover;
    private final Consumer<Identifier> optionalElementAdder;
    private final Consumer<Identifier> tagAdder;
    private final Consumer<Identifier> tagRemover;
    private final Consumer<Identifier> optionalTagAdder;
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

    public final MekanismTagBuilder<TYPE> add(List<TagKey<TYPE>> tags) {
        return apply(tagAdder, TagKey::location, tags);
    }

    public <COLOR> MekanismTagBuilder<TYPE> add(ColorCollection<COLOR> colorCollection, Function<COLOR, ResourceKey<TYPE>> keyExtractor) {
        colorCollection.forEach(color -> elementAdder.accept(keyExtractor.apply(color).identifier()));
        return this;
    }

    public <COLOR> MekanismTagBuilder<TYPE> add(EnumColorCollection<COLOR> colorCollection, Function<COLOR, ResourceKey<TYPE>> keyExtractor) {
        colorCollection.forEach(color -> elementAdder.accept(keyExtractor.apply(color).identifier()));
        return this;
    }

    @SafeVarargs
    public final MekanismTagBuilder<TYPE> add(ResourceKey<TYPE>... keys) {
        return apply(elementAdder, ResourceKey::identifier, keys);
    }

    public final MekanismTagBuilder<TYPE> add(Identifier... locations) {
        return apply(elementAdder, locations);
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
        builder.setReplace(value);
        return this;
    }

    public MekanismTagBuilder<TYPE> addOptional(Identifier... locations) {
        return apply(optionalElementAdder, locations);
    }

    @SafeVarargs
    public final MekanismTagBuilder<TYPE> addOptional(TagKey<TYPE>... tags) {
        return apply(optionalTagAdder, TagKey::location, tags);
    }

    public MekanismTagBuilder<TYPE> addOptionalTag(Identifier... locations) {
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