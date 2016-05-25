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
import net.minecraft.block.Block;
import net.minecraft.client.renderer.block.statemap.IStateMapper;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.fml.common.registry.GameRegistry.ObjectHolder;

@ObjectHolder("Mekanism")
public class MekanismBlocks
{
	public static final Block BasicBlock = BlockBasic.getBlockBasic(BASIC_BLOCK_1).setUnlocalizedName("BasicBlock");
	public static final Block BasicBlock2 = BlockBasic.getBlockBasic(BASIC_BLOCK_2).setUnlocalizedName("BasicBlock2");
	public static final Block MachineBlock = BlockMachine.getBlockMachine(MACHINE_BLOCK_1).setUnlocalizedName("MachineBlock");
	public static final Block MachineBlock2 = BlockMachine.getBlockMachine(MACHINE_BLOCK_2).setUnlocalizedName("MachineBlock2");
	public static final Block MachineBlock3 = BlockMachine.getBlockMachine(MACHINE_BLOCK_3).setUnlocalizedName("MachineBlock3");
	public static final Block OreBlock = new BlockOre().setUnlocalizedName("OreBlock");
	public static final Block ObsidianTNT = new BlockObsidianTNT().setUnlocalizedName("ObsidianTNT").setCreativeTab(Mekanism.tabMekanism);
	public static final Block EnergyCube = new BlockEnergyCube().setUnlocalizedName("EnergyCube");
	public static final Block BoundingBlock = (BlockBounding)new BlockBounding().setUnlocalizedName("BoundingBlock");
	public static final Block GasTank = new BlockGasTank().setUnlocalizedName("GasTank");
	public static final Block CardboardBox = new BlockCardboardBox().setUnlocalizedName("CardboardBox");
	public static final Block PlasticBlock = new BlockPlastic(PlasticBlockType.PLASTIC).setUnlocalizedName("PlasticBlock");
	public static final Block SlickPlasticBlock = new BlockPlastic(PlasticBlockType.SLICK).setUnlocalizedName("SlickPlasticBlock");
	public static final Block GlowPlasticBlock = new BlockPlastic(PlasticBlockType.GLOW).setUnlocalizedName("GlowPlasticBlock");
	public static final Block ReinforcedPlasticBlock = new BlockPlastic(PlasticBlockType.REINFORCED).setUnlocalizedName("ReinforcedPlasticBlock");
	public static final Block RoadPlasticBlock = new BlockPlastic(PlasticBlockType.ROAD).setUnlocalizedName("RoadPlasticBlock");
	public static final Block PlasticFence = new BlockPlasticFence().setUnlocalizedName("PlasticFence");
	public static final Block SaltBlock = new BlockSalt().setUnlocalizedName("SaltBlock");

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
		
		GameRegistry.register(new ItemBlockBasic(BasicBlock).setUnlocalizedName("BasicBlock"));
		GameRegistry.register(new ItemBlockBasic(BasicBlock2).setUnlocalizedName("BasicBlock2"));
		GameRegistry.register(new ItemBlockBasic(MachineBlock).setUnlocalizedName("MachineBlock"));
		GameRegistry.register(new ItemBlockBasic(MachineBlock2).setUnlocalizedName("MachineBlock2"));
		GameRegistry.register(new ItemBlockBasic(MachineBlock3).setUnlocalizedName("MachineBlock3"));
		GameRegistry.register(new ItemBlockBasic(OreBlock).setUnlocalizedName("OreBlock"));
		GameRegistry.register(new ItemBlockBasic(EnergyCube).setUnlocalizedName("EnergyCube"));
		GameRegistry.register(new ItemBlockBasic(GasTank).setUnlocalizedName("GasTank"));
		GameRegistry.register(new ItemBlockBasic(CardboardBox).setUnlocalizedName("CardboardBox"));
		GameRegistry.register(new ItemBlockBasic(PlasticBlock).setUnlocalizedName("PlasticBlock"));
		GameRegistry.register(new ItemBlockBasic(SlickPlasticBlock).setUnlocalizedName("SlickPlasticBlock"));
		GameRegistry.register(new ItemBlockBasic(GlowPlasticBlock).setUnlocalizedName("GlowPlasticBlock"));
		GameRegistry.register(new ItemBlockBasic(ReinforcedPlasticBlock).setUnlocalizedName("ReinforcedPlasticBlock"));
		GameRegistry.register(new ItemBlockBasic(RoadPlasticBlock).setUnlocalizedName("RoadPlasticBlock"));
		GameRegistry.register(new ItemBlockBasic(PlasticFence).setUnlocalizedName("PlasticFence"));

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
}
