package mekanism.common.recipe;

import java.util.stream.Stream;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import mekanism.common.util.RecipeUtils;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.Ingredient;

//TODO: Make our recipes that care about keeping energy and stuff and mark them as "Special" in the same way repair recipes work for grabbing data
public class MekanismIngredient extends Ingredient {

    private final ItemStack stack;

    protected MekanismIngredient(ItemStack stack) {
        //TODO: Check if this is correct
        super(Stream.of(new Ingredient.SingleItemList(stack)));
        this.stack = stack;
    }

    @Nonnull
    public static Ingredient fromStacks(ItemStack... stacks) {
        if (stacks.length > 0) {
            for (ItemStack stack : stacks) {
                if (!stack.isEmpty()) {
                    return new MekanismIngredient(stack);
                }
            }
        }
        return EMPTY;
    }

    @Override
    public boolean test(@Nullable ItemStack input) {
        if (input == null) {
            return false;
        }
        return RecipeUtils.areItemsEqualForCrafting(this.stack, input);
    }

    //TODO: Support serializing this if we are keeping MekanismIngredient
    // Used in _factories.json
    /*public static class IngredientFactory implements IIngredientFactory {

        @Nonnull
        @Override
        public Ingredient parse(JsonContext context, JsonObject json) {
            final ItemStack stack = CraftingHelper.getItemStack(json, context);
            return MekanismIngredient.fromStacks(stack);
        }
    }*/
}