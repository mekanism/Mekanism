package mekanism.common.integration.crafttweaker.ingredient;

import com.blamejared.crafttweaker.api.annotations.ZenRegister;
import com.blamejared.crafttweaker.api.brackets.CommandStringDisplayable;
import com.blamejared.crafttweaker.impl.tag.MCTag;
import java.util.function.Function;
import java.util.function.IntFunction;
import mekanism.api.recipes.inputs.InputIngredient;
import mekanism.common.integration.crafttweaker.CrTConstants;
import net.minecraft.tags.ITag;
import org.openzen.zencode.java.ZenCodeType;

@ZenRegister
@ZenCodeType.Name(CrTConstants.CLASS_INGREDIENT_WRAPPER)
public class CrTIngredientWrapper<TYPE, INGREDIENT extends InputIngredient<TYPE>> {

    /**
     * Validates that the amount is greater than zero. If it is not it throws an error.
     */
    protected static void assertValidAmount(String ingredientType, long amount) {
        if (amount <= 0) {
            throw new IllegalArgumentException(ingredientType + " can only be created with a size of at least one. Received size was: " + amount);
        }
    }

    /**
     * Validates that the amount is greater than zero and that the tag exists. If it does it gets and returns the tag, otherwise it throws an error.
     */
    protected static <TYPE, CRT_TYPE extends CommandStringDisplayable> ITag<TYPE> assertValidAndGet(MCTag<CRT_TYPE> crtTag, long amount,
          Function<MCTag<CRT_TYPE>, ITag<TYPE>> getter, String ingredientType) {
        assertValidAmount(ingredientType, amount);
        ITag<TYPE> tag = getter.apply(crtTag);
        if (tag == null) {
            throw new IllegalArgumentException("Tag " + crtTag.getCommandString() + " does not exist.");
        }
        return tag;
    }

    @SafeVarargs
    protected static <TYPE, INGREDIENT extends InputIngredient<TYPE>, CRT_INGREDIENT extends CrTIngredientWrapper<TYPE, INGREDIENT>> CRT_INGREDIENT
    createMulti(String ingredientType, IntFunction<INGREDIENT[]> arrayCreator, Function<INGREDIENT[], CRT_INGREDIENT> multiCreator, CRT_INGREDIENT... crtIngredients) {
        if (crtIngredients.length == 0) {
            throw new IllegalArgumentException("Multi " + ingredientType + " ingredients cannot be made out of no ingredients!");
        } else if (crtIngredients.length == 1) {
            return crtIngredients[0];
        }
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