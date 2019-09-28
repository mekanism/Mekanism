package mekanism.common.recipe.impl;

import javax.annotation.Nonnull;
import mekanism.api.recipes.CombinerRecipe;
import mekanism.api.recipes.inputs.ItemStackIngredient;
import mekanism.common.MekanismBlock;
import mekanism.common.recipe.MekanismRecipeSerializers;
import mekanism.common.recipe.MekanismRecipeType;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.item.crafting.IRecipeSerializer;
import net.minecraft.item.crafting.IRecipeType;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;

public class CombinerIRecipe extends CombinerRecipe implements IRecipe<IInventory> {

    private final ResourceLocation id;

    public CombinerIRecipe(ResourceLocation id, ItemStackIngredient mainInput, ItemStackIngredient extraInput, ItemStack output) {
        super(mainInput, extraInput, output);
        this.id = id;
    }

    @Override
    public boolean matches(@Nonnull IInventory inv, @Nonnull World world) {
        //TODO: Check if this matches properly, maybe require some IInventory that gives more information about slots
        return true;
    }

    @Override
    public boolean isDynamic() {
        //TODO: If we make this non dynamic, we can make it show in vanilla's crafting book
        // and also then obey the recipe locking.
        // For now none of that works/makes sense in our concept so don't lock it
        return true;
    }

    @Nonnull
    @Override
    public ItemStack getCraftingResult(@Nonnull IInventory inv) {
        //TODO
        return ItemStack.EMPTY;
    }

    @Override
    public boolean canFit(int width, int height) {
        //TODO: This is always true for cooking recipes, so I assume we probably also want it true
        return true;
    }

    @Nonnull
    @Override
    public ItemStack getRecipeOutput() {
        //TODO
        return ItemStack.EMPTY;
    }

    @Nonnull
    @Override
    public ResourceLocation getId() {
        return id;
    }

    @Nonnull
    @Override
    public IRecipeType<CombinerIRecipe> getType() {
        return MekanismRecipeType.COMBINER;
    }

    @Nonnull
    @Override
    public IRecipeSerializer<CombinerIRecipe> getSerializer() {
        return MekanismRecipeSerializers.COMBINER;
    }

    @Nonnull
    @Override
    public String getGroup() {
        return MekanismBlock.COMBINER.getName();
    }

    @Nonnull
    @Override
    public ItemStack getIcon() {
        return MekanismBlock.COMBINER.getItemStack();
    }
}