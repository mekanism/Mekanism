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

        //Iron
        RecipeHandler.addCombinerRecipe(ItemStackIngredient.from(MekanismTags.DUSTS_IRON, 8), ItemStackIngredient.from(Tags.Items.COBBLESTONE),
              new ItemStack(Blocks.IRON_ORE));
        //Gold
        RecipeHandler.addCombinerRecipe(ItemStackIngredient.from(MekanismTags.DUSTS_GOLD, 8), ItemStackIngredient.from(Tags.Items.COBBLESTONE),
              new ItemStack(Blocks.GOLD_ORE));
        //Osmium
        RecipeHandler.addCombinerRecipe(ItemStackIngredient.from(MekanismTags.DUSTS_OSMIUM, 8), ItemStackIngredient.from(Tags.Items.COBBLESTONE),
              MekanismBlock.OSMIUM_ORE.getItemStack());
        //Copper
        RecipeHandler.addCombinerRecipe(ItemStackIngredient.from(MekanismTags.DUSTS_COPPER, 8), ItemStackIngredient.from(Tags.Items.COBBLESTONE),
              MekanismBlock.COPPER_ORE.getItemStack());
        //Tin
        RecipeHandler.addCombinerRecipe(ItemStackIngredient.from(MekanismTags.DUSTS_TIN, 8), ItemStackIngredient.from(Tags.Items.COBBLESTONE),
              MekanismBlock.TIN_ORE.getItemStack());

        RecipeHandler.addCombinerRecipe(ItemStackIngredient.from(MekanismTags.DUSTS_DIAMOND, 3), ItemStackIngredient.from(Tags.Items.COBBLESTONE), new ItemStack(Blocks.DIAMOND_ORE));

        RecipeHandler.addCombinerRecipe(ItemStackIngredient.from(MekanismTags.DUSTS_EMERALD, 3), ItemStackIngredient.from(Tags.Items.COBBLESTONE), new ItemStack(Blocks.EMERALD_ORE));

        //TODO: Re-enable
        /*minorCompatIngot.forEach(OreDictManager::addStandardOredictMetal);
        minorCompatGem.forEach(OreDictManager::addStandardOredictGem);

        oreDict = OreDictionary.getOres("dustYellorium", false);
        if (oreDict.size() > 0) {
            RecipeHandler.addEnrichmentChamberRecipe(ItemStackIngredient.from("oreYellorite"), StackUtils.size(oreDict.get(0), 2));
        }*/

        RecipeHandler.addCombinerRecipe(ItemStackIngredient.from(MekanismTags.DUSTS_QUARTZ, 8), ItemStackIngredient.from(Tags.Items.NETHERRACK),
              new ItemStack(Blocks.NETHER_QUARTZ_ORE));

        RecipeHandler.addCombinerRecipe(ItemStackIngredient.from(MekanismTags.DUSTS_LAPIS_LAZULI, 16), ItemStackIngredient.from(Tags.Items.COBBLESTONE), new ItemStack(Blocks.LAPIS_ORE));

        RecipeHandler.addCombinerRecipe(ItemStackIngredient.from(Tags.Items.DUSTS_REDSTONE, 16), ItemStackIngredient.from(Tags.Items.COBBLESTONE),
              new ItemStack(Blocks.REDSTONE_ORE));

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

        RecipeHandler.addMetallurgicInfuserRecipe(InfusionIngredient.from(MekanismInfuseTypes.REDSTONE, 10), ItemStackIngredient.from(MekanismTags.INGOTS_OSMIUM),
              MekanismItem.BASIC_CONTROL_CIRCUIT.getItemStack());

        //TODO: Re-enable
        //RecipeHandler.addCrusherRecipe(ItemStackIngredient.from("ingotRedstone"), new ItemStack(Items.REDSTONE));

        //TODO: IC2
        /*if (Mekanism.hooks.IC2Loaded) {
            addIC2BronzeRecipe();
        }*/

        RecipeHandler.addPRCRecipe(ItemStackIngredient.from(MekanismTags.DUSTS_COAL), FluidStackIngredient.from(FluidTags.WATER, 100),
              GasStackIngredient.from(MekanismTags.OXYGEN, 100), MekanismItem.SULFUR_DUST.getItemStack(), MekanismGases.HYDROGEN, 100, 0, 100);

        RecipeHandler.addPRCRecipe(ItemStackIngredient.from(MekanismTags.DUSTS_CHARCOAL), FluidStackIngredient.from(FluidTags.WATER, 100),
              GasStackIngredient.from(MekanismTags.OXYGEN, 100), MekanismItem.SULFUR_DUST.getItemStack(), MekanismGases.HYDROGEN, 100, 0, 100);

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
        //TODO: Re-enable/fix this for third party logs
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
        RecipeHandler.addPrecisionSawmillRecipe(ItemStackIngredient.from(ItemTags.OAK_LOGS), new ItemStack(Blocks.OAK_PLANKS, 6), MekanismItem.SAWDUST.getItemStack(),
              MekanismConfig.general.sawdustChanceLog.get());
        RecipeHandler.addPrecisionSawmillRecipe(ItemStackIngredient.from(ItemTags.SPRUCE_LOGS), new ItemStack(Blocks.SPRUCE_PLANKS, 6), MekanismItem.SAWDUST.getItemStack(),
              MekanismConfig.general.sawdustChanceLog.get());
        RecipeHandler.addPrecisionSawmillRecipe(ItemStackIngredient.from(ItemTags.BIRCH_LOGS), new ItemStack(Blocks.BIRCH_PLANKS, 6), MekanismItem.SAWDUST.getItemStack(),
              MekanismConfig.general.sawdustChanceLog.get());
        RecipeHandler.addPrecisionSawmillRecipe(ItemStackIngredient.from(ItemTags.JUNGLE_LOGS), new ItemStack(Blocks.JUNGLE_PLANKS, 6), MekanismItem.SAWDUST.getItemStack(),
              MekanismConfig.general.sawdustChanceLog.get());
        RecipeHandler.addPrecisionSawmillRecipe(ItemStackIngredient.from(ItemTags.ACACIA_LOGS), new ItemStack(Blocks.ACACIA_PLANKS, 6), MekanismItem.SAWDUST.getItemStack(),
              MekanismConfig.general.sawdustChanceLog.get());
        RecipeHandler.addPrecisionSawmillRecipe(ItemStackIngredient.from(ItemTags.DARK_OAK_LOGS), new ItemStack(Blocks.DARK_OAK_PLANKS, 6), MekanismItem.SAWDUST.getItemStack(),
              MekanismConfig.general.sawdustChanceLog.get());
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