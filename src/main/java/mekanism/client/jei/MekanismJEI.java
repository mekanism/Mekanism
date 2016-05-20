package mekanism.client.jei;

import java.util.ArrayList;
import java.util.List;

import mekanism.client.gui.GuiChemicalInjectionChamber;
import mekanism.client.gui.GuiCombiner;
import mekanism.client.gui.GuiCrusher;
import mekanism.client.gui.GuiEnrichmentChamber;
import mekanism.client.gui.GuiOsmiumCompressor;
import mekanism.client.gui.GuiPrecisionSawmill;
import mekanism.client.gui.GuiPurificationChamber;
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
import mekanism.common.MekanismBlocks;
import mekanism.common.MekanismItems;
import mekanism.common.recipe.RecipeHandler.Recipe;
import mekanism.common.recipe.machines.AdvancedMachineRecipe;
import mekanism.common.recipe.machines.BasicMachineRecipe;
import mekanism.common.recipe.machines.ChanceMachineRecipe;
import mekanism.common.util.ItemDataUtils;
import mezz.jei.api.IItemRegistry;
import mezz.jei.api.IJeiHelpers;
import mezz.jei.api.IJeiRuntime;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.IModRegistry;
import mezz.jei.api.IRecipeRegistry;
import mezz.jei.api.JEIPlugin;
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
	}
	
	private void registerBasicMachine(IModRegistry registry, Recipe recipe, String unlocalized, ProgressBar bar, Class<? extends MachineRecipeHandler> handler, Class<? extends MachineRecipeWrapper> wrapper) throws Exception
	{
		MachineRecipeCategory category = new MachineRecipeCategory(registry.getJeiHelpers().getGuiHelper(), recipe.name().toLowerCase(), unlocalized, bar);
		
		registry.addRecipeCategories(category);
		registry.addRecipeHandlers(handler.getConstructor(MachineRecipeCategory.class).newInstance(category));
		
		List<MachineRecipeWrapper> recipes = new ArrayList<MachineRecipeWrapper>();
		
		for(Object obj : recipe.get().values())
		{
			if(obj instanceof BasicMachineRecipe)
			{
				recipes.add(wrapper.getConstructor(BasicMachineRecipe.class, MachineRecipeCategory.class).newInstance((BasicMachineRecipe)obj, category));
			}
		}
		
		registry.addRecipes(recipes);
	}
	
	private void registerAdvancedMachine(IModRegistry registry, Recipe recipe, String unlocalized, ProgressBar bar, Class<? extends AdvancedMachineRecipeHandler> handler, Class<? extends AdvancedMachineRecipeWrapper> wrapper) throws Exception
	{
		AdvancedMachineRecipeCategory category = new AdvancedMachineRecipeCategory(registry.getJeiHelpers().getGuiHelper(), recipe.name().toLowerCase(), unlocalized, bar);
		
		registry.addRecipeCategories(category);
		registry.addRecipeHandlers(handler.getConstructor(AdvancedMachineRecipeCategory.class).newInstance(category));
		
		List<AdvancedMachineRecipeWrapper> recipes = new ArrayList<AdvancedMachineRecipeWrapper>();
		
		for(Object obj : recipe.get().values())
		{
			if(obj instanceof AdvancedMachineRecipe)
			{
				recipes.add(wrapper.getConstructor(AdvancedMachineRecipe.class, AdvancedMachineRecipeCategory.class).newInstance((AdvancedMachineRecipe)obj, category));
			}
		}
		
		registry.addRecipes(recipes);
	}
	
	private void registerChanceMachine(IModRegistry registry, Recipe recipe, String unlocalized, ProgressBar bar, Class<? extends ChanceMachineRecipeHandler> handler, Class<? extends ChanceMachineRecipeWrapper> wrapper) throws Exception
	{
		ChanceMachineRecipeCategory category = new ChanceMachineRecipeCategory(registry.getJeiHelpers().getGuiHelper(), recipe.name().toLowerCase(), unlocalized, bar);
		
		registry.addRecipeCategories(category);
		registry.addRecipeHandlers(handler.getConstructor(ChanceMachineRecipeCategory.class).newInstance(category));
		
		List<ChanceMachineRecipeWrapper> recipes = new ArrayList<ChanceMachineRecipeWrapper>();
		
		for(Object obj : recipe.get().values())
		{
			if(obj instanceof ChanceMachineRecipe)
			{
				recipes.add(wrapper.getConstructor(ChanceMachineRecipe.class, ChanceMachineRecipeCategory.class).newInstance((ChanceMachineRecipe)obj, category));
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
