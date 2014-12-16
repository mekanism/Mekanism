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
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.fml.common.registry.GameRegistry.ObjectHolder;

@ObjectHolder("Mekanism")
public class MekanismBlocks
{
	public static final Block BasicBlock = new BlockBasic().setUnlocalizedName("BasicBlock");
	public static final Block MachineBlock = new BlockMachine().setUnlocalizedName("MachineBlock");
	public static final Block OreBlock = new BlockOre().setUnlocalizedName("OreBlock");
	public static final Block ObsidianTNT = new BlockObsidianTNT().setUnlocalizedName("ObsidianTNT").setCreativeTab(Mekanism.tabMekanism);
	public static final Block EnergyCube = new BlockEnergyCube().setUnlocalizedName("EnergyCube");
	public static final Block BoundingBlock = (BlockBounding)new BlockBounding().setUnlocalizedName("BoundingBlock");
	public static final Block GasTank = new BlockGasTank().setUnlocalizedName("GasTank");
	public static final Block CardboardBox = new BlockCardboardBox().setUnlocalizedName("CardboardBox");
	public static final Block PlasticBlock = new BlockPlastic().setUnlocalizedName("PlasticBlock");
	public static final Block SlickPlasticBlock = new BlockPlastic().setUnlocalizedName("SlickPlasticBlock");
	public static final Block GlowPlasticBlock = new BlockPlastic().setUnlocalizedName("GlowPlasticBlock");
	public static final Block ReinforcedPlasticBlock = new BlockPlastic().setUnlocalizedName("ReinforcedPlasticBlock");
	public static final Block RoadPlasticBlock = new BlockPlastic().setUnlocalizedName("RoadPlasticBlock");
	public static final Block PlasticFence = new BlockPlasticFence().setUnlocalizedName("PlasticFence");
	public static final Block SaltBlock = new BlockSalt().setUnlocalizedName("SaltBlock");

	/**
	 * Adds and registers all blocks.
	 */
	public static void register()
	{
		GameRegistry.registerBlock(BasicBlock, ItemBlockBasic.class, "BasicBlock");
		GameRegistry.registerBlock(MachineBlock, ItemBlockMachine.class, "MachineBlock");
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
