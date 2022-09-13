package mekanism.common.integration;

import ic2.api.recipe.RecipeInputOreDict;
import ic2.api.recipe.Recipes;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import mekanism.api.MekanismConfig;
import mekanism.api.gas.GasRegistry;
import mekanism.api.gas.GasStack;
import mekanism.api.infuse.InfuseObject;
import mekanism.api.infuse.InfuseRegistry;
import mekanism.api.recipe.RecipeHelper;
import mekanism.api.util.StackUtils;
import mekanism.common.*;
import mekanism.common.block.BlockMachine;
import mekanism.common.recipe.RecipeHandler;
import mekanism.common.recipe.RecipeHandler.Recipe;
import mekanism.common.recipe.ShapedMekanismRecipe;
import mekanism.common.util.MekanismUtils;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.CraftingManager;
import net.minecraft.item.crafting.FurnaceRecipes;
import net.minecraftforge.oredict.OreDictionary;
import cpw.mods.fml.common.Optional.Method;

public final class OreDictManager
{
	private static final List<String> minorCompat = Arrays.asList("Nickel", "Aluminum");
	private static List<String> osmiumcompat = new ArrayList<>();

	
	public static void init() {
		if (MekanismConfig.general.OreDictOsmium) {
			osmiumcompat.add("Osmium");
		}
		if (MekanismConfig.general.OreDictPlatinum) {
			osmiumcompat.add("Platinum");
		}
		addLogRecipes();

		for (ItemStack ore : OreDictionary.getOres("plankWood")) {
			if (ore.getHasSubtypes()) {
				ItemStack wildStack = new ItemStack(ore.getItem(), 1, OreDictionary.WILDCARD_VALUE);

				if (!Recipe.PRECISION_SAWMILL.containsRecipe(wildStack)) {
					RecipeHandler.addPrecisionSawmillRecipe(wildStack, new ItemStack(Items.stick, 6), new ItemStack(MekanismItems.Sawdust), 0.25);
				}
			} else {
				RecipeHandler.addPrecisionSawmillRecipe(StackUtils.size(ore, 1), new ItemStack(Items.stick, 6), new ItemStack(MekanismItems.Sawdust), 0.25);
			}
		}
		for (ItemStack ore : OreDictionary.getOres("ingotSteel")) {
			RecipeHandler.addMetallurgicInfuserRecipe(InfuseRegistry.get("REDSTONE"), 10, StackUtils.size(ore, 1), new ItemStack(MekanismItems.EnrichedAlloy));
		}
		for (ItemStack ore : OreDictionary.getOres("oreNetherSteel")) {
			RecipeHandler.addEnrichmentChamberRecipe(StackUtils.size(ore, 1), new ItemStack(MekanismItems.OtherDust, 4, 1));
		}

		if (OreDictionary.getOres("itemRubber").size() > 0) {
			for (ItemStack ore : OreDictionary.getOres("woodRubber")) {
			RecipeHandler.addPrecisionSawmillRecipe(StackUtils.size(ore, 1), new ItemStack(Blocks.planks, 4), StackUtils.size(OreDictionary.getOres("itemRawRubber").get(0), 2), 1F);			}
		}

		for (ItemStack ore : OreDictionary.getOres("dustSulfur")) {
			RecipeHandler.addChemicalOxidizerRecipe(StackUtils.size(ore, 1), new GasStack(GasRegistry.getGas("sulfurDioxideGas"), 100));
		}

		for (ItemStack ore : OreDictionary.getOres("dustSalt")) {
			RecipeHandler.addChemicalOxidizerRecipe(StackUtils.size(ore, 1), new GasStack(GasRegistry.getGas("brine"), 15));
		}

		for (ItemStack ore : OreDictionary.getOres("dustRefinedObsidian")) {
			RecipeHandler.addOsmiumCompressorRecipe(StackUtils.size(ore, 1), new ItemStack(MekanismItems.Ingot, 1, 0));
			RecipeHandler.addCrusherRecipe(StackUtils.size(ore, 1), new ItemStack(MekanismItems.OtherDust, 1, 6));
			RecipeHandler.addEnrichmentChamberRecipe(StackUtils.size(ore, 1), new ItemStack(MekanismItems.CompressedObsidian));

			InfuseRegistry.registerInfuseObject(StackUtils.size(ore, 1), new InfuseObject(InfuseRegistry.get("OBSIDIAN"), 10));
		}

		for (Resource resource : Resource.values()) {
			for (ItemStack ore : OreDictionary.getOres("clump" + resource.getName())) {
				RecipeHandler.addCrusherRecipe(StackUtils.size(ore, 1), new ItemStack(MekanismItems.DirtyDust, 1, resource.ordinal()));
			}

			for (ItemStack ore : OreDictionary.getOres("shard" + resource.getName())) {
				RecipeHandler.addPurificationChamberRecipe(StackUtils.size(ore, 1), new ItemStack(MekanismItems.Clump, 1, resource.ordinal()));
			}

			for (ItemStack ore : OreDictionary.getOres("crystal" + resource.getName())) {
				RecipeHandler.addChemicalInjectionChamberRecipe(StackUtils.size(ore, 1), "hydrogenChloride", new ItemStack(MekanismItems.Shard, 1, resource.ordinal()));
			}

			for (ItemStack ore : OreDictionary.getOres("dustDirty" + resource.getName())) {
				RecipeHandler.addEnrichmentChamberRecipe(StackUtils.size(ore, 1), new ItemStack(MekanismItems.Dust, 1, resource.ordinal()));
			}

			for (ItemStack ore : OreDictionary.getOres("ore" + resource.getName())) {
				RecipeHandler.addEnrichmentChamberRecipe(StackUtils.size(ore, 1), new ItemStack(MekanismItems.Dust, 2, resource.ordinal()));
				RecipeHandler.addPurificationChamberRecipe(StackUtils.size(ore, 1), new ItemStack(MekanismItems.Clump, 3, resource.ordinal()));
				RecipeHandler.addChemicalInjectionChamberRecipe(StackUtils.size(ore, 1), "hydrogenChloride", new ItemStack(MekanismItems.Shard, 4, resource.ordinal()));
				RecipeHandler.addChemicalDissolutionChamberRecipe(StackUtils.size(ore, 1), new GasStack(GasRegistry.getGas(resource.getName().toLowerCase()), 1000));
			}

			for (ItemStack ore : OreDictionary.getOres("ingot" + resource.getName())) {
				RecipeHandler.addCrusherRecipe(StackUtils.size(ore, 1), new ItemStack(MekanismItems.Dust, 1, resource.ordinal()));
			}

			try {
				for (ItemStack ore : OreDictionary.getOres("dust" + resource.getName())) {
					RecipeHandler.addCombinerRecipe(StackUtils.size(ore, 8), StackUtils.size(OreDictionary.getOres("ore" + resource.getName()).get(0), 1));
				}
			} catch (Exception e) {
			}
		}
		if (MekanismConfig.general.OreDictOsmium || MekanismConfig.general.OreDictPlatinum) {
			for (String s : osmiumcompat) {
				CraftingManager.getInstance().getRecipeList().add(new ShapedMekanismRecipe(new ItemStack(MekanismBlocks.BasicBlock, 1, 0), new Object[]{
						"XXX", "XXX", "XXX", Character.valueOf('X'), "ingot" + s
				}));
				CraftingManager.getInstance().getRecipeList().add(new ShapedMekanismRecipe(new ItemStack(MekanismItems.SpeedUpgrade), new Object[]{
						" G ", "ADA", " G ", Character.valueOf('G'), "blockGlass", Character.valueOf('A'), MekanismItems.EnrichedAlloy, Character.valueOf('D'), "dust" + s
				}));
				BlockMachine.MachineType.METALLURGIC_INFUSER.addRecipe(new ShapedMekanismRecipe(new ItemStack(MekanismBlocks.MachineBlock, 1, 8), new Object[]{
						"IFI", "ROR", "IFI", Character.valueOf('I'), "ingotIron", Character.valueOf('F'), Blocks.furnace, Character.valueOf('R'), "dustRedstone", Character.valueOf('O'), "ingot" + s
				}));
				BlockMachine.MachineType.PURIFICATION_CHAMBER.addRecipe(new ShapedMekanismRecipe(new ItemStack(MekanismBlocks.MachineBlock, 1, 9), new Object[]{
						"ECE", "ORO", "ECE", Character.valueOf('C'), MekanismUtils.getControlCircuit(Tier.BaseTier.ADVANCED), Character.valueOf('E'), "alloyAdvanced", Character.valueOf('O'), "ingot" + s, Character.valueOf('R'), new ItemStack(MekanismBlocks.MachineBlock, 1, 0)
				}));
				CraftingManager.getInstance().getRecipeList().add(new ShapedMekanismRecipe(new ItemStack(MekanismBlocks.BasicBlock, 1, 8), new Object[]{
						"SGS", "GPG", "SGS", Character.valueOf('S'), "ingotSteel", Character.valueOf('P'), "ingot" + s, Character.valueOf('G'), "blockGlass"
				}));
				BlockMachine.MachineType.ELECTRIC_PUMP.addRecipe(new ShapedMekanismRecipe(new ItemStack(MekanismBlocks.MachineBlock, 1, 12), new Object[]{
						" B ", "ECE", "OOO", Character.valueOf('B'), Items.bucket, Character.valueOf('E'), MekanismItems.EnrichedAlloy, Character.valueOf('C'), new ItemStack(MekanismBlocks.BasicBlock, 1, 8), Character.valueOf('O'), "ingot" + s
				}));
				CraftingManager.getInstance().getRecipeList().add(new ShapedMekanismRecipe(new ItemStack(MekanismItems.WalkieTalkie), new Object[]{
						"  O", "SCS", " S ", Character.valueOf('O'), "ingot" + s, Character.valueOf('S'), "ingotSteel", Character.valueOf('C'), MekanismUtils.getControlCircuit(Tier.BaseTier.BASIC)
				}));
				CraftingManager.getInstance().getRecipeList().add(new ShapedMekanismRecipe(new ItemStack(MekanismItems.ElectrolyticCore), new Object[]{
						"EPE", "IEG", "EPE", Character.valueOf('E'), MekanismItems.EnrichedAlloy, Character.valueOf('P'), "dust" + s, Character.valueOf('I'), "dustIron", Character.valueOf('G'), "dustGold"
				}));
				CraftingManager.getInstance().getRecipeList().add(new ShapedMekanismRecipe(new ItemStack(Blocks.rail, 24), new Object[]{
						"O O", "OSO", "O O", Character.valueOf('O'), "ingot" + s, Character.valueOf('S'), "stickWood"
				}));
				CraftingManager.getInstance().getRecipeList().add(new ShapedMekanismRecipe(MekanismItems.GaugeDropper.getEmptyItem(), new Object[]{
						" O ", "G G", "GGG", Character.valueOf('O'), "ingot" + s, Character.valueOf('G'), "paneGlass"
				}));
				CraftingManager.getInstance().getRecipeList().add(new ShapedMekanismRecipe(new ItemStack(MekanismItems.TierInstaller, 1, 1), new Object[]{
						"ECE", "oWo", "ECE", Character.valueOf('E'), "alloyAdvanced", Character.valueOf('C'), MekanismUtils.getControlCircuit(Tier.BaseTier.ADVANCED), Character.valueOf('o'), "ingot" + s, Character.valueOf('W'), "plankWood"
				}));
				CraftingManager.getInstance().getRecipeList().add(new ShapedMekanismRecipe(MekanismUtils.getEnergyCube(Tier.EnergyCubeTier.ADVANCED), new Object[]{
						"ETE", "oBo", "ETE", Character.valueOf('E'), "alloyAdvanced", Character.valueOf('o'), "ingot" + s, Character.valueOf('T'), MekanismItems.EnergyTablet.getUnchargedItem(), Character.valueOf('B'), MekanismUtils.getEnergyCube(Tier.EnergyCubeTier.BASIC)
				}));
				//Gas Tank Recipes
				CraftingManager.getInstance().getRecipeList().add(new ShapedMekanismRecipe(MekanismUtils.getEmptyGasTank(Tier.GasTankTier.BASIC), new Object[]{
						"APA", "P P", "APA", Character.valueOf('P'), "ingot" + s, Character.valueOf('A'), "alloyBasic"
				}));
				CraftingManager.getInstance().getRecipeList().add(new ShapedMekanismRecipe(MekanismUtils.getEmptyGasTank(Tier.GasTankTier.ADVANCED), new Object[]{
						"APA", "PTP", "APA", Character.valueOf('P'), "ingot" + s, Character.valueOf('A'), "alloyAdvanced", Character.valueOf('T'), MekanismUtils.getEmptyGasTank(Tier.GasTankTier.BASIC)
				}));
				CraftingManager.getInstance().getRecipeList().add(new ShapedMekanismRecipe(MekanismUtils.getEmptyGasTank(Tier.GasTankTier.ELITE), new Object[]{
						"APA", "PTP", "APA", Character.valueOf('P'), "ingot" + s, Character.valueOf('A'), "alloyElite", Character.valueOf('T'), MekanismUtils.getEmptyGasTank(Tier.GasTankTier.ADVANCED)
				}));
				CraftingManager.getInstance().getRecipeList().add(new ShapedMekanismRecipe(MekanismUtils.getEmptyGasTank(Tier.GasTankTier.ULTIMATE), new Object[]{
						"APA", "PTP", "APA", Character.valueOf('P'), "ingot" + s, Character.valueOf('A'), "alloyUltimate", Character.valueOf('T'), MekanismUtils.getEmptyGasTank(Tier.GasTankTier.ELITE)
				}));
				for (ItemStack ore : OreDictionary.getOres("ingot" + s)) {
					RecipeHandler.addMetallurgicInfuserRecipe(InfuseRegistry.get("REDSTONE"), 10, StackUtils.size(ore, 1), new ItemStack(MekanismItems.ControlCircuit, 1, 0));
				}
			}
		}

		for (String s : minorCompat) {
			for (ItemStack ore : OreDictionary.getOres("ore" + s)) {
				try {
					RecipeHandler.addEnrichmentChamberRecipe(StackUtils.size(ore, 1), StackUtils.size(OreDictionary.getOres("dust" + s).get(0), 2));
				} catch (Exception e) {
				}
			}

			for (ItemStack ore : OreDictionary.getOres("ingot" + s)) {
				try {
					RecipeHandler.addCrusherRecipe(StackUtils.size(ore, 1), StackUtils.size(OreDictionary.getOres("dust" + s).get(0), 1));
				} catch (Exception e) {
				}
			}

			for (ItemStack ore : OreDictionary.getOres("dust" + s)) {
				try {
					RecipeHandler.addCombinerRecipe(StackUtils.size(ore, 8), StackUtils.size(OreDictionary.getOres("ore" + s).get(0), 1));
				} catch (Exception e) {
				}
			}
		}

		for (ItemStack ore : OreDictionary.getOres("oreYellorite")) {
			try {
				RecipeHandler.addEnrichmentChamberRecipe(StackUtils.size(ore, 1), StackUtils.size(OreDictionary.getOres("dustYellorium").get(0), 2));
			} catch (Exception e) {
			}
		}

		for (ItemStack ore : OreDictionary.getOres("oreCertusQuartz")) {
			try {
				RecipeHandler.addEnrichmentChamberRecipe(StackUtils.size(ore, 1), StackUtils.size(OreDictionary.getOres("dustCertusQuartz").get(0), 4));
			} catch (Exception e) {
			}
		}

		for (ItemStack ore : OreDictionary.getOres("crystalCertusQuartz")) {
			try {
				RecipeHandler.addCrusherRecipe(StackUtils.size(ore, 1), StackUtils.size(OreDictionary.getOres("dustCertusQuartz").get(0), 1));
			} catch (Exception e) {
			}
		}

		for (ItemStack ore : OreDictionary.getOres("dustCertusQuartz")) {
			try {
				RecipeHandler.addEnrichmentChamberRecipe(StackUtils.size(ore, 1), StackUtils.size(OreDictionary.getOres("crystalCertusQuartz").get(0), 1));
			} catch (Exception e) {
			}
		}

		for (ItemStack ore : OreDictionary.getOres("gemQuartz")) {
			try {
				RecipeHandler.addCrusherRecipe(StackUtils.size(ore, 1), StackUtils.size(OreDictionary.getOres("dustNetherQuartz").get(0), 1));
			} catch (Exception e) {
			}
		}

		for (ItemStack ore : OreDictionary.getOres("dustNetherQuartz")) {
			try {
				RecipeHandler.addEnrichmentChamberRecipe(StackUtils.size(ore, 1), StackUtils.size(OreDictionary.getOres("gemQuartz").get(0), 1));
			} catch (Exception e) {
			}
		}

		for (ItemStack ore : OreDictionary.getOres("oreQuartz")) {
			RecipeHandler.addEnrichmentChamberRecipe(StackUtils.size(ore, 1), new ItemStack(Items.quartz, 6));
		}

		for (ItemStack ore : OreDictionary.getOres("crystalFluix")) {
			try {
				RecipeHandler.addCrusherRecipe(StackUtils.size(ore, 1), StackUtils.size(OreDictionary.getOres("dustFluix").get(0), 1));
			} catch (Exception e) {
			}
		}

		for (ItemStack ore : OreDictionary.getOres("dustFluix")) {
			try {
				RecipeHandler.addEnrichmentChamberRecipe(StackUtils.size(ore, 1), StackUtils.size(OreDictionary.getOres("crystalFluix").get(0), 1));
			} catch (Exception e) {
			}
		}

		for (ItemStack ore : OreDictionary.getOres("ingotCopper")) {
			RecipeHandler.addMetallurgicInfuserRecipe(InfuseRegistry.get("TIN"), 10, StackUtils.size(ore, 1), new ItemStack(MekanismItems.Ingot, 1, 2));
		}

		for (ItemStack ore : OreDictionary.getOres("ingotRefinedObsidian")) {
			RecipeHandler.addCrusherRecipe(StackUtils.size(ore, 1), new ItemStack(MekanismItems.OtherDust, 1, 6));
		}
		for(ItemStack ore : OreDictionary.getOres("ingotRedstone"))
		{
			RecipeHandler.addCrusherRecipe(StackUtils.size(ore, 1), new ItemStack(Items.redstone));
		}
		
		for(ItemStack ore : OreDictionary.getOres("ingotRefinedGlowstone"))
		{
			RecipeHandler.addCrusherRecipe(StackUtils.size(ore, 1), new ItemStack(Items.glowstone_dust));
		}
		
		try {
			RecipeHandler.addCrusherRecipe(new ItemStack(MekanismItems.Ingot, 1, 2), StackUtils.size(OreDictionary.getOres("dustBronze").get(0), 1));
			
			if(Mekanism.hooks.IC2Loaded)
			{
				addIC2BronzeRecipe();
			}
		} catch(Exception e) {}
		
		try {
			FurnaceRecipes.smelting().func_151394_a(new ItemStack(MekanismItems.Dust, 1, Resource.SILVER.ordinal()), StackUtils.size(OreDictionary.getOres("ingotSilver").get(0), 1), 0.0F);
		} catch(Exception e) {}
		
		try {
			FurnaceRecipes.smelting().func_151394_a(new ItemStack(MekanismItems.Dust, 1, Resource.LEAD.ordinal()), StackUtils.size(OreDictionary.getOres("ingotLead").get(0), 1), 0.0F);
		} catch(Exception e) {}
		
		try {
			RecipeHandler.addCrusherRecipe(new ItemStack(Items.coal), StackUtils.size(OreDictionary.getOres("dustCoal").get(0), 1));
		} catch(Exception e) {}
		
		try {
			RecipeHandler.addCrusherRecipe(new ItemStack(Items.coal, 1, 1), StackUtils.size(OreDictionary.getOres("dustCharcoal").get(0), 1));
		} catch(Exception e) {}
		
		try {
			RecipeHandler.addCrusherRecipe(new ItemStack(Items.gunpowder), StackUtils.size(OreDictionary.getOres("dustSaltpeter").get(0), 1));
		} catch(Exception e) {}
		
		for(ItemStack ore : OreDictionary.getOres("sand"))
		{
			try {
				RecipeHandler.addCrusherRecipe(StackUtils.size(ore, 1), StackUtils.size(OreDictionary.getOres("itemSilicon").get(0), 1));
			} catch(Exception e) {}
		}
		
		for(ItemStack ore : OreDictionary.getOres("dustSaltpeter"))
		{
			RecipeHandler.addEnrichmentChamberRecipe(StackUtils.size(ore, 1), new ItemStack(Items.gunpowder));
		}
		
		for(ItemStack ore : OreDictionary.getOres("ingotSteel"))
		{
			RecipeHandler.addCrusherRecipe(StackUtils.size(ore, 1), new ItemStack(MekanismItems.OtherDust, 1, 1));
		}
		
		for(ItemStack ore : OreDictionary.getOres("dustLapis"))
		{
			RecipeHandler.addCrusherRecipe(new ItemStack(Items.dye, 1, 4), StackUtils.size(ore, 1));
		}
		
		for(ItemStack ore : OreDictionary.getOres("dustLithium"))
		{
			RecipeHandler.addChemicalOxidizerRecipe(StackUtils.size(ore, 1), new GasStack(GasRegistry.getGas("lithium"), 100));
		}
		
		for(ItemStack ore : OreDictionary.getOres("dustObsidian"))
		{
			RecipeHandler.addCombinerRecipe(StackUtils.size(ore, 4), new ItemStack(Blocks.obsidian));
			RecipeHandler.addMetallurgicInfuserRecipe(InfuseRegistry.get("DIAMOND"), 10, StackUtils.size(ore, 1), new ItemStack(MekanismItems.OtherDust, 1, 5));
		}
		
		for(ItemStack ore : OreDictionary.getOres("dustDiamond"))
		{
			InfuseRegistry.registerInfuseObject(ore, new InfuseObject(InfuseRegistry.get("DIAMOND"), 10));
			RecipeHandler.addEnrichmentChamberRecipe(StackUtils.size(ore, 1), new ItemStack(Items.diamond));
		}
			
		for(ItemStack ore : OreDictionary.getOres("dustTin"))
		{
			InfuseRegistry.registerInfuseObject(ore, new InfuseObject(InfuseRegistry.get("TIN"), 50));
		}

		try {
			for(ItemStack ore : OreDictionary.getOres("treeSapling"))
			{
				if(ore.getItemDamage() == 0 || ore.getItemDamage() == OreDictionary.WILDCARD_VALUE)
				{
					RecipeHandler.addCrusherRecipe(new ItemStack(ore.getItem(), 1, OreDictionary.WILDCARD_VALUE), new ItemStack(MekanismItems.BioFuel, 2));
				}
			}
		} catch(Exception e) {}
	}
	
	@Method(modid = "IC2")
	public static void addIC2BronzeRecipe()
	{
		try {
			Recipes.macerator.addRecipe(new RecipeInputOreDict("ingotBronze"), null, StackUtils.size(OreDictionary.getOres("dustBronze").get(0), 1));
		} catch(Exception e) {}
	}


	/**
	 * Handy method for retrieving all log items, finding their corresponding planks, and making recipes with them. Credit to CofhCore.
	 */
	public static void addLogRecipes()
	{
		Container tempContainer = new Container() {
			@Override
			public boolean canInteractWith(EntityPlayer player)
			{
				return false;
			}
		};
		
		InventoryCrafting tempCrafting = new InventoryCrafting(tempContainer, 3, 3);
		ArrayList recipeList = (ArrayList)CraftingManager.getInstance().getRecipeList();

		for(int i = 1; i < 9; i++)
		{
			tempCrafting.setInventorySlotContents(i, null);
		}

		ArrayList registeredOres = OreDictionary.getOres("logWood");
		
		for(int i = 0; i < registeredOres.size(); i++)
		{
			ItemStack logEntry = (ItemStack)registeredOres.get(i);

			if(logEntry.getItemDamage() == OreDictionary.WILDCARD_VALUE)
			{
				for(int j = 0; j < 16; j++)
				{
					ItemStack log = new ItemStack(logEntry.getItem(), 1, j);
					tempCrafting.setInventorySlotContents(0, log);
					ItemStack resultEntry = MekanismUtils.findMatchingRecipe(tempCrafting, null);

					if(resultEntry != null)
					{
						RecipeHandler.addPrecisionSawmillRecipe(log, StackUtils.size(resultEntry, 6), new ItemStack(MekanismItems.Sawdust), 1);
					}
				}
			}
			else {
				ItemStack log = StackUtils.size(logEntry, 1);
				tempCrafting.setInventorySlotContents(0, log);
				ItemStack resultEntry = MekanismUtils.findMatchingRecipe(tempCrafting, null);

				if(resultEntry != null)
				{
					RecipeHandler.addPrecisionSawmillRecipe(log, StackUtils.size(resultEntry, 6), new ItemStack(MekanismItems.Sawdust), 1);
				}
			}
		}
	}
	public static void terralizationcompat() {
		if (MekanismConfig.general.EnableQuartzCompat) {
			// Enrich quartz dust into quartz
			for (ItemStack ore : OreDictionary.getOres("dustQuartz")) {
				RecipeHelper.addEnrichmentChamberRecipe(StackUtils.size(ore, 1), new ItemStack(Items.quartz));
			}
			for (ItemStack ore : OreDictionary.getOres("dustNetherQuartz")) {
				RecipeHelper.addEnrichmentChamberRecipe(StackUtils.size(ore, 1), new ItemStack(Items.quartz));
			}
			// Enrich quartz ore into 2 quartz dust
			for (ItemStack ore : OreDictionary.getOres("dustQuartz")) {
				RecipeHelper.addEnrichmentChamberRecipe(new ItemStack(Blocks.quartz_ore), StackUtils.size(ore, 2));
			}
			for (ItemStack ore : OreDictionary.getOres("dustNetherQuartz")) {
				RecipeHelper.addEnrichmentChamberRecipe(new ItemStack(Blocks.quartz_ore), StackUtils.size(ore, 2));
			}
		}
		// Add gemdiamond oredict for compressed diamond
		if (MekanismConfig.general.EnableDiamondCompat) {
			for (ItemStack ore : OreDictionary.getOres("gemDiamond")) {
				InfuseRegistry.registerInfuseObject(ore, new InfuseObject(InfuseRegistry.get("DIAMOND"), 10));
				RecipeHelper.addEnrichmentChamberRecipe(StackUtils.size(ore, 1), new ItemStack(MekanismItems.CompressedDiamond));
			}
		}
		if (MekanismConfig.general.EnablePoorOresCompat) {
			for (ItemStack ore : OreDictionary.getOres("orePoorIron")) {
				for (ItemStack ore2 : OreDictionary.getOres("clumpIron")) {
					RecipeHelper.addPurificationChamberRecipe(ore, ore2);
				}
			}
			for (ItemStack ore : OreDictionary.getOres("orePoorGold")) {
				for (ItemStack ore2 : OreDictionary.getOres("clumpGold")) {
					RecipeHelper.addPurificationChamberRecipe(ore, ore2);
				}
			}
			for (ItemStack ore : OreDictionary.getOres("orePoorCopper")) {
				for (ItemStack ore2 : OreDictionary.getOres("clumpCopper")) {
					RecipeHelper.addPurificationChamberRecipe(ore, ore2);
				}
			}
			for (ItemStack ore : OreDictionary.getOres("orePoorTin")) {
				for (ItemStack ore2 : OreDictionary.getOres("clumpTin")) {
					RecipeHelper.addPurificationChamberRecipe(ore, ore2);
				}
			}
			for (ItemStack ore : OreDictionary.getOres("orePoorLead")) {
				for (ItemStack ore2 : OreDictionary.getOres("clumpLead")) {
					RecipeHelper.addPurificationChamberRecipe(ore, ore2);
				}
			}
		}
	}
}
