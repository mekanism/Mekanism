package mekanism.common.recipe.ingredient.chemical;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import javax.annotation.Nonnull;
import mekanism.api.JsonConstants;
import mekanism.api.annotations.NonNull;
import mekanism.api.chemical.Chemical;
import mekanism.api.chemical.ChemicalStack;
import mekanism.api.recipes.ingredients.ChemicalStackIngredient;
import mekanism.common.recipe.ingredient.chemical.ChemicalIngredientDeserializer.IngredientType;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.tags.Tag;

public abstract class TaggedChemicalStackIngredient<CHEMICAL extends Chemical<CHEMICAL>, STACK extends ChemicalStack<CHEMICAL>>
      implements ChemicalStackIngredient<CHEMICAL, STACK> {

    @Nonnull
    private final Tag<CHEMICAL> tag;
    private final long amount;

    public TaggedChemicalStackIngredient(@Nonnull Tag<CHEMICAL> tag, long amount) {
        this.tag = tag;
        this.amount = amount;
    }

    protected abstract ChemicalIngredientInfo<CHEMICAL, STACK> getIngredientInfo();

    @Override
    public boolean test(@Nonnull STACK chemicalStack) {
        return testType(chemicalStack) && chemicalStack.getAmount() >= amount;
    }

    @Override
    public boolean testType(@Nonnull STACK chemicalStack) {
        return testType(Objects.requireNonNull(chemicalStack).getType());
    }

    @Override
    public boolean testType(@Nonnull CHEMICAL chemical) {
        return Objects.requireNonNull(chemical).isIn(tag);
    }

    @Nonnull
    @Override
    public STACK getMatchingInstance(@Nonnull STACK chemicalStack) {
        if (test(chemicalStack)) {
            //Our chemical is in the tag, so we make a new stack with the given amount
            return getIngredientInfo().createStack(chemicalStack, amount);
        }
        return getIngredientInfo().getEmptyStack();
    }

    @Override
    public long getNeededAmount(@Nonnull STACK stack) {
        return testType(stack) ? amount : 0;
    }

    @Nonnull
    @Override
    public List<@NonNull STACK> getRepresentations() {
        ChemicalIngredientInfo<CHEMICAL, STACK> ingredientInfo = getIngredientInfo();
        //TODO: Can this be cached some how
        List<@NonNull STACK> representations = new ArrayList<>();
        for (CHEMICAL chemical : tag.getValues()) {
            representations.add(ingredientInfo.createStack(chemical, amount));
        }
        return representations;
    }

    /**
     * For use in recipe input caching.
     */
    public List<CHEMICAL> getRawInput() {
        return tag.getValues();
    }

    @Override
    public void write(FriendlyByteBuf buffer) {
        buffer.writeEnum(IngredientType.TAGGED);
        buffer.writeResourceLocation(getIngredientInfo().getTagLocation(tag));
        buffer.writeVarLong(amount);
    }

    @Nonnull
    @Override
    public JsonElement serialize() {
        JsonObject json = new JsonObject();
        json.addProperty(JsonConstants.AMOUNT, amount);
        json.addProperty(JsonConstants.TAG, getIngredientInfo().getTagLocation(tag).toString());
        return json;
    }
}