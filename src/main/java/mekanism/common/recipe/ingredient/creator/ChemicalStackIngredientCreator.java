package mekanism.common.recipe.ingredient.creator;

import com.google.gson.JsonElement;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import mekanism.api.chemical.Chemical;
import mekanism.api.chemical.ChemicalStack;
import mekanism.api.recipes.ingredients.ChemicalStackIngredient;
import mekanism.api.recipes.ingredients.creator.IChemicalStackIngredientCreator;
import mekanism.common.recipe.ingredient.chemical.ChemicalIngredientDeserializer;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.network.FriendlyByteBuf;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public abstract class ChemicalStackIngredientCreator<CHEMICAL extends Chemical<CHEMICAL>, STACK extends ChemicalStack<CHEMICAL>,
      INGREDIENT extends ChemicalStackIngredient<CHEMICAL, STACK>> implements IChemicalStackIngredientCreator<CHEMICAL, STACK, INGREDIENT> {

    protected abstract ChemicalIngredientDeserializer<CHEMICAL, STACK, INGREDIENT> getDeserializer();

    @Override
    public INGREDIENT read(FriendlyByteBuf buffer) {
        return getDeserializer().read(buffer);
    }

    @Override
    public INGREDIENT deserialize(@Nullable JsonElement json) {
        return getDeserializer().deserialize(json);
    }

    @Override
    @SafeVarargs
    public final INGREDIENT createMulti(INGREDIENT... ingredients) {
        return getDeserializer().createMulti(ingredients);
    }

    protected final void assertNonEmpty(CHEMICAL chemical) {
        if (chemical.isEmptyType()) {
            throw new IllegalArgumentException("ChemicalStackIngredients cannot be created using the empty chemical.");
        }
    }

    protected final void assertPositiveAmount(long amount) {
        if (amount <= 0) {
            throw new IllegalArgumentException("ChemicalStackIngredients must have an amount of at least one. Received size was: " + amount);
        }
    }
}