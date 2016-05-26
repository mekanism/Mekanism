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
	public static Block BasicBlock = BlockBasic.getBlockBasic(BASIC_BLOCK_1);
	public static Block BasicBlock2 = BlockBasic.getBlockBasic(BASIC_BLOCK_2);
	public static Block MachineBlock = BlockMachine.getBlockMachine(MACHINE_BLOCK_1);
	public static Block MachineBlock2 = BlockMachine.getBlockMachine(MACHINE_BLOCK_2);
	public static Block MachineBlock3 = BlockMachine.getBlockMachine(MACHINE_BLOCK_3);
	public static Block OreBlock = new BlockOre();
	public static Block ObsidianTNT = new BlockObsidianTNT().setCreativeTab(Mekanism.tabMekanism);
	public static Block EnergyCube = new BlockEnergyCube();
	public static Block BoundingBlock = (BlockBounding)new BlockBounding();
	public static Block GasTank = new BlockGasTank();
	public static Block CardboardBox = new BlockCardboardBox();
	public static Block PlasticBlock = new BlockPlastic(PlasticBlockType.PLASTIC);
	public static Block SlickPlasticBlock = new BlockPlastic(PlasticBlockType.SLICK);
	public static Block GlowPlasticBlock = new BlockPlastic(PlasticBlockType.GLOW);
	public static Block ReinforcedPlasticBlock = new BlockPlastic(PlasticBlockType.REINFORCED);
	public static Block RoadPlasticBlock = new BlockPlastic(PlasticBlockType.ROAD);
	public static Block PlasticFence = new BlockPlasticFence();
	public static Block SaltBlock = new BlockSalt();

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
		GameRegistry.register(init(BasicBlock, "BasicBlock"));
		GameRegistry.register(init(BasicBlock2, "BasicBlock2"));
		GameRegistry.register(init(MachineBlock, "MachineBlock"));
		GameRegistry.register(init(MachineBlock2, "MachineBlock2"));
		GameRegistry.register(init(MachineBlock3, "MachineBlock3"));
		GameRegistry.register(init(OreBlock, "OreBlock"));
		GameRegistry.register(init(EnergyCube, "EnergyCube"));
		GameRegistry.register(init(ObsidianTNT, "ObsidianTNT"));
		GameRegistry.register(init(BoundingBlock, "BoundingBlock"));
		GameRegistry.register(init(GasTank, "GasTank"));
		GameRegistry.register(init(CardboardBox, "CardboardBox"));
		GameRegistry.register(init(PlasticBlock, "PlasticBlock"));
		GameRegistry.register(init(SlickPlasticBlock, "SlickPlasticBlock"));
		GameRegistry.register(init(GlowPlasticBlock, "GlowPlasticBlock"));
		GameRegistry.register(init(ReinforcedPlasticBlock, "ReinforcedPlasticBlock"));
		GameRegistry.register(init(RoadPlasticBlock, "RoadPlasticBlock"));
		GameRegistry.register(init(PlasticFence, "PlasticFence"));
		GameRegistry.register(init(SaltBlock, "SaltBlock"));
		
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
