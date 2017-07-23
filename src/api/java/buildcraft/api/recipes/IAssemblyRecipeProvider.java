package buildcraft.api.recipes;

import java.util.List;

import javax.annotation.Nonnull;

import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;

/** Provides a way of registering complex recipes without needing to register every possible variant. If you want the
 * recipes to be viewable in JEI and the guide book then you will *also* need to implement the bc lib class
 * IAssemblyRecipeViewable. */
public interface IAssemblyRecipeProvider {

    /** Should return a list of all the valid recipes given the input items. The list may be empty, but must not be
     * null. Note that this will *most likely* be slow to check everything, so you should cache the return value and
     * re-use it when you can. */
    @Nonnull
    List<AssemblyRecipe> getRecipesFor(@Nonnull NonNullList<ItemStack> possibleIn);
}
