package mekanism.common.recipe.impl;

import mekanism.api.MekanismAPI;
import mekanism.api.annotations.NothingNullByDefault;
import mekanism.api.recipes.ItemStackToFluidRecipe;
import mekanism.api.recipes.basic.BasicItemStackToFluidRecipe;
import mekanism.api.recipes.ingredients.ItemStackIngredient;
import mekanism.common.registries.MekanismBlocks;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.registries.ForgeRegistries;
import net.neoforged.neoforge.registries.RegistryObject;

@NothingNullByDefault
public class NutritionalLiquifierIRecipe extends BasicItemStackToFluidRecipe {

    private static final RegistryObject<Item> NUTRITIONAL_LIQUIFIER = RegistryObject.create(new ResourceLocation(MekanismAPI.MEKANISM_MODID, "nutritional_liquifier"), ForgeRegistries.ITEMS);

    public NutritionalLiquifierIRecipe(ItemStackIngredient input, FluidStack output) {
        super(input, output);
        //TODO - V11: Make the recipe system support a concept similar to vanilla's "special recipe". The backend already exists
        // but we don't currently have a way for it to get registered and added to the list. getType and getSerializer are nonnull,
        // but return a null value due to us not having a good way to handle this. It doesn't matter as they don't get synced across
        // the network and always exist, but we should improve this so that they do get properly implemented
    }

    @Override
    public RecipeType<ItemStackToFluidRecipe> getType() {
        return null;
    }

    @Override
    public RecipeSerializer<ItemStackToFluidRecipe> getSerializer() {
        return null;
    }

    @Override
    public String getGroup() {
        return "nutritional_liquifier";
    }

    @Override
    public ItemStack getToastSymbol() {
        return new ItemStack(NUTRITIONAL_LIQUIFIER.get());
    }
}