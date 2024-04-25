package mekanism.common.recipe.ingredient.chemical;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.BiFunction;
import mekanism.api.JsonConstants;
import mekanism.api.SerializerHelper;
import mekanism.api.annotations.NothingNullByDefault;
import mekanism.api.chemical.Chemical;
import mekanism.api.chemical.ChemicalStack;
import mekanism.api.recipes.ingredients.ChemicalStackIngredient;
import mekanism.api.recipes.ingredients.IngredientType;
import mekanism.common.network.PacketUtils;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.core.Registry;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.TagKey;
import org.jetbrains.annotations.NotNull;

@NothingNullByDefault
public abstract class TaggedChemicalStackIngredient<CHEMICAL extends Chemical<CHEMICAL>, STACK extends ChemicalStack<CHEMICAL>>
      implements ChemicalStackIngredient<CHEMICAL, STACK> {

    public static <CHEMICAL extends Chemical<CHEMICAL>, STACK extends ChemicalStack<CHEMICAL>, CLAZZ extends TaggedChemicalStackIngredient<CHEMICAL, STACK>> Codec<CLAZZ>
    makeCodec(ResourceKey<? extends Registry<CHEMICAL>> registry, BiFunction<TagKey<CHEMICAL>, Long, CLAZZ> constructor) {
        return RecordCodecBuilder.create(instance -> instance.group(
              TagKey.codec(registry).fieldOf(JsonConstants.TAG).forGetter(TaggedChemicalStackIngredient::getTag),
              SerializerHelper.POSITIVE_NONZERO_LONG_CODEC.fieldOf(JsonConstants.AMOUNT).forGetter(TaggedChemicalStackIngredient::getRawAmount)
        ).apply(instance, constructor));
    }

    public static <CHEMICAL extends Chemical<CHEMICAL>, STACK extends ChemicalStack<CHEMICAL>, CLAZZ extends TaggedChemicalStackIngredient<CHEMICAL, STACK>>
    StreamCodec<RegistryFriendlyByteBuf, CLAZZ> makeStreamCodec(ResourceKey<? extends Registry<CHEMICAL>> registry, BiFunction<TagKey<CHEMICAL>, Long, CLAZZ> constructor) {
        return StreamCodec.composite(
              PacketUtils.tagKeyCodec(registry), TaggedChemicalStackIngredient::getTag,
              ByteBufCodecs.VAR_LONG, TaggedChemicalStackIngredient::getRawAmount,
              constructor
        );
    }

    private final HolderSet.Named<CHEMICAL> tag;
    private final long amount;

    protected TaggedChemicalStackIngredient(HolderSet.Named<CHEMICAL> tag, long amount) {
        this.tag = tag;
        this.amount = amount;
    }

    @Override
    public boolean test(STACK chemicalStack) {
        return testType(chemicalStack) && chemicalStack.getAmount() >= amount;
    }

    @Override
    public boolean testType(STACK chemicalStack) {
        return testType(Objects.requireNonNull(chemicalStack).getChemical());
    }

    @Override
    public boolean testType(CHEMICAL chemical) {
        return Objects.requireNonNull(chemical).is(getTag());
    }

    @Override
    @SuppressWarnings("unchecked")
    public STACK getMatchingInstance(STACK chemicalStack) {
        if (test(chemicalStack)) {
            //Our chemical is in the tag, so we make a new stack with the given amount
            return (STACK) chemicalStack.copyWithAmount(amount);
        }
        return getEmptyStack();
    }

    @Override
    public long getNeededAmount(STACK stack) {
        return testType(stack) ? amount : 0;
    }

    public long getRawAmount() {
        return this.amount;
    }

    @Override
    public boolean hasNoMatchingInstances() {
        return tag.size() == 0;
    }

    @Override
    @SuppressWarnings("unchecked")
    public List<@NotNull STACK> getRepresentations() {
        //TODO: Can this be cached somehow
        List<@NotNull STACK> representations = new ArrayList<>();
        for (Holder<CHEMICAL> chemical : tag) {
            representations.add((STACK) chemical.value().getStack(amount));
        }
        return representations;
    }

    /**
     * For use in recipe input caching.
     */
    public Iterable<Holder<CHEMICAL>> getRawInput() {
        return tag;
    }

    public TagKey<CHEMICAL> getTag() {
        return tag.key();
    }

    @Override
    public IngredientType getType() {
        return IngredientType.TAGGED;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        } else if (o == null || getClass() != o.getClass()) {
            return false;
        }
        TaggedChemicalStackIngredient<?, ?> other = (TaggedChemicalStackIngredient<?, ?>) o;
        return amount == other.amount && tag.equals(other.tag);
    }

    @Override
    public int hashCode() {
        return Objects.hash(tag, amount);
    }
}