package mekanism.common.recipe;

import mekanism.api.annotations.NothingNullByDefault;
import net.minecraft.core.NonNullList;
import net.minecraft.core.RegistryAccess;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.ShapedRecipe;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.common.crafting.IShapedRecipe;

@NothingNullByDefault
public abstract class WrappedShapedRecipe implements CraftingRecipe, IShapedRecipe<CraftingContainer> {

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
    public abstract ItemStack assemble(CraftingContainer inv, RegistryAccess registryAccess);

    @Override
    public boolean matches(CraftingContainer inv, Level world) {
        //Note: We do not override the matches method if it matches ignoring NBT,
        // to ensure that we return the proper value for if there is a match that gives a proper output
        return internal.matches(inv, world) && !assemble(inv, world.registryAccess()).isEmpty();
    }

    @Override
    public boolean canCraftInDimensions(int width, int height) {
        return internal.canCraftInDimensions(width, height);
    }

    @Override
    public ItemStack getResultItem(RegistryAccess registryAccess) {
        return internal.getResultItem(registryAccess);
    }

    @Override
    public NonNullList<ItemStack> getRemainingItems(CraftingContainer inv) {
        return internal.getRemainingItems(inv);
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
    public int getRecipeWidth() {
        return internal.getRecipeWidth();
    }

    @Override
    public int getRecipeHeight() {
        return internal.getRecipeHeight();
    }

    @Override
    public boolean isIncomplete() {
        return internal.isIncomplete();
    }
}