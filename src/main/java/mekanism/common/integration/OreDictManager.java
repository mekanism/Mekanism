package mekanism.common.integration;

import ic2.api.recipe.Recipes;
import java.util.Arrays;
import java.util.List;
import javax.annotation.Nonnull;
import mekanism.api.gas.GasRegistry;
import mekanism.api.gas.GasStack;
import mekanism.api.infuse.InfuseObject;
import mekanism.api.infuse.InfuseRegistry;
import mekanism.api.infuse.InfuseType;
import mekanism.common.Mekanism;
import mekanism.common.MekanismFluids;
import mekanism.common.MekanismItem;
import mekanism.common.Resource;
import mekanism.common.config.MekanismConfig;
import mekanism.common.recipe.RecipeHandler;
import mekanism.common.recipe.RecipeHandler.Recipe;
import mekanism.common.util.StackUtils;
import mekanism.common.world.DummyWorld;
import net.minecraft.block.BlockPlanks;
import net.minecraft.block.Blocks;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Items;
import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.CraftingInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.CraftingManager;
import net.minecraft.item.crafting.FurnaceRecipes;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.util.NonNullList;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.Optional.Method;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.oredict.OreDictionary;

@EventBusSubscriber(modid = Mekanism.MODID)
public final class OreDictManager {

    private static final List<String> minorCompatIngot = Arrays.asList("Aluminum", "Draconium", "Iridium", "Mithril", "Nickel", "Platinum", "Uranium");
    private static final List<String> minorCompatGem = Arrays.asList("Amber", "Diamond", "Emerald", "Malachite", "Peridot", "Ruby", "Sapphire", "Tanzanite", "Topaz");

    @SubscribeEvent(priority = EventPriority.LOW)
    public static void init(RegistryEvent.Register<IRecipe> event) {
        addLogRecipes();

        List<ItemStack> oreDict;

        for (ItemStack plank : OreDictionary.getOres("plankWood", false)) {
            plank = StackUtils.size(plank, 1);
            if (!Recipe.PRECISION_SAWMILL.containsRecipe(plank)) {
                RecipeHandler.addPrecisionSawmillRecipe(plank, new ItemStack(Items.STICK, 6), MekanismItem.SAWDUST.getItemStack(), MekanismConfig.current().general.sawdustChancePlank.val());
            }
            RecipeHandler.addPRCRecipe(plank, new FluidStack(FluidRegistry.WATER, 20), new GasStack(MekanismFluids.Oxygen, 20), ItemStack.EMPTY,
                  new GasStack(MekanismFluids.Hydrogen, 20), 0, 30);
        }

        for (ItemStack slab : OreDictionary.getOres("slabWood", false)) {
            slab = StackUtils.size(slab, 1);
            if (!Recipe.PRECISION_SAWMILL.containsRecipe(slab)) {
                RecipeHandler.addPrecisionSawmillRecipe(slab, new ItemStack(Items.STICK, 3), MekanismItem.SAWDUST.getItemStack(),
                      MekanismConfig.current().general.sawdustChancePlank.val() / 2);
            }
            RecipeHandler.addPRCRecipe(slab, new FluidStack(FluidRegistry.WATER, 10), new GasStack(MekanismFluids.Oxygen, 10), ItemStack.EMPTY,
                  new GasStack(MekanismFluids.Hydrogen, 10), 0, 15);
        }

        for (ItemStack stick : OreDictionary.getOres("stickWood", false)) {
            stick = StackUtils.size(stick, 1);
            if (!Recipe.PRECISION_SAWMILL.containsRecipe(stick)) {
                RecipeHandler.addPrecisionSawmillRecipe(stick, MekanismItem.SAWDUST.getItemStack());
            }
            RecipeHandler.addPRCRecipe(stick, new FluidStack(FluidRegistry.WATER, 4), new GasStack(MekanismFluids.Oxygen, 4), ItemStack.EMPTY,
                  new GasStack(MekanismFluids.Hydrogen, 4), 0, 6);
        }

        for (ItemStack ore : OreDictionary.getOres("oreNetherSteel", false)) {
            RecipeHandler.addEnrichmentChamberRecipe(StackUtils.size(ore, 1), MekanismItem.STEEL_DUST.getItemStack(4));
        }

        oreDict = OreDictionary.getOres("itemRubber", false);
        if (oreDict.size() > 0) {
            ItemStack itemRubber = StackUtils.size(oreDict.get(0), 1);
            for (ItemStack rubber : OreDictionary.getOres("woodRubber", false)) {
                RecipeHandler.addPrecisionSawmillRecipe(StackUtils.size(rubber, 1), new ItemStack(Blocks.PLANKS, BlockPlanks.EnumType.JUNGLE.getMetadata(), 4), itemRubber, 1F);
            }
        }

        for (ItemStack sulfur : OreDictionary.getOres("dustSulfur", false)) {
            sulfur = StackUtils.size(sulfur, 1);
            RecipeHandler.addChemicalOxidizerRecipe(sulfur, new GasStack(MekanismFluids.SulfurDioxide, 100));
            RecipeHandler.addEnrichmentChamberRecipe(sulfur, new ItemStack(Items.GUNPOWDER));
        }

        for (ItemStack salt : OreDictionary.getOres("dustSalt", false)) {
            RecipeHandler.addChemicalOxidizerRecipe(StackUtils.size(salt, 1), new GasStack(MekanismFluids.Brine, 15));
        }

        for (ItemStack dust : OreDictionary.getOres("dustRefinedObsidian", false)) {
            dust = StackUtils.size(dust, 1);
            RecipeHandler.addOsmiumCompressorRecipe(dust, MekanismItem.REFINED_OBSIDIAN_INGOT.getItemStack());
            RecipeHandler.addEnrichmentChamberRecipe(dust, MekanismItem.COMPRESSED_OBSIDIAN.getItemStack());
            InfuseRegistry.registerInfuseObject(dust, new InfuseObject(InfuseRegistry.get("OBSIDIAN"), 10));
        }

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
            ItemStack dustYellorium = StackUtils.size(oreDict.get(0), 2);
            for (ItemStack ore : OreDictionary.getOres("oreYellorite", false)) {
                RecipeHandler.addEnrichmentChamberRecipe(StackUtils.size(ore, 1), dustYellorium);
            }
        }

        oreDict = OreDictionary.getOres("dustNetherQuartz", false);
        if (oreDict.size() > 0) {
            ItemStack dustNeterQuartz = StackUtils.size(oreDict.get(0), 1);
            for (ItemStack gem : OreDictionary.getOres("gemQuartz", false)) {
                RecipeHandler.addCrusherRecipe(StackUtils.size(gem, 1), dustNeterQuartz);
            }
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
            ItemStack gemQuartz = StackUtils.size(oreDict.get(0), 1);
            for (ItemStack dust : OreDictionary.getOres("dustNetherQuartz", false)) {
                RecipeHandler.addEnrichmentChamberRecipe(StackUtils.size(dust, 1), gemQuartz);
            }
            gemQuartz = StackUtils.size(gemQuartz, 6);
            for (ItemStack ore : OreDictionary.getOres("oreQuartz", false)) {
                RecipeHandler.addEnrichmentChamberRecipe(StackUtils.size(ore, 1), gemQuartz);
            }
        }

        oreDict = OreDictionary.getOres("dustLapis", false);
        if (oreDict.size() > 0) {
            ItemStack dustLapis = StackUtils.size(oreDict.get(0), 1);
            for (ItemStack gem : OreDictionary.getOres("gemLapis", false)) {
                RecipeHandler.addCrusherRecipe(StackUtils.size(gem, 1), dustLapis);
            }
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

        InfuseType tinInfuseType = InfuseRegistry.get("TIN");
        for (ItemStack ingot : OreDictionary.getOres("ingotCopper", false)) {
            RecipeHandler.addMetallurgicInfuserRecipe(tinInfuseType, 10, StackUtils.size(ingot, 3), MekanismItem.BRONZE_INGOT.getItemStack(4));
        }

        for (ItemStack ingot : OreDictionary.getOres("ingotRefinedObsidian", false)) {
            RecipeHandler.addCrusherRecipe(StackUtils.size(ingot, 1), MekanismItem.REFINED_OBSIDIAN_DUST.getItemStack());
        }

        InfuseType redstoneInfuseType = InfuseRegistry.get("REDSTONE");
        for (ItemStack ingot : OreDictionary.getOres("ingotOsmium", false)) {
            RecipeHandler.addMetallurgicInfuserRecipe(redstoneInfuseType, 10, StackUtils.size(ingot, 1), MekanismItem.BASIC_CONTROL_CIRCUIT.getItemStack());
        }

        for (ItemStack ingot : OreDictionary.getOres("ingotRedstone", false)) {
            RecipeHandler.addCrusherRecipe(StackUtils.size(ingot, 1), new ItemStack(Items.REDSTONE));
        }

        for (ItemStack ingot : OreDictionary.getOres("ingotRefinedGlowstone", false)) {
            RecipeHandler.addCrusherRecipe(StackUtils.size(ingot, 1), new ItemStack(Items.GLOWSTONE_DUST));
        }

        oreDict = OreDictionary.getOres("dustBronze", false);
        if (oreDict.size() > 0) {
            ItemStack dustBronze = StackUtils.size(oreDict.get(0), 1);
            RecipeHandler.addCrusherRecipe(MekanismItem.BRONZE_INGOT.getItemStack(), dustBronze);
        }

        if (Mekanism.hooks.IC2Loaded) {
            addIC2BronzeRecipe();
        }

        oreDict = OreDictionary.getOres("ingotSilver", false);
        if (oreDict.size() > 0) {
            ItemStack ingotSilver = StackUtils.size(oreDict.get(0), 1);
            FurnaceRecipes.instance().addSmeltingRecipe(MekanismItem.SILVER_DUST.getItemStack(), ingotSilver, 0.0F);
        }

        oreDict = OreDictionary.getOres("ingotLead", false);
        if (oreDict.size() > 0) {
            ItemStack ingotLead = StackUtils.size(oreDict.get(0), 1);
            FurnaceRecipes.instance().addSmeltingRecipe(MekanismItem.LEAD_DUST.getItemStack(), ingotLead, 0.0F);
        }

        oreDict = OreDictionary.getOres("dustCoal", false);
        if (oreDict.size() > 0) {
            ItemStack dustCoal = StackUtils.size(oreDict.get(0), 1);
            RecipeHandler.addCrusherRecipe(new ItemStack(Items.COAL), dustCoal);
            for (ItemStack dust : oreDict) {
                dust = StackUtils.size(dust, 1);
                RecipeHandler.addEnrichmentChamberRecipe(dust, new ItemStack(Items.COAL));
                RecipeHandler.addPRCRecipe(dust, new FluidStack(FluidRegistry.WATER, 100), new GasStack(MekanismFluids.Oxygen, 100),
                      MekanismItem.SULFUR_DUST.getItemStack(), new GasStack(MekanismFluids.Hydrogen, 100), 0, 100);
            }
        }

        oreDict = OreDictionary.getOres("dustCharcoal", false);
        if (oreDict.size() > 0) {
            ItemStack dustCharcoal = StackUtils.size(oreDict.get(0), 1);
            RecipeHandler.addCrusherRecipe(new ItemStack(Items.COAL, 1, 1), dustCharcoal);
            for (ItemStack dust : oreDict) {
                dust = StackUtils.size(dust, 1);
                RecipeHandler.addEnrichmentChamberRecipe(dust, new ItemStack(Items.COAL, 1, 1));
                RecipeHandler.addPRCRecipe(dust, new FluidStack(FluidRegistry.WATER, 100), new GasStack(MekanismFluids.Oxygen, 100),
                      MekanismItem.SULFUR_DUST.getItemStack(), new GasStack(MekanismFluids.Hydrogen, 100), 0, 100);
            }
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

        for (ItemStack ingot : OreDictionary.getOres("ingotSteel", false)) {
            RecipeHandler.addCrusherRecipe(StackUtils.size(ingot, 1), MekanismItem.STEEL_DUST.getItemStack());
        }

        for (ItemStack dust : OreDictionary.getOres("dustLithium", false)) {
            RecipeHandler.addChemicalOxidizerRecipe(StackUtils.size(dust, 1), new GasStack(MekanismFluids.Lithium, 100));
        }

        InfuseType diamondInfuseType = InfuseRegistry.get("DIAMOND");
        for (ItemStack dust : OreDictionary.getOres("dustObsidian", false)) {
            RecipeHandler.addCombinerRecipe(StackUtils.size(dust, 4), new ItemStack(Blocks.COBBLESTONE), new ItemStack(Blocks.OBSIDIAN));
            RecipeHandler.addMetallurgicInfuserRecipe(diamondInfuseType, 10, StackUtils.size(dust, 1),
                  MekanismItem.REFINED_OBSIDIAN_DUST.getItemStack());
        }

        for (ItemStack dust : OreDictionary.getOres("dustDiamond", false)) {
            InfuseRegistry.registerInfuseObject(StackUtils.size(dust, 1), new InfuseObject(diamondInfuseType, 10));
        }

        for (ItemStack dust : OreDictionary.getOres("dustTin", false)) {
            InfuseRegistry.registerInfuseObject(StackUtils.size(dust, 1), new InfuseObject(tinInfuseType, 10));
        }

        for (ItemStack sapling : OreDictionary.getOres("treeSapling", false)) {
            if (sapling.getDamage() == 0 || sapling.getDamage() == OreDictionary.WILDCARD_VALUE) {
                RecipeHandler.addCrusherRecipe(new ItemStack(sapling.getItem(), 1, OreDictionary.WILDCARD_VALUE), MekanismItem.BIO_FUEL.getItemStack(2));
            }
        }

        for (ItemStack coal : OreDictionary.getOres("blockCoal", false)) {
            RecipeHandler.addPRCRecipe(StackUtils.size(coal, 1), new FluidStack(FluidRegistry.WATER, 1000), new GasStack(MekanismFluids.Oxygen, 1000),
                  MekanismItem.SULFUR_DUST.getItemStack(9), new GasStack(MekanismFluids.Hydrogen, 1000), 0, 900);
        }

        for (ItemStack coal : OreDictionary.getOres("blockCharcoal", false)) {
            RecipeHandler.addPRCRecipe(StackUtils.size(coal, 1), new FluidStack(FluidRegistry.WATER, 1000), new GasStack(MekanismFluids.Oxygen, 1000),
                  MekanismItem.SULFUR_DUST.getItemStack(9), new GasStack(MekanismFluids.Hydrogen, 1000), 0, 900);
        }

        for (ItemStack sawdust : OreDictionary.getOres("dustWood", false)) {
            //TODO: 1.14 evaluate adding a charcoal dust item to Mekanism, and if so use that instead of charcoal here
            RecipeHandler.addEnrichmentChamberRecipe(StackUtils.size(sawdust, 8), new ItemStack(Items.COAL, 1, 1));
            RecipeHandler.addPRCRecipe(StackUtils.size(sawdust, 1), new FluidStack(FluidRegistry.WATER, 20), new GasStack(MekanismFluids.Oxygen, 20),
                  ItemStack.EMPTY, new GasStack(MekanismFluids.Hydrogen, 20), 0, 30);
        }
    }

    @Method(modid = MekanismHooks.IC2_MOD_ID)
    private static void addIC2BronzeRecipe() {
        Recipes.macerator.addRecipe(Recipes.inputFactory.forOreDict("ingotBronze"), null, false,
              StackUtils.size(OreDictionary.getOres("dustBronze", false).get(0), 1));
    }

    private static void addResourceRecipes(Resource resource, MekanismItem shardItem, MekanismItem clumpItem, MekanismItem dirtyDustItem, MekanismItem dustItem) {
        for (ItemStack clump : OreDictionary.getOres("clump" + resource.getName(), false)) {
            RecipeHandler.addCrusherRecipe(StackUtils.size(clump, 1), dirtyDustItem.getItemStack());
        }

        for (ItemStack shard : OreDictionary.getOres("shard" + resource.getName(), false)) {
            RecipeHandler.addPurificationChamberRecipe(StackUtils.size(shard, 1), clumpItem.getItemStack());
        }

        for (ItemStack crystal : OreDictionary.getOres("crystal" + resource.getName(), false)) {
            RecipeHandler.addChemicalInjectionChamberRecipe(StackUtils.size(crystal, 1), MekanismFluids.HydrogenChloride, shardItem.getItemStack());
        }

        for (ItemStack dust : OreDictionary.getOres("dustDirty" + resource.getName(), false)) {
            RecipeHandler.addEnrichmentChamberRecipe(StackUtils.size(dust, 1), dustItem.getItemStack());
        }

        for (ItemStack ore : OreDictionary.getOres("ore" + resource.getName(), false)) {
            RecipeHandler.addEnrichmentChamberRecipe(StackUtils.size(ore, 1), dustItem.getItemStack(2));
            RecipeHandler.addPurificationChamberRecipe(StackUtils.size(ore, 1), clumpItem.getItemStack(3));
            RecipeHandler.addChemicalInjectionChamberRecipe(StackUtils.size(ore, 1), MekanismFluids.HydrogenChloride, shardItem.getItemStack(4));
            RecipeHandler.addChemicalDissolutionChamberRecipe(StackUtils.size(ore, 1), new GasStack(GasRegistry.getGas(resource.getName()), 1000));
        }

        for (ItemStack ingot : OreDictionary.getOres("ingot" + resource.getName(), false)) {
            RecipeHandler.addCrusherRecipe(StackUtils.size(ingot, 1), dustItem.getItemStack());
        }

        List<ItemStack> oreDict = OreDictionary.getOres("ore" + resource.getName(), false);
        if (oreDict.size() > 0) {
            ItemStack ore = StackUtils.size(oreDict.get(0), 1);
            for (ItemStack dust : OreDictionary.getOres("dust" + resource.getName(), false)) {
                RecipeHandler.addCombinerRecipe(StackUtils.size(dust, 8), new ItemStack(Blocks.COBBLESTONE), ore);
            }
        }
    }

    /**
     * Handy method for retrieving all log items, finding their corresponding planks, and making recipes with them. Credit to CofhCore.
     */
    private static void addLogRecipes() {
        Container tempContainer = new Container() {
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
            if (logEntry.getDamage() == OreDictionary.WILDCARD_VALUE) {
                for (int j = 0; j < 16; j++) {
                    addSawmillLog(tempCrafting, new ItemStack(logEntry.getItem(), 1, j), dummyWorld);
                }
            } else {
                addSawmillLog(tempCrafting, StackUtils.size(logEntry, 1), dummyWorld);
            }
            RecipeHandler.addPRCRecipe(StackUtils.size(logEntry, 1), new FluidStack(FluidRegistry.WATER, 100), new GasStack(MekanismFluids.Oxygen, 100), ItemStack.EMPTY,
                  new GasStack(MekanismFluids.Hydrogen, 100), 0, 150);
        }
    }

    private static void addSawmillLog(CraftingInventory tempCrafting, ItemStack log, DummyWorld world) {
        tempCrafting.setInventorySlotContents(0, log);
        IRecipe matchingRecipe = CraftingManager.findMatchingRecipe(tempCrafting, world);
        ItemStack resultEntry = matchingRecipe != null ? matchingRecipe.getRecipeOutput() : ItemStack.EMPTY;

        if (!resultEntry.isEmpty()) {
            RecipeHandler.addPrecisionSawmillRecipe(log, StackUtils.size(resultEntry, 6), MekanismItem.SAWDUST.getItemStack(),
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
