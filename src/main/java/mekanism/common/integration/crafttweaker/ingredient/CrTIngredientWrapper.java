package mekanism.common.integration.crafttweaker.ingredient;

import com.blamejared.crafttweaker.impl.tag.MCTag;
import java.util.function.Function;
import java.util.function.IntFunction;
import java.util.function.Predicate;
import mekanism.api.recipes.inputs.InputIngredient;

public class CrTIngredientWrapper<TYPE, INGREDIENT extends InputIngredient<TYPE>> {

    protected static void assertValidAmount(String ingredientType, long amount) {
        if (amount <= 0) {
            throw new IllegalArgumentException(ingredientType + " can only be created with a size of at least one. Received size was: " + amount);
        }
    }

    protected static void assertValid(MCTag tag, long amount, Predicate<MCTag> isCorrectType, String ingredientType, String tagType) {
        assertValidAmount(ingredientType, amount);
        if (!isCorrectType.test(tag)) {
            throw new IllegalArgumentException("Tag " + tag.getCommandString() + " is not a " + tagType);
        }
    }

    @SafeVarargs
    protected static <TYPE, INGREDIENT extends InputIngredient<TYPE>, CRT_INGREDIENT extends CrTIngredientWrapper<TYPE, INGREDIENT>> CRT_INGREDIENT
    createMulti(IntFunction<INGREDIENT[]> arrayCreator, Function<INGREDIENT[], CRT_INGREDIENT> multiCreator, CRT_INGREDIENT... crtIngredients) {
        INGREDIENT[] ingredients = arrayCreator.apply(crtIngredients.length);
        for (int i = 0; i < ingredients.length; i++) {
            ingredients[i] = crtIngredients[i].getInternal();
        }
        return multiCreator.apply(ingredients);
    }

    private final INGREDIENT ingredient;

    protected CrTIngredientWrapper(INGREDIENT ingredient) {
        this.ingredient = ingredient;
    }

    public INGREDIENT getInternal() {
        return ingredient;
    }
}