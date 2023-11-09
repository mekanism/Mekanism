package mekanism.common.recipe.ingredient.chemical;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.BiFunction;
import mekanism.api.JsonConstants;
import mekanism.api.SerializerHelper;
import mekanism.api.chemical.Chemical;
import mekanism.api.chemical.ChemicalStack;
import mekanism.api.chemical.ChemicalTags;
import mekanism.api.recipes.ingredients.ChemicalStackIngredient;
import mekanism.common.recipe.ingredient.chemical.ChemicalIngredientDeserializer.IngredientType;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.tags.TagKey;
import net.minecraft.util.ExtraCodecs;
import net.neoforged.neoforge.registries.tags.ITag;
import org.jetbrains.annotations.NotNull;

public abstract class TaggedChemicalStackIngredient<CHEMICAL extends Chemical<CHEMICAL>, STACK extends ChemicalStack<CHEMICAL>>
      implements ChemicalStackIngredient<CHEMICAL, STACK> {

    public static <CHEMICAL extends Chemical<CHEMICAL>, STACK extends ChemicalStack<CHEMICAL>, CLAZZ extends TaggedChemicalStackIngredient<CHEMICAL, STACK>> Codec<CLAZZ>
    makeCodec(ChemicalTags<CHEMICAL> tags, BiFunction<TagKey<CHEMICAL>, Long, CLAZZ> constructor) {
        return ExtraCodecs.lazyInitializedCodec(() -> RecordCodecBuilder.create(instance->instance.group(
              TagKey.hashedCodec(tags.getResourceKey()).fieldOf(JsonConstants.TAG).forGetter(TaggedChemicalStackIngredient::getTag),
              SerializerHelper.POSITIVE_NONZERO_LONG_CODEC.fieldOf(JsonConstants.AMOUNT).forGetter(TaggedChemicalStackIngredient::getRawAmount)
        ).apply(instance, constructor)));
    }

    @NotNull
    private final ITag<CHEMICAL> tag;
    private final long amount;

    protected TaggedChemicalStackIngredient(@NotNull ChemicalTags<CHEMICAL> tags, @NotNull TagKey<CHEMICAL> tag, long amount) {
        this(tags.getManager().map(manager -> manager.getTag(tag)).orElseThrow(), amount);
    }

    protected TaggedChemicalStackIngredient(@NotNull ITag<CHEMICAL> tag, long amount) {
        this.tag = tag;
        this.amount = amount;
    }

    protected abstract ChemicalIngredientInfo<CHEMICAL, STACK> getIngredientInfo();

    @Override
    public boolean test(@NotNull STACK chemicalStack) {
        return testType(chemicalStack) && chemicalStack.getAmount() >= amount;
    }

    @Override
    public boolean testType(@NotNull STACK chemicalStack) {
        return testType(Objects.requireNonNull(chemicalStack).getType());
    }

    @Override
    public boolean testType(@NotNull CHEMICAL chemical) {
        return tag.contains(Objects.requireNonNull(chemical));
    }

    @NotNull
    @Override
    public STACK getMatchingInstance(@NotNull STACK chemicalStack) {
        if (test(chemicalStack)) {
            //Our chemical is in the tag, so we make a new stack with the given amount
            return getIngredientInfo().createStack(chemicalStack, amount);
        }
        return getIngredientInfo().getEmptyStack();
    }

    @Override
    public long getNeededAmount(@NotNull STACK stack) {
        return testType(stack) ? amount : 0;
    }

    public long getRawAmount() {
        return this.amount;
    }

    @Override
    public boolean hasNoMatchingInstances() {
        return tag.isEmpty();
    }

    @NotNull
    @Override
    public List<@NotNull STACK> getRepresentations() {
        ChemicalIngredientInfo<CHEMICAL, STACK> ingredientInfo = getIngredientInfo();
        //TODO: Can this be cached somehow
        List<@NotNull STACK> representations = new ArrayList<>();
        for (CHEMICAL chemical : tag) {
            representations.add(ingredientInfo.createStack(chemical, amount));
        }
        return representations;
    }

    /**
     * For use in recipe input caching.
     */
    public Iterable<CHEMICAL> getRawInput() {
        return tag;
    }

    public TagKey<CHEMICAL> getTag() {
        return tag.getKey();
    }

    @Override
    public void write(FriendlyByteBuf buffer) {
        buffer.writeEnum(IngredientType.TAGGED);
        buffer.writeResourceLocation(tag.getKey().location());
        buffer.writeVarLong(amount);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        } else if (o == null || getClass() != o.getClass()) {
            return false;
        }
        TaggedChemicalStackIngredient<CHEMICAL, STACK> other = (TaggedChemicalStackIngredient<CHEMICAL, STACK>) o;
        return amount == other.amount && tag.equals(other.tag);
    }

    @Override
    public int hashCode() {
        return Objects.hash(tag, amount);
    }
}