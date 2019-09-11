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
import mekanism.api.recipes.inputs.GasStackIngredient;
import mekanism.api.recipes.inputs.InfusionIngredient;
import mekanism.api.recipes.inputs.ItemStackIngredient;
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

        //TODO: Remove size checks?
        List<ItemStack> oreDict;

        ItemStackIngredient plankWood = ItemStackIngredient.from("plankWood");
        RecipeHandler.addPrecisionSawmillRecipe(plankWood, new ItemStack(Items.STICK, 6),
              new ItemStack(MekanismItems.Sawdust), MekanismConfig.current().general.sawdustChancePlank.val());
        RecipeHandler.addPRCRecipe(plankWood, FluidStackIngredient.from(FluidRegistry.WATER, 20),
              GasStackIngredient.from(MekanismFluids.Oxygen, 20), ItemStack.EMPTY, MekanismFluids.Hydrogen, 20, 0, 30);

        ItemStackIngredient slabWood = ItemStackIngredient.from("slabWood");
        RecipeHandler.addPrecisionSawmillRecipe(slabWood, new ItemStack(Items.STICK, 3), new ItemStack(MekanismItems.Sawdust),
              MekanismConfig.current().general.sawdustChancePlank.val() / 2);
        RecipeHandler.addPRCRecipe(slabWood, FluidStackIngredient.from(FluidRegistry.WATER, 10),
              GasStackIngredient.from(MekanismFluids.Oxygen, 10), ItemStack.EMPTY, MekanismFluids.Hydrogen, 10, 0, 15);

        ItemStackIngredient stickWood = ItemStackIngredient.from("stickWood");
        RecipeHandler.addPrecisionSawmillRecipe(stickWood, new ItemStack(MekanismItems.Sawdust));
        RecipeHandler.addPRCRecipe(stickWood, FluidStackIngredient.from(FluidRegistry.WATER, 4),
              GasStackIngredient.from(MekanismFluids.Oxygen, 4), ItemStack.EMPTY, MekanismFluids.Hydrogen, 4, 0, 6);

        RecipeHandler.addEnrichmentChamberRecipe(ItemStackIngredient.from("oreNetherSteel"), new ItemStack(MekanismItems.OtherDust, 4, 1));

        oreDict = OreDictionary.getOres("itemRubber", false);
        if (oreDict.size() > 0) {
            RecipeHandler.addPrecisionSawmillRecipe(ItemStackIngredient.from("woodRubber"), new ItemStack(Blocks.PLANKS, BlockPlanks.EnumType.JUNGLE.getMetadata(), 4),
                  StackUtils.size(oreDict.get(0), 1), 1F);
        }

        ItemStackIngredient sulfur = ItemStackIngredient.from("dustSulfur");
        RecipeHandler.addChemicalOxidizerRecipe(sulfur, MekanismFluids.SulfurDioxide, 100);
        RecipeHandler.addEnrichmentChamberRecipe(sulfur, new ItemStack(Items.GUNPOWDER));

        RecipeHandler.addChemicalOxidizerRecipe(ItemStackIngredient.from("dustSalt"), MekanismFluids.Brine, 15);

        ItemStackIngredient dustRefinedObsidian = ItemStackIngredient.from("dustRefinedObsidian");
        RecipeHandler.addOsmiumCompressorRecipe(dustRefinedObsidian, GasStackIngredient.from(MekanismFluids.LiquidOsmium, 1),
              new ItemStack(MekanismItems.Ingot, 1, 0));
        RecipeHandler.addEnrichmentChamberRecipe(dustRefinedObsidian, new ItemStack(MekanismItems.CompressedObsidian));
        InfuseRegistry.registerInfuseObject(new OreIngredient("dustRefinedObsidian"), new InfuseObject(Objects.requireNonNull(InfuseRegistry.get("OBSIDIAN")), 10));

        for (Resource resource : Resource.values()) {
            RecipeHandler.addCrusherRecipe(ItemStackIngredient.from("clump" + resource.getName()), new ItemStack(MekanismItems.DirtyDust, 1, resource.ordinal()));
            RecipeHandler.addPurificationChamberRecipe(ItemStackIngredient.from("shard" + resource.getName()), new ItemStack(MekanismItems.Clump, 1, resource.ordinal()));
            RecipeHandler.addChemicalInjectionChamberRecipe(ItemStackIngredient.from("crystal" + resource.getName()), GasStackIngredient.from(MekanismFluids.HydrogenChloride, 1),
                  new ItemStack(MekanismItems.Shard, 1, resource.ordinal()));
            RecipeHandler.addEnrichmentChamberRecipe(ItemStackIngredient.from("dustDirty" + resource.getName()), new ItemStack(MekanismItems.Dust, 1, resource.ordinal()));

            ItemStackIngredient oreIngredient = ItemStackIngredient.from("ore" + resource.getName());
            RecipeHandler.addEnrichmentChamberRecipe(oreIngredient, new ItemStack(MekanismItems.Dust, 2, resource.ordinal()));
            RecipeHandler.addPurificationChamberRecipe(oreIngredient, new ItemStack(MekanismItems.Clump, 3, resource.ordinal()));
            RecipeHandler.addChemicalInjectionChamberRecipe(oreIngredient, GasStackIngredient.from(MekanismFluids.HydrogenChloride, 1),
                  new ItemStack(MekanismItems.Shard, 4, resource.ordinal()));
            Gas oreGas = GasRegistry.getGas(resource.getName().toLowerCase(Locale.ROOT));
            if (oreGas != null) {
                RecipeHandler.addChemicalDissolutionChamberRecipe(oreIngredient, GasStackIngredient.from(MekanismFluids.SulfuricAcid, 1), oreGas, 1000);
            }

            RecipeHandler.addCrusherRecipe(ItemStackIngredient.from("ingot" + resource.getName()), new ItemStack(MekanismItems.Dust, 1, resource.ordinal()));

            oreDict = OreDictionary.getOres("ore" + resource.getName(), false);
            if (oreDict.size() > 0) {
                RecipeHandler.addCombinerRecipe(ItemStackIngredient.from("dust" + resource.getName(), 8), ItemStackIngredient.from("cobblestone"),
                      StackUtils.size(oreDict.get(0), 1));
            }
        }

        minorCompatIngot.forEach(OreDictManager::addStandardOredictMetal);
        minorCompatGem.forEach(OreDictManager::addStandardOredictGem);

        oreDict = OreDictionary.getOres("dustYellorium", false);
        if (oreDict.size() > 0) {
            RecipeHandler.addEnrichmentChamberRecipe(ItemStackIngredient.from("oreYellorite"), StackUtils.size(oreDict.get(0), 2));
        }

        oreDict = OreDictionary.getOres("dustNetherQuartz", false);
        if (oreDict.size() > 0) {
            RecipeHandler.addCrusherRecipe(ItemStackIngredient.from("gemQuartz"), StackUtils.size(oreDict.get(0), 1));
        }

        oreDict = OreDictionary.getOres("oreQuartz", false);
        if (oreDict.size() > 0) {
            ItemStack oreQuartz = StackUtils.size(oreDict.get(0), 1);
            oreDict = OreDictionary.getOres("dustQuartz", false);
            if (oreDict.size() > 0) {
                RecipeHandler.addCombinerRecipe(ItemStackIngredient.from("dustQuartz", 8), ItemStackIngredient.from("cobblestone"), oreQuartz);
            } else {
                RecipeHandler.addCombinerRecipe(ItemStackIngredient.from("gemQuartz", 8), ItemStackIngredient.from("cobblestone"), oreQuartz);
            }
        }

        oreDict = OreDictionary.getOres("gemQuartz", false);
        if (oreDict.size() > 0) {
            RecipeHandler.addEnrichmentChamberRecipe(ItemStackIngredient.from("dustNetherQuartz"), StackUtils.size(oreDict.get(0), 1));
            RecipeHandler.addEnrichmentChamberRecipe(ItemStackIngredient.from("oreQuartz"), StackUtils.size(oreDict.get(0), 6));
        }

        oreDict = OreDictionary.getOres("dustLapis", false);
        if (oreDict.size() > 0) {
            RecipeHandler.addCrusherRecipe(ItemStackIngredient.from("gemLapis"), StackUtils.size(oreDict.get(0), 1));
        }

        oreDict = OreDictionary.getOres("oreLapis", false);
        if (oreDict.size() > 0) {
            ItemStack oreLapis = StackUtils.size(oreDict.get(0), 1);
            oreDict = OreDictionary.getOres("dustLapis", false);
            if (oreDict.size() > 0) {
                RecipeHandler.addCombinerRecipe(ItemStackIngredient.from("dustLapis", 16), ItemStackIngredient.from("cobblestone"), oreLapis);
            } else {
                RecipeHandler.addCombinerRecipe(ItemStackIngredient.from("gemLapis", 16), ItemStackIngredient.from("cobblestone"), oreLapis);
            }
        }

        oreDict = OreDictionary.getOres("gemLapis", false);
        if (oreDict.size() > 0) {
            RecipeHandler.addEnrichmentChamberRecipe(ItemStackIngredient.from("dustLapis"), StackUtils.size(oreDict.get(0), 1));
            RecipeHandler.addEnrichmentChamberRecipe(ItemStackIngredient.from("oreLapis"), StackUtils.size(oreDict.get(0), 12));
        }

        oreDict = OreDictionary.getOres("oreRedstone", false);
        if (oreDict.size() > 0) {
            RecipeHandler.addCombinerRecipe(ItemStackIngredient.from("dustRedstone", 16), ItemStackIngredient.from("cobblestone"), StackUtils.size(oreDict.get(0), 1));
        }

        oreDict = OreDictionary.getOres("dustRedstone", false);
        if (oreDict.size() > 0) {
            RecipeHandler.addEnrichmentChamberRecipe(ItemStackIngredient.from("oreRedstone"), StackUtils.size(oreDict.get(0), 12));
        }

        RecipeHandler.addEnrichmentChamberRecipe(ItemStackIngredient.from("oreCoal"), new ItemStack(Items.COAL, 2));

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
                RecipeHandler.addCombinerRecipe(ItemStackIngredient.from("dustApatite", 6), ItemStackIngredient.from("cobblestone"), oreApatite);
            } else {
                RecipeHandler.addCombinerRecipe(ItemStackIngredient.from("gemApatite", 6), ItemStackIngredient.from("cobblestone"), oreApatite);
            }
        }

        oreDict = OreDictionary.getOres("gemApatite", false);
        if (oreDict.size() > 0) {
            RecipeHandler.addEnrichmentChamberRecipe(ItemStackIngredient.from("oreApatite"), StackUtils.size(oreDict.get(0), 4));
        }

        InfuseType tinInfuseType = Objects.requireNonNull(InfuseRegistry.get("TIN"));
        RecipeHandler.addMetallurgicInfuserRecipe(InfusionIngredient.from(tinInfuseType, 10), ItemStackIngredient.from("ingotCopper", 3),
              new ItemStack(MekanismItems.Ingot, 4, 2));

        RecipeHandler.addCrusherRecipe(ItemStackIngredient.from("ingotRefinedObsidian"), new ItemStack(MekanismItems.OtherDust, 1, 5));

        RecipeHandler.addMetallurgicInfuserRecipe(InfusionIngredient.from(InfuseRegistry.get("REDSTONE"), 10), ItemStackIngredient.from("ingotOsmium"),
              new ItemStack(MekanismItems.ControlCircuit, 1, 0));

        RecipeHandler.addCrusherRecipe(ItemStackIngredient.from("ingotRedstone"), new ItemStack(Items.REDSTONE));
        RecipeHandler.addCrusherRecipe(ItemStackIngredient.from("ingotRefinedGlowstone"), new ItemStack(Items.GLOWSTONE_DUST));

        oreDict = OreDictionary.getOres("dustBronze", false);
        if (oreDict.size() > 0) {
            RecipeHandler.addCrusherRecipe(ItemStackIngredient.from("ingotBronze"), StackUtils.size(oreDict.get(0), 1));
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
            RecipeHandler.addCrusherRecipe(ItemStackIngredient.from(Items.COAL), StackUtils.size(oreDict.get(0), 1));
            ItemStackIngredient dustCoal = ItemStackIngredient.from("dustCoal");
            RecipeHandler.addEnrichmentChamberRecipe(dustCoal, new ItemStack(Items.COAL));
            RecipeHandler.addPRCRecipe(dustCoal, FluidStackIngredient.from(FluidRegistry.WATER, 100),
                  GasStackIngredient.from(MekanismFluids.Oxygen, 100), new ItemStack(MekanismItems.OtherDust, 1, 3),
                  MekanismFluids.Hydrogen, 100, 0, 100);
        }

        oreDict = OreDictionary.getOres("dustCharcoal", false);
        if (oreDict.size() > 0) {
            RecipeHandler.addCrusherRecipe(ItemStackIngredient.from(new ItemStack(Items.COAL, 1, 1)), StackUtils.size(oreDict.get(0), 1));
            ItemStackIngredient dustCharcoal = ItemStackIngredient.from("dustCharcoal");
            RecipeHandler.addEnrichmentChamberRecipe(dustCharcoal, new ItemStack(Items.COAL, 1, 1));
            RecipeHandler.addPRCRecipe(dustCharcoal, FluidStackIngredient.from(FluidRegistry.WATER, 100),
                  GasStackIngredient.from(MekanismFluids.Oxygen, 100), new ItemStack(MekanismItems.OtherDust, 1, 3),
                  MekanismFluids.Hydrogen, 100, 0, 100);
        }

        oreDict = OreDictionary.getOres("dustSaltpeter", false);
        if (oreDict.size() > 0) {
            RecipeHandler.addCrusherRecipe(ItemStackIngredient.from("gunpowder"), StackUtils.size(oreDict.get(0), 1));
            RecipeHandler.addEnrichmentChamberRecipe(ItemStackIngredient.from("dustSaltpeter"), new ItemStack(Items.GUNPOWDER));
        }

        oreDict = OreDictionary.getOres("itemSilicon", false);
        if (oreDict.size() > 0) {
            RecipeHandler.addCrusherRecipe(ItemStackIngredient.from("sand"), StackUtils.size(oreDict.get(0), 1));
        }

        RecipeHandler.addCrusherRecipe(ItemStackIngredient.from("ingotSteel"), new ItemStack(MekanismItems.OtherDust, 1, 1));
        RecipeHandler.addChemicalOxidizerRecipe(ItemStackIngredient.from("dustLithium"), MekanismFluids.Lithium, 100);

        InfuseType diamondInfuseType = Objects.requireNonNull(InfuseRegistry.get("DIAMOND"));
        RecipeHandler.addMetallurgicInfuserRecipe(InfusionIngredient.from(diamondInfuseType, 10), ItemStackIngredient.from("dustObsidian"),
              new ItemStack(MekanismItems.OtherDust, 1, 5));
        RecipeHandler.addCombinerRecipe(ItemStackIngredient.from("dustObsidian", 4), ItemStackIngredient.from("cobblestone"), new ItemStack(Blocks.OBSIDIAN));

        InfuseRegistry.registerInfuseObject(new OreIngredient("dustDiamond"), new InfuseObject(diamondInfuseType, 10));

        InfuseRegistry.registerInfuseObject(new OreIngredient("dustTin"), new InfuseObject(tinInfuseType, 10));

        RecipeHandler.addCrusherRecipe(ItemStackIngredient.from("treeSapling"), new ItemStack(MekanismItems.BioFuel, 2));

        RecipeHandler.addPRCRecipe(ItemStackIngredient.from("blockCoal"), FluidStackIngredient.from(FluidRegistry.WATER, 1000),
              GasStackIngredient.from(MekanismFluids.Oxygen, 1000), new ItemStack(MekanismItems.OtherDust, 9, 3),
              MekanismFluids.Hydrogen, 1000, 0, 900);
        RecipeHandler.addPRCRecipe(ItemStackIngredient.from("blockCharcoal"), FluidStackIngredient.from(FluidRegistry.WATER, 1000),
              GasStackIngredient.from(MekanismFluids.Oxygen, 1000), new ItemStack(MekanismItems.OtherDust, 9, 3),
              MekanismFluids.Hydrogen, 1000, 0, 900);

        RecipeHandler.addPRCRecipe(ItemStackIngredient.from("dustWood"), FluidStackIngredient.from(FluidRegistry.WATER, 20),
              GasStackIngredient.from(MekanismFluids.Oxygen, 20), ItemStack.EMPTY, MekanismFluids.Hydrogen, 20, 0, 30);
        //TODO: 1.14 evaluate adding a charcoal dust item to Mekanism, and if so use that instead of charcoal here
        RecipeHandler.addEnrichmentChamberRecipe(ItemStackIngredient.from("dustWood", 8), new ItemStack(Items.COAL, 1, 1));
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

        for (ItemStack logEntry : OreDictionary.getOres("logWood", false)) {
            if (logEntry.getItemDamage() == OreDictionary.WILDCARD_VALUE) {
                for (int j = 0; j < 16; j++) {
                    addSawmillLog(tempCrafting, new ItemStack(logEntry.getItem(), 1, j), dummyWorld);
                }
            } else {
                addSawmillLog(tempCrafting, StackUtils.size(logEntry, 1), dummyWorld);
            }
        }
        RecipeHandler.addPRCRecipe(ItemStackIngredient.from("logWood"), FluidStackIngredient.from(FluidRegistry.WATER, 100),
              GasStackIngredient.from(MekanismFluids.Oxygen, 100), ItemStack.EMPTY, MekanismFluids.Hydrogen, 100, 0, 150);
    }

    private static void addSawmillLog(InventoryCrafting tempCrafting, ItemStack log, DummyWorld world) {
        tempCrafting.setInventorySlotContents(0, log);
        IRecipe matchingRecipe = CraftingManager.findMatchingRecipe(tempCrafting, world);
        ItemStack resultEntry = matchingRecipe != null ? matchingRecipe.getRecipeOutput() : ItemStack.EMPTY;

        if (!resultEntry.isEmpty()) {
            //TODO: Figure out if this should be using the "logWood" entry? It probably shouldn't due to the fact that would include multiple wood types
            RecipeHandler.addPrecisionSawmillRecipe(ItemStackIngredient.from(log), StackUtils.size(resultEntry, 6), new ItemStack(MekanismItems.Sawdust),
                  MekanismConfig.current().general.sawdustChanceLog.val());
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
            RecipeHandler.addCombinerRecipe(ItemStackIngredient.from("dust" + suffix), ItemStackIngredient.from("cobblestone"), StackUtils.size(ores.get(0), 1));
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
            ItemStackIngredient base = ItemStackIngredient.from("cobblestone");
            if (dusts.size() > 0) {
                RecipeHandler.addCombinerRecipe(ItemStackIngredient.from("dust" + suffix, 3), base, ore);
            } else {
                RecipeHandler.addCombinerRecipe(ItemStackIngredient.from("gem" + suffix, 3), base, ore);
            }
        }
    }
}
