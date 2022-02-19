package mekanism.common.recipe.compat;

import java.util.function.Consumer;
import javax.annotation.ParametersAreNonnullByDefault;
import mekanism.api.datagen.recipe.builder.ItemStackChemicalToItemStackRecipeBuilder;
import mekanism.api.datagen.recipe.builder.SawmillRecipeBuilder;
import mekanism.api.recipes.inputs.ItemStackIngredient;
import mekanism.api.recipes.inputs.chemical.PigmentStackIngredient;
import mekanism.api.text.EnumColor;
import mekanism.common.Mekanism;
import mekanism.common.recipe.RecipeProviderUtil;
import mekanism.common.recipe.condition.ModVersionLoadedCondition;
import mekanism.common.recipe.impl.PigmentExtractingRecipeProvider;
import mekanism.common.registries.MekanismItems;
import mekanism.common.registries.MekanismPigments;
import mekanism.common.util.EnumUtils;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.data.IFinishedRecipe;
import net.minecraft.item.DyeColor;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.util.IItemProvider;
import net.minecraftforge.common.crafting.conditions.ICondition;
import yamahari.ilikewood.ILikeWood;
import yamahari.ilikewood.plugin.vanilla.VanillaWoodTypes;
import yamahari.ilikewood.registry.objecttype.WoodenBlockType;
import yamahari.ilikewood.registry.objecttype.WoodenItemType;
import yamahari.ilikewood.registry.woodtype.IWoodType;

@ParametersAreNonnullByDefault
public class ILikeWoodRecipeProvider extends CompatRecipeProvider {

    //TODO - 1.18: Remove having these extra conditions
    private final ICondition modLoadedBedVersion;
    private final ICondition modLoadedSoulTorchVersion;

    public ILikeWoodRecipeProvider() {
        super(yamahari.ilikewood.util.Constants.MOD_ID);
        modLoadedBedVersion = new ModVersionLoadedCondition(modid, "1.16.3-4.0.2.0");
        modLoadedSoulTorchVersion = new ModVersionLoadedCondition(modid, "1.16.5-6.2.2.0");
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
        addWoodType(consumer, modLoaded, basePath, planks, log, fences, woodType, modLoadedBedVersion, modLoadedSoulTorchVersion);
    }

    //TODO: Maybe move some of these into RecipeProviderUtil, so that we make sure the numbers stay consistent
    public static void addWoodType(Consumer<IFinishedRecipe> consumer, ICondition condition, String basePath, IItemProvider planks, IItemProvider log,
          IItemProvider fences, IWoodType woodType, ICondition bedVersion, ICondition soulTorchVersion) {
        String name = woodType.getName();
        Item stick = ILikeWood.getItem(woodType, WoodenItemType.STICK);
        //Barrel
        SawmillRecipeBuilder.sawing(
                    ItemStackIngredient.from(ILikeWood.getBlock(woodType, WoodenBlockType.BARREL)),
                    new ItemStack(planks, 7)
              ).addCondition(condition)
              .build(consumer, Mekanism.rl(basePath + "barrel/" + name));
        //Chest
        SawmillRecipeBuilder.sawing(
                    ItemStackIngredient.from(ILikeWood.getBlock(woodType, WoodenBlockType.CHEST)),
                    new ItemStack(planks, 8)
              ).addCondition(condition)
              .build(consumer, Mekanism.rl(basePath + "chest/" + name));
        //Composter
        SawmillRecipeBuilder.sawing(
                    ItemStackIngredient.from(ILikeWood.getBlock(woodType, WoodenBlockType.COMPOSTER)),
                    new ItemStack(planks, 3),
                    new ItemStack(fences, 4),
                    1
              ).addCondition(condition)
              .build(consumer, Mekanism.rl(basePath + "composter/" + name));
        //Crafting table
        SawmillRecipeBuilder.sawing(
                    ItemStackIngredient.from(ILikeWood.getBlock(woodType, WoodenBlockType.CRAFTING_TABLE)),
                    new ItemStack(planks, 4)
              ).addCondition(condition)
              .build(consumer, Mekanism.rl(basePath + "crafting_table/" + name));
        //Item Frame
        SawmillRecipeBuilder.sawing(
                    ItemStackIngredient.from(ILikeWood.getItem(woodType, WoodenItemType.ITEM_FRAME)),
                    new ItemStack(stick, 8),
                    new ItemStack(Items.LEATHER),
                    1
              ).addCondition(condition)
              .build(consumer, Mekanism.rl(basePath + "item_frame/" + name));
        //Ladder
        SawmillRecipeBuilder.sawing(
                    ItemStackIngredient.from(ILikeWood.getBlock(woodType, WoodenBlockType.LADDER), 3),
                    new ItemStack(stick, 7)
              ).addCondition(condition)
              .build(consumer, Mekanism.rl(basePath + "ladder/" + name));
        //Lectern
        SawmillRecipeBuilder.sawing(
                    ItemStackIngredient.from(ILikeWood.getBlock(woodType, WoodenBlockType.LECTERN)),
                    new ItemStack(planks, 8),
                    new ItemStack(Items.BOOK, 3),
                    1
              ).addCondition(condition)
              .build(consumer, Mekanism.rl(basePath + "lectern/" + name));
        //Panel
        SawmillRecipeBuilder.sawing(
                    ItemStackIngredient.from(ILikeWood.getBlock(woodType, WoodenBlockType.PANELS)),
                    new ItemStack(stick, 6),
                    MekanismItems.SAWDUST.getItemStack(),
                    0.25
              ).addCondition(condition)
              .build(consumer, Mekanism.rl(basePath + "panel/" + name));
        //Post
        ItemStackIngredient postIngredient;
        if (woodType.getBlockTypes().contains(WoodenBlockType.STRIPPED_POST)) {
            postIngredient = ItemStackIngredient.from(Ingredient.of(
                  ILikeWood.getBlock(woodType, WoodenBlockType.POST),
                  ILikeWood.getBlock(woodType, WoodenBlockType.STRIPPED_POST)
            ));
        } else {
            postIngredient = ItemStackIngredient.from(ILikeWood.getBlock(woodType, WoodenBlockType.POST));
        }
        SawmillRecipeBuilder.sawing(
                    postIngredient,
                    new ItemStack(planks, 3),
                    MekanismItems.SAWDUST.getItemStack(),
                    0.125
              ).addCondition(condition)
              .build(consumer, Mekanism.rl(basePath + "post/" + name));
        //Torch
        Block torch = ILikeWood.getBlock(woodType, WoodenBlockType.TORCH);
        SawmillRecipeBuilder.sawing(
                    ItemStackIngredient.from(torch, 4),
                    new ItemStack(stick),
                    new ItemStack(Items.COAL),
                    1
              ).addCondition(condition)
              .build(consumer, Mekanism.rl(basePath + "torch/" + name));
        //Soul Torch
        SawmillRecipeBuilder.sawing(
                    ItemStackIngredient.from(ILikeWood.getBlock(woodType, WoodenBlockType.SOUL_TORCH), 4),
                    new ItemStack(torch, 4),
                    new ItemStack(Blocks.SOUL_SOIL),
                    1
              ).addCondition(soulTorchVersion)
              .build(consumer, Mekanism.rl(basePath + "soul_torch/" + name));
        //Wall
        if (woodType.getBlockTypes().contains(WoodenBlockType.WALL)) {
            SawmillRecipeBuilder.sawing(
                        ItemStackIngredient.from(ILikeWood.getBlock(woodType, WoodenBlockType.WALL)),
                        new ItemStack(log)
                  ).addCondition(condition)
                  .build(consumer, Mekanism.rl(basePath + "wall/" + name));
        }
        //Beds
        addBedRecipes(consumer, bedVersion, planks, woodType, basePath + "bed/" + name + "/");
    }

    private static void addBedRecipes(Consumer<IFinishedRecipe> consumer, ICondition condition, IItemProvider planks, IWoodType woodType, String basePath) {
        for (EnumColor color : EnumUtils.COLORS) {
            addBedRecipe(consumer, condition, planks, woodType, color, basePath);
        }
    }

    private static void addBedRecipe(Consumer<IFinishedRecipe> consumer, ICondition condition, IItemProvider planks, IWoodType woodType, EnumColor color, String basePath) {
        DyeColor dye = color.getDyeColor();
        if (dye != null) {
            WoodenBlockType bedType = getBedType(dye);
            Block bed = ILikeWood.getBlock(woodType, bedType);
            RecipeProviderUtil.addPrecisionSawmillBedRecipe(consumer, basePath, bed, planks, dye, condition);
            ItemStackChemicalToItemStackRecipeBuilder.painting(
                        ItemStackIngredient.from(Ingredient.of(WoodenBlockType.getBeds().filter(b -> !b.equals(bedType)).map(b -> new ItemStack(ILikeWood.getBlock(woodType, b))))),
                        PigmentStackIngredient.from(MekanismPigments.PIGMENT_COLOR_LOOKUP.get(color), PigmentExtractingRecipeProvider.DYE_RATE),
                        new ItemStack(bed)
                  ).addCondition(condition)
                  .build(consumer, Mekanism.rl(basePath + "painting/" + color.getRegistryPrefix()));
        }
    }

    private static WoodenBlockType getBedType(DyeColor dyeColor) {
        switch (dyeColor) {
            case WHITE:
                return WoodenBlockType.WHITE_BED;
            case ORANGE:
                return WoodenBlockType.ORANGE_BED;
            case MAGENTA:
                return WoodenBlockType.MAGENTA_BED;
            case LIGHT_BLUE:
                return WoodenBlockType.LIGHT_BLUE_BED;
            case YELLOW:
                return WoodenBlockType.YELLOW_BED;
            case LIME:
                return WoodenBlockType.LIME_BED;
            case PINK:
                return WoodenBlockType.PINK_BED;
            case GRAY:
                return WoodenBlockType.GRAY_BED;
            case LIGHT_GRAY:
                return WoodenBlockType.LIGHT_GRAY_BED;
            case CYAN:
                return WoodenBlockType.CYAN_BED;
            case PURPLE:
                return WoodenBlockType.PURPLE_BED;
            case BLUE:
                return WoodenBlockType.BLUE_BED;
            case BROWN:
                return WoodenBlockType.BROWN_BED;
            case GREEN:
                return WoodenBlockType.GREEN_BED;
            case RED:
                return WoodenBlockType.RED_BED;
            case BLACK:
                return WoodenBlockType.BLACK_BED;
        }
        throw new IllegalArgumentException("Unknown dye color.");
    }
}