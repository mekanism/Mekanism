package mekanism.api.recipes.basic;

import mekanism.api.MekanismAPI;
import mekanism.api.recipes.MekanismRecipeSerializers;
import mekanism.api.recipes.MekanismRecipeTypes;
import mekanism.api.recipes.ingredients.ChemicalStackIngredient;
import mekanism.api.recipes.ingredients.ItemStackIngredient;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.neoforged.neoforge.registries.DeferredHolder;

public class BasicCompressingRecipe extends BasicItemStackChemicalToItemStackRecipe {

    private static final Holder<Item> OSMIUM_COMPRESSOR = DeferredHolder.create(Registries.ITEM, Identifier.fromNamespaceAndPath(MekanismAPI.MEKANISM_MODID, "osmium_compressor"));

    public BasicCompressingRecipe(ItemStackIngredient itemInput, ChemicalStackIngredient chemicalInput, ItemStackTemplate output, boolean perTickUsage) {
        super(itemInput, chemicalInput, output, perTickUsage, MekanismRecipeTypes.TYPE_COMPRESSING.value());
    }

    @Override
    public RecipeSerializer<BasicCompressingRecipe> getSerializer() {
        return MekanismRecipeSerializers.COMPRESSING.value();
    }

    @Override
    public ItemStack getToastSymbol() {
        return new ItemStack(OSMIUM_COMPRESSOR);
    }
}