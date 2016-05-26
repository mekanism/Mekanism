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
import mekanism.common.item.ItemFactoryInstaller;
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
import mekanism.common.item.ItemUpgrade;
import mekanism.common.item.ItemWalkieTalkie;
import mekanism.common.multipart.ItemGlowPanel;
import mekanism.common.multipart.ItemPartTransmitter;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fluids.FluidContainerRegistry;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.fml.common.registry.GameRegistry.ObjectHolder;

@ObjectHolder("Mekanism")
public class MekanismItems
{
	public static final Item EnrichedAlloy = init(new ItemAlloy(), "EnrichedAlloy");
	public static final Item ReinforcedAlloy = init(new ItemAlloy(), "ReinforcedAlloy");
	public static final Item AtomicAlloy = init(new ItemAlloy(), "AtomicAlloy");
	public static final Item TeleportationCore = init(new ItemMekanism(), "TeleportationCore");
	public static final Item ElectrolyticCore = init(new ItemMekanism(), "ElectrolyticCore");
	public static final Item Substrate = init(new ItemMekanism(), "Substrate");
	public static final Item Polyethene = init(new ItemHDPE(), "HDPE");
	public static final Item BioFuel = init(new ItemMekanism(), "BioFuel");
	public static final Item ItemProxy = init(new ItemProxy(), "ItemProxy");
	public static final Item EnrichedIron = init(new ItemMekanism(), "EnrichedIron");
	public static final Item CompressedCarbon = init(new ItemMekanism(), "CompressedCarbon");
	public static final Item CompressedRedstone = init(new ItemMekanism(), "CompressedRedstone");
	public static final Item CompressedDiamond = init(new ItemMekanism(), "CompressedDiamond");
	public static final Item CompressedObsidian = init(new ItemMekanism(), "CompressedObsidian");
	public static final Item BrineBucket = init(new ItemMekanism().setMaxStackSize(1).setContainerItem(Items.BUCKET), "BrineBucket");
	public static final Item LithiumBucket = init(new ItemMekanism().setMaxStackSize(1).setContainerItem(Items.BUCKET), "LithiumBucket");
	public static final Item HeavyWaterBucket = init(new ItemMekanism().setMaxStackSize(1).setContainerItem(Items.BUCKET), "HeavyWaterBucket");
	public static final Item SpeedUpgrade = init(new ItemUpgrade(Upgrade.SPEED), "SpeedUpgrade");
	public static final Item EnergyUpgrade = init(new ItemUpgrade(Upgrade.ENERGY), "EnergyUpgrade");
	public static final Item FilterUpgrade = init(new ItemUpgrade(Upgrade.FILTER), "FilterUpgrade");
	public static final Item MufflingUpgrade = init(new ItemUpgrade(Upgrade.MUFFLING), "MufflingUpgrade");
	public static final Item GasUpgrade = init(new ItemUpgrade(Upgrade.GAS), "GasUpgrade");
	public static final Item FactoryInstaller = init(new ItemFactoryInstaller(), "FactoryInstaller");
	public static final ItemEnergized EnergyTablet = (ItemEnergized)init(new ItemEnergized(1000000), "EnergyTablet");
	public static final ItemRobit Robit = (ItemRobit)init(new ItemRobit(), "Robit");
	public static final ItemAtomicDisassembler AtomicDisassembler = (ItemAtomicDisassembler)init(new ItemAtomicDisassembler(), "AtomicDisassembler");
	public static final ItemPortableTeleporter PortableTeleporter = (ItemPortableTeleporter)init(new ItemPortableTeleporter(), "PortableTeleporter");
	public static final ItemConfigurator Configurator = (ItemConfigurator)init(new ItemConfigurator(), "Configurator");
	public static final ItemNetworkReader NetworkReader = (ItemNetworkReader)init(new ItemNetworkReader(), "NetworkReader");
	public static final Item WalkieTalkie = init(new ItemWalkieTalkie(), "WalkieTalkie");
	public static final ItemElectricBow ElectricBow = (ItemElectricBow)init(new ItemElectricBow(), "ElectricBow");
	public static final ItemFlamethrower Flamethrower = (ItemFlamethrower)init(new ItemFlamethrower(), "Flamethrower");
	public static final ItemSeismicReader SeismicReader = (ItemSeismicReader)init(new ItemSeismicReader(), "SeismicReader");
	public static final Item Dictionary = init(new ItemDictionary(), "Dictionary");
	public static final ItemGaugeDropper GaugeDropper = (ItemGaugeDropper)init(new ItemGaugeDropper(), "GaugeDropper");
	public static final Item ConfigurationCard = init(new ItemConfigurationCard(), "ConfigurationCard");
	public static final Item CraftingFormula = init(new ItemCraftingFormula(), "CraftingFormula");
	public static final Item PartTransmitter = init(new ItemPartTransmitter(), "MultipartTransmitter");
	public static final Item GlowPanel = init(new ItemGlowPanel(), "GlowPanel");
	public static final ItemScubaTank ScubaTank = (ItemScubaTank)init(new ItemScubaTank(), "ScubaTank");
	public static final ItemGasMask GasMask = (ItemGasMask)init(new ItemGasMask(), "GasMask");
	public static final ItemJetpack Jetpack = (ItemJetpack)init(new ItemJetpack(), "Jetpack");
	public static final ItemJetpack ArmoredJetpack = (ItemJetpack)init(new ItemJetpack(), "ArmoredJetpack");
	public static final ItemFreeRunners FreeRunners = (ItemFreeRunners)init(new ItemFreeRunners(), "FreeRunners");
	public static final Item Balloon = init(new ItemBalloon(), "Balloon");

	//Multi-ID Items
	public static final Item OtherDust = init(new ItemOtherDust(), "OtherDust");
	public static final Item Dust = init(new ItemDust(), "Dust");
	public static final Item Sawdust = init(new ItemMekanism(), "Sawdust");
	public static final Item Salt = init(new ItemMekanism(), "Salt");
	public static final Item Ingot = init(new ItemIngot(), "Ingot");
	public static final Item Clump = init(new ItemClump(), "Clump");
	public static final Item DirtyDust = init(new ItemDirtyDust(), "DirtyDust");
	public static final Item Shard = init(new ItemShard(), "Shard");
	public static final Item Crystal = init(new ItemCrystal(), "Crystal");
	public static final Item ControlCircuit = init(new ItemControlCircuit(), "ControlCircuit");

	/**
	 * Adds and registers all items.
	 */
	public static void register()
	{
		GameRegistry.register(PartTransmitter);
		GameRegistry.register(ElectricBow);
		GameRegistry.register(Dust);
		GameRegistry.register(Ingot);
		GameRegistry.register(EnergyTablet);
		GameRegistry.register(SpeedUpgrade);
		GameRegistry.register(EnergyUpgrade);
		GameRegistry.register(FilterUpgrade);
		GameRegistry.register(MufflingUpgrade);
		GameRegistry.register(GasUpgrade);
		GameRegistry.register(Robit);
		GameRegistry.register(AtomicDisassembler);
		GameRegistry.register(EnrichedAlloy);
		GameRegistry.register(ReinforcedAlloy);
		GameRegistry.register(AtomicAlloy);
		GameRegistry.register(ItemProxy);
		GameRegistry.register(ControlCircuit);
		GameRegistry.register(EnrichedIron);
		GameRegistry.register(CompressedCarbon);
		GameRegistry.register(CompressedRedstone);
		GameRegistry.register(CompressedDiamond);
		GameRegistry.register(CompressedObsidian);
		GameRegistry.register(PortableTeleporter);
		GameRegistry.register(TeleportationCore);
		GameRegistry.register(Clump);
		GameRegistry.register(DirtyDust);
		GameRegistry.register(Configurator);
		GameRegistry.register(NetworkReader);
		GameRegistry.register(WalkieTalkie);
		GameRegistry.register(Jetpack);
		GameRegistry.register(Dictionary);
		GameRegistry.register(GasMask);
		GameRegistry.register(ScubaTank);
		GameRegistry.register(Balloon);
		GameRegistry.register(Shard);
		GameRegistry.register(ElectrolyticCore);
		GameRegistry.register(Sawdust);
		GameRegistry.register(Salt);
		GameRegistry.register(BrineBucket);
		GameRegistry.register(LithiumBucket);
		GameRegistry.register(HeavyWaterBucket);
		GameRegistry.register(Crystal);
		GameRegistry.register(FreeRunners);
		GameRegistry.register(ArmoredJetpack);
		GameRegistry.register(ConfigurationCard);
		GameRegistry.register(CraftingFormula);
		GameRegistry.register(SeismicReader);
		GameRegistry.register(Substrate);
		GameRegistry.register(Polyethene);
		GameRegistry.register(BioFuel);
		GameRegistry.register(GlowPanel);
		GameRegistry.register(Flamethrower);
		GameRegistry.register(GaugeDropper);
		GameRegistry.register(FactoryInstaller);
		GameRegistry.register(OtherDust);

		FluidContainerRegistry.registerFluidContainer(FluidRegistry.getFluid("brine"), new ItemStack(BrineBucket), FluidContainerRegistry.EMPTY_BUCKET);
		FluidContainerRegistry.registerFluidContainer(FluidRegistry.getFluid("lithium"), new ItemStack(LithiumBucket), FluidContainerRegistry.EMPTY_BUCKET);
		FluidContainerRegistry.registerFluidContainer(FluidRegistry.getFluid("heavywater"), new ItemStack(HeavyWaterBucket), FluidContainerRegistry.EMPTY_BUCKET);

		MinecraftForge.EVENT_BUS.register(GasMask);
		MinecraftForge.EVENT_BUS.register(FreeRunners);
		
		Mekanism.proxy.registerItemRenders();
	}
	
	public static Item init(Item item, String name)
	{
		return item.setUnlocalizedName(name).setRegistryName("mekanism:" + name);
	}
}
