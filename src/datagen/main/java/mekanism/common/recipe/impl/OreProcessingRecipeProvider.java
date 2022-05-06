package mekanism.common.recipe.impl;

import java.util.function.Consumer;
import javax.annotation.Nullable;
import mekanism.api.datagen.recipe.builder.ChemicalChemicalToChemicalRecipeBuilder;
import mekanism.api.datagen.recipe.builder.ChemicalCrystallizerRecipeBuilder;
import mekanism.api.datagen.recipe.builder.ChemicalDissolutionRecipeBuilder;
import mekanism.api.datagen.recipe.builder.CombinerRecipeBuilder;
import mekanism.api.datagen.recipe.builder.FluidSlurryToSlurryRecipeBuilder;
import mekanism.api.datagen.recipe.builder.GasToGasRecipeBuilder;
import mekanism.api.datagen.recipe.builder.ItemStackChemicalToItemStackRecipeBuilder;
import mekanism.api.datagen.recipe.builder.ItemStackToChemicalRecipeBuilder;
import mekanism.api.datagen.recipe.builder.ItemStackToItemStackRecipeBuilder;
import mekanism.api.providers.IItemProvider;
import mekanism.api.recipes.ingredients.ItemStackIngredient;
import mekanism.api.recipes.ingredients.creator.IngredientCreatorAccess;
import mekanism.common.Mekanism;
import mekanism.common.recipe.ISubRecipeProvider;
import mekanism.common.recipe.RecipeProviderUtil;
import mekanism.common.recipe.builder.ExtendedShapedRecipeBuilder;
import mekanism.common.recipe.builder.ExtendedShapelessRecipeBuilder;
import mekanism.common.recipe.pattern.Pattern;
import mekanism.common.registration.impl.SlurryRegistryObject;
import mekanism.common.registries.MekanismBlocks;
import mekanism.common.registries.MekanismGases;
import mekanism.common.registries.MekanismItems;
import mekanism.common.registries.MekanismSlurries;
import mekanism.common.resource.PrimaryResource;
import mekanism.common.resource.ResourceType;
import mekanism.common.resource.ore.OreBlockType;
import mekanism.common.resource.ore.OreType;
import mekanism.common.tags.MekanismTags;
import mekanism.common.util.EnumUtils;
import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.tags.FluidTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.Blocks;
import net.minecraftforge.common.Tags;

class OreProcessingRecipeProvider implements ISubRecipeProvider {

    @Override
    public void addRecipes(Consumer<FinishedRecipe> consumer) {
        String basePath = "processing/";
        for (PrimaryResource resource : EnumUtils.PRIMARY_RESOURCES) {
            addDynamicOreProcessingIngotRecipes(consumer, basePath + resource.getRegistrySuffix() + "/", resource);
        }
        //Raw Gold plus netherrack to nether gold ore
        CombinerRecipeBuilder.combining(
              IngredientCreatorAccess.item().from(Tags.Items.RAW_MATERIALS_GOLD, 8),
              IngredientCreatorAccess.item().from(Blocks.NETHERRACK),
              new ItemStack(Blocks.NETHER_GOLD_ORE)
        ).build(consumer, Mekanism.rl(basePath + "gold/ore/nether_from_raw"));

        //Iron -> enriched iron
        ItemStackChemicalToItemStackRecipeBuilder.metallurgicInfusing(
              IngredientCreatorAccess.item().from(Tags.Items.INGOTS_IRON),
              IngredientCreatorAccess.infusion().from(MekanismTags.InfuseTypes.CARBON, 10),
              MekanismItems.ENRICHED_IRON.getItemStack()
        ).build(consumer, Mekanism.rl(basePath + "iron/enriched"));
        addNetheriteProcessingRecipes(consumer, basePath + "netherite/");
        addBronzeProcessingRecipes(consumer, basePath + "bronze/");
        addCoalOreProcessingRecipes(consumer, basePath + "coal/");
        addOreProcessingGemRecipes(consumer, basePath + "diamond/", Blocks.DIAMOND_ORE, Blocks.DEEPSLATE_DIAMOND_ORE, Tags.Items.ORES_DIAMOND,
              MekanismItems.DIAMOND_DUST, MekanismTags.Items.DUSTS_DIAMOND, Items.DIAMOND, Tags.Items.GEMS_DIAMOND, 2, 5, Tags.Items.COBBLESTONE_NORMAL);
        addOreProcessingGemRecipes(consumer, basePath + "emerald/", Blocks.EMERALD_ORE, Blocks.DEEPSLATE_EMERALD_ORE, Tags.Items.ORES_EMERALD,
              MekanismItems.EMERALD_DUST, MekanismTags.Items.DUSTS_EMERALD, Items.EMERALD, Tags.Items.GEMS_EMERALD, 2, 5, Tags.Items.COBBLESTONE_NORMAL);
        addOreProcessingGemRecipes(consumer, basePath + "lapis_lazuli/", Blocks.LAPIS_ORE, Blocks.DEEPSLATE_LAPIS_ORE, Tags.Items.ORES_LAPIS,
              MekanismItems.LAPIS_LAZULI_DUST, MekanismTags.Items.DUSTS_LAPIS, Items.LAPIS_LAZULI, Tags.Items.GEMS_LAPIS, 12, 27, Tags.Items.COBBLESTONE_NORMAL);
        addOreProcessingGemRecipes(consumer, basePath + "quartz/", Blocks.NETHER_QUARTZ_ORE, null, Tags.Items.ORES_QUARTZ, MekanismItems.QUARTZ_DUST,
              MekanismTags.Items.DUSTS_QUARTZ, Items.QUARTZ, Tags.Items.GEMS_QUARTZ, 6, 14, IngredientCreatorAccess.item().from(Blocks.NETHERRACK));
        addRedstoneProcessingRecipes(consumer, basePath + "redstone/");
        addRefinedGlowstoneProcessingRecipes(consumer, basePath + "refined_glowstone/");
        addRefinedObsidianProcessingRecipes(consumer, basePath + "refined_obsidian/");
        addSteelProcessingRecipes(consumer, basePath + "steel/");
        addFluoriteRecipes(consumer, basePath + "fluorite/");
        addUraniumRecipes(consumer, basePath + "uranium/");
    }

    private void addDynamicOreProcessingIngotRecipes(Consumer<FinishedRecipe> consumer, String basePath, PrimaryResource resource) {
        //TODO - 1.18: Take into account if the ore is a single drop or multi like vanilla copper is?
        // We may want to consider this at least for the silk touched ore to ingot?
        ItemLike ingot = MekanismItems.PROCESSED_RESOURCES.get(ResourceType.INGOT, resource);
        TagKey<Item> ingotTag = MekanismTags.Items.PROCESSED_RESOURCES.get(ResourceType.INGOT, resource);
        ItemLike nugget = MekanismItems.PROCESSED_RESOURCES.get(ResourceType.NUGGET, resource);
        TagKey<Item> nuggetTag = MekanismTags.Items.PROCESSED_RESOURCES.get(ResourceType.NUGGET, resource);
        ItemLike block = MekanismBlocks.PROCESSED_RESOURCE_BLOCKS.get(resource);
        TagKey<Item> blockTag = MekanismTags.Items.PROCESSED_RESOURCE_BLOCKS.get(resource);
        ItemLike raw = MekanismItems.PROCESSED_RESOURCES.get(ResourceType.RAW, resource);
        TagKey<Item> rawTag = MekanismTags.Items.PROCESSED_RESOURCES.get(ResourceType.RAW, resource);
        ItemLike rawBlock = MekanismBlocks.PROCESSED_RESOURCE_BLOCKS.get(resource.getRawResourceBlockInfo());
        TagKey<Item> rawBlockTag = MekanismTags.Items.PROCESSED_RESOURCE_BLOCKS.get(resource.getRawResourceBlockInfo());
        OreBlockType oreBlockType = MekanismBlocks.ORES.get(OreType.get(resource));
        ItemLike ore = oreBlockType == null ? null : oreBlockType.stone();
        ItemLike deepslateOre = oreBlockType == null ? null : oreBlockType.deepslate();
        TagKey<Item> oreTag = resource.getOreTag();
        float dustExperience = 0.3F;
        int toOre = 8;
        if (resource.isVanilla()) {
            //Note: We only bother setting types we actually use
            switch (resource) {
                case IRON -> {
                    ingot = Items.IRON_INGOT;
                    ingotTag = Tags.Items.INGOTS_IRON;
                    raw = Items.RAW_IRON;
                    rawTag = Tags.Items.RAW_MATERIALS_IRON;
                    rawBlock = Items.RAW_IRON_BLOCK;
                    rawBlockTag = Tags.Items.STORAGE_BLOCKS_RAW_IRON;
                    ore = Blocks.IRON_ORE;
                    deepslateOre = Blocks.DEEPSLATE_IRON_ORE;
                    dustExperience = 0.35F;
                }
                case GOLD -> {
                    ingot = Items.GOLD_INGOT;
                    ingotTag = Tags.Items.INGOTS_GOLD;
                    raw = Items.RAW_GOLD;
                    rawTag = Tags.Items.RAW_MATERIALS_GOLD;
                    rawBlock = Items.RAW_GOLD_BLOCK;
                    rawBlockTag = Tags.Items.STORAGE_BLOCKS_RAW_GOLD;
                    ore = Blocks.GOLD_ORE;
                    deepslateOre = Blocks.DEEPSLATE_GOLD_ORE;
                    dustExperience = 0.5F;
                }
                case COPPER -> {
                    ingot = Items.COPPER_INGOT;
                    ingotTag = Tags.Items.INGOTS_COPPER;
                    raw = Items.RAW_COPPER;
                    rawTag = Tags.Items.RAW_MATERIALS_COPPER;
                    rawBlock = Items.RAW_COPPER_BLOCK;
                    rawBlockTag = Tags.Items.STORAGE_BLOCKS_RAW_COPPER;
                    ore = Blocks.COPPER_ORE;
                    deepslateOre = Blocks.DEEPSLATE_COPPER_ORE;
                    dustExperience = 0.35F;
                    toOre = 20;//8 * 2.5
                }
                default -> throw new IllegalStateException("Unknown defaults for primary resource: " + resource.getRegistrySuffix());
            }
        }

        IItemProvider dust = MekanismItems.PROCESSED_RESOURCES.get(ResourceType.DUST, resource);
        IItemProvider dirtyDust = MekanismItems.PROCESSED_RESOURCES.get(ResourceType.DIRTY_DUST, resource);
        IItemProvider clump = MekanismItems.PROCESSED_RESOURCES.get(ResourceType.CLUMP, resource);
        IItemProvider crystal = MekanismItems.PROCESSED_RESOURCES.get(ResourceType.CRYSTAL, resource);
        IItemProvider shard = MekanismItems.PROCESSED_RESOURCES.get(ResourceType.SHARD, resource);
        TagKey<Item> dustTag = MekanismTags.Items.PROCESSED_RESOURCES.get(ResourceType.DUST, resource);
        TagKey<Item> dirtyDustTag = MekanismTags.Items.PROCESSED_RESOURCES.get(ResourceType.DIRTY_DUST, resource);
        TagKey<Item> clumpTag = MekanismTags.Items.PROCESSED_RESOURCES.get(ResourceType.CLUMP, resource);
        TagKey<Item> shardTag = MekanismTags.Items.PROCESSED_RESOURCES.get(ResourceType.SHARD, resource);
        TagKey<Item> crystalTag = MekanismTags.Items.PROCESSED_RESOURCES.get(ResourceType.CRYSTAL, resource);

        SlurryRegistryObject<?, ?> slurry = MekanismSlurries.PROCESSED_RESOURCES.get(resource);

        // Miscellaneous
        if (!resource.isVanilla()) {
            // from block
            ExtendedShapelessRecipeBuilder.shapelessRecipe(ingot, 9).addIngredient(blockTag).build(consumer, Mekanism.rl(basePath + "ingot/from_block"));
            // to block
            ExtendedShapedRecipeBuilder.shapedRecipe(block)
                  .pattern(MekanismRecipeProvider.STORAGE_PATTERN)
                  .key(Pattern.CONSTANT, ingotTag)
                  .build(consumer, Mekanism.rl(basePath + "storage_blocks/from_ingots"));
            // from nuggets
            ExtendedShapedRecipeBuilder.shapedRecipe(ingot)
                  .pattern(MekanismRecipeProvider.STORAGE_PATTERN)
                  .key(Pattern.CONSTANT, nuggetTag)
                  .build(consumer, Mekanism.rl(basePath + "ingot/from_nuggets"));
            // to nuggets
            ExtendedShapelessRecipeBuilder.shapelessRecipe(nugget, 9)
                  .addIngredient(ingotTag)
                  .build(consumer, Mekanism.rl(basePath + "nugget/from_ingot"));
            // from ore
            RecipeProviderUtil.addSmeltingBlastingRecipes(consumer, Ingredient.of(oreTag), ingot, dustExperience * 2, 200,
                  Mekanism.rl(basePath + "ingot/from_ore_blasting"), Mekanism.rl(basePath + "ingot/from_ore_smelting"));
            // from raw
            RecipeProviderUtil.addSmeltingBlastingRecipes(consumer, Ingredient.of(rawTag), ingot, dustExperience * 2, 200,
                  Mekanism.rl(basePath + "ingot/from_raw_blasting"), Mekanism.rl(basePath + "ingot/from_raw_smelting"));
            // raw from raw block
            ExtendedShapelessRecipeBuilder.shapelessRecipe(raw, 9).addIngredient(rawBlockTag).build(consumer, Mekanism.rl(basePath + "raw/from_raw_block"));
            // raw to raw block
            ExtendedShapedRecipeBuilder.shapedRecipe(rawBlock)
                  .pattern(MekanismRecipeProvider.STORAGE_PATTERN)
                  .key(Pattern.CONSTANT, rawTag)
                  .build(consumer, Mekanism.rl(basePath + "raw_storage_blocks/from_raw"));
        }

        ItemStackIngredient forOre = IngredientCreatorAccess.item().from(rawTag, toOre);
        // Ore from Dust
        CombinerRecipeBuilder.combining(
                forOre,
                IngredientCreatorAccess.item().from(Tags.Items.COBBLESTONE_NORMAL),
                new ItemStack(ore)
                ).build(consumer, Mekanism.rl(basePath + "ore/from_raw"));
        // Deepslate Ore from Dust
        CombinerRecipeBuilder.combining(
                forOre,
                IngredientCreatorAccess.item().from(Tags.Items.COBBLESTONE_DEEPSLATE),
                new ItemStack(deepslateOre)
                ).build(consumer, Mekanism.rl(basePath + "ore/deepslate_from_raw"));

        //Dust from Ingot
        ItemStackToItemStackRecipeBuilder.crushing(IngredientCreatorAccess.item().from(ingotTag), dust.getItemStack())
        .build(consumer, Mekanism.rl(basePath + "dust/from_ingot"));

        // Intermediate Steps
        // Ingot from Dust
        RecipeProviderUtil.addSmeltingBlastingRecipes(consumer, Ingredient.of(dustTag), ingot, dustExperience, 200,
                Mekanism.rl(basePath + "ingot/from_dust_blasting"), Mekanism.rl(basePath + "ingot/from_dust_smelting"));
        // Dust from Dirty Dust
        ItemStackToItemStackRecipeBuilder.enriching(IngredientCreatorAccess.item().from(dirtyDustTag), dust.getItemStack())
        .build(consumer, Mekanism.rl(basePath + "dust/from_dirty_dust"));
        // Dirty Dust from Clump
        ItemStackToItemStackRecipeBuilder.crushing(IngredientCreatorAccess.item().from(clumpTag), dirtyDust.getItemStack())
        .build(consumer, Mekanism.rl(basePath + "dirty_dust/from_clump"));
        // Clump from Shard
        ItemStackChemicalToItemStackRecipeBuilder.purifying(
                IngredientCreatorAccess.item().from(shardTag),
                IngredientCreatorAccess.gas().from(MekanismGases.OXYGEN, 1),
                clump.getItemStack()
                ).build(consumer, Mekanism.rl(basePath + "clump/from_shard"));
        // Shard from Crystal
        ItemStackChemicalToItemStackRecipeBuilder.injecting(
                IngredientCreatorAccess.item().from(crystalTag),
                IngredientCreatorAccess.gas().from(MekanismGases.HYDROGEN_CHLORIDE, 1),
                shard.getItemStack()
                ).build(consumer, Mekanism.rl(basePath + "shard/from_crystal"));
        // Crystal from Clean Slurry
        ChemicalCrystallizerRecipeBuilder.crystallizing(IngredientCreatorAccess.slurry().from(slurry.getCleanSlurry(), 200), crystal.getItemStack())
        .build(consumer, Mekanism.rl(basePath + "crystal/from_slurry"));
        // Clean Slurry from Dirty Slurry
        FluidSlurryToSlurryRecipeBuilder.washing(
                IngredientCreatorAccess.fluid().from(FluidTags.WATER, 5),
                IngredientCreatorAccess.slurry().from(slurry.getDirtySlurry(), 1),
                slurry.getCleanSlurry().getStack(1)
                ).build(consumer, Mekanism.rl(basePath + "slurry/clean"));

        // From ore
        // Dust
        ItemStackToItemStackRecipeBuilder.enriching(IngredientCreatorAccess.item().from(oreTag), dust.getItemStack(2))
        .build(consumer, Mekanism.rl(basePath + "dust/from_ore"));
        // Clump
        ItemStackChemicalToItemStackRecipeBuilder.purifying(
                IngredientCreatorAccess.item().from(oreTag),
                IngredientCreatorAccess.gas().from(MekanismGases.OXYGEN, 1),
                clump.getItemStack(3)
                ).build(consumer, Mekanism.rl(basePath + "clump/from_ore"));
        // Shard
        ItemStackChemicalToItemStackRecipeBuilder.injecting(
                IngredientCreatorAccess.item().from(oreTag),
                IngredientCreatorAccess.gas().from(MekanismGases.HYDROGEN_CHLORIDE, 1),
                shard.getItemStack(4)
                ).build(consumer, Mekanism.rl(basePath + "shard/from_ore"));
        // Dirty Slurry
        ChemicalDissolutionRecipeBuilder.dissolution(
                IngredientCreatorAccess.item().from(oreTag),
                IngredientCreatorAccess.gas().from(MekanismGases.SULFURIC_ACID, 1),
                slurry.getDirtySlurry().getStack(1_000)
                ).build(consumer, Mekanism.rl(basePath + "slurry/dirty/from_ore"));

        // From raw ore
        // Dust
        ItemStackToItemStackRecipeBuilder.enriching(IngredientCreatorAccess.item().from(rawTag, 3), dust.getItemStack(4))
        .build(consumer, Mekanism.rl(basePath + "dust/from_raw_ore"));
        // Clump
        ItemStackChemicalToItemStackRecipeBuilder.purifying(
                IngredientCreatorAccess.item().from(rawTag),
                IngredientCreatorAccess.gas().from(MekanismGases.OXYGEN, 1),
                clump.getItemStack(2)
                ).build(consumer, Mekanism.rl(basePath + "clump/from_raw_ore"));
        // Shard
        ItemStackChemicalToItemStackRecipeBuilder.injecting(
                IngredientCreatorAccess.item().from(rawTag, 3),
                IngredientCreatorAccess.gas().from(MekanismGases.HYDROGEN_CHLORIDE, 1),
                shard.getItemStack(8)
                ).build(consumer, Mekanism.rl(basePath + "shard/from_raw_ore"));
        // Dirty Slurry
        ChemicalDissolutionRecipeBuilder.dissolution(
                IngredientCreatorAccess.item().from(rawTag, 3),
                IngredientCreatorAccess.gas().from(MekanismGases.SULFURIC_ACID, 1),
                slurry.getDirtySlurry().getStack(2_000)
                ).build(consumer, Mekanism.rl(basePath + "slurry/dirty/from_raw_ore"));

        // From raw ore block
        // Dust
        ItemStackToItemStackRecipeBuilder.enriching(IngredientCreatorAccess.item().from(rawBlockTag), dust.getItemStack(12))
        .build(consumer, Mekanism.rl(basePath + "dust/from_raw_block"));
        // Clump
        ItemStackChemicalToItemStackRecipeBuilder.purifying(
                IngredientCreatorAccess.item().from(rawBlockTag),
                IngredientCreatorAccess.gas().from(MekanismGases.OXYGEN, 2),
                clump.getItemStack(18)
          ).build(consumer, Mekanism.rl(basePath + "clump/from_raw_block"));
        // Shard
        ItemStackChemicalToItemStackRecipeBuilder.injecting(
                IngredientCreatorAccess.item().from(rawBlockTag),
                IngredientCreatorAccess.gas().from(MekanismGases.HYDROGEN_CHLORIDE, 2),
                shard.getItemStack(24)
          ).build(consumer, Mekanism.rl(basePath + "shard/from_raw_block"));
        // Dirty Slurry
        ChemicalDissolutionRecipeBuilder.dissolution(
                IngredientCreatorAccess.item().from(rawBlockTag),
                IngredientCreatorAccess.gas().from(MekanismGases.SULFURIC_ACID, 2),
                slurry.getDirtySlurry().getStack(6_000)
          ).build(consumer, Mekanism.rl(basePath + "slurry/dirty/from_raw_block"));
    }

    private void addCoalOreProcessingRecipes(Consumer<FinishedRecipe> consumer, String basePath) {
        //from dust
        ItemStackToItemStackRecipeBuilder.enriching(
              IngredientCreatorAccess.item().from(MekanismTags.Items.DUSTS_COAL),
              new ItemStack(Items.COAL)
        ).build(consumer, Mekanism.rl(basePath + "from_dust"));
        //from ore
        ItemStackToItemStackRecipeBuilder.enriching(
              IngredientCreatorAccess.item().from(Tags.Items.ORES_COAL),
              new ItemStack(Items.COAL, 2)
        ).build(consumer, Mekanism.rl(basePath + "from_ore"));
        //to dust
        ItemStackToItemStackRecipeBuilder.crushing(
              IngredientCreatorAccess.item().from(Items.COAL),
              MekanismItems.COAL_DUST.getItemStack()
        ).build(consumer, Mekanism.rl(basePath + "to_dust"));
        ItemStackIngredient forOre = IngredientCreatorAccess.item().from(MekanismTags.Items.DUSTS_COAL, 8);
        //to ore
        CombinerRecipeBuilder.combining(
              forOre,
              IngredientCreatorAccess.item().from(Tags.Items.COBBLESTONE_NORMAL),
              new ItemStack(Blocks.COAL_ORE)
        ).build(consumer, Mekanism.rl(basePath + "to_ore"));
        //to deepslate ore
        CombinerRecipeBuilder.combining(
              forOre,
              IngredientCreatorAccess.item().from(Tags.Items.COBBLESTONE_DEEPSLATE),
              new ItemStack(Blocks.DEEPSLATE_COAL_ORE)
        ).build(consumer, Mekanism.rl(basePath + "to_deepslate_ore"));
    }

    private void addOreProcessingGemRecipes(Consumer<FinishedRecipe> consumer, String basePath, ItemLike ore, @Nullable ItemLike deepslateOre, TagKey<Item> oreTag,
          IItemProvider dust, TagKey<Item> dustTag, ItemLike gem, TagKey<Item> gemTag, int fromOre, int toOre, TagKey<Item> combineType) {
        addOreProcessingGemRecipes(consumer, basePath, ore, deepslateOre, oreTag, dust, dustTag, gem, gemTag, fromOre, toOre,
              IngredientCreatorAccess.item().from(combineType));
    }

    private void addOreProcessingGemRecipes(Consumer<FinishedRecipe> consumer, String basePath, ItemLike ore, @Nullable ItemLike deepslateOre, TagKey<Item> oreTag,
          IItemProvider dust, TagKey<Item> dustTag, ItemLike gem, TagKey<Item> gemTag, int fromOre, int toOre, ItemStackIngredient combineType) {
        //from dust
        ItemStackToItemStackRecipeBuilder.enriching(
              IngredientCreatorAccess.item().from(dustTag),
              new ItemStack(gem)
        ).build(consumer, Mekanism.rl(basePath + "from_dust"));
        //from ore
        ItemStackToItemStackRecipeBuilder.enriching(
              IngredientCreatorAccess.item().from(oreTag),
              new ItemStack(gem, fromOre)
        ).build(consumer, Mekanism.rl(basePath + "from_ore"));
        //to dust
        ItemStackToItemStackRecipeBuilder.crushing(
              IngredientCreatorAccess.item().from(gemTag),
              dust.getItemStack()
        ).build(consumer, Mekanism.rl(basePath + "to_dust"));
        ItemStackIngredient forOre = IngredientCreatorAccess.item().from(dustTag, toOre);
        //to ore
        CombinerRecipeBuilder.combining(
              forOre,
              combineType,
              new ItemStack(ore)
        ).build(consumer, Mekanism.rl(basePath + "to_ore"));
        if (deepslateOre != null) {
            //to deepslate ore
            CombinerRecipeBuilder.combining(
                  forOre,
                  IngredientCreatorAccess.item().from(Tags.Items.COBBLESTONE_DEEPSLATE),
                  new ItemStack(deepslateOre)
            ).build(consumer, Mekanism.rl(basePath + "to_deepslate_ore"));
        }
    }

    private void addNetheriteProcessingRecipes(Consumer<FinishedRecipe> consumer, String basePath) {
        //Ancient Debris to Dirty Netherite Scrap
        ItemStackToItemStackRecipeBuilder.crushing(
              IngredientCreatorAccess.item().from(Tags.Items.ORES_NETHERITE_SCRAP),
              MekanismItems.DIRTY_NETHERITE_SCRAP.getItemStack(3)
        ).build(consumer, Mekanism.rl(basePath + "ancient_debris_to_dirty_scrap"));
        //Dirty Netherite Scrap to Netherite Scrap
        ItemStackToItemStackRecipeBuilder.enriching(
              IngredientCreatorAccess.item().from(MekanismItems.DIRTY_NETHERITE_SCRAP),
              new ItemStack(Items.NETHERITE_SCRAP)
        ).build(consumer, Mekanism.rl(basePath + "dirty_scrap_to_scrap"));
        //Ancient Debris to Netherite Scrap
        ItemStackToItemStackRecipeBuilder.enriching(
              IngredientCreatorAccess.item().from(Tags.Items.ORES_NETHERITE_SCRAP),
              new ItemStack(Items.NETHERITE_SCRAP, 2)
        ).build(consumer, Mekanism.rl(basePath + "ancient_debris_to_scrap"));
        //Netherite scrap to netherite dust
        ItemStackChemicalToItemStackRecipeBuilder.metallurgicInfusing(
              IngredientCreatorAccess.item().from(Items.NETHERITE_SCRAP, 4),
              IngredientCreatorAccess.infusion().from(MekanismTags.InfuseTypes.GOLD, 40),
              MekanismItems.NETHERITE_DUST.getItemStack()
        ).build(consumer, Mekanism.rl(basePath + "scrap_to_dust"));
        //Netherite Dust to Netherite Ingot
        RecipeProviderUtil.addSmeltingBlastingRecipes(consumer, Ingredient.of(MekanismTags.Items.DUSTS_NETHERITE), Items.NETHERITE_INGOT, 1, 200,
              Mekanism.rl(basePath + "ingot_from_dust_blasting"), Mekanism.rl(basePath + "ingot_from_dust_smelting"));
        //Netherite Ingot to Netherite Dust
        ItemStackToItemStackRecipeBuilder.crushing(
              IngredientCreatorAccess.item().from(Tags.Items.INGOTS_NETHERITE),
              MekanismItems.NETHERITE_DUST.getItemStack()
        ).build(consumer, Mekanism.rl(basePath + "ingot_to_dust"));
        //Netherite Dust to Ancient Debris
        // Note: We only require two dust as that is equivalent to 8 scrap
        CombinerRecipeBuilder.combining(
              IngredientCreatorAccess.item().from(MekanismTags.Items.DUSTS_NETHERITE, 2),
              IngredientCreatorAccess.item().from(Blocks.BASALT),
              new ItemStack(Blocks.ANCIENT_DEBRIS)
        ).build(consumer, Mekanism.rl(basePath + "dust_to_ancient_debris"));
    }

    private void addBronzeProcessingRecipes(Consumer<FinishedRecipe> consumer, String basePath) {
        //Dust
        //from infusing
        ItemStackChemicalToItemStackRecipeBuilder.metallurgicInfusing(
              IngredientCreatorAccess.item().from(MekanismTags.Items.PROCESSED_RESOURCES.get(ResourceType.DUST, PrimaryResource.COPPER), 3),
              IngredientCreatorAccess.infusion().from(MekanismTags.InfuseTypes.TIN, 10),
              MekanismItems.BRONZE_DUST.getItemStack(4)
        ).build(consumer, Mekanism.rl(basePath + "dust/from_infusing"));
        //from ingot
        ItemStackToItemStackRecipeBuilder.crushing(
              IngredientCreatorAccess.item().from(MekanismTags.Items.INGOTS_BRONZE),
              MekanismItems.BRONZE_DUST.getItemStack()
        ).build(consumer, Mekanism.rl(basePath + "dust/from_ingot"));
        //Ingot
        //from block
        ExtendedShapelessRecipeBuilder.shapelessRecipe(MekanismItems.BRONZE_INGOT, 9)
              .addIngredient(MekanismTags.Items.STORAGE_BLOCKS_BRONZE)
              .build(consumer, Mekanism.rl(basePath + "ingot/from_block"));
        //from dust
        RecipeProviderUtil.addSmeltingBlastingRecipes(consumer, Ingredient.of(MekanismTags.Items.DUSTS_BRONZE), MekanismItems.BRONZE_INGOT, 0.35F, 200,
              Mekanism.rl(basePath + "ingot/from_dust_blasting"), Mekanism.rl(basePath + "ingot/from_dust_smelting"));
        //from infusing
        ItemStackChemicalToItemStackRecipeBuilder.metallurgicInfusing(
              IngredientCreatorAccess.item().from(Tags.Items.INGOTS_COPPER, 3),
              IngredientCreatorAccess.infusion().from(MekanismTags.InfuseTypes.TIN, 10),
              MekanismItems.BRONZE_INGOT.getItemStack(4)
        ).build(consumer, Mekanism.rl(basePath + "ingot/from_infusing"));
        //from nuggets
        ExtendedShapedRecipeBuilder.shapedRecipe(MekanismItems.BRONZE_INGOT)
              .pattern(MekanismRecipeProvider.STORAGE_PATTERN)
              .key(Pattern.CONSTANT, MekanismTags.Items.NUGGETS_BRONZE)
              .build(consumer, Mekanism.rl(basePath + "ingot/from_nuggets"));
    }

    private void addRedstoneProcessingRecipes(Consumer<FinishedRecipe> consumer, String basePath) {
        //from ore
        ItemStackToItemStackRecipeBuilder.enriching(
              IngredientCreatorAccess.item().from(Tags.Items.ORES_REDSTONE),
              new ItemStack(Items.REDSTONE, 12)
        ).build(consumer, Mekanism.rl(basePath + "from_ore"));
        ItemStackIngredient forOre = IngredientCreatorAccess.item().from(Tags.Items.DUSTS_REDSTONE, 16);
        //to ore
        CombinerRecipeBuilder.combining(
              forOre,
              IngredientCreatorAccess.item().from(Tags.Items.COBBLESTONE_NORMAL),
              new ItemStack(Blocks.REDSTONE_ORE)
        ).build(consumer, Mekanism.rl(basePath + "to_ore"));
        //to deepslate ore
        CombinerRecipeBuilder.combining(
              forOre,
              IngredientCreatorAccess.item().from(Tags.Items.COBBLESTONE_DEEPSLATE),
              new ItemStack(Blocks.DEEPSLATE_REDSTONE_ORE)
        ).build(consumer, Mekanism.rl(basePath + "to_deepslate_ore"));
    }

    private void addRefinedGlowstoneProcessingRecipes(Consumer<FinishedRecipe> consumer, String basePath) {
        //Ingot
        //from block
        ExtendedShapelessRecipeBuilder.shapelessRecipe(MekanismItems.REFINED_GLOWSTONE_INGOT, 9)
              .addIngredient(MekanismTags.Items.STORAGE_BLOCKS_REFINED_GLOWSTONE)
              .build(consumer, Mekanism.rl(basePath + "ingot/from_block"));
        //from dust
        ItemStackChemicalToItemStackRecipeBuilder.compressing(
              IngredientCreatorAccess.item().from(Tags.Items.DUSTS_GLOWSTONE),
              IngredientCreatorAccess.gas().from(MekanismGases.OSMIUM, 1),
              MekanismItems.REFINED_GLOWSTONE_INGOT.getItemStack()
        ).build(consumer, Mekanism.rl(basePath + "ingot/from_dust"));
        //from nuggets
        ExtendedShapedRecipeBuilder.shapedRecipe(MekanismItems.REFINED_GLOWSTONE_INGOT)
              .pattern(MekanismRecipeProvider.STORAGE_PATTERN)
              .key(Pattern.CONSTANT, MekanismTags.Items.NUGGETS_REFINED_GLOWSTONE)
              .build(consumer, Mekanism.rl(basePath + "ingot/from_nuggets"));
        //Ingot -> dust
        ItemStackToItemStackRecipeBuilder.crushing(
              IngredientCreatorAccess.item().from(MekanismTags.Items.INGOTS_REFINED_GLOWSTONE),
              new ItemStack(Items.GLOWSTONE_DUST)
        ).build(consumer, Mekanism.rl(basePath + "ingot_to_dust"));
    }

    private void addRefinedObsidianProcessingRecipes(Consumer<FinishedRecipe> consumer, String basePath) {
        //Dust
        //from ingot
        ItemStackToItemStackRecipeBuilder.crushing(
              IngredientCreatorAccess.item().from(MekanismTags.Items.INGOTS_REFINED_OBSIDIAN),
              MekanismItems.REFINED_OBSIDIAN_DUST.getItemStack()
        ).build(consumer, Mekanism.rl(basePath + "dust/from_ingot"));
        //from obsidian dust
        ItemStackChemicalToItemStackRecipeBuilder.metallurgicInfusing(
              IngredientCreatorAccess.item().from(MekanismTags.Items.DUSTS_OBSIDIAN),
              IngredientCreatorAccess.infusion().from(MekanismTags.InfuseTypes.DIAMOND, 10),
              MekanismItems.REFINED_OBSIDIAN_DUST.getItemStack()
        ).build(consumer, Mekanism.rl(basePath + "dust/from_obsidian_dust"));
        //Ingot
        //from block
        ExtendedShapelessRecipeBuilder.shapelessRecipe(MekanismItems.REFINED_OBSIDIAN_INGOT, 9)
              .addIngredient(MekanismTags.Items.STORAGE_BLOCKS_REFINED_OBSIDIAN)
              .build(consumer, Mekanism.rl(basePath + "ingot/from_block"));
        //from dust
        ItemStackChemicalToItemStackRecipeBuilder.compressing(
              IngredientCreatorAccess.item().from(MekanismTags.Items.DUSTS_REFINED_OBSIDIAN),
              IngredientCreatorAccess.gas().from(MekanismGases.OSMIUM, 1),
              MekanismItems.REFINED_OBSIDIAN_INGOT.getItemStack()
        ).build(consumer, Mekanism.rl(basePath + "ingot/from_dust"));
        //from nuggets
        ExtendedShapedRecipeBuilder.shapedRecipe(MekanismItems.REFINED_OBSIDIAN_INGOT)
              .pattern(MekanismRecipeProvider.STORAGE_PATTERN)
              .key(Pattern.CONSTANT, MekanismTags.Items.NUGGETS_REFINED_OBSIDIAN)
              .build(consumer, Mekanism.rl(basePath + "ingot/from_nuggets"));
    }

    private void addSteelProcessingRecipes(Consumer<FinishedRecipe> consumer, String basePath) {
        //Ingot
        //from block
        ExtendedShapelessRecipeBuilder.shapelessRecipe(MekanismItems.STEEL_INGOT, 9)
              .addIngredient(MekanismTags.Items.STORAGE_BLOCKS_STEEL)
              .build(consumer, Mekanism.rl(basePath + "ingot/from_block"));
        //from dust
        RecipeProviderUtil.addSmeltingBlastingRecipes(consumer, Ingredient.of(MekanismTags.Items.DUSTS_STEEL), MekanismItems.STEEL_INGOT, 0.4F, 200,
              Mekanism.rl(basePath + "ingot/from_dust_blasting"), Mekanism.rl(basePath + "ingot/from_dust_smelting"));
        //from nuggets
        ExtendedShapedRecipeBuilder.shapedRecipe(MekanismItems.STEEL_INGOT)
              .pattern(MekanismRecipeProvider.STORAGE_PATTERN)
              .key(Pattern.CONSTANT, MekanismTags.Items.NUGGETS_STEEL)
              .build(consumer, Mekanism.rl(basePath + "ingot/from_nuggets"));
        //Enriched iron -> dust
        ItemStackChemicalToItemStackRecipeBuilder.metallurgicInfusing(
              IngredientCreatorAccess.item().from(MekanismItems.ENRICHED_IRON),
              IngredientCreatorAccess.infusion().from(MekanismTags.InfuseTypes.CARBON, 10),
              MekanismItems.STEEL_DUST.getItemStack()
        ).build(consumer, Mekanism.rl(basePath + "enriched_iron_to_dust"));
        //Ingot -> dust
        ItemStackToItemStackRecipeBuilder.crushing(
              IngredientCreatorAccess.item().from(MekanismTags.Items.INGOTS_STEEL),
              MekanismItems.STEEL_DUST.getItemStack()
        ).build(consumer, Mekanism.rl(basePath + "ingot_to_dust"));
    }

    private void addFluoriteRecipes(Consumer<FinishedRecipe> consumer, String basePath) {
        OreBlockType fluorite = MekanismBlocks.ORES.get(OreType.FLUORITE);
        addOreProcessingGemRecipes(consumer, basePath, fluorite.stone(), fluorite.deepslate(), MekanismTags.Items.ORES.get(OreType.FLUORITE),
              MekanismItems.FLUORITE_DUST, MekanismTags.Items.DUSTS_FLUORITE, MekanismItems.FLUORITE_GEM, MekanismTags.Items.GEMS_FLUORITE, 6, 14,
              Tags.Items.COBBLESTONE_NORMAL);
        //Gem from block
        ExtendedShapelessRecipeBuilder.shapelessRecipe(MekanismItems.FLUORITE_GEM, 9)
              .addIngredient(MekanismTags.Items.STORAGE_BLOCKS_FLUORITE)
              .build(consumer, Mekanism.rl(basePath + "from_block"));
    }

    private void addUraniumRecipes(Consumer<FinishedRecipe> consumer, String basePath) {
        //yellow cake
        ItemStackToItemStackRecipeBuilder.enriching(
              IngredientCreatorAccess.item().from(MekanismTags.Items.PROCESSED_RESOURCES.get(ResourceType.INGOT, PrimaryResource.URANIUM)),
              MekanismItems.YELLOW_CAKE_URANIUM.getItemStack(2)
        ).build(consumer, Mekanism.rl(basePath + "yellow_cake_uranium"));
        //hydrofluoric acid
        ChemicalDissolutionRecipeBuilder.dissolution(
              IngredientCreatorAccess.item().from(MekanismTags.Items.GEMS_FLUORITE),
              IngredientCreatorAccess.gas().from(MekanismGases.SULFURIC_ACID, 1),
              MekanismGases.HYDROFLUORIC_ACID.getStack(1_000)
        ).build(consumer, Mekanism.rl(basePath + "hydrofluoric_acid"));
        ChemicalDissolutionRecipeBuilder.dissolution(
              IngredientCreatorAccess.item().from(MekanismTags.Items.STORAGE_BLOCKS_FLUORITE),
              IngredientCreatorAccess.gas().from(MekanismGases.SULFURIC_ACID, 9),
              MekanismGases.HYDROFLUORIC_ACID.getStack(9_000)
        ).build(consumer, Mekanism.rl(basePath + "hydrofluoric_acid_from_block"));
        //uranium oxide
        ItemStackToChemicalRecipeBuilder.oxidizing(
              IngredientCreatorAccess.item().from(MekanismTags.Items.YELLOW_CAKE_URANIUM),
              MekanismGases.URANIUM_OXIDE.getStack(250)
        ).build(consumer, Mekanism.rl(basePath + "uranium_oxide"));
        //uranium hexafluoride
        ChemicalChemicalToChemicalRecipeBuilder.chemicalInfusing(
              IngredientCreatorAccess.gas().from(MekanismGases.HYDROFLUORIC_ACID, 1),
              IngredientCreatorAccess.gas().from(MekanismGases.URANIUM_OXIDE, 1),
              MekanismGases.URANIUM_HEXAFLUORIDE.getStack(2)
        ).build(consumer, Mekanism.rl(basePath + "sulfuric_acid"));
        //fissile fuel
        GasToGasRecipeBuilder.centrifuging(
              IngredientCreatorAccess.gas().from(MekanismGases.URANIUM_HEXAFLUORIDE, 1),
              MekanismGases.FISSILE_FUEL.getStack(1)
        ).build(consumer, Mekanism.rl(basePath + "fissile_fuel"));
        //fissile fuel reprocessing (IMPORTANT)
        ItemStackChemicalToItemStackRecipeBuilder.injecting(
              IngredientCreatorAccess.item().from(MekanismTags.Items.PELLETS_PLUTONIUM),
              IngredientCreatorAccess.gas().from(MekanismGases.HYDROGEN_CHLORIDE, 1),
              MekanismItems.REPROCESSED_FISSILE_FRAGMENT.getItemStack(4)
        ).build(consumer, Mekanism.rl(basePath + "reprocessing/from_plutonium"));
        //fragment -> fuel
        ItemStackToChemicalRecipeBuilder.oxidizing(
              IngredientCreatorAccess.item().from(MekanismItems.REPROCESSED_FISSILE_FRAGMENT),
              MekanismGases.FISSILE_FUEL.getStack(2_000)
        ).build(consumer, Mekanism.rl(basePath + "reprocessing/to_fuel"));
    }
}