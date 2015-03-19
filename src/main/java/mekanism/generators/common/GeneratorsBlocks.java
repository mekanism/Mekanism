package mekanism.generators.common;

import mekanism.generators.common.block.BlockGenerator;
import mekanism.generators.common.block.BlockReactor;
import mekanism.generators.common.item.ItemBlockGenerator;
import mekanism.generators.common.item.ItemBlockReactor;

import net.minecraft.block.Block;
import cpw.mods.fml.common.registry.GameRegistry;
import cpw.mods.fml.common.registry.GameRegistry.ObjectHolder;

@ObjectHolder("MekanismGenerators")
public class GeneratorsBlocks
{
	public static final Block Generator = new BlockGenerator().setBlockName("Generator");
	public static final Block Reactor = new BlockReactor().setBlockName("Reactor");
	public static final Block ReactorGlass = new BlockReactor().setBlockName("ReactorGlass");

	public static void register()
	{
		GameRegistry.registerBlock(Generator, ItemBlockGenerator.class, "Generator");
		GameRegistry.registerBlock(Reactor, ItemBlockReactor.class, "Reactor");
		GameRegistry.registerBlock(ReactorGlass, ItemBlockReactor.class, "ReactorGlass");
	}
}
