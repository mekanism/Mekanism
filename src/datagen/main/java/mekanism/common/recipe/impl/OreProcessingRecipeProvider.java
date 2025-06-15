package mekanism.common.recipe.impl;

import java.util.Objects;
import mekanism.api.MekanismAPITags;
import mekanism.api.chemical.ChemicalStack;
import mekanism.api.datagen.recipe.builder.ChemicalChemicalToChemicalRecipeBuilder;
import mekanism.api.datagen.recipe.builder.ChemicalCrystallizerRecipeBuilder;
import mekanism.api.datagen.recipe.builder.ChemicalDissolutionRecipeBuilder;
import mekanism.api.datagen.recipe.builder.ChemicalToChemicalRecipeBuilder;
import mekanism.api.datagen.recipe.builder.CombinerRecipeBuilder;
import mekanism.api.datagen.recipe.builder.FluidChemicalToChemicalRecipeBuilder;
import mekanism.api.datagen.recipe.builder.ItemStackChemicalToItemStackRecipeBuilder;
import mekanism.api.datagen.recipe.builder.ItemStackToChemicalRecipeBuilder;
import mekanism.api.datagen.recipe.builder.ItemStackToItemStackRecipeBuilder;
import mekanism.api.recipes.ingredients.ItemStackIngredient;
import mekanism.api.recipes.ingredients.creator.IngredientCreatorAccess;
import mekanism.common.Mekanism;
import mekanism.common.recipe.BaseRecipeProvider;
import mekanism.common.recipe.ISubRecipeProvider;
import mekanism.common.recipe.RecipeProviderUtil;
import mekanism.common.recipe.builder.ExtendedShapedRecipeBuilder;
import mekanism.common.recipe.builder.ExtendedShapelessRecipeBuilder;
import mekanism.common.recipe.pattern.Pattern;
import mekanism.common.registration.impl.SlurryRegistryObject;
import mekanism.common.registries.MekanismBlocks;
import mekanism.common.registries.MekanismChemicals;
import mekanism.common.registries.MekanismItems;
import mekanism.common.resource.PrimaryResource;
import mekanism.common.resource.ResourceType;
import mekanism.common.resource.ore.OreBlockType;
import mekanism.common.resource.ore.OreType;
import mekanism.common.tags.MekanismTags;
import mekanism.common.util.EnumUtils;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.tags.FluidTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.ItemLike;
import net.neoforged.neoforge.common.Tags;
import org.jetbrains.annotations.Nullable;

class OreProcessingRecipeProvider implements ISubRecipeProvider {

    @Override
    public void addRecipes(RecipeOutput consumer, HolderLookup.Provider registries) {
        String basePath = "processing/";
        for (PrimaryResource resource : EnumUtils.PRIMARY_RESOURCES) {
            addDynamicOreProcessingIngotRecipes(consumer, basePath + resource.getRegistrySuffix() + "/", resource);
        }
        //Raw Gold plus netherrack to nether gold ore
        CombinerRecipeBuilder.combining(
              IngredientCreatorAccess.item().from(Tags.Items.RAW_MATERIALS_GOLD, 8),
              IngredientCreatorAccess.item().from(Items.NETHERRACK),
              new ItemStack(Items.NETHER_GOLD_ORE)
        ).build(consumer, Mekanism.rl(basePath + "gold/ore/nether_from_raw"));

        //Iron -> enriched iron
        ItemStackChemicalToItemStackRecipeBuilder.metallurgicInfusing(
              IngredientCreatorAccess.item().from(Tags.Items.INGOTS_IRON),
              IngredientCreatorAccess.chemicalStack().from(MekanismAPITags.Chemicals.CARBON, 10),
              MekanismItems.ENRICHED_IRON.asStack(),
              false
        ).build(consumer, Mekanism.rl(basePath + "iron/enriched"));
        ItemStackChemicalToItemStackRecipeBuilder.metallurgicInfusing(
              IngredientCreatorAccess.item().from(MekanismTags.Items.PROCESSED_RESOURCES.get(ResourceType.DUST, PrimaryResource.IRON)),
              IngredientCreatorAccess.chemicalStack().from(MekanismAPITags.Chemicals.CARBON, 10),
              MekanismItems.ENRICHED_IRON.asStack(),
              false
        ).build(consumer, Mekanism.rl(basePath + "iron/enriched_dust"));
        addNetheriteProcessingRecipes(consumer, basePath + "netherite/");
        addBronzeProcessingRecipes(consumer, basePath + "bronze/");
        addCoalOreProcessingRecipes(consumer, basePath + "coal/");
        addOreProcessingGemRecipes(consumer, basePath + "diamond/", Items.DIAMOND_ORE, Items.DEEPSLATE_DIAMOND_ORE, Tags.Items.ORES_DIAMOND,
              MekanismItems.DIAMOND_DUST, MekanismTags.Items.DUSTS_DIAMOND, Items.DIAMOND, Tags.Items.GEMS_DIAMOND, 2, 5, Tags.Items.COBBLESTONES_NORMAL);
        addOreProcessingGemRecipes(consumer, basePath + "emerald/", Items.EMERALD_ORE, Items.DEEPSLATE_EMERALD_ORE, Tags.Items.ORES_EMERALD,
              MekanismItems.EMERALD_DUST, MekanismTags.Items.DUSTS_EMERALD, Items.EMERALD, Tags.Items.GEMS_EMERALD, 2, 5, Tags.Items.COBBLESTONES_NORMAL);
        addOreProcessingGemRecipes(consumer, basePath + "lapis_lazuli/", Items.LAPIS_ORE, Items.DEEPSLATE_LAPIS_ORE, Tags.Items.ORES_LAPIS,
              MekanismItems.LAPIS_LAZULI_DUST, MekanismTags.Items.DUSTS_LAPIS, Items.LAPIS_LAZULI, Tags.Items.GEMS_LAPIS, 12, 27, Tags.Items.COBBLESTONES_NORMAL);
        addOreProcessingGemRecipes(consumer, basePath + "quartz/", Items.NETHER_QUARTZ_ORE, null, Tags.Items.ORES_QUARTZ, MekanismItems.QUARTZ_DUST,
              MekanismTags.Items.DUSTS_QUARTZ, Items.QUARTZ, Tags.Items.GEMS_QUARTZ, 2, 14, IngredientCreatorAccess.item().from(Items.NETHERRACK));
        addRedstoneProcessingRecipes(consumer, basePath + "redstone/");
        addRefinedGlowstoneProcessingRecipes(consumer, basePath + "refined_glowstone/");
        addRefinedObsidianProcessingRecipes(consumer, basePath + "refined_obsidian/");
        addSteelProcessingRecipes(consumer, basePath + "steel/");
        addFluoriteRecipes(consumer, basePath + "fluorite/");
        addUraniumRecipes(consumer, basePath + "uranium/");
    }

    @SuppressWarnings("deprecation")
    private void addDynamicOreProcessingIngotRecipes(RecipeOutput consumer, String basePath, PrimaryResource resource) {
        //TODO - 1.18: Take into account if the ore is a single drop or multi like vanilla copper is?
        // We may want to consider this at least for the silk touched ore to ingot?
        Holder<Item> ingot, nugget, block, raw, rawBlock, ore, deepslateOre;
        TagKey<Item> ingotTag, nuggetTag, rawTag, rawBlockTag;
        TagKey<Item> oreTag = resource.getOreTag();
        float dustExperience = 0.3F;
        int toOre = 8;
        switch (resource) {
            case IRON -> {
                ingot = Items.IRON_INGOT.builtInRegistryHolder();
                ingotTag = Tags.Items.INGOTS_IRON;
                nugget = Items.IRON_NUGGET.builtInRegistryHolder();
                nuggetTag = Tags.Items.NUGGETS_IRON;
                block = Items.IRON_BLOCK.builtInRegistryHolder();
                raw = Items.RAW_IRON.builtInRegistryHolder();
                rawTag = Tags.Items.RAW_MATERIALS_IRON;
                rawBlock = Items.RAW_IRON_BLOCK.builtInRegistryHolder();
                rawBlockTag = Tags.Items.STORAGE_BLOCKS_RAW_IRON;
                ore = Items.IRON_ORE.builtInRegistryHolder();
                deepslateOre = Items.DEEPSLATE_IRON_ORE.builtInRegistryHolder();
                dustExperience = 0.35F;
            }
            case GOLD -> {
                ingot = Items.GOLD_INGOT.builtInRegistryHolder();
                ingotTag = Tags.Items.INGOTS_GOLD;
                nugget = Items.GOLD_NUGGET.builtInRegistryHolder();
                nuggetTag = Tags.Items.NUGGETS_GOLD;
                block = Items.GOLD_BLOCK.builtInRegistryHolder();
                raw = Items.RAW_GOLD.builtInRegistryHolder();
                rawTag = Tags.Items.RAW_MATERIALS_GOLD;
                rawBlock = Items.RAW_GOLD_BLOCK.builtInRegistryHolder();
                rawBlockTag = Tags.Items.STORAGE_BLOCKS_RAW_GOLD;
                ore = Items.GOLD_ORE.builtInRegistryHolder();
                deepslateOre = Items.DEEPSLATE_GOLD_ORE.builtInRegistryHolder();
                dustExperience = 0.5F;
            }
            case COPPER -> {
                ingot = Items.COPPER_INGOT.builtInRegistryHolder();
                ingotTag = Tags.Items.INGOTS_COPPER;
                nugget = null;
                nuggetTag = null;
                block = Items.COPPER_BLOCK.builtInRegistryHolder();
                raw = Items.RAW_COPPER.builtInRegistryHolder();
                rawTag = Tags.Items.RAW_MATERIALS_COPPER;
                rawBlock = Items.RAW_COPPER_BLOCK.builtInRegistryHolder();
                rawBlockTag = Tags.Items.STORAGE_BLOCKS_RAW_COPPER;
                ore = Items.COPPER_ORE.builtInRegistryHolder();
                deepslateOre = Items.DEEPSLATE_COPPER_ORE.builtInRegistryHolder();
                dustExperience = 0.35F;
                toOre = 20;//8 * 2.5
            }
            default -> {
                ingot = Objects.requireNonNull(MekanismItems.PROCESSED_RESOURCES.get(ResourceType.INGOT, resource));
                ingotTag = Objects.requireNonNull(MekanismTags.Items.PROCESSED_RESOURCES.get(ResourceType.INGOT, resource));
                nugget = Objects.requireNonNull(MekanismItems.PROCESSED_RESOURCES.get(ResourceType.NUGGET, resource));
                nuggetTag = Objects.requireNonNull(MekanismTags.Items.PROCESSED_RESOURCES.get(ResourceType.NUGGET, resource));
                block = Objects.requireNonNull(MekanismBlocks.PROCESSED_RESOURCE_BLOCKS.get(resource)).getItemHolder();
                raw = Objects.requireNonNull(MekanismItems.PROCESSED_RESOURCES.get(ResourceType.RAW, resource));
                rawTag = Objects.requireNonNull(MekanismTags.Items.PROCESSED_RESOURCES.get(ResourceType.RAW, resource));
                rawBlock = Objects.requireNonNull(MekanismBlocks.PROCESSED_RESOURCE_BLOCKS.get(resource.getRawResourceBlockInfo())).getItemHolder();
                rawBlockTag = Objects.requireNonNull(MekanismTags.Items.PROCESSED_RESOURCE_BLOCKS.get(resource.getRawResourceBlockInfo()));
                OreBlockType oreBlockType = Objects.requireNonNull(MekanismBlocks.ORES.get(OreType.get(resource)));
                ore = oreBlockType.stone().getItemHolder();
                deepslateOre = oreBlockType.deepslate().getItemHolder();
            }
        }

        Holder<Item> dust = Objects.requireNonNull(MekanismItems.PROCESSED_RESOURCES.get(ResourceType.DUST, resource));
        Holder<Item> dirtyDust = Objects.requireNonNull(MekanismItems.PROCESSED_RESOURCES.get(ResourceType.DIRTY_DUST, resource));
        Holder<Item> clump = Objects.requireNonNull(MekanismItems.PROCESSED_RESOURCES.get(ResourceType.CLUMP, resource));
        Holder<Item> crystal = Objects.requireNonNull(MekanismItems.PROCESSED_RESOURCES.get(ResourceType.CRYSTAL, resource));
        Holder<Item> shard = Objects.requireNonNull(MekanismItems.PROCESSED_RESOURCES.get(ResourceType.SHARD, resource));
        TagKey<Item> dustTag = Objects.requireNonNull(MekanismTags.Items.PROCESSED_RESOURCES.get(ResourceType.DUST, resource));
        TagKey<Item> dirtyDustTag = Objects.requireNonNull(MekanismTags.Items.PROCESSED_RESOURCES.get(ResourceType.DIRTY_DUST, resource));
        TagKey<Item> clumpTag = Objects.requireNonNull(MekanismTags.Items.PROCESSED_RESOURCES.get(ResourceType.CLUMP, resource));
        TagKey<Item> shardTag = Objects.requireNonNull(MekanismTags.Items.PROCESSED_RESOURCES.get(ResourceType.SHARD, resource));
        TagKey<Item> crystalTag = Objects.requireNonNull(MekanismTags.Items.PROCESSED_RESOURCES.get(ResourceType.CRYSTAL, resource));

        SlurryRegistryObject<?, ?> slurry = MekanismChemicals.PROCESSED_RESOURCES.get(resource);

        // Miscellaneous
        if (!resource.isVanilla()) {
            // from block
            ExtendedShapelessRecipeBuilder.shapelessRecipe(ingot, 9)
                  .addIngredient(block)
                  .build(consumer, Mekanism.rl(basePath + "ingot/from_block"));
            // to block
            ExtendedShapedRecipeBuilder.shapedRecipe(block)
                  .pattern(MekanismRecipeProvider.TYPED_STORAGE_PATTERN)
                  .key(Pattern.PREVIOUS, ingot)
                  .key(Pattern.CONSTANT, ingotTag)
                  .build(consumer, Mekanism.rl(basePath + "storage_blocks/from_ingots"));
            // from nuggets
            ExtendedShapedRecipeBuilder.shapedRecipe(ingot)
                  .pattern(MekanismRecipeProvider.TYPED_STORAGE_PATTERN)
                  .key(Pattern.PREVIOUS, nugget)
                  .key(Pattern.CONSTANT, nuggetTag)
                  .build(consumer, Mekanism.rl(basePath + "ingot/from_nuggets"));
            // to nuggets
            ExtendedShapelessRecipeBuilder.shapelessRecipe(nugget, 9)
                  .addIngredient(ingot)
                  .build(consumer, Mekanism.rl(basePath + "nugget/from_ingot"));
            // from ore
            RecipeProviderUtil.addSmeltingBlastingRecipes(consumer, BaseRecipeProvider.createIngredient(ore, deepslateOre), ingot, dustExperience * 2, 200,
                  Mekanism.rl(basePath + "ingot/from_ore_blasting"), Mekanism.rl(basePath + "ingot/from_ore_smelting"));
            // from raw
            RecipeProviderUtil.addSmeltingBlastingRecipes(consumer, BaseRecipeProvider.createIngredient(raw), ingot, dustExperience * 2, 200,
                  Mekanism.rl(basePath + "ingot/from_raw_blasting"), Mekanism.rl(basePath + "ingot/from_raw_smelting"));
            // raw from raw block
            ExtendedShapelessRecipeBuilder.shapelessRecipe(raw, 9)
                  .addIngredient(rawBlock)
                  .build(consumer, Mekanism.rl(basePath + "raw/from_raw_block"));
            // raw to raw block
            ExtendedShapedRecipeBuilder.shapedRecipe(rawBlock)
                  .pattern(MekanismRecipeProvider.TYPED_STORAGE_PATTERN)
                  .key(Pattern.PREVIOUS, raw)
                  .key(Pattern.CONSTANT, rawTag)
                  .build(consumer, Mekanism.rl(basePath + "raw_storage_blocks/from_raw"));
        }

        ItemStackIngredient forOre = IngredientCreatorAccess.item().from(rawTag, toOre);
        // Ore from Dust
        CombinerRecipeBuilder.combining(
              forOre,
              IngredientCreatorAccess.item().from(Tags.Items.COBBLESTONES_NORMAL),
              new ItemStack(ore)
        ).build(consumer, Mekanism.rl(basePath + "ore/from_raw"));
        // Deepslate Ore from Dust
        CombinerRecipeBuilder.combining(
              forOre,
              IngredientCreatorAccess.item().from(Tags.Items.COBBLESTONES_DEEPSLATE),
              new ItemStack(deepslateOre)
        ).build(consumer, Mekanism.rl(basePath + "ore/deepslate_from_raw"));

        //Dust from Ingot
        ItemStackToItemStackRecipeBuilder.crushing(IngredientCreatorAccess.item().from(ingotTag), new ItemStack(dust))
              .build(consumer, Mekanism.rl(basePath + "dust/from_ingot"));

        // Intermediate Steps
        // Ingot from Dust
        RecipeProviderUtil.addSmeltingBlastingRecipes(consumer, BaseRecipeProvider.createIngredient(dust), ingot, dustExperience, 200,
              Mekanism.rl(basePath + "ingot/from_dust_blasting"), Mekanism.rl(basePath + "ingot/from_dust_smelting"));
        // Dust from Dirty Dust
        ItemStackToItemStackRecipeBuilder.enriching(IngredientCreatorAccess.item().from(dirtyDustTag), new ItemStack(dust))
              .build(consumer, Mekanism.rl(basePath + "dust/from_dirty_dust"));
        // Dirty Dust from Clump
        ItemStackToItemStackRecipeBuilder.crushing(IngredientCreatorAccess.item().from(clumpTag), new ItemStack(dirtyDust))
              .build(consumer, Mekanism.rl(basePath + "dirty_dust/from_clump"));
        // Clump from Shard
        ItemStackChemicalToItemStackRecipeBuilder.purifying(
              IngredientCreatorAccess.item().from(shardTag),
              IngredientCreatorAccess.chemicalStack().fromHolder(MekanismChemicals.OXYGEN, 1),
              new ItemStack(clump),
              true
        ).build(consumer, Mekanism.rl(basePath + "clump/from_shard"));
        // Shard from Crystal
        ItemStackChemicalToItemStackRecipeBuilder.injecting(
              IngredientCreatorAccess.item().from(crystalTag),
              IngredientCreatorAccess.chemicalStack().fromHolder(MekanismChemicals.HYDROGEN_CHLORIDE, 1),
              new ItemStack(shard),
              true
        ).build(consumer, Mekanism.rl(basePath + "shard/from_crystal"));
        // Crystal from Clean Slurry
        ChemicalCrystallizerRecipeBuilder.crystallizing(IngredientCreatorAccess.chemicalStack().fromHolder(slurry.getCleanSlurry(), 200), new ItemStack(crystal))
              .build(consumer, Mekanism.rl(basePath + "crystal/from_slurry"));
        // Clean Slurry from Dirty Slurry
        FluidChemicalToChemicalRecipeBuilder.washing(
              IngredientCreatorAccess.fluid().from(FluidTags.WATER, 5),
              IngredientCreatorAccess.chemicalStack().fromHolder(slurry, 1),
              new ChemicalStack(slurry.getCleanSlurry(), 1)
        ).build(consumer, Mekanism.rl(basePath + "slurry/clean"));

        // From ore
        // Dust
        ItemStackToItemStackRecipeBuilder.enriching(IngredientCreatorAccess.item().from(oreTag), new ItemStack(dust, 2))
              .build(consumer, Mekanism.rl(basePath + "dust/from_ore"));
        // Clump
        ItemStackChemicalToItemStackRecipeBuilder.purifying(
              IngredientCreatorAccess.item().from(oreTag),
              IngredientCreatorAccess.chemicalStack().fromHolder(MekanismChemicals.OXYGEN, 1),
              new ItemStack(clump, 3),
              true
        ).build(consumer, Mekanism.rl(basePath + "clump/from_ore"));
        // Shard
        ItemStackChemicalToItemStackRecipeBuilder.injecting(
              IngredientCreatorAccess.item().from(oreTag),
              IngredientCreatorAccess.chemicalStack().fromHolder(MekanismChemicals.HYDROGEN_CHLORIDE, 1),
              new ItemStack(shard, 4),
              true
        ).build(consumer, Mekanism.rl(basePath + "shard/from_ore"));
        // Dirty Slurry
        ChemicalDissolutionRecipeBuilder.dissolution(
              IngredientCreatorAccess.item().from(oreTag),
              IngredientCreatorAccess.chemicalStack().fromHolder(MekanismChemicals.SULFURIC_ACID, 1),
              new ChemicalStack(slurry, 1_000),
              true
        ).build(consumer, Mekanism.rl(basePath + "slurry/dirty/from_ore"));

        // From raw ore
        // Dust
        ItemStackToItemStackRecipeBuilder.enriching(IngredientCreatorAccess.item().from(rawTag, 3), new ItemStack(dust, 4))
              .build(consumer, Mekanism.rl(basePath + "dust/from_raw_ore"));
        // Clump
        ItemStackChemicalToItemStackRecipeBuilder.purifying(
              IngredientCreatorAccess.item().from(rawTag),
              IngredientCreatorAccess.chemicalStack().fromHolder(MekanismChemicals.OXYGEN, 1),
              new ItemStack(clump, 2),
              true
        ).build(consumer, Mekanism.rl(basePath + "clump/from_raw_ore"));
        // Shard
        ItemStackChemicalToItemStackRecipeBuilder.injecting(
              IngredientCreatorAccess.item().from(rawTag, 3),
              IngredientCreatorAccess.chemicalStack().fromHolder(MekanismChemicals.HYDROGEN_CHLORIDE, 1),
              new ItemStack(shard, 8),
              true
        ).build(consumer, Mekanism.rl(basePath + "shard/from_raw_ore"));
        // Dirty Slurry
        ChemicalDissolutionRecipeBuilder.dissolution(
              IngredientCreatorAccess.item().from(rawTag, 3),
              IngredientCreatorAccess.chemicalStack().fromHolder(MekanismChemicals.SULFURIC_ACID, 1),
              new ChemicalStack(slurry, 2_000),
              true
        ).build(consumer, Mekanism.rl(basePath + "slurry/dirty/from_raw_ore"));

        // From raw ore block
        // Dust
        ItemStackToItemStackRecipeBuilder.enriching(IngredientCreatorAccess.item().from(rawBlockTag), new ItemStack(dust, 12))
              .build(consumer, Mekanism.rl(basePath + "dust/from_raw_block"));
        // Clump
        ItemStackChemicalToItemStackRecipeBuilder.purifying(
              IngredientCreatorAccess.item().from(rawBlockTag),
              IngredientCreatorAccess.chemicalStack().fromHolder(MekanismChemicals.OXYGEN, 2),
              new ItemStack(clump, 18),
              true
        ).build(consumer, Mekanism.rl(basePath + "clump/from_raw_block"));
        // Shard
        ItemStackChemicalToItemStackRecipeBuilder.injecting(
              IngredientCreatorAccess.item().from(rawBlockTag),
              IngredientCreatorAccess.chemicalStack().fromHolder(MekanismChemicals.HYDROGEN_CHLORIDE, 2),
              new ItemStack(shard, 24),
              true
        ).build(consumer, Mekanism.rl(basePath + "shard/from_raw_block"));
        // Dirty Slurry
        ChemicalDissolutionRecipeBuilder.dissolution(
              IngredientCreatorAccess.item().from(rawBlockTag),
              IngredientCreatorAccess.chemicalStack().fromHolder(MekanismChemicals.SULFURIC_ACID, 2),
              new ChemicalStack(slurry, 6_000),
              true
        ).build(consumer, Mekanism.rl(basePath + "slurry/dirty/from_raw_block"));
    }

    private void addCoalOreProcessingRecipes(RecipeOutput consumer, String basePath) {
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
              MekanismItems.COAL_DUST.asStack()
        ).build(consumer, Mekanism.rl(basePath + "to_dust"));
        ItemStackIngredient forOre = IngredientCreatorAccess.item().from(MekanismTags.Items.DUSTS_COAL, 8);
        //to ore
        CombinerRecipeBuilder.combining(
              forOre,
              IngredientCreatorAccess.item().from(Tags.Items.COBBLESTONES_NORMAL),
              new ItemStack(Items.COAL_ORE)
        ).build(consumer, Mekanism.rl(basePath + "to_ore"));
        //to deepslate ore
        CombinerRecipeBuilder.combining(
              forOre,
              IngredientCreatorAccess.item().from(Tags.Items.COBBLESTONES_DEEPSLATE),
              new ItemStack(Items.DEEPSLATE_COAL_ORE)
        ).build(consumer, Mekanism.rl(basePath + "to_deepslate_ore"));
    }

    private void addOreProcessingGemRecipes(RecipeOutput consumer, String basePath, ItemLike ore, @Nullable ItemLike deepslateOre, TagKey<Item> oreTag,
          Holder<Item> dust, TagKey<Item> dustTag, ItemLike gem, TagKey<Item> gemTag, int fromOre, int toOre, TagKey<Item> combineType) {
        addOreProcessingGemRecipes(consumer, basePath, ore, deepslateOre, oreTag, dust, dustTag, gem, gemTag, fromOre, toOre,
              IngredientCreatorAccess.item().from(combineType));
    }

    private void addOreProcessingGemRecipes(RecipeOutput consumer, String basePath, ItemLike ore, @Nullable ItemLike deepslateOre, TagKey<Item> oreTag,
          Holder<Item> dust, TagKey<Item> dustTag, ItemLike gem, TagKey<Item> gemTag, int fromOre, int toOre, ItemStackIngredient combineType) {
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
              new ItemStack(dust)
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
                  IngredientCreatorAccess.item().from(Tags.Items.COBBLESTONES_DEEPSLATE),
                  new ItemStack(deepslateOre)
            ).build(consumer, Mekanism.rl(basePath + "to_deepslate_ore"));
        }
    }

    private void addNetheriteProcessingRecipes(RecipeOutput consumer, String basePath) {
        //Ancient Debris to Dirty Netherite Scrap
        ItemStackToItemStackRecipeBuilder.crushing(
              IngredientCreatorAccess.item().from(Tags.Items.ORES_NETHERITE_SCRAP),
              MekanismItems.DIRTY_NETHERITE_SCRAP.asStack(3)
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
              IngredientCreatorAccess.chemicalStack().from(MekanismAPITags.Chemicals.GOLD, 40),
              MekanismItems.NETHERITE_DUST.asStack(),
              false
        ).build(consumer, Mekanism.rl(basePath + "scrap_to_dust"));
        //Netherite Dust to Netherite Ingot
        RecipeProviderUtil.addSmeltingBlastingRecipes(consumer, Ingredient.of(MekanismItems.NETHERITE_DUST), Items.NETHERITE_INGOT.builtInRegistryHolder(), 1, 200,
              Mekanism.rl(basePath + "ingot_from_dust_blasting"), Mekanism.rl(basePath + "ingot_from_dust_smelting"));
        //Netherite Ingot to Netherite Dust
        ItemStackToItemStackRecipeBuilder.crushing(
              IngredientCreatorAccess.item().from(Tags.Items.INGOTS_NETHERITE),
              MekanismItems.NETHERITE_DUST.asStack()
        ).build(consumer, Mekanism.rl(basePath + "ingot_to_dust"));
        //Netherite Dust to Ancient Debris
        // Note: We only require two dust as that is equivalent to 8 scrap
        CombinerRecipeBuilder.combining(
              IngredientCreatorAccess.item().from(MekanismTags.Items.DUSTS_NETHERITE, 2),
              IngredientCreatorAccess.item().from(Items.BASALT),
              new ItemStack(Items.ANCIENT_DEBRIS)
        ).build(consumer, Mekanism.rl(basePath + "dust_to_ancient_debris"));
    }

    private void addBronzeProcessingRecipes(RecipeOutput consumer, String basePath) {
        //Dust
        //from infusing
        ItemStackChemicalToItemStackRecipeBuilder.metallurgicInfusing(
              IngredientCreatorAccess.item().from(MekanismTags.Items.PROCESSED_RESOURCES.get(ResourceType.DUST, PrimaryResource.COPPER), 3),
              IngredientCreatorAccess.chemicalStack().from(MekanismAPITags.Chemicals.TIN, 10),
              MekanismItems.BRONZE_DUST.asStack(4),
              false
        ).build(consumer, Mekanism.rl(basePath + "dust/from_infusing"));
        //from ingot
        ItemStackToItemStackRecipeBuilder.crushing(
              IngredientCreatorAccess.item().from(MekanismTags.Items.INGOTS_BRONZE),
              MekanismItems.BRONZE_DUST.asStack()
        ).build(consumer, Mekanism.rl(basePath + "dust/from_ingot"));
        //Ingot
        //from block
        ExtendedShapelessRecipeBuilder.shapelessRecipe(MekanismItems.BRONZE_INGOT, 9)
              .addIngredient(MekanismBlocks.BRONZE_BLOCK)
              .build(consumer, Mekanism.rl(basePath + "ingot/from_block"));
        //from dust
        RecipeProviderUtil.addSmeltingBlastingRecipes(consumer, Ingredient.of(MekanismItems.BRONZE_DUST), MekanismItems.BRONZE_INGOT, 0.35F, 200,
              Mekanism.rl(basePath + "ingot/from_dust_blasting"), Mekanism.rl(basePath + "ingot/from_dust_smelting"));
        //from infusing
        ItemStackChemicalToItemStackRecipeBuilder.metallurgicInfusing(
              IngredientCreatorAccess.item().from(Tags.Items.INGOTS_COPPER, 3),
              IngredientCreatorAccess.chemicalStack().from(MekanismAPITags.Chemicals.TIN, 10),
              MekanismItems.BRONZE_INGOT.asStack(4),
              false
        ).build(consumer, Mekanism.rl(basePath + "ingot/from_infusing"));
        //from nuggets
        ExtendedShapedRecipeBuilder.shapedRecipe(MekanismItems.BRONZE_INGOT)
              .pattern(MekanismRecipeProvider.TYPED_STORAGE_PATTERN)
              .key(Pattern.PREVIOUS, MekanismItems.BRONZE_NUGGET)
              .key(Pattern.CONSTANT, MekanismTags.Items.NUGGETS_BRONZE)
              .build(consumer, Mekanism.rl(basePath + "ingot/from_nuggets"));
    }

    private void addRedstoneProcessingRecipes(RecipeOutput consumer, String basePath) {
        //from ore
        ItemStackToItemStackRecipeBuilder.enriching(
              IngredientCreatorAccess.item().from(Tags.Items.ORES_REDSTONE),
              new ItemStack(Items.REDSTONE, 12)
        ).build(consumer, Mekanism.rl(basePath + "from_ore"));
        ItemStackIngredient forOre = IngredientCreatorAccess.item().from(Tags.Items.DUSTS_REDSTONE, 16);
        //to ore
        CombinerRecipeBuilder.combining(
              forOre,
              IngredientCreatorAccess.item().from(Tags.Items.COBBLESTONES_NORMAL),
              new ItemStack(Items.REDSTONE_ORE)
        ).build(consumer, Mekanism.rl(basePath + "to_ore"));
        //to deepslate ore
        CombinerRecipeBuilder.combining(
              forOre,
              IngredientCreatorAccess.item().from(Tags.Items.COBBLESTONES_DEEPSLATE),
              new ItemStack(Items.DEEPSLATE_REDSTONE_ORE)
        ).build(consumer, Mekanism.rl(basePath + "to_deepslate_ore"));
    }

    private void addRefinedGlowstoneProcessingRecipes(RecipeOutput consumer, String basePath) {
        //Ingot
        //from block
        ExtendedShapelessRecipeBuilder.shapelessRecipe(MekanismItems.REFINED_GLOWSTONE_INGOT, 9)
              .addIngredient(MekanismBlocks.REFINED_GLOWSTONE_BLOCK)
              .build(consumer, Mekanism.rl(basePath + "ingot/from_block"));
        //from dust
        ItemStackChemicalToItemStackRecipeBuilder.compressing(
              IngredientCreatorAccess.item().from(Tags.Items.DUSTS_GLOWSTONE),
              IngredientCreatorAccess.chemicalStack().fromHolder(MekanismChemicals.OSMIUM, 1),
              MekanismItems.REFINED_GLOWSTONE_INGOT.asStack(),
              true
        ).build(consumer, Mekanism.rl(basePath + "ingot/from_dust"));
        //from nuggets
        ExtendedShapedRecipeBuilder.shapedRecipe(MekanismItems.REFINED_GLOWSTONE_INGOT)
              .pattern(MekanismRecipeProvider.TYPED_STORAGE_PATTERN)
              .key(Pattern.PREVIOUS, MekanismItems.REFINED_GLOWSTONE_NUGGET)
              .key(Pattern.CONSTANT, MekanismTags.Items.NUGGETS_REFINED_GLOWSTONE)
              .build(consumer, Mekanism.rl(basePath + "ingot/from_nuggets"));
        //Ingot -> dust
        ItemStackToItemStackRecipeBuilder.crushing(
              IngredientCreatorAccess.item().from(MekanismTags.Items.INGOTS_REFINED_GLOWSTONE),
              new ItemStack(Items.GLOWSTONE_DUST)
        ).build(consumer, Mekanism.rl(basePath + "ingot_to_dust"));
    }

    private void addRefinedObsidianProcessingRecipes(RecipeOutput consumer, String basePath) {
        //Dust
        //from ingot
        ItemStackToItemStackRecipeBuilder.crushing(
              IngredientCreatorAccess.item().from(MekanismTags.Items.INGOTS_REFINED_OBSIDIAN),
              MekanismItems.REFINED_OBSIDIAN_DUST.asStack()
        ).build(consumer, Mekanism.rl(basePath + "dust/from_ingot"));
        //from obsidian dust
        ItemStackChemicalToItemStackRecipeBuilder.metallurgicInfusing(
              IngredientCreatorAccess.item().from(MekanismTags.Items.DUSTS_OBSIDIAN),
              IngredientCreatorAccess.chemicalStack().from(MekanismAPITags.Chemicals.DIAMOND, 10),
              MekanismItems.REFINED_OBSIDIAN_DUST.asStack(),
              false
        ).build(consumer, Mekanism.rl(basePath + "dust/from_obsidian_dust"));
        //Ingot
        //from block
        ExtendedShapelessRecipeBuilder.shapelessRecipe(MekanismItems.REFINED_OBSIDIAN_INGOT, 9)
              .addIngredient(MekanismBlocks.REFINED_OBSIDIAN_BLOCK)
              .build(consumer, Mekanism.rl(basePath + "ingot/from_block"));
        //from dust
        ItemStackChemicalToItemStackRecipeBuilder.compressing(
              IngredientCreatorAccess.item().from(MekanismTags.Items.DUSTS_REFINED_OBSIDIAN),
              IngredientCreatorAccess.chemicalStack().fromHolder(MekanismChemicals.OSMIUM, 1),
              MekanismItems.REFINED_OBSIDIAN_INGOT.asStack(),
              true
        ).build(consumer, Mekanism.rl(basePath + "ingot/from_dust"));
        //from nuggets
        ExtendedShapedRecipeBuilder.shapedRecipe(MekanismItems.REFINED_OBSIDIAN_INGOT)
              .pattern(MekanismRecipeProvider.TYPED_STORAGE_PATTERN)
              .key(Pattern.PREVIOUS, MekanismItems.REFINED_OBSIDIAN_NUGGET)
              .key(Pattern.CONSTANT, MekanismTags.Items.NUGGETS_REFINED_OBSIDIAN)
              .build(consumer, Mekanism.rl(basePath + "ingot/from_nuggets"));
    }

    private void addSteelProcessingRecipes(RecipeOutput consumer, String basePath) {
        //Ingot
        //from block
        ExtendedShapelessRecipeBuilder.shapelessRecipe(MekanismItems.STEEL_INGOT, 9)
              .addIngredient(MekanismBlocks.STEEL_BLOCK)
              .build(consumer, Mekanism.rl(basePath + "ingot/from_block"));
        //from dust
        RecipeProviderUtil.addSmeltingBlastingRecipes(consumer, Ingredient.of(MekanismItems.STEEL_DUST), MekanismItems.STEEL_INGOT, 0.4F, 200,
              Mekanism.rl(basePath + "ingot/from_dust_blasting"), Mekanism.rl(basePath + "ingot/from_dust_smelting"));
        //from nuggets
        ExtendedShapedRecipeBuilder.shapedRecipe(MekanismItems.STEEL_INGOT)
              .pattern(MekanismRecipeProvider.TYPED_STORAGE_PATTERN)
              .key(Pattern.PREVIOUS, MekanismItems.STEEL_NUGGET)
              .key(Pattern.CONSTANT, MekanismTags.Items.NUGGETS_STEEL)
              .build(consumer, Mekanism.rl(basePath + "ingot/from_nuggets"));
        //Enriched iron -> dust
        ItemStackChemicalToItemStackRecipeBuilder.metallurgicInfusing(
              IngredientCreatorAccess.item().from(MekanismItems.ENRICHED_IRON),
              IngredientCreatorAccess.chemicalStack().from(MekanismAPITags.Chemicals.CARBON, 10),
              MekanismItems.STEEL_DUST.asStack(),
              false
        ).build(consumer, Mekanism.rl(basePath + "enriched_iron_to_dust"));
        //Ingot -> dust
        ItemStackToItemStackRecipeBuilder.crushing(
              IngredientCreatorAccess.item().from(MekanismTags.Items.INGOTS_STEEL),
              MekanismItems.STEEL_DUST.asStack()
        ).build(consumer, Mekanism.rl(basePath + "ingot_to_dust"));
    }

    private void addFluoriteRecipes(RecipeOutput consumer, String basePath) {
        OreBlockType fluorite = MekanismBlocks.ORES.get(OreType.FLUORITE);
        addOreProcessingGemRecipes(consumer, basePath, fluorite.stone(), fluorite.deepslate(), MekanismTags.Items.ORES.get(OreType.FLUORITE),
              MekanismItems.FLUORITE_DUST, MekanismTags.Items.DUSTS_FLUORITE, MekanismItems.FLUORITE_GEM, MekanismTags.Items.GEMS_FLUORITE, 6, 14,
              Tags.Items.COBBLESTONES_NORMAL);
        //Gem from block
        ExtendedShapelessRecipeBuilder.shapelessRecipe(MekanismItems.FLUORITE_GEM, 9)
              .addIngredient(MekanismBlocks.FLUORITE_BLOCK)
              .build(consumer, Mekanism.rl(basePath + "from_block"));
    }

    private void addUraniumRecipes(RecipeOutput consumer, String basePath) {
        //yellow cake
        ItemStackToItemStackRecipeBuilder.enriching(
              IngredientCreatorAccess.item().from(MekanismTags.Items.PROCESSED_RESOURCES.get(ResourceType.INGOT, PrimaryResource.URANIUM)),
              MekanismItems.YELLOW_CAKE_URANIUM.asStack(2)
        ).build(consumer, Mekanism.rl(basePath + "yellow_cake_uranium"));
        //hydrofluoric acid
        ChemicalDissolutionRecipeBuilder.dissolution(
              IngredientCreatorAccess.item().from(MekanismTags.Items.GEMS_FLUORITE),
              IngredientCreatorAccess.chemicalStack().fromHolder(MekanismChemicals.SULFURIC_ACID, 1),
              MekanismChemicals.HYDROFLUORIC_ACID.asStack(1_000),
              true
        ).build(consumer, Mekanism.rl(basePath + "hydrofluoric_acid"));
        ChemicalDissolutionRecipeBuilder.dissolution(
              IngredientCreatorAccess.item().from(MekanismTags.Items.STORAGE_BLOCKS_FLUORITE),
              IngredientCreatorAccess.chemicalStack().fromHolder(MekanismChemicals.SULFURIC_ACID, 9),
              MekanismChemicals.HYDROFLUORIC_ACID.asStack(9_000),
              true
        ).build(consumer, Mekanism.rl(basePath + "hydrofluoric_acid_from_block"));
        //uranium oxide
        ItemStackToChemicalRecipeBuilder.oxidizing(
              IngredientCreatorAccess.item().from(MekanismItems.YELLOW_CAKE_URANIUM),
              MekanismChemicals.URANIUM_OXIDE.asStack(250)
        ).build(consumer, Mekanism.rl(basePath + "uranium_oxide"));
        //uranium hexafluoride
        ChemicalChemicalToChemicalRecipeBuilder.chemicalInfusing(
              IngredientCreatorAccess.chemicalStack().fromHolder(MekanismChemicals.HYDROFLUORIC_ACID, 1),
              IngredientCreatorAccess.chemicalStack().fromHolder(MekanismChemicals.URANIUM_OXIDE, 1),
              MekanismChemicals.URANIUM_HEXAFLUORIDE.asStack(2)
        ).build(consumer, Mekanism.rl(basePath + "sulfuric_acid"));
        //fissile fuel
        ChemicalToChemicalRecipeBuilder.centrifuging(
              IngredientCreatorAccess.chemicalStack().fromHolder(MekanismChemicals.URANIUM_HEXAFLUORIDE, 1),
              MekanismChemicals.FISSILE_FUEL.asStack(1)
        ).build(consumer, Mekanism.rl(basePath + "fissile_fuel"));
        //fissile fuel reprocessing (IMPORTANT)
        ItemStackChemicalToItemStackRecipeBuilder.injecting(
              IngredientCreatorAccess.item().from(MekanismTags.Items.PELLETS_PLUTONIUM),
              IngredientCreatorAccess.chemicalStack().fromHolder(MekanismChemicals.HYDROGEN_CHLORIDE, 1),
              MekanismItems.REPROCESSED_FISSILE_FRAGMENT.asStack(4),
              true
        ).build(consumer, Mekanism.rl(basePath + "reprocessing/from_plutonium"));
        //fragment -> fuel
        ItemStackToChemicalRecipeBuilder.oxidizing(
              IngredientCreatorAccess.item().from(MekanismItems.REPROCESSED_FISSILE_FRAGMENT),
              MekanismChemicals.FISSILE_FUEL.asStack(2_000)
        ).build(consumer, Mekanism.rl(basePath + "reprocessing/to_fuel"));
    }
}