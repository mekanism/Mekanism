package mekanism.common;

import mekanism.common.item.ItemAtomicDisassembler;
import mekanism.common.item.ItemBalloon;
import mekanism.common.item.ItemClump;
import mekanism.common.item.ItemConfigurator;
import mekanism.common.item.ItemControlCircuit;
import mekanism.common.item.ItemCrystal;
import mekanism.common.item.ItemDictionary;
import mekanism.common.item.ItemDirtyDust;
import mekanism.common.item.ItemDust;
import mekanism.common.item.ItemElectricBow;
import mekanism.common.item.ItemEnergized;
import mekanism.common.item.ItemFilterCard;
import mekanism.common.item.ItemFlamethrower;
import mekanism.common.item.ItemFreeRunners;
import mekanism.common.item.ItemGasMask;
import mekanism.common.item.ItemHDPE;
import mekanism.common.item.ItemIngot;
import mekanism.common.item.ItemJetpack;
import mekanism.common.item.ItemMekanism;
import mekanism.common.item.ItemNetworkReader;
import mekanism.common.item.ItemPortableTeleporter;
import mekanism.common.item.ItemProxy;
import mekanism.common.item.ItemRobit;
import mekanism.common.item.ItemScubaTank;
import mekanism.common.item.ItemSeismicReader;
import mekanism.common.item.ItemShard;
import mekanism.common.item.ItemUpgrade;
import mekanism.common.item.ItemWalkieTalkie;
import mekanism.common.multipart.ItemGlowPanel;
import mekanism.common.multipart.ItemPartTransmitter;

import net.minecraft.init.Items;
import net.minecraft.item.Item;
import cpw.mods.fml.common.registry.GameRegistry.ObjectHolder;

@ObjectHolder("Mekanism")
public class MekanismItems
{
	//Items
	public static final ItemElectricBow ElectricBow = (ItemElectricBow)new ItemElectricBow().setUnlocalizedName("ElectricBow");
	public static final Item EnrichedAlloy = new ItemMekanism().setUnlocalizedName("EnrichedAlloy");
	public static final Item ReinforcedAlloy = new ItemMekanism().setUnlocalizedName("ReinforcedAlloy");
	public static final Item AtomicAlloy = new ItemMekanism().setUnlocalizedName("AtomicAlloy");
	public static final ItemEnergized EnergyTablet = (ItemEnergized)new ItemEnergized(1000000).setUnlocalizedName("EnergyTablet");
	public static final Item SpeedUpgrade = new ItemUpgrade(Upgrade.SPEED).setUnlocalizedName("SpeedUpgrade");
	public static final Item EnergyUpgrade = new ItemUpgrade(Upgrade.ENERGY).setUnlocalizedName("EnergyUpgrade");
	public static final ItemRobit Robit = (ItemRobit)new ItemRobit().setUnlocalizedName("Robit");
	public static final ItemAtomicDisassembler AtomicDisassembler = (ItemAtomicDisassembler)new ItemAtomicDisassembler().setUnlocalizedName("AtomicDisassembler");
	public static final Item EnrichedIron = new ItemMekanism().setUnlocalizedName("EnrichedIron");
	public static final Item CompressedCarbon = new ItemMekanism().setUnlocalizedName("CompressedCarbon");
	public static final Item CompressedRedstone = new ItemMekanism().setUnlocalizedName("CompressedRedstone");
	public static final Item CompressedDiamond = new ItemMekanism().setUnlocalizedName("CompressedDiamond");
	public static final Item CompressedObsidian = new ItemMekanism().setUnlocalizedName("CompressedObsidian");
	public static final Item PortableTeleporter = new ItemPortableTeleporter().setUnlocalizedName("PortableTeleporter");
	public static final Item TeleportationCore = new ItemMekanism().setUnlocalizedName("TeleportationCore");
	public static final Item Configurator = new ItemConfigurator().setUnlocalizedName("Configurator");
	public static final Item NetworkReader = new ItemNetworkReader().setUnlocalizedName("NetworkReader");
	public static final Item WalkieTalkie = new ItemWalkieTalkie().setUnlocalizedName("WalkieTalkie");
	public static final Item Proxy = new ItemProxy().setUnlocalizedName("ItemProxy");
	public static final Item PartTransmitter = new ItemPartTransmitter().setUnlocalizedName("MultipartTransmitter");
	public static final Item GlowPanel = new ItemGlowPanel().setUnlocalizedName("GlowPanel");
	public static final ItemJetpack Jetpack = (ItemJetpack)new ItemJetpack().setUnlocalizedName("Jetpack");
	public static final ItemScubaTank ScubaTank = (ItemScubaTank)new ItemScubaTank().setUnlocalizedName("ScubaTank");
	public static final ItemGasMask GasMask = (ItemGasMask)new ItemGasMask().setUnlocalizedName("GasMask");
	public static final ItemFlamethrower Flamethrower = (ItemFlamethrower)new ItemFlamethrower().setUnlocalizedName("Flamethrower");
	public static final Item Dictionary = new ItemDictionary().setUnlocalizedName("Dictionary");
	public static final Item Balloon = new ItemBalloon().setUnlocalizedName("Balloon");
	public static final Item ElectrolyticCore = new ItemMekanism().setUnlocalizedName("ElectrolyticCore");
	public static final Item Sawdust = new ItemMekanism().setUnlocalizedName("Sawdust");
	public static final Item Salt = new ItemMekanism().setUnlocalizedName("Salt");
	public static final Item BrineBucket = new ItemMekanism().setMaxStackSize(1).setContainerItem(Items.bucket).setUnlocalizedName("BrineBucket");
	public static final Item FreeRunners = new ItemFreeRunners().setUnlocalizedName("FreeRunners");
	public static final ItemJetpack ArmoredJetpack = (ItemJetpack)new ItemJetpack().setUnlocalizedName("ArmoredJetpack");
	public static final Item FilterCard = new ItemFilterCard().setUnlocalizedName("FilterCard");
	public static final ItemSeismicReader SeismicReader = (ItemSeismicReader)new ItemSeismicReader().setUnlocalizedName("SeismicReader");
	public static final Item Substrate = new ItemMekanism().setUnlocalizedName("Substrate");
	public static final Item Polyethene = new ItemHDPE().setUnlocalizedName("HDPE");
	public static final Item BioFuel = new ItemMekanism().setUnlocalizedName("BioFuel");

	//Multi-ID Items
	public static final Item Dust = new ItemDust();
	public static final Item Ingot = new ItemIngot();
	public static final Item Clump = new ItemClump();
	public static final Item DirtyDust = new ItemDirtyDust();
	public static final Item Shard = new ItemShard();
	public static final Item Crystal = new ItemCrystal();
	public static final Item ControlCircuit = new ItemControlCircuit();
}
