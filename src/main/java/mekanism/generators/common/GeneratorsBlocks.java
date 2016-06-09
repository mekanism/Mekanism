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
	public static final Block Generator = BlockGenerator.getGeneratorBlock(GENERATOR_BLOCK_1);
	public static final Block Reactor = BlockReactor.getReactorBlock(REACTOR_BLOCK);
	public static final Block ReactorGlass = BlockReactor.getReactorBlock(REACTOR_GLASS);
	
	public static void register()
	{
		GameRegistry.register(init(Generator, "Generator"));
		GameRegistry.register(init(Reactor, "Reactor"));
		GameRegistry.register(init(ReactorGlass, "ReactorGlass"));
		
		GameRegistry.register(GeneratorsItems.init(new ItemBlockGenerator(Generator), "Generator"));
		GameRegistry.register(GeneratorsItems.init(new ItemBlockReactor(Reactor), "Reactor"));
		GameRegistry.register(GeneratorsItems.init(new ItemBlockReactor(ReactorGlass), "ReactorGlass"));
		
		MekanismGenerators.proxy.registerBlockRenders();
	}
	
	public static Block init(Block block, String name)
	{
		return block.setUnlocalizedName(name).setRegistryName("mekanismgenerators:" + name);
	}
}
