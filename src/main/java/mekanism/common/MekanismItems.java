package mekanism.common;

import mekanism.common.item.ItemAlloy;
import mekanism.common.item.ItemAtomicDisassembler;
import mekanism.common.item.ItemBalloon;
import mekanism.common.item.ItemClump;
import mekanism.common.item.ItemConfigurationCard;
import mekanism.common.item.ItemConfigurator;
import mekanism.common.item.ItemControlCircuit;
import mekanism.common.item.ItemCraftingFormula;
import mekanism.common.item.ItemCrystal;
import mekanism.common.item.ItemDictionary;
import mekanism.common.item.ItemDirtyDust;
import mekanism.common.item.ItemDust;
import mekanism.common.item.ItemElectricBow;
import mekanism.common.item.ItemEnergized;
import mekanism.common.item.ItemFlamethrower;
import mekanism.common.item.ItemFreeRunners;
import mekanism.common.item.ItemGasMask;
import mekanism.common.item.ItemGaugeDropper;
import mekanism.common.item.ItemHDPE;
import mekanism.common.item.ItemIngot;
import mekanism.common.item.ItemJetpack;
import mekanism.common.item.ItemMekanism;
import mekanism.common.item.ItemNetworkReader;
import mekanism.common.item.ItemOtherDust;
import mekanism.common.item.ItemPortableTeleporter;
import mekanism.common.item.ItemProxy;
import mekanism.common.item.ItemRobit;
import mekanism.common.item.ItemScubaTank;
import mekanism.common.item.ItemSeismicReader;
import mekanism.common.item.ItemShard;
import mekanism.common.item.ItemTierInstaller;
import mekanism.common.item.ItemUpgrade;
import mekanism.common.item.ItemWalkieTalkie;
import mekanism.common.multipart.ItemGlowPanel;
import mekanism.common.multipart.ItemPartTransmitter;
import net.minecraft.item.Item;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.fml.common.registry.GameRegistry.ObjectHolder;

@ObjectHolder("Mekanism")
public class MekanismItems
{
	public static final Item EnrichedAlloy = new ItemAlloy();
	public static final Item ReinforcedAlloy = new ItemAlloy();
	public static final Item AtomicAlloy = new ItemAlloy();
	public static final Item TeleportationCore = new ItemMekanism();
	public static final Item ElectrolyticCore = new ItemMekanism();
	public static final Item Substrate = new ItemMekanism();
	public static final Item Polyethene = new ItemHDPE();
	public static final Item BioFuel = new ItemMekanism();
	public static final Item ItemProxy = new ItemProxy();
	public static final Item EnrichedIron = new ItemMekanism();
	public static final Item CompressedCarbon = new ItemMekanism();
	public static final Item CompressedRedstone = new ItemMekanism();
	public static final Item CompressedDiamond = new ItemMekanism();
	public static final Item CompressedObsidian = new ItemMekanism();
	public static final Item SpeedUpgrade = new ItemUpgrade(Upgrade.SPEED);
	public static final Item EnergyUpgrade = new ItemUpgrade(Upgrade.ENERGY);
	public static final Item FilterUpgrade = new ItemUpgrade(Upgrade.FILTER);
	public static final Item MufflingUpgrade = new ItemUpgrade(Upgrade.MUFFLING);
	public static final Item GasUpgrade = new ItemUpgrade(Upgrade.GAS);
	public static final Item AnchorUpgrade = new ItemUpgrade(Upgrade.ANCHOR);
	public static final Item TierInstaller = new ItemTierInstaller();
	public static final ItemEnergized EnergyTablet = (ItemEnergized)new ItemEnergized(1000000);
	public static final ItemRobit Robit = (ItemRobit)new ItemRobit();
	public static final ItemAtomicDisassembler AtomicDisassembler = (ItemAtomicDisassembler)new ItemAtomicDisassembler();
	public static final ItemPortableTeleporter PortableTeleporter = (ItemPortableTeleporter)new ItemPortableTeleporter();
	public static final ItemConfigurator Configurator = (ItemConfigurator)new ItemConfigurator();
	public static final ItemNetworkReader NetworkReader = (ItemNetworkReader)new ItemNetworkReader();
	public static final Item WalkieTalkie = new ItemWalkieTalkie();
	public static final ItemElectricBow ElectricBow = (ItemElectricBow)new ItemElectricBow();
	public static final ItemFlamethrower Flamethrower = (ItemFlamethrower)new ItemFlamethrower();
	public static final ItemSeismicReader SeismicReader = (ItemSeismicReader)new ItemSeismicReader();
	public static final Item Dictionary = new ItemDictionary();
	public static final ItemGaugeDropper GaugeDropper = (ItemGaugeDropper)new ItemGaugeDropper();
	public static final Item ConfigurationCard = new ItemConfigurationCard();
	public static final Item CraftingFormula = new ItemCraftingFormula();
	public static final Item PartTransmitter = new ItemPartTransmitter();
	public static final Item GlowPanel = new ItemGlowPanel();
	public static final ItemScubaTank ScubaTank = (ItemScubaTank)new ItemScubaTank();
	public static final ItemGasMask GasMask = (ItemGasMask)new ItemGasMask();
	public static final ItemJetpack Jetpack = (ItemJetpack)new ItemJetpack();
	public static final ItemJetpack ArmoredJetpack = (ItemJetpack)new ItemJetpack();
	public static final ItemFreeRunners FreeRunners = (ItemFreeRunners)new ItemFreeRunners();
	public static final Item Balloon = new ItemBalloon();

	//Multi-ID Items
	public static final Item OtherDust = new ItemOtherDust();
	public static final Item Dust = new ItemDust();
	public static final Item Sawdust = new ItemMekanism();
	public static final Item Salt = new ItemMekanism();
	public static final Item Ingot = new ItemIngot();
	public static final Item Clump = new ItemClump();
	public static final Item DirtyDust = new ItemDirtyDust();
	public static final Item Shard = new ItemShard();
	public static final Item Crystal = new ItemCrystal();
	public static final Item ControlCircuit = new ItemControlCircuit();

	/**
	 * Adds and registers all items.
	 */
	public static void register()
	{
		GameRegistry.register(init(PartTransmitter, "MultipartTransmitter"));
		GameRegistry.register(init(ElectricBow, "ElectricBow"));
		GameRegistry.register(init(Dust, "Dust"));
		GameRegistry.register(init(Ingot, "Ingot"));
		GameRegistry.register(init(EnergyTablet, "EnergyTablet"));
		GameRegistry.register(init(SpeedUpgrade, "SpeedUpgrade"));
		GameRegistry.register(init(EnergyUpgrade, "EnergyUpgrade"));
		GameRegistry.register(init(FilterUpgrade, "FilterUpgrade"));
		GameRegistry.register(init(MufflingUpgrade, "MufflingUpgrade"));
		GameRegistry.register(init(GasUpgrade, "GasUpgrade"));
		GameRegistry.register(init(AnchorUpgrade, "AnchorUpgrade"));
		GameRegistry.register(init(Robit, "Robit"));
		GameRegistry.register(init(AtomicDisassembler, "AtomicDisassembler"));
		GameRegistry.register(init(EnrichedAlloy, "EnrichedAlloy"));
		GameRegistry.register(init(ReinforcedAlloy, "ReinforcedAlloy"));
		GameRegistry.register(init(AtomicAlloy, "AtomicAlloy"));
		GameRegistry.register(init(ItemProxy, "ItemProxy"));
		GameRegistry.register(init(ControlCircuit, "ControlCircuit"));
		GameRegistry.register(init(EnrichedIron, "EnrichedIron"));
		GameRegistry.register(init(CompressedCarbon, "CompressedCarbon"));
		GameRegistry.register(init(CompressedRedstone, "CompressedRedstone"));
		GameRegistry.register(init(CompressedDiamond, "CompressedDiamond"));
		GameRegistry.register(init(CompressedObsidian, "CompressedObsidian"));
		GameRegistry.register(init(PortableTeleporter, "PortableTeleporter"));
		GameRegistry.register(init(TeleportationCore, "TeleportationCore"));
		GameRegistry.register(init(Clump, "Clump"));
		GameRegistry.register(init(DirtyDust, "DirtyDust"));
		GameRegistry.register(init(Configurator, "Configurator"));
		GameRegistry.register(init(NetworkReader, "NetworkReader"));
		GameRegistry.register(init(WalkieTalkie, "WalkieTalkie"));
		GameRegistry.register(init(Jetpack, "Jetpack"));
		GameRegistry.register(init(Dictionary, "Dictionary"));
		GameRegistry.register(init(GasMask, "GasMask"));
		GameRegistry.register(init(ScubaTank, "ScubaTank"));
		GameRegistry.register(init(Balloon, "Balloon"));
		GameRegistry.register(init(Shard, "Shard"));
		GameRegistry.register(init(ElectrolyticCore, "ElectrolyticCore"));
		GameRegistry.register(init(Sawdust, "Sawdust"));
		GameRegistry.register(init(Salt, "Salt"));
		GameRegistry.register(init(Crystal, "Crystal"));
		GameRegistry.register(init(FreeRunners, "FreeRunners"));
		GameRegistry.register(init(ArmoredJetpack, "ArmoredJetpack"));
		GameRegistry.register(init(ConfigurationCard, "ConfigurationCard"));
		GameRegistry.register(init(CraftingFormula, "CraftingFormula"));
		GameRegistry.register(init(SeismicReader, "SeismicReader"));
		GameRegistry.register(init(Substrate, "Substrate"));
		GameRegistry.register(init(Polyethene, "Polyethene"));
		GameRegistry.register(init(BioFuel, "BioFuel"));
		GameRegistry.register(init(GlowPanel, "GlowPanel"));
		GameRegistry.register(init(Flamethrower, "Flamethrower"));
		GameRegistry.register(init(GaugeDropper, "GaugeDropper"));
		GameRegistry.register(init(TierInstaller, "TierInstaller"));
		GameRegistry.register(init(OtherDust, "OtherDust"));

		MinecraftForge.EVENT_BUS.register(GasMask);
		MinecraftForge.EVENT_BUS.register(FreeRunners);
		
		Mekanism.proxy.registerItemRenders();
	}
	
	public static Item init(Item item, String name)
	{
		return item.setUnlocalizedName(name).setRegistryName("mekanism:" + name);
	}
}
