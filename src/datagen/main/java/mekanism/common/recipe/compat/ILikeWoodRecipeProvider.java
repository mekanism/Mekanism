package mekanism.common.recipe.compat;

import biomesoplenty.api.block.BOPBlocks;
import java.util.function.Consumer;
import javax.annotation.ParametersAreNonnullByDefault;
import mekanism.api.datagen.recipe.builder.SawmillRecipeBuilder;
import mekanism.api.recipes.inputs.ItemStackIngredient;
import mekanism.common.Mekanism;
import mekanism.common.recipe.RecipeProviderUtil;
import mekanism.common.registries.MekanismItems;
import net.minecraft.block.Blocks;
import net.minecraft.data.IFinishedRecipe;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.IItemProvider;
import net.minecraftforge.common.crafting.conditions.AndCondition;
import net.minecraftforge.common.crafting.conditions.ICondition;
import net.minecraftforge.common.crafting.conditions.ModLoadedCondition;
import net.minecraftforge.fml.ModList;
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
        addWoodType(consumer, basePath, Blocks.ACACIA_PLANKS, Blocks.ACACIA_LOG, Blocks.ACACIA_LOG, WoodType.ACACIA);
        addWoodType(consumer, basePath, Blocks.BIRCH_PLANKS, Blocks.BIRCH_LOG, Blocks.BIRCH_FENCE, WoodType.BIRCH);
        addWoodType(consumer, basePath, Blocks.CRIMSON_PLANKS, Blocks.CRIMSON_STEM, Blocks.CRIMSON_FENCE, WoodType.CRIMSON);
        addWoodType(consumer, basePath, Blocks.DARK_OAK_PLANKS, Blocks.DARK_OAK_LOG, Blocks.DARK_OAK_FENCE, WoodType.DARK_OAK);
        addWoodType(consumer, basePath, Blocks.JUNGLE_PLANKS, Blocks.JUNGLE_LOG, Blocks.JUNGLE_FENCE, WoodType.JUNGLE);
        addWoodType(consumer, basePath, Blocks.OAK_PLANKS, Blocks.OAK_LOG, Blocks.OAK_FENCE, WoodType.OAK);
        addWoodType(consumer, basePath, Blocks.SPRUCE_PLANKS, Blocks.SPRUCE_LOG, Blocks.SPRUCE_FENCE, WoodType.SPRUCE);
        addWoodType(consumer, basePath, Blocks.WARPED_PLANKS, Blocks.WARPED_STEM, Blocks.WARPED_FENCE, WoodType.WARPED);
        addBiomesOPlentyWoodTypes(consumer, basePath, "biomesoplenty");
    }

    private void addBiomesOPlentyWoodTypes(Consumer<IFinishedRecipe> consumer, String basePath, String secondaryCompatMod) {
        //Validate BOP is loaded so that we don't add these recipes if BOP isn't updated yet but we are
        // still able to compile against it so don't have to comment it out
        if (ModList.get().isLoaded(secondaryCompatMod)) {
            basePath += secondaryCompatMod + "/";
            ICondition modsLoaded = new AndCondition(modLoaded, new ModLoadedCondition(secondaryCompatMod));
            addWoodType(consumer, modsLoaded, basePath, BOPBlocks.cherry_planks, BOPBlocks.cherry_log, BOPBlocks.cherry_fence, WoodType.CHERRY);
            addWoodType(consumer, modsLoaded, basePath, BOPBlocks.dead_planks, BOPBlocks.dead_log, BOPBlocks.dead_fence, WoodType.DEAD);
            addWoodType(consumer, modsLoaded, basePath, BOPBlocks.fir_planks, BOPBlocks.fir_log, BOPBlocks.fir_fence, WoodType.FIR);
            addWoodType(consumer, modsLoaded, basePath, BOPBlocks.hellbark_planks, BOPBlocks.hellbark_log, BOPBlocks.hellbark_fence, WoodType.HELLBARK);
            addWoodType(consumer, modsLoaded, basePath, BOPBlocks.jacaranda_planks, BOPBlocks.jacaranda_log, BOPBlocks.jacaranda_fence, WoodType.JACARANDA);
            addWoodType(consumer, modsLoaded, basePath, BOPBlocks.magic_planks, BOPBlocks.magic_log, BOPBlocks.magic_fence, WoodType.MAGIC);
            addWoodType(consumer, modsLoaded, basePath, BOPBlocks.mahogany_planks, BOPBlocks.mahogany_log, BOPBlocks.mahogany_fence, WoodType.MAHOGANY);
            addWoodType(consumer, modsLoaded, basePath, BOPBlocks.palm_planks, BOPBlocks.palm_log, BOPBlocks.palm_fence, WoodType.PALM);
            addWoodType(consumer, modsLoaded, basePath, BOPBlocks.redwood_planks, BOPBlocks.redwood_log, BOPBlocks.redwood_fence, WoodType.REDWOOD);
            addWoodType(consumer, modsLoaded, basePath, BOPBlocks.umbran_planks, BOPBlocks.umbran_log, BOPBlocks.umbran_fence, WoodType.UMBRAN);
            addWoodType(consumer, modsLoaded, basePath, BOPBlocks.willow_planks, BOPBlocks.willow_log, BOPBlocks.willow_fence, WoodType.WILLOW);
        }
    }

    private void addWoodType(Consumer<IFinishedRecipe> consumer, String basePath, IItemProvider planks, IItemProvider log, IItemProvider fences, WoodType woodType) {
        addWoodType(consumer, modLoaded, basePath, planks, log, fences, woodType);
    }

    //TODO: Maybe move some of these into RecipeProviderUtil, so that we make sure the numbers stay consistent
    private void addWoodType(Consumer<IFinishedRecipe> consumer, ICondition condition, String basePath, IItemProvider planks, IItemProvider log, IItemProvider fences,
          WoodType woodType) {
        String name = woodType.toString();
        Item stick = WoodenItems.getItem(WoodenObjectType.STICK, woodType);
        //Barrel
        SawmillRecipeBuilder.sawing(
              ItemStackIngredient.from(WoodenItems.getItem(WoodenObjectType.BARREL, woodType)),
              new ItemStack(planks, 7)
        ).addCondition(condition)
              .build(consumer, Mekanism.rl(basePath + "barrel/" + name));
        //Bookshelf
        SawmillRecipeBuilder.sawing(
              ItemStackIngredient.from(WoodenItems.getItem(WoodenObjectType.BOOKSHELF, woodType)),
              new ItemStack(planks, 6),
              new ItemStack(Items.BOOK, 3),
              1
        ).addCondition(condition)
              .build(consumer, Mekanism.rl(basePath + "bookshelf/" + name));
        //Chest
        SawmillRecipeBuilder.sawing(
              ItemStackIngredient.from(WoodenItems.getItem(WoodenObjectType.CHEST, woodType)),
              new ItemStack(planks, 8)
        ).addCondition(condition)
              .build(consumer, Mekanism.rl(basePath + "chest/" + name));
        //Composter
        SawmillRecipeBuilder.sawing(
              ItemStackIngredient.from(WoodenItems.getItem(WoodenObjectType.COMPOSTER, woodType)),
              new ItemStack(planks, 3),
              new ItemStack(fences, 4),
              1
        ).addCondition(condition)
              .build(consumer, Mekanism.rl(basePath + "composter/" + name));
        //Crafting table
        SawmillRecipeBuilder.sawing(
              ItemStackIngredient.from(WoodenItems.getItem(WoodenObjectType.CRAFTING_TABLE, woodType)),
              new ItemStack(planks, 4)
        ).addCondition(condition)
              .build(consumer, Mekanism.rl(basePath + "crafting_table/" + name));
        //Item Frame
        SawmillRecipeBuilder.sawing(
              ItemStackIngredient.from(WoodenItems.getItem(WoodenObjectType.ITEM_FRAME, woodType)),
              new ItemStack(stick, 8),
              new ItemStack(Items.LEATHER),
              1
        ).addCondition(condition)
              .build(consumer, Mekanism.rl(basePath + "item_frame/" + name));
        //Ladder
        SawmillRecipeBuilder.sawing(
              ItemStackIngredient.from(WoodenItems.getItem(WoodenObjectType.LADDER, woodType), 3),
              new ItemStack(stick, 7)
        ).addCondition(condition)
              .build(consumer, Mekanism.rl(basePath + "ladder/" + name));
        //Lectern
        SawmillRecipeBuilder.sawing(
              ItemStackIngredient.from(WoodenItems.getItem(WoodenObjectType.LECTERN, woodType)),
              new ItemStack(planks, 8),
              new ItemStack(Items.BOOK, 3),
              1
        ).addCondition(condition)
              .build(consumer, Mekanism.rl(basePath + "lectern/" + name));
        //Panel
        SawmillRecipeBuilder.sawing(
              ItemStackIngredient.from(WoodenItems.getItem(WoodenObjectType.PANELS, woodType)),
              new ItemStack(stick, 6),
              MekanismItems.SAWDUST.getItemStack(),
              0.25
        ).addCondition(condition)
              .build(consumer, Mekanism.rl(basePath + "panel/" + name));
        //Post
        SawmillRecipeBuilder.sawing(
              ItemStackIngredient.createMulti(
                    ItemStackIngredient.from(WoodenItems.getItem(WoodenObjectType.POST, woodType)),
                    ItemStackIngredient.from(WoodenItems.getItem(WoodenObjectType.STRIPPED_POST, woodType))
              ),
              new ItemStack(planks, 3),
              MekanismItems.SAWDUST.getItemStack(),
              0.125
        ).addCondition(condition)
              .build(consumer, Mekanism.rl(basePath + "post/" + name));
        //Torch
        SawmillRecipeBuilder.sawing(
              ItemStackIngredient.from(WoodenItems.getItem(WoodenObjectType.TORCH, woodType), 4),
              new ItemStack(stick),
              new ItemStack(Items.COAL),
              1
        ).addCondition(condition)
              .build(consumer, Mekanism.rl(basePath + "torch/" + name));
        //Wall
        SawmillRecipeBuilder.sawing(
              ItemStackIngredient.from(WoodenItems.getItem(WoodenObjectType.WALL, woodType)),
              new ItemStack(log)
        ).addCondition(condition)
              .build(consumer, Mekanism.rl(basePath + "wall/" + name));
        //Beds
        //addPrecisionSawmillBedRecipes(consumer, condition, planks, basePath + name + "/");
    }

    private void addPrecisionSawmillBedRecipes(Consumer<IFinishedRecipe> consumer, ICondition condition, IItemProvider planks, String basePath) {
        //TODO: FIXME, use the correct type of bed, and implement once beds are added back into the mod
        RecipeProviderUtil.addPrecisionSawmillBedRecipe(consumer, basePath, Items.BLACK_BED, planks, Blocks.BLACK_WOOL, "black", condition);
        RecipeProviderUtil.addPrecisionSawmillBedRecipe(consumer, basePath, Items.BLUE_BED, planks, Blocks.BLUE_WOOL, "blue", condition);
        RecipeProviderUtil.addPrecisionSawmillBedRecipe(consumer, basePath, Items.BROWN_BED, planks, Blocks.BROWN_WOOL, "brown", condition);
        RecipeProviderUtil.addPrecisionSawmillBedRecipe(consumer, basePath, Items.CYAN_BED, planks, Blocks.CYAN_WOOL, "cyan", condition);
        RecipeProviderUtil.addPrecisionSawmillBedRecipe(consumer, basePath, Items.GRAY_BED, planks, Blocks.GRAY_WOOL, "gray", condition);
        RecipeProviderUtil.addPrecisionSawmillBedRecipe(consumer, basePath, Items.GREEN_BED, planks, Blocks.GREEN_WOOL, "green", condition);
        RecipeProviderUtil.addPrecisionSawmillBedRecipe(consumer, basePath, Items.LIGHT_BLUE_BED, planks, Blocks.LIGHT_BLUE_WOOL, "light_blue", condition);
        RecipeProviderUtil.addPrecisionSawmillBedRecipe(consumer, basePath, Items.LIGHT_GRAY_BED, planks, Blocks.LIGHT_GRAY_WOOL, "light_gray", condition);
        RecipeProviderUtil.addPrecisionSawmillBedRecipe(consumer, basePath, Items.LIME_BED, planks, Blocks.LIME_WOOL, "lime", condition);
        RecipeProviderUtil.addPrecisionSawmillBedRecipe(consumer, basePath, Items.MAGENTA_BED, planks, Blocks.MAGENTA_WOOL, "magenta", condition);
        RecipeProviderUtil.addPrecisionSawmillBedRecipe(consumer, basePath, Items.ORANGE_BED, planks, Blocks.ORANGE_WOOL, "orange", condition);
        RecipeProviderUtil.addPrecisionSawmillBedRecipe(consumer, basePath, Items.PINK_BED, planks, Blocks.PINK_WOOL, "pink", condition);
        RecipeProviderUtil.addPrecisionSawmillBedRecipe(consumer, basePath, Items.PURPLE_BED, planks, Blocks.PURPLE_WOOL, "purple", condition);
        RecipeProviderUtil.addPrecisionSawmillBedRecipe(consumer, basePath, Items.RED_BED, planks, Blocks.RED_WOOL, "red", condition);
        RecipeProviderUtil.addPrecisionSawmillBedRecipe(consumer, basePath, Items.WHITE_BED, planks, Blocks.WHITE_WOOL, "white", condition);
        RecipeProviderUtil.addPrecisionSawmillBedRecipe(consumer, basePath, Items.YELLOW_BED, planks, Blocks.YELLOW_WOOL, "yellow", condition);
    }
}