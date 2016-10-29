package mekanism.client.jei;

import java.util.ArrayList;
import java.util.List;

import mekanism.api.gas.Gas;
import mekanism.api.gas.GasRegistry;
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
import mekanism.client.jei.machine.other.ElectrolyticSeparatorRecipeCategory;
import mekanism.client.jei.machine.other.ElectrolyticSeparatorRecipeHandler;
import mekanism.client.jei.machine.other.ElectrolyticSeparatorRecipeWrapper;
import mekanism.client.jei.machine.other.MetallurgicInfuserRecipeCategory;
import mekanism.client.jei.machine.other.MetallurgicInfuserRecipeHandler;
import mekanism.client.jei.machine.other.MetallurgicInfuserRecipeWrapper;
import mekanism.client.jei.machine.other.PRCRecipeCategory;
import mekanism.client.jei.machine.other.PRCRecipeHandler;
import mekanism.client.jei.machine.other.PRCRecipeWrapper;
import mekanism.client.jei.machine.other.RotaryCondensentratorRecipeCategory;
import mekanism.client.jei.machine.other.RotaryCondensentratorRecipeHandler;
import mekanism.client.jei.machine.other.RotaryCondensentratorRecipeWrapper;
import mekanism.client.jei.machine.other.SolarNeutronRecipeCategory;
import mekanism.client.jei.machine.other.SolarNeutronRecipeHandler;
import mekanism.client.jei.machine.other.SolarNeutronRecipeWrapper;
import mekanism.client.jei.machine.other.ThermalEvaporationRecipeCategory;
import mekanism.client.jei.machine.other.ThermalEvaporationRecipeHandler;
import mekanism.client.jei.machine.other.ThermalEvaporationRecipeWrapper;
import mekanism.common.MekanismBlocks;
import mekanism.common.MekanismItems;
import mekanism.common.base.IFactory;
import mekanism.common.base.IFactory.RecipeType;
import mekanism.common.base.ITierItem;
import mekanism.common.block.states.BlockStateBasic.BasicBlockType;
import mekanism.common.block.states.BlockStateMachine.MachineType;
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
import mekanism.common.recipe.machines.PressurizedRecipe;
import mekanism.common.recipe.machines.SeparatorRecipe;
import mekanism.common.recipe.machines.SolarNeutronRecipe;
import mekanism.common.recipe.machines.ThermalEvaporationRecipe;
import mekanism.common.recipe.machines.WasherRecipe;
import mekanism.common.util.ItemDataUtils;
import mezz.jei.api.IJeiRuntime;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.IModRegistry;
import mezz.jei.api.ISubtypeRegistry.ISubtypeInterpreter;
import mezz.jei.api.JEIPlugin;
import mezz.jei.api.recipe.IRecipeCategory;
import mezz.jei.api.recipe.IRecipeWrapper;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

@JEIPlugin
public class MekanismJEI implements IModPlugin
{
	public static ISubtypeInterpreter NBT_INTERPRETER = new ISubtypeInterpreter() {
		@Override
		public String getSubtypeInfo(ItemStack itemStack) 
		{
			String ret = Integer.toString(itemStack.getMetadata());
			
			if(itemStack.getItem() instanceof ITierItem)
			{
				ret += ":" + ((ITierItem)itemStack.getItem()).getBaseTier(itemStack).getSimpleName();
			}
			
			if(itemStack.getItem() instanceof IFactory)
			{
				ret += ":" + RecipeType.values()[((IFactory)itemStack.getItem()).getRecipeType(itemStack)].getName();
			}
			
			return ret.isEmpty() ? null : ret.toLowerCase();
		}
	};
	
	@Override
	public void register(IModRegistry registry)
	{
		registry.addAdvancedGuiHandlers(new GuiElementHandler());
		
		registry.addRecipeHandlers(new ShapedMekanismRecipeHandler());
		registry.addRecipeHandlers(new ShapelessMekanismRecipeHandler());
		
		registry.getJeiHelpers().getItemBlacklist().addItemToBlacklist(new ItemStack(MekanismItems.ItemProxy));
		registry.getJeiHelpers().getItemBlacklist().addItemToBlacklist(new ItemStack(MekanismBlocks.BoundingBlock));
		
		registry.getJeiHelpers().getSubtypeRegistry().registerNbtInterpreter(Item.getItemFromBlock(MekanismBlocks.EnergyCube), NBT_INTERPRETER);
		registry.getJeiHelpers().getSubtypeRegistry().registerNbtInterpreter(Item.getItemFromBlock(MekanismBlocks.MachineBlock), NBT_INTERPRETER);
		registry.getJeiHelpers().getSubtypeRegistry().registerNbtInterpreter(Item.getItemFromBlock(MekanismBlocks.MachineBlock2), NBT_INTERPRETER);
		registry.getJeiHelpers().getSubtypeRegistry().registerNbtInterpreter(Item.getItemFromBlock(MekanismBlocks.MachineBlock3), NBT_INTERPRETER);
		registry.getJeiHelpers().getSubtypeRegistry().registerNbtInterpreter(Item.getItemFromBlock(MekanismBlocks.BasicBlock), NBT_INTERPRETER);
		registry.getJeiHelpers().getSubtypeRegistry().registerNbtInterpreter(Item.getItemFromBlock(MekanismBlocks.BasicBlock2), NBT_INTERPRETER);
		registry.getJeiHelpers().getSubtypeRegistry().registerNbtInterpreter(Item.getItemFromBlock(MekanismBlocks.GasTank), NBT_INTERPRETER);
		registry.getJeiHelpers().getSubtypeRegistry().registerNbtInterpreter(Item.getItemFromBlock(MekanismBlocks.CardboardBox), NBT_INTERPRETER);
		
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
			
			SolarNeutronRecipeCategory solarNeutronCategory = new SolarNeutronRecipeCategory(registry.getJeiHelpers().getGuiHelper());
			registry.addRecipeCategories(solarNeutronCategory);
			registry.addRecipeHandlers(new SolarNeutronRecipeHandler(solarNeutronCategory));
			addRecipes(registry, Recipe.SOLAR_NEUTRON_ACTIVATOR, solarNeutronCategory, SolarNeutronRecipe.class, SolarNeutronRecipeCategory.class, SolarNeutronRecipeWrapper.class);
			
			ElectrolyticSeparatorRecipeCategory electrolyticSeparatorCategory = new ElectrolyticSeparatorRecipeCategory(registry.getJeiHelpers().getGuiHelper());
			registry.addRecipeCategories(electrolyticSeparatorCategory);
			registry.addRecipeHandlers(new ElectrolyticSeparatorRecipeHandler(electrolyticSeparatorCategory));
			addRecipes(registry, Recipe.ELECTROLYTIC_SEPARATOR, electrolyticSeparatorCategory, SeparatorRecipe.class, ElectrolyticSeparatorRecipeCategory.class, ElectrolyticSeparatorRecipeWrapper.class);
			
			ThermalEvaporationRecipeCategory thermalEvaporationCategory = new ThermalEvaporationRecipeCategory(registry.getJeiHelpers().getGuiHelper());
			registry.addRecipeCategories(thermalEvaporationCategory);
			registry.addRecipeHandlers(new ThermalEvaporationRecipeHandler(thermalEvaporationCategory));
			addRecipes(registry, Recipe.THERMAL_EVAPORATION_PLANT, thermalEvaporationCategory, ThermalEvaporationRecipe.class, ThermalEvaporationRecipeCategory.class, ThermalEvaporationRecipeWrapper.class);
			
			PRCRecipeCategory prcCategory = new PRCRecipeCategory(registry.getJeiHelpers().getGuiHelper());
			registry.addRecipeCategories(prcCategory);
			registry.addRecipeHandlers(new PRCRecipeHandler(prcCategory));
			addRecipes(registry, Recipe.PRESSURIZED_REACTION_CHAMBER, prcCategory, PressurizedRecipe.class, PRCRecipeCategory.class, PRCRecipeWrapper.class);
		} catch(Exception e) {
			e.printStackTrace();
		}
		
		List<RotaryCondensentratorRecipeWrapper> condensentratorRecipes = new ArrayList<RotaryCondensentratorRecipeWrapper>();
		
		RotaryCondensentratorRecipeCategory rotaryCondensentratorCategory = new RotaryCondensentratorRecipeCategory(registry.getJeiHelpers().getGuiHelper());
		registry.addRecipeCategories(rotaryCondensentratorCategory);
		registry.addRecipeHandlers(new RotaryCondensentratorRecipeHandler(rotaryCondensentratorCategory));
		
		for(Gas gas : GasRegistry.getRegisteredGasses())
		{
			if(gas.hasFluid())
			{
				condensentratorRecipes.add(new RotaryCondensentratorRecipeWrapper(gas.getFluid(), gas, true, rotaryCondensentratorCategory));
				condensentratorRecipes.add(new RotaryCondensentratorRecipeWrapper(gas.getFluid(), gas, false, rotaryCondensentratorCategory));
			}
		}
		
		registry.addRecipes(condensentratorRecipes);
		
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
		
		registerRecipeItem(registry, MachineType.ENRICHMENT_CHAMBER);
		registerRecipeItem(registry, MachineType.CRUSHER);
		registerRecipeItem(registry, MachineType.COMBINER);
		registerRecipeItem(registry, MachineType.PURIFICATION_CHAMBER);
		registerRecipeItem(registry, MachineType.OSMIUM_COMPRESSOR);
		registerRecipeItem(registry, MachineType.CHEMICAL_INJECTION_CHAMBER);
		registerRecipeItem(registry, MachineType.PRECISION_SAWMILL);
		registerRecipeItem(registry, MachineType.METALLURGIC_INFUSER);
		registerRecipeItem(registry, MachineType.CHEMICAL_CRYSTALLIZER);
		registerRecipeItem(registry, MachineType.CHEMICAL_DISSOLUTION_CHAMBER);
		registerRecipeItem(registry, MachineType.CHEMICAL_INFUSER);
		registerRecipeItem(registry, MachineType.CHEMICAL_OXIDIZER);
		registerRecipeItem(registry, MachineType.CHEMICAL_WASHER);
		registerRecipeItem(registry, MachineType.SOLAR_NEUTRON_ACTIVATOR);
		registerRecipeItem(registry, MachineType.ELECTROLYTIC_SEPARATOR);
		registerRecipeItem(registry, MachineType.PRESSURIZED_REACTION_CHAMBER);
		registerRecipeItem(registry, MachineType.ROTARY_CONDENSENTRATOR);
		
		registry.addRecipeCategoryCraftingItem(BasicBlockType.THERMAL_EVAPORATION_CONTROLLER.getStack(1), "mekanism.thermal_evaporation_plant");
	}
	
	private void registerRecipeItem(IModRegistry registry, MachineType type)
	{
		registry.addRecipeCategoryCraftingItem(type.getStack(), "mekanism." + type.getName());
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
	public void onRuntimeAvailable(IJeiRuntime runtime) {}
}
