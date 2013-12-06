package mekanism.induction.common;

import mekanism.common.IModule;
import mekanism.common.Mekanism;
import mekanism.common.MekanismRecipe;
import mekanism.common.Tier.EnergyCubeTier;
import mekanism.common.Version;
import mekanism.common.util.MekanismUtils;
import mekanism.induction.common.block.BlockBattery;
import mekanism.induction.common.block.BlockEMContractor;
import mekanism.induction.common.block.BlockTesla;
import mekanism.induction.common.item.ItemBlockContractor;
import mekanism.induction.common.item.ItemBlockMultimeter;
import mekanism.induction.common.tileentity.TileEntityBattery;
import mekanism.induction.common.tileentity.TileEntityEMContractor;
import mekanism.induction.common.tileentity.TileEntityMultimeter;
import mekanism.induction.common.tileentity.TileEntityTesla;
import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraftforge.common.MinecraftForge;
import universalelectricity.core.vector.Vector3;
import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.Mod.EventHandler;
import cpw.mods.fml.common.Mod.Instance;
import cpw.mods.fml.common.SidedProxy;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLPostInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.network.NetworkMod;
import cpw.mods.fml.common.network.NetworkRegistry;
import cpw.mods.fml.common.registry.GameRegistry;

@Mod(modid = "MekanismInduction", name = "MekanismInduction", version = "5.6.0", dependencies = "required-after:Mekanism;after:MekanismGenerators;after:ForgeMultipart")
@NetworkMod(clientSideRequired = true, serverSideRequired = false)
public class MekanismInduction implements IModule
{
	@Instance("MekanismInduction")
	public static MekanismInduction instance;

	@SidedProxy(clientSide = "mekanism.induction.client.InductionClientProxy", serverSide = "mekanism.induction.common.InductionCommonProxy")
	public static InductionCommonProxy proxy;

	/** MekanismInduction version number */
	public static Version versionNumber = new Version(5, 6, 0);

	/**
	 * Settings
	 */
	public static boolean SOUND_FXS = true;

	/** Block ID by Jyzarc */
	private static final int BLOCK_ID_PREFIX = 3200;
	/** Item ID by Horfius */
	private static final int ITEM_ID_PREFIX = 20150;
	public static int MAX_CONTRACTOR_DISTANCE = 200;

	private static int NEXT_BLOCK_ID = BLOCK_ID_PREFIX;
	private static int NEXT_ITEM_ID = ITEM_ID_PREFIX;

	public static int getNextBlockID()
	{
		return NEXT_BLOCK_ID++;
	}

	public static int getNextItemID()
	{
		return NEXT_ITEM_ID++;
	}

	//Blocks
	public static Block Tesla;
	public static Block ElectromagneticContractor;
	public static Block Battery;

	public static final Vector3[] DYE_COLORS = new Vector3[] { new Vector3(), new Vector3(1, 0, 0), new Vector3(0, 0.608, 0.232), new Vector3(0.588, 0.294, 0), new Vector3(0, 0, 1), new Vector3(0.5, 0, 05), new Vector3(0, 1, 1), new Vector3(0.8, 0.8, 0.8), new Vector3(0.3, 0.3, 0.3), new Vector3(1, 0.412, 0.706), new Vector3(0.616, 1, 0), new Vector3(1, 1, 0), new Vector3(0.46f, 0.932, 1), new Vector3(0.5, 0.2, 0.5), new Vector3(0.7, 0.5, 0.1), new Vector3(1, 1, 1) };

	@EventHandler
	public void preInit(FMLPreInitializationEvent evt)
	{
		NetworkRegistry.instance().registerGuiHandler(this, MekanismInduction.proxy);
		MinecraftForge.EVENT_BUS.register(new MultimeterEventHandler());

		//Blocks
		Tesla = new BlockTesla(Mekanism.configuration.getBlock("Tesla", getNextBlockID()).getInt()).setUnlocalizedName("Tesla");
		ElectromagneticContractor = new BlockEMContractor(Mekanism.configuration.getBlock("ElectromagneticContractor", getNextBlockID()).getInt()).setUnlocalizedName("ElectromagneticContractor");
		Battery = new BlockBattery(Mekanism.configuration.getBlock("Battery", getNextBlockID()).getInt()).setUnlocalizedName("Battery");

		GameRegistry.registerBlock(Tesla, "Tesla");
		GameRegistry.registerBlock(ElectromagneticContractor, ItemBlockContractor.class, "ElectromagneticContractor");
		GameRegistry.registerBlock(Battery, "Battery");

		//Tiles
		GameRegistry.registerTileEntity(TileEntityTesla.class, "Tesla");
		GameRegistry.registerTileEntity(TileEntityMultimeter.class, "Multimeter");
		GameRegistry.registerTileEntity(TileEntityEMContractor.class, "ElectromagneticContractor");
		GameRegistry.registerTileEntity(TileEntityBattery.class, "Battery");

		MekanismInduction.proxy.registerRenderers();
	}

	@EventHandler
	public void init(FMLInitializationEvent evt)
	{
		//Add this module to the core list
		Mekanism.modulesLoaded.add(this);
	}

	@EventHandler
	public void postInit(FMLPostInitializationEvent evt)
	{
		GameRegistry.addRecipe(new MekanismRecipe(new ItemStack(Tesla), new Object[] { "WEW", " C ", " I ", 'W', Mekanism.EnrichedAlloy, 'E', Item.eyeOfEnder, 'C', Mekanism.EnergyTablet.getUnchargedItem(), 'I', new ItemStack(Mekanism.BasicBlock, 1, 8) }));
		GameRegistry.addRecipe(new MekanismRecipe(new ItemStack(Battery, 4), new Object[] { "RRR", "CIC", "RRR", 'R', Item.redstone, 'I', MekanismUtils.getEnergyCube(EnergyCubeTier.BASIC), 'C', "circuitBasic" }));
		GameRegistry.addRecipe(new MekanismRecipe(new ItemStack(ElectromagneticContractor), new Object[] { " I ", "GCG", "WWW", 'W', "ingotSteel", 'C', Mekanism.EnergyTablet.getUnchargedItem(), 'G', "ingotOsmium", 'I', "ingotSteel" }));
	}

	@Override
	public Version getVersion()
	{
		return versionNumber;
	}

	@Override
	public String getName()
	{
		return "Induction";
	}
}
