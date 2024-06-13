package mekanism.tools.common.recipe;

import mekanism.api.annotations.NothingNullByDefault;
import mekanism.api.math.MathUtils;
import mekanism.common.recipe.WrappedShapedRecipe;
import mekanism.tools.common.registries.ToolsRecipeSerializers;
import net.minecraft.core.HolderLookup;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.ShapedRecipe;

@NothingNullByDefault
public class PaxelRecipe extends WrappedShapedRecipe {

    public PaxelRecipe(ShapedRecipe internal) {
        super(internal);
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return ToolsRecipeSerializers.PAXEL.get();
    }

    @Override
    public ItemStack assemble(CraftingInput inv, HolderLookup.Provider provider) {
        ItemStack resultItem = getResultItem(provider);
        if (resultItem.isEmpty()) {
            return ItemStack.EMPTY;
        }
        ItemStack toReturn = resultItem.copy();
        if (!toReturn.isDamageableItem() || toReturn.isDamaged()) {
            //If the output can't be damaged or is already damaged because someone is using this recipe in a weird way, just return the output
            return toReturn;
        }
        int totalDurability = 0;
        int totalMaxDurability = 0;
        for (int i = 0; i < inv.size(); i++) {
            ItemStack stack = inv.getItem(i);
            //Note: We check if the item for the stack is damageable rather than if the stack is damageable
            // so that if an item has the unbreakable flag on it, we still can take the percentage durability
            // into account properly.
            if (!stack.isEmpty() && stack.getItem().isDamageable(stack)) {
                //Note: We can just add all the damage values and max damage values together as no item should have
                // more durability than the maximum durability it can handle. Which means we can then calculate the
                // percent durability directly with these two numbers without having to care how many inputs were
                // damageable
                int max = stack.getMaxDamage();
                totalDurability += (max - stack.getDamageValue());
                totalMaxDurability += max;
            }
        }
        if (totalDurability == totalMaxDurability) {
            //If all our items are at max durability, just return the crafted stack
            return toReturn;
        }
        int maxDurability = toReturn.getMaxDamage();
        int targetDurability = MathUtils.clampToInt(maxDurability * (totalDurability / (double) totalMaxDurability));
        if (targetDurability == 0) {
            //If the components are so damaged that they don't equate to a single durability point
            // return that they do not match the recipe
            return ItemStack.EMPTY;
        } else if (targetDurability < maxDurability) {
            //If the item should be damaged and isn't at full durability, damage it
            toReturn.setDamageValue(maxDurability - targetDurability);
        }
        return toReturn;
    }
}