package mekanism.common.recipe;

import mekanism.api.annotations.NothingNullByDefault;
import net.minecraft.core.HolderLookup;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.ShapedRecipe;
import net.minecraft.world.item.crafting.ShapedRecipePattern;
import net.minecraft.world.level.Level;

@NothingNullByDefault
public abstract class WrappedShapedRecipe extends ShapedRecipe implements CraftingRecipe {

    protected WrappedShapedRecipe(String group, CraftingBookCategory category, ShapedRecipePattern pattern, ItemStack result, boolean showNotification) {
        super(group, category, pattern, result, showNotification);
    }

    @Override
    public abstract ItemStack assemble(CraftingInput input, HolderLookup.Provider provider);

    @Override
    public boolean matches(CraftingInput input, Level world) {
        //Note: We do not override the matches method if it matches ignoring NBT,
        // to ensure that we return the proper value for if there is a match that gives a proper output
        return super.matches(input, world) && !assemble(input, world.registryAccess()).isEmpty();
    }

    public ShapedRecipePattern getPattern() {
        return this.pattern;
    }
}