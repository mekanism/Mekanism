package mekanism.common.recipe.generation;

import mekanism.api.EnumColor;
import mekanism.common.MekanismBlocks;
import mekanism.common.MekanismItems;
import mekanism.common.Tier.BaseTier;
import mekanism.common.Tier.BinTier;
import mekanism.common.Tier.EnergyCubeTier;
import mekanism.common.Tier.FactoryTier;
import mekanism.common.Tier.FluidTankTier;
import mekanism.common.Tier.GasTankTier;
import mekanism.common.Tier.InductionCellTier;
import mekanism.common.Tier.InductionProviderTier;
import mekanism.common.base.IFactory.RecipeType;
import mekanism.common.block.states.BlockStateMachine.MachineType;
import mekanism.common.block.states.BlockStateTransmitter.TransmitterType;
import mekanism.common.util.MekanismUtils;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraftforge.oredict.OreDictionary;

public class MekanismRecipes {
    public static void generate() {
        RecipeGenerator recipeGenerator = new RecipeGenerator("mekanism");

        //Storage Recipes
        recipeGenerator.addShapedRecipe(new ItemStack(MekanismBlocks.BasicBlock, 1, 3), "***", "***", "***", '*', new ItemStack(Items.COAL, 1, 1));
        recipeGenerator.addShapedRecipe(new ItemStack(Items.COAL, 9, 1), "*", '*', new ItemStack(MekanismBlocks.BasicBlock, 1, 3));
        recipeGenerator.addShapedRecipe(new ItemStack(MekanismBlocks.BasicBlock, 1, 2), "***", "***", "***", '*', "ingotRefinedObsidian");
        recipeGenerator.addShapedRecipe(new ItemStack(MekanismItems.Ingot, 9, 0), "*", '*', new ItemStack(MekanismBlocks.BasicBlock, 1, 2));
        recipeGenerator.addShapedRecipe(new ItemStack(MekanismBlocks.BasicBlock, 1, 4), "***", "***", "***", '*', "ingotRefinedGlowstone");
        recipeGenerator.addShapedRecipe(new ItemStack(MekanismItems.Ingot, 9, 3), "*", '*', new ItemStack(MekanismBlocks.BasicBlock, 1, 4));
        recipeGenerator.addShapedRecipe(new ItemStack(MekanismBlocks.BasicBlock, 1, 0), "XXX", "XXX", "XXX", 'X', "ingotOsmium");
        recipeGenerator.addShapedRecipe(new ItemStack(MekanismItems.Ingot, 9, 1), "*", '*', new ItemStack(MekanismBlocks.BasicBlock, 1, 0));
        recipeGenerator.addShapedRecipe(new ItemStack(MekanismBlocks.BasicBlock, 1, 1), "***", "***", "***", '*', "ingotBronze");
        recipeGenerator.addShapedRecipe(new ItemStack(MekanismItems.Ingot, 9, 2), "*", '*', new ItemStack(MekanismBlocks.BasicBlock, 1, 1));
        recipeGenerator.addShapedRecipe(new ItemStack(MekanismBlocks.BasicBlock, 1, 5), "***", "***", "***", '*', "ingotSteel");
        recipeGenerator.addShapedRecipe(new ItemStack(MekanismItems.Ingot, 9, 4), "*", '*', new ItemStack(MekanismBlocks.BasicBlock, 1, 5));
        recipeGenerator.addShapedRecipe(new ItemStack(MekanismBlocks.BasicBlock, 1, 12), "***", "***", "***", '*', "ingotCopper");
        recipeGenerator.addShapedRecipe(new ItemStack(MekanismItems.Ingot, 9, 5), "*", '*', new ItemStack(MekanismBlocks.BasicBlock, 1, 12));
        recipeGenerator.addShapedRecipe(new ItemStack(MekanismBlocks.BasicBlock, 1, 13), "***", "***", "***", '*', "ingotTin");
        recipeGenerator.addShapedRecipe(new ItemStack(MekanismItems.Ingot, 9, 6), "*", '*', new ItemStack(MekanismBlocks.BasicBlock, 1, 13));
        recipeGenerator.addShapedRecipe(new ItemStack(MekanismBlocks.SaltBlock), "**", "**", '*', MekanismItems.Salt);
        recipeGenerator.addShapedRecipe(new ItemStack(MekanismItems.Ingot, 1, 0), "***", "***", "***", '*', "nuggetObsidian");
        recipeGenerator.addShapedRecipe(new ItemStack(MekanismItems.Nugget, 9, 0), "*", '*', "ingotObsidian");
        recipeGenerator.addShapedRecipe(new ItemStack(MekanismItems.Ingot, 1, 1), "***", "***", "***", '*', "nuggetOsmium");
        recipeGenerator.addShapedRecipe(new ItemStack(MekanismItems.Nugget, 9, 1), "*", '*', "ingotOsmium");
        recipeGenerator.addShapedRecipe(new ItemStack(MekanismItems.Ingot, 1, 2), "***", "***", "***", '*', "nuggetBronze");
        recipeGenerator.addShapedRecipe(new ItemStack(MekanismItems.Nugget, 9, 2), "*", '*', "ingotBronze");
        recipeGenerator.addShapedRecipe(new ItemStack(MekanismItems.Ingot, 1, 3), "***", "***", "***", '*', "nuggetGlowstone");
        recipeGenerator.addShapedRecipe(new ItemStack(MekanismItems.Nugget, 9, 3), "*", '*', "ingotGlowstone");
        recipeGenerator.addShapedRecipe(new ItemStack(MekanismItems.Ingot, 1, 4), "***", "***", "***", '*', "nuggetSteel");
        recipeGenerator.addShapedRecipe(new ItemStack(MekanismItems.Nugget, 9, 4), "*", '*', "ingotSteel");
        recipeGenerator.addShapedRecipe(new ItemStack(MekanismItems.Ingot, 1, 5), "***", "***", "***", '*', "nuggetCopper");
        recipeGenerator.addShapedRecipe(new ItemStack(MekanismItems.Nugget, 9, 5), "*", '*', "ingotCopper");
        recipeGenerator.addShapedRecipe(new ItemStack(MekanismItems.Ingot, 1, 6), "***", "***", "***", '*', "nuggetTin");
        recipeGenerator.addShapedRecipe(new ItemStack(MekanismItems.Nugget, 9, 6), "*", '*', "ingotTin");

        //Base Recipes
        recipeGenerator.addShapedRecipe(new ItemStack(MekanismBlocks.ObsidianTNT, 1), "***", "XXX", "***", '*', Blocks.OBSIDIAN, 'X', Blocks.TNT);
        recipeGenerator.addShapedRecipe(MekanismItems.ElectricBow.getUnchargedItem(), " AB", "E B", " AB", 'A', MekanismItems.EnrichedAlloy, 'B', Items.STRING, 'E', MekanismItems.EnergyTablet.getUnchargedItem());
        recipeGenerator.addShapedRecipe(MekanismItems.EnergyTablet.getUnchargedItem(), "RCR", "ECE", "RCR", 'C', "ingotGold", 'R', "dustRedstone", 'E', MekanismItems.EnrichedAlloy);
        recipeGenerator.addShapedRecipe(MachineType.ENRICHMENT_CHAMBER.getStack(), "RCR", "iIi", "RCR", 'i', "ingotIron", 'C', MekanismUtils.getControlCircuit(BaseTier.BASIC), 'R', "alloyBasic", 'I', new ItemStack(MekanismBlocks.BasicBlock, 1, 8));
        recipeGenerator.addShapedRecipe(MachineType.OSMIUM_COMPRESSOR.getStack(), "ECE", "BIB", "ECE", 'E', "alloyAdvanced", 'C', MekanismUtils.getControlCircuit(BaseTier.ADVANCED), 'B', Items.BUCKET, 'I', new ItemStack(MekanismBlocks.BasicBlock, 1, 8));
        recipeGenerator.addShapedRecipe(MachineType.COMBINER.getStack(), "RCR", "SIS", "RCR", 'S', "cobblestone", 'C', MekanismUtils.getControlCircuit(BaseTier.ELITE), 'R', "alloyElite", 'I', new ItemStack(MekanismBlocks.BasicBlock, 1, 8));
        recipeGenerator.addShapedRecipe(MachineType.CRUSHER.getStack(), "RCR", "LIL", "RCR", 'R', "dustRedstone", 'L', Items.LAVA_BUCKET, 'C', MekanismUtils.getControlCircuit(BaseTier.BASIC), 'I', new ItemStack(MekanismBlocks.BasicBlock, 1, 8));
        recipeGenerator.addShapedRecipe(new ItemStack(MekanismItems.SpeedUpgrade), " G ", "ADA", " G ", 'G', "blockGlass", 'A', MekanismItems.EnrichedAlloy, 'D', "dustOsmium");
        recipeGenerator.addShapedRecipe(new ItemStack(MekanismItems.EnergyUpgrade), " G ", "ADA", " G ", 'G', "blockGlass", 'A', MekanismItems.EnrichedAlloy, 'D', "dustGold");
        recipeGenerator.addShapedRecipe(new ItemStack(MekanismItems.GasUpgrade), " G ", "ADA", " G ", 'G', "blockGlass", 'A', MekanismItems.EnrichedAlloy, 'D', "dustIron");
        recipeGenerator.addShapedRecipe(new ItemStack(MekanismItems.FilterUpgrade), " G ", "ADA", " G ", 'G', "blockGlass", 'A', MekanismItems.EnrichedAlloy, 'D', "dustTin");
        recipeGenerator.addShapedRecipe(new ItemStack(MekanismItems.MufflingUpgrade), " G ", "ADA", " G ", 'G', "blockGlass", 'A', MekanismItems.EnrichedAlloy, 'D', "dustSteel");
        recipeGenerator.addShapedRecipe(new ItemStack(MekanismItems.AnchorUpgrade), " G ", "ADA", " G ", 'G', "blockGlass", 'A', MekanismItems.EnrichedAlloy, 'D', "dustDiamond");
        recipeGenerator.addShapedRecipe(MekanismItems.AtomicDisassembler.getUnchargedItem(), "AEA", "ACA", " O ", 'A', MekanismItems.EnrichedAlloy, 'E', MekanismItems.EnergyTablet.getUnchargedItem(), 'C', MekanismItems.AtomicAlloy, 'O', "ingotRefinedObsidian");
        recipeGenerator.addShapedRecipe(MachineType.METALLURGIC_INFUSER.getStack(), "IFI", "ROR", "IFI", 'I', "ingotIron", 'F', Blocks.FURNACE, 'R', "dustRedstone", 'O', "ingotOsmium");
        recipeGenerator.addShapedRecipe(new ItemStack(MekanismItems.TeleportationCore), "LAL", "GDG", "LAL", 'L', new ItemStack(Items.DYE, 1, 4), 'A', MekanismItems.AtomicAlloy, 'G', "ingotGold", 'D', Items.DIAMOND);
        recipeGenerator.addShapedRecipe(MekanismItems.PortableTeleporter.getUnchargedItem(), " E ", "CTC", " E ", 'E', MekanismItems.EnergyTablet.getUnchargedItem(), 'C', MekanismUtils.getControlCircuit(BaseTier.BASIC), 'T', MekanismItems.TeleportationCore);
        recipeGenerator.addShapedRecipe(MachineType.TELEPORTER.getStack(), "COC", "OTO", "COC", 'C', MekanismUtils.getControlCircuit(BaseTier.BASIC), 'O', new ItemStack(MekanismBlocks.BasicBlock, 1, 8), 'T', MekanismItems.TeleportationCore);
        recipeGenerator.addShapedRecipe(MachineType.PURIFICATION_CHAMBER.getStack(), "ECE", "ORO", "ECE", 'C', MekanismUtils.getControlCircuit(BaseTier.ADVANCED), 'E', "alloyAdvanced", 'O', "ingotOsmium", 'R', MachineType.ENRICHMENT_CHAMBER.getStack());
        recipeGenerator.addShapedRecipe(MekanismItems.Configurator.getUnchargedItem(), " L ", "AEA", " S ", 'L', new ItemStack(Items.DYE, 1, 4), 'A', MekanismItems.EnrichedAlloy, 'E', MekanismItems.EnergyTablet.getUnchargedItem(), 'S', Items.STICK);
        recipeGenerator.addShapedRecipe(new ItemStack(MekanismBlocks.BasicBlock, 9, 7), "OOO", "OGO", "OOO", 'O', "ingotRefinedObsidian", 'G', "ingotRefinedGlowstone");
        recipeGenerator.addShapedRecipe(new ItemStack(MekanismBlocks.BasicBlock, 1, 8), "SGS", "GPG", "SGS", 'S', "ingotSteel", 'P', "ingotOsmium", 'G', "blockGlass");
        recipeGenerator.addShapedRecipe(MachineType.ENERGIZED_SMELTER.getStack(), "RCR", "GIG", "RCR", 'C', MekanismUtils.getControlCircuit(BaseTier.BASIC), 'R', "alloyBasic", 'G', "blockGlass", 'I', new ItemStack(MekanismBlocks.BasicBlock, 1, 8));
        recipeGenerator.addShapedRecipe(MachineType.ELECTRIC_PUMP.getStack(), " B ", "ECE", "OOO", 'B', Items.BUCKET, 'E', MekanismItems.EnrichedAlloy, 'C', new ItemStack(MekanismBlocks.BasicBlock, 1, 8), 'O', "ingotOsmium");
        recipeGenerator.addShapedRecipe(MachineType.PERSONAL_CHEST.getStack(), "SGS", "CcC", "SSS", 'S', "ingotSteel", 'G', "blockGlass", 'C', "chestWood", 'c', MekanismUtils.getControlCircuit(BaseTier.BASIC));
        recipeGenerator.addShapedRecipe(new ItemStack(MekanismBlocks.BasicBlock, 4, 9), " I ", "IBI", " I ", 'I', "ingotSteel", 'B', Items.BUCKET);
        recipeGenerator.addShapedRecipe(new ItemStack(MekanismBlocks.BasicBlock, 4, 10), " I ", "IGI", " I ", 'I', "ingotSteel", 'G', "blockGlass");
        recipeGenerator.addShapedRecipe(new ItemStack(MekanismBlocks.BasicBlock, 2, 11), " I ", "ICI", " I ", 'I', new ItemStack(MekanismBlocks.BasicBlock, 1, 9), 'C', MekanismUtils.getControlCircuit(BaseTier.BASIC));
        recipeGenerator.addShapedRecipe(MachineType.CHARGEPAD.getStack(), "PPP", "SES", 'P', Blocks.STONE_PRESSURE_PLATE, 'S', "ingotSteel", 'E', MekanismItems.EnergyTablet.getUnchargedItem());
        recipeGenerator.addShapedRecipe(MekanismItems.Robit.getUnchargedItem(), " S ", "ECE", "OIO", 'S', "ingotSteel", 'E', MekanismItems.EnergyTablet.getUnchargedItem(), 'C', MekanismItems.AtomicAlloy, 'O', "ingotRefinedObsidian", 'I', MachineType.PERSONAL_CHEST.getStack());
        recipeGenerator.addShapedRecipe(MekanismItems.NetworkReader.getUnchargedItem(), " G ", "AEA", " I ", 'G', "blockGlass", 'A', MekanismItems.EnrichedAlloy, 'E', MekanismItems.EnergyTablet.getUnchargedItem(), 'I', "ingotSteel");
        recipeGenerator.addShapedRecipe(new ItemStack(MekanismItems.WalkieTalkie), "  O", "SCS", " S ", 'O', "ingotOsmium", 'S', "ingotSteel", 'C', MekanismUtils.getControlCircuit(BaseTier.BASIC));
        recipeGenerator.addShapedRecipe(MachineType.LOGISTICAL_SORTER.getStack(), "IPI", "ICI", "III", 'I', "ingotIron", 'P', Blocks.PISTON, 'C', MekanismUtils.getControlCircuit(BaseTier.BASIC));
        recipeGenerator.addShapedRecipe(MachineType.DIGITAL_MINER.getStack(), "ACA", "SES", "TIT", 'A', MekanismItems.AtomicAlloy, 'C', MekanismUtils.getControlCircuit(BaseTier.BASIC), 'S', MachineType.LOGISTICAL_SORTER.getStack(), 'E', MekanismItems.Robit.getUnchargedItem(), 'I', new ItemStack(MekanismBlocks.BasicBlock, 1, 8), 'T', MekanismItems.TeleportationCore);
        recipeGenerator.addShapedRecipe(MachineType.ROTARY_CONDENSENTRATOR.getStack(), "GCG", "tEI", "GCG", 'G', "blockGlass", 'C', MekanismUtils.getControlCircuit(BaseTier.BASIC), 't', MekanismUtils.getEmptyGasTank(GasTankTier.BASIC), 'E', MekanismItems.EnergyTablet.getUnchargedItem(), 'I', MekanismUtils.getEmptyFluidTank(FluidTankTier.BASIC));
        recipeGenerator.addShapedRecipe(MekanismItems.Jetpack.getEmptyItem(), "SCS", "TGT", " T ", 'S', "ingotSteel", 'C', MekanismUtils.getControlCircuit(BaseTier.BASIC), 'T', "ingotTin", 'G', MekanismUtils.getEmptyGasTank(GasTankTier.BASIC));
        recipeGenerator.addShapedRecipe(new ItemStack(MekanismItems.Dictionary), "C", "B", 'C', MekanismUtils.getControlCircuit(BaseTier.BASIC), 'B', Items.BOOK);
        recipeGenerator.addShapedRecipe(new ItemStack(MekanismItems.GasMask), " S ", "GCG", "S S", 'S', "ingotSteel", 'G', "blockGlass", 'C', MekanismUtils.getControlCircuit(BaseTier.BASIC));
        recipeGenerator.addShapedRecipe(MekanismItems.ScubaTank.getEmptyItem(), " C ", "AGA", "SSS", 'C', MekanismUtils.getControlCircuit(BaseTier.BASIC), 'A', MekanismItems.EnrichedAlloy, 'S', "ingotSteel", 'G', MekanismUtils.getEmptyGasTank(GasTankTier.BASIC));
        recipeGenerator.addShapedRecipe(MachineType.CHEMICAL_OXIDIZER.getStack(), "ACA", "ERG", "ACA", 'C', MekanismUtils.getControlCircuit(BaseTier.BASIC), 'R', new ItemStack(MekanismBlocks.BasicBlock, 1, 9), 'G', MekanismUtils.getEmptyGasTank(GasTankTier.BASIC), 'E', MachineType.PERSONAL_CHEST.getStack(), 'A', MekanismItems.EnrichedAlloy);
        recipeGenerator.addShapedRecipe(MachineType.CHEMICAL_INFUSER.getStack(), "ACA", "GRG", "ACA", 'C', MekanismUtils.getControlCircuit(BaseTier.BASIC), 'R', new ItemStack(MekanismBlocks.BasicBlock, 1, 9), 'G', MekanismUtils.getEmptyGasTank(GasTankTier.BASIC), 'A', MekanismItems.EnrichedAlloy);
        recipeGenerator.addShapedRecipe(MachineType.CHEMICAL_INJECTION_CHAMBER.getStack(), "RCR", "GPG", "RCR", 'C', MekanismUtils.getControlCircuit(BaseTier.ELITE), 'R', "alloyElite", 'G', "ingotGold", 'P', MachineType.PURIFICATION_CHAMBER.getStack());
        recipeGenerator.addShapedRecipe(MachineType.ELECTROLYTIC_SEPARATOR.getStack(), "IRI", "ECE", "IRI", 'I', "ingotIron", 'R', "dustRedstone", 'E', MekanismItems.EnrichedAlloy, 'C', MekanismItems.ElectrolyticCore);
        recipeGenerator.addShapedRecipe(new ItemStack(MekanismItems.ElectrolyticCore), "EPE", "IEG", "EPE", 'E', MekanismItems.EnrichedAlloy, 'P', "dustOsmium", 'I', "dustIron", 'G', "dustGold");
        recipeGenerator.addShapedRecipe(new ItemStack(MekanismBlocks.CardboardBox), "SS", "SS", 'S', "pulpWood");
        recipeGenerator.addShapedRecipe(MachineType.PRECISION_SAWMILL.getStack(), "ICI", "ASA", "ICI", 'I', "ingotIron", 'C', MekanismUtils.getControlCircuit(BaseTier.BASIC), 'A', MekanismItems.EnrichedAlloy, 'S', new ItemStack(MekanismBlocks.BasicBlock, 1, 8));
        recipeGenerator.addShapedRecipe(new ItemStack(MekanismBlocks.BasicBlock, 1, 14), "CGC", "IBI", "III", 'C', MekanismUtils.getControlCircuit(BaseTier.ADVANCED), 'G', "paneGlass", 'I', new ItemStack(MekanismBlocks.BasicBlock2, 1, 0), 'B', Items.BUCKET);
        recipeGenerator.addShapedRecipe(new ItemStack(MekanismBlocks.BasicBlock, 1, 15), " I ", "ICI", " I ", 'I', new ItemStack(MekanismBlocks.BasicBlock2, 1, 0), 'C', MekanismUtils.getControlCircuit(BaseTier.ADVANCED));
        recipeGenerator.addShapedRecipe(new ItemStack(MekanismBlocks.BasicBlock2, 4, 0), " S ", "SCS", " S ", 'C', "ingotCopper", 'S', "ingotSteel");
        recipeGenerator.addShapedRecipe(MachineType.CHEMICAL_DISSOLUTION_CHAMBER.getStack(), "CGC", "EAE", "CGC", 'G', MekanismUtils.getEmptyGasTank(GasTankTier.BASIC), 'C', MekanismUtils.getControlCircuit(BaseTier.BASIC), 'A', MekanismItems.AtomicAlloy, 'E', MekanismItems.EnrichedAlloy);
        recipeGenerator.addShapedRecipe(MachineType.CHEMICAL_WASHER.getStack(), "CWC", "EIE", "CGC", 'W', Items.BUCKET, 'C', MekanismUtils.getControlCircuit(BaseTier.BASIC), 'E', MekanismItems.EnrichedAlloy, 'G', MekanismUtils.getEmptyGasTank(GasTankTier.BASIC), 'I', new ItemStack(MekanismBlocks.BasicBlock, 1, 8));
        recipeGenerator.addShapedRecipe(MachineType.CHEMICAL_CRYSTALLIZER.getStack(), "CGC", "ASA", "CGC", 'G', MekanismUtils.getEmptyGasTank(GasTankTier.BASIC), 'C', MekanismUtils.getControlCircuit(BaseTier.BASIC), 'A', MekanismItems.AtomicAlloy, 'S', new ItemStack(MekanismBlocks.BasicBlock, 1, 8));
        recipeGenerator.addShapedRecipe(MekanismItems.FreeRunners.getUnchargedItem(), "C C", "A A", "T T", 'C', MekanismUtils.getControlCircuit(BaseTier.BASIC), 'A', MekanismItems.EnrichedAlloy, 'T', MekanismItems.EnergyTablet.getUnchargedItem());
        recipeGenerator.addShapedRecipe(MekanismItems.ArmoredJetpack.getEmptyItem(), "D D", "BSB", " J ", 'D', "dustDiamond", 'B', "ingotBronze", 'S', "blockSteel", 'J', MekanismItems.Jetpack.getEmptyItem());
        recipeGenerator.addShapedRecipe(new ItemStack(MekanismItems.ConfigurationCard), " A ", "ACA", " A ", 'A', MekanismItems.EnrichedAlloy, 'C', MekanismUtils.getControlCircuit(BaseTier.BASIC));
        recipeGenerator.addShapedRecipe(MekanismItems.SeismicReader.getUnchargedItem(), "SLS", "STS", "SSS", 'S', "ingotSteel", 'L', new ItemStack(Items.DYE, 1, 4), 'T', MekanismItems.EnergyTablet.getUnchargedItem());
        recipeGenerator.addShapedRecipe(MachineType.SEISMIC_VIBRATOR.getStack(), "TLT", "CIC", "TTT", 'T', "ingotTin", 'L', new ItemStack(Items.DYE, 1, 4), 'C', MekanismUtils.getControlCircuit(BaseTier.BASIC), 'I', new ItemStack(MekanismBlocks.BasicBlock, 1, 8));
        recipeGenerator.addShapedRecipe(MachineType.PRESSURIZED_REACTION_CHAMBER.getStack(), "SES", "CIC", "GFG", 'S', "ingotSteel", 'E', MekanismItems.EnrichedAlloy, 'C', MekanismUtils.getControlCircuit(BaseTier.BASIC), 'G', MekanismUtils.getEmptyGasTank(GasTankTier.BASIC), 'I', MachineType.ENRICHMENT_CHAMBER.getStack(), 'F', new ItemStack(MekanismBlocks.BasicBlock, 1, 9));
        recipeGenerator.addShapedRecipe(MachineType.FLUIDIC_PLENISHER.getStack(), "TTT", "CPC", "TTT", 'P', MachineType.ELECTRIC_PUMP.getStack(), 'T', "ingotTin", 'C', MekanismUtils.getControlCircuit(BaseTier.BASIC));
        recipeGenerator.addShapedRecipe(new ItemStack(Blocks.RAIL, 24), "O O", "OSO", "O O", 'O', "ingotOsmium", 'S', "stickWood");
        recipeGenerator.addShapedRecipe(MekanismItems.Flamethrower.getEmptyItem(), "TTT", "TGS", "BCB", 'T', "ingotTin", 'G', MekanismUtils.getEmptyGasTank(GasTankTier.BASIC), 'S', Items.FLINT_AND_STEEL, 'B', "ingotBronze", 'C', MekanismUtils.getControlCircuit(BaseTier.ADVANCED));/**/
        recipeGenerator.addShapedRecipe(MekanismItems.GaugeDropper.getEmptyItem(), " O ", "G G", "GGG", 'O', "ingotOsmium", 'G', "paneGlass");
        recipeGenerator.addShapedRecipe(MachineType.SOLAR_NEUTRON_ACTIVATOR.getStack(), "APA", "CSC", "BBB", 'A', "alloyElite", 'S', new ItemStack(MekanismBlocks.BasicBlock, 1, 8), 'P', new ItemStack(MekanismItems.Polyethene, 1, 2), 'B', "ingotBronze", 'C', MekanismUtils.getControlCircuit(BaseTier.ELITE));
        recipeGenerator.addShapedRecipe(new ItemStack(MekanismBlocks.BasicBlock2, 4, 1), " S ", "SES", " S ", 'S', "ingotSteel", 'E', MekanismItems.EnergyTablet.getUnchargedItem());
        recipeGenerator.addShapedRecipe(new ItemStack(MekanismBlocks.BasicBlock2, 2, 2), " I ", "ICI", " I ", 'I', new ItemStack(MekanismBlocks.BasicBlock2, 1, 1), 'C', MekanismUtils.getControlCircuit(BaseTier.ELITE));
        recipeGenerator.addShapedRecipe(new ItemStack(MekanismItems.TierInstaller, 1, 0), "RCR", "iWi", "RCR", 'R', "alloyBasic", 'C', MekanismUtils.getControlCircuit(BaseTier.BASIC), 'i', "ingotIron", 'W', "plankWood");
        recipeGenerator.addShapedRecipe(new ItemStack(MekanismItems.TierInstaller, 1, 1), "ECE", "oWo", "ECE", 'E', "alloyAdvanced", 'C', MekanismUtils.getControlCircuit(BaseTier.ADVANCED), 'o', "ingotOsmium", 'W', "plankWood");
        recipeGenerator.addShapedRecipe(new ItemStack(MekanismItems.TierInstaller, 1, 2), "RCR", "gWg", "RCR", 'R', "alloyElite", 'C', MekanismUtils.getControlCircuit(BaseTier.ELITE), 'g', "ingotGold", 'W', "plankWood");
        recipeGenerator.addShapedRecipe(new ItemStack(MekanismItems.TierInstaller, 1, 3), "RCR", "dWd", "RCR", 'R', "alloyUltimate", 'C', MekanismUtils.getControlCircuit(BaseTier.ULTIMATE), 'd', "gemDiamond", 'W', "plankWood");
        recipeGenerator.addShapedRecipe(MachineType.OREDICTIONIFICATOR.getStack(), "SGS", "CBC", "SWS", 'S', "ingotSteel", 'G', "paneGlass", 'C', MekanismUtils.getControlCircuit(BaseTier.BASIC), 'B', MekanismItems.Dictionary, 'W', "chestWood");
        recipeGenerator.addShapedRecipe(MachineType.LASER.getStack(), "RE ", "RCD", "RE ", 'R', "alloyElite", 'E', MekanismItems.EnergyTablet.getUnchargedItem(), 'C', new ItemStack(MekanismBlocks.BasicBlock, 1, 8), 'D', "gemDiamond");
        recipeGenerator.addShapedRecipe(MachineType.LASER_AMPLIFIER.getStack(), "SSS", "SED", "SSS", 'S', "ingotSteel", 'E', MekanismUtils.getEnergyCube(EnergyCubeTier.BASIC), 'D', "gemDiamond");
        recipeGenerator.addShapedRecipe(MachineType.LASER_TRACTOR_BEAM.getStack(), "C", "F", 'C', MachineType.PERSONAL_CHEST.getStack(), 'F', MachineType.LASER_AMPLIFIER.getStack());
        recipeGenerator.addShapedRecipe(new ItemStack(MekanismBlocks.BasicBlock2, 1, 6), "SFS", "FAF", "SFS", 'S', "ingotSteel", 'A', MekanismItems.EnrichedAlloy, 'F', Blocks.IRON_BARS);
        recipeGenerator.addShapedRecipe(new ItemStack(MekanismBlocks.BasicBlock2, 4, 7), " S ", "SIS", " S ", 'S', "ingotSteel", 'I', "ingotIron");
        recipeGenerator.addShapedRecipe(new ItemStack(MekanismBlocks.BasicBlock2, 2, 8), " I ", "ICI", " I ", 'I', new ItemStack(MekanismBlocks.BasicBlock2, 1, 7), 'C', MekanismUtils.getControlCircuit(BaseTier.ADVANCED));
        recipeGenerator.addShapedRecipe(new ItemStack(MekanismBlocks.BasicBlock2, 1, 5), "ACA", "CIC", "ACA", 'I', new ItemStack(MekanismBlocks.BasicBlock, 1, 8), 'C', "ingotCopper", 'A', "alloyBasic");
        recipeGenerator.addShapedRecipe(MachineType.RESISTIVE_HEATER.getStack(), "CRC", "RHR", "CEC", 'C', "ingotTin", 'R', "dustRedstone", 'H', new ItemStack(MekanismBlocks.BasicBlock2, 1, 5), 'E', MekanismItems.EnergyTablet.getUnchargedItem());
        recipeGenerator.addShapedRecipe(MachineType.QUANTUM_ENTANGLOPORTER.getStack(), "OCO", "ATA", "OCO", 'O', "ingotRefinedObsidian", 'C', MekanismUtils.getControlCircuit(BaseTier.ULTIMATE), 'A', "alloyUltimate", 'T', MekanismItems.TeleportationCore);
        recipeGenerator.addShapedRecipe(MachineType.FORMULAIC_ASSEMBLICATOR.getStack(), "STS", "BIB", "SCS", 'S', "ingotSteel", 'T', Blocks.CRAFTING_TABLE, 'B', MekanismUtils.getControlCircuit(BaseTier.BASIC), 'I', new ItemStack(MekanismBlocks.BasicBlock, 1, 8), 'C', "chestWood");
        recipeGenerator.addShapelessRecipe(new ItemStack(MekanismItems.CraftingFormula), Items.PAPER, MekanismUtils.getControlCircuit(BaseTier.BASIC));
        recipeGenerator.addShapedRecipe(new ItemStack(MekanismBlocks.BasicBlock2, 1, 9), "SGS", "CIC", "STS", 'S', "ingotSteel", 'G', "blockGlass", 'C', MekanismUtils.getControlCircuit(BaseTier.ELITE), 'I', new ItemStack(MekanismBlocks.BasicBlock, 1, 8), 'T', MekanismItems.TeleportationCore);
        recipeGenerator.addShapedRecipe(MachineType.FUELWOOD_HEATER.getStack(), "SCS", "FHF", "SSS", 'S', "ingotSteel", 'C', MekanismUtils.getControlCircuit(BaseTier.BASIC), 'F', Blocks.FURNACE, 'H', new ItemStack(MekanismBlocks.BasicBlock, 1, 8));
        recipeGenerator.addShapedRecipe(new ItemStack(Items.PAPER, 6), "SSS", 'S', "pulpWood");
        recipeGenerator.addShapedRecipe(new ItemStack(Items.PAPER, 6), "SSS", 'S', "dustWood");

        //Energy Cube recipes
        recipeGenerator.addShapedRecipe(MekanismUtils.getEnergyCube(EnergyCubeTier.BASIC), "RTR", "iIi", "RTR", 'R', "alloyBasic", 'i', "ingotIron", 'T', MekanismItems.EnergyTablet.getUnchargedItem(), 'I', new ItemStack(MekanismBlocks.BasicBlock, 1, 8));
        recipeGenerator.addShapedRecipe(MekanismUtils.getEnergyCube(EnergyCubeTier.ADVANCED), "ETE", "oBo", "ETE", 'E', "alloyAdvanced", 'o', "ingotOsmium", 'T', MekanismItems.EnergyTablet.getUnchargedItem(), 'B', MekanismUtils.getEnergyCube(EnergyCubeTier.BASIC));
        recipeGenerator.addShapedRecipe(MekanismUtils.getEnergyCube(EnergyCubeTier.ELITE), "RTR", "gAg", "RTR", 'R', "alloyElite", 'g', "ingotGold", 'T', MekanismItems.EnergyTablet.getUnchargedItem(), 'A', MekanismUtils.getEnergyCube(EnergyCubeTier.ADVANCED));
        recipeGenerator.addShapedRecipe(MekanismUtils.getEnergyCube(EnergyCubeTier.ULTIMATE), "ATA", "dEd", "ATA", 'A', "alloyUltimate", 'd', "gemDiamond", 'T', MekanismItems.EnergyTablet.getUnchargedItem(), 'E', MekanismUtils.getEnergyCube(EnergyCubeTier.ELITE));

        //Gas Tank Recipes
        recipeGenerator.addShapedRecipe(MekanismUtils.getEmptyGasTank(GasTankTier.BASIC), "APA", "P P", "APA", 'P', "ingotOsmium", 'A', "alloyBasic");
        recipeGenerator.addShapedRecipe(MekanismUtils.getEmptyGasTank(GasTankTier.ADVANCED), "APA", "PTP", "APA", 'P', "ingotOsmium", 'A', "alloyAdvanced", 'T', MekanismUtils.getEmptyGasTank(GasTankTier.BASIC));
        recipeGenerator.addShapedRecipe(MekanismUtils.getEmptyGasTank(GasTankTier.ELITE), "APA", "PTP", "APA", 'P', "ingotOsmium", 'A', "alloyElite", 'T', MekanismUtils.getEmptyGasTank(GasTankTier.ADVANCED));
        recipeGenerator.addShapedRecipe(MekanismUtils.getEmptyGasTank(GasTankTier.ULTIMATE), "APA", "PTP", "APA", 'P', "ingotOsmium", 'A', "alloyUltimate", 'T', MekanismUtils.getEmptyGasTank(GasTankTier.ELITE));

        //Fluid Tank Recipes
        recipeGenerator.addShapedRecipe(MekanismUtils.getEmptyFluidTank(FluidTankTier.BASIC), "AIA", "I I", "AIA", 'I', "ingotIron", 'A', "alloyBasic");
        recipeGenerator.addShapedRecipe(MekanismUtils.getEmptyFluidTank(FluidTankTier.ADVANCED), "AIA", "ITI", "AIA", 'I', "ingotIron", 'A', "alloyAdvanced", 'T', MekanismUtils.getEmptyFluidTank(FluidTankTier.BASIC));
        recipeGenerator.addShapedRecipe(MekanismUtils.getEmptyFluidTank(FluidTankTier.ELITE), "AIA", "ITI", "AIA", 'I', "ingotIron", 'A', "alloyElite", 'T', MekanismUtils.getEmptyFluidTank(FluidTankTier.ADVANCED));
        recipeGenerator.addShapedRecipe(MekanismUtils.getEmptyFluidTank(FluidTankTier.ULTIMATE), "AIA", "ITI", "AIA", 'I', "ingotIron", 'A', "alloyUltimate", 'T', MekanismUtils.getEmptyFluidTank(FluidTankTier.ELITE));

        //Bin recipes
        recipeGenerator.addShapedRecipe(MekanismUtils.getBin(BinTier.BASIC), "SCS", "A A", "SSS", 'S', "cobblestone", 'C', MekanismUtils.getControlCircuit(BaseTier.BASIC), 'A', "alloyBasic");
        recipeGenerator.addShapedRecipe(MekanismUtils.getBin(BinTier.ADVANCED), "SCS", "ABA", "SSS", 'S', "cobblestone", 'C', MekanismUtils.getControlCircuit(BaseTier.ADVANCED), 'A', "alloyAdvanced", 'B', MekanismUtils.getBin(BinTier.BASIC));
        recipeGenerator.addShapedRecipe(MekanismUtils.getBin(BinTier.ELITE), "SCS", "ABA", "SSS", 'S', "cobblestone", 'C', MekanismUtils.getControlCircuit(BaseTier.ELITE), 'A', "alloyElite", 'B', MekanismUtils.getBin(BinTier.ADVANCED));
        recipeGenerator.addShapedRecipe(MekanismUtils.getBin(BinTier.ULTIMATE), "SCS", "ABA", "SSS", 'S', "cobblestone", 'C', MekanismUtils.getControlCircuit(BaseTier.ULTIMATE), 'A', "alloyUltimate", 'B', MekanismUtils.getBin(BinTier.ELITE));

        //Induction Cell recipes
        recipeGenerator.addShapedRecipe(MekanismUtils.getInductionCell(InductionCellTier.BASIC), "LTL", "TET", "LTL", 'T', MekanismItems.EnergyTablet.getUnchargedItem(), 'E', MekanismUtils.getEnergyCube(EnergyCubeTier.BASIC), 'L', "dustLithium");
        recipeGenerator.addShapedRecipe(MekanismUtils.getInductionCell(InductionCellTier.ADVANCED), "TCT", "CEC", "TCT", 'T', MekanismItems.EnergyTablet.getUnchargedItem(), 'E', MekanismUtils.getEnergyCube(EnergyCubeTier.ADVANCED), 'C', MekanismUtils.getInductionCell(InductionCellTier.BASIC));
        recipeGenerator.addShapedRecipe(MekanismUtils.getInductionCell(InductionCellTier.ELITE), "TCT", "CEC", "TCT", 'T', MekanismItems.EnergyTablet.getUnchargedItem(), 'E', MekanismUtils.getEnergyCube(EnergyCubeTier.ELITE), 'C', MekanismUtils.getInductionCell(InductionCellTier.ADVANCED));
        recipeGenerator.addShapedRecipe(MekanismUtils.getInductionCell(InductionCellTier.ULTIMATE), "TCT", "CEC", "TCT", 'T', MekanismItems.EnergyTablet.getUnchargedItem(), 'E', MekanismUtils.getEnergyCube(EnergyCubeTier.ULTIMATE), 'C', MekanismUtils.getInductionCell(InductionCellTier.ELITE));

        //Induction Provider recipes
        recipeGenerator.addShapedRecipe(MekanismUtils.getInductionProvider(InductionProviderTier.BASIC), "LCL", "CEC", "LCL", 'C', MekanismUtils.getControlCircuit(BaseTier.BASIC), 'E', MekanismUtils.getEnergyCube(EnergyCubeTier.BASIC), 'L', "dustLithium");
        recipeGenerator.addShapedRecipe(MekanismUtils.getInductionProvider(InductionProviderTier.ADVANCED), "CPC", "PEP", "CPC", 'C', MekanismUtils.getControlCircuit(BaseTier.ADVANCED), 'E', MekanismUtils.getEnergyCube(EnergyCubeTier.ADVANCED), 'P', MekanismUtils.getInductionProvider(InductionProviderTier.BASIC));
        recipeGenerator.addShapedRecipe(MekanismUtils.getInductionProvider(InductionProviderTier.ELITE), "CPC", "PEP", "CPC", 'C', MekanismUtils.getControlCircuit(BaseTier.ELITE), 'E', MekanismUtils.getEnergyCube(EnergyCubeTier.ELITE), 'P', MekanismUtils.getInductionProvider(InductionProviderTier.ADVANCED));
        recipeGenerator.addShapedRecipe(MekanismUtils.getInductionProvider(InductionProviderTier.ULTIMATE), "CPC", "PEP", "CPC", 'C', MekanismUtils.getControlCircuit(BaseTier.ULTIMATE), 'E', MekanismUtils.getEnergyCube(EnergyCubeTier.ULTIMATE), 'P', MekanismUtils.getInductionProvider(InductionProviderTier.ELITE));

        //Circuit recipes
        recipeGenerator.addShapedRecipe(new ItemStack(MekanismItems.ControlCircuit, 1, 1), "ECE", 'C', new ItemStack(MekanismItems.ControlCircuit, 1, 0), 'E', "alloyAdvanced");
        recipeGenerator.addShapedRecipe(new ItemStack(MekanismItems.ControlCircuit, 1, 2), "RCR", 'C', new ItemStack(MekanismItems.ControlCircuit, 1, 1), 'R', "alloyElite");
        recipeGenerator.addShapedRecipe(new ItemStack(MekanismItems.ControlCircuit, 1, 3), "ACA", 'C', new ItemStack(MekanismItems.ControlCircuit, 1, 2), 'A', "alloyUltimate");

        //Factory recipes
        for (RecipeType type : RecipeType.values()) {
            recipeGenerator.addShapedRecipe(MekanismUtils.getFactory(FactoryTier.BASIC, type), "RCR", "iOi", "RCR", 'R', "alloyBasic", 'C', MekanismUtils.getControlCircuit(BaseTier.BASIC), 'i', "ingotIron", 'O', type.getStack());
            recipeGenerator.addShapedRecipe(MekanismUtils.getFactory(FactoryTier.ADVANCED, type), "ECE", "oOo", "ECE", 'E', "alloyAdvanced", 'C', MekanismUtils.getControlCircuit(BaseTier.ADVANCED), 'o', "ingotOsmium", 'O', MekanismUtils.getFactory(FactoryTier.BASIC, type));
            recipeGenerator.addShapedRecipe(MekanismUtils.getFactory(FactoryTier.ELITE, type), "RCR", "gOg", "RCR", 'R', "alloyElite", 'C', MekanismUtils.getControlCircuit(BaseTier.ELITE), 'g', "ingotGold", 'O', MekanismUtils.getFactory(FactoryTier.ADVANCED, type));
        }

        //Add the bin recipe system to the CraftingManager
        // CraftingManager.getInstance().getRecipeList().add(new BinRecipe();

        //Transmitters
        recipeGenerator.addShapedRecipe(MekanismUtils.getTransmitter(TransmitterType.UNIVERSAL_CABLE, BaseTier.BASIC, 8), "SRS", 'S', "ingotSteel", 'R', "dustRedstone");
        recipeGenerator.addShapedRecipe(MekanismUtils.getTransmitter(TransmitterType.UNIVERSAL_CABLE, BaseTier.ADVANCED, 8), "TTT", "TET", "TTT", 'E', "alloyAdvanced", 'T', MekanismUtils.getTransmitter(TransmitterType.UNIVERSAL_CABLE, BaseTier.BASIC, 1));
        recipeGenerator.addShapedRecipe(MekanismUtils.getTransmitter(TransmitterType.UNIVERSAL_CABLE, BaseTier.ELITE, 8), "TTT", "TRT", "TTT", 'R', "alloyElite", 'T', MekanismUtils.getTransmitter(TransmitterType.UNIVERSAL_CABLE, BaseTier.ADVANCED, 1));
        recipeGenerator.addShapedRecipe(MekanismUtils.getTransmitter(TransmitterType.UNIVERSAL_CABLE, BaseTier.ULTIMATE, 8), "TTT", "TAT", "TTT", 'A', "alloyUltimate", 'T', MekanismUtils.getTransmitter(TransmitterType.UNIVERSAL_CABLE, BaseTier.ELITE, 1));
        recipeGenerator.addShapedRecipe(MekanismUtils.getTransmitter(TransmitterType.MECHANICAL_PIPE, BaseTier.BASIC, 8), "SBS", 'S', "ingotSteel", 'B', Items.BUCKET);
        recipeGenerator.addShapedRecipe(MekanismUtils.getTransmitter(TransmitterType.MECHANICAL_PIPE, BaseTier.ADVANCED, 8), "TTT", "TET", "TTT", 'E', "alloyAdvanced", 'T', MekanismUtils.getTransmitter(TransmitterType.MECHANICAL_PIPE, BaseTier.BASIC, 1));
        recipeGenerator.addShapedRecipe(MekanismUtils.getTransmitter(TransmitterType.MECHANICAL_PIPE, BaseTier.ELITE, 8), "TTT", "TRT", "TTT", 'R', "alloyElite", 'T', MekanismUtils.getTransmitter(TransmitterType.MECHANICAL_PIPE, BaseTier.ADVANCED, 1));
        recipeGenerator.addShapedRecipe(MekanismUtils.getTransmitter(TransmitterType.MECHANICAL_PIPE, BaseTier.ULTIMATE, 8), "TTT", "TAT", "TTT", 'A', "alloyUltimate", 'T', MekanismUtils.getTransmitter(TransmitterType.MECHANICAL_PIPE, BaseTier.ELITE, 1));
        recipeGenerator.addShapedRecipe(MekanismUtils.getTransmitter(TransmitterType.PRESSURIZED_TUBE, BaseTier.BASIC, 8), "SGS", 'S', "ingotSteel", 'G', "blockGlass");
        recipeGenerator.addShapedRecipe(MekanismUtils.getTransmitter(TransmitterType.PRESSURIZED_TUBE, BaseTier.ADVANCED, 8), "TTT", "TET", "TTT", 'E', "alloyAdvanced", 'T', MekanismUtils.getTransmitter(TransmitterType.PRESSURIZED_TUBE, BaseTier.BASIC, 1));
        recipeGenerator.addShapedRecipe(MekanismUtils.getTransmitter(TransmitterType.PRESSURIZED_TUBE, BaseTier.ELITE, 8), "TTT", "TRT", "TTT", 'R', "alloyElite", 'T', MekanismUtils.getTransmitter(TransmitterType.PRESSURIZED_TUBE, BaseTier.ADVANCED, 1));
        recipeGenerator.addShapedRecipe(MekanismUtils.getTransmitter(TransmitterType.PRESSURIZED_TUBE, BaseTier.ULTIMATE, 8), "TTT", "TAT", "TTT", 'A', "alloyUltimate", 'T', MekanismUtils.getTransmitter(TransmitterType.PRESSURIZED_TUBE, BaseTier.ELITE, 1));
        recipeGenerator.addShapedRecipe(MekanismUtils.getTransmitter(TransmitterType.LOGISTICAL_TRANSPORTER, BaseTier.BASIC, 8), "SCS", 'S', "ingotSteel", 'C', MekanismUtils.getControlCircuit(BaseTier.BASIC));
        recipeGenerator.addShapedRecipe(MekanismUtils.getTransmitter(TransmitterType.LOGISTICAL_TRANSPORTER, BaseTier.ADVANCED, 8), "TTT", "TET", "TTT", 'E', "alloyAdvanced", 'T', MekanismUtils.getTransmitter(TransmitterType.LOGISTICAL_TRANSPORTER, BaseTier.BASIC, 1));
        recipeGenerator.addShapedRecipe(MekanismUtils.getTransmitter(TransmitterType.LOGISTICAL_TRANSPORTER, BaseTier.ELITE, 8), "TTT", "TRT", "TTT", 'R', "alloyElite", 'T', MekanismUtils.getTransmitter(TransmitterType.LOGISTICAL_TRANSPORTER, BaseTier.ADVANCED, 1));
        recipeGenerator.addShapedRecipe(MekanismUtils.getTransmitter(TransmitterType.LOGISTICAL_TRANSPORTER, BaseTier.ULTIMATE, 8), "TTT", "TAT", "TTT", 'A', "alloyUltimate", 'T', MekanismUtils.getTransmitter(TransmitterType.LOGISTICAL_TRANSPORTER, BaseTier.ELITE, 1));
        recipeGenerator.addShapedRecipe(MekanismUtils.getTransmitter(TransmitterType.RESTRICTIVE_TRANSPORTER, BaseTier.BASIC, 2), "SBS", 'S', "ingotSteel", 'B', Blocks.IRON_BARS);
        recipeGenerator.addShapedRecipe(MekanismUtils.getTransmitter(TransmitterType.DIVERSION_TRANSPORTER, BaseTier.BASIC, 2), "RRR", "SBS", "RRR", 'R', "dustRedstone", 'S', "ingotSteel", 'B', Blocks.IRON_BARS);
        recipeGenerator.addShapedRecipe(MekanismUtils.getTransmitter(TransmitterType.THERMODYNAMIC_CONDUCTOR, BaseTier.BASIC, 8), "SCS", 'S', "ingotSteel", 'C', "ingotCopper");
        recipeGenerator.addShapedRecipe(MekanismUtils.getTransmitter(TransmitterType.THERMODYNAMIC_CONDUCTOR, BaseTier.ADVANCED, 8), "TTT", "TET", "TTT", 'E', "alloyAdvanced", 'T', MekanismUtils.getTransmitter(TransmitterType.THERMODYNAMIC_CONDUCTOR, BaseTier.BASIC, 1));
        recipeGenerator.addShapedRecipe(MekanismUtils.getTransmitter(TransmitterType.THERMODYNAMIC_CONDUCTOR, BaseTier.ELITE, 8), "TTT", "TRT", "TTT", 'R', "alloyElite", 'T', MekanismUtils.getTransmitter(TransmitterType.THERMODYNAMIC_CONDUCTOR, BaseTier.ADVANCED, 1));
        recipeGenerator.addShapedRecipe(MekanismUtils.getTransmitter(TransmitterType.THERMODYNAMIC_CONDUCTOR, BaseTier.ULTIMATE, 8), "TTT", "TAT", "TTT", 'A', "alloyUltimate", 'T', MekanismUtils.getTransmitter(TransmitterType.THERMODYNAMIC_CONDUCTOR, BaseTier.ELITE, 1));

        //Plastic stuff
        recipeGenerator.addShapedRecipe(new ItemStack(MekanismItems.Polyethene, 1, 1), "PP", "PP", 'P', new ItemStack(MekanismItems.Polyethene, 1, 0));
        recipeGenerator.addShapedRecipe(new ItemStack(MekanismItems.Polyethene, 1, 2), "PPP", "P P", "PPP", 'P', new ItemStack(MekanismItems.Polyethene, 1, 0));
        recipeGenerator.addShapedRecipe(new ItemStack(MekanismItems.Polyethene, 1, 3), "R", "R", 'R', new ItemStack(MekanismItems.Polyethene, 1, 1));

        //Creation of plastic block and glow panel with colors
        recipeGenerator.addShapedRecipe(new ItemStack(MekanismBlocks.PlasticBlock, 4, 15), "SSS", "S S", "SSS", 'S', new ItemStack(MekanismItems.Polyethene, 1, 2));
        recipeGenerator.addShapedRecipe(new ItemStack(MekanismBlocks.GlowPanel, 2, 15), "PSP", "S S", "GSG", 'P', "paneGlass", 'S', new ItemStack(MekanismItems.Polyethene, 1, 2), 'G', "dustGlowstone");
        for (int i = 0; i < EnumColor.DYES.length - 1; i++) {
            recipeGenerator.addShapedRecipe(new ItemStack(MekanismBlocks.PlasticBlock, 4, i), "SSS", "SDS", "SSS", 'S', new ItemStack(MekanismItems.Polyethene, 1, 2), 'D', "dye" + EnumColor.DYES[i].dyeName);
            recipeGenerator.addShapedRecipe(new ItemStack(MekanismBlocks.GlowPanel, 2, i), "PSP", "SDS", "GSG", 'P', "paneGlass", 'S', new ItemStack(MekanismItems.Polyethene, 1, 2), 'D', "dye" + EnumColor.DYES[i].dyeName, 'G', "dustGlowstone");
        }

        for (int i = 0; i < EnumColor.DYES.length; i++) {
            /*
             * Balloon
             * Plastic block
             * Slick
             * Glow
             * Reinforced
             * Road
             * Panel
             * Fence
             */

            //Creation
            recipeGenerator.addShapelessRecipe(new ItemStack(MekanismItems.Balloon, 2, i), Items.LEATHER, Items.STRING, "dye" + EnumColor.DYES[i].dyeName);
            //Plastic block creation is separate
            recipeGenerator.addShapedRecipe(new ItemStack(MekanismBlocks.SlickPlasticBlock, 4, i), " P ", "PSP", " P ", 'P', new ItemStack(MekanismBlocks.PlasticBlock, 1, i), 'S', "slimeball");
            recipeGenerator.addShapelessRecipe(new ItemStack(MekanismBlocks.GlowPlasticBlock, 3, i), new ItemStack(MekanismBlocks.PlasticBlock, 1, i), new ItemStack(MekanismBlocks.PlasticBlock, 1, i), new ItemStack(MekanismBlocks.PlasticBlock, 1, i), new ItemStack(Items.GLOWSTONE_DUST));
            recipeGenerator.addShapedRecipe(new ItemStack(MekanismBlocks.ReinforcedPlasticBlock, 4, i), " P ", "POP", " P ", 'P', new ItemStack(MekanismBlocks.PlasticBlock, 1, i), 'O', "dustOsmium");
            recipeGenerator.addShapedRecipe(new ItemStack(MekanismBlocks.RoadPlasticBlock, 3, i), "SSS", "PPP", "SSS", 'S', Blocks.SAND, 'P', new ItemStack(MekanismBlocks.SlickPlasticBlock, 1, i));
            //Panel creation is separate
            recipeGenerator.addShapedRecipe(new ItemStack(MekanismBlocks.PlasticFence, 3, i), "BSB", "BSB", 'B', new ItemStack(MekanismBlocks.PlasticBlock, 1, i), 'S', new ItemStack(MekanismItems.Polyethene, 1, 3));

            //Recolor
            recipeGenerator.addShapelessRecipe(new ItemStack(MekanismItems.Balloon, 1, i), new ItemStack(MekanismItems.Balloon, 1, OreDictionary.WILDCARD_VALUE), "dye" + EnumColor.DYES[i].dyeName);
            recipeGenerator.addShapedRecipe(new ItemStack(MekanismBlocks.PlasticBlock, 4, i), " P ", "PDP", " P ", 'P', new ItemStack(MekanismBlocks.PlasticBlock, 1, OreDictionary.WILDCARD_VALUE), 'D', "dye" + EnumColor.DYES[i].dyeName);
            recipeGenerator.addShapedRecipe(new ItemStack(MekanismBlocks.SlickPlasticBlock, 4, i), " P ", "PDP", " P ", 'P', new ItemStack(MekanismBlocks.SlickPlasticBlock, 1, OreDictionary.WILDCARD_VALUE), 'D', "dye" + EnumColor.DYES[i].dyeName);
            recipeGenerator.addShapedRecipe(new ItemStack(MekanismBlocks.GlowPlasticBlock, 4, i), " P ", "PDP", " P ", 'P', new ItemStack(MekanismBlocks.GlowPlasticBlock, 1, OreDictionary.WILDCARD_VALUE), 'D', "dye" + EnumColor.DYES[i].dyeName);
            recipeGenerator.addShapedRecipe(new ItemStack(MekanismBlocks.ReinforcedPlasticBlock, 4, i), " P ", "PDP", " P ", 'P', new ItemStack(MekanismBlocks.ReinforcedPlasticBlock, 1, OreDictionary.WILDCARD_VALUE), 'D', "dye" + EnumColor.DYES[i].dyeName);
            recipeGenerator.addShapedRecipe(new ItemStack(MekanismBlocks.RoadPlasticBlock, 4, i), " P ", "PDP", " P ", 'P', new ItemStack(MekanismBlocks.RoadPlasticBlock, 1, OreDictionary.WILDCARD_VALUE), 'D', "dye" + EnumColor.DYES[i].dyeName);
            recipeGenerator.addShapedRecipe(new ItemStack(MekanismBlocks.GlowPanel, 4, i), " P ", "PDP", " P ", 'P', new ItemStack(MekanismBlocks.GlowPanel, 1, OreDictionary.WILDCARD_VALUE), 'D', "dye" + EnumColor.DYES[i].dyeName);
            recipeGenerator.addShapedRecipe(new ItemStack(MekanismBlocks.PlasticFence, 4, i), " P ", "PDP", " P ", 'P', new ItemStack(MekanismBlocks.PlasticFence, 1, OreDictionary.WILDCARD_VALUE), 'D', "dye" + EnumColor.DYES[i].dyeName);
        }
    }
}
