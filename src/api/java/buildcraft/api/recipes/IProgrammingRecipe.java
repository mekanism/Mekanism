package buildcraft.api.recipes;

import java.util.List;

import net.minecraft.item.ItemStack;

public interface IProgrammingRecipe {
    String getId();

    /** Get a list (size at least width*height) of ItemStacks representing options.
     * 
     * @param width The width of the Programming Table panel.
     * @param height The height of the Programming Table panel.
     * @return */
    List<ItemStack> getOptions(int width, int height);

    /** Get the energy cost of a given option ItemStack.
     * 
     * @param option
     * @return */
    int getEnergyCost(ItemStack option);

    /** @param input The input stack.
     * @return Whether this recipe applies to the given input stack. */
    boolean canCraft(ItemStack input);

    /** Craft the input ItemStack with the given option into an output ItemStack.
     * 
     * @param input
     * @param option
     * @return The output ItemStack. */
    ItemStack craft(ItemStack input, ItemStack option);
}
