package ic2.api.item;

import net.minecraft.item.ItemStack;

import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.ModContainer;

import ic2.api.info.Info;


/**
 * Provides access to IC2 blocks and items.
 *
 * Some items can be acquired through the ore dictionary which is the
 * recommended way. The items are initialized while IC2 is being loaded - try to
 * use ModsLoaded() or load your mod after IC2. Some blocks/items can be
 * disabled by a config setting, so it's recommended to check if they're null
 * first.
 *
 * Getting the associated Block/Item for an ItemStack x: Blocks:
 * ((ItemBlock)x.getItem).getBlock() Items: x.getItem()
 * Alternatively, you can directly call IC2Items.instance.getItem(name) or IC2Items.instance.getBlock(name)
 *
 * It is recommended, that you keep a reference to the Items you get here.
 *
 * @author Aroma1997
 */
public final class IC2Items {

	/*
	 * To find out the name and variant of an Item/ItemStack
	 * have that Item in your hand in-game and type "/ic2 itemNameWithVariant"
	 */

	/**
	 * Get an ItemStack for a specific item name.
	 *
	 * @param name
	 *            item name
	 * @param variant
	 *            the variant/subtype for the Item.
	 * @return The item or null if the item does not exist or an error occurred
	 */
	public static ItemStack getItem(String name, String variant) {
		if (instance == null)  {
			return null;
		}
		return instance.getItemStack(name, variant);
	}

	/**
	 * Get an ItemStack for a specific item name.
	 *
	 * @param name
	 *            item name
	 * @return The item or null if the item does not exist or an error occurred
	 */
	public static ItemStack getItem(String name) {
		return getItem(name, null);
	}

	/**
	 * Get the ItemAPI Instance.
	 * @return the ItemAPI instance.
	 */
	public static IItemAPI getItemAPI() {
		return instance;
	}


	private static IItemAPI instance;

	/**
	 * Sets the internal IItemAPI instance.
	 * ONLY IC2 CAN DO THIS!!!!!!!
	 */
	public static void setInstance(IItemAPI api) {
		ModContainer mc = Loader.instance().activeModContainer();

		if (mc == null || !Info.MOD_ID.equals(mc.getModId())) {
			throw new IllegalAccessError("invoked from "+mc);
		}

		instance = api;
	}

}

/*
=================================================
Info
=================================================

The different Items are separaed by an empty line.
The first entry is always the itemname followed by a ":"
If you only want to access the Item, you have to use getItemAPI().getItem() with that name.
If you only want to access the Block, you have to use getItemAPI().getBlock() with that name.
Then come the different variants as follows:
If the item has variants, it is displayed like that:
itemname,variant <TAB> Display name
Otherwise, it is displayed like that:
itemname <TAB> Display name
You can access ItemStacks of Items without variants using the variant null


=================================================
Items
=================================================

boat:
boat,broken_rubber	Damaged Rubber Dinghy
boat,rubber	Rubber Dinghy
boat,carbon	Carbon Fiber Canoe
boat,electric	Electric Boat

crushed:
crushed,copper	Crushed Copper Ore
crushed,gold	Crushed Gold Ore
crushed,iron	Crushed Iron Ore
crushed,lead	Crushed Lead Ore
crushed,silver	Crushed Silver Ore
crushed,tin	Crushed Tin Ore
crushed,uranium	Crushed Uranium Ore

purified:
purified,copper	Purified Crushed Copper Ore
purified,gold	Purified Crushed Gold Ore
purified,iron	Purified Crushed Iron Ore
purified,lead	Purified Crushed Lead Ore
purified,silver	Purified Crushed Silver Ore
purified,tin	Purified Crushed Tin Ore
purified,uranium	Purified Crushed Uranium Ore

dust:
dust,bronze	Bronze Dust
dust,clay	Clay Dust
dust,coal	Coal Dust
dust,coal_fuel	Hydrated Coal Dust
dust,copper	Copper Dust
dust,diamond	Diamond Dust
dust,energium	Energium Dust
dust,gold	Gold Dust
dust,iron	Iron Dust
dust,lapis	Lapis Lazuli Dust
dust,lead	Lead Dust
dust,lithium	Lithium Dust
dust,obsidian	Obsidian Dust
dust,silicon_dioxide	Silicon Dioxide
dust,silver	Silver Dust
dust,stone	Stone Dust
dust,sulfur	Sulfur Dust
dust,tin	Tin Dust
dust,small_bronze	Tiny Pile of Bronze Dust
dust,small_copper	Tiny Pile of Copper Dust
dust,small_gold	Tiny Pile of Gold Dust
dust,small_iron	Tiny Pile of Iron Dust
dust,small_lapis	Tiny Pile of Lapis Dust
dust,small_lead	Tiny Pile of Lead Dust
dust,small_lithium	Tiny Pile of Lithium Dust
dust,small_obsidian	Tiny Pile of Obsidian Dust
dust,small_silver	Tiny Pile of Silver Dust
dust,small_sulfur	Tiny Pile of Sulfur Dust
dust,small_tin	Tiny Pile of Tin Dust
dust,tin_hydrated	Hydrated Tin Dust

ingot:
ingot,alloy	Mixed Metal Ingot
ingot,bronze	Bronze Ingot
ingot,copper	Copper Ingot
ingot,lead	Lead Ingot
ingot,silver	Silver Ingot
ingot,steel	Steel Ingot
ingot,tin	Tin Ingot

plate:
plate,bronze	Bronze Plate
plate,copper	Copper Plate
plate,gold	Gold Plate
plate,iron	Iron Plate
plate,lapis	Lapis Lazuli Plate
plate,lead	Lead Plate
plate,obsidian	Obsidian Plate
plate,steel	Steel Plate
plate,tin	Tin Plate
plate,dense_bronze	Dense Bronze Plate
plate,dense_copper	Dense Copper Plate
plate,dense_gold	Dense Gold Plate
plate,dense_iron	Dense Iron Plate
plate,dense_lapis	Dense Lapis Lazuli Plate
plate,dense_lead	Dense Lead Plate
plate,dense_obsidian	Dense Obsidian Plate
plate,dense_steel	Dense Steel Plate
plate,dense_tin	Dense Tin Plate

casing:
casing,bronze	Bronze Item Casing
casing,copper	Copper Item Casing
casing,gold	Gold Item Casing
casing,iron	Iron Item Casing
casing,lead	Lead Item Casing
casing,steel	Steel Item Casing
casing,tin	Tin Item Casing

nuclear:
nuclear,uranium	Enriched Uranium Nuclear Fuel
nuclear,uranium_235	Uranium 235
nuclear,uranium_238	Uranium 238
nuclear,plutonium	Plutonium
nuclear,mox	MOX Nuclear Fuel
nuclear,small_uranium_235	Tiny Pile of Uranium 235
nuclear,small_uranium_238	Tiny Pile of Uranium 238
nuclear,small_plutonium	Tiny Pile of Plutonium
nuclear,uranium_pellet	Pellets of Enriched Uranium Nuclear Fuel
nuclear,mox_pellet	Pellets of MOX Nuclear Fuel
nuclear,rtg_pellet	Pellets of RTG Fuel
nuclear,depleted_uranium	Fuel Rod (Depleted Uranium)
nuclear,depleted_dual_uranium	Dual Fuel Rod (Depleted Uranium)
nuclear,depleted_quad_uranium	Quad Fuel Rod (Depleted Uranium)
nuclear,depleted_mox	Fuel Rod (Depleted MOX)
nuclear,depleted_dual_mox	Dual Fuel Rod (Depleted MOX)
nuclear,depleted_quad_mox	Quad Fuel Rod (Depleted MOX)

misc_resource:
misc_resource,ashes	Ashes
misc_resource,iridium_ore	Iridium Ore
misc_resource,iridium_shard	Iridium Shard
misc_resource,matter	UU-Matter
misc_resource,resin	Sticky Resin
misc_resource,slag	Slag
misc_resource,iodine	Iodine

block_cutting_blade:
block_cutting_blade,iron	Block Cutting Blade (Iron)
block_cutting_blade,steel	Block Cutting Blade (Steel)
block_cutting_blade,diamond	Block Cutting Blade (Diamond)

crafting:
crafting,rubber	Rubber
crafting,circuit	Electronic Circuit
crafting,advanced_circuit	Advanced Circuit
crafting,alloy	Advanced Alloy
crafting,iridium	Iridium Reinforced Plate
crafting,coil	Coil
crafting,electric_motor	Electric Motor
crafting,heat_conductor	Heat Conductor
crafting,copper_boiler	Copper Boiler
crafting,fuel_rod	Fuel Rod (Empty)
crafting,tin_can	Tin Can
crafting,small_power_unit	Small Power Unit
crafting,power_unit	Power Unit
crafting,carbon_fibre	Raw Carbon Fibre
crafting,carbon_mesh	Raw Carbon Mesh
crafting,carbon_plate	Carbon Plate
crafting,coal_ball	Coal Ball
crafting,coal_block	Compressed Coal Ball
crafting,coal_chunk	Coal Chunk
crafting,industrial_diamond	Industrial Diamond
crafting,plant_ball	Plantball
crafting,bio_chaff	Bio Chaff
crafting,compressed_hydrated_coal	H. Coal
crafting,scrap	Scrap
crafting,scrap_box	Scrap Box
crafting,cf_powder	CF Powder
crafting,pellet	CF Pellet
crafting,raw_crystal_memory	Crystal Memory (raw)
crafting,iron_shaft	Shaft (Iron)
crafting,steel_shaft	Shaft (Steel)
crafting,wood_rotor_blade	Wood Rotor Blade
crafting,iron_rotor_blade	Iron Rotor Blade
crafting,steel_rotor_blade	Steel Rotor Blade
crafting,carbon_rotor_blade	Carbon Rotor Blade
crafting,steam_turbine_blade	Steam Turbine Blade
crafting,steam_turbine	Steam Turbine
crafting,jetpack_attachment_plate	Jetpack Attachment Plate
crafting,coin	Industrial Credit

crystal_memory:
crystal_memory	Crystal Memory

upgrade_kit:
upgrade_kit,mfsu	MFSU Upgrade Kit

crop_res:
crop_res,coffee_beans	Coffee Beans
crop_res,coffee_powder	Coffee Powder
crop_res,fertilizer	Fertilizer
crop_res,grin_powder	Grin Powder
crop_res,hops	Hops
crop_res,weed	Weed

terra_wart:
terra_wart	Terra Wart

re_battery:
re_battery	RE-Battery

advanced_re_battery:
advanced_re_battery	Advanced RE-Battery

energy_crystal:
energy_crystal	Energy Crystal

lapotron_crystal:
lapotron_crystal	Lapotron Crystal

single_use_battery:
single_use_battery	Single-Use Battery

charging_re_battery:
charging_re_battery	Charging RE Battery

advanced_charging_re_battery:
advanced_charging_re_battery	Advanced Charging Battery

charging_energy_crystal:
charging_energy_crystal	Charging Energy Crystal

charging_lapotron_crystal:
charging_lapotron_crystal	Charging Lapotron Crystal

heat_storage:
heat_storage	10k Coolant Cell

tri_heat_storage:
tri_heat_storage	30k Coolant Cell

hex_heat_storage:
hex_heat_storage	60k Coolant Cell

plating:
plating	Reactor Plating

heat_plating:
heat_plating	Heat-Capacity Reactor Plating

containment_plating:
containment_plating	Containment Reactor Plating

heat_exchanger:
heat_exchanger	Heat Exchanger

reactor_heat_exchanger:
reactor_heat_exchanger	Reactor Heat Exchanger

component_heat_exchanger:
component_heat_exchanger	Component Heat Exchanger

advanced_heat_exchanger:
advanced_heat_exchanger	Advanced Heat Exchanger

heat_vent:
heat_vent	Heat Vent

reactor_heat_vent:
reactor_heat_vent	Reactor Heat Vent

overclocked_heat_vent:
overclocked_heat_vent	Overclocked Heat Vent

component_heat_vent:
component_heat_vent	Component Heat Vent

advanced_heat_vent:
advanced_heat_vent	Advanced Heat Vent

neutron_reflector:
neutron_reflector	Neutron Reflector

thick_neutron_reflector:
thick_neutron_reflector	Thick Neutron Reflector

iridium_reflector:
iridium_reflector	Iridium Neutron Reflector

rsh_condensator:
rsh_condensator	RSH-Condensator

lzh_condensator:
lzh_condensator	LZH-Condensator

uranium_fuel_rod:
uranium_fuel_rod	Fuel Rod (Uranium)

dual_uranium_fuel_rod:
dual_uranium_fuel_rod	Dual Fuel Rod (Uranium)

quad_uranium_fuel_rod:
quad_uranium_fuel_rod	Quad Fuel Rod (Uranium)

mox_fuel_rod:
mox_fuel_rod	Fuel Rod (MOX)

dual_mox_fuel_rod:
dual_mox_fuel_rod	Dual Fuel Rod (MOX)

quad_mox_fuel_rod:
quad_mox_fuel_rod	Quad Fuel Rod (MOX)

lithium_fuel_rod:
lithium_fuel_rod	Fuel Rod (Lithium)

tfbp:
tfbp,blank	TFBP - Empty
tfbp,chilling	TFBP - Chilling
tfbp,cultivation	TFBP - Cultivation
tfbp,desertification	TFBP - Desertification
tfbp,flatification	TFBP - Flatification
tfbp,irrigation	TFBP - Irrigation
tfbp,mushroom	TFBP - Mushroom

bronze_axe:
bronze_axe	Bronze Axe

bronze_hoe:
bronze_hoe	Bronze Hoe

bronze_pickaxe:
bronze_pickaxe	Bronze Pickaxe

bronze_shovel:
bronze_shovel	Bronze Shovel

bronze_sword:
bronze_sword	Bronze Sword

containment_box:
containment_box	Containment Box

cutter:
cutter	Cutter

debug_item:
debug_item	Debug Item

foam_sprayer:
foam_sprayer	CF Sprayer
foam_sprayer,ic2construction_foam	CF Sprayer

forge_hammer:
forge_hammer	Forge Hammer

frequency_transmitter:
frequency_transmitter	Frequency Transmitter

meter:
meter	EU-Reader

remote:
remote	Dynamite-O-Mote

tool_box:
tool_box	Tool Box

treetap:
treetap	Treetap

wrench:
wrench	Wrench

barrel:
barrel	Empty Booze Barrel

booze_mug:
booze_mug	Zero

mug:
mug,empty	Stone Mug
mug,cold_coffee	Cold Coffee
mug,dark_coffee	Dark Coffee
mug,coffee	Coffee

crop_stick:
crop_stick	Crop

cropnalyzer:
cropnalyzer	Cropnalyzer

crop_seed_bag:
crop_seed_bag	* Seeds

weeding_trowel:
weeding_trowel	Weeding Trowel

advanced_scanner:
advanced_scanner	OV Scanner

chainsaw:
chainsaw	Chainsaw

diamond_drill:
diamond_drill	Diamond Drill

drill:
drill	Mining Drill

electric_hoe:
electric_hoe	Electric Hoe

electric_treetap:
electric_treetap	Electric Treetap

electric_wrench:
electric_wrench	Electric Wrench

iridium_drill:
iridium_drill	Iridium Drill

mining_laser:
mining_laser	Mining Laser

nano_saber:
nano_saber	Nano Saber

obscurator:
obscurator	Obscurator

scanner:
scanner	OD Scanner

wind_meter:
wind_meter	Windmeter

painter:
painter	Painter
painter,black	Black Painter
painter,blue	Blue Painter
painter,brown	Brown Painter
painter,cyan	Cyan Painter
painter,gray	Dark Grey Painter
painter,green	Green Painter
painter,light_blue	Light Blue Painter
painter,light_gray	Light Grey Painter
painter,lime	Lime Painter
painter,magenta	Magenta Painter
painter,orange	Orange Painter
painter,pink	Pink Painter
painter,purple	Purple Painter
painter,red	Red Painter
painter,white	White Painter
painter,yellow	Yellow Painter

fluid_cell:
fluid_cell	Universal Fluid Cell
fluid_cell,ic2air	Universal Fluid Cell
fluid_cell,ic2biogas	Universal Fluid Cell
fluid_cell,ic2biomass	Universal Fluid Cell
fluid_cell,ic2construction_foam	Universal Fluid Cell
fluid_cell,ic2coolant	Universal Fluid Cell
fluid_cell,ic2distilled_water	Universal Fluid Cell
fluid_cell,ic2heavy_water	Universal Fluid Cell
fluid_cell,ic2hot_coolant	Universal Fluid Cell
fluid_cell,ic2hot_water	Universal Fluid Cell
fluid_cell,ic2hydrogen	Universal Fluid Cell
fluid_cell,ic2oxygen	Universal Fluid Cell
fluid_cell,ic2pahoehoe_lava	Universal Fluid Cell
fluid_cell,ic2steam	Universal Fluid Cell
fluid_cell,ic2superheated_steam	Universal Fluid Cell
fluid_cell,ic2uu_matter	Universal Fluid Cell
fluid_cell,ic2weed_ex	Universal Fluid Cell
fluid_cell,lava	Universal Fluid Cell
fluid_cell,water	Universal Fluid Cell

cable:
cable,type:copper,insulation:0	Copper Cable
cable,type:copper,insulation:1	Insulated Copper Cable
cable,type:glass,insulation:0	Glass Fibre Cable
cable,type:gold,insulation:0	Gold Cable
cable,type:gold,insulation:1	Insulated Gold Cable
cable,type:gold,insulation:2	2x Ins. Gold Cable
cable,type:iron,insulation:0	HV Cable
cable,type:iron,insulation:1	Insulated HV Cable
cable,type:iron,insulation:2	2x Ins. HV Cable
cable,type:iron,insulation:3	3x Ins. HV Cable
cable,type:tin,insulation:0	Tin Cable
cable,type:tin,insulation:1	Insulated Tin Cable
cable,type:detector,insulation:0	EU-Detector Cable
cable,type:splitter,insulation:0	EU-Splitter Cable

upgrade:
upgrade,overclocker	Overclocker Upgrade
upgrade,transformer	Transformer Upgrade
upgrade,energy_storage	Energy Storage Upgrade
upgrade,redstone_inverter	Redstone Signal Inverter Upgrade
upgrade,ejector	Ejector Upgrade
upgrade,advanced_ejector	Advanced Ejector Upgrade
upgrade,pulling	Pulling Upgrade
upgrade,advanced_pulling	Advanced Pulling Upgrade
upgrade,fluid_ejector	Fluid Ejector Upgrade
upgrade,fluid_pulling	Fluid Pulling Upgrade

advanced_batpack:
advanced_batpack	Adv.Batpack

alloy_chestplate:
alloy_chestplate	Composite Vest

batpack:
batpack	BatPack

bronze_boots:
bronze_boots	Bronze Boots

bronze_chestplate:
bronze_chestplate	Bronze Chestplate

bronze_helmet:
bronze_helmet	Bronze Helmet

bronze_leggings:
bronze_leggings	Bronze Leggings

cf_pack:
cf_pack	CF Backpack
cf_pack	CF Backpack

energy_pack:
energy_pack	Energypack

hazmat_chestplate:
hazmat_chestplate	Hazmat Suit

hazmat_helmet:
hazmat_helmet	Scuba Helmet

hazmat_leggings:
hazmat_leggings	Hazmat Suit Leggings

jetpack:
jetpack	Jetpack
jetpack	Jetpack

jetpack_electric:
jetpack_electric	Electric Jetpack

lappack:
lappack	Lappack

nano_boots:
nano_boots	NanoSuit Boots

nano_chestplate:
nano_chestplate	NanoSuit Bodyarmor

nano_helmet:
nano_helmet	NanoSuit Helmet

nano_leggings:
nano_leggings	NanoSuit Leggings

nightvision_goggles:
nightvision_goggles	Nightvision Goggles

quantum_boots:
quantum_boots	QuantumSuit Boots

quantum_chestplate:
quantum_chestplate	QuantumSuit Bodyarmor

quantum_helmet:
quantum_helmet	QuantumSuit Helmet

quantum_leggings:
quantum_leggings	QuantumSuit Leggings

rubber_boots:
rubber_boots	Rubber Boots

solar_helmet:
solar_helmet	Solar Helmet

static_boots:
static_boots	Static Boots

filled_tin_can:
filled_tin_can	(Filled) Tin Can

iodine_tablet:
iodine_tablet	Iodine Tablet

rotor_wood:
rotor_wood	Kinetic Gearbox Rotor (Wood)

rotor_iron:
rotor_iron	Kinetic Gearbox Rotor (Iron)

rotor_carbon:
rotor_carbon	Kinetic Gearbox Rotor (Carbon)

rotor_steel:
rotor_steel	Kinetic Gearbox Rotor (Steel)

dynamite:
dynamite	Dynamite

dynamite_sticky:
dynamite_sticky	Sticky Dynamite


=================================================
Blocks:
=================================================

te:
te,itnt	Industrial TNT
te,nuke	Nuke
te,generator	Generator
te,geo_generator	Geothermal Generator
te,kinetic_generator	Kinetic Generator
te,rt_generator	Radioisotope Thermoelectric Generator
te,semifluid_generator	Semifluid Generator
te,solar_generator	Solar Panel
te,stirling_generator	Stirling Generator
te,water_generator	Water Mill
te,wind_generator	Wind Mill
te,electric_heat_generator	Electric Heater
te,fluid_heat_generator	Liquid Fuel Firebox
te,rt_heat_generator	Radioisotope Heat Generator
te,solid_heat_generator	Solid Fuel Firebox
te,electric_kinetic_generator	Electric Motor
te,manual_kinetic_generator	Manual Kinetic Generator
te,steam_kinetic_generator	Steam Turbine
te,stirling_kinetic_generator	Stirling Kinetic Generator
te,water_kinetic_generator	Water Turbine
te,wind_kinetic_generator	Wind Turbine
te,nuclear_reactor	Nuclear Reactor
te,reactor_access_hatch	Reactor Access Hatch
te,reactor_chamber	Reactor Chamber
te,reactor_fluid_port	Reactor Fluid Port
te,reactor_redstone_port	Reactor Redstone Port
te,condenser	Condenser
te,fluid_bottler	Bottling Plant
te,fluid_distributor	Fluid Distributor
te,fluid_regulator	Fluid Regulator
te,liquid_heat_exchanger	Liquid Heat Exchanger
te,pump	Pump
te,solar_distiller	Solar Distiller
te,steam_generator	Steam Boiler
te,item_buffer	Item Buffer
te,luminator_flat	Luminator
te,magnetizer	Magnetizer
te,sorting_machine	Electric Sorting Machine
te,teleporter	Teleporter
te,terraformer	Terraformer
te,tesla_coil	Tesla Coil
te,canner	Fluid/Solid Canning Machine
te,compressor	Compressor
te,electric_furnace	Electric Furnace
te,extractor	Extractor
te,iron_furnace	Iron Furnace
te,macerator	Macerator
te,recycler	Recycler
te,solid_canner	Solid Canning Machine
te,blast_furnace	Blast Furnace
te,block_cutter	Block Cutting Machine
te,centrifuge	Thermal Centrifuge
te,fermenter	Fermenter
te,induction_furnace	Induction Furnace
te,metal_former	Metal Former
te,ore_washing_plant	Ore Washing Plant
te,advanced_miner	Advanced Miner
te,crop_harvester	Crop Harvester
te,cropmatron	Crop-Matron
te,miner	Miner
te,matter_generator	Mass Fabricator
te,pattern_storage	Pattern Storage
te,replicator	Replicator
te,scanner	Scanner
te,energy_o_mat	Energy-O-Mat
te,personal_chest	Personal Safe
te,trade_o_mat	Trade-O-Mat
te,chargepad_batbox	Charge Pad (BatBox)
te,chargepad_cesu	Charge Pad (CESU)
te,chargepad_mfe	Charge Pad (MFE)
te,chargepad_mfsu	Charge Pad (MFSU)
te,batbox	BatBox
te,cesu	CESU
te,mfe	MFE
te,mfsu	MFSU
te,electrolyzer	Electrolyzer
te,lv_transformer	LV-Transformer
te,mv_transformer	MV-Transformer
te,hv_transformer	HV-Transformer
te,ev_transformer	EV-Transformer
te,tank	Tank
te,chunk_loader	Chunk Loader
te,item_buffer_2	Compact Item Buffer
te,rci_rsh	Reactor Coolant Injector (RSH)
te,rci_lzh	Reactor Coolant Injector (LZH)
te,creative_generator	Creative Generator
te,steam_repressurizer	Steam Re-Pressurizer
te,industrial_workbench	Industrial Workbench
te,batch_crafter	Batch Crafter

resource:
resource,basalt	Basalt
resource,copper_ore	Copper Ore
resource,lead_ore	Lead Ore
resource,tin_ore	Tin Ore
resource,uranium_ore	Uranium Ore
resource,bronze_block	Bronze Block
resource,copper_block	Copper Block
resource,lead_block	Lead Block
resource,steel_block	Steel Block
resource,tin_block	Tin Block
resource,uranium_block	Uranium Block
resource,reinforced_stone	Reinforced Stone
resource,machine	Basic Machine Casing
resource,advanced_machine	Advanced Machine Casing
resource,reactor_vessel	Reactor Pressure Vessel

leaves:
leaves	Rubber Tree Leaves

rubber_wood:
rubber_wood	Rubber Wood

sapling:
sapling	Rubber Tree Sapling

scaffold:
scaffold,wood	Scaffold
scaffold,reinforced_wood	Reinforced Scaffold
scaffold,iron	Iron Scaffold
scaffold,reinforced_iron	Reinforced Iron Scaffold

foam:
foam,normal	Construction Foam
foam,reinforced	Reinforced Construction Foam

fence:
fence,iron	Iron Fence

sheet:
sheet,resin	Resin Sheet
sheet,rubber	Rubber Sheet
sheet,wool	Wool Sheet

glass:
glass,reinforced	Reinforced Glass

wall:
wall,black	Construction Foam Wall (Black)
wall,blue	Construction Foam Wall (Blue)
wall,brown	Construction Foam Wall (Brown)
wall,cyan	Construction Foam Wall (Cyan)
wall,gray	Construction Foam Wall (Gray)
wall,green	Construction Foam Wall (Green)
wall,light_blue	Construction Foam Wall (Light Blue)
wall,light_gray	Construction Foam Wall (Light Gray)
wall,lime	Construction Foam Wall (Lime)
wall,magenta	Construction Foam Wall (Magenta)
wall,orange	Construction Foam Wall (Orange)
wall,pink	Construction Foam Wall (Pink)
wall,purple	Construction Foam Wall (Purple)
wall,red	Construction Foam Wall (Red)
wall,white	Construction Foam Wall (White)
wall,yellow	Construction Foam Wall (Yellow)

mining_pipe:
mining_pipe,pipe	Mining Pipe

reinforced_door:
reinforced_door	Reinforced Door

 */
