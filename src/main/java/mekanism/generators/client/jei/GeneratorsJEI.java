package mekanism.generators.client.jei;

import mekanism.client.jei.MekanismJEI;
import mekanism.generators.common.GeneratorsBlocks;
import mezz.jei.api.IJeiRuntime;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.IModRegistry;
import mezz.jei.api.JEIPlugin;
import net.minecraft.item.Item;

@JEIPlugin
public class GeneratorsJEI implements IModPlugin
{
	@Override
	public void register(IModRegistry registry)
	{
		registry.getJeiHelpers().getNbtIgnoreList().ignoreNbtTagNames(Item.getItemFromBlock(GeneratorsBlocks.Generator), MekanismJEI.UNUSED_TAGS);
	}
	
	@Override
	public void onRuntimeAvailable(IJeiRuntime runtime) {}
}
