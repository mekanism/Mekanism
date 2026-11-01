package mekanism.common.recipe.compat;

import biomesoplenty.api.item.BOPItems;
import java.util.Arrays;
import mekanism.api.chemical.ChemicalIds;
import mekanism.api.datagen.recipe.builder.ItemStackToChemicalRecipeBuilder;
import mekanism.api.datagen.recipe.builder.ItemStackToItemStackRecipeBuilder;
import mekanism.api.recipes.book.CrushingBookCategory;
import mekanism.api.recipes.book.EnrichingBookCategory;
import mekanism.api.recipes.ingredients.ItemStackIngredient;
import mekanism.api.recipes.ingredients.creator.IngredientCreatorAccess;
import mekanism.api.text.EnumColor;
import mekanism.common.recipe.RecipeProviderUtil;
import mekanism.common.recipe.impl.PigmentExtractingRecipeProvider;
import net.minecraft.advancements.Advancement;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.references.ItemIds;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.crafting.Recipe;

public class BiomesOPlentyRecipeProvider extends CompatRecipeProvider {

    public BiomesOPlentyRecipeProvider(BootstrapContext<Recipe<?>> recipeOutput, BootstrapContext<Advancement> advancementOutput, String modid) {
        super(recipeOutput, advancementOutput, modid);
    }

    @Override
    protected void addRecipes() {
        String basePath = getBasePath();
        addDyeRecipes(basePath);
        addPrecisionSawmillRecipes(basePath + "sawing/");
        addSandRecipes(basePath + "sandstone_to_sand/");
    }

    private void addPrecisionSawmillRecipes(String basePath) {
        addPrecisionSawmillWoodTypeRecipes(basePath, BOPItems.DEAD_PLANKS, BOPItems.DEAD_BOAT, BOPItems.DEAD_CHEST_BOAT, BOPItems.DEAD_DOOR,
              BOPItems.DEAD_FENCE_GATE, BOPItems.DEAD_PRESSURE_PLATE, BOPItems.DEAD_TRAPDOOR, BOPItems.DEAD_HANGING_SIGN,
              BOPItems.STRIPPED_DEAD_LOG, BOPItems.DEAD_SHELF, "dead");
        addPrecisionSawmillWoodTypeRecipes(basePath, BOPItems.EMPYREAL_PLANKS, BOPItems.EMPYREAL_BOAT, BOPItems.EMPYREAL_CHEST_BOAT, BOPItems.EMPYREAL_DOOR,
              BOPItems.EMPYREAL_FENCE_GATE, BOPItems.EMPYREAL_PRESSURE_PLATE, BOPItems.EMPYREAL_TRAPDOOR, BOPItems.EMPYREAL_HANGING_SIGN,
              BOPItems.STRIPPED_EMPYREAL_LOG, BOPItems.EMPYREAL_SHELF, "empyreal");
        addPrecisionSawmillWoodTypeRecipes(basePath, BOPItems.FIR_PLANKS, BOPItems.FIR_BOAT, BOPItems.FIR_CHEST_BOAT, BOPItems.FIR_DOOR,
              BOPItems.FIR_FENCE_GATE, BOPItems.FIR_PRESSURE_PLATE, BOPItems.FIR_TRAPDOOR, BOPItems.FIR_HANGING_SIGN,
              BOPItems.STRIPPED_FIR_LOG, BOPItems.FIR_SHELF, "fir");
        addPrecisionSawmillWoodTypeRecipes(basePath, BOPItems.PINE_PLANKS, BOPItems.PINE_BOAT, BOPItems.PINE_CHEST_BOAT, BOPItems.PINE_DOOR,
              BOPItems.PINE_FENCE_GATE, BOPItems.PINE_PRESSURE_PLATE, BOPItems.PINE_TRAPDOOR, BOPItems.PINE_HANGING_SIGN,
              BOPItems.STRIPPED_PINE_LOG, BOPItems.PINE_SHELF, "pine");
        addPrecisionSawmillWoodTypeRecipes(basePath, BOPItems.MAPLE_PLANKS, BOPItems.MAPLE_BOAT, BOPItems.MAPLE_CHEST_BOAT, BOPItems.MAPLE_DOOR,
              BOPItems.MAPLE_FENCE_GATE, BOPItems.MAPLE_PRESSURE_PLATE, BOPItems.MAPLE_TRAPDOOR, BOPItems.MAPLE_HANGING_SIGN,
              BOPItems.STRIPPED_MAPLE_LOG, BOPItems.MAPLE_SHELF, "maple");
        addPrecisionSawmillWoodTypeRecipes(basePath, BOPItems.HELLBARK_PLANKS, BOPItems.HELLBARK_BOAT, BOPItems.HELLBARK_CHEST_BOAT, BOPItems.HELLBARK_DOOR,
              BOPItems.HELLBARK_FENCE_GATE, BOPItems.HELLBARK_PRESSURE_PLATE, BOPItems.HELLBARK_TRAPDOOR, BOPItems.HELLBARK_HANGING_SIGN,
              BOPItems.STRIPPED_HELLBARK_LOG, BOPItems.HELLBARK_SHELF, "hellbark");
        addPrecisionSawmillWoodTypeRecipes(basePath, BOPItems.JACARANDA_PLANKS, BOPItems.JACARANDA_BOAT, BOPItems.JACARANDA_CHEST_BOAT, BOPItems.JACARANDA_DOOR,
              BOPItems.JACARANDA_FENCE_GATE, BOPItems.JACARANDA_PRESSURE_PLATE, BOPItems.JACARANDA_TRAPDOOR, BOPItems.JACARANDA_HANGING_SIGN,
              BOPItems.STRIPPED_JACARANDA_LOG, BOPItems.JACARANDA_SHELF, "jacaranda");
        addPrecisionSawmillWoodTypeRecipes(basePath, BOPItems.MAGIC_PLANKS, BOPItems.MAGIC_BOAT, BOPItems.MAGIC_CHEST_BOAT, BOPItems.MAGIC_DOOR,
              BOPItems.MAGIC_FENCE_GATE, BOPItems.MAGIC_PRESSURE_PLATE, BOPItems.MAGIC_TRAPDOOR, BOPItems.MAGIC_HANGING_SIGN,
              BOPItems.STRIPPED_MAGIC_LOG, BOPItems.MAGIC_SHELF, "magic");
        addPrecisionSawmillWoodTypeRecipes(basePath, BOPItems.MAHOGANY_PLANKS, BOPItems.MAHOGANY_BOAT, BOPItems.MAHOGANY_CHEST_BOAT, BOPItems.MAHOGANY_DOOR,
              BOPItems.MAHOGANY_FENCE_GATE, BOPItems.MAHOGANY_PRESSURE_PLATE, BOPItems.MAHOGANY_TRAPDOOR, BOPItems.MAHOGANY_HANGING_SIGN,
              BOPItems.STRIPPED_MAHOGANY_LOG, BOPItems.MAHOGANY_SHELF, "mahogany");
        addPrecisionSawmillWoodTypeRecipes(basePath, BOPItems.PALM_PLANKS, BOPItems.PALM_BOAT, BOPItems.PALM_CHEST_BOAT, BOPItems.PALM_DOOR,
              BOPItems.PALM_FENCE_GATE, BOPItems.PALM_PRESSURE_PLATE, BOPItems.PALM_TRAPDOOR, BOPItems.PALM_HANGING_SIGN,
              BOPItems.STRIPPED_PALM_LOG, BOPItems.PALM_SHELF, "palm");
        addPrecisionSawmillWoodTypeRecipes(basePath, BOPItems.REDWOOD_PLANKS, BOPItems.REDWOOD_BOAT, BOPItems.REDWOOD_CHEST_BOAT, BOPItems.REDWOOD_DOOR,
              BOPItems.REDWOOD_FENCE_GATE, BOPItems.REDWOOD_PRESSURE_PLATE, BOPItems.REDWOOD_TRAPDOOR, BOPItems.REDWOOD_HANGING_SIGN,
              BOPItems.STRIPPED_REDWOOD_LOG, BOPItems.REDWOOD_SHELF, "redwood");
        addPrecisionSawmillWoodTypeRecipes(basePath, BOPItems.UMBRAN_PLANKS, BOPItems.UMBRAN_BOAT, BOPItems.UMBRAN_CHEST_BOAT, BOPItems.UMBRAN_DOOR,
              BOPItems.UMBRAN_FENCE_GATE, BOPItems.UMBRAN_PRESSURE_PLATE, BOPItems.UMBRAN_TRAPDOOR, BOPItems.UMBRAN_HANGING_SIGN,
              BOPItems.STRIPPED_UMBRAN_LOG, BOPItems.UMBRAN_SHELF, "umbran");
        addPrecisionSawmillWoodTypeRecipes(basePath, BOPItems.WILLOW_PLANKS, BOPItems.WILLOW_BOAT, BOPItems.WILLOW_CHEST_BOAT, BOPItems.WILLOW_DOOR,
              BOPItems.WILLOW_FENCE_GATE, BOPItems.WILLOW_PRESSURE_PLATE, BOPItems.WILLOW_TRAPDOOR, BOPItems.WILLOW_HANGING_SIGN,
              BOPItems.STRIPPED_WILLOW_LOG, BOPItems.WILLOW_SHELF, "willow");
    }

    private void addPrecisionSawmillWoodTypeRecipes(String basePath, Item planks, Item boat, Item chestBoat, Item door, Item fenceGate,
          Item pressurePlate, Item trapdoor, Item hangingSign, Item strippedLog, Item shelf, String name) {
        RecipeProviderUtil.addPrecisionSawmillWoodTypeRecipes(this.output, this.items, basePath, planks, boat, chestBoat, door, fenceGate,
              tag(name + "_logs"), pressurePlate, trapdoor, hangingSign, strippedLog, shelf, name);
    }

    private void addSandRecipes(String basePath) {
        //Black Sandstone -> Sand
        addSandStoneToSandRecipe(basePath + "black", BOPItems.BLACK_SAND, BOPItems.BLACK_SANDSTONE, BOPItems.CHISELED_BLACK_SANDSTONE,
              BOPItems.CUT_BLACK_SANDSTONE, BOPItems.SMOOTH_BLACK_SANDSTONE);
        //Orange Sandstone -> Sand
        addSandStoneToSandRecipe(basePath + "orange", BOPItems.ORANGE_SAND, BOPItems.ORANGE_SANDSTONE, BOPItems.CHISELED_ORANGE_SANDSTONE,
              BOPItems.CUT_ORANGE_SANDSTONE, BOPItems.SMOOTH_ORANGE_SANDSTONE);
        //White Sandstone -> Sand
        addSandStoneToSandRecipe(basePath + "white", BOPItems.WHITE_SAND, BOPItems.WHITE_SANDSTONE, BOPItems.CHISELED_WHITE_SANDSTONE,
              BOPItems.CUT_WHITE_SANDSTONE, BOPItems.SMOOTH_WHITE_SANDSTONE);
    }

    private void addSandStoneToSandRecipe(String path, Item sand, Item... sandstones) {
        ItemStackToItemStackRecipeBuilder.crushing(
                    IngredientCreatorAccess.item().from(sandstones),
                    new ItemStackTemplate(sand, 2)
              ).category(CrushingBookCategory.BUILDING)
              .unlockedBy(Arrays.stream(sandstones).map(this::hasCriterion))
              .save(this.output, path);
    }

    private void addDyeRecipes(String basePath) {
        //Brown
        largeDye(basePath, ItemIds.DYE.brown(), EnumColor.BROWN, BOPItems.CATTAIL);
        //Red
        dye(basePath, ItemIds.DYE.red(), EnumColor.RED, BOPItems.ORIGIN_ROSE, BOPItems.WATERLILY);
        //Green
        dye(basePath, ItemIds.DYE.green(), EnumColor.DARK_GREEN, BOPItems.TINY_CACTUS);
        //Purple
        dye(basePath, ItemIds.DYE.purple(), EnumColor.PURPLE, BOPItems.VIOLET, BOPItems.LAVENDER);
        largeDye(basePath, ItemIds.DYE.purple(), EnumColor.PURPLE, BOPItems.TALL_LAVENDER);
        //Magenta
        dye(basePath, ItemIds.DYE.magenta(), EnumColor.PINK, BOPItems.PURPLE_WILDFLOWERS);
        //Orange
        dye(basePath, ItemIds.DYE.orange(), EnumColor.ORANGE, BOPItems.ORANGE_COSMOS, BOPItems.BURNING_BLOSSOM);
        //Pink
        dye(basePath, ItemIds.DYE.pink(), EnumColor.BRIGHT_PINK, BOPItems.PINK_DAFFODIL, BOPItems.PINK_HIBISCUS);
        //Cyan
        dye(basePath, ItemIds.DYE.cyan(), EnumColor.DARK_AQUA, BOPItems.GLOWFLOWER);
        //Gray
        dye(basePath, ItemIds.DYE.gray(), EnumColor.DARK_GRAY, BOPItems.WILTED_LILY);
        //Light Blue
        dye(basePath, ItemIds.DYE.lightBlue(), EnumColor.INDIGO, BOPItems.BLUE_HYDRANGEA);
        largeDye(basePath, ItemIds.DYE.lightBlue(), EnumColor.INDIGO, BOPItems.ICY_IRIS);
        //Light Gray
        dye(basePath, ItemIds.DYE.lightGray(), EnumColor.GRAY, BOPItems.ENDBLOOM);
        //White
        dye(basePath, ItemIds.DYE.white(), EnumColor.WHITE, BOPItems.WHITE_LAVENDER, BOPItems.WHITE_PETALS);
        largeDye(basePath, ItemIds.DYE.white(), EnumColor.WHITE, BOPItems.TALL_WHITE_LAVENDER);
        //Yellow
        dye(basePath, ItemIds.DYE.yellow(), EnumColor.YELLOW, BOPItems.GOLDENROD);
    }

    private void dye(String basePath, ResourceKey<Item> output, EnumColor color, Item... inputs) {
        ItemStackIngredient inputIngredient = IngredientCreatorAccess.item().from(inputs);
        ItemStackToItemStackRecipeBuilder.enriching(
                    inputIngredient,
                    new ItemStackTemplate(this.items.getOrThrow(output), 2)
              ).category(EnrichingBookCategory.DECORATIVE)
              .group(output)
              .unlockedBy(Arrays.stream(inputs).map(this::hasCriterion))
              .save(this.output, basePath + "dye/" + color.getRegistryPrefix());
        //Flowers -> 4x dye output (See PigmentExtractingRecipeProvider#addFlowerExtractionRecipes for note)
        int flowerRate = 3 * PigmentExtractingRecipeProvider.DYE_RATE;
        ItemStackToChemicalRecipeBuilder.pigmentExtracting(
                    inputIngredient,
                    chemicalTemplate(ChemicalIds.SIMPLE_PIGMENTS.pick(color), flowerRate)
              ).group(output)
              .unlockedBy(Arrays.stream(inputs).map(this::hasCriterion))
              .save(this.output, basePath + "pigment_extracting/" + color.getRegistryPrefix());
    }

    private void largeDye(String basePath, ResourceKey<Item> output, EnumColor color, Item... inputs) {
        ItemStackIngredient inputIngredient = IngredientCreatorAccess.item().from(inputs);
        ItemStackToItemStackRecipeBuilder.enriching(
                    inputIngredient,
                    new ItemStackTemplate(this.items.getOrThrow(output), 4)
              ).category(EnrichingBookCategory.DECORATIVE)
              .group(output)
              .unlockedBy(Arrays.stream(inputs).map(this::hasCriterion))
              .save(this.output, basePath + "dye/large_" + color.getRegistryPrefix());
        //Flowers -> 4x dye output (See PigmentExtractingRecipeProvider#addFlowerExtractionRecipes for note)
        int largeFlowerRate = 6 * PigmentExtractingRecipeProvider.DYE_RATE;
        ItemStackToChemicalRecipeBuilder.pigmentExtracting(
                    inputIngredient,
                    chemicalTemplate(ChemicalIds.SIMPLE_PIGMENTS.pick(color), largeFlowerRate)
              ).group(output)
              .unlockedBy(Arrays.stream(inputs).map(this::hasCriterion))
              .save(this.output, basePath + "pigment_extracting/large_" + color.getRegistryPrefix());
    }
}