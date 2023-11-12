package mekanism.common.recipe.impl;

import mekanism.api.MekanismAPI;
import mekanism.api.annotations.NothingNullByDefault;
import mekanism.api.chemical.gas.GasStack;
import mekanism.api.recipes.MekanismRecipeTypes;
import mekanism.api.recipes.basic.BasicItemStackToGasRecipe;
import mekanism.api.recipes.ingredients.ItemStackIngredient;
import mekanism.common.registries.MekanismRecipeSerializers;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.neoforged.neoforge.registries.ForgeRegistries;
import net.neoforged.neoforge.registries.RegistryObject;

@NothingNullByDefault
public class BasicChemicalOxidizerRecipe extends BasicItemStackToGasRecipe {

    private static final RegistryObject<Item> CHEMICAL_OXIDIZER = RegistryObject.create(new ResourceLocation(MekanismAPI.MEKANISM_MODID, "chemical_oxidizer"), ForgeRegistries.ITEMS);

    public BasicChemicalOxidizerRecipe(ItemStackIngredient input, GasStack output) {
        super(input, output, MekanismRecipeTypes.TYPE_OXIDIZING.get());
    }

    @Override
    public RecipeSerializer<BasicChemicalOxidizerRecipe> getSerializer() {
        return MekanismRecipeSerializers.OXIDIZING.get();
    }

    @Override
    public String getGroup() {
        return "chemical_oxidizer";
    }

    @Override
    public ItemStack getToastSymbol() {
        return new ItemStack(CHEMICAL_OXIDIZER.get());
    }

}