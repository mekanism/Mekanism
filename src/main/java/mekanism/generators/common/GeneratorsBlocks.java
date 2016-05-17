package mekanism.generators.common;

import static mekanism.generators.common.block.states.BlockStateGenerator.GeneratorBlock.GENERATOR_BLOCK_1;
import static mekanism.generators.common.block.states.BlockStateReactor.ReactorBlock.REACTOR_BLOCK;
import static mekanism.generators.common.block.states.BlockStateReactor.ReactorBlock.REACTOR_GLASS;
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
	public static final Block Generator = BlockGenerator.getGeneratorBlock(GENERATOR_BLOCK_1).setUnlocalizedName("Generator");
	public static final Block Reactor = BlockReactor.getReactorBlock(REACTOR_BLOCK).setUnlocalizedName("Reactor");
	public static final Block ReactorGlass = BlockReactor.getReactorBlock(REACTOR_GLASS).setUnlocalizedName("ReactorGlass");

	private static final IStateMapper generatorMapper = new GeneratorBlockStateMapper();
	private static final IStateMapper reactorMapper = new ReactorBlockStateMapper();
	
	public static void register()
	{
		GameRegistry.registerBlock(Generator, ItemBlockGenerator.class, "Generator");
		GameRegistry.registerBlock(Reactor, ItemBlockReactor.class, "Reactor");
		GameRegistry.registerBlock(ReactorGlass, ItemBlockReactor.class, "ReactorGlass");
		
		ModelLoader.setCustomStateMapper(Generator, generatorMapper);
		ModelLoader.setCustomStateMapper(Reactor, reactorMapper);
		ModelLoader.setCustomStateMapper(ReactorGlass, reactorMapper);
		
		MekanismGenerators.proxy.registerBlockRenders();
	}
}
