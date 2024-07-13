package mekanism.common.recipe;

import mekanism.api.annotations.NothingNullByDefault;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.ShapedRecipe;
import net.minecraft.world.level.Level;

@NothingNullByDefault
public abstract class WrappedShapedRecipe extends ShapedRecipe {

    private final ShapedRecipe internal;

    protected WrappedShapedRecipe(ShapedRecipe internal) {
        //Note: We override all uses and calls to pattern and result, so we can just pass null and empty to them
        // Because pattern is AT'd to public however, we make use of it to pass it to super, in case another mod is querying the value
        super(internal.getGroup(), internal.category(), internal.pattern, ItemStack.EMPTY, internal.showNotification());
        this.internal = internal;
    }

    public ShapedRecipe getInternal() {
        return internal;
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