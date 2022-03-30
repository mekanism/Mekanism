package mekanism.common.recipe.ingredient.chemical;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import java.util.Collections;
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

public abstract class SingleChemicalStackIngredient<CHEMICAL extends Chemical<CHEMICAL>, STACK extends ChemicalStack<CHEMICAL>>
      implements ChemicalStackIngredient<CHEMICAL, STACK> {

    @Nonnull
    private final STACK chemicalInstance;

    public SingleChemicalStackIngredient(@Nonnull STACK chemicalInstance) {
        this.chemicalInstance = chemicalInstance;
    }

    protected abstract ChemicalIngredientInfo<CHEMICAL, STACK> getIngredientInfo();

    @Override
    public boolean test(@Nonnull STACK chemicalStack) {
        return testType(chemicalStack) && chemicalStack.getAmount() >= chemicalInstance.getAmount();
    }

    @Override
    public boolean testType(@Nonnull STACK chemicalStack) {
        return chemicalInstance.isTypeEqual(Objects.requireNonNull(chemicalStack));
    }

    @Override
    public boolean testType(@Nonnull CHEMICAL chemical) {
        return chemicalInstance.isTypeEqual(Objects.requireNonNull(chemical));
    }

    @Nonnull
    @Override
    public STACK getMatchingInstance(@Nonnull STACK chemicalStack) {
        if (test(chemicalStack)) {
            //Note: We manually "implement" the copy to ensure it returns the proper type as ChemicalStack#copy returns ChemicalStack<CHEMICAL> instead of STACK
            return getIngredientInfo().createStack(chemicalInstance, chemicalInstance.getAmount());
        }
        return getIngredientInfo().getEmptyStack();
    }

    @Override
    public long getNeededAmount(@Nonnull STACK stack) {
        return testType(stack) ? chemicalInstance.getAmount() : 0;
    }

    @Override
    public boolean hasNoMatchingInstances() {
        return false;
    }

    @Nonnull
    @Override
    public List<@NonNull STACK> getRepresentations() {
        return Collections.singletonList(chemicalInstance);
    }

    /**
     * For use in recipe input caching.
     */
    public CHEMICAL getInputRaw() {
        return chemicalInstance.getType();
    }

    @Override
    public void write(FriendlyByteBuf buffer) {
        buffer.writeEnum(IngredientType.SINGLE);
        chemicalInstance.writeToPacket(buffer);
    }

    @Nonnull
    @Override
    public JsonElement serialize() {
        JsonObject json = new JsonObject();
        json.addProperty(JsonConstants.AMOUNT, chemicalInstance.getAmount());
        json.addProperty(getIngredientInfo().getSerializationKey(), chemicalInstance.getTypeRegistryName().toString());
        return json;
    }
}