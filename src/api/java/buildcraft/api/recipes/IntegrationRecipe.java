package buildcraft.api.recipes;

import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.google.common.collect.ImmutableList;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;

import buildcraft.api.core.BuildCraftAPI;

public final class IntegrationRecipe {
    public final ResourceLocation name;
    public final long requiredMicroJoules;
    public final StackDefinition target;
    public final ImmutableList<StackDefinition> toIntegrate;
    public final @Nonnull ItemStack output;
    /**
     * Additional tag used to restore network-transmitted recipe to same state
     * You need to register own {@link IIntegrationRecipeProvider} using {@link IIntegrationRecipeRegistry#addRecipeProvider(IIntegrationRecipeProvider)}
     * to handle this and declare {@link IIntegrationRecipeProvider#getRecipe(ResourceLocation, NBTTagCompound)} method
     */
    public final @Nullable NBTTagCompound recipeTag;

    public IntegrationRecipe(ResourceLocation name, long requiredMicroJoules, StackDefinition target, List<StackDefinition> toIntegrate, @Nonnull ItemStack output, @Nullable NBTTagCompound recipeTag) {
        this.name = name;
        this.requiredMicroJoules = requiredMicroJoules;
        this.target = target;
        this.toIntegrate = ImmutableList.copyOf(toIntegrate);
        this.output = output;
        this.recipeTag = recipeTag;
    }

    public IntegrationRecipe(String name, long requiredMicroJoules, StackDefinition target, List<StackDefinition> toIntegrate, @Nonnull ItemStack output, @Nullable NBTTagCompound recipeTag) {
        this(BuildCraftAPI.nameToResourceLocation(name), requiredMicroJoules, target, toIntegrate, output, recipeTag);
    }

    public IntegrationRecipe(ResourceLocation name, long requiredMicroJoules, StackDefinition target, List<StackDefinition> toIntegrate, @Nonnull ItemStack output) {
        this(name, requiredMicroJoules, target, toIntegrate, output, null);
    }

    public IntegrationRecipe(String name, long requiredMicroJoules, StackDefinition target, List<StackDefinition> toIntegrate, @Nonnull ItemStack output) {
        this(name, requiredMicroJoules, target, toIntegrate, output, null);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        IntegrationRecipe that = (IntegrationRecipe) o;

        return name.equals(that.name);
    }

    @Override
    public int hashCode() {
        return name.hashCode();
    }
}
