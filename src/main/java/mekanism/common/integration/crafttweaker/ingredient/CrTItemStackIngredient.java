package mekanism.common.integration.crafttweaker.ingredient;

import com.blamejared.crafttweaker.api.annotation.ZenRegister;
import com.blamejared.crafttweaker.api.data.base.IData;
import com.blamejared.crafttweaker.api.data.base.converter.JSONConverter;
import com.blamejared.crafttweaker.api.ingredient.IIngredient;
import com.blamejared.crafttweaker.api.ingredient.IIngredientWithAmount;
import com.blamejared.crafttweaker.api.ingredient.type.IIngredientList;
import com.blamejared.crafttweaker.api.item.IItemStack;
import com.blamejared.crafttweaker.api.item.MCItemStack;
import com.blamejared.crafttweaker.api.tag.type.KnownTag;
import com.blamejared.crafttweaker.api.util.Many;
import com.blamejared.crafttweaker_annotations.annotations.NativeTypeRegistration;
import java.util.ArrayList;
import java.util.List;
import mekanism.api.recipes.ingredients.ItemStackIngredient;
import mekanism.api.recipes.ingredients.creator.IngredientCreatorAccess;
import mekanism.common.integration.crafttweaker.CrTConstants;
import mekanism.common.integration.crafttweaker.CrTUtils;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.crafting.Ingredient;
import org.openzen.zencode.java.ZenCodeType;

@ZenRegister
@NativeTypeRegistration(value = ItemStackIngredient.class, zenCodeName = CrTConstants.CLASS_ITEM_STACK_INGREDIENT)
public class CrTItemStackIngredient {

    private CrTItemStackIngredient() {
    }

    /**
     * Creates a {@link ItemStackIngredient} that matches a given item stack.
     *
     * @param stack Item stack to match
     *
     * @return A {@link ItemStackIngredient} that matches a given item stack.
     */
    @ZenCodeType.StaticExpansionMethod
    public static ItemStackIngredient from(IItemStack stack) {
        if (stack.isEmpty()) {
            throw new IllegalArgumentException("ItemStackIngredients cannot be created from an empty stack.");
        }
        return from(stack, stack.getAmount());
    }

    /**
     * Creates a {@link ItemStackIngredient} that matches a given item with an amount of one.
     *
     * @param item Item to match
     *
     * @return A {@link ItemStackIngredient} that matches a given item with an amount of one.
     */
    @ZenCodeType.StaticExpansionMethod
    public static ItemStackIngredient from(Item item) {
        return from(item, 1);
    }

    /**
     * Creates a {@link ItemStackIngredient} that matches a given item and amount.
     *
     * @param item   Item to match
     * @param amount Amount needed
     *
     * @return A {@link ItemStackIngredient} that matches a given item and amount.
     */
    @ZenCodeType.StaticExpansionMethod
    public static ItemStackIngredient from(Item item, int amount) {
        CrTIngredientHelper.assertValidAmount("ItemStackIngredients", amount);
        return IngredientCreatorAccess.item().from(item, amount);
    }

    /**
     * Creates a {@link ItemStackIngredient} that matches a given item tag with a given amount.
     *
     * @param itemTag Tag to match
     * @param amount  Amount needed
     *
     * @return A {@link ItemStackIngredient} that matches a given item tag with a given amount.
     */
    @ZenCodeType.StaticExpansionMethod
    public static ItemStackIngredient from(KnownTag<Item> itemTag, int amount) {
        TagKey<Item> tag = CrTIngredientHelper.assertValidAndGet(itemTag, amount, "ItemStackIngredients");
        return IngredientCreatorAccess.item().from(tag, amount);
    }

    /**
     * Creates a {@link ItemStackIngredient} that matches a given item tag with an amount of one.
     *
     * @param itemTag Tag to match
     *
     * @return A {@link ItemStackIngredient} that matches a given item tag with an amount of one.
     */
    @ZenCodeType.StaticExpansionMethod
    public static ItemStackIngredient from(KnownTag<Item> itemTag) {
        return from(itemTag, 1);
    }

    /**
     * Creates a {@link ItemStackIngredient} that matches a given item tag with amount.
     *
     * @param itemTag Tag and amount to match
     *
     * @return A {@link ItemStackIngredient} that matches a given item tag with amount.
     */
    @ZenCodeType.StaticExpansionMethod
    public static ItemStackIngredient from(Many<KnownTag<Item>> itemTag) {
        return from(itemTag.getData(), itemTag.getAmount());
    }

    /**
     * Creates a {@link ItemStackIngredient} that matches a given ingredient with an amount of one.
     *
     * @param ingredient Ingredient to match
     *
     * @return A {@link ItemStackIngredient} that matches a given ingredient with an amount of one.
     */
    @ZenCodeType.StaticExpansionMethod
    public static ItemStackIngredient from(IIngredient ingredient) {
        return from(ingredient, 1);
    }

    /**
     * Creates a {@link ItemStackIngredient} that matches a given ingredient with amount.
     *
     * @param ingredient Ingredient and amount to match
     *
     * @return A {@link ItemStackIngredient} that matches a given ingredient with amount.
     */
    @ZenCodeType.StaticExpansionMethod
    public static ItemStackIngredient from(IIngredientWithAmount ingredient) {
        return from(ingredient.getIngredient(), ingredient.getAmount());
    }

    /**
     * Creates a {@link ItemStackIngredient} that matches a given ingredient and amount.
     *
     * @param ingredient Ingredient to match
     * @param amount     Amount needed
     *
     * @return A {@link ItemStackIngredient} that matches a given ingredient and amount.
     */
    @ZenCodeType.StaticExpansionMethod
    public static ItemStackIngredient from(IIngredient ingredient, int amount) {
        CrTIngredientHelper.assertValidAmount("ItemStackIngredients", amount);
        //Note: the IIngredient cases also handle item tags/item stacks
        Ingredient vanillaIngredient = ingredient.asVanillaIngredient();
        if (vanillaIngredient == Ingredient.EMPTY) {
            throw new IllegalArgumentException("ItemStackIngredients cannot be made using the empty ingredient: " + amount);
        }
        return IngredientCreatorAccess.item().from(vanillaIngredient, amount);
    }

    /**
     * Creates a {@link ItemStackIngredient} out of all the ingredients in the given {@link IIngredientList}.
     *
     * @param ingredientList Ingredients to match
     *
     * @return A {@link ItemStackIngredient} made up of all the ingredients in the given {@link IIngredientList}.
     */
    @ZenCodeType.StaticExpansionMethod
    public static ItemStackIngredient from(IIngredientList ingredientList) {
        IIngredient[] ingredients = ingredientList.getIngredients();
        if (ingredients.length == 0) {
            throw new IllegalArgumentException("ItemStackIngredients cannot be created from an empty ingredient list!");
        }
        List<ItemStackIngredient> itemStackIngredients = new ArrayList<>();
        addIngredients(itemStackIngredients, ingredients);
        return createMulti(itemStackIngredients.toArray(new ItemStackIngredient[0]));
    }

    private static void addIngredients(List<ItemStackIngredient> itemStackIngredients, IIngredient[] ingredients) {
        for (IIngredient ingredient : ingredients) {
            if (ingredient instanceof IItemStack stack) {
                //If the ingredient is an IItemStack make sure to process it as such so
                itemStackIngredients.add(from(stack));
            } else if (ingredient instanceof IIngredientList ingredientList) {
                //If it is another multi ingredient add the different components
                addIngredients(itemStackIngredients, ingredientList.getIngredients());
            } else {
                itemStackIngredients.add(from(ingredient));
            }
        }
    }

    /**
     * Combines multiple {@link ItemStackIngredient}s into a single {@link ItemStackIngredient}.
     *
     * @param ingredients Ingredients to combine
     *
     * @return A single {@link ItemStackIngredient} representing all the passed in ingredients.
     */
    @ZenCodeType.StaticExpansionMethod
    public static ItemStackIngredient createMulti(ItemStackIngredient... ingredients) {
        return CrTIngredientHelper.createMulti("ItemStackIngredients", IngredientCreatorAccess.item(), ingredients);
    }

    /**
     * Converts this {@link ItemStackIngredient} into JSON ({@link IData}).
     *
     * @return {@link ItemStackIngredient} as JSON.
     */
    @ZenCodeType.Method
    @ZenCodeType.Caster(implicit = true)
    public static IData asIData(ItemStackIngredient _this) {
        return JSONConverter.convert(_this.serialize());
    }

    /**
     * Checks if a given {@link IItemStack} has a type match for this {@link ItemStackIngredient}. Type matches ignore stack size.
     *
     * @param type Type to check for a match
     *
     * @return {@code true} if the type is supported by this {@link ItemStackIngredient}.
     */
    @ZenCodeType.Method
    public static boolean testType(ItemStackIngredient _this, IItemStack type) {
        return _this.testType(type.getInternal());
    }

    /**
     * Checks if a given {@link IItemStack} matches this {@link ItemStackIngredient}. (Checks size for >=)
     *
     * @param stack Stack to check for a match
     *
     * @return {@code true} if the stack fulfills the requirements for this {@link ItemStackIngredient}.
     */
    @ZenCodeType.Method
    public static boolean test(ItemStackIngredient _this, IItemStack stack) {
        return _this.test(stack.getInternal());
    }

    /**
     * Gets a list of valid instances for this {@link ItemStackIngredient}, may not include all or may be empty depending on how complex the ingredient is as the internal
     * version is mostly used for JEI display purposes.
     */
    @ZenCodeType.Method
    @ZenCodeType.Getter("representations")
    public static List<IItemStack> getRepresentations(ItemStackIngredient _this) {
        return CrTUtils.convert(_this.getRepresentations(), MCItemStack::new);
    }

    /**
     * OR's this {@link ItemStackIngredient} with another {@link ItemStackIngredient} to create a multi {@link ItemStackIngredient}
     *
     * @param other {@link ItemStackIngredient} to combine with.
     *
     * @return Multi {@link ItemStackIngredient} that matches both the source {@link ItemStackIngredient} and the OR'd {@link ItemStackIngredient}.
     */
    @ZenCodeType.Method
    @ZenCodeType.Operator(ZenCodeType.OperatorType.OR)
    public static ItemStackIngredient or(ItemStackIngredient _this, ItemStackIngredient other) {
        return IngredientCreatorAccess.item().createMulti(_this, other);
    }
}