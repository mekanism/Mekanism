package mekanism.common.integration;

import ic2.api.recipe.Recipes;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import javax.annotation.Nonnull;
import mekanism.api.gas.GasRegistry;
import mekanism.api.gas.GasStack;
import mekanism.api.infuse.InfuseObject;
import mekanism.api.infuse.InfuseRegistry;
import mekanism.api.infuse.InfuseType;
import mekanism.common.Mekanism;
import mekanism.common.MekanismFluids;
import mekanism.common.MekanismItems;
import mekanism.common.Resource;
import mekanism.common.config.MekanismConfig;
import mekanism.common.recipe.RecipeHandler;
import mekanism.common.recipe.RecipeHandler.Recipe;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.StackUtils;
import net.minecraft.block.BlockPlanks;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.FurnaceRecipes;
import net.minecraft.util.NonNullList;
import net.minecraftforge.fml.common.Optional.Method;
import net.minecraftforge.oredict.OreDictionary;

public final class OreDictManager {

    private static final List<String> minorCompatIngot = Arrays.asList("Aluminum", "Draconium", "Iridium", "Mithril", "Nickel", "Platinum", "Uranium");
    private static final List<String> minorCompatGem = Arrays.asList("Amber", "Diamond", "Emerald", "Malachite", "Peridot", "Ruby", "Sapphire", "Tanzanite", "Topaz");

    public static void init() {
        addLogRecipes();

        List<ItemStack> oreDict;

        for (ItemStack plankWood : OreDictionary.getOres("plankWood")) {
            ItemStack plank = StackUtils.size(plankWood, 1);
            if (!Recipe.PRECISION_SAWMILL.containsRecipe(plank)) {
                RecipeHandler.addPrecisionSawmillRecipe(plank, new ItemStack(Items.STICK, 6),
                      new ItemStack(MekanismItems.Sawdust), MekanismConfig.current().general.sawdustChancePlank.val());
            }
        }

        for (ItemStack stickWood : OreDictionary.getOres("stickWood")) {
            ItemStack stick = StackUtils.size(stickWood, 1);
            if (!Recipe.PRECISION_SAWMILL.containsRecipe(stick)) {
                RecipeHandler.addPrecisionSawmillRecipe(stick, new ItemStack(MekanismItems.Sawdust));
            }
        }

        for (ItemStack ore : OreDictionary.getOres("oreNetherSteel")) {
            RecipeHandler.addEnrichmentChamberRecipe(StackUtils.size(ore, 1), new ItemStack(MekanismItems.OtherDust, 4, 1));
        }

        oreDict = OreDictionary.getOres("itemRubber");
        if (oreDict.size() > 0) {
            ItemStack itemRubber = StackUtils.size(oreDict.get(0), 1);
            for (ItemStack rubber : OreDictionary.getOres("woodRubber")) {
                RecipeHandler.addPrecisionSawmillRecipe(StackUtils.size(rubber, 1), new ItemStack(Blocks.PLANKS, BlockPlanks.EnumType.JUNGLE.getMetadata(), 4),
                      itemRubber, 1F);
            }
        }

        for (ItemStack sulfur : OreDictionary.getOres("dustSulfur")) {
            RecipeHandler.addChemicalOxidizerRecipe(StackUtils.size(sulfur, 1), new GasStack(MekanismFluids.SulfurDioxide, 100));
        }

        for (ItemStack salt : OreDictionary.getOres("dustSalt")) {
            RecipeHandler.addChemicalOxidizerRecipe(StackUtils.size(salt, 1), new GasStack(MekanismFluids.Brine, 15));
        }

        for (ItemStack dust : OreDictionary.getOres("dustRefinedObsidian")) {
            RecipeHandler.addOsmiumCompressorRecipe(StackUtils.size(dust, 1), new ItemStack(MekanismItems.Ingot, 1, 0));
            RecipeHandler.addEnrichmentChamberRecipe(StackUtils.size(dust, 1), new ItemStack(MekanismItems.CompressedObsidian));
            InfuseRegistry.registerInfuseObject(StackUtils.size(dust, 1), new InfuseObject(InfuseRegistry.get("OBSIDIAN"), 10));
        }

        for (Resource resource : Resource.values()) {
            for (ItemStack clump : OreDictionary.getOres("clump" + resource.getName())) {
                RecipeHandler.addCrusherRecipe(StackUtils.size(clump, 1), new ItemStack(MekanismItems.DirtyDust, 1, resource.ordinal()));
            }

            for (ItemStack shard : OreDictionary.getOres("shard" + resource.getName())) {
                RecipeHandler.addPurificationChamberRecipe(StackUtils.size(shard, 1), new ItemStack(MekanismItems.Clump, 1, resource.ordinal()));
            }

            for (ItemStack crystal : OreDictionary.getOres("crystal" + resource.getName())) {
                RecipeHandler.addChemicalInjectionChamberRecipe(StackUtils.size(crystal, 1), MekanismFluids.HydrogenChloride,
                      new ItemStack(MekanismItems.Shard, 1, resource.ordinal()));
            }

            for (ItemStack dust : OreDictionary.getOres("dustDirty" + resource.getName())) {
                RecipeHandler.addEnrichmentChamberRecipe(StackUtils.size(dust, 1), new ItemStack(MekanismItems.Dust, 1, resource.ordinal()));
            }

            for (ItemStack ore : OreDictionary.getOres("ore" + resource.getName())) {
                RecipeHandler.addEnrichmentChamberRecipe(StackUtils.size(ore, 1), new ItemStack(MekanismItems.Dust, 2, resource.ordinal()));
                RecipeHandler.addPurificationChamberRecipe(StackUtils.size(ore, 1), new ItemStack(MekanismItems.Clump, 3, resource.ordinal()));
                RecipeHandler.addChemicalInjectionChamberRecipe(StackUtils.size(ore, 1), MekanismFluids.HydrogenChloride,
                      new ItemStack(MekanismItems.Shard, 4, resource.ordinal()));
                RecipeHandler.addChemicalDissolutionChamberRecipe(StackUtils.size(ore, 1),
                      new GasStack(GasRegistry.getGas(resource.getName().toLowerCase(Locale.ROOT)), 1000));
            }

            for (ItemStack ingot : OreDictionary.getOres("ingot" + resource.getName())) {
                RecipeHandler.addCrusherRecipe(StackUtils.size(ingot, 1), new ItemStack(MekanismItems.Dust, 1, resource.ordinal()));
            }

            oreDict = OreDictionary.getOres("ore" + resource.getName());
            if (oreDict.size() > 0) {
                ItemStack ore = StackUtils.size(oreDict.get(0), 1);
                for (ItemStack dust : OreDictionary.getOres("dust" + resource.getName())) {
                    RecipeHandler.addCombinerRecipe(StackUtils.size(dust, 8), new ItemStack(Blocks.COBBLESTONE), ore);
                }
            }
        }

        minorCompatIngot.forEach(OreDictManager::addStandardOredictMetal);
        minorCompatGem.forEach(OreDictManager::addStandardOredictGem);

        oreDict = OreDictionary.getOres("dustYellorium");
        if (oreDict.size() > 0) {
            ItemStack dustYellorium = StackUtils.size(oreDict.get(0), 2);
            for (ItemStack ore : OreDictionary.getOres("oreYellorite")) {
                RecipeHandler.addEnrichmentChamberRecipe(StackUtils.size(ore, 1), dustYellorium);
            }
        }

        oreDict = OreDictionary.getOres("dustNetherQuartz");
        if (oreDict.size() > 0) {
            ItemStack dustNeterQuartz = StackUtils.size(oreDict.get(0), 1);
            for (ItemStack gem : OreDictionary.getOres("gemQuartz")) {
                RecipeHandler.addCrusherRecipe(StackUtils.size(gem, 1), dustNeterQuartz);
            }
        }

        oreDict = OreDictionary.getOres("oreQuartz");
        if (oreDict.size() > 0) {
            ItemStack oreQuartz = StackUtils.size(oreDict.get(0), 1);
            oreDict = OreDictionary.getOres("dustQuartz");
            if (oreDict.size() > 0) {
                for (ItemStack dust : oreDict) {
                    RecipeHandler.addCombinerRecipe(StackUtils.size(dust, 8), new ItemStack(Blocks.COBBLESTONE), oreQuartz);
                }
            } else {
                for (ItemStack gem : OreDictionary.getOres("gemQuartz")) {
                    RecipeHandler.addCombinerRecipe(StackUtils.size(gem, 8), new ItemStack(Blocks.COBBLESTONE), oreQuartz);
                }
            }
        }

        oreDict = OreDictionary.getOres("gemQuartz");
        if (oreDict.size() > 0) {
            ItemStack gemQuartz = StackUtils.size(oreDict.get(0), 1);
            for (ItemStack dust : OreDictionary.getOres("dustNetherQuartz")) {
                RecipeHandler.addEnrichmentChamberRecipe(StackUtils.size(dust, 1), gemQuartz);
            }
            gemQuartz = StackUtils.size(gemQuartz, 6);
            for (ItemStack ore : OreDictionary.getOres("oreQuartz")) {
                RecipeHandler.addEnrichmentChamberRecipe(StackUtils.size(ore, 1), gemQuartz);
            }
        }

        oreDict = OreDictionary.getOres("dustLapis");
        if (oreDict.size() > 0) {
            ItemStack dustLapis = StackUtils.size(oreDict.get(0), 1);
            for (ItemStack gem : OreDictionary.getOres("gemLapis")) {
                RecipeHandler.addCrusherRecipe(StackUtils.size(gem, 1), dustLapis);
            }
        }

        oreDict = OreDictionary.getOres("oreLapis");
        if (oreDict.size() > 0) {
            ItemStack oreLapis = StackUtils.size(oreDict.get(0), 1);
            oreDict = OreDictionary.getOres("dustLapis");
            if (oreDict.size() > 0) {
                for (ItemStack dust : oreDict) {
                    RecipeHandler.addCombinerRecipe(StackUtils.size(dust, 16), new ItemStack(Blocks.COBBLESTONE), oreLapis);
                }
            } else {
                for (ItemStack gem : OreDictionary.getOres("gemLapis")) {
                    RecipeHandler.addCombinerRecipe(StackUtils.size(gem, 16), new ItemStack(Blocks.COBBLESTONE), oreLapis);
                }
            }
        }

        oreDict = OreDictionary.getOres("gemLapis");
        if (oreDict.size() > 0) {
            ItemStack gemLapis = StackUtils.size(oreDict.get(0), 1);
            for (ItemStack dust : OreDictionary.getOres("dustLapis")) {
                RecipeHandler.addEnrichmentChamberRecipe(StackUtils.size(dust, 1), gemLapis);
            }

            gemLapis = StackUtils.size(gemLapis, 12);
            for (ItemStack ore : OreDictionary.getOres("oreLapis")) {
                RecipeHandler.addEnrichmentChamberRecipe(StackUtils.size(ore, 1), gemLapis);
            }
        }

        oreDict = OreDictionary.getOres("oreRedstone");
        if (oreDict.size() > 0) {
            ItemStack oreRedstone = StackUtils.size(oreDict.get(0), 1);
            for (ItemStack dust : OreDictionary.getOres("dustRedstone")) {
                RecipeHandler.addCombinerRecipe(StackUtils.size(dust, 16), new ItemStack(Blocks.COBBLESTONE), oreRedstone);
            }
        }

        oreDict = OreDictionary.getOres("dustRedstone");
        if (oreDict.size() > 0) {
            ItemStack dustRedstone = StackUtils.size(oreDict.get(0), 12);
            for (ItemStack ore : OreDictionary.getOres("oreRedstone")) {
                RecipeHandler.addEnrichmentChamberRecipe(StackUtils.size(ore, 1), dustRedstone);
            }
        }

        for (ItemStack ore : OreDictionary.getOres("oreCoal")) {
            RecipeHandler.addEnrichmentChamberRecipe(StackUtils.size(ore, 1), new ItemStack(Items.COAL, 2));
        }

        oreDict = OreDictionary.getOres("oreAmethyst");
        if (oreDict.size() > 0) {
            ItemStack oreAmethyst = StackUtils.size(oreDict.get(0), 1);
            oreDict = OreDictionary.getOres("dustAmethyst");
            if (oreDict.size() > 0) {
                for (ItemStack dust : oreDict) {
                    RecipeHandler.addCombinerRecipe(StackUtils.size(dust, 3), new ItemStack(Blocks.END_STONE), oreAmethyst);
                }
            } else {
                for (ItemStack gem : OreDictionary.getOres("gemAmethyst")) {
                    RecipeHandler.addCombinerRecipe(StackUtils.size(gem, 3), new ItemStack(Blocks.END_STONE), oreAmethyst);
                }
            }
        }

        oreDict = OreDictionary.getOres("gemAmethyst");
        if (oreDict.size() > 0) {
            ItemStack gemAmethyst = StackUtils.size(oreDict.get(0), 2);
            for (ItemStack ore : OreDictionary.getOres("oreAmethyst")) {
                RecipeHandler.addEnrichmentChamberRecipe(StackUtils.size(ore, 1), gemAmethyst);
            }
        }

        oreDict = OreDictionary.getOres("oreApatite");
        if (oreDict.size() > 0) {
            ItemStack oreApatite = StackUtils.size(oreDict.get(0), 1);
            oreDict = OreDictionary.getOres("dustApatite");
            if (oreDict.size() > 0) {
                for (ItemStack dust : oreDict) {
                    RecipeHandler.addCombinerRecipe(StackUtils.size(dust, 6), new ItemStack(Blocks.COBBLESTONE), oreApatite);
                }
            } else {
                for (ItemStack gem : OreDictionary.getOres("gemApatite")) {
                    RecipeHandler.addCombinerRecipe(StackUtils.size(gem, 6), new ItemStack(Blocks.COBBLESTONE), oreApatite);
                }
            }
        }

        oreDict = OreDictionary.getOres("gemApatite");
        if (oreDict.size() > 0) {
            ItemStack gemApatite = StackUtils.size(oreDict.get(0), 4);
            for (ItemStack ore : OreDictionary.getOres("oreApatite")) {
                RecipeHandler.addEnrichmentChamberRecipe(StackUtils.size(ore, 1), gemApatite);
            }
        }

        InfuseType tinInfuseType = InfuseRegistry.get("TIN");
        for (ItemStack ingot : OreDictionary.getOres("ingotCopper")) {
            RecipeHandler.addMetallurgicInfuserRecipe(tinInfuseType, 10, StackUtils.size(ingot, 3),
                  new ItemStack(MekanismItems.Ingot, 4, 2));
        }

        for (ItemStack ingot : OreDictionary.getOres("ingotRefinedObsidian")) {
            RecipeHandler.addCrusherRecipe(StackUtils.size(ingot, 1), new ItemStack(MekanismItems.OtherDust, 1, 5));
        }

        InfuseType redstoneInfuseType = InfuseRegistry.get("REDSTONE");
        for (ItemStack ingot : OreDictionary.getOres("ingotOsmium")) {
            RecipeHandler.addMetallurgicInfuserRecipe(redstoneInfuseType, 10, StackUtils.size(ingot, 1),
                  new ItemStack(MekanismItems.ControlCircuit, 1, 0));
        }

        for (ItemStack ingot : OreDictionary.getOres("ingotRedstone")) {
            RecipeHandler.addCrusherRecipe(StackUtils.size(ingot, 1), new ItemStack(Items.REDSTONE));
        }

        for (ItemStack ingot : OreDictionary.getOres("ingotRefinedGlowstone")) {
            RecipeHandler.addCrusherRecipe(StackUtils.size(ingot, 1), new ItemStack(Items.GLOWSTONE_DUST));
        }

        for (ItemStack dust : OreDictionary.getOres("dustCoal")) {
            RecipeHandler.addEnrichmentChamberRecipe(dust, new ItemStack(Items.COAL));
        }

        for (ItemStack dust : OreDictionary.getOres("dustCharCoal")) {
            RecipeHandler.addEnrichmentChamberRecipe(dust, new ItemStack(Items.COAL, 1, 1));
        }

        oreDict = OreDictionary.getOres("dustBronze");
        if (oreDict.size() > 0) {
            ItemStack dustBronze = StackUtils.size(oreDict.get(0), 1);
            RecipeHandler.addCrusherRecipe(new ItemStack(MekanismItems.Ingot, 1, 2), dustBronze);
        }

        if (Mekanism.hooks.IC2Loaded) {
            addIC2BronzeRecipe();
        }

        oreDict = OreDictionary.getOres("ingotSilver");
        if (oreDict.size() > 0) {
            ItemStack ingotSilver = StackUtils.size(oreDict.get(0), 1);
            FurnaceRecipes.instance().addSmeltingRecipe(new ItemStack(MekanismItems.Dust, 1, Resource.SILVER.ordinal()), ingotSilver, 0.0F);
        }

        oreDict = OreDictionary.getOres("ingotLead");
        if (oreDict.size() > 0) {
            ItemStack ingotLead = StackUtils.size(oreDict.get(0), 1);
            FurnaceRecipes.instance().addSmeltingRecipe(new ItemStack(MekanismItems.Dust, 1, Resource.LEAD.ordinal()), ingotLead, 0.0F);
        }

        oreDict = OreDictionary.getOres("dustCoal");
        if (oreDict.size() > 0) {
            ItemStack dustCoal = StackUtils.size(oreDict.get(0), 1);
            RecipeHandler.addCrusherRecipe(new ItemStack(Items.COAL), dustCoal);
        }

        oreDict = OreDictionary.getOres("dustCharcoal");
        if (oreDict.size() > 0) {
            ItemStack dustCharcoal = StackUtils.size(oreDict.get(0), 1);
            RecipeHandler.addCrusherRecipe(new ItemStack(Items.COAL, 1, 1), dustCharcoal);
        }

        oreDict = OreDictionary.getOres("dustSaltpeter");
        if (oreDict.size() > 0) {
            ItemStack dustSaltpeter = StackUtils.size(oreDict.get(0), 1);
            RecipeHandler.addCrusherRecipe(new ItemStack(Items.GUNPOWDER), dustSaltpeter);
        }

        oreDict = OreDictionary.getOres("itemSilicon");
        if (oreDict.size() > 0) {
            ItemStack itemSilicon = StackUtils.size(oreDict.get(0), 1);
            for (ItemStack sand : OreDictionary.getOres("sand")) {
                RecipeHandler.addCrusherRecipe(StackUtils.size(sand, 1), itemSilicon);
            }
        }

        for (ItemStack dust : OreDictionary.getOres("dustSaltpeter")) {
            RecipeHandler.addEnrichmentChamberRecipe(StackUtils.size(dust, 1), new ItemStack(Items.GUNPOWDER));
        }

        for (ItemStack ingot : OreDictionary.getOres("ingotSteel")) {
            RecipeHandler.addCrusherRecipe(StackUtils.size(ingot, 1), new ItemStack(MekanismItems.OtherDust, 1, 1));
        }

        for (ItemStack dust : OreDictionary.getOres("dustLithium")) {
            RecipeHandler.addChemicalOxidizerRecipe(StackUtils.size(dust, 1), new GasStack(MekanismFluids.Lithium, 100));
        }

        InfuseType diamondInfuseType = InfuseRegistry.get("DIAMOND");
        for (ItemStack dust : OreDictionary.getOres("dustObsidian")) {
            RecipeHandler.addCombinerRecipe(StackUtils.size(dust, 4), new ItemStack(Blocks.COBBLESTONE), new ItemStack(Blocks.OBSIDIAN));
            RecipeHandler.addMetallurgicInfuserRecipe(diamondInfuseType, 10, StackUtils.size(dust, 1),
                  new ItemStack(MekanismItems.OtherDust, 1, 5));
        }

        for (ItemStack dust : OreDictionary.getOres("dustDiamond")) {
            InfuseRegistry.registerInfuseObject(dust, new InfuseObject(diamondInfuseType, 10));
        }

        for (ItemStack dust : OreDictionary.getOres("dustTin")) {
            InfuseRegistry.registerInfuseObject(dust, new InfuseObject(tinInfuseType, 10));
        }

        for (ItemStack ore : OreDictionary.getOres("treeSapling")) {
            if (ore.getItemDamage() == 0 || ore.getItemDamage() == OreDictionary.WILDCARD_VALUE) {
                RecipeHandler.addCrusherRecipe(new ItemStack(ore.getItem(), 1, OreDictionary.WILDCARD_VALUE), new ItemStack(MekanismItems.BioFuel, 2));
            }
        }
    }

    @Method(modid = MekanismHooks.IC2_MOD_ID)
    private static void addIC2BronzeRecipe() {
        Recipes.macerator.addRecipe(Recipes.inputFactory.forOreDict("ingotBronze"), null, false,
              StackUtils.size(OreDictionary.getOres("dustBronze").get(0), 1));
    }


    /**
     * Handy method for retrieving all log items, finding their corresponding planks, and making recipes with them. Credit to CofhCore.
     */
    private static void addLogRecipes() {
        Container tempContainer = new Container() {
            @Override
            public boolean canInteractWith(@Nonnull EntityPlayer player) {
                return false;
            }
        };

        InventoryCrafting tempCrafting = new InventoryCrafting(tempContainer, 3, 3);

        for (int i = 1; i < 9; i++) {
            tempCrafting.setInventorySlotContents(i, ItemStack.EMPTY);
        }

        List<ItemStack> logs = OreDictionary.getOres("logWood");

        for (ItemStack logEntry : logs) {
            if (logEntry.getItemDamage() == OreDictionary.WILDCARD_VALUE) {
                for (int j = 0; j < 16; j++) {
                    addSawmillLog(tempCrafting, new ItemStack(logEntry.getItem(), 1, j));
                }
            } else {
                addSawmillLog(tempCrafting, StackUtils.size(logEntry, 1));
            }
        }
    }

    private static void addSawmillLog(InventoryCrafting tempCrafting, ItemStack log) {
        tempCrafting.setInventorySlotContents(0, log);
        ItemStack resultEntry = MekanismUtils.findMatchingRecipe(tempCrafting, null);

        if (!resultEntry.isEmpty()) {
            RecipeHandler.addPrecisionSawmillRecipe(log, StackUtils.size(resultEntry, 6), new ItemStack(MekanismItems.Sawdust),
                  MekanismConfig.current().general.sawdustChanceLog.val());
        }
    }

    public static void addStandardOredictMetal(String suffix) {
        NonNullList<ItemStack> dusts = OreDictionary.getOres("dust" + suffix);
        NonNullList<ItemStack> ores = OreDictionary.getOres("ore" + suffix);
        if (dusts.size() > 0) {
            for (ItemStack ore : ores) {
                RecipeHandler.addEnrichmentChamberRecipe(StackUtils.size(ore, 1), StackUtils.size(dusts.get(0), 2));
            }

            for (ItemStack ingot : OreDictionary.getOres("ingot" + suffix)) {
                RecipeHandler.addCrusherRecipe(StackUtils.size(ingot, 1), StackUtils.size(dusts.get(0), 1));
            }
        }

        if (ores.size() > 0) {
            for (ItemStack dust : dusts) {
                RecipeHandler.addCombinerRecipe(StackUtils.size(dust, 8), new ItemStack(Blocks.COBBLESTONE), StackUtils.size(ores.get(0), 1));
            }
        }
    }

    public static void addStandardOredictGem(String suffix) {
        NonNullList<ItemStack> gems = OreDictionary.getOres("gem" + suffix);
        NonNullList<ItemStack> dusts = OreDictionary.getOres("dust" + suffix);
        NonNullList<ItemStack> ores = OreDictionary.getOres("ore" + suffix);
        if (gems.size() > 0) {
            for (ItemStack ore : ores) {
                RecipeHandler.addEnrichmentChamberRecipe(StackUtils.size(ore, 1), StackUtils.size(gems.get(0), 2));
            }
        }

        if (dusts.size() > 0) {
            for (ItemStack gem : gems) {
                RecipeHandler.addEnrichmentChamberRecipe(StackUtils.size(dusts.get(0), 1), StackUtils.size(gem, 1));
                RecipeHandler.addCrusherRecipe(StackUtils.size(gem, 1), StackUtils.size(dusts.get(0), 1));
            }
        }

        if (ores.size() > 0) {
            ItemStack ore = StackUtils.size(ores.get(0), 1);
            ItemStack base = new ItemStack(Blocks.COBBLESTONE);
            if (dusts.size() > 0) {
                for (ItemStack dust : dusts) {
                    RecipeHandler.addCombinerRecipe(StackUtils.size(dust, 3), base, ore);
                }
            } else {
                for (ItemStack gem : gems) {
                    RecipeHandler.addCombinerRecipe(StackUtils.size(gem, 3), base, ore);
                }
            }
        }
    }
}