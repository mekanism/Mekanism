package mekanism.api.recipes;

import mekanism.api.annotations.NothingNullByDefault;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.PlacementInfo;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeBookCategories;
import net.minecraft.world.item.crafting.RecipeBookCategory;
import net.minecraft.world.item.crafting.RecipeInput;

/**
 * Base class for helping wrap our recipes into IRecipes.
 */
@NothingNullByDefault//todo 26.1 implement display()?
public abstract class MekanismRecipe<INPUT extends RecipeInput> implements Recipe<INPUT> {
    //TODO: Should we make implementations override equals and hashcode?

    private final String group;

    protected MekanismRecipe() {
        this("");//TODO - 1.21.11: Remove this constructor and force usage of groups
    }

    protected MekanismRecipe(String group) {
        this.group = group;
    }

    @Override
    public final String group() {
        return group;
    }

    @Override
    public boolean isSpecial() {
        //Note: If we make this non-dynamic, we can make it show in vanilla's crafting book and also then obey the recipe locking.
        // For now none of that works/makes sense in our concept so don't lock it
        return true;
    }

    //Force implementation of this method as our ingredients is always empty so the super implementation would have all ours as incomplete
    //@Override//TODO - 1.21.11: Re-evaluate this, we might want to keep some form of it?
    public abstract boolean isIncomplete();//TODO - 1.21.11: This is now part of PlacementInfo#isImpossibleToPlace ??

    public abstract void logMissingTags();

    @Override
    public ItemStack assemble(INPUT input) {
        return ItemStack.EMPTY;
    }

    public ItemStack getToastSymbol() {//TODO - 1.21.11: Remove this after removing the things that use it
        return ItemStack.EMPTY;
    }

    @Override
    public PlacementInfo placementInfo() {
        return PlacementInfo.NOT_PLACEABLE;
    }

    @Override
    public boolean showNotification() {
        return false;
    }

    @Override
    public RecipeBookCategory recipeBookCategory() {
        return RecipeBookCategories.CRAFTING_MISC;
    }
}