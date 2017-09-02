package mekanism.common.recipe.generation;

import mekanism.common.MekanismBlocks;
import mekanism.common.MekanismItems;
import mekanism.common.Tier.BaseTier;
import mekanism.common.Tier.GasTankTier;
import mekanism.common.util.MekanismUtils;
import mekanism.generators.common.GeneratorsBlocks;
import mekanism.generators.common.GeneratorsItems;
import mekanism.generators.common.block.states.BlockStateGenerator.GeneratorType;
import mekanism.generators.common.block.states.BlockStateReactor.ReactorBlockType;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;

public class MekanismGeneratorRecipes {
    public static void generate() {
        RecipeGenerator recipeGenerator = new RecipeGenerator("mekanismgenerators");

        recipeGenerator.addShapedRecipe(GeneratorType.HEAT_GENERATOR.getStack(), "III", "WOW", "CFC", 'I', "ingotIron", 'C', "ingotCopper", 'O', "ingotOsmium", 'F', Blocks.FURNACE, 'W', "plankWood");
        recipeGenerator.addShapedRecipe(GeneratorType.SOLAR_GENERATOR.getStack(), "SSS", "AIA", "PEP", 'S', GeneratorsItems.SolarPanel, 'A', MekanismItems.EnrichedAlloy, 'I', "ingotIron", 'P', "dustOsmium", 'E', MekanismItems.EnergyTablet.getUnchargedItem());
        recipeGenerator.addShapedRecipe(GeneratorType.ADVANCED_SOLAR_GENERATOR.getStack(), "SES", "SES", "III", 'S', GeneratorType.SOLAR_GENERATOR.getStack(), 'E', MekanismItems.EnrichedAlloy, 'I', "ingotIron");
        recipeGenerator.addShapedRecipe(GeneratorType.BIO_GENERATOR.getStack(), "RER", "BCB", "NEN", 'R', "dustRedstone", 'E', MekanismItems.EnrichedAlloy, 'B', MekanismItems.BioFuel, 'C', MekanismUtils.getControlCircuit(BaseTier.BASIC), 'N', "ingotIron");
        recipeGenerator.addShapedRecipe(GeneratorType.GAS_GENERATOR.getStack(), "PEP", "ICI", "PEP", 'P', "ingotOsmium", 'E', MekanismItems.EnrichedAlloy, 'I', new ItemStack(MekanismBlocks.BasicBlock, 1, 8), 'C', MekanismItems.ElectrolyticCore);
        recipeGenerator.addShapedRecipe(new ItemStack(GeneratorsItems.SolarPanel), "GGG", "RAR", "PPP", 'G', "paneGlass", 'R', "dustRedstone", 'A', MekanismItems.EnrichedAlloy, 'P', "ingotOsmium");
        recipeGenerator.addShapedRecipe(new ItemStack(GeneratorsBlocks.Generator, 1, 6), " O ", "OAO", "ECE", 'O', "ingotOsmium", 'A', MekanismItems.EnrichedAlloy, 'E', MekanismItems.EnergyTablet.getUnchargedItem(), 'C', MekanismUtils.getControlCircuit(BaseTier.BASIC));
        recipeGenerator.addShapedRecipe(new ItemStack(GeneratorsItems.TurbineBlade), " S ", "SAS", " S ", 'S', "ingotSteel", 'A', MekanismItems.EnrichedAlloy);
        recipeGenerator.addShapedRecipe(new ItemStack(GeneratorsBlocks.Generator, 1, 7), "SAS", "SAS", "SAS", 'S', "ingotSteel", 'A', MekanismItems.EnrichedAlloy);
        recipeGenerator.addShapedRecipe(new ItemStack(GeneratorsBlocks.Generator, 1, 8), "SAS", "CAC", "SAS", 'S', "ingotSteel", 'A', MekanismItems.EnrichedAlloy, 'C', MekanismUtils.getControlCircuit(BaseTier.ADVANCED));
        recipeGenerator.addShapedRecipe(new ItemStack(GeneratorsBlocks.Generator, 1, 9), "SGS", "GEG", "SGS", 'S', "ingotSteel", 'G', "ingotGold", 'E', MekanismItems.EnergyTablet.getUnchargedItem());
        recipeGenerator.addShapedRecipe(new ItemStack(GeneratorsBlocks.Generator, 4, 10), " S ", "SOS", " S ", 'S', "ingotSteel", 'O', "ingotOsmium");
        recipeGenerator.addShapedRecipe(new ItemStack(GeneratorsBlocks.Generator, 2, 11), " I ", "ICI", " I ", 'I', new ItemStack(GeneratorsBlocks.Generator, 1, 10), 'C', MekanismUtils.getControlCircuit(BaseTier.ADVANCED));
        recipeGenerator.addShapedRecipe(new ItemStack(GeneratorsBlocks.Generator, 2, 12), " I ", "IFI", " I ", 'I', new ItemStack(GeneratorsBlocks.Generator, 1, 10), 'F', Blocks.IRON_BARS);
        recipeGenerator.addShapedRecipe(new ItemStack(GeneratorsBlocks.Generator, 1, 13), "STS", "TBT", "STS", 'S', "ingotSteel", 'T', "ingotTin", 'B', Items.BUCKET);

        //Reactor Recipes
        recipeGenerator.addShapedRecipe(ReactorBlockType.REACTOR_FRAME.getStack(4), " C ", "CAC", " C ", 'C', new ItemStack(MekanismBlocks.BasicBlock, 1, 8), 'A', "alloyUltimate");
        recipeGenerator.addShapedRecipe(ReactorBlockType.REACTOR_PORT.getStack(2), " I ", "ICI", " I ", 'I', ReactorBlockType.REACTOR_FRAME.getStack(1), 'C', MekanismUtils.getControlCircuit(BaseTier.ULTIMATE));
        recipeGenerator.addShapedRecipe(ReactorBlockType.REACTOR_GLASS.getStack(4), " I ", "IGI", " I ", 'I', ReactorBlockType.REACTOR_FRAME.getStack(1), 'G', "blockGlass");
        recipeGenerator.addShapedRecipe(ReactorBlockType.REACTOR_CONTROLLER.getStack(1), "CGC", "ITI", "III", 'C', MekanismUtils.getControlCircuit(BaseTier.ULTIMATE), 'G', "paneGlass", 'I', ReactorBlockType.REACTOR_FRAME.getStack(1), 'T', MekanismUtils.getEmptyGasTank(GasTankTier.BASIC));
        recipeGenerator.addShapedRecipe(ReactorBlockType.LASER_FOCUS_MATRIX.getStack(2), " I ", "ILI", " I ", 'I', ReactorBlockType.REACTOR_GLASS.getStack(1), 'L', "blockRedstone");
        recipeGenerator.addShapedRecipe(ReactorBlockType.REACTOR_LOGIC_ADAPTER.getStack(1), " R ", "RFR", " R ", 'R', "dustRedstone", 'F', ReactorBlockType.REACTOR_FRAME.getStack(1));
    }
}
