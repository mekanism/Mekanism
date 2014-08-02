package mekanism.generators.common;

import mekanism.generators.common.block.BlockGenerator;
import mekanism.generators.common.block.BlockReactor;

import net.minecraft.block.Block;
import cpw.mods.fml.common.registry.GameRegistry.ObjectHolder;

@ObjectHolder("MekanismGenerators")
public class GeneratorsBlocks
{
	//Blocks
	public static final Block Generator = new BlockGenerator().setBlockName("Generator");
	public static final Block Reactor = new BlockReactor().setBlockName("Reactor");
	public static final Block ReactorGlass = new BlockReactor().setBlockName("ReactorGlass");
}
