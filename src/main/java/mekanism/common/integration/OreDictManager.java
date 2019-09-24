package mekanism.common.integration;

import java.util.Arrays;
import java.util.List;
import mekanism.api.infuse.InfuseRegistry;
import mekanism.api.infuse.InfusionStack;
import mekanism.api.recipes.inputs.FluidStackIngredient;
import mekanism.api.recipes.inputs.GasStackIngredient;
import mekanism.api.recipes.inputs.InfusionIngredient;
import mekanism.api.recipes.inputs.ItemStackIngredient;
import mekanism.common.MekanismBlock;
import mekanism.common.MekanismGases;
import mekanism.common.MekanismInfuseTypes;
import mekanism.common.MekanismItem;
import mekanism.common.config.MekanismConfig;
import mekanism.common.recipe.RecipeHandler;
import mekanism.common.tags.MekanismTags;
import net.minecraft.block.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.tags.FluidTags;
import net.minecraft.tags.ItemTags;
import net.minecraftforge.common.Tags;

public final class OreDictManager {

    private static final List<String> minorCompatIngot = Arrays.asList("Aluminum", "Draconium", "Iridium", "Mithril", "Nickel", "Platinum", "Uranium");
    private static final List<String> minorCompatGem = Arrays.asList("Amber", "Malachite", "Peridot", "Ruby", "Sapphire", "Tanzanite", "Topaz");

    public static void init() {
        addLogRecipes();

        ItemStackIngredient plankWood = ItemStackIngredient.from(ItemTags.PLANKS);
        RecipeHandler.addPrecisionSawmillRecipe(plankWood, new ItemStack(Items.STICK, 6),
              MekanismItem.SAWDUST.getItemStack(), MekanismConfig.general.sawdustChancePlank.get());
        RecipeHandler.addPRCRecipe(plankWood, FluidStackIngredient.from(FluidTags.WATER, 20),
              GasStackIngredient.from(MekanismTags.OXYGEN, 20), ItemStack.EMPTY, MekanismGases.HYDROGEN, 20, 0, 30);

        ItemStackIngredient slabWood = ItemStackIngredient.from(ItemTags.WOODEN_SLABS);
        RecipeHandler.addPrecisionSawmillRecipe(slabWood, new ItemStack(Items.STICK, 3), MekanismItem.SAWDUST.getItemStack(),
              MekanismConfig.general.sawdustChancePlank.get() / 2);
        RecipeHandler.addPRCRecipe(slabWood, FluidStackIngredient.from(FluidTags.WATER, 10),
              GasStackIngredient.from(MekanismTags.OXYGEN, 10), ItemStack.EMPTY, MekanismGases.HYDROGEN, 10, 0, 15);

        ItemStackIngredient stickWood = ItemStackIngredient.from(Tags.Items.RODS_WOODEN);
        RecipeHandler.addPrecisionSawmillRecipe(stickWood, MekanismItem.SAWDUST.getItemStack());
        RecipeHandler.addPRCRecipe(stickWood, FluidStackIngredient.from(FluidTags.WATER, 4),
              GasStackIngredient.from(MekanismTags.OXYGEN, 4), ItemStack.EMPTY, MekanismGases.HYDROGEN, 4, 0, 6);

        //TODO: Re-enable
        /*RecipeHandler.addEnrichmentChamberRecipe(ItemStackIngredient.from("oreNetherSteel"), MekanismItem.STEEL_DUST.getItemStack(4));

        oreDict = OreDictionary.getOres("itemRubber", false);
        if (oreDict.size() > 0) {
            RecipeHandler.addPrecisionSawmillRecipe(ItemStackIngredient.from("woodRubber"), new ItemStack(Blocks.JUNGLE_PLANKS, 4),
                  StackUtils.size(oreDict.get(0), 1), 1F);
        }*/

        ItemStackIngredient sulfur = ItemStackIngredient.from(MekanismTags.DUSTS_SULFUR);
        RecipeHandler.addChemicalOxidizerRecipe(sulfur, MekanismGases.SULFUR_DIOXIDE, 100);
        RecipeHandler.addEnrichmentChamberRecipe(sulfur, new ItemStack(Items.GUNPOWDER));

        RecipeHandler.addChemicalOxidizerRecipe(ItemStackIngredient.from(MekanismTags.DUSTS_SALT), MekanismGases.BRINE, 15);

        ItemStackIngredient dustRefinedObsidian = ItemStackIngredient.from(MekanismTags.DUSTS_REFINED_OBSIDIAN);
        RecipeHandler.addOsmiumCompressorRecipe(dustRefinedObsidian, GasStackIngredient.from(MekanismGases.LIQUID_OSMIUM, 1), MekanismItem.REFINED_OBSIDIAN_INGOT.getItemStack());
        RecipeHandler.addEnrichmentChamberRecipe(dustRefinedObsidian, MekanismItem.ENRICHED_OBSIDIAN.getItemStack());

        //Iron
        RecipeHandler.addCrusherRecipe(ItemStackIngredient.from(MekanismTags.CLUMPS_IRON), MekanismItem.DIRTY_IRON_DUST.getItemStack());
        RecipeHandler.addPurificationChamberRecipe(ItemStackIngredient.from(MekanismTags.SHARDS_IRON), MekanismItem.IRON_CLUMP.getItemStack());
        RecipeHandler.addChemicalInjectionChamberRecipe(ItemStackIngredient.from(MekanismTags.CRYSTALS_IRON),
              GasStackIngredient.from(MekanismGases.HYDROGEN_CHLORIDE, 1), MekanismItem.IRON_SHARD.getItemStack());
        RecipeHandler.addEnrichmentChamberRecipe(ItemStackIngredient.from(MekanismTags.DIRTY_DUSTS_IRON), MekanismItem.IRON_DUST.getItemStack());
        ItemStackIngredient ironOreIngredient = ItemStackIngredient.from(Tags.Items.ORES_IRON);
        RecipeHandler.addEnrichmentChamberRecipe(ironOreIngredient, MekanismItem.IRON_DUST.getItemStack(2));
        RecipeHandler.addPurificationChamberRecipe(ironOreIngredient, MekanismItem.IRON_CLUMP.getItemStack(3));
        RecipeHandler.addChemicalInjectionChamberRecipe(ironOreIngredient, GasStackIngredient.from(MekanismGases.HYDROGEN_CHLORIDE, 1),
              MekanismItem.IRON_SHARD.getItemStack(4));
        RecipeHandler.addChemicalCrystallizerRecipe(GasStackIngredient.from(MekanismGases.CLEAN_IRON_SLURRY, 200), MekanismItem.IRON_CRYSTAL.getItemStack());
        RecipeHandler.addChemicalWasherRecipe(FluidStackIngredient.from(FluidTags.WATER, 5), GasStackIngredient.from(MekanismGases.DIRTY_IRON_SLURRY, 1),
              MekanismGases.CLEAN_IRON_SLURRY.getGasStack(1));
        RecipeHandler.addChemicalDissolutionChamberRecipe(ironOreIngredient, GasStackIngredient.from(MekanismTags.SULFURIC_ACID, 1),
              MekanismGases.DIRTY_IRON_SLURRY, 1000);
        RecipeHandler.addCrusherRecipe(ItemStackIngredient.from(Tags.Items.INGOTS_IRON), MekanismItem.IRON_DUST.getItemStack());
        RecipeHandler.addCombinerRecipe(ItemStackIngredient.from(MekanismTags.DUSTS_IRON, 8), ItemStackIngredient.from(Tags.Items.COBBLESTONE),
              new ItemStack(Blocks.IRON_ORE));
        //Gold
        RecipeHandler.addCrusherRecipe(ItemStackIngredient.from(MekanismTags.CLUMPS_GOLD), MekanismItem.DIRTY_GOLD_DUST.getItemStack());
        RecipeHandler.addPurificationChamberRecipe(ItemStackIngredient.from(MekanismTags.SHARDS_GOLD), MekanismItem.GOLD_CLUMP.getItemStack());
        RecipeHandler.addChemicalInjectionChamberRecipe(ItemStackIngredient.from(MekanismTags.CRYSTALS_GOLD),
              GasStackIngredient.from(MekanismGases.HYDROGEN_CHLORIDE, 1), MekanismItem.GOLD_SHARD.getItemStack());
        RecipeHandler.addEnrichmentChamberRecipe(ItemStackIngredient.from(MekanismTags.DIRTY_DUSTS_GOLD), MekanismItem.GOLD_DUST.getItemStack());
        ItemStackIngredient goldOreIngredient = ItemStackIngredient.from(Tags.Items.ORES_GOLD);
        RecipeHandler.addEnrichmentChamberRecipe(goldOreIngredient, MekanismItem.GOLD_DUST.getItemStack(2));
        RecipeHandler.addPurificationChamberRecipe(goldOreIngredient, MekanismItem.GOLD_CLUMP.getItemStack(3));
        RecipeHandler.addChemicalInjectionChamberRecipe(goldOreIngredient, GasStackIngredient.from(MekanismGases.HYDROGEN_CHLORIDE, 1),
              MekanismItem.GOLD_SHARD.getItemStack(4));
        RecipeHandler.addChemicalCrystallizerRecipe(GasStackIngredient.from(MekanismGases.CLEAN_GOLD_SLURRY, 200), MekanismItem.GOLD_CRYSTAL.getItemStack());
        RecipeHandler.addChemicalWasherRecipe(FluidStackIngredient.from(FluidTags.WATER, 5), GasStackIngredient.from(MekanismGases.DIRTY_GOLD_SLURRY, 1),
              MekanismGases.CLEAN_GOLD_SLURRY.getGasStack(1));
        RecipeHandler.addChemicalDissolutionChamberRecipe(goldOreIngredient, GasStackIngredient.from(MekanismTags.SULFURIC_ACID, 1),
              MekanismGases.DIRTY_GOLD_SLURRY, 1000);
        RecipeHandler.addCrusherRecipe(ItemStackIngredient.from(Tags.Items.INGOTS_GOLD), MekanismItem.GOLD_DUST.getItemStack());
        RecipeHandler.addCombinerRecipe(ItemStackIngredient.from(MekanismTags.DUSTS_GOLD, 8), ItemStackIngredient.from(Tags.Items.COBBLESTONE),
              new ItemStack(Blocks.GOLD_ORE));
        //Osmium
        RecipeHandler.addCrusherRecipe(ItemStackIngredient.from(MekanismTags.CLUMPS_OSMIUM), MekanismItem.DIRTY_OSMIUM_DUST.getItemStack());
        RecipeHandler.addPurificationChamberRecipe(ItemStackIngredient.from(MekanismTags.SHARDS_OSMIUM), MekanismItem.OSMIUM_CLUMP.getItemStack());
        RecipeHandler.addChemicalInjectionChamberRecipe(ItemStackIngredient.from(MekanismTags.CRYSTALS_OSMIUM),
              GasStackIngredient.from(MekanismGases.HYDROGEN_CHLORIDE, 1), MekanismItem.OSMIUM_SHARD.getItemStack());
        RecipeHandler.addEnrichmentChamberRecipe(ItemStackIngredient.from(MekanismTags.DIRTY_DUSTS_OSMIUM), MekanismItem.OSMIUM_DUST.getItemStack());
        ItemStackIngredient osmiumOreIngredient = ItemStackIngredient.from(MekanismTags.ORES_OSMIUM);
        RecipeHandler.addEnrichmentChamberRecipe(osmiumOreIngredient, MekanismItem.OSMIUM_DUST.getItemStack(2));
        RecipeHandler.addPurificationChamberRecipe(osmiumOreIngredient, MekanismItem.OSMIUM_CLUMP.getItemStack(3));
        RecipeHandler.addChemicalInjectionChamberRecipe(osmiumOreIngredient, GasStackIngredient.from(MekanismGases.HYDROGEN_CHLORIDE, 1),
              MekanismItem.OSMIUM_SHARD.getItemStack(4));
        RecipeHandler.addChemicalCrystallizerRecipe(GasStackIngredient.from(MekanismGases.CLEAN_OSMIUM_SLURRY, 200), MekanismItem.OSMIUM_CRYSTAL.getItemStack());
        RecipeHandler.addChemicalWasherRecipe(FluidStackIngredient.from(FluidTags.WATER, 5), GasStackIngredient.from(MekanismGases.DIRTY_OSMIUM_SLURRY, 1),
              MekanismGases.CLEAN_OSMIUM_SLURRY.getGasStack(1));
        RecipeHandler.addChemicalDissolutionChamberRecipe(osmiumOreIngredient, GasStackIngredient.from(MekanismTags.SULFURIC_ACID, 1),
              MekanismGases.DIRTY_OSMIUM_SLURRY, 1000);
        RecipeHandler.addCrusherRecipe(ItemStackIngredient.from(MekanismTags.INGOTS_OSMIUM), MekanismItem.OSMIUM_DUST.getItemStack());
        RecipeHandler.addCombinerRecipe(ItemStackIngredient.from(MekanismTags.DUSTS_OSMIUM, 8), ItemStackIngredient.from(Tags.Items.COBBLESTONE),
              MekanismBlock.OSMIUM_ORE.getItemStack());
        //Copper
        RecipeHandler.addCrusherRecipe(ItemStackIngredient.from(MekanismTags.CLUMPS_COPPER), MekanismItem.DIRTY_COPPER_DUST.getItemStack());
        RecipeHandler.addPurificationChamberRecipe(ItemStackIngredient.from(MekanismTags.SHARDS_COPPER), MekanismItem.COPPER_CLUMP.getItemStack());
        RecipeHandler.addChemicalInjectionChamberRecipe(ItemStackIngredient.from(MekanismTags.CRYSTALS_COPPER),
              GasStackIngredient.from(MekanismGases.HYDROGEN_CHLORIDE, 1), MekanismItem.COPPER_SHARD.getItemStack());
        RecipeHandler.addEnrichmentChamberRecipe(ItemStackIngredient.from(MekanismTags.DIRTY_DUSTS_COPPER), MekanismItem.COPPER_DUST.getItemStack());
        ItemStackIngredient copperOreIngredient = ItemStackIngredient.from(MekanismTags.ORES_COPPER);
        RecipeHandler.addEnrichmentChamberRecipe(copperOreIngredient, MekanismItem.COPPER_DUST.getItemStack(2));
        RecipeHandler.addPurificationChamberRecipe(copperOreIngredient, MekanismItem.COPPER_CLUMP.getItemStack(3));
        RecipeHandler.addChemicalInjectionChamberRecipe(copperOreIngredient, GasStackIngredient.from(MekanismGases.HYDROGEN_CHLORIDE, 1),
              MekanismItem.COPPER_SHARD.getItemStack(4));
        RecipeHandler.addChemicalCrystallizerRecipe(GasStackIngredient.from(MekanismGases.CLEAN_COPPER_SLURRY, 200), MekanismItem.COPPER_CRYSTAL.getItemStack());
        RecipeHandler.addChemicalWasherRecipe(FluidStackIngredient.from(FluidTags.WATER, 5), GasStackIngredient.from(MekanismGases.DIRTY_COPPER_SLURRY, 1),
              MekanismGases.CLEAN_COPPER_SLURRY.getGasStack(1));
        RecipeHandler.addChemicalDissolutionChamberRecipe(copperOreIngredient, GasStackIngredient.from(MekanismTags.SULFURIC_ACID, 1),
              MekanismGases.DIRTY_COPPER_SLURRY, 1000);
        RecipeHandler.addCrusherRecipe(ItemStackIngredient.from(MekanismTags.INGOTS_COPPER), MekanismItem.COPPER_DUST.getItemStack());
        RecipeHandler.addCombinerRecipe(ItemStackIngredient.from(MekanismTags.DUSTS_COPPER, 8), ItemStackIngredient.from(Tags.Items.COBBLESTONE),
              MekanismBlock.COPPER_ORE.getItemStack());
        //Tin
        RecipeHandler.addCrusherRecipe(ItemStackIngredient.from(MekanismTags.CLUMPS_TIN), MekanismItem.DIRTY_TIN_DUST.getItemStack());
        RecipeHandler.addPurificationChamberRecipe(ItemStackIngredient.from(MekanismTags.SHARDS_TIN), MekanismItem.TIN_CLUMP.getItemStack());
        RecipeHandler.addChemicalInjectionChamberRecipe(ItemStackIngredient.from(MekanismTags.CRYSTALS_TIN),
              GasStackIngredient.from(MekanismGases.HYDROGEN_CHLORIDE, 1), MekanismItem.TIN_SHARD.getItemStack());
        RecipeHandler.addEnrichmentChamberRecipe(ItemStackIngredient.from(MekanismTags.DIRTY_DUSTS_TIN), MekanismItem.TIN_DUST.getItemStack());
        ItemStackIngredient tinOreIngredient = ItemStackIngredient.from(MekanismTags.ORES_TIN);
        RecipeHandler.addEnrichmentChamberRecipe(tinOreIngredient, MekanismItem.TIN_DUST.getItemStack(2));
        RecipeHandler.addPurificationChamberRecipe(tinOreIngredient, MekanismItem.TIN_CLUMP.getItemStack(3));
        RecipeHandler.addChemicalInjectionChamberRecipe(tinOreIngredient, GasStackIngredient.from(MekanismGases.HYDROGEN_CHLORIDE, 1),
              MekanismItem.TIN_SHARD.getItemStack(4));
        RecipeHandler.addChemicalCrystallizerRecipe(GasStackIngredient.from(MekanismGases.CLEAN_TIN_SLURRY, 200), MekanismItem.TIN_CRYSTAL.getItemStack());
        RecipeHandler.addChemicalWasherRecipe(FluidStackIngredient.from(FluidTags.WATER, 5), GasStackIngredient.from(MekanismGases.DIRTY_TIN_SLURRY, 1),
              MekanismGases.CLEAN_TIN_SLURRY.getGasStack(1));
        RecipeHandler.addChemicalDissolutionChamberRecipe(tinOreIngredient, GasStackIngredient.from(MekanismTags.SULFURIC_ACID, 1),
              MekanismGases.DIRTY_TIN_SLURRY, 1000);
        RecipeHandler.addCrusherRecipe(ItemStackIngredient.from(MekanismTags.INGOTS_TIN), MekanismItem.TIN_DUST.getItemStack());
        RecipeHandler.addCombinerRecipe(ItemStackIngredient.from(MekanismTags.DUSTS_TIN, 8), ItemStackIngredient.from(Tags.Items.COBBLESTONE),
              MekanismBlock.TIN_ORE.getItemStack());

        RecipeHandler.addEnrichmentChamberRecipe(ItemStackIngredient.from(Tags.Items.ORES_DIAMOND), new ItemStack(Items.DIAMOND, 2));
        RecipeHandler.addEnrichmentChamberRecipe(ItemStackIngredient.from(MekanismTags.DUSTS_DIAMOND), new ItemStack(Items.DIAMOND));
        RecipeHandler.addCrusherRecipe(ItemStackIngredient.from(Tags.Items.GEMS_DIAMOND), MekanismItem.DIAMOND_DUST.getItemStack());
        RecipeHandler.addCombinerRecipe(ItemStackIngredient.from(MekanismTags.DUSTS_DIAMOND, 3), ItemStackIngredient.from(Tags.Items.COBBLESTONE), new ItemStack(Blocks.DIAMOND_ORE));

        RecipeHandler.addEnrichmentChamberRecipe(ItemStackIngredient.from(Tags.Items.ORES_EMERALD), new ItemStack(Items.EMERALD, 2));
        RecipeHandler.addEnrichmentChamberRecipe(ItemStackIngredient.from(MekanismTags.DUSTS_EMERALD), new ItemStack(Items.EMERALD));
        RecipeHandler.addCrusherRecipe(ItemStackIngredient.from(Tags.Items.GEMS_EMERALD), MekanismItem.EMERALD_DUST.getItemStack());
        RecipeHandler.addCombinerRecipe(ItemStackIngredient.from(MekanismTags.DUSTS_EMERALD, 3), ItemStackIngredient.from(Tags.Items.COBBLESTONE), new ItemStack(Blocks.EMERALD_ORE));

        //TODO: Re-enable
        /*minorCompatIngot.forEach(OreDictManager::addStandardOredictMetal);
        minorCompatGem.forEach(OreDictManager::addStandardOredictGem);

        oreDict = OreDictionary.getOres("dustYellorium", false);
        if (oreDict.size() > 0) {
            RecipeHandler.addEnrichmentChamberRecipe(ItemStackIngredient.from("oreYellorite"), StackUtils.size(oreDict.get(0), 2));
        }*/

        RecipeHandler.addCrusherRecipe(ItemStackIngredient.from(Tags.Items.GEMS_QUARTZ), MekanismItem.QUARTZ_DUST.getItemStack());
        RecipeHandler.addCombinerRecipe(ItemStackIngredient.from(MekanismTags.DUSTS_QUARTZ, 8), ItemStackIngredient.from(Tags.Items.NETHERRACK),
              new ItemStack(Blocks.NETHER_QUARTZ_ORE));
        RecipeHandler.addEnrichmentChamberRecipe(ItemStackIngredient.from(MekanismTags.DUSTS_QUARTZ), new ItemStack(Items.QUARTZ));
        RecipeHandler.addEnrichmentChamberRecipe(ItemStackIngredient.from(Tags.Items.ORES_QUARTZ), new ItemStack(Items.QUARTZ, 6));

        RecipeHandler.addCrusherRecipe(ItemStackIngredient.from(Tags.Items.GEMS_LAPIS), MekanismItem.LAPIS_LAZULI_DUST.getItemStack());
        RecipeHandler.addCombinerRecipe(ItemStackIngredient.from(MekanismTags.DUSTS_LAPIS_LAZULI, 16), ItemStackIngredient.from(Tags.Items.COBBLESTONE), new ItemStack(Blocks.LAPIS_ORE));

        RecipeHandler.addEnrichmentChamberRecipe(ItemStackIngredient.from(MekanismTags.DUSTS_LAPIS_LAZULI), new ItemStack(Items.LAPIS_LAZULI));
        RecipeHandler.addEnrichmentChamberRecipe(ItemStackIngredient.from(Tags.Items.ORES_LAPIS), new ItemStack(Items.LAPIS_LAZULI, 12));

        RecipeHandler.addCombinerRecipe(ItemStackIngredient.from(Tags.Items.DUSTS_REDSTONE, 16), ItemStackIngredient.from(Tags.Items.COBBLESTONE),
              new ItemStack(Blocks.REDSTONE_ORE));

        RecipeHandler.addEnrichmentChamberRecipe(ItemStackIngredient.from(Tags.Items.ORES_REDSTONE), new ItemStack(Items.REDSTONE, 12));

        RecipeHandler.addEnrichmentChamberRecipe(ItemStackIngredient.from(Tags.Items.ORES_COAL), new ItemStack(Items.COAL, 2));

        //TODO: Re-enable
        /*oreDict = OreDictionary.getOres("oreAmethyst", false);
        if (oreDict.size() > 0) {
            ItemStack oreAmethyst = StackUtils.size(oreDict.get(0), 1);
            oreDict = OreDictionary.getOres("dustAmethyst", false);
            if (oreDict.size() > 0) {
                RecipeHandler.addCombinerRecipe(ItemStackIngredient.from("dustAmethyst", 3), ItemStackIngredient.from("endstone"), oreAmethyst);
            } else {
                RecipeHandler.addCombinerRecipe(ItemStackIngredient.from("gemAmethyst", 3), ItemStackIngredient.from("endstone"), oreAmethyst);
            }
        }

        oreDict = OreDictionary.getOres("gemAmethyst", false);
        if (oreDict.size() > 0) {
            RecipeHandler.addEnrichmentChamberRecipe(ItemStackIngredient.from("oreAmethyst"), StackUtils.size(oreDict.get(0), 2));
        }

        oreDict = OreDictionary.getOres("oreApatite", false);
        if (oreDict.size() > 0) {
            ItemStack oreApatite = StackUtils.size(oreDict.get(0), 1);
            oreDict = OreDictionary.getOres("dustApatite", false);
            if (oreDict.size() > 0) {
                RecipeHandler.addCombinerRecipe(ItemStackIngredient.from("dustApatite", 6), ItemStackIngredient.from(Tags.Items.COBBLESTONE), oreApatite);
            } else {
                RecipeHandler.addCombinerRecipe(ItemStackIngredient.from("gemApatite", 6), ItemStackIngredient.from(Tags.Items.COBBLESTONE), oreApatite);
            }
        }

        oreDict = OreDictionary.getOres("gemApatite", false);
        if (oreDict.size() > 0) {
            RecipeHandler.addEnrichmentChamberRecipe(ItemStackIngredient.from("oreApatite"), StackUtils.size(oreDict.get(0), 4));
        }*/

        RecipeHandler.addMetallurgicInfuserRecipe(InfusionIngredient.from(MekanismInfuseTypes.TIN, 10), ItemStackIngredient.from(MekanismTags.INGOTS_COPPER, 3),
              MekanismItem.BRONZE_INGOT.getItemStack(4));

        RecipeHandler.addCrusherRecipe(ItemStackIngredient.from(MekanismTags.INGOTS_REFINED_OBSIDIAN), MekanismItem.REFINED_OBSIDIAN_DUST.getItemStack());

        RecipeHandler.addMetallurgicInfuserRecipe(InfusionIngredient.from(MekanismInfuseTypes.REDSTONE, 10), ItemStackIngredient.from(MekanismTags.INGOTS_OSMIUM),
              MekanismItem.BASIC_CONTROL_CIRCUIT.getItemStack());

        //TODO: Re-enable
        //RecipeHandler.addCrusherRecipe(ItemStackIngredient.from("ingotRedstone"), new ItemStack(Items.REDSTONE));
        RecipeHandler.addCrusherRecipe(ItemStackIngredient.from(MekanismTags.INGOTS_REFINED_GLOWSTONE), new ItemStack(Items.GLOWSTONE_DUST));
        RecipeHandler.addCrusherRecipe(ItemStackIngredient.from(MekanismTags.INGOTS_BRONZE), MekanismItem.BRONZE_DUST.getItemStack());

        //TODO: IC2
        /*if (Mekanism.hooks.IC2Loaded) {
            addIC2BronzeRecipe();
        }*/

        RecipeHandler.addCrusherRecipe(ItemStackIngredient.from(Items.COAL), MekanismItem.COAL_DUST.getItemStack());
        ItemStackIngredient dustCoal = ItemStackIngredient.from(MekanismTags.DUSTS_COAL);
        RecipeHandler.addEnrichmentChamberRecipe(dustCoal, new ItemStack(Items.COAL));
        RecipeHandler.addPRCRecipe(dustCoal, FluidStackIngredient.from(FluidTags.WATER, 100), GasStackIngredient.from(MekanismTags.OXYGEN, 100),
              MekanismItem.SULFUR_DUST.getItemStack(), MekanismGases.HYDROGEN, 100, 0, 100);

        RecipeHandler.addCrusherRecipe(ItemStackIngredient.from(Items.CHARCOAL), MekanismItem.CHARCOAL_DUST.getItemStack());
        ItemStackIngredient dustCharcoal = ItemStackIngredient.from(MekanismTags.DUSTS_CHARCOAL);
        RecipeHandler.addEnrichmentChamberRecipe(dustCharcoal, new ItemStack(Items.CHARCOAL));
        RecipeHandler.addPRCRecipe(dustCharcoal, FluidStackIngredient.from(FluidTags.WATER, 100), GasStackIngredient.from(MekanismTags.OXYGEN, 100),
              MekanismItem.SULFUR_DUST.getItemStack(), MekanismGases.HYDROGEN, 100, 0, 100);

        //TODO: Re-enable
        /*oreDict = OreDictionary.getOres("dustSaltpeter", false);
        if (oreDict.size() > 0) {
            RecipeHandler.addCrusherRecipe(ItemStackIngredient.from(Tags.Items.GUNPOWDER), StackUtils.size(oreDict.get(0), 1));
            RecipeHandler.addEnrichmentChamberRecipe(ItemStackIngredient.from("dustSaltpeter"), new ItemStack(Items.GUNPOWDER));
        }

        oreDict = OreDictionary.getOres("itemSilicon", false);
        if (oreDict.size() > 0) {
            RecipeHandler.addCrusherRecipe(ItemStackIngredient.from(Tags.Items.SAND), StackUtils.size(oreDict.get(0), 1));
        }*/

        RecipeHandler.addCrusherRecipe(ItemStackIngredient.from(MekanismTags.INGOTS_STEEL), MekanismItem.STEEL_DUST.getItemStack());
        RecipeHandler.addChemicalOxidizerRecipe(ItemStackIngredient.from(MekanismTags.DUSTS_LITHIUM), MekanismGases.LITHIUM, 100);

        RecipeHandler.addMetallurgicInfuserRecipe(InfusionIngredient.from(MekanismInfuseTypes.DIAMOND, 10), ItemStackIngredient.from(MekanismTags.DUSTS_OBSIDIAN),
              MekanismItem.REFINED_OBSIDIAN_DUST.getItemStack());
        RecipeHandler.addCombinerRecipe(ItemStackIngredient.from(MekanismTags.DUSTS_OBSIDIAN, 4), ItemStackIngredient.from(Tags.Items.COBBLESTONE), new ItemStack(Blocks.OBSIDIAN));

        InfuseRegistry.registerInfuseObject(ItemStackIngredient.from(MekanismTags.DUSTS_DIAMOND), new InfusionStack(MekanismInfuseTypes.DIAMOND, 10));

        InfuseRegistry.registerInfuseObject(ItemStackIngredient.from(MekanismTags.DUSTS_TIN), new InfusionStack(MekanismInfuseTypes.TIN, 10));

        RecipeHandler.addPRCRecipe(ItemStackIngredient.from(Tags.Items.STORAGE_BLOCKS_COAL), FluidStackIngredient.from(FluidTags.WATER, 1000),
              GasStackIngredient.from(MekanismTags.OXYGEN, 1000), MekanismItem.SULFUR_DUST.getItemStack(9), MekanismGases.HYDROGEN, 1000, 0, 900);
        RecipeHandler.addPRCRecipe(ItemStackIngredient.from(MekanismTags.STORAGE_BLOCKS_CHARCOAL), FluidStackIngredient.from(FluidTags.WATER, 1000),
              GasStackIngredient.from(MekanismTags.OXYGEN, 1000), MekanismItem.SULFUR_DUST.getItemStack(9), MekanismGases.HYDROGEN,
              1000, 0, 900);
        RecipeHandler.addPRCRecipe(ItemStackIngredient.from(MekanismTags.DUSTS_WOOD), FluidStackIngredient.from(FluidTags.WATER, 20),
              GasStackIngredient.from(MekanismTags.OXYGEN, 20), ItemStack.EMPTY, MekanismGases.HYDROGEN, 20, 0, 30);
        RecipeHandler.addEnrichmentChamberRecipe(ItemStackIngredient.from(MekanismTags.DUSTS_WOOD, 8), MekanismItem.CHARCOAL_DUST.getItemStack());
    }

    //TODO: IC2
    /*@Method(modid = MekanismHooks.IC2_MOD_ID)
    private static void addIC2BronzeRecipe() {
        Recipes.macerator.addRecipe(Recipes.inputFactory.forOreDict("ingotBronze"), null, false,
              StackUtils.size(OreDictionary.getOres("dustBronze", false).get(0), 1));
    }*/

    /**
     * Handy method for retrieving all log items, finding their corresponding planks, and making recipes with them. Credit to CofhCore.
     */
    private static void addLogRecipes() {
        //TODO: Re-enable
        /*Container tempContainer = new Container(ContainerType.CRAFTING, 0) {
            @Override
            public boolean canInteractWith(@Nonnull PlayerEntity player) {
                return false;
            }
        };
        DummyWorld dummyWorld = null;
        try {
            dummyWorld = new DummyWorld();
        } catch (Exception ignored) {
        }

        CraftingInventory tempCrafting = new CraftingInventory(tempContainer, 3, 3);

        for (int i = 1; i < 9; i++) {
            tempCrafting.setInventorySlotContents(i, ItemStack.EMPTY);
        }

        for (ItemStack logEntry : OreDictionary.getOres("logWood", false)) {
            addSawmillLog(tempCrafting, StackUtils.size(logEntry, 1), dummyWorld);
        }*/
        RecipeHandler.addPRCRecipe(ItemStackIngredient.from(ItemTags.LOGS), FluidStackIngredient.from(FluidTags.WATER, 100),
              GasStackIngredient.from(MekanismTags.OXYGEN, 100), ItemStack.EMPTY, MekanismGases.HYDROGEN, 100, 0, 150);
    }

    //TODO: Re-enable
    /*private static void addSawmillLog(CraftingInventory tempCrafting, ItemStack log, DummyWorld world) {
        tempCrafting.setInventorySlotContents(0, log);
        IRecipe matchingRecipe = CraftingManager.findMatchingRecipe(tempCrafting, world);
        ItemStack resultEntry = matchingRecipe != null ? matchingRecipe.getRecipeOutput() : ItemStack.EMPTY;

        if (!resultEntry.isEmpty()) {
            RecipeHandler.addPrecisionSawmillRecipe(ItemStackIngredient.from(log), StackUtils.size(resultEntry, 6), MekanismItem.SAWDUST.getItemStack(),
                  MekanismConfig.general.sawdustChanceLog.get());
        }
    }*/

    //TODO: Re-enable
    /*public static void addStandardOredictMetal(String suffix) {
        NonNullList<ItemStack> dusts = OreDictionary.getOres("dust" + suffix, false);
        NonNullList<ItemStack> ores = OreDictionary.getOres("ore" + suffix, false);
        if (dusts.size() > 0) {
            RecipeHandler.addEnrichmentChamberRecipe(ItemStackIngredient.from("ore" + suffix), StackUtils.size(dusts.get(0), 2));
            RecipeHandler.addCrusherRecipe(ItemStackIngredient.from("ingot" + suffix), StackUtils.size(dusts.get(0), 1));
        }

        if (ores.size() > 0) {
            RecipeHandler.addCombinerRecipe(ItemStackIngredient.from("dust" + suffix), ItemStackIngredient.from(Tags.Items.COBBLESTONE), StackUtils.size(ores.get(0), 1));
        }
    }

    public static void addStandardOredictGem(String suffix) {
        NonNullList<ItemStack> gems = OreDictionary.getOres("gem" + suffix, false);
        NonNullList<ItemStack> dusts = OreDictionary.getOres("dust" + suffix, false);
        NonNullList<ItemStack> ores = OreDictionary.getOres("ore" + suffix, false);
        if (gems.size() > 0) {
            RecipeHandler.addEnrichmentChamberRecipe(ItemStackIngredient.from("ore" + suffix), StackUtils.size(gems.get(0), 2));
        }

        if (dusts.size() > 0) {
            for (ItemStack gem : gems) {
                RecipeHandler.addEnrichmentChamberRecipe(ItemStackIngredient.from("dust" + suffix), StackUtils.size(gem, 1));
                RecipeHandler.addCrusherRecipe(ItemStackIngredient.from("gem" + suffix), StackUtils.size(dusts.get(0), 1));
            }
        }

        if (ores.size() > 0) {
            ItemStack ore = StackUtils.size(ores.get(0), 1);
            ItemStackIngredient base = ItemStackIngredient.from(Tags.Items.COBBLESTONE);
            if (dusts.size() > 0) {
                RecipeHandler.addCombinerRecipe(ItemStackIngredient.from("dust" + suffix, 3), base, ore);
            } else {
                RecipeHandler.addCombinerRecipe(ItemStackIngredient.from("gem" + suffix, 3), base, ore);
            }
        }
    }*/
}