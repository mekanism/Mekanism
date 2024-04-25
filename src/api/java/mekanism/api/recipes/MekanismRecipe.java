package mekanism.api.recipes;

import mekanism.api.inventory.IgnoredIInventory;
import net.minecraft.core.HolderLookup;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;

/**
 * Base class for helping wrap our recipes into IRecipes.
 */
public abstract class MekanismRecipe implements Recipe<IgnoredIInventory> {//TODO: Should we make implementations override equals and hashcode?

    @Override
    public boolean matches(@NotNull IgnoredIInventory inv, @NotNull Level world) {
        //TODO: Decide if we ever want to make use of this method
        //Default to not being able to match incomplete recipes though
        return !isIncomplete();
    }

    @Override
    public boolean isSpecial() {
        //Note: If we make this non-dynamic, we can make it show in vanilla's crafting book and also then obey the recipe locking.
        // For now none of that works/makes sense in our concept so don't lock it
        return true;
    }

    //Force implementation of this method as our ingredients is always empty so the super implementation would have all ours as incomplete
    @Override
    public abstract boolean isIncomplete();

    @NotNull
    @Override
    public ItemStack assemble(@NotNull IgnoredIInventory inv, @NotNull HolderLookup.Provider provider) {
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