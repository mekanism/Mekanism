package mekanism.common.recipe.impl;

import java.util.function.Consumer;
import mekanism.api.datagen.recipe.builder.ItemStackGasToItemStackRecipeBuilder;
import mekanism.api.recipes.inputs.ItemStackIngredient;
import mekanism.api.recipes.inputs.chemical.GasStackIngredient;
import mekanism.common.Mekanism;
import mekanism.common.recipe.ISubRecipeProvider;
import mekanism.common.registries.MekanismGases;
import mekanism.common.registries.MekanismItems;
import mekanism.common.tags.MekanismTags;
import net.minecraft.data.IFinishedRecipe;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraftforge.common.Tags;

class ChemicalInjectorRecipeProvider implements ISubRecipeProvider {

    @Override
    public void addRecipes(Consumer<IFinishedRecipe> consumer) {
        String basePath = "injecting/";
        //Brick -> clay ball
        ItemStackGasToItemStackRecipeBuilder.injecting(
              ItemStackIngredient.from(Tags.Items.INGOTS_BRICK),
              GasStackIngredient.from(MekanismTags.Gases.WATER_VAPOR, 1),
              new ItemStack(Items.CLAY_BALL)
        ).build(consumer, Mekanism.rl(basePath + "brick_to_clay_ball"));
        //Dirt -> clay
        ItemStackGasToItemStackRecipeBuilder.injecting(
              ItemStackIngredient.from(Items.DIRT),
              GasStackIngredient.from(MekanismTags.Gases.WATER_VAPOR, 1),
              new ItemStack(Items.CLAY)
        ).build(consumer, Mekanism.rl(basePath + "dirt_to_clay"));
        //Gunpowder -> sulfur
        ItemStackGasToItemStackRecipeBuilder.injecting(
              ItemStackIngredient.from(Tags.Items.GUNPOWDER),
              GasStackIngredient.from(MekanismGases.HYDROGEN_CHLORIDE, 1),
              MekanismItems.SULFUR_DUST.getItemStack()
        ).build(consumer, Mekanism.rl(basePath + "gunpowder_to_sulfur"));
        //Terracotta -> clay
        ItemStackGasToItemStackRecipeBuilder.injecting(
              ItemStackIngredient.from(Items.TERRACOTTA),
              GasStackIngredient.from(MekanismTags.Gases.WATER_VAPOR, 1),
              new ItemStack(Items.CLAY)
        ).build(consumer, Mekanism.rl(basePath + "terracotta_to_clay"));
        addChemicalInjectorConcreteRecipes(consumer, basePath + "concrete/");
    }

    private void addChemicalInjectorConcreteRecipes(Consumer<IFinishedRecipe> consumer, String basePath) {
        addChemicalInjectorConcreteRecipes(consumer, basePath, Items.BLACK_CONCRETE_POWDER, Items.BLACK_CONCRETE, "black");
        addChemicalInjectorConcreteRecipes(consumer, basePath, Items.BLUE_CONCRETE_POWDER, Items.BLUE_CONCRETE, "blue");
        addChemicalInjectorConcreteRecipes(consumer, basePath, Items.BROWN_CONCRETE_POWDER, Items.BROWN_CONCRETE, "brown");
        addChemicalInjectorConcreteRecipes(consumer, basePath, Items.CYAN_CONCRETE_POWDER, Items.CYAN_CONCRETE, "cyan");
        addChemicalInjectorConcreteRecipes(consumer, basePath, Items.GRAY_CONCRETE_POWDER, Items.GRAY_CONCRETE, "gray");
        addChemicalInjectorConcreteRecipes(consumer, basePath, Items.GREEN_CONCRETE_POWDER, Items.GREEN_CONCRETE, "green");
        addChemicalInjectorConcreteRecipes(consumer, basePath, Items.LIGHT_BLUE_CONCRETE_POWDER, Items.LIGHT_BLUE_CONCRETE, "light_blue");
        addChemicalInjectorConcreteRecipes(consumer, basePath, Items.LIGHT_GRAY_CONCRETE_POWDER, Items.LIGHT_GRAY_CONCRETE, "light_gray");
        addChemicalInjectorConcreteRecipes(consumer, basePath, Items.LIME_CONCRETE_POWDER, Items.LIME_CONCRETE, "lime");
        addChemicalInjectorConcreteRecipes(consumer, basePath, Items.MAGENTA_CONCRETE_POWDER, Items.MAGENTA_CONCRETE, "magenta");
        addChemicalInjectorConcreteRecipes(consumer, basePath, Items.ORANGE_CONCRETE_POWDER, Items.ORANGE_CONCRETE, "orange");
        addChemicalInjectorConcreteRecipes(consumer, basePath, Items.PINK_CONCRETE_POWDER, Items.PINK_CONCRETE, "pink");
        addChemicalInjectorConcreteRecipes(consumer, basePath, Items.PURPLE_CONCRETE_POWDER, Items.PURPLE_CONCRETE, "purple");
        addChemicalInjectorConcreteRecipes(consumer, basePath, Items.RED_CONCRETE_POWDER, Items.RED_CONCRETE, "red");
        addChemicalInjectorConcreteRecipes(consumer, basePath, Items.WHITE_CONCRETE_POWDER, Items.WHITE_CONCRETE, "white");
        addChemicalInjectorConcreteRecipes(consumer, basePath, Items.YELLOW_CONCRETE_POWDER, Items.YELLOW_CONCRETE, "yellow");
    }

    private void addChemicalInjectorConcreteRecipes(Consumer<IFinishedRecipe> consumer, String basePath, Item powder, Item concrete, String name) {
        ItemStackGasToItemStackRecipeBuilder.injecting(
              ItemStackIngredient.from(powder),
              GasStackIngredient.from(MekanismTags.Gases.WATER_VAPOR, 1),
              new ItemStack(concrete)
        ).build(consumer, Mekanism.rl(basePath + name));
    }
}