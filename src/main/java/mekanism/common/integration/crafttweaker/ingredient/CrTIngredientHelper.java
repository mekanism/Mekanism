package mekanism.common.integration.crafttweaker.ingredient;

import com.blamejared.crafttweaker.api.tag.type.KnownTag;
import mekanism.api.chemical.Chemical;
import mekanism.api.recipes.ingredients.InputIngredient;
import mekanism.api.recipes.ingredients.creator.IIngredientCreator;
import mekanism.common.integration.crafttweaker.CrTUtils;
import mekanism.common.integration.crafttweaker.chemical.ICrTChemicalStack;
import net.minecraft.tags.TagKey;

public class CrTIngredientHelper {

    /**
     * Validates that the amount is greater than zero. If it is not it throws an error.
     */
    static void assertValidAmount(String ingredientType, long amount) {
        if (amount <= 0) {
            throw new IllegalArgumentException(ingredientType + " can only be created with a size of at least one. Received size was: " + amount);
        }
    }

    /**
     * Validates that the amount is greater than zero and that the tag exists. If it does it get and returns the tag, otherwise it throws an error.
     */
    static <TYPE> TagKey<TYPE> assertValidAndGet(KnownTag<TYPE> crtTag, long amount, String ingredientType) {
        assertValidAmount(ingredientType, amount);
        return CrTUtils.validateTagAndGet(crtTag);
    }

    /**
     * Validates that the amount is greater than zero and that given chemical is not the empty variant. If one of these is not true, an error is thrown.
     */
    static void assertValid(Chemical<?> instance, long amount, String ingredientType, String chemicalType) {
        assertValidAmount(ingredientType, amount);
        if (instance.isEmptyType()) {
            throw new IllegalArgumentException(ingredientType + " cannot be created from an empty " + chemicalType + ".");
        }
    }

    /**
     * Validates that the chemical stack is not empty. If it is, an error is thrown.
     */
    static void assertValid(ICrTChemicalStack<?, ?, ?> instance, String ingredientType) {
        if (instance.isEmpty()) {
            throw new IllegalArgumentException(ingredientType + " cannot be created from an empty stack.");
        }
    }

    /**
     * Combines multiple ingredients into a single ingredient.
     */
    @SafeVarargs
    static <INGREDIENT extends InputIngredient<?>> INGREDIENT createMulti(String ingredientType, IIngredientCreator<?, ?, INGREDIENT> creator, INGREDIENT... ingredients) {
        if (ingredients.length == 0) {
            throw new IllegalArgumentException("Multi " + ingredientType + " ingredients cannot be made out of no ingredients!");
        } else if (ingredients.length == 1) {
            //While this technically isn't needed because the multi creator methods also do this, it is good to be on the safe side
            return ingredients[0];
        }
        return creator.createMulti(ingredients);
    }
}