package mekanism.common.recipe.compat;

import corgiaoc.byg.core.BYGBlocks;
import corgiaoc.byg.core.BYGItems;
import java.util.function.Consumer;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import mekanism.api.datagen.recipe.builder.ItemStackChemicalToItemStackRecipeBuilder;
import mekanism.api.datagen.recipe.builder.ItemStackToChemicalRecipeBuilder;
import mekanism.api.datagen.recipe.builder.ItemStackToItemStackRecipeBuilder;
import mekanism.api.recipes.inputs.ItemStackIngredient;
import mekanism.api.recipes.inputs.chemical.InfusionStackIngredient;
import mekanism.api.text.EnumColor;
import mekanism.common.Mekanism;
import mekanism.common.recipe.BaseRecipeProvider;
import mekanism.common.recipe.RecipeProviderUtil;
import mekanism.common.recipe.impl.PigmentExtractingRecipeProvider;
import mekanism.common.registries.MekanismPigments;
import mekanism.common.tags.MekanismTags;
import net.minecraft.block.Blocks;
import net.minecraft.data.IFinishedRecipe;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.IItemProvider;

@ParametersAreNonnullByDefault
public class BYGRecipeProvider extends CompatRecipeProvider {

    public BYGRecipeProvider() {
        super("byg");
    }

    @Override
    protected void registerRecipes(Consumer<IFinishedRecipe> consumer, String basePath) {
        addDyeRecipes(consumer, basePath);
        addCrushingRecipes(consumer, basePath + "crushing/");
        addEnrichingRecipes(consumer, basePath + "enriching/");
        addMetallurgicInfusingRecipes(consumer, basePath + "metallurgic_infusing/");
        addPrecisionSawmillRecipes(consumer, basePath + "sawing/");
        addSandRecipes(consumer, basePath + "sandstone_to_sand/");
        //TODO: Bio-fuel recipes?
    }

    private void addPrecisionSawmillRecipes(Consumer<IFinishedRecipe> consumer, String basePath) {
        addPrecisionSawmillWoodTypeRecipes(consumer, basePath, BYGBlocks.ASPEN_PLANKS, BYGItems.ASPEN_BOAT, BYGBlocks.ASPEN_DOOR, BYGBlocks.ASPEN_FENCE_GATE,
              BYGBlocks.ASPEN_PRESSURE_PLATE, BYGBlocks.ASPEN_TRAPDOOR, "aspen");
        addPrecisionSawmillWoodTypeRecipes(consumer, basePath, BYGBlocks.BAOBAB_PLANKS, BYGItems.BAOBAB_BOAT, BYGBlocks.BAOBAB_DOOR, BYGBlocks.BAOBAB_FENCE_GATE,
              BYGBlocks.BAOBAB_PRESSURE_PLATE, BYGBlocks.BAOBAB_TRAPDOOR, "baobab");
        addPrecisionSawmillWoodTypeRecipes(consumer, basePath, BYGBlocks.BLUE_ENCHANTED_PLANKS, BYGItems.BLUE_ENCHANTED_BOAT, BYGBlocks.BLUE_ENCHANTED_DOOR,
              BYGBlocks.BLUE_ENCHANTED_FENCE_GATE, BYGBlocks.BLUE_ENCHANTED_PRESSURE_PLATE, BYGBlocks.BLUE_ENCHANTED_TRAPDOOR, "blue_enchanted");
        addPrecisionSawmillWoodTypeRecipes(consumer, basePath, BYGBlocks.BULBIS_PLANKS, null, BYGBlocks.BULBIS_DOOR, BYGBlocks.BULBIS_FENCE_GATE,
              BYGBlocks.BULBIS_PRESSURE_PLATE, BYGBlocks.BULBIS_TRAPDOOR, "bulbis", "stems");
        addPrecisionSawmillWoodTypeRecipes(consumer, basePath, BYGBlocks.CHERRY_PLANKS, BYGItems.CHERRY_BOAT, BYGBlocks.CHERRY_DOOR, BYGBlocks.CHERRY_FENCE_GATE,
              BYGBlocks.CHERRY_PRESSURE_PLATE, BYGBlocks.CHERRY_TRAPDOOR, "cherry");
        addPrecisionSawmillWoodTypeRecipes(consumer, basePath, BYGBlocks.CIKA_PLANKS, BYGItems.CIKA_BOAT, BYGBlocks.CIKA_DOOR, BYGBlocks.CIKA_FENCE_GATE,
              BYGBlocks.CIKA_PRESSURE_PLATE, BYGBlocks.CIKA_TRAPDOOR, "cika");
        addPrecisionSawmillWoodTypeRecipes(consumer, basePath, BYGBlocks.CYPRESS_PLANKS, BYGItems.CYPRESS_BOAT, BYGBlocks.CYPRESS_DOOR, BYGBlocks.CYPRESS_FENCE_GATE,
              BYGBlocks.CYPRESS_PRESSURE_PLATE, BYGBlocks.CYPRESS_TRAPDOOR, "cypress");
        addPrecisionSawmillWoodTypeRecipes(consumer, basePath, BYGBlocks.EBONY_PLANKS, BYGItems.EBONY_BOAT, BYGBlocks.EBONY_DOOR, BYGBlocks.EBONY_FENCE_GATE,
              BYGBlocks.EBONY_PRESSURE_PLATE, BYGBlocks.EBONY_TRAPDOOR, "ebony");
        addPrecisionSawmillWoodTypeRecipes(consumer, basePath, BYGBlocks.EMBUR_PLANKS, null, BYGBlocks.EMBUR_DOOR, BYGBlocks.EMBUR_FENCE_GATE,
              BYGBlocks.EMBUR_PRESSURE_PLATE, BYGBlocks.EMBUR_TRAPDOOR, "embur", "pedus");
        addPrecisionSawmillWoodTypeRecipes(consumer, basePath, BYGBlocks.ETHER_PLANKS, null, BYGBlocks.ETHER_DOOR, BYGBlocks.ETHER_FENCE_GATE,
              BYGBlocks.ETHER_PRESSURE_PLATE, BYGBlocks.ETHER_TRAPDOOR, "ether");
        addPrecisionSawmillWoodTypeRecipes(consumer, basePath, BYGBlocks.FIR_PLANKS, BYGItems.FIR_BOAT, BYGBlocks.FIR_DOOR, BYGBlocks.FIR_FENCE_GATE,
              BYGBlocks.FIR_PRESSURE_PLATE, BYGBlocks.FIR_TRAPDOOR, "fir");
        addPrecisionSawmillWoodTypeRecipes(consumer, basePath, BYGBlocks.GREEN_ENCHANTED_PLANKS, BYGItems.GREEN_ENCHANTED_BOAT, BYGBlocks.GREEN_ENCHANTED_DOOR,
              BYGBlocks.GREEN_ENCHANTED_FENCE_GATE, BYGBlocks.GREEN_ENCHANTED_PRESSURE_PLATE, BYGBlocks.GREEN_ENCHANTED_TRAPDOOR, "green_enchanted");
        addPrecisionSawmillWoodTypeRecipes(consumer, basePath, BYGBlocks.HOLLY_PLANKS, BYGItems.HOLLY_BOAT, BYGBlocks.HOLLY_DOOR, BYGBlocks.HOLLY_FENCE_GATE,
              BYGBlocks.HOLLY_PRESSURE_PLATE, BYGBlocks.HOLLY_TRAPDOOR, "holly");
        addPrecisionSawmillWoodTypeRecipes(consumer, basePath, BYGBlocks.IMPARIUS_PLANKS, null, BYGBlocks.IMPARIUS_DOOR, BYGBlocks.IMPARIUS_FENCE_GATE,
              BYGBlocks.IMPARIUS_PRESSURE_PLATE, BYGBlocks.IMPARIUS_TRAPDOOR, "imparius", "stems");
        addPrecisionSawmillWoodTypeRecipes(consumer, basePath, BYGBlocks.JACARANDA_PLANKS, BYGItems.JACARANDA_BOAT, BYGBlocks.JACARANDA_DOOR,
              BYGBlocks.JACARANDA_FENCE_GATE, BYGBlocks.JACARANDA_PRESSURE_PLATE, BYGBlocks.JACARANDA_TRAPDOOR, "jacaranda");
        addPrecisionSawmillWoodTypeRecipes(consumer, basePath, BYGBlocks.LAMENT_PLANKS, null, BYGBlocks.LAMENT_DOOR, BYGBlocks.LAMENT_FENCE_GATE,
              BYGBlocks.LAMENT_PRESSURE_PLATE, BYGBlocks.LAMENT_TRAPDOOR, "lament");
        addPrecisionSawmillWoodTypeRecipes(consumer, basePath, BYGBlocks.MAHOGANY_PLANKS, BYGItems.MAHOGANY_BOAT, BYGBlocks.MAHOGANY_DOOR, BYGBlocks.MAHOGANY_FENCE_GATE,
              BYGBlocks.MAHOGANY_PRESSURE_PLATE, BYGBlocks.MAHOGANY_TRAPDOOR, "mahogany");
        addPrecisionSawmillWoodTypeRecipes(consumer, basePath, BYGBlocks.MANGROVE_PLANKS, BYGItems.MANGROVE_BOAT, BYGBlocks.MANGROVE_DOOR, BYGBlocks.MANGROVE_FENCE_GATE,
              BYGBlocks.MANGROVE_PRESSURE_PLATE, BYGBlocks.MANGROVE_TRAPDOOR, "mangrove");
        addPrecisionSawmillWoodTypeRecipes(consumer, basePath, BYGBlocks.MAPLE_PLANKS, BYGItems.MAPLE_BOAT, BYGBlocks.MAPLE_DOOR, BYGBlocks.MAPLE_FENCE_GATE,
              BYGBlocks.MAPLE_PRESSURE_PLATE, BYGBlocks.MAPLE_TRAPDOOR, "maple");
        addPrecisionSawmillWoodTypeRecipes(consumer, basePath, BYGBlocks.NIGHTSHADE_PLANKS, null, BYGBlocks.NIGHTSHADE_DOOR, BYGBlocks.NIGHTSHADE_FENCE_GATE,
              BYGBlocks.NIGHTSHADE_PRESSURE_PLATE, BYGBlocks.NIGHTSHADE_TRAPDOOR, "nightshade");
        addPrecisionSawmillWoodTypeRecipes(consumer, basePath, BYGBlocks.PALM_PLANKS, BYGItems.PALM_BOAT, BYGBlocks.PALM_DOOR, BYGBlocks.PALM_FENCE_GATE,
              BYGBlocks.PALM_PRESSURE_PLATE, BYGBlocks.PALM_TRAPDOOR, "palm");
        addPrecisionSawmillWoodTypeRecipes(consumer, basePath, BYGBlocks.PINE_PLANKS, BYGItems.PINE_BOAT, BYGBlocks.PINE_DOOR, BYGBlocks.PINE_FENCE_GATE,
              BYGBlocks.PINE_PRESSURE_PLATE, BYGBlocks.PINE_TRAPDOOR, "pine");
        addPrecisionSawmillWoodTypeRecipes(consumer, basePath, BYGBlocks.RAINBOW_EUCALYPTUS_PLANKS, BYGItems.RAINBOW_EUCALYPTUS_BOAT, BYGBlocks.RAINBOW_EUCALYPTUS_DOOR,
              BYGBlocks.RAINBOW_EUCALYPTUS_FENCE_GATE, BYGBlocks.RAINBOW_EUCALYPTUS_PRESSURE_PLATE, BYGBlocks.RAINBOW_EUCALYPTUS_TRAPDOOR, "rainbow_eucalyptus");
        addPrecisionSawmillWoodTypeRecipes(consumer, basePath, BYGBlocks.REDWOOD_PLANKS, BYGItems.REDWOOD_BOAT, BYGBlocks.REDWOOD_DOOR, BYGBlocks.REDWOOD_FENCE_GATE,
              BYGBlocks.REDWOOD_PRESSURE_PLATE, BYGBlocks.REDWOOD_TRAPDOOR, "redwood");
        addPrecisionSawmillWoodTypeRecipes(consumer, basePath, BYGBlocks.SKYRIS_PLANKS, BYGItems.SKYRIS_BOAT, BYGBlocks.SKYRIS_DOOR, BYGBlocks.SKYRIS_FENCE_GATE,
              BYGBlocks.SKYRIS_PRESSURE_PLATE, BYGBlocks.SKYRIS_TRAPDOOR, "skyris");
        addPrecisionSawmillWoodTypeRecipes(consumer, basePath, BYGBlocks.SYTHIAN_PLANKS, null, BYGBlocks.SYTHIAN_DOOR, BYGBlocks.SYTHIAN_FENCE_GATE,
              BYGBlocks.SYTHIAN_PRESSURE_PLATE, BYGBlocks.SYTHIAN_TRAPDOOR, "sythian", "stems");
        addPrecisionSawmillWoodTypeRecipes(consumer, basePath, BYGBlocks.WILLOW_PLANKS, BYGItems.WILLOW_BOAT, BYGBlocks.WILLOW_DOOR, BYGBlocks.WILLOW_FENCE_GATE,
              BYGBlocks.WILLOW_PRESSURE_PLATE, BYGBlocks.WILLOW_TRAPDOOR, "willow");
        addPrecisionSawmillWoodTypeRecipes(consumer, basePath, BYGBlocks.WITCH_HAZEL_PLANKS, BYGItems.WITCH_HAZEL_BOAT, BYGBlocks.WITCH_HAZEL_DOOR,
              BYGBlocks.WITCH_HAZEL_FENCE_GATE, BYGBlocks.WITCH_HAZEL_PRESSURE_PLATE, BYGBlocks.WITCH_HAZEL_TRAPDOOR, "witch_hazel");
        addPrecisionSawmillWoodTypeRecipes(consumer, basePath, BYGBlocks.ZELKOVA_PLANKS, BYGItems.ZELKOVA_BOAT, BYGBlocks.ZELKOVA_DOOR, BYGBlocks.ZELKOVA_FENCE_GATE,
              BYGBlocks.ZELKOVA_PRESSURE_PLATE, BYGBlocks.ZELKOVA_TRAPDOOR, "zelkova");
    }

    private void addPrecisionSawmillWoodTypeRecipes(Consumer<IFinishedRecipe> consumer, String basePath, IItemProvider planks, @Nullable IItemProvider boat,
          IItemProvider door, IItemProvider fenceGate, IItemProvider pressurePlate, IItemProvider trapdoor, String name) {
        addPrecisionSawmillWoodTypeRecipes(consumer, basePath, planks, boat, door, fenceGate, pressurePlate, trapdoor, name, "logs");
    }

    private void addPrecisionSawmillWoodTypeRecipes(Consumer<IFinishedRecipe> consumer, String basePath, IItemProvider planks, @Nullable IItemProvider boat,
          IItemProvider door, IItemProvider fenceGate, IItemProvider pressurePlate, IItemProvider trapdoor, String name, String logTagType) {
        RecipeProviderUtil.addPrecisionSawmillWoodTypeRecipes(consumer, basePath, planks, boat, door, fenceGate, tag(name + "_" + logTagType), pressurePlate,
              trapdoor, name, modLoaded);
    }

    private void addSandRecipes(Consumer<IFinishedRecipe> consumer, String basePath) {
        //Black Sandstone -> Sand
        RecipeProviderUtil.addSandStoneToSandRecipe(consumer, basePath + "black", modLoaded, BYGBlocks.BLACK_SAND, BYGBlocks.BLACK_SANDSTONE,
              BYGBlocks.BLACK_CHISELED_SANDSTONE, BYGBlocks.BLACK_CUT_SANDSTONE, BYGBlocks.BLACK_SMOOTH_SANDSTONE);
        //Blue Sandstone -> Sand
        RecipeProviderUtil.addSandStoneToSandRecipe(consumer, basePath + "blue", modLoaded, BYGBlocks.BLUE_SAND, BYGBlocks.BLUE_SANDSTONE,
              BYGBlocks.BLUE_CHISELED_SANDSTONE, BYGBlocks.BLUE_CUT_SANDSTONE, BYGBlocks.BLUE_SMOOTH_SANDSTONE);
        //Pink Sandstone -> Sand
        RecipeProviderUtil.addSandStoneToSandRecipe(consumer, basePath + "pink", modLoaded, BYGBlocks.PINK_SAND, BYGBlocks.PINK_SANDSTONE,
              BYGBlocks.PINK_CHISELED_SANDSTONE, BYGBlocks.PINK_CUT_SANDSTONE, BYGBlocks.PINK_SMOOTH_SANDSTONE);
        //Purple Sandstone -> Sand
        RecipeProviderUtil.addSandStoneToSandRecipe(consumer, basePath + "purple", modLoaded, BYGBlocks.PURPLE_SAND, BYGBlocks.PURPLE_SANDSTONE,
              BYGBlocks.PURPLE_CHISELED_SANDSTONE, BYGBlocks.PURPLE_CUT_SANDSTONE, BYGBlocks.PURPLE_SMOOTH_SANDSTONE);
        //White Sandstone -> Sand
        RecipeProviderUtil.addSandStoneToSandRecipe(consumer, basePath + "white", modLoaded, BYGBlocks.WHITE_SAND, BYGBlocks.WHITE_SANDSTONE,
              BYGBlocks.WHITE_CHISELED_SANDSTONE, BYGBlocks.WHITE_CUT_SANDSTONE, BYGBlocks.WHITE_SMOOTH_SANDSTONE);
    }

    private void addDyeRecipes(Consumer<IFinishedRecipe> consumer, String basePath) {
        //Black
        dye(consumer, basePath, Items.BLACK_DYE, false, EnumColor.BLACK, "black_dye");
        //Blue
        dye(consumer, basePath, Items.BLUE_DYE, false, EnumColor.DARK_BLUE, "blue_dye");
        dye(consumer, basePath, Items.BLUE_DYE, true, EnumColor.DARK_BLUE, "double_blue_dye");
        //Brown
        dye(consumer, basePath, Items.BROWN_DYE, false, EnumColor.BROWN, "brown_dye");
        //Cyan
        dye(consumer, basePath, Items.CYAN_DYE, false, EnumColor.DARK_AQUA, "cyan_dye", BYGBlocks.WARPED_CACTUS);
        dye(consumer, basePath, Items.CYAN_DYE, true, EnumColor.DARK_AQUA, "double_cyan_dye");
        //Green
        dye(consumer, basePath, Items.GREEN_DYE, false, EnumColor.DARK_GREEN, "green_dye");
        //Light Blue
        dye(consumer, basePath, Items.LIGHT_BLUE_DYE, false, EnumColor.INDIGO, "light_blue_dye");
        //Light Gray
        dye(consumer, basePath, Items.LIGHT_GRAY_DYE, false, EnumColor.GRAY, "light_gray_dye");
        //Lime
        dye(consumer, basePath, Items.LIME_DYE, false, EnumColor.BRIGHT_GREEN, "lime_dye");
        //Magenta
        dye(consumer, basePath, Items.MAGENTA_DYE, false, EnumColor.PINK, "magenta_dye");
        //Orange
        dye(consumer, basePath, Items.ORANGE_DYE, false, EnumColor.ORANGE, "orange_dye");
        //Pink
        dye(consumer, basePath, Items.PINK_DYE, false, EnumColor.BRIGHT_PINK, "pink_dye");
        dye(consumer, basePath, Items.PINK_DYE, true, EnumColor.BRIGHT_PINK, "double_pink_dye");
        //Purple
        dye(consumer, basePath, Items.PURPLE_DYE, false, EnumColor.PURPLE, "purple_dye");
        dye(consumer, basePath, Items.PURPLE_DYE, true, EnumColor.PURPLE, "double_purple_dye");
        //Red
        dye(consumer, basePath, Items.RED_DYE, false, EnumColor.RED, "red_dye");
        //White
        dye(consumer, basePath, Items.WHITE_DYE, false, EnumColor.WHITE, "white_dye", BYGBlocks.ODDITY_CACTUS);
        //Yellow
        dye(consumer, basePath, Items.YELLOW_DYE, false, EnumColor.YELLOW, "yellow_dye");
    }

    private void dye(Consumer<IFinishedRecipe> consumer, String basePath, IItemProvider output, boolean large, EnumColor color, String inputTag,
          IItemProvider... extraInputs) {
        ItemStackIngredient inputIngredient = ItemStackIngredient.from(BaseRecipeProvider.createIngredient(
              tag(inputTag),
              extraInputs
        ));
        String name = large ? "large_" + color.getRegistryPrefix() : color.getRegistryPrefix();
        ItemStackToItemStackRecipeBuilder.enriching(
                    inputIngredient,
                    new ItemStack(output, large ? 4 : 2)
              ).addCondition(modLoaded)
              .build(consumer, Mekanism.rl(basePath + "dye/" + name));
        //Flowers -> 4x dye output (See PigmentExtractingRecipeProvider#addFlowerExtractionRecipes for note)
        long flowerRate = 3 * PigmentExtractingRecipeProvider.DYE_RATE;
        ItemStackToChemicalRecipeBuilder.pigmentExtracting(
                    inputIngredient,
                    MekanismPigments.PIGMENT_COLOR_LOOKUP.get(color).getStack(large ? 2 * flowerRate : flowerRate)
              ).addCondition(modLoaded)
              .build(consumer, Mekanism.rl(basePath + "pigment_extracting/" + name));
    }

    private void addCrushingRecipes(Consumer<IFinishedRecipe> consumer, String basePath) {
        addCrusherDaciteRecipes(consumer, basePath + "dacite/");
        addCrusherEtherRecipes(consumer, basePath + "ether/");
        addCrusherRedRockRecipes(consumer, basePath + "red_rock/");
        addCrusherScoriaRecipes(consumer, basePath + "scoria/");
        addCrusherSoapstoneRecipes(consumer, basePath + "soapstone/");
        addCrusherTravertineRecipes(consumer, basePath + "travertine/");
    }

    private void addCrusherDaciteRecipes(Consumer<IFinishedRecipe> consumer, String basePath) {
        //Dacite -> Dacite Cobblestone
        crushing(consumer, BYGBlocks.DACITE, BYGBlocks.DACITE_COBBLESTONE, basePath + "to_cobblestone");
        crushing(consumer, BYGBlocks.DACITE_SLAB, BYGBlocks.DACITE_COBBLESTONE_SLAB, basePath + "slabs_to_cobblestone_slabs");
        crushing(consumer, BYGBlocks.DACITE_STAIRS, BYGBlocks.DACITE_COBBLESTONE_STAIRS, basePath + "stairs_to_cobblestone_stairs");
        crushing(consumer, BYGBlocks.DACITE_WALL, BYGBlocks.DACITE_COBBLESTONE_WALL, basePath + "walls_to_cobblestone_walls");
        //Dacite Tile -> Dacite Bricks
        crushing(consumer, BYGBlocks.DACITE_TILE, BYGBlocks.DACITE_BRICKS, basePath + "tile_to_brick");
        crushing(consumer, BYGBlocks.DACITE_TILE_SLAB, BYGBlocks.DACITE_BRICK_SLAB, basePath + "tile_slabs_to_brick_slabs");
        crushing(consumer, BYGBlocks.DACITE_TILE_STAIRS, BYGBlocks.DACITE_BRICK_STAIRS, basePath + "tile_stairs_to_brick_stairs");
        crushing(consumer, BYGBlocks.DACITE_TILE_WALL, BYGBlocks.DACITE_BRICK_WALL, basePath + "tile_walls_to_brick_walls");
        //Dacite Bricks -> Dacite
        crushing(consumer, BYGBlocks.DACITE_BRICKS, BYGBlocks.DACITE, basePath + "from_brick");
        crushing(consumer, BYGBlocks.DACITE_BRICK_SLAB, BYGBlocks.DACITE_SLAB, basePath + "brick_slabs_to_slabs");
        crushing(consumer, BYGBlocks.DACITE_BRICK_STAIRS, BYGBlocks.DACITE_STAIRS, basePath + "brick_stairs_to_stairs");
        crushing(consumer, BYGBlocks.DACITE_BRICK_WALL, BYGBlocks.DACITE_WALL, basePath + "brick_walls_to_walls");
        //Dacite Pillar -> Dacite
        ItemStackToItemStackRecipeBuilder.crushing(
                    ItemStackIngredient.from(BYGBlocks.DACITE_PILLAR),
                    new ItemStack(BYGBlocks.DACITE, 2)
              ).addCondition(modLoaded)
              .build(consumer, Mekanism.rl(basePath + "from_pillar"));
    }

    private void addCrusherEtherRecipes(Consumer<IFinishedRecipe> consumer, String basePath) {
        //Ether -> Cobbled Ether
        crushing(consumer, BYGBlocks.ETHER_STONE, BYGBlocks.COBBLED_ETHER_STONE, basePath + "to_cobblestone");
        crushing(consumer, BYGBlocks.ETHER_STONE_SLAB, BYGBlocks.COBBLED_ETHER_STONE_SLAB, basePath + "slabs_to_cobblestone_slabs");
        crushing(consumer, BYGBlocks.ETHER_STONE_STAIRS, BYGBlocks.COBBLED_ETHER_STONE_STAIRS, basePath + "stairs_to_cobblestone_stairs");
        crushing(consumer, BYGBlocks.ETHER_STONE_WALL, BYGBlocks.COBBLED_ETHER_STONE_WALL, basePath + "walls_to_cobblestone_walls");
        //Carved Ether -> Ether
        crushing(consumer, BYGBlocks.CARVED_ETHER_STONE, BYGBlocks.ETHER_STONE, basePath + "from_carved");
        crushing(consumer, BYGBlocks.CARVED_ETHER_STONE_SLAB, BYGBlocks.ETHER_STONE_SLAB, basePath + "carved_slabs_to_slabs");
        crushing(consumer, BYGBlocks.CARVED_ETHER_STONE_STAIRS, BYGBlocks.ETHER_STONE_STAIRS, basePath + "carved_stairs_to_stairs");
        crushing(consumer, BYGBlocks.CARVED_ETHER_STONE_WALL, BYGBlocks.ETHER_STONE_WALL, basePath + "carved_walls_to_walls");
    }

    private void addCrusherRedRockRecipes(Consumer<IFinishedRecipe> consumer, String basePath) {
        //Chiseled Red Rock -> Red Rock Bricks
        crushing(consumer, BYGBlocks.CHISELED_RED_ROCK_BRICKS, BYGBlocks.RED_ROCK_BRICKS, basePath + "chiseled_to_brick");
        crushing(consumer, BYGBlocks.CHISELED_RED_ROCK_BRICK_SLAB, BYGBlocks.RED_ROCK_BRICK_SLAB, basePath + "chiseled_slabs_to_brick_slabs");
        crushing(consumer, BYGBlocks.CHISELED_RED_ROCK_BRICK_STAIRS, BYGBlocks.RED_ROCK_BRICK_STAIRS, basePath + "chiseled_stairs_to_brick_stairs");
        crushing(consumer, BYGBlocks.CHISELED_RED_ROCK_BRICK_WALL, BYGBlocks.RED_ROCK_BRICK_WALL, basePath + "chiseled_walls_to_brick_walls");
        //Red Rock Bricks -> Cracked Red Rock Bricks
        crushing(consumer, BYGBlocks.RED_ROCK_BRICKS, BYGBlocks.CRACKED_RED_ROCK_BRICKS, basePath + "bricks_to_cracked_bricks");
        crushing(consumer, BYGBlocks.RED_ROCK_BRICK_SLAB, BYGBlocks.CRACKED_RED_ROCK_BRICK_SLAB, basePath + "brick_slabs_to_cracked_brick_slabs");
        crushing(consumer, BYGBlocks.RED_ROCK_BRICK_STAIRS, BYGBlocks.CRACKED_RED_ROCK_BRICK_STAIRS, basePath + "brick_stairs_to_cracked_brick_stairs");
        crushing(consumer, BYGBlocks.RED_ROCK_BRICK_WALL, BYGBlocks.CRACKED_RED_ROCK_BRICK_WALL, basePath + "brick_walls_to_cracked_brick_walls");
        //Cracked Red Rock Bricks -> Red Rock
        crushing(consumer, BYGBlocks.CRACKED_RED_ROCK_BRICKS, BYGBlocks.RED_ROCK, basePath + "from_cracked_bricks");
        crushing(consumer, BYGBlocks.CRACKED_RED_ROCK_BRICK_SLAB, BYGBlocks.RED_ROCK_SLAB, basePath + "brick_slabs_to_slabs");
        crushing(consumer, BYGBlocks.CRACKED_RED_ROCK_BRICK_STAIRS, BYGBlocks.RED_ROCK_STAIRS, basePath + "brick_stairs_to_stairs");
        crushing(consumer, BYGBlocks.CRACKED_RED_ROCK_BRICK_WALL, BYGBlocks.RED_ROCK_WALL, basePath + "brick_walls_to_walls");
    }

    private void addCrusherScoriaRecipes(Consumer<IFinishedRecipe> consumer, String basePath) {
        //Scoria -> Scoria Cobblestone
        crushing(consumer, BYGBlocks.SCORIA_STONE, BYGBlocks.SCORIA_COBBLESTONE, basePath + "to_cobblestone");
        crushing(consumer, BYGBlocks.SCORIA_SLAB, BYGBlocks.SCORIA_COBBLESTONE_SLAB, basePath + "slabs_to_cobblestone_slabs");
        crushing(consumer, BYGBlocks.SCORIA_STAIRS, BYGBlocks.SCORIA_COBBLESTONE_STAIRS, basePath + "stairs_to_cobblestone_stairs");
        crushing(consumer, BYGBlocks.SCORIA_WALL, BYGBlocks.SCORIA_COBBLESTONE_WALL, basePath + "walls_to_cobblestone_walls");
        //Scoria Stone Bricks -> Cracked Scoria Stone Bricks
        crushing(consumer, BYGBlocks.SCORIA_STONEBRICKS, BYGBlocks.CRACKED_SCORIA_STONE_BRICKS, basePath + "bricks_to_cracked_bricks");
        //Cracked Scoria Stone Bricks -> Scoria
        crushing(consumer, BYGBlocks.CRACKED_SCORIA_STONE_BRICKS, BYGBlocks.SCORIA_STONE, basePath + "from_cracked_bricks");
        //Scoria Stone Bricks -> Scoria
        crushing(consumer, BYGBlocks.SCORIA_STONEBRICK_SLAB, BYGBlocks.SCORIA_SLAB, basePath + "brick_slabs_to_slabs");
        crushing(consumer, BYGBlocks.SCORIA_STONEBRICK_STAIRS, BYGBlocks.SCORIA_STAIRS, basePath + "brick_stairs_to_stairs");
        crushing(consumer, BYGBlocks.SCORIA_STONEBRICK_WALL, BYGBlocks.SCORIA_WALL, basePath + "brick_walls_to_walls");
        //Scoria Pillar -> Scoria
        ItemStackToItemStackRecipeBuilder.crushing(
                    ItemStackIngredient.from(BYGBlocks.SCORIA_PILLAR),
                    new ItemStack(BYGBlocks.SCORIA_STONE, 2)
              ).addCondition(modLoaded)
              .build(consumer, Mekanism.rl(basePath + "from_pillar"));
    }

    private void addCrusherSoapstoneRecipes(Consumer<IFinishedRecipe> consumer, String basePath) {
        //Polished Soapstone -> Soapstone
        crushing(consumer, BYGBlocks.POLISHED_SOAPSTONE, BYGBlocks.SOAPSTONE, basePath + "from_polished");
        crushing(consumer, BYGBlocks.POLISHED_SOAPSTONE_SLAB, BYGBlocks.SOAPSTONE_SLAB, basePath + "polished_slabs_to_slabs");
        crushing(consumer, BYGBlocks.POLISHED_SOAPSTONE_STAIRS, BYGBlocks.SOAPSTONE_STAIRS, basePath + "polished_stairs_to_stairs");
        crushing(consumer, BYGBlocks.POLISHED_SOAPSTONE_WALL, BYGBlocks.SOAPSTONE_WALL, basePath + "polished_walls_to_walls");
        //Soapstone Bricks -> Polished Soapstone
        crushing(consumer, BYGBlocks.SOAPSTONE_BRICKS, BYGBlocks.POLISHED_SOAPSTONE, basePath + "brick_to_polished");
        crushing(consumer, BYGBlocks.SOAPSTONE_BRICK_SLAB, BYGBlocks.POLISHED_SOAPSTONE_SLAB, basePath + "brick_slabs_to_polished_slabs");
        crushing(consumer, BYGBlocks.SOAPSTONE_BRICK_STAIRS, BYGBlocks.POLISHED_SOAPSTONE_STAIRS, basePath + "brick_stairs_to_polished_stairs");
        crushing(consumer, BYGBlocks.SOAPSTONE_BRICK_WALL, BYGBlocks.POLISHED_SOAPSTONE_WALL, basePath + "brick_walls_to_polished_walls");
        //Soapstone Tile -> Soapstone Bricks
        crushing(consumer, BYGBlocks.SOAPSTONE_TILE, BYGBlocks.SOAPSTONE_BRICKS, basePath + "tile_to_brick");
        crushing(consumer, BYGBlocks.SOAPSTONE_TILE_SLAB, BYGBlocks.SOAPSTONE_BRICK_SLAB, basePath + "tile_slabs_to_brick_slabs");
        crushing(consumer, BYGBlocks.SOAPSTONE_TILE_STAIRS, BYGBlocks.SOAPSTONE_BRICK_STAIRS, basePath + "tile_stairs_to_brick_stairs");
        crushing(consumer, BYGBlocks.SOAPSTONE_TILE_WALL, BYGBlocks.SOAPSTONE_BRICK_WALL, basePath + "tile_walls_to_brick_walls");
        //Soapstone Pillar -> Soapstone
        ItemStackToItemStackRecipeBuilder.crushing(
                    ItemStackIngredient.from(BYGBlocks.SOAPSTONE_PILLAR),
                    new ItemStack(BYGBlocks.SOAPSTONE, 2)
              ).addCondition(modLoaded)
              .build(consumer, Mekanism.rl(basePath + "from_pillar"));
    }

    private void addCrusherTravertineRecipes(Consumer<IFinishedRecipe> consumer, String basePath) {
        //Polished Travertine -> Travertine
        crushing(consumer, BYGBlocks.POLISHED_TRAVERTINE, BYGBlocks.TRAVERTINE, basePath + "from_polished");
        crushing(consumer, BYGBlocks.POLISHED_TRAVERTINE_SLAB, BYGBlocks.TRAVERTINE_SLAB, basePath + "polished_slabs_to_slabs");
        crushing(consumer, BYGBlocks.POLISHED_TRAVERTINE_STAIRS, BYGBlocks.TRAVERTINE_STAIRS, basePath + "polished_stairs_to_stairs");
        crushing(consumer, BYGBlocks.POLISHED_TRAVERTINE_WALL, BYGBlocks.TRAVERTINE_WALL, basePath + "polished_walls_to_walls");
        //Chiseled Travertine -> Polished Travertine
        crushing(consumer, BYGBlocks.CHISELED_TRAVERTINE, BYGBlocks.POLISHED_TRAVERTINE, basePath + "chiseled_to_polished");
        crushing(consumer, BYGBlocks.CHISELED_TRAVERTINE_SLAB, BYGBlocks.POLISHED_TRAVERTINE_SLAB, basePath + "chiseled_slabs_to_polished_slabs");
        crushing(consumer, BYGBlocks.CHISELED_TRAVERTINE_STAIRS, BYGBlocks.POLISHED_TRAVERTINE_STAIRS, basePath + "chiseled_stairs_to_polished_stairs");
        crushing(consumer, BYGBlocks.CHISELED_TRAVERTINE_WALL, BYGBlocks.POLISHED_TRAVERTINE_WALL, basePath + "chiseled_walls_to_polished_walls");
    }

    private void crushing(Consumer<IFinishedRecipe> consumer, IItemProvider input, IItemProvider output, String path) {
        ItemStackToItemStackRecipeBuilder.crushing(
                    ItemStackIngredient.from(input),
                    new ItemStack(output)
              ).addCondition(modLoaded)
              .build(consumer, Mekanism.rl(path));
    }

    private void addEnrichingRecipes(Consumer<IFinishedRecipe> consumer, String basePath) {
        addMossyStoneEnrichingRecipes(consumer, basePath + "mossy_stone/");
        addDaciteEnrichingRecipes(consumer, basePath + "dacite/");
        addEtherEnrichingRecipes(consumer, basePath + "ether/");
        addRedRockEnrichingRecipes(consumer, basePath + "red_rock/");
        addScoriaEnrichingRecipes(consumer, basePath + "scoria/");
        addSoapstoneEnrichingRecipes(consumer, basePath + "soapstone/");
        addTravertineEnrichingRecipes(consumer, basePath + "travertine/");
    }

    private void addMossyStoneEnrichingRecipes(Consumer<IFinishedRecipe> consumer, String basePath) {
        enriching(consumer, BYGBlocks.MOSSY_STONE, Blocks.STONE, basePath + "stone");
        enriching(consumer, BYGBlocks.MOSSY_STONE_SLAB, Blocks.STONE_SLAB, basePath + "slabs");
        enriching(consumer, BYGBlocks.MOSSY_STONE_STAIRS, Blocks.STONE_STAIRS, basePath + "stairs");
    }

    private void addDaciteEnrichingRecipes(Consumer<IFinishedRecipe> consumer, String basePath) {
        //Dacite Bricks -> Dacite Tile
        enriching(consumer, BYGBlocks.DACITE_BRICKS, BYGBlocks.DACITE_TILE, basePath + "brick_to_tile");
        enriching(consumer, BYGBlocks.DACITE_BRICK_SLAB, BYGBlocks.DACITE_TILE_SLAB, basePath + "brick_slabs_to_tile_slabs");
        enriching(consumer, BYGBlocks.DACITE_BRICK_STAIRS, BYGBlocks.DACITE_TILE_STAIRS, basePath + "brick_stairs_to_tile_stairs");
        enriching(consumer, BYGBlocks.DACITE_BRICK_WALL, BYGBlocks.DACITE_TILE_WALL, basePath + "brick_walls_to_tile_walls");
        //Dacite -> Dacite Bricks
        enriching(consumer, BYGBlocks.DACITE, BYGBlocks.DACITE_BRICKS, basePath + "to_brick");
        enriching(consumer, BYGBlocks.DACITE_SLAB, BYGBlocks.DACITE_BRICK_SLAB, basePath + "slabs_to_brick_slabs");
        enriching(consumer, BYGBlocks.DACITE_STAIRS, BYGBlocks.DACITE_BRICK_STAIRS, basePath + "stairs_to_brick_stairs");
        enriching(consumer, BYGBlocks.DACITE_WALL, BYGBlocks.DACITE_BRICK_WALL, basePath + "walls_to_brick_walls");
    }

    private void addEtherEnrichingRecipes(Consumer<IFinishedRecipe> consumer, String basePath) {
        //Ether -> Carved Ether
        enriching(consumer, BYGBlocks.ETHER_STONE, BYGBlocks.CARVED_ETHER_STONE, basePath + "to_carved");
        enriching(consumer, BYGBlocks.ETHER_STONE_SLAB, BYGBlocks.CARVED_ETHER_STONE_SLAB, basePath + "slabs_to_carved_slabs");
        enriching(consumer, BYGBlocks.ETHER_STONE_STAIRS, BYGBlocks.CARVED_ETHER_STONE_STAIRS, basePath + "stairs_to_carved_stairs");
        enriching(consumer, BYGBlocks.ETHER_STONE_WALL, BYGBlocks.CARVED_ETHER_STONE_WALL, basePath + "walls_to_carved_walls");
    }

    private void addRedRockEnrichingRecipes(Consumer<IFinishedRecipe> consumer, String basePath) {
        //Red Rock -> Cracked Red Rock Bricks
        enriching(consumer, BYGBlocks.RED_ROCK, BYGBlocks.CRACKED_RED_ROCK_BRICKS, basePath + "to_cracked_bricks");
        enriching(consumer, BYGBlocks.RED_ROCK_SLAB, BYGBlocks.CRACKED_RED_ROCK_BRICK_SLAB, basePath + "slabs_to_brick_slabs");
        enriching(consumer, BYGBlocks.RED_ROCK_STAIRS, BYGBlocks.CRACKED_RED_ROCK_BRICK_STAIRS, basePath + "stairs_to_brick_stairs");
        enriching(consumer, BYGBlocks.RED_ROCK_WALL, BYGBlocks.CRACKED_RED_ROCK_BRICK_WALL, basePath + "walls_to_brick_walls");
        //Cracked Red Rock Bricks -> Red Rock Bricks
        enriching(consumer, BYGBlocks.CRACKED_RED_ROCK_BRICKS, BYGBlocks.RED_ROCK_BRICKS, basePath + "cracked_bricks_to_bricks");
        enriching(consumer, BYGBlocks.CRACKED_RED_ROCK_BRICK_SLAB, BYGBlocks.RED_ROCK_BRICK_SLAB, basePath + "cracked_brick_slabs_to_brick_slabs");
        enriching(consumer, BYGBlocks.CRACKED_RED_ROCK_BRICK_STAIRS, BYGBlocks.RED_ROCK_BRICK_STAIRS, basePath + "cracked_brick_stairs_to_brick_stairs");
        enriching(consumer, BYGBlocks.CRACKED_RED_ROCK_BRICK_WALL, BYGBlocks.RED_ROCK_BRICK_WALL, basePath + "cracked_brick_walls_to_brick_walls");
        //Red Rock Bricks -> Chiseled Red Rock
        enriching(consumer, BYGBlocks.RED_ROCK_BRICKS, BYGBlocks.CHISELED_RED_ROCK_BRICKS, basePath + "brick_to_chiseled");
        enriching(consumer, BYGBlocks.RED_ROCK_BRICK_SLAB, BYGBlocks.CHISELED_RED_ROCK_BRICK_SLAB, basePath + "brick_slabs_to_chiseled_slabs");
        enriching(consumer, BYGBlocks.RED_ROCK_BRICK_STAIRS, BYGBlocks.CHISELED_RED_ROCK_BRICK_STAIRS, basePath + "brick_stairs_to_chiseled_stairs");
        enriching(consumer, BYGBlocks.RED_ROCK_BRICK_WALL, BYGBlocks.CHISELED_RED_ROCK_BRICK_WALL, basePath + "brick_walls_to_chiseled_walls");
        //Mossy Red Rock Bricks -> Red Rock Bricks
        enriching(consumer, BYGBlocks.MOSSY_RED_ROCK_BRICKS, BYGBlocks.RED_ROCK_BRICKS, basePath + "chiseled_to_brick");
        enriching(consumer, BYGBlocks.MOSSY_RED_ROCK_BRICK_SLAB, BYGBlocks.RED_ROCK_BRICK_SLAB, basePath + "chiseled_slabs_to_brick_slabs");
        enriching(consumer, BYGBlocks.MOSSY_RED_ROCK_BRICK_STAIRS, BYGBlocks.RED_ROCK_BRICK_STAIRS, basePath + "chiseled_stairs_to_brick_stairs");
        enriching(consumer, BYGBlocks.MOSSY_RED_ROCK_BRICK_WALL, BYGBlocks.RED_ROCK_BRICK_WALL, basePath + "chiseled_walls_to_brick_walls");
    }

    private void addScoriaEnrichingRecipes(Consumer<IFinishedRecipe> consumer, String basePath) {
        //Cracked Scoria Stone Bricks -> Scoria Stone Bricks
        enriching(consumer, BYGBlocks.CRACKED_SCORIA_STONE_BRICKS, BYGBlocks.SCORIA_STONEBRICKS, basePath + "cracked_bricks_to_bricks");
        //Scoria -> Cracked Scoria Stone Bricks
        enriching(consumer, BYGBlocks.SCORIA_STONE, BYGBlocks.CRACKED_SCORIA_STONE_BRICKS, basePath + "to_cracked_bricks");
        //Scoria -> Scoria Stone Bricks
        enriching(consumer, BYGBlocks.SCORIA_SLAB, BYGBlocks.SCORIA_STONEBRICK_SLAB, basePath + "slabs_to_brick_slabs");
        enriching(consumer, BYGBlocks.SCORIA_STAIRS, BYGBlocks.SCORIA_STONEBRICK_STAIRS, basePath + "stairs_to_brick_stairs");
        enriching(consumer, BYGBlocks.SCORIA_WALL, BYGBlocks.SCORIA_STONEBRICK_WALL, basePath + "walls_to_brick_walls");
    }

    private void addSoapstoneEnrichingRecipes(Consumer<IFinishedRecipe> consumer, String basePath) {
        //Soapstone -> Polished Soapstone
        enriching(consumer, BYGBlocks.SOAPSTONE, BYGBlocks.POLISHED_SOAPSTONE, basePath + "to_polished");
        enriching(consumer, BYGBlocks.SOAPSTONE_SLAB, BYGBlocks.POLISHED_SOAPSTONE_SLAB, basePath + "slabs_to_polished_slabs");
        enriching(consumer, BYGBlocks.SOAPSTONE_STAIRS, BYGBlocks.POLISHED_SOAPSTONE_STAIRS, basePath + "stairs_to_polished_stairs");
        enriching(consumer, BYGBlocks.SOAPSTONE_WALL, BYGBlocks.POLISHED_SOAPSTONE_WALL, basePath + "walls_to_polished_walls");
        //Polished Soapstone -> Soapstone Bricks
        enriching(consumer, BYGBlocks.POLISHED_SOAPSTONE, BYGBlocks.SOAPSTONE_BRICKS, basePath + "polished_to_brick");
        enriching(consumer, BYGBlocks.POLISHED_SOAPSTONE_SLAB, BYGBlocks.SOAPSTONE_BRICK_SLAB, basePath + "polished_slabs_to_brick_slabs");
        enriching(consumer, BYGBlocks.POLISHED_SOAPSTONE_STAIRS, BYGBlocks.SOAPSTONE_BRICK_STAIRS, basePath + "polished_stairs_to_brick_stairs");
        enriching(consumer, BYGBlocks.POLISHED_SOAPSTONE_WALL, BYGBlocks.SOAPSTONE_BRICK_WALL, basePath + "polished_walls_to_brick_walls");
        //Soapstone Bricks -> Soapstone Tile
        enriching(consumer, BYGBlocks.SOAPSTONE_BRICKS, BYGBlocks.SOAPSTONE_TILE, basePath + "brick_to_tile");
        enriching(consumer, BYGBlocks.SOAPSTONE_BRICK_SLAB, BYGBlocks.SOAPSTONE_TILE_SLAB, basePath + "brick_slabs_to_tile_slabs");
        enriching(consumer, BYGBlocks.SOAPSTONE_BRICK_STAIRS, BYGBlocks.SOAPSTONE_TILE_STAIRS, basePath + "brick_stairs_to_tile_stairs");
        enriching(consumer, BYGBlocks.SOAPSTONE_BRICK_WALL, BYGBlocks.SOAPSTONE_TILE_WALL, basePath + "brick_walls_to_tile_walls");
    }

    private void addTravertineEnrichingRecipes(Consumer<IFinishedRecipe> consumer, String basePath) {
        //Travertine -> Polished Travertine
        enriching(consumer, BYGBlocks.TRAVERTINE, BYGBlocks.POLISHED_TRAVERTINE, basePath + "to_polished");
        enriching(consumer, BYGBlocks.TRAVERTINE_SLAB, BYGBlocks.POLISHED_TRAVERTINE_SLAB, basePath + "slabs_to_polished_slabs");
        enriching(consumer, BYGBlocks.TRAVERTINE_STAIRS, BYGBlocks.POLISHED_TRAVERTINE_STAIRS, basePath + "stairs_to_polished_stairs");
        enriching(consumer, BYGBlocks.TRAVERTINE_WALL, BYGBlocks.POLISHED_TRAVERTINE_WALL, basePath + "walls_to_polished_walls");
        //Polished Travertine -> Chiseled Travertine
        enriching(consumer, BYGBlocks.POLISHED_TRAVERTINE, BYGBlocks.CHISELED_TRAVERTINE, basePath + "polished_to_chiseled");
        enriching(consumer, BYGBlocks.POLISHED_TRAVERTINE_SLAB, BYGBlocks.CHISELED_TRAVERTINE_SLAB, basePath + "polished_slabs_to_chiseled_slabs");
        enriching(consumer, BYGBlocks.POLISHED_TRAVERTINE_STAIRS, BYGBlocks.CHISELED_TRAVERTINE_STAIRS, basePath + "polished_stairs_to_chiseled_stairs");
        enriching(consumer, BYGBlocks.POLISHED_TRAVERTINE_WALL, BYGBlocks.CHISELED_TRAVERTINE_WALL, basePath + "polished_walls_to_chiseled_walls");
    }

    private void enriching(Consumer<IFinishedRecipe> consumer, IItemProvider input, IItemProvider output, String path) {
        ItemStackToItemStackRecipeBuilder.enriching(
                    ItemStackIngredient.from(input),
                    new ItemStack(output)
              ).addCondition(modLoaded)
              .build(consumer, Mekanism.rl(path));
    }

    private void addMetallurgicInfusingRecipes(Consumer<IFinishedRecipe> consumer, String basePath) {
        addMossyStoneInfusingRecipes(consumer, basePath + "mossy_stone/");
        addRedRockInfusingRecipes(consumer, basePath + "red_rock/");
        infuseMoss(consumer, Blocks.NETHERRACK, BYGBlocks.MOSSY_NETHERRACK, basePath + "netherrack_to_mossy_netherrack");
    }

    private void addMossyStoneInfusingRecipes(Consumer<IFinishedRecipe> consumer, String basePath) {
        infuseMoss(consumer, Blocks.STONE, BYGBlocks.MOSSY_STONE, basePath + "stone");
        infuseMoss(consumer, Blocks.STONE_SLAB, BYGBlocks.MOSSY_STONE_SLAB, basePath + "stone_slab");
        infuseMoss(consumer, Blocks.STONE_STAIRS, BYGBlocks.MOSSY_STONE_STAIRS, basePath + "stone_stairs");
    }

    private void addRedRockInfusingRecipes(Consumer<IFinishedRecipe> consumer, String basePath) {
        infuseMoss(consumer, BYGBlocks.RED_ROCK_BRICKS, BYGBlocks.MOSSY_RED_ROCK_BRICKS, basePath + "red_rock_brick");
        infuseMoss(consumer, BYGBlocks.RED_ROCK_BRICK_SLAB, BYGBlocks.MOSSY_RED_ROCK_BRICK_SLAB, basePath + "red_rock_brick_slab");
        infuseMoss(consumer, BYGBlocks.RED_ROCK_BRICK_STAIRS, BYGBlocks.MOSSY_RED_ROCK_BRICK_STAIRS, basePath + "red_rock_brick_stairs");
        infuseMoss(consumer, BYGBlocks.RED_ROCK_BRICK_WALL, BYGBlocks.MOSSY_RED_ROCK_BRICK_WALL, basePath + "red_rock_brick_wall");
    }

    private void infuseMoss(Consumer<IFinishedRecipe> consumer, IItemProvider input, IItemProvider output, String path) {
        ItemStackChemicalToItemStackRecipeBuilder.metallurgicInfusing(
                    ItemStackIngredient.from(input),
                    InfusionStackIngredient.from(MekanismTags.InfuseTypes.BIO, 10),
                    new ItemStack(output)
              ).addCondition(modLoaded)
              .build(consumer, Mekanism.rl(path));
    }
}