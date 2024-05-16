package mekanism.api.recipes.ingredients.creator;

import java.util.Objects;
import mekanism.api.annotations.NothingNullByDefault;
import mekanism.api.recipes.ingredients.ItemStackIngredient;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponentPredicate;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.ItemLike;
import net.neoforged.neoforge.common.crafting.DataComponentIngredient;
import net.neoforged.neoforge.common.crafting.SizedIngredient;

@NothingNullByDefault
public interface IItemStackIngredientCreator extends IIngredientCreator<Item, ItemStack, ItemStackIngredient> {

    @Override
    default ItemStackIngredient from(ItemStack instance) {
        Objects.requireNonNull(instance, "ItemStackIngredients cannot be created from a null ItemStack.");
        return from(instance, instance.getCount());
    }

    /**
     * Creates an Item Stack Ingredient that matches a given item stack with a specified amount.
     *
     * @param stack  Item stack to match.
     * @param amount Amount needed.
     *
     * @apiNote If the amount needed is the same as the stack's size, {@link #from(ItemStack)} can be used instead.
     */
    default ItemStackIngredient from(ItemStack stack, int amount) {
        Objects.requireNonNull(stack, "ItemStackIngredients cannot be created from a null ItemStack.");
        if (stack.isEmpty()) {
            throw new IllegalArgumentException("ItemStackIngredients cannot be created using the empty stack.");
        }
        //Copy the stack to ensure it doesn't get modified afterward
        stack = stack.copy();
        //Support Components that are on the stack in case it matters
        // Note: Only bother making it a data component ingredient if the stack has data, otherwise there is no point in doing the extra checks
        DataComponentPredicate predicate = IngredientCreatorAccess.getComponentPatchPredicate(stack.getComponentsPatch());
        if (predicate != null) {
            return from(DataComponentIngredient.of(false, predicate, stack.getItemHolder()), amount);
        }
        return from(Ingredient.of(stack), amount);
    }

    /**
     * Creates an Item Stack Ingredient that matches a provided item.
     *
     * @param item Item provider that provides the item to match.
     *
     * @implNote This wraps via {@link #from(ItemStack)} so if there is any durability or default NBT it will be included in the ingredient. If this is not desired,
     * manually create an ingredient and call {@link #from(Ingredient)}.
     * @since 10.5.0
     */
    default ItemStackIngredient fromHolder(Holder<Item> item) {
        return fromHolder(item, 1);
    }

    /**
     * Creates an Item Stack Ingredient that matches a provided item.
     *
     * @param item Item provider that provides the item to match.
     *
     * @implNote This wraps via {@link #from(ItemStack)} so if there is any durability or default NBT it will be included in the ingredient. If this is not desired,
     * manually create an ingredient and call {@link #from(Ingredient)}.
     */
    default ItemStackIngredient from(ItemLike item) {
        return from(item, 1);
    }

    /**
     * Creates an Item Stack Ingredient that matches a provided item and amount.
     *
     * @param item   Item provider that provides the item to match.
     * @param amount Amount needed.
     *
     * @implNote This wraps via {@link #from(ItemStack, int)} so if there is any durability or default NBT it will be included in the ingredient. If this is not desired,
     * manually create an ingredient and call {@link #from(Ingredient, int)}.
     */
    default ItemStackIngredient from(ItemLike item, int amount) {
        return from(new ItemStack(item), amount);
    }

    /**
     * {@inheritDoc}
     *
     * @implNote This wraps via {@link #from(ItemStack)} so if there is any durability or default NBT it will be included in the ingredient. If this is not desired,
     * manually create an ingredient and call {@link #from(Ingredient)}.
     */
    @Override
    default ItemStackIngredient from(Item item, int amount) {
        return from((ItemLike) item, amount);
    }

    /**
     * Creates an Item Stack Ingredient that matches a given Item tag.
     *
     * @param tag Tag to match.
     */
    default ItemStackIngredient from(TagKey<Item> tag) {
        return from(tag, 1);
    }

    @Override
    default ItemStackIngredient from(TagKey<Item> tag, int amount) {
        Objects.requireNonNull(tag, "ItemStackIngredients cannot be created from a null tag.");
        return from(Ingredient.of(tag), amount);
    }

    /**
     * Creates an Item Stack Ingredient that matches a given ingredient.
     *
     * @param ingredient Ingredient to match.
     */
    default ItemStackIngredient from(Ingredient ingredient) {
        return from(ingredient, 1);
    }

    /**
     * Creates an Item Stack Ingredient that matches a given ingredient and amount.
     *
     * @param ingredient Ingredient to match.
     * @param amount     Amount needed.
     *
     * @throws NullPointerException     if the given instance is null.
     * @throws IllegalArgumentException if the given instance is empty or an amount smaller than one.
     */
    default ItemStackIngredient from(Ingredient ingredient, int amount) {
        Objects.requireNonNull(ingredient, "ItemStackIngredients cannot be created from a null ingredient.");
        return from(new SizedIngredient(ingredient, amount));
    }

    /**
     * Creates an Item Stack Ingredient that matches a given ingredient and amount.
     *
     * @param ingredient Sized ingredient to match.
     *
     * @throws NullPointerException     if the given instance is null.
     * @throws IllegalArgumentException if the given instance is empty.
     * @since 10.6.0
     */
    ItemStackIngredient from(SizedIngredient ingredient);
}