package mekanism.induction.common;

import ic2.api.item.Items;

import java.util.Map;

import mekanism.common.IModule;
import mekanism.common.Mekanism;
import mekanism.common.MekanismRecipe;
import mekanism.common.Tier.EnergyCubeTier;
import mekanism.common.Version;
import mekanism.common.util.MekanismUtils;
import mekanism.induction.common.block.BlockBattery;
import mekanism.induction.common.block.BlockEMContractor;
import mekanism.induction.common.block.BlockMultimeter;
import mekanism.induction.common.block.BlockTesla;
import mekanism.induction.common.block.BlockWire;
import mekanism.induction.common.item.ItemBlockContractor;
import mekanism.induction.common.item.ItemBlockMultimeter;
import mekanism.induction.common.item.ItemBlockWire;
import mekanism.induction.common.tileentity.TileEntityBattery;
import mekanism.induction.common.tileentity.TileEntityEMContractor;
import mekanism.induction.common.tileentity.TileEntityMultimeter;
import mekanism.induction.common.tileentity.TileEntityTesla;
import mekanism.induction.common.tileentity.TileEntityWire;
import mekanism.induction.common.wire.EnumWireMaterial;
import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.common.Configuration;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.oredict.ShapedOreRecipe;
import net.minecraftforge.oredict.ShapelessOreRecipe;
import universalelectricity.compatibility.Compatibility;
import universalelectricity.core.vector.Vector3;
import cpw.mods.fml.common.Loader;
import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.Mod.EventHandler;
import cpw.mods.fml.common.Mod.Instance;
import cpw.mods.fml.common.ObfuscationReflectionHelper;
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
	public static boolean LO_FI_INSULATION = false;
	public static boolean SHINY_SILVER = true;

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

	/** With Forge Multipart; Use EnumWireMaterial reference. **/
	private static Item itemPartWire;

	//Blocks
	public static Block Tesla;
	public static Block Multimeter;
	public static Block ElectromagneticContractor;
	public static Block Battery;
	/** Without Forge Multipart **/
	private static Block blockWire;

	public static final Vector3[] DYE_COLORS = new Vector3[] { new Vector3(), new Vector3(1, 0, 0), new Vector3(0, 0.608, 0.232), new Vector3(0.588, 0.294, 0), new Vector3(0, 0, 1), new Vector3(0.5, 0, 05), new Vector3(0, 1, 1), new Vector3(0.8, 0.8, 0.8), new Vector3(0.3, 0.3, 0.3), new Vector3(1, 0.412, 0.706), new Vector3(0.616, 1, 0), new Vector3(1, 1, 0), new Vector3(0.46f, 0.932, 1), new Vector3(0.5, 0.2, 0.5), new Vector3(0.7, 0.5, 0.1), new Vector3(1, 1, 1) };

	@EventHandler
	public void preInit(FMLPreInitializationEvent evt)
	{
		NetworkRegistry.instance().registerGuiHandler(this, MekanismInduction.proxy);
		MinecraftForge.EVENT_BUS.register(new MultimeterEventHandler());

		//Register Items
		if(Loader.isModLoaded("ForgeMultipart"))
		{
			try {
				itemPartWire = ((Item)Class.forName("mekanism.induction.common.wire.ItemPartWire").getDeclaredConstructor(Integer.TYPE).newInstance(getNextItemID())).setUnlocalizedName("Wire");
				Mekanism.logger.fine("Mekanism Induction multipart loaded.");
			} catch(Exception e) {
				Mekanism.logger.severe("Failed to load multipart wire.");
				e.printStackTrace();
			}
		}
		else {
			Mekanism.logger.fine("Mekanism Induction Multipart disabled due to Forge Multipart not found.");
		}

		//Blocks
		Tesla = new BlockTesla(Mekanism.configuration.getBlock("Tesla", getNextBlockID()).getInt()).setUnlocalizedName("Tesla");
		Multimeter = new BlockMultimeter(Mekanism.configuration.getBlock("Multimeter", getNextBlockID()).getInt()).setUnlocalizedName("Multimeter");
		ElectromagneticContractor = new BlockEMContractor(Mekanism.configuration.getBlock("ElectromagneticContractor", getNextBlockID()).getInt()).setUnlocalizedName("ElectromagneticContractor");
		Battery = new BlockBattery(Mekanism.configuration.getBlock("Battery", getNextBlockID()).getInt()).setUnlocalizedName("Battery");

		if(itemPartWire == null)
		{
			blockWire = new BlockWire(getNextBlockID()).setUnlocalizedName("Wire");
		}

		Mekanism.configuration.save();

		GameRegistry.registerBlock(Tesla, "Tesla");
		GameRegistry.registerBlock(Multimeter, ItemBlockMultimeter.class, "Multimeter");
		GameRegistry.registerBlock(ElectromagneticContractor, ItemBlockContractor.class, "ElectromagneticContractor");
		GameRegistry.registerBlock(Battery, "Battery");

		//Tiles
		if(blockWire != null)
		{
			GameRegistry.registerBlock(blockWire, ItemBlockWire.class, blockWire.getUnlocalizedName());
		}

		//Tiles
		GameRegistry.registerTileEntity(TileEntityTesla.class, "Tesla");
		GameRegistry.registerTileEntity(TileEntityMultimeter.class, "Multimeter");
		GameRegistry.registerTileEntity(TileEntityEMContractor.class, "ElectromagneticContractor");
		GameRegistry.registerTileEntity(TileEntityBattery.class, "Battery");

		if(blockWire != null)
		{
			GameRegistry.registerTileEntity(TileEntityWire.class, blockWire.getUnlocalizedName());
		}

		MekanismInduction.proxy.registerRenderers();

		if(itemPartWire != null)
		{
			for(EnumWireMaterial material : EnumWireMaterial.values())
			{
				material.setWire(itemPartWire);
			}
		}
		else {
			for(EnumWireMaterial material : EnumWireMaterial.values())
			{
				material.setWire(blockWire);
			}
		}
	}

	@EventHandler
	public void init(FMLInitializationEvent evt)
	{
		//Add this module to the core list
		Mekanism.modulesLoaded.add(this);

		if(itemPartWire != null)
		{
			try {
				Class.forName("mekanism.induction.common.MultipartMI").newInstance();
			} catch(Exception e) {
				e.printStackTrace();
				Mekanism.logger.severe("Failed to initiate Mekanism Induction multipart module.");
			}
		}

		Compatibility.initiate();
	}

	@EventHandler
	public void postInit(FMLPostInitializationEvent evt)
	{
		/**
		 * Recipes
		 */
		final ItemStack defaultWire = EnumWireMaterial.IRON.getWire();

		/** Tesla - by Jyzarc */
		GameRegistry.addRecipe(new MekanismRecipe(new ItemStack(Tesla), new Object[] { "WEW", " C ", " I ", 'W', Mekanism.EnrichedAlloy, 'E', Item.eyeOfEnder, 'C', Mekanism.EnergyTablet.getUnchargedItem(), 'I', new ItemStack(Mekanism.BasicBlock, 1, 8) }));

		/** Multimeter */
		GameRegistry.addRecipe(new MekanismRecipe(new ItemStack(Multimeter), new Object[] { "WWW", "ICI", 'W', Mekanism.EnrichedAlloy, 'C', "battery", 'I', "ingotSteel" }));

		/** Battery */
		GameRegistry.addRecipe(new MekanismRecipe(new ItemStack(Battery, 4), new Object[] { "RRR", "CIC", "RRR", 'R', Item.redstone, 'I', MekanismUtils.getEnergyCube(EnergyCubeTier.BASIC), 'C', "circuitBasic" }));

		/** EM Contractor */
		GameRegistry.addRecipe(new MekanismRecipe(new ItemStack(ElectromagneticContractor), new Object[] { " I ", "GCG", "WWW", 'W', "ingotSteel", 'C', Mekanism.EnergyTablet.getUnchargedItem(), 'G', "ingotOsmium", 'I', "ingotSteel" }));

		/** Wires **/
		GameRegistry.addRecipe(new ShapedOreRecipe(EnumWireMaterial.COPPER.getWire(3), "MMM", 'M', "ingotCopper"));
		GameRegistry.addRecipe(new ShapedOreRecipe(EnumWireMaterial.TIN.getWire(3), "MMM", 'M', "ingotTin"));
		GameRegistry.addRecipe(new ShapedOreRecipe(EnumWireMaterial.IRON.getWire(3), "MMM", 'M', Item.ingotIron));
		GameRegistry.addRecipe(new ShapedOreRecipe(EnumWireMaterial.ALUMINUM.getWire(3), "MMM", 'M', "ingotAluminum"));
		GameRegistry.addRecipe(new ShapedOreRecipe(EnumWireMaterial.SILVER.getWire(), "MMM", 'M', "ingotSilver"));

		/** Wire Compatiblity **/
		if(Loader.isModLoaded("IC2"))
		{
			GameRegistry.addRecipe(new ShapelessOreRecipe(EnumWireMaterial.COPPER.getWire(), Items.getItem("copperCableItem")));
			GameRegistry.addRecipe(new ShapelessOreRecipe(EnumWireMaterial.TIN.getWire(), Items.getItem("tinCableItem")));
			GameRegistry.addRecipe(new ShapelessOreRecipe(EnumWireMaterial.IRON.getWire(), Items.getItem("ironCableItem")));
		}
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
