package mekanism.common.recipe.impl;

import java.util.Objects;
import mekanism.api.MekanismAPITags;
import mekanism.api.chemical.Chemical;
import mekanism.api.chemical.ChemicalIds;
import mekanism.api.chemical.CleanDirtySlurryId;
import mekanism.api.datagen.recipe.RecipeCriterion;
import mekanism.api.datagen.recipe.builder.ChemicalChemicalToChemicalRecipeBuilder;
import mekanism.api.datagen.recipe.builder.ChemicalCrystallizerRecipeBuilder;
import mekanism.api.datagen.recipe.builder.ChemicalDissolutionRecipeBuilder;
import mekanism.api.datagen.recipe.builder.ChemicalToChemicalRecipeBuilder;
import mekanism.api.datagen.recipe.builder.CombinerRecipeBuilder;
import mekanism.api.datagen.recipe.builder.FluidChemicalToChemicalRecipeBuilder;
import mekanism.api.datagen.recipe.builder.ItemStackChemicalToItemStackRecipeBuilder;
import mekanism.api.datagen.recipe.builder.ItemStackToChemicalRecipeBuilder;
import mekanism.api.datagen.recipe.builder.ItemStackToItemStackRecipeBuilder;
import mekanism.api.recipes.book.CombiningBookCategory;
import mekanism.api.recipes.book.CrushingBookCategory;
import mekanism.api.recipes.book.CrystallizingBookCategory;
import mekanism.api.recipes.book.DissolutionBookCategory;
import mekanism.api.recipes.book.EnrichingBookCategory;
import mekanism.api.recipes.book.InjectingBookCategory;
import mekanism.api.recipes.ingredients.ItemStackIngredient;
import mekanism.api.recipes.ingredients.creator.IngredientCreatorAccess;
import mekanism.common.recipe.BaseRecipeProvider;
import mekanism.common.recipe.RecipeProviderUtil;
import mekanism.common.recipe.builder.ExtendedShapedRecipeBuilder;
import mekanism.common.recipe.builder.ExtendedShapelessRecipeBuilder;
import mekanism.common.recipe.pattern.Pattern;
import mekanism.common.registration.impl.BlockRegistryObject;
import mekanism.common.registries.MekanismBlocks;
import mekanism.common.registries.MekanismChemicals;
import mekanism.common.registries.MekanismItems;
import mekanism.common.resource.PrimaryResource;
import mekanism.common.resource.ResourceType;
import mekanism.common.resource.ore.OreBlockType;
import mekanism.common.resource.ore.OreType;
import mekanism.common.tags.MekanismTags;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderGetter;
import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.data.recipes.RecipeProvider;
import net.minecraft.references.BlockItemId;
import net.minecraft.references.BlockItemIds;
import net.minecraft.references.ItemIds;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.BlockItemTagId;
import net.minecraft.tags.FluidTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.display.SlotDisplay.TagSlotDisplay;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.material.Fluid;
import net.neoforged.neoforge.common.Tags;
import org.jspecify.annotations.Nullable;

class OreProcessingRecipeProvider extends BaseSubRecipeProvider {

    OreProcessingRecipeProvider(HolderGetter<Item> items, HolderGetter<Fluid> fluids, HolderGetter<Chemical> chemicals) {
        super(items, fluids, chemicals);
    }

    @Override
    public void addRecipes(RecipeOutput output) {
        String basePath = "processing/";
        for (PrimaryResource resource : PrimaryResource.VALUES) {
            addDynamicOreProcessingIngotRecipes(output, basePath + resource.getRegistrySuffix() + "/", resource);
        }
        //Raw Gold plus netherrack to nether gold ore
        CombinerRecipeBuilder.combining(
              IngredientCreatorAccess.item().from(this.items, Tags.Items.RAW_MATERIALS_GOLD, 8),
              IngredientCreatorAccess.item().from(this.items, Tags.BlockItems.NETHERRACKS),
              template(BlockItemIds.NETHER_GOLD_ORE)
        ).category(CombiningBookCategory.BUILDING)
              .group("gold_ore")
              .unlockedBy("has_raw_gold", has(Tags.Items.RAW_MATERIALS_GOLD))
              .save(output, basePath + "gold/ore/nether_from_raw");

        //Iron -> enriched iron
        ItemStackChemicalToItemStackRecipeBuilder.metallurgicInfusing(
              IngredientCreatorAccess.item().from(this.items, Tags.Items.INGOTS_IRON),
              IngredientCreatorAccess.chemicalStack().from(this.chemicals, MekanismAPITags.Chemicals.CARBON, 10),
              false,
              MekanismItems.ENRICHED_IRON.asTemplate()
        ).group(MekanismItems.ENRICHED_IRON.key())
              .unlockedBy("has_iron_ingot", has(Tags.Items.INGOTS_IRON))
              .save(output, basePath + "iron/enriched");
        TagKey<Item> ironDust = MekanismTags.Items.getProcessedResource(ResourceType.DUST, PrimaryResource.IRON);
        ItemStackChemicalToItemStackRecipeBuilder.metallurgicInfusing(
              IngredientCreatorAccess.item().from(this.items, ironDust),
              IngredientCreatorAccess.chemicalStack().from(this.chemicals, MekanismAPITags.Chemicals.CARBON, 10),
              false,
              MekanismItems.ENRICHED_IRON.asTemplate()
        ).group(MekanismItems.ENRICHED_IRON.key())
              .unlockedBy("has_iron_dust", has(ironDust))
              .save(output, basePath + "iron/enriched_dust");
        addNetheriteProcessingRecipes(output, basePath + "netherite/");
        addBronzeProcessingRecipes(output, basePath + "bronze/");
        addCoalOreProcessingRecipes(output, basePath + "coal/");
        addOreProcessingGemRecipes(output, basePath + "diamond/", BlockItemIds.DIAMOND_ORE, BlockItemIds.DEEPSLATE_DIAMOND_ORE, Tags.BlockItems.ORES_DIAMOND,
              MekanismItems.DIAMOND_DUST, MekanismTags.Items.DUSTS_DIAMOND, ItemIds.DIAMOND, Tags.Items.GEMS_DIAMOND, 2, 5, Tags.BlockItems.COBBLESTONES_NORMAL);
        addOreProcessingGemRecipes(output, basePath + "emerald/", BlockItemIds.EMERALD_ORE, BlockItemIds.DEEPSLATE_EMERALD_ORE, Tags.BlockItems.ORES_EMERALD,
              MekanismItems.EMERALD_DUST, MekanismTags.Items.DUSTS_EMERALD, ItemIds.EMERALD, Tags.Items.GEMS_EMERALD, 2, 5, Tags.BlockItems.COBBLESTONES_NORMAL);
        addOreProcessingGemRecipes(output, basePath + "lapis_lazuli/", BlockItemIds.LAPIS_ORE, BlockItemIds.DEEPSLATE_LAPIS_ORE, Tags.BlockItems.ORES_LAPIS,
              MekanismItems.LAPIS_LAZULI_DUST, MekanismTags.Items.DUSTS_LAPIS, ItemIds.LAPIS_LAZULI, Tags.Items.GEMS_LAPIS, 12, 27, Tags.BlockItems.COBBLESTONES_NORMAL);
        addOreProcessingGemRecipes(output, basePath + "quartz/", BlockItemIds.NETHER_QUARTZ_ORE, null, Tags.BlockItems.ORES_QUARTZ,
              MekanismItems.QUARTZ_DUST, MekanismTags.Items.DUSTS_QUARTZ, ItemIds.QUARTZ, Tags.Items.GEMS_QUARTZ, 2, 14, Tags.BlockItems.NETHERRACKS);
        addRedstoneProcessingRecipes(output, basePath + "redstone/");
        addRefinedGlowstoneProcessingRecipes(output, basePath + "refined_glowstone/");
        addRefinedObsidianProcessingRecipes(output, basePath + "refined_obsidian/");
        addSteelProcessingRecipes(output, basePath + "steel/");
        addFluoriteRecipes(output, basePath + "fluorite/");
        addUraniumRecipes(output, basePath + "uranium/");
    }

    private void addDynamicOreProcessingIngotRecipes(RecipeOutput output, String basePath, PrimaryResource resource) {
        //TODO - 1.18: Take into account if the ore is a single drop or multi like vanilla copper is?
        // We may want to consider this at least for the silk touched ore to ingot?
        Holder<Item> ingot, nugget, block, raw, rawBlock, ore, deepslateOre;
        TagKey<Item> ingotTag, nuggetTag, rawTag, rawBlockTag;
        TagKey<Item> oreTag = resource.getOreTag();
        float dustExperience = 0.3F;
        int toOre = 8;
        switch (resource) {
            case IRON -> {
                ingot = items.getOrThrow(ItemIds.IRON_INGOT);
                ingotTag = Tags.Items.INGOTS_IRON;
                nugget = items.getOrThrow(ItemIds.IRON_NUGGET);
                nuggetTag = Tags.Items.NUGGETS_IRON;
                block = items.getOrThrow(BlockItemIds.IRON_BLOCK.item());
                raw = items.getOrThrow(ItemIds.RAW_IRON);
                rawTag = Tags.Items.RAW_MATERIALS_IRON;
                rawBlock = items.getOrThrow(BlockItemIds.RAW_IRON_BLOCK.item());
                rawBlockTag = Tags.BlockItems.STORAGE_BLOCKS_RAW_IRON.item();
                ore = items.getOrThrow(BlockItemIds.IRON_ORE.item());
                deepslateOre = items.getOrThrow(BlockItemIds.DEEPSLATE_IRON_ORE.item());
                dustExperience = 0.35F;
            }
            case GOLD -> {
                ingot = items.getOrThrow(ItemIds.GOLD_INGOT);
                ingotTag = Tags.Items.INGOTS_GOLD;
                nugget = items.getOrThrow(ItemIds.GOLD_NUGGET);
                nuggetTag = Tags.Items.NUGGETS_GOLD;
                block = items.getOrThrow(BlockItemIds.GOLD_BLOCK.item());
                raw = items.getOrThrow(ItemIds.RAW_GOLD);
                rawTag = Tags.Items.RAW_MATERIALS_GOLD;
                rawBlock = items.getOrThrow(BlockItemIds.RAW_GOLD_BLOCK.item());
                rawBlockTag = Tags.BlockItems.STORAGE_BLOCKS_RAW_GOLD.item();
                ore = items.getOrThrow(BlockItemIds.GOLD_ORE.item());
                deepslateOre = items.getOrThrow(BlockItemIds.DEEPSLATE_GOLD_ORE.item());
                dustExperience = 0.5F;
            }
            case COPPER -> {
                ingot = items.getOrThrow(ItemIds.COPPER_INGOT);
                ingotTag = Tags.Items.INGOTS_COPPER;
                nugget = items.getOrThrow(ItemIds.COPPER_NUGGET);
                nuggetTag = Tags.Items.NUGGETS_COPPER;
                block = items.getOrThrow(BlockItemIds.COPPER_BLOCK.weathering().unaffected().item());
                raw = items.getOrThrow(ItemIds.RAW_COPPER);
                rawTag = Tags.Items.RAW_MATERIALS_COPPER;
                rawBlock = items.getOrThrow(BlockItemIds.RAW_COPPER_BLOCK.item());
                rawBlockTag = Tags.BlockItems.STORAGE_BLOCKS_RAW_COPPER.item();
                ore = items.getOrThrow(BlockItemIds.COPPER_ORE.item());
                deepslateOre = items.getOrThrow(BlockItemIds.DEEPSLATE_COPPER_ORE.item());
                dustExperience = 0.35F;
                toOre = 20;//8 * 2.5
            }
            default -> {
                ingot = Objects.requireNonNull(MekanismItems.getProcessedResource(ResourceType.INGOT, resource));
                ingotTag = Objects.requireNonNull(MekanismTags.Items.getProcessedResource(ResourceType.INGOT, resource));
                nugget = Objects.requireNonNull(MekanismItems.getProcessedResource(ResourceType.NUGGET, resource));
                nuggetTag = Objects.requireNonNull(MekanismTags.Items.getProcessedResource(ResourceType.NUGGET, resource));
                block = Objects.requireNonNull(MekanismBlocks.PROCESSED_RESOURCE_BLOCKS.get(resource.getResourceBlockInfo())).getItemHolder();
                raw = Objects.requireNonNull(MekanismItems.getProcessedResource(ResourceType.RAW, resource));
                rawTag = Objects.requireNonNull(MekanismTags.Items.getProcessedResource(ResourceType.RAW, resource));
                rawBlock = Objects.requireNonNull(MekanismBlocks.PROCESSED_RESOURCE_BLOCKS.get(resource.getRawResourceBlockInfo())).getItemHolder();
                rawBlockTag = Objects.requireNonNull(MekanismTags.BlockItems.PROCESSED_RESOURCE_BLOCKS.get(resource.getRawResourceBlockInfo())).item();
                OreBlockType oreBlockType = Objects.requireNonNull(MekanismBlocks.ORES.get(OreType.get(resource)));
                ore = oreBlockType.stone().getItemHolder();
                deepslateOre = oreBlockType.deepslate().getItemHolder();
            }
        }

        Holder<Item> dust = Objects.requireNonNull(MekanismItems.getProcessedResource(ResourceType.DUST, resource));
        Holder<Item> dirtyDust = Objects.requireNonNull(MekanismItems.getProcessedResource(ResourceType.DIRTY_DUST, resource));
        Holder<Item> clump = Objects.requireNonNull(MekanismItems.getProcessedResource(ResourceType.CLUMP, resource));
        Holder<Item> crystal = Objects.requireNonNull(MekanismItems.getProcessedResource(ResourceType.CRYSTAL, resource));
        Holder<Item> shard = Objects.requireNonNull(MekanismItems.getProcessedResource(ResourceType.SHARD, resource));
        TagKey<Item> dustTag = Objects.requireNonNull(MekanismTags.Items.getProcessedResource(ResourceType.DUST, resource));
        TagKey<Item> dirtyDustTag = Objects.requireNonNull(MekanismTags.Items.getProcessedResource(ResourceType.DIRTY_DUST, resource));
        TagKey<Item> clumpTag = Objects.requireNonNull(MekanismTags.Items.getProcessedResource(ResourceType.CLUMP, resource));
        TagKey<Item> shardTag = Objects.requireNonNull(MekanismTags.Items.getProcessedResource(ResourceType.SHARD, resource));
        TagKey<Item> crystalTag = Objects.requireNonNull(MekanismTags.Items.getProcessedResource(ResourceType.CRYSTAL, resource));

        CleanDirtySlurryId slurry = MekanismChemicals.PROCESSED_RESOURCES.get(resource);

        // Miscellaneous
        if (!resource.isVanilla()) {
            //From packing unpacking
            fromPackingUnpacking(output, basePath + "ingot/", block, nugget, nuggetTag, ingot);
            // to block
            ExtendedShapedRecipeBuilder.shaped(block)
                  .pattern(MekanismRecipeProvider.TYPED_STORAGE_PATTERN)
                  .define(Pattern.PREVIOUS, ingot)
                  .define(Pattern.CONSTANT, this.items, ingotTag)
                  .category(RecipeCategory.BUILDING_BLOCKS)
                  .unlockedBy(hasCriterion(ingot))
                  .save(output, basePath + "storage_blocks/from_ingots");
            // to nuggets
            ExtendedShapelessRecipeBuilder.shapeless(nugget, 9)
                  .requires(ingot)
                  .unlockedBy(hasCriterion(ingot))
                  .save(output, basePath + "nugget/from_ingot");
            Identifier ingotGroup = ingot.key().identifier();
            // from ore
            RecipeProviderUtil.addSmeltingBlastingRecipes(output, BaseRecipeProvider.ingredient(ore), ingot, dustExperience * 2, 200,
                  ingotGroup, basePath + "ingot/from_ore", hasCriterion(ore));
            RecipeProviderUtil.addSmeltingBlastingRecipes(output, BaseRecipeProvider.ingredient(deepslateOre), ingot, dustExperience * 2, 200,
                  ingotGroup, basePath + "ingot/from_deepslate_ore", hasCriterion(deepslateOre));
            // from raw
            RecipeProviderUtil.addSmeltingBlastingRecipes(output, BaseRecipeProvider.ingredient(raw), ingot, dustExperience * 2, 200,
                  ingotGroup, basePath + "ingot/from_raw", hasCriterion(raw));
            // raw from raw block
            ExtendedShapelessRecipeBuilder.shapeless(raw, 9)
                  .requires(rawBlock)
                  .unlockedBy(hasCriterion(rawBlock))
                  .save(output, basePath + "raw/from_raw_block");
            // raw to raw block
            ExtendedShapedRecipeBuilder.shaped(rawBlock)
                  .pattern(MekanismRecipeProvider.TYPED_STORAGE_PATTERN)
                  .define(Pattern.PREVIOUS, raw)
                  .define(Pattern.CONSTANT, this.items, rawTag)
                  .category(RecipeCategory.BUILDING_BLOCKS)
                  .unlockedBy(hasCriterion(raw))
                  .save(output, basePath + "raw_storage_blocks/from_raw");
        }

        ItemStackIngredient forOre = IngredientCreatorAccess.item().from(this.items, rawTag, toOre);
        // Ore from Dust
        CombinerRecipeBuilder.combining(
              forOre,
              IngredientCreatorAccess.item().from(this.items, Tags.BlockItems.COBBLESTONES_NORMAL),
              new ItemStackTemplate(ore)
        ).category(CombiningBookCategory.BUILDING)
              //Group with normal ore
              .group(ore.key())
              .unlockedBy("has_raw_material", has(rawTag))
              .save(output, basePath + "ore/from_raw");
        // Deepslate Ore from Dust
        CombinerRecipeBuilder.combining(
              forOre,
              IngredientCreatorAccess.item().from(this.items, Tags.BlockItems.COBBLESTONES_DEEPSLATE),
              new ItemStackTemplate(deepslateOre)
        ).category(CombiningBookCategory.BUILDING)
              //Group with normal ore
              .group(ore.key())
              .unlockedBy("has_raw_material", has(rawTag))
              .save(output, basePath + "ore/deepslate_from_raw");

        Identifier dustGroup = dust.key().identifier();

        //Dust from Ingot
        ItemStackToItemStackRecipeBuilder.crushing(
              IngredientCreatorAccess.item().from(this.items, ingotTag),
              new ItemStackTemplate(dust)
        ).group(dustGroup)
              .unlockedBy("has_ingot", has(ingotTag))
              .save(output, basePath + "dust/from_ingot");

        // Intermediate Steps
        // Ingot from Dust
        // Note: We make sure to group it with vanilla's ingot smelting recipes
        RecipeProviderUtil.addSmeltingBlastingRecipes(output, BaseRecipeProvider.ingredient(dust), ingot, dustExperience, 200,
              ingot.key().identifier(), basePath + "ingot/from_dust", new RecipeCriterion("has_dust", has(dust)));
        // Dust from Dirty Dust
        ItemStackToItemStackRecipeBuilder.enriching(
                    IngredientCreatorAccess.item().from(this.items, dirtyDustTag),
                    new ItemStackTemplate(dust)
              ).category(EnrichingBookCategory.EFFICIENCY)
              .group(dustGroup)
              .unlockedBy("has_dirty", has(dirtyDustTag))
              .save(output, basePath + "dust/from_dirty_dust");
        // Dirty Dust from Clump
        ItemStackToItemStackRecipeBuilder.crushing(
              IngredientCreatorAccess.item().from(this.items, clumpTag),
              new ItemStackTemplate(dirtyDust)
        ).category(CrushingBookCategory.EFFICIENCY)
              .group(dirtyDust.key())
              .unlockedBy("has_clump", has(clumpTag))
              .save(output, basePath + "dirty_dust/from_clump");
        // Clump from Shard
        ItemStackChemicalToItemStackRecipeBuilder.purifying(
              IngredientCreatorAccess.item().from(this.items, shardTag),
              IngredientCreatorAccess.chemicalStack().from(this.chemicals, ChemicalIds.OXYGEN, 1),
              true,
              new ItemStackTemplate(clump)
        ).group(clump.key())
              .unlockedBy("has_shard", has(shardTag))
              .save(output, basePath + "clump/from_shard");
        // Shard from Crystal
        ItemStackChemicalToItemStackRecipeBuilder.injecting(
              IngredientCreatorAccess.item().from(this.items, crystalTag),
              IngredientCreatorAccess.chemicalStack().from(this.chemicals, ChemicalIds.HYDROGEN_CHLORIDE, 1),
              true,
              new ItemStackTemplate(shard)
        ).category(InjectingBookCategory.EFFICIENCY)
              .group(shard.key())
              .unlockedBy("has_crystal", has(crystalTag))
              .save(output, basePath + "shard/from_crystal");
        // Crystal from Clean Slurry
        ChemicalCrystallizerRecipeBuilder.crystallizing(
              IngredientCreatorAccess.chemicalStack().from(chemicals, slurry.clean(), 200),
              new TagSlotDisplay(this.items.getOrThrow(oreTag)),
              new ItemStackTemplate(crystal)
        ).category(CrystallizingBookCategory.EFFICIENCY)
              .unlockedBy("has_ore", has(oreTag))
              .save(output, basePath + "crystal/from_slurry");
        // Clean Slurry from Dirty Slurry
        FluidChemicalToChemicalRecipeBuilder.washing(
              IngredientCreatorAccess.fluid().from(this.fluids, FluidTags.WATER, 5),
              IngredientCreatorAccess.chemicalStack().from(chemicals, slurry.dirty(), 1),
              chemicalTemplate(slurry.clean(), 1)
        ).unlockedBy("in_water", RecipeProvider.insideOf(Blocks.WATER))
              .save(output, basePath + "slurry/clean");

        // From ore
        // Dust
        ItemStackToItemStackRecipeBuilder.enriching(
                    IngredientCreatorAccess.item().from(this.items, oreTag),
                    new ItemStackTemplate(dust, 2)
              ).category(EnrichingBookCategory.EFFICIENCY)
              .group(dustGroup)
              .unlockedBy("has_ore", has(oreTag))
              .save(output, basePath + "dust/from_ore");
        // Clump
        ItemStackChemicalToItemStackRecipeBuilder.purifying(
              IngredientCreatorAccess.item().from(this.items, oreTag),
              IngredientCreatorAccess.chemicalStack().from(this.chemicals, ChemicalIds.OXYGEN, 1),
              true,
              new ItemStackTemplate(clump, 3)
        ).group(clump.key())
              .unlockedBy("has_ore", has(oreTag))
              .save(output, basePath + "clump/from_ore");
        // Shard
        ItemStackChemicalToItemStackRecipeBuilder.injecting(
              IngredientCreatorAccess.item().from(this.items, oreTag),
              IngredientCreatorAccess.chemicalStack().from(this.chemicals, ChemicalIds.HYDROGEN_CHLORIDE, 1),
              true,
              new ItemStackTemplate(shard, 4)
        ).category(InjectingBookCategory.EFFICIENCY)
              .group(shard.key())
              .unlockedBy("has_ore", has(oreTag))
              .save(output, basePath + "shard/from_ore");
        // Dirty Slurry
        ChemicalDissolutionRecipeBuilder.dissolution(
              IngredientCreatorAccess.item().from(this.items, oreTag),
              IngredientCreatorAccess.chemicalStack().from(this.chemicals, ChemicalIds.SULFURIC_ACID, 1),
              true,
              chemicalTemplate(slurry.dirty(), 1_000)
        ).category(DissolutionBookCategory.EFFICIENCY)
              .group(slurry.dirty())
              .unlockedBy("has_ore", has(oreTag))
              .save(output, basePath + "slurry/dirty/from_ore");

        // From raw ore
        // Dust
        ItemStackToItemStackRecipeBuilder.enriching(
                    IngredientCreatorAccess.item().from(this.items, rawTag, 3),
                    new ItemStackTemplate(dust, 4)
              ).category(EnrichingBookCategory.EFFICIENCY)
              .group(dustGroup)
              .unlockedBy("has_raw_material", has(rawTag))
              .save(output, basePath + "dust/from_raw_ore");
        // Clump
        ItemStackChemicalToItemStackRecipeBuilder.purifying(
              IngredientCreatorAccess.item().from(this.items, rawTag),
              IngredientCreatorAccess.chemicalStack().from(this.chemicals, ChemicalIds.OXYGEN, 1),
              true,
              new ItemStackTemplate(clump, 2)
        ).group(clump.key())
              .unlockedBy("has_raw_material", has(rawTag))
              .save(output, basePath + "clump/from_raw_ore");
        // Shard
        ItemStackChemicalToItemStackRecipeBuilder.injecting(
              IngredientCreatorAccess.item().from(this.items, rawTag, 3),
              IngredientCreatorAccess.chemicalStack().from(this.chemicals, ChemicalIds.HYDROGEN_CHLORIDE, 1),
              true,
              new ItemStackTemplate(shard, 8)
        ).category(InjectingBookCategory.EFFICIENCY)
              .group(shard.key())
              .unlockedBy("has_raw_material", has(rawTag))
              .save(output, basePath + "shard/from_raw_ore");
        // Dirty Slurry
        ChemicalDissolutionRecipeBuilder.dissolution(
              IngredientCreatorAccess.item().from(this.items, rawTag, 3),
              IngredientCreatorAccess.chemicalStack().from(this.chemicals, ChemicalIds.SULFURIC_ACID, 1),
              true,
              chemicalTemplate(slurry.dirty(), 2_000)
        ).category(DissolutionBookCategory.EFFICIENCY)
              .group(slurry.dirty())
              .unlockedBy("has_raw_material", has(rawTag))
              .save(output, basePath + "slurry/dirty/from_raw_ore");

        // From raw ore block
        // Dust
        ItemStackToItemStackRecipeBuilder.enriching(
                    IngredientCreatorAccess.item().from(this.items, rawBlockTag),
                    new ItemStackTemplate(dust, 12)
              ).category(EnrichingBookCategory.EFFICIENCY)
              .group(dustGroup)
              .unlockedBy("has_raw_material", has(rawTag))
              .unlockedBy("has_raw_block", has(rawBlockTag))
              .save(output, basePath + "dust/from_raw_block");
        // Clump
        ItemStackChemicalToItemStackRecipeBuilder.purifying(
              IngredientCreatorAccess.item().from(this.items, rawBlockTag),
              IngredientCreatorAccess.chemicalStack().from(this.chemicals, ChemicalIds.OXYGEN, 2),
              true,
              new ItemStackTemplate(clump, 18)
        ).group(clump.key())
              .unlockedBy("has_raw_material", has(rawTag))
              .unlockedBy("has_raw_block", has(rawBlockTag))
              .save(output, basePath + "clump/from_raw_block");
        // Shard
        ItemStackChemicalToItemStackRecipeBuilder.injecting(
              IngredientCreatorAccess.item().from(this.items, rawBlockTag),
              IngredientCreatorAccess.chemicalStack().from(this.chemicals, ChemicalIds.HYDROGEN_CHLORIDE, 2),
              true,
              new ItemStackTemplate(shard, 24)
        ).category(InjectingBookCategory.EFFICIENCY)
              .group(shard.key())
              .unlockedBy("has_raw_material", has(rawTag))
              .unlockedBy("has_raw_block", has(rawBlockTag))
              .save(output, basePath + "shard/from_raw_block");
        // Dirty Slurry
        ChemicalDissolutionRecipeBuilder.dissolution(
              IngredientCreatorAccess.item().from(this.items, rawBlockTag),
              IngredientCreatorAccess.chemicalStack().from(this.chemicals, ChemicalIds.SULFURIC_ACID, 2),
              true,
              chemicalTemplate(slurry.dirty(), 6_000)
        ).category(DissolutionBookCategory.EFFICIENCY)
              .group(slurry.dirty())
              .unlockedBy("has_raw_block", has(rawBlockTag))
              .save(output, basePath + "slurry/dirty/from_raw_block");
    }

    private void addCoalOreProcessingRecipes(RecipeOutput output, String basePath) {
        //from dust
        ItemStackToItemStackRecipeBuilder.enriching(
                    IngredientCreatorAccess.item().from(this.items, MekanismTags.Items.DUSTS_COAL),
                    template(ItemIds.COAL)
              ).category(EnrichingBookCategory.EFFICIENCY)
              .group(ItemIds.COAL)
              .unlockedBy("has_dust", has(MekanismTags.Items.DUSTS_COAL))
              .save(output, basePath + "from_dust");
        //from ore
        ItemStackToItemStackRecipeBuilder.enriching(
                    IngredientCreatorAccess.item().from(this.items, Tags.BlockItems.ORES_COAL),
                    template(ItemIds.COAL, 2)
              ).category(EnrichingBookCategory.EFFICIENCY)
              .group(ItemIds.COAL)
              .unlockedBy("has_ore", has(Tags.BlockItems.ORES_COAL))
              .save(output, basePath + "from_ore");
        //to dust
        ItemStackToItemStackRecipeBuilder.crushing(
              IngredientCreatorAccess.item().from(this.items, ItemIds.COAL),
              MekanismItems.COAL_DUST.asTemplate()
        ).group(MekanismItems.COAL_DUST.key())
              .unlockedBy(hasCriterion(ItemIds.COAL))
              .save(output, basePath + "to_dust");
        ItemStackIngredient forOre = IngredientCreatorAccess.item().from(this.items, MekanismTags.Items.DUSTS_COAL, 8);
        //to ore
        CombinerRecipeBuilder.combining(
              forOre,
              IngredientCreatorAccess.item().from(this.items, Tags.BlockItems.COBBLESTONES_NORMAL),
              template(BlockItemIds.COAL_ORE)
        ).category(CombiningBookCategory.BUILDING)
              .group("coal_ore")
              .unlockedBy("has_coal", has(MekanismTags.Items.DUSTS_COAL))
              .save(output, basePath + "to_ore");
        //to deepslate ore
        CombinerRecipeBuilder.combining(
              forOre,
              IngredientCreatorAccess.item().from(this.items, Tags.BlockItems.COBBLESTONES_DEEPSLATE),
              template(BlockItemIds.DEEPSLATE_COAL_ORE)
        ).category(CombiningBookCategory.BUILDING)
              .group("coal_ore")
              .unlockedBy("has_coal", has(MekanismTags.Items.DUSTS_COAL))
              .save(output, basePath + "to_deepslate_ore");
    }

    private void addOreProcessingGemRecipes(RecipeOutput output, String basePath, BlockItemId ore, @Nullable BlockItemId deepslateOre, BlockItemTagId oreTag,
          Holder<Item> dust, TagKey<Item> dustTag, ResourceKey<Item> gem, TagKey<Item> gemTag, int fromOre, int toOre, BlockItemTagId combineType) {
        addOreProcessingGemRecipes(output, basePath, items.getOrThrow(ore.item()), deepslateOre == null ? null : items.getOrThrow(deepslateOre.item()),
              oreTag, dust, dustTag, items.getOrThrow(gem), gemTag, fromOre, toOre, combineType);
    }

    private void addOreProcessingGemRecipes(RecipeOutput output, String basePath, Holder<Item> ore, @Nullable Holder<Item> deepslateOre, BlockItemTagId oreTag,
          Holder<Item> dust, TagKey<Item> dustTag, Holder<Item> gem, TagKey<Item> gemTag, int fromOre, int toOre, BlockItemTagId combineType) {
        addOreProcessingGemRecipes(output, basePath, ore, deepslateOre, oreTag, dust, dustTag, gem, gemTag, fromOre, toOre,
              IngredientCreatorAccess.item().from(this.items, combineType));
    }

    private void addOreProcessingGemRecipes(RecipeOutput output, String basePath, Holder<Item> ore, @Nullable Holder<Item> deepslateOre, BlockItemTagId oreTag,
          Holder<Item> dust, TagKey<Item> dustTag, Holder<Item> gem, TagKey<Item> gemTag, int fromOre, int toOre, ItemStackIngredient combineType) {
        //from dust
        ItemStackToItemStackRecipeBuilder.enriching(
                    IngredientCreatorAccess.item().from(this.items, dustTag),
                    new ItemStackTemplate(gem)
              ).unlockedBy("has_dust", has(dustTag))
              .save(output, basePath + "from_dust");
        //from ore
        ItemStackToItemStackRecipeBuilder.enriching(
                    IngredientCreatorAccess.item().from(this.items, oreTag),
                    new ItemStackTemplate(gem, fromOre)
              ).category(EnrichingBookCategory.EFFICIENCY)
              .unlockedBy("has_ore", has(oreTag))
              .save(output, basePath + "from_ore");
        //to dust
        ItemStackToItemStackRecipeBuilder.crushing(
              IngredientCreatorAccess.item().from(this.items, gemTag),
              new ItemStackTemplate(dust)
        ).group(dust.key())
              .unlockedBy("has_gem", has(gemTag))
              .save(output, basePath + "to_dust");
        ItemStackIngredient forOre = IngredientCreatorAccess.item().from(this.items, dustTag, toOre);
        //to ore
        CombinerRecipeBuilder.combining(
              forOre,
              combineType,
              new ItemStackTemplate(ore)
        ).category(CombiningBookCategory.BUILDING)
              .group(ore.key())
              .unlockedBy("has_dust", has(dustTag))
              .save(output, basePath + "to_ore");
        if (deepslateOre != null) {
            //to deepslate ore
            CombinerRecipeBuilder.combining(
                  forOre,
                  IngredientCreatorAccess.item().from(this.items, Tags.BlockItems.COBBLESTONES_DEEPSLATE),
                  new ItemStackTemplate(deepslateOre)
            ).category(CombiningBookCategory.BUILDING)
                  //Group with normal ore
                  .group(ore.key())
                  .unlockedBy("has_dust", has(dustTag))
                  .save(output, basePath + "to_deepslate_ore");
        }
    }

    private void addNetheriteProcessingRecipes(RecipeOutput output, String basePath) {
        //Ancient Debris to Dirty Netherite Scrap
        ItemStackToItemStackRecipeBuilder.crushing(
              IngredientCreatorAccess.item().from(this.items, Tags.BlockItems.ORES_NETHERITE_SCRAP),
              MekanismItems.DIRTY_NETHERITE_SCRAP.asTemplate(3)
        ).category(CrushingBookCategory.EFFICIENCY)
              .unlockedBy("has_ancient_debris", has(Tags.BlockItems.ORES_NETHERITE_SCRAP))
              .save(output, basePath + "ancient_debris_to_dirty_scrap");
        Identifier scrapGroup = ItemIds.NETHERITE_SCRAP.identifier();
        //Dirty Netherite Scrap to Netherite Scrap
        ItemStackToItemStackRecipeBuilder.enriching(
                    IngredientCreatorAccess.item().fromHolder(MekanismItems.DIRTY_NETHERITE_SCRAP),
                    template(ItemIds.NETHERITE_SCRAP)
              ).category(EnrichingBookCategory.EFFICIENCY)
              .group(scrapGroup)
              .unlockedBy("has_dirty_scrap", has(MekanismItems.DIRTY_NETHERITE_SCRAP))
              .save(output, basePath + "dirty_scrap_to_scrap");
        //Ancient Debris to Netherite Scrap
        ItemStackToItemStackRecipeBuilder.enriching(
                    IngredientCreatorAccess.item().from(this.items, Tags.BlockItems.ORES_NETHERITE_SCRAP),
                    template(ItemIds.NETHERITE_SCRAP, 2)
              ).category(EnrichingBookCategory.EFFICIENCY)
              .group(scrapGroup)
              .unlockedBy("has_ancient_debris", has(Tags.BlockItems.ORES_NETHERITE_SCRAP))
              .save(output, basePath + "ancient_debris_to_scrap");
        //Netherite scrap to netherite dust
        ItemStackChemicalToItemStackRecipeBuilder.metallurgicInfusing(
              IngredientCreatorAccess.item().from(this.items, ItemIds.NETHERITE_SCRAP, 4),
              IngredientCreatorAccess.chemicalStack().from(this.chemicals, MekanismAPITags.Chemicals.GOLD, 40),
              false,
              MekanismItems.NETHERITE_DUST.asTemplate()
        ).group(MekanismItems.NETHERITE_DUST.key())
              .unlockedBy(hasCriterion(ItemIds.NETHERITE_SCRAP))
              .save(output, basePath + "scrap_to_dust");
        //Netherite Dust to Netherite Ingot
        RecipeProviderUtil.addSmeltingBlastingRecipes(output, Ingredient.of(MekanismItems.NETHERITE_DUST), items.getOrThrow(ItemIds.NETHERITE_INGOT), 1, 200,
              null, basePath + "ingot_from_dust", new RecipeCriterion("has_dust", has(MekanismItems.NETHERITE_DUST)));
        //Netherite Ingot to Netherite Dust
        ItemStackToItemStackRecipeBuilder.crushing(
              IngredientCreatorAccess.item().from(this.items, Tags.Items.INGOTS_NETHERITE),
              MekanismItems.NETHERITE_DUST.asTemplate()
        ).group(MekanismItems.NETHERITE_DUST.key())
              .unlockedBy("has_netherite_ingot", has(Tags.Items.INGOTS_NETHERITE))
              .save(output, basePath + "ingot_to_dust");
        //Netherite Dust to Ancient Debris
        // Note: We only require two dust as that is equivalent to 8 scrap
        CombinerRecipeBuilder.combining(
              IngredientCreatorAccess.item().from(this.items, MekanismTags.Items.DUSTS_NETHERITE, 2),
              IngredientCreatorAccess.item().from(this.items, BlockItemIds.BASALT),
              template(BlockItemIds.ANCIENT_DEBRIS)
        ).category(CombiningBookCategory.BUILDING)
              .group(BlockItemIds.ANCIENT_DEBRIS.item())
              .unlockedBy("has_dust", has(MekanismTags.Items.DUSTS_NETHERITE))
              .save(output, basePath + "dust_to_ancient_debris");
    }

    private void addBronzeProcessingRecipes(RecipeOutput output, String basePath) {
        TagKey<Item> copperDust = MekanismTags.Items.getProcessedResource(ResourceType.DUST, PrimaryResource.COPPER);
        //Dust
        //from infusing
        ItemStackChemicalToItemStackRecipeBuilder.metallurgicInfusing(
              IngredientCreatorAccess.item().from(this.items, copperDust, 3),
              IngredientCreatorAccess.chemicalStack().from(this.chemicals, MekanismAPITags.Chemicals.TIN, 10),
              false,
              MekanismItems.BRONZE_DUST.asTemplate(4)
        ).group(MekanismItems.BRONZE_DUST.getId())
              .unlockedBy("has_copper_dust", has(copperDust))
              .save(output, basePath + "dust/from_infusing");
        //from ingot
        ItemStackToItemStackRecipeBuilder.crushing(
              IngredientCreatorAccess.item().from(this.items, MekanismTags.Items.INGOTS_BRONZE),
              MekanismItems.BRONZE_DUST.asTemplate()
        ).group(MekanismItems.BRONZE_DUST.getId())
              .unlockedBy("has_bronze_ingot", has(MekanismTags.Items.INGOTS_BRONZE))
              .save(output, basePath + "dust/from_ingot");
        //Ingot
        //From packing unpacking
        fromPackingUnpacking(output, basePath + "ingot/", MekanismBlocks.BRONZE_BLOCK, MekanismItems.BRONZE_NUGGET, MekanismTags.Items.NUGGETS_BRONZE,
              MekanismItems.BRONZE_INGOT);
        //from dust
        RecipeProviderUtil.addSmeltingBlastingRecipes(output, Ingredient.of(MekanismItems.BRONZE_DUST), MekanismItems.BRONZE_INGOT, 0.35F, 200,
              MekanismItems.BRONZE_INGOT.getId(), basePath + "ingot/from_dust", new RecipeCriterion("has_dust", has(MekanismItems.BRONZE_DUST)));
        //from infusing
        ItemStackChemicalToItemStackRecipeBuilder.metallurgicInfusing(
              IngredientCreatorAccess.item().from(this.items, Tags.Items.INGOTS_COPPER, 3),
              IngredientCreatorAccess.chemicalStack().from(this.chemicals, MekanismAPITags.Chemicals.TIN, 10),
              false,
              MekanismItems.BRONZE_INGOT.asTemplate(4)
        ).group(MekanismItems.BRONZE_INGOT.key())
              .unlockedBy("has_copper_ingot", has(Tags.Items.INGOTS_COPPER))
              .save(output, basePath + "ingot/from_infusing");
    }

    private void fromPackingUnpacking(RecipeOutput output, String basePath, BlockRegistryObject<?, ?> block, Holder<Item> nugget, TagKey<Item> nuggetTag, Holder<Item> result) {
        fromPackingUnpacking(output, basePath, block.getItemHolder(), nugget, nuggetTag, result);
    }

    private void fromPackingUnpacking(RecipeOutput output, String basePath, Holder<Item> block, Holder<Item> nugget, TagKey<Item> nuggetTag, Holder<Item> result) {
        //from block
        ExtendedShapelessRecipeBuilder.shapeless(result, 9)
              .requires(block)
              .group(result.key())
              .unlockedBy(hasCriterion(block))
              .save(output, basePath + "from_block");
        //from nuggets
        ExtendedShapedRecipeBuilder.shaped(result)
              .pattern(MekanismRecipeProvider.TYPED_STORAGE_PATTERN)
              .define(Pattern.PREVIOUS, nugget)
              .define(Pattern.CONSTANT, this.items, nuggetTag)
              .group(result.key())
              .unlockedBy(hasCriterion(nugget))
              .save(output, basePath + "from_nuggets");
    }

    private void addRedstoneProcessingRecipes(RecipeOutput output, String basePath) {
        //from ore
        ItemStackToItemStackRecipeBuilder.enriching(
                    IngredientCreatorAccess.item().from(this.items, Tags.BlockItems.ORES_REDSTONE),
                    template(BlockItemIds.REDSTONE_DUST, 12)
              ).category(EnrichingBookCategory.EFFICIENCY)
              .unlockedBy("has_redstone_ore", has(Tags.BlockItems.ORES_REDSTONE))
              .save(output, basePath + "from_ore");
        ItemStackIngredient forOre = IngredientCreatorAccess.item().from(this.items, Tags.Items.DUSTS_REDSTONE, 16);
        //to ore
        CombinerRecipeBuilder.combining(
              forOre,
              IngredientCreatorAccess.item().from(this.items, Tags.BlockItems.COBBLESTONES_NORMAL),
              template(BlockItemIds.REDSTONE_ORE)
        ).category(CombiningBookCategory.BUILDING)
              .group("redstone_ore")
              .unlockedBy("has_redstone", has(Tags.Items.DUSTS_REDSTONE))
              .save(output, basePath + "to_ore");
        //to deepslate ore
        CombinerRecipeBuilder.combining(
              forOre,
              IngredientCreatorAccess.item().from(this.items, Tags.BlockItems.COBBLESTONES_DEEPSLATE),
              template(BlockItemIds.DEEPSLATE_REDSTONE_ORE)
        ).category(CombiningBookCategory.BUILDING)
              .group("redstone_ore")
              .unlockedBy("has_redstone", has(Tags.Items.DUSTS_REDSTONE))
              .save(output, basePath + "to_deepslate_ore");
    }

    private void addRefinedGlowstoneProcessingRecipes(RecipeOutput output, String basePath) {
        //Ingot
        //From packing unpacking
        fromPackingUnpacking(output, basePath + "ingot/", MekanismBlocks.REFINED_GLOWSTONE_BLOCK, MekanismItems.REFINED_GLOWSTONE_NUGGET,
              MekanismTags.Items.NUGGETS_REFINED_GLOWSTONE, MekanismItems.REFINED_GLOWSTONE_INGOT);
        //from dust
        ItemStackChemicalToItemStackRecipeBuilder.compressing(
              IngredientCreatorAccess.item().from(this.items, Tags.Items.DUSTS_GLOWSTONE),
              IngredientCreatorAccess.chemicalStack().from(this.chemicals, ChemicalIds.OSMIUM, 1),
              true,
              MekanismItems.REFINED_GLOWSTONE_INGOT.asTemplate()
        ).unlockedBy("has_glowstone", has(Tags.Items.DUSTS_GLOWSTONE))
              .save(output, basePath + "ingot/from_dust");
        //Ingot -> dust
        ItemStackToItemStackRecipeBuilder.crushing(
              IngredientCreatorAccess.item().from(this.items, MekanismTags.Items.INGOTS_REFINED_GLOWSTONE),
              template(ItemIds.GLOWSTONE_DUST)
        ).unlockedBy("has_refined_glowstone_ingot", has(MekanismTags.Items.INGOTS_REFINED_GLOWSTONE))
              .save(output, basePath + "ingot_to_dust");
    }

    private void addRefinedObsidianProcessingRecipes(RecipeOutput output, String basePath) {
        //Dust
        //from ingot
        ItemStackToItemStackRecipeBuilder.crushing(
              IngredientCreatorAccess.item().from(this.items, MekanismTags.Items.INGOTS_REFINED_OBSIDIAN),
              MekanismItems.REFINED_OBSIDIAN_DUST.asTemplate()
        ).group(MekanismItems.REFINED_OBSIDIAN_DUST.getId())
              .unlockedBy("has_refined_obsidian_ingot", has(MekanismTags.Items.INGOTS_REFINED_OBSIDIAN))
              .save(output, basePath + "dust/from_ingot");
        //from obsidian dust
        ItemStackChemicalToItemStackRecipeBuilder.metallurgicInfusing(
              IngredientCreatorAccess.item().from(this.items, MekanismTags.Items.DUSTS_OBSIDIAN),
              IngredientCreatorAccess.chemicalStack().from(this.chemicals, MekanismAPITags.Chemicals.DIAMOND, 10),
              false,
              MekanismItems.REFINED_OBSIDIAN_DUST.asTemplate()
        ).group(MekanismItems.REFINED_OBSIDIAN_DUST.getId())
              .unlockedBy("has_refined_obsidian_dust", has(MekanismTags.Items.DUSTS_OBSIDIAN))
              .save(output, basePath + "dust/from_obsidian_dust");
        //Ingot
        //From packing unpacking
        fromPackingUnpacking(output, basePath + "ingot/", MekanismBlocks.REFINED_OBSIDIAN_BLOCK, MekanismItems.REFINED_OBSIDIAN_NUGGET,
              MekanismTags.Items.NUGGETS_REFINED_OBSIDIAN, MekanismItems.REFINED_OBSIDIAN_INGOT);
        //from dust
        ItemStackChemicalToItemStackRecipeBuilder.compressing(
              IngredientCreatorAccess.item().from(this.items, MekanismTags.Items.DUSTS_REFINED_OBSIDIAN),
              IngredientCreatorAccess.chemicalStack().from(this.chemicals, ChemicalIds.OSMIUM, 1),
              true,
              MekanismItems.REFINED_OBSIDIAN_INGOT.asTemplate()
        ).unlockedBy("has_refined_obsidian_dust", has(MekanismTags.Items.DUSTS_OBSIDIAN))
              .save(output, basePath + "ingot/from_dust");
    }

    private void addSteelProcessingRecipes(RecipeOutput output, String basePath) {
        //Ingot
        //From packing unpacking
        fromPackingUnpacking(output, basePath + "ingot/", MekanismBlocks.STEEL_BLOCK, MekanismItems.STEEL_NUGGET, MekanismTags.Items.NUGGETS_STEEL,
              MekanismItems.STEEL_INGOT);
        //from dust
        RecipeProviderUtil.addSmeltingBlastingRecipes(output, Ingredient.of(MekanismItems.STEEL_DUST), MekanismItems.STEEL_INGOT, 0.4F, 200,
              MekanismItems.STEEL_INGOT.key().identifier(), basePath + "ingot/from_dust", new RecipeCriterion("has_dust", has(MekanismItems.STEEL_DUST)));
        //Enriched iron -> dust
        ItemStackChemicalToItemStackRecipeBuilder.metallurgicInfusing(
              IngredientCreatorAccess.item().fromHolder(MekanismItems.ENRICHED_IRON),
              IngredientCreatorAccess.chemicalStack().from(this.chemicals, MekanismAPITags.Chemicals.CARBON, 10),
              false,
              MekanismItems.STEEL_DUST.asTemplate()
        ).group(MekanismItems.STEEL_DUST.getId())
              .unlockedBy(hasCriterion(MekanismItems.ENRICHED_IRON))
              .save(output, basePath + "enriched_iron_to_dust");
        //Ingot -> dust
        ItemStackToItemStackRecipeBuilder.crushing(
              IngredientCreatorAccess.item().from(this.items, MekanismTags.Items.INGOTS_STEEL),
              MekanismItems.STEEL_DUST.asTemplate()
        ).group(MekanismItems.STEEL_DUST.getId())
              .unlockedBy("has_steel_ingot", has(MekanismTags.Items.INGOTS_STEEL))
              .save(output, basePath + "ingot_to_dust");
    }

    private void addFluoriteRecipes(RecipeOutput output, String basePath) {
        OreBlockType fluorite = MekanismBlocks.ORES.get(OreType.FLUORITE);
        addOreProcessingGemRecipes(output, basePath, fluorite.stone().getItemHolder(), fluorite.deepslate().getItemHolder(), MekanismTags.BlockItems.ORES.get(OreType.FLUORITE),
              MekanismItems.FLUORITE_DUST, MekanismTags.Items.DUSTS_FLUORITE, MekanismItems.FLUORITE_GEM, MekanismTags.Items.GEMS_FLUORITE, 6, 14,
              Tags.BlockItems.COBBLESTONES_NORMAL);
        //Gem from block
        ExtendedShapelessRecipeBuilder.shapeless(MekanismItems.FLUORITE_GEM, 9)
              .requires(MekanismBlocks.FLUORITE_BLOCK)
              .unlockedBy(hasCriterion(MekanismItems.FLUORITE_GEM))
              .save(output, basePath + "from_block");
    }

    private void addUraniumRecipes(RecipeOutput output, String basePath) {
        TagKey<Item> uraniumIngot = MekanismTags.Items.getProcessedResource(ResourceType.INGOT, PrimaryResource.URANIUM);
        //yellow cake
        ItemStackToItemStackRecipeBuilder.enriching(
                    IngredientCreatorAccess.item().from(this.items, uraniumIngot),
                    MekanismItems.YELLOW_CAKE_URANIUM.asTemplate(2)
              ).unlockedBy("has_uranium_ingot", has(uraniumIngot))
              .save(output, basePath + "yellow_cake_uranium");
        //hydrofluoric acid
        ChemicalDissolutionRecipeBuilder.dissolution(
              IngredientCreatorAccess.item().from(this.items, MekanismTags.Items.GEMS_FLUORITE),
              IngredientCreatorAccess.chemicalStack().from(this.chemicals, ChemicalIds.SULFURIC_ACID, 1),
              true,
              chemicalTemplate(ChemicalIds.HYDROFLUORIC_ACID, 1_000)
        ).group(ChemicalIds.HYDROFLUORIC_ACID)
              .unlockedBy("has_fluorite", has(MekanismTags.Items.GEMS_FLUORITE))
              .save(output, basePath + "hydrofluoric_acid");
        ChemicalDissolutionRecipeBuilder.dissolution(
              IngredientCreatorAccess.item().from(this.items, MekanismTags.BlockItems.STORAGE_BLOCKS_FLUORITE),
              IngredientCreatorAccess.chemicalStack().from(this.chemicals, ChemicalIds.SULFURIC_ACID, 9),
              true,
              chemicalTemplate(ChemicalIds.HYDROFLUORIC_ACID, 9_000)
        ).group(ChemicalIds.HYDROFLUORIC_ACID)
              .unlockedBy("has_fluorite", has(MekanismTags.Items.GEMS_FLUORITE))
              .save(output, basePath + "hydrofluoric_acid_from_block");
        //uranium oxide
        ItemStackToChemicalRecipeBuilder.oxidizing(
              IngredientCreatorAccess.item().fromHolder(MekanismItems.YELLOW_CAKE_URANIUM),
              chemicalTemplate(ChemicalIds.URANIUM_OXIDE, 250)
        ).unlockedBy(hasCriterion(MekanismItems.YELLOW_CAKE_URANIUM))
              .save(output, basePath + "uranium_oxide");
        //uranium hexafluoride
        ChemicalChemicalToChemicalRecipeBuilder.chemicalInfusing(
              IngredientCreatorAccess.chemicalStack().from(this.chemicals, ChemicalIds.HYDROFLUORIC_ACID, 1),
              IngredientCreatorAccess.chemicalStack().from(this.chemicals, ChemicalIds.URANIUM_OXIDE, 1),
              chemicalTemplate(ChemicalIds.URANIUM_HEXAFLUORIDE, 2)
        ).unlockedBy("has_uranium_ingot", has(uraniumIngot))
              .save(output, basePath + "sulfuric_acid");
        //fissile fuel
        ChemicalToChemicalRecipeBuilder.centrifuging(
              IngredientCreatorAccess.chemicalStack().from(this.chemicals, ChemicalIds.URANIUM_HEXAFLUORIDE, 1),
              chemicalTemplate(ChemicalIds.FISSILE_FUEL, 1)
        ).unlockedBy("has_uranium_ingot", has(uraniumIngot))
              .save(output, basePath + "fissile_fuel");
        //fissile fuel reprocessing (IMPORTANT)
        ItemStackChemicalToItemStackRecipeBuilder.injecting(
              IngredientCreatorAccess.item().from(this.items, MekanismTags.Items.PELLETS_PLUTONIUM),
              IngredientCreatorAccess.chemicalStack().from(this.chemicals, ChemicalIds.HYDROGEN_CHLORIDE, 1),
              true,
              MekanismItems.REPROCESSED_FISSILE_FRAGMENT.asTemplate(4)
        ).unlockedBy("has_plutonium", has(MekanismTags.Items.PELLETS_PLUTONIUM))
              .save(output, basePath + "reprocessing/from_plutonium");
        //fragment -> fuel
        ItemStackToChemicalRecipeBuilder.oxidizing(
              IngredientCreatorAccess.item().fromHolder(MekanismItems.REPROCESSED_FISSILE_FRAGMENT),
              chemicalTemplate(ChemicalIds.FISSILE_FUEL, 2_000)
        ).unlockedBy(hasCriterion(MekanismItems.REPROCESSED_FISSILE_FRAGMENT))
              .save(output, basePath + "reprocessing/to_fuel");
    }
}