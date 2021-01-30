package mekanism.common.recipe.compat;

import java.util.function.Consumer;
import javax.annotation.ParametersAreNonnullByDefault;
import mekanism.api.datagen.recipe.builder.SawmillRecipeBuilder;
import mekanism.api.recipes.inputs.ItemStackIngredient;
import mekanism.common.Mekanism;
import mekanism.common.recipe.RecipeProviderUtil;
import mekanism.common.recipe.condition.ModVersionLoadedCondition;
import mekanism.common.registries.MekanismItems;
import net.minecraft.block.Blocks;
import net.minecraft.data.IFinishedRecipe;
import net.minecraft.item.DyeColor;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.IItemProvider;
import net.minecraftforge.common.crafting.conditions.ICondition;
import yamahari.ilikewood.plugin.vanilla.VanillaWoodTypes;
import yamahari.ilikewood.registry.WoodenItems;
import yamahari.ilikewood.registry.woodtype.IWoodType;
import yamahari.ilikewood.util.WoodenObjectType;

@ParametersAreNonnullByDefault
public class ILikeWoodRecipeProvider extends CompatRecipeProvider {

    //TODO - 1.17: Remove having this as a second condition type
    private final ICondition modLoadedBedVersion;

    public ILikeWoodRecipeProvider() {
        super(yamahari.ilikewood.util.Constants.MOD_ID);
        modLoadedBedVersion = new ModVersionLoadedCondition(modid, "1.16.3-4.0.2.0");
    }

    @Override
    protected void registerRecipes(Consumer<IFinishedRecipe> consumer, String basePath) {
        addWoodType(consumer, basePath, Blocks.ACACIA_PLANKS, Blocks.ACACIA_LOG, Blocks.ACACIA_LOG, VanillaWoodTypes.ACACIA);
        addWoodType(consumer, basePath, Blocks.BIRCH_PLANKS, Blocks.BIRCH_LOG, Blocks.BIRCH_FENCE, VanillaWoodTypes.BIRCH);
        addWoodType(consumer, basePath, Blocks.CRIMSON_PLANKS, Blocks.CRIMSON_STEM, Blocks.CRIMSON_FENCE, VanillaWoodTypes.CRIMSON);
        addWoodType(consumer, basePath, Blocks.DARK_OAK_PLANKS, Blocks.DARK_OAK_LOG, Blocks.DARK_OAK_FENCE, VanillaWoodTypes.DARK_OAK);
        addWoodType(consumer, basePath, Blocks.JUNGLE_PLANKS, Blocks.JUNGLE_LOG, Blocks.JUNGLE_FENCE, VanillaWoodTypes.JUNGLE);
        addWoodType(consumer, basePath, Blocks.OAK_PLANKS, Blocks.OAK_LOG, Blocks.OAK_FENCE, VanillaWoodTypes.OAK);
        addWoodType(consumer, basePath, Blocks.SPRUCE_PLANKS, Blocks.SPRUCE_LOG, Blocks.SPRUCE_FENCE, VanillaWoodTypes.SPRUCE);
        addWoodType(consumer, basePath, Blocks.WARPED_PLANKS, Blocks.WARPED_STEM, Blocks.WARPED_FENCE, VanillaWoodTypes.WARPED);
    }

    private void addWoodType(Consumer<IFinishedRecipe> consumer, String basePath, IItemProvider planks, IItemProvider log, IItemProvider fences, IWoodType woodType) {
        addWoodType(consumer, modLoaded, basePath, planks, log, fences, woodType, modLoadedBedVersion);
    }

    //TODO: Maybe move some of these into RecipeProviderUtil, so that we make sure the numbers stay consistent
    public static void addWoodType(Consumer<IFinishedRecipe> consumer, ICondition condition, String basePath, IItemProvider planks, IItemProvider log, IItemProvider fences,
          IWoodType woodType, ICondition bedVersion) {
        String name = woodType.getName();
        Item stick = WoodenItems.getItem(WoodenObjectType.STICK, woodType);
        //Barrel
        SawmillRecipeBuilder.sawing(
              ItemStackIngredient.from(WoodenItems.getItem(WoodenObjectType.BARREL, woodType)),
              new ItemStack(planks, 7)
        ).addCondition(condition)
              .build(consumer, Mekanism.rl(basePath + "barrel/" + name));
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
        addPrecisionSawmillBedRecipes(consumer, bedVersion, planks, woodType, basePath + "bed/" + name + "/");
    }

    private static void addPrecisionSawmillBedRecipes(Consumer<IFinishedRecipe> consumer, ICondition condition, IItemProvider planks, IWoodType woodType, String basePath) {
        addPrecisionSawmillBedRecipe(consumer, condition, planks, woodType, DyeColor.BLACK, basePath);
        addPrecisionSawmillBedRecipe(consumer, condition, planks, woodType, DyeColor.BLUE, basePath);
        addPrecisionSawmillBedRecipe(consumer, condition, planks, woodType, DyeColor.BROWN, basePath);
        addPrecisionSawmillBedRecipe(consumer, condition, planks, woodType, DyeColor.CYAN, basePath);
        addPrecisionSawmillBedRecipe(consumer, condition, planks, woodType, DyeColor.GRAY, basePath);
        addPrecisionSawmillBedRecipe(consumer, condition, planks, woodType, DyeColor.GREEN, basePath);
        addPrecisionSawmillBedRecipe(consumer, condition, planks, woodType, DyeColor.LIGHT_BLUE, basePath);
        addPrecisionSawmillBedRecipe(consumer, condition, planks, woodType, DyeColor.LIGHT_GRAY, basePath);
        addPrecisionSawmillBedRecipe(consumer, condition, planks, woodType, DyeColor.LIME, basePath);
        addPrecisionSawmillBedRecipe(consumer, condition, planks, woodType, DyeColor.MAGENTA, basePath);
        addPrecisionSawmillBedRecipe(consumer, condition, planks, woodType, DyeColor.ORANGE, basePath);
        addPrecisionSawmillBedRecipe(consumer, condition, planks, woodType, DyeColor.PINK, basePath);
        addPrecisionSawmillBedRecipe(consumer, condition, planks, woodType, DyeColor.PURPLE, basePath);
        addPrecisionSawmillBedRecipe(consumer, condition, planks, woodType, DyeColor.RED, basePath);
        addPrecisionSawmillBedRecipe(consumer, condition, planks, woodType, DyeColor.WHITE, basePath);
        addPrecisionSawmillBedRecipe(consumer, condition, planks, woodType, DyeColor.YELLOW, basePath);
    }

    private static void addPrecisionSawmillBedRecipe(Consumer<IFinishedRecipe> consumer, ICondition condition, IItemProvider planks, IWoodType woodType, DyeColor color,
          String basePath) {
        RecipeProviderUtil.addPrecisionSawmillBedRecipe(consumer, basePath, WoodenItems.getBedItem(woodType, color), planks, color, condition);
    }
}