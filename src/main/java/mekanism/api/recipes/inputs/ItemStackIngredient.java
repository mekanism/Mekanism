package mekanism.api.recipes.inputs;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import mekanism.api.annotations.NonNull;
import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.Ingredient;
import net.minecraftforge.oredict.OreIngredient;

public class ItemStackIngredient implements InputPredicate<@NonNull ItemStack> {

    //TODO: Make ones that take a list of blocks/items

    public static ItemStackIngredient from(@NonNull ItemStack stack) {
        return from(stack, stack.getCount());
    }

    public static ItemStackIngredient from(@NonNull ItemStack stack, int amount) {
        return from(Ingredient.fromStacks(stack), amount);
    }

    public static ItemStackIngredient from(@NonNull Block block) {
        return from(block, 1);
    }

    public static ItemStackIngredient from(@NonNull Block block, int amount) {
        return from(new ItemStack(block), amount);
    }

    public static ItemStackIngredient from(@NonNull Item item) {
        return from(item, 1);
    }

    public static ItemStackIngredient from(@NonNull Item item, int amount) {
        //By default don't do any wildcard stuff.
        //TODO: Check if anything that is calling this should actually wants the wild card
        return from(new ItemStack(item), amount);
    }

    public static ItemStackIngredient from(@NonNull String oreName) {
        //TODO: 1.14 replace with tags
        return from(oreName, 1);
    }

    public static ItemStackIngredient from(@NonNull String oreName, int amount) {
        //TODO: 1.14 replace with tags
        return from(new OreIngredient(oreName), amount);
    }

    public static ItemStackIngredient from(@NonNull Ingredient ingredient) {
        return from(ingredient, 1);
    }

    public static ItemStackIngredient from(@NonNull Ingredient ingredient, int amount) {
        return new ItemStackIngredient(ingredient, amount);
    }

    @NonNull
    private final Ingredient ingredient;
    private final int amount;

    public ItemStackIngredient(@NonNull Ingredient ingredient, int amount) {
        this.ingredient = Objects.requireNonNull(ingredient);
        this.amount = amount;
    }

    @Override
    public boolean test(@NonNull ItemStack stack) {
        return testType(stack) && stack.getCount() >= amount;
    }

    @Override
    public boolean testType(@NonNull ItemStack stack) {
        //TODO: Should this fail on empty stacks
        return ingredient.apply(stack);
    }

    /**
     * Primarily for JEI, a list of valid instances of the stack (i.e. the ItemStack(s) that match the ingredient with the proper size)
     *
     * @return List (empty means no valid registrations found and recipe is to be hidden)
     */
    @NonNull
    @Override
    public List<ItemStack> getRepresentations() {
        //TODO: Can this be cached some how
        List<ItemStack> representations = new ArrayList<>();
        for (ItemStack stack : ingredient.getMatchingStacks()) {
            //TODO: if there a cleaner way to do this that doesn't require copying at least when the size is the same
            ItemStack copy = stack.copy();
            copy.setCount(amount);
            representations.add(copy);
        }
        return representations;
    }
}