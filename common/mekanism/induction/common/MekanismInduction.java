package mekanism.induction.common;

import mekanism.common.IModule;
import mekanism.common.Mekanism;
import mekanism.common.Version;
import mekanism.induction.common.block.BlockBattery;
import mekanism.induction.common.block.BlockEMContractor;
import mekanism.induction.common.block.BlockMultimeter;
import mekanism.induction.common.block.BlockTesla;
import mekanism.induction.common.item.ItemBlockContractor;
import mekanism.induction.common.item.ItemBlockMultimeter;
import mekanism.induction.common.item.ItemLinker;
import mekanism.induction.common.tileentity.TileEntityBattery;
import mekanism.induction.common.tileentity.TileEntityEMContractor;
import mekanism.induction.common.tileentity.TileEntityMultimeter;
import mekanism.induction.common.tileentity.TileEntityTesla;
import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraftforge.common.Configuration;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.oredict.ShapedOreRecipe;
import universalelectricity.compatibility.Compatibility;
import universalelectricity.core.item.IItemElectric;
import universalelectricity.core.vector.Vector3;
import calclavia.lib.UniversalRecipes;
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

@Mod(modid = "MekanismInduction", name = "MekanismInduction", version = "5.6.0", dependencies = "required-after:Mekanism")
@NetworkMod(clientSideRequired = true, serverSideRequired = false)
public class MekanismInduction implements IModule
{
	@Instance("MekanismInduction")
	public static MekanismInduction instance;

	@SidedProxy(clientSide = "mekanism.induction.client.InductionClientProxy", serverSide = "mekanism.induction.common.InductionCommonProxy")
	public static InductionCommonProxy proxy;

	/** MekanismInduction version number */
	public static Version versionNumber = new Version(5, 6, 0);

	//Settings
	public static float FURNACE_WATTAGE = 10;

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

	//Items
	public static Item Linker;

	//Blocks
	public static Block Tesla;
	public static Block Multimeter;
	public static Block ElectromagneticContractor;
	public static Block Battery;
	public static Block blockAdvancedFurnaceIdle, blockAdvancedFurnaceBurning;

	public static final Vector3[] DYE_COLORS = new Vector3[] { new Vector3(), new Vector3(1, 0, 0), new Vector3(0, 0.608, 0.232), new Vector3(0.588, 0.294, 0), new Vector3(0, 0, 1), new Vector3(0.5, 0, 05), new Vector3(0, 1, 1), new Vector3(0.8, 0.8, 0.8), new Vector3(0.3, 0.3, 0.3), new Vector3(1, 0.412, 0.706), new Vector3(0.616, 1, 0), new Vector3(1, 1, 0), new Vector3(0.46f, 0.932, 1), new Vector3(0.5, 0.2, 0.5), new Vector3(0.7, 0.5, 0.1), new Vector3(1, 1, 1) };

	@EventHandler
	public void preInit(FMLPreInitializationEvent evt)
	{
		NetworkRegistry.instance().registerGuiHandler(this, MekanismInduction.proxy);
		MinecraftForge.EVENT_BUS.register(new MultimeterEventHandler());
		Mekanism.configuration.load();

		//Items
		Linker = new ItemLinker(Mekanism.configuration.get(Configuration.CATEGORY_ITEM, "Linker", getNextItemID()).getInt()).setUnlocalizedName("Linker");

		//Blocks
		Tesla = new BlockTesla(Mekanism.configuration.getBlock("Tesla", getNextBlockID()).getInt()).setUnlocalizedName("Tesla");
		Multimeter = new BlockMultimeter(Mekanism.configuration.getBlock("Multimeter", getNextBlockID()).getInt()).setUnlocalizedName("Multimeter");
		ElectromagneticContractor = new BlockEMContractor(Mekanism.configuration.getBlock("ElectromagneticContractor", getNextBlockID()).getInt()).setUnlocalizedName("ElectromagneticContractor");
		Battery = new BlockBattery(Mekanism.configuration.getBlock("Battery", getNextBlockID()).getInt()).setUnlocalizedName("Battery");

		Mekanism.configuration.save();

		GameRegistry.registerItem(Linker, "Linker");

		GameRegistry.registerBlock(Tesla, "Tesla");
		GameRegistry.registerBlock(Multimeter, ItemBlockMultimeter.class, "Multimeter");
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
		// Add this module to the core list
		Mekanism.modulesLoaded.add(this);

		Compatibility.initiate();
	}

	@EventHandler
	public void postInit(FMLPostInitializationEvent evt)
	{
		/** Capacitor **/
		GameRegistry.addRecipe(new ShapedOreRecipe(Mekanism.EnergyTablet, "RRR", "RIR", "RRR", 'R', Item.redstone, 'I', UniversalRecipes.PRIMARY_METAL));

		/** Linker **/
		GameRegistry.addRecipe(new ShapedOreRecipe(Linker, " E ", "GCG", " E ", 'E', Item.eyeOfEnder, 'C', Mekanism.EnergyTablet, 'G', UniversalRecipes.SECONDARY_METAL));

		/** Tesla - by Jyzarc */
		GameRegistry.addRecipe(new ShapedOreRecipe(Tesla, "WEW", " C ", " I ", 'W', Mekanism.EnrichedAlloy, 'E', Item.eyeOfEnder, 'C', Mekanism.EnergyTablet, 'I', Mekanism.MachineBlock));

		/** Multimeter */
		GameRegistry.addRecipe(new ShapedOreRecipe(Multimeter, "WWW", "ICI", 'W', Mekanism.EnrichedAlloy, 'C', Mekanism.EnergyTablet, 'I', UniversalRecipes.PRIMARY_METAL));

		/** Battery */
		GameRegistry.addRecipe(new ShapedOreRecipe(Battery, "III", "IRI", "III", 'R', Block.blockRedstone, 'I', Mekanism.EnergyCube));

		/** EM Contractor */
		GameRegistry.addRecipe(new ShapedOreRecipe(ElectromagneticContractor, " I ", "GCG", "WWW", 'W', UniversalRecipes.PRIMARY_METAL, 'C', Mekanism.EnergyTablet, 'G', UniversalRecipes.SECONDARY_METAL, 'I', UniversalRecipes.PRIMARY_METAL));
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
