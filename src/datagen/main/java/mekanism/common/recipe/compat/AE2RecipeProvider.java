package mekanism.common.recipe.compat;

import appeng.api.IAppEngApi;
import appeng.api.definitions.IBlocks;
import appeng.api.definitions.IItems;
import appeng.api.definitions.IMaterials;
import appeng.core.Api;
import java.util.function.Consumer;
import javax.annotation.ParametersAreNonnullByDefault;
import mekanism.api.datagen.recipe.builder.ItemStackToItemStackRecipeBuilder;
import mekanism.api.recipes.inputs.ItemStackIngredient;
import mekanism.common.Mekanism;
import net.minecraft.data.IFinishedRecipe;
import net.minecraftforge.common.Tags;

@ParametersAreNonnullByDefault
public class AE2RecipeProvider extends CompatRecipeProvider {

    public AE2RecipeProvider() {
        super("appliedenergistics2");
    }

    @Override
    protected void registerRecipes(Consumer<IFinishedRecipe> consumer, String basePath) {
        //We cannot use the public API here since data generators do not run the common setup phase,
        // which would normally expose the API to addons after AE has initialized and registered all of its blocks.
        IAppEngApi api = Api.instance();
        if (api == null) {
            throw new IllegalStateException("AE2 was not initialized");
        }

        IItems items = api.definitions().items();
        IMaterials materials = api.definitions().materials();
        IBlocks blocks = api.definitions().blocks();

        //Certus Crystal -> Certus Dust
        ItemStackToItemStackRecipeBuilder.crushing(
              ItemStackIngredient.createMulti(
                    ItemStackIngredient.from(materials.certusQuartzCrystal()),
                    ItemStackIngredient.from(materials.certusQuartzCrystalCharged())
              ),
              materials.certusQuartzDust().stack(1)
        ).addCondition(modLoaded)
              .build(consumer, Mekanism.rl(basePath + "certus_crystal_to_dust"));

        //Fluix Crystal -> Fluix Dust
        ItemStackToItemStackRecipeBuilder.crushing(
              ItemStackIngredient.from(materials.fluixCrystal()),
              materials.fluixDust().stack(1)
        ).addCondition(modLoaded)
              .build(consumer, Mekanism.rl(basePath + "fluix_crystal_to_dust"));

        //Certus Ore -> Certus Crystal
        ItemStackToItemStackRecipeBuilder.enriching(
              ItemStackIngredient.from(blocks.quartzOre()),
              materials.certusQuartzCrystal().stack(4)
        ).addCondition(modLoaded)
              .build(consumer, Mekanism.rl(basePath + "certus_ore_to_crystal"));

        //Charged Certus Ore -> Charged Certus Crystal
        ItemStackToItemStackRecipeBuilder.enriching(
              ItemStackIngredient.from(blocks.quartzOreCharged()),
              materials.certusQuartzCrystalCharged().stack(4)
        ).addCondition(modLoaded)
              .build(consumer, Mekanism.rl(basePath + "charged_certus_ore_to_crystal"));

        //Certus Crystal & Certus Dust -> Purified Certus Crystal
        ItemStackToItemStackRecipeBuilder.enriching(
              ItemStackIngredient.createMulti(
                    ItemStackIngredient.from(materials.certusQuartzCrystal()),
                    ItemStackIngredient.from(materials.certusQuartzDust())
              ),
              materials.purifiedCertusQuartzCrystal().stack(1)
        ).addCondition(modLoaded)
              .build(consumer, Mekanism.rl(basePath + "certus_crystal_purification"));

        //Fluix Crystal & Fluix Dust -> Purified Fluix Crystal
        ItemStackToItemStackRecipeBuilder.enriching(
              ItemStackIngredient.createMulti(
                    ItemStackIngredient.from(materials.fluixCrystal()),
                    ItemStackIngredient.from(materials.fluixDust())
              ),
              materials.purifiedFluixCrystal().stack(1)
        ).addCondition(modLoaded)
              .build(consumer, Mekanism.rl(basePath + "fluix_crystal_purification"));

        //Certus Crystal Seed -> Purified Certus Crystal
        ItemStackToItemStackRecipeBuilder.enriching(
              ItemStackIngredient.from(items.certusCrystalSeed()),
              materials.purifiedCertusQuartzCrystal().stack(1)
        ).addCondition(modLoaded)
              .build(consumer, Mekanism.rl(basePath + "certus_seed_to_purified_crystal"));

        //Nether Crystal Seed -> Purified Nether Crystal
        ItemStackToItemStackRecipeBuilder.enriching(
              ItemStackIngredient.from(items.netherQuartzSeed()),
              materials.purifiedNetherQuartzCrystal().stack(1)
        ).addCondition(modLoaded)
              .build(consumer, Mekanism.rl(basePath + "nether_seed_to_purified_crystal"));

        //Fluix Crystal Seed -> Purified Fluix Crystal
        ItemStackToItemStackRecipeBuilder.enriching(
              ItemStackIngredient.from(items.fluixCrystalSeed()),
              materials.purifiedFluixCrystal().stack(1)
        ).addCondition(modLoaded)
              .build(consumer, Mekanism.rl(basePath + "fluix_seed_to_purified_crystal"));

        //Sky Stone -> Sky Stone Dust
        ItemStackToItemStackRecipeBuilder.crushing(
              ItemStackIngredient.from(blocks.skyStoneBlock()),
              materials.skyDust().stack(1)
        ).addCondition(modLoaded)
              .build(consumer, Mekanism.rl(basePath + "sky_stone_to_dust"));

        //Smooth Sky Stone -> Sky Stone
        ItemStackToItemStackRecipeBuilder.enriching(
              ItemStackIngredient.from(blocks.smoothSkyStoneBlock()),
              blocks.skyStoneBlock().stack(1)
        ).addCondition(modLoaded)
              .build(consumer, Mekanism.rl(basePath + "smooth_sky_stone_to_sky_stone"));

        //Sky Stone Dust -> Sky Stone
        ItemStackToItemStackRecipeBuilder.enriching(
              ItemStackIngredient.from(materials.skyDust()),
              blocks.skyStoneBlock().stack(1)
        ).addCondition(modLoaded)
              .build(consumer, Mekanism.rl(basePath + "sky_stone_dust_to_sky_stone"));
        //Ender pearl -> Ender dust
        ItemStackToItemStackRecipeBuilder.crushing(
              ItemStackIngredient.from(Tags.Items.ENDER_PEARLS),
              materials.enderDust().stack(1)
        ).addCondition(modLoaded)
              .build(consumer, Mekanism.rl(basePath + "ender_pearl_to_dust"));
    }
}