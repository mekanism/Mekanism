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
import net.minecraft.block.Blocks;
import net.minecraft.data.IFinishedRecipe;
import net.minecraft.item.DyeColor;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.util.IItemProvider;
import net.minecraftforge.common.crafting.conditions.ICondition;
import yamahari.ilikewood.plugin.vanilla.VanillaWoodTypes;
import yamahari.ilikewood.registry.WoodenItems;
import yamahari.ilikewood.registry.woodtype.IWoodType;
import yamahari.ilikewood.util.objecttype.WoodenObjectTypes;

@ParametersAreNonnullByDefault
public class ILikeWoodRecipeProvider extends CompatRecipeProvider {

    //TODO - 1.17: Remove having these extra conditions
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
        Item stick = WoodenItems.getItem(WoodenObjectTypes.STICK, woodType);
        //Barrel
        SawmillRecipeBuilder.sawing(
              ItemStackIngredient.from(WoodenItems.getItem(WoodenObjectTypes.BARREL, woodType)),
              new ItemStack(planks, 7)
        ).addCondition(condition)
              .build(consumer, Mekanism.rl(basePath + "barrel/" + name));
        //Chest
        SawmillRecipeBuilder.sawing(
              ItemStackIngredient.from(WoodenItems.getItem(WoodenObjectTypes.CHEST, woodType)),
              new ItemStack(planks, 8)
        ).addCondition(condition)
              .build(consumer, Mekanism.rl(basePath + "chest/" + name));
        //Composter
        SawmillRecipeBuilder.sawing(
              ItemStackIngredient.from(WoodenItems.getItem(WoodenObjectTypes.COMPOSTER, woodType)),
              new ItemStack(planks, 3),
              new ItemStack(fences, 4),
              1
        ).addCondition(condition)
              .build(consumer, Mekanism.rl(basePath + "composter/" + name));
        //Crafting table
        SawmillRecipeBuilder.sawing(
              ItemStackIngredient.from(WoodenItems.getItem(WoodenObjectTypes.CRAFTING_TABLE, woodType)),
              new ItemStack(planks, 4)
        ).addCondition(condition)
              .build(consumer, Mekanism.rl(basePath + "crafting_table/" + name));
        //Item Frame
        SawmillRecipeBuilder.sawing(
              ItemStackIngredient.from(WoodenItems.getItem(WoodenObjectTypes.ITEM_FRAME, woodType)),
              new ItemStack(stick, 8),
              new ItemStack(Items.LEATHER),
              1
        ).addCondition(condition)
              .build(consumer, Mekanism.rl(basePath + "item_frame/" + name));
        //Ladder
        SawmillRecipeBuilder.sawing(
              ItemStackIngredient.from(WoodenItems.getItem(WoodenObjectTypes.LADDER, woodType), 3),
              new ItemStack(stick, 7)
        ).addCondition(condition)
              .build(consumer, Mekanism.rl(basePath + "ladder/" + name));
        //Lectern
        SawmillRecipeBuilder.sawing(
              ItemStackIngredient.from(WoodenItems.getItem(WoodenObjectTypes.LECTERN, woodType)),
              new ItemStack(planks, 8),
              new ItemStack(Items.BOOK, 3),
              1
        ).addCondition(condition)
              .build(consumer, Mekanism.rl(basePath + "lectern/" + name));
        //Panel
        SawmillRecipeBuilder.sawing(
              ItemStackIngredient.from(WoodenItems.getItem(WoodenObjectTypes.PANELS, woodType)),
              new ItemStack(stick, 6),
              MekanismItems.SAWDUST.getItemStack(),
              0.25
        ).addCondition(condition)
              .build(consumer, Mekanism.rl(basePath + "panel/" + name));
        //Post
        SawmillRecipeBuilder.sawing(
              ItemStackIngredient.createMulti(
                    ItemStackIngredient.from(WoodenItems.getItem(WoodenObjectTypes.POST, woodType)),
                    ItemStackIngredient.from(WoodenItems.getItem(WoodenObjectTypes.STRIPPED_POST, woodType))
              ),
              new ItemStack(planks, 3),
              MekanismItems.SAWDUST.getItemStack(),
              0.125
        ).addCondition(condition)
              .build(consumer, Mekanism.rl(basePath + "post/" + name));
        //Torch
        Item torch = WoodenItems.getItem(WoodenObjectTypes.TORCH, woodType);
        SawmillRecipeBuilder.sawing(
              ItemStackIngredient.from(torch, 4),
              new ItemStack(stick),
              new ItemStack(Items.COAL),
              1
        ).addCondition(condition)
              .build(consumer, Mekanism.rl(basePath + "torch/" + name));
        //Soul Torch
        SawmillRecipeBuilder.sawing(
              ItemStackIngredient.from(WoodenItems.getItem(WoodenObjectTypes.SOUL_TORCH, woodType), 4),
              new ItemStack(torch, 4),
              new ItemStack(Blocks.SOUL_SOIL),
              1
        ).addCondition(soulTorchVersion)
              .build(consumer, Mekanism.rl(basePath + "soul_torch/" + name));
        //Wall
        SawmillRecipeBuilder.sawing(
              ItemStackIngredient.from(WoodenItems.getItem(WoodenObjectTypes.WALL, woodType)),
              new ItemStack(log)
        ).addCondition(condition)
              .build(consumer, Mekanism.rl(basePath + "wall/" + name));
        //Beds
        addBedRecipes(consumer, bedVersion, planks, woodType, basePath + "bed/" + name + "/");
    }

    private static void addBedRecipes(Consumer<IFinishedRecipe> consumer, ICondition condition, IItemProvider planks, IWoodType woodType, String basePath) {
        addBedRecipe(consumer, condition, planks, woodType, DyeColor.BLACK, EnumColor.BLACK, basePath);
        addBedRecipe(consumer, condition, planks, woodType, DyeColor.BLUE, EnumColor.DARK_BLUE, basePath);
        addBedRecipe(consumer, condition, planks, woodType, DyeColor.BROWN, EnumColor.BROWN, basePath);
        addBedRecipe(consumer, condition, planks, woodType, DyeColor.CYAN, EnumColor.DARK_AQUA, basePath);
        addBedRecipe(consumer, condition, planks, woodType, DyeColor.GRAY, EnumColor.DARK_GRAY, basePath);
        addBedRecipe(consumer, condition, planks, woodType, DyeColor.GREEN, EnumColor.DARK_GREEN, basePath);
        addBedRecipe(consumer, condition, planks, woodType, DyeColor.LIGHT_BLUE, EnumColor.INDIGO, basePath);
        addBedRecipe(consumer, condition, planks, woodType, DyeColor.LIGHT_GRAY, EnumColor.GRAY, basePath);
        addBedRecipe(consumer, condition, planks, woodType, DyeColor.LIME, EnumColor.BRIGHT_GREEN, basePath);
        addBedRecipe(consumer, condition, planks, woodType, DyeColor.MAGENTA, EnumColor.PINK, basePath);
        addBedRecipe(consumer, condition, planks, woodType, DyeColor.ORANGE, EnumColor.ORANGE, basePath);
        addBedRecipe(consumer, condition, planks, woodType, DyeColor.PINK, EnumColor.BRIGHT_PINK, basePath);
        addBedRecipe(consumer, condition, planks, woodType, DyeColor.PURPLE, EnumColor.PURPLE, basePath);
        addBedRecipe(consumer, condition, planks, woodType, DyeColor.RED, EnumColor.RED, basePath);
        addBedRecipe(consumer, condition, planks, woodType, DyeColor.WHITE, EnumColor.WHITE, basePath);
        addBedRecipe(consumer, condition, planks, woodType, DyeColor.YELLOW, EnumColor.YELLOW, basePath);
    }

    private static void addBedRecipe(Consumer<IFinishedRecipe> consumer, ICondition condition, IItemProvider planks, IWoodType woodType, DyeColor dyeColor,
          EnumColor color, String basePath) {
        Item bed = WoodenItems.getBedItem(woodType, dyeColor);
        RecipeProviderUtil.addPrecisionSawmillBedRecipe(consumer, basePath, bed, planks, dyeColor, condition);
        ItemStackChemicalToItemStackRecipeBuilder.painting(
              ItemStackIngredient.from(Ingredient.of(WoodenItems.getBedItems(woodType).filter(b -> b != bed).map(ItemStack::new))),
              PigmentStackIngredient.from(MekanismPigments.PIGMENT_COLOR_LOOKUP.get(color), PigmentExtractingRecipeProvider.DYE_RATE),
              new ItemStack(bed)
        ).addCondition(condition)
              .build(consumer, Mekanism.rl(basePath + "painting/" + color.getRegistryPrefix()));
    }
}