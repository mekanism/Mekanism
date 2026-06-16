package mekanism.common.recipe.impl;

import java.util.Map;
import mekanism.api.chemical.Chemical;
import mekanism.api.datagen.recipe.builder.ItemStackChemicalToItemStackRecipeBuilder;
import mekanism.api.recipes.ingredients.ChemicalStackIngredient;
import mekanism.api.recipes.ingredients.creator.IngredientCreatorAccess;
import mekanism.common.Mekanism;
import mekanism.common.registries.MekanismChemicals;
import mekanism.common.registries.MekanismItems;
import mekanism.common.tags.MekanismTags;
import mekanism.common.util.RegistryUtils;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.references.BlockItemId;
import net.minecraft.references.BlockItemIds;
import net.minecraft.references.ItemIds;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.ColorCollection;
import net.minecraft.world.level.block.WeatheringCopper;
import net.minecraft.world.level.material.Fluid;
import net.neoforged.neoforge.common.Tags;

class ChemicalInjectorRecipeProvider extends BaseSubRecipeProvider {

    ChemicalInjectorRecipeProvider(HolderGetter<Item> items, HolderGetter<Fluid> fluids, HolderGetter<Chemical> chemicals) {
        super(items, fluids, chemicals);
    }

    @Override
    public void addRecipes(RecipeOutput consumer, HolderLookup.Provider registries) {
        String basePath = "injecting/";
        //Brick -> clay ball
        ItemStackChemicalToItemStackRecipeBuilder.injecting(
              IngredientCreatorAccess.item().from(this.items, Tags.Items.BRICKS_NORMAL),
              IngredientCreatorAccess.chemicalStack().from(this.chemicals, MekanismTags.Chemicals.WATER_VAPOR, 1),
              template(ItemIds.CLAY_BALL),
              true
        ).save(consumer, Mekanism.rl(basePath + "brick_to_clay_ball"));
        //Dirt -> mud
        ItemStackChemicalToItemStackRecipeBuilder.injecting(
              IngredientCreatorAccess.item().from(this.items, BlockItemIds.DIRT),
              IngredientCreatorAccess.chemicalStack().from(this.chemicals, MekanismTags.Chemicals.WATER_VAPOR, 1),
              template(BlockItemIds.MUD),
              true
        ).save(consumer, Mekanism.rl(basePath + "dirt_to_mud"));
        //Gunpowder -> sulfur
        ItemStackChemicalToItemStackRecipeBuilder.injecting(
              IngredientCreatorAccess.item().from(this.items, Tags.Items.GUNPOWDERS),
              IngredientCreatorAccess.chemicalStack().fromHolder(MekanismChemicals.HYDROGEN_CHLORIDE, 1),
              MekanismItems.SULFUR_DUST.asTemplate(),
              true
        ).save(consumer, Mekanism.rl(basePath + "gunpowder_to_sulfur"));
        //Terracotta -> clay
        ItemStackChemicalToItemStackRecipeBuilder.injecting(
              IngredientCreatorAccess.item().from(this.items, BlockItemIds.TERRACOTTA),
              IngredientCreatorAccess.chemicalStack().from(this.chemicals, MekanismTags.Chemicals.WATER_VAPOR, 1),
              template(BlockItemIds.CLAY),
              true
        ).save(consumer, Mekanism.rl(basePath + "terracotta_to_clay"));
        addChemicalInjectorConcreteRecipes(consumer, basePath + "concrete/");
        addChemicalInjectorCoralRevivalRecipes(consumer, basePath + "coral/");
        addChemicalInjectorOxidizingRecipe(consumer, basePath + "oxidizing/");
    }

    private void addChemicalInjectorConcreteRecipes(RecipeOutput consumer, String basePath) {
        ColorCollection.VALUES.forEach(color -> ItemStackChemicalToItemStackRecipeBuilder.injecting(
              IngredientCreatorAccess.item().from(this.items, BlockItemIds.CONCRETE_POWDER.pick(color)),
              IngredientCreatorAccess.chemicalStack().from(this.chemicals, MekanismTags.Chemicals.WATER_VAPOR, 1),
              template(BlockItemIds.CONCRETE.pick(color)),
              true
        ).save(consumer, Mekanism.rl(basePath + color)));
    }

    private void addChemicalInjectorCoralRevivalRecipes(RecipeOutput consumer, String basePath) {
        addChemicalInjectorCoralRevivalRecipe(consumer, basePath, BlockItemIds.DEAD_BRAIN_CORAL_BLOCK, BlockItemIds.BRAIN_CORAL_BLOCK, 5);
        addChemicalInjectorCoralRevivalRecipe(consumer, basePath, BlockItemIds.DEAD_BUBBLE_CORAL_BLOCK, BlockItemIds.BUBBLE_CORAL_BLOCK, 5);
        addChemicalInjectorCoralRevivalRecipe(consumer, basePath, BlockItemIds.DEAD_FIRE_CORAL_BLOCK, BlockItemIds.FIRE_CORAL_BLOCK, 5);
        addChemicalInjectorCoralRevivalRecipe(consumer, basePath, BlockItemIds.DEAD_HORN_CORAL_BLOCK, BlockItemIds.HORN_CORAL_BLOCK, 5);
        addChemicalInjectorCoralRevivalRecipe(consumer, basePath, BlockItemIds.DEAD_TUBE_CORAL_BLOCK, BlockItemIds.TUBE_CORAL_BLOCK, 5);
        addChemicalInjectorCoralRevivalRecipe(consumer, basePath, BlockItemIds.DEAD_BRAIN_CORAL, BlockItemIds.BRAIN_CORAL, 3);
        addChemicalInjectorCoralRevivalRecipe(consumer, basePath, BlockItemIds.DEAD_BUBBLE_CORAL, BlockItemIds.BUBBLE_CORAL, 3);
        addChemicalInjectorCoralRevivalRecipe(consumer, basePath, BlockItemIds.DEAD_FIRE_CORAL, BlockItemIds.FIRE_CORAL, 3);
        addChemicalInjectorCoralRevivalRecipe(consumer, basePath, BlockItemIds.DEAD_HORN_CORAL, BlockItemIds.HORN_CORAL, 3);
        addChemicalInjectorCoralRevivalRecipe(consumer, basePath, BlockItemIds.DEAD_TUBE_CORAL, BlockItemIds.TUBE_CORAL, 3);
        addChemicalInjectorCoralRevivalRecipe(consumer, basePath, BlockItemIds.DEAD_BRAIN_CORAL_FAN, BlockItemIds.BRAIN_CORAL_FAN, 3);
        addChemicalInjectorCoralRevivalRecipe(consumer, basePath, BlockItemIds.DEAD_BUBBLE_CORAL_FAN, BlockItemIds.BUBBLE_CORAL_FAN, 3);
        addChemicalInjectorCoralRevivalRecipe(consumer, basePath, BlockItemIds.DEAD_FIRE_CORAL_FAN, BlockItemIds.FIRE_CORAL_FAN, 3);
        addChemicalInjectorCoralRevivalRecipe(consumer, basePath, BlockItemIds.DEAD_HORN_CORAL_FAN, BlockItemIds.HORN_CORAL_FAN, 3);
        addChemicalInjectorCoralRevivalRecipe(consumer, basePath, BlockItemIds.DEAD_TUBE_CORAL_FAN, BlockItemIds.TUBE_CORAL_FAN, 3);
    }

    private void addChemicalInjectorCoralRevivalRecipe(RecipeOutput consumer, String basePath, BlockItemId dead, BlockItemId living, int water) {
        ItemStackChemicalToItemStackRecipeBuilder.injecting(
              IngredientCreatorAccess.item().from(this.items, dead),
              IngredientCreatorAccess.chemicalStack().from(this.chemicals, MekanismTags.Chemicals.WATER_VAPOR, water),
              template(living),
              true
        ).save(consumer, Mekanism.rl(basePath + living.item().identifier().getPath()));
    }

    private void addChemicalInjectorOxidizingRecipe(RecipeOutput consumer, String basePath) {
        //Generate baseline recipes from weathering recipe set
        ChemicalStackIngredient oxygen = IngredientCreatorAccess.chemicalStack().fromHolder(MekanismChemicals.OXYGEN, 1);
        //TODO - 26.2: Switch this to being created at runtime and making use of the Neo DataMap?
        // https://github.com/neoforged/NeoForge/commit/87875183dcd8239404cbddbe8717db1dbe4f64ee
        // Likely will require a PR based on https://github.com/neoforged/NeoForge/pull/1915 to move data maps before registries?
        for (Map.Entry<Block, Block> entry : WeatheringCopper.NEXT_BY_BLOCK.get().entrySet()) {
            Block result = entry.getValue();
            ItemStackChemicalToItemStackRecipeBuilder.injecting(
                  IngredientCreatorAccess.item().from(entry.getKey()),
                  oxygen,
                  new ItemStackTemplate(result.asItem()),
                  true
            ).save(consumer, Mekanism.rl(basePath + RegistryUtils.getPath(result)));
        }
    }
}