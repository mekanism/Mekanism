/*package mekanism.common.recipe.compat;

import java.util.function.Consumer;
import javax.annotation.ParametersAreNonnullByDefault;
import mekanism.api.datagen.recipe.builder.SawmillRecipeBuilder;
import mekanism.api.recipes.inputs.ItemStackIngredient;
import mekanism.common.Mekanism;
import mekanism.common.registries.MekanismItems;
import net.minecraft.data.IFinishedRecipe;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import yamahari.ilikewood.registry.WoodenItems;
import yamahari.ilikewood.util.Constants;
import yamahari.ilikewood.util.WoodType;
import yamahari.ilikewood.util.WoodenObjectType;

@ParametersAreNonnullByDefault
public class ILikeWoodRecipeProvider extends CompatRecipeProvider {

    public ILikeWoodRecipeProvider() {
        super(Constants.MOD_ID);
    }

    @Override
    protected void registerRecipes(Consumer<IFinishedRecipe> consumer, String basePath) {
        addWoodType(consumer, basePath, Items.ACACIA_PLANKS, Items.ACACIA_LOG, WoodType.ACACIA);
        addWoodType(consumer, basePath, Items.BIRCH_PLANKS, Items.BIRCH_LOG, WoodType.BIRCH);
        addWoodType(consumer, basePath, Items.DARK_OAK_PLANKS, Items.DARK_OAK_LOG, WoodType.DARK_OAK);
        addWoodType(consumer, basePath, Items.JUNGLE_PLANKS, Items.JUNGLE_LOG, WoodType.JUNGLE);
        addWoodType(consumer, basePath, Items.OAK_PLANKS, Items.OAK_LOG, WoodType.OAK);
        addWoodType(consumer, basePath, Items.SPRUCE_PLANKS, Items.SPRUCE_LOG, WoodType.SPRUCE);
    }

    //TODO: Maybe move some of these into RecipeProviderUtil, so that we make sure the numbers stay consistent
    private void addWoodType(Consumer<IFinishedRecipe> consumer, String basePath, Item planks, Item log, WoodType woodType) {
        String name = woodType.toString();
        Item stick = WoodenItems.getItem(WoodenObjectType.STICK, woodType);
        //Barrel
        SawmillRecipeBuilder.sawing(
              ItemStackIngredient.from(WoodenItems.getItem(WoodenObjectType.BARREL, woodType)),
              new ItemStack(planks, 7)
        ).addCondition(modLoaded)
              .build(consumer, Mekanism.rl(basePath + "barrel/" + name));
        //Bookshelf
        SawmillRecipeBuilder.sawing(
              ItemStackIngredient.from(WoodenItems.getItem(WoodenObjectType.BOOKSHELF, woodType)),
              new ItemStack(planks, 6),
              new ItemStack(Items.BOOK, 3),
              1
        ).addCondition(modLoaded)
              .build(consumer, Mekanism.rl(basePath + "bookshelf/" + name));
        //Chest
        SawmillRecipeBuilder.sawing(
              ItemStackIngredient.from(WoodenItems.getItem(WoodenObjectType.CHEST, woodType)),
              new ItemStack(planks, 8)
        ).addCondition(modLoaded)
              .build(consumer, Mekanism.rl(basePath + "chest/" + name));
        //Crafting table
        SawmillRecipeBuilder.sawing(
              ItemStackIngredient.from(WoodenItems.getItem(WoodenObjectType.CRAFTING_TABLE, woodType)),
              new ItemStack(planks, 4)
        ).addCondition(modLoaded)
              .build(consumer, Mekanism.rl(basePath + "crafting_table/" + name));
        //Ladder
        SawmillRecipeBuilder.sawing(
              ItemStackIngredient.from(WoodenItems.getItem(WoodenObjectType.LADDER, woodType), 3),
              new ItemStack(stick, 7)
        ).addCondition(modLoaded)
              .build(consumer, Mekanism.rl(basePath + "ladder/" + name));
        //Lectern
        SawmillRecipeBuilder.sawing(
              ItemStackIngredient.from(WoodenItems.getItem(WoodenObjectType.LECTERN, woodType)),
              new ItemStack(planks, 8),
              new ItemStack(Items.BOOK, 3),
              1
        ).addCondition(modLoaded)
              .build(consumer, Mekanism.rl(basePath + "lectern/" + name));
        //Panels
        SawmillRecipeBuilder.sawing(
              ItemStackIngredient.from(WoodenItems.getItem(WoodenObjectType.PANELS, woodType)),
              new ItemStack(stick, 6),
              MekanismItems.SAWDUST.getItemStack(),
              0.25
        ).addCondition(modLoaded)
              .build(consumer, Mekanism.rl(basePath + "panels/" + name));
        //Torch
        SawmillRecipeBuilder.sawing(
              ItemStackIngredient.from(WoodenItems.getItem(WoodenObjectType.TORCH, woodType), 4),
              new ItemStack(stick),
              new ItemStack(Items.COAL),
              1
        ).addCondition(modLoaded)
              .build(consumer, Mekanism.rl(basePath + "torch/" + name));
        //Wall
        SawmillRecipeBuilder.sawing(
              ItemStackIngredient.from(WoodenItems.getItem(WoodenObjectType.WALL, woodType)),
              new ItemStack(log)
        ).addCondition(modLoaded)
              .build(consumer, Mekanism.rl(basePath + "wall/" + name));
        //Beds
        addPrecisionSawmillBedRecipes(consumer, planks, basePath + name + "/");
    }

    private void addPrecisionSawmillBedRecipes(Consumer<IFinishedRecipe> consumer, Item planks, String basePath) {
        //TODO: FIXME, use the correct type of bed, and implement once beds are added back into the mod
        *//*RecipeProviderUtil.addPrecisionSawmillBedRecipe(consumer, basePath, Items.BLACK_BED, planks, Items.BLACK_WOOL, "black", modLoaded);
        RecipeProviderUtil.addPrecisionSawmillBedRecipe(consumer, basePath, Items.BLUE_BED, planks, Items.BLUE_WOOL, "blue", modLoaded);
        RecipeProviderUtil.addPrecisionSawmillBedRecipe(consumer, basePath, Items.BROWN_BED, planks, Items.BROWN_WOOL, "brown", modLoaded);
        RecipeProviderUtil.addPrecisionSawmillBedRecipe(consumer, basePath, Items.CYAN_BED, planks, Items.CYAN_WOOL, "cyan", modLoaded);
        RecipeProviderUtil.addPrecisionSawmillBedRecipe(consumer, basePath, Items.GRAY_BED, planks, Items.GRAY_WOOL, "gray", modLoaded);
        RecipeProviderUtil.addPrecisionSawmillBedRecipe(consumer, basePath, Items.GREEN_BED, planks, Items.GREEN_WOOL, "green", modLoaded);
        RecipeProviderUtil.addPrecisionSawmillBedRecipe(consumer, basePath, Items.LIGHT_BLUE_BED, planks, Items.LIGHT_BLUE_WOOL, "light_blue", modLoaded);
        RecipeProviderUtil.addPrecisionSawmillBedRecipe(consumer, basePath, Items.LIGHT_GRAY_BED, planks, Items.LIGHT_GRAY_WOOL, "light_gray", modLoaded);
        RecipeProviderUtil.addPrecisionSawmillBedRecipe(consumer, basePath, Items.LIME_BED, planks, Items.LIME_WOOL, "lime", modLoaded);
        RecipeProviderUtil.addPrecisionSawmillBedRecipe(consumer, basePath, Items.MAGENTA_BED, planks, Items.MAGENTA_WOOL, "magenta", modLoaded);
        RecipeProviderUtil.addPrecisionSawmillBedRecipe(consumer, basePath, Items.ORANGE_BED, planks, Items.ORANGE_WOOL, "orange", modLoaded);
        RecipeProviderUtil.addPrecisionSawmillBedRecipe(consumer, basePath, Items.PINK_BED, planks, Items.PINK_WOOL, "pink", modLoaded);
        RecipeProviderUtil.addPrecisionSawmillBedRecipe(consumer, basePath, Items.PURPLE_BED, planks, Items.PURPLE_WOOL, "purple", modLoaded);
        RecipeProviderUtil.addPrecisionSawmillBedRecipe(consumer, basePath, Items.RED_BED, planks, Items.RED_WOOL, "red", modLoaded);
        RecipeProviderUtil.addPrecisionSawmillBedRecipe(consumer, basePath, Items.WHITE_BED, planks, Items.WHITE_WOOL, "white", modLoaded);
        RecipeProviderUtil.addPrecisionSawmillBedRecipe(consumer, basePath, Items.YELLOW_BED, planks, Items.YELLOW_WOOL, "yellow", modLoaded);*//*
    }
}*/