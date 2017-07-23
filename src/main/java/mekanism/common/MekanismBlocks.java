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
import mekanism.common.block.BlockGlowPanel;
import mekanism.common.block.BlockMachine;
import mekanism.common.block.BlockObsidianTNT;
import mekanism.common.block.BlockOre;
import mekanism.common.block.BlockPlastic;
import mekanism.common.block.BlockPlasticFence;
import mekanism.common.block.BlockSalt;
import mekanism.common.block.BlockTransmitter;
import mekanism.common.block.states.BlockStatePlastic.PlasticBlockType;
import mekanism.common.item.ItemBlockBasic;
import mekanism.common.item.ItemBlockCardboardBox;
import mekanism.common.item.ItemBlockEnergyCube;
import mekanism.common.item.ItemBlockGasTank;
import mekanism.common.item.ItemBlockGlowPanel;
import mekanism.common.item.ItemBlockMachine;
import mekanism.common.item.ItemBlockOre;
import mekanism.common.item.ItemBlockPlastic;
import mekanism.common.item.ItemBlockTransmitter;
import net.minecraft.block.Block;
import net.minecraft.item.ItemBlock;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.fml.common.registry.GameRegistry.ObjectHolder;

@ObjectHolder("mekanism")
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
	public static Block Transmitter = new BlockTransmitter();
	public static Block BoundingBlock = (BlockBounding)new BlockBounding();
	public static Block GasTank = new BlockGasTank();
	public static Block CardboardBox = new BlockCardboardBox();
	public static Block GlowPanel = new BlockGlowPanel();
	public static Block PlasticBlock = new BlockPlastic(PlasticBlockType.PLASTIC);
	public static Block SlickPlasticBlock = new BlockPlastic(PlasticBlockType.SLICK);
	public static Block GlowPlasticBlock = new BlockPlastic(PlasticBlockType.GLOW);
	public static Block ReinforcedPlasticBlock = new BlockPlastic(PlasticBlockType.REINFORCED);
	public static Block RoadPlasticBlock = new BlockPlastic(PlasticBlockType.ROAD);
	public static Block PlasticFence = new BlockPlasticFence();
	public static Block SaltBlock = new BlockSalt();

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
		GameRegistry.register(init(Transmitter, "Transmitter"));
		GameRegistry.register(init(ObsidianTNT, "ObsidianTNT"));
		GameRegistry.register(init(BoundingBlock, "BoundingBlock"));
		GameRegistry.register(init(GasTank, "GasTank"));
		GameRegistry.register(init(CardboardBox, "CardboardBox"));
		GameRegistry.register(init(GlowPanel, "GlowPanel"));
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
		GameRegistry.register(MekanismItems.init(new ItemBlockTransmitter(Transmitter), "Transmitter"));
		GameRegistry.register(MekanismItems.init(new ItemBlock(ObsidianTNT), "ObsidianTNT"));
		GameRegistry.register(MekanismItems.init(new ItemBlock(BoundingBlock), "BoundingBlock"));
		GameRegistry.register(MekanismItems.init(new ItemBlockGasTank(GasTank), "GasTank"));
		GameRegistry.register(MekanismItems.init(new ItemBlockCardboardBox(CardboardBox), "CardboardBox"));
		GameRegistry.register(MekanismItems.init(new ItemBlockGlowPanel(GlowPanel), "GlowPanel"));
		GameRegistry.register(MekanismItems.init(new ItemBlockPlastic(PlasticBlock), "PlasticBlock"));
		GameRegistry.register(MekanismItems.init(new ItemBlockPlastic(SlickPlasticBlock), "SlickPlasticBlock"));
		GameRegistry.register(MekanismItems.init(new ItemBlockPlastic(GlowPlasticBlock), "GlowPlasticBlock"));
		GameRegistry.register(MekanismItems.init(new ItemBlockPlastic(ReinforcedPlasticBlock), "ReinforcedPlasticBlock"));
		GameRegistry.register(MekanismItems.init(new ItemBlockPlastic(RoadPlasticBlock), "RoadPlasticBlock"));
		GameRegistry.register(MekanismItems.init(new ItemBlockPlastic(PlasticFence), "PlasticFence"));
		GameRegistry.register(MekanismItems.init(new ItemBlock(SaltBlock), "SaltBlock"));
		
		Mekanism.proxy.registerBlockRenders();
	}
	
	public static Block init(Block block, String name)
	{
		return block.setUnlocalizedName(name).setRegistryName("mekanism:" + name);
	}
}
