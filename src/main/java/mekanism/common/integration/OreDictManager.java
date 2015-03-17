package mekanism.common.integration;

import ic2.api.recipe.RecipeInputOreDict;
import ic2.api.recipe.Recipes;

import java.util.ArrayList;

import mekanism.api.gas.GasRegistry;
import mekanism.api.gas.GasStack;
import mekanism.api.infuse.InfuseObject;
import mekanism.api.infuse.InfuseRegistry;
import mekanism.api.util.StackUtils;
import mekanism.common.Mekanism;
import mekanism.common.MekanismBlocks;
import mekanism.common.MekanismItems;
import mekanism.common.Resource;
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
					RecipeHandler.addPrecisionSawmillRecipe(wildStack, new ItemStack(Items.stick, 6), new ItemStack(MekanismItems.Sawdust), 0.25);
				}
			}
			else {
				RecipeHandler.addPrecisionSawmillRecipe(StackUtils.size(ore, 1), new ItemStack(Items.stick, 6), new ItemStack(MekanismItems.Sawdust), 0.25);
			}
		}
		
		for(ItemStack ore : OreDictionary.getOres("oreNetherSteel"))
		{
			RecipeHandler.addEnrichmentChamberRecipe(StackUtils.size(ore, 1), new ItemStack(MekanismItems.OtherDust, 4, 1));
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
			RecipeHandler.addCrusherRecipe(MekanismUtils.size(ore, 1), new ItemStack(MekanismItems.OtherDust, 1, 6));
			RecipeHandler.addEnrichmentChamberRecipe(MekanismUtils.size(ore, 1), new ItemStack(MekanismItems.CompressedObsidian));
			
			InfuseRegistry.registerInfuseObject(MekanismUtils.size(ore, 1), new InfuseObject(InfuseRegistry.get("OBSIDIAN"), 10));
		}
		
		for(Resource resource : Resource.values())
		{
			for(ItemStack ore : OreDictionary.getOres("clump" + resource.getName()))
			{
				RecipeHandler.addCrusherRecipe(MekanismUtils.size(ore, 1), new ItemStack(MekanismItems.DirtyDust, 1, resource.ordinal()));
			}
			
			for(ItemStack ore : OreDictionary.getOres("shard" + resource.getName()))
			{
				RecipeHandler.addPurificationChamberRecipe(MekanismUtils.size(ore, 1), new ItemStack(MekanismItems.Clump, 1, resource.ordinal()));
			}
			
			for(ItemStack ore : OreDictionary.getOres("crystal" + resource.getName()))
			{
				RecipeHandler.addChemicalInjectionChamberRecipe(MekanismUtils.size(ore, 1), "hydrogenChloride", new ItemStack(MekanismItems.Shard, 1, resource.ordinal()));
			}
			
			for(ItemStack ore : OreDictionary.getOres("dustDirty" + resource.getName()))
			{
				RecipeHandler.addEnrichmentChamberRecipe(MekanismUtils.size(ore, 1), new ItemStack(MekanismItems.Dust, 1, resource.ordinal()));
			}
			
			for(ItemStack ore : OreDictionary.getOres("ore" + resource.getName()))
			{
				RecipeHandler.addEnrichmentChamberRecipe(MekanismUtils.size(ore, 1), new ItemStack(MekanismItems.Dust, 2, resource.ordinal()));
				RecipeHandler.addPurificationChamberRecipe(MekanismUtils.size(ore, 1), new ItemStack(MekanismItems.Clump, 3, resource.ordinal()));
				RecipeHandler.addChemicalInjectionChamberRecipe(MekanismUtils.size(ore, 1), "hydrogenChloride", new ItemStack(MekanismItems.Shard, 4, resource.ordinal()));
				RecipeHandler.addChemicalDissolutionChamberRecipe(MekanismUtils.size(ore, 1), new GasStack(GasRegistry.getGas(resource.getName().toLowerCase()), 1000));
			}
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
			RecipeHandler.addCrusherRecipe(MekanismUtils.size(ore, 1), new ItemStack(MekanismItems.Dust, 1, Resource.COPPER.ordinal()));
			RecipeHandler.addMetallurgicInfuserRecipe(InfuseRegistry.get("TIN"), 10, MekanismUtils.size(ore, 1), new ItemStack(MekanismItems.Ingot, 1, 2));
		}
	
		for(ItemStack ore : OreDictionary.getOres("ingotTin"))
		{
			RecipeHandler.addCrusherRecipe(MekanismUtils.size(ore, 1), new ItemStack(MekanismItems.Dust, 1, Resource.TIN.ordinal()));
		}
		
		for(ItemStack ore : OreDictionary.getOres("ingotLead"))
		{
			RecipeHandler.addCrusherRecipe(MekanismUtils.size(ore, 1), new ItemStack(MekanismItems.Dust, 1, Resource.LEAD.ordinal()));
		}
		
		for(ItemStack ore : OreDictionary.getOres("ingotSilver"))
		{
			RecipeHandler.addCrusherRecipe(MekanismUtils.size(ore, 1), new ItemStack(MekanismItems.Dust, 1, Resource.SILVER.ordinal()));
		}
		
		for(ItemStack ore : OreDictionary.getOres("ingotRefinedObsidian"))
		{
			RecipeHandler.addCrusherRecipe(MekanismUtils.size(ore, 1), new ItemStack(MekanismItems.OtherDust, 1, 6));
		}
		
		for(ItemStack ore : OreDictionary.getOres("ingotOsmium"))
		{
			RecipeHandler.addCrusherRecipe(MekanismUtils.size(ore, 1), new ItemStack(MekanismItems.Dust, 1, Resource.OSMIUM.ordinal()));
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
			FurnaceRecipes.smelting().func_151394_a(new ItemStack(MekanismItems.Dust, 1, Resource.SILVER.ordinal()), MekanismUtils.size(OreDictionary.getOres("ingotSilver").get(0), 1), 0.0F);
		} catch(Exception e) {}
		
		try {
			FurnaceRecipes.smelting().func_151394_a(new ItemStack(MekanismItems.Dust, 1, Resource.LEAD.ordinal()), MekanismUtils.size(OreDictionary.getOres("ingotLead").get(0), 1), 0.0F);
		} catch(Exception e) {}
		
		try {
			RecipeHandler.addCrusherRecipe(new ItemStack(Items.coal), MekanismUtils.size(OreDictionary.getOres("dustCoal").get(0), 1));
		} catch(Exception e) {}
		
		try {
			RecipeHandler.addCrusherRecipe(new ItemStack(Items.coal, 1, 1), MekanismUtils.size(OreDictionary.getOres("dustCharcoal").get(0), 1));
		} catch(Exception e) {}
		
		try {
			RecipeHandler.addCrusherRecipe(new ItemStack(Items.gunpowder), MekanismUtils.size(OreDictionary.getOres("dustSaltpeter").get(0), 1));
		} catch(Exception e) {}
		
		for(ItemStack ore : OreDictionary.getOres("dustSaltpeter"))
		{
			RecipeHandler.addEnrichmentChamberRecipe(MekanismUtils.size(ore, 1), new ItemStack(Items.gunpowder));
		}
		
		for(ItemStack ore : OreDictionary.getOres("dustIron"))
		{
			RecipeHandler.addCombinerRecipe(MekanismUtils.size(ore, 8), new ItemStack(Blocks.iron_ore));
		}
		
		for(ItemStack ore : OreDictionary.getOres("ingotSteel"))
		{
			RecipeHandler.addCrusherRecipe(MekanismUtils.size(ore, 1), new ItemStack(MekanismItems.OtherDust, 1, 1));
		}
		
		for(ItemStack ore : OreDictionary.getOres("dustGold"))
		{
			RecipeHandler.addCombinerRecipe(MekanismUtils.size(ore, 8), new ItemStack(Blocks.gold_ore));
		}
		
		for(ItemStack ore : OreDictionary.getOres("dustLapis"))
		{
			RecipeHandler.addCrusherRecipe(new ItemStack(Items.dye, 1, 4), MekanismUtils.size(ore, 1));
		}
		
		for(ItemStack ore : OreDictionary.getOres("dustLithium"))
		{
			RecipeHandler.addChemicalOxidizerRecipe(MekanismUtils.size(ore, 1), new GasStack(GasRegistry.getGas("lithium"), 100));
		}
		
		for(ItemStack ore : OreDictionary.getOres("dustObsidian"))
		{
			RecipeHandler.addCombinerRecipe(MekanismUtils.size(ore, 4), new ItemStack(Blocks.obsidian));
			RecipeHandler.addMetallurgicInfuserRecipe(InfuseRegistry.get("DIAMOND"), 10, MekanismUtils.size(ore, 1), new ItemStack(MekanismItems.OtherDust, 1, 5));
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
}
