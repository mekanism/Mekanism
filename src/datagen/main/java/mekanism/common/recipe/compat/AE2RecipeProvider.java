package mekanism.common.recipe.compat;

import appeng.core.definitions.AEBlocks;
import appeng.core.definitions.AEItems;
import java.util.function.Consumer;
import mekanism.api.annotations.ParametersAreNotNullByDefault;
import mekanism.api.datagen.recipe.builder.ItemStackToItemStackRecipeBuilder;
import mekanism.api.recipes.ingredients.creator.IngredientCreatorAccess;
import mekanism.common.Mekanism;
import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.item.ItemStack;

@ParametersAreNotNullByDefault
public class AE2RecipeProvider extends CompatRecipeProvider {

    public AE2RecipeProvider() {
        super("ae2");
    }

    @Override
    protected void registerRecipes(Consumer<FinishedRecipe> consumer, String basePath) {
        //Certus Crystal -> Certus Dust
        ItemStackToItemStackRecipeBuilder.crushing(
                    IngredientCreatorAccess.item().from(ItemTags.create(new ResourceLocation("forge", "gems/certus_quartz"))),
                    AEItems.CERTUS_QUARTZ_DUST.stack(1)
              ).addCondition(modLoaded)
              .build(consumer, Mekanism.rl(basePath + "certus_crystal_to_dust"));

        //Fluix Crystal -> Fluix Dust
        ItemStackToItemStackRecipeBuilder.crushing(
                    IngredientCreatorAccess.item().from(AEItems.FLUIX_CRYSTAL),
                    AEItems.FLUIX_DUST.stack(1)
              ).addCondition(modLoaded)
              .build(consumer, Mekanism.rl(basePath + "fluix_crystal_to_dust"));

        //Sky Stone -> Sky Stone Dust
        ItemStackToItemStackRecipeBuilder.crushing(
                    IngredientCreatorAccess.item().from(AEBlocks.SKY_STONE_BLOCK),
                    AEItems.SKY_DUST.stack(1)
              ).addCondition(modLoaded)
              .build(consumer, Mekanism.rl(basePath + "sky_stone_to_dust"));

        //Sky Stone Dust -> Sky Stone
        ItemStackToItemStackRecipeBuilder.enriching(
                    IngredientCreatorAccess.item().from(AEItems.SKY_DUST),
                    AEBlocks.SKY_STONE_BLOCK.stack(1)
              ).addCondition(modLoaded)
              .build(consumer, Mekanism.rl(basePath + "sky_stone_dust_to_sky_stone"));

        addDecorativeRecipes(consumer, basePath + "decorative/");
    }

    private void addDecorativeRecipes(Consumer<FinishedRecipe> consumer, String basePath) {
        addDecorativeQuartzRecipes(consumer, basePath + "certus_quartz/");
        addDecorativeSkyStoneRecipes(consumer, basePath + "sky_stone/");
    }

    private void addDecorativeSkyStoneRecipes(Consumer<FinishedRecipe> consumer, String basePath) {
        addDecorativeCrushingSkyStoneRecipes(consumer, basePath + "crushing/");
        addDecorativeEnrichingSkyStoneRecipes(consumer, basePath + "enriching/");
    }

    private void addDecorativeCrushingSkyStoneRecipes(Consumer<FinishedRecipe> consumer, String basePath) {
        //Smooth Sky Stone Chest -> Sky Stone Chest
        ItemStackToItemStackRecipeBuilder.crushing(
                    IngredientCreatorAccess.item().from(AEBlocks.SMOOTH_SKY_STONE_CHEST),
                    AEBlocks.SKY_STONE_CHEST.stack(1)
              ).addCondition(modLoaded)
              .build(consumer, Mekanism.rl(basePath + "smooth_chest_to_chest"));

        //Smooth Sky Stone -> Sky Stone
        ItemStackToItemStackRecipeBuilder.crushing(
                    IngredientCreatorAccess.item().from(AEBlocks.SMOOTH_SKY_STONE_BLOCK),
                    AEBlocks.SKY_STONE_BLOCK.stack(1)
              ).addCondition(modLoaded)
              .build(consumer, Mekanism.rl(basePath + "smooth_to_stone"));
        //Smooth Sky Stone Slab -> Sky Stone Slab
        ItemStackToItemStackRecipeBuilder.enriching(
                    IngredientCreatorAccess.item().from(AEBlocks.SMOOTH_SKY_STONE_SLAB),
                    AEBlocks.SKY_STONE_SLAB.stack(1)
              ).addCondition(modLoaded)
              .build(consumer, Mekanism.rl(basePath + "smooth_slab_to_slab"));
        //Smooth Sky Stone Stairs -> Sky Stone Stairs
        ItemStackToItemStackRecipeBuilder.enriching(
                    IngredientCreatorAccess.item().from(AEBlocks.SMOOTH_SKY_STONE_STAIRS),
                    AEBlocks.SKY_STONE_STAIRS.stack(1)
              ).addCondition(modLoaded)
              .build(consumer, Mekanism.rl(basePath + "smooth_stairs_to_stairs"));
        //Smooth Sky Stone Wall -> Sky Stone Wall
        ItemStackToItemStackRecipeBuilder.enriching(
                    IngredientCreatorAccess.item().from(AEBlocks.SMOOTH_SKY_STONE_WALL),
                    AEBlocks.SKY_STONE_WALL.stack(1)
              ).addCondition(modLoaded)
              .build(consumer, Mekanism.rl(basePath + "smooth_wall_to_wall"));

        //Sky Stone Brick -> Smooth Sky Stone
        ItemStackToItemStackRecipeBuilder.enriching(
                    IngredientCreatorAccess.item().from(AEBlocks.SKY_STONE_BRICK),
                    AEBlocks.SMOOTH_SKY_STONE_BLOCK.stack(1)
              ).addCondition(modLoaded)
              .build(consumer, Mekanism.rl(basePath + "brick_to_smooth"));
        //Sky Stone Brick Slab -> Smooth Sky Stone Slab
        ItemStackToItemStackRecipeBuilder.enriching(
                    IngredientCreatorAccess.item().from(AEBlocks.SKY_STONE_BRICK_SLAB),
                    AEBlocks.SMOOTH_SKY_STONE_SLAB.stack(1)
              ).addCondition(modLoaded)
              .build(consumer, Mekanism.rl(basePath + "brick_slab_to_smooth_slab"));
        //Sky Stone Brick Stairs -> Smooth Sky Stone Stairs
        ItemStackToItemStackRecipeBuilder.enriching(
                    IngredientCreatorAccess.item().from(AEBlocks.SKY_STONE_BRICK_STAIRS),
                    AEBlocks.SMOOTH_SKY_STONE_STAIRS.stack(1)
              ).addCondition(modLoaded)
              .build(consumer, Mekanism.rl(basePath + "brick_stairs_to_smooth_stairs"));
        //Sky Stone Brick Wall -> Smooth Sky Stone Wall
        ItemStackToItemStackRecipeBuilder.enriching(
                    IngredientCreatorAccess.item().from(AEBlocks.SKY_STONE_BRICK_WALL),
                    AEBlocks.SMOOTH_SKY_STONE_WALL.stack(1)
              ).addCondition(modLoaded)
              .build(consumer, Mekanism.rl(basePath + "brick_wall_to_smooth_wall"));

        //Sky Stone Small Brick -> Sky Stone Brick
        ItemStackToItemStackRecipeBuilder.enriching(
                    IngredientCreatorAccess.item().from(AEBlocks.SKY_STONE_SMALL_BRICK),
                    AEBlocks.SKY_STONE_BRICK.stack(1)
              ).addCondition(modLoaded)
              .build(consumer, Mekanism.rl(basePath + "small_brick_to_brick"));
        //Sky Stone Small Brick Slab -> Sky Stone Brick Slab
        ItemStackToItemStackRecipeBuilder.enriching(
                    IngredientCreatorAccess.item().from(AEBlocks.SKY_STONE_SMALL_BRICK_SLAB),
                    AEBlocks.SKY_STONE_BRICK_SLAB.stack(1)
              ).addCondition(modLoaded)
              .build(consumer, Mekanism.rl(basePath + "small_brick_slab_to_brick_slab"));
        //Sky Stone Small Brick Stairs -> Sky Stone Brick Stairs
        ItemStackToItemStackRecipeBuilder.enriching(
                    IngredientCreatorAccess.item().from(AEBlocks.SKY_STONE_SMALL_BRICK_STAIRS),
                    AEBlocks.SKY_STONE_BRICK_STAIRS.stack(1)
              ).addCondition(modLoaded)
              .build(consumer, Mekanism.rl(basePath + "small_brick_stairs_to_brick_stairs"));
        //Sky Stone Small Brick Wall -> Sky Stone Brick Wall
        ItemStackToItemStackRecipeBuilder.enriching(
                    IngredientCreatorAccess.item().from(AEBlocks.SKY_STONE_SMALL_BRICK_WALL),
                    AEBlocks.SKY_STONE_BRICK_WALL.stack(1)
              ).addCondition(modLoaded)
              .build(consumer, Mekanism.rl(basePath + "small_brick_wall_to_brick_wall"));
    }

    private void addDecorativeEnrichingSkyStoneRecipes(Consumer<FinishedRecipe> consumer, String basePath) {
        //Sky Stone Chest -> Smooth Sky Stone Chest
        ItemStackToItemStackRecipeBuilder.enriching(
                    IngredientCreatorAccess.item().from(AEBlocks.SKY_STONE_CHEST),
                    AEBlocks.SMOOTH_SKY_STONE_CHEST.stack(1)
              ).addCondition(modLoaded)
              .build(consumer, Mekanism.rl(basePath + "chest_to_smooth_chest"));

        //Sky Stone Slab -> Smooth Sky Stone Slab
        ItemStackToItemStackRecipeBuilder.enriching(
                    IngredientCreatorAccess.item().from(AEBlocks.SKY_STONE_SLAB),
                    AEBlocks.SMOOTH_SKY_STONE_SLAB.stack(1)
              ).addCondition(modLoaded)
              .build(consumer, Mekanism.rl(basePath + "slab_to_smooth_slab"));
        //Sky Stone Stairs -> Smooth Sky Stone Stairs
        ItemStackToItemStackRecipeBuilder.enriching(
                    IngredientCreatorAccess.item().from(AEBlocks.SKY_STONE_STAIRS),
                    AEBlocks.SMOOTH_SKY_STONE_STAIRS.stack(1)
              ).addCondition(modLoaded)
              .build(consumer, Mekanism.rl(basePath + "stairs_to_smooth_stairs"));
        //Sky Stone Wall -> Smooth Sky Stone Wall
        ItemStackToItemStackRecipeBuilder.enriching(
                    IngredientCreatorAccess.item().from(AEBlocks.SKY_STONE_WALL),
                    AEBlocks.SMOOTH_SKY_STONE_WALL.stack(1)
              ).addCondition(modLoaded)
              .build(consumer, Mekanism.rl(basePath + "wall_to_smooth_wall"));

        //Smooth Sky Stone -> Sky Stone Brick
        ItemStackToItemStackRecipeBuilder.enriching(
                    IngredientCreatorAccess.item().from(AEBlocks.SMOOTH_SKY_STONE_BLOCK),
                    AEBlocks.SKY_STONE_BRICK.stack(1)
              ).addCondition(modLoaded)
              .build(consumer, Mekanism.rl(basePath + "smooth_to_brick"));
        //Smooth Sky Stone Slab -> Sky Stone Brick Slab
        ItemStackToItemStackRecipeBuilder.enriching(
                    IngredientCreatorAccess.item().from(AEBlocks.SMOOTH_SKY_STONE_SLAB),
                    AEBlocks.SKY_STONE_BRICK_SLAB.stack(1)
              ).addCondition(modLoaded)
              .build(consumer, Mekanism.rl(basePath + "smooth_slab_to_brick_slab"));
        //Smooth Sky Stone Stairs -> Sky Stone Brick Stairs
        ItemStackToItemStackRecipeBuilder.enriching(
                    IngredientCreatorAccess.item().from(AEBlocks.SMOOTH_SKY_STONE_STAIRS),
                    AEBlocks.SKY_STONE_BRICK_STAIRS.stack(1)
              ).addCondition(modLoaded)
              .build(consumer, Mekanism.rl(basePath + "smooth_stairs_to_brick_stairs"));
        //Smooth Sky Stone Wall -> Sky Stone Brick Wall
        ItemStackToItemStackRecipeBuilder.enriching(
                    IngredientCreatorAccess.item().from(AEBlocks.SMOOTH_SKY_STONE_WALL),
                    AEBlocks.SKY_STONE_BRICK_WALL.stack(1)
              ).addCondition(modLoaded)
              .build(consumer, Mekanism.rl(basePath + "smooth_wall_to_brick_wall"));

        //Sky Stone Brick -> Sky Stone Small Brick
        ItemStackToItemStackRecipeBuilder.enriching(
                    IngredientCreatorAccess.item().from(AEBlocks.SKY_STONE_BRICK),
                    AEBlocks.SKY_STONE_SMALL_BRICK.stack(1)
              ).addCondition(modLoaded)
              .build(consumer, Mekanism.rl(basePath + "brick_to_small_brick"));
        //Sky Stone Brick Slab -> Sky Stone Small Brick Slab
        ItemStackToItemStackRecipeBuilder.enriching(
                    IngredientCreatorAccess.item().from(AEBlocks.SKY_STONE_BRICK_SLAB),
                    AEBlocks.SKY_STONE_SMALL_BRICK_SLAB.stack(1)
              ).addCondition(modLoaded)
              .build(consumer, Mekanism.rl(basePath + "brick_slab_to_small_brick_slab"));
        //Sky Stone Brick Stairs -> Sky Stone Small Brick Stairs
        ItemStackToItemStackRecipeBuilder.enriching(
                    IngredientCreatorAccess.item().from(AEBlocks.SKY_STONE_BRICK_STAIRS),
                    AEBlocks.SKY_STONE_SMALL_BRICK_STAIRS.stack(1)
              ).addCondition(modLoaded)
              .build(consumer, Mekanism.rl(basePath + "brick_stairs_to_small_brick_stairs"));
        //Sky Stone Brick Wall -> Sky Stone Small Brick Wall
        ItemStackToItemStackRecipeBuilder.enriching(
                    IngredientCreatorAccess.item().from(AEBlocks.SKY_STONE_BRICK_WALL),
                    AEBlocks.SKY_STONE_SMALL_BRICK_WALL.stack(1)
              ).addCondition(modLoaded)
              .build(consumer, Mekanism.rl(basePath + "brick_wall_to_small_brick_wall"));
    }

    private void addDecorativeQuartzRecipes(Consumer<FinishedRecipe> consumer, String basePath) {
        addDecorativeQuartzCrushingRecipes(consumer, basePath + "crushing/");
        addDecorativeQuartzEnrichingRecipes(consumer, basePath + "enriching/");
    }

    private void addDecorativeQuartzCrushingRecipes(Consumer<FinishedRecipe> consumer, String basePath) {
        //Certus Quartz Block -> Chiseled Certus Quartz Block
        ItemStackToItemStackRecipeBuilder.crushing(
                    IngredientCreatorAccess.item().from(AEBlocks.QUARTZ_BLOCK),
                    new ItemStack(AEBlocks.CHISELED_QUARTZ_BLOCK)
              ).addCondition(modLoaded)
              .build(consumer, Mekanism.rl(basePath + "block_to_chiseled_block"));
        //Certus Quartz Slab -> Chiseled Certus Quartz Slab
        ItemStackToItemStackRecipeBuilder.crushing(
                    IngredientCreatorAccess.item().from(AEBlocks.QUARTZ_SLAB),
                    new ItemStack(AEBlocks.CHISELED_QUARTZ_SLAB)
              ).addCondition(modLoaded)
              .build(consumer, Mekanism.rl(basePath + "slab_to_chiseled_slab"));
        //Certus Quartz Stairs -> Chiseled Certus Quartz Stairs
        ItemStackToItemStackRecipeBuilder.crushing(
                    IngredientCreatorAccess.item().from(AEBlocks.QUARTZ_STAIRS),
                    new ItemStack(AEBlocks.CHISELED_QUARTZ_STAIRS)
              ).addCondition(modLoaded)
              .build(consumer, Mekanism.rl(basePath + "stairs_to_chiseled_stairs"));
        //Certus Quartz Wall -> Chiseled Certus Quartz Wall
        ItemStackToItemStackRecipeBuilder.crushing(
                    IngredientCreatorAccess.item().from(AEBlocks.QUARTZ_WALL),
                    new ItemStack(AEBlocks.CHISELED_QUARTZ_WALL)
              ).addCondition(modLoaded)
              .build(consumer, Mekanism.rl(basePath + "wall_to_chiseled_wall"));

        //Chiseled Certus Quartz Block -> Certus Quartz Pillar Block
        ItemStackToItemStackRecipeBuilder.crushing(
                    IngredientCreatorAccess.item().from(AEBlocks.CHISELED_QUARTZ_BLOCK),
                    new ItemStack(AEBlocks.QUARTZ_PILLAR)
              ).addCondition(modLoaded)
              .build(consumer, Mekanism.rl(basePath + "chiseled_block_to_pillar"));
        //Chiseled Certus Quartz Slab -> Certus Quartz Pillar Slab
        ItemStackToItemStackRecipeBuilder.crushing(
                    IngredientCreatorAccess.item().from(AEBlocks.CHISELED_QUARTZ_SLAB),
                    new ItemStack(AEBlocks.QUARTZ_PILLAR_SLAB)
              ).addCondition(modLoaded)
              .build(consumer, Mekanism.rl(basePath + "chiseled_slab_to_pillar_slab"));
        //Chiseled Certus Quartz Stairs -> Certus Quartz Pillar Stairs
        ItemStackToItemStackRecipeBuilder.crushing(
                    IngredientCreatorAccess.item().from(AEBlocks.CHISELED_QUARTZ_STAIRS),
                    new ItemStack(AEBlocks.QUARTZ_PILLAR_STAIRS)
              ).addCondition(modLoaded)
              .build(consumer, Mekanism.rl(basePath + "chiseled_stairs_to_pillar_stairs"));
        //Chiseled Certus Quartz Wall -> Certus Quartz Pillar Wall
        ItemStackToItemStackRecipeBuilder.crushing(
                    IngredientCreatorAccess.item().from(AEBlocks.CHISELED_QUARTZ_WALL),
                    new ItemStack(AEBlocks.QUARTZ_PILLAR_WALL)
              ).addCondition(modLoaded)
              .build(consumer, Mekanism.rl(basePath + "chiseled_wall_to_pillar_wall"));

        //Certus Quartz Pillar Block -> Certus Quartz Block
        ItemStackToItemStackRecipeBuilder.crushing(
                    IngredientCreatorAccess.item().from(AEBlocks.QUARTZ_PILLAR),
                    new ItemStack(AEBlocks.QUARTZ_BLOCK)
              ).addCondition(modLoaded)
              .build(consumer, Mekanism.rl(basePath + "pillar_to_block"));
        //Certus Quartz Pillar Slab -> Certus Quartz Slab
        ItemStackToItemStackRecipeBuilder.crushing(
                    IngredientCreatorAccess.item().from(AEBlocks.QUARTZ_PILLAR_SLAB),
                    new ItemStack(AEBlocks.QUARTZ_SLAB)
              ).addCondition(modLoaded)
              .build(consumer, Mekanism.rl(basePath + "pillar_slab_to_slab"));
        //Certus Quartz Pillar Stairs -> Certus Quartz Stairs
        ItemStackToItemStackRecipeBuilder.crushing(
                    IngredientCreatorAccess.item().from(AEBlocks.QUARTZ_PILLAR_STAIRS),
                    new ItemStack(AEBlocks.QUARTZ_STAIRS)
              ).addCondition(modLoaded)
              .build(consumer, Mekanism.rl(basePath + "pillar_stairs_to_stairs"));
        //Certus Quartz Pillar Wall -> Certus Quartz Wall
        ItemStackToItemStackRecipeBuilder.crushing(
                    IngredientCreatorAccess.item().from(AEBlocks.QUARTZ_PILLAR_WALL),
                    new ItemStack(AEBlocks.QUARTZ_WALL)
              ).addCondition(modLoaded)
              .build(consumer, Mekanism.rl(basePath + "pillar_wall_to_wall"));
    }

    private void addDecorativeQuartzEnrichingRecipes(Consumer<FinishedRecipe> consumer, String basePath) {
        //Chiseled Certus Quartz Block -> Certus Quartz Block
        ItemStackToItemStackRecipeBuilder.enriching(
                    IngredientCreatorAccess.item().from(AEBlocks.CHISELED_QUARTZ_BLOCK),
                    new ItemStack(AEBlocks.QUARTZ_BLOCK)
              ).addCondition(modLoaded)
              .build(consumer, Mekanism.rl(basePath + "chiseled_block_to_block"));
        //Chiseled Certus Quartz Slab -> Certus Quartz Slab
        ItemStackToItemStackRecipeBuilder.enriching(
                    IngredientCreatorAccess.item().from(AEBlocks.CHISELED_QUARTZ_SLAB),
                    new ItemStack(AEBlocks.QUARTZ_SLAB)
              ).addCondition(modLoaded)
              .build(consumer, Mekanism.rl(basePath + "chiseled_slab_to_slab"));
        //Chiseled Certus Quartz Stairs -> Certus Quartz Stairs
        ItemStackToItemStackRecipeBuilder.enriching(
                    IngredientCreatorAccess.item().from(AEBlocks.CHISELED_QUARTZ_STAIRS),
                    new ItemStack(AEBlocks.QUARTZ_STAIRS)
              ).addCondition(modLoaded)
              .build(consumer, Mekanism.rl(basePath + "chiseled_stairs_to_stairs"));
        //Chiseled Certus Quartz Wall -> Certus Quartz Wall
        ItemStackToItemStackRecipeBuilder.enriching(
                    IngredientCreatorAccess.item().from(AEBlocks.CHISELED_QUARTZ_WALL),
                    new ItemStack(AEBlocks.QUARTZ_WALL)
              ).addCondition(modLoaded)
              .build(consumer, Mekanism.rl(basePath + "chiseled_wall_to_wall"));

        //Certus Quartz Pillar Block -> Chiseled Certus Quartz Block
        ItemStackToItemStackRecipeBuilder.enriching(
                    IngredientCreatorAccess.item().from(AEBlocks.QUARTZ_PILLAR),
                    new ItemStack(AEBlocks.CHISELED_QUARTZ_BLOCK)
              ).addCondition(modLoaded)
              .build(consumer, Mekanism.rl(basePath + "pillar_to_chiseled_block"));
        //Certus Quartz Pillar Slab -> Chiseled Certus Quartz Slab
        ItemStackToItemStackRecipeBuilder.enriching(
                    IngredientCreatorAccess.item().from(AEBlocks.QUARTZ_PILLAR_SLAB),
                    new ItemStack(AEBlocks.CHISELED_QUARTZ_SLAB)
              ).addCondition(modLoaded)
              .build(consumer, Mekanism.rl(basePath + "pillar_slab_to_chiseled_slab"));
        //Certus Quartz Pillar Stairs -> Chiseled Certus Quartz Stairs
        ItemStackToItemStackRecipeBuilder.enriching(
                    IngredientCreatorAccess.item().from(AEBlocks.QUARTZ_PILLAR_STAIRS),
                    new ItemStack(AEBlocks.CHISELED_QUARTZ_STAIRS)
              ).addCondition(modLoaded)
              .build(consumer, Mekanism.rl(basePath + "pillar_stairs_to_chiseled_stairs"));
        //Certus Quartz Pillar Wall -> Chiseled Certus Quartz Wall
        ItemStackToItemStackRecipeBuilder.enriching(
                    IngredientCreatorAccess.item().from(AEBlocks.QUARTZ_PILLAR_WALL),
                    new ItemStack(AEBlocks.CHISELED_QUARTZ_WALL)
              ).addCondition(modLoaded)
              .build(consumer, Mekanism.rl(basePath + "pillar_wall_to_chiseled_wall"));

        //Certus Quartz Block -> Certus Quartz Pillar Block
        ItemStackToItemStackRecipeBuilder.enriching(
                    IngredientCreatorAccess.item().from(AEBlocks.QUARTZ_BLOCK),
                    new ItemStack(AEBlocks.QUARTZ_PILLAR)
              ).addCondition(modLoaded)
              .build(consumer, Mekanism.rl(basePath + "block_to_pillar"));
        //Certus Quartz Slab -> Certus Quartz Pillar Slab
        ItemStackToItemStackRecipeBuilder.enriching(
                    IngredientCreatorAccess.item().from(AEBlocks.QUARTZ_SLAB),
                    new ItemStack(AEBlocks.QUARTZ_PILLAR_SLAB)
              ).addCondition(modLoaded)
              .build(consumer, Mekanism.rl(basePath + "slab_to_pillar_slab"));
        //Certus Quartz Stairs -> Certus Quartz Pillar Stairs
        ItemStackToItemStackRecipeBuilder.enriching(
                    IngredientCreatorAccess.item().from(AEBlocks.QUARTZ_STAIRS),
                    new ItemStack(AEBlocks.QUARTZ_PILLAR_STAIRS)
              ).addCondition(modLoaded)
              .build(consumer, Mekanism.rl(basePath + "stairs_to_pillar_stairs"));
        //Certus Quartz Wall -> Certus Quartz Pillar Wall
        ItemStackToItemStackRecipeBuilder.enriching(
                    IngredientCreatorAccess.item().from(AEBlocks.QUARTZ_WALL),
                    new ItemStack(AEBlocks.QUARTZ_PILLAR_WALL)
              ).addCondition(modLoaded)
              .build(consumer, Mekanism.rl(basePath + "wall_to_pillar_wall"));
    }
}