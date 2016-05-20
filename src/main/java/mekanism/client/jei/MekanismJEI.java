package mekanism.client.jei;

import java.util.ArrayList;
import java.util.List;

import mekanism.client.gui.GuiChemicalCrystallizer;
import mekanism.client.gui.GuiChemicalDissolutionChamber;
import mekanism.client.gui.GuiChemicalInfuser;
import mekanism.client.gui.GuiChemicalInjectionChamber;
import mekanism.client.gui.GuiChemicalOxidizer;
import mekanism.client.gui.GuiChemicalWasher;
import mekanism.client.gui.GuiCombiner;
import mekanism.client.gui.GuiCrusher;
import mekanism.client.gui.GuiElectrolyticSeparator;
import mekanism.client.gui.GuiEnrichmentChamber;
import mekanism.client.gui.GuiMetallurgicInfuser;
import mekanism.client.gui.GuiOsmiumCompressor;
import mekanism.client.gui.GuiPRC;
import mekanism.client.gui.GuiPrecisionSawmill;
import mekanism.client.gui.GuiPurificationChamber;
import mekanism.client.gui.GuiRotaryCondensentrator;
import mekanism.client.gui.GuiSolarNeutronActivator;
import mekanism.client.gui.GuiThermalEvaporationController;
import mekanism.client.gui.element.GuiProgress.ProgressBar;
import mekanism.client.jei.crafting.ShapedMekanismRecipeHandler;
import mekanism.client.jei.crafting.ShapelessMekanismRecipeHandler;
import mekanism.client.jei.machine.AdvancedMachineRecipeCategory;
import mekanism.client.jei.machine.AdvancedMachineRecipeHandler;
import mekanism.client.jei.machine.AdvancedMachineRecipeWrapper;
import mekanism.client.jei.machine.ChanceMachineRecipeCategory;
import mekanism.client.jei.machine.ChanceMachineRecipeHandler;
import mekanism.client.jei.machine.ChanceMachineRecipeWrapper;
import mekanism.client.jei.machine.MachineRecipeCategory;
import mekanism.client.jei.machine.MachineRecipeHandler;
import mekanism.client.jei.machine.MachineRecipeWrapper;
import mekanism.client.jei.machine.advanced.ChemicalInjectionChamberRecipeHandler;
import mekanism.client.jei.machine.advanced.ChemicalInjectionChamberRecipeWrapper;
import mekanism.client.jei.machine.advanced.CombinerRecipeHandler;
import mekanism.client.jei.machine.advanced.CombinerRecipeWrapper;
import mekanism.client.jei.machine.advanced.OsmiumCompressorRecipeHandler;
import mekanism.client.jei.machine.advanced.OsmiumCompressorRecipeWrapper;
import mekanism.client.jei.machine.advanced.PurificationChamberRecipeHandler;
import mekanism.client.jei.machine.advanced.PurificationChamberRecipeWrapper;
import mekanism.client.jei.machine.basic.CrusherRecipeHandler;
import mekanism.client.jei.machine.basic.CrusherRecipeWrapper;
import mekanism.client.jei.machine.basic.EnrichmentRecipeHandler;
import mekanism.client.jei.machine.basic.EnrichmentRecipeWrapper;
import mekanism.client.jei.machine.chance.PrecisionSawmillRecipeHandler;
import mekanism.client.jei.machine.chance.PrecisionSawmillRecipeWrapper;
import mekanism.client.jei.machine.chemical.ChemicalCrystallizerRecipeCategory;
import mekanism.client.jei.machine.chemical.ChemicalCrystallizerRecipeHandler;
import mekanism.client.jei.machine.chemical.ChemicalCrystallizerRecipeWrapper;
import mekanism.client.jei.machine.chemical.ChemicalDissolutionChamberRecipeCategory;
import mekanism.client.jei.machine.chemical.ChemicalDissolutionChamberRecipeHandler;
import mekanism.client.jei.machine.chemical.ChemicalDissolutionChamberRecipeWrapper;
import mekanism.client.jei.machine.chemical.ChemicalInfuserRecipeCategory;
import mekanism.client.jei.machine.chemical.ChemicalInfuserRecipeHandler;
import mekanism.client.jei.machine.chemical.ChemicalInfuserRecipeWrapper;
import mekanism.client.jei.machine.chemical.ChemicalOxidizerRecipeCategory;
import mekanism.client.jei.machine.chemical.ChemicalOxidizerRecipeHandler;
import mekanism.client.jei.machine.chemical.ChemicalOxidizerRecipeWrapper;
import mekanism.client.jei.machine.chemical.ChemicalWasherRecipeCategory;
import mekanism.client.jei.machine.chemical.ChemicalWasherRecipeHandler;
import mekanism.client.jei.machine.chemical.ChemicalWasherRecipeWrapper;
import mekanism.client.jei.machine.other.MetallurgicInfuserRecipeCategory;
import mekanism.client.jei.machine.other.MetallurgicInfuserRecipeHandler;
import mekanism.client.jei.machine.other.MetallurgicInfuserRecipeWrapper;
import mekanism.common.MekanismBlocks;
import mekanism.common.MekanismItems;
import mekanism.common.recipe.RecipeHandler.Recipe;
import mekanism.common.recipe.machines.AdvancedMachineRecipe;
import mekanism.common.recipe.machines.BasicMachineRecipe;
import mekanism.common.recipe.machines.ChanceMachineRecipe;
import mekanism.common.recipe.machines.ChemicalInfuserRecipe;
import mekanism.common.recipe.machines.CrystallizerRecipe;
import mekanism.common.recipe.machines.DissolutionRecipe;
import mekanism.common.recipe.machines.MachineRecipe;
import mekanism.common.recipe.machines.MetallurgicInfuserRecipe;
import mekanism.common.recipe.machines.OxidationRecipe;
import mekanism.common.recipe.machines.WasherRecipe;
import mekanism.common.util.ItemDataUtils;
import mezz.jei.api.IItemRegistry;
import mezz.jei.api.IJeiHelpers;
import mezz.jei.api.IJeiRuntime;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.IModRegistry;
import mezz.jei.api.IRecipeRegistry;
import mezz.jei.api.JEIPlugin;
import mezz.jei.api.recipe.IRecipeCategory;
import mezz.jei.api.recipe.IRecipeWrapper;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

@JEIPlugin
public class MekanismJEI implements IModPlugin
{
	public static final String[] UNUSED_TAGS = new String[] {ItemDataUtils.DATA_ID};
	
	@Override
	public void register(IModRegistry registry)
	{
		registry.addAdvancedGuiHandlers(new GuiElementHandler());
		
		registry.addRecipeHandlers(new ShapedMekanismRecipeHandler());
		registry.addRecipeHandlers(new ShapelessMekanismRecipeHandler());
		
		registry.getJeiHelpers().getItemBlacklist().addItemToBlacklist(new ItemStack(MekanismItems.ItemProxy));
		registry.getJeiHelpers().getItemBlacklist().addItemToBlacklist(new ItemStack(MekanismBlocks.BoundingBlock));
		
		registry.getJeiHelpers().getNbtIgnoreList().ignoreNbtTagNames(Item.getItemFromBlock(MekanismBlocks.EnergyCube), UNUSED_TAGS);
		registry.getJeiHelpers().getNbtIgnoreList().ignoreNbtTagNames(Item.getItemFromBlock(MekanismBlocks.MachineBlock), UNUSED_TAGS);
		registry.getJeiHelpers().getNbtIgnoreList().ignoreNbtTagNames(Item.getItemFromBlock(MekanismBlocks.MachineBlock2), UNUSED_TAGS);
		registry.getJeiHelpers().getNbtIgnoreList().ignoreNbtTagNames(Item.getItemFromBlock(MekanismBlocks.MachineBlock3), UNUSED_TAGS);
		registry.getJeiHelpers().getNbtIgnoreList().ignoreNbtTagNames(Item.getItemFromBlock(MekanismBlocks.BasicBlock), UNUSED_TAGS);
		registry.getJeiHelpers().getNbtIgnoreList().ignoreNbtTagNames(Item.getItemFromBlock(MekanismBlocks.BasicBlock2), UNUSED_TAGS);
		registry.getJeiHelpers().getNbtIgnoreList().ignoreNbtTagNames(Item.getItemFromBlock(MekanismBlocks.GasTank), UNUSED_TAGS);
		registry.getJeiHelpers().getNbtIgnoreList().ignoreNbtTagNames(Item.getItemFromBlock(MekanismBlocks.CardboardBox), UNUSED_TAGS);
		
		try {
			registerBasicMachine(registry, Recipe.ENRICHMENT_CHAMBER, "tile.MachineBlock.EnrichmentChamber.name", ProgressBar.BLUE, EnrichmentRecipeHandler.class, EnrichmentRecipeWrapper.class);
			registerBasicMachine(registry, Recipe.CRUSHER, "tile.MachineBlock.Crusher.name", ProgressBar.CRUSH, CrusherRecipeHandler.class, CrusherRecipeWrapper.class);
			
			registerAdvancedMachine(registry, Recipe.COMBINER, "tile.MachineBlock.Combiner.name", ProgressBar.STONE, CombinerRecipeHandler.class, CombinerRecipeWrapper.class);
			registerAdvancedMachine(registry, Recipe.PURIFICATION_CHAMBER, "tile.MachineBlock.PurificationChamber.name", ProgressBar.RED, PurificationChamberRecipeHandler.class, PurificationChamberRecipeWrapper.class);
			registerAdvancedMachine(registry, Recipe.OSMIUM_COMPRESSOR, "tile.MachineBlock.OsmiumCompressor.name", ProgressBar.RED, OsmiumCompressorRecipeHandler.class, OsmiumCompressorRecipeWrapper.class);
			registerAdvancedMachine(registry, Recipe.CHEMICAL_INJECTION_CHAMBER, "nei.chemicalInjectionChamber", ProgressBar.YELLOW, ChemicalInjectionChamberRecipeHandler.class, ChemicalInjectionChamberRecipeWrapper.class);
			
			registerChanceMachine(registry, Recipe.PRECISION_SAWMILL, "tile.MachineBlock2.PrecisionSawmill.name", ProgressBar.PURPLE, PrecisionSawmillRecipeHandler.class, PrecisionSawmillRecipeWrapper.class);
			
			MetallurgicInfuserRecipeCategory metallurgicInfuserCategory = new MetallurgicInfuserRecipeCategory(registry.getJeiHelpers().getGuiHelper());
			registry.addRecipeCategories(metallurgicInfuserCategory);
			registry.addRecipeHandlers(new MetallurgicInfuserRecipeHandler(metallurgicInfuserCategory));
			addRecipes(registry, Recipe.METALLURGIC_INFUSER, metallurgicInfuserCategory, MetallurgicInfuserRecipe.class, MetallurgicInfuserRecipeCategory.class, MetallurgicInfuserRecipeWrapper.class);
			
			ChemicalCrystallizerRecipeCategory chemicalCrystallizerCategory = new ChemicalCrystallizerRecipeCategory(registry.getJeiHelpers().getGuiHelper());
			registry.addRecipeCategories(chemicalCrystallizerCategory);
			registry.addRecipeHandlers(new ChemicalCrystallizerRecipeHandler(chemicalCrystallizerCategory));
			addRecipes(registry, Recipe.CHEMICAL_CRYSTALLIZER, chemicalCrystallizerCategory, CrystallizerRecipe.class, ChemicalCrystallizerRecipeCategory.class, ChemicalCrystallizerRecipeWrapper.class);
			
			ChemicalDissolutionChamberRecipeCategory chemicalDissolutionChamberCategory = new ChemicalDissolutionChamberRecipeCategory(registry.getJeiHelpers().getGuiHelper());
			registry.addRecipeCategories(chemicalDissolutionChamberCategory);
			registry.addRecipeHandlers(new ChemicalDissolutionChamberRecipeHandler(chemicalDissolutionChamberCategory));
			addRecipes(registry, Recipe.CHEMICAL_DISSOLUTION_CHAMBER, chemicalDissolutionChamberCategory, DissolutionRecipe.class, ChemicalDissolutionChamberRecipeCategory.class, ChemicalDissolutionChamberRecipeWrapper.class);
			
			ChemicalInfuserRecipeCategory chemicalInfuserCategory = new ChemicalInfuserRecipeCategory(registry.getJeiHelpers().getGuiHelper());
			registry.addRecipeCategories(chemicalInfuserCategory);
			registry.addRecipeHandlers(new ChemicalInfuserRecipeHandler(chemicalInfuserCategory));
			addRecipes(registry, Recipe.CHEMICAL_INFUSER, chemicalInfuserCategory, ChemicalInfuserRecipe.class, ChemicalInfuserRecipeCategory.class, ChemicalInfuserRecipeWrapper.class);
			
			ChemicalOxidizerRecipeCategory chemicalOxidizerCategory = new ChemicalOxidizerRecipeCategory(registry.getJeiHelpers().getGuiHelper());
			registry.addRecipeCategories(chemicalOxidizerCategory);
			registry.addRecipeHandlers(new ChemicalOxidizerRecipeHandler(chemicalOxidizerCategory));
			addRecipes(registry, Recipe.CHEMICAL_OXIDIZER, chemicalOxidizerCategory, OxidationRecipe.class, ChemicalOxidizerRecipeCategory.class, ChemicalOxidizerRecipeWrapper.class);
			
			ChemicalWasherRecipeCategory chemicalWasherCategory = new ChemicalWasherRecipeCategory(registry.getJeiHelpers().getGuiHelper());
			registry.addRecipeCategories(chemicalWasherCategory);
			registry.addRecipeHandlers(new ChemicalWasherRecipeHandler(chemicalWasherCategory));
			addRecipes(registry, Recipe.CHEMICAL_WASHER, chemicalWasherCategory, WasherRecipe.class, ChemicalWasherRecipeCategory.class, ChemicalWasherRecipeWrapper.class);
		} catch(Exception e) {
			e.printStackTrace();
		}
		
		registry.addRecipeClickArea(GuiEnrichmentChamber.class, 79, 40, 24, 7, "mekanism.enrichment_chamber");
		registry.addRecipeClickArea(GuiCrusher.class, 79, 40, 24, 7, "mekanism.crusher");
		registry.addRecipeClickArea(GuiCombiner.class, 79, 40, 24, 7, "mekanism.combiner");
		registry.addRecipeClickArea(GuiPurificationChamber.class, 79, 40, 24, 7, "mekanism.purification_chamber");
		registry.addRecipeClickArea(GuiOsmiumCompressor.class, 79, 40, 24, 7, "mekanism.osmium_compressor");
		registry.addRecipeClickArea(GuiChemicalInjectionChamber.class, 79, 40, 24, 7, "mekanism.chemical_injection_chamber");
		registry.addRecipeClickArea(GuiPrecisionSawmill.class, 79, 40, 24, 7, "mekanism.precision_sawmill");
		registry.addRecipeClickArea(GuiMetallurgicInfuser.class, 72, 47, 32, 8, "mekanism.metallurgic_infuser");
		registry.addRecipeClickArea(GuiChemicalCrystallizer.class, 53, 62, 48, 8, "mekanism.chemical_crystallizer");
		registry.addRecipeClickArea(GuiChemicalDissolutionChamber.class, 64, 40, 48, 8, "mekanism.chemical_dissolution_chamber");
		registry.addRecipeClickArea(GuiChemicalInfuser.class, 47, 39, 28, 8, "mekanism.chemical_infuser");
		registry.addRecipeClickArea(GuiChemicalInfuser.class, 101, 39, 28, 8, "mekanism.chemical_infuser");
		registry.addRecipeClickArea(GuiChemicalOxidizer.class, 64, 40, 48, 8, "mekanism.chemical_oxidizer");
		registry.addRecipeClickArea(GuiChemicalWasher.class, 61, 39, 55, 8, "mekanism.chemical_washer");
		registry.addRecipeClickArea(GuiSolarNeutronActivator.class, 64, 39, 48, 8, "mekanism.solar_neutron_activator");
		registry.addRecipeClickArea(GuiElectrolyticSeparator.class, 80, 30, 16, 6, "mekanism.electrolytic_separator");
		registry.addRecipeClickArea(GuiThermalEvaporationController.class, 49, 20, 78, 38, "mekanism.thermal_evaporation_plant");
		registry.addRecipeClickArea(GuiPRC.class, 75, 37, 36, 10, "mekanism.pressurized_reaction_chamber");
		registry.addRecipeClickArea(GuiRotaryCondensentrator.class, 64, 39, 48, 8, "mekanism.rotary_condensentrator");
	}
	
	private void registerBasicMachine(IModRegistry registry, Recipe recipe, String unlocalized, ProgressBar bar, Class<? extends MachineRecipeHandler> handler, Class<? extends MachineRecipeWrapper> wrapper) throws Exception
	{
		MachineRecipeCategory category = new MachineRecipeCategory(registry.getJeiHelpers().getGuiHelper(), recipe.name().toLowerCase(), unlocalized, bar);
		
		registry.addRecipeCategories(category);
		registry.addRecipeHandlers(handler.getConstructor(MachineRecipeCategory.class).newInstance(category));
		
		addRecipes(registry, recipe, category, BasicMachineRecipe.class, MachineRecipeCategory.class, wrapper);
	}
	
	private void registerAdvancedMachine(IModRegistry registry, Recipe recipe, String unlocalized, ProgressBar bar, Class<? extends AdvancedMachineRecipeHandler> handler, Class<? extends AdvancedMachineRecipeWrapper> wrapper) throws Exception
	{
		AdvancedMachineRecipeCategory category = new AdvancedMachineRecipeCategory(registry.getJeiHelpers().getGuiHelper(), recipe.name().toLowerCase(), unlocalized, bar);
		
		registry.addRecipeCategories(category);
		registry.addRecipeHandlers(handler.getConstructor(AdvancedMachineRecipeCategory.class).newInstance(category));
		
		addRecipes(registry, recipe, category, AdvancedMachineRecipe.class, AdvancedMachineRecipeCategory.class, wrapper);
	}
	
	private void registerChanceMachine(IModRegistry registry, Recipe recipe, String unlocalized, ProgressBar bar, Class<? extends ChanceMachineRecipeHandler> handler, Class<? extends ChanceMachineRecipeWrapper> wrapper) throws Exception
	{
		ChanceMachineRecipeCategory category = new ChanceMachineRecipeCategory(registry.getJeiHelpers().getGuiHelper(), recipe.name().toLowerCase(), unlocalized, bar);
		
		registry.addRecipeCategories(category);
		registry.addRecipeHandlers(handler.getConstructor(ChanceMachineRecipeCategory.class).newInstance(category));
		
		addRecipes(registry, recipe, category, ChanceMachineRecipe.class, ChanceMachineRecipeCategory.class, wrapper);
	}
	
	private void addRecipes(IModRegistry registry, Recipe type, IRecipeCategory cat, Class recipe, Class category, Class<? extends IRecipeWrapper> wrapper) throws Exception
	{
		List<IRecipeWrapper> recipes = new ArrayList<IRecipeWrapper>();
		
		for(Object obj : type.get().values())
		{
			if(obj instanceof MachineRecipe)
			{
				recipes.add(wrapper.getConstructor(recipe, category).newInstance(obj, cat));
			}
		}
		
		registry.addRecipes(recipes);
	}
	
	@Override
	public void onItemRegistryAvailable(IItemRegistry registry) {}

	@Override
	public void onJeiHelpersAvailable(IJeiHelpers helpers) {}

	@Override
	public void onRecipeRegistryAvailable(IRecipeRegistry registry) {}

	@Override
	public void onRuntimeAvailable(IJeiRuntime runtime) {}
}
