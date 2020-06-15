package mekanism.client.jei;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
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
import net.minecraft.item.Item;
import net.minecraft.item.crafting.IRecipeSerializer;
import net.minecraft.item.crafting.IRecipeType;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.registries.ForgeRegistries;

public class RecipeRegistryHelper {

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
        ResourceLocation condensentrating = new ResourceLocation(Mekanism.MODID, "rotary_condensentrator_condensentrating");
        ResourceLocation decondensentrating = new ResourceLocation(Mekanism.MODID, "rotary_condensentrator_decondensentrating");
        registry.addRecipes(condensentratorRecipes, condensentrating);
        registry.addRecipes(decondensentratorRecipes, decondensentrating);
    }

    public static void registerSmelter(IRecipeRegistration registry) {
        IBlockProvider mekanismBlock = MekanismBlocks.ENERGIZED_SMELTER;
        //TODO: Re-evaluate all of this once we add back CraftTweaker integration
        // Note: There is a possibility this currently is not adding any smelting recipes of ours that are not in the vanilla furnace as well
        //registry.addRecipes(Collections.singleton(SmeltingRecipe.class), mekanismBlock.getJEICategory());
        /*if (Mekanism.hooks.CraftTweakerLoaded && EnergizedSmelter.hasRemovedRecipe()) {// Removed / Removed + Added
            registry.addRecipes(MekanismRecipeType.SMELTING.getRecipes(getWorld()), mekanismBlock.getRegistryName());
        } else if (Mekanism.hooks.CraftTweakerLoaded && EnergizedSmelter.hasAddedRecipe()) {// Added but not removed
            // Only add added recipes
            /*Map<ItemStackInput, SmeltingRecipe> smeltingRecipes = Recipe.ENERGIZED_SMELTER.get();
            List<MachineRecipeWrapper> smeltingWrapper = new ArrayList<>();
            for (Entry<ItemStackInput, SmeltingRecipe> entry : smeltingRecipes.entrySet()) {
                if (!FurnaceRecipes.instance().getSmeltingList().containsKey(entry.getKey().ingredient)) {
                    smeltingWrapper.add(new MachineRecipeWrapper<>(entry.getValue()));
                }
            }
            registry.addRecipes(smeltingWrapper, mekanismBlock.getJEICategory());*/
        //}
    }

    public static <RECIPE extends MekanismRecipe> void register(IRecipeRegistration registry, IBlockProvider mekanismBlock, MekanismRecipeType<RECIPE> type) {
        registry.addRecipes(type.getRecipes(getWorld()), mekanismBlock.getRegistryName());
    }

    public static void registerNutritionalLiquifier(IRecipeRegistration registry) {
        registry.addRecipes(ForgeRegistries.ITEMS.getValues().stream().filter(Item::isFood)
              .map(item -> new NutritionalLiquifierIRecipe(Mekanism.rl("liquifier/" + item.getRegistryName()), ItemStackIngredient.from(item), MekanismGases.NUTRITIONAL_PASTE.getStack(item.getFood().getHealing() * 50)))
              .collect(Collectors.toList()), MekanismBlocks.NUTRITIONAL_LIQUIFIER.getRegistryName());
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