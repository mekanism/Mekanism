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
import net.minecraft.block.Blocks;
import net.minecraft.data.IFinishedRecipe;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.IItemProvider;
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
              ItemStackIngredient.from(Blocks.DIRT),
              GasStackIngredient.from(MekanismTags.Gases.WATER_VAPOR, 1),
              new ItemStack(Blocks.CLAY)
        ).build(consumer, Mekanism.rl(basePath + "dirt_to_clay"));
        //Gunpowder -> sulfur
        ItemStackGasToItemStackRecipeBuilder.injecting(
              ItemStackIngredient.from(Tags.Items.GUNPOWDER),
              GasStackIngredient.from(MekanismGases.HYDROGEN_CHLORIDE, 1),
              MekanismItems.SULFUR_DUST.getItemStack()
        ).build(consumer, Mekanism.rl(basePath + "gunpowder_to_sulfur"));
        //Terracotta -> clay
        ItemStackGasToItemStackRecipeBuilder.injecting(
              ItemStackIngredient.from(Blocks.TERRACOTTA),
              GasStackIngredient.from(MekanismTags.Gases.WATER_VAPOR, 1),
              new ItemStack(Blocks.CLAY)
        ).build(consumer, Mekanism.rl(basePath + "terracotta_to_clay"));
        addChemicalInjectorConcreteRecipes(consumer, basePath + "concrete/");
    }

    private void addChemicalInjectorConcreteRecipes(Consumer<IFinishedRecipe> consumer, String basePath) {
        addChemicalInjectorConcreteRecipes(consumer, basePath, Blocks.BLACK_CONCRETE_POWDER, Blocks.BLACK_CONCRETE, "black");
        addChemicalInjectorConcreteRecipes(consumer, basePath, Blocks.BLUE_CONCRETE_POWDER, Blocks.BLUE_CONCRETE, "blue");
        addChemicalInjectorConcreteRecipes(consumer, basePath, Blocks.BROWN_CONCRETE_POWDER, Blocks.BROWN_CONCRETE, "brown");
        addChemicalInjectorConcreteRecipes(consumer, basePath, Blocks.CYAN_CONCRETE_POWDER, Blocks.CYAN_CONCRETE, "cyan");
        addChemicalInjectorConcreteRecipes(consumer, basePath, Blocks.GRAY_CONCRETE_POWDER, Blocks.GRAY_CONCRETE, "gray");
        addChemicalInjectorConcreteRecipes(consumer, basePath, Blocks.GREEN_CONCRETE_POWDER, Blocks.GREEN_CONCRETE, "green");
        addChemicalInjectorConcreteRecipes(consumer, basePath, Blocks.LIGHT_BLUE_CONCRETE_POWDER, Blocks.LIGHT_BLUE_CONCRETE, "light_blue");
        addChemicalInjectorConcreteRecipes(consumer, basePath, Blocks.LIGHT_GRAY_CONCRETE_POWDER, Blocks.LIGHT_GRAY_CONCRETE, "light_gray");
        addChemicalInjectorConcreteRecipes(consumer, basePath, Blocks.LIME_CONCRETE_POWDER, Blocks.LIME_CONCRETE, "lime");
        addChemicalInjectorConcreteRecipes(consumer, basePath, Blocks.MAGENTA_CONCRETE_POWDER, Blocks.MAGENTA_CONCRETE, "magenta");
        addChemicalInjectorConcreteRecipes(consumer, basePath, Blocks.ORANGE_CONCRETE_POWDER, Blocks.ORANGE_CONCRETE, "orange");
        addChemicalInjectorConcreteRecipes(consumer, basePath, Blocks.PINK_CONCRETE_POWDER, Blocks.PINK_CONCRETE, "pink");
        addChemicalInjectorConcreteRecipes(consumer, basePath, Blocks.PURPLE_CONCRETE_POWDER, Blocks.PURPLE_CONCRETE, "purple");
        addChemicalInjectorConcreteRecipes(consumer, basePath, Blocks.RED_CONCRETE_POWDER, Blocks.RED_CONCRETE, "red");
        addChemicalInjectorConcreteRecipes(consumer, basePath, Blocks.WHITE_CONCRETE_POWDER, Blocks.WHITE_CONCRETE, "white");
        addChemicalInjectorConcreteRecipes(consumer, basePath, Blocks.YELLOW_CONCRETE_POWDER, Blocks.YELLOW_CONCRETE, "yellow");
    }

    private void addChemicalInjectorConcreteRecipes(Consumer<IFinishedRecipe> consumer, String basePath, IItemProvider powder, IItemProvider concrete, String name) {
        ItemStackGasToItemStackRecipeBuilder.injecting(
              ItemStackIngredient.from(powder),
              GasStackIngredient.from(MekanismTags.Gases.WATER_VAPOR, 1),
              new ItemStack(concrete)
        ).build(consumer, Mekanism.rl(basePath + name));
    }
}