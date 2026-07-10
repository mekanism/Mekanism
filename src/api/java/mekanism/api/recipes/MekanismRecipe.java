package mekanism.api.recipes;

import java.util.List;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.PlacementInfo;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeBookCategories;
import net.minecraft.world.item.crafting.RecipeBookCategory;
import net.minecraft.world.item.crafting.RecipeInput;
import net.minecraft.world.item.crafting.display.RecipeDisplay;

/// Base class for helping wrap our recipes into IRecipes.
public abstract class MekanismRecipe<INPUT extends RecipeInput> implements Recipe<INPUT> {

    private static final Recipe.CommonInfo NO_DISPLAY = new Recipe.CommonInfo(false);

    private final Recipe.CommonInfo commonInfo;
    private final String group;

    protected MekanismRecipe() {
        this(NO_DISPLAY, "");//TODO - 26.2: Remove this constructor and force usage of groups
    }

    protected MekanismRecipe(Recipe.CommonInfo commonInfo, String group) {
        this.commonInfo = commonInfo;
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
    //@Override//TODO - 26.2: Re-evaluate this, we might want to keep some form of it?
    public abstract boolean isIncomplete();//TODO - 26.2: This is now part of PlacementInfo#isImpossibleToPlace ??

    public abstract void logMissingTags();

    @Override
    public ItemStack assemble(INPUT input) {
        return ItemStack.EMPTY;
    }

    @Override
    public PlacementInfo placementInfo() {
        //TODO: Can we implement this?
        return PlacementInfo.NOT_PLACEABLE;
    }

    @Override
    public final boolean showNotification() {
        return commonInfo.showNotification();
    }

    @Override//Force implementation
    public abstract List<RecipeDisplay> display();

    @Override
    public RecipeBookCategory recipeBookCategory() {
        //TODO: Support custom recipe book categories?
        return RecipeBookCategories.CRAFTING_MISC;
    }
}