package mekanism.common.recipe;

import com.google.gson.JsonObject;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import mekanism.common.util.RecipeUtils;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.Ingredient;
import net.minecraftforge.common.crafting.CraftingHelper;
import net.minecraftforge.common.crafting.IIngredientFactory;
import net.minecraftforge.common.crafting.JsonContext;

public class MekanismIngredient extends Ingredient {

    private final ItemStack stack;

    protected MekanismIngredient(ItemStack stack) {
        super(stack);
        this.stack = stack;
    }

    public static Ingredient fromStacks(ItemStack... stacks) {
        if (stacks.length > 0) {
            for (ItemStack itemstack : stacks) {
                if (!itemstack.isEmpty()) {
                    return new MekanismIngredient(itemstack);
                }
            }
        }

        return EMPTY;
    }

    @Override
    public boolean apply(@Nullable ItemStack input) {
        if (input == null) {
            return false;
        }
        return RecipeUtils.areItemsEqualForCrafting(this.stack, input);
    }

    // Used in _factories.json
    public static class IngredientFactory implements IIngredientFactory {

        @Nonnull
        @Override
        public Ingredient parse(JsonContext context, JsonObject json) {
            final ItemStack stack = CraftingHelper.getItemStack(json, context);

            return MekanismIngredient.fromStacks(stack);
        }
    }
}
