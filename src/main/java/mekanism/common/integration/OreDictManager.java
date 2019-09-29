package mekanism.common.integration;

import java.util.Arrays;
import java.util.List;
import mekanism.api.recipes.inputs.ItemStackIngredient;
import mekanism.common.MekanismItem;
import mekanism.common.config.MekanismConfig;
import mekanism.common.recipe.RecipeHandler;
import net.minecraft.block.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.tags.ItemTags;
import net.minecraftforge.common.Tags;

public final class OreDictManager {

    private static final List<String> minorCompatIngot = Arrays.asList("Aluminum", "Draconium", "Iridium", "Mithril", "Nickel", "Platinum", "Uranium");
    private static final List<String> minorCompatGem = Arrays.asList("Amber", "Malachite", "Peridot", "Ruby", "Sapphire", "Tanzanite", "Topaz");

    public static void init() {
        addLogRecipes();

        RecipeHandler.addPrecisionSawmillRecipe(ItemStackIngredient.from(ItemTags.PLANKS), new ItemStack(Items.STICK, 6),
              MekanismItem.SAWDUST.getItemStack(), MekanismConfig.general.sawdustChancePlank.get());

        RecipeHandler.addPrecisionSawmillRecipe(ItemStackIngredient.from(ItemTags.WOODEN_SLABS), new ItemStack(Items.STICK, 3), MekanismItem.SAWDUST.getItemStack(),
              MekanismConfig.general.sawdustChancePlank.get() / 2);

        RecipeHandler.addPrecisionSawmillRecipe(ItemStackIngredient.from(Tags.Items.RODS_WOODEN), MekanismItem.SAWDUST.getItemStack());

        //TODO: Re-enable
        /*RecipeHandler.addEnrichmentChamberRecipe(ItemStackIngredient.from("oreNetherSteel"), MekanismItem.STEEL_DUST.getItemStack(4));

        oreDict = OreDictionary.getOres("itemRubber", false);
        if (oreDict.size() > 0) {
            RecipeHandler.addPrecisionSawmillRecipe(ItemStackIngredient.from("woodRubber"), new ItemStack(Blocks.JUNGLE_PLANKS, 4),
                  StackUtils.size(oreDict.get(0), 1), 1F);
        }

        minorCompatIngot.forEach(OreDictManager::addStandardOredictMetal);
        minorCompatGem.forEach(OreDictManager::addStandardOredictGem);

        oreDict = OreDictionary.getOres("dustYellorium", false);
        if (oreDict.size() > 0) {
            RecipeHandler.addEnrichmentChamberRecipe(ItemStackIngredient.from("oreYellorite"), StackUtils.size(oreDict.get(0), 2));
        }

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

        RecipeHandler.addCrusherRecipe(ItemStackIngredient.from("ingotRedstone"), new ItemStack(Items.REDSTONE));

        oreDict = OreDictionary.getOres("dustSaltpeter", false);
        if (oreDict.size() > 0) {
            RecipeHandler.addCrusherRecipe(ItemStackIngredient.from(Tags.Items.GUNPOWDER), StackUtils.size(oreDict.get(0), 1));
            RecipeHandler.addEnrichmentChamberRecipe(ItemStackIngredient.from("dustSaltpeter"), new ItemStack(Items.GUNPOWDER));
        }

        oreDict = OreDictionary.getOres("itemSilicon", false);
        if (oreDict.size() > 0) {
            RecipeHandler.addCrusherRecipe(ItemStackIngredient.from(Tags.Items.SAND), StackUtils.size(oreDict.get(0), 1));
        }*/

        //TODO: IC2
        /*if (Mekanism.hooks.IC2Loaded) {
            addIC2BronzeRecipe();
        }*/
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