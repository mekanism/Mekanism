package mekanism.generators.common;

import static mekanism.common.MekanismBlocks.init;
import static mekanism.generators.common.block.states.BlockStateGenerator.GeneratorBlock.GENERATOR_BLOCK_1;
import static mekanism.generators.common.block.states.BlockStateReactor.ReactorBlock.REACTOR_BLOCK;
import static mekanism.generators.common.block.states.BlockStateReactor.ReactorBlock.REACTOR_GLASS;
import mekanism.common.MekanismItems;
import mekanism.generators.common.block.BlockGenerator;
import mekanism.generators.common.block.BlockReactor;
import mekanism.generators.common.block.states.BlockStateGenerator.GeneratorBlockStateMapper;
import mekanism.generators.common.block.states.BlockStateReactor.ReactorBlockStateMapper;
import mekanism.generators.common.item.ItemBlockGenerator;
import mekanism.generators.common.item.ItemBlockReactor;
import net.minecraft.block.Block;
import net.minecraft.client.renderer.block.statemap.IStateMapper;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.fml.common.registry.GameRegistry.ObjectHolder;

@ObjectHolder("MekanismGenerators")
public class GeneratorsBlocks
{
	public static final Block Generator = init(BlockGenerator.getGeneratorBlock(GENERATOR_BLOCK_1), "Generator");
	public static final Block Reactor = init(BlockReactor.getReactorBlock(REACTOR_BLOCK), "Reactor");
	public static final Block ReactorGlass = init(BlockReactor.getReactorBlock(REACTOR_GLASS), "ReactorGlass");

	private static final IStateMapper generatorMapper = new GeneratorBlockStateMapper();
	private static final IStateMapper reactorMapper = new ReactorBlockStateMapper();
	
	public static void register()
	{
		GameRegistry.register(Generator);
		GameRegistry.register(Reactor);
		GameRegistry.register(ReactorGlass);
		
		GameRegistry.register(MekanismItems.init(new ItemBlockGenerator(Generator), "Generator"));
		GameRegistry.register(MekanismItems.init(new ItemBlockReactor(Reactor), "Reactor"));
		GameRegistry.register(MekanismItems.init(new ItemBlockReactor(ReactorGlass), "ReactorGlass"));
		
		ModelLoader.setCustomStateMapper(Generator, generatorMapper);
		ModelLoader.setCustomStateMapper(Reactor, reactorMapper);
		ModelLoader.setCustomStateMapper(ReactorGlass, reactorMapper);
		
		MekanismGenerators.proxy.registerBlockRenders();
	}
}
