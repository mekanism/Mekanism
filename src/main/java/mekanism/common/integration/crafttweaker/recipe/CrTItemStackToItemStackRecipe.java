package mekanism.common.integration.crafttweaker.recipe;

import com.blamejared.crafttweaker.api.annotation.ZenRegister;
import com.blamejared.crafttweaker.api.item.IItemStack;
import com.blamejared.crafttweaker.api.item.MCItemStack;
import com.blamejared.crafttweaker_annotations.annotations.NativeTypeRegistration;
import java.util.List;
import mekanism.api.recipes.ItemStackToItemStackRecipe;
import mekanism.api.recipes.ingredients.ItemStackIngredient;
import mekanism.common.integration.crafttweaker.CrTConstants;
import mekanism.common.integration.crafttweaker.CrTUtils;
import org.openzen.zencode.java.ZenCodeType;

@ZenRegister
@NativeTypeRegistration(value = ItemStackToItemStackRecipe.class, zenCodeName = CrTConstants.CLASS_RECIPE_ITEM_STACK_TO_ITEM_STACK)
public class CrTItemStackToItemStackRecipe {

    private CrTItemStackToItemStackRecipe() {
    }

    /**
     * Gets the input ingredient.
     */
    @ZenCodeType.Method
    @ZenCodeType.Getter("input")
    public static ItemStackIngredient getInput(ItemStackToItemStackRecipe _this) {
        return _this.getInput();
    }

    /**
     * Output representations, this list may or may not be complete and likely only contains one element, but has the possibility of containing multiple.
     */
    @ZenCodeType.Method
    @ZenCodeType.Getter("outputs")
    public static List<IItemStack> getOutputs(ItemStackToItemStackRecipe _this) {
        return CrTUtils.convert(_this.getOutputDefinition(), MCItemStack::new);
    }
}