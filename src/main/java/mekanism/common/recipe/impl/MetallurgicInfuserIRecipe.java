package mekanism.common.recipe.impl;

import javax.annotation.Nonnull;
import mekanism.api.recipes.MetallurgicInfuserRecipe;
import mekanism.api.recipes.inputs.InfusionIngredient;
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

public class MetallurgicInfuserIRecipe extends MetallurgicInfuserRecipe implements IRecipe<IInventory> {

    private final ResourceLocation id;

    public MetallurgicInfuserIRecipe(ResourceLocation id, ItemStackIngredient itemInput, InfusionIngredient infusionInput, ItemStack output) {
        super(itemInput, infusionInput, output);
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
    public IRecipeType<MetallurgicInfuserIRecipe> getType() {
        return MekanismRecipeType.METALLURGIC_INFUSER;
    }

    @Nonnull
    @Override
    public IRecipeSerializer<MetallurgicInfuserIRecipe> getSerializer() {
        return MekanismRecipeSerializers.METALLURGIC_INFUSER;
    }

    @Nonnull
    @Override
    public String getGroup() {
        return MekanismBlock.METALLURGIC_INFUSER.getName();
    }

    @Nonnull
    @Override
    public ItemStack getIcon() {
        return MekanismBlock.METALLURGIC_INFUSER.getItemStack();
    }
}