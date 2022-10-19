package mekanism.common.recipe.ingredient.chemical;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import mekanism.api.JsonConstants;
import mekanism.api.chemical.Chemical;
import mekanism.api.chemical.ChemicalStack;
import mekanism.api.recipes.ingredients.ChemicalStackIngredient;
import mekanism.common.recipe.ingredient.chemical.ChemicalIngredientDeserializer.IngredientType;
import net.minecraft.network.FriendlyByteBuf;
import org.jetbrains.annotations.NotNull;

public abstract class SingleChemicalStackIngredient<CHEMICAL extends Chemical<CHEMICAL>, STACK extends ChemicalStack<CHEMICAL>>
      implements ChemicalStackIngredient<CHEMICAL, STACK> {

    @NotNull
    private final STACK chemicalInstance;

    public SingleChemicalStackIngredient(@NotNull STACK chemicalInstance) {
        this.chemicalInstance = chemicalInstance;
    }

    protected abstract ChemicalIngredientInfo<CHEMICAL, STACK> getIngredientInfo();

    @Override
    public boolean test(@NotNull STACK chemicalStack) {
        return testType(chemicalStack) && chemicalStack.getAmount() >= chemicalInstance.getAmount();
    }

    @Override
    public boolean testType(@NotNull STACK chemicalStack) {
        return chemicalInstance.isTypeEqual(Objects.requireNonNull(chemicalStack));
    }

    @Override
    public boolean testType(@NotNull CHEMICAL chemical) {
        return chemicalInstance.isTypeEqual(Objects.requireNonNull(chemical));
    }

    @NotNull
    @Override
    public STACK getMatchingInstance(@NotNull STACK chemicalStack) {
        if (test(chemicalStack)) {
            //Note: We manually "implement" the copy to ensure it returns the proper type as ChemicalStack#copy returns ChemicalStack<CHEMICAL> instead of STACK
            return getIngredientInfo().createStack(chemicalInstance, chemicalInstance.getAmount());
        }
        return getIngredientInfo().getEmptyStack();
    }

    @Override
    public long getNeededAmount(@NotNull STACK stack) {
        return testType(stack) ? chemicalInstance.getAmount() : 0;
    }

    @Override
    public boolean hasNoMatchingInstances() {
        return false;
    }

    @NotNull
    @Override
    public List<@NotNull STACK> getRepresentations() {
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

    @NotNull
    @Override
    public JsonElement serialize() {
        JsonObject json = new JsonObject();
        json.addProperty(JsonConstants.AMOUNT, chemicalInstance.getAmount());
        json.addProperty(getIngredientInfo().getSerializationKey(), chemicalInstance.getTypeRegistryName().toString());
        return json;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        } else if (o == null || getClass() != o.getClass()) {
            return false;
        }
        return chemicalInstance.equals(((SingleChemicalStackIngredient<CHEMICAL, STACK>) o).chemicalInstance);
    }

    @Override
    public int hashCode() {
        return chemicalInstance.hashCode();
    }
}