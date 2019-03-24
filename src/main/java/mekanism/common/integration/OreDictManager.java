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
import mekanism.common.Mekanism;
import mekanism.common.MekanismFluids;
import mekanism.common.MekanismItems;
import mekanism.common.Resource;
import mekanism.common.config.MekanismConfig.general;
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
import net.minecraftforge.fml.common.Optional.Method;
import net.minecraftforge.oredict.OreDictionary;

public final class OreDictManager {

    private static final List<String> minorCompat = Arrays
          .asList("Nickel", "Aluminum", "Uranium", "Draconium", "Platinum", "Iridium");

    public static void init() {
        addLogRecipes();

        for (ItemStack ore : OreDictionary.getOres("plankWood")) {
            ItemStack plank = StackUtils.size(ore, 1);
            if (!Recipe.PRECISION_SAWMILL.containsRecipe(plank)) {
                RecipeHandler.addPrecisionSawmillRecipe(plank, new ItemStack(Items.STICK, 6),
                      new ItemStack(MekanismItems.Sawdust), general.sawdustChancePlank);
            }
        }

        for (ItemStack ore : OreDictionary.getOres("stickWood")) {
            ItemStack stick = StackUtils.size(ore, 1);
            if (!Recipe.PRECISION_SAWMILL.containsRecipe(stick)) {
                RecipeHandler.addPrecisionSawmillRecipe(stick, new ItemStack(MekanismItems.Sawdust));
            }
        }

        for (ItemStack ore : OreDictionary.getOres("oreNetherSteel")) {
            RecipeHandler
                  .addEnrichmentChamberRecipe(StackUtils.size(ore, 1), new ItemStack(MekanismItems.OtherDust, 4, 1));
        }

        if (OreDictionary.getOres("itemRubber").size() > 0) {
            for (ItemStack ore : OreDictionary.getOres("woodRubber")) {
                RecipeHandler.addPrecisionSawmillRecipe(StackUtils.size(ore, 1),
                      new ItemStack(Blocks.PLANKS, BlockPlanks.EnumType.JUNGLE.getMetadata(), 4),
                      StackUtils.size(OreDictionary.getOres("itemRubber").get(0), 1), 1F);
            }
        }

        for (ItemStack ore : OreDictionary.getOres("dustSulfur")) {
            RecipeHandler
                  .addChemicalOxidizerRecipe(StackUtils.size(ore, 1), new GasStack(MekanismFluids.SulfurDioxide, 100));
        }

        for (ItemStack ore : OreDictionary.getOres("dustSalt")) {
            RecipeHandler.addChemicalOxidizerRecipe(StackUtils.size(ore, 1), new GasStack(MekanismFluids.Brine, 15));
        }

        for (ItemStack ore : OreDictionary.getOres("dustRefinedObsidian")) {
            RecipeHandler.addOsmiumCompressorRecipe(StackUtils.size(ore, 1), new ItemStack(MekanismItems.Ingot, 1, 0));
            RecipeHandler.addCrusherRecipe(StackUtils.size(ore, 1), new ItemStack(MekanismItems.OtherDust, 1, 6));
            RecipeHandler
                  .addEnrichmentChamberRecipe(StackUtils.size(ore, 1), new ItemStack(MekanismItems.CompressedObsidian));

            InfuseRegistry
                  .registerInfuseObject(StackUtils.size(ore, 1), new InfuseObject(InfuseRegistry.get("OBSIDIAN"), 10));
        }

        for (Resource resource : Resource.values()) {
            for (ItemStack ore : OreDictionary.getOres("clump" + resource.getName())) {
                RecipeHandler.addCrusherRecipe(StackUtils.size(ore, 1),
                      new ItemStack(MekanismItems.DirtyDust, 1, resource.ordinal()));
            }

            for (ItemStack ore : OreDictionary.getOres("shard" + resource.getName())) {
                RecipeHandler.addPurificationChamberRecipe(StackUtils.size(ore, 1),
                      new ItemStack(MekanismItems.Clump, 1, resource.ordinal()));
            }

            for (ItemStack ore : OreDictionary.getOres("crystal" + resource.getName())) {
                RecipeHandler
                      .addChemicalInjectionChamberRecipe(StackUtils.size(ore, 1), MekanismFluids.HydrogenChloride,
                            new ItemStack(MekanismItems.Shard, 1, resource.ordinal()));
            }

            for (ItemStack ore : OreDictionary.getOres("dustDirty" + resource.getName())) {
                RecipeHandler.addEnrichmentChamberRecipe(StackUtils.size(ore, 1),
                      new ItemStack(MekanismItems.Dust, 1, resource.ordinal()));
            }

            for (ItemStack ore : OreDictionary.getOres("ore" + resource.getName())) {
                RecipeHandler.addEnrichmentChamberRecipe(StackUtils.size(ore, 1),
                      new ItemStack(MekanismItems.Dust, 2, resource.ordinal()));
                RecipeHandler.addPurificationChamberRecipe(StackUtils.size(ore, 1),
                      new ItemStack(MekanismItems.Clump, 3, resource.ordinal()));
                RecipeHandler
                      .addChemicalInjectionChamberRecipe(StackUtils.size(ore, 1), MekanismFluids.HydrogenChloride,
                            new ItemStack(MekanismItems.Shard, 4, resource.ordinal()));
                RecipeHandler.addChemicalDissolutionChamberRecipe(StackUtils.size(ore, 1),
                      new GasStack(GasRegistry.getGas(resource.getName().toLowerCase(Locale.ROOT)), 1000));
            }

            for (ItemStack ore : OreDictionary.getOres("ingot" + resource.getName())) {
                RecipeHandler.addCrusherRecipe(StackUtils.size(ore, 1),
                      new ItemStack(MekanismItems.Dust, 1, resource.ordinal()));
            }

            try {
                for (ItemStack ore : OreDictionary.getOres("dust" + resource.getName())) {
                    RecipeHandler.addCombinerRecipe(StackUtils.size(ore, 8), new ItemStack(Blocks.COBBLESTONE),
                          StackUtils.size(OreDictionary.getOres("ore" + resource.getName()).get(0), 1));
                }
            } catch (Exception ignored) {
            }
        }

        for (String s : minorCompat) {
            for (ItemStack ore : OreDictionary.getOres("ore" + s)) {
                try {
                    RecipeHandler.addEnrichmentChamberRecipe(StackUtils.size(ore, 1),
                          StackUtils.size(OreDictionary.getOres("dust" + s).get(0), 2));
                } catch (Exception ignored) {
                }
            }

            for (ItemStack ore : OreDictionary.getOres("ingot" + s)) {
                try {
                    RecipeHandler.addCrusherRecipe(StackUtils.size(ore, 1),
                          StackUtils.size(OreDictionary.getOres("dust" + s).get(0), 1));
                } catch (Exception ignored) {
                }
            }

            for (ItemStack ore : OreDictionary.getOres("dust" + s)) {
                try {
                    RecipeHandler.addCombinerRecipe(StackUtils.size(ore, 8), new ItemStack(Blocks.COBBLESTONE),
                          StackUtils.size(OreDictionary.getOres("ore" + s).get(0), 1));
                } catch (Exception ignored) {
                }
            }
        }

        for (ItemStack ore : OreDictionary.getOres("oreYellorite")) {
            try {
                RecipeHandler.addEnrichmentChamberRecipe(StackUtils.size(ore, 1),
                      StackUtils.size(OreDictionary.getOres("dustYellorium").get(0), 2));
            } catch (Exception ignored) {
            }
        }

        for (ItemStack ore : OreDictionary.getOres("gemQuartz")) {
            try {
                RecipeHandler.addCrusherRecipe(StackUtils.size(ore, 1),
                      StackUtils.size(OreDictionary.getOres("dustNetherQuartz").get(0), 1));
            } catch (Exception ignored) {
            }
        }

        for (ItemStack ore : OreDictionary.getOres("dustNetherQuartz")) {
            try {
                RecipeHandler.addEnrichmentChamberRecipe(StackUtils.size(ore, 1),
                      StackUtils.size(OreDictionary.getOres("gemQuartz").get(0), 1));
            } catch (Exception ignored) {
            }
        }

        for (ItemStack ore : OreDictionary.getOres("oreQuartz")) {
            RecipeHandler.addEnrichmentChamberRecipe(StackUtils.size(ore, 1), new ItemStack(Items.QUARTZ, 6));
        }

        for (ItemStack ore : OreDictionary.getOres("ingotCopper")) {
            RecipeHandler.addMetallurgicInfuserRecipe(InfuseRegistry.get("TIN"), 10, StackUtils.size(ore, 3),
                  new ItemStack(MekanismItems.Ingot, 4, 2));
        }

        for (ItemStack ore : OreDictionary.getOres("ingotRefinedObsidian")) {
            RecipeHandler.addCrusherRecipe(StackUtils.size(ore, 1), new ItemStack(MekanismItems.OtherDust, 1, 6));
        }

        for (ItemStack ore : OreDictionary.getOres("ingotOsmium")) {
            RecipeHandler.addMetallurgicInfuserRecipe(InfuseRegistry.get("REDSTONE"), 10, StackUtils.size(ore, 1),
                  new ItemStack(MekanismItems.ControlCircuit, 1, 0));
        }

        for (ItemStack ore : OreDictionary.getOres("ingotRedstone")) {
            RecipeHandler.addCrusherRecipe(StackUtils.size(ore, 1), new ItemStack(Items.REDSTONE));
        }

        for (ItemStack ore : OreDictionary.getOres("ingotRefinedGlowstone")) {
            RecipeHandler.addCrusherRecipe(StackUtils.size(ore, 1), new ItemStack(Items.GLOWSTONE_DUST));
        }

        try {
            RecipeHandler.addCrusherRecipe(new ItemStack(MekanismItems.Ingot, 1, 2),
                  StackUtils.size(OreDictionary.getOres("dustBronze").get(0), 1));

            if (Mekanism.hooks.IC2Loaded) {
                addIC2BronzeRecipe();
            }
        } catch (Exception ignored) {
        }

        try {
            FurnaceRecipes.instance().addSmeltingRecipe(new ItemStack(MekanismItems.Dust, 1, Resource.SILVER.ordinal()),
                  StackUtils.size(OreDictionary.getOres("ingotSilver").get(0), 1), 0.0F);
        } catch (Exception ignored) {
        }

        try {
            FurnaceRecipes.instance().addSmeltingRecipe(new ItemStack(MekanismItems.Dust, 1, Resource.LEAD.ordinal()),
                  StackUtils.size(OreDictionary.getOres("ingotLead").get(0), 1), 0.0F);
        } catch (Exception ignored) {
        }

        try {
            RecipeHandler.addCrusherRecipe(new ItemStack(Items.COAL),
                  StackUtils.size(OreDictionary.getOres("dustCoal").get(0), 1));
        } catch (Exception ignored) {
        }

        try {
            RecipeHandler.addCrusherRecipe(new ItemStack(Items.COAL, 1, 1),
                  StackUtils.size(OreDictionary.getOres("dustCharcoal").get(0), 1));
        } catch (Exception ignored) {
        }

        try {
            RecipeHandler.addCrusherRecipe(new ItemStack(Items.GUNPOWDER),
                  StackUtils.size(OreDictionary.getOres("dustSaltpeter").get(0), 1));
        } catch (Exception ignored) {
        }

        for (ItemStack ore : OreDictionary.getOres("sand")) {
            try {
                RecipeHandler.addCrusherRecipe(StackUtils.size(ore, 1),
                      StackUtils.size(OreDictionary.getOres("itemSilicon").get(0), 1));
            } catch (Exception ignored) {
            }
        }

        for (ItemStack ore : OreDictionary.getOres("dustSaltpeter")) {
            RecipeHandler.addEnrichmentChamberRecipe(StackUtils.size(ore, 1), new ItemStack(Items.GUNPOWDER));
        }

        for (ItemStack ore : OreDictionary.getOres("ingotSteel")) {
            RecipeHandler.addCrusherRecipe(StackUtils.size(ore, 1), new ItemStack(MekanismItems.OtherDust, 1, 1));
        }

        for (ItemStack ore : OreDictionary.getOres("dustLapis")) {
            RecipeHandler.addCrusherRecipe(new ItemStack(Items.DYE, 1, 4), StackUtils.size(ore, 1));
        }

        for (ItemStack ore : OreDictionary.getOres("dustLithium")) {
            RecipeHandler.addChemicalOxidizerRecipe(StackUtils.size(ore, 1), new GasStack(MekanismFluids.Lithium, 100));
        }

        for (ItemStack ore : OreDictionary.getOres("dustObsidian")) {
            RecipeHandler.addCombinerRecipe(StackUtils.size(ore, 4), new ItemStack(Blocks.COBBLESTONE),
                  new ItemStack(Blocks.OBSIDIAN));
            RecipeHandler.addMetallurgicInfuserRecipe(InfuseRegistry.get("DIAMOND"), 10, StackUtils.size(ore, 1),
                  new ItemStack(MekanismItems.OtherDust, 1, 5));
        }

        for (ItemStack ore : OreDictionary.getOres("dustDiamond")) {
            RecipeHandler.addCombinerRecipe(StackUtils.size(ore, 3), new ItemStack(Blocks.COBBLESTONE),
                  new ItemStack(Blocks.DIAMOND_ORE));
            InfuseRegistry.registerInfuseObject(ore, new InfuseObject(InfuseRegistry.get("DIAMOND"), 10));
            RecipeHandler.addEnrichmentChamberRecipe(StackUtils.size(ore, 1), new ItemStack(Items.DIAMOND));
        }

        for (ItemStack ore : OreDictionary.getOres("dustTin")) {
            InfuseRegistry.registerInfuseObject(ore, new InfuseObject(InfuseRegistry.get("TIN"), 10));
        }

        try {
            for (ItemStack ore : OreDictionary.getOres("treeSapling")) {
                if (ore.getItemDamage() == 0 || ore.getItemDamage() == OreDictionary.WILDCARD_VALUE) {
                    RecipeHandler.addCrusherRecipe(new ItemStack(ore.getItem(), 1, OreDictionary.WILDCARD_VALUE),
                          new ItemStack(MekanismItems.BioFuel, 2));
                }
            }
        } catch (Exception ignored) {
        }
    }

    @Method(modid = MekanismHooks.IC2_MOD_ID)
    public static void addIC2BronzeRecipe() {
        try {
            Recipes.macerator.addRecipe(Recipes.inputFactory.forOreDict("ingotBronze"), null, false,
                  StackUtils.size(OreDictionary.getOres("dustBronze").get(0), 1));
        } catch (Exception ignored) {
        }
    }


    /**
     * Handy method for retrieving all log items, finding their corresponding planks, and making recipes with them.
     * Credit to CofhCore.
     */
    public static void addLogRecipes() {
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

        List<ItemStack> registeredOres = OreDictionary.getOres("logWood");

        for (ItemStack logEntry : registeredOres) {
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
            RecipeHandler.addPrecisionSawmillRecipe(log, StackUtils.size(resultEntry, 6),
                  new ItemStack(MekanismItems.Sawdust), general.sawdustChanceLog);
        }
    }
}
