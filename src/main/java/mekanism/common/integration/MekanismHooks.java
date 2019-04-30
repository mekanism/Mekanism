package mekanism.common.integration;

import appeng.api.AEApi;
import appeng.api.definitions.IBlocks;
import appeng.api.definitions.IItems;
import appeng.api.definitions.IMaterials;
import dan200.computercraft.api.ComputerCraftAPI;
import ic2.api.recipe.IRecipeInput;
import ic2.api.recipe.MachineRecipe;
import ic2.api.recipe.Recipes;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import li.cil.oc.api.Driver;
import mekanism.api.transmitters.TransmissionType;
import mekanism.common.Mekanism;
import mekanism.common.MekanismBlocks;
import mekanism.common.MekanismItems;
import mekanism.common.OreDictCache;
import mekanism.common.Resource;
import mekanism.common.block.states.BlockStateTransmitter.TransmitterType;
import mekanism.common.integration.computer.CCPeripheral;
import mekanism.common.integration.computer.OCDriver;
import mekanism.common.integration.crafttweaker.CrafttweakerIntegration;
import mekanism.common.integration.wrenches.Wrenches;
import mekanism.common.recipe.RecipeHandler;
import mekanism.common.recipe.inputs.MachineInput;
import mekanism.common.util.StackUtils;
import net.minecraft.block.Block;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.Optional.Method;
import net.minecraftforge.fml.common.event.FMLInterModComms;
import net.minecraftforge.fml.common.registry.ForgeRegistries;
import net.minecraftforge.oredict.OreDictionary;

/**
 * Hooks for Mekanism. Use to grab items or blocks out of different mods.
 *
 * @author AidanBrady
 */
public final class MekanismHooks {

    public static final String COFH_API_MOD_ID = "cofhapi";
    public static final String IC2_MOD_ID = "ic2";
    public static final String COMPUTERCRAFT_MOD_ID = "computercraft";
    public static final String APPLIED_ENERGISTICS_2_MOD_ID = "appliedenergistics2";
    public static final String TESLA_MOD_ID = "tesla";
    public static final String MCMULTIPART_MOD_ID = "mcmultipart";
    public static final String REDSTONEFLUX_MOD_ID = "redstoneflux";
    public static final String METALLURGY_MOD_ID = "metallurgy";
    public static final String OPENCOMPUTERS_MOD_ID = "opencomputers";
    public static final String GALACTICRAFT_MOD_ID = "Galacticraft API";
    public static final String WAILA_MOD_ID = "Waila";
    public static final String TOP_MOD_ID = "theoneprobe";
    public static final String BUILDCRAFT_MOD_ID = "BuildCraft";
    public static final String CYCLIC_MOD_ID = "cyclicmagic";
    public static final String MYSTICALAGRICULTURE_MOD_ID = "mysticalagriculture";
    public static final String CRAFTTWEAKER_MOD_ID = "crafttweaker";

    public boolean IC2Loaded = false;
    public boolean CCLoaded = false;
    public boolean AE2Loaded = false;
    public boolean TeslaLoaded = false;
    public boolean MCMPLoaded = false;
    public boolean RFLoaded = false;
    public boolean MetallurgyLoaded = false;
    public boolean CyclicLoaded = false;
    public boolean OCLoaded = false;
    public boolean MALoaded = false;
    public boolean CraftTweakerLoaded = false;

    public void hookPreInit() {
        if (Loader.isModLoaded(IC2_MOD_ID)) {
            IC2Loaded = true;
        }
        if (Loader.isModLoaded(COMPUTERCRAFT_MOD_ID)) {
            CCLoaded = true;
        }
        if (Loader.isModLoaded(APPLIED_ENERGISTICS_2_MOD_ID)) {
            AE2Loaded = true;
        }
        if (Loader.isModLoaded(OPENCOMPUTERS_MOD_ID)) {
            OCLoaded = true;
        }
        if (Loader.isModLoaded(TESLA_MOD_ID)) {
            TeslaLoaded = true;
        }
        if (Loader.isModLoaded(MCMULTIPART_MOD_ID)) {
            MCMPLoaded = true;
        }
        if (Loader.isModLoaded(REDSTONEFLUX_MOD_ID)) {
            RFLoaded = true;
        }
        if (Loader.isModLoaded(CYCLIC_MOD_ID)) {
            CyclicLoaded = true;
        }
        if (Loader.isModLoaded(METALLURGY_MOD_ID)) {
            MetallurgyLoaded = true;
        }
        if (Loader.isModLoaded(MYSTICALAGRICULTURE_MOD_ID)) {
            MALoaded = true;
        }
        if (Loader.isModLoaded(CRAFTTWEAKER_MOD_ID)) {
            CraftTweakerLoaded = true;
        }
    }

    public void hookInit() {
        //Integrate with Waila
        FMLInterModComms
              .sendMessage(WAILA_MOD_ID, "register", "mekanism.common.integration.WailaDataProvider.register");

        //Register TOP handler
        FMLInterModComms
              .sendFunctionMessage(TOP_MOD_ID, "getTheOneProbe", "mekanism.common.integration.TOPProvider");
        if (OCLoaded) {
            loadOCDrivers();
        }
        if (AE2Loaded) {
            registerAE2P2P();
        }
    }

    public void hookPostInit() {
        if (IC2Loaded) {
            hookIC2Recipes();
            Mekanism.logger.info("Hooked into IC2 successfully.");
        }
        if (AE2Loaded) {
            registerAE2Recipes();
            Mekanism.logger.info("Hooked into AE2 successfully.");
        }
        if (CCLoaded) {
            loadCCPeripheralProviders();
            Mekanism.logger.info("Hooked into Computer Craft successfully.");
        }
        if (CyclicLoaded) {
            registerCyclicRecipes();
            Mekanism.logger.info("Hooked into Cyclic successfully.");
        }
        if (MetallurgyLoaded) {
            addMetallurgy();
            Mekanism.logger.info("Hooked into Metallurgy successfully.");
        }
        if (MALoaded) {
            registerMysticalAgricultureRecipes();
        }

        if (CraftTweakerLoaded) {
            //CraftTweaker must be ran after all other recipe changes
            CrafttweakerIntegration.registerCommands();
            CrafttweakerIntegration.applyRecipeChanges();
        }

        Wrenches.initialise();
    }

    @Method(modid = MekanismHooks.IC2_MOD_ID)
    private void hookIC2Recipes() {
        for (MachineRecipe<IRecipeInput, Collection<ItemStack>> entry : Recipes.macerator.getRecipes()) {
            if (!entry.getInput().getInputs().isEmpty()) {
                if (!RecipeHandler.Recipe.CRUSHER.containsRecipe(entry.getInput().getInputs().get(0))) {
                    List<String> names = OreDictCache.getOreDictName(entry.getInput().getInputs().get(0));

                    for (String name : names) {
                        if (name.startsWith("ingot") || name.startsWith("crystal")) {
                            RecipeHandler.addCrusherRecipe(entry.getInput().getInputs().get(0),
                                  entry.getOutput().iterator().next());
                            break;
                        }
                    }
                }
            }
        }

        try {
            Recipes.macerator.addRecipe(Recipes.inputFactory.forOreDict("oreOsmium"), null, false,
                  new ItemStack(MekanismItems.Dust, 2, Resource.OSMIUM.ordinal()));
        } catch (Exception ignored) {
        }

        try {
            Recipes.macerator.addRecipe(Recipes.inputFactory.forOreDict("ingotOsmium"), null, false,
                  new ItemStack(MekanismItems.Dust, 1, Resource.OSMIUM.ordinal()));
            Recipes.macerator.addRecipe(Recipes.inputFactory.forOreDict("ingotRefinedObsidian"), null, false,
                  new ItemStack(MekanismItems.OtherDust, 1, 5));
            Recipes.macerator.addRecipe(Recipes.inputFactory.forOreDict("ingotRefinedGlowstone"), null, false,
                  new ItemStack(Items.GLOWSTONE_DUST));
            Recipes.macerator.addRecipe(Recipes.inputFactory.forOreDict("ingotSteel"), null, false,
                  new ItemStack(MekanismItems.OtherDust, 1, 1));
        } catch (Exception ignored) {
        }

        try {
            for (Resource resource : Resource.values()) {
                Recipes.macerator.addRecipe(Recipes.inputFactory.forOreDict("clump" + resource.getName()), null, false,
                      new ItemStack(MekanismItems.DirtyDust, 1, resource.ordinal()));
            }
        } catch (Exception ignored) {
        }
    }

    @Method(modid = COMPUTERCRAFT_MOD_ID)
    private void loadCCPeripheralProviders() {
        try {
            ComputerCraftAPI.registerPeripheralProvider(new CCPeripheral.CCPeripheralProvider());
        } catch (Exception ignored) {
        }
    }

    @Method(modid = OPENCOMPUTERS_MOD_ID)
    private void loadOCDrivers() {
        try {
            Driver.add(new OCDriver());
        } catch (Exception ignored) {
        }
    }

    private void registerCyclicCombinerOreRecipe(String ore, int quantity, ItemStack extra, String outputName) {
        Item outputItem = ForgeRegistries.ITEMS.getValue(new ResourceLocation(CYCLIC_MOD_ID, outputName));
        if (outputItem != null) {
            for (ItemStack stack : OreDictionary.getOres(ore)) {
                RecipeHandler.addCombinerRecipe(StackUtils.size(stack, quantity), extra, new ItemStack(outputItem));
            }
        }
    }

    private void registerCyclicCombinerRecipe(ItemStack input, ItemStack extra, String outputName) {
        Item outputItem = ForgeRegistries.ITEMS.getValue(new ResourceLocation(CYCLIC_MOD_ID, outputName));
        if (outputItem != null) {
            RecipeHandler.addCombinerRecipe(input, extra, new ItemStack(outputItem));
        }
    }

    private void registerCyclicRecipes() {
        ItemStack netherrack = new ItemStack(Item.getItemFromBlock(Blocks.NETHERRACK));
        registerCyclicCombinerRecipe(new ItemStack(Items.REDSTONE, 3), netherrack, "nether_redstone_ore");
        registerCyclicCombinerOreRecipe("dustIron", 8, netherrack, "nether_iron_ore");
        registerCyclicCombinerOreRecipe("dustGold", 8, netherrack, "nether_gold_ore");
        registerCyclicCombinerRecipe(new ItemStack(Items.COAL, 3), netherrack, "nether_coal_ore");
        registerCyclicCombinerRecipe(new ItemStack(Items.DYE, 5, 4), netherrack, "nether_lapis_ore");
        registerCyclicCombinerRecipe(new ItemStack(Items.EMERALD, 3), netherrack, "nether_emerald_ore");
        registerCyclicCombinerOreRecipe("dustDiamond", 3, netherrack, "nether_diamond_ore");

        ItemStack end_stone = new ItemStack(Item.getItemFromBlock(Blocks.END_STONE));
        registerCyclicCombinerRecipe(new ItemStack(Items.REDSTONE, 3), end_stone, "end_redstone_ore");
        registerCyclicCombinerRecipe(new ItemStack(Items.COAL, 3), end_stone, "end_coal_ore");
        registerCyclicCombinerRecipe(new ItemStack(Items.DYE, 5, 4), end_stone, "end_lapis_ore");
        registerCyclicCombinerRecipe(new ItemStack(Items.EMERALD, 3), end_stone, "end_emerald_ore");
        registerCyclicCombinerOreRecipe("dustDiamond", 3, end_stone, "end_diamond_ore");
        registerCyclicCombinerOreRecipe("dustGold", 8, end_stone, "end_gold_ore");
        registerCyclicCombinerOreRecipe("dustIron", 8, end_stone, "end_iron_ore");
    }

    private void registerAE2P2P() {
        for (TransmitterType type : TransmitterType.values()) {
            if (type.getTransmission().equals(TransmissionType.ITEM)) {
                FMLInterModComms.sendMessage(APPLIED_ENERGISTICS_2_MOD_ID, "add-p2p-attunement-item",
                      new ItemStack(MekanismBlocks.Transmitter, 1, type.ordinal()));
            } else if (type.getTransmission().equals(TransmissionType.FLUID)) {
                FMLInterModComms.sendMessage(APPLIED_ENERGISTICS_2_MOD_ID, "add-p2p-attunement-fluid",
                      new ItemStack(MekanismBlocks.Transmitter, 1, type.ordinal()));
            } else if (type.getTransmission().equals(TransmissionType.ENERGY)) {
                FMLInterModComms.sendMessage(APPLIED_ENERGISTICS_2_MOD_ID, "add-p2p-attunement-fe-power",
                      new ItemStack(MekanismBlocks.Transmitter, 1, type.ordinal()));
            }
        }
    }

    private void registerAE2Recipes() {
        try {
            IItems itemApi = AEApi.instance().definitions().items();
            IMaterials materialsApi = AEApi.instance().definitions().materials();
            IBlocks blocksApi = AEApi.instance().definitions().blocks();

            Optional<ItemStack> certusCrystal = materialsApi.certusQuartzCrystal().maybeStack(1);
            Optional<ItemStack> certusDust = materialsApi.certusQuartzDust().maybeStack(1);
            Optional<ItemStack> pureCertus = materialsApi.purifiedCertusQuartzCrystal().maybeStack(1);
            Optional<ItemStack> chargedCrystal = materialsApi.certusQuartzCrystalCharged().maybeStack(1);
            Optional<ItemStack> fluixCrystal = materialsApi.fluixCrystal().maybeStack(1);
            Optional<ItemStack> pureFluix = materialsApi.purifiedFluixCrystal().maybeStack(1);
            Optional<ItemStack> fluixDust = materialsApi.fluixDust().maybeStack(1);
            Optional<ItemStack> certusOre = blocksApi.quartzOre().maybeStack(1);
            Optional<ItemStack> chargedOre = blocksApi.quartzOreCharged().maybeStack(1);
            Optional<Item> crystalSeed = itemApi.crystalSeed().maybeItem();
            Optional<ItemStack> pureNether = materialsApi.purifiedNetherQuartzCrystal().maybeStack(1);

            if (certusCrystal.isPresent() && certusDust.isPresent()) {
                RecipeHandler.addCrusherRecipe(certusCrystal.get().copy(), certusDust.get().copy());
            }

            if (chargedCrystal.isPresent() && certusDust.isPresent()) {
                RecipeHandler.addCrusherRecipe(chargedCrystal.get().copy(), certusDust.get().copy());
            }

            if (fluixCrystal.isPresent() && fluixDust.isPresent()) {
                RecipeHandler.addCrusherRecipe(fluixCrystal.get().copy(), fluixDust.get().copy());
            }

            if (certusOre.isPresent() && certusCrystal.isPresent()) {
                ItemStack crystalOut = certusCrystal.get().copy();
                crystalOut.setCount(4);
                RecipeHandler.addEnrichmentChamberRecipe(certusOre.get().copy(), crystalOut);
            }

            if (chargedOre.isPresent() && chargedCrystal.isPresent()) {
                ItemStack crystalOut = chargedCrystal.get().copy();
                crystalOut.setCount(4);
                RecipeHandler.addEnrichmentChamberRecipe(chargedOre.get().copy(), crystalOut);
            }

            if (certusDust.isPresent() && pureCertus.isPresent()) {
                ItemStack crystalOut = pureCertus.get().copy();
                RecipeHandler.addEnrichmentChamberRecipe(certusDust.get().copy(), crystalOut);
            }

            if (fluixDust.isPresent() && pureFluix.isPresent()) {
                ItemStack crystalOut = pureFluix.get().copy();
                RecipeHandler.addEnrichmentChamberRecipe(fluixDust.get().copy(), crystalOut);
            }

            if (fluixCrystal.isPresent() && pureFluix.isPresent()) {
                RecipeHandler.addEnrichmentChamberRecipe(fluixCrystal.get().copy(), pureFluix.get().copy());
            }

            if (certusCrystal.isPresent() && pureCertus.isPresent()) {
                RecipeHandler.addEnrichmentChamberRecipe(certusCrystal.get().copy(), pureCertus.get().copy());
            }

            if (crystalSeed.isPresent()) {
                NonNullList<ItemStack> seeds = NonNullList.create();
                //there appears to be no way to get this via api, so fall back to unloc names
                crystalSeed.get().getSubItems(CreativeTabs.SEARCH, seeds);
                //Crystal seeds use a meta AND NBT to determine growth state, so we need to ignore the NBT, and use the meta which should be fixed on what stage it's at
                MachineInput.addCustomItemMatcher(crystalSeed.get().getClass(),
                      (def, test) -> def.getItem() == test.getItem() && def.getMetadata() == test.getMetadata());
                for (ItemStack stack : seeds) {
                    String unloc = crystalSeed.get().getTranslationKey(stack);
                    if (unloc.endsWith("certus") && pureCertus.isPresent()) {
                        RecipeHandler.addEnrichmentChamberRecipe(stack, pureCertus.get().copy());
                    } else if (unloc.endsWith("nether") && pureNether.isPresent()) {
                        RecipeHandler.addEnrichmentChamberRecipe(stack, pureNether.get().copy());
                    } else if (unloc.endsWith("fluix") && pureFluix.isPresent()) {
                        RecipeHandler.addEnrichmentChamberRecipe(stack, pureFluix.get().copy());
                    }
                }
            }

        } catch (Exception e) {
            Mekanism.logger.error("Something went wrong with ae2 integration", e);
        } catch (IncompatibleClassChangeError e) {
            Mekanism.logger.error("AE2 api has changed unexpectedly", e);
        }
    }

    private void addMetallurgy() {
        OreDictManager.addStandardOredictMetal("Adamantine");
        OreDictManager.addStandardOredictMetal("Alduorite");
        OreDictManager.addStandardOredictMetal("Angmallen");
        OreDictManager.addStandardOredictMetal("AstralSilver");
        OreDictManager.addStandardOredictMetal("Atlarus");
        OreDictManager.addStandardOredictMetal("Amordrine");
        OreDictManager.addStandardOredictMetal("BlackSteel");
        OreDictManager.addStandardOredictMetal("Brass");
        OreDictManager.addStandardOredictMetal("Bronze");
        OreDictManager.addStandardOredictMetal("Carmot");
        OreDictManager.addStandardOredictMetal("Celenegil");
        OreDictManager.addStandardOredictMetal("Ceruclase");
        OreDictManager.addStandardOredictMetal("DamascusSteel");
        OreDictManager.addStandardOredictMetal("DeepIron");
        OreDictManager.addStandardOredictMetal("Desichalkos");
        OreDictManager.addStandardOredictMetal("Electrum");
        OreDictManager.addStandardOredictMetal("Eximite");
        OreDictManager.addStandardOredictMetal("Haderoth");
        OreDictManager.addStandardOredictMetal("Hepatizon");
        OreDictManager.addStandardOredictMetal("Ignatius");
        OreDictManager.addStandardOredictMetal("Infuscolium");
        OreDictManager.addStandardOredictMetal("Inolashite");
        OreDictManager.addStandardOredictMetal("Kalendrite");
        OreDictManager.addStandardOredictMetal("Lemurite");
        OreDictManager.addStandardOredictMetal("Manganese");
        OreDictManager.addStandardOredictMetal("Meutoite");
        OreDictManager.addStandardOredictMetal("Midasium");
        OreDictManager.addStandardOredictMetal("Mithril");
        OreDictManager.addStandardOredictMetal("Orichalcum");
        OreDictManager.addStandardOredictMetal("Oureclase");
        OreDictManager.addStandardOredictMetal("Prometheum");
        OreDictManager.addStandardOredictMetal("Quicksilver");
        OreDictManager.addStandardOredictMetal("Rubracium");
        OreDictManager.addStandardOredictMetal("Sanguinite");
        OreDictManager.addStandardOredictMetal("ShadowIron");
        OreDictManager.addStandardOredictMetal("ShadowSteel");
        OreDictManager.addStandardOredictMetal("Tartarite");
        OreDictManager.addStandardOredictMetal("Vulcanite");
        OreDictManager.addStandardOredictMetal("Vyroxeres");
        OreDictManager.addStandardOredictMetal("Zinc");
    }

    private void registerMARecipeSet(MAOre ore, MAOreType type) {
        String oreName = type.orePrefix + ore.name().toLowerCase() + "_ore";
        Item oreItem = ForgeRegistries.ITEMS.getValue(new ResourceLocation(MYSTICALAGRICULTURE_MOD_ID, oreName));
        Item dropItem = ForgeRegistries.ITEMS.getValue(new ResourceLocation(MYSTICALAGRICULTURE_MOD_ID, ore.itemName));
        if (oreItem != null && dropItem != null) {
            RecipeHandler.addEnrichmentChamberRecipe(new ItemStack(oreItem),
                  new ItemStack(dropItem, type.quantity, ore.itemMeta));
            RecipeHandler.addCombinerRecipe(new ItemStack(dropItem, type.quantity + 2, ore.itemMeta),
                  new ItemStack(type.baseBlock), new ItemStack(oreItem));
        }
    }

    private void registerMysticalAgricultureRecipes() {
    	registerMARecipeSet(MAOre.INFERIUM, MAOreType.OVERWORLD);
    	registerMARecipeSet(MAOre.INFERIUM, MAOreType.NETHER);
    	registerMARecipeSet(MAOre.INFERIUM, MAOreType.END);
    	registerMARecipeSet(MAOre.PROSPERITY, MAOreType.OVERWORLD);
    	registerMARecipeSet(MAOre.PROSPERITY, MAOreType.NETHER);
    	registerMARecipeSet(MAOre.PROSPERITY, MAOreType.END);
    }

    private enum MAOre {
        INFERIUM("crafting", 0),
        PROSPERITY("crafting", 5);

        private final String itemName;
        private final int itemMeta;

        private MAOre(String name, int meta) {
            itemName = name;
            itemMeta = meta;
        }
    }

    private enum MAOreType {
        OVERWORLD("", Blocks.COBBLESTONE, 4),
        NETHER("nether_", Blocks.NETHERRACK, 6),
        END("end_", Blocks.END_STONE, 8);

        private final String orePrefix;
        private final Block baseBlock;
        private final int quantity;

        private MAOreType(String prefix, Block base, int quantity) {
            orePrefix = prefix;
            baseBlock = base;
            this.quantity = quantity;
        }
    }
    
}