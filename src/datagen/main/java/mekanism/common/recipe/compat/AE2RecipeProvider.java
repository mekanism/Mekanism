package mekanism.common.recipe.compat;

import appeng.api.ids.AEBlockIds;
import appeng.api.ids.AEItemIds;
import mekanism.api.annotations.ParametersAreNotNullByDefault;
import mekanism.api.datagen.recipe.builder.ItemStackToItemStackRecipeBuilder;
import mekanism.api.recipes.ingredients.creator.IngredientCreatorAccess;
import mekanism.common.Mekanism;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.item.Items;
import net.neoforged.neoforge.common.Tags;

@ParametersAreNotNullByDefault
public class AE2RecipeProvider extends CompatRecipeProvider {

    public AE2RecipeProvider(String modid) {
        super(modid);
    }

    @Override
    protected void registerRecipes(RecipeOutput consumer, String basePath, HolderLookup.Provider registries) {
        //Certus Crystal -> Certus Dust
        ItemStackToItemStackRecipeBuilder.crushing(
                    IngredientCreatorAccess.item().from(ItemTags.create(Tags.Items.GEMS.location().withSuffix("/certus_quartz"))),
                    foreignItemStack(registries, AEItemIds.CERTUS_QUARTZ_DUST)
              ).addCondition(modLoaded)
              .build(consumer, Mekanism.rl(basePath + "certus_crystal_to_dust"));

        //Fluix Crystal -> Fluix Dust
        ItemStackToItemStackRecipeBuilder.crushing(
                    IngredientCreatorAccess.item().from(registries, AEItemIds.FLUIX_CRYSTAL),
                    foreignItemStack(registries, AEItemIds.FLUIX_DUST)
              ).addCondition(modLoaded)
              .build(consumer, Mekanism.rl(basePath + "fluix_crystal_to_dust"));

        //Sky Stone -> Sky Stone Dust
        ItemStackToItemStackRecipeBuilder.crushing(
                    IngredientCreatorAccess.item().from(registries, AEBlockIds.SKY_STONE_BLOCK),
                    foreignItemStack(registries, AEItemIds.SKY_DUST)
              ).addCondition(modLoaded)
              .build(consumer, Mekanism.rl(basePath + "sky_stone_to_dust"));

        //Sky Stone Dust -> Sky Stone
        ItemStackToItemStackRecipeBuilder.enriching(
                    IngredientCreatorAccess.item().from(registries, AEItemIds.SKY_DUST),
                    foreignItemStack(registries, AEBlockIds.SKY_STONE_BLOCK)
              ).addCondition(modLoaded)
              .build(consumer, Mekanism.rl(basePath + "sky_stone_dust_to_sky_stone"));

        //Ender Pearl -> Ender Dust
        ItemStackToItemStackRecipeBuilder.crushing(
                    IngredientCreatorAccess.item().from(Items.ENDER_PEARL),
                    foreignItemStack(registries, AEItemIds.ENDER_DUST)
              ).addCondition(modLoaded)
              .build(consumer, Mekanism.rl(basePath + "ender_pearl_to_dust"));

        //Sand -> Silicon
        ItemStackToItemStackRecipeBuilder.crushing(
                    IngredientCreatorAccess.item().from(Tags.Items.SANDS),
                    foreignItemStack(registries, AEItemIds.SILICON)
              ).addCondition(modLoaded)
              .build(consumer, Mekanism.rl(basePath + "sand_to_silicon"));

        //Certus Dust to Silicon
        ItemStackToItemStackRecipeBuilder.enriching(
                    IngredientCreatorAccess.item().from(registries, AEItemIds.CERTUS_QUARTZ_DUST),
                    foreignItemStack(registries, AEItemIds.SILICON, 2)
              ).addCondition(modLoaded)
              .build(consumer, Mekanism.rl(basePath + "certus_quartz_dust_to_silicon"));

        addDecorativeRecipes(consumer, basePath + "decorative/", registries);
    }

    private void addDecorativeRecipes(RecipeOutput consumer, String basePath, HolderLookup.Provider registries) {
        addDecorativeQuartzRecipes(consumer, basePath + "certus_quartz/", registries);
        addDecorativeSkyStoneRecipes(consumer, basePath + "sky_stone/", registries);
    }

    private void addDecorativeSkyStoneRecipes(RecipeOutput consumer, String basePath, HolderLookup.Provider registries) {
        addDecorativeCrushingSkyStoneRecipes(consumer, basePath + "crushing/", registries);
        addDecorativeEnrichingSkyStoneRecipes(consumer, basePath + "enriching/", registries);
    }

    private void addDecorativeCrushingSkyStoneRecipes(RecipeOutput consumer, String basePath, HolderLookup.Provider registries) {
        //Smooth Sky Stone Chest -> Sky Stone Chest
        ItemStackToItemStackRecipeBuilder.crushing(
                    IngredientCreatorAccess.item().from(registries, AEBlockIds.SMOOTH_SKY_STONE_CHEST),
                    foreignItemStack(registries, AEBlockIds.SKY_STONE_CHEST)
              ).addCondition(modLoaded)
              .build(consumer, Mekanism.rl(basePath + "smooth_chest_to_chest"));

        //Smooth Sky Stone -> Sky Stone
        ItemStackToItemStackRecipeBuilder.crushing(
                    IngredientCreatorAccess.item().from(registries, AEBlockIds.SMOOTH_SKY_STONE_BLOCK),
                    foreignItemStack(registries, AEBlockIds.SKY_STONE_BLOCK)
              ).addCondition(modLoaded)
              .build(consumer, Mekanism.rl(basePath + "smooth_to_stone"));
        //Smooth Sky Stone Slab -> Sky Stone Slab
        ItemStackToItemStackRecipeBuilder.crushing(
                    IngredientCreatorAccess.item().from(registries, AEBlockIds.SMOOTH_SKY_STONE_SLAB),
                    foreignItemStack(registries, AEBlockIds.SKY_STONE_SLAB)
              ).addCondition(modLoaded)
              .build(consumer, Mekanism.rl(basePath + "smooth_slab_to_slab"));
        //Smooth Sky Stone Stairs -> Sky Stone Stairs
        ItemStackToItemStackRecipeBuilder.crushing(
                    IngredientCreatorAccess.item().from(registries, AEBlockIds.SMOOTH_SKY_STONE_STAIRS),
                    foreignItemStack(registries, AEBlockIds.SKY_STONE_STAIRS)
              ).addCondition(modLoaded)
              .build(consumer, Mekanism.rl(basePath + "smooth_stairs_to_stairs"));
        //Smooth Sky Stone Wall -> Sky Stone Wall
        ItemStackToItemStackRecipeBuilder.crushing(
                    IngredientCreatorAccess.item().from(registries, AEBlockIds.SMOOTH_SKY_STONE_WALL),
                    foreignItemStack(registries, AEBlockIds.SKY_STONE_WALL)
              ).addCondition(modLoaded)
              .build(consumer, Mekanism.rl(basePath + "smooth_wall_to_wall"));

        //Sky Stone Brick -> Smooth Sky Stone
        ItemStackToItemStackRecipeBuilder.crushing(
                    IngredientCreatorAccess.item().from(registries, AEBlockIds.SKY_STONE_BRICK),
                    foreignItemStack(registries, AEBlockIds.SMOOTH_SKY_STONE_BLOCK)
              ).addCondition(modLoaded)
              .build(consumer, Mekanism.rl(basePath + "brick_to_smooth"));
        //Sky Stone Brick Slab -> Smooth Sky Stone Slab
        ItemStackToItemStackRecipeBuilder.crushing(
                    IngredientCreatorAccess.item().from(registries, AEBlockIds.SKY_STONE_BRICK_SLAB),
                    foreignItemStack(registries, AEBlockIds.SMOOTH_SKY_STONE_SLAB)
              ).addCondition(modLoaded)
              .build(consumer, Mekanism.rl(basePath + "brick_slab_to_smooth_slab"));
        //Sky Stone Brick Stairs -> Smooth Sky Stone Stairs
        ItemStackToItemStackRecipeBuilder.crushing(
                    IngredientCreatorAccess.item().from(registries, AEBlockIds.SKY_STONE_BRICK_STAIRS),
                    foreignItemStack(registries, AEBlockIds.SMOOTH_SKY_STONE_STAIRS)
              ).addCondition(modLoaded)
              .build(consumer, Mekanism.rl(basePath + "brick_stairs_to_smooth_stairs"));
        //Sky Stone Brick Wall -> Smooth Sky Stone Wall
        ItemStackToItemStackRecipeBuilder.crushing(
                    IngredientCreatorAccess.item().from(registries, AEBlockIds.SKY_STONE_BRICK_WALL),
                    foreignItemStack(registries, AEBlockIds.SMOOTH_SKY_STONE_WALL)
              ).addCondition(modLoaded)
              .build(consumer, Mekanism.rl(basePath + "brick_wall_to_smooth_wall"));

        //Sky Stone Small Brick -> Sky Stone Brick
        ItemStackToItemStackRecipeBuilder.crushing(
                    IngredientCreatorAccess.item().from(registries, AEBlockIds.SKY_STONE_SMALL_BRICK),
                    foreignItemStack(registries, AEBlockIds.SKY_STONE_BRICK)
              ).addCondition(modLoaded)
              .build(consumer, Mekanism.rl(basePath + "small_brick_to_brick"));
        //Sky Stone Small Brick Slab -> Sky Stone Brick Slab
        ItemStackToItemStackRecipeBuilder.crushing(
                    IngredientCreatorAccess.item().from(registries, AEBlockIds.SKY_STONE_SMALL_BRICK_SLAB),
                    foreignItemStack(registries, AEBlockIds.SKY_STONE_BRICK_SLAB)
              ).addCondition(modLoaded)
              .build(consumer, Mekanism.rl(basePath + "small_brick_slab_to_brick_slab"));
        //Sky Stone Small Brick Stairs -> Sky Stone Brick Stairs
        ItemStackToItemStackRecipeBuilder.crushing(
                    IngredientCreatorAccess.item().from(registries, AEBlockIds.SKY_STONE_SMALL_BRICK_STAIRS),
                    foreignItemStack(registries, AEBlockIds.SKY_STONE_BRICK_STAIRS)
              ).addCondition(modLoaded)
              .build(consumer, Mekanism.rl(basePath + "small_brick_stairs_to_brick_stairs"));
        //Sky Stone Small Brick Wall -> Sky Stone Brick Wall
        ItemStackToItemStackRecipeBuilder.crushing(
                    IngredientCreatorAccess.item().from(registries, AEBlockIds.SKY_STONE_SMALL_BRICK_WALL),
                    foreignItemStack(registries, AEBlockIds.SKY_STONE_BRICK_WALL)
              ).addCondition(modLoaded)
              .build(consumer, Mekanism.rl(basePath + "small_brick_wall_to_brick_wall"));
    }

    private void addDecorativeEnrichingSkyStoneRecipes(RecipeOutput consumer, String basePath, HolderLookup.Provider registries) {
        //Sky Stone Chest -> Smooth Sky Stone Chest
        ItemStackToItemStackRecipeBuilder.enriching(
                    IngredientCreatorAccess.item().from(registries, AEBlockIds.SKY_STONE_CHEST),
                    foreignItemStack(registries, AEBlockIds.SMOOTH_SKY_STONE_CHEST)
              ).addCondition(modLoaded)
              .build(consumer, Mekanism.rl(basePath + "chest_to_smooth_chest"));

        //Sky Stone Slab -> Smooth Sky Stone Slab
        ItemStackToItemStackRecipeBuilder.enriching(
                    IngredientCreatorAccess.item().from(registries, AEBlockIds.SKY_STONE_SLAB),
                    foreignItemStack(registries, AEBlockIds.SMOOTH_SKY_STONE_SLAB)
              ).addCondition(modLoaded)
              .build(consumer, Mekanism.rl(basePath + "slab_to_smooth_slab"));
        //Sky Stone Stairs -> Smooth Sky Stone Stairs
        ItemStackToItemStackRecipeBuilder.enriching(
                    IngredientCreatorAccess.item().from(registries, AEBlockIds.SKY_STONE_STAIRS),
                    foreignItemStack(registries, AEBlockIds.SMOOTH_SKY_STONE_STAIRS)
              ).addCondition(modLoaded)
              .build(consumer, Mekanism.rl(basePath + "stairs_to_smooth_stairs"));
        //Sky Stone Wall -> Smooth Sky Stone Wall
        ItemStackToItemStackRecipeBuilder.enriching(
                    IngredientCreatorAccess.item().from(registries, AEBlockIds.SKY_STONE_WALL),
                    foreignItemStack(registries, AEBlockIds.SMOOTH_SKY_STONE_WALL)
              ).addCondition(modLoaded)
              .build(consumer, Mekanism.rl(basePath + "wall_to_smooth_wall"));

        //Smooth Sky Stone -> Sky Stone Brick
        ItemStackToItemStackRecipeBuilder.enriching(
                    IngredientCreatorAccess.item().from(registries, AEBlockIds.SMOOTH_SKY_STONE_BLOCK),
                    foreignItemStack(registries, AEBlockIds.SKY_STONE_BRICK)
              ).addCondition(modLoaded)
              .build(consumer, Mekanism.rl(basePath + "smooth_to_brick"));
        //Smooth Sky Stone Slab -> Sky Stone Brick Slab
        ItemStackToItemStackRecipeBuilder.enriching(
                    IngredientCreatorAccess.item().from(registries, AEBlockIds.SMOOTH_SKY_STONE_SLAB),
                    foreignItemStack(registries, AEBlockIds.SKY_STONE_BRICK_SLAB)
              ).addCondition(modLoaded)
              .build(consumer, Mekanism.rl(basePath + "smooth_slab_to_brick_slab"));
        //Smooth Sky Stone Stairs -> Sky Stone Brick Stairs
        ItemStackToItemStackRecipeBuilder.enriching(
                    IngredientCreatorAccess.item().from(registries, AEBlockIds.SMOOTH_SKY_STONE_STAIRS),
                    foreignItemStack(registries, AEBlockIds.SKY_STONE_BRICK_STAIRS)
              ).addCondition(modLoaded)
              .build(consumer, Mekanism.rl(basePath + "smooth_stairs_to_brick_stairs"));
        //Smooth Sky Stone Wall -> Sky Stone Brick Wall
        ItemStackToItemStackRecipeBuilder.enriching(
                    IngredientCreatorAccess.item().from(registries, AEBlockIds.SMOOTH_SKY_STONE_WALL),
                    foreignItemStack(registries, AEBlockIds.SKY_STONE_BRICK_WALL)
              ).addCondition(modLoaded)
              .build(consumer, Mekanism.rl(basePath + "smooth_wall_to_brick_wall"));

        //Sky Stone Brick -> Sky Stone Small Brick
        ItemStackToItemStackRecipeBuilder.enriching(
                    IngredientCreatorAccess.item().from(registries, AEBlockIds.SKY_STONE_BRICK),
                    foreignItemStack(registries, AEBlockIds.SKY_STONE_SMALL_BRICK)
              ).addCondition(modLoaded)
              .build(consumer, Mekanism.rl(basePath + "brick_to_small_brick"));
        //Sky Stone Brick Slab -> Sky Stone Small Brick Slab
        ItemStackToItemStackRecipeBuilder.enriching(
                    IngredientCreatorAccess.item().from(registries, AEBlockIds.SKY_STONE_BRICK_SLAB),
                    foreignItemStack(registries, AEBlockIds.SKY_STONE_SMALL_BRICK_SLAB)
              ).addCondition(modLoaded)
              .build(consumer, Mekanism.rl(basePath + "brick_slab_to_small_brick_slab"));
        //Sky Stone Brick Stairs -> Sky Stone Small Brick Stairs
        ItemStackToItemStackRecipeBuilder.enriching(
                    IngredientCreatorAccess.item().from(registries, AEBlockIds.SKY_STONE_BRICK_STAIRS),
                    foreignItemStack(registries, AEBlockIds.SKY_STONE_SMALL_BRICK_STAIRS)
              ).addCondition(modLoaded)
              .build(consumer, Mekanism.rl(basePath + "brick_stairs_to_small_brick_stairs"));
        //Sky Stone Brick Wall -> Sky Stone Small Brick Wall
        ItemStackToItemStackRecipeBuilder.enriching(
                    IngredientCreatorAccess.item().from(registries, AEBlockIds.SKY_STONE_BRICK_WALL),
                    foreignItemStack(registries, AEBlockIds.SKY_STONE_SMALL_BRICK_WALL)
              ).addCondition(modLoaded)
              .build(consumer, Mekanism.rl(basePath + "brick_wall_to_small_brick_wall"));
    }

    private void addDecorativeQuartzRecipes(RecipeOutput consumer, String basePath, HolderLookup.Provider registries) {
        addDecorativeQuartzCrushingRecipes(consumer, basePath + "crushing/", registries);
        addDecorativeQuartzEnrichingRecipes(consumer, basePath + "enriching/", registries);
    }

    private void addDecorativeQuartzCrushingRecipes(RecipeOutput consumer, String basePath, HolderLookup.Provider registries) {
        //Certus Quartz Block -> Chiseled Certus Quartz Block
        ItemStackToItemStackRecipeBuilder.crushing(
                    IngredientCreatorAccess.item().from(registries, AEBlockIds.QUARTZ_BLOCK),
                    foreignItemStack(registries, AEBlockIds.CHISELED_QUARTZ_BLOCK)
              ).addCondition(modLoaded)
              .build(consumer, Mekanism.rl(basePath + "block_to_chiseled_block"));
        //Certus Quartz Slab -> Chiseled Certus Quartz Slab
        ItemStackToItemStackRecipeBuilder.crushing(
                    IngredientCreatorAccess.item().from(registries, AEBlockIds.QUARTZ_SLAB),
                    foreignItemStack(registries, AEBlockIds.CHISELED_QUARTZ_SLAB)
              ).addCondition(modLoaded)
              .build(consumer, Mekanism.rl(basePath + "slab_to_chiseled_slab"));
        //Certus Quartz Stairs -> Chiseled Certus Quartz Stairs
        ItemStackToItemStackRecipeBuilder.crushing(
                    IngredientCreatorAccess.item().from(registries, AEBlockIds.QUARTZ_STAIRS),
                    foreignItemStack(registries, AEBlockIds.CHISELED_QUARTZ_STAIRS)
              ).addCondition(modLoaded)
              .build(consumer, Mekanism.rl(basePath + "stairs_to_chiseled_stairs"));
        //Certus Quartz Wall -> Chiseled Certus Quartz Wall
        ItemStackToItemStackRecipeBuilder.crushing(
                    IngredientCreatorAccess.item().from(registries, AEBlockIds.QUARTZ_WALL),
                    foreignItemStack(registries, AEBlockIds.CHISELED_QUARTZ_WALL)
              ).addCondition(modLoaded)
              .build(consumer, Mekanism.rl(basePath + "wall_to_chiseled_wall"));

        //Chiseled Certus Quartz Block -> Certus Quartz Pillar Block
        ItemStackToItemStackRecipeBuilder.crushing(
                    IngredientCreatorAccess.item().from(registries, AEBlockIds.CHISELED_QUARTZ_BLOCK),
                    foreignItemStack(registries, AEBlockIds.QUARTZ_PILLAR)
              ).addCondition(modLoaded)
              .build(consumer, Mekanism.rl(basePath + "chiseled_block_to_pillar"));
        //Chiseled Certus Quartz Slab -> Certus Quartz Pillar Slab
        ItemStackToItemStackRecipeBuilder.crushing(
                    IngredientCreatorAccess.item().from(registries, AEBlockIds.CHISELED_QUARTZ_SLAB),
                    foreignItemStack(registries, AEBlockIds.QUARTZ_PILLAR_SLAB)
              ).addCondition(modLoaded)
              .build(consumer, Mekanism.rl(basePath + "chiseled_slab_to_pillar_slab"));
        //Chiseled Certus Quartz Stairs -> Certus Quartz Pillar Stairs
        ItemStackToItemStackRecipeBuilder.crushing(
                    IngredientCreatorAccess.item().from(registries, AEBlockIds.CHISELED_QUARTZ_STAIRS),
                    foreignItemStack(registries, AEBlockIds.QUARTZ_PILLAR_STAIRS)
              ).addCondition(modLoaded)
              .build(consumer, Mekanism.rl(basePath + "chiseled_stairs_to_pillar_stairs"));
        //Chiseled Certus Quartz Wall -> Certus Quartz Pillar Wall
        ItemStackToItemStackRecipeBuilder.crushing(
                    IngredientCreatorAccess.item().from(registries, AEBlockIds.CHISELED_QUARTZ_WALL),
                    foreignItemStack(registries, AEBlockIds.QUARTZ_PILLAR_WALL)
              ).addCondition(modLoaded)
              .build(consumer, Mekanism.rl(basePath + "chiseled_wall_to_pillar_wall"));

        //Certus Quartz Pillar Block -> Certus Quartz Block
        ItemStackToItemStackRecipeBuilder.crushing(
                    IngredientCreatorAccess.item().from(registries, AEBlockIds.QUARTZ_PILLAR),
                    foreignItemStack(registries, AEBlockIds.QUARTZ_BLOCK)
              ).addCondition(modLoaded)
              .build(consumer, Mekanism.rl(basePath + "pillar_to_block"));
        //Certus Quartz Pillar Slab -> Certus Quartz Slab
        ItemStackToItemStackRecipeBuilder.crushing(
                    IngredientCreatorAccess.item().from(registries, AEBlockIds.QUARTZ_PILLAR_SLAB),
                    foreignItemStack(registries, AEBlockIds.QUARTZ_SLAB)
              ).addCondition(modLoaded)
              .build(consumer, Mekanism.rl(basePath + "pillar_slab_to_slab"));
        //Certus Quartz Pillar Stairs -> Certus Quartz Stairs
        ItemStackToItemStackRecipeBuilder.crushing(
                    IngredientCreatorAccess.item().from(registries, AEBlockIds.QUARTZ_PILLAR_STAIRS),
                    foreignItemStack(registries, AEBlockIds.QUARTZ_STAIRS)
              ).addCondition(modLoaded)
              .build(consumer, Mekanism.rl(basePath + "pillar_stairs_to_stairs"));
        //Certus Quartz Pillar Wall -> Certus Quartz Wall
        ItemStackToItemStackRecipeBuilder.crushing(
                    IngredientCreatorAccess.item().from(registries, AEBlockIds.QUARTZ_PILLAR_WALL),
                    foreignItemStack(registries, AEBlockIds.QUARTZ_WALL)
              ).addCondition(modLoaded)
              .build(consumer, Mekanism.rl(basePath + "pillar_wall_to_wall"));
    }

    private void addDecorativeQuartzEnrichingRecipes(RecipeOutput consumer, String basePath, HolderLookup.Provider registries) {
        //Chiseled Certus Quartz Block -> Certus Quartz Block
        ItemStackToItemStackRecipeBuilder.enriching(
                    IngredientCreatorAccess.item().from(registries, AEBlockIds.CHISELED_QUARTZ_BLOCK),
                    foreignItemStack(registries, AEBlockIds.QUARTZ_BLOCK)
              ).addCondition(modLoaded)
              .build(consumer, Mekanism.rl(basePath + "chiseled_block_to_block"));
        //Chiseled Certus Quartz Slab -> Certus Quartz Slab
        ItemStackToItemStackRecipeBuilder.enriching(
                    IngredientCreatorAccess.item().from(registries, AEBlockIds.CHISELED_QUARTZ_SLAB),
                    foreignItemStack(registries, AEBlockIds.QUARTZ_SLAB)
              ).addCondition(modLoaded)
              .build(consumer, Mekanism.rl(basePath + "chiseled_slab_to_slab"));
        //Chiseled Certus Quartz Stairs -> Certus Quartz Stairs
        ItemStackToItemStackRecipeBuilder.enriching(
                    IngredientCreatorAccess.item().from(registries, AEBlockIds.CHISELED_QUARTZ_STAIRS),
                    foreignItemStack(registries, AEBlockIds.QUARTZ_STAIRS)
              ).addCondition(modLoaded)
              .build(consumer, Mekanism.rl(basePath + "chiseled_stairs_to_stairs"));
        //Chiseled Certus Quartz Wall -> Certus Quartz Wall
        ItemStackToItemStackRecipeBuilder.enriching(
                    IngredientCreatorAccess.item().from(registries, AEBlockIds.CHISELED_QUARTZ_WALL),
                    foreignItemStack(registries, AEBlockIds.QUARTZ_WALL)
              ).addCondition(modLoaded)
              .build(consumer, Mekanism.rl(basePath + "chiseled_wall_to_wall"));

        //Certus Quartz Pillar Block -> Chiseled Certus Quartz Block
        ItemStackToItemStackRecipeBuilder.enriching(
                    IngredientCreatorAccess.item().from(registries, AEBlockIds.QUARTZ_PILLAR),
                    foreignItemStack(registries, AEBlockIds.CHISELED_QUARTZ_BLOCK)
              ).addCondition(modLoaded)
              .build(consumer, Mekanism.rl(basePath + "pillar_to_chiseled_block"));
        //Certus Quartz Pillar Slab -> Chiseled Certus Quartz Slab
        ItemStackToItemStackRecipeBuilder.enriching(
                    IngredientCreatorAccess.item().from(registries, AEBlockIds.QUARTZ_PILLAR_SLAB),
                    foreignItemStack(registries, AEBlockIds.CHISELED_QUARTZ_SLAB)
              ).addCondition(modLoaded)
              .build(consumer, Mekanism.rl(basePath + "pillar_slab_to_chiseled_slab"));
        //Certus Quartz Pillar Stairs -> Chiseled Certus Quartz Stairs
        ItemStackToItemStackRecipeBuilder.enriching(
                    IngredientCreatorAccess.item().from(registries, AEBlockIds.QUARTZ_PILLAR_STAIRS),
                    foreignItemStack(registries, AEBlockIds.CHISELED_QUARTZ_STAIRS)
              ).addCondition(modLoaded)
              .build(consumer, Mekanism.rl(basePath + "pillar_stairs_to_chiseled_stairs"));
        //Certus Quartz Pillar Wall -> Chiseled Certus Quartz Wall
        ItemStackToItemStackRecipeBuilder.enriching(
                    IngredientCreatorAccess.item().from(registries, AEBlockIds.QUARTZ_PILLAR_WALL),
                    foreignItemStack(registries, AEBlockIds.CHISELED_QUARTZ_WALL)
              ).addCondition(modLoaded)
              .build(consumer, Mekanism.rl(basePath + "pillar_wall_to_chiseled_wall"));

        //Certus Quartz Block -> Certus Quartz Pillar Block
        ItemStackToItemStackRecipeBuilder.enriching(
                    IngredientCreatorAccess.item().from(registries, AEBlockIds.QUARTZ_BLOCK),
                    foreignItemStack(registries, AEBlockIds.QUARTZ_PILLAR)
              ).addCondition(modLoaded)
              .build(consumer, Mekanism.rl(basePath + "block_to_pillar"));
        //Certus Quartz Slab -> Certus Quartz Pillar Slab
        ItemStackToItemStackRecipeBuilder.enriching(
                    IngredientCreatorAccess.item().from(registries, AEBlockIds.QUARTZ_SLAB),
                    foreignItemStack(registries, AEBlockIds.QUARTZ_PILLAR_SLAB)
              ).addCondition(modLoaded)
              .build(consumer, Mekanism.rl(basePath + "slab_to_pillar_slab"));
        //Certus Quartz Stairs -> Certus Quartz Pillar Stairs
        ItemStackToItemStackRecipeBuilder.enriching(
                    IngredientCreatorAccess.item().from(registries, AEBlockIds.QUARTZ_STAIRS),
                    foreignItemStack(registries, AEBlockIds.QUARTZ_PILLAR_STAIRS)
              ).addCondition(modLoaded)
              .build(consumer, Mekanism.rl(basePath + "stairs_to_pillar_stairs"));
        //Certus Quartz Wall -> Certus Quartz Pillar Wall
        ItemStackToItemStackRecipeBuilder.enriching(
                    IngredientCreatorAccess.item().from(registries, AEBlockIds.QUARTZ_WALL),
                    foreignItemStack(registries, AEBlockIds.QUARTZ_PILLAR_WALL)
              ).addCondition(modLoaded)
              .build(consumer, Mekanism.rl(basePath + "wall_to_pillar_wall"));
    }
}