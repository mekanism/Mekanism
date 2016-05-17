package mekanism.generators.common;

import static mekanism.generators.common.block.states.BlockStateGenerator.GeneratorBlock.GENERATOR_BLOCK_1;
import static mekanism.generators.common.block.states.BlockStateReactor.ReactorBlock.REACTOR_BLOCK;
import static mekanism.generators.common.block.states.BlockStateReactor.ReactorBlock.REACTOR_GLASS;
import mekanism.generators.common.block.BlockGenerator;
import mekanism.generators.common.block.BlockReactor;
import mekanism.generators.common.item.ItemBlockGenerator;
import mekanism.generators.common.item.ItemBlockReactor;
import net.minecraft.block.Block;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.fml.common.registry.GameRegistry.ObjectHolder;

@ObjectHolder("MekanismGenerators")
public class GeneratorsBlocks
{
	public static final Block Generator = BlockGenerator.getGeneratorBlock(GENERATOR_BLOCK_1).setUnlocalizedName("Generator");
	public static final Block Reactor = BlockReactor.getReactorBlock(REACTOR_BLOCK).setUnlocalizedName("Reactor");
	public static final Block ReactorGlass = BlockReactor.getReactorBlock(REACTOR_GLASS).setUnlocalizedName("ReactorGlass");

	public static void register()
	{
		GameRegistry.registerBlock(Generator, ItemBlockGenerator.class, "Generator");
		GameRegistry.registerBlock(Reactor, ItemBlockReactor.class, "Reactor");
		GameRegistry.registerBlock(ReactorGlass, ItemBlockReactor.class, "ReactorGlass");
		
		MekanismGenerators.proxy.registerBlockRenders();
	}
}
