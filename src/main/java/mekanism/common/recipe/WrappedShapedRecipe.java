package mekanism.common.recipe;

import mekanism.api.annotations.NothingNullByDefault;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.ShapedRecipe;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.common.crafting.IShapedRecipe;

@NothingNullByDefault
public abstract class WrappedShapedRecipe implements CraftingRecipe, IShapedRecipe<CraftingInput> {

    private final ShapedRecipe internal;

    protected WrappedShapedRecipe(ShapedRecipe internal) {
        this.internal = internal;
    }

    public ShapedRecipe getInternal() {
        return internal;
    }

    @Override
    public CraftingBookCategory category() {
        return internal.category();
    }

    @Override
    public abstract ItemStack assemble(CraftingInput input, HolderLookup.Provider provider);

    @Override
    public boolean matches(CraftingInput input, Level world) {
        //Note: We do not override the matches method if it matches ignoring NBT,
        // to ensure that we return the proper value for if there is a match that gives a proper output
        return internal.matches(input, world) && !assemble(input, world.registryAccess()).isEmpty();
    }

    @Override
    public boolean canCraftInDimensions(int width, int height) {
        return internal.canCraftInDimensions(width, height);
    }

    @Override
    public ItemStack getResultItem(HolderLookup.Provider provider) {
        return internal.getResultItem(provider);
    }

    @Override
    public NonNullList<ItemStack> getRemainingItems(CraftingInput input) {
        return internal.getRemainingItems(input);
    }

    @Override
    public NonNullList<Ingredient> getIngredients() {
        return internal.getIngredients();
    }

    @Override
    public boolean isSpecial() {
        return internal.isSpecial();
    }

    @Override
    public String getGroup() {
        return internal.getGroup();
    }

    @Override
    public ItemStack getToastSymbol() {
        return internal.getToastSymbol();
    }

    @Override
    public int getWidth() {
        return internal.getWidth();
    }

    @Override
    public int getHeight() {
        return internal.getHeight();
    }

    @Override
    public boolean isIncomplete() {
        return internal.isIncomplete();
    }
}