package mekanism.client.jei;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.annotation.Nonnull;
import mekanism.api.providers.IBlockProvider;
import mekanism.api.recipes.GasToGasRecipe;
import mekanism.api.recipes.MekanismRecipe;
import mekanism.api.recipes.RotaryRecipe;
import mekanism.api.recipes.inputs.ItemStackIngredient;
import mekanism.api.recipes.inputs.chemical.GasStackIngredient;
import mekanism.common.Mekanism;
import mekanism.common.recipe.MekanismRecipeType;
import mekanism.common.recipe.impl.NutritionalLiquifierIRecipe;
import mekanism.common.registries.MekanismBlocks;
import mekanism.common.registries.MekanismGases;
import mezz.jei.api.registration.IRecipeRegistration;
import net.minecraft.client.Minecraft;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.item.Food;
import net.minecraft.item.Item;
import net.minecraft.item.crafting.IRecipeSerializer;
import net.minecraft.item.crafting.IRecipeType;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.registries.ForgeRegistries;

public class RecipeRegistryHelper {

    private RecipeRegistryHelper() {
    }

    public static void registerCondensentrator(IRecipeRegistration registry) {
        List<RotaryRecipe> condensentratorRecipes = new ArrayList<>();
        List<RotaryRecipe> decondensentratorRecipes = new ArrayList<>();
        List<RotaryRecipe> recipes = MekanismRecipeType.ROTARY.getRecipes(getWorld());
        for (RotaryRecipe recipe : recipes) {
            if (recipe.hasGasToFluid()) {
                condensentratorRecipes.add(recipe);
            }
            if (recipe.hasFluidToGas()) {
                decondensentratorRecipes.add(recipe);
            }
        }
        ResourceLocation condensentrating = Mekanism.rl("rotary_condensentrator_condensentrating");
        ResourceLocation decondensentrating = Mekanism.rl("rotary_condensentrator_decondensentrating");
        registry.addRecipes(condensentratorRecipes, condensentrating);
        registry.addRecipes(decondensentratorRecipes, decondensentrating);
    }

    public static <RECIPE extends MekanismRecipe> void register(IRecipeRegistration registry, IBlockProvider mekanismBlock, MekanismRecipeType<RECIPE> type) {
        register(registry, mekanismBlock.getRegistryName(), type);
    }

    public static <RECIPE extends MekanismRecipe> void register(IRecipeRegistration registry, ResourceLocation id, MekanismRecipeType<RECIPE> type) {
        registry.addRecipes(type.getRecipes(getWorld()), id);
    }

    public static void registerNutritionalLiquifier(IRecipeRegistration registry) {
        List<NutritionalLiquifierIRecipe> list = new ArrayList<>();
        for (Item item : ForgeRegistries.ITEMS.getValues()) {
            if (item.isFood()) {
                Food food = item.getFood();
                //Only display consuming foods that provide healing as otherwise no paste will be made
                if (food != null && food.getHealing() > 0) {
                    list.add(new NutritionalLiquifierIRecipe(item, ItemStackIngredient.from(item), MekanismGases.NUTRITIONAL_PASTE.getStack(food.getHealing() * 50L)));
                }
            }
        }
        registry.addRecipes(list, MekanismBlocks.NUTRITIONAL_LIQUIFIER.getRegistryName());
    }

    public static void registerSPS(IRecipeRegistration registry) {
        //TODO - V11: Make the SPS have a proper recipe type to allow for custom recipes
        // Note: While the serializer and type are nonnull, they aren't used anywhere by recipes that are only added to JEI
        GasToGasRecipe recipe = new GasToGasRecipe(Mekanism.rl("processing/polonium_to_antimatter"), GasStackIngredient.from(MekanismGases.POLONIUM, 1_000), MekanismGases.ANTIMATTER.getStack(1)) {
            @Nonnull
            @Override
            public IRecipeSerializer<?> getSerializer() {
                return null;
            }

            @Nonnull
            @Override
            public IRecipeType<?> getType() {
                return null;
            }
        };
        registry.addRecipes(Collections.singletonList(recipe), MekanismBlocks.SPS_CASING.getRegistryName());
    }

    private static ClientWorld getWorld() {
        return Minecraft.getInstance().world;
    }
}