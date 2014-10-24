package mekanism.common.integration;

import ic2.api.recipe.RecipeInputOreDict;
import ic2.api.recipe.Recipes;

import java.util.ArrayList;

import mekanism.api.AdvancedInput;
import mekanism.api.ChanceOutput;
import mekanism.api.StackUtils;
import mekanism.api.gas.GasRegistry;
import mekanism.api.gas.GasStack;
import mekanism.api.infuse.InfuseObject;
import mekanism.api.infuse.InfuseRegistry;
import mekanism.api.infuse.InfusionInput;
import mekanism.common.Mekanism;
import mekanism.common.item.ItemDust;
import mekanism.common.recipe.RecipeHandler;
import mekanism.common.recipe.RecipeHandler.Recipe;
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
import ca.bradj.orecore.item.OreCoreItems;
import ca.bradj.orecoreext.item.OreCoreExtendedItems;
import cpw.mods.fml.common.Optional.Method;

public final class OreDictManager
{
	public static void init()
	{
		addLogRecipes();
		
		for(ItemStack ore : OreDictionary.getOres("plankWood"))
		{
			if(ore.getHasSubtypes())
			{
				ItemStack wildStack = new ItemStack(ore.getItem(), 1, OreDictionary.WILDCARD_VALUE);
				
				if(!Recipe.PRECISION_SAWMILL.containsRecipe(wildStack))
				{
					RecipeHandler.addPrecisionSawmillRecipe(wildStack, new ChanceOutput(new ItemStack(Items.stick, 6), new ItemStack(Mekanism.Sawdust), 0.25));
				}
			}
			else {
				RecipeHandler.addPrecisionSawmillRecipe(StackUtils.size(ore, 1), new ChanceOutput(new ItemStack(Items.stick, 6), new ItemStack(Mekanism.Sawdust), 0.25));
			}
		}
		
		for(ItemStack ore : OreDictionary.getOres("oreNetherSteel"))
		{
			RecipeHandler.addEnrichmentChamberRecipe(StackUtils.size(ore, 1), new ItemStack(OreCoreItems.steelDust, 4));
		}
		
		if(OreDictionary.getOres("itemRubber").size() > 0)
		{
			for(ItemStack ore : OreDictionary.getOres("woodRubber"))
			{
				RecipeHandler.addPrecisionSawmillRecipe(MekanismUtils.size(ore, 1), new ChanceOutput(new ItemStack(Blocks.planks, 4), MekanismUtils.size(OreDictionary.getOres("itemRubber").get(0), 1), 1F));
			}
		}
		
		for(ItemStack ore : OreDictionary.getOres("dustSulfur"))
		{
			RecipeHandler.addChemicalOxidizerRecipe(MekanismUtils.size(ore, 1), new GasStack(GasRegistry.getGas("sulfurDioxideGas"), 100));
		}
		
		for(ItemStack ore : OreDictionary.getOres("dustSalt"))
		{
			RecipeHandler.addChemicalOxidizerRecipe(MekanismUtils.size(ore, 1), new GasStack(GasRegistry.getGas("brine"), 15));
		}
		
		for(ItemStack ore : OreDictionary.getOres("dustRefinedObsidian"))
		{
			RecipeHandler.addOsmiumCompressorRecipe(MekanismUtils.size(ore, 1), new ItemStack(OreCoreExtendedItems.obsidianIngot, 1));
			RecipeHandler.addCrusherRecipe(MekanismUtils.size(ore, 1), new ItemStack(OreCoreExtendedItems.obsidianDirtyDust, 1));
			RecipeHandler.addEnrichmentChamberRecipe(MekanismUtils.size(ore, 1), new ItemStack(Mekanism.CompressedObsidian));
			
			InfuseRegistry.registerInfuseObject(MekanismUtils.size(ore, 1), new InfuseObject(InfuseRegistry.get("OBSIDIAN"), 10));
		}
		
        for(ItemStack ore : OreDictionary.getOres("clumpIron"))
        {
            RecipeHandler.addCrusherRecipe(MekanismUtils.size(ore, 1), new ItemStack(OreCoreExtendedItems.ironDirtyDust, 1));
        }
        
        for(ItemStack ore : OreDictionary.getOres("clumpGold"))
        {
            RecipeHandler.addCrusherRecipe(MekanismUtils.size(ore, 1), new ItemStack(OreCoreExtendedItems.goldDirtyDust, 1));
        }
        
        for(ItemStack ore : OreDictionary.getOres("clumpOsmium"))
        {
            RecipeHandler.addCrusherRecipe(MekanismUtils.size(ore, 1), new ItemStack(OreCoreExtendedItems.osmiumDirtyDust, 1));
        }
        
        for(ItemStack ore : OreDictionary.getOres("clumpCopper"))
        {
            RecipeHandler.addCrusherRecipe(MekanismUtils.size(ore, 1), new ItemStack(OreCoreExtendedItems.copperDirtyDust, 1));
        }
        
        for(ItemStack ore : OreDictionary.getOres("clumpTin"))
        {
            RecipeHandler.addCrusherRecipe(MekanismUtils.size(ore, 1), new ItemStack(OreCoreExtendedItems.tinDirtyDust, 1));
        }
        
        for(ItemStack ore : OreDictionary.getOres("clumpSilver"))
        {
            RecipeHandler.addCrusherRecipe(MekanismUtils.size(ore, 1), new ItemStack(OreCoreExtendedItems.silverDirtyDust, 1));
        }
        
        for(ItemStack ore : OreDictionary.getOres("clumpObsidian"))
        {
            RecipeHandler.addCrusherRecipe(MekanismUtils.size(ore, 1), new ItemStack(OreCoreExtendedItems.obsidianDirtyDust, 1));
        }
        
        for(ItemStack ore : OreDictionary.getOres("clumpLead"))
        {
            RecipeHandler.addCrusherRecipe(MekanismUtils.size(ore, 1), new ItemStack(OreCoreExtendedItems.leadDirtyDust, 1));
        }
		
		for(ItemStack ore : OreDictionary.getOres("shardIron"))
		{
			RecipeHandler.addPurificationChamberRecipe(MekanismUtils.size(ore, 1), new ItemStack(OreCoreExtendedItems.ironClump, 1));
		}
		
		for(ItemStack ore : OreDictionary.getOres("shardGold"))
		{
			RecipeHandler.addPurificationChamberRecipe(MekanismUtils.size(ore, 1), new ItemStack(OreCoreExtendedItems.goldClump, 1));
		}
		
		for(ItemStack ore : OreDictionary.getOres("shardOsmium"))
		{
			RecipeHandler.addPurificationChamberRecipe(MekanismUtils.size(ore, 1), new ItemStack(OreCoreExtendedItems.osmiumClump, 1));
		}
		
		for(ItemStack ore : OreDictionary.getOres("shardCopper"))
		{
			RecipeHandler.addPurificationChamberRecipe(MekanismUtils.size(ore, 1), new ItemStack(OreCoreExtendedItems.copperClump, 1));
		}
		
		for(ItemStack ore : OreDictionary.getOres("shardTin"))
		{
			RecipeHandler.addPurificationChamberRecipe(MekanismUtils.size(ore, 1), new ItemStack(OreCoreExtendedItems.tinClump, 1));
		}
		
		for(ItemStack ore : OreDictionary.getOres("shardSilver"))
		{
			RecipeHandler.addPurificationChamberRecipe(MekanismUtils.size(ore, 1), new ItemStack(OreCoreExtendedItems.silverClump, 1));
		}
		
		for(ItemStack ore : OreDictionary.getOres("shardObsidian"))
		{
			RecipeHandler.addPurificationChamberRecipe(MekanismUtils.size(ore, 1), new ItemStack(OreCoreExtendedItems.obsidianClump, 1)); 
		}
		
		for(ItemStack ore : OreDictionary.getOres("shardLead"))
		{
			RecipeHandler.addPurificationChamberRecipe(MekanismUtils.size(ore, 1), new ItemStack(OreCoreExtendedItems.leadClump, 1)); 
		}
		
		for(ItemStack ore : OreDictionary.getOres("shardSteel"))
		{
			RecipeHandler.addPurificationChamberRecipe(MekanismUtils.size(ore, 1), new ItemStack(OreCoreExtendedItems.steelClump, 1)); 
		}
		
		for(ItemStack ore : OreDictionary.getOres("crystalIron"))
		{
			RecipeHandler.addChemicalInjectionChamberRecipe(new AdvancedInput(MekanismUtils.size(ore, 1), GasRegistry.getGas("hydrogenChloride")), new ItemStack(OreCoreExtendedItems.ironShard, 1));
		}
		
		for(ItemStack ore : OreDictionary.getOres("crystalGold"))
		{
			RecipeHandler.addChemicalInjectionChamberRecipe(new AdvancedInput(MekanismUtils.size(ore, 1), GasRegistry.getGas("hydrogenChloride")), new ItemStack(OreCoreExtendedItems.goldShard, 1));
		}
		
		for(ItemStack ore : OreDictionary.getOres("crystalOsmium"))
		{
			RecipeHandler.addChemicalInjectionChamberRecipe(new AdvancedInput(MekanismUtils.size(ore, 1), GasRegistry.getGas("hydrogenChloride")), new ItemStack(OreCoreExtendedItems.osmiumShard, 1));
		}
		
		for(ItemStack ore : OreDictionary.getOres("crystalCopper"))
		{
			RecipeHandler.addChemicalInjectionChamberRecipe(new AdvancedInput(MekanismUtils.size(ore, 1), GasRegistry.getGas("hydrogenChloride")), new ItemStack(OreCoreExtendedItems.copperShard, 1));
		}
		
		for(ItemStack ore : OreDictionary.getOres("crystalTin"))
		{
			RecipeHandler.addChemicalInjectionChamberRecipe(new AdvancedInput(MekanismUtils.size(ore, 1), GasRegistry.getGas("hydrogenChloride")), new ItemStack(OreCoreExtendedItems.tinShard, 1));
		}
		
		for(ItemStack ore : OreDictionary.getOres("crystalSilver"))
		{
			RecipeHandler.addChemicalInjectionChamberRecipe(new AdvancedInput(MekanismUtils.size(ore, 1), GasRegistry.getGas("hydrogenChloride")), new ItemStack(OreCoreExtendedItems.silverShard, 1));
		}
		
		for(ItemStack ore : OreDictionary.getOres("crystalObsidian"))
		{
			RecipeHandler.addChemicalInjectionChamberRecipe(new AdvancedInput(MekanismUtils.size(ore, 1), GasRegistry.getGas("hydrogenChloride")), new ItemStack(OreCoreExtendedItems.obsidianShard, 1));
		}
		
		for(ItemStack ore : OreDictionary.getOres("crystalLead"))
		{
			RecipeHandler.addChemicalInjectionChamberRecipe(new AdvancedInput(MekanismUtils.size(ore, 1), GasRegistry.getGas("hydrogenChloride")), new ItemStack(OreCoreExtendedItems.leadShard, 1));
		}
		
		for(ItemStack ore : OreDictionary.getOres("dustDirtyIron"))
		{
			RecipeHandler.addEnrichmentChamberRecipe(MekanismUtils.size(ore, 1), new ItemStack(OreCoreItems.ironDust, 1));
		}
		
		for(ItemStack ore : OreDictionary.getOres("dustDirtyGold"))
		{
			RecipeHandler.addEnrichmentChamberRecipe(MekanismUtils.size(ore, 1), new ItemStack(OreCoreItems.goldDust, 1));
		}
		
		for(ItemStack ore : OreDictionary.getOres("dustDirtyOsmium"))
		{
			RecipeHandler.addEnrichmentChamberRecipe(MekanismUtils.size(ore, 1), new ItemStack(OreCoreItems.osmiumDust, 1));
		}
		
		for(ItemStack ore : OreDictionary.getOres("dustDirtyCopper"))
		{
			RecipeHandler.addEnrichmentChamberRecipe(MekanismUtils.size(ore, 1), new ItemStack(OreCoreItems.copperDust, 1));
		}
		
		for(ItemStack ore : OreDictionary.getOres("dustDirtyTin"))
		{
			RecipeHandler.addEnrichmentChamberRecipe(MekanismUtils.size(ore, 1), new ItemStack(OreCoreItems.tinDust, 1));
		}
		
		for(ItemStack ore : OreDictionary.getOres("dustDirtySilver"))
		{
			RecipeHandler.addEnrichmentChamberRecipe(MekanismUtils.size(ore, 1), new ItemStack(OreCoreItems.silverDust, 1));
		}
		
		for(ItemStack ore : OreDictionary.getOres("dustDirtyLead"))
		{
			RecipeHandler.addEnrichmentChamberRecipe(MekanismUtils.size(ore, 1), new ItemStack(OreCoreItems.leadDust, 1));
		}
		
		for(ItemStack ore : OreDictionary.getOres("dustDirtySteel"))
		{
			RecipeHandler.addEnrichmentChamberRecipe(MekanismUtils.size(ore, 1), new ItemStack(OreCoreItems.steelDust, 1));
		}
		
		for(ItemStack ore : OreDictionary.getOres("oreCopper"))
		{
			RecipeHandler.addEnrichmentChamberRecipe(MekanismUtils.size(ore, 1), new ItemStack(OreCoreItems.copperDust, 2));
			RecipeHandler.addPurificationChamberRecipe(MekanismUtils.size(ore, 1), new ItemStack(OreCoreExtendedItems.copperClump, 3));
			RecipeHandler.addChemicalInjectionChamberRecipe(new AdvancedInput(MekanismUtils.size(ore, 1), GasRegistry.getGas("hydrogenChloride")), new ItemStack(OreCoreExtendedItems.copperShard, 4));
			RecipeHandler.addChemicalDissolutionChamberRecipe(MekanismUtils.size(ore, 1), new GasStack(GasRegistry.getGas("copper"), 1000));
		}
		
		for(ItemStack ore : OreDictionary.getOres("oreTin"))
		{
			RecipeHandler.addEnrichmentChamberRecipe(MekanismUtils.size(ore, 1), new ItemStack(OreCoreItems.tinDust, 2));
			RecipeHandler.addPurificationChamberRecipe(MekanismUtils.size(ore, 1), new ItemStack(OreCoreExtendedItems.tinClump, 3));
			RecipeHandler.addChemicalInjectionChamberRecipe(new AdvancedInput(MekanismUtils.size(ore, 1), GasRegistry.getGas("hydrogenChloride")), new ItemStack(OreCoreExtendedItems.tinShard, 4));
			RecipeHandler.addChemicalDissolutionChamberRecipe(MekanismUtils.size(ore, 1), new GasStack(GasRegistry.getGas("tin"), 1000));
		}
		
		for(ItemStack ore : OreDictionary.getOres("oreOsmium"))
		{
			RecipeHandler.addEnrichmentChamberRecipe(MekanismUtils.size(ore, 1), new ItemStack(OreCoreItems.osmiumDust, 2));
			RecipeHandler.addPurificationChamberRecipe(MekanismUtils.size(ore, 1), new ItemStack(OreCoreExtendedItems.osmiumClump, 3));
			RecipeHandler.addChemicalInjectionChamberRecipe(new AdvancedInput(MekanismUtils.size(ore, 1), GasRegistry.getGas("hydrogenChloride")), new ItemStack(OreCoreExtendedItems.osmiumShard, 4));
			RecipeHandler.addChemicalDissolutionChamberRecipe(MekanismUtils.size(ore, 1), new GasStack(GasRegistry.getGas("osmium"), 1000));
		}
		
		for(ItemStack ore : OreDictionary.getOres("oreIron"))
		{
			RecipeHandler.addEnrichmentChamberRecipe(MekanismUtils.size(ore, 1), new ItemStack(OreCoreItems.ironDust, 2));
	        RecipeHandler.addPurificationChamberRecipe(MekanismUtils.size(ore, 1), new ItemStack(OreCoreExtendedItems.ironClump, 3));
			RecipeHandler.addChemicalInjectionChamberRecipe(new AdvancedInput(MekanismUtils.size(ore, 1), GasRegistry.getGas("hydrogenChloride")), new ItemStack(OreCoreExtendedItems.ironShard, 4));
			RecipeHandler.addChemicalDissolutionChamberRecipe(MekanismUtils.size(ore, 1), new GasStack(GasRegistry.getGas("iron"), 1000));
		}
		
		for(ItemStack ore : OreDictionary.getOres("oreGold"))
		{
			RecipeHandler.addEnrichmentChamberRecipe(MekanismUtils.size(ore, 1), new ItemStack(OreCoreItems.goldDust, 2));
	        RecipeHandler.addPurificationChamberRecipe(MekanismUtils.size(ore, 1), new ItemStack(OreCoreExtendedItems.goldClump, 3));
			RecipeHandler.addChemicalInjectionChamberRecipe(new AdvancedInput(MekanismUtils.size(ore, 1), GasRegistry.getGas("hydrogenChloride")), new ItemStack(OreCoreExtendedItems.goldShard, 4));
			RecipeHandler.addChemicalDissolutionChamberRecipe(MekanismUtils.size(ore, 1), new GasStack(GasRegistry.getGas("gold"), 1000));
		}
		
		for(ItemStack ore : OreDictionary.getOres("oreSilver"))
		{
			RecipeHandler.addEnrichmentChamberRecipe(MekanismUtils.size(ore, 1), new ItemStack(OreCoreItems.silverDust, 2));
			RecipeHandler.addPurificationChamberRecipe(MekanismUtils.size(ore, 1), new ItemStack(OreCoreExtendedItems.silverClump, 3));
			RecipeHandler.addChemicalInjectionChamberRecipe(new AdvancedInput(MekanismUtils.size(ore, 1), GasRegistry.getGas("hydrogenChloride")), new ItemStack(OreCoreExtendedItems.silverShard, 4));
			RecipeHandler.addChemicalDissolutionChamberRecipe(MekanismUtils.size(ore, 1), new GasStack(GasRegistry.getGas("silver"), 1000));
		}
		
		for(ItemStack ore : OreDictionary.getOres("oreLead"))
		{
			RecipeHandler.addEnrichmentChamberRecipe(MekanismUtils.size(ore, 1), new ItemStack(OreCoreItems.leadDust, 2));
			RecipeHandler.addPurificationChamberRecipe(MekanismUtils.size(ore, 1), new ItemStack(OreCoreExtendedItems.leadClump, 3));
			RecipeHandler.addChemicalInjectionChamberRecipe(new AdvancedInput(MekanismUtils.size(ore, 1), GasRegistry.getGas("hydrogenChloride")), new ItemStack(OreCoreExtendedItems.leadShard, 4));
			RecipeHandler.addChemicalDissolutionChamberRecipe(MekanismUtils.size(ore, 1), new GasStack(GasRegistry.getGas("lead"), 1000));
		}
		
		for(ItemStack ore : OreDictionary.getOres("oreNickel"))
		{
			RecipeHandler.addEnrichmentChamberRecipe(MekanismUtils.size(ore, 1), new ItemStack(OreCoreItems.nickelDust, 2));
	        RecipeHandler.addPurificationChamberRecipe(MekanismUtils.size(ore, 1), new ItemStack(OreCoreExtendedItems.nickelClump, 3));
			RecipeHandler.addChemicalInjectionChamberRecipe(new AdvancedInput(MekanismUtils.size(ore, 1), GasRegistry.getGas("hydrogenChloride")), new ItemStack(OreCoreExtendedItems.nickelShard, 4));
		}
		
		for(ItemStack ore : OreDictionary.getOres("oreYellorite"))
		{
			try {
				RecipeHandler.addEnrichmentChamberRecipe(MekanismUtils.size(ore, 1), StackUtils.size(OreDictionary.getOres("dustYellorium").get(0), 2));
			} catch(Exception e) {}
		}
		
		for(ItemStack ore : OreDictionary.getOres("oreCertusQuartz"))
		{
			try {
				RecipeHandler.addEnrichmentChamberRecipe(MekanismUtils.size(ore, 1), StackUtils.size(OreDictionary.getOres("dustCertusQuartz").get(0), 4));
			} catch(Exception e) {}
		}
		
		for(ItemStack ore : OreDictionary.getOres("crystalCertusQuartz"))
		{
			try {
				RecipeHandler.addCrusherRecipe(MekanismUtils.size(ore, 1), StackUtils.size(OreDictionary.getOres("dustCertusQuartz").get(0), 1));
			} catch(Exception e) {}
		}
		
		for(ItemStack ore : OreDictionary.getOres("dustCertusQuartz"))
		{
			try {
				RecipeHandler.addEnrichmentChamberRecipe(MekanismUtils.size(ore, 1), StackUtils.size(OreDictionary.getOres("crystalCertusQuartz").get(0), 1));
			} catch(Exception e) {}
		}
		
		for(ItemStack ore : OreDictionary.getOres("gemQuartz"))
		{
			try {
				RecipeHandler.addCrusherRecipe(MekanismUtils.size(ore, 1), StackUtils.size(OreDictionary.getOres("dustNetherQuartz").get(0), 1));
			} catch(Exception e) {}
		}
		
		for(ItemStack ore : OreDictionary.getOres("dustNetherQuartz"))
		{
			try {
				RecipeHandler.addEnrichmentChamberRecipe(MekanismUtils.size(ore, 1), StackUtils.size(OreDictionary.getOres("gemQuartz").get(0), 1));
			} catch(Exception e) {}
		}
		
		for(ItemStack ore : OreDictionary.getOres("oreQuartz"))
		{
			RecipeHandler.addEnrichmentChamberRecipe(MekanismUtils.size(ore, 1), new ItemStack(Items.quartz, 6));
		}
		
		for(ItemStack ore : OreDictionary.getOres("ingotCopper"))
		{
			RecipeHandler.addCrusherRecipe(MekanismUtils.size(ore, 1), new ItemStack(OreCoreItems.copperDust, 1));
		}
	
		for(ItemStack ore : OreDictionary.getOres("ingotTin"))
		{
			RecipeHandler.addCrusherRecipe(MekanismUtils.size(ore, 1), new ItemStack(OreCoreItems.tinDust, 1));
		}
		
		for(ItemStack ore : OreDictionary.getOres("ingotLead"))
		{
			RecipeHandler.addCrusherRecipe(MekanismUtils.size(ore, 1), new ItemStack(OreCoreItems.leadDust, 1));
		}
		
		for(ItemStack ore : OreDictionary.getOres("ingotSilver"))
		{
			RecipeHandler.addCrusherRecipe(MekanismUtils.size(ore, 1), new ItemStack(OreCoreItems.silverDust, 1));
		}
		
		for(ItemStack ore : OreDictionary.getOres("ingotRefinedObsidian"))
		{
			RecipeHandler.addCrusherRecipe(MekanismUtils.size(ore, 1), new ItemStack(OreCoreExtendedItems.obsidianDust, 1));
		}
		
		for(ItemStack ore : OreDictionary.getOres("ingotObsidian"))
		{
			RecipeHandler.addCrusherRecipe(MekanismUtils.size(ore, 1), new ItemStack(OreCoreExtendedItems.obsidianDust, 1));
		}
		
		for(ItemStack ore : OreDictionary.getOres("ingotOsmium"))
		{
			RecipeHandler.addCrusherRecipe(MekanismUtils.size(ore, 1), new ItemStack(OreCoreItems.osmiumDust, 1));
			RecipeHandler.addMetallurgicInfuserRecipe(InfusionInput.getInfusion(InfuseRegistry.get("REDSTONE"), 10, MekanismUtils.size(ore, 1)), new ItemStack(Mekanism.ControlCircuit, 1, 0));
		}
		
		for(ItemStack ore : OreDictionary.getOres("ingotRedstone"))
		{
			RecipeHandler.addCrusherRecipe(MekanismUtils.size(ore, 1), new ItemStack(Items.redstone));
		}
		
		for(ItemStack ore : OreDictionary.getOres("ingotRefinedGlowstone"))
		{
			RecipeHandler.addCrusherRecipe(MekanismUtils.size(ore, 1), new ItemStack(Items.glowstone_dust));
		}
		
		for(ItemStack ore : OreDictionary.getOres("ingotNickel"))
		{
			try {
				RecipeHandler.addCrusherRecipe(MekanismUtils.size(ore, 1), new ItemStack(OreCoreItems.nickelDust, 1));
			} catch(Exception e) {}
		}
		
		try {
			RecipeHandler.addCrusherRecipe(new ItemStack(OreCoreItems.bronzeIngot, 1), new ItemStack(OreCoreItems.bronzeDust, 1));
			
			if(Mekanism.hooks.IC2Loaded)
			{
				addIC2BronzeRecipe();
			}
		} catch(Exception e) {}
		
		try {
			RecipeHandler.addCrusherRecipe(new ItemStack(Items.coal), MekanismUtils.size(OreDictionary.getOres("dustCoal").get(0), 1));
		} catch(Exception e) {}
		
		try {
			RecipeHandler.addCrusherRecipe(new ItemStack(Items.coal, 1, 1), MekanismUtils.size(OreDictionary.getOres("dustCharcoal").get(0), 1));
		} catch(Exception e) {}
		
		for(ItemStack ore : OreDictionary.getOres("dustIron"))
		{
			RecipeHandler.addCombinerRecipe(MekanismUtils.size(ore, 8), new ItemStack(Blocks.iron_ore));
		}
		
		for(ItemStack ore : OreDictionary.getOres("ingotSteel"))
		{
			RecipeHandler.addCrusherRecipe(MekanismUtils.size(ore, 1), new ItemStack(OreCoreItems.steelDust, 1));
		}
		
		for(ItemStack ore : OreDictionary.getOres("dustGold"))
		{
			RecipeHandler.addCombinerRecipe(MekanismUtils.size(ore, 8), new ItemStack(Blocks.gold_ore));
		}
		
		for(ItemStack ore : OreDictionary.getOres("dustLapisLazuli"))
		{
			RecipeHandler.addCrusherRecipe(new ItemStack(Items.dye, 1, 4), MekanismUtils.size(ore, 1));
		}
		
		for(ItemStack ore : OreDictionary.getOres("dustDirtyObsidian"))
		{
			RecipeHandler.addCombinerRecipe(MekanismUtils.size(ore, 4), new ItemStack(Blocks.obsidian));
			RecipeHandler.addMetallurgicInfuserRecipe(InfusionInput.getInfusion(InfuseRegistry.get("DIAMOND"), 10, MekanismUtils.size(ore, 1)), new ItemStack(OreCoreExtendedItems.obsidianDust, 1));
		}
		
		for(ItemStack ore : OreDictionary.getOres("dustOsmium"))
		{
			RecipeHandler.addCombinerRecipe(MekanismUtils.size(ore, 8), new ItemStack(OreCoreItems.osmium, 1));
		}
		
		for(ItemStack ore : OreDictionary.getOres("dustDiamond"))
		{
			InfuseRegistry.registerInfuseObject(ore, new InfuseObject(InfuseRegistry.get("DIAMOND"), 10));
			RecipeHandler.addEnrichmentChamberRecipe(MekanismUtils.size(ore, 1), new ItemStack(Items.diamond));
		}
		
		for(ItemStack ore : OreDictionary.getOres("dustCopper"))
		{
			RecipeHandler.addCombinerRecipe(MekanismUtils.size(ore, 8), new ItemStack(OreCoreItems.copper, 1));
		}
		
		for(ItemStack ore : OreDictionary.getOres("ingotCopper"))
		{
			RecipeHandler.addMetallurgicInfuserRecipe(InfusionInput.getInfusion(InfuseRegistry.get("TIN"), 10, MekanismUtils.size(ore, 1)), new ItemStack(OreCoreItems.bronzeIngot, 1));
		}
			
		for(ItemStack ore : OreDictionary.getOres("dustTin"))
		{
			RecipeHandler.addCombinerRecipe(MekanismUtils.size(ore, 8), new ItemStack(OreCoreItems.tin, 1, 2));
			InfuseRegistry.registerInfuseObject(ore, new InfuseObject(InfuseRegistry.get("TIN"), 50));
		}
		
		try {
			for(ItemStack ore : OreDictionary.getOres("dustLead"))
			{
				RecipeHandler.addCombinerRecipe(MekanismUtils.size(ore, 8), MekanismUtils.size(OreDictionary.getOres("oreLead").get(0), 1));
			}
		} catch(Exception e) {}
		
		try {
			for(ItemStack ore : OreDictionary.getOres("dustSilver"))
			{
				RecipeHandler.addCombinerRecipe(MekanismUtils.size(ore, 8), new ItemStack(OreCoreItems.silver, 1));
			}
		} catch(Exception e) {}

		try {
			for(ItemStack ore : OreDictionary.getOres("treeSapling"))
			{
				if(ore.getItemDamage() == 0 || ore.getItemDamage() == OreDictionary.WILDCARD_VALUE)
				{
					RecipeHandler.addCrusherRecipe(new ItemStack(ore.getItem(), 1, OreDictionary.WILDCARD_VALUE), new ItemStack(Mekanism.BioFuel, 2));
				}
			}
		} catch(Exception e) {}

	}
	
	@Method(modid = "IC2")
	public static void addIC2BronzeRecipe()
	{
		try {
			Recipes.macerator.addRecipe(new RecipeInputOreDict("ingotBronze"), null, new ItemStack(OreCoreItems.bronzeDust, 1));
		} catch(Exception e) {}
	}


	/**
	 * Handy method for retrieving all log items, finding their corresponding planks, and making recipes with them. Taken from CofhCore.
	 */
	public static void addLogRecipes()
	{
		Container tempContainer = new Container() {
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
						RecipeHandler.addPrecisionSawmillRecipe(log, new ChanceOutput(StackUtils.size(resultEntry, 6), new ItemStack(Mekanism.Sawdust), 1));
					}
				}
			}
			else {
				ItemStack log = StackUtils.size(logEntry, 1);
				tempCrafting.setInventorySlotContents(0, log);
				ItemStack resultEntry = MekanismUtils.findMatchingRecipe(tempCrafting, null);

				if(resultEntry != null)
				{
					RecipeHandler.addPrecisionSawmillRecipe(log, new ChanceOutput(StackUtils.size(resultEntry, 6), new ItemStack(Mekanism.Sawdust), 1));
				}
			}
		}
	}
}
