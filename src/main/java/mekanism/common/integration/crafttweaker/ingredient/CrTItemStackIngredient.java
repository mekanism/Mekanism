package mekanism.common.integration.crafttweaker.ingredient;

import com.blamejared.crafttweaker.api.annotations.ZenRegister;
import com.blamejared.crafttweaker.api.item.IIngredient;
import com.blamejared.crafttweaker.api.item.IItemStack;
import com.blamejared.crafttweaker.impl.item.MCIngredientList;
import com.blamejared.crafttweaker.impl.item.MCItemDefinition;
import com.blamejared.crafttweaker.impl.tag.MCTag;
import com.blamejared.crafttweaker.impl.tag.manager.TagManagerItem;
import java.util.ArrayList;
import java.util.List;
import mekanism.api.recipes.inputs.ItemStackIngredient;
import mekanism.common.integration.crafttweaker.CrTConstants;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.tags.ITag;
import org.openzen.zencode.java.ZenCodeType;

@ZenRegister
@ZenCodeType.Name(CrTConstants.CLASS_ITEM_STACK_INGREDIENT)
public class CrTItemStackIngredient extends CrTIngredientWrapper<ItemStack, ItemStackIngredient> {

    @ZenCodeType.Method
    public static CrTItemStackIngredient from(IItemStack stack) {
        if (stack.isEmpty()) {
            throw new IllegalArgumentException("ItemStackIngredients cannot be created from an empty stack.");
        }
        return from(stack, stack.getAmount());
    }

    @ZenCodeType.Method
    public static CrTItemStackIngredient from(MCItemDefinition item) {
        return from(item, 1);
    }

    @ZenCodeType.Method
    public static CrTItemStackIngredient from(MCItemDefinition item, int amount) {
        assertValidAmount("ItemStackIngredients", amount);
        return new CrTItemStackIngredient(ItemStackIngredient.from(item.getInternal(), amount));
    }

    @ZenCodeType.Method
    public static CrTItemStackIngredient from(MCTag<MCItemDefinition> itemTag) {
        return from(itemTag, 1);
    }

    @ZenCodeType.Method
    public static CrTItemStackIngredient from(MCTag<MCItemDefinition> itemTag, int amount) {
        ITag<Item> tag = assertValidAndGet(itemTag, amount, TagManagerItem.INSTANCE::getInternal, "ItemStackIngredients");
        return new CrTItemStackIngredient(ItemStackIngredient.from(tag, amount));
    }

    @ZenCodeType.Method
    public static CrTItemStackIngredient from(IIngredient ingredient) {
        return from(ingredient, 1);
    }

    @ZenCodeType.Method
    public static CrTItemStackIngredient from(IIngredient ingredient, int amount) {
        assertValidAmount("ItemStackIngredients", amount);
        //Note: the IIngredient cases also handle item tags/item stacks
        Ingredient vanillaIngredient = ingredient.asVanillaIngredient();
        if (vanillaIngredient == Ingredient.EMPTY) {
            throw new IllegalArgumentException("ItemStackIngredients cannot be made using the empty ingredient: " + amount);
        }
        return new CrTItemStackIngredient(ItemStackIngredient.from(vanillaIngredient, amount));
    }

    @ZenCodeType.Method
    public static CrTItemStackIngredient from(MCIngredientList ingredientList) {
        IIngredient[] ingredients = ingredientList.getIngredients();
        if (ingredients.length == 0) {
            throw new IllegalArgumentException("ItemStackIngredients cannot be created from an empty ingredient list!");
        }
        List<CrTItemStackIngredient> crtIngredients = new ArrayList<>();
        addIngredients(crtIngredients, ingredients);
        return createMulti(crtIngredients.toArray(new CrTItemStackIngredient[0]));
    }

    private static void addIngredients(List<CrTItemStackIngredient> crtIngredients, IIngredient[] ingredients) {
        for (IIngredient ingredient : ingredients) {
            if (ingredient instanceof IItemStack) {
                //If the ingredient is an IItemStack make sure to process it as such so
                crtIngredients.add(from((IItemStack) ingredient));
            } else if (ingredient instanceof MCIngredientList) {
                //If it is another multi ingredient add the different components
                addIngredients(crtIngredients, ((MCIngredientList) ingredient).getIngredients());
            } else {
                crtIngredients.add(from(ingredient));
            }
        }
    }

    @ZenCodeType.Method
    public static CrTItemStackIngredient createMulti(CrTItemStackIngredient... crtIngredients) {
        return createMulti("ItemStackIngredients", ItemStackIngredient[]::new,
              ingredients -> new CrTItemStackIngredient(ItemStackIngredient.createMulti(ingredients)), crtIngredients);
    }

    private CrTItemStackIngredient(ItemStackIngredient ingredient) {
        super(ingredient);
    }
}