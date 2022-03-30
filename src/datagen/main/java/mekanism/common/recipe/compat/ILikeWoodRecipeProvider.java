package mekanism.common.recipe.compat;

import java.util.Arrays;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.annotation.ParametersAreNonnullByDefault;
import mekanism.api.datagen.recipe.builder.ItemStackChemicalToItemStackRecipeBuilder;
import mekanism.api.datagen.recipe.builder.SawmillRecipeBuilder;
import mekanism.api.recipes.ingredients.ItemStackIngredient;
import mekanism.api.recipes.ingredients.creator.IngredientCreatorAccess;
import mekanism.api.text.EnumColor;
import mekanism.common.Mekanism;
import mekanism.common.lib.FieldReflectionHelper;
import mekanism.common.recipe.RecipeProviderUtil;
import mekanism.common.recipe.impl.PigmentExtractingRecipeProvider;
import mekanism.common.registries.MekanismItems;
import mekanism.common.registries.MekanismPigments;
import mekanism.common.util.EnumUtils;
import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraftforge.common.crafting.conditions.AndCondition;
import net.minecraftforge.common.crafting.conditions.ICondition;
import net.minecraftforge.common.crafting.conditions.ModLoadedCondition;
import yamahari.ilikewood.ILikeWood;
import yamahari.ilikewood.config.ILikeWoodConfig;
import yamahari.ilikewood.plugin.vanilla.VanillaWoodTypes;
import yamahari.ilikewood.registry.objecttype.AbstractWoodenObjectType;
import yamahari.ilikewood.registry.objecttype.WoodenBlockType;
import yamahari.ilikewood.registry.objecttype.WoodenItemType;
import yamahari.ilikewood.registry.woodtype.IWoodType;

@ParametersAreNonnullByDefault
public class ILikeWoodRecipeProvider extends CompatRecipeProvider {

    private static final FieldReflectionHelper<AndCondition, ICondition[]> EXPLODER = new FieldReflectionHelper<>(AndCondition.class, "children",
          () -> new ICondition[0]);
    private static final FieldReflectionHelper<AbstractWoodenObjectType, Supplier<Boolean>> CONFIG_FINDER = new FieldReflectionHelper<>(AbstractWoodenObjectType.class,
          "flag", () -> () -> false);
    private static final Map<ILikeWoodConfig, String> KNOWN_CONFIGS = ILikeWoodConfig.getAll().collect(Collectors.toMap(Function.identity(),
          config -> String.format("%s_%s", yamahari.ilikewood.util.Constants.MOD_ID, config.name())));

    public ILikeWoodRecipeProvider() {
        super(yamahari.ilikewood.util.Constants.MOD_ID);
    }

    @Override
    protected void registerRecipes(Consumer<FinishedRecipe> consumer, String basePath) {
        addWoodType(consumer, basePath, Blocks.ACACIA_PLANKS, Blocks.ACACIA_LOG, Blocks.ACACIA_LOG, VanillaWoodTypes.ACACIA);
        addWoodType(consumer, basePath, Blocks.BIRCH_PLANKS, Blocks.BIRCH_LOG, Blocks.BIRCH_FENCE, VanillaWoodTypes.BIRCH);
        addWoodType(consumer, basePath, Blocks.CRIMSON_PLANKS, Blocks.CRIMSON_STEM, Blocks.CRIMSON_FENCE, VanillaWoodTypes.CRIMSON);
        addWoodType(consumer, basePath, Blocks.DARK_OAK_PLANKS, Blocks.DARK_OAK_LOG, Blocks.DARK_OAK_FENCE, VanillaWoodTypes.DARK_OAK);
        addWoodType(consumer, basePath, Blocks.JUNGLE_PLANKS, Blocks.JUNGLE_LOG, Blocks.JUNGLE_FENCE, VanillaWoodTypes.JUNGLE);
        addWoodType(consumer, basePath, Blocks.OAK_PLANKS, Blocks.OAK_LOG, Blocks.OAK_FENCE, VanillaWoodTypes.OAK);
        addWoodType(consumer, basePath, Blocks.SPRUCE_PLANKS, Blocks.SPRUCE_LOG, Blocks.SPRUCE_FENCE, VanillaWoodTypes.SPRUCE);
        addWoodType(consumer, basePath, Blocks.WARPED_PLANKS, Blocks.WARPED_STEM, Blocks.WARPED_FENCE, VanillaWoodTypes.WARPED);
    }

    private void addWoodType(Consumer<FinishedRecipe> consumer, String basePath, ItemLike planks, ItemLike log, ItemLike fences, IWoodType woodType) {
        addWoodType(consumer, modLoaded, basePath, planks, log, fences, woodType);
    }

    //TODO: Maybe move some of these into RecipeProviderUtil, so that we make sure the numbers stay consistent
    public static void addWoodType(Consumer<FinishedRecipe> consumer, ICondition condition, String basePath, ItemLike planks, ItemLike log,
          ItemLike fences, IWoodType woodType) {
        String name = woodType.getName();
        Item stick = ILikeWood.getItem(woodType, WoodenItemType.STICK);
        //Barrel
        SawmillRecipeBuilder.sawing(
                    IngredientCreatorAccess.item().from(ILikeWood.getBlock(woodType, WoodenBlockType.BARREL)),
                    new ItemStack(planks, 7)
              ).addCondition(compoundCondition(condition, WoodenBlockType.BARREL))
              .build(consumer, Mekanism.rl(basePath + "barrel/" + name));
        //Chest
        SawmillRecipeBuilder.sawing(
                    IngredientCreatorAccess.item().from(ILikeWood.getBlock(woodType, WoodenBlockType.CHEST)),
                    new ItemStack(planks, 8)
              ).addCondition(compoundCondition(condition, WoodenBlockType.CHEST))
              .build(consumer, Mekanism.rl(basePath + "chest/" + name));
        //Composter
        SawmillRecipeBuilder.sawing(
                    IngredientCreatorAccess.item().from(ILikeWood.getBlock(woodType, WoodenBlockType.COMPOSTER)),
                    new ItemStack(planks, 3),
                    new ItemStack(fences, 4),
                    1
              ).addCondition(compoundCondition(condition, WoodenBlockType.COMPOSTER))
              .build(consumer, Mekanism.rl(basePath + "composter/" + name));
        //Crafting table
        SawmillRecipeBuilder.sawing(
                    IngredientCreatorAccess.item().from(ILikeWood.getBlock(woodType, WoodenBlockType.CRAFTING_TABLE)),
                    new ItemStack(planks, 4)
              ).addCondition(compoundCondition(condition, WoodenBlockType.CRAFTING_TABLE))
              .build(consumer, Mekanism.rl(basePath + "crafting_table/" + name));
        //Item Frame
        SawmillRecipeBuilder.sawing(
                    IngredientCreatorAccess.item().from(ILikeWood.getItem(woodType, WoodenItemType.ITEM_FRAME)),
                    new ItemStack(stick, 8),
                    new ItemStack(Items.LEATHER),
                    1
              ).addCondition(compoundCondition(condition, WoodenItemType.STICK, WoodenItemType.ITEM_FRAME))
              .build(consumer, Mekanism.rl(basePath + "item_frame/" + name));
        //Ladder
        SawmillRecipeBuilder.sawing(
                    IngredientCreatorAccess.item().from(ILikeWood.getBlock(woodType, WoodenBlockType.LADDER), 3),
                    new ItemStack(stick, 7)
              ).addCondition(compoundCondition(condition, WoodenItemType.STICK, WoodenBlockType.LADDER))
              .build(consumer, Mekanism.rl(basePath + "ladder/" + name));
        //Lectern
        SawmillRecipeBuilder.sawing(
                    IngredientCreatorAccess.item().from(ILikeWood.getBlock(woodType, WoodenBlockType.LECTERN)),
                    new ItemStack(planks, 8),
                    new ItemStack(Items.BOOK, 3),
                    1
              ).addCondition(compoundCondition(condition, WoodenBlockType.LECTERN))
              .build(consumer, Mekanism.rl(basePath + "lectern/" + name));
        //Panel
        SawmillRecipeBuilder.sawing(
                    IngredientCreatorAccess.item().from(ILikeWood.getBlock(woodType, WoodenBlockType.PANELS)),
                    new ItemStack(stick, 6),
                    MekanismItems.SAWDUST.getItemStack(),
                    0.25
              ).addCondition(compoundCondition(condition, WoodenItemType.STICK, WoodenBlockType.PANELS))
              .build(consumer, Mekanism.rl(basePath + "panel/" + name));
        //Post
        ItemStackIngredient postIngredient;
        ICondition postCondition;
        if (woodType.getBlockTypes().contains(WoodenBlockType.STRIPPED_POST)) {
            postIngredient = IngredientCreatorAccess.item().from(Ingredient.of(
                  ILikeWood.getBlock(woodType, WoodenBlockType.POST),
                  ILikeWood.getBlock(woodType, WoodenBlockType.STRIPPED_POST)
            ));
            postCondition = compoundCondition(condition, WoodenBlockType.POST, WoodenBlockType.STRIPPED_POST);
        } else {
            postIngredient = IngredientCreatorAccess.item().from(ILikeWood.getBlock(woodType, WoodenBlockType.POST));
            postCondition = compoundCondition(condition, WoodenBlockType.POST);
        }
        SawmillRecipeBuilder.sawing(
                    postIngredient,
                    new ItemStack(planks, 3),
                    MekanismItems.SAWDUST.getItemStack(),
                    0.125
              ).addCondition(postCondition)
              .build(consumer, Mekanism.rl(basePath + "post/" + name));
        //Torch
        Block torch = ILikeWood.getBlock(woodType, WoodenBlockType.TORCH);
        SawmillRecipeBuilder.sawing(
                    IngredientCreatorAccess.item().from(torch, 4),
                    new ItemStack(stick),
                    new ItemStack(Items.COAL),
                    1
              ).addCondition(compoundCondition(condition, WoodenItemType.STICK, WoodenBlockType.TORCH))
              .build(consumer, Mekanism.rl(basePath + "torch/" + name));
        //Soul Torch
        SawmillRecipeBuilder.sawing(
                    IngredientCreatorAccess.item().from(ILikeWood.getBlock(woodType, WoodenBlockType.SOUL_TORCH), 4),
                    new ItemStack(torch, 4),
                    new ItemStack(Blocks.SOUL_SOIL),
                    1
              ).addCondition(compoundCondition(condition, WoodenBlockType.SOUL_TORCH, WoodenBlockType.TORCH))
              .build(consumer, Mekanism.rl(basePath + "soul_torch/" + name));
        //Wall
        if (woodType.getBlockTypes().contains(WoodenBlockType.WALL)) {
            SawmillRecipeBuilder.sawing(
                        IngredientCreatorAccess.item().from(ILikeWood.getBlock(woodType, WoodenBlockType.WALL)),
                        new ItemStack(log)
                  ).addCondition(compoundCondition(condition, WoodenBlockType.WALL))
                  .build(consumer, Mekanism.rl(basePath + "wall/" + name));
        }
        //Beds
        addBedRecipes(consumer, condition, planks, woodType, basePath + "bed/" + name + "/");
    }

    private static void addBedRecipes(Consumer<FinishedRecipe> consumer, ICondition condition, ItemLike planks, IWoodType woodType, String basePath) {
        //All beds share the same config so just use the white bed for condition lookup
        condition = compoundCondition(condition, WoodenBlockType.WHITE_BED);
        for (EnumColor color : EnumUtils.COLORS) {
            addBedRecipe(consumer, condition, planks, woodType, color, basePath);
        }
    }

    private static void addBedRecipe(Consumer<FinishedRecipe> consumer, ICondition condition, ItemLike planks, IWoodType woodType, EnumColor color, String basePath) {
        DyeColor dye = color.getDyeColor();
        if (dye != null) {
            WoodenBlockType bedType = getBedType(dye);
            Block bed = ILikeWood.getBlock(woodType, bedType);
            RecipeProviderUtil.addPrecisionSawmillBedRecipe(consumer, basePath, bed, planks, dye, condition);
            ItemStackChemicalToItemStackRecipeBuilder.painting(
                        IngredientCreatorAccess.item().from(Ingredient.of(WoodenBlockType.getBeds().filter(b -> !b.equals(bedType)).map(b -> new ItemStack(ILikeWood.getBlock(woodType, b))))),
                        IngredientCreatorAccess.pigment().from(MekanismPigments.PIGMENT_COLOR_LOOKUP.get(color), PigmentExtractingRecipeProvider.DYE_RATE),
                        new ItemStack(bed)
                  ).addCondition(condition)
                  .build(consumer, Mekanism.rl(basePath + "painting/" + color.getRegistryPrefix()));
        }
    }

    private static WoodenBlockType getBedType(DyeColor dyeColor) {
        return switch (dyeColor) {
            case WHITE -> WoodenBlockType.WHITE_BED;
            case ORANGE -> WoodenBlockType.ORANGE_BED;
            case MAGENTA -> WoodenBlockType.MAGENTA_BED;
            case LIGHT_BLUE -> WoodenBlockType.LIGHT_BLUE_BED;
            case YELLOW -> WoodenBlockType.YELLOW_BED;
            case LIME -> WoodenBlockType.LIME_BED;
            case PINK -> WoodenBlockType.PINK_BED;
            case GRAY -> WoodenBlockType.GRAY_BED;
            case LIGHT_GRAY -> WoodenBlockType.LIGHT_GRAY_BED;
            case CYAN -> WoodenBlockType.CYAN_BED;
            case PURPLE -> WoodenBlockType.PURPLE_BED;
            case BLUE -> WoodenBlockType.BLUE_BED;
            case BROWN -> WoodenBlockType.BROWN_BED;
            case GREEN -> WoodenBlockType.GREEN_BED;
            case RED -> WoodenBlockType.RED_BED;
            case BLACK -> WoodenBlockType.BLACK_BED;
        };
    }

    private static ICondition compoundCondition(ICondition base, AbstractWoodenObjectType... objects) {
        Stream<ICondition> baseConditions;
        if (base instanceof AndCondition and) {
            //Only support to a depth of one
            baseConditions = Arrays.stream(EXPLODER.getValue(and));
        } else {
            baseConditions = Stream.of(base);
        }
        return new AndCondition(Stream.concat(baseConditions, Arrays.stream(objects)
              .map(object -> {
                  Supplier<Boolean> config = CONFIG_FINDER.getValue(object);
                  for (Map.Entry<ILikeWoodConfig, String> entry : KNOWN_CONFIGS.entrySet()) {
                      if (config == entry.getKey().flag()) {
                          return entry.getValue();
                      }
                  }
                  throw new RuntimeException("Could not find corresponding config for object type: " + object.getName());
              })
              .distinct()
              .sorted()
              .map(ModLoadedCondition::new)
        ).toArray(ICondition[]::new));
    }
}