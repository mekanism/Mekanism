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
import mekanism.common.block.states.BlockStateBasic.BasicBlockStateMapper;
import mekanism.common.block.states.BlockStateBasic.BasicBlockType;
import mekanism.common.block.states.BlockStateMachine.MachineBlockStateMapper;
import mekanism.common.block.states.BlockStateMachine.MachineType;
import mekanism.common.block.BlockObsidianTNT;
import mekanism.common.block.BlockOre;
import mekanism.common.block.BlockPlastic;
import mekanism.common.block.BlockPlasticFence;
import mekanism.common.block.BlockSalt;
import mekanism.common.block.states.BlockStateMachine;
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
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.ItemModelMesher;
import net.minecraft.client.renderer.block.statemap.IStateMapper;
import net.minecraft.client.resources.model.ModelBakery;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.item.Item;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.gen.structure.StructureVillagePieces.Road;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.fml.common.registry.GameRegistry.ObjectHolder;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javafx.scene.effect.Glow;

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
	}

	@SideOnly(Side.CLIENT)
	public static void registerRender()
	{
		ItemModelMesher mesher = Minecraft.getMinecraft().getRenderItem().getItemModelMesher();

		for(MachineType type : BlockStateMachine.MachineType.values())
		{
			mesher.register(Item.getItemFromBlock(type.typeBlock.getBlock()), type.meta, new ModelResourceLocation("mekanism:" + type.getName(), "inventory"));
		}

		for(BasicBlockType type : BasicBlockType.values())
		{
			mesher.register(Item.getItemFromBlock(type.blockType.getBlock()), type.meta, new ModelResourceLocation("mekanism:" + type.getName(), "inventory"));
		}

		mesher.register(Item.getItemFromBlock(EnergyCube), 0, new ModelResourceLocation("mekanism:energy_cube", "inventory"));

		ModelBakery.registerItemVariants(Item.getItemFromBlock(MachineBlock),
				new ResourceLocation("mekanism", "enrichment_chamber"),
				new ResourceLocation("mekanism", "osmium_compressor"),
				new ResourceLocation("mekanism", "combiner"),
				new ResourceLocation("mekanism", "crusher"),
				new ResourceLocation("mekanism", "digital_miner"),
				new ResourceLocation("mekanism", "basic_factory"),
				new ResourceLocation("mekanism", "advanced_factory"),
				new ResourceLocation("mekanism", "elite_factory"),
				new ResourceLocation("mekanism", "metallurgic_infuser"),
				new ResourceLocation("mekanism", "purification_chamber"),
				new ResourceLocation("mekanism", "energized_smelter"),
				new ResourceLocation("mekanism", "teleporter"),
				new ResourceLocation("mekanism", "electric_pump"),
				new ResourceLocation("mekanism", "electric_chest"),
				new ResourceLocation("mekanism", "chargepad"),
				new ResourceLocation("mekanism", "logistical_sorter")
		);

		ModelBakery.registerItemVariants(Item.getItemFromBlock(MachineBlock2),
				new ResourceLocation("mekanism", "rotary_condensentrator"),
				new ResourceLocation("mekanism", "chemical_oxidiser"),
				new ResourceLocation("mekanism", "chemical_infuser"),
				new ResourceLocation("mekanism", "chemical_injection_chamber"),
				new ResourceLocation("mekanism", "electrolytic_separator"),
				new ResourceLocation("mekanism", "precision_sawmill"),
				new ResourceLocation("mekanism", "chemical_dissolution_chamber"),
				new ResourceLocation("mekanism", "chemical_washer"),
				new ResourceLocation("mekanism", "chemical_crystallizer"),
				new ResourceLocation("mekanism", "seismic_vibrator"),
				new ResourceLocation("mekanism", "pressurized_reaction_chamber"),
				new ResourceLocation("mekanism", "portable_tank"),
				new ResourceLocation("mekanism", "fluidic_plenisher"),
				new ResourceLocation("mekanism", "laser"),
				new ResourceLocation("mekanism", "laser_amplifier"),
				new ResourceLocation("mekanism", "laser_tractor_bean")
		);

		ModelBakery.registerItemVariants(Item.getItemFromBlock(MachineBlock3),
				new ResourceLocation("mekanism", "entangled_block"),
				new ResourceLocation("mekanism", "solar_neutron_activator"),
				new ResourceLocation("mekanism", "ambient_accumulator"),
				new ResourceLocation("mekanism", "oredictionificator")
		);

		ModelBakery.registerItemVariants(Item.getItemFromBlock(BasicBlock),
				new ResourceLocation("mekanism", "osmium_block"),
				new ResourceLocation("mekanism", "bronze_block"),
				new ResourceLocation("mekanism", "refined_obsidian"),
				new ResourceLocation("mekanism", "coal_block"),
				new ResourceLocation("mekanism", "refined_glowstone"),
				new ResourceLocation("mekanism", "steel_block"),
				new ResourceLocation("mekanism", "bin"),
				new ResourceLocation("mekanism", "teleporter_frame"),
				new ResourceLocation("mekanism", "steel_casing"),
				new ResourceLocation("mekanism", "dynamic_tank"),
				new ResourceLocation("mekanism", "dynamic_glass"),
				new ResourceLocation("mekanism", "dynamic_valve"),
				new ResourceLocation("mekanism", "copper_block"),
				new ResourceLocation("mekanism", "tin_block"),
				new ResourceLocation("mekanism", "solar_evaporation_controller"),
				new ResourceLocation("mekanism", "solar_evaporation_valve")
		);

		ModelBakery.registerItemVariants(Item.getItemFromBlock(BasicBlock2),
				new ResourceLocation("mekanism", "solar_evaporation_block"),
				new ResourceLocation("mekanism", "induction_casing"),
				new ResourceLocation("mekanism", "induction_port"),
				new ResourceLocation("mekanism", "induction_cell"),
				new ResourceLocation("mekanism", "induction_provider")
		);

	}
}
