package mekanism.common.integration;

import ic2.api.recipe.RecipeInputOreDict;
import ic2.api.recipe.Recipes;
import mekanism.api.infuse.InfuseObject;
import mekanism.api.infuse.InfuseRegistry;
import mekanism.api.infuse.InfusionInput;
import mekanism.common.Mekanism;
import mekanism.common.recipe.RecipeHandler;
import mekanism.common.util.MekanismUtils;
import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.FurnaceRecipes;
import net.minecraftforge.oredict.OreDictionary;

public final class OreDictManager
{
	public static void init()
	{
		for(ItemStack ore : OreDictionary.getOres("dustRefinedObsidian"))
		{
			RecipeHandler.addOsmiumCompressorRecipe(MekanismUtils.size(ore, 1), new ItemStack(Mekanism.Ingot, 1, 0));
			RecipeHandler.addCrusherRecipe(MekanismUtils.size(ore, 1), new ItemStack(Mekanism.DirtyDust, 1, 6));
		}
		
        for(ItemStack ore : OreDictionary.getOres("clumpIron"))
        {
            RecipeHandler.addCrusherRecipe(MekanismUtils.size(ore, 1), new ItemStack(Mekanism.DirtyDust, 1, 0));
        }
        
        for(ItemStack ore : OreDictionary.getOres("clumpGold"))
        {
            RecipeHandler.addCrusherRecipe(MekanismUtils.size(ore, 1), new ItemStack(Mekanism.DirtyDust, 1, 1));
        }
        
        for(ItemStack ore : OreDictionary.getOres("clumpOsmium"))
        {
            RecipeHandler.addCrusherRecipe(MekanismUtils.size(ore, 1), new ItemStack(Mekanism.DirtyDust, 1, 2));
        }
        
        for(ItemStack ore : OreDictionary.getOres("clumpCopper"))
        {
            RecipeHandler.addCrusherRecipe(MekanismUtils.size(ore, 1), new ItemStack(Mekanism.DirtyDust, 1, 3));
        }
        
        for(ItemStack ore : OreDictionary.getOres("clumpTin"))
        {
            RecipeHandler.addCrusherRecipe(MekanismUtils.size(ore, 1), new ItemStack(Mekanism.DirtyDust, 1, 4));
        }
        
        for(ItemStack ore : OreDictionary.getOres("clumpSilver"))
        {
            RecipeHandler.addCrusherRecipe(MekanismUtils.size(ore, 1), new ItemStack(Mekanism.DirtyDust, 1, 5));
        }
        
        for(ItemStack ore : OreDictionary.getOres("clumpObsidian"))
        {
            RecipeHandler.addCrusherRecipe(MekanismUtils.size(ore, 1), new ItemStack(Mekanism.DirtyDust, 1, 6));
        }
        
        for(ItemStack ore : OreDictionary.getOres("clumpLead"))
        {
            RecipeHandler.addCrusherRecipe(MekanismUtils.size(ore, 1), new ItemStack(Mekanism.DirtyDust, 1, 7));
        }
		
		for(ItemStack ore : OreDictionary.getOres("shardIron"))
		{
			RecipeHandler.addPurificationChamberRecipe(MekanismUtils.size(ore, 1), new ItemStack(Mekanism.Clump, 1, 0));
		}
		
		for(ItemStack ore : OreDictionary.getOres("shardGold"))
		{
			RecipeHandler.addPurificationChamberRecipe(MekanismUtils.size(ore, 1), new ItemStack(Mekanism.Clump, 1, 1));
		}
		
		for(ItemStack ore : OreDictionary.getOres("shardOsmium"))
		{
			RecipeHandler.addPurificationChamberRecipe(MekanismUtils.size(ore, 1), new ItemStack(Mekanism.Clump, 1, 2));
		}
		
		for(ItemStack ore : OreDictionary.getOres("shardCopper"))
		{
			RecipeHandler.addPurificationChamberRecipe(MekanismUtils.size(ore, 1), new ItemStack(Mekanism.Clump, 1, 3));
		}
		
		for(ItemStack ore : OreDictionary.getOres("shardTin"))
		{
			RecipeHandler.addPurificationChamberRecipe(MekanismUtils.size(ore, 1), new ItemStack(Mekanism.Clump, 1, 4));
		}
		
		for(ItemStack ore : OreDictionary.getOres("shardSilver"))
		{
			RecipeHandler.addPurificationChamberRecipe(MekanismUtils.size(ore, 1), new ItemStack(Mekanism.Clump, 1, 5));
		}
		
		for(ItemStack ore : OreDictionary.getOres("shardObsidian"))
		{
			RecipeHandler.addPurificationChamberRecipe(MekanismUtils.size(ore, 1), new ItemStack(Mekanism.Clump, 1, 6));
		}
		
		for(ItemStack ore : OreDictionary.getOres("shardLead"))
		{
			RecipeHandler.addPurificationChamberRecipe(MekanismUtils.size(ore, 1), new ItemStack(Mekanism.Clump, 1, 7));
		}
		
		for(ItemStack ore : OreDictionary.getOres("shardIron"))
		{
			RecipeHandler.addPurificationChamberRecipe(MekanismUtils.size(ore, 1), new ItemStack(Mekanism.Clump, 1, 0));
		}
		
		for(ItemStack ore : OreDictionary.getOres("shardGold"))
		{
			RecipeHandler.addPurificationChamberRecipe(MekanismUtils.size(ore, 1), new ItemStack(Mekanism.Clump, 1, 1));
		}
		
		for(ItemStack ore : OreDictionary.getOres("shardOsmium"))
		{
			RecipeHandler.addPurificationChamberRecipe(MekanismUtils.size(ore, 1), new ItemStack(Mekanism.Clump, 1, 2));
		}
		
		for(ItemStack ore : OreDictionary.getOres("shardCopper"))
		{
			RecipeHandler.addPurificationChamberRecipe(MekanismUtils.size(ore, 1), new ItemStack(Mekanism.Clump, 1, 3));
		}
		
		for(ItemStack ore : OreDictionary.getOres("shardTin"))
		{
			RecipeHandler.addPurificationChamberRecipe(MekanismUtils.size(ore, 1), new ItemStack(Mekanism.Clump, 1, 4));
		}
		
		for(ItemStack ore : OreDictionary.getOres("shardSilver"))
		{
			RecipeHandler.addPurificationChamberRecipe(MekanismUtils.size(ore, 1), new ItemStack(Mekanism.Clump, 1, 5));
		}
		
		for(ItemStack ore : OreDictionary.getOres("shardObsidian"))
		{
			RecipeHandler.addPurificationChamberRecipe(MekanismUtils.size(ore, 1), new ItemStack(Mekanism.Clump, 1, 6));
		}
		
		for(ItemStack ore : OreDictionary.getOres("shardLead"))
		{
			RecipeHandler.addPurificationChamberRecipe(MekanismUtils.size(ore, 1), new ItemStack(Mekanism.Clump, 1, 7));
		}
		
		for(ItemStack ore : OreDictionary.getOres("dustDirtyIron"))
		{
			RecipeHandler.addEnrichmentChamberRecipe(MekanismUtils.size(ore, 1), new ItemStack(Mekanism.Dust, 1, 0));
		}
		
		for(ItemStack ore : OreDictionary.getOres("dustDirtyGold"))
		{
			RecipeHandler.addEnrichmentChamberRecipe(MekanismUtils.size(ore, 1), new ItemStack(Mekanism.Dust, 1, 1));
		}
		
		for(ItemStack ore : OreDictionary.getOres("dustDirtyOsmium"))
		{
			RecipeHandler.addEnrichmentChamberRecipe(MekanismUtils.size(ore, 1), new ItemStack(Mekanism.Dust, 1, 2));
		}
		
		for(ItemStack ore : OreDictionary.getOres("dustDirtyCopper"))
		{
			RecipeHandler.addEnrichmentChamberRecipe(MekanismUtils.size(ore, 1), new ItemStack(Mekanism.Dust, 1, 6));
		}
		
		for(ItemStack ore : OreDictionary.getOres("dustDirtyTin"))
		{
			RecipeHandler.addEnrichmentChamberRecipe(MekanismUtils.size(ore, 1), new ItemStack(Mekanism.Dust, 1, 7));
		}
		
		for(ItemStack ore : OreDictionary.getOres("dustDirtySilver"))
		{
			RecipeHandler.addEnrichmentChamberRecipe(MekanismUtils.size(ore, 1), new ItemStack(Mekanism.Dust, 1, 8));
		}
		
		for(ItemStack ore : OreDictionary.getOres("dustDirtyLead"))
		{
			RecipeHandler.addEnrichmentChamberRecipe(MekanismUtils.size(ore, 1), new ItemStack(Mekanism.Dust, 1, 9));
		}
		
		for(ItemStack ore : OreDictionary.getOres("oreCopper"))
		{
			RecipeHandler.addEnrichmentChamberRecipe(MekanismUtils.size(ore, 1), new ItemStack(Mekanism.Dust, 2, 6));
			RecipeHandler.addPurificationChamberRecipe(MekanismUtils.size(ore, 1), new ItemStack(Mekanism.Clump, 3, 3));
			RecipeHandler.addChemicalInjectionChamberRecipe(MekanismUtils.size(ore, 1), new ItemStack(Mekanism.Shard, 4, 3));
		}
		
		for(ItemStack ore : OreDictionary.getOres("oreTin"))
		{
			RecipeHandler.addEnrichmentChamberRecipe(MekanismUtils.size(ore, 1), new ItemStack(Mekanism.Dust, 2, 7));
			RecipeHandler.addPurificationChamberRecipe(MekanismUtils.size(ore, 1), new ItemStack(Mekanism.Clump, 3, 4));
			RecipeHandler.addChemicalInjectionChamberRecipe(MekanismUtils.size(ore, 1), new ItemStack(Mekanism.Shard, 4, 4));
		}
		
		for(ItemStack ore : OreDictionary.getOres("oreOsmium"))
		{
			RecipeHandler.addEnrichmentChamberRecipe(MekanismUtils.size(ore, 1), new ItemStack(Mekanism.Dust, 2, 2));
			RecipeHandler.addPurificationChamberRecipe(MekanismUtils.size(ore, 1), new ItemStack(Mekanism.Clump, 3, 2));
			RecipeHandler.addChemicalInjectionChamberRecipe(MekanismUtils.size(ore, 1), new ItemStack(Mekanism.Shard, 4, 2));
		}
		
		for(ItemStack ore : OreDictionary.getOres("oreIron"))
		{
			RecipeHandler.addEnrichmentChamberRecipe(new ItemStack(Block.oreIron), new ItemStack(Mekanism.Dust, 2, 0));
	        RecipeHandler.addPurificationChamberRecipe(new ItemStack(Block.oreIron), new ItemStack(Mekanism.Clump, 3, 0));
			RecipeHandler.addChemicalInjectionChamberRecipe(MekanismUtils.size(ore, 1), new ItemStack(Mekanism.Shard, 4, 0));
		}
		
		for(ItemStack ore : OreDictionary.getOres("oreGold"))
		{
			RecipeHandler.addEnrichmentChamberRecipe(new ItemStack(Block.oreGold), new ItemStack(Mekanism.Dust, 2, 1));
	        RecipeHandler.addPurificationChamberRecipe(new ItemStack(Block.oreGold), new ItemStack(Mekanism.Clump, 3, 1));
			RecipeHandler.addChemicalInjectionChamberRecipe(MekanismUtils.size(ore, 1), new ItemStack(Mekanism.Shard, 4, 1));
		}
		
		for(ItemStack ore : OreDictionary.getOres("oreSilver"))
		{
			RecipeHandler.addEnrichmentChamberRecipe(MekanismUtils.size(ore, 1), new ItemStack(Mekanism.Dust, 2, 8));
			RecipeHandler.addPurificationChamberRecipe(MekanismUtils.size(ore, 1), new ItemStack(Mekanism.Clump, 3, 5));
			RecipeHandler.addChemicalInjectionChamberRecipe(MekanismUtils.size(ore, 1), new ItemStack(Mekanism.Shard, 4, 5));
		}
		
		for(ItemStack ore : OreDictionary.getOres("oreLead"))
		{
			RecipeHandler.addEnrichmentChamberRecipe(MekanismUtils.size(ore, 1), new ItemStack(Mekanism.Dust, 2, 9));
			RecipeHandler.addPurificationChamberRecipe(MekanismUtils.size(ore, 1), new ItemStack(Mekanism.Clump, 3, 7));
			RecipeHandler.addChemicalInjectionChamberRecipe(MekanismUtils.size(ore, 1), new ItemStack(Mekanism.Shard, 4, 7));
		}
		
		for(ItemStack ore : OreDictionary.getOres("ingotLead"))
		{
			RecipeHandler.addCrusherRecipe(MekanismUtils.size(ore, 1), new ItemStack(Mekanism.Dust, 1, 9));
		}
		
		for(ItemStack ore : OreDictionary.getOres("ingotSilver"))
		{
			RecipeHandler.addCrusherRecipe(MekanismUtils.size(ore, 1), new ItemStack(Mekanism.Dust, 1, 8));
		}
		
		for(ItemStack ore : OreDictionary.getOres("ingotRefinedObsidian"))
		{
			RecipeHandler.addCrusherRecipe(MekanismUtils.size(ore, 1), new ItemStack(Mekanism.Dust, 1, 3));
		}
		
		for(ItemStack ore : OreDictionary.getOres("ingotOsmium"))
		{
			RecipeHandler.addCrusherRecipe(MekanismUtils.size(ore, 1), new ItemStack(Mekanism.Dust, 1, 2));
		}
		
		for(ItemStack ore : OreDictionary.getOres("ingotRedstone"))
		{
			RecipeHandler.addCrusherRecipe(MekanismUtils.size(ore, 1), new ItemStack(Item.redstone));
		}
		
		for(ItemStack ore : OreDictionary.getOres("ingotRefinedGlowstone"))
		{
			RecipeHandler.addCrusherRecipe(MekanismUtils.size(ore, 1), new ItemStack(Item.glowstone));
		}
		
		try {
			RecipeHandler.addCrusherRecipe(new ItemStack(Mekanism.Ingot, 1, 2), MekanismUtils.size(OreDictionary.getOres("dustBronze").get(0), 1));
			
			if(Mekanism.hooks.IC2Loaded)
			{
				Recipes.macerator.addRecipe(new RecipeInputOreDict("ingotBronze"), null, MekanismUtils.size(OreDictionary.getOres("dustBronze").get(0), 1));
			}
		} catch(Exception e) {}
		
		try {
			FurnaceRecipes.smelting().addSmelting(Mekanism.Dust.itemID, 8, MekanismUtils.size(OreDictionary.getOres("ingotSilver").get(0), 1), 0.0F);
		} catch(Exception e) {}
		
		try {
			FurnaceRecipes.smelting().addSmelting(Mekanism.Dust.itemID, 9, MekanismUtils.size(OreDictionary.getOres("ingotLead").get(0), 1), 0.0F);
		} catch(Exception e) {}
		
		try {
			RecipeHandler.addCrusherRecipe(new ItemStack(Item.coal), MekanismUtils.size(OreDictionary.getOres("dustCoal").get(0), 1));
		} catch(Exception e) {}
		
		try {
			RecipeHandler.addCrusherRecipe(new ItemStack(Item.coal, 1, 1), MekanismUtils.size(OreDictionary.getOres("dustCharcoal").get(0), 1));
		} catch(Exception e) {}
		
		for(ItemStack ore : OreDictionary.getOres("ingotCopper"))
		{
			RecipeHandler.addCrusherRecipe(MekanismUtils.size(ore, 1), new ItemStack(Mekanism.Dust, 1, 6));
		}
	
		for(ItemStack ore : OreDictionary.getOres("ingotTin"))
		{
			RecipeHandler.addCrusherRecipe(MekanismUtils.size(ore, 1), new ItemStack(Mekanism.Dust, 1, 7));
		}
		
		try {
			for(ItemStack ore : OreDictionary.getOres("ingotSilver"))
			{
				RecipeHandler.addCrusherRecipe(MekanismUtils.size(ore, 1), new ItemStack(Mekanism.Dust, 1, 8));
			}
		} catch(Exception e) {}
		
		for(ItemStack ore : OreDictionary.getOres("dustIron"))
		{
			RecipeHandler.addCombinerRecipe(MekanismUtils.size(ore, 8), new ItemStack(Block.oreIron));
		}
		
		for(ItemStack ore : OreDictionary.getOres("ingotSteel"))
		{
			RecipeHandler.addCrusherRecipe(MekanismUtils.size(ore, 1), new ItemStack(Mekanism.Dust, 1, 5));
		}
		
		for(ItemStack ore : OreDictionary.getOres("dustGold"))
		{
			RecipeHandler.addCombinerRecipe(MekanismUtils.size(ore, 8), new ItemStack(Block.oreGold));
		}
		
		for(ItemStack ore : OreDictionary.getOres("dustLapisLazuli"))
		{
			RecipeHandler.addCrusherRecipe(new ItemStack(Item.dyePowder, 1, 4), MekanismUtils.size(ore, 1));
		}
		
		for(ItemStack ore : OreDictionary.getOres("dustObsidian"))
		{
			RecipeHandler.addCombinerRecipe(MekanismUtils.size(ore, 4), new ItemStack(Block.obsidian));
			RecipeHandler.addMetallurgicInfuserRecipe(InfusionInput.getInfusion(InfuseRegistry.get("DIAMOND"), 10, MekanismUtils.size(ore, 1)), new ItemStack(Mekanism.Dust, 1, 3));
		}
		
		for(ItemStack ore : OreDictionary.getOres("dustOsmium"))
		{
			RecipeHandler.addCombinerRecipe(MekanismUtils.size(ore, 8), new ItemStack(Mekanism.OreBlock, 1, 0));
		}
		
		for(ItemStack ore : OreDictionary.getOres("dustDiamond"))
		{
			InfuseRegistry.registerInfuseObject(ore, new InfuseObject(InfuseRegistry.get("DIAMOND"), 80));
			RecipeHandler.addEnrichmentChamberRecipe(MekanismUtils.size(ore, 1), new ItemStack(Item.diamond));
		}
		
		for(ItemStack ore : OreDictionary.getOres("dustCopper"))
		{
			RecipeHandler.addCombinerRecipe(MekanismUtils.size(ore, 8), new ItemStack(Mekanism.OreBlock, 1, 1));
		}
		
		for(ItemStack ore : OreDictionary.getOres("ingotCopper"))
		{
			RecipeHandler.addMetallurgicInfuserRecipe(InfusionInput.getInfusion(InfuseRegistry.get("TIN"), 10, MekanismUtils.size(ore, 1)), new ItemStack(Mekanism.Ingot, 1, 2));
		}
			
		for(ItemStack ore : OreDictionary.getOres("dustTin"))
		{
			RecipeHandler.addCombinerRecipe(MekanismUtils.size(ore, 8), new ItemStack(Mekanism.OreBlock, 1, 2));
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
	}
}
