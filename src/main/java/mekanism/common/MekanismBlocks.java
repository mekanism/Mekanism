package mekanism.common;

import static mekanism.common.block.states.BlockStateBasic.BasicBlock.BASIC_BLOCK_1;
import static mekanism.common.block.states.BlockStateBasic.BasicBlock.BASIC_BLOCK_2;
import static mekanism.common.block.states.BlockStateMachine.MachineBlock.MACHINE_BLOCK_1;
import static mekanism.common.block.states.BlockStateMachine.MachineBlock.MACHINE_BLOCK_2;
import static mekanism.common.block.states.BlockStateMachine.MachineBlock.MACHINE_BLOCK_3;
import mekanism.common.block.BlockBasic;
import mekanism.common.block.BlockBounding;
import mekanism.common.block.BlockCardboardBox;
import mekanism.common.block.BlockEnergyCube;
import mekanism.common.block.BlockGasTank;
import mekanism.common.block.BlockMachine;
import mekanism.common.block.BlockObsidianTNT;
import mekanism.common.block.BlockOre;
import mekanism.common.block.BlockPlastic;
import mekanism.common.block.BlockPlasticFence;
import mekanism.common.block.BlockPlasticFence.PlasticFenceStateMapper;
import mekanism.common.block.BlockSalt;
import mekanism.common.block.states.BlockStateBasic.BasicBlockStateMapper;
import mekanism.common.block.states.BlockStateCardboardBox.CardboardBoxStateMapper;
import mekanism.common.block.states.BlockStateMachine.MachineBlockStateMapper;
import mekanism.common.block.states.BlockStatePlastic.PlasticBlockStateMapper;
import mekanism.common.block.states.BlockStatePlastic.PlasticBlockType;
import mekanism.common.item.ItemBlockBasic;
import mekanism.common.item.ItemBlockCardboardBox;
import mekanism.common.item.ItemBlockEnergyCube;
import mekanism.common.item.ItemBlockGasTank;
import mekanism.common.item.ItemBlockMachine;
import mekanism.common.item.ItemBlockOre;
import mekanism.common.item.ItemBlockPlastic;
import net.minecraft.block.Block;
import net.minecraft.client.renderer.block.statemap.IStateMapper;
import net.minecraft.item.ItemBlock;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.fml.common.registry.GameRegistry.ObjectHolder;

@ObjectHolder("Mekanism")
public class MekanismBlocks
{
	public static final Block BasicBlock = init(BlockBasic.getBlockBasic(BASIC_BLOCK_1), "BasicBlock");
	public static final Block BasicBlock2 = init(BlockBasic.getBlockBasic(BASIC_BLOCK_2), "BasicBlock2");
	public static final Block MachineBlock = init(BlockMachine.getBlockMachine(MACHINE_BLOCK_1), "MachineBlock");
	public static final Block MachineBlock2 = init(BlockMachine.getBlockMachine(MACHINE_BLOCK_2), "MachineBlock2");
	public static final Block MachineBlock3 = init(BlockMachine.getBlockMachine(MACHINE_BLOCK_3), "MachineBlock3");
	public static final Block OreBlock = init(new BlockOre(), "OreBlock");
	public static final Block ObsidianTNT = init(new BlockObsidianTNT(), "ObsidianTNT").setCreativeTab(Mekanism.tabMekanism);
	public static final Block EnergyCube = init(new BlockEnergyCube(), "EnergyCube");
	public static final Block BoundingBlock = (BlockBounding)init(new BlockBounding(), "BoundingBlock");
	public static final Block GasTank = init(new BlockGasTank(), "GasTank");
	public static final Block CardboardBox = init(new BlockCardboardBox(), "CardboardBox");
	public static final Block PlasticBlock = init(new BlockPlastic(PlasticBlockType.PLASTIC), "PlasticBlock");
	public static final Block SlickPlasticBlock = init(new BlockPlastic(PlasticBlockType.SLICK), "SlickPlasticBlock");
	public static final Block GlowPlasticBlock = init(new BlockPlastic(PlasticBlockType.GLOW), "GlowPlasticBlock");
	public static final Block ReinforcedPlasticBlock = init(new BlockPlastic(PlasticBlockType.REINFORCED), "ReinforcedPlasticBlock");
	public static final Block RoadPlasticBlock = init(new BlockPlastic(PlasticBlockType.ROAD), "RoadPlasticBlock");
	public static final Block PlasticFence = init(new BlockPlasticFence(), "PlasticFence");
	public static final Block SaltBlock = init(new BlockSalt(), "SaltBlock");

	private static final IStateMapper machineMapper = new MachineBlockStateMapper();
	private static final IStateMapper basicMapper = new BasicBlockStateMapper();
	private static final IStateMapper plasticMapper = new PlasticBlockStateMapper();
	private static final IStateMapper fenceMapper = new PlasticFenceStateMapper();
	private static final IStateMapper boxMapper = new CardboardBoxStateMapper();

	/**
	 * Adds and registers all blocks.
	 */
	public static void register()
	{
		GameRegistry.register(BasicBlock);
		GameRegistry.register(BasicBlock2);
		GameRegistry.register(MachineBlock);
		GameRegistry.register(MachineBlock2);
		GameRegistry.register(MachineBlock3);
		GameRegistry.register(OreBlock);
		GameRegistry.register(EnergyCube);
		GameRegistry.register(ObsidianTNT);
		GameRegistry.register(BoundingBlock);
		GameRegistry.register(GasTank);
		GameRegistry.register(CardboardBox);
		GameRegistry.register(PlasticBlock);
		GameRegistry.register(SlickPlasticBlock);
		GameRegistry.register(GlowPlasticBlock);
		GameRegistry.register(ReinforcedPlasticBlock);
		GameRegistry.register(RoadPlasticBlock);
		GameRegistry.register(PlasticFence);
		GameRegistry.register(SaltBlock);
		
		GameRegistry.register(MekanismItems.init(new ItemBlockBasic(BasicBlock), "BasicBlock"));
		GameRegistry.register(MekanismItems.init(new ItemBlockBasic(BasicBlock2), "BasicBlock2"));
		GameRegistry.register(MekanismItems.init(new ItemBlockMachine(MachineBlock), "MachineBlock"));
		GameRegistry.register(MekanismItems.init(new ItemBlockMachine(MachineBlock2), "MachineBlock2"));
		GameRegistry.register(MekanismItems.init(new ItemBlockMachine(MachineBlock3), "MachineBlock3"));
		GameRegistry.register(MekanismItems.init(new ItemBlockOre(OreBlock), "OreBlock"));
		GameRegistry.register(MekanismItems.init(new ItemBlockEnergyCube(EnergyCube), "EnergyCube"));
		GameRegistry.register(MekanismItems.init(new ItemBlock(ObsidianTNT), "ObsidianTNT"));
		GameRegistry.register(MekanismItems.init(new ItemBlock(BoundingBlock), "BoundingBlock"));
		GameRegistry.register(MekanismItems.init(new ItemBlockGasTank(GasTank), "GasTank"));
		GameRegistry.register(MekanismItems.init(new ItemBlockCardboardBox(CardboardBox), "CardboardBox"));
		GameRegistry.register(MekanismItems.init(new ItemBlockPlastic(PlasticBlock), "PlasticBlock"));
		GameRegistry.register(MekanismItems.init(new ItemBlockPlastic(SlickPlasticBlock), "SlickPlasticBlock"));
		GameRegistry.register(MekanismItems.init(new ItemBlockPlastic(GlowPlasticBlock), "GlowPlasticBlock"));
		GameRegistry.register(MekanismItems.init(new ItemBlockPlastic(ReinforcedPlasticBlock), "ReinforcedPlasticBlock"));
		GameRegistry.register(MekanismItems.init(new ItemBlockPlastic(RoadPlasticBlock), "RoadPlasticBlock"));
		GameRegistry.register(MekanismItems.init(new ItemBlockPlastic(PlasticFence), "PlasticFence"));
		GameRegistry.register(MekanismItems.init(new ItemBlock(SaltBlock), "SaltBlock"));

		ModelLoader.setCustomStateMapper(MachineBlock, machineMapper);
		ModelLoader.setCustomStateMapper(MachineBlock2, machineMapper);
		ModelLoader.setCustomStateMapper(MachineBlock3, machineMapper);
		ModelLoader.setCustomStateMapper(BasicBlock, basicMapper);
		ModelLoader.setCustomStateMapper(BasicBlock2, basicMapper);
		ModelLoader.setCustomStateMapper(PlasticBlock, plasticMapper);
		ModelLoader.setCustomStateMapper(SlickPlasticBlock, plasticMapper);
		ModelLoader.setCustomStateMapper(GlowPlasticBlock, plasticMapper);
		ModelLoader.setCustomStateMapper(ReinforcedPlasticBlock, plasticMapper);
		ModelLoader.setCustomStateMapper(RoadPlasticBlock, plasticMapper);
		ModelLoader.setCustomStateMapper(PlasticFence, fenceMapper);
		ModelLoader.setCustomStateMapper(CardboardBox, boxMapper);
		
		Mekanism.proxy.registerBlockRenders();
	}
	
	public static Block init(Block block, String name)
	{
		return block.setUnlocalizedName(name).setRegistryName("mekanism:" + name);
	}
}
