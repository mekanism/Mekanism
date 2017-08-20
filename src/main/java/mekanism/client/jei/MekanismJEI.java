package mekanism.client.jei;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import mekanism.api.gas.Gas;
import mekanism.api.gas.GasRegistry;
import mekanism.api.gas.GasStack;
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
import mekanism.client.jei.gas.GasStackRenderer;
import mekanism.client.jei.machine.AdvancedMachineRecipeCategory;
import mekanism.client.jei.machine.AdvancedMachineRecipeWrapper;
import mekanism.client.jei.machine.BaseRecipeHandler;
import mekanism.client.jei.machine.ChanceMachineRecipeCategory;
import mekanism.client.jei.machine.ChanceMachineRecipeWrapper;
import mekanism.client.jei.machine.MachineRecipeCategory;
import mekanism.client.jei.machine.MachineRecipeWrapper;
import mekanism.client.jei.machine.advanced.ChemicalInjectionChamberRecipeWrapper;
import mekanism.client.jei.machine.advanced.CombinerRecipeWrapper;
import mekanism.client.jei.machine.advanced.OsmiumCompressorRecipeWrapper;
import mekanism.client.jei.machine.advanced.PurificationChamberRecipeWrapper;
import mekanism.client.jei.machine.basic.CrusherRecipeWrapper;
import mekanism.client.jei.machine.basic.EnrichmentRecipeWrapper;
import mekanism.client.jei.machine.chance.PrecisionSawmillRecipeWrapper;
import mekanism.client.jei.machine.chemical.ChemicalCrystallizerRecipeCategory;
import mekanism.client.jei.machine.chemical.ChemicalCrystallizerRecipeWrapper;
import mekanism.client.jei.machine.chemical.ChemicalDissolutionChamberRecipeCategory;
import mekanism.client.jei.machine.chemical.ChemicalDissolutionChamberRecipeWrapper;
import mekanism.client.jei.machine.chemical.ChemicalInfuserRecipeCategory;
import mekanism.client.jei.machine.chemical.ChemicalInfuserRecipeWrapper;
import mekanism.client.jei.machine.chemical.ChemicalOxidizerRecipeCategory;
import mekanism.client.jei.machine.chemical.ChemicalOxidizerRecipeWrapper;
import mekanism.client.jei.machine.chemical.ChemicalWasherRecipeCategory;
import mekanism.client.jei.machine.chemical.ChemicalWasherRecipeWrapper;
import mekanism.client.jei.machine.other.ElectrolyticSeparatorRecipeCategory;
import mekanism.client.jei.machine.other.ElectrolyticSeparatorRecipeWrapper;
import mekanism.client.jei.machine.other.MetallurgicInfuserRecipeCategory;
import mekanism.client.jei.machine.other.MetallurgicInfuserRecipeWrapper;
import mekanism.client.jei.machine.other.PRCRecipeCategory;
import mekanism.client.jei.machine.other.PRCRecipeWrapper;
import mekanism.client.jei.machine.other.RotaryCondensentratorRecipeCategory;
import mekanism.client.jei.machine.other.RotaryCondensentratorRecipeWrapper;
import mekanism.client.jei.machine.other.SolarNeutronRecipeCategory;
import mekanism.client.jei.machine.other.SolarNeutronRecipeWrapper;
import mekanism.client.jei.machine.other.ThermalEvaporationRecipeCategory;
import mekanism.client.jei.machine.other.ThermalEvaporationRecipeWrapper;
import mekanism.common.MekanismBlocks;
import mekanism.common.MekanismItems;
import mekanism.common.base.IFactory;
import mekanism.common.base.IFactory.RecipeType;
import mekanism.common.base.ITierItem;
import mekanism.common.block.states.BlockStateBasic.BasicBlockType;
import mekanism.common.block.states.BlockStateMachine.MachineType;
import mekanism.common.item.ItemBlockEnergyCube;
import mekanism.common.item.ItemBlockGasTank;
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
import mezz.jei.api.BlankModPlugin;
import mezz.jei.api.IModRegistry;
import mezz.jei.api.ISubtypeRegistry;
import mezz.jei.api.ISubtypeRegistry.ISubtypeInterpreter;
import mezz.jei.api.JEIPlugin;
import mezz.jei.api.ingredients.IModIngredientRegistration;
import mezz.jei.api.recipe.IRecipeCategory;
import mezz.jei.api.recipe.IRecipeWrapper;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fluids.Fluid;

@JEIPlugin
public class MekanismJEI extends BlankModPlugin
{
	public static GasStackRenderer GAS_RENDERER;

	public static final ISubtypeInterpreter NBT_INTERPRETER = itemStack ->
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

        if(itemStack.getItem() instanceof ItemBlockGasTank)
		{
			GasStack gasStack = ((ItemBlockGasTank)itemStack.getItem()).getGas(itemStack);
			if (gasStack != null)
			{
				ret += ":" + gasStack.getGas().getName();
			}
		}

		if(itemStack.getItem() instanceof ItemBlockEnergyCube)
		{
			ret += ":" + (((ItemBlockEnergyCube)itemStack.getItem()).getEnergy(itemStack) > 0 ? "filled" : "empty");
		}

        return ret.isEmpty() ? null : ret.toLowerCase();
    };
	
	@Override
	public void registerItemSubtypes(ISubtypeRegistry registry)
	{
		registry.registerSubtypeInterpreter(Item.getItemFromBlock(MekanismBlocks.EnergyCube), NBT_INTERPRETER);
		registry.registerSubtypeInterpreter(Item.getItemFromBlock(MekanismBlocks.MachineBlock), NBT_INTERPRETER);
		registry.registerSubtypeInterpreter(Item.getItemFromBlock(MekanismBlocks.MachineBlock2), NBT_INTERPRETER);
		registry.registerSubtypeInterpreter(Item.getItemFromBlock(MekanismBlocks.MachineBlock3), NBT_INTERPRETER);
		registry.registerSubtypeInterpreter(Item.getItemFromBlock(MekanismBlocks.BasicBlock), NBT_INTERPRETER);
		registry.registerSubtypeInterpreter(Item.getItemFromBlock(MekanismBlocks.BasicBlock2), NBT_INTERPRETER);
		registry.registerSubtypeInterpreter(Item.getItemFromBlock(MekanismBlocks.GasTank), NBT_INTERPRETER);
		registry.registerSubtypeInterpreter(Item.getItemFromBlock(MekanismBlocks.CardboardBox), NBT_INTERPRETER);
		registry.registerSubtypeInterpreter(Item.getItemFromBlock(MekanismBlocks.Transmitter), NBT_INTERPRETER);
	}
	
	@Override
	public void registerIngredients(IModIngredientRegistration registry)
	{
		List<GasStack> list = GasRegistry.getRegisteredGasses().stream().filter(g -> g.isVisible()).map(g -> new GasStack(g, Fluid.BUCKET_VOLUME)).collect(Collectors.toList());
		registry.register(GasStack.class, list, new GasStackHelper(), GAS_RENDERER = new GasStackRenderer());
	}
	
	@Override
	public void register(IModRegistry registry)
	{
		registry.addAdvancedGuiHandlers(new GuiElementHandler());
		
		registry.addRecipeHandlers(new ShapedMekanismRecipeHandler(registry.getJeiHelpers()));
		registry.addRecipeHandlers(new ShapelessMekanismRecipeHandler(registry.getJeiHelpers()));
		
		registry.getJeiHelpers().getIngredientBlacklist().addIngredientToBlacklist(new ItemStack(MekanismItems.ItemProxy));
		registry.getJeiHelpers().getIngredientBlacklist().addIngredientToBlacklist(new ItemStack(MekanismBlocks.BoundingBlock));
		
		try {
			registerBasicMachine(registry, Recipe.ENRICHMENT_CHAMBER, "tile.MachineBlock.EnrichmentChamber.name", ProgressBar.BLUE, EnrichmentRecipeWrapper.class);
			registerBasicMachine(registry, Recipe.CRUSHER, "tile.MachineBlock.Crusher.name", ProgressBar.CRUSH, CrusherRecipeWrapper.class);
			
			registerAdvancedMachine(registry, Recipe.COMBINER, "tile.MachineBlock.Combiner.name", ProgressBar.STONE, CombinerRecipeWrapper.class);
			registerAdvancedMachine(registry, Recipe.PURIFICATION_CHAMBER, "tile.MachineBlock.PurificationChamber.name", ProgressBar.RED, PurificationChamberRecipeWrapper.class);
			registerAdvancedMachine(registry, Recipe.OSMIUM_COMPRESSOR, "tile.MachineBlock.OsmiumCompressor.name", ProgressBar.RED, OsmiumCompressorRecipeWrapper.class);
			registerAdvancedMachine(registry, Recipe.CHEMICAL_INJECTION_CHAMBER, "nei.chemicalInjectionChamber", ProgressBar.YELLOW, ChemicalInjectionChamberRecipeWrapper.class);
			
			registerChanceMachine(registry, Recipe.PRECISION_SAWMILL, "tile.MachineBlock2.PrecisionSawmill.name", ProgressBar.PURPLE, PrecisionSawmillRecipeWrapper.class);
			
			MetallurgicInfuserRecipeCategory metallurgicInfuserCategory = new MetallurgicInfuserRecipeCategory(registry.getJeiHelpers().getGuiHelper());
			registry.addRecipeCategories(metallurgicInfuserCategory);
			registry.addRecipeHandlers(new BaseRecipeHandler(metallurgicInfuserCategory, MetallurgicInfuserRecipeWrapper.class));
			addRecipes(registry, Recipe.METALLURGIC_INFUSER, metallurgicInfuserCategory, MetallurgicInfuserRecipe.class, MetallurgicInfuserRecipeCategory.class, MetallurgicInfuserRecipeWrapper.class);
			
			ChemicalCrystallizerRecipeCategory chemicalCrystallizerCategory = new ChemicalCrystallizerRecipeCategory(registry.getJeiHelpers().getGuiHelper());
			registry.addRecipeCategories(chemicalCrystallizerCategory);
			registry.addRecipeHandlers(new BaseRecipeHandler(chemicalCrystallizerCategory, ChemicalCrystallizerRecipeWrapper.class));
			addRecipes(registry, Recipe.CHEMICAL_CRYSTALLIZER, chemicalCrystallizerCategory, CrystallizerRecipe.class, ChemicalCrystallizerRecipeCategory.class, ChemicalCrystallizerRecipeWrapper.class);
			
			ChemicalDissolutionChamberRecipeCategory chemicalDissolutionChamberCategory = new ChemicalDissolutionChamberRecipeCategory(registry.getJeiHelpers().getGuiHelper());
			registry.addRecipeCategories(chemicalDissolutionChamberCategory);
			registry.addRecipeHandlers(new BaseRecipeHandler(chemicalDissolutionChamberCategory, ChemicalDissolutionChamberRecipeWrapper.class));
			addRecipes(registry, Recipe.CHEMICAL_DISSOLUTION_CHAMBER, chemicalDissolutionChamberCategory, DissolutionRecipe.class, ChemicalDissolutionChamberRecipeCategory.class, ChemicalDissolutionChamberRecipeWrapper.class);
			
			ChemicalInfuserRecipeCategory chemicalInfuserCategory = new ChemicalInfuserRecipeCategory(registry.getJeiHelpers().getGuiHelper());
			registry.addRecipeCategories(chemicalInfuserCategory);
			registry.addRecipeHandlers(new BaseRecipeHandler(chemicalInfuserCategory, ChemicalInfuserRecipeWrapper.class));
			addRecipes(registry, Recipe.CHEMICAL_INFUSER, chemicalInfuserCategory, ChemicalInfuserRecipe.class, ChemicalInfuserRecipeCategory.class, ChemicalInfuserRecipeWrapper.class);
			
			ChemicalOxidizerRecipeCategory chemicalOxidizerCategory = new ChemicalOxidizerRecipeCategory(registry.getJeiHelpers().getGuiHelper());
			registry.addRecipeCategories(chemicalOxidizerCategory);
			registry.addRecipeHandlers(new BaseRecipeHandler(chemicalOxidizerCategory, ChemicalOxidizerRecipeWrapper.class));
			addRecipes(registry, Recipe.CHEMICAL_OXIDIZER, chemicalOxidizerCategory, OxidationRecipe.class, ChemicalOxidizerRecipeCategory.class, ChemicalOxidizerRecipeWrapper.class);
			
			ChemicalWasherRecipeCategory chemicalWasherCategory = new ChemicalWasherRecipeCategory(registry.getJeiHelpers().getGuiHelper());
			registry.addRecipeCategories(chemicalWasherCategory);
			registry.addRecipeHandlers(new BaseRecipeHandler(chemicalWasherCategory, ChemicalWasherRecipeWrapper.class));
			addRecipes(registry, Recipe.CHEMICAL_WASHER, chemicalWasherCategory, WasherRecipe.class, ChemicalWasherRecipeCategory.class, ChemicalWasherRecipeWrapper.class);
			
			SolarNeutronRecipeCategory solarNeutronCategory = new SolarNeutronRecipeCategory(registry.getJeiHelpers().getGuiHelper());
			registry.addRecipeCategories(solarNeutronCategory);
			registry.addRecipeHandlers(new BaseRecipeHandler(solarNeutronCategory, SolarNeutronRecipeWrapper.class));
			addRecipes(registry, Recipe.SOLAR_NEUTRON_ACTIVATOR, solarNeutronCategory, SolarNeutronRecipe.class, SolarNeutronRecipeCategory.class, SolarNeutronRecipeWrapper.class);
			
			ElectrolyticSeparatorRecipeCategory electrolyticSeparatorCategory = new ElectrolyticSeparatorRecipeCategory(registry.getJeiHelpers().getGuiHelper());
			registry.addRecipeCategories(electrolyticSeparatorCategory);
			registry.addRecipeHandlers(new BaseRecipeHandler(electrolyticSeparatorCategory, ElectrolyticSeparatorRecipeWrapper.class));
			addRecipes(registry, Recipe.ELECTROLYTIC_SEPARATOR, electrolyticSeparatorCategory, SeparatorRecipe.class, ElectrolyticSeparatorRecipeCategory.class, ElectrolyticSeparatorRecipeWrapper.class);
			
			ThermalEvaporationRecipeCategory thermalEvaporationCategory = new ThermalEvaporationRecipeCategory(registry.getJeiHelpers().getGuiHelper());
			registry.addRecipeCategories(thermalEvaporationCategory);
			registry.addRecipeHandlers(new BaseRecipeHandler(thermalEvaporationCategory, ThermalEvaporationRecipeWrapper.class));
			addRecipes(registry, Recipe.THERMAL_EVAPORATION_PLANT, thermalEvaporationCategory, ThermalEvaporationRecipe.class, ThermalEvaporationRecipeCategory.class, ThermalEvaporationRecipeWrapper.class);
			
			PRCRecipeCategory prcCategory = new PRCRecipeCategory(registry.getJeiHelpers().getGuiHelper());
			registry.addRecipeCategories(prcCategory);
			registry.addRecipeHandlers(new BaseRecipeHandler(prcCategory, PRCRecipeWrapper.class));
			addRecipes(registry, Recipe.PRESSURIZED_REACTION_CHAMBER, prcCategory, PressurizedRecipe.class, PRCRecipeCategory.class, PRCRecipeWrapper.class);
		} catch(Exception e) {
			e.printStackTrace();
		}
		
		List<RotaryCondensentratorRecipeWrapper> condensentratorRecipes = new ArrayList<RotaryCondensentratorRecipeWrapper>();
		
		RotaryCondensentratorRecipeCategory rotaryCondensentratorCategory = new RotaryCondensentratorRecipeCategory(registry.getJeiHelpers().getGuiHelper());
		registry.addRecipeCategories(rotaryCondensentratorCategory);
		registry.addRecipeHandlers(new BaseRecipeHandler(rotaryCondensentratorCategory, RotaryCondensentratorRecipeWrapper.class));
		
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
	
	private void registerBasicMachine(IModRegistry registry, Recipe recipe, String unlocalized, ProgressBar bar, Class<? extends MachineRecipeWrapper> wrapper) throws Exception
	{
		MachineRecipeCategory category = new MachineRecipeCategory(registry.getJeiHelpers().getGuiHelper(), recipe.name().toLowerCase(), unlocalized, bar);
		
		registry.addRecipeCategories(category);
		registry.addRecipeHandlers(new BaseRecipeHandler(category, wrapper));
		
		addRecipes(registry, recipe, category, BasicMachineRecipe.class, MachineRecipeCategory.class, wrapper);
	}
	
	private void registerAdvancedMachine(IModRegistry registry, Recipe recipe, String unlocalized, ProgressBar bar, Class<? extends AdvancedMachineRecipeWrapper> wrapper) throws Exception
	{
		AdvancedMachineRecipeCategory category = new AdvancedMachineRecipeCategory(registry.getJeiHelpers().getGuiHelper(), recipe.name().toLowerCase(), unlocalized, bar);
		
		registry.addRecipeCategories(category);
		registry.addRecipeHandlers(new BaseRecipeHandler(category, wrapper));
		
		addRecipes(registry, recipe, category, AdvancedMachineRecipe.class, AdvancedMachineRecipeCategory.class, wrapper);
	}
	
	private void registerChanceMachine(IModRegistry registry, Recipe recipe, String unlocalized, ProgressBar bar, Class<? extends ChanceMachineRecipeWrapper> wrapper) throws Exception
	{
		ChanceMachineRecipeCategory category = new ChanceMachineRecipeCategory(registry.getJeiHelpers().getGuiHelper(), recipe.name().toLowerCase(), unlocalized, bar);
		
		registry.addRecipeCategories(category);
		registry.addRecipeHandlers(new BaseRecipeHandler(category, wrapper));
		
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
}
