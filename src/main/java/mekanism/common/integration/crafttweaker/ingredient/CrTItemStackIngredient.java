package mekanism.common.integration.crafttweaker.ingredient;

import com.blamejared.crafttweaker.api.annotations.ZenRegister;
import com.blamejared.crafttweaker.api.item.IIngredient;
import com.blamejared.crafttweaker.api.item.IItemStack;
import com.blamejared.crafttweaker.impl.tag.MCTag;
import mekanism.api.recipes.inputs.ItemStackIngredient;
import mekanism.common.integration.crafttweaker.CrTConstants;
import net.minecraft.item.ItemStack;
import org.openzen.zencode.java.ZenCodeType;

@ZenRegister
@ZenCodeType.Name(CrTConstants.CLASS_ITEM_STACK_INGREDIENT)
public class CrTItemStackIngredient extends CrTIngredientWrapper<ItemStack, ItemStackIngredient> {

    @ZenCodeType.Method
    public static CrTItemStackIngredient from(IItemStack stack) {//TODO: Look at also supporting MCItemDefinition if/when it becomes a thing
        if (stack.isEmpty()) {
            throw new IllegalArgumentException("ItemStackIngredients cannot be created from an empty stack.");
        }
        return from(stack, stack.getAmount());
    }

    @ZenCodeType.Method
    public static CrTItemStackIngredient from(MCTag itemTag) {
        return from(itemTag, 1);
    }

    @ZenCodeType.Method
    public static CrTItemStackIngredient from(MCTag itemTag, int amount) {
        if (itemTag.isItemTag()) {
            return from((IIngredient) itemTag, amount);
        }
        throw new IllegalArgumentException("Tag " + itemTag.getCommandString() + " is not an ItemTag");
    }

    @ZenCodeType.Method
    public static CrTItemStackIngredient from(IIngredient ingredient) {
        return from(ingredient, 1);
    }

    @ZenCodeType.Method
    public static CrTItemStackIngredient from(IIngredient ingredient, int amount) {
        assertValidAmount("ItemStackIngredients", amount);
        //Note: the IIngredient cases also handle item tags/item stacks
        return new CrTItemStackIngredient(ItemStackIngredient.from(ingredient.asVanillaIngredient(), amount));
    }

    @ZenCodeType.Method
    public static CrTItemStackIngredient createMulti(CrTItemStackIngredient... crtIngredients) {
        return createMulti(ItemStackIngredient[]::new, ingredients -> new CrTItemStackIngredient(ItemStackIngredient.createMulti(ingredients)), crtIngredients);
    }

    private CrTItemStackIngredient(ItemStackIngredient ingredient) {
        super(ingredient);
    }
}