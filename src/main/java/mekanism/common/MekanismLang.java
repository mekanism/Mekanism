package mekanism.common;

import mekanism.common.base.ILangEntry;
import net.minecraft.util.Util;

//TODO: Figure out some good way to organize this file
//TODO: Also go through and convert all keys to lower case?
//TODO: Also potentially make better names for various things
//TODO: Look at !n and determine if they are artifacts that should be deleted
//TODO: Also search for "= in case any copy paste errors happened
public enum MekanismLang implements ILangEntry {
    //Vanilla lang strings we use, for purposes of not having to have them copy pasted all over the place
    INVENTORY("container.inventory"),
    REPAIR("container.repair"),
    REPAIR_COST("container.repair.cost"),
    REPAIR_EXPENSIVE("container.repair.expensive"),

    //Gui lang strings
    MEKANISM("mekanism.mod_name"),
    DEBUG_TITLE("mekanism.debug_title"),
    LOG_FORMAT("mekanism.log_format"),
    FORGE("mekanism.forge"),
    IC2("mekanism.ic2"),

    KEY_MODE("mekanism.key.mode"),
    KEY_ARMOR_MODE("mekanism.key.armorMode"),
    KEY_FEET_MODE("mekanism.key.feetMode"),

    HOLIDAY_BORDER("holiday.mekanism.border"),
    HOLIDAY_SIGNATURE("holiday.mekanism.signature"),

    CHRISTMAS_LINE_ONE("holiday.mekanism.christmas.1"),
    CHRISTMAS_LINE_TWO("holiday.mekanism.christmas.2"),
    CHRISTMAS_LINE_THREE("holiday.mekanism.christmas.3"),
    CHRISTMAS_LINE_FOUR("holiday.mekanism.christmas.4"),

    NEW_YEAR_LINE_ONE("holiday.mekanism.new_year.1"),
    NEW_YEAR_LINE_TWO("holiday.mekanism.new_year.2"),
    NEW_YEAR_LINE_THREE("holiday.mekanism.new_year.3"),

    NETWORK_READER_BORDER("chat.mekanism.network_reader.border"),
    NETWORK_READER_ABOVE_AMBIENT("chat.mekanism.network_reader.above_ambient"),
    NETWORK_READER_TRANSMITTERS("chat.mekanism.network_reader.transmitters"),
    NETWORK_READER_ACCEPTORS("chat.mekanism.network_reader.acceptors"),
    NETWORK_READER_NEEDED("chat.mekanism.network_reader.needed"),
    NETWORK_READER_BUFFER("chat.mekanism.network_reader.buffer"),
    NETWORK_READER_THROUGHPUT("chat.mekanism.network_reader.throughput"),
    NETWORK_READER_CAPACITY("chat.mekanism.network_reader.capacity"),
    NETWORK_READER_CONNECTED_SIDES("chat.mekanism.network_reader.connected"),

    MAX_OUTPUT("gui.mekanism.maxOutput"),
    STORED_ENERGY("tooltip.mekanism.stored_energy"),
    STORING("gui.mekanism.storing"),

    TEMPERATURE_KELVIN("mekanism.temperature.kelvin"),
    TEMPERATURE_KELVIN_SHORT("mekanism.temperature.kelvin.short"),
    TEMPERATURE_CELSIUS("mekanism.temperature.celsius"),
    TEMPERATURE_CELSIUS_SHORT("mekanism.temperature.celsius.short"),
    TEMPERATURE_RANKINE("mekanism.temperature.rankine"),
    TEMPERATURE_RANKINE_SHORT("mekanism.temperature.rankine.short"),
    TEMPERATURE_FAHRENHEIT("mekanism.temperature.fahrenheit"),
    TEMPERATURE_FAHRENHEIT_SHORT("mekanism.temperature.fahrenheit.short"),
    TEMPERATURE_AMBIENT("mekanism.temperature.ambient"),
    TEMPERATURE_AMBIENT_SHORT("mekanism.temperature.ambient.short"),

    ENERGY_JOULES("mekanism.energy.joules"),
    ENERGY_JOULES_PLURAL("mekanism.energy.joules"),
    ENERGY_JOULES_SHORT("mekanism.energy.joules.short"),
    ENERGY_FORGE("mekanism.energy.forge"),
    ENERGY_FORGE_SHORT("mekanism.energy.forge.short"),
    ENERGY_EU("mekanism.energy.eu"),
    ENERGY_EU_PLURAL("mekanism.energy.eu"),
    ENERGY_EU_SHORT("mekanism.energy.eu.short"),

    GENERIC("gui.mekanism.generic"),
    GENERIC_SQUARE_BRACKET("mekanism.generic_square_bracket"),
    GENERIC_STORED("gui.mekanism.generic_stored"),
    GENERIC_STORED_MB("gui.mekanism.generic_stored_mb"),
    GENERIC_MB("gui.mekanism.generic_mb"),
    GENERIC_PRE_COLON("gui.mekanism.generic_pre_colon"),
    GENERIC_PARENTHESIS("gui.mekanism.generic_parenthesis"),
    GENERIC_FRACTION("gui.mekanism.generic_fraction"),
    GENERIC_TRANSFER("tooltip.mekanism.generic_transfer"),
    GENERIC_PER_TICK("gui.mekanism.generic_per_tick"),

    NETWORK_MB_PER_TICK("gui.mekanism.network_mb_per_tick"),
    NETWORK_MB_STORED("gui.mekanism.network_mb_stored"),

    FLUID_CONTAINER_BOTH("mekanism.fluidedit.both"),
    FLUID_CONTAINER_FILL("mekanism.fluidedit.fill"),
    FLUID_CONTAINER_EMPTY("mekanism.fluidedit.empty"),

    CONNECTION_NORMAL("mekanism.pipe.connectiontype.normal"),
    CONNECTION_PUSH("mekanism.pipe.connectiontype.push"),
    CONNECTION_PULL("mekanism.pipe.connectiontype.pull"),
    CONNECTION_NONE("mekanism.pipe.connectiontype.none"),

    NOT_APPLICABLE("gui.mekanism.not_applicable"),
    HEAT_NETWORK_STORED("gui.mekanism.heat_network_stored"),
    HEAT_NETWORK_FLOW("gui.mekanism.heat_network_flow"),
    HEAT_NETWORK_FLOW_EFFICIENCY("gui.mekanism.heat_network_flow.efficiency"),
    FLUID_NETWORK_NEEDED("gui.mekanism.fluid_network_needed"),

    YES("tooltip.mekanism.yes"),
    NO("tooltip.mekanism.no"),
    ON("gui.mekanism.on"),
    OFF("gui.mekanism.off"),

    ENTITY_DETECTION("gui.mekanism.entityDetection"),
    ENERGY_CONTENTS("gui.mekanism.energyContents"),

    REDSTONE_CONTROL_DISABLED("tooltip.mekanism.control.disabled"),
    REDSTONE_CONTROL_HIGH("tooltip.mekanism.control.high"),
    REDSTONE_CONTROL_LOW("tooltip.mekanism.control.low"),
    REDSTONE_CONTROL_PULSE("tooltip.mekanism.control.pulse"),

    UPGRADE_DISPLAY("upgrade.mekanism.display"),
    UPGRADE_DISPLAY_LEVEL("upgrade.mekanism.display.level"),

    ABUNDANCY("gui.mekanism.abundancy"),

    HEIGHT("gui.mekanism.height"),

    DISASSEMBLER_MODE_TOGGLE("tooltip.mekanism.modeToggle"),
    DISASSEMBLER_EFFICIENCY("tooltip.mekanism.efficiency"),
    DISASSEMBLER_NORMAL("tooltip.mekanism.disassembler.normal"),
    DISASSEMBLER_SLOW("tooltip.mekanism.disassembler.slow"),
    DISASSEMBLER_FAST("tooltip.mekanism.disassembler.fast"),
    DISASSEMBLER_VEIN("tooltip.mekanism.disassembler.vein"),
    DISASSEMBLER_EXTENDED_VEIN("tooltip.mekanism.disassembler.extended_vein"),
    DISASSEMBLER_OFF("tooltip.mekanism.disassembler.off"),

    SMELTING("gui.mekanism.factory.smelting"),
    ENRICHING("gui.mekanism.factory.enriching"),
    CRUSHING("gui.mekanism.factory.crushing"),
    COMPRESSING("gui.mekanism.factory.compressing"),
    COMBINING("gui.mekanism.factory.combining"),
    PURIFYING("gui.mekanism.factory.purifying"),
    INJECTING("gui.mekanism.factory.injecting"),
    INFUSING("gui.mekanism.factory.infusing"),
    SAWING("gui.mekanism.factory.sawing"),

    CAPACITY("tooltip.mekanism.capacity"),
    CAPACITY_ITEMS("tooltip.mekanism.capacity.items"),
    CAPACITY_MB("tooltip.mekanism.capacity.mb"),
    CAPACITY_PER_TICK("tooltip.mekanism.capacity.per_tick"),
    CAPACITY_MB_PER_TICK("tooltip.mekanism.capacity.mb.per_tick"),

    CAPABLE_OF_TRANSFERRING("tooltip.mekanism.capable_trans"),

    SECURITY("gui.mekanism.security"),
    SECURITY_OVERRIDDEN("gui.mekanism.overridden"),
    SECURITY_OFFLINE("gui.mekanism.securityOffline"),
    SECURITY_ADD("gui.mekanism.add"),
    SECURITY_OVERRIDE("gui.mekanism.securityOverride"),

    CHUNK("gui.mekanism.chunk"),
    CHUNK_COMMAND("command.mekanism.chunk"),

    STRICT_INPUT_ENABLED("gui.mekanism.strictInput"),

    DISSIPATED_RATE("gui.mekanism.dissipated"),

    STATUS("gui.mekanism.status"),
    STATUS_OK("gui.mekanism.allOK"),

    MULTIBLOCK_INCOMPLETE("gui.mekanism.incomplete"),
    MULTIBLOCK_FORMED("gui.mekanism.formed"),
    MULTIBLOCK_CONFLICT("gui.mekanism.conflict"),

    STORED("tooltip.mekanism.stored"),

    FUEL("gui.mekanism.fuel"),

    INFINITE("gui.mekanism.infinite"),
    NONE("gui.mekanism.none"),
    EMPTY("gui.mekanism.empty"),

    VOLUME("gui.mekanism.volume"),
    NO_FLUID("gui.mekanism.noFluid"),

    NO_ACCESS("gui.mekanism.noAccessDesc"),

    GAS("gui.mekanism.gas"),

    CONFIGURE_STATE("tooltip.mekanism.configureState"),
    FIRE_MODE("tooltip.mekanism.fire_mode"),
    BUCKET_MODE("tooltip.mekanism.portableTank.bucketMode"),
    FLAME_THROWER_MODE_BUMP("tooltip.mekanism.flamethrower.modeBump"),

    OWNER("gui.mekanism.owner"),
    NO_OWNER("gui.mekanism.no_owner"),

    UPGRADES_EFFECT("gui.mekanism.upgrades.effect"),
    TEMPERATURE("gui.mekanism.temp"),

    HAS_DATA("gui.mekanism.data"),
    PUMP_RESET("tooltip.mekanism.configurator.pump_reset"),
    PLENISHER_RESET("tooltip.mekanism.configurator.plenisher_reset"),

    MODE("tooltip.mekanism.mode"),

    BOIL_RATE("gui.mekanism.boilRate"),
    MAX_BOIL_RATE("gui.mekanism.maxBoil"),

    OUTPUTTING_RATE("gui.mekanism.outputting_rate"),

    FLUID_PRODUCTION("gui.mekanism.fluid_production"),

    IDLE("gui.mekanism.idle"),
    DUMPING_EXCESS("gui.mekanism.dumping_excess"),
    DUMPING("gui.mekanism.dumping"),

    VIBRATING("gui.mekanism.vibrating"),

    CHAT_MULTIBLOCK_FORMED("chat.mek.multiblockformed"),

    UNIT("gui.mekanism.unit"),
    USING("gui.mekanism.using"),
    NEEDED("gui.mekanism.needed"),
    NEEDED_PER_TICK("gui.mekanism.needed_per_tick"),

    UNIVERSAL("tooltip.mekanism.universal"),
    ITEMS("tooltip.mekanism.items"),
    BLOCKS("tooltip.mekanism.blocks"),
    FLUIDS("tooltip.mekanism.fluids"),
    GASES("tooltip.mekanism.gases"),
    HEAT("tooltip.mekanism.heat"),

    CONDUCTION("tooltip.mekanism.conduction"),
    INSULATION("tooltip.mekanism.insulation"),
    HEAT_CAPACITY("tooltip.mekanism.heatCapacity"),

    DIVERSION_CONTROL_DISABLED("tooltip.mekanism.control.disabled.desc"),
    DIVERSION_CONTROL_HIGH("tooltip.mekanism.control.high.desc"),
    DIVERSION_CONTROL_LOW("tooltip.mekanism.control.low.desc"),
    TOGGLE_DIVERTER("tooltip.mekanism.configurator.toggle_diverter"),

    TOGGLE_COLOR("tooltip.mekanism.configurator.toggle_color"),
    CURRENT_COLOR("tooltip.mekanism.configurator.view_color"),

    REDSTONE_SENSITIVITY("tooltip.mekanism.configurator.redstone_sensitivity"),
    CONNECTION_TYPE("tooltip.mekanism.configurator.mode_change"),

    HOLD_FOR_DETAILS("tooltip.mekanism.hold_for_details"),
    HOLD_FOR_DESCRIPTION("tooltip.mekanism.hold_for_description"),

    BOILER("gui.mekanism.thermoelectric_boiler"),
    BOILER_STATS("gui.mekanism.boilerStats"),
    BOILER_MAX_WATER("gui.mekanism.maxWater"),
    BOILER_MAX_STEAM("gui.mekanism.maxSteam"),
    BOILER_HEAT_TRANSFER("gui.mekanism.heatTransfer"),
    BOILER_HEATERS("gui.mekanism.superheaters"),
    BOILER_BOIL_RATE("gui.mekanism.boilCapacity"),

    FINISHED("gui.mekanism.finished"),

    INSUFFICIENT_BUFFER("gui.mekanism.insufficientbuffer"),
    BUFFER_FREE("gui.mekanism.bufferfree"),
    MINER_TO_MINE("gui.mekanism.digitalMiner.toMine"),
    MINER_SILK_ENABLED("gui.mekanism.digitalMiner.silk"),
    MINER_AUTO_PULL("gui.mekanism.digitalMiner.pull"),
    MINER_RUNNING("gui.mekanism.digitalMiner.running"),
    MINER_LOW_POWER("gui.mekanism.digitalMiner.lowPower"),
    MINER_ENERGY_CAPACITY("gui.mekanism.digitalMiner.capacity"),
    MINER_MISSING_BLOCK("gui.mekanism.digitalMiner.missingBlock"),
    MINER_WELL("gui.mekanism.well"),
    MINER_CONFIG("gui.mekanism.digitalMinerConfig"),
    MINER_SILK("gui.mekanism.digitalMiner.silkTouch"),
    MINER_RESET("gui.mekanism.digitalMiner.reset"),
    MINER_INVERSE("gui.mekanism.digitalMiner.inverse"),
    MINER_VISUALS("gui.mekanism.visuals"),
    MINER_VISUALS_TOO_BIG("gui.mekanism.visuals.toobig"),
    MINER_FUZZY_MODE("gui.mekanism.digitalMiner.fuzzyMode"),
    MINER_REQUIRE_REPLACE("gui.mekanism.digitalMiner.requireReplace"),
    MINER_IS_INVERSE("gui.mekanism.digital_miner.is_inverse"),
    MINER_RADIUS("gui.mekanism.digital_miner.radius"),

    MINER_IDLE("gui.mekanism.digital_miner.idle"),
    MINER_SEARCHING("gui.mekanism.digital_miner.searching"),
    MINER_PAUSED("gui.mekanism.digital_miner.paused"),
    MINER_READY("gui.mekanism.digital_miner.ready"),

    FILTER_COUNT("gui.mekanism.filter.count"),

    NO_RECIPE("gui.mekanism.noRecipe"),

    AUTO_PULL("gui.mekanism.digitalMiner.autoPull"),
    AUTO_EJECT("gui.mekanism.autoEject"),

    EJECT("gui.mekanism.eject"),

    STATE("gui.mekanism.state"),
    FILTERS("gui.mekanism.filters"),

    STOCK_CONTROL("gui.mekanism.stockControl"),
    AUTO_MODE("gui.mekanism.autoModeToggle"),

    MIN("gui.mekanism.min"),
    MAX("gui.mekanism.max"),

    NO_DELAY("gui.mekanism.noDelay"),
    DELAY("gui.mekanism.delay"),

    ENERGY("gui.mekanism.energy"),

    ITEM_AMOUNT("tooltip.mekanism.itemAmount"),

    PUBLIC("security.mekanism.public"),
    TRUSTED("security.mekanism.trusted"),
    PRIVATE("security.mekanism.private"),

    PUBLIC_MODE("gui.mekanism.publicMode"),
    TRUSTED_MODE("gui.mekanism.trustedMode"),
    PRIVATE_MODE("gui.mekanism.privateMode"),

    ALLOW_DEFAULT("gui.mekanism.allowDefault"),

    FREQUENCY("gui.mekanism.frequency"),
    NO_FREQUENCY("gui.mekanism.teleporter.noFreq"),

    FACTORY_TYPE("tooltip.mekanism.recipe_type"),

    CONSTITUENTS("gui.mekanism.constituents"),
    DIMENSIONS("gui.mekanism.dimensions"),
    DIMENSION_REPRESENTATION("gui.mekanism.dimensions.representation"),

    RESISTIVE_HEATER_USAGE("gui.mekanism.usage"),

    OUTPUT("gui.mekanism.output"),
    OUTPUT_AMOUNT("gui.mekanism.output_amount"),
    OUTPUT_RATE("gui.mekanism.output_rate"),

    INPUT("gui.mekanism.input"),
    INPUT_AMOUNT("gui.mekanism.input_amount"),
    INPUT_RATE("gui.mekanism.input_rate"),

    SORTER_SINGLE_ITEM("gui.mekanism.logisticalSorter.singleItem"),
    SORTER_ROUND_ROBIN("gui.mekanism.logisticalSorter.roundRobin"),
    SORTER_AUTO_EJECT("gui.mekanism.logisticalSorter.autoEject"),
    SORTER_DEFAULT("gui.mekanism.logisticalSorter.default"),

    SORTER_SINGLE_ITEM_DESCRIPTION("gui.mekanism.logisticalSorter.singleItem.tooltip"),
    SORTER_ROUND_ROBIN_DESCRIPTION("gui.mekanism.logisticalSorter.roundRobin.tooltip"),
    SORTER_AUTO_EJECT_DESCRIPTION("gui.mekanism.logisticalSorter.autoEject.tooltip"),

    MATRIX("gui.mekanism.induction_matrix"),
    MATRIX_RECEIVING_RATE("gui.mekanism.receiving"),
    MATRIX_STATS("gui.mekanism.matrixStats"),

    MATRIX_CELLS("gui.mekanism.cells"),
    MATRIX_PROVIDERS("gui.mekanism.providers"),

    INDUCTION_PORT_MODE("tooltip.mekanism.configurator.induction_port_mode"),
    INDUCTION_PORT_OUTPUT_RATE("tooltip.mekanism.outputRate"),

    FLOWING("tooltip.mekanism.flowing"),

    DYNAMIC_TANK("gui.mekanism.dynamic_tank"),

    TELEPORTER_READY("gui.mekanism.teleporter.ready"),
    TELEPORTER_NO_FRAME("gui.mekanism.teleporter.noFrame"),
    TELEPORTER_NO_LINK("gui.mekanism.teleporter.noLink"),
    TELEPORTER_NEEDS_ENERGY("gui.mekanism.teleporter.needsEnergy"),

    INDEX("gui.mekanism.index"),
    BUTTON_CONFIRM("gui.mekanism.confirm"),
    BUTTON_START("gui.mekanism.start"),
    BUTTON_STOP("gui.mekanism.stop"),
    BUTTON_CONFIG("gui.mekanism.config"),
    BUTTON_NEW_FILTER("gui.mekanism.newFilter"),
    BUTTON_REMOVE("gui.mekanism.remove"),
    BUTTON_SET("gui.mekanism.button_set"),
    BUTTON_DELETE("gui.mekanism.delete"),
    BUTTON_TELEPORT("gui.mekanism.teleport"),
    BUTTON_ITEMSTACK_FILTER("gui.mekanism.itemstack"),
    BUTTON_TAG_FILTER("gui.mekanism.oredict"),
    BUTTON_MATERIAL_FILTER("gui.mekanism.material"),
    BUTTON_MODID_FILTER("gui.mekanism.modID"),
    BUTTON_SAVE("gui.mekanism.save"),

    ERROR("mekanism.error"),

    NO_NETWORK("mekanism.transmitter.no_network"),

    ITEM_FILTER("gui.mekanism.itemFilter"),
    TAG_FILTER("gui.mekanism.oredictFilter"),
    MATERIAL_FILTER("gui.mekanism.materialFilter"),
    MODID_FILTER("gui.mekanism.modIDFilter"),

    FILTER("gui.mekanism.filter"),
    FILTER_NEW("gui.mekanism.new"),
    FILTER_EDIT("gui.mekanism.edit"),

    AUTO_SORT("gui.mekanism.factory.autoSort"),

    TOGGLE_CONDENSENTRATOR("gui.mekanism.rotaryCondensentrator.toggleOperation"),
    CONDENSENTRATING("gui.mekanism.condensentrating"),
    DECONDENSENTRATING("gui.mekanism.decondensentrating"),

    STRICT_INPUT("gui.mekanism.configuration.strictInput"),

    ENCODE_FORMULA("gui.mekanism.encodeFormula"),
    CRAFT_SINGLE("gui.mekanism.craftSingle"),
    CRAFT_AVAILABLE("gui.mekanism.craftAvailable"),
    FILL_EMPTY("gui.mekanism.fillEmpty"),

    TRUSTED_PLAYERS("gui.mekanism.trustedPlayers"),

    NO_EJECT("gui.mekanism.noEject"),
    SLOTS("gui.mekanism.slots"),

    TRANSPORTER_CONFIG("gui.mekanism.configuration.transporter"),
    SIDE_CONFIG("gui.mekanism.configuration.side"),

    SIDE_DATA_NONE("side_data.mekanism.none"),
    SIDE_DATA_INPUT("side_data.mekanism.input"),
    SIDE_DATA_OUTPUT("side_data.mekanism.output"),
    SIDE_DATA_ENERGY("side_data.mekanism.energy"),
    SIDE_DATA_EXTRA("side_data.mekanism.extra"),


    CHEMICAL_DISSOLUTION_CHAMBER_SHORT("gui.mekanism.chemicalDissolutionChamber.short"),
    CHEMICAL_INFUSER_SHORT("gui.mekanism.chemicalInfuser.short"),

    SIZE_MODE("gui.mekanism.sizeMode"),
    SIZE_MODE_CONFLICT("gui.mekanism.sizeModeConflict"),
    CREATE_FILTER_TITLE("gui.mekanism.filterSelect.title"),

    LAST_ITEM("gui.mekanism.lastItem"),
    NEXT_ITEM("gui.mekanism.nextItem"),
    TAG_COMPAT("gui.mekanism.oreDictCompat"),

    UPGRADES("gui.mekanism.upgrades"),
    UPGRADE_NO_SELECTION("gui.mekanism.upgrades.noSelection"),
    UPGRADES_SUPPORTED("gui.mekanism.upgrades.supported"),
    UPGRADE_COUNT("gui.mekanism.upgrades.amount"),
    UPGRADE_TYPE("gui.mekanism.upgrade"),

    CONFIG_CARD_GOT("tooltip.mekanism.configurationCard.got"),
    CONFIG_CARD_SET("tooltip.mekanism.configurationCard.set"),
    CONFIG_CARD_UNEQUAL("tooltip.mekanism.configurationCard.unequal"),

    CONFIGURATOR_VIEW_MODE("tooltip.mekanism.configurator.view_mode"),
    CONFIGURATOR_TOGGLE_MODE("tooltip.mekanism.configurator.toggle_mode"),
    CONFIGURATOR_CONFIGURATE("tooltip.mekanism.configurator.configurate"),
    CONFIGURATOR_EMPTY("tooltip.mekanism.configurator.empty"),
    CONFIGURATOR_ROTATE("tooltip.mekanism.configurator.rotate"),
    CONFIGURATOR_WRENCH("tooltip.mekanism.configurator.wrench"),

    TAG_FILTER_NO_KEY("gui.mekanism.oredictFilter.noKey"),
    TAG_FILTER_SAME_KEY("gui.mekanism.oredictFilter.sameKey"),
    MODID_FILTER_NO_ID("gui.mekanism.modIDFilter.noID"),
    MODID_FILTER_SAME_ID("gui.mekanism.modIDFilter.sameID"),
    ITEM_FILTER_NO_ITEM("gui.mekanism.itemFilter.noItem"),
    ITEM_FILTER_SIZE_MODE("gui.mekanism.itemFilter.size_mode"),
    MODID_FILTER_ID("gui.mekanism.id"),
    TAG_FILTER_TAG("gui.mekanism.key"),
    ITEM_FILTER_DETAILS("gui.mekanism.itemFilter.details"),
    MATERIAL_FILTER_DETAILS("gui.mekanism.materialFilter.details"),
    ITEM_FILTER_MAX_LESS_THAN_MIN("gui.mekanism.item_filter.max_less_than_min"),
    ITEM_FILTER_OVER_SIZED("gui.mekanism.item_filter.over_sized"),
    ITEM_FILTER_SIZE_MISSING("gui.mekanism.item_filter.size_missing"),

    JEI_AMOUNT_WITH_CAPACITY("tooltip.mekanism.jei.amount.with.capacity"),

    INVALID("tooltip.mekanism.invalid"),
    ENCODED("tooltip.mekanism.encoded"),

    NO_KEY("tooltip.mekanism.noKey"),
    KEYS_FOUND("tooltip.mekanism.keysFound"),
    DICTIONARY_KEY("tooltip.mekanism.dictionary.key"),

    FLAME_THROWER_COMBAT("tooltip.flamethrower.combat"),
    FLAME_THROWER_HEAT("tooltip.flamethrower.heat"),
    FLAME_THROWER_INFERNO("tooltip.flamethrower.inferno"),

    JETPACK_NORMAL("tooltip.jetpack.regular"),
    JETPACK_HOVER("tooltip.jetpack.hover"),
    JETPACK_DISABLED("tooltip.jetpack.disabled"),

    FREE_RUNNER_NORMAL("tooltip.freerunner.regular"),
    FREE_RUNNER_DISABLED("tooltip.freerunner.disabled"),

    NOW_OWN("gui.mekanism.nowOwn"),

    NAME("tooltip.mekanism.name"),

    NEEDS_ENERGY("tooltip.mekanism.seismicReader.needsEnergy"),
    NO_VIBRATIONS("tooltip.mekanism.seismicReader.noVibrations"),

    INGREDIENTS("tooltip.mekanism.ingredients"),
    INGREDIENT("tooltip.mekanism.ingredient"),

    BLOCK_DATA("tooltip.mekanism.blockData"),
    BLOCK("tooltip.mekanism.block"),
    TILE("tooltip.mekanism.tile"),

    DIVERSION_DESCRIPTION("tooltip.mekanism.diversion_description"),
    RESTRICTIVE_DESCRIPTION("tooltip.mekanism.restrictive_description"),

    PUMP_RATE("tooltip.mekanism.pump_rate"),
    PUMP_RATE_MB("tooltip.mekanism.pump_rate.mb"),
    SPEED("tooltip.mekanism.speed"),

    MOVE_UP("gui.mekanism.moveUp"),
    MOVE_DOWN("gui.mekanism.moveDown"),

    CONFIG_TYPE("gui.mekanism.config_type"),
    SET("gui.mekanism.set"),

    ROBIT("gui.mekanism.robit"),
    ROBIT_SMELTING("gui.mekanism.robit.smelting"),
    ROBIT_CRAFTING("gui.mekanism.robit.crafting"),
    ROBIT_INVENTORY("gui.mekanism.robit.inventory"),
    ROBIT_REPAIR("gui.mekanism.robit.repair"),
    ROBIT_TELEPORT("gui.mekanism.robit.teleport"),
    ROBIT_TOGGLE_PICKUP("gui.mekanism.robit.togglePickup"),
    ROBIT_RENAME("gui.mekanism.robit.rename"),
    ROBIT_TOGGLE_FOLLOW("gui.mekanism.robit.toggleFollow"),
    ROBIT_GREETING("gui.mekanism.robit.greeting"),
    ROBIT_OWNER("gui.mekanism.robit.owner"),
    ROBIT_FOLLOWING("gui.mekanism.robit.following"),
    ROBIT_DROP_PICKUP("gui.mekanism.robit.dropPickup"),

    REDSTONE_OUTPUT("gui.mekanism.redstoneOutput"),

    HAS_INVENTORY("tooltip.mekanism.inventory"),
    NO_GAS("tooltip.mekanism.noGas"),

    STATS_TAB("gui.mekanism.stats"),
    MAIN_TAB("gui.mekanism.main"),

    SEISMIC_READER_DESCRIPTION("tooltip.mekanism.seismic_reader"),

    NETWORK_DESCRIPTION("chat.mekanism.network_description"),
    INVENTORY_NETWORK("chat.mekanism.network.inventory"),
    FLUID_NETWORK("chat.mekanism.network.fluid"),
    GAS_NETWORK("chat.mekanism.network.gas"),
    HEAT_NETWORK("chat.mekanism.network.heat"),
    ENERGY_NETWORK("chat.mekanism.network.energy"),

    DESCRIPTION_BIN("tooltip.mekanism.description.bin"),
    DESCRIPTION_TELEPORTER_FRAME("tooltip.mekanism.description.teleporter_frame"),
    DESCRIPTION_STEEL_CASING("tooltip.mekanism.description.steel_casing"),
    DESCRIPTION_DYNAMIC_TANK("tooltip.mekanism.description.dynamic_tank"),
    DESCRIPTION_STRUCTURAL_GLASS("tooltip.mekanism.description.structural_glass"),
    DESCRIPTION_DYNAMIC_VALVE("tooltip.mekanism.description.dynamic_valve"),
    DESCRIPTION_THERMAL_EVAPORATION_CONTROLLER("tooltip.mekanism.description.thermal_evaporation_controller"),
    DESCRIPTION_THERMAL_EVAPORATION_VALVE("tooltip.mekanism.description.thermal_evaporation_valve"),
    DESCRIPTION_THERMAL_EVAPORATION_BLOCK("tooltip.mekanism.description.thermal_evaporation_block"),
    DESCRIPTION_INDUCTION_CASING("tooltip.mekanism.description.induction_casing"),
    DESCRIPTION_INDUCTION_PORT("tooltip.mekanism.description.induction_port"),
    DESCRIPTION_INDUCTION_CELL("tooltip.mekanism.description.induction_cell"),
    DESCRIPTION_INDUCTION_PROVIDER("tooltip.mekanism.description.induction_provider"),
    DESCRIPTION_SUPERHEATING_ELEMENT("tooltip.mekanism.description.superheating_element"),
    DESCRIPTION_PRESSURE_DISPERSER("tooltip.mekanism.description.pressure_disperser"),
    DESCRIPTION_BOILER_CASING("tooltip.mekanism.description.boiler_casing"),
    DESCRIPTION_BOILER_VALVE("tooltip.mekanism.description.boiler_valve"),
    DESCRIPTION_SECURITY_DESK("tooltip.mekanism.description.security_desk"),

    DESCRIPTION_ENRICHMENT_CHAMBER("tooltip.mekanism.description.enrichment_chamber"),
    DESCRIPTION_OSMIUM_COMPRESSOR("tooltip.mekanism.description.osmium_compressor"),
    DESCRIPTION_COMBINER("tooltip.mekanism.description.combiner"),
    DESCRIPTION_CRUSHER("tooltip.mekanism.description.crusher"),
    DESCRIPTION_DIGITAL_MINER("tooltip.mekanism.description.digital_miner"),
    DESCRIPTION_METALLURGIC_INFUSER("tooltip.mekanism.description.metallurgic_infuser"),
    DESCRIPTION_PURIFICATION_CHAMBER("tooltip.mekanism.description.purification_chamber"),
    DESCRIPTION_ENERGIZED_SMELTER("tooltip.mekanism.description.energized_smelter"),
    DESCRIPTION_TELEPORTER("tooltip.mekanism.description.teleporter"),
    DESCRIPTION_ELECTRIC_PUMP("tooltip.mekanism.description.electric_pump"),
    DESCRIPTION_PERSONAL_CHEST("tooltip.mekanism.description.personal_chest"),
    DESCRIPTION_CHARGEPAD("tooltip.mekanism.description.chargepad"),
    DESCRIPTION_LOGISTICAL_SORTER("tooltip.mekanism.description.logistical_sorter"),
    DESCRIPTION_ROTARY_CONDENSENTRATOR("tooltip.mekanism.description.rotary_condensentrator"),
    DESCRIPTION_CHEMICAL_INJECTION_CHAMBER("tooltip.mekanism.description.chemical_injection_chamber"),
    DESCRIPTION_ELECTROLYTIC_SEPARATOR("tooltip.mekanism.description.electrolytic_separator"),
    DESCRIPTION_PRECISION_SAWMILL("tooltip.mekanism.description.precision_sawmill"),
    DESCRIPTION_CHEMICAL_DISSOLUTION_CHAMBER("tooltip.mekanism.description.chemical_dissolution_chamber"),
    DESCRIPTION_CHEMICAL_WASHER("tooltip.mekanism.description.chemical_washer"),
    DESCRIPTION_CHEMICAL_CRYSTALLIZER("tooltip.mekanism.description.chemical_crystallizer"),
    DESCRIPTION_CHEMICAL_OXIDIZER("tooltip.mekanism.description.chemical_oxidizer"),
    DESCRIPTION_CHEMICAL_INFUSER("tooltip.mekanism.description.chemical_infuser"),
    DESCRIPTION_SEISMIC_VIBRATOR("tooltip.mekanism.description.seismic_vibrator"),
    DESCRIPTION_PRESSURIZED_REACTION_CHAMBER("tooltip.mekanism.description.pressurized_reaction_chamber"),
    DESCRIPTION_FLUID_TANK("tooltip.mekanism.description.fluid_tank"),
    DESCRIPTION_FLUIDIC_PLENISHER("tooltip.mekanism.description.fluidic_plenisher"),
    DESCRIPTION_LASER("tooltip.mekanism.description.laser"),
    DESCRIPTION_LASER_AMPLIFIER("tooltip.mekanism.description.laser_amplifier"),
    DESCRIPTION_LASER_TRACTOR_BEAM("tooltip.mekanism.description.laser_tractor_beam"),
    DESCRIPTION_SOLAR_NEUTRON_ACTIVATOR("tooltip.mekanism.description.solar_neutron_activator"),
    DESCRIPTION_OREDICTIONIFICATOR("tooltip.mekanism.description.oredictionificator"),
    DESCRIPTION_FACTORY("tooltip.mekanism.description.factory"),
    DESCRIPTION_RESISTIVE_HEATER("tooltip.mekanism.description.resistive_heater"),
    DESCRIPTION_FORMULAIC_ASSEMBLICATOR("tooltip.mekanism.description.formulaic_assemblicator"),
    DESCRIPTION_FUELWOOD_HEATER("tooltip.mekanism.description.fuelwood_heater"),
    DESCRIPTION_QUANTUM_ENTANGLOPORTER("tooltip.mekanism.description.quantum_entangloporter"),

    DESCRIPTION_GAS_TANK("tooltip.mekanism.description.gas_tank"),
    DESCRIPTION_ENERGY_CUBE("tooltip.mekanism.description.energy_cube"),

    DESCRIPTION_OSMIUM_ORE("tooltip.mekanism.description.osmium_ore"),
    DESCRIPTION_COPPER_ORE("tooltip.mekanism.description.copper_ore"),
    DESCRIPTION_TIN_ORE("tooltip.mekanism.description.tin_ore"),
    ;

    private final String key;

    //TODO: Use this?
    MekanismLang(String type, String path) {
        this(Util.makeTranslationKey(type, Mekanism.rl(path)));
    }

    MekanismLang(String key) {
        this.key = key;
    }

    @Override
    public String getTranslationKey() {
        return key;
    }
}