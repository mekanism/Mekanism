package mekanism.client.jei;

import mekanism.common.MekanismBlocks;
import mekanism.common.MekanismItems;
import mekanism.common.util.ItemDataUtils;
import mezz.jei.api.IItemRegistry;
import mezz.jei.api.IJeiHelpers;
import mezz.jei.api.IJeiRuntime;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.IModRegistry;
import mezz.jei.api.IRecipeRegistry;
import mezz.jei.api.JEIPlugin;
import mezz.jei.api.recipe.IRecipeHandler;
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
		
		registry.addRecipeHandlers(new IRecipeHandler[] {new ShapedMekanismRecipeHandler()});
		registry.addRecipeHandlers(new IRecipeHandler[] {new ShapelessMekanismRecipeHandler()});
		
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
