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

import net.minecraft.block.Block;
import cpw.mods.fml.common.registry.GameRegistry.ObjectHolder;

@ObjectHolder("Mekanism")
public class MekanismBlocks
{
	public static final Block BasicBlock = new BlockBasic().setBlockName("BasicBlock");
	public static final Block BasicBlock2 = new BlockBasic().setBlockName("BasicBlock2");
	public static final Block MachineBlock = new BlockMachine().setBlockName("MachineBlock");
	public static final Block MachineBlock2 = new BlockMachine().setBlockName("MachineBlock2");
	public static final Block OreBlock = new BlockOre().setBlockName("OreBlock");
	public static final Block ObsidianTNT = new BlockObsidianTNT().setBlockName("ObsidianTNT").setCreativeTab(Mekanism.tabMekanism);
	public static final Block EnergyCube = new BlockEnergyCube().setBlockName("EnergyCube");
	public static final Block BoundingBlock = (BlockBounding)new BlockBounding().setBlockName("BoundingBlock");
	public static final Block GasTank = new BlockGasTank().setBlockName("GasTank");
	public static final Block CardboardBox = new BlockCardboardBox().setBlockName("CardboardBox");
	public static final Block BlockHDPE = new BlockPlastic().setBlockName("PlasticBlock");
	public static final Block BlockSlickHDPE = new BlockPlastic().setBlockName("SlickPlasticBlock");
	public static final Block BlockGlowHDPE = new BlockPlastic().setBlockName("GlowPlasticBlock");
	public static final Block BlockReinforcedHDPE = new BlockPlastic().setBlockName("ReinforcedPlasticBlock");
	public static final Block BlockRoadHDPE = new BlockPlastic().setBlockName("RoadPlasticBlock");
	public static final Block BlockHDPEFence = new BlockPlasticFence().setBlockName("PlasticFence");
	public static final Block SaltBlock = new BlockSalt().setBlockName("SaltBlock");
}
