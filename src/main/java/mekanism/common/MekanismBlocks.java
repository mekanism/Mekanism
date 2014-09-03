package mekanism.common;

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
import mekanism.common.block.BlockSalt;
import mekanism.common.item.ItemBlockBasic;
import mekanism.common.item.ItemBlockCardboardBox;
import mekanism.common.item.ItemBlockEnergyCube;
import mekanism.common.item.ItemBlockGasTank;
import mekanism.common.item.ItemBlockMachine;
import mekanism.common.item.ItemBlockOre;
import mekanism.common.item.ItemBlockPlastic;

import net.minecraft.block.Block;
import cpw.mods.fml.common.registry.GameRegistry;
import cpw.mods.fml.common.registry.GameRegistry.ObjectHolder;

import static mekanism.common.block.BlockBasic.BasicBlock.BASIC_BLOCK_1;
import static mekanism.common.block.BlockBasic.BasicBlock.BASIC_BLOCK_2;
import static mekanism.common.block.BlockMachine.MachineBlock.MACHINE_BLOCK_1;
import static mekanism.common.block.BlockMachine.MachineBlock.MACHINE_BLOCK_2;
import static mekanism.common.block.BlockMachine.MachineBlock.MACHINE_BLOCK_3;

@ObjectHolder("Mekanism")
public class MekanismBlocks
{
	public static final Block BasicBlock = new BlockBasic(BASIC_BLOCK_1).setBlockName("BasicBlock");
	public static final Block BasicBlock2 = new BlockBasic(BASIC_BLOCK_2).setBlockName("BasicBlock2");
	public static final Block MachineBlock = new BlockMachine(MACHINE_BLOCK_1).setBlockName("MachineBlock");
	public static final Block MachineBlock2 = new BlockMachine(MACHINE_BLOCK_2).setBlockName("MachineBlock2");
	public static final Block MachineBlock3 = new BlockMachine(MACHINE_BLOCK_3).setBlockName("MachineBlock3");
	public static final Block OreBlock = new BlockOre().setBlockName("OreBlock");
	public static final Block ObsidianTNT = new BlockObsidianTNT().setBlockName("ObsidianTNT").setCreativeTab(Mekanism.tabMekanism);
	public static final Block EnergyCube = new BlockEnergyCube().setBlockName("EnergyCube");
	public static final Block BoundingBlock = (BlockBounding)new BlockBounding().setBlockName("BoundingBlock");
	public static final Block GasTank = new BlockGasTank().setBlockName("GasTank");
	public static final Block CardboardBox = new BlockCardboardBox().setBlockName("CardboardBox");
	public static final Block PlasticBlock = new BlockPlastic().setBlockName("PlasticBlock");
	public static final Block SlickPlasticBlock = new BlockPlastic().setBlockName("SlickPlasticBlock");
	public static final Block GlowPlasticBlock = new BlockPlastic().setBlockName("GlowPlasticBlock");
	public static final Block ReinforcedPlasticBlock = new BlockPlastic().setBlockName("ReinforcedPlasticBlock");
	public static final Block RoadPlasticBlock = new BlockPlastic().setBlockName("RoadPlasticBlock");
	public static final Block PlasticFence = new BlockPlasticFence().setBlockName("PlasticFence");
	public static final Block SaltBlock = new BlockSalt().setBlockName("SaltBlock");

	/**
	 * Adds and registers all blocks.
	 */
	public static void register()
	{
		GameRegistry.registerBlock(BasicBlock, ItemBlockBasic.class, "BasicBlock");
		GameRegistry.registerBlock(BasicBlock2, ItemBlockBasic.class, "BasicBlock2");
		GameRegistry.registerBlock(MachineBlock, ItemBlockMachine.class, "MachineBlock");
		GameRegistry.registerBlock(MachineBlock2, ItemBlockMachine.class, "MachineBlock2");
		GameRegistry.registerBlock(MachineBlock3, ItemBlockMachine.class, "MachineBlock3");
		GameRegistry.registerBlock(OreBlock, ItemBlockOre.class, "OreBlock");
		GameRegistry.registerBlock(EnergyCube, ItemBlockEnergyCube.class, "EnergyCube");
		GameRegistry.registerBlock(ObsidianTNT, "ObsidianTNT");
		GameRegistry.registerBlock(BoundingBlock, "BoundingBlock");
		GameRegistry.registerBlock(GasTank, ItemBlockGasTank.class, "GasTank");
		GameRegistry.registerBlock(CardboardBox, ItemBlockCardboardBox.class, "CardboardBox");
		GameRegistry.registerBlock(PlasticBlock, ItemBlockPlastic.class, "PlasticBlock");
		GameRegistry.registerBlock(SlickPlasticBlock, ItemBlockPlastic.class, "SlickPlasticBlock");
		GameRegistry.registerBlock(GlowPlasticBlock, ItemBlockPlastic.class, "GlowPlasticBlock");
		GameRegistry.registerBlock(ReinforcedPlasticBlock, ItemBlockPlastic.class, "ReinforcedPlasticBlock");
		GameRegistry.registerBlock(RoadPlasticBlock, ItemBlockPlastic.class, "RoadPlasticBlock");
		GameRegistry.registerBlock(PlasticFence, ItemBlockPlastic.class, "PlasticFence");
		GameRegistry.registerBlock(SaltBlock, "SaltBlock");
	}
}
