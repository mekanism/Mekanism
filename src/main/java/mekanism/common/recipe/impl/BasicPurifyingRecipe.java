package mekanism.common.recipe.impl;

import mekanism.api.MekanismAPI;
import mekanism.api.annotations.NothingNullByDefault;
import mekanism.api.recipes.MekanismRecipeTypes;
import mekanism.api.recipes.basic.BasicItemStackGasToItemStackRecipe;
import mekanism.api.recipes.basic.IBasicItemStackOutput;
import mekanism.api.recipes.ingredients.ChemicalStackIngredient.GasStackIngredient;
import mekanism.api.recipes.ingredients.ItemStackIngredient;
import mekanism.common.registries.MekanismRecipeSerializers;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.neoforged.neoforge.registries.ForgeRegistries;
import net.neoforged.neoforge.registries.RegistryObject;

@NothingNullByDefault
public class BasicPurifyingRecipe extends BasicItemStackGasToItemStackRecipe implements IBasicItemStackOutput {

    private static final RegistryObject<Item> PURIFICATION_CHAMBER = RegistryObject.create(new ResourceLocation(MekanismAPI.MEKANISM_MODID, "purification_chamber"), ForgeRegistries.ITEMS);

    public BasicPurifyingRecipe(ItemStackIngredient itemInput, GasStackIngredient gasInput, ItemStack output) {
        super(itemInput, gasInput, output, MekanismRecipeTypes.TYPE_PURIFYING.get());
    }

    @Override
    public RecipeSerializer<BasicPurifyingRecipe> getSerializer() {
        return MekanismRecipeSerializers.PURIFYING.get();
    }

    @Override
    public String getGroup() {
        return "purification_chamber";
    }

    @Override
    public ItemStack getToastSymbol() {
        return new ItemStack(PURIFICATION_CHAMBER.get());
    }

}