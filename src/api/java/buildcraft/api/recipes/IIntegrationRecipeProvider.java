package buildcraft.api.recipes;

import java.util.Optional;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;

/** Provides a way of registering complex recipes without needing to register every possible variant. If you want the
 * recipes to be viewable in JEI and the guide book then you will *also* need to implement the bc lib class
 * IIntegrationRecipeViewable. */
public interface IIntegrationRecipeProvider {
    /** Gets an integration recipe for the given ingredients.
     * 
     * @param target The center itemstack.
     * @param toIntegrate A list of stacks to try to integrate to the
     * @return */
    @Nullable
    IntegrationRecipe getRecipeFor(@Nonnull ItemStack target, @Nonnull NonNullList<ItemStack> toIntegrate);

    /**
     * Returns recipe by it's name
     * @param recipeTag Additional tag attached to network-transmitted recipe definition
     */
    default Optional<IntegrationRecipe> getRecipe(@Nonnull ResourceLocation name, @Nullable NBTTagCompound recipeTag) {
        return Optional.empty();
    }
}
