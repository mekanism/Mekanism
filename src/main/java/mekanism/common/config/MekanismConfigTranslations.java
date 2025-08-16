package mekanism.common.config;

import java.util.Locale;
import java.util.Objects;
import java.util.function.UnaryOperator;
import java.util.stream.Stream;
import mekanism.api.tier.ITier;
import mekanism.common.Mekanism;
import mekanism.common.tier.BinTier;
import mekanism.common.tier.CableTier;
import mekanism.common.tier.ChemicalTankTier;
import mekanism.common.tier.ConductorTier;
import mekanism.common.tier.EnergyCubeTier;
import mekanism.common.tier.FluidTankTier;
import mekanism.common.tier.InductionCellTier;
import mekanism.common.tier.InductionProviderTier;
import mekanism.common.tier.PipeTier;
import mekanism.common.tier.TransporterTier;
import mekanism.common.tier.TubeTier;
import mekanism.common.util.text.TextUtils;
import net.minecraft.Util;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public enum MekanismConfigTranslations implements IConfigTranslation {
    //Client config
    CLIENT_ACCESSIBILITY("client.accessibility", "Accessibility Settings",
          "Settings for configuring Accessibility settings provided by Mekanism. Note: Some settings such as HUD scale, "
          + "may be located throughout the rest of this config.", true),
    CLIENT_WHITE_RADIAL_TEXT("client.accessibility.white_radial_text", "White Radial Text",
          "Tries to force all text rendered as part of radial menus to be white instead of the color of the slice's mode."),
    CLIENT_ENERGY_COLOR("client.accessibility.energy.color", "Energy Color", "The color of energy when displayed as the durability bar on items."),
    CLIENT_SCROLL_MODE_CHANGE("client.accessibility.mode_change.scroll", "Scroll Mode Change", "Allows changing the mode of held items by holding sneak and using the scroll wheel."),

    CLIENT_SOUNDS("client.sounds", "Sound Settings", "Settings for configuring Mekanism's Sounds", true),
    CLIENT_BASE_SOUND_VOLUME("client.sounds.base_volume", "Base Sound Volume", "Adjust Mekanism sounds' base volume. < 1 is softer, higher is louder."),
    CLIENT_PLAYER_SOUNDS_ENABLED("client.sounds.player.enable", "Enable Player Sounds",
          "Play sounds for when any player is using a Jetpack, Scuba Mask, Flamethrower, or Gravitational Modulating Unit. This also affects playing the Radiation clicking sound."),
    CLIENT_MACHINE_SOUNDS_ENABLED("client.sounds.machine.enable", "Enable Machine Sounds", "Play sounds for active machines."),

    CLIENT_RENDERING("client.rendering", "Rendering Settings", "Settings for adjusting how Mekanism renders certain blocks and how many particles Mekanism adds.", true),
    CLIENT_OPAQUE_TRANSMITTERS("client.transmitters.opaque", "Opaque Transmitters",
          "If true, renders sides for Universal Cables, Mechanical Pipes, Pressurized Tubes, Logistical Transporters, and Thermodynamic Conductors instead of "
          + "rendering their contents. If transmitters are causing you fps issues, this should hopefully help."),
    CLIENT_BE_RENDER_RANGE("client.render_range.block_entity", "BE Render Range",
          "Range at which Block Entity Renderers added by Mekanism can render at, for example the contents of multiblocks. Vanilla defaults the rendering range for "
          + "BERs to 64 for most blocks (for example chests), but uses a range of 256 for blocks like beacons and end gateways. Lowering this will likely increase "
          + "your performance, at the cost of multiblock contents and wind generators 'popping' in"),

    CLIENT_PARTICLE("client.particle", "Particle Settings", "Settings for configuring Mekanism's Particles", true),
    CLIENT_PARTICLE_MULTIBLOCK_FORMATION("client.particle.multiblock_formation", "Multiblock Formation",
          "When multiblock forms, represent that by red sparkles around the multiblock. If this is disabled a notification message will display on the action bar instead."),
    CLIENT_PARTICLE_MACHINE_EFFECTS("client.particle.machine_effects", "Machine Effects", "Show particles when machines active."),
    CLIENT_PARTICLE_RADIATION_RADIUS("client.particle.radiation.radius", "Radiation Particle Radius",
          "How far (in blocks) from the player radiation particles can spawn."),
    CLIENT_PARTICLE_RADIATION_COUNT("client.particle.radiation.count", "Radiation Particle Count",
          "How many particles spawn when rendering radiation effects (scaled by radiation level)."),
    CLIENT_PARTICLE_MAGNETIC_ATTRACTION("client.particle.magnetic_attraction", "Magnetic Attraction",
          "Show bolts between the player and items when the Magnetic Attraction Unit is pulling items towards a player."),
    CLIENT_PARTICLE_TOOL_AOE("client.particle.tool_aoe", "Tool AOE", "Show bolts for various AOE tool behaviors such as tilling, debarking, and vein mining."),

    CLIENT_HUD("client.hud", "HUD Settings", "Settings for configuring Mekanism's HUD", true),
    CLIENT_HUD_ENABLED("client.hud.enabled", "Enabled",
          "Enable a HUD that displays information about equipped Mekanism items, and displays additional information when wearing a MekaSuit Helmet."),
    CLIENT_HUD_SCALE("client.hud.scale", "Scale", "Scale of the text displayed on the HUD."),
    CLIENT_HUD_REVERSE("client.hud.reverse", "Reverse",
          "Reverses the HUD's text alignment and compass rendering to the right side of the screen, and moves the MekaSuit module rendering to the left side. "
          + "Warning: This may cause subtitles to overlap parts of the HUD such as the compass."),
    CLIENT_HUD_OPACITY("client.hud.opacity", "Opacity", "Opacity of HUD used by MekaSuit."),
    CLIENT_HUD_COLOR("client.hud.color", "Color", "Color (RGB) of HUD used by MekaSuit."),
    CLIENT_HUD_COLOR_WARNING("client.hud.color.warning", "Warning Color", "Color (RGB) of warning HUD elements used by MekaSuit."),
    CLIENT_HUD_COLOR_DANGER("client.hud.color.danger", "Danger Color", "Color (RGB) of danger HUD elements used by MekaSuit."),
    CLIENT_HUD_JITTER("client.hud.jitter", "Jitter", "Visual jitter of the MekaSuit HUD, seen when moving the player's head. Higher values increases the "
                                                     + "amount of jitter."),
    CLIENT_HUD_COMPASS("client.hud.compass", "Compass", "Display a fancy compass when the MekaSuit Helmet is worn."),
    CLIENT_HUD_SUBTITLE_AVOID("client.hud.avoid_subtitles", "Avoid Subtitle Overlap", "Shift the HUD elements when sound subtitles are being displayed to avoid overlap"),
    CLIENT_HUD_MEKASUIT_ENERGY_BAR("client.hud.mekasuit.energy_bar", "MekaSuit Energy Bar",
          "Enables rendering an energy bar above the armor bar that displays the total energy of the currently equipped MekaSuit."),

    CLIENT_QIO("client.qio", "QIO Settings", "Settings for configuring Mekanism's QIO", true),
    CLIENT_QIO_SORT_TYPE("client.qio.sort.type", "Sort Type", "Sorting strategy when viewing items in a QIO Dashboard."),
    CLIENT_QIO_SORT_DIRECTION("client.qio.sort.direction", "Sort Direction", "Sorting direction when viewing items in a QIO Dashboard."),
    CLIENT_QIO_SLOTS_X("client.qio.slots.x", "Slots Wide", "Number of slots to view horizontally on a QIO Dashboard."),
    CLIENT_QIO_SLOTS_Y("client.qio.slots.y", "Slots Tall", "Number of slots to view vertically on a QIO Dashboard."),
    CLIENT_QIO_AUTO_FOCUS("client.qio.auto_focus", "Auto-Focus", "Determines whether the search bar is automatically focused when a QIO Dashboard is opened."),
    CLIENT_QIO_REJECTS_DESTINATION("client.qio.rejects.destination", "Transfer Rejects To Inventory",
          "Determines if items in a QIO crafting window should be moved to the player's inventory or the frequency first when replacing the items in the "
          + "crafting grid using a recipe viewer."),

    CLIENT_LAST_WINDOW_POSITIONS("client.last_window_positions", "Last Window Positions",
          "Stores the last position various windows were in when they were closed, and whether they are pinned. In general these values should not be modified manually.",
          "Edit Window Positions"),
    CLIENT_LAST_WINDOW_POSITIONS_X("client.last_window_positions.x", "X Component", "The x component of this window's last position."),
    CLIENT_LAST_WINDOW_POSITIONS_Y("client.last_window_positions.y", "Y Component", "The y component of this window's last position."),
    CLIENT_LAST_WINDOW_POSITIONS_PINNED("client.last_window_positions.pinned", "Pinned",
          "Determines whether this window is pinned, and should open automatically when the GUI is reopened."),

    //Common Config
    COMMON_UNIT_ENERGY("common.unit.energy", "Energy Unit", "Displayed energy type in Mekanism GUIs (client) and network reader readings (server)."),
    COMMON_UNIT_TEMPERATURE("common.unit.temperature", "Temperature Unit", "Displayed temperature unit in Mekanism GUIs (client) and network reader readings (server)."),
    COMMON_DECAY_TIMERS("common.decay_timers", "Decay Timers",
          "Display the time it will take for radiation to decay when readings are above safe levels. Set this to false on the client side to disable "
          + "MekaSuit Geiger and Dosimeter Unit timers. Set this to false on the server side to disable handheld Geiger Counter and Dosimeter timers."),
    COMMON_COPY_BLOCK_DATA("common.copy_block_data", "Copy Block Data",
          "Determines whether machine configuration data is copied when using middle click. If this is set to false no data will be copied and the default instance "
          + "of the stack will be returned."),
    COMMON_HOLIDAYS("common.holidays", "Holidays",
          "Should holiday greetings and easter eggs play for holidays (ex: Christmas and New Years) on the client. Also controls whether Robits with "
          + "the default skin should have their skin randomized on the server."),

    //General Config
    GENERAL_ALPHA_WARNING("general.misc.alpha_warning", "Alpha Warning",
          "Display Mekanism's alpha warning when joining the game if Mekanism is currently in alpha. Dev mode only setting."),
    GENERAL_PACKET_LOGGING("general.misc.packet_logging", "Debug Packet Logging", "Log Mekanism packet names. Debug setting."),
    GENERAL_CHUNKLOADING("general.misc.chunkloading", "Allow Chunkloading",
          "Controls whether Mekanism allows players to load chunks. If this is disabled, neither the Anchor Upgrade nor the Dimensional Stabilizer will provide any functionality."),
    GENERAL_DEACTIVATION_DELAY("general.misc.deactivation_delay.block", "Block Deactivation Delay",
          "How many ticks must pass until a block's active state is synced with the client when the block stops being active. This prevents rapid state changes "
          + "that can cause lag. Note: Mekanism's blocks always sync immediately when they go from inactive to active; this only controls the time it takes to go from "
          + "active to inactive."),
    GENERAL_AESTHETIC_DAMAGE("general.misc.aesthetic_damage", "Aesthetic World Damage", "If enabled, lasers can break blocks and the flamethrower can start fires."),
    GENERAL_ALLOY_UPGRADING("general.misc.alloy_upgrading", "Alloy Transmitter Upgrading",
          "Allow upgrading Universal Cables, Mechanical Pipes, Pressurized Tubes, Logistical Transporters, and Thermodynamic Conductors by right clicking them "
          + "with the next tier of alloy."),
    GENERAL_UPGRADE_MULTIPLIER("general.misc.upgrade_multiplier", "Max Upgrade Multiplier",
          "Base factor for working out machine performance with upgrades - UpgradeModifier * (UpgradesInstalled/UpgradesPossible)."),
    GENERAL_RATE_SNA("general.misc.rate.sna", "Peak SNA Processing Rate",
          "Peak processing rate in mB/t for the Solar Neutron Activator. Note: It can go higher than this value in some extreme environments."),

    GENERAL_HEATER("general.heater", "Heater Settings", "Settings for configuring heaters", true),
    GENERAL_FUELWOOD_HEAT("general.heater.fuelwood.heat", "Fuelwood Heat per Tick", "Amount of heat produced per fuel tick of a fuel's burn time in the Fuelwood Heater."),
    GENERAL_FUELWOOD_DURATION("general.heater.fuelwood.duration", "Fuelwood Burn Duration",
          "Number of ticks to burn an item at in a Fuelwood Heater. Use this config option to effectively make Fuelwood Heaters burn faster but produce the same amount of heat per item."),
    GENERAL_RESISTIVE_EFFICIENCY("general.heater.resistive.efficiency", "Resistive Heater Efficiency",
          "How much heat energy is created from one Joule of regular energy in the Resistive Heater."),

    GENERAL_FILL_RATE("general.fill_rate", "Item Fill Rate", "Settings for configuring the fill rates of tanks that are stored on items.", true),
    GENERAL_FILL_RATE_FLUID("general.fill_rate.fluid", "Fluid Item Fill Rate", "Rate in mB/t at which generic fluid storage items can be filled or emptied."),
    GENERAL_FILL_RATE_CHEMICAL("general.fill_rate.chemical", "Chemical Item Fill Rate", "Rate in mB/t at which generic chemical storage items can be filled or emptied."),

    GENERAL_CARDBOARD("general.cardboard", "Cardboard Box", "Settings for configuring Cardboard Boxes", true),
    GENERAL_CARDBOARD_STRICT_UNBOXING("general.cardboard.strict_unboxing", "Strict Unboxing",
          "Enable this to disable unboxing any block that has a fluid that would be vaporized on placement, instead of trying to vaporize it and leave the "
          + "remainder of the block. For example with this disabled trying to unbox a waterlogged slab in the nether will leave just the slab, but with this enabled "
          + "the cardboard box won't open."),
    GENERAL_CARDBOARD_MOD_BLACKLIST("general.cardboard.mod_blacklist", "Cardboard Box Mod Blacklist",
          "Any modids added to this list will not be able to have any of their blocks, picked up by the cardboard box. For example: [\"" + Mekanism.MODID + "\"]",
          "Edit Blacklist"),

    GENERAL_DYNAMIC_TANK("general.dynamic_tank", "Dynamic Tank Settings", "Settings for configuring Dynamic Tanks", true),
    GENERAL_DYNAMIC_TANK_FLUID_CAPACITY("general.dynamic_tank.capacity.fluid", "Fluid Capacity Per Block",
          "Amount of fluid (mB) that each block of the dynamic tank contributes to the volume. Total Fluid Capacity = volume * fluidPerTank"),
    GENERAL_DYNAMIC_TANK_CHEMICAL_CAPACITY("general.dynamic_tank.capacity.chemical", "Chemical Capacity Per Block",
          "Amount of chemical (mB) that each block of the dynamic tank contributes to the volume. Total Chemical Capacity = volume * chemicalPerTank"),

    GENERAL_AUTO_EJECT("general.auto_eject", "Auto Eject Settings", "Settings for configuring Auto Eject from block entities", "Edit Ejection Settings"),
    GENERAL_AUTO_EJECT_RATE_FLUID("general.auto_eject.rate.fluid", "Fluid Rate", "Rate in mB/t at which fluid gets auto ejected from block entities."),
    GENERAL_AUTO_EJECT_RATE_CHEMICAL("general.auto_eject.rate.chemical", "Chemical Rate", "Rate in mB/t at which chemicals gets auto ejected from block entities."),
    GENERAL_AUTO_EJECT_EXCESS("general.auto_eject.excess", "Excess Percentage", "The percentage of a tank's capacity to leave contents in when set to dumping excess."),

    GENERAL_PREFILLED_TANKS("general.prefilled_tanks", "Prefilled Tanks", "Settings for configuring Prefilled Tanks", true),
    GENERAL_PREFILLED_TANKS_FLUID("general.prefilled_tanks.fluid", "Fluid Tanks",
          "Add filled variants of creative fluid tanks to creative/recipe viewers for all registered fluids."),
    GENERAL_PREFILLED_TANKS_CHEMICAL("general.prefilled_tanks.chemical", "Chemical Tanks",
          "Add filled variants of creative chemical tanks to creative/recipe viewers for all registered chemicals. Note: This includes radioactive ones that "
          + "normally can't be stored in chemical tanks."),

    GENERAL_ENERGY_CONVERSION("general.energy_conversion", "Energy Conversion Rate", "Settings for configuring Energy Conversions", "Edit Conversion Rates"),
    GENERAL_ENERGY_CONVERSION_BLACKLIST_FE("general.energy_conversion.blacklist.neoforge", "Blacklist Forge Energy", "Disables Forge Energy (FE/RF) power integration."),
    GENERAL_ENERGY_CONVERSION_BLACKLIST_FN("general.energy_conversion.blacklist.fluxnetworks", "Blacklist Flux Networks",
          "Disables Flux Networks' higher throughput Forge Energy (FE/RF) power integration. Note: Blacklisting Forge Energy also disables this."),
    GENERAL_ENERGY_CONVERSION_BLACKLIST_GP("general.energy_conversion.blacklist.grandpower", "Blacklist GrandPower",
          "Disables Grand Power's higher throughput Forge Energy (FE/RF) power integration. Note: Blacklisting Forge Energy also disables this."),
    GENERAL_ENERGY_CONVERSION_FE("general.energy_conversion.forge_energy", "FE Conversion Rate", "Conversion multiplier from Forge Energy to Joules (FE * feConversionRate = Joules)"),
    GENERAL_ENERGY_CONVERSION_STEAM("general.energy_conversion.steam", "Steam Energy Density", "Maximum Joules per mB of Steam. Also affects Thermoelectric Boiler."),

    GENERAL_RADIATION("general.radiation", "Radiation Settings", "Settings for configuring Radiation", true),
    GENERAL_RADIATION_ENABLED("general.radiation.enabled", "Enabled", "Enable worldwide radiation effects."),
    GENERAL_RADIATION_CHUNK_RADIUS("general.radiation.chunk_radius", "Chunk Check Radius",
          "The radius of chunks checked when running radiation calculations. The algorithm is efficient, but don't abuse it by making this crazy high."),
    GENERAL_RADIATION_DECAY_RATE_SOURCE("general.radiation.decay_rate.source", "Source Decay Rate",
          "Radiation sources are multiplied by this constant roughly once per second to represent their emission decay. At the default rate, it takes roughly 10 hours to remove a 1,000 Sv/h (crazy high) source."),
    GENERAL_RADIATION_DECAY_RATE_TARGET("general.radiation.decay_rate.target", "Target Decay Rate",
          "Radiated objects and entities are multiplied by this constant roughly once per second to represent their dosage decay."),
    GENERAL_RADIATION_MIN_SEVERITY("general.radiation.min_severity", "Negative Effects Min Severity",
          "Defines the minimum severity radiation dosage severity (scale of 0 to 1) for which negative effects can take place. Set to 1 to disable negative effects completely."),
    GENERAL_RADIATION_BARREL_CAPACITY("general.radiation.barrel.capacity", "Waste Barrel Capacity", "Amount of chemical (mB) that can be stored in a Radioactive Waste Barrel."),
    GENERAL_RADIATION_BARREL_DECAY_FREQUENCY("general.radiation.barrel.decay.frequency", "Waste Barrel Process Frequency",
          "Number of ticks required for radioactive chemical stored in a Radioactive Waste Barrel to decay radioactiveWasteBarrelDecayAmount mB."),
    GENERAL_RADIATION_BARREL_DECAY_AMOUNT("general.radiation.barrel.decay.amount", "Waste Barrel Decay Amount",
          "Number of mB of chemical that decay every radioactiveWasteBarrelProcessTicks ticks when stored in a Radioactive Waste Barrel. Set to zero to disable decay "
          + "all together. (Chemicals in the mekanism:waste_barrel_decay_blacklist tag will not decay)."),

    GENERAL_MINER("general.miner", "Digital Miner", "Settings for configuring the Digital Miner", true),
    GENERAL_MINER_SILK_MULTIPLIER("general.miner.silk", "Silk Touch Energy Multiplier", "Energy multiplier for using silk touch mode with the Digital Miner."),
    GENERAL_MINER_MAX_RADIUS("general.miner.max_radius", "Max Radius",
          "Maximum radius in blocks that the Digital Miner can reach. Increasing this may have negative effects on stability and memory usage. We strongly "
          + "recommend you leave this at the default value."),
    GENERAL_MINER_TICK_RATE("general.miner.ticks_per_mine", "Ticks Per Mine",
          "Number of ticks required to mine a single block with a Digital Miner (without any upgrades)."),
    GENERAL_MINER_EASY_FILTERS("general.miner.easy_filters", "Easy Miner Filters",
          "Allows dragging items from recipe viewers into the target slot of Digital Miner filters. This allows selecting specific blocks without having to acquire "
          + "an instance of the block first."),

    GENERAL_LASER("general.laser", "Laser Settings", "Settings for configuring Lasers", true),
    GENERAL_LASER_RANGE("general.laser.range", "Range", "How far in blocks a laser can travel."),
    GENERAL_LASER_ENERGY_HARDNESS("general.laser.energy.hardness", "Energy Per Hardness", "Energy needed to destroy or attract blocks with a Laser (per block hardness level)."),
    GENERAL_LASER_ENERGY_DAMAGE("general.laser.energy.damage", "Energy Per Damage", "Energy used per half heart of damage being transferred to entities."),

    GENERAL_OREDICTIONIFICATOR("general.oredictionificator", "Oredictionificator Settings", "Settings for configuring the Oredictionificator",
          "Edit Oredictionificator"),
    GENERAL_OREDICTIONIFICATOR_VALID_ITEMS("general.oredictionificator.valid_filters.item", "Valid Item Filters",
          "The list of valid tag prefixes for the Oredictionificator. Note: It is highly recommended to only include well known/defined tag prefixes otherwise it is "
          + "very easy to potentially add accidental conversions for things that are not actually equivalent.", "Edit Filters"),

    GENERAL_PUMP("general.pump", "Pump Settings", "Settings for configuring Electric Pumps and Fluidic Plenishers", true),
    GENERAL_PUMP_RANGE("general.pump.range", "Range", "Maximum block distance to pull fluid from for the Electric Pump."),
    GENERAL_PUMP_INFINITE_FLUIDS("general.pump.infinite_fluids", "Drain Infinite Fluids",
          "If enabled and the waterSourceConversion game rule is enabled makes Water blocks be removed from the world on pump. Similarly behavior for the "
          + "lavaSourceConversion game rule and Lava"),
    GENERAL_PUMP_HEAVY_WATER("general.pump.heavy_water", "Heavy Water Amount",
          "Amount of Heavy Water in mB that is extracted per block of Water by the Electric Pump with a Filter Upgrade."),
    GENERAL_PUMP_PLENISHER_MAX_NODES("general.pump.plenisher.max_nodes", "Max Plenisher Nodes",
          "The number of blocks Fluidic Plenisher attempts to place fluid in before stopping."),

    GENERAL_QE("general.qe", "Quantum Entangloporter", "Settings for configuring Quantum Entangloporters", "Edit Entangloporter Settings"),
    GENERAL_QE_BUFFER_ENERGY("general.qe.buffer.energy", "Energy Buffer",
          "Maximum energy buffer (Joules) of an Entangoloporter frequency - i.e. the maximum transfer per tick per frequency. Default is the ultimate energy cube's capacity."),
    GENERAL_QE_BUFFER_FLUID("general.qe.buffer.fluid",
          "Fluid Buffer", "Maximum fluid buffer (mb) of an Entangoloporter frequency - i.e. the maximum transfer per tick per frequency. Default is the ultimate fluid tank's capacity."),
    GENERAL_QE_BUFFER_CHEMICAL("general.qe.buffer.chemical", "Chemical Buffer",
          "Maximum chemical buffer (mb) of an Entangoloporter frequency - i.e. the maximum transfer per tick per frequency. Default is the ultimate chemical tank's capacity."),

    GENERAL_SECURITY("general.security", "Block security/protection Settings", "Settings for configuring Mekanism's security system", true),
    GENERAL_SECURITY_ENABLED("general.security.enabled", "Allow Protection",
          "Enable the security system. This allows players to prevent other players from accessing their machines. Does NOT affect Frequencies."),
    GENERAL_SECURITY_OPS_BYPASS("general.security.ops_bypass", "Operator Bypass",
          "If this is enabled then players with the 'mekanism.bypass_security' permission (default for ops) can bypass block and item security restrictions."),

    GENERAL_PASTE("general.paste", "Nutritional Paste", "Settings for configuring Nutritional Paste", true),
    GENERAL_PASTE_SATURATION("general.paste.saturation", "Saturation", "Saturation level of Nutritional Paste when eaten."),
    GENERAL_PASTE_PER_FOOD("general.paste.per_food", "Paste per Food", "How much mB of Nutritional Paste equates to one 'half-food'."),

    GENERAL_BOILER("general.boiler", "Thermoelectric Boiler", "Settings for configuring Boilers", "Edit Boiler Settings"),
    GENERAL_BOILER_CAPACITY_WATER("general.boiler.capacity.water", "Water Capacity Per Block",
          "Amount of fluid (mB) that each block of the boiler's water portion contributes to the volume. Max = volume * waterPerTank"),
    GENERAL_BOILER_CAPACITY_STEAM("general.boiler.capacity.steam", "Steam Capacity Per Block",
          "Amount of steam (mB) that each block of the boiler's steam portion contributes to the volume. Max = volume * steamPerTank"),
    GENERAL_BOILER_CAPACITY_HEATED_COOLANT("general.boiler.capacity.heated_coolant", "Heated Coolant Capacity Per Block",
          "Amount of steam (mB) that each block of the boiler's heated coolant portion contributes to the volume. Max = volume * heatedCoolantPerTank"),
    GENERAL_BOILER_CAPACITY_COOLED_COOLANT("general.boiler.capacity.cooled_coolant", "Cooled Capacity Per Block",
          "Amount of steam (mB) that each block of the boiler's cooled coolant portion contributes to the volume. Max = volume * cooledCoolantPerTank"),
    GENERAL_BOILER_WATER_CONDUCTIVITY("general.boiler.water_conductivity", "Water Conductivity", "How much Boiler heat is immediately usable to convert water to steam."),
    GENERAL_BOILER_HEAT_TRANSFER("general.boiler.heat_transfer", "Superheating Heat Transfer", "Amount of heat each Boiler heating element produces."),

    GENERAL_TEP("general.tep", "Thermal Evaporation Plant", "Settings for configuring Thermal Evaporation Plants", "Edit TEP Settings"),
    GENERAL_TEP_HEAT_LOSS("general.tep.heat.loss", "Heat Dissipation", "Thermal Evaporation Plant heat loss per tick."),
    GENERAL_TEP_HEAT_SOLAR("general.tep.heat.solar", "Heat per Solar", "Heat to absorb per Solar Panel array installed on a Thermal Evaporation Plant."),
    GENERAL_TEP_TEMP_MULT("general.tep.temperature_multiplier", "Temperature Multiplier", "Ratio of temperature to output amount produced."),
    GENERAL_TEP_CAPACITY_HEAT("general.tep.capacity.heat", "Heat Capacity",
          "Heat capacity of Thermal Evaporation Plant layers. This increases the amount of energy needed to increase temperature."),
    GENERAL_TEP_CAPACITY_INPUT("general.tep.capacity.input", "Input Tank Capacity Per Block",
          "Amount of fluid (mB) that each block of the evaporation plant contributes to the input tank capacity. Max = volume * fluidPerTank"),
    GENERAL_TEP_CAPACITY_OUTPUT("general.tep.capacity.output", "Output Tank Capacity", "Amount of output fluid (mB) that the evaporation plant can store."),

    GENERAL_SPS("general.sps", "SPS Settings", "Settings for configuring the Supercritical Phase Shifter", true),
    GENERAL_SPS_ANTIMATTER_COST("general.sps.antimatter_cost", "Polonium Per Antimatter",
          "How much polonium in mB must be processed to make 1 mB of antimatter. The input tank capacity is 2x this value."),
    GENERAL_SPS_CAPACITY_OUTPUT("general.sps.capacity.output", "Output Tank Capacity", "Amount of antimatter in mB that the SPS can store."),
    GENERAL_SPS_ENERGY_PER("general.sps.energy_per", "Energy Per Polonium",
          "Energy needed to process 1 mB of input (inputPerAntimatter * energyPerInput = energy to produce 1 mB of antimatter)."),

    //Tier Config
    TIER_ENERGY_CUBE("tier.energy_cube", "Energy Cubes", "Settings for configuring Energy Cubes", true),
    TIER_FLUID_TANK("tier.fluid_tank", "Fluid Tanks", "Settings for configuring Fluid Tanks", true),
    TIER_CHEMICAL_TANK("tier.chemical_tank", "Chemical Tanks", "Settings for configuring Chemical Tanks", true),
    TIER_BIN("tier.bin", "Bins", "Settings for configuring Bins", "Edit Bin Settings"),
    TIER_INDUCTION("tier.induction", "Induction Matrix", "Settings for configuring Induction Cells and Providers", "Edit Matrix Settings"),
    TIER_TRANSMITTERS("tier.transmitter", "Transmitters", "Settings for configuring Transmitters", "Edit Transmitter Settings"),
    TIER_TRANSMITTERS_ENERGY("tier.transmitter.energy", "Universal Cables", "Settings for configuring Universal Cables", "Edit Cable Settings"),
    TIER_TRANSMITTERS_FLUID("tier.transmitter.fluid", "Mechanical Pipes", "Settings for configuring Mechanical Pipes", "Edit Pipe Settings"),
    TIER_TRANSMITTERS_CHEMICAL("tier.transmitter.chemical", "Pressurized Tubes", "Settings for configuring Pressurized Tubes", "Edit Tube Settings"),
    TIER_TRANSMITTERS_ITEM("tier.transmitter.item", "Logistical Transporters", "Settings for configuring Logistical Transporters", "Edit Transporter Settings"),
    TIER_TRANSMITTERS_HEAT("tier.transmitter.heat", "Thermodynamic Conductors", "Settings for configuring Thermodynamic Conductors", "Edit Conductor Settings"),

    //Storage Config
    ENERGY_STORAGE_ENRICHMENT_CHAMBER(TranslationPreset.ENERGY_STORAGE, "Enrichment Chamber"),
    ENERGY_STORAGE_COMPRESSOR(TranslationPreset.ENERGY_STORAGE, "Osmium Compressor"),
    ENERGY_STORAGE_COMBINER(TranslationPreset.ENERGY_STORAGE, "Combiner"),
    ENERGY_STORAGE_CRUSHER(TranslationPreset.ENERGY_STORAGE, "Crusher"),
    ENERGY_STORAGE_METALLURGIC_INFUSER(TranslationPreset.ENERGY_STORAGE, "Metallurgic Infuser"),
    ENERGY_STORAGE_PURIFICATION_CHAMBER(TranslationPreset.ENERGY_STORAGE, "Purification Chamber"),
    ENERGY_STORAGE_SMELTER(TranslationPreset.ENERGY_STORAGE, "Energized Smelter"),
    ENERGY_STORAGE_MINER(TranslationPreset.ENERGY_STORAGE, "Digital Miner"),
    ENERGY_STORAGE_PUMP(TranslationPreset.ENERGY_STORAGE, "Electric Pump"),
    ENERGY_STORAGE_CHARGEPAD(TranslationPreset.ENERGY_STORAGE, "Chargepad"),
    ENERGY_STORAGE_CONDENSENTRATOR(TranslationPreset.ENERGY_STORAGE, "Rotary Condensentrator"),
    ENERGY_STORAGE_OXIDIZER(TranslationPreset.ENERGY_STORAGE, "Chemical Oxidizer"),
    ENERGY_STORAGE_CHEMICAL_INFUSER(TranslationPreset.ENERGY_STORAGE, "Chemical Infuser"),
    ENERGY_STORAGE_INJECTION_CHAMBER(TranslationPreset.ENERGY_STORAGE, "Chemical Injection Chamber"),
    ENERGY_STORAGE_SEPARATOR(TranslationPreset.ENERGY_STORAGE, "Electrolytic Separator"),
    ENERGY_STORAGE_SAWMILL(TranslationPreset.ENERGY_STORAGE, "Precision Sawmill"),
    ENERGY_STORAGE_CDC(TranslationPreset.ENERGY_STORAGE, "Chemical Dissolution Chamber"),
    ENERGY_STORAGE_WASHER(TranslationPreset.ENERGY_STORAGE, "Chemical Washer"),
    ENERGY_STORAGE_CRYSTALLIZER(TranslationPreset.ENERGY_STORAGE, "Chemical Crystallizer"),
    ENERGY_STORAGE_VIBRATOR(TranslationPreset.ENERGY_STORAGE, "Seismic Vibrator"),
    ENERGY_STORAGE_PRC(TranslationPreset.ENERGY_STORAGE, "Pressurized Reaction Chamber"),
    ENERGY_STORAGE_PLENISHER(TranslationPreset.ENERGY_STORAGE, "Fluidic Plenisher"),
    ENERGY_STORAGE_LASER(TranslationPreset.ENERGY_STORAGE, "Laser"),
    ENERGY_STORAGE_LASER_AMPLIFIER(TranslationPreset.ENERGY_STORAGE, "Laser Amplifier"),
    ENERGY_STORAGE_TRACTOR_BEAM(TranslationPreset.ENERGY_STORAGE, "Laser Tractor Beam"),
    ENERGY_STORAGE_ASSEMBLICATOR(TranslationPreset.ENERGY_STORAGE, "Formulaic Assemblicator"),
    ENERGY_STORAGE_TELEPORTER(TranslationPreset.ENERGY_STORAGE, "Teleporter"),
    ENERGY_STORAGE_MODIFICATION_STATION(TranslationPreset.ENERGY_STORAGE, "Modification Station"),
    ENERGY_STORAGE_CENTRIFUGE(TranslationPreset.ENERGY_STORAGE, "Isotopic Centrifuge"),
    ENERGY_STORAGE_LIQUIFIER(TranslationPreset.ENERGY_STORAGE, "Nutritional Liquifier"),
    ENERGY_STORAGE_NUCLEOSYNTHESIZER(TranslationPreset.ENERGY_STORAGE, "Nucleosynthesizer", ". Also defines max process rate."),
    ENERGY_STORAGE_PIGMENT_EXTRACTOR(TranslationPreset.ENERGY_STORAGE, "Pigment Extractor"),
    ENERGY_STORAGE_PIGMENT_MIXER(TranslationPreset.ENERGY_STORAGE, "Pigment Mixer"),
    ENERGY_STORAGE_PAINTING(TranslationPreset.ENERGY_STORAGE, "Painting Machine"),
    ENERGY_STORAGE_SPS_PORT(TranslationPreset.ENERGY_STORAGE, "SPS Port", ". Also defines max output rate."),
    ENERGY_STORAGE_DIMENSIONAL_STABILIZER(TranslationPreset.ENERGY_STORAGE, "Dimensional Stabilizer"),

    //Usage Config
    ENERGY_USAGE_ENRICHMENT_CHAMBER(TranslationPreset.ENERGY_USAGE, "Enrichment Chamber"),
    ENERGY_USAGE_COMPRESSOR(TranslationPreset.ENERGY_USAGE, "Osmium Compressor"),
    ENERGY_USAGE_COMBINER(TranslationPreset.ENERGY_USAGE, "Combiner"),
    ENERGY_USAGE_CRUSHER(TranslationPreset.ENERGY_USAGE, "Crusher"),
    ENERGY_USAGE_METALLURGIC_INFUSER(TranslationPreset.ENERGY_USAGE, "Metallurgic Infuser"),
    ENERGY_USAGE_PURIFICATION_CHAMBER(TranslationPreset.ENERGY_USAGE, "Purification Chamber"),
    ENERGY_USAGE_SMELTER(TranslationPreset.ENERGY_USAGE, "Energized Smelter"),
    ENERGY_USAGE_MINER(TranslationPreset.ENERGY_USAGE, "Digital Miner"),
    ENERGY_USAGE_PUMP(TranslationPreset.ENERGY_USAGE, "Electric Pump"),
    ENERGY_USAGE_CHARGEPAD("usage.chargepad.energy", "Chargepad Energy Usage", "Energy in Joules that can be transferred at once per charge operation."),
    ENERGY_USAGE_CONDENSENTRATOR(TranslationPreset.ENERGY_USAGE, "Rotary Condensentrator"),
    ENERGY_USAGE_OXIDIZER(TranslationPreset.ENERGY_USAGE, "Chemical Oxidizer"),
    ENERGY_USAGE_CHEMICAL_INFUSER(TranslationPreset.ENERGY_USAGE, "Chemical Infuser"),
    ENERGY_USAGE_INJECTION_CHAMBER(TranslationPreset.ENERGY_USAGE, "Chemical Injection Chamber"),
    ENERGY_USAGE_SAWMILL(TranslationPreset.ENERGY_USAGE, "Precision Sawmill"),
    ENERGY_USAGE_CDC(TranslationPreset.ENERGY_USAGE, "Chemical Dissolution Chamber"),
    ENERGY_USAGE_WASHER(TranslationPreset.ENERGY_USAGE, "Chemical Washer"),
    ENERGY_USAGE_CRYSTALLIZER(TranslationPreset.ENERGY_USAGE, "Chemical Crystallizer"),
    ENERGY_USAGE_VIBRATOR(TranslationPreset.ENERGY_USAGE, "Seismic Vibrator"),
    ENERGY_USAGE_PRC(TranslationPreset.ENERGY_USAGE, "Pressurized Reaction Chamber"),
    ENERGY_USAGE_PLENISHER(TranslationPreset.ENERGY_USAGE, "Fluidic Plenisher"),
    ENERGY_USAGE_LASER(TranslationPreset.ENERGY_USAGE, "Laser"),
    ENERGY_USAGE_ASSEMBLICATOR(TranslationPreset.ENERGY_USAGE, "Formulaic Assemblicator"),
    ENERGY_USAGE_MODIFICATION_STATION(TranslationPreset.ENERGY_USAGE, "Modification Station"),
    ENERGY_USAGE_CENTRIFUGE(TranslationPreset.ENERGY_USAGE, "Isotopic Centrifuge"),
    ENERGY_USAGE_LIQUIFIER(TranslationPreset.ENERGY_USAGE, "Nutritional Liquifier"),
    ENERGY_USAGE_NUCLEOSYNTHESIZER(TranslationPreset.ENERGY_USAGE, "Nucleosynthesizer"),
    ENERGY_USAGE_PIGMENT_EXTRACTOR(TranslationPreset.ENERGY_USAGE, "Pigment Extractor"),
    ENERGY_USAGE_PIGMENT_MIXER(TranslationPreset.ENERGY_USAGE, "Pigment Mixer"),
    ENERGY_USAGE_PAINTING(TranslationPreset.ENERGY_USAGE, "Painting Machine"),
    ENERGY_USAGE_DIMENSIONAL_STABILIZER("usage.stabilizer.energy", "Dimensional Stabilizer Energy Usage", "Energy per chunk per tick in Joules."),
    SECONDARY_CHEMICAL_USAGE_RANDOMIZED("usage.secondary.chemical.random", "Randomized  Secondary Chemical Usage",
          "If enabled, Chemical Injectors and Purification Chambers will consume a semi randomized amount of chemical each operation."),

    USAGE_TELEPORTER("usage.teleporter", "Teleporter", "Settings for configuring Teleporter Energy Usage", true),
    USAGE_TELEPORTER_BASE("usage.teleporter.base", "Base Energy Usage", "Base cost in Joules for teleporting an entity."),
    USAGE_TELEPORTER_DISTANCE("usage.teleporter.distance", "Distance Energy Usage",
          "Joules per unit of distance travelled during teleportation - sqrt(xDiff^2 + yDiff^2 + zDiff^2)."),
    USAGE_TELEPORTER_PENALTY("usage.teleporter.penalty.dimension", "Dimension Energy Penalty",
          "Flat additional cost for interdimensional teleportation. Distance is still taken into account minimizing energy cost based on dimension scales."),

    //World Config
    WORLD_RETROGEN("world.retrogen", "Retrogen",
          "Allows chunks to retrogen Mekanism salt and ore blocks. In general when enabling this you also want to bump the userWorldGenVersion."),
    WORLD_WORLD_VERSION("world.world_version", "User World Version",
          "Change this value to cause Mekanism to regen its ore in all loaded chunks that have a version lower than this."),
    WORLD_HEIGHT_RANGE_PLATEAU("world.height_range.plateau", "Plateau",
          "Half length of short side of trapezoid, only used if the shape is Trapezoid. A value of zero means the shape is a triangle."),
    WORLD_ANCHOR_TYPE("world.height_range.anchor.type", "Anchor Type", "The type of anchor. This is used to define how the value is used in calculating the y-level."),
    WORLD_ANCHOR_VALUE("world.height_range.anchor.value", "Value", "Value used for calculating the y-level for the anchor based on the type."),

    WORLD_SALT("world.salt", "Salt Generation Settings", "Generation Settings for salt.", "Edit Salt Settings"),
    WORLD_SALT_SHOULD_GENERATE("world.salt.generate", "Should Generate", "Determines if salt should be added to world generation."),
    WORLD_SALT_PER_CHUNK("world.salt.per_chunk", "Per Chunk", "Chance that salt generates in a chunk."),
    WORLD_SALT_RADIUS_MIN("world.salt.radius.min", "Min Radius", "Base radius of a vein of salt."),
    WORLD_SALT_RADIUS_MAX("world.salt.radius.max", "Min Radius", "Extended variability (spread) for the radius in a vein of salt."),
    WORLD_SALT_HALF_HEIGHT("world.salt.half_height", "Half Height", "Number of blocks to extend up and down when placing a vein of salt."),

    //Gear Config
    GEAR_DISASSEMBLER("gear.disassembler", "Atomic Disassembler", "Settings for configuring the Atomic Disassembler", "Edit Disassembler Settings"),
    GEAR_DISASSEMBLER_MAX_ENERGY("gear.disassembler.max_energy", "Max Energy", "Maximum amount (joules) of energy the Atomic Disassembler can contain."),
    GEAR_DISASSEMBLER_CHARGE_RATE("gear.disassembler.charge_rate", "Charge Rate", "Amount (joules) of energy the Atomic Disassembler can accept per tick."),
    GEAR_DISASSEMBLER_ENERGY_USAGE("gear.disassembler.energy_usage", "Energy Usage",
          "Base Energy (Joules) usage of the Atomic Disassembler. (Gets multiplied by speed factor)"),
    GEAR_DISASSEMBLER_ENERGY_USAGE_WEAPON("gear.disassembler.energy_usage.weapon", "Energy Usage Weapon",
          "Cost in Joules of using the Atomic Disassembler as a weapon."),
    GEAR_DISASSEMBLER_MIN_DAMAGE("gear.disassembler.damage.min", "Min Damage",
          "The bonus attack damage of the Atomic Disassembler when it is out of power. (Value is in number of half hearts)"),
    GEAR_DISASSEMBLER_MAX_DAMAGE("gear.disassembler.damage.max", "Max Damage",
          "The bonus attack damage of the Atomic Disassembler when it has at least energyUsageWeapon power stored. (Value is in number of half hearts)"),
    GEAR_DISASSEMBLER_ATTACK_SPEED("gear.disassembler.attack_speed", "Attack Speed", "Attack speed of the Atomic Disassembler."),
    GEAR_DISASSEMBLER_SLOW("gear.disassembler.slow", "Slow Mode Enabled", "Enable the 'Slow' mode for the Atomic Disassembler."),
    GEAR_DISASSEMBLER_FAST("gear.disassembler.fast", "Fast Mode Enabled", "Enable the 'Fast' mode for the Atomic Disassembler."),
    GEAR_DISASSEMBLER_VEIN_MINING("gear.disassembler.vein_mining", "Vein Mining",
          "Enable the 'Vein Mining' mode for the Atomic Disassembler. This mode is limited to vein mining just ores and logs."),
    GEAR_DISASSEMBLER_MINING_COUNT("gear.disassembler.mining_count", "Vein Mining Block Count",
          "The max number of blocks the Atomic Disassembler can mine using the 'Vein Mining' mode. Requires veinMining to be enabled."),

    GEAR_BOW("gear.bow", "Electric Bow", "Settings for configuring the Electric Bow", true),
    GEAR_BOW_MAX_ENERGY("gear.bow.max_energy", "Max Energy", "Maximum amount (joules) of energy the Electric Bow can contain."),
    GEAR_BOW_CHARGE_RATE("gear.bow.charge_rate", "Charge Rate", "Amount (joules) of energy the Electric Bow can accept per tick."),
    GEAR_BOW_ENERGY_USAGE("gear.bow.energy_usage", "Energy Usage", "Cost in Joules of using the Electric Bow."),
    GEAR_BOW_ENERGY_USAGE_FLAME("gear.bow.energy_usage.flame", "Flame Energy Usage", "Cost in Joules of using the Electric Bow with flame mode active."),

    GEAR_ENERGY_TABLET("gear.energy_table", "Energy Tablet", "Settings for configuring Energy Tablets", true),
    GEAR_ENERGY_TABLET_MAX_ENERGY("gear.energy_table.max_energy", "Max Energy", "Maximum amount (joules) of energy the Energy Tablet can contain."),
    GEAR_ENERGY_TABLET_CHARGE_RATE("gear.energy_table.charge_rate", "Charge Rate", "Amount (joules) of energy the Energy Tablet can accept per tick."),

    GEAR_GAUGE_DROPPER("gear.gauge_dropper", "Gauge Dropper", "Settings for configuring Gauge Droppers", true),
    GEAR_GAUGE_DROPPER_CAPACITY("gear.gauge_dropper.capacity", "Capacity", "Capacity in mB of gauge droppers."),
    GEAR_GAUGE_DROPPER_TRANSFER_RATE("gear.gauge_dropper.transfer_rate", "Transfer Rate", "Rate in mB/t at which a gauge dropper can be filled or emptied."),

    GEAR_FLAMETHROWER("gear.flamethrower", "Flamethrower", "Settings for configuring the Flamethrower", true),
    GEAR_FLAMETHROWER_CAPACITY("gear.flamethrower.capacity", "Capacity", "Flamethrower tank capacity in mB."),
    GEAR_FLAMETHROWER_FILL_RATE("gear.flamethrower.fill_rate", "Fill Rate", "Rate in mB/t at which a Flamethrower's tank can accept hydrogen."),
    GEAR_FLAMETHROWER_DESTROY_ITEMS("gear.flamethrower.destroy_items", "Destroy Item Entities",
          "Determines whether or not the Flamethrower can destroy item entities the flame hits if it fails to smelt them."),

    GEAR_FREE_RUNNERS("gear.free_runners", "Free Runner", "Settings for configuring Free Runners", true),
    GEAR_FREE_RUNNERS_MAX_ENERGY("gear.free_runners.max_energy", "Max Energy", "Maximum amount (joules) of energy Free Runners can contain."),
    GEAR_FREE_RUNNERS_CHARGE_RATE("gear.free_runners.charge_rate", "Charge Rate", "Amount (joules) of energy the Free Runners can accept per tick."),
    GEAR_FREE_RUNNERS_FALL_COST("gear.free_runners.fall.energy", "Fall Energy Cost",
          "Energy cost multiplier in Joules for reducing fall damage with free runners. Energy cost is: FallDamage * fallEnergyCost. (1 FallDamage is 1 half heart)"),
    GEAR_FREE_RUNNERS_FALL_DAMAGE("gear.free_runners.fall.reduction", "Fall Damage Reduction Ratio",
          "Percent of damage taken from falling that can be absorbed by Free Runners when they have enough power."),

    GEAR_JETPACK("gear.jetpack", "Jetpack", "Settings for configuring Jetpacks", true),
    GEAR_JETPACK_CAPACITY("gear.jetpack.capacity", "Capacity", "Jetpack tank capacity in mB."),
    GEAR_JETPACK_FILL_RATE("gear.jetpack.fill_rate", "Fill Rate", "Rate in mB/t at which a Jetpack's tank can accept hydrogen."),

    GEAR_NETWORK_READER("gear.network_reader", "Network Reader", "Settings for configuring Network Readers", true),
    GEAR_NETWORK_READER_MAX_ENERGY("gear.network_reader.max_energy", "Max Energy", "Maximum amount (joules) of energy the Network Reader can contain."),
    GEAR_NETWORK_READER_CHARGE_RATE("gear.network_reader.charge_rate", "Charge Rate", "Amount (joules) of energy the Network Reader can accept per tick."),
    GEAR_NETWORK_READER_ENERGY_USAGE("gear.network_reader.energy_usage", "Energy Usage", "Energy usage in joules for each network reading."),

    GEAR_PORTABLE_TELEPORTER("gear.portable_teleporter", "Portable Teleporter", "Settings for configuring the Portable Teleporter", true),
    GEAR_PORTABLE_TELEPORTER_MAX_ENERGY("gear.portable_teleporter.max_energy", "Max Energy", "Maximum amount (joules) of energy the Portable Teleporter can contain."),
    GEAR_PORTABLE_TELEPORTER_CHARGE_RATE("gear.portable_teleporter.charge_rate", "Charge Rate", "Amount (joules) of energy the Portable Teleporter can accept per tick."),
    GEAR_PORTABLE_TELEPORTER_DELAY("gear.portable_teleporter.delay", "Teleportation Delay",
          "Delay in ticks before a player is teleported after clicking the Teleport button in the portable teleporter."),

    GEAR_SCUBA_TANK("gear.scuba_tank", "Scuba Tank", "Settings for configuring Scuba Tanks", true),
    GEAR_SCUBA_TANK_CAPACITY("gear.scuba_tank.capacity", "Capacity", "Scuba Tank capacity in mB."),
    GEAR_SCUBA_TANK_FILL_RATE("gear.scuba_tank.fill_rate", "Fill Rate", "Rate in mB/t at which a Scuba Tank can accept oxygen."),

    GEAR_SEISMIC_READER("gear.seismic_reader", "Seismic Reader", "Settings for configuring Seismic Readers", true),
    GEAR_SEISMIC_READER_MAX_ENERGY("gear.seismic_reader.max_energy", "Max Energy", "Maximum amount (joules) of energy the Seismic Reader can contain."),
    GEAR_SEISMIC_READER_CHARGE_RATE("gear.seismic_reader.charge_rate", "Charge Rate", "Amount (joules) of energy the Seismic Reader can accept per tick."),
    GEAR_SEISMIC_READER_ENERGY_USAGE("gear.seismic_reader.energy_usage", "Energy Usage", "Energy usage in joules required to use the Seismic Reader."),

    GEAR_CANTEEN("gear.canteen", "Canteen", "Settings for configuring Canteens", true),
    GEAR_CANTEEN_CAPACITY("gear.canteen.capacity", "Capacity", "Maximum amount in mB of Nutritional Paste storable by the Canteen."),
    GEAR_CANTEEN_TRANSFER_RATE("gear.canteen.transfer_rate", "Transfer Rate", "Rate in mB/t at which Nutritional Paste can be transferred into a Canteen."),

    GEAR_MEKA_TOOL("gear.meka_tool", "Meka-Tool", "Settings for configuring the Meka-Tool", true),
    GEAR_MEKA_TOOL_CAPACITY("gear.meka_tool.capacity", "Max Energy", "Energy capacity (Joules) of the Meka-Tool without any installed upgrades. Quadratically scaled by upgrades."),
    GEAR_MEKA_TOOL_CHARGE_RATE("gear.meka_tool.charge_rate", "Charge Rate", "Amount (joules) of energy the Meka-Tool can accept per tick. Quadratically scaled by upgrades."),
    GEAR_MEKA_TOOL_DAMAGE("gear.meka_tool.damage", "Base Damage", "Base bonus damage applied by the Meka-Tool without using any energy."),
    GEAR_MEKA_TOOL_ATTACK_SPEED("gear.meka_tool.attack_speed", "Attack Speed", "Attack speed of the Meka-Tool."),
    GEAR_MEKA_TOOL_EFFICIENCY("gear.meka_tool.efficiency", "Efficiency", "Efficiency of the Meka-Tool with energy but without any upgrades."),
    GEAR_MEKA_TOOL_TELEPORTATION_DISTANCE("gear.meka_tool.teleportation_distance", "Max Teleportation Distance", "Maximum distance a player can teleport with the Meka-Tool."),
    GEAR_MEKA_TOOL_EXTENDED_VEIN("gear.meka_tool.extended_vein", "Extended Vein Mining",
          "Enable the 'Extended Vein Mining' mode for the Meka-Tool. Allows vein mining everything, instead of being limited to just ores and logs."),

    GEAR_MEKA_TOOL_ENERGY_USAGE("gear.meka_tool.energy_usage", "Energy Usage", "Settings for configuring the Meka-Tool's Energy Usage", true),
    GEAR_MEKA_TOOL_ENERGY_USAGE_BASE("gear.meka_tool.energy_usage.base", "Base", "Base energy (Joules) usage of the Meka-Tool. (Gets multiplied by speed factor)"),
    GEAR_MEKA_TOOL_ENERGY_USAGE_SILK("gear.meka_tool.energy_usage.silk", "Silk Touch", "Silk touch energy (Joules) usage of the Meka-Tool. (Gets multiplied by speed factor)"),
    GEAR_MEKA_TOOL_ENERGY_USAGE_WEAPON("gear.meka_tool.energy_usage.weapon", "Weapon", "Cost in Joules of using the Meka-Tool to deal 4 units of damage."),
    GEAR_MEKA_TOOL_HOE("gear.meka_tool.hoe", "Hoe", "Cost in Joules of using the Meka-Tool as a hoe."),
    GEAR_MEKA_TOOL_SHOVEL("gear.meka_tool.shovel", "Shovel", "Cost in Joules of using the Meka-Tool as a shovel for making paths and dowsing campfires."),
    GEAR_MEKA_TOOL_AXE("gear.meka_tool.axe", "Axe", "Cost in Joules of using the Meka-Tool as an axe for stripping logs, scraping, or removing wax."),
    GEAR_MEKA_TOOL_SHEAR_ENTITY("gear.meka_tool.shear.entity", "Shear Entities", "Cost in Joules of using the Meka-Tool to shear entities."),
    GEAR_MEKA_TOOL_SHEAR_BLOCK("gear.meka_tool.shear.block", "Shear Blocks", "Cost in Joules of using the Meka-Tool to carefully shear and trim blocks."),
    GEAR_MEKA_TOOL_ENERGY_USAGE_TELEPORT("gear.meka_tool.energy_usage.teleport", "Teleport Energy Usage", "Cost in Joules of using the Meka-Tool to teleport 10 blocks."),

    GEAR_MEKA_SUIT("gear.meka_suit", "MekaSuit", "Settings for configuring the MekaSuit", true),
    GEAR_MEKA_SUIT_CAPACITY("gear.meka_suit.capacity.energy", "Max Energy",
          "Energy capacity (Joules) of MekaSuit items without any installed upgrades. Quadratically scaled by upgrades."),
    GEAR_MEKA_SUIT_CHARGE_RATE("gear.meka_suit.charge_rate", "Charge Rate", "Amount (joules) of energy the MekaSuit can accept per tick. Quadratically scaled by upgrades."),
    GEAR_MEKA_SUIT_CHARGE_RATE_INVENTORY("gear.meka_suit.charge_rate.inventory", "Inventory Charge Rate",
          "Charge rate of inventory items in Joules/t when charging the player's inventory with the Charge Distribution Unit."),
    GEAR_MEKA_SUIT_CHARGE_RATE_SOLAR("gear.meka_suit.charge_rate.solar", "Solar Charging Rate", "Solar recharging rate in Joules/t of the helmet, per upgrade installed."),
    GEAR_MEKA_SUIT_FLIGHT_VIBRATIONS("gear.meka_suit.gravitational_vibrations", "Gravitational Vibrations",
          "Should the Gravitational Modulation unit give off vibrations that can be detected by Sculk Sensors and Shriekers when in use."),
    GEAR_MEKA_SUIT_PASTE_CAPACITY("gear.meka_suit.paste.capacity", "Nutritional Paste Capacity", "Maximum amount in mB of Nutritional Paste storable by the nutritional injection unit."),
    GEAR_MEKA_SUIT_PASTE_TRANSFER_RATE("gear.meka_suit.paste.transfer_rate", "Nutritional Paste Transfer Rate", "Rate in mB/t at which Nutritional Paste can be transferred into the nutritional injection unit."),
    GEAR_MEKA_SUIT_JETPACK_CAPACITY("gear.meka_suit.jetpack.capacity", "Jetpack Hydrogen Capacity", "Maximum amount in mB of Hydrogen storable per installed jetpack unit."),
    GEAR_MEKA_SUIT_JETPACK_TRANSFER_RATE("gear.meka_suit.jetpack.transfer_rate", "Jetpack Transfer Rate", "Rate in mB/t at which Hydrogen can be transferred into the jetpack unit."),

    GEAR_MEKA_SUIT_ENERGY_USAGE("gear.meka_suit.energy_usage", "Energy Usage", "Settings for configuring the MekaSuit's Energy Usage", true),
    GEAR_MEKA_SUIT_ENERGY_USAGE_DAMAGE("gear.meka_suit.energy_usage.damage", "Damage Reduction", "Energy usage (Joules) of MekaSuit per unit of damage applied."),
    GEAR_MEKA_SUIT_ENERGY_USAGE_MAGIC("gear.meka_suit.energy_usage.magic", "Magic Prevention",
          "Energy cost multiplier in Joules for reducing magic damage via the inhalation purification unit. Energy cost is: MagicDamage * magicReduce. (1 MagicDamage is 1 half heart)."),
    GEAR_MEKA_SUIT_ENERGY_USAGE_FALL("gear.meka_suit.energy_usage.fall", "Fall Reduction",
          "Energy cost multiplier in Joules for reducing fall damage with MekaSuit Boots. Energy cost is: FallDamage * fall. (1 FallDamage is 1 half heart)"),
    GEAR_MEKA_SUIT_ENERGY_USAGE_JUMP("gear.meka_suit.energy_usage.jump", "Jump Boost", "Energy usage (Joules) of MekaSuit when adding 0.1 to jump motion."),
    GEAR_MEKA_SUIT_ENERGY_USAGE_ELYTRA("gear.meka_suit.energy_usage.elytra", "Elytra", "Energy usage (Joules) per second of the MekaSuit when flying with the Elytra Unit."),
    GEAR_MEKA_SUIT_ENERGY_USAGE_POTION("gear.meka_suit.energy_usage.potion", "Potion Effect Speedup", "Energy usage (Joules) of MekaSuit when lessening a potion effect."),
    GEAR_MEKA_SUIT_ENERGY_USAGE_SPRINT("gear.meka_suit.energy_usage.sprint", "Sprint Boost", "Energy usage (Joules) of MekaSuit when adding 0.1 to sprint motion."),
    GEAR_MEKA_SUIT_ENERGY_USAGE_FLIGHT("gear.meka_suit.energy_usage.flight", "Gravitational Modulation", "Energy usage (Joules) of MekaSuit per tick when flying via Gravitational Modulation."),
    GEAR_MEKA_SUIT_ENERGY_USAGE_VISION("gear.meka_suit.energy_usage.vision", "Vision Enhancement", "Energy usage (Joules) of MekaSuit per tick of using vision enhancement."),
    GEAR_MEKA_SUIT_ENERGY_USAGE_SWIM("gear.meka_suit.energy_usage.swim", "Hydrostatic Repulsion", "Energy usage (Joules) of MekaSuit per tick of using hydrostatic repulsion."),
    GEAR_MEKA_SUIT_ENERGY_USAGE_FOOD("gear.meka_suit.energy_usage.food", "Nutritional Injection", "Energy usage (Joules) of MekaSuit per half-food of nutritional injection."),
    GEAR_MEKA_SUIT_ENERGY_USAGE_MAGNET("gear.meka_suit.energy_usage.magnet", "Magnetic Attraction", "Energy usage (Joules) of MekaSuit per tick of attracting a single item."),

    GEAR_MEKA_SUIT_DAMAGE_ABSORPTION("gear.meka_suit.damage_absorption", "MekaSuit Damage Absorption",
          "Settings for configuring damage absorption of the MekaSuit", "Edit Absorption Ratios"),
    GEAR_MEKA_SUIT_ABSORPTION_FALL("gear.meka_suit.damage_absorption.fall", "Fall Damage Ratio",
          "Percent of damage taken from falling that can be absorbed by MekaSuit Boots when they have enough power."),
    GEAR_MEKA_SUIT_ABSORPTION_MAGIC("gear.meka_suit.damage_absorption.magic", "Magic Damage Ratio",
          "Percent of damage taken from magic damage that can be absorbed by MekaSuit Helmet with Purification unit when it has enough power."),
    GEAR_MEKA_SUIT_ABSORPTION_UNSPECIFIED("gear.meka_suit.damage_absorption.unspecified", "Unspecified Damage Ratio",
          "Percent of damage taken from other non explicitly supported damage types that don't bypass armor when the MekaSuit has enough power and a full suit is equipped. "
          + "Note: Support for specific damage types can be added by adding an entry for the damage type in the mekanism:mekasuit_absorption data map."),

    //Startup config
    STARTUP_GEAR("startup.gear", "Gear Settings",
          "Settings for configuring Mekanism's gear settings. This config is not synced automatically between client and server. It is highly "
          + "recommended to ensure you are using the same values for this config on the server and client.", true),

    STARTUP_FREE_RUNNERS_ARMORED("gear.free_runners.armored", "Armored Free Runners", "Settings for configuring Armored Free Runners",
          "Edit Free Runner Settings"),
    STARTUP_FREE_RUNNERS_ARMOR("gear.free_runners.armored.armor", "Armor", "Armor value of the Armored Free Runners"),
    STARTUP_FREE_RUNNERS_TOUGHNESS("gear.free_runners.armored.toughness", "Toughness", "Toughness value of the Armored Free Runners."),
    STARTUP_FREE_RUNNERS_KNOCKBACK_RESISTANCE("gear.free_runners.armored.knockback_resistance", "Knockback Resistance",
          "Knockback resistance value of the Armored Free Runners."),

    STARTUP_JETPACK_ARMORED("gear.jetpack.armored", "Armored Jetpack", "Settings for configuring Armored Jetpacks", "Edit Jetpack Settings"),
    STARTUP_JETPACK_ARMOR("gear.jetpack.armored.armor", "Armor", "Armor value of the Armored Jetpacks"),
    STARTUP_JETPACK_TOUGHNESS("gear.jetpack.armored.toughness", "Toughness", "Toughness value of the Armored Jetpacks."),
    STARTUP_JETPACK_KNOCKBACK_RESISTANCE("gear.jetpack.armored.knockback_resistance", "Knockback Resistance",
          "Knockback resistance value of the Armored Jetpacks."),

    STARTUP_MEKA_SUIT_ARMOR_HELMET("gear.meka_suit.armor.helmet", "Helmet Armor", "Armor value of MekaSuit Helmets."),
    STARTUP_MEKA_SUIT_ARMOR_CHESTPLATE("gear.meka_suit.armor.chestplate", "BodyArmor Armor", "Armor value of MekaSuit BodyArmor."),
    STARTUP_MEKA_SUIT_ARMOR_LEGGINGS("gear.meka_suit.armor.leggings", "Pants Armor", "Armor value of MekaSuit Pants."),
    STARTUP_MEKA_SUIT_ARMOR_BOOTS("gear.meka_suit.armor.boots", "Boots Armor", "Armor value of MekaSuit Boots."),
    STARTUP_MEKA_SUIT_TOUGHNESS(".gear.meka_suit.toughness", "Toughness", "Toughness value of the MekaSuit."),
    STARTUP_MEKA_SUIT_KNOCKBACK_RESISTANCE("gear.meka_suit.knockback_resistance", "Knockback Resistance", "Knockback resistance value of the MekaSuit."),

    //ProjectE integration configs
    PE_CONTENTS_PROCESSOR("projecte.data_component_processor.contents", "Mek Contents Processor",
          "Increases the EMC value of any Mekanism items by the value of the stored or installed contents", true),

    PE_MAPPER_CHEMICAL_CHEMICAL_TO_CHEMICAL("projecte.mapper.chemical_chemical_to_chemical", Mekanism.MOD_NAME + " CC2C",
          "Maps Mekanism chemical infuser and pigment mixer recipes.", true),
    PE_MAPPER_CRYSTALLIZER("projecte.mapper.chemical_crystallizer", Mekanism.MOD_NAME + " Crystallizer", "Maps Mekanism crystallizing recipes.", true),
    PE_MAPPER_DISSOLUTION("projecte.mapper.chemical_dissolution", Mekanism.MOD_NAME + " Dissolution", "Maps Mekanism dissolution recipes.", true),
    PE_MAPPER_CHEMICAL_TO_CHEMICAL("projecte.mapper.chemical_to_chemical", Mekanism.MOD_NAME + " C2C",
          "Maps Mekanism activating and centrifuging recipes.", true),
    PE_MAPPER_COMBINER("projecte.mapper.combiner", Mekanism.MOD_NAME + " Combiner", "Maps Mekanism combiner recipes.", true),
    PE_MAPPER_ELECTROLYSIS("projecte.mapper.electrolysis", Mekanism.MOD_NAME + " Electrolysis", "Maps Mekanism electrolytic separator recipes.", true),
    PE_MAPPER_WASHING("projecte.mapper.todo", Mekanism.MOD_NAME + " Washing", "Maps Mekanism washing recipes.", true),
    PE_MAPPER_EVAPORATING("projecte.mapper.evaporating", Mekanism.MOD_NAME + " Evaporating", "Maps Mekanism evaporating recipes.", true),
    PE_MAPPER_ITEM_CHEMICAL_TO_ITEM("projecte.mapper.item_chemical_to_item", Mekanism.MOD_NAME + " IC2I",
          "Maps Mekanism Machine recipes that go from item, chemical to item. (Compressing, Purifying, Injecting, Metallurgic Infusing, Painting, Nucleosynthesizing)", true),
    PE_MAPPER_ITEM_TO_CHEMICAL("projecte.mapper.item_to_chemical", Mekanism.MOD_NAME + " I2C",
          "Maps Mekanism item stack to chemical recipes. (Chemical conversion, Oxidizing, Pigment Extracting)", true),
    PE_MAPPER_ITEM_TO_ITEM("projecte.mapper.item_to_item", Mekanism.MOD_NAME + " Item To Item",
          "Maps Mekanism Machine recipes that go from item to item. (Crushing, Enriching, Smelting)", true),
    PE_MAPPER_PRESSURIZED_REACTION("projecte.mapper.pressurized_reaction", Mekanism.MOD_NAME + " PRC", "Maps Mekanism pressurized reaction recipes. (Disabled by default)", true),
    PE_MAPPER_ROTARY("projecte.mapper.rotary", Mekanism.MOD_NAME + " Rotary", "Maps Mekanism rotary condensentrator recipes.", true),
    PE_MAPPER_SAWING("projecte.mapper.sawing", Mekanism.MOD_NAME + " Sawing",
          "Maps Mekanism sawmill recipes. (Disabled by default, due to causing various EMC values to be removed pertaining to charcoal/wood)", true),
    ;

    private final String key;
    private final String title;
    private final String tooltip;
    @Nullable
    private final String button;

    MekanismConfigTranslations(TranslationPreset preset, String type) {
        this(preset.path(type), preset.title(type), preset.tooltip(type));
    }

    MekanismConfigTranslations(TranslationPreset preset, String type, String tooltipSuffix) {
        this(preset.path(type), preset.title(type), preset.tooltip(type) + tooltipSuffix);
    }

    MekanismConfigTranslations(String path, String title, String tooltip) {
        this(path, title, tooltip, false);
    }

    MekanismConfigTranslations(String path, String title, String tooltip, boolean isSection) {
        this(path, title, tooltip, IConfigTranslation.getSectionTitle(title, isSection));
    }

    MekanismConfigTranslations(String path, String title, String tooltip, @Nullable String button) {
        this.key = Util.makeDescriptionId("configuration", Mekanism.rl(path));
        this.title = title;
        this.tooltip = tooltip;
        this.button = button;
    }

    @NotNull
    @Override
    public String getTranslationKey() {
        return key;
    }

    @Override
    public String title() {
        return title;
    }

    @Override
    public String tooltip() {
        return tooltip;
    }

    @Nullable
    @Override
    public String button() {
        return button;
    }

    public record TierTranslations(@Nullable IConfigTranslation first, @Nullable IConfigTranslation second, @Nullable IConfigTranslation third) {

        public TierTranslations {
            if (first == null && second == null) {
                throw new IllegalArgumentException("Tier Translations must have at least a first, second, or third tooltip");
            }
        }

        public IConfigTranslation[] toArray() {
            return Stream.of(first, second, third).filter(Objects::nonNull).toArray(IConfigTranslation[]::new);
        }

        @NotNull
        @Override
        public IConfigTranslation first() {
            if (first == null) {
                throw new IllegalStateException("This method should not be called when first is null. Define first");
            }
            return first;
        }

        @NotNull
        @Override
        public IConfigTranslation second() {
            if (second == null) {
                throw new IllegalStateException("This method should not be called when storage is null. Define second");
            }
            return second;
        }

        @NotNull
        @Override
        public IConfigTranslation third() {
            if (third == null) {
                throw new IllegalStateException("This method should not be called when third is null. Define third");
            }
            return third;
        }

        private static String getKey(String type, String tier, String path) {
            return Util.makeDescriptionId("configuration", Mekanism.rl("tier." + type + "." + tier + "." + path));
        }

        public static TierTranslations create(ITier tier, String type, @Nullable UnaryOperator<String> storageTooltip, @Nullable UnaryOperator<String> outputTooltip) {
            return create(tier, type, storageTooltip, outputTooltip, " Output Rate");
        }

        public static TierTranslations create(ITier tier, String type, @Nullable UnaryOperator<String> storageTooltip, @Nullable UnaryOperator<String> outputTooltip,
              String rateSuffix) {
            String tierName = tier.getBaseTier().getSimpleName();
            String key = tierName.toLowerCase(Locale.ROOT);
            return new TierTranslations(
                  storageTooltip == null ? null : new ConfigTranslation(getKey(type, key, "storage"), tierName + " Storage", storageTooltip.apply(tierName)),
                  outputTooltip == null ? null : new ConfigTranslation(getKey(type, key, "rate"), tierName + rateSuffix, outputTooltip.apply(tierName)),
                  null
            );
        }

        public static TierTranslations create(EnergyCubeTier tier) {
            return create(tier, "energy_cube", name -> "Maximum number of Joules " + name + " energy cubes can store.",
                  name -> "Output rate in Joules of " + name + " energy cubes."
            );
        }

        public static TierTranslations create(FluidTankTier tier) {
            return create(tier, "fluid_tank", name -> "Storage size of " + name + " fluid tanks in mB.",
                  name -> "Output rate of " + name + " fluid tanks in mB."
            );
        }

        public static TierTranslations create(ChemicalTankTier tier) {
            return create(tier, "chemical_tank", name -> "Storage size of " + name + " chemical tanks in mB.",
                  name -> "Output rate of " + name + " chemical tanks in mB."
            );
        }

        public static TierTranslations create(BinTier tier) {
            return create(tier, "bin", name -> "The number of items " + name + " bins can store.", null);
        }

        public static TierTranslations create(InductionCellTier tier) {
            return create(tier, "induction.cell", name -> "Maximum number of Joules " + name + " induction cells can store.", null);
        }

        public static TierTranslations create(InductionProviderTier tier) {
            return create(tier, "induction.provider", null, name -> "Maximum number of Joules " + name + " induction providers can output or accept.");
        }

        public static TierTranslations create(CableTier tier) {
            return create(tier, "transmitter.energy", name -> "Internal buffer in Joules of each " + name + " universal cable.", null);
        }

        public static TierTranslations create(PipeTier tier) {
            return create(tier, "transmitter.fluid", name -> "Capacity of " + name + " mechanical pipes in mB.",
                  name -> "Pump rate of " + name + " mechanical pipes in mB/t.", " Pull Rate"
            );
        }

        public static TierTranslations create(TubeTier tier) {
            return create(tier, "transmitter.chemical", name -> "Capacity of " + name + " pressurized tubes in mB.",
                  name -> "Pump rate of " + name + " pressurized tubes in mB/t.", " Pull Rate"
            );
        }

        public static TierTranslations create(TransporterTier tier) {
            String type = "transmitter.item";
            String tierName = tier.getBaseTier().getSimpleName();
            String key = tierName.toLowerCase(Locale.ROOT);
            return new TierTranslations(new ConfigTranslation(getKey(type, key, "pull_rate"), tierName + " Pull Rate",
                  "Item throughput rate of " + tierName + " logistical transporters in items/half second. This value assumes a target tick rate of 20 ticks per second."
            ), new ConfigTranslation(getKey(type, key, "speed"), tierName + " Transfer Speed",
                  "Five times the travel speed in m/s of " + tierName + " logistical transporter. This value assumes a target tick rate of 20 ticks per second."
            ), null);
        }

        public static TierTranslations create(ConductorTier tier) {
            String type = "transmitter.heat";
            String tierName = tier.getBaseTier().getSimpleName();
            String key = tierName.toLowerCase(Locale.ROOT);
            return new TierTranslations(new ConfigTranslation(getKey(type, key, "inverse_conduction"), tierName + " Inverse Conduction",
                  "Conduction value of " + tierName + " thermodynamic conductors."
            ), new ConfigTranslation(getKey(type, key, "heat_capacity"), tierName + " Heat Capacity",
                  "Heat capacity of " + tierName + " thermodynamic conductors."
            ), new ConfigTranslation(getKey(type, key, "insulation"), tierName + " Insulation",
                  "Insulation value of " + tierName + " thermodynamic conductor."
            ));
        }
    }

    public record OreConfigTranslations(IConfigTranslation topLevel, IConfigTranslation shouldGenerate) {

        public IConfigTranslation[] toArray() {
            return new IConfigTranslation[]{topLevel, shouldGenerate};
        }

        private static String getKey(String ore, String path) {
            return Util.makeDescriptionId("configuration", Mekanism.rl("world." + ore + "." + path));
        }

        public static OreConfigTranslations create(String ore) {
            String name = TextUtils.formatAndCapitalize(ore);
            return new OreConfigTranslations(
                  new ConfigTranslation(getKey(ore, "top_level"), name + " Generation Settings", "Generation Settings for " + name + " ore.", "Edit " + name + " Settings"),
                  new ConfigTranslation(getKey(ore, "generate"), "Should Generate", "Determines if " + name + " ore should be added to world generation.")
            );
        }
    }

    public record OreVeinConfigTranslations(
          IConfigTranslation topLevel, IConfigTranslation shouldGenerate,
          IConfigTranslation perChunk, IConfigTranslation maxVeinSize, IConfigTranslation discardChanceOnAirExposure,
          IConfigTranslation distributionShape, IConfigTranslation minInclusive, IConfigTranslation maxInclusive
    ) {

        public IConfigTranslation[] toArray() {
            return new IConfigTranslation[]{topLevel, shouldGenerate, perChunk, maxVeinSize, discardChanceOnAirExposure, distributionShape, minInclusive,
                                            maxInclusive};
        }

        private static String getKey(String ore, String vein, String path) {
            return Util.makeDescriptionId("configuration", Mekanism.rl("world." + ore + "." + vein + "." + path));
        }

        public static OreVeinConfigTranslations create(String ore, String vein) {
            String capitalizedOre = TextUtils.formatAndCapitalize(ore);
            String capitalizedVein = TextUtils.formatAndCapitalize(vein);
            String name = capitalizedVein + " " + capitalizedOre + " Vein";
            return new OreVeinConfigTranslations(
                  new ConfigTranslation(getKey(ore, vein, "top_level"), capitalizedVein + " Vein", name + " Generation Settings.", true),
                  new ConfigTranslation(getKey(ore, vein, "generate"), "Should Generate",
                        "Determines if " + name + "s should be added to world generation. Note: Requires generating " + ore + " ore to be enabled."),
                  new ConfigTranslation(getKey(ore, vein, "per_chunk"), "Per Chunk", "Chance that " + name + "s generates in a chunk."),
                  new ConfigTranslation(getKey(ore, vein, "max_size"), "Max Size", "Maximum number of blocks in a " + name + "."),
                  new ConfigTranslation(getKey(ore, vein, "discard_chance"), "Discard Chance",
                        "Chance that blocks that are directly exposed to air in a " + name + " are not placed."),

                  new ConfigTranslation(getKey(ore, vein, "shape"), "Distribution shape", "Distribution shape for placing " + name + "s."),
                  new ConfigTranslation(getKey(ore, vein, "min"), "Min Anchor",
                        "Minimum (inclusive) height anchor for " + name + "s. This defines the minimum y-level that " + name + "s can spawn at.", true),
                  new ConfigTranslation(getKey(ore, vein, "max"), "Max Anchor",
                        "Maximum (inclusive) height anchor for " + name + "s. This defines the maximum y-level that " + name + "s can spawn at.", true)
            );
        }
    }
}