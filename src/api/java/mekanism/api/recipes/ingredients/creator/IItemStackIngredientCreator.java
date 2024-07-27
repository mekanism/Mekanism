package mekanism.api.recipes.ingredients.creator;

import java.util.Objects;
import mekanism.api.annotations.NothingNullByDefault;
import mekanism.api.recipes.ingredients.ItemStackIngredient;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponentPredicate;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.ItemLike;
import net.neoforged.neoforge.common.crafting.DataComponentIngredient;
import net.neoforged.neoforge.common.crafting.SizedIngredient;

@NothingNullByDefault
public interface IItemStackIngredientCreator extends IIngredientCreator<Item, ItemStack, ItemStackIngredient> {

    /**
     * {@inheritDoc}
     *
     * @implNote If the stack has any non-default data components, a non-strict component matching those additions will be used.
     */
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
     * @implNote If the stack has any non-default data components, a non-strict component matching those additions will be used.
     */
    default ItemStackIngredient from(ItemStack stack, int amount) {
        Objects.requireNonNull(stack, "ItemStackIngredients cannot be created from a null ItemStack.");
        if (stack.isEmpty()) {
            throw new IllegalArgumentException("ItemStackIngredients cannot be created using the empty stack.");
        }
        //Copy the stack to ensure it doesn't get modified afterward
        stack = stack.copy();
        //Support Components that are on the stack in case it matters
        // Note: Only bother making it a data component ingredient if the stack has non-default data, otherwise there is no point in doing the extra checks
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
     * @implNote This wraps via {@link #from(Ingredient)} so if there is any durability or default NBT it will <strong>NOT</strong> be included in the ingredient. If this
     * is not desired, manually create the ingredient via {@link DataComponentIngredient} and call {@link #from(Ingredient)}.
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
     * @implNote This wraps via {@link #from(Ingredient, int)} so if there is any durability or default NBT it will <strong>NOT</strong> be included in the ingredient. If
     * this is not desired, manually create the ingredient via {@link DataComponentIngredient} and call {@link #from(Ingredient, int)}.
     */
    default ItemStackIngredient from(ItemLike item, int amount) {
        return from(Ingredient.of(item), amount);
    }

    /**
     * Creates an Item Stack Ingredient that matches a provided items.
     *
     * @param items Item providers that provides the items to match.
     *
     * @throws IllegalArgumentException if no items are passed.
     * @implNote This wraps via {@link #from(Ingredient)} so if there is any durability or default NBT it will <strong>NOT</strong> be included in the ingredient. If this
     * is not desired, manually create the ingredients via {@link DataComponentIngredient} and call {@link #from(Ingredient)}.
     * @since 10.6.0
     */
    default ItemStackIngredient from(ItemLike... items) {
        return from(1, items);
    }

    /**
     * Creates an Item Stack Ingredient that matches a provided items and amount.
     *
     * @param amount Amount needed.
     * @param items  Item providers that provides the items to match.
     *
     * @throws IllegalArgumentException if no items are passed.
     * @implNote This wraps via {@link #from(Ingredient, int)} so if there is any durability or default NBT it will <strong>NOT</strong> be included in the ingredient. If
     * this is not desired, manually create the ingredients via {@link DataComponentIngredient} and call {@link #from(Ingredient, int)}.
     * @since 10.6.0
     */
    default ItemStackIngredient from(int amount, ItemLike... items) {
        if (items.length == 0) {
            throw new IllegalArgumentException("Attempted to create an ItemStackIngredient with no items.");
        }
        return from(Ingredient.of(items), amount);
    }

    /**
     * {@inheritDoc}
     *
     * @implNote This wraps via {@link #from(Ingredient)} so if there is any durability or default NBT it will <strong>NOT</strong> be included in the ingredient. If this
     * is not desired, manually create the ingredient via {@link DataComponentIngredient} and call {@link #from(Ingredient)}.
     */
    @Override
    default ItemStackIngredient from(Item item, int amount) {
        return from((ItemLike) item, amount);
    }

    /**
     * {@inheritDoc}
     *
     * @implNote This wraps via {@link #from(Ingredient)} so if there is any durability or default NBT it will <strong>NOT</strong> be included in the ingredient. If this
     * is not desired, manually create the ingredient via {@link DataComponentIngredient} and call {@link #from(Ingredient)}.
     * @since 10.6.0
     */
    @Override
    default ItemStackIngredient from(int amount, Item... items) {
        return from(amount, (ItemLike[]) items);
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
    default ItemStackIngredient from(SizedIngredient ingredient) {
        return ItemStackIngredient.of(ingredient);
    }

    default ItemStackIngredient from(HolderLookup.Provider registries, ResourceLocation itemId) {
        return fromHolder(registries.lookupOrThrow(Registries.ITEM).getOrThrow(ResourceKey.create(Registries.ITEM, itemId)));
    }
}