package mekanism.client.jei;

import com.google.common.collect.ImmutableList;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.function.Function;
import mekanism.api.providers.IBlockProvider;
import mekanism.api.providers.IItemProvider;
import mekanism.api.recipes.MekanismRecipe;
import mekanism.api.recipes.RotaryRecipe;
import mekanism.api.recipes.inputs.ItemStackIngredient;
import mekanism.common.Mekanism;
import mekanism.common.recipe.MekanismRecipeType;
import mekanism.common.recipe.impl.NutritionalLiquifierIRecipe;
import mekanism.common.registries.MekanismBlocks;
import mekanism.common.registries.MekanismGases;
import mezz.jei.api.constants.VanillaRecipeCategoryUid;
import mezz.jei.api.recipe.vanilla.IVanillaRecipeFactory;
import mezz.jei.api.registration.IRecipeRegistration;
import net.minecraft.client.Minecraft;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.item.Food;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
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

    public static <RECIPE extends MekanismRecipe> void register(IRecipeRegistration registry, IBlockProvider mekanismBlock, MekanismRecipeType<RECIPE, ?> type) {
        register(registry, mekanismBlock.getRegistryName(), type);
    }

    public static <RECIPE extends MekanismRecipe> void register(IRecipeRegistration registry, ResourceLocation id, MekanismRecipeType<RECIPE, ?> type) {
        registry.addRecipes(type.getRecipes(getWorld()), id);
    }

    public static void registerNutritionalLiquifier(IRecipeRegistration registry) {
        List<NutritionalLiquifierIRecipe> list = new ArrayList<>();
        for (Item item : ForgeRegistries.ITEMS.getValues()) {
            if (item.isEdible()) {
                Food food = item.getFoodProperties();
                //Only display consuming foods that provide healing as otherwise no paste will be made
                if (food != null && food.getNutrition() > 0) {
                    list.add(new NutritionalLiquifierIRecipe(item, ItemStackIngredient.from(item), MekanismGases.NUTRITIONAL_PASTE.getStack(food.getNutrition() * 50L)));
                }
            }
        }
        registry.addRecipes(list, MekanismBlocks.NUTRITIONAL_LIQUIFIER.getRegistryName());
    }

    public static void addAnvilRecipes(IRecipeRegistration registry, IItemProvider item, Function<Item, ItemStack[]> repairMaterials) {
        IVanillaRecipeFactory factory = registry.getVanillaRecipeFactory();
        //Based off of how JEI adds for Vanilla items
        ItemStack damaged2 = item.getItemStack();
        damaged2.setDamageValue(damaged2.getMaxDamage() * 3 / 4);
        ItemStack damaged3 = item.getItemStack();
        damaged3.setDamageValue(damaged3.getMaxDamage() * 2 / 4);
        //Two damaged items combine to undamaged
        registry.addRecipes(ImmutableList.of(factory.createAnvilRecipe(damaged2, Collections.singletonList(damaged2), Collections.singletonList(damaged3))),
              VanillaRecipeCategoryUid.ANVIL);
        ItemStack[] repairStacks = repairMaterials.apply(item.getItem());
        //Damaged item + the repair material
        if (repairStacks != null && repairStacks.length > 0) {
            //While this is damaged1 it is down here as we don't need to bother creating the reference if we don't have a repair material
            ItemStack damaged1 = item.getItemStack();
            damaged1.setDamageValue(damaged1.getMaxDamage());
            registry.addRecipes(ImmutableList.of(factory.createAnvilRecipe(damaged1, Arrays.asList(repairStacks), Collections.singletonList(damaged2))),
                  VanillaRecipeCategoryUid.ANVIL);
        }
    }

    private static ClientWorld getWorld() {
        return Minecraft.getInstance().level;
    }
}