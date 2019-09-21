package mekanism.api.recipes.inputs;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import mekanism.api.annotations.NonNull;
import mekanism.api.providers.IItemProvider;
import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.tags.Tag;
import net.minecraftforge.common.crafting.IngredientNBT;

//TODO: Allow for empty item stacks?
public abstract class ItemStackIngredient implements InputIngredient<@NonNull ItemStack> {

    //TODO: Make ones that take a list of blocks/items

    public static ItemStackIngredient from(@NonNull ItemStack stack) {
        return from(stack, stack.getCount());
    }

    public static ItemStackIngredient from(@NonNull ItemStack stack, int amount) {
        //Support NBT that is on the stack in case it matters
        //It is a protected constructor so pretend we are extending it and implementing it via the {}
        // Note: Only bother making it an NBT ingredient if the stack has NBT, otherwise there is no point in doing the extra checks
        //TODO: Figure out if this note is correct on what we should do
        Ingredient ingredient = stack.hasTag() ? new IngredientNBT(stack) {} : Ingredient.fromStacks(stack);
        return from(ingredient, amount);
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

    public static ItemStackIngredient from(@NonNull IItemProvider itemProvider) {
        return from(itemProvider, 1);
    }

    public static ItemStackIngredient from(@NonNull IItemProvider itemProvider, int amount) {
        return from(itemProvider.getItemStack(amount));
    }

    //TODO: Should we instead have it accept a Tag<Item> instead of a resource location
    public static ItemStackIngredient from(@NonNull Tag<Item> itemTag) {
        return from(itemTag, 1);
    }

    //TODO: Should we instead have it accept a Tag<Item> instead of a resource location
    public static ItemStackIngredient from(@NonNull Tag<Item> itemTag, int amount) {
        return from(Ingredient.fromTag(itemTag), amount);
    }

    public static ItemStackIngredient from(@NonNull Ingredient ingredient) {
        return from(ingredient, 1);
    }

    public static ItemStackIngredient from(@NonNull Ingredient ingredient, int amount) {
        return new Single(ingredient, amount);
    }

    public static class Single extends ItemStackIngredient {

        @NonNull
        private final Ingredient ingredient;
        private final int amount;

        public Single(@NonNull Ingredient ingredient, int amount) {
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
            return ingredient.test(stack);
        }

        @Override
        public @NonNull ItemStack getMatchingInstance(@NonNull ItemStack stack) {
            if (test(stack)) {
                ItemStack matching = stack.copy();
                matching.setCount(amount);
                return matching;
            }
            return ItemStack.EMPTY;
        }

        @NonNull
        @Override
        public List<@NonNull ItemStack> getRepresentations() {
            //TODO: Can this be cached some how
            List<@NonNull ItemStack> representations = new ArrayList<>();
            for (ItemStack stack : ingredient.getMatchingStacks()) {
                //TODO: if there a cleaner way to do this that doesn't require copying at least when the size is the same
                ItemStack copy = stack.copy();
                copy.setCount(amount);
                representations.add(copy);
            }
            return representations;
        }
    }

    //TODO: Maybe name this better, at the very least make it easier/possible to create new instances of this
    // Also cleanup the javadoc comment about this, and try to make the helpers that create a new instance
    // return a normal ItemStackIngredient (Single), if we only have a singular one
    public static class Multi extends ItemStackIngredient {

        private final ItemStackIngredient[] ingredients;

        protected Multi(@NonNull ItemStackIngredient... ingredients) {
            this.ingredients = ingredients;
        }

        @Override
        public boolean test(@NonNull ItemStack stack) {
            return Arrays.stream(ingredients).anyMatch(ingredient -> ingredient.test(stack));
        }

        @Override
        public boolean testType(@NonNull ItemStack stack) {
            return Arrays.stream(ingredients).anyMatch(ingredient -> ingredient.testType(stack));
        }

        @Override
        public @NonNull ItemStack getMatchingInstance(@NonNull ItemStack stack) {
            for (ItemStackIngredient ingredient : ingredients) {
                ItemStack matchingInstance = ingredient.getMatchingInstance(stack);
                if (!matchingInstance.isEmpty()) {
                    return matchingInstance;
                }
            }
            return ItemStack.EMPTY;
        }

        @NonNull
        @Override
        public List<@NonNull ItemStack> getRepresentations() {
            List<@NonNull ItemStack> representations = new ArrayList<>();
            for (ItemStackIngredient ingredient : ingredients) {
                representations.addAll(ingredient.getRepresentations());
            }
            return representations;
        }
    }
}