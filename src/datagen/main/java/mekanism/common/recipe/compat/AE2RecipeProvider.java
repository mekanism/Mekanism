package mekanism.common.recipe.compat;

import appeng.core.definitions.AEBlocks;
import appeng.core.definitions.AEItems;
import java.util.function.Consumer;
import javax.annotation.ParametersAreNonnullByDefault;
import mekanism.api.datagen.recipe.builder.ItemStackToItemStackRecipeBuilder;
import mekanism.api.recipes.inputs.ItemStackIngredient;
import mekanism.common.Mekanism;
import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraftforge.common.Tags;

//TODO - 1.18: New content (deepslate ores
@ParametersAreNonnullByDefault
public class AE2RecipeProvider extends CompatRecipeProvider {

    public AE2RecipeProvider() {
        super("ae2");
    }

    @Override
    protected void registerRecipes(Consumer<FinishedRecipe> consumer, String basePath) {
        //Certus Crystal -> Certus Dust
        ItemStackToItemStackRecipeBuilder.crushing(
                    ItemStackIngredient.from(Ingredient.of(
                          AEItems.CERTUS_QUARTZ_CRYSTAL,
                          AEItems.CERTUS_QUARTZ_CRYSTAL_CHARGED
                    )),
                    AEItems.CERTUS_QUARTZ_DUST.stack(1)
              ).addCondition(modLoaded)
              .build(consumer, Mekanism.rl(basePath + "certus_crystal_to_dust"));

        //Fluix Crystal -> Fluix Dust
        ItemStackToItemStackRecipeBuilder.crushing(
                    ItemStackIngredient.from(AEItems.FLUIX_CRYSTAL),
                    AEItems.FLUIX_DUST.stack(1)
              ).addCondition(modLoaded)
              .build(consumer, Mekanism.rl(basePath + "fluix_crystal_to_dust"));

        //Certus Ore -> Certus Crystal
        ItemStackToItemStackRecipeBuilder.enriching(
                    ItemStackIngredient.from(AEBlocks.QUARTZ_ORE),
                    AEItems.CERTUS_QUARTZ_CRYSTAL.stack(4)
              ).addCondition(modLoaded)
              .build(consumer, Mekanism.rl(basePath + "certus_ore_to_crystal"));

        //TODO - 1.18: Re-evaluate
        //Certus Dust -> Certus Crystal
        ItemStackToItemStackRecipeBuilder.enriching(
                    ItemStackIngredient.from(Ingredient.of(
                          AEItems.CERTUS_QUARTZ_DUST
                    )),
                    AEItems.CERTUS_QUARTZ_CRYSTAL.stack(1)
              ).addCondition(modLoaded)
              .build(consumer, Mekanism.rl(basePath + "certus_dust_purification"));

        //TODO - 1.18: Re-evaluate
        //Fluix Dust -> Fluix Crystal
        ItemStackToItemStackRecipeBuilder.enriching(
                    ItemStackIngredient.from(AEItems.FLUIX_DUST),
                    AEItems.FLUIX_CRYSTAL.stack(1)
              ).addCondition(modLoaded)
              .build(consumer, Mekanism.rl(basePath + "fluix_dust_purification"));

        //Certus Crystal Seed -> Certus Crystal
        ItemStackToItemStackRecipeBuilder.enriching(
                    ItemStackIngredient.from(AEItems.CERTUS_CRYSTAL_SEED),
                    AEItems.CERTUS_QUARTZ_CRYSTAL.stack(1)
              ).addCondition(modLoaded)
              .build(consumer, Mekanism.rl(basePath + "certus_seed_to_crystal"));

        //Fluix Crystal Seed -> Fluix Crystal
        ItemStackToItemStackRecipeBuilder.enriching(
                    ItemStackIngredient.from(AEItems.FLUIX_CRYSTAL_SEED),
                    AEItems.FLUIX_CRYSTAL.stack(1)
              ).addCondition(modLoaded)
              .build(consumer, Mekanism.rl(basePath + "fluix_seed_to_crystal"));

        //Sky Stone -> Sky Stone Dust
        ItemStackToItemStackRecipeBuilder.crushing(
                    ItemStackIngredient.from(AEBlocks.SKY_STONE_BLOCK),
                    AEItems.SKY_DUST.stack(1)
              ).addCondition(modLoaded)
              .build(consumer, Mekanism.rl(basePath + "sky_stone_to_dust"));

        //Smooth Sky Stone -> Sky Stone
        ItemStackToItemStackRecipeBuilder.enriching(
                    ItemStackIngredient.from(AEBlocks.SMOOTH_SKY_STONE_BLOCK),
                    AEBlocks.SKY_STONE_BLOCK.stack(1)
              ).addCondition(modLoaded)
              .build(consumer, Mekanism.rl(basePath + "smooth_sky_stone_to_sky_stone"));

        //Sky Stone Dust -> Sky Stone
        ItemStackToItemStackRecipeBuilder.enriching(
                    ItemStackIngredient.from(AEItems.SKY_DUST),
                    AEBlocks.SKY_STONE_BLOCK.stack(1)
              ).addCondition(modLoaded)
              .build(consumer, Mekanism.rl(basePath + "sky_stone_dust_to_sky_stone"));
        //Ender pearl -> Ender dust
        ItemStackToItemStackRecipeBuilder.crushing(
                    ItemStackIngredient.from(Tags.Items.ENDER_PEARLS),
                    AEItems.ENDER_DUST.stack(1)
              ).addCondition(modLoaded)
              .build(consumer, Mekanism.rl(basePath + "ender_pearl_to_dust"));
    }
}