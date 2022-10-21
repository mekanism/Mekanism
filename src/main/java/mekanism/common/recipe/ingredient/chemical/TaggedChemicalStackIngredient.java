package mekanism.common.recipe.ingredient.chemical;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import mekanism.api.JsonConstants;
import mekanism.api.chemical.Chemical;
import mekanism.api.chemical.ChemicalStack;
import mekanism.api.chemical.ChemicalTags;
import mekanism.api.recipes.ingredients.ChemicalStackIngredient;
import mekanism.common.recipe.ingredient.chemical.ChemicalIngredientDeserializer.IngredientType;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.tags.TagKey;
import net.minecraftforge.registries.tags.ITag;
import org.jetbrains.annotations.NotNull;

public abstract class TaggedChemicalStackIngredient<CHEMICAL extends Chemical<CHEMICAL>, STACK extends ChemicalStack<CHEMICAL>>
      implements ChemicalStackIngredient<CHEMICAL, STACK> {

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

    @Override
    public boolean hasNoMatchingInstances() {
        return tag.isEmpty();
    }

    @NotNull
    @Override
    public List<@NotNull STACK> getRepresentations() {
        ChemicalIngredientInfo<CHEMICAL, STACK> ingredientInfo = getIngredientInfo();
        //TODO: Can this be cached some how
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

    @Override
    public void write(FriendlyByteBuf buffer) {
        buffer.writeEnum(IngredientType.TAGGED);
        buffer.writeResourceLocation(tag.getKey().location());
        buffer.writeVarLong(amount);
    }

    @NotNull
    @Override
    public JsonElement serialize() {
        JsonObject json = new JsonObject();
        json.addProperty(JsonConstants.AMOUNT, amount);
        json.addProperty(JsonConstants.TAG, tag.getKey().location().toString());
        return json;
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