package mekanism.client.jei;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import mekanism.api.providers.IBlockProvider;
import mekanism.api.recipes.MekanismRecipe;
import mekanism.api.recipes.RotaryRecipe;
import mekanism.api.recipes.inputs.ItemStackIngredient;
import mekanism.common.Mekanism;
import mekanism.common.recipe.MekanismRecipeType;
import mekanism.common.recipe.impl.NutritionalLiquifierIRecipe;
import mekanism.common.registries.MekanismBlocks;
import mekanism.common.registries.MekanismGases;
import mezz.jei.api.registration.IRecipeRegistration;
import net.minecraft.client.Minecraft;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.item.Item;
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
        //TODO - V10: FIXME don't use a null recipe id
        registry.addRecipes(ForgeRegistries.ITEMS.getValues().stream().filter(Item::isFood)
              .map(item -> new NutritionalLiquifierIRecipe(null, ItemStackIngredient.from(item), MekanismGases.NUTRITIONAL_PASTE.getGasStack(item.getFood().getHealing() * 50)))
              .collect(Collectors.toList()), MekanismBlocks.NUTRITIONAL_LIQUIFIER.getRegistryName());
    }

    private static ClientWorld getWorld() {
        return Minecraft.getInstance().world;
    }
}