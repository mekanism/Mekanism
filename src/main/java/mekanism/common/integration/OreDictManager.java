package mekanism.common.integration;

import java.util.List;

import mekanism.api.gas.GasRegistry;
import mekanism.api.gas.GasStack;
import mekanism.api.infuse.InfuseObject;
import mekanism.api.infuse.InfuseRegistry;
import mekanism.api.util.StackUtils;
import mekanism.common.Mekanism;
import mekanism.common.MekanismBlocks;
import mekanism.common.MekanismItems;
import mekanism.common.recipe.RecipeHandler;
import mekanism.common.recipe.RecipeHandler.Recipe;
import mekanism.common.util.MekanismUtils;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.FurnaceRecipes;
import net.minecraftforge.oredict.OreDictionary;
import net.minecraftforge.fml.common.Optional.Method;

import ic2.api.recipe.RecipeInputOreDict;
import ic2.api.recipe.Recipes;

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
					RecipeHandler.addPrecisionSawmillRecipe(wildStack, new ItemStack(Items.stick, 6), new ItemStack(MekanismItems.Sawdust), 0.25);
				}
			}
			else {
				RecipeHandler.addPrecisionSawmillRecipe(StackUtils.size(ore, 1), new ItemStack(Items.stick, 6), new ItemStack(MekanismItems.Sawdust), 0.25);
			}
		}
		
		for(ItemStack ore : OreDictionary.getOres("oreNetherSteel"))
		{
			RecipeHandler.addEnrichmentChamberRecipe(StackUtils.size(ore, 1), new ItemStack(MekanismItems.Dust, 4, 5));
		}
		
		if(OreDictionary.getOres("itemRubber").size() > 0)
		{
			for(ItemStack ore : OreDictionary.getOres("woodRubber"))
			{
				RecipeHandler.addPrecisionSawmillRecipe(MekanismUtils.size(ore, 1), new ItemStack(Blocks.planks, 4), MekanismUtils.size(OreDictionary.getOres("itemRubber").get(0), 1), 1F);
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
			RecipeHandler.addOsmiumCompressorRecipe(MekanismUtils.size(ore, 1), new ItemStack(MekanismItems.Ingot, 1, 0));
			RecipeHandler.addCrusherRecipe(MekanismUtils.size(ore, 1), new ItemStack(MekanismItems.DirtyDust, 1, 6));
			RecipeHandler.addEnrichmentChamberRecipe(MekanismUtils.size(ore, 1), new ItemStack(MekanismItems.CompressedObsidian));
			
			InfuseRegistry.registerInfuseObject(MekanismUtils.size(ore, 1), new InfuseObject(InfuseRegistry.get("OBSIDIAN"), 10));
		}
		
        for(ItemStack ore : OreDictionary.getOres("clumpIron"))
        {
            RecipeHandler.addCrusherRecipe(MekanismUtils.size(ore, 1), new ItemStack(MekanismItems.DirtyDust, 1, 0));
        }
        
        for(ItemStack ore : OreDictionary.getOres("clumpGold"))
        {
            RecipeHandler.addCrusherRecipe(MekanismUtils.size(ore, 1), new ItemStack(MekanismItems.DirtyDust, 1, 1));
        }
        
        for(ItemStack ore : OreDictionary.getOres("clumpOsmium"))
        {
            RecipeHandler.addCrusherRecipe(MekanismUtils.size(ore, 1), new ItemStack(MekanismItems.DirtyDust, 1, 2));
        }
        
        for(ItemStack ore : OreDictionary.getOres("clumpCopper"))
        {
            RecipeHandler.addCrusherRecipe(MekanismUtils.size(ore, 1), new ItemStack(MekanismItems.DirtyDust, 1, 3));
        }
        
        for(ItemStack ore : OreDictionary.getOres("clumpTin"))
        {
            RecipeHandler.addCrusherRecipe(MekanismUtils.size(ore, 1), new ItemStack(MekanismItems.DirtyDust, 1, 4));
        }
        
        for(ItemStack ore : OreDictionary.getOres("clumpSilver"))
        {
            RecipeHandler.addCrusherRecipe(MekanismUtils.size(ore, 1), new ItemStack(MekanismItems.DirtyDust, 1, 5));
        }
        
        for(ItemStack ore : OreDictionary.getOres("clumpObsidian"))
        {
            RecipeHandler.addCrusherRecipe(MekanismUtils.size(ore, 1), new ItemStack(MekanismItems.DirtyDust, 1, 6));
        }
        
        for(ItemStack ore : OreDictionary.getOres("clumpLead"))
        {
            RecipeHandler.addCrusherRecipe(MekanismUtils.size(ore, 1), new ItemStack(MekanismItems.DirtyDust, 1, 7));
        }
		
		for(ItemStack ore : OreDictionary.getOres("shardIron"))
		{
			RecipeHandler.addPurificationChamberRecipe(MekanismUtils.size(ore, 1), new ItemStack(MekanismItems.Clump, 1, 0));
		}
		
		for(ItemStack ore : OreDictionary.getOres("shardGold"))
		{
			RecipeHandler.addPurificationChamberRecipe(MekanismUtils.size(ore, 1), new ItemStack(MekanismItems.Clump, 1, 1));
		}
		
		for(ItemStack ore : OreDictionary.getOres("shardOsmium"))
		{
			RecipeHandler.addPurificationChamberRecipe(MekanismUtils.size(ore, 1), new ItemStack(MekanismItems.Clump, 1, 2));
		}
		
		for(ItemStack ore : OreDictionary.getOres("shardCopper"))
		{
			RecipeHandler.addPurificationChamberRecipe(MekanismUtils.size(ore, 1), new ItemStack(MekanismItems.Clump, 1, 3));
		}
		
		for(ItemStack ore : OreDictionary.getOres("shardTin"))
		{
			RecipeHandler.addPurificationChamberRecipe(MekanismUtils.size(ore, 1), new ItemStack(MekanismItems.Clump, 1, 4));
		}
		
		for(ItemStack ore : OreDictionary.getOres("shardSilver"))
		{
			RecipeHandler.addPurificationChamberRecipe(MekanismUtils.size(ore, 1), new ItemStack(MekanismItems.Clump, 1, 5));
		}
		
		for(ItemStack ore : OreDictionary.getOres("shardObsidian"))
		{
			RecipeHandler.addPurificationChamberRecipe(MekanismUtils.size(ore, 1), new ItemStack(MekanismItems.Clump, 1, 6));
		}
		
		for(ItemStack ore : OreDictionary.getOres("shardLead"))
		{
			RecipeHandler.addPurificationChamberRecipe(MekanismUtils.size(ore, 1), new ItemStack(MekanismItems.Clump, 1, 7));
		}
		
		for(ItemStack ore : OreDictionary.getOres("crystalIron"))
		{
			RecipeHandler.addChemicalInjectionChamberRecipe(MekanismUtils.size(ore, 1), "hydrogenChloride", new ItemStack(MekanismItems.Shard, 1, 0));
		}
		
		for(ItemStack ore : OreDictionary.getOres("crystalGold"))
		{
			RecipeHandler.addChemicalInjectionChamberRecipe(MekanismUtils.size(ore, 1), "hydrogenChloride", new ItemStack(MekanismItems.Shard, 1, 1));
		}
		
		for(ItemStack ore : OreDictionary.getOres("crystalOsmium"))
		{
			RecipeHandler.addChemicalInjectionChamberRecipe(MekanismUtils.size(ore, 1), "hydrogenChloride", new ItemStack(MekanismItems.Shard, 1, 2));
		}
		
		for(ItemStack ore : OreDictionary.getOres("crystalCopper"))
		{
			RecipeHandler.addChemicalInjectionChamberRecipe(MekanismUtils.size(ore, 1), "hydrogenChloride", new ItemStack(MekanismItems.Shard, 1, 3));
		}
		
		for(ItemStack ore : OreDictionary.getOres("crystalTin"))
		{
			RecipeHandler.addChemicalInjectionChamberRecipe(MekanismUtils.size(ore, 1), "hydrogenChloride", new ItemStack(MekanismItems.Shard, 1, 4));
		}
		
		for(ItemStack ore : OreDictionary.getOres("crystalSilver"))
		{
			RecipeHandler.addChemicalInjectionChamberRecipe(MekanismUtils.size(ore, 1), "hydrogenChloride", new ItemStack(MekanismItems.Shard, 1, 5));
		}
		
		for(ItemStack ore : OreDictionary.getOres("crystalObsidian"))
		{
			RecipeHandler.addChemicalInjectionChamberRecipe(MekanismUtils.size(ore, 1), "hydrogenChloride", new ItemStack(MekanismItems.Shard, 1, 6));
		}
		
		for(ItemStack ore : OreDictionary.getOres("crystalLead"))
		{
			RecipeHandler.addChemicalInjectionChamberRecipe(MekanismUtils.size(ore, 1), "hydrogenChloride", new ItemStack(MekanismItems.Shard, 1, 7));
		}
		
		for(ItemStack ore : OreDictionary.getOres("dustDirtyIron"))
		{
			RecipeHandler.addEnrichmentChamberRecipe(MekanismUtils.size(ore, 1), new ItemStack(MekanismItems.Dust, 1, 0));
		}
		
		for(ItemStack ore : OreDictionary.getOres("dustDirtyGold"))
		{
			RecipeHandler.addEnrichmentChamberRecipe(MekanismUtils.size(ore, 1), new ItemStack(MekanismItems.Dust, 1, 1));
		}
		
		for(ItemStack ore : OreDictionary.getOres("dustDirtyOsmium"))
		{
			RecipeHandler.addEnrichmentChamberRecipe(MekanismUtils.size(ore, 1), new ItemStack(MekanismItems.Dust, 1, 2));
		}
		
		for(ItemStack ore : OreDictionary.getOres("dustDirtyCopper"))
		{
			RecipeHandler.addEnrichmentChamberRecipe(MekanismUtils.size(ore, 1), new ItemStack(MekanismItems.Dust, 1, 6));
		}
		
		for(ItemStack ore : OreDictionary.getOres("dustDirtyTin"))
		{
			RecipeHandler.addEnrichmentChamberRecipe(MekanismUtils.size(ore, 1), new ItemStack(MekanismItems.Dust, 1, 7));
		}
		
		for(ItemStack ore : OreDictionary.getOres("dustDirtySilver"))
		{
			RecipeHandler.addEnrichmentChamberRecipe(MekanismUtils.size(ore, 1), new ItemStack(MekanismItems.Dust, 1, 8));
		}
		
		for(ItemStack ore : OreDictionary.getOres("dustDirtyLead"))
		{
			RecipeHandler.addEnrichmentChamberRecipe(MekanismUtils.size(ore, 1), new ItemStack(MekanismItems.Dust, 1, 9));
		}
		
		for(ItemStack ore : OreDictionary.getOres("oreCopper"))
		{
			RecipeHandler.addEnrichmentChamberRecipe(MekanismUtils.size(ore, 1), new ItemStack(MekanismItems.Dust, 2, 6));
			RecipeHandler.addPurificationChamberRecipe(MekanismUtils.size(ore, 1), new ItemStack(MekanismItems.Clump, 3, 3));
			RecipeHandler.addChemicalInjectionChamberRecipe(MekanismUtils.size(ore, 1), "hydrogenChloride", new ItemStack(MekanismItems.Shard, 4, 3));
			RecipeHandler.addChemicalDissolutionChamberRecipe(MekanismUtils.size(ore, 1), new GasStack(GasRegistry.getGas("copper"), 1000));
		}
		
		for(ItemStack ore : OreDictionary.getOres("oreTin"))
		{
			RecipeHandler.addEnrichmentChamberRecipe(MekanismUtils.size(ore, 1), new ItemStack(MekanismItems.Dust, 2, 7));
			RecipeHandler.addPurificationChamberRecipe(MekanismUtils.size(ore, 1), new ItemStack(MekanismItems.Clump, 3, 4));
			RecipeHandler.addChemicalInjectionChamberRecipe(MekanismUtils.size(ore, 1), "hydrogenChloride", new ItemStack(MekanismItems.Shard, 4, 4));
			RecipeHandler.addChemicalDissolutionChamberRecipe(MekanismUtils.size(ore, 1), new GasStack(GasRegistry.getGas("tin"), 1000));
		}
		
		for(ItemStack ore : OreDictionary.getOres("oreOsmium"))
		{
			RecipeHandler.addEnrichmentChamberRecipe(MekanismUtils.size(ore, 1), new ItemStack(MekanismItems.Dust, 2, 2));
			RecipeHandler.addPurificationChamberRecipe(MekanismUtils.size(ore, 1), new ItemStack(MekanismItems.Clump, 3, 2));
			RecipeHandler.addChemicalInjectionChamberRecipe(MekanismUtils.size(ore, 1), "hydrogenChloride", new ItemStack(MekanismItems.Shard, 4, 2));
			RecipeHandler.addChemicalDissolutionChamberRecipe(MekanismUtils.size(ore, 1), new GasStack(GasRegistry.getGas("osmium"), 1000));
		}
		
		for(ItemStack ore : OreDictionary.getOres("oreIron"))
		{
			RecipeHandler.addEnrichmentChamberRecipe(MekanismUtils.size(ore, 1), new ItemStack(MekanismItems.Dust, 2, 0));
	        RecipeHandler.addPurificationChamberRecipe(MekanismUtils.size(ore, 1), new ItemStack(MekanismItems.Clump, 3, 0));
			RecipeHandler.addChemicalInjectionChamberRecipe(MekanismUtils.size(ore, 1), "hydrogenChloride", new ItemStack(MekanismItems.Shard, 4, 0));
			RecipeHandler.addChemicalDissolutionChamberRecipe(MekanismUtils.size(ore, 1), new GasStack(GasRegistry.getGas("iron"), 1000));
		}
		
		for(ItemStack ore : OreDictionary.getOres("oreGold"))
		{
			RecipeHandler.addEnrichmentChamberRecipe(MekanismUtils.size(ore, 1), new ItemStack(MekanismItems.Dust, 2, 1));
	        RecipeHandler.addPurificationChamberRecipe(MekanismUtils.size(ore, 1), new ItemStack(MekanismItems.Clump, 3, 1));
			RecipeHandler.addChemicalInjectionChamberRecipe(MekanismUtils.size(ore, 1), "hydrogenChloride", new ItemStack(MekanismItems.Shard, 4, 1));
			RecipeHandler.addChemicalDissolutionChamberRecipe(MekanismUtils.size(ore, 1), new GasStack(GasRegistry.getGas("gold"), 1000));
		}
		
		for(ItemStack ore : OreDictionary.getOres("oreSilver"))
		{
			RecipeHandler.addEnrichmentChamberRecipe(MekanismUtils.size(ore, 1), new ItemStack(MekanismItems.Dust, 2, 8));
			RecipeHandler.addPurificationChamberRecipe(MekanismUtils.size(ore, 1), new ItemStack(MekanismItems.Clump, 3, 5));
			RecipeHandler.addChemicalInjectionChamberRecipe(MekanismUtils.size(ore, 1), "hydrogenChloride", new ItemStack(MekanismItems.Shard, 4, 5));
			RecipeHandler.addChemicalDissolutionChamberRecipe(MekanismUtils.size(ore, 1), new GasStack(GasRegistry.getGas("silver"), 1000));
		}
		
		for(ItemStack ore : OreDictionary.getOres("oreLead"))
		{
			RecipeHandler.addEnrichmentChamberRecipe(MekanismUtils.size(ore, 1), new ItemStack(MekanismItems.Dust, 2, 9));
			RecipeHandler.addPurificationChamberRecipe(MekanismUtils.size(ore, 1), new ItemStack(MekanismItems.Clump, 3, 7));
			RecipeHandler.addChemicalInjectionChamberRecipe(MekanismUtils.size(ore, 1), "hydrogenChloride", new ItemStack(MekanismItems.Shard, 4, 7));
			RecipeHandler.addChemicalDissolutionChamberRecipe(MekanismUtils.size(ore, 1), new GasStack(GasRegistry.getGas("lead"), 1000));
		}
		
		for(ItemStack ore : OreDictionary.getOres("oreNickel"))
		{
			try {
				RecipeHandler.addEnrichmentChamberRecipe(MekanismUtils.size(ore, 1), StackUtils.size(OreDictionary.getOres("dustNickel").get(0), 2));
			} catch(Exception e) {}
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
		
		for(ItemStack ore : OreDictionary.getOres("crystalFluix"))
		{
			try {
				RecipeHandler.addCrusherRecipe(MekanismUtils.size(ore, 1), StackUtils.size(OreDictionary.getOres("dustFluix").get(0), 1));
			} catch(Exception e) {}
		}
		
		for(ItemStack ore : OreDictionary.getOres("dustFluix"))
		{
			try {
				RecipeHandler.addEnrichmentChamberRecipe(MekanismUtils.size(ore, 1), StackUtils.size(OreDictionary.getOres("crystalFluix").get(0), 1));
			} catch(Exception e) {}
		}
		
		for(ItemStack ore : OreDictionary.getOres("ingotCopper"))
		{
			RecipeHandler.addCrusherRecipe(MekanismUtils.size(ore, 1), new ItemStack(MekanismItems.Dust, 1, 6));
		}
	
		for(ItemStack ore : OreDictionary.getOres("ingotTin"))
		{
			RecipeHandler.addCrusherRecipe(MekanismUtils.size(ore, 1), new ItemStack(MekanismItems.Dust, 1, 7));
		}
		
		for(ItemStack ore : OreDictionary.getOres("ingotLead"))
		{
			RecipeHandler.addCrusherRecipe(MekanismUtils.size(ore, 1), new ItemStack(MekanismItems.Dust, 1, 9));
		}
		
		for(ItemStack ore : OreDictionary.getOres("ingotSilver"))
		{
			RecipeHandler.addCrusherRecipe(MekanismUtils.size(ore, 1), new ItemStack(MekanismItems.Dust, 1, 8));
		}
		
		for(ItemStack ore : OreDictionary.getOres("ingotRefinedObsidian"))
		{
			RecipeHandler.addCrusherRecipe(MekanismUtils.size(ore, 1), new ItemStack(MekanismItems.Dust, 1, 3));
		}
		
		for(ItemStack ore : OreDictionary.getOres("ingotOsmium"))
		{
			RecipeHandler.addCrusherRecipe(MekanismUtils.size(ore, 1), new ItemStack(MekanismItems.Dust, 1, 2));
			RecipeHandler.addMetallurgicInfuserRecipe(InfuseRegistry.get("REDSTONE"), 10, MekanismUtils.size(ore, 1), new ItemStack(MekanismItems.ControlCircuit, 1, 0));
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
				RecipeHandler.addCrusherRecipe(MekanismUtils.size(ore, 1), StackUtils.size(OreDictionary.getOres("dustNickel").get(0), 1));
			} catch(Exception e) {}
		}
		
		try {
			RecipeHandler.addCrusherRecipe(new ItemStack(MekanismItems.Ingot, 1, 2), MekanismUtils.size(OreDictionary.getOres("dustBronze").get(0), 1));
			
			if(Mekanism.hooks.IC2Loaded)
			{
				addIC2BronzeRecipe();
			}
		} catch(Exception e) {}
		
		try {
			FurnaceRecipes.instance().addSmeltingRecipe(new ItemStack(MekanismItems.Dust, 1, 8), MekanismUtils.size(OreDictionary.getOres("ingotSilver").get(0), 1), 0.0F);
		} catch(Exception e) {}
		
		try {
			FurnaceRecipes.instance().addSmeltingRecipe(new ItemStack(MekanismItems.Dust, 1, 9), MekanismUtils.size(OreDictionary.getOres("ingotLead").get(0), 1), 0.0F);
		} catch(Exception e) {}
		
		try {
			RecipeHandler.addCrusherRecipe(new ItemStack(Items.coal), MekanismUtils.size(OreDictionary.getOres("dustCoal").get(0), 1));
		} catch(Exception e) {}
		
		try {
			RecipeHandler.addCrusherRecipe(new ItemStack(Items.coal, 1, 1), MekanismUtils.size(OreDictionary.getOres("dustCharcoal").get(0), 1));
		} catch(Exception e) {}
		
		try {
			for(ItemStack ore : OreDictionary.getOres("ingotSilver"))
			{
				RecipeHandler.addCrusherRecipe(MekanismUtils.size(ore, 1), new ItemStack(MekanismItems.Dust, 1, 8));
			}
		} catch(Exception e) {}
		
		for(ItemStack ore : OreDictionary.getOres("dustIron"))
		{
			RecipeHandler.addCombinerRecipe(MekanismUtils.size(ore, 8), new ItemStack(Blocks.iron_ore));
		}
		
		for(ItemStack ore : OreDictionary.getOres("ingotSteel"))
		{
			RecipeHandler.addCrusherRecipe(MekanismUtils.size(ore, 1), new ItemStack(MekanismItems.Dust, 1, 5));
		}
		
		for(ItemStack ore : OreDictionary.getOres("dustGold"))
		{
			RecipeHandler.addCombinerRecipe(MekanismUtils.size(ore, 8), new ItemStack(Blocks.gold_ore));
		}
		
		for(ItemStack ore : OreDictionary.getOres("dustLapisLazuli"))
		{
			RecipeHandler.addCrusherRecipe(new ItemStack(Items.dye, 1, 4), MekanismUtils.size(ore, 1));
		}
		
		for(ItemStack ore : OreDictionary.getOres("dustObsidian"))
		{
			RecipeHandler.addCombinerRecipe(MekanismUtils.size(ore, 4), new ItemStack(Blocks.obsidian));
			RecipeHandler.addMetallurgicInfuserRecipe(InfuseRegistry.get("DIAMOND"), 10, MekanismUtils.size(ore, 1), new ItemStack(MekanismItems.Dust, 1, 3));
		}
		
		for(ItemStack ore : OreDictionary.getOres("dustOsmium"))
		{
			RecipeHandler.addCombinerRecipe(MekanismUtils.size(ore, 8), new ItemStack(MekanismBlocks.OreBlock, 1, 0));
		}
		
		for(ItemStack ore : OreDictionary.getOres("dustDiamond"))
		{
			InfuseRegistry.registerInfuseObject(ore, new InfuseObject(InfuseRegistry.get("DIAMOND"), 10));
			RecipeHandler.addEnrichmentChamberRecipe(MekanismUtils.size(ore, 1), new ItemStack(Items.diamond));
		}
		
		for(ItemStack ore : OreDictionary.getOres("dustCopper"))
		{
			RecipeHandler.addCombinerRecipe(MekanismUtils.size(ore, 8), new ItemStack(MekanismBlocks.OreBlock, 1, 1));
		}
		
		for(ItemStack ore : OreDictionary.getOres("ingotCopper"))
		{
			RecipeHandler.addMetallurgicInfuserRecipe(InfuseRegistry.get("TIN"), 10, MekanismUtils.size(ore, 1), new ItemStack(MekanismItems.Ingot, 1, 2));
		}
			
		for(ItemStack ore : OreDictionary.getOres("dustTin"))
		{
			RecipeHandler.addCombinerRecipe(MekanismUtils.size(ore, 8), new ItemStack(MekanismBlocks.OreBlock, 1, 2));
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
				RecipeHandler.addCombinerRecipe(MekanismUtils.size(ore, 8), MekanismUtils.size(OreDictionary.getOres("oreSilver").get(0), 1));
			}
		} catch(Exception e) {}

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
			Recipes.macerator.addRecipe(new RecipeInputOreDict("ingotBronze"), null, MekanismUtils.size(OreDictionary.getOres("dustBronze").get(0), 1));
		} catch(Exception e) {}
	}


	/**
	 * Handy method for retrieving all log items, finding their corresponding planks, and making recipes with them. Credit to CofhCore.
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

		for(int i = 1; i < 9; i++)
		{
			tempCrafting.setInventorySlotContents(i, null);
		}

		List<ItemStack> registeredOres = OreDictionary.getOres("logWood");
		
		for(int i = 0; i < registeredOres.size(); i++)
		{
			ItemStack logEntry = registeredOres.get(i);

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
}
