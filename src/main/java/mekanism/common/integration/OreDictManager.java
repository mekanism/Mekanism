package mekanism.common.integration;

import ic2.api.recipe.Recipes;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import javax.annotation.Nonnull;
import mekanism.api.gas.Gas;
import mekanism.api.gas.GasRegistry;
import mekanism.api.infuse.InfuseObject;
import mekanism.api.infuse.InfuseRegistry;
import mekanism.api.infuse.InfuseType;
import mekanism.api.recipes.inputs.FluidStackIngredient;
import mekanism.api.recipes.inputs.GasIngredient;
import mekanism.api.recipes.inputs.GasStackIngredient;
import mekanism.api.recipes.inputs.InfusionIngredient;
import mekanism.common.Mekanism;
import mekanism.common.MekanismFluids;
import mekanism.common.MekanismItems;
import mekanism.common.Resource;
import mekanism.common.config.MekanismConfig;
import mekanism.common.recipe.RecipeHandler;
import mekanism.common.util.StackUtils;
import mekanism.common.world.DummyWorld;
import net.minecraft.block.BlockPlanks;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.CraftingManager;
import net.minecraft.item.crafting.FurnaceRecipes;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.util.NonNullList;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.Optional.Method;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.oredict.OreDictionary;
import net.minecraftforge.oredict.OreIngredient;

@EventBusSubscriber(modid = Mekanism.MODID)
public final class OreDictManager {

    private static final List<String> minorCompatIngot = Arrays.asList("Aluminum", "Draconium", "Iridium", "Mithril", "Nickel", "Platinum", "Uranium");
    private static final List<String> minorCompatGem = Arrays.asList("Amber", "Diamond", "Emerald", "Malachite", "Peridot", "Ruby", "Sapphire", "Tanzanite", "Topaz");

    @SubscribeEvent(priority = EventPriority.LOW)
    public static void init(RegistryEvent.Register<IRecipe> event) {
        addLogRecipes();

        List<ItemStack> oreDict;

        OreIngredient plankWood = new OreIngredient("plankWood");
        RecipeHandler.addPrecisionSawmillRecipe(plankWood, new ItemStack(Items.STICK, 6),
              new ItemStack(MekanismItems.Sawdust), MekanismConfig.current().general.sawdustChancePlank.val());
        RecipeHandler.addPRCRecipe(plankWood, FluidStackIngredient.fromInstance(FluidRegistry.WATER, 20),
              GasStackIngredient.fromInstance(MekanismFluids.Oxygen, 20), ItemStack.EMPTY, MekanismFluids.Hydrogen, 20, 0, 30);

        OreIngredient slabWood = new OreIngredient("slabWood");
        RecipeHandler.addPrecisionSawmillRecipe(slabWood, new ItemStack(Items.STICK, 3), new ItemStack(MekanismItems.Sawdust),
              MekanismConfig.current().general.sawdustChancePlank.val() / 2);
        RecipeHandler.addPRCRecipe(slabWood, FluidStackIngredient.fromInstance(FluidRegistry.WATER, 10),
              GasStackIngredient.fromInstance(MekanismFluids.Oxygen, 10), ItemStack.EMPTY, MekanismFluids.Hydrogen, 10, 0, 15);

        OreIngredient stickWood = new OreIngredient("stickWood");
        RecipeHandler.addPrecisionSawmillRecipe(stickWood, new ItemStack(MekanismItems.Sawdust));
        RecipeHandler.addPRCRecipe(stickWood, FluidStackIngredient.fromInstance(FluidRegistry.WATER, 4),
              GasStackIngredient.fromInstance(MekanismFluids.Oxygen, 4), ItemStack.EMPTY, MekanismFluids.Hydrogen, 4, 0, 6);

        RecipeHandler.addEnrichmentChamberRecipe(new OreIngredient("oreNetherSteel"), new ItemStack(MekanismItems.OtherDust, 4, 1));

        oreDict = OreDictionary.getOres("itemRubber", false);
        if (oreDict.size() > 0) {
            RecipeHandler.addPrecisionSawmillRecipe(new OreIngredient("woodRubber"), new ItemStack(Blocks.PLANKS, BlockPlanks.EnumType.JUNGLE.getMetadata(), 4),
                  StackUtils.size(oreDict.get(0), 1), 1F);
        }

        OreIngredient sulfur = new OreIngredient("dustSulfur");
        RecipeHandler.addChemicalOxidizerRecipe(sulfur, MekanismFluids.SulfurDioxide, 100);
        RecipeHandler.addEnrichmentChamberRecipe(sulfur, new ItemStack(Items.GUNPOWDER));

        RecipeHandler.addChemicalOxidizerRecipe(new OreIngredient("dustSalt"), MekanismFluids.Brine, 15);

        OreIngredient dustRefinedObsidian = new OreIngredient("dustRefinedObsidian");
        RecipeHandler.addOsmiumCompressorRecipe(dustRefinedObsidian, GasIngredient.fromInstance(MekanismFluids.LiquidOsmium), new ItemStack(MekanismItems.Ingot, 1, 0));
        RecipeHandler.addEnrichmentChamberRecipe(dustRefinedObsidian, new ItemStack(MekanismItems.CompressedObsidian));
        InfuseRegistry.registerInfuseObject(dustRefinedObsidian, new InfuseObject(Objects.requireNonNull(InfuseRegistry.get("OBSIDIAN")), 10));

        for (Resource resource : Resource.values()) {
            RecipeHandler.addCrusherRecipe(new OreIngredient("clump" + resource.getName()), new ItemStack(MekanismItems.DirtyDust, 1, resource.ordinal()));
            RecipeHandler.addPurificationChamberRecipe(new OreIngredient("shard" + resource.getName()), new ItemStack(MekanismItems.Clump, 1, resource.ordinal()));
            RecipeHandler.addChemicalInjectionChamberRecipe(new OreIngredient("crystal" + resource.getName()), GasIngredient.fromInstance(MekanismFluids.HydrogenChloride),
                  new ItemStack(MekanismItems.Shard, 1, resource.ordinal()));
            RecipeHandler.addEnrichmentChamberRecipe(new OreIngredient("dustDirty" + resource.getName()), new ItemStack(MekanismItems.Dust, 1, resource.ordinal()));

            OreIngredient oreIngredient = new OreIngredient("ore" + resource.getName());
            RecipeHandler.addEnrichmentChamberRecipe(oreIngredient, new ItemStack(MekanismItems.Dust, 2, resource.ordinal()));
            RecipeHandler.addPurificationChamberRecipe(oreIngredient, new ItemStack(MekanismItems.Clump, 3, resource.ordinal()));
            RecipeHandler.addChemicalInjectionChamberRecipe(oreIngredient, GasIngredient.fromInstance(MekanismFluids.HydrogenChloride),
                  new ItemStack(MekanismItems.Shard, 4, resource.ordinal()));
            Gas oreGas = GasRegistry.getGas(resource.getName().toLowerCase(Locale.ROOT));
            if (oreGas != null) {
                RecipeHandler.addChemicalDissolutionChamberRecipe(oreIngredient, oreGas, 1000);
            }

            RecipeHandler.addCrusherRecipe(new OreIngredient("ingot" + resource.getName()), new ItemStack(MekanismItems.Dust, 1, resource.ordinal()));

            oreDict = OreDictionary.getOres("ore" + resource.getName(), false);
            if (oreDict.size() > 0) {
                ItemStack ore = StackUtils.size(oreDict.get(0), 1);
                for (ItemStack dust : OreDictionary.getOres("dust" + resource.getName(), false)) {
                    RecipeHandler.addCombinerRecipe(StackUtils.size(dust, 8), new ItemStack(Blocks.COBBLESTONE), ore);
                }
            }
        }

        minorCompatIngot.forEach(OreDictManager::addStandardOredictMetal);
        minorCompatGem.forEach(OreDictManager::addStandardOredictGem);

        oreDict = OreDictionary.getOres("dustYellorium", false);
        if (oreDict.size() > 0) {
            RecipeHandler.addEnrichmentChamberRecipe(new OreIngredient("oreYellorite"), StackUtils.size(oreDict.get(0), 2));
        }

        oreDict = OreDictionary.getOres("dustNetherQuartz", false);
        if (oreDict.size() > 0) {
            RecipeHandler.addCrusherRecipe(new OreIngredient("gemQuartz"), StackUtils.size(oreDict.get(0), 1));
        }

        oreDict = OreDictionary.getOres("oreQuartz", false);
        if (oreDict.size() > 0) {
            ItemStack oreQuartz = StackUtils.size(oreDict.get(0), 1);
            oreDict = OreDictionary.getOres("dustQuartz", false);
            if (oreDict.size() > 0) {
                for (ItemStack dust : oreDict) {
                    RecipeHandler.addCombinerRecipe(StackUtils.size(dust, 8), new ItemStack(Blocks.COBBLESTONE), oreQuartz);
                }
            } else {
                for (ItemStack gem : OreDictionary.getOres("gemQuartz", false)) {
                    RecipeHandler.addCombinerRecipe(StackUtils.size(gem, 8), new ItemStack(Blocks.COBBLESTONE), oreQuartz);
                }
            }
        }

        oreDict = OreDictionary.getOres("gemQuartz", false);
        if (oreDict.size() > 0) {
            RecipeHandler.addEnrichmentChamberRecipe(new OreIngredient("dustNetherQuartz"), StackUtils.size(oreDict.get(0), 1));
            RecipeHandler.addEnrichmentChamberRecipe(new OreIngredient("oreQuartz"), StackUtils.size(oreDict.get(0), 6));
        }

        oreDict = OreDictionary.getOres("dustLapis", false);
        if (oreDict.size() > 0) {
            RecipeHandler.addCrusherRecipe(new OreIngredient("gemLapis"), StackUtils.size(oreDict.get(0), 1));
        }

        oreDict = OreDictionary.getOres("oreLapis", false);
        if (oreDict.size() > 0) {
            ItemStack oreLapis = StackUtils.size(oreDict.get(0), 1);
            oreDict = OreDictionary.getOres("dustLapis", false);
            if (oreDict.size() > 0) {
                for (ItemStack dust : oreDict) {
                    RecipeHandler.addCombinerRecipe(StackUtils.size(dust, 16), new ItemStack(Blocks.COBBLESTONE), oreLapis);
                }
            } else {
                for (ItemStack gem : OreDictionary.getOres("gemLapis", false)) {
                    RecipeHandler.addCombinerRecipe(StackUtils.size(gem, 16), new ItemStack(Blocks.COBBLESTONE), oreLapis);
                }
            }
        }

        oreDict = OreDictionary.getOres("gemLapis", false);
        if (oreDict.size() > 0) {
            ItemStack gemLapis = StackUtils.size(oreDict.get(0), 1);
            for (ItemStack dust : OreDictionary.getOres("dustLapis", false)) {
                RecipeHandler.addEnrichmentChamberRecipe(StackUtils.size(dust, 1), gemLapis);
            }

            gemLapis = StackUtils.size(gemLapis, 12);
            for (ItemStack ore : OreDictionary.getOres("oreLapis", false)) {
                RecipeHandler.addEnrichmentChamberRecipe(StackUtils.size(ore, 1), gemLapis);
            }
        }

        oreDict = OreDictionary.getOres("oreRedstone", false);
        if (oreDict.size() > 0) {
            ItemStack oreRedstone = StackUtils.size(oreDict.get(0), 1);
            for (ItemStack dust : OreDictionary.getOres("dustRedstone", false)) {
                RecipeHandler.addCombinerRecipe(StackUtils.size(dust, 16), new ItemStack(Blocks.COBBLESTONE), oreRedstone);
            }
        }

        oreDict = OreDictionary.getOres("dustRedstone", false);
        if (oreDict.size() > 0) {
            ItemStack dustRedstone = StackUtils.size(oreDict.get(0), 12);
            for (ItemStack ore : OreDictionary.getOres("oreRedstone", false)) {
                RecipeHandler.addEnrichmentChamberRecipe(StackUtils.size(ore, 1), dustRedstone);
            }
        }

        for (ItemStack ore : OreDictionary.getOres("oreCoal", false)) {
            RecipeHandler.addEnrichmentChamberRecipe(StackUtils.size(ore, 1), new ItemStack(Items.COAL, 2));
        }

        oreDict = OreDictionary.getOres("oreAmethyst", false);
        if (oreDict.size() > 0) {
            ItemStack oreAmethyst = StackUtils.size(oreDict.get(0), 1);
            oreDict = OreDictionary.getOres("dustAmethyst", false);
            if (oreDict.size() > 0) {
                for (ItemStack dust : oreDict) {
                    RecipeHandler.addCombinerRecipe(StackUtils.size(dust, 3), new ItemStack(Blocks.END_STONE), oreAmethyst);
                }
            } else {
                for (ItemStack gem : OreDictionary.getOres("gemAmethyst", false)) {
                    RecipeHandler.addCombinerRecipe(StackUtils.size(gem, 3), new ItemStack(Blocks.END_STONE), oreAmethyst);
                }
            }
        }

        oreDict = OreDictionary.getOres("gemAmethyst", false);
        if (oreDict.size() > 0) {
            ItemStack gemAmethyst = StackUtils.size(oreDict.get(0), 2);
            for (ItemStack ore : OreDictionary.getOres("oreAmethyst", false)) {
                RecipeHandler.addEnrichmentChamberRecipe(StackUtils.size(ore, 1), gemAmethyst);
            }
        }

        oreDict = OreDictionary.getOres("oreApatite", false);
        if (oreDict.size() > 0) {
            ItemStack oreApatite = StackUtils.size(oreDict.get(0), 1);
            oreDict = OreDictionary.getOres("dustApatite", false);
            if (oreDict.size() > 0) {
                for (ItemStack dust : oreDict) {
                    RecipeHandler.addCombinerRecipe(StackUtils.size(dust, 6), new ItemStack(Blocks.COBBLESTONE), oreApatite);
                }
            } else {
                for (ItemStack gem : OreDictionary.getOres("gemApatite", false)) {
                    RecipeHandler.addCombinerRecipe(StackUtils.size(gem, 6), new ItemStack(Blocks.COBBLESTONE), oreApatite);
                }
            }
        }

        oreDict = OreDictionary.getOres("gemApatite", false);
        if (oreDict.size() > 0) {
            ItemStack gemApatite = StackUtils.size(oreDict.get(0), 4);
            for (ItemStack ore : OreDictionary.getOres("oreApatite", false)) {
                RecipeHandler.addEnrichmentChamberRecipe(StackUtils.size(ore, 1), gemApatite);
            }
        }

        InfuseType tinInfuseType = Objects.requireNonNull(InfuseRegistry.get("TIN"));
        for (ItemStack ingot : OreDictionary.getOres("ingotCopper", false)) {
            RecipeHandler.addMetallurgicInfuserRecipe(tinInfuseType, 10, StackUtils.size(ingot, 3),
                  new ItemStack(MekanismItems.Ingot, 4, 2));
        }

        for (ItemStack ingot : OreDictionary.getOres("ingotRefinedObsidian", false)) {
            RecipeHandler.addCrusherRecipe(StackUtils.size(ingot, 1), new ItemStack(MekanismItems.OtherDust, 1, 5));
        }

        RecipeHandler.addMetallurgicInfuserRecipe(InfusionIngredient.from(InfuseRegistry.get("REDSTONE"), 10), new OreIngredient("ingotOsmium"),
              new ItemStack(MekanismItems.ControlCircuit, 1, 0));

        for (ItemStack ingot : OreDictionary.getOres("ingotRedstone", false)) {
            RecipeHandler.addCrusherRecipe(StackUtils.size(ingot, 1), new ItemStack(Items.REDSTONE));
        }

        for (ItemStack ingot : OreDictionary.getOres("ingotRefinedGlowstone", false)) {
            RecipeHandler.addCrusherRecipe(StackUtils.size(ingot, 1), new ItemStack(Items.GLOWSTONE_DUST));
        }

        oreDict = OreDictionary.getOres("dustBronze", false);
        if (oreDict.size() > 0) {
            ItemStack dustBronze = StackUtils.size(oreDict.get(0), 1);
            RecipeHandler.addCrusherRecipe(new ItemStack(MekanismItems.Ingot, 1, 2), dustBronze);
        }

        if (Mekanism.hooks.IC2Loaded) {
            addIC2BronzeRecipe();
        }

        oreDict = OreDictionary.getOres("ingotSilver", false);
        if (oreDict.size() > 0) {
            ItemStack ingotSilver = StackUtils.size(oreDict.get(0), 1);
            FurnaceRecipes.instance().addSmeltingRecipe(new ItemStack(MekanismItems.Dust, 1, Resource.SILVER.ordinal()), ingotSilver, 0.0F);
        }

        oreDict = OreDictionary.getOres("ingotLead", false);
        if (oreDict.size() > 0) {
            ItemStack ingotLead = StackUtils.size(oreDict.get(0), 1);
            FurnaceRecipes.instance().addSmeltingRecipe(new ItemStack(MekanismItems.Dust, 1, Resource.LEAD.ordinal()), ingotLead, 0.0F);
        }

        oreDict = OreDictionary.getOres("dustCoal", false);
        if (oreDict.size() > 0) {
            RecipeHandler.addCrusherRecipe(new ItemStack(Items.COAL), StackUtils.size(oreDict.get(0), 1));
            OreIngredient dustCoal = new OreIngredient("dustCoal");
            RecipeHandler.addEnrichmentChamberRecipe(dustCoal, new ItemStack(Items.COAL));
            RecipeHandler.addPRCRecipe(dustCoal, FluidStackIngredient.fromInstance(FluidRegistry.WATER, 100),
                  GasStackIngredient.fromInstance(MekanismFluids.Oxygen, 100), new ItemStack(MekanismItems.OtherDust, 1, 3),
                  MekanismFluids.Hydrogen, 100, 0, 100);
        }

        oreDict = OreDictionary.getOres("dustCharcoal", false);
        if (oreDict.size() > 0) {
            RecipeHandler.addCrusherRecipe(new ItemStack(Items.COAL, 1, 1), StackUtils.size(oreDict.get(0), 1));
            OreIngredient dustCharcoal = new OreIngredient("dustCharcoal");
            RecipeHandler.addEnrichmentChamberRecipe(dustCharcoal, new ItemStack(Items.COAL, 1, 1));
            RecipeHandler.addPRCRecipe(dustCharcoal, FluidStackIngredient.fromInstance(FluidRegistry.WATER, 100),
                  GasStackIngredient.fromInstance(MekanismFluids.Oxygen, 100), new ItemStack(MekanismItems.OtherDust, 1, 3),
                  MekanismFluids.Hydrogen, 100, 0, 100);
        }

        oreDict = OreDictionary.getOres("dustSaltpeter", false);
        if (oreDict.size() > 0) {
            ItemStack dustSaltpeter = StackUtils.size(oreDict.get(0), 1);
            RecipeHandler.addCrusherRecipe(new ItemStack(Items.GUNPOWDER), dustSaltpeter);
            for (ItemStack dust : oreDict) {
                RecipeHandler.addEnrichmentChamberRecipe(StackUtils.size(dust, 1), new ItemStack(Items.GUNPOWDER));
            }
        }

        oreDict = OreDictionary.getOres("itemSilicon", false);
        if (oreDict.size() > 0) {
            ItemStack itemSilicon = StackUtils.size(oreDict.get(0), 1);
            for (ItemStack sand : OreDictionary.getOres("sand", false)) {
                RecipeHandler.addCrusherRecipe(StackUtils.size(sand, 1), itemSilicon);
            }
        }

        RecipeHandler.addCrusherRecipe(new OreIngredient("ingotSteel"), new ItemStack(MekanismItems.OtherDust, 1, 1));
        RecipeHandler.addChemicalOxidizerRecipe(new OreIngredient("dustLithium"), MekanismFluids.Lithium, 100);

        InfuseType diamondInfuseType = Objects.requireNonNull(InfuseRegistry.get("DIAMOND"));
        RecipeHandler.addMetallurgicInfuserRecipe(InfusionIngredient.from(diamondInfuseType, 10), new OreIngredient("dustObsidian"),
              new ItemStack(MekanismItems.OtherDust, 1, 5));
        for (ItemStack dust : OreDictionary.getOres("dustObsidian", false)) {
            RecipeHandler.addCombinerRecipe(StackUtils.size(dust, 4), new ItemStack(Blocks.COBBLESTONE), new ItemStack(Blocks.OBSIDIAN));
        }

        InfuseRegistry.registerInfuseObject(new OreIngredient("dustDiamond"), new InfuseObject(diamondInfuseType, 10));

        InfuseRegistry.registerInfuseObject(new OreIngredient("dustTin"), new InfuseObject(tinInfuseType, 10));

        RecipeHandler.addCrusherRecipe(new OreIngredient("treeSapling"), new ItemStack(MekanismItems.BioFuel, 2));

        RecipeHandler.addPRCRecipe(new OreIngredient("blockCoal"), FluidStackIngredient.fromInstance(FluidRegistry.WATER, 1000),
              GasStackIngredient.fromInstance(MekanismFluids.Oxygen, 1000), new ItemStack(MekanismItems.OtherDust, 9, 3),
              MekanismFluids.Hydrogen, 1000, 0, 900);
        RecipeHandler.addPRCRecipe(new OreIngredient("blockCharcoal"), FluidStackIngredient.fromInstance(FluidRegistry.WATER, 1000),
              GasStackIngredient.fromInstance(MekanismFluids.Oxygen, 1000), new ItemStack(MekanismItems.OtherDust, 9, 3),
              MekanismFluids.Hydrogen, 1000, 0, 900);

        RecipeHandler.addPRCRecipe(new OreIngredient("dustWood"), FluidStackIngredient.fromInstance(FluidRegistry.WATER, 20),
              GasStackIngredient.fromInstance(MekanismFluids.Oxygen, 20), ItemStack.EMPTY, MekanismFluids.Hydrogen, 20, 0, 30);
        for (ItemStack sawdust : OreDictionary.getOres("dustWood", false)) {
            //TODO: 1.14 evaluate adding a charcoal dust item to Mekanism, and if so use that instead of charcoal here
            RecipeHandler.addEnrichmentChamberRecipe(StackUtils.size(sawdust, 8), new ItemStack(Items.COAL, 1, 1));
        }
    }

    @Method(modid = MekanismHooks.IC2_MOD_ID)
    private static void addIC2BronzeRecipe() {
        Recipes.macerator.addRecipe(Recipes.inputFactory.forOreDict("ingotBronze"), null, false,
              StackUtils.size(OreDictionary.getOres("dustBronze", false).get(0), 1));
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
        DummyWorld dummyWorld = null;
        try {
            dummyWorld = new DummyWorld();
        } catch (Exception ignored) {
        }

        InventoryCrafting tempCrafting = new InventoryCrafting(tempContainer, 3, 3);

        for (int i = 1; i < 9; i++) {
            tempCrafting.setInventorySlotContents(i, ItemStack.EMPTY);
        }

        OreIngredient logWood = new OreIngredient("logWood");
        for (ItemStack logEntry : OreDictionary.getOres("logWood", false)) {
            if (logEntry.getItemDamage() == OreDictionary.WILDCARD_VALUE) {
                for (int j = 0; j < 16; j++) {
                    addSawmillLog(tempCrafting, new ItemStack(logEntry.getItem(), 1, j), dummyWorld);
                }
            } else {
                addSawmillLog(tempCrafting, StackUtils.size(logEntry, 1), dummyWorld);
            }
        }
        RecipeHandler.addPRCRecipe(logWood, FluidStackIngredient.fromInstance(FluidRegistry.WATER, 100),
              GasStackIngredient.fromInstance(MekanismFluids.Oxygen, 100), ItemStack.EMPTY, MekanismFluids.Hydrogen, 100, 0, 150);
    }

    private static void addSawmillLog(InventoryCrafting tempCrafting, ItemStack log, DummyWorld world) {
        tempCrafting.setInventorySlotContents(0, log);
        IRecipe matchingRecipe = CraftingManager.findMatchingRecipe(tempCrafting, world);
        ItemStack resultEntry = matchingRecipe != null ? matchingRecipe.getRecipeOutput() : ItemStack.EMPTY;

        if (!resultEntry.isEmpty()) {
            //TODO: Figure out if this should be using the "logWood" entry? It probably shouldn't due to the fact that would include multiple wood types
            RecipeHandler.addPrecisionSawmillRecipe(Ingredient.fromStacks(log), StackUtils.size(resultEntry, 6), new ItemStack(MekanismItems.Sawdust),
                  MekanismConfig.current().general.sawdustChanceLog.val());
        }
    }

    public static void addStandardOredictMetal(String suffix) {
        NonNullList<ItemStack> dusts = OreDictionary.getOres("dust" + suffix, false);
        NonNullList<ItemStack> ores = OreDictionary.getOres("ore" + suffix, false);
        if (dusts.size() > 0) {
            for (ItemStack ore : ores) {
                RecipeHandler.addEnrichmentChamberRecipe(StackUtils.size(ore, 1), StackUtils.size(dusts.get(0), 2));
            }

            for (ItemStack ingot : OreDictionary.getOres("ingot" + suffix, false)) {
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
        NonNullList<ItemStack> gems = OreDictionary.getOres("gem" + suffix, false);
        NonNullList<ItemStack> dusts = OreDictionary.getOres("dust" + suffix, false);
        NonNullList<ItemStack> ores = OreDictionary.getOres("ore" + suffix, false);
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
