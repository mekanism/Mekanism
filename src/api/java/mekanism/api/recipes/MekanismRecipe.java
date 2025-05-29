package mekanism.api.recipes;

import mekanism.api.MekanismAPI;
import net.minecraft.core.HolderLookup;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeInput;
import org.jetbrains.annotations.NotNull;

/**
 * Base class for helping wrap our recipes into IRecipes.
 */
public abstract class MekanismRecipe<INPUT extends RecipeInput> implements Recipe<INPUT> {
    //TODO: Should we make implementations override equals and hashcode?

    @Override
    public boolean isSpecial() {
        //Note: If we make this non-dynamic, we can make it show in vanilla's crafting book and also then obey the recipe locking.
        // For now none of that works/makes sense in our concept so don't lock it
        return true;
    }

    //Force implementation of this method as our ingredients is always empty so the super implementation would have all ours as incomplete
    @Override
    public abstract boolean isIncomplete();

    //todo 1.21.5 or 1.22: make this abstract
    public void logMissingTags() {
        MekanismAPI.logger.error("Please implement logMissingTags(): {}", this);
    }

    @NotNull
    @Override
    public ItemStack assemble(@NotNull INPUT input, @NotNull HolderLookup.Provider provider) {
        return ItemStack.EMPTY;
    }

    @Override
    public boolean canCraftInDimensions(int width, int height) {
        return true;
    }

    @NotNull
    @Override
    public ItemStack getResultItem(@NotNull HolderLookup.Provider provider) {
        return ItemStack.EMPTY;
    }
}