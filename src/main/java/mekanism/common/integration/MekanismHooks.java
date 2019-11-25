package mekanism.common.integration;

import mekanism.common.integration.crafttweaker.CrafttweakerIntegration;
import mekanism.common.integration.wrenches.Wrenches;
import net.minecraftforge.fml.ModList;

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
    public static final String MCMULTIPART_MOD_ID = "mcmultipart";
    public static final String METALLURGY_MOD_ID = "metallurgy";
    public static final String OPENCOMPUTERS_MOD_ID = "opencomputers";
    public static final String GALACTICRAFT_MOD_ID = "Galacticraft API";
    public static final String WAILA_MOD_ID = "Waila";
    public static final String TOP_MOD_ID = "theoneprobe";
    public static final String BUILDCRAFT_MOD_ID = "buildcraftcore";
    public static final String CYCLIC_MOD_ID = "cyclicmagic";
    public static final String MYSTICALAGRICULTURE_MOD_ID = "mysticalagriculture";
    public static final String CRAFTTWEAKER_MOD_ID = "crafttweaker";

    public boolean AE2Loaded = false;
    public boolean BuildCraftLoaded = false;
    public boolean CCLoaded = false;
    public boolean CraftTweakerLoaded = false;
    public boolean CyclicLoaded = false;
    public boolean IC2Loaded = false;
    public boolean MALoaded = false;
    public boolean MCMPLoaded = false;
    public boolean MetallurgyLoaded = false;
    public boolean OCLoaded = false;

    public void hookPreInit() {
        ModList modList = ModList.get();
        AE2Loaded = modList.isLoaded(APPLIED_ENERGISTICS_2_MOD_ID);
        BuildCraftLoaded = modList.isLoaded(BUILDCRAFT_MOD_ID);
        CCLoaded = modList.isLoaded(COMPUTERCRAFT_MOD_ID);
        CraftTweakerLoaded = modList.isLoaded(CRAFTTWEAKER_MOD_ID);
        CyclicLoaded = modList.isLoaded(CYCLIC_MOD_ID);
        IC2Loaded = modList.isLoaded(IC2_MOD_ID);
        MCMPLoaded = modList.isLoaded(MCMULTIPART_MOD_ID);
        MetallurgyLoaded = modList.isLoaded(METALLURGY_MOD_ID);
        MALoaded = modList.isLoaded(MYSTICALAGRICULTURE_MOD_ID);
        OCLoaded = modList.isLoaded(OPENCOMPUTERS_MOD_ID);
    }

    public void hookCommonSetup() {
        //TODO
        //Integrate with Waila
        /*FMLInterModComms.sendMessage(WAILA_MOD_ID, "register", "mekanism.common.integration.WailaDataProvider.register");

        //Register TOP handler
        FMLInterModComms.sendFunctionMessage(TOP_MOD_ID, "getTheOneProbe", "mekanism.common.integration.TOPProvider");
        if (OCLoaded) {
            loadOCDrivers();
        }
        if (AE2Loaded) {
            registerAE2P2P();
        }
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
        }*/
        if (CraftTweakerLoaded) {
            //CraftTweaker must be ran after all other recipe changes
            CrafttweakerIntegration.registerCommands();
            CrafttweakerIntegration.applyRecipeChanges();
        }
        Wrenches.initialise();
    }

    //TODO: ComputerCraft
    /*@Method(modid = COMPUTERCRAFT_MOD_ID)
    private void loadCCPeripheralProviders() {
        try {
            ComputerCraftAPI.registerPeripheralProvider(new CCPeripheral.CCPeripheralProvider());
        } catch (Exception ignored) {
        }
    }*/

    //TODO: OpenComputers
    /*@Method(modid = OPENCOMPUTERS_MOD_ID)
    private void loadOCDrivers() {
        try {
            Driver.add(new OCDriver());
        } catch (Exception ignored) {
        }
    }*/

    //TODO: Cyclic
    /*private void registerCyclicCombinerRecipe(ItemStackIngredient input, ItemStackIngredient extra, String outputName) {
        Item outputItem = ForgeRegistries.ITEMS.getValue(new ResourceLocation(CYCLIC_MOD_ID, outputName));
        if (outputItem != null) {
            RecipeHandler.addCombinerRecipe(input, extra, new ItemStack(outputItem));
        }
    }

    private void registerCyclicRecipes() {
        ItemStackIngredient netherrack = ItemStackIngredient.from("netherrack");
        registerCyclicCombinerRecipe(ItemStackIngredient.from("dustRedstone", 3), netherrack, "nether_redstone_ore");
        registerCyclicCombinerRecipe(ItemStackIngredient.from("dustIron", 8), netherrack, "nether_iron_ore");
        registerCyclicCombinerRecipe(ItemStackIngredient.from("dustGold", 8), netherrack, "nether_gold_ore");
        registerCyclicCombinerRecipe(ItemStackIngredient.from(Items.COAL, 3), netherrack, "nether_coal_ore");
        registerCyclicCombinerRecipe(ItemStackIngredient.from("gemLapis", 5), netherrack, "nether_lapis_ore");
        registerCyclicCombinerRecipe(ItemStackIngredient.from("gemEmerald", 3), netherrack, "nether_emerald_ore");
        registerCyclicCombinerRecipe(ItemStackIngredient.from("dustDiamond", 3), netherrack, "nether_diamond_ore");

        ItemStackIngredient end_stone = ItemStackIngredient.from("endstone");
        registerCyclicCombinerRecipe(ItemStackIngredient.from("dustRedstone", 3), end_stone, "end_redstone_ore");
        registerCyclicCombinerRecipe(ItemStackIngredient.from(Items.COAL, 3), end_stone, "end_coal_ore");
        registerCyclicCombinerRecipe(ItemStackIngredient.from("gemLapis", 5), end_stone, "end_lapis_ore");
        registerCyclicCombinerRecipe(ItemStackIngredient.from("gemEmerald", 3), end_stone, "end_emerald_ore");
        registerCyclicCombinerRecipe(ItemStackIngredient.from("dustDiamond", 3), end_stone, "end_diamond_ore");
        registerCyclicCombinerRecipe(ItemStackIngredient.from("dustGold", 8), end_stone, "end_gold_ore");
        registerCyclicCombinerRecipe(ItemStackIngredient.from("dustIron", 8), end_stone, "end_iron_ore");
    }*/

    //TODO: AE2
    /*private void registerAE2P2P() {
        //ITEMS
        FMLInterModComms.sendMessage(APPLIED_ENERGISTICS_2_MOD_ID, "add-p2p-attunement-item", MekanismBlock.BASIC_LOGISTICAL_TRANSPORTER.getItemStack());
        FMLInterModComms.sendMessage(APPLIED_ENERGISTICS_2_MOD_ID, "add-p2p-attunement-item", MekanismBlock.ADVANCED_LOGISTICAL_TRANSPORTER.getItemStack());
        FMLInterModComms.sendMessage(APPLIED_ENERGISTICS_2_MOD_ID, "add-p2p-attunement-item", MekanismBlock.ELITE_LOGISTICAL_TRANSPORTER.getItemStack());
        FMLInterModComms.sendMessage(APPLIED_ENERGISTICS_2_MOD_ID, "add-p2p-attunement-item", MekanismBlock.ULTIMATE_LOGISTICAL_TRANSPORTER.getItemStack());
        FMLInterModComms.sendMessage(APPLIED_ENERGISTICS_2_MOD_ID, "add-p2p-attunement-item", MekanismBlock.RESTRICTIVE_TRANSPORTER.getItemStack());
        FMLInterModComms.sendMessage(APPLIED_ENERGISTICS_2_MOD_ID, "add-p2p-attunement-item", MekanismBlock.DIVERSION_TRANSPORTER.getItemStack());

        //FLUID
        FMLInterModComms.sendMessage(APPLIED_ENERGISTICS_2_MOD_ID, "add-p2p-attunement-fluid", MekanismBlock.BASIC_MECHANICAL_PIPE.getItemStack());
        FMLInterModComms.sendMessage(APPLIED_ENERGISTICS_2_MOD_ID, "add-p2p-attunement-fluid", MekanismBlock.ADVANCED_MECHANICAL_PIPE.getItemStack());
        FMLInterModComms.sendMessage(APPLIED_ENERGISTICS_2_MOD_ID, "add-p2p-attunement-fluid", MekanismBlock.ELITE_MECHANICAL_PIPE.getItemStack());
        FMLInterModComms.sendMessage(APPLIED_ENERGISTICS_2_MOD_ID, "add-p2p-attunement-fluid", MekanismBlock.ULTIMATE_MECHANICAL_PIPE.getItemStack());

        //ENERGY
        FMLInterModComms.sendMessage(APPLIED_ENERGISTICS_2_MOD_ID, "add-p2p-attunement-fe-power", MekanismBlock.BASIC_UNIVERSAL_CABLE.getItemStack());
        FMLInterModComms.sendMessage(APPLIED_ENERGISTICS_2_MOD_ID, "add-p2p-attunement-fe-power", MekanismBlock.ADVANCED_UNIVERSAL_CABLE.getItemStack());
        FMLInterModComms.sendMessage(APPLIED_ENERGISTICS_2_MOD_ID, "add-p2p-attunement-fe-power", MekanismBlock.ELITE_UNIVERSAL_CABLE.getItemStack());
        FMLInterModComms.sendMessage(APPLIED_ENERGISTICS_2_MOD_ID, "add-p2p-attunement-fe-power", MekanismBlock.ULTIMATE_UNIVERSAL_CABLE.getItemStack());
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
            Optional<ItemStack> pureFluix = materialsApi.purifiedFluixCrystal().maybeStack(1);
            Optional<ItemStack> fluixDust = materialsApi.fluixDust().maybeStack(1);
            Optional<ItemStack> certusOre = blocksApi.quartzOre().maybeStack(1);
            Optional<Item> crystalSeed = itemApi.crystalSeed().maybeItem();
            Optional<ItemStack> pureNether = materialsApi.purifiedNetherQuartzCrystal().maybeStack(1);

            //TODO: Change as much of this as possible to using ore dict names
            certusDust.ifPresent(stack -> RecipeHandler.addCrusherRecipe(ItemStackIngredient.from("crystalCertusQuartz"), stack.copy()));

            if (chargedCrystal.isPresent() && certusDust.isPresent()) {
                RecipeHandler.addCrusherRecipe(ItemStackIngredient.from(chargedCrystal.get().copy()), certusDust.get().copy());
            }

            fluixDust.ifPresent(stack -> RecipeHandler.addCrusherRecipe(ItemStackIngredient.from("crystalFluix"), stack.copy()));

            if (certusOre.isPresent() && certusCrystal.isPresent()) {
                //cannot use oreCertusQuartz as charged certus ore is also in that entry
                RecipeHandler.addEnrichmentChamberRecipe(ItemStackIngredient.from(certusOre.get().copy()), StackUtils.size(certusCrystal.get(), 4));
            }

            chargedCrystal.ifPresent(stack -> RecipeHandler.addEnrichmentChamberRecipe(ItemStackIngredient.from("oreChargedCertusQuartz"), StackUtils.size(stack, 4)));

            pureCertus.ifPresent(stack -> {
                RecipeHandler.addEnrichmentChamberRecipe(ItemStackIngredient.from("dustCertusQuartz"), stack.copy());
                RecipeHandler.addEnrichmentChamberRecipe(ItemStackIngredient.from("crystalCertusQuartz"), stack.copy());
            });

            pureFluix.ifPresent(stack -> {
                RecipeHandler.addEnrichmentChamberRecipe(ItemStackIngredient.from("dustFluix"), stack.copy());
                RecipeHandler.addEnrichmentChamberRecipe(ItemStackIngredient.from("crystalFluix"), stack.copy());
            });

            if (crystalSeed.isPresent()) {
                NonNullList<ItemStack> seeds = NonNullList.create();
                //there appears to be no way to get this via api, so fall back to unloc names
                crystalSeed.get().fillItemGroup(ItemGroup.SEARCH, seeds);
                //Crystal seeds use a meta AND NBT to determine growth state, so we need to ignore the NBT, and use the meta which should be fixed on what stage it's at
                // Because we want to ignore it we forge pass an Ingredient instance that ignores NBT, as ItemStackIngredient.from(ItemStack) defaults to an ingredient
                // that supports matching NBT
                for (ItemStack stack : seeds) {
                    String unloc = crystalSeed.get().getTranslationKey(stack);
                    if (unloc.endsWith("certus") && pureCertus.isPresent()) {
                        RecipeHandler.addEnrichmentChamberRecipe(ItemStackIngredient.from(Ingredient.fromStacks(stack)), pureCertus.get().copy());
                    } else if (unloc.endsWith("nether") && pureNether.isPresent()) {
                        RecipeHandler.addEnrichmentChamberRecipe(ItemStackIngredient.from(Ingredient.fromStacks(stack)), pureNether.get().copy());
                    } else if (unloc.endsWith("fluix") && pureFluix.isPresent()) {
                        RecipeHandler.addEnrichmentChamberRecipe(ItemStackIngredient.from(Ingredient.fromStacks(stack)), pureFluix.get().copy());
                    }
                }
            }
        } catch (Exception e) {
            Mekanism.logger.error("Something went wrong with ae2 integration", e);
        } catch (IncompatibleClassChangeError e) {
            Mekanism.logger.error("AE2 api has changed unexpectedly", e);
        }
    }*/

    //TODO: Metallurgy
    /*private void addMetallurgy() {
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
    }*/

    //TODO: Mystical Agriculture
    /*private void registerMARecipeSet(MAOre ore, MAOreType type) {
        String oreName = type.orePrefix + ore.name().toLowerCase() + "_ore";
        Item oreItem = ForgeRegistries.ITEMS.getValue(new ResourceLocation(MYSTICALAGRICULTURE_MOD_ID, oreName));
        Item dropItem = ForgeRegistries.ITEMS.getValue(new ResourceLocation(MYSTICALAGRICULTURE_MOD_ID, ore.itemName));
        if (oreItem != null && dropItem != null) {
            RecipeHandler.addEnrichmentChamberRecipe(ItemStackIngredient.from(oreItem), new ItemStack(dropItem, type.quantity, ore.itemMeta));
            RecipeHandler.addCombinerRecipe(ItemStackIngredient.from(new ItemStack(dropItem, type.quantity + 2, ore.itemMeta)),
                  ItemStackIngredient.from(type.baseBlockOre), new ItemStack(oreItem));
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

        MAOre(String name, int meta) {
            itemName = name;
            itemMeta = meta;
        }
    }

    private enum MAOreType {
        OVERWORLD("", "cobblestone", 4),
        NETHER("nether_", "netherrack", 6),
        END("end_", "endstone", 8);

        private final String orePrefix;
        private final String baseBlockOre;
        private final int quantity;

        MAOreType(String prefix, String baseBlockOre, int quantity) {
            orePrefix = prefix;
            this.baseBlockOre = baseBlockOre;
            this.quantity = quantity;
        }
    }*/
}