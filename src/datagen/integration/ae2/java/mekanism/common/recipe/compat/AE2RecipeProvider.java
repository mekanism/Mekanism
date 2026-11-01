package mekanism.common.recipe.compat;

import appeng.api.ids.AEBlockIds;
import appeng.api.ids.AEItemIds;
import appeng.api.util.AEColor;
import java.util.Map;
import mekanism.api.chemical.ChemicalIds;
import mekanism.api.datagen.recipe.builder.ItemStackChemicalToItemStackRecipeBuilder;
import mekanism.api.datagen.recipe.builder.ItemStackToItemStackRecipeBuilder;
import mekanism.api.datagen.recipe.builder.SawmillRecipeBuilder;
import mekanism.api.recipes.book.CrushingBookCategory;
import mekanism.api.recipes.book.EnrichingBookCategory;
import mekanism.api.recipes.ingredients.creator.IngredientCreatorAccess;
import mekanism.common.Mekanism;
import mekanism.common.recipe.impl.PaintingRecipeProvider;
import net.minecraft.advancements.Advancement;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.references.ItemIds;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.crafting.Recipe;
import net.neoforged.neoforge.common.Tags;

public class AE2RecipeProvider extends CompatRecipeProvider {

    public AE2RecipeProvider(BootstrapContext<Recipe<?>> recipeOutput, BootstrapContext<Advancement> advancementOutput, String modid) {
        super(recipeOutput, advancementOutput, modid);
    }

    @Override
    protected void addRecipes() {
        String basePath = getBasePath();
        addPaintingRecipes(basePath + "painting/");
        //Certus Crystal -> Certus Dust
        TagKey<Item> certusQuartz = ItemTags.create(Tags.Items.GEMS.location().withSuffix("/certus_quartz"));
        ItemStackToItemStackRecipeBuilder.crushing(
                    IngredientCreatorAccess.item().from(this.items, certusQuartz),
                    foreignTemplate(AEItemIds.CERTUS_QUARTZ_DUST)
              ).unlockedBy("has_certus_quartz", has(certusQuartz))
              .save(output, basePath + "certus_crystal_to_dust");

        //Fluix Crystal -> Fluix Dust
        ItemStackToItemStackRecipeBuilder.crushing(
                    IngredientCreatorAccess.item().from(this.items, AEItemIds.FLUIX_CRYSTAL),
                    foreignTemplate(AEItemIds.FLUIX_DUST)
              ).unlockedBy(hasForeignCriterion(AEItemIds.FLUIX_CRYSTAL))
              .save(output, basePath + "fluix_crystal_to_dust");

        //Sky Stone -> Sky Stone Dust
        ItemStackToItemStackRecipeBuilder.crushing(
                    IngredientCreatorAccess.item().from(this.items, AEBlockIds.SKY_STONE_BLOCK),
                    foreignTemplate(AEItemIds.SKY_DUST)
              ).unlockedBy(hasForeignCriterion(AEBlockIds.SKY_STONE_BLOCK))
              .save(output, basePath + "sky_stone_to_dust");

        //Sky Stone Dust -> Sky Stone
        ItemStackToItemStackRecipeBuilder.enriching(
                    IngredientCreatorAccess.item().from(this.items, AEItemIds.SKY_DUST),
                    foreignTemplate(AEBlockIds.SKY_STONE_BLOCK)
              ).unlockedBy(hasForeignCriterion(AEItemIds.SKY_DUST))
              .save(output, basePath + "sky_stone_dust_to_sky_stone");

        //Ender Pearl -> Ender Dust
        ItemStackToItemStackRecipeBuilder.crushing(
                    IngredientCreatorAccess.item().from(items, ItemIds.ENDER_PEARL),
                    foreignTemplate(AEItemIds.ENDER_DUST)
              ).unlockedBy(hasCriterion(ItemIds.ENDER_PEARL))
              .save(output, basePath + "ender_pearl_to_dust");

        //Sand -> Silicon
        ItemStackToItemStackRecipeBuilder.crushing(
                    IngredientCreatorAccess.item().from(this.items, Tags.BlockItems.SANDS),
                    foreignTemplate(AEItemIds.SILICON)
              ).unlockedBy("has_sand", has(Tags.BlockItems.SANDS))
              .save(output, basePath + "sand_to_silicon");

        //Certus Dust to Silicon
        ItemStackToItemStackRecipeBuilder.enriching(
                    IngredientCreatorAccess.item().from(this.items, AEItemIds.CERTUS_QUARTZ_DUST),
                    foreignTemplate(AEItemIds.SILICON, 2)
              ).unlockedBy(hasForeignCriterion(AEItemIds.CERTUS_QUARTZ_DUST))
              .save(output, basePath + "certus_quartz_dust_to_silicon");

        //Crank
        SawmillRecipeBuilder.sawing(
                    IngredientCreatorAccess.item().from(items, AEBlockIds.CRANK),
                    template(ItemIds.STICK, 4),
                    template(ItemIds.COPPER_INGOT),
                    1
              ).unlockedBy(hasForeignCriterion(AEBlockIds.CRANK))
              .save(output, basePath + "sawing/crank");

        addDecorativeRecipes(basePath + "decorative/");
    }
    
    private void addPaintingRecipes(String basePath) {
        for (Map.Entry<AEColor, Identifier> entry : AEItemIds.COLORED_PAINT_BALL.entrySet()) {
            DyeColor dyeColor = entry.getKey().dye;
            ItemStackChemicalToItemStackRecipeBuilder.painting(
                        IngredientCreatorAccess.item().from(this.items, AEItemIds.MATTER_BALL),
                        IngredientCreatorAccess.chemicalStack().from(chemicals, ChemicalIds.SIMPLE_PIGMENTS.pick(dyeColor), PaintingRecipeProvider.EIGHT_AT_A_TIME),
                        false,
                        foreignTemplate(entry.getValue())
                  ).group(Mekanism.rl("paint_ball"))
                  .unlockedBy("has_needed_dye", has(dyeColor.getTag()))
                  .save(output, basePath + dyeColor);
        }
    }

    private void addDecorativeRecipes(String basePath) {
        addDecorativeQuartzRecipes(basePath + "certus_quartz/");
        addDecorativeSkyStoneRecipes(basePath + "sky_stone/");
    }

    private void addDecorativeSkyStoneRecipes(String basePath) {
        addDecorativeCrushingSkyStoneRecipes(basePath + "crushing/");
        addDecorativeEnrichingSkyStoneRecipes(basePath + "enriching/");
    }

    private void addDecorativeCrushingSkyStoneRecipes(String basePath) {
        //Smooth Sky Stone Chest -> Sky Stone Chest
        ItemStackToItemStackRecipeBuilder.crushing(
                    IngredientCreatorAccess.item().from(this.items, AEBlockIds.SMOOTH_SKY_STONE_CHEST),
                    foreignTemplate(AEBlockIds.SKY_STONE_CHEST)
              ).category(CrushingBookCategory.BUILDING)
              .unlockedBy(hasForeignCriterion(AEBlockIds.SMOOTH_SKY_STONE_CHEST))
              .save(output, basePath + "smooth_chest_to_chest");

        //Smooth Sky Stone -> Sky Stone
        ItemStackToItemStackRecipeBuilder.crushing(
                    IngredientCreatorAccess.item().from(this.items, AEBlockIds.SMOOTH_SKY_STONE_BLOCK),
                    foreignTemplate(AEBlockIds.SKY_STONE_BLOCK)
              ).category(CrushingBookCategory.BUILDING)
              .unlockedBy(hasForeignCriterion(AEBlockIds.SMOOTH_SKY_STONE_BLOCK))
              .save(output, basePath + "smooth_to_stone");
        //Smooth Sky Stone Slab -> Sky Stone Slab
        ItemStackToItemStackRecipeBuilder.crushing(
                    IngredientCreatorAccess.item().from(this.items, AEBlockIds.SMOOTH_SKY_STONE_SLAB),
                    foreignTemplate(AEBlockIds.SKY_STONE_SLAB)
              ).category(CrushingBookCategory.BUILDING)
              .unlockedBy(hasForeignCriterion(AEBlockIds.SMOOTH_SKY_STONE_SLAB))
              .save(output, basePath + "smooth_slab_to_slab");
        //Smooth Sky Stone Stairs -> Sky Stone Stairs
        ItemStackToItemStackRecipeBuilder.crushing(
                    IngredientCreatorAccess.item().from(this.items, AEBlockIds.SMOOTH_SKY_STONE_STAIRS),
                    foreignTemplate(AEBlockIds.SKY_STONE_STAIRS)
              ).category(CrushingBookCategory.BUILDING)
              .unlockedBy(hasForeignCriterion(AEBlockIds.SMOOTH_SKY_STONE_STAIRS))
              .save(output, basePath + "smooth_stairs_to_stairs");
        //Smooth Sky Stone Wall -> Sky Stone Wall
        ItemStackToItemStackRecipeBuilder.crushing(
                    IngredientCreatorAccess.item().from(this.items, AEBlockIds.SMOOTH_SKY_STONE_WALL),
                    foreignTemplate(AEBlockIds.SKY_STONE_WALL)
              ).category(CrushingBookCategory.BUILDING)
              .unlockedBy(hasForeignCriterion(AEBlockIds.SMOOTH_SKY_STONE_WALL))
              .save(output, basePath + "smooth_wall_to_wall");

        //Sky Stone Brick -> Smooth Sky Stone
        ItemStackToItemStackRecipeBuilder.crushing(
                    IngredientCreatorAccess.item().from(this.items, AEBlockIds.SKY_STONE_BRICK),
                    foreignTemplate(AEBlockIds.SMOOTH_SKY_STONE_BLOCK)
              ).category(CrushingBookCategory.BUILDING)
              .unlockedBy(hasForeignCriterion(AEBlockIds.SKY_STONE_BRICK))
              .save(output, basePath + "brick_to_smooth");
        //Sky Stone Brick Slab -> Smooth Sky Stone Slab
        ItemStackToItemStackRecipeBuilder.crushing(
                    IngredientCreatorAccess.item().from(this.items, AEBlockIds.SKY_STONE_BRICK_SLAB),
                    foreignTemplate(AEBlockIds.SMOOTH_SKY_STONE_SLAB)
              ).category(CrushingBookCategory.BUILDING)
              .unlockedBy(hasForeignCriterion(AEBlockIds.SKY_STONE_BRICK_SLAB))
              .save(output, basePath + "brick_slab_to_smooth_slab");
        //Sky Stone Brick Stairs -> Smooth Sky Stone Stairs
        ItemStackToItemStackRecipeBuilder.crushing(
                    IngredientCreatorAccess.item().from(this.items, AEBlockIds.SKY_STONE_BRICK_STAIRS),
                    foreignTemplate(AEBlockIds.SMOOTH_SKY_STONE_STAIRS)
              ).category(CrushingBookCategory.BUILDING)
              .unlockedBy(hasForeignCriterion(AEBlockIds.SKY_STONE_BRICK_STAIRS))
              .save(output, basePath + "brick_stairs_to_smooth_stairs");
        //Sky Stone Brick Wall -> Smooth Sky Stone Wall
        ItemStackToItemStackRecipeBuilder.crushing(
                    IngredientCreatorAccess.item().from(this.items, AEBlockIds.SKY_STONE_BRICK_WALL),
                    foreignTemplate(AEBlockIds.SMOOTH_SKY_STONE_WALL)
              ).category(CrushingBookCategory.BUILDING)
              .unlockedBy(hasForeignCriterion(AEBlockIds.SKY_STONE_BRICK_WALL))
              .save(output, basePath + "brick_wall_to_smooth_wall");

        //Sky Stone Small Brick -> Sky Stone Brick
        ItemStackToItemStackRecipeBuilder.crushing(
                    IngredientCreatorAccess.item().from(this.items, AEBlockIds.SKY_STONE_SMALL_BRICK),
                    foreignTemplate(AEBlockIds.SKY_STONE_BRICK)
              ).category(CrushingBookCategory.BUILDING)
              .unlockedBy(hasForeignCriterion(AEBlockIds.SKY_STONE_SMALL_BRICK))
              .save(output, basePath + "small_brick_to_brick");
        //Sky Stone Small Brick Slab -> Sky Stone Brick Slab
        ItemStackToItemStackRecipeBuilder.crushing(
                    IngredientCreatorAccess.item().from(this.items, AEBlockIds.SKY_STONE_SMALL_BRICK_SLAB),
                    foreignTemplate(AEBlockIds.SKY_STONE_BRICK_SLAB)
              ).category(CrushingBookCategory.BUILDING)
              .unlockedBy(hasForeignCriterion(AEBlockIds.SKY_STONE_SMALL_BRICK_SLAB))
              .save(output, basePath + "small_brick_slab_to_brick_slab");
        //Sky Stone Small Brick Stairs -> Sky Stone Brick Stairs
        ItemStackToItemStackRecipeBuilder.crushing(
                    IngredientCreatorAccess.item().from(this.items, AEBlockIds.SKY_STONE_SMALL_BRICK_STAIRS),
                    foreignTemplate(AEBlockIds.SKY_STONE_BRICK_STAIRS)
              ).category(CrushingBookCategory.BUILDING)
              .unlockedBy(hasForeignCriterion(AEBlockIds.SKY_STONE_SMALL_BRICK_STAIRS))
              .save(output, basePath + "small_brick_stairs_to_brick_stairs");
        //Sky Stone Small Brick Wall -> Sky Stone Brick Wall
        ItemStackToItemStackRecipeBuilder.crushing(
                    IngredientCreatorAccess.item().from(this.items, AEBlockIds.SKY_STONE_SMALL_BRICK_WALL),
                    foreignTemplate(AEBlockIds.SKY_STONE_BRICK_WALL)
              ).category(CrushingBookCategory.BUILDING)
              .unlockedBy(hasForeignCriterion(AEBlockIds.SKY_STONE_SMALL_BRICK_WALL))
              .save(output, basePath + "small_brick_wall_to_brick_wall");
    }

    private void addDecorativeEnrichingSkyStoneRecipes(String basePath) {
        //Sky Stone Chest -> Smooth Sky Stone Chest
        ItemStackToItemStackRecipeBuilder.enriching(
                    IngredientCreatorAccess.item().from(this.items, AEBlockIds.SKY_STONE_CHEST),
                    foreignTemplate(AEBlockIds.SMOOTH_SKY_STONE_CHEST)
              ).category(EnrichingBookCategory.BUILDING)
              .unlockedBy(hasForeignCriterion(AEBlockIds.SKY_STONE_CHEST))
              .save(output, basePath + "chest_to_smooth_chest");

        //Sky Stone Slab -> Smooth Sky Stone Slab
        ItemStackToItemStackRecipeBuilder.enriching(
                    IngredientCreatorAccess.item().from(this.items, AEBlockIds.SKY_STONE_SLAB),
                    foreignTemplate(AEBlockIds.SMOOTH_SKY_STONE_SLAB)
              ).category(EnrichingBookCategory.BUILDING)
              .unlockedBy(hasForeignCriterion(AEBlockIds.SKY_STONE_SLAB))
              .save(output, basePath + "slab_to_smooth_slab");
        //Sky Stone Stairs -> Smooth Sky Stone Stairs
        ItemStackToItemStackRecipeBuilder.enriching(
                    IngredientCreatorAccess.item().from(this.items, AEBlockIds.SKY_STONE_STAIRS),
                    foreignTemplate(AEBlockIds.SMOOTH_SKY_STONE_STAIRS)
              ).category(EnrichingBookCategory.BUILDING)
              .unlockedBy(hasForeignCriterion(AEBlockIds.SKY_STONE_STAIRS))
              .save(output, basePath + "stairs_to_smooth_stairs");
        //Sky Stone Wall -> Smooth Sky Stone Wall
        ItemStackToItemStackRecipeBuilder.enriching(
                    IngredientCreatorAccess.item().from(this.items, AEBlockIds.SKY_STONE_WALL),
                    foreignTemplate(AEBlockIds.SMOOTH_SKY_STONE_WALL)
              ).category(EnrichingBookCategory.BUILDING)
              .unlockedBy(hasForeignCriterion(AEBlockIds.SKY_STONE_WALL))
              .save(output, basePath + "wall_to_smooth_wall");

        //Smooth Sky Stone -> Sky Stone Brick
        ItemStackToItemStackRecipeBuilder.enriching(
                    IngredientCreatorAccess.item().from(this.items, AEBlockIds.SMOOTH_SKY_STONE_BLOCK),
                    foreignTemplate(AEBlockIds.SKY_STONE_BRICK)
              ).category(EnrichingBookCategory.BUILDING)
              .unlockedBy(hasForeignCriterion(AEBlockIds.SMOOTH_SKY_STONE_BLOCK))
              .save(output, basePath + "smooth_to_brick");
        //Smooth Sky Stone Slab -> Sky Stone Brick Slab
        ItemStackToItemStackRecipeBuilder.enriching(
                    IngredientCreatorAccess.item().from(this.items, AEBlockIds.SMOOTH_SKY_STONE_SLAB),
                    foreignTemplate(AEBlockIds.SKY_STONE_BRICK_SLAB)
              ).category(EnrichingBookCategory.BUILDING)
              .unlockedBy(hasForeignCriterion(AEBlockIds.SMOOTH_SKY_STONE_SLAB))
              .save(output, basePath + "smooth_slab_to_brick_slab");
        //Smooth Sky Stone Stairs -> Sky Stone Brick Stairs
        ItemStackToItemStackRecipeBuilder.enriching(
                    IngredientCreatorAccess.item().from(this.items, AEBlockIds.SMOOTH_SKY_STONE_STAIRS),
                    foreignTemplate(AEBlockIds.SKY_STONE_BRICK_STAIRS)
              ).category(EnrichingBookCategory.BUILDING)
              .unlockedBy(hasForeignCriterion(AEBlockIds.SMOOTH_SKY_STONE_STAIRS))
              .save(output, basePath + "smooth_stairs_to_brick_stairs");
        //Smooth Sky Stone Wall -> Sky Stone Brick Wall
        ItemStackToItemStackRecipeBuilder.enriching(
                    IngredientCreatorAccess.item().from(this.items, AEBlockIds.SMOOTH_SKY_STONE_WALL),
                    foreignTemplate(AEBlockIds.SKY_STONE_BRICK_WALL)
              ).category(EnrichingBookCategory.BUILDING)
              .unlockedBy(hasForeignCriterion(AEBlockIds.SMOOTH_SKY_STONE_WALL))
              .save(output, basePath + "smooth_wall_to_brick_wall");

        //Sky Stone Brick -> Sky Stone Small Brick
        ItemStackToItemStackRecipeBuilder.enriching(
                    IngredientCreatorAccess.item().from(this.items, AEBlockIds.SKY_STONE_BRICK),
                    foreignTemplate(AEBlockIds.SKY_STONE_SMALL_BRICK)
              ).category(EnrichingBookCategory.BUILDING)
              .unlockedBy(hasForeignCriterion(AEBlockIds.SKY_STONE_BRICK))
              .save(output, basePath + "brick_to_small_brick");
        //Sky Stone Brick Slab -> Sky Stone Small Brick Slab
        ItemStackToItemStackRecipeBuilder.enriching(
                    IngredientCreatorAccess.item().from(this.items, AEBlockIds.SKY_STONE_BRICK_SLAB),
                    foreignTemplate(AEBlockIds.SKY_STONE_SMALL_BRICK_SLAB)
              ).category(EnrichingBookCategory.BUILDING)
              .unlockedBy(hasForeignCriterion(AEBlockIds.SKY_STONE_BRICK_SLAB))
              .save(output, basePath + "brick_slab_to_small_brick_slab");
        //Sky Stone Brick Stairs -> Sky Stone Small Brick Stairs
        ItemStackToItemStackRecipeBuilder.enriching(
                    IngredientCreatorAccess.item().from(this.items, AEBlockIds.SKY_STONE_BRICK_STAIRS),
                    foreignTemplate(AEBlockIds.SKY_STONE_SMALL_BRICK_STAIRS)
              ).category(EnrichingBookCategory.BUILDING)
              .unlockedBy(hasForeignCriterion(AEBlockIds.SKY_STONE_BRICK_STAIRS))
              .save(output, basePath + "brick_stairs_to_small_brick_stairs");
        //Sky Stone Brick Wall -> Sky Stone Small Brick Wall
        ItemStackToItemStackRecipeBuilder.enriching(
                    IngredientCreatorAccess.item().from(this.items, AEBlockIds.SKY_STONE_BRICK_WALL),
                    foreignTemplate(AEBlockIds.SKY_STONE_SMALL_BRICK_WALL)
              ).category(EnrichingBookCategory.BUILDING)
              .unlockedBy(hasForeignCriterion(AEBlockIds.SKY_STONE_BRICK_WALL))
              .save(output, basePath + "brick_wall_to_small_brick_wall");
    }

    private void addDecorativeQuartzRecipes(String basePath) {
        addDecorativeQuartzCrushingRecipes(basePath + "crushing/");
        addDecorativeQuartzEnrichingRecipes(basePath + "enriching/");
    }

    private void addDecorativeQuartzCrushingRecipes(String basePath) {
        //Smooth Certus Quartz Block -> Certus Quartz Block
        ItemStackToItemStackRecipeBuilder.crushing(
                    IngredientCreatorAccess.item().from(this.items, AEBlockIds.SMOOTH_QUARTZ_BLOCK),
                    foreignTemplate(AEBlockIds.QUARTZ_BLOCK)
              ).category(CrushingBookCategory.BUILDING)
              .unlockedBy(hasForeignCriterion(AEBlockIds.SMOOTH_QUARTZ_BLOCK))
              .save(output, basePath + "smooth_to_block");
        //Smooth Certus Quartz Slab -> Certus Quartz Slab
        ItemStackToItemStackRecipeBuilder.crushing(
                    IngredientCreatorAccess.item().from(this.items, AEBlockIds.SMOOTH_QUARTZ_SLAB),
                    foreignTemplate(AEBlockIds.QUARTZ_SLAB)
              ).category(CrushingBookCategory.BUILDING)
              .unlockedBy(hasForeignCriterion(AEBlockIds.SMOOTH_QUARTZ_SLAB))
              .save(output, basePath + "smooth_slab_to_slab");
        //Smooth Certus Quartz Stairs -> Certus Quartz Stairs
        ItemStackToItemStackRecipeBuilder.crushing(
                    IngredientCreatorAccess.item().from(this.items, AEBlockIds.SMOOTH_QUARTZ_STAIRS),
                    foreignTemplate(AEBlockIds.QUARTZ_STAIRS)
              ).category(CrushingBookCategory.BUILDING)
              .unlockedBy(hasForeignCriterion(AEBlockIds.SMOOTH_QUARTZ_STAIRS))
              .save(output, basePath + "smooth_stairs_to_stairs");
        //Smooth Certus Quartz Wall -> Certus Quartz Wall
        ItemStackToItemStackRecipeBuilder.crushing(
                    IngredientCreatorAccess.item().from(this.items, AEBlockIds.SMOOTH_QUARTZ_WALL),
                    foreignTemplate(AEBlockIds.QUARTZ_WALL)
              ).category(CrushingBookCategory.BUILDING)
              .unlockedBy(hasForeignCriterion(AEBlockIds.SMOOTH_QUARTZ_WALL))
              .save(output, basePath + "smooth_wall_to_wall");

        //Certus Quartz Block -> Cut Certus Quartz Block
        ItemStackToItemStackRecipeBuilder.crushing(
                    IngredientCreatorAccess.item().from(this.items, AEBlockIds.QUARTZ_BLOCK),
                    foreignTemplate(AEBlockIds.CUT_QUARTZ_BLOCK)
              ).category(CrushingBookCategory.BUILDING)
              .unlockedBy(hasForeignCriterion(AEBlockIds.QUARTZ_BLOCK))
              .save(output, basePath + "block_to_cut");
        //Certus Quartz Slab -> Cut Certus Quartz Slab
        ItemStackToItemStackRecipeBuilder.crushing(
                    IngredientCreatorAccess.item().from(this.items, AEBlockIds.QUARTZ_SLAB),
                    foreignTemplate(AEBlockIds.CUT_QUARTZ_SLAB)
              ).category(CrushingBookCategory.BUILDING)
              .unlockedBy(hasForeignCriterion(AEBlockIds.QUARTZ_SLAB))
              .save(output, basePath + "slab_to_cut");
        //Certus Quartz Stairs -> Cut Certus Quartz Stairs
        ItemStackToItemStackRecipeBuilder.crushing(
                    IngredientCreatorAccess.item().from(this.items, AEBlockIds.QUARTZ_STAIRS),
                    foreignTemplate(AEBlockIds.CUT_QUARTZ_STAIRS)
              ).category(CrushingBookCategory.BUILDING)
              .unlockedBy(hasForeignCriterion(AEBlockIds.QUARTZ_STAIRS))
              .save(output, basePath + "stairs_to_cut");
        //Certus Quartz Wall -> Cut Certus Quartz Wall
        ItemStackToItemStackRecipeBuilder.crushing(
                    IngredientCreatorAccess.item().from(this.items, AEBlockIds.QUARTZ_WALL),
                    foreignTemplate(AEBlockIds.CUT_QUARTZ_WALL)
              ).category(CrushingBookCategory.BUILDING)
              .unlockedBy(hasForeignCriterion(AEBlockIds.QUARTZ_WALL))
              .save(output, basePath + "wall_to_cut");

        //Cut Certus Quartz Block -> Certus Quartz Brick
        ItemStackToItemStackRecipeBuilder.crushing(
                    IngredientCreatorAccess.item().from(this.items, AEBlockIds.CUT_QUARTZ_BLOCK),
                    foreignTemplate(AEBlockIds.QUARTZ_BRICKS)
              ).category(CrushingBookCategory.BUILDING)
              .unlockedBy(hasForeignCriterion(AEBlockIds.CUT_QUARTZ_BLOCK))
              .save(output, basePath + "cut_block_to_brick");
        //Cut Certus Quartz Slab -> Certus Quartz Brick Slab
        ItemStackToItemStackRecipeBuilder.crushing(
                    IngredientCreatorAccess.item().from(this.items, AEBlockIds.CUT_QUARTZ_SLAB),
                    foreignTemplate(AEBlockIds.QUARTZ_BRICK_SLAB)
              ).category(CrushingBookCategory.BUILDING)
              .unlockedBy(hasForeignCriterion(AEBlockIds.CUT_QUARTZ_SLAB))
              .save(output, basePath + "cut_slab_to_brick");
        //Cut Certus Quartz Stairs -> Certus Quartz Brick Stairs
        ItemStackToItemStackRecipeBuilder.crushing(
                    IngredientCreatorAccess.item().from(this.items, AEBlockIds.CUT_QUARTZ_STAIRS),
                    foreignTemplate(AEBlockIds.QUARTZ_BRICK_STAIRS)
              ).category(CrushingBookCategory.BUILDING)
              .unlockedBy(hasForeignCriterion(AEBlockIds.CUT_QUARTZ_STAIRS))
              .save(output, basePath + "cut_stairs_to_brick");
        //Cut Certus Quartz Wall -> Certus Quartz Brick Wall
        ItemStackToItemStackRecipeBuilder.crushing(
                    IngredientCreatorAccess.item().from(this.items, AEBlockIds.CUT_QUARTZ_WALL),
                    foreignTemplate(AEBlockIds.QUARTZ_BRICK_WALL)
              ).category(CrushingBookCategory.BUILDING)
              .unlockedBy(hasForeignCriterion(AEBlockIds.CUT_QUARTZ_WALL))
              .save(output, basePath + "cut_wall_to_brick");

        //Certus Quartz Bricks -> Chiseled Certus Quartz
        ItemStackToItemStackRecipeBuilder.crushing(
                    IngredientCreatorAccess.item().from(this.items, AEBlockIds.QUARTZ_BRICKS),
                    foreignTemplate(AEBlockIds.CHISELED_QUARTZ_BLOCK)
              ).category(CrushingBookCategory.BUILDING)
              .unlockedBy(hasForeignCriterion(AEBlockIds.QUARTZ_BRICKS))
              .save(output, basePath + "brick_to_chiseled");
        //Certus Quartz Brick Slab -> Chiseled Certus Quartz Slab
        ItemStackToItemStackRecipeBuilder.crushing(
                    IngredientCreatorAccess.item().from(this.items, AEBlockIds.QUARTZ_BRICK_SLAB),
                    foreignTemplate(AEBlockIds.CHISELED_QUARTZ_SLAB)
              ).category(CrushingBookCategory.BUILDING)
              .unlockedBy(hasForeignCriterion(AEBlockIds.QUARTZ_BRICK_SLAB))
              .save(output, basePath + "brick_slab_to_chiseled");
        //Certus Quartz Brick Stairs -> Chiseled Certus Quartz Stairs
        ItemStackToItemStackRecipeBuilder.crushing(
                    IngredientCreatorAccess.item().from(this.items, AEBlockIds.QUARTZ_BRICK_STAIRS),
                    foreignTemplate(AEBlockIds.CHISELED_QUARTZ_STAIRS)
              ).category(CrushingBookCategory.BUILDING)
              .unlockedBy(hasForeignCriterion(AEBlockIds.QUARTZ_BRICK_STAIRS))
              .save(output, basePath + "brick_stairs_to_chiseled");
        //Certus Quartz Brick Wall -> Chiseled Certus Quartz Wall
        ItemStackToItemStackRecipeBuilder.crushing(
                    IngredientCreatorAccess.item().from(this.items, AEBlockIds.QUARTZ_BRICK_WALL),
                    foreignTemplate(AEBlockIds.CHISELED_QUARTZ_WALL)
              ).category(CrushingBookCategory.BUILDING)
              .unlockedBy(hasForeignCriterion(AEBlockIds.QUARTZ_BRICK_WALL))
              .save(output, basePath + "brick_wall_to_chiseled");

        //Chiseled Certus Quartz Block -> Certus Quartz Pillar Block
        ItemStackToItemStackRecipeBuilder.crushing(
                    IngredientCreatorAccess.item().from(this.items, AEBlockIds.CHISELED_QUARTZ_BLOCK),
                    foreignTemplate(AEBlockIds.QUARTZ_PILLAR)
              ).category(CrushingBookCategory.BUILDING)
              .unlockedBy(hasForeignCriterion(AEBlockIds.CHISELED_QUARTZ_BLOCK))
              .save(output, basePath + "chiseled_block_to_pillar");
        //Chiseled Certus Quartz Slab -> Certus Quartz Pillar Slab
        ItemStackToItemStackRecipeBuilder.crushing(
                    IngredientCreatorAccess.item().from(this.items, AEBlockIds.CHISELED_QUARTZ_SLAB),
                    foreignTemplate(AEBlockIds.QUARTZ_PILLAR_SLAB)
              ).category(CrushingBookCategory.BUILDING)
              .unlockedBy(hasForeignCriterion(AEBlockIds.CHISELED_QUARTZ_SLAB))
              .save(output, basePath + "chiseled_slab_to_pillar_slab");
        //Chiseled Certus Quartz Stairs -> Certus Quartz Pillar Stairs
        ItemStackToItemStackRecipeBuilder.crushing(
                    IngredientCreatorAccess.item().from(this.items, AEBlockIds.CHISELED_QUARTZ_STAIRS),
                    foreignTemplate(AEBlockIds.QUARTZ_PILLAR_STAIRS)
              ).category(CrushingBookCategory.BUILDING)
              .unlockedBy(hasForeignCriterion(AEBlockIds.CHISELED_QUARTZ_STAIRS))
              .save(output, basePath + "chiseled_stairs_to_pillar_stairs");
        //Chiseled Certus Quartz Wall -> Certus Quartz Pillar Wall
        ItemStackToItemStackRecipeBuilder.crushing(
                    IngredientCreatorAccess.item().from(this.items, AEBlockIds.CHISELED_QUARTZ_WALL),
                    foreignTemplate(AEBlockIds.QUARTZ_PILLAR_WALL)
              ).category(CrushingBookCategory.BUILDING)
              .unlockedBy(hasForeignCriterion(AEBlockIds.CHISELED_QUARTZ_WALL))
              .save(output, basePath + "chiseled_wall_to_pillar_wall");
    }

    //TODO - 26.3: certus quartz bricks, smooth certus quartz, cut certus quartz?
    private void addDecorativeQuartzEnrichingRecipes(String basePath) {
        //Certus Quartz Pillar Block -> Chiseled Certus Quartz Block
        ItemStackToItemStackRecipeBuilder.enriching(
                    IngredientCreatorAccess.item().from(this.items, AEBlockIds.QUARTZ_PILLAR),
                    foreignTemplate(AEBlockIds.CHISELED_QUARTZ_BLOCK)
              ).category(EnrichingBookCategory.BUILDING)
              .unlockedBy(hasForeignCriterion(AEBlockIds.QUARTZ_PILLAR))
              .save(output, basePath + "pillar_to_chiseled_block");
        //Certus Quartz Pillar Slab -> Chiseled Certus Quartz Slab
        ItemStackToItemStackRecipeBuilder.enriching(
                    IngredientCreatorAccess.item().from(this.items, AEBlockIds.QUARTZ_PILLAR_SLAB),
                    foreignTemplate(AEBlockIds.CHISELED_QUARTZ_SLAB)
              ).category(EnrichingBookCategory.BUILDING)
              .unlockedBy(hasForeignCriterion(AEBlockIds.QUARTZ_PILLAR_SLAB))
              .save(output, basePath + "pillar_slab_to_chiseled_slab");
        //Certus Quartz Pillar Stairs -> Chiseled Certus Quartz Stairs
        ItemStackToItemStackRecipeBuilder.enriching(
                    IngredientCreatorAccess.item().from(this.items, AEBlockIds.QUARTZ_PILLAR_STAIRS),
                    foreignTemplate(AEBlockIds.CHISELED_QUARTZ_STAIRS)
              ).category(EnrichingBookCategory.BUILDING)
              .unlockedBy(hasForeignCriterion(AEBlockIds.QUARTZ_PILLAR_STAIRS))
              .save(output, basePath + "pillar_stairs_to_chiseled_stairs");
        //Certus Quartz Pillar Wall -> Chiseled Certus Quartz Wall
        ItemStackToItemStackRecipeBuilder.enriching(
                    IngredientCreatorAccess.item().from(this.items, AEBlockIds.QUARTZ_PILLAR_WALL),
                    foreignTemplate(AEBlockIds.CHISELED_QUARTZ_WALL)
              ).category(EnrichingBookCategory.BUILDING)
              .unlockedBy(hasForeignCriterion(AEBlockIds.QUARTZ_PILLAR_WALL))
              .save(output, basePath + "pillar_wall_to_chiseled_wall");

        //Chiseled Certus Quartz Block -> Certus Quartz Bricks
        ItemStackToItemStackRecipeBuilder.enriching(
                    IngredientCreatorAccess.item().from(this.items, AEBlockIds.CHISELED_QUARTZ_BLOCK),
                    foreignTemplate(AEBlockIds.QUARTZ_BRICKS)
              ).category(EnrichingBookCategory.BUILDING)
              .unlockedBy(hasForeignCriterion(AEBlockIds.CHISELED_QUARTZ_BLOCK))
              .save(output, basePath + "chiseled_block_to_brick");
        //Chiseled Certus Quartz Slab -> Certus Quartz Brick Slab
        ItemStackToItemStackRecipeBuilder.enriching(
                    IngredientCreatorAccess.item().from(this.items, AEBlockIds.CHISELED_QUARTZ_SLAB),
                    foreignTemplate(AEBlockIds.QUARTZ_BRICK_SLAB)
              ).category(EnrichingBookCategory.BUILDING)
              .unlockedBy(hasForeignCriterion(AEBlockIds.CHISELED_QUARTZ_SLAB))
              .save(output, basePath + "chiseled_slab_to_brick");
        //Chiseled Certus Quartz Stairs -> Certus Quartz Brick Stairs
        ItemStackToItemStackRecipeBuilder.enriching(
                    IngredientCreatorAccess.item().from(this.items, AEBlockIds.CHISELED_QUARTZ_STAIRS),
                    foreignTemplate(AEBlockIds.QUARTZ_BRICK_STAIRS)
              ).category(EnrichingBookCategory.BUILDING)
              .unlockedBy(hasForeignCriterion(AEBlockIds.CHISELED_QUARTZ_STAIRS))
              .save(output, basePath + "chiseled_stairs_to_brick");
        //Chiseled Certus Quartz Wall -> Certus Quartz Brick Wall
        ItemStackToItemStackRecipeBuilder.enriching(
                    IngredientCreatorAccess.item().from(this.items, AEBlockIds.CHISELED_QUARTZ_WALL),
                    foreignTemplate(AEBlockIds.QUARTZ_BRICK_WALL)
              ).category(EnrichingBookCategory.BUILDING)
              .unlockedBy(hasForeignCriterion(AEBlockIds.CHISELED_QUARTZ_WALL))
              .save(output, basePath + "chiseled_wall_to_brick");

        //Certus Quartz Bricks -> Cut Certus Quartz
        ItemStackToItemStackRecipeBuilder.enriching(
                    IngredientCreatorAccess.item().from(this.items, AEBlockIds.QUARTZ_BRICKS),
                    foreignTemplate(AEBlockIds.CUT_QUARTZ_BLOCK)
              ).category(EnrichingBookCategory.BUILDING)
              .unlockedBy(hasForeignCriterion(AEBlockIds.QUARTZ_BRICKS))
              .save(output, basePath + "brick_block_to_cut");
        //Certus Quartz Brick Slab -> Cut Certus Quartz Slab
        ItemStackToItemStackRecipeBuilder.enriching(
                    IngredientCreatorAccess.item().from(this.items, AEBlockIds.QUARTZ_BRICK_SLAB),
                    foreignTemplate(AEBlockIds.CUT_QUARTZ_SLAB)
              ).category(EnrichingBookCategory.BUILDING)
              .unlockedBy(hasForeignCriterion(AEBlockIds.QUARTZ_BRICK_SLAB))
              .save(output, basePath + "brick_slab_to_cut");
        //Certus Quartz Brick Stairs -> Cut Certus Quartz Stairs
        ItemStackToItemStackRecipeBuilder.enriching(
                    IngredientCreatorAccess.item().from(this.items, AEBlockIds.QUARTZ_BRICK_STAIRS),
                    foreignTemplate(AEBlockIds.CUT_QUARTZ_STAIRS)
              ).category(EnrichingBookCategory.BUILDING)
              .unlockedBy(hasForeignCriterion(AEBlockIds.QUARTZ_BRICK_STAIRS))
              .save(output, basePath + "brick_stairs_to_cut");
        //Certus Quartz Brick Wall -> Cut Certus Quartz Wall
        ItemStackToItemStackRecipeBuilder.enriching(
                    IngredientCreatorAccess.item().from(this.items, AEBlockIds.QUARTZ_BRICK_WALL),
                    foreignTemplate(AEBlockIds.CUT_QUARTZ_WALL)
              ).category(EnrichingBookCategory.BUILDING)
              .unlockedBy(hasForeignCriterion(AEBlockIds.QUARTZ_BRICK_WALL))
              .save(output, basePath + "brick_wall_to_cut");

        //Cut Certus Quartz Block -> Certus Quartz Block
        ItemStackToItemStackRecipeBuilder.enriching(
                    IngredientCreatorAccess.item().from(this.items, AEBlockIds.CUT_QUARTZ_BLOCK),
                    foreignTemplate(AEBlockIds.QUARTZ_BLOCK)
              ).category(EnrichingBookCategory.BUILDING)
              .unlockedBy(hasForeignCriterion(AEBlockIds.CUT_QUARTZ_BLOCK))
              .save(output, basePath + "cut_block_to_block");
        //Cut Certus Quartz Slab -> Certus Quartz Slab
        ItemStackToItemStackRecipeBuilder.enriching(
                    IngredientCreatorAccess.item().from(this.items, AEBlockIds.CUT_QUARTZ_SLAB),
                    foreignTemplate(AEBlockIds.QUARTZ_SLAB)
              ).category(EnrichingBookCategory.BUILDING)
              .unlockedBy(hasForeignCriterion(AEBlockIds.CUT_QUARTZ_SLAB))
              .save(output, basePath + "cut_slab_to_slab");
        //Cut Certus Quartz Stairs -> Certus Quartz Stairs
        ItemStackToItemStackRecipeBuilder.enriching(
                    IngredientCreatorAccess.item().from(this.items, AEBlockIds.CUT_QUARTZ_STAIRS),
                    foreignTemplate(AEBlockIds.QUARTZ_STAIRS)
              ).category(EnrichingBookCategory.BUILDING)
              .unlockedBy(hasForeignCriterion(AEBlockIds.CUT_QUARTZ_STAIRS))
              .save(output, basePath + "cut_stairs_to_stairs");
        //Cut Certus Quartz Wall -> Certus Quartz Wall
        ItemStackToItemStackRecipeBuilder.enriching(
                    IngredientCreatorAccess.item().from(this.items, AEBlockIds.CUT_QUARTZ_WALL),
                    foreignTemplate(AEBlockIds.QUARTZ_WALL)
              ).category(EnrichingBookCategory.BUILDING)
              .unlockedBy(hasForeignCriterion(AEBlockIds.CUT_QUARTZ_WALL))
              .save(output, basePath + "cut_wall_to_wall");

        //Certus Quartz Block -> Smooth Certus Quartz Block
        ItemStackToItemStackRecipeBuilder.enriching(
                    IngredientCreatorAccess.item().from(this.items, AEBlockIds.QUARTZ_BLOCK),
                    foreignTemplate(AEBlockIds.SMOOTH_QUARTZ_BLOCK)
              ).category(EnrichingBookCategory.BUILDING)
              .unlockedBy(hasForeignCriterion(AEBlockIds.QUARTZ_BLOCK))
              .save(output, basePath + "block_to_smooth_block");
        //Certus Quartz Slab -> Smooth Certus Quartz Slab
        ItemStackToItemStackRecipeBuilder.enriching(
                    IngredientCreatorAccess.item().from(this.items, AEBlockIds.QUARTZ_SLAB),
                    foreignTemplate(AEBlockIds.SMOOTH_QUARTZ_SLAB)
              ).category(EnrichingBookCategory.BUILDING)
              .unlockedBy(hasForeignCriterion(AEBlockIds.QUARTZ_SLAB))
              .save(output, basePath + "slab_to_smooth_slab");
        //Certus Quartz Stairs -> Smooth Certus Quartz Stairs
        ItemStackToItemStackRecipeBuilder.enriching(
                    IngredientCreatorAccess.item().from(this.items, AEBlockIds.QUARTZ_STAIRS),
                    foreignTemplate(AEBlockIds.SMOOTH_QUARTZ_STAIRS)
              ).category(EnrichingBookCategory.BUILDING)
              .unlockedBy(hasForeignCriterion(AEBlockIds.QUARTZ_STAIRS))
              .save(output, basePath + "stairs_to_smooth_stairs");
        //Certus Quartz Wall -> Smooth Certus Quartz Wall
        ItemStackToItemStackRecipeBuilder.enriching(
                    IngredientCreatorAccess.item().from(this.items, AEBlockIds.QUARTZ_WALL),
                    foreignTemplate(AEBlockIds.SMOOTH_QUARTZ_WALL)
              ).category(EnrichingBookCategory.BUILDING)
              .unlockedBy(hasForeignCriterion(AEBlockIds.QUARTZ_WALL))
              .save(output, basePath + "wall_to_smooth_wall");
    }
}