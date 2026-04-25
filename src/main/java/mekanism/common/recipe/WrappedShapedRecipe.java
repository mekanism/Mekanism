package mekanism.common.recipe;

import mekanism.api.annotations.NothingNullByDefault;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.ShapedRecipe;
import net.minecraft.world.item.crafting.ShapedRecipePattern;
import net.minecraft.world.level.Level;

@NothingNullByDefault
public abstract class WrappedShapedRecipe extends ShapedRecipe {

    protected WrappedShapedRecipe(Recipe.CommonInfo commonInfo, CraftingRecipe.CraftingBookInfo bookInfo, ShapedRecipePattern pattern, ItemStackTemplate result) {
        super(commonInfo, bookInfo, pattern, result);
    }

    @Override
    public boolean matches(CraftingInput input, Level world) {
        //Note: We do not override the matches method if it matches ignoring NBT,
        // to ensure that we return the proper value for if there is a match that gives a proper output
        return super.matches(input, world) && !assemble(input).isEmpty();//TODO - 26.1: can this ever be empty?
    }
}