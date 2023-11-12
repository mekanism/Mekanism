package mekanism.common.recipe.impl;

import mekanism.api.MekanismAPI;
import mekanism.api.annotations.NothingNullByDefault;
import mekanism.api.recipes.MekanismRecipeTypes;
import mekanism.api.recipes.basic.BasicItemStackToItemStackRecipe;
import mekanism.api.recipes.basic.IBasicItemStackOutput;
import mekanism.api.recipes.ingredients.ItemStackIngredient;
import mekanism.common.registries.MekanismRecipeSerializers;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.neoforged.neoforge.registries.ForgeRegistries;
import net.neoforged.neoforge.registries.RegistryObject;

@NothingNullByDefault
public class BasicSmeltingRecipe extends BasicItemStackToItemStackRecipe implements IBasicItemStackOutput {

    private static final RegistryObject<Item> ENERGIZED_SMELTER = RegistryObject.create(new ResourceLocation(MekanismAPI.MEKANISM_MODID, "energized_smelter"), ForgeRegistries.ITEMS);

    public BasicSmeltingRecipe(ItemStackIngredient input, ItemStack output) {
        super(input, output, MekanismRecipeTypes.TYPE_SMELTING.get());
    }

    @Override
    public RecipeSerializer<BasicSmeltingRecipe> getSerializer() {
        return MekanismRecipeSerializers.SMELTING.get();
    }

    @Override
    public String getGroup() {
        return "energized_smelter";
    }

    @Override
    public ItemStack getToastSymbol() {
        return new ItemStack(ENERGIZED_SMELTER.get());
    }

}