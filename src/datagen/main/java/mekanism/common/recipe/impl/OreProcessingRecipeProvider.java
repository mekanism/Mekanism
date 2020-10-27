package mekanism.common.recipe.impl;

import java.util.function.Consumer;
import mekanism.api.datagen.recipe.builder.ChemicalCrystallizerRecipeBuilder;
import mekanism.api.datagen.recipe.builder.ChemicalDissolutionRecipeBuilder;
import mekanism.api.datagen.recipe.builder.ChemicalInfuserRecipeBuilder;
import mekanism.api.datagen.recipe.builder.CombinerRecipeBuilder;
import mekanism.api.datagen.recipe.builder.FluidSlurryToSlurryRecipeBuilder;
import mekanism.api.datagen.recipe.builder.GasToGasRecipeBuilder;
import mekanism.api.datagen.recipe.builder.ItemStackGasToItemStackRecipeBuilder;
import mekanism.api.datagen.recipe.builder.ItemStackToChemicalRecipeBuilder;
import mekanism.api.datagen.recipe.builder.ItemStackToItemStackRecipeBuilder;
import mekanism.api.datagen.recipe.builder.MetallurgicInfuserRecipeBuilder;
import mekanism.api.providers.IItemProvider;
import mekanism.api.recipes.inputs.FluidStackIngredient;
import mekanism.api.recipes.inputs.ItemStackIngredient;
import mekanism.api.recipes.inputs.chemical.GasStackIngredient;
import mekanism.api.recipes.inputs.chemical.InfusionStackIngredient;
import mekanism.api.recipes.inputs.chemical.SlurryStackIngredient;
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
import mekanism.common.resource.OreType;
import mekanism.common.resource.PrimaryResource;
import mekanism.common.resource.ResourceType;
import mekanism.common.tags.MekanismTags;
import mekanism.common.util.EnumUtils;
import net.minecraft.block.Blocks;
import net.minecraft.data.IFinishedRecipe;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.tags.FluidTags;
import net.minecraft.tags.ITag;
import net.minecraftforge.common.Tags;

class OreProcessingRecipeProvider implements ISubRecipeProvider {

    @Override
    public void addRecipes(Consumer<IFinishedRecipe> consumer) {
        String basePath = "processing/";
        for (PrimaryResource resource : EnumUtils.PRIMARY_RESOURCES) {
            addDynamicOreProcessingIngotRecipes(consumer, basePath + resource.getRegistrySuffix() + "/", resource);
        }
        //Gold Dust plus netherrack to nether gold ore
        CombinerRecipeBuilder.combining(
              ItemStackIngredient.from(MekanismItems.PROCESSED_RESOURCES.get(ResourceType.DUST, PrimaryResource.GOLD), 8),
              ItemStackIngredient.from(Tags.Items.NETHERRACK),
              new ItemStack(Blocks.NETHER_GOLD_ORE)
        ).build(consumer, Mekanism.rl(basePath + "gold/ore/nether_from_dust"));

        //Iron -> enriched iron
        MetallurgicInfuserRecipeBuilder.metallurgicInfusing(
              ItemStackIngredient.from(Tags.Items.INGOTS_IRON),
              InfusionStackIngredient.from(MekanismTags.InfuseTypes.CARBON, 10),
              MekanismItems.ENRICHED_IRON.getItemStack()
        ).build(consumer, Mekanism.rl(basePath + "iron/enriched"));
        addNetheriteProcessingRecipes(consumer, basePath + "netherite/");
        addBronzeProcessingRecipes(consumer, basePath + "bronze/");
        addCoalOreProcessingRecipes(consumer, basePath + "coal/");
        addOreProcessingGemRecipes(consumer, basePath + "diamond/", Blocks.DIAMOND_ORE, Tags.Items.ORES_DIAMOND, MekanismItems.DIAMOND_DUST,
              MekanismTags.Items.DUSTS_DIAMOND, Items.DIAMOND, Tags.Items.GEMS_DIAMOND, 2, 3, Tags.Items.COBBLESTONE);
        addOreProcessingGemRecipes(consumer, basePath + "emerald/", Blocks.EMERALD_ORE, Tags.Items.ORES_EMERALD, MekanismItems.EMERALD_DUST,
              MekanismTags.Items.DUSTS_EMERALD, Items.EMERALD, Tags.Items.GEMS_EMERALD, 2, 3, Tags.Items.COBBLESTONE);
        addOreProcessingGemRecipes(consumer, basePath + "lapis_lazuli/", Blocks.LAPIS_ORE, Tags.Items.ORES_LAPIS, MekanismItems.LAPIS_LAZULI_DUST,
              MekanismTags.Items.DUSTS_LAPIS, Items.LAPIS_LAZULI, Tags.Items.GEMS_LAPIS, 12, 16, Tags.Items.COBBLESTONE);
        addOreProcessingGemRecipes(consumer, basePath + "quartz/", Blocks.NETHER_QUARTZ_ORE, Tags.Items.ORES_QUARTZ, MekanismItems.QUARTZ_DUST,
              MekanismTags.Items.DUSTS_QUARTZ, Items.QUARTZ, Tags.Items.GEMS_QUARTZ, 6, 8, Tags.Items.NETHERRACK);
        addOreProcessingGemRecipes(consumer, basePath + "fluorite/", MekanismBlocks.ORES.get(OreType.FLUORITE), MekanismTags.Items.ORES.get(OreType.FLUORITE),
              MekanismItems.FLUORITE_DUST, MekanismTags.Items.DUSTS_FLUORITE, MekanismItems.FLUORITE_GEM, MekanismTags.Items.GEMS_FLUORITE, 6, 8,
              Tags.Items.COBBLESTONE);
        addRedstoneProcessingRecipes(consumer, basePath + "redstone/");
        addRefinedGlowstoneProcessingRecipes(consumer, basePath + "refined_glowstone/");
        addRefinedObsidianProcessingRecipes(consumer, basePath + "refined_obsidian/");
        addSteelProcessingRecipes(consumer, basePath + "steel/");

        addUraniumRecipes(consumer, basePath + "uranium/");
    }

    private void addDynamicOreProcessingIngotRecipes(Consumer<IFinishedRecipe> consumer, String basePath, PrimaryResource resource) {
        net.minecraft.util.IItemProvider ingot = MekanismItems.PROCESSED_RESOURCES.get(ResourceType.INGOT, resource);
        ITag<Item> ingotTag = MekanismTags.Items.PROCESSED_RESOURCES.get(ResourceType.INGOT, resource);
        net.minecraft.util.IItemProvider nugget = MekanismItems.PROCESSED_RESOURCES.get(ResourceType.NUGGET, resource);
        ITag<Item> nuggetTag = MekanismTags.Items.PROCESSED_RESOURCES.get(ResourceType.NUGGET, resource);
        net.minecraft.util.IItemProvider block = MekanismBlocks.PROCESSED_RESOURCE_BLOCKS.get(resource);
        ITag<Item> blockTag = MekanismTags.Items.PROCESSED_RESOURCE_BLOCKS.get(resource);
        net.minecraft.util.IItemProvider ore = MekanismBlocks.ORES.get(OreType.get(resource));
        float dustExperience = 0.3F;

        if (resource == PrimaryResource.IRON) {
            ingot = Items.IRON_INGOT;
            ingotTag = Tags.Items.INGOTS_IRON;
            nugget = Items.IRON_NUGGET;
            nuggetTag = Tags.Items.NUGGETS_IRON;
            block = Blocks.IRON_BLOCK;
            blockTag = Tags.Items.STORAGE_BLOCKS_IRON;
            ore = Blocks.IRON_ORE;
            dustExperience = 0.35F;
        } else if (resource == PrimaryResource.GOLD) {
            ingot = Items.GOLD_INGOT;
            ingotTag = Tags.Items.INGOTS_GOLD;
            nugget = Items.GOLD_NUGGET;
            nuggetTag = Tags.Items.NUGGETS_GOLD;
            block = Blocks.GOLD_BLOCK;
            blockTag = Tags.Items.STORAGE_BLOCKS_GOLD;
            ore = Blocks.GOLD_ORE;
            dustExperience = 0.5F;
        }

        IItemProvider dust = MekanismItems.PROCESSED_RESOURCES.get(ResourceType.DUST, resource);
        IItemProvider dirtyDust = MekanismItems.PROCESSED_RESOURCES.get(ResourceType.DIRTY_DUST, resource);
        IItemProvider clump = MekanismItems.PROCESSED_RESOURCES.get(ResourceType.CLUMP, resource);
        IItemProvider crystal = MekanismItems.PROCESSED_RESOURCES.get(ResourceType.CRYSTAL, resource);
        IItemProvider shard = MekanismItems.PROCESSED_RESOURCES.get(ResourceType.SHARD, resource);
        ITag<Item> dustTag = MekanismTags.Items.PROCESSED_RESOURCES.get(ResourceType.DUST, resource);
        ITag<Item> dirtyDustTag = MekanismTags.Items.PROCESSED_RESOURCES.get(ResourceType.DIRTY_DUST, resource);
        ITag<Item> clumpTag = MekanismTags.Items.PROCESSED_RESOURCES.get(ResourceType.CLUMP, resource);
        ITag<Item> shardTag = MekanismTags.Items.PROCESSED_RESOURCES.get(ResourceType.SHARD, resource);
        ITag<Item> crystalTag = MekanismTags.Items.PROCESSED_RESOURCES.get(ResourceType.CRYSTAL, resource);

        SlurryRegistryObject<?, ?> slurry = MekanismSlurries.PROCESSED_RESOURCES.get(resource);

        // Clump
        // from ore
        ItemStackGasToItemStackRecipeBuilder.purifying(ItemStackIngredient.from(resource.getOreTag()), GasStackIngredient.from(MekanismGases.OXYGEN, 1), clump.getItemStack(3))
              .build(consumer, Mekanism.rl(basePath + "clump/from_ore"));
        // from shard
        ItemStackGasToItemStackRecipeBuilder.purifying(ItemStackIngredient.from(shardTag), GasStackIngredient.from(MekanismGases.OXYGEN, 1), clump.getItemStack())
              .build(consumer, Mekanism.rl(basePath + "clump/from_shard"));
        // Crystal
        // from slurry
        ChemicalCrystallizerRecipeBuilder.crystallizing(SlurryStackIngredient.from(slurry.getCleanSlurry(), 200), crystal.getItemStack())
              .build(consumer, Mekanism.rl(basePath + "crystal/from_slurry"));
        // Dirty Dust
        // from clump
        ItemStackToItemStackRecipeBuilder.crushing(ItemStackIngredient.from(clumpTag), dirtyDust.getItemStack())
              .build(consumer, Mekanism.rl(basePath + "dirty_dust/from_clump"));
        // Dust
        // from dirty dust
        ItemStackToItemStackRecipeBuilder.enriching(ItemStackIngredient.from(dirtyDustTag), dust.getItemStack())
              .build(consumer, Mekanism.rl(basePath + "dust/from_dirty_dust"));
        // from ingot
        ItemStackToItemStackRecipeBuilder.crushing(ItemStackIngredient.from(ingotTag), dust.getItemStack())
              .build(consumer, Mekanism.rl(basePath + "dust/from_ingot"));
        // from ore
        ItemStackToItemStackRecipeBuilder.enriching(ItemStackIngredient.from(resource.getOreTag()), dust.getItemStack(2))
              .build(consumer, Mekanism.rl(basePath + "dust/from_ore"));
        // Ingot
        // from dust
        RecipeProviderUtil.addSmeltingBlastingRecipes(consumer, Ingredient.fromTag(dustTag), ingot, dustExperience, 200,
              Mekanism.rl(basePath + "ingot/from_dust_blasting"), Mekanism.rl(basePath + "ingot/from_dust_smelting"));
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
            RecipeProviderUtil.addSmeltingBlastingRecipes(consumer, Ingredient.fromTag(resource.getOreTag()), ingot, dustExperience * 2, 200,
                  Mekanism.rl(basePath + "ingot/from_ore_blasting"), Mekanism.rl(basePath + "ingot/from_ore_smelting"));
        }
        // Ore
        // from dust
        CombinerRecipeBuilder.combining(
              ItemStackIngredient.from(dustTag, 8),
              ItemStackIngredient.from(Tags.Items.COBBLESTONE),
              new ItemStack(ore)
        ).build(consumer, Mekanism.rl(basePath + "ore/from_dust"));
        // Shard
        // from crystal
        ItemStackGasToItemStackRecipeBuilder.injecting(
              ItemStackIngredient.from(crystalTag),
              GasStackIngredient.from(MekanismGases.HYDROGEN_CHLORIDE, 1),
              shard.getItemStack()
        ).build(consumer, Mekanism.rl(basePath + "shard/from_crystal"));
        // from ore
        ItemStackGasToItemStackRecipeBuilder.injecting(
              ItemStackIngredient.from(resource.getOreTag()),
              GasStackIngredient.from(MekanismGases.HYDROGEN_CHLORIDE, 1),
              shard.getItemStack(4)
        ).build(consumer, Mekanism.rl(basePath + "shard/from_ore"));
        // Slurry
        // clean
        FluidSlurryToSlurryRecipeBuilder.washing(
              FluidStackIngredient.from(FluidTags.WATER, 5),
              SlurryStackIngredient.from(slurry.getDirtySlurry(), 1),
              slurry.getCleanSlurry().getStack(1)
        ).build(consumer, Mekanism.rl(basePath + "slurry/clean"));
        // dirty
        ChemicalDissolutionRecipeBuilder.dissolution(
              ItemStackIngredient.from(resource.getOreTag()),
              GasStackIngredient.from(MekanismGases.SULFURIC_ACID, 1),
              slurry.getDirtySlurry().getStack(1_000)
        ).build(consumer, Mekanism.rl(basePath + "slurry/dirty"));
    }

    private void addCoalOreProcessingRecipes(Consumer<IFinishedRecipe> consumer, String basePath) {
        //from dust
        ItemStackToItemStackRecipeBuilder.enriching(
              ItemStackIngredient.from(MekanismTags.Items.DUSTS_COAL),
              new ItemStack(Items.COAL)
        ).build(consumer, Mekanism.rl(basePath + "from_dust"));
        //from ore
        ItemStackToItemStackRecipeBuilder.enriching(
              ItemStackIngredient.from(Tags.Items.ORES_COAL),
              new ItemStack(Items.COAL, 2)
        ).build(consumer, Mekanism.rl(basePath + "from_ore"));
        //to dust
        ItemStackToItemStackRecipeBuilder.crushing(
              ItemStackIngredient.from(Items.COAL),
              MekanismItems.COAL_DUST.getItemStack()
        ).build(consumer, Mekanism.rl(basePath + "to_dust"));
        //to ore
        CombinerRecipeBuilder.combining(
              ItemStackIngredient.from(MekanismTags.Items.DUSTS_COAL, 8),
              ItemStackIngredient.from(Tags.Items.COBBLESTONE),
              new ItemStack(Blocks.COAL_ORE)
        ).build(consumer, Mekanism.rl(basePath + "to_ore"));
    }

    private void addOreProcessingGemRecipes(Consumer<IFinishedRecipe> consumer, String basePath, net.minecraft.util.IItemProvider ore, ITag<Item> oreTag,
          IItemProvider dust, ITag<Item> dustTag, net.minecraft.util.IItemProvider gem, ITag<Item> gemTag, int fromOre, int toOre, ITag<Item> combineType) {
        //from dust
        ItemStackToItemStackRecipeBuilder.enriching(
              ItemStackIngredient.from(dustTag),
              new ItemStack(gem)
        ).build(consumer, Mekanism.rl(basePath + "from_dust"));
        //from ore
        ItemStackToItemStackRecipeBuilder.enriching(
              ItemStackIngredient.from(oreTag),
              new ItemStack(gem, fromOre)
        ).build(consumer, Mekanism.rl(basePath + "from_ore"));
        //to dust
        ItemStackToItemStackRecipeBuilder.crushing(
              ItemStackIngredient.from(gemTag),
              dust.getItemStack()
        ).build(consumer, Mekanism.rl(basePath + "to_dust"));
        //to ore
        CombinerRecipeBuilder.combining(
              ItemStackIngredient.from(dustTag, toOre),
              ItemStackIngredient.from(combineType),
              new ItemStack(ore)
        ).build(consumer, Mekanism.rl(basePath + "to_ore"));
    }

    private void addNetheriteProcessingRecipes(Consumer<IFinishedRecipe> consumer, String basePath) {
        //Ancient Debris to Dirty Netherite Scrap
        ItemStackToItemStackRecipeBuilder.crushing(
              ItemStackIngredient.from(Tags.Items.ORES_NETHERITE_SCRAP),
              MekanismItems.DIRTY_NETHERITE_SCRAP.getItemStack(3)
        ).build(consumer, Mekanism.rl(basePath + "ancient_debris_to_dirty_scrap"));
        //Dirty Netherite Scrap to Netherite Scrap
        ItemStackToItemStackRecipeBuilder.enriching(
              ItemStackIngredient.from(MekanismItems.DIRTY_NETHERITE_SCRAP),
              new ItemStack(Items.NETHERITE_SCRAP)
        ).build(consumer, Mekanism.rl(basePath + "dirty_scrap_to_scrap"));
        //Ancient Debris to Netherite Scrap
        ItemStackToItemStackRecipeBuilder.enriching(
              ItemStackIngredient.from(Tags.Items.ORES_NETHERITE_SCRAP),
              new ItemStack(Items.NETHERITE_SCRAP, 2)
        ).build(consumer, Mekanism.rl(basePath + "ancient_debris_to_scrap"));
        //Netherite scrap to netherite dust
        MetallurgicInfuserRecipeBuilder.metallurgicInfusing(
              ItemStackIngredient.from(Items.NETHERITE_SCRAP, 4),
              InfusionStackIngredient.from(MekanismTags.InfuseTypes.GOLD, 40),
              MekanismItems.NETHERITE_DUST.getItemStack()
        ).build(consumer, Mekanism.rl(basePath + "scrap_to_dust"));
        //Netherite Dust to Netherite Ingot
        RecipeProviderUtil.addSmeltingBlastingRecipes(consumer, Ingredient.fromTag(MekanismTags.Items.DUSTS_NETHERITE), Items.NETHERITE_INGOT, 1, 200,
              Mekanism.rl(basePath + "ingot_from_dust_blasting"), Mekanism.rl(basePath + "ingot_from_dust_smelting"));
        //Netherite Ingot to Netherite Dust
        ItemStackToItemStackRecipeBuilder.crushing(
              ItemStackIngredient.from(Tags.Items.INGOTS_NETHERITE),
              MekanismItems.NETHERITE_DUST.getItemStack()
        ).build(consumer, Mekanism.rl(basePath + "ingot_to_dust"));
        //Netherite Dust to Ancient Debris
        // Note: We only require two dust as that is equivalent to 8 scrap
        CombinerRecipeBuilder.combining(
              ItemStackIngredient.from(MekanismTags.Items.DUSTS_NETHERITE, 2),
              ItemStackIngredient.from(Blocks.BASALT),
              new ItemStack(Blocks.ANCIENT_DEBRIS)
        ).build(consumer, Mekanism.rl(basePath + "dust_to_ancient_debris"));
    }

    private void addBronzeProcessingRecipes(Consumer<IFinishedRecipe> consumer, String basePath) {
        //Dust
        //from infusing
        MetallurgicInfuserRecipeBuilder.metallurgicInfusing(
              ItemStackIngredient.from(MekanismTags.Items.PROCESSED_RESOURCES.get(ResourceType.DUST, PrimaryResource.COPPER), 3),
              InfusionStackIngredient.from(MekanismTags.InfuseTypes.TIN, 10),
              MekanismItems.BRONZE_DUST.getItemStack(4)
        ).build(consumer, Mekanism.rl(basePath + "dust/from_infusing"));
        //from ingot
        ItemStackToItemStackRecipeBuilder.crushing(
              ItemStackIngredient.from(MekanismTags.Items.INGOTS_BRONZE),
              MekanismItems.BRONZE_DUST.getItemStack()
        ).build(consumer, Mekanism.rl(basePath + "dust/from_ingot"));
        //Ingot
        //from block
        ExtendedShapelessRecipeBuilder.shapelessRecipe(MekanismItems.BRONZE_INGOT, 9)
              .addIngredient(MekanismTags.Items.STORAGE_BLOCKS_BRONZE)
              .build(consumer, Mekanism.rl(basePath + "ingot/from_block"));
        //from dust
        RecipeProviderUtil.addSmeltingBlastingRecipes(consumer, Ingredient.fromTag(MekanismTags.Items.DUSTS_BRONZE), MekanismItems.BRONZE_INGOT, 0.35F, 200,
              Mekanism.rl(basePath + "ingot/from_dust_blasting"), Mekanism.rl(basePath + "ingot/from_dust_smelting"));
        //from infusing
        MetallurgicInfuserRecipeBuilder.metallurgicInfusing(
              ItemStackIngredient.from(MekanismTags.Items.PROCESSED_RESOURCES.get(ResourceType.INGOT, PrimaryResource.COPPER), 3),
              InfusionStackIngredient.from(MekanismTags.InfuseTypes.TIN, 10),
              MekanismItems.BRONZE_INGOT.getItemStack(4)
        ).build(consumer, Mekanism.rl(basePath + "ingot/from_infusing"));
        //from nuggets
        ExtendedShapedRecipeBuilder.shapedRecipe(MekanismItems.BRONZE_INGOT)
              .pattern(MekanismRecipeProvider.STORAGE_PATTERN)
              .key(Pattern.CONSTANT, MekanismTags.Items.NUGGETS_BRONZE)
              .build(consumer, Mekanism.rl(basePath + "ingot/from_nuggets"));
    }

    private void addRedstoneProcessingRecipes(Consumer<IFinishedRecipe> consumer, String basePath) {
        //from ore
        ItemStackToItemStackRecipeBuilder.enriching(
              ItemStackIngredient.from(Tags.Items.ORES_REDSTONE),
              new ItemStack(Items.REDSTONE, 12)
        ).build(consumer, Mekanism.rl(basePath + "from_ore"));
        //to ore
        CombinerRecipeBuilder.combining(
              ItemStackIngredient.from(Tags.Items.DUSTS_REDSTONE, 16),
              ItemStackIngredient.from(Tags.Items.COBBLESTONE),
              new ItemStack(Blocks.REDSTONE_ORE)
        ).build(consumer, Mekanism.rl(basePath + "to_ore"));
    }

    private void addRefinedGlowstoneProcessingRecipes(Consumer<IFinishedRecipe> consumer, String basePath) {
        //Ingot
        //from block
        ExtendedShapelessRecipeBuilder.shapelessRecipe(MekanismItems.REFINED_GLOWSTONE_INGOT, 9)
              .addIngredient(MekanismTags.Items.STORAGE_BLOCKS_REFINED_GLOWSTONE)
              .build(consumer, Mekanism.rl(basePath + "ingot/from_block"));
        //from dust
        ItemStackGasToItemStackRecipeBuilder.compressing(
              ItemStackIngredient.from(Tags.Items.DUSTS_GLOWSTONE),
              GasStackIngredient.from(MekanismGases.LIQUID_OSMIUM, 1),
              MekanismItems.REFINED_GLOWSTONE_INGOT.getItemStack()
        ).build(consumer, Mekanism.rl(basePath + "ingot/from_dust"));
        //from nuggets
        ExtendedShapedRecipeBuilder.shapedRecipe(MekanismItems.REFINED_GLOWSTONE_INGOT)
              .pattern(MekanismRecipeProvider.STORAGE_PATTERN)
              .key(Pattern.CONSTANT, MekanismTags.Items.NUGGETS_REFINED_GLOWSTONE)
              .build(consumer, Mekanism.rl(basePath + "ingot/from_nuggets"));
        //Ingot -> dust
        ItemStackToItemStackRecipeBuilder.crushing(
              ItemStackIngredient.from(MekanismTags.Items.INGOTS_REFINED_GLOWSTONE),
              new ItemStack(Items.GLOWSTONE_DUST)
        ).build(consumer, Mekanism.rl(basePath + "ingot_to_dust"));
    }

    private void addRefinedObsidianProcessingRecipes(Consumer<IFinishedRecipe> consumer, String basePath) {
        //Dust
        //from ingot
        ItemStackToItemStackRecipeBuilder.crushing(
              ItemStackIngredient.from(MekanismTags.Items.INGOTS_REFINED_OBSIDIAN),
              MekanismItems.REFINED_OBSIDIAN_DUST.getItemStack()
        ).build(consumer, Mekanism.rl(basePath + "dust/from_ingot"));
        //from obsidian dust
        MetallurgicInfuserRecipeBuilder.metallurgicInfusing(
              ItemStackIngredient.from(MekanismTags.Items.DUSTS_OBSIDIAN),
              InfusionStackIngredient.from(MekanismTags.InfuseTypes.DIAMOND, 10),
              MekanismItems.REFINED_OBSIDIAN_DUST.getItemStack()
        ).build(consumer, Mekanism.rl(basePath + "dust/from_obsidian_dust"));
        //Ingot
        //from block
        ExtendedShapelessRecipeBuilder.shapelessRecipe(MekanismItems.REFINED_OBSIDIAN_INGOT, 9)
              .addIngredient(MekanismTags.Items.STORAGE_BLOCKS_REFINED_OBSIDIAN)
              .build(consumer, Mekanism.rl(basePath + "ingot/from_block"));
        //from dust
        ItemStackGasToItemStackRecipeBuilder.compressing(
              ItemStackIngredient.from(MekanismTags.Items.DUSTS_REFINED_OBSIDIAN),
              GasStackIngredient.from(MekanismGases.LIQUID_OSMIUM, 1),
              MekanismItems.REFINED_OBSIDIAN_INGOT.getItemStack()
        ).build(consumer, Mekanism.rl(basePath + "ingot/from_dust"));
        //from nuggets
        ExtendedShapedRecipeBuilder.shapedRecipe(MekanismItems.REFINED_OBSIDIAN_INGOT)
              .pattern(MekanismRecipeProvider.STORAGE_PATTERN)
              .key(Pattern.CONSTANT, MekanismTags.Items.NUGGETS_REFINED_OBSIDIAN)
              .build(consumer, Mekanism.rl(basePath + "ingot/from_nuggets"));
    }

    private void addSteelProcessingRecipes(Consumer<IFinishedRecipe> consumer, String basePath) {
        //Ingot
        //from block
        ExtendedShapelessRecipeBuilder.shapelessRecipe(MekanismItems.STEEL_INGOT, 9)
              .addIngredient(MekanismTags.Items.STORAGE_BLOCKS_STEEL)
              .build(consumer, Mekanism.rl(basePath + "ingot/from_block"));
        //from dust
        RecipeProviderUtil.addSmeltingBlastingRecipes(consumer, Ingredient.fromTag(MekanismTags.Items.DUSTS_STEEL), MekanismItems.STEEL_INGOT, 0.4F, 200,
              Mekanism.rl(basePath + "ingot/from_dust_blasting"), Mekanism.rl(basePath + "ingot/from_dust_smelting"));
        //from nuggets
        ExtendedShapedRecipeBuilder.shapedRecipe(MekanismItems.STEEL_INGOT)
              .pattern(MekanismRecipeProvider.STORAGE_PATTERN)
              .key(Pattern.CONSTANT, MekanismTags.Items.NUGGETS_STEEL)
              .build(consumer, Mekanism.rl(basePath + "ingot/from_nuggets"));
        //Enriched iron -> dust
        MetallurgicInfuserRecipeBuilder.metallurgicInfusing(
              ItemStackIngredient.from(MekanismItems.ENRICHED_IRON),
              InfusionStackIngredient.from(MekanismTags.InfuseTypes.CARBON, 10),
              MekanismItems.STEEL_DUST.getItemStack()
        ).build(consumer, Mekanism.rl(basePath + "enriched_iron_to_dust"));
        //Ingot -> dust
        ItemStackToItemStackRecipeBuilder.crushing(
              ItemStackIngredient.from(MekanismTags.Items.INGOTS_STEEL),
              MekanismItems.STEEL_DUST.getItemStack()
        ).build(consumer, Mekanism.rl(basePath + "ingot_to_dust"));
    }

    private void addUraniumRecipes(Consumer<IFinishedRecipe> consumer, String basePath) {
        //yellow cake
        ItemStackToItemStackRecipeBuilder.enriching(
              ItemStackIngredient.from(MekanismTags.Items.PROCESSED_RESOURCES.get(ResourceType.INGOT, PrimaryResource.URANIUM)),
              MekanismItems.YELLOW_CAKE_URANIUM.getItemStack(2)
        ).build(consumer, Mekanism.rl(basePath + "yellow_cake_uranium"));
        //hydrofluoric acid
        ChemicalDissolutionRecipeBuilder.dissolution(
              ItemStackIngredient.from(MekanismTags.Items.GEMS_FLUORITE),
              GasStackIngredient.from(MekanismGases.SULFURIC_ACID, 1),
              MekanismGases.HYDROFLUORIC_ACID.getStack(1_000)
        ).build(consumer, Mekanism.rl(basePath + "hydrofluoric_acid"));
        //uranium oxide
        ItemStackToChemicalRecipeBuilder.oxidizing(
              ItemStackIngredient.from(MekanismTags.Items.YELLOW_CAKE_URANIUM),
              MekanismGases.URANIUM_OXIDE.getStack(250)
        ).build(consumer, Mekanism.rl(basePath + "uranium_oxide"));
        //uranium hexafluoride
        ChemicalInfuserRecipeBuilder.chemicalInfusing(
              GasStackIngredient.from(MekanismGases.HYDROFLUORIC_ACID, 1),
              GasStackIngredient.from(MekanismGases.URANIUM_OXIDE, 1),
              MekanismGases.URANIUM_HEXAFLUORIDE.getStack(2)
        ).build(consumer, Mekanism.rl(basePath + "sulfuric_acid"));
        //fissile fuel
        GasToGasRecipeBuilder.centrifuging(
              GasStackIngredient.from(MekanismGases.URANIUM_HEXAFLUORIDE, 1),
              MekanismGases.FISSILE_FUEL.getStack(1)
        ).build(consumer, Mekanism.rl(basePath + "fissile_fuel"));
        //fissile fuel reprocessing (IMPORTANT)
        ItemStackGasToItemStackRecipeBuilder.injecting(
              ItemStackIngredient.from(MekanismTags.Items.PELLETS_PLUTONIUM),
              GasStackIngredient.from(MekanismGases.HYDROGEN_CHLORIDE, 1),
              MekanismItems.REPROCESSED_FISSILE_FRAGMENT.getItemStack(4)
        ).build(consumer, Mekanism.rl(basePath + "reprocessing/from_plutonium"));
        //fragment -> fuel
        ItemStackToChemicalRecipeBuilder.oxidizing(
              ItemStackIngredient.from(MekanismItems.REPROCESSED_FISSILE_FRAGMENT),
              MekanismGases.FISSILE_FUEL.getStack(2_000)
        ).build(consumer, Mekanism.rl(basePath + "reprocessing/to_fuel"));
    }
}