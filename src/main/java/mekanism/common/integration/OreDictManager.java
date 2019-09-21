package mekanism.common.integration;

import java.util.Arrays;
import java.util.List;
import javax.annotation.Nonnull;
import mekanism.api.infuse.InfuseRegistry;
import mekanism.api.infuse.InfusionStack;
import mekanism.api.recipes.inputs.FluidStackIngredient;
import mekanism.api.recipes.inputs.GasStackIngredient;
import mekanism.api.recipes.inputs.InfusionIngredient;
import mekanism.api.recipes.inputs.ItemStackIngredient;
import mekanism.common.MekanismGases;
import mekanism.common.MekanismInfuseTypes;
import mekanism.common.MekanismItem;
import mekanism.common.Resource;
import mekanism.common.config.MekanismConfig;
import mekanism.common.recipe.RecipeHandler;
import mekanism.common.tags.MekanismTags;
import mekanism.common.util.StackUtils;
import mekanism.common.world.DummyWorld;
import net.minecraft.block.Blocks;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.CraftingInventory;
import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.container.ContainerType;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.tags.FluidTags;
import net.minecraft.tags.ItemTags;
import net.minecraft.util.NonNullList;
import net.minecraftforge.common.Tags;

public final class OreDictManager {

    private static final List<String> minorCompatIngot = Arrays.asList("Aluminum", "Draconium", "Iridium", "Mithril", "Nickel", "Platinum", "Uranium");
    private static final List<String> minorCompatGem = Arrays.asList("Amber", "Diamond", "Emerald", "Malachite", "Peridot", "Ruby", "Sapphire", "Tanzanite", "Topaz");

    public static void init() {
        addLogRecipes();

        //TODO: Remove size checks?
        List<ItemStack> oreDict;

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

        RecipeHandler.addEnrichmentChamberRecipe(ItemStackIngredient.from("oreNetherSteel"), MekanismItem.STEEL_DUST.getItemStack(4));

        oreDict = OreDictionary.getOres("itemRubber", false);
        if (oreDict.size() > 0) {
            RecipeHandler.addPrecisionSawmillRecipe(ItemStackIngredient.from("woodRubber"), new ItemStack(Blocks.JUNGLE_PLANKS, 4),
                  StackUtils.size(oreDict.get(0), 1), 1F);
        }

        ItemStackIngredient sulfur = ItemStackIngredient.from(MekanismTags.DUSTS_SULFUR);
        RecipeHandler.addChemicalOxidizerRecipe(sulfur, MekanismGases.SULFUR_DIOXIDE, 100);
        RecipeHandler.addEnrichmentChamberRecipe(sulfur, new ItemStack(Items.GUNPOWDER));

        RecipeHandler.addChemicalOxidizerRecipe(ItemStackIngredient.from(MekanismTags.DUSTS_SALT), MekanismGases.BRINE, 15);

        ItemStackIngredient dustRefinedObsidian = ItemStackIngredient.from(MekanismTags.DUSTS_REFINED_OBSIDIAN);
        RecipeHandler.addOsmiumCompressorRecipe(dustRefinedObsidian, GasStackIngredient.from(MekanismGases.LIQUID_OSMIUM, 1), MekanismItem.REFINED_OBSIDIAN_INGOT.getItemStack());
        RecipeHandler.addEnrichmentChamberRecipe(dustRefinedObsidian, MekanismItem.ENRICHED_OBSIDIAN.getItemStack());

        addResourceRecipes(Resource.IRON, MekanismItem.IRON_SHARD, MekanismItem.IRON_CLUMP, MekanismItem.DIRTY_IRON_DUST, MekanismItem.IRON_DUST);
        addResourceRecipes(Resource.GOLD, MekanismItem.GOLD_SHARD, MekanismItem.GOLD_CLUMP, MekanismItem.DIRTY_GOLD_DUST, MekanismItem.GOLD_DUST);
        addResourceRecipes(Resource.OSMIUM, MekanismItem.OSMIUM_SHARD, MekanismItem.OSMIUM_CLUMP, MekanismItem.DIRTY_OSMIUM_DUST, MekanismItem.OSMIUM_DUST);
        addResourceRecipes(Resource.COPPER, MekanismItem.COPPER_SHARD, MekanismItem.COPPER_CLUMP, MekanismItem.DIRTY_COPPER_DUST, MekanismItem.COPPER_DUST);
        addResourceRecipes(Resource.TIN, MekanismItem.TIN_SHARD, MekanismItem.TIN_CLUMP, MekanismItem.DIRTY_TIN_DUST, MekanismItem.TIN_DUST);
        addResourceRecipes(Resource.SILVER, MekanismItem.SILVER_SHARD, MekanismItem.SILVER_CLUMP, MekanismItem.DIRTY_SILVER_DUST, MekanismItem.SILVER_DUST);
        addResourceRecipes(Resource.LEAD, MekanismItem.LEAD_SHARD, MekanismItem.LEAD_CLUMP, MekanismItem.DIRTY_LEAD_DUST, MekanismItem.LEAD_DUST);

        minorCompatIngot.forEach(OreDictManager::addStandardOredictMetal);
        minorCompatGem.forEach(OreDictManager::addStandardOredictGem);

        oreDict = OreDictionary.getOres("dustYellorium", false);
        if (oreDict.size() > 0) {
            RecipeHandler.addEnrichmentChamberRecipe(ItemStackIngredient.from("oreYellorite"), StackUtils.size(oreDict.get(0), 2));
        }

        oreDict = OreDictionary.getOres("dustNetherQuartz", false);
        if (oreDict.size() > 0) {
            RecipeHandler.addCrusherRecipe(ItemStackIngredient.from(Tags.Items.GEMS_QUARTZ), StackUtils.size(oreDict.get(0), 1));
        }

        oreDict = OreDictionary.getOres("dustQuartz", false);
        if (oreDict.size() > 0) {
            RecipeHandler.addCombinerRecipe(ItemStackIngredient.from("dustQuartz", 8), ItemStackIngredient.from(Tags.Items.NETHERRACK), new ItemStack(Blocks.NETHER_QUARTZ_ORE));
        } else {
            RecipeHandler.addCombinerRecipe(ItemStackIngredient.from(Tags.Items.GEMS_QUARTZ, 8), ItemStackIngredient.from(Tags.Items.NETHERRACK), new ItemStack(Blocks.NETHER_QUARTZ_ORE));
        }

        RecipeHandler.addEnrichmentChamberRecipe(ItemStackIngredient.from("dustNetherQuartz"), new ItemStack(Items.QUARTZ));
        RecipeHandler.addEnrichmentChamberRecipe(ItemStackIngredient.from(Tags.Items.ORES_QUARTZ), new ItemStack(Items.QUARTZ, 6));

        oreDict = OreDictionary.getOres("dustLapis", false);
        if (oreDict.size() > 0) {
            RecipeHandler.addCrusherRecipe(ItemStackIngredient.from(Tags.Items.GEMS_LAPIS), StackUtils.size(oreDict.get(0), 1));
        }

        oreDict = OreDictionary.getOres("oreLapis", false);
        if (oreDict.size() > 0) {
            ItemStack oreLapis = StackUtils.size(oreDict.get(0), 1);
            oreDict = OreDictionary.getOres("dustLapis", false);
            if (oreDict.size() > 0) {
                RecipeHandler.addCombinerRecipe(ItemStackIngredient.from("dustLapis", 16), ItemStackIngredient.from(Tags.Items.COBBLESTONE), oreLapis);
            } else {
                RecipeHandler.addCombinerRecipe(ItemStackIngredient.from(Tags.Items.GEMS_LAPIS, 16), ItemStackIngredient.from(Tags.Items.COBBLESTONE), oreLapis);
            }
        }

        RecipeHandler.addEnrichmentChamberRecipe(ItemStackIngredient.from("dustLapis"), new ItemStack(Items.LAPIS_LAZULI));
        RecipeHandler.addEnrichmentChamberRecipe(ItemStackIngredient.from(Tags.Items.ORES_LAPIS), new ItemStack(Items.LAPIS_LAZULI, 12));

        RecipeHandler.addCombinerRecipe(ItemStackIngredient.from(Tags.Items.DUSTS_REDSTONE, 16), ItemStackIngredient.from(Tags.Items.COBBLESTONE),
              new ItemStack(Blocks.REDSTONE_ORE));

        RecipeHandler.addEnrichmentChamberRecipe(ItemStackIngredient.from(Tags.Items.ORES_REDSTONE), new ItemStack(Items.REDSTONE, 12));

        RecipeHandler.addEnrichmentChamberRecipe(ItemStackIngredient.from(Tags.Items.ORES_COAL), new ItemStack(Items.COAL, 2));

        oreDict = OreDictionary.getOres("oreAmethyst", false);
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
        }

        RecipeHandler.addMetallurgicInfuserRecipe(InfusionIngredient.from(MekanismInfuseTypes.TIN, 10), ItemStackIngredient.from(MekanismTags.INGOTS_COPPER, 3),
              MekanismItem.BRONZE_INGOT.getItemStack(4));

        RecipeHandler.addCrusherRecipe(ItemStackIngredient.from(MekanismTags.INGOTS_REFINED_OBSIDIAN), MekanismItem.REFINED_OBSIDIAN_DUST.getItemStack());

        RecipeHandler.addMetallurgicInfuserRecipe(InfusionIngredient.from(MekanismInfuseTypes.REDSTONE, 10), ItemStackIngredient.from(MekanismTags.INGOTS_OSMIUM),
              MekanismItem.BASIC_CONTROL_CIRCUIT.getItemStack());

        RecipeHandler.addCrusherRecipe(ItemStackIngredient.from("ingotRedstone"), new ItemStack(Items.REDSTONE));
        RecipeHandler.addCrusherRecipe(ItemStackIngredient.from(MekanismTags.INGOTS_REFINED_GLOWSTONE), new ItemStack(Items.GLOWSTONE_DUST));

        oreDict = OreDictionary.getOres("dustBronze", false);
        if (oreDict.size() > 0) {
            RecipeHandler.addCrusherRecipe(ItemStackIngredient.from(MekanismTags.INGOTS_BRONZE), StackUtils.size(oreDict.get(0), 1));
        }

        //TODO: IC2
        /*if (Mekanism.hooks.IC2Loaded) {
            addIC2BronzeRecipe();
        }*/

        //TODO: Remove silver and lead??
        /*oreDict = OreDictionary.getOres("ingotSilver", false);
        if (oreDict.size() > 0) {
            FurnaceRecipes.instance().addSmeltingRecipe(MekanismItem.SILVER_DUST.getItemStack(), StackUtils.size(oreDict.get(0), 1), 0.0F);
        }

        oreDict = OreDictionary.getOres("ingotLead", false);
        if (oreDict.size() > 0) {
            FurnaceRecipes.instance().addSmeltingRecipe(MekanismItem.LEAD_DUST.getItemStack(), StackUtils.size(oreDict.get(0), 1), 0.0F);
        }*/

        oreDict = OreDictionary.getOres("dustCoal", false);
        if (oreDict.size() > 0) {
            RecipeHandler.addCrusherRecipe(ItemStackIngredient.from(Items.COAL), StackUtils.size(oreDict.get(0), 1));
            ItemStackIngredient dustCoal = ItemStackIngredient.from("dustCoal");
            RecipeHandler.addEnrichmentChamberRecipe(dustCoal, new ItemStack(Items.COAL));
            RecipeHandler.addPRCRecipe(dustCoal, FluidStackIngredient.from(FluidTags.WATER, 100), GasStackIngredient.from(MekanismTags.OXYGEN, 100),
                  MekanismItem.SULFUR_DUST.getItemStack(), MekanismGases.HYDROGEN, 100, 0, 100);
        }

        oreDict = OreDictionary.getOres("dustCharcoal", false);
        if (oreDict.size() > 0) {
            RecipeHandler.addCrusherRecipe(ItemStackIngredient.from(Items.CHARCOAL), StackUtils.size(oreDict.get(0), 1));
            ItemStackIngredient dustCharcoal = ItemStackIngredient.from("dustCharcoal");
            RecipeHandler.addEnrichmentChamberRecipe(dustCharcoal, new ItemStack(Items.CHARCOAL));
            RecipeHandler.addPRCRecipe(dustCharcoal, FluidStackIngredient.from(FluidTags.WATER, 100), GasStackIngredient.from(MekanismTags.OXYGEN, 100),
                  MekanismItem.SULFUR_DUST.getItemStack(), MekanismGases.HYDROGEN, 100, 0, 100);
        }

        oreDict = OreDictionary.getOres("dustSaltpeter", false);
        if (oreDict.size() > 0) {
            RecipeHandler.addCrusherRecipe(ItemStackIngredient.from(Tags.Items.GUNPOWDER), StackUtils.size(oreDict.get(0), 1));
            RecipeHandler.addEnrichmentChamberRecipe(ItemStackIngredient.from("dustSaltpeter"), new ItemStack(Items.GUNPOWDER));
        }

        oreDict = OreDictionary.getOres("itemSilicon", false);
        if (oreDict.size() > 0) {
            RecipeHandler.addCrusherRecipe(ItemStackIngredient.from(Tags.Items.SAND), StackUtils.size(oreDict.get(0), 1));
        }

        RecipeHandler.addCrusherRecipe(ItemStackIngredient.from(MekanismTags.INGOTS_STEEL), MekanismItem.STEEL_DUST.getItemStack());
        RecipeHandler.addChemicalOxidizerRecipe(ItemStackIngredient.from(MekanismTags.DUSTS_LITHIUM), MekanismGases.LITHIUM, 100);

        RecipeHandler.addMetallurgicInfuserRecipe(InfusionIngredient.from(MekanismInfuseTypes.DIAMOND, 10), ItemStackIngredient.from(MekanismTags.DUSTS_OBSIDIAN),
              MekanismItem.REFINED_OBSIDIAN_DUST.getItemStack());
        RecipeHandler.addCombinerRecipe(ItemStackIngredient.from(MekanismTags.DUSTS_OBSIDIAN, 4), ItemStackIngredient.from(Tags.Items.COBBLESTONE), new ItemStack(Blocks.OBSIDIAN));

        InfuseRegistry.registerInfuseObject(ItemStackIngredient.from(MekanismTags.DUSTS_DIAMOND), new InfusionStack(MekanismInfuseTypes.DIAMOND, 10));

        InfuseRegistry.registerInfuseObject(ItemStackIngredient.from(MekanismTags.DUSTS_TIN), new InfusionStack(MekanismInfuseTypes.TIN, 10));

        RecipeHandler.addPRCRecipe(ItemStackIngredient.from("blockCoal"), FluidStackIngredient.from(FluidTags.WATER, 1000),
              GasStackIngredient.from(MekanismTags.OXYGEN, 1000), MekanismItem.SULFUR_DUST.getItemStack(9), MekanismGases.HYDROGEN, 1000, 0, 900);
        RecipeHandler.addPRCRecipe(ItemStackIngredient.from("blockCharcoal"), FluidStackIngredient.from(FluidTags.WATER, 1000),
              GasStackIngredient.from(MekanismTags.OXYGEN, 1000), MekanismItem.SULFUR_DUST.getItemStack(9), MekanismGases.HYDROGEN, 1000, 0, 900);

        RecipeHandler.addPRCRecipe(ItemStackIngredient.from(MekanismTags.DUSTS_WOOD), FluidStackIngredient.from(FluidTags.WATER, 20),
              GasStackIngredient.from(MekanismTags.OXYGEN, 20), ItemStack.EMPTY, MekanismGases.HYDROGEN, 20, 0, 30);
        //TODO: 1.14 evaluate adding a charcoal dust item to Mekanism, and if so use that instead of charcoal here
        RecipeHandler.addEnrichmentChamberRecipe(ItemStackIngredient.from(MekanismTags.DUSTS_WOOD, 8), new ItemStack(Items.CHARCOAL));
    }

    //TODO: IC2
    /*@Method(modid = MekanismHooks.IC2_MOD_ID)
    private static void addIC2BronzeRecipe() {
        Recipes.macerator.addRecipe(Recipes.inputFactory.forOreDict("ingotBronze"), null, false,
              StackUtils.size(OreDictionary.getOres("dustBronze", false).get(0), 1));
    }*/

    private static void addResourceRecipes(Resource resource, MekanismItem shardItem, MekanismItem clumpItem, MekanismItem dirtyDustItem, MekanismItem dustItem) {
        RecipeHandler.addCrusherRecipe(ItemStackIngredient.from("clump" + resource.getRegistrySuffix()), dirtyDustItem.getItemStack());
        RecipeHandler.addPurificationChamberRecipe(ItemStackIngredient.from("shard" + resource.getRegistrySuffix()), clumpItem.getItemStack());
        RecipeHandler.addChemicalInjectionChamberRecipe(ItemStackIngredient.from("crystal" + resource.getRegistrySuffix()),
              GasStackIngredient.from(MekanismGases.HYDROGEN_CHLORIDE, 1), shardItem.getItemStack());
        RecipeHandler.addEnrichmentChamberRecipe(ItemStackIngredient.from("dustDirty" + resource.getRegistrySuffix()), dustItem.getItemStack());

        ItemStackIngredient oreIngredient = ItemStackIngredient.from("ore" + resource.getRegistrySuffix());
        RecipeHandler.addEnrichmentChamberRecipe(oreIngredient, dustItem.getItemStack(2));
        RecipeHandler.addPurificationChamberRecipe(oreIngredient, clumpItem.getItemStack(3));
        RecipeHandler.addChemicalInjectionChamberRecipe(oreIngredient, GasStackIngredient.from(MekanismGases.HYDROGEN_CHLORIDE, 1), shardItem.getItemStack(4));
        RecipeHandler.addChemicalDissolutionChamberRecipe(oreIngredient, GasStackIngredient.from(MekanismTags.SULFURIC_ACID, 1),
              GasRegistry.getGas(resource.getRegistrySuffix()), 1000);

        RecipeHandler.addCrusherRecipe(ItemStackIngredient.from("ingot" + resource.getRegistrySuffix()), dustItem.getItemStack());
        List<ItemStack> oreDict = OreDictionary.getOres("ore" + resource.getRegistrySuffix(), false);
        if (oreDict.size() > 0) {
            RecipeHandler.addCombinerRecipe(ItemStackIngredient.from("dust" + resource.getRegistrySuffix(), 8), ItemStackIngredient.from(Tags.Items.COBBLESTONE),
                  StackUtils.size(oreDict.get(0), 1));
        }
    }


    /**
     * Handy method for retrieving all log items, finding their corresponding planks, and making recipes with them. Credit to CofhCore.
     */
    private static void addLogRecipes() {
        Container tempContainer = new Container(ContainerType.CRAFTING, 0) {
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
        }
        RecipeHandler.addPRCRecipe(ItemStackIngredient.from(ItemTags.LOGS), FluidStackIngredient.from(FluidTags.WATER, 100),
              GasStackIngredient.from(MekanismTags.OXYGEN, 100), ItemStack.EMPTY, MekanismGases.HYDROGEN, 100, 0, 150);
    }

    private static void addSawmillLog(CraftingInventory tempCrafting, ItemStack log, DummyWorld world) {
        tempCrafting.setInventorySlotContents(0, log);
        IRecipe matchingRecipe = CraftingManager.findMatchingRecipe(tempCrafting, world);
        ItemStack resultEntry = matchingRecipe != null ? matchingRecipe.getRecipeOutput() : ItemStack.EMPTY;

        if (!resultEntry.isEmpty()) {
            RecipeHandler.addPrecisionSawmillRecipe(ItemStackIngredient.from(log), StackUtils.size(resultEntry, 6), MekanismItem.SAWDUST.getItemStack(),
                  MekanismConfig.general.sawdustChanceLog.get());
        }
    }

    public static void addStandardOredictMetal(String suffix) {
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
    }
}