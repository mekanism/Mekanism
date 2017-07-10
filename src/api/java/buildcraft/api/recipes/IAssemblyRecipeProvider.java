package buildcraft.api.recipes;

import java.util.List;
import java.util.Optional;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;

/** Provides a way of registering complex recipes without needing to register every possible variant. If you want the
 * recipes to be viewable in JEI and the guide book then you will *also* need to implement the bc lib class
 * IRecipeViewable. */
public interface IAssemblyRecipeProvider {

    /** Should return a list of all the valid recipes given the input items. The list may be empty, but must not be
     * null. Note that this will *most likely* be slow to check everything, so you should cache the return value and
     * re-use it when you can. */
    @Nonnull
    List<AssemblyRecipe> getRecipesFor(@Nonnull NonNullList<ItemStack> possible);

    /**
     * Returns recipe by it's name
     * @param recipeTag Additional tag attached to network-transmitted recipe definition
     */
    default Optional<AssemblyRecipe> getRecipe(@Nonnull ResourceLocation name, @Nullable NBTTagCompound recipeTag) {
        return Optional.empty();
    }
}
