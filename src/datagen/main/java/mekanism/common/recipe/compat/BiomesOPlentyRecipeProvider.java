package mekanism.common.recipe.compat;

import biomesoplenty.api.block.BOPBlocks;
import biomesoplenty.api.item.BOPItems;
import java.util.Arrays;
import java.util.function.Consumer;
import mekanism.api.annotations.NothingNullByDefault;
import mekanism.api.datagen.recipe.builder.ItemStackToChemicalRecipeBuilder;
import mekanism.api.datagen.recipe.builder.ItemStackToItemStackRecipeBuilder;
import mekanism.api.recipes.ingredients.ItemStackIngredient;
import mekanism.api.recipes.ingredients.creator.IngredientCreatorAccess;
import mekanism.api.text.EnumColor;
import mekanism.common.Mekanism;
import mekanism.common.recipe.RecipeProviderUtil;
import mekanism.common.recipe.impl.PigmentExtractingRecipeProvider;
import mekanism.common.registries.MekanismPigments;
import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.registries.RegistryObject;

@NothingNullByDefault
public class BiomesOPlentyRecipeProvider extends CompatRecipeProvider {

    public BiomesOPlentyRecipeProvider() {
        super("biomesoplenty");
    }

    @Override
    protected void registerRecipes(Consumer<FinishedRecipe> consumer, String basePath) {
        addDyeRecipes(consumer, basePath);
        addPrecisionSawmillRecipes(consumer, basePath + "sawing/");
        addSandRecipes(consumer, basePath + "sandstone_to_sand/");
        //TODO: Bio-fuel recipes?
    }

    private void addPrecisionSawmillRecipes(Consumer<FinishedRecipe> consumer, String basePath) {
        addPrecisionSawmillWoodTypeRecipes(consumer, basePath, BOPBlocks.CHERRY_PLANKS, BOPItems.CHERRY_BOAT, BOPItems.CHERRY_CHEST_BOAT, BOPBlocks.CHERRY_DOOR,
              BOPBlocks.CHERRY_FENCE_GATE, BOPBlocks.CHERRY_PRESSURE_PLATE, BOPBlocks.CHERRY_TRAPDOOR, "cherry");
        addPrecisionSawmillWoodTypeRecipes(consumer, basePath, BOPBlocks.DEAD_PLANKS, BOPItems.DEAD_BOAT, BOPItems.DEAD_CHEST_BOAT, BOPBlocks.DEAD_DOOR,
              BOPBlocks.DEAD_FENCE_GATE, BOPBlocks.DEAD_PRESSURE_PLATE, BOPBlocks.DEAD_TRAPDOOR, "dead");
        addPrecisionSawmillWoodTypeRecipes(consumer, basePath, BOPBlocks.FIR_PLANKS, BOPItems.FIR_BOAT, BOPItems.FIR_CHEST_BOAT, BOPBlocks.FIR_DOOR,
              BOPBlocks.FIR_FENCE_GATE, BOPBlocks.FIR_PRESSURE_PLATE, BOPBlocks.FIR_TRAPDOOR, "fir");
        addPrecisionSawmillWoodTypeRecipes(consumer, basePath, BOPBlocks.HELLBARK_PLANKS, BOPItems.HELLBARK_BOAT, BOPItems.HELLBARK_CHEST_BOAT, BOPBlocks.HELLBARK_DOOR,
              BOPBlocks.HELLBARK_FENCE_GATE, BOPBlocks.HELLBARK_PRESSURE_PLATE, BOPBlocks.HELLBARK_TRAPDOOR, "hellbark");
        addPrecisionSawmillWoodTypeRecipes(consumer, basePath, BOPBlocks.JACARANDA_PLANKS, BOPItems.JACARANDA_BOAT, BOPItems.JACARANDA_CHEST_BOAT, BOPBlocks.JACARANDA_DOOR,
              BOPBlocks.JACARANDA_FENCE_GATE, BOPBlocks.JACARANDA_PRESSURE_PLATE, BOPBlocks.JACARANDA_TRAPDOOR, "jacaranda");
        addPrecisionSawmillWoodTypeRecipes(consumer, basePath, BOPBlocks.MAGIC_PLANKS, BOPItems.MAGIC_BOAT, BOPItems.MAGIC_CHEST_BOAT, BOPBlocks.MAGIC_DOOR,
              BOPBlocks.MAGIC_FENCE_GATE, BOPBlocks.MAGIC_PRESSURE_PLATE, BOPBlocks.MAGIC_TRAPDOOR, "magic");
        addPrecisionSawmillWoodTypeRecipes(consumer, basePath, BOPBlocks.MAHOGANY_PLANKS, BOPItems.MAHOGANY_BOAT, BOPItems.MAHOGANY_CHEST_BOAT, BOPBlocks.MAHOGANY_DOOR,
              BOPBlocks.MAHOGANY_FENCE_GATE, BOPBlocks.MAHOGANY_PRESSURE_PLATE, BOPBlocks.MAHOGANY_TRAPDOOR, "mahogany");
        addPrecisionSawmillWoodTypeRecipes(consumer, basePath, BOPBlocks.PALM_PLANKS, BOPItems.PALM_BOAT, BOPItems.PALM_CHEST_BOAT, BOPBlocks.PALM_DOOR,
              BOPBlocks.PALM_FENCE_GATE, BOPBlocks.PALM_PRESSURE_PLATE, BOPBlocks.PALM_TRAPDOOR, "palm");
        addPrecisionSawmillWoodTypeRecipes(consumer, basePath, BOPBlocks.REDWOOD_PLANKS, BOPItems.REDWOOD_BOAT, BOPItems.REDWOOD_CHEST_BOAT, BOPBlocks.REDWOOD_DOOR,
              BOPBlocks.REDWOOD_FENCE_GATE, BOPBlocks.REDWOOD_PRESSURE_PLATE, BOPBlocks.REDWOOD_TRAPDOOR, "redwood");
        addPrecisionSawmillWoodTypeRecipes(consumer, basePath, BOPBlocks.UMBRAN_PLANKS, BOPItems.UMBRAN_BOAT, BOPItems.UMBRAN_CHEST_BOAT, BOPBlocks.UMBRAN_DOOR,
              BOPBlocks.UMBRAN_FENCE_GATE, BOPBlocks.UMBRAN_PRESSURE_PLATE, BOPBlocks.UMBRAN_TRAPDOOR, "umbran");
        addPrecisionSawmillWoodTypeRecipes(consumer, basePath, BOPBlocks.WILLOW_PLANKS, BOPItems.WILLOW_BOAT, BOPItems.WILLOW_CHEST_BOAT, BOPBlocks.WILLOW_DOOR,
              BOPBlocks.WILLOW_FENCE_GATE, BOPBlocks.WILLOW_PRESSURE_PLATE, BOPBlocks.WILLOW_TRAPDOOR, "willow");
    }

    private void addPrecisionSawmillWoodTypeRecipes(Consumer<FinishedRecipe> consumer, String basePath, RegistryObject<Block> planks, RegistryObject<Item> boat,
          RegistryObject<Item> chestBoat, RegistryObject<Block> door, RegistryObject<Block> fenceGate, RegistryObject<Block> pressurePlate, RegistryObject<Block> trapdoor,
          String name) {
        RecipeProviderUtil.addPrecisionSawmillWoodTypeRecipes(consumer, basePath, planks.get(), boat.get(), chestBoat.get(), door.get(), fenceGate.get(),
              tag(name + "_logs"), pressurePlate.get(), trapdoor.get(), name, modLoaded);
    }

    private void addSandRecipes(Consumer<FinishedRecipe> consumer, String basePath) {
        //Black Sandstone -> Sand
        addSandStoneToSandRecipe(consumer, basePath + "black", BOPBlocks.BLACK_SAND, BOPBlocks.BLACK_SANDSTONE, BOPBlocks.CHISELED_BLACK_SANDSTONE,
              BOPBlocks.CUT_BLACK_SANDSTONE, BOPBlocks.SMOOTH_BLACK_SANDSTONE);
        //Orange Sandstone -> Sand
        addSandStoneToSandRecipe(consumer, basePath + "orange", BOPBlocks.ORANGE_SAND, BOPBlocks.ORANGE_SANDSTONE, BOPBlocks.CHISELED_ORANGE_SANDSTONE,
              BOPBlocks.CUT_ORANGE_SANDSTONE, BOPBlocks.SMOOTH_ORANGE_SANDSTONE);
        //White Sandstone -> Sand
        addSandStoneToSandRecipe(consumer, basePath + "white", BOPBlocks.WHITE_SAND, BOPBlocks.WHITE_SANDSTONE, BOPBlocks.CHISELED_WHITE_SANDSTONE,
              BOPBlocks.CUT_WHITE_SANDSTONE, BOPBlocks.SMOOTH_WHITE_SANDSTONE);
    }

    @SafeVarargs
    private void addSandStoneToSandRecipe(Consumer<FinishedRecipe> consumer, String path, RegistryObject<Block> sand, RegistryObject<Block>... sandstones) {
        RecipeProviderUtil.addSandStoneToSandRecipe(consumer, path, modLoaded, sand.get(), toItemLike(sandstones));
    }

    private void addDyeRecipes(Consumer<FinishedRecipe> consumer, String basePath) {
        //Red
        dye(consumer, basePath, Items.RED_DYE, EnumColor.RED, BOPBlocks.ROSE);
        //Purple
        dye(consumer, basePath, Items.PURPLE_DYE, EnumColor.PURPLE, BOPBlocks.VIOLET, BOPBlocks.LAVENDER);
        //Magenta
        dye(consumer, basePath, Items.MAGENTA_DYE, EnumColor.PINK, BOPBlocks.WILDFLOWER);
        //Orange
        dye(consumer, basePath, Items.ORANGE_DYE, EnumColor.ORANGE, BOPBlocks.ORANGE_COSMOS, BOPBlocks.BURNING_BLOSSOM);
        //Pink
        dye(consumer, basePath, Items.PINK_DYE, EnumColor.BRIGHT_PINK, BOPBlocks.PINK_DAFFODIL, BOPBlocks.PINK_HIBISCUS);
        //Cyan
        dye(consumer, basePath, Items.CYAN_DYE, EnumColor.DARK_AQUA, BOPBlocks.GLOWFLOWER);
        //Gray
        dye(consumer, basePath, Items.GRAY_DYE, EnumColor.DARK_GRAY, BOPBlocks.WILTED_LILY);
        //Light Blue
        dye(consumer, basePath, Items.LIGHT_BLUE_DYE, EnumColor.INDIGO, BOPBlocks.BLUE_HYDRANGEA);
        //Yellow
        dye(consumer, basePath, Items.YELLOW_DYE, EnumColor.YELLOW, BOPBlocks.GOLDENROD);
    }

    @SafeVarargs
    private void dye(Consumer<FinishedRecipe> consumer, String basePath, ItemLike output, EnumColor color, RegistryObject<Block>... inputs) {
        ItemStackIngredient inputIngredient = IngredientCreatorAccess.item().from(Ingredient.of(toItemLike(inputs)));
        ItemStackToItemStackRecipeBuilder.enriching(
                    inputIngredient,
                    new ItemStack(output, 2)
              ).addCondition(modLoaded)
              .build(consumer, Mekanism.rl(basePath + "dye/" + color.getRegistryPrefix()));
        //Flowers -> 4x dye output (See PigmentExtractingRecipeProvider#addFlowerExtractionRecipes for note)
        long flowerRate = 3 * PigmentExtractingRecipeProvider.DYE_RATE;
        ItemStackToChemicalRecipeBuilder.pigmentExtracting(
                    inputIngredient,
                    MekanismPigments.PIGMENT_COLOR_LOOKUP.get(color).getStack(flowerRate)
              ).addCondition(modLoaded)
              .build(consumer, Mekanism.rl(basePath + "pigment_extracting/" + color.getRegistryPrefix()));
    }

    @SafeVarargs
    private static ItemLike[] toItemLike(RegistryObject<Block>... ros) {
        return Arrays.stream(ros).map(RegistryObject::get).toArray(ItemLike[]::new);
    }
}