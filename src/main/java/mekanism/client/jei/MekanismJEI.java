package mekanism.client.jei;

import mekanism.common.MekanismBlocks;
import mekanism.common.MekanismItems;
import mezz.jei.api.IItemRegistry;
import mezz.jei.api.IJeiHelpers;
import mezz.jei.api.IJeiRuntime;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.IModRegistry;
import mezz.jei.api.IRecipeRegistry;
import mezz.jei.api.JEIPlugin;
import net.minecraft.item.ItemStack;

@JEIPlugin
public class MekanismJEI implements IModPlugin
{
	@Override
	public void register(IModRegistry registry)
	{
		registry.addAdvancedGuiHandlers(new GuiElementHandler());
		
		registry.getJeiHelpers().getItemBlacklist().addItemToBlacklist(new ItemStack(MekanismItems.ItemProxy));
		registry.getJeiHelpers().getItemBlacklist().addItemToBlacklist(new ItemStack(MekanismBlocks.BoundingBlock));
	}
	
	@Override
	public void onItemRegistryAvailable(IItemRegistry registry) 
	{
		
	}

	@Override
	public void onJeiHelpersAvailable(IJeiHelpers helpers) 
	{

	}

	@Override
	public void onRecipeRegistryAvailable(IRecipeRegistry registry) 
	{
		
	}

	@Override
	public void onRuntimeAvailable(IJeiRuntime runtime)
	{
		
	}
}
