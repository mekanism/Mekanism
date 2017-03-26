package mekanism.generators.client.jei;

import mekanism.client.jei.MekanismJEI;
import mekanism.generators.common.GeneratorsBlocks;
import mezz.jei.api.BlankModPlugin;
import mezz.jei.api.ISubtypeRegistry;
import mezz.jei.api.JEIPlugin;
import net.minecraft.item.Item;

@JEIPlugin
public class GeneratorsJEI extends BlankModPlugin
{
	@Override
	public void registerItemSubtypes(ISubtypeRegistry registry)
	{
		registry.registerSubtypeInterpreter(Item.getItemFromBlock(GeneratorsBlocks.Generator), MekanismJEI.NBT_INTERPRETER);
	}
}
