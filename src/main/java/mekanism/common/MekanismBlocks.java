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
import mekanism.common.block.states.BlockStateBasic;
import mekanism.common.block.states.BlockStateBasic.BasicBlock;
import mekanism.common.block.states.BlockStateBasic.BasicBlockStateMapper;
import mekanism.common.block.states.BlockStateBasic.BasicBlockType;
import mekanism.common.block.states.BlockStateMachine;
import mekanism.common.block.states.BlockStateMachine.MachineBlock;
import mekanism.common.block.states.BlockStateMachine.MachineBlockStateMapper;
import mekanism.common.block.states.BlockStateMachine.MachineBlockType;
import mekanism.common.block.states.BlockStatePlastic.PlasticBlockType;
import mekanism.common.item.ItemBlockBasic;
import mekanism.common.item.ItemBlockCardboardBox;
import mekanism.common.item.ItemBlockEnergyCube;
import mekanism.common.item.ItemBlockGasTank;
import mekanism.common.item.ItemBlockMachine;
import mekanism.common.item.ItemBlockOre;
import mekanism.common.item.ItemBlockPlastic;

import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.ItemModelMesher;
import net.minecraft.client.renderer.entity.RenderItem;
import net.minecraft.client.resources.model.ModelBakery;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.item.Item;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.fml.common.registry.GameRegistry.ObjectHolder;

import com.google.common.collect.Lists;

@ObjectHolder("Mekanism")
public class MekanismBlocks
{
	public static final Block BasicBlock = getBlockBasic(BlockStateBasic.BasicBlock.BASIC_BLOCK_1).setUnlocalizedName("BasicBlock");
	public static final Block BasicBlock2 = getBlockBasic(BlockStateBasic.BasicBlock.BASIC_BLOCK_2).setUnlocalizedName("BasicBlock2");
	public static final Block MachineBlock = getBlockMachine(BlockStateMachine.MachineBlock.MACHINE_BLOCK_1).setUnlocalizedName("MachineBlock");
	public static final Block MachineBlock2 = getBlockMachine(BlockStateMachine.MachineBlock.MACHINE_BLOCK_2).setUnlocalizedName("MachineBlock2");
	public static final Block MachineBlock3 = getBlockMachine(BlockStateMachine.MachineBlock.MACHINE_BLOCK_3).setUnlocalizedName("MachineBlock3");
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

	public static void registerRender()
	{
		ItemModelMesher mesher = Minecraft.getMinecraft().getRenderItem().getItemModelMesher();
		for(MachineBlockType type : MachineBlockType.values())
		{
			mesher.register(Item.getItemFromBlock(type.machineBlock.implBlock), type.meta, new ModelResourceLocation("mekanism:" + type.getName(), "inventory"));
		}
		for(BasicBlockType type : BasicBlockType.values())
		{
			mesher.register(Item.getItemFromBlock(type.blockType.implBlock), type.meta, new ModelResourceLocation("mekanism:" + type.getName(), "inventory"));
		}

		ModelBakery.addVariantName(Item.getItemFromBlock(MachineBlock),
				"mekanism:enrichment_chamber",
				"mekanism:osmium_compressor",
				"mekanism:combiner",
				"mekanism:crusher",
				"mekanism:digital_miner",
				"mekanism:basic_factory",
				"mekanism:advanced_factory",
				"mekanism:elite_factory",
				"mekanism:metallurgic_infuser",
				"mekanism:purification_chamber",
				"mekanism:energized_smelter",
				"mekanism:teleporter",
				"mekanism:electric_pump",
				"mekanism:electric_chest",
				"mekanism:chargepad",
				"mekanism:logistical_sorter"
		);

		ModelBakery.addVariantName(Item.getItemFromBlock(MachineBlock2),
				"mekanism:rotary_condensentrator",
				"mekanism:chemical_oxidiser",
				"mekanism:chemical_infuser",
				"mekanism:chemical_injection_chamber",
				"mekanism:electrolytic_separator",
				"mekanism:precision_sawmill",
				"mekanism:chemical_dissolution_chamber",
				"mekanism:chemical_washer",
				"mekanism:chemical_crystallizer",
				"mekanism:seismic_vibrator",
				"mekanism:pressurized_reaction_chamber",
				"mekanism:portable_tank",
				"mekanism:fluidic_plenisher",
				"mekanism:laser",
				"mekanism:laser_amplifier",
				"mekanism:laser_tractor_bean"
		);

		ModelBakery.addVariantName(Item.getItemFromBlock(MachineBlock3),
				"mekanism:ambient_accumulator",
				"mekanism:entangled_block",
				"mekanism:gas_centrifuge"
		);

		ModelBakery.addVariantName(Item.getItemFromBlock(BasicBlock),
				"mekanism:osmium_block",
				"mekanism:bronze_block",
				"mekanism:refined_obsidian",
				"mekanism:coal_block",
				"mekanism:refined_glowstone",
				"mekanism:steel_block",
				"mekanism:bin",
				"mekanism:teleporter_frame",
				"mekanism:steel_casing",
				"mekanism:dynamic_tank",
				"mekanism:dynamic_glass",
				"mekanism:dynamic_valve",
				"mekanism:copper_block",
				"mekanism:tin_block",
				"mekanism:salination_controller",
				"mekanism:salination_valve"
		);

		ModelBakery.addVariantName(Item.getItemFromBlock(BasicBlock2),
				"mekanism:salination_block"
		);

		Minecraft.getMinecraft().getBlockRendererDispatcher().getBlockModelShapes().registerBlockWithStateMapper(MachineBlock, new MachineBlockStateMapper());
		Minecraft.getMinecraft().getBlockRendererDispatcher().getBlockModelShapes().registerBlockWithStateMapper(MachineBlock2, new MachineBlockStateMapper());
		Minecraft.getMinecraft().getBlockRendererDispatcher().getBlockModelShapes().registerBlockWithStateMapper(MachineBlock3, new MachineBlockStateMapper());
		Minecraft.getMinecraft().getBlockRendererDispatcher().getBlockModelShapes().registerBlockWithStateMapper(BasicBlock, new BasicBlockStateMapper());
		Minecraft.getMinecraft().getBlockRendererDispatcher().getBlockModelShapes().registerBlockWithStateMapper(BasicBlock2, new BasicBlockStateMapper());
	}

	public static BlockBasic getBlockBasic(final BasicBlock basicBlock)
	{
		return new BlockBasic()
		{
			@Override
			public BasicBlock getBasicBlock()
			{
				basicBlock.setImplBlock(this);
				basicBlock.setProperty();
				return basicBlock;
			}
		};
	}

	public static BlockMachine getBlockMachine(final MachineBlock machineBlock)
	{
		return new BlockMachine()
		{
			@Override
			public MachineBlock getMachineBlock()
			{
				machineBlock.setImplBlock(this);
				machineBlock.setProperty();
				return machineBlock;
			}
		};
	}
}
