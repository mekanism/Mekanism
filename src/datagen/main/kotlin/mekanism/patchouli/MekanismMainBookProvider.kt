package mekanism.patchouli

import mekanism.client.MekanismKeyHandler
import mekanism.common.Mekanism
import mekanism.common.MekanismLang
import mekanism.common.content.gear.Modules
import mekanism.common.registries.MekanismBlocks.*
import mekanism.common.registries.MekanismFluids
import mekanism.common.registries.MekanismGases.*
import mekanism.common.registries.MekanismItems.*
import mekanism.common.resource.OreType
import mekanism.common.resource.PrimaryResource
import mekanism.common.resource.ResourceType
import mekanism.patchouli.dsl.invoke
import mekanism.patchouli.dsl.link
import net.minecraft.data.DataGenerator
import net.minecraft.data.DirectoryCache
import net.minecraft.item.ItemStack
import net.minecraft.util.Direction

/**
 * Created by Thiakil on 16/09/2020.
 */
class MekanismMainBookProvider(generator: DataGenerator): BasePatchouliProvider(generator, Mekanism.MODID) {
    override fun act(output: DirectoryCache) {
        output(Companion.bookId) {
            name = "Mekanism HandyGuide"
            locale = "en_us"
            landingText = "Here at Mekanism, Inc. we pride ourselves on our user-friendly creations, but sometimes a little nudge in the right direction is needed. Enter: the Mekanism HandyGuide - your handy dandy guide to the world of Mekanism."
            creativeTab = Mekanism.tabMekanism
            showProgress = false
            i18n = true//some item names etc
            subtitle = Mekanism.instance.versionNumber.toString()

            GuideCategory.ITEMS {
                name = "Items List"
                description = "A list of the items in Mekanism."
                icon = INFUSED_ALLOY
                sortNum = FORCED_ITEM_SORT_NUM

                GuideCategory.ITEMS_GEAR {
                    name = "Gear"
                    description = "Suit up, attack, or configure with these items."
                    icon = ELECTRIC_BOW

                    JETPACK("The Jetpack is an item that allows the player to fly, equippable in the chestplate slot. It uses Hydrogen gas as a fuel, of which it can store up to 24,000 mB.") {
                        text {
                            title = "Fueling"
                            text = "The Jetpack can be filled up wherever Hydrogen gas is outputted into a slot.$(br)" +
                                    "Here are a few examples:" +
                                    "$(li)It can be placed in the ${link(ELECTROLYTIC_SEPARATOR, "Electrolytic Separator's")} left output slot (where Hydrogen is outputted) after placing water in the machine's input slot" +
                                    "$(li)It can be placed in a ${link(GuideEntry.TANKS_GAS, "Chemical Tank's")} output slot.$(br2)" +
                                    "While worn, the Jetpack displays the Hydrogen remaining and the mode active in the bottom left corner."
                        }
                        text {
                            title = "Operation Modes"
                            text = "The Jetpack has three modes to choose from, which can be toggled by pressing the ${MekanismKeyHandler.chestModeSwitchKey()} key." +
                                    "$(li)$(bold)Regular$() (default): Press $(k:jump) to increase your height and release to fall. Note that you will take fall damage unless you carefully lower yourself to the ground." +
                                    "$(li)$(bold)Hover$(): Constant flight, without the need to level yourself like you do with Regular mode. Press $(k:jump) to increase altitude and press $(k:sneak) to decrease. Note that this mode constantly consumes Hydrogen, but at a reduced rate as compared to Regular mode." +
                                    "$(li)$(bold)Disabled$(): The Jetpack is disabled."
                        }
                        text {
                            title = "Tips"
                            text = "\$(li)The Jetpack cannot be worn with chestplate armor, since it uses the same slot, consider upgrading it to the ${link(ARMORED_JETPACK, "Armored Jetpack")} if you want protection." +
                                    "\$(li)The Jetpack emits fire particles; however, it will not set anything on fire."+
                                    "\$(li)If you want to maintain your altitude, choose Hover mode. If you want to ascend/descend rapidly, use Regular mode. If you want to conserve fuel while trekking across hills, mountains, consider Disabled mode."+
                                    "\$(li)The Jetpack can be paired with the Free Runners to protect against fall damage."
                        }
                    }
                    ATOMIC_DISASSEMBLER("The Atomic Disassembler is Mekanism's an all-in-one tool, essentially the ultimate, electronic version of the Paxel (working at any mining level). Also functions as a Hoe & Scoop (Forestry)$(p)The Atomic Disassembler has multiple modes that can be cycled with $(k:sneak) + right click.") {
                        text {
                            title = "Normal Mode"
                            text = "Base speed setting, single block, roughly equivalent to Efficiency II.$(p)Farmland tilling and Grass Path functions have been moved to the MekaTool ${link(Modules.FARMING_UNIT, "Farming Unit")}"
                        }
                        text {
                            title = "Slow Mode"
                            text = "Slower than Normal Mode, less power usage"
                        }
                        text {
                            title = "Fast Mode"
                            text = "Super mode, roughly equivalent to Efficiency V. Uses more energy to function."
                        }
                        text {
                            title = "Vein Mode"
                            text = "Like normal mode but will mine a vein of Ore or Log blocks (tagged with forge:ores or forge:logs) matching the start block."
                        }
                        text {
                            title = "Extended Vein Mining"
                            text = "Like Vein Mode, but works with any block."
                        }
                        text {
                            title = "Off"
                            text = "Functions as if out of power - no mining speed benefits or extended functionality."
                        }
                    }
                    ARMORED_JETPACK("The Armored Jetpack is an upgraded version of the ${link(JETPACK, "Jetpack")}. It is intended to provide 12 armor points, offering slightly better protection than a Diamond Chestplate with Protection IV. Numbers accurate as of Minecraft 1.7.10")
                    SCUBA_TANK("A piece of equipment worn in the chest armor slot that provides underwater respiration when a ${link(SCUBA_MASK, "Scuba Mask")} is worn. The Scuba Tank must be filled with Oxygen gas in order to function.") {
                        +"When you first put on the Scuba Tank, its oxygen supply will be turned off. In order to use it underwater you must turn it on by using the ${MekanismKeyHandler.chestModeSwitchKey()} button. Also, you must have the Gas Mask equipped in the helmet armor slot or you won't be able to breathe and will start to drown."
                        text {
                            title = "Tips"
                            text = "$(li)Any potion effect will instantly stop once you equip the Gas Mask and turn on the oxygen supply." +
                                    "$(li)Be sure to turn the oxygen supply OFF when you are above surface, otherwise you'll waste oxygen." +
                                    "$(li)Leave the oxygen supply on while underwater. You can't cheat to conserve oxygen by turning it on and off, because the oxygen will be consumed faster to refill the breath meter." +
                                    "$(li)The Scuba Tank will not work in Airless dimensions in some other mods, such as the outer space dimensions in Galacticraft (Moon, Mars, etc.)."
                        }
                    }
                    SCUBA_MASK("The Scuba Mask is an utility head armor piece, used in conjunction with the ${link(SCUBA_TANK, "Scuba Tank")} to breathe underwater.$(p)It can be enchanted with respiration and water breathing for extended use.")
                    CONFIGURATOR("The Configurator is a configuration tool for Mekanism machines & pipes.$(p)It comes with several different modes that you can switch between by sneaking and then pressing the Item Mode Switch Key (${MekanismKeyHandler.handModeSwitchKey()})") {
                        text("Configurate") {
                            text = "Mousing over a Mekanism machine or factory will show the color for that side, using the the Configuration Color Scheme. Right clicking will print a message announcing both the color and input/output mode. $(k:sneak) + Right Clicking will cycle through the valid colors for the given sub-mode."+
                                    "$(li)Grey is no connection (neither in nor out)." +
                                    "\$(li)Dark Red is input (items, gasses)." +
                                    "\$(li)Dark Blue is output (items, gasses)." +
                                    "\$(li)Green is for Energy input (items, cable)." +
                                    "\$(li)Purple is Infusion item input (for the Metallurgic Infuser)" +
                                    "\$(li)Yellow is for fluids (for the Pressurized Reaction Chamber)"
                        }
                        +("Additionally, you can interact with any of the cables, pipes, transporters, or tubes to set their connection type between machines/inventory and their redstone sensitivity. Right clicking on the center of the cable/pipe/transporter/tube will toggle sensitivity off/on (default is on). \$(k:sneak) + Right clicking on a segment between the center of the cable/etc. and machine will cycle between:" +
                                "$(li)Normal" +
                                "\$(li)Pull - try to take items, etc. from the machine" +
                                "\$(li)Push - try to put items, etc., into the machine" +
                                "\$(li)None - no connection. Will not try to push or pull items from the machine.")
                        text("Empty") {
                            text = "\$(k:sneak) + Right Clicking on the machine while in this mode will eject any and all items currently in the machine in random directions. It will not dump fluids or gasses."
                        }
                        text("Rotate") {
                            text = "Right clicking on a face will have that set as \"forward\" while \$(k:sneak) + Right clicking will have that set as \"back\" The Energy Cube can have its top and bottom faces designated as \"forward.\""
                        }
                        text("Wrench") {
                            text = "Behaves like a wrench from most other mods. Right click to rotate the machine clockwise on the ground, \$(k:sneak) + Right click to have the machine instantly pried loose as an item (works on cables and pipes, too!)"
                        }
                    }
                    ELECTRIC_BOW("Much like a normal bow, but uses energy instead of durability. Can also set arrows on fire (toggle with ${MekanismKeyHandler.handModeSwitchKey()}).")
                    FLAMETHROWER("The Flamethrower is a ranged weapon which uses Hydrogen gas as its fuel. It is fairly effective against mobs as it deals damage when they are directly hit with the stream and sets them on fire. It is most effective on large groups of mobs, where the user can hose down the entire group with fuel at a short distance.") {
                        text("Modes") {
                            text = "You can switch between three fire modes using \$(k:sneak) + ${MekanismKeyHandler.handModeSwitchKey()}. The modes are" +
                                    "$(li)\$(bold)Combat\$() - The default mode. Damages mobs and sets them on fire. Destroys any items on the ground. Does not set fire to blocks nor damage them." +
                                    "$(li)\$(bold)Heat\$() - Same as combat, but blocks/items that have a smelter recipe will be instantly converted into it. For example you can fire a short burst at iron ore block and a single ingot of iron will be dropped.1\n" +
                                    "$(li)\$(bold)Inferno\$() - Same as combat, but blocks that the stream hits will be hit with blast damage (like with creepers, ghasts, TNT) and will usually be destroyed. Nearby blocks will be set on fire."
                        }
                    }
                    FREE_RUNNERS("Free Runners are an item that allows players to ascend 1-block inclines automatically, as well as preventing fall damage as long as they are charged. A fall will reduce the item's charge, depending on how far the fall was.$(p)Can be toggled with ${MekanismKeyHandler.feetModeSwitchKey()}")
                    NETWORK_READER ("Sends information about the targeted pipe network to chat.")
                    PORTABLE_TELEPORTER("A player kept teleportation device. It can store power and like all Mekanism teleporters, energy drain increases with the distance the player teleports to.") {
                        text {
                            title = "Usage"
                            text = "Right-clicking with this device in hand will open a GUI similar to that of the full Teleporter, allowing instant travel to any Teleporters that the player has set up. The Portable Teleporter is capable of multidimensional travel.$(p)Note that in order for the Portable Teleporter to be functional, the complete Teleporter Portal structure does $(bold)not$() need to be built; only the Teleporter block must be present (and supplied with power)."
                        }
                    }
                    SEISMIC_READER ("The Seismic Reader is used in conjunction with the ${link(SEISMIC_VIBRATOR, "Seismic Vibrator")} to analyze the ground immediately around the vibrator, informing you of the blocks, by level, all the way to bedrock level.")
                    CANTEEN("The Canteen is used to store ${link(NUTRITIONAL_PASTE, "Nutritional Paste")} (total of 64 Buckets). When hungry, you can hold right click to drink some Nutritional Paste. Each hunger point (half a hunger bar) consumes 50mB of Nutritional Paste.")
                }
                GuideCategory.ITEMS_METAL_AND_ORE {
                    name = "Metals & Ores"
                    description = "Ore/Metal processing based materials."
                    icon = PROCESSED_RESOURCES.get(ResourceType.INGOT, PrimaryResource.OSMIUM)

                    entry(BRONZE_INGOT) {
                        name = "Bronze"
                        +"Bronze is an alloy of Copper and Tin."
                        spotlight(BRONZE_INGOT)
                        spotlight(BRONZE_DUST)
                        spotlight(BRONZE_NUGGET)
                    }

                    CHARCOAL_DUST("Crushed form of Charcoal")
                    COAL_DUST("Crushed form of Coal")
                    DIAMOND_DUST("Crushed form of Diamond")
                    EMERALD_DUST("Crushed form of Emerald")
                    OBSIDIAN_DUST("Crushed form of Obsidian")
                    QUARTZ_DUST("Crushed form of Quartz")
                    LAPIS_LAZULI_DUST("Crushed form of Lapis Lazuli")

                    LITHIUM_DUST("Crystallized form of ${link(LITHIUM, "Lithium")}")

                    entry(REFINED_GLOWSTONE_INGOT) {
                        name = "Refined Glowstone"
                        +"Refined glowstone is a stronger form of Glowstone, reinforced with Osmium in the ${link(OSMIUM_COMPRESSOR, "Osmium Compressor")}."
                        spotlight(REFINED_GLOWSTONE_INGOT)
                        spotlight(REFINED_GLOWSTONE_NUGGET)
                        spotlight(REFINED_GLOWSTONE_BLOCK)
                    }

                    entry(REFINED_OBSIDIAN_INGOT) {
                        name = "Refined Obsidian"
                        +"Harder obsidian? Unpossible!$(p)Obsidian reinforced with Osmium in the ${link(OSMIUM_COMPRESSOR, "Osmium Compressor")}. Can be used to form a Nether Portal"

                        spotlight(REFINED_OBSIDIAN_DUST)
                        spotlight(REFINED_OBSIDIAN_INGOT)
                        spotlight(REFINED_OBSIDIAN_NUGGET)
                        spotlight(REFINED_OBSIDIAN_BLOCK)
                    }

                    entry(STEEL_INGOT) {
                        name = "Steel"
                        +"Steel is a hardened metal used in most Mekanism constructions."
                        spotlight(ENRICHED_IRON, "Intermediate step in Mekanism Steel production.")
                        spotlight(STEEL_INGOT)
                        spotlight(STEEL_DUST)
                        spotlight(STEEL_NUGGET)
                        spotlight(STEEL_BLOCK)
                    }

                    SULFUR_DUST("Solidified sulfur, can be used to make ${link(SULFURIC_ACID, "Sulfuric Acid")}.")

                    entry(PROCESSED_RESOURCES.get(ResourceType.CLUMP, PrimaryResource.IRON)!!) {
                        name = "Clumps"
                        +"Clumps are part of the ${link(GuideEntry.ORE_TRIPLING, "3x Ore Processing")} pipeline and above."
                        PROCESSED_RESOURCES.row(ResourceType.CLUMP).values.forEach(this::spotlight)
                    }

                    entry(PROCESSED_RESOURCES.get(ResourceType.SHARD, PrimaryResource.IRON)!!) {
                        name = "Crystals"
                        +"Crystals are part of the ${link(GuideEntry.ORE_QUADRUPLING, "4x Ore Processing")} pipeline and above."
                        PROCESSED_RESOURCES.row(ResourceType.SHARD).values.forEach(this::spotlight)
                    }

                    entry(PROCESSED_RESOURCES.get(ResourceType.CRYSTAL, PrimaryResource.IRON)!!) {
                        name = "Crystals"
                        +"Crystals are part of the ${link(GuideEntry.ORE_QUINTUPLING, "5x Ore Processing")} pipeline and above."
                        PROCESSED_RESOURCES.row(ResourceType.CRYSTAL).values.forEach(this::spotlight)
                    }

                    DIRTY_NETHERITE_SCRAP("Dirty Netherite Scraps are part of the ore processing of Ancient Debris.")

                    entry(PROCESSED_RESOURCES.get(ResourceType.INGOT, PrimaryResource.OSMIUM)) {
                        name = "Osmium"
                        +"Osmium is a hard, brittle, bluish-white transition metal in the platinum group that is found as a trace element in alloys, mostly in platinum ores.$(p)Osmium is the densest stable element; it is approximately twice as dense as lead and slightly denser than iridium."
                        spotlight(ORES[OreType.OSMIUM]!!)
                        PROCESSED_RESOURCES.column(PrimaryResource.OSMIUM).values.forEach(this::spotlight)
                    }

                    entry(PROCESSED_RESOURCES.get(ResourceType.INGOT, PrimaryResource.COPPER)) {
                        name = "Copper"
                        +"Copper is a soft, malleable, and ductile metal with very high thermal and electrical conductivity. A freshly exposed surface of pure copper has a pinkish-orange color."
                        spotlight(ORES[OreType.COPPER]!!)
                        PROCESSED_RESOURCES.column(PrimaryResource.COPPER).values.forEach(this::spotlight)
                    }

                    entry(PROCESSED_RESOURCES.get(ResourceType.INGOT, PrimaryResource.TIN)) {
                        name = "Tin"
                        +"Tin is a silvery metal that characteristically has a faint yellow hue. Tin, like indium, is soft enough to be cut without much force."
                        spotlight(ORES[OreType.TIN]!!)
                        PROCESSED_RESOURCES.column(PrimaryResource.TIN).values.forEach(this::spotlight)
                    }

                    entry(PROCESSED_RESOURCES.get(ResourceType.INGOT, PrimaryResource.URANIUM)) {
                        name = "Uranium"
                        +"Uranium is a silvery-grey metal in the actinide series of the periodic table. A uranium atom has 92 protons and 92 electrons, of which 6 are valence electrons. Uranium is weakly radioactive because all isotopes of uranium are unstable; the half-lives of its naturally occurring isotopes range between 159,200 years and 4.5 billion years."
                        spotlight(ORES[OreType.URANIUM]!!)
                        PROCESSED_RESOURCES.column(PrimaryResource.URANIUM).values.forEach(this::spotlight)
                    }

                    entry(PROCESSED_RESOURCES.get(ResourceType.INGOT, PrimaryResource.LEAD)) {
                        name = "Lead"
                        +"Lead is a heavy metal that is denser than most common materials. Lead is soft and malleable, and also has a relatively low melting point."
                        spotlight(ORES[OreType.URANIUM]!!)
                        PROCESSED_RESOURCES.column(PrimaryResource.URANIUM).values.forEach(this::spotlight)
                    }

                    entry(FLUORITE_GEM) {
                        name = "Fluorite"
                        +"Fluorite (also called fluorspar) is the mineral form of calcium fluoride, CaF2. It belongs to the halide minerals group."
                        spotlight(ORES[OreType.FLUORITE]!!)
                        spotlight(FLUORITE_DUST)
                        spotlight(FLUORITE_GEM)
                    }

                }
                GuideCategory.ITEMS_UPGRADES {
                    name = "Upgrades"
                    description = "You gotta pump up them numbers, rookie. Increase various abilities of machines with these items.$(br)Insert via the machine's GUI, Upgrades tab."
                    icon = SPEED_UPGRADE

                    SPEED_UPGRADE("An upgrade to increate the running speed of a machine.") {
                        +"Note that every speed upgrade makes the machine 33% faster, and the Power Usage increases with ~77% (33²%), which makes for an increase in power usage for each operation with 33%.$(p)$(bold)The machine must have enough buffer to run one operation or it will not run at all."
                    }
                    ENERGY_UPGRADE("Upgrades the energy buffer of a machine and reduces its per-operation consumption.$(p)The ${link(ELECTROLYTIC_SEPARATOR, "Electrolytic Separator")} only receives a buffer increase.")
                    FILTER_UPGRADE("The Filter Upgrade is an upgrade that, when used in the ${link(ELECTRIC_PUMP, "Electric Pump")}, allows the Electric Pump to produce Heavy Water.")
                    MUFFLING_UPGRADE("Reduces the sound produced by a machine.")
                    GAS_UPGRADE("The gas upgrade allows you to increase the gas usage efficiency of a Mekanism machine which consumes gas.")
                    ANCHOR_UPGRADE("The Anchor Upgrade is a machine upgrade which keeps the chunk of the machine to which it is applied loaded. This is helpful for machines like the Digital Miner and Teleporter which must be in loaded chunks to function properly.$(p)$(bold)Compatible machines$()$(li)${link(DIGITAL_MINER, "Digital Miner")}$(li)${link(QUANTUM_ENTANGLOPORTER, "Quantum Entangloporter")}$(li)${link(GuideEntry.TELEPORTER, "Teleporter")}")
                }

                GuideEntry.ALLOYS {
                    name = "Alloys"
                    icon = INFUSED_ALLOY
                    +"Crafting components used to make tiered items. Can also be right clicked on Logistical Transporters, Mechanical Pipes, Pressurized Tubes, Thermodynamic Conductors, and Universal Cables to upgrade tiers in-world.$(p)Created in a ${link(METALLURGIC_INFUSER, "Metallurgic Infuser")}."
                    spotlight(INFUSED_ALLOY, "Redstone infused")
                    spotlight(REINFORCED_ALLOY, "Diamond infused")
                    spotlight(ATOMIC_ALLOY, "Refined Obsidian infused")
                }

                GuideEntry.CIRCUITS {
                    name = "Circuits"
                    icon = BASIC_CONTROL_CIRCUIT
                    +"Crafting components used to make tiered items. Created in the ${link(METALLURGIC_INFUSER, "Metallurgic Infuser")}."
                    spotlight(BASIC_CONTROL_CIRCUIT, "Osmium based.")
                    spotlight(ADVANCED_CONTROL_CIRCUIT, "Infused Alloy based.")
                    spotlight(ELITE_CONTROL_CIRCUIT, "Reinforced Alloy based.")
                    spotlight(ULTIMATE_CONTROL_CIRCUIT, "Atomic Alloy based.")
                }

                GuideEntry.INSTALLERS {
                    name = "Installers"
                    icon = BASIC_TIER_INSTALLER
                    +"Upgrade the tier of a block in world, without needing to put it in a crafting grid.$(p)Can upgrade factory machines, Bins, and Energy Cubes"
                    spotlight(BASIC_TIER_INSTALLER, "Upgrades block to basic tier. Used to turn machines into their factory variant.")
                    spotlight(ADVANCED_TIER_INSTALLER, "Upgrades block to Advanced tier. Requires block to be Basic tier.")
                    spotlight(ELITE_TIER_INSTALLER, "Upgrades block to Elite tier. Requires block to be Advanced tier.")
                    spotlight(ULTIMATE_TIER_INSTALLER, "Upgrades block to Ultimate tier. Requires block to be Elite tier.")
                }

                BIO_FUEL("A fuel made from plant material in a Crusher.$(p)Used in a Biofuel Generator (Mekanism Generators required) for power or ${link(PRESSURIZED_REACTION_CHAMBER, "Pressurized Reaction Chamber")} to produce Ethylene.") {
                    //todo override in mek generators' book generator?
                }
                CONFIGURATION_CARD("An item used to copy configuration data from one machine to another.$(p)To copy data to the card, $(k:sneak) + right click on the source machine, then right click the destination machine. Chat messages will inform you of the success/failure.$(p)Supported machines: ${link(DIGITAL_MINER, "Digital Miner")}, ${link(GuideEntry.ENERGY_CUBES, "Energy Cubes")}, ${link(FORMULAIC_ASSEMBLICATOR, "Formulaic Assemblicator")}, ${link(LOGISTICAL_SORTER, "Logistical Sorter")}, ${link(OREDICTIONIFICATOR, "Oredictionificator")}, and any machine with configurable sides.")
                CRAFTING_FORMULA("Used in the ${link(FORMULAIC_ASSEMBLICATOR, "Formulaic Assemblicator")} to encode a crafting recipe for automatic operation.")
            }// end items category

            GuideCategory.BLOCKS {
                name = "Blocks List"
                description = "A list of the blocks in Mekanism."
                icon = ULTIMATE_ENERGY_CUBE
                sortNum = FORCED_BLOCK_SORT_NUM

                GuideEntry.PIPES_LOGISTICAL {
                    name = "Logistical Transporters"
                    icon = BASIC_LOGISTICAL_TRANSPORTER
                    +"The Logistical Transporter is the basic item transport pipe for Mekanism logistics.$(br)With the Configurator the player can choose to \"paint\" the pipe with colors can can be detected by the pipe's color sorter by $(k:sneak) + right-clicking the center of a transporter with a Configurator.$(br2)It has 2 other cousins called the Diversion Transporter and the Restrictive Transporter."
                    spotlight(BASIC_LOGISTICAL_TRANSPORTER)
                    spotlight(ADVANCED_LOGISTICAL_TRANSPORTER)
                    spotlight(ELITE_LOGISTICAL_TRANSPORTER)
                    spotlight(ULTIMATE_LOGISTICAL_TRANSPORTER)
                }

                GuideEntry.PIPES_MECHANICAL {
                    name = "Mechanical Pipes"
                    icon = BASIC_MECHANICAL_PIPE
                    +"Mechanical Pipe is the fluid pipe for Mekanism logistics. It can be used to connect to any blocks with Fluid Handlers in them."
                    spotlight(BASIC_MECHANICAL_PIPE)
                    spotlight(ADVANCED_MECHANICAL_PIPE)
                    spotlight(ELITE_MECHANICAL_PIPE)
                    spotlight(ULTIMATE_MECHANICAL_PIPE)
                }

                GuideEntry.PIPES_GAS {
                    name = "Pressurized Tubes"
                    icon = BASIC_PRESSURIZED_TUBE
                    +"Pressurized Tubes are used to transport Gases. Similar to their mechanical counterparts, they can be upgraded to higher tiers to increase flow rate and capacity of pumped gases."
                    spotlight(BASIC_PRESSURIZED_TUBE)
                    spotlight(ADVANCED_PRESSURIZED_TUBE)
                    spotlight(ELITE_PRESSURIZED_TUBE)
                    spotlight(ULTIMATE_PRESSURIZED_TUBE)
                }

                GuideEntry.PIPES_HEAT {
                    name = "Thermodynamic Conductors"
                    icon = BASIC_THERMODYNAMIC_CONDUCTOR
                    +"Similar to the Universal Cable, the Thermodynamic Conductor is Mekanism's way of transferring power in the form of heat (essentially a heat pipe).$(2br)Transfer is lossy, depending on the biome the conductor is in. Warmer biomes have a higher transfer efficiency (less heat is lost), while colder biomes are lower (more heat is lost)."
                    spotlight(BASIC_THERMODYNAMIC_CONDUCTOR)
                    spotlight(ADVANCED_THERMODYNAMIC_CONDUCTOR)
                    spotlight(ELITE_THERMODYNAMIC_CONDUCTOR)
                    spotlight(ULTIMATE_THERMODYNAMIC_CONDUCTOR)
                }

                GuideEntry.PIPES_POWER {
                    name = "Universal Cables"
                    icon = BASIC_UNIVERSAL_CABLE
                    +"Universal Cables are Mekanism's way to transfer power. They are capable of transferring Mekanism's power Joules (J), as well as a variety of other power types such as Forge Energy (FE), Thermal Expansion's Redstone Flux (RF), Buildcraft's Minecraftjoules (MJ - display only), and Industrialcraft Energy Unit (EU). This flexibility allows players to mix power generation from different mods while still only using one type of cabling."
                    spotlight(BASIC_UNIVERSAL_CABLE)
                    spotlight(ADVANCED_UNIVERSAL_CABLE)
                    spotlight(ELITE_UNIVERSAL_CABLE)
                    spotlight(ULTIMATE_UNIVERSAL_CABLE)
                }

                GuideEntry.TANKS_LIQUID {
                    name = "Fluid Tanks"
                    icon = BASIC_FLUID_TANK
                    +"Tanks which store fluids. They can be placed as a block or used in Bucket mode ($(k:sneak) + $(k:${MekanismLang.KEY_HAND_MODE.translationKey}) to toggle)"
                    spotlight(BASIC_FLUID_TANK)
                    spotlight(ADVANCED_FLUID_TANK)
                    spotlight(ELITE_FLUID_TANK)
                    spotlight(ULTIMATE_FLUID_TANK)
                    spotlight(CREATIVE_FLUID_TANK)
                }

                GuideEntry.TANKS_GAS {
                    name = "Chemical Tanks"
                    icon = BASIC_CHEMICAL_TANK
                    +"Chemical Tanks are Mekanism's batteries for storing Gases and other chemicals. They can be placed as a block and interact with Pressurized Tubes. They come in four tiers, each increasing the storage capacity and output rate."
                    spotlight(BASIC_CHEMICAL_TANK)
                    spotlight(ADVANCED_CHEMICAL_TANK)
                    spotlight(ELITE_CHEMICAL_TANK)
                    spotlight(ULTIMATE_CHEMICAL_TANK)
                    spotlight(CREATIVE_CHEMICAL_TANK)
                }

                GuideEntry.ENERGY_CUBES {
                    name = "Energy Cubes"
                    icon = BASIC_ENERGY_CUBE
                    +"An Energy Cube is an advanced type of battery that is compatible with multiple energy systems. The Input/Output mode of the side can be configured in the GUI"
                    spotlight(BASIC_ENERGY_CUBE)
                    spotlight(ADVANCED_ENERGY_CUBE)
                    spotlight(ELITE_ENERGY_CUBE)
                    spotlight(ULTIMATE_ENERGY_CUBE)
                    spotlight(CREATIVE_ENERGY_CUBE)
                }

                GuideEntry.INDUCTION_CELL {
                    name = "Induction Cells"
                    icon = BASIC_INDUCTION_CELL
                    +"Induction Cells are components in the ${link(GuideEntry.INDUCTION, "Induction Matrix")}. Each cell increases the total energy storage of a Matrix. Note that this does not increase transfer rate; look to the ${link(GuideEntry.INDUCTION_PROVIDER, "Induction Providers")} for that."
                    spotlight(BASIC_INDUCTION_CELL, "Adds 8 GJ to the Matrix's capacity")
                    spotlight(ADVANCED_INDUCTION_CELL, "Adds 64 GJ to the Matrix's capacity.")
                    spotlight(ELITE_INDUCTION_CELL, "Adds 512 GJ to the Matrix's capacity")
                    spotlight(ULTIMATE_INDUCTION_CELL, "Adds 4 TJ to the Matrix's capacity")
                    crafting(BASIC_INDUCTION_CELL) {
                        secondaryRecipe = ADVANCED_INDUCTION_CELL
                    }
                    crafting(ELITE_INDUCTION_CELL) {
                        secondaryRecipe = ULTIMATE_INDUCTION_CELL
                    }

                }

                GuideEntry.INDUCTION_PROVIDER {
                    name = "Induction Providers"
                    icon = BASIC_INDUCTION_PROVIDER
                    +"The Induction Providers are used in the ${link(GuideEntry.INDUCTION, "Induction Matrix")} to determine how fast it is able to output energy through the Induction Port.$(2br)Using multiple Induction Providers in the same Induction Matrix will add extra output capacity, by adding their values together.$(2br)The total output value is for the entire multi-block structure, and not on a \"per port\" basis."
                    spotlight(BASIC_INDUCTION_PROVIDER, "Adds 256 kJ/t to output/input rate.")
                    spotlight(ADVANCED_INDUCTION_PROVIDER, "Adds 2.04 MJ/t to output/input rate.")
                    spotlight(ELITE_INDUCTION_PROVIDER, "Adds 16.38 MJ/t to output/input rate.")
                    spotlight(ULTIMATE_INDUCTION_PROVIDER, "Adds 131.07 MJ/t to output/input rate.")
                }

                GuideEntry.BINS{ name = "Bins"
                    icon = BASIC_BIN
                    +"Bins are storage blocks which can hold large amounts of a single item. It will retain its inventory when broken. Each tier increases the storage capacity.$(p)To store something in a Bin right-click any side while holding an item or stack. This will store what's in your hand."
                    +"Double right-click to put the complete amount of an item in your inventory to the bin.$(p)Left-click on the front of the bin to extract a stack. $(k:sneak)-click to extract a single item.$(p)Items can be piped into the bin from the top, and piped out from the bottom. NB: other mods' item handlers are not restricted in this manner."
                    +"If you $(k:sneak)-click the bin with a Configurator it will be placed into auto-eject mode. This is indicated by green accents on the front, top, and bottom. In this mode it will pump items out of the bottom automatically."
                    spotlight(BASIC_BIN, "Holds 4,096 items.")
                    spotlight(ADVANCED_BIN, "Holds 8,192 items.")
                    spotlight(ELITE_BIN, "Holds 32,768 items.")
                    spotlight(ULTIMATE_BIN, "Holds 262,144 items.")
                    spotlight(CREATIVE_BIN, "Holds an infinite amount, does not deplete when withdrawing items.")

                }
            }

            GuideCategory.MULTIBLOCKS {
                name = "Multiblocks"
                description = "Structures formed using multiple blocks."
                icon = THERMAL_EVAPORATION_CONTROLLER

                GuideEntry.THERMAL_EVAP {
                    name = "Thermal Evaporation Plant"
                    icon = THERMAL_EVAPORATION_CONTROLLER
                    +"The Thermal Evaporation Plant is a 4x4 base multiblock for producing one liquid from another by way of heat energy. Minimum height is 3, maximum is 18.$(p)Heat can be supplied passively, actively by solar, or externally supplied."

                    multiblock {
                        name = "TEP Test"
                        definition {
                            layer {
                                row { space();  +THERMAL_EVAPORATION_BLOCK; +THERMAL_EVAPORATION_BLOCK; space(); }
                                row { +THERMAL_EVAPORATION_BLOCK; space();                    space();                    +THERMAL_EVAPORATION_BLOCK }
                                row { +THERMAL_EVAPORATION_BLOCK; space();                    space();                    +THERMAL_EVAPORATION_BLOCK }
                                row { space();  +THERMAL_EVAPORATION_BLOCK; +THERMAL_EVAPORATION_BLOCK; space(); }
                            }
                            layer {
                                row { +THERMAL_EVAPORATION_BLOCK; +THERMAL_EVAPORATION_BLOCK; +THERMAL_EVAPORATION_BLOCK; +THERMAL_EVAPORATION_BLOCK }
                                row { +THERMAL_EVAPORATION_BLOCK; space();                    space();                    +THERMAL_EVAPORATION_BLOCK }
                                row { +THERMAL_EVAPORATION_BLOCK; space();                    space();                    THERMAL_EVAPORATION_CONTROLLER facing Direction.EAST }
                                row { +THERMAL_EVAPORATION_BLOCK; +THERMAL_EVAPORATION_BLOCK; +THERMAL_EVAPORATION_BLOCK; +THERMAL_EVAPORATION_BLOCK }
                            }
                            layer {
                                row { +THERMAL_EVAPORATION_BLOCK; +THERMAL_EVAPORATION_BLOCK; +THERMAL_EVAPORATION_BLOCK; +THERMAL_EVAPORATION_BLOCK }
                                row { +THERMAL_EVAPORATION_BLOCK; +THERMAL_EVAPORATION_BLOCK; +THERMAL_EVAPORATION_BLOCK; +THERMAL_EVAPORATION_BLOCK }
                                row { +THERMAL_EVAPORATION_BLOCK; +THERMAL_EVAPORATION_BLOCK; center(THERMAL_EVAPORATION_BLOCK); +THERMAL_EVAPORATION_BLOCK }
                                row { +THERMAL_EVAPORATION_BLOCK; +THERMAL_EVAPORATION_BLOCK; +THERMAL_EVAPORATION_BLOCK; +THERMAL_EVAPORATION_BLOCK }
                            }
                        }
                    }
                }
                GuideEntry.DYNAMIC_TANK {
                    name = "Dynamic Tank"
                    icon = DYNAMIC_VALVE
                    +"The blocks Dynamic Tank, Dynamic Glass, and Dynamic Valve make up the multi-block that is the Dynamic Tank, a fluid storage structure that can hold a large amount of a single type of fluid.$(p)Dynamic Tanks can be made in any size from 3x3x3 to 18x18x18, and does not need to be a cube."
                    +"A valid Dynamic Tank structure will flash with \"active redstone\" particles upon completion.$(p)Notes:$(li)All of the Dynamic Tank's borders must be made out of Dynamic Tank (not glass or valve)$(li)The tank's length, width, and height can be any number within the size limits - e.g. 3x4x5"
                }
                GuideEntry.TELEPORTER {
                    name = "Teleporter"
                    icon = TELEPORTER
                }
                GuideEntry.INDUCTION {
                    name = "Induction Matrix"
                    icon = BASIC_INDUCTION_CELL
                }
                GuideEntry.BOILER {
                    name = MekanismLang.BOILER.translationKey
                    icon = BOILER_VALVE
                }
            }

            GuideCategory.ORE_PROCESSING {
                name = "Ore Processing"
                description = "Get more ingots from your ore with these machine combinations."
                icon = ORES[OreType.OSMIUM]!!

                GuideEntry.ORE_DOUBLING {
                    name = "2x - Ore Doubling"
                    icon = ENRICHMENT_CHAMBER
                    +"To double your ores, simply use an ${link(ENRICHMENT_CHAMBER, "Enrichment Chamber")} to turn your ores into dusts, which can be smelted into ingots in a furnace or our patented ${link(ENERGIZED_SMELTER, "Energized Smelter")}"
                }
                GuideEntry.ORE_TRIPLING {
                    name = "3x - Ore Tripling"
                    icon = PURIFICATION_CHAMBER
                    +"To triple ores, convert them into clumps with a ${link(PURIFICATION_CHAMBER, "Purification Chamber")} (requires oxygen), then convert those into dirty dusts with a ${link(CRUSHER, "Crusher")}"
                    +"These dirty dusts can then be fed into your enrichment chamber"
                }
                GuideEntry.ORE_QUADRUPLING {
                    name = "4x - Ore Quadrupling"
                    icon = CHEMICAL_INJECTION_CHAMBER
                    +"Quadrupling your ores is significantly more complex than doubling or tripling, due to the need for ${link(HYDROGEN_CHLORIDE, "Hydrogen Chloride")}."
                    +"Once you have that set up, use a ${link(CHEMICAL_INJECTION_CHAMBER, "Chemical Injection Chamber")} to convert ores into shards, and feed those into your crusher."
                }
                GuideEntry.ORE_QUINTUPLING {
                    name = "5x - Ore Quintupling"
                    icon = CHEMICAL_DISSOLUTION_CHAMBER
                    +"Quintupling ores is quite complicated, however it can lead to huge riches, especially with a ${link(DIGITAL_MINER, "Digital Miner")}."
                    +"To quintuple ores, put them and ${link(SULFURIC_ACID, "Sulfuric Acid")} into a ${link(CHEMICAL_DISSOLUTION_CHAMBER, "Chemical Dissolution Chamber")}, which will convert them into slurry.  Clean the slurry in a ${link(CHEMICAL_WASHER, "Chemical Washer")}, then use a ${link(CHEMICAL_CRYSTALLIZER, "Chemical Crystallizer")} to turn them into crystals."
                    +"Put the crystals into your injection chamber."
                }
            }

            GuideCategory.LIQUIDS {
                name = "Liquids"
                description = "Splish splash, Robit's taking a bath. Warranty void when exposed to liquids."
                icon = ULTIMATE_FLUID_TANK

                GuideEntry.LIQUID_HEAVY_WATER {
                    name = "Heavy Water"
                    iconItem = ItemStack(MekanismFluids.HEAVY_WATER.bucket, 1)
                    +"Heavy Water is obtaind by having an ${link(ELECTRIC_PUMP, "Electric Pump")} with a ${link(FILTER_UPGRADE, "Filter Upgrade")} pump a regular water source."
                }
            }

            GuideCategory.CHEMICALS {
                name = "Chemicals"
                description = "Gasses and other chemicals. Transport these with ${link(GuideEntry.PIPES_GAS, "Pressurized Tubes")} and store them in ${link(GuideEntry.TANKS_GAS, "Chemical Tanks")} or a high capacity ${link(GuideEntry.DYNAMIC_TANK, "Dynamic Tank")}."
                icon = ULTIMATE_CHEMICAL_TANK

                BRINE {
                    +"The gaseous form of brine, made by putting ${link(SALT, "salt")} in a ${link(CHEMICAL_OXIDIZER, "Chemical Oxidizer")}, or by evaporating liquid brine in a ${link(ROTARY_CONDENSENTRATOR, "Rotary Condenstrator")}."
                    +"The Condenstrator can also turn it back into liquid brine, by pressing the \"Switch Operation\" toggle."
                }

                CHLORINE {
                    +"Chlorine is combined in a ${link(CHEMICAL_INFUSER, "Chemical Infuser")} along with ${link(HYDROGEN, "Hydrogen")} to make ${link(HYDROGEN_CHLORIDE, "Hydrogen Chloride")}."
                    +"It is produced by putting brine in an ${link(ELECTROLYTIC_SEPARATOR, "Electrolytic Separator")}, which also produces ${link(SODIUM, "Sodium")}"
                }

                //todo move to Generators book
                GuideEntry.CHEMICAL_DEUTERIUM {
                    name = "Deuterium"
                    icon = ULTIMATE_CHEMICAL_TANK
                    +"Deuterium is an isotope of ${link(HYDROGEN, "Hydrogen")} with an extra neutron, used in a ${link(GuideEntry.GENERATORS_FUSION, "Fusion Reactor")}."
                    +"It is made by putting heavy water in an ${link(ELECTROLYTIC_SEPARATOR, "Electrolytic Separator")}, which also produces ${link(OXYGEN, "oxygen")}"
                    +"Deuterium and ${link(GuideEntry.CHEMICAL_TRITIUM, "Tritium")} can be either injected straight into a reactor, or combined in a ${link(CHEMICAL_INFUSER, "Chemical Infuser")} to create ${link(GuideEntry.CHEMICAL_DT_FUEL, "D-T Fuel")}"
                    +"See ${link(GuideEntry.GENERATORS_FUSION, "Fusion Reactor")} for more details."
                }

                //todo move to Generators book
                GuideEntry.CHEMICAL_DT_FUEL {
                    name = "D-T Fuel"
                    icon = ULTIMATE_CHEMICAL_TANK
                    +"D-T Fuel is used to fill the hohlraum, and can also be injected into a ${link(GuideEntry.GENERATORS_FUSION, "Fusion Reactor")} for very high energy production"
                    +"Direct D-T injection is only feasible for very high fuel production, see ${link(GuideEntry.GENERATORS_FUSION, "Fusion Reactor")} for more details."
                }

                ETHENE {
                    +"Ethylene is the fuel for our ultra-efficient Gas-Burning generator.  It is a byproduct of ${link(HDPE_PELLET, "HDPE (High-Density Polyethylene)")}."
                    +"It is produced by the ${link(PRESSURIZED_REACTION_CHAMBER, "Pressurized Reaction Chamber")} when turning ${link(BIO_FUEL, "bio-fuel")} into ${link(SUBSTRATE, "substrate")}, using ${link(HYDROGEN, "hydrogen")} and water."
                }

                HYDROGEN {
                    +"Hydrogen is a carrier of energy.  It is produced by putting water in an ${link(ELECTROLYTIC_SEPARATOR, "Electrolytic Separator")}, and can be used as fuel in a Gas-Burning Generator, as well as a ${link(JETPACK, "Jetpack")} or ${link(ARMORED_JETPACK, "Armored Jetpack")} and ${link(FLAMETHROWER, "Flamethrower")}."
                    +"It is also used in a ${link(CHEMICAL_INFUSER, "Chemical Infuser")} to make ${link(HYDROGEN_CHLORIDE, "Hydrogen Chloride")}."
                }

                HYDROGEN_CHLORIDE {
                    +"Hydrogen Chloride is needed for the ${link(CHEMICAL_INJECTION_CHAMBER, "Injection Chamber")}, part of ${link(GuideEntry.ORE_QUADRUPLING, "Ore Quadrupling")}. It is created by putting ${link(CHLORINE, "Chlorine")} and ${link(HYDROGEN, "Hydrogen")} in a ${link(CHEMICAL_INFUSER, "Chemical Infuser")}."
                }

                LITHIUM {
                    +"Lithium is produced by ${link(GuideEntry.THERMAL_EVAP, "evaporating")} ${link(BRINE, "brine")}.  It is processed into ${link(GuideEntry.CHEMICAL_TRITIUM, "Tritium")} in a ${link(SOLAR_NEUTRON_ACTIVATOR, "Solar Neutron Activator")}."
                }

                OXYGEN {
                    +"Oxygen is produced by splitting water using an ${link(ELECTROLYTIC_SEPARATOR, "Electrolytic Separator")}, which also produces ${link(HYDROGEN, "Hydrogen")}"
                    +"It is used in the ${link(PURIFICATION_CHAMBER, "Purification Chamber")}, part of ${link(GuideEntry.ORE_TRIPLING, "Ore Tripling")}.  It is also combined with ${link(SULFUR_TRIOXIDE, "Sulfur Trioxide")} in the ${link(CHEMICAL_INFUSER, "Chemical Infuser")} to make ${link(SULFUR_DIOXIDE, "Sulfur Dioxide")}, as well as in the ${link(SCUBA_MASK, "Scuba Mask")}."
                }

                SODIUM {
                    +"Sodium is a coolant for the fission reactor.  it has a higher heat capacity than water and is needed for larger and/or more active reactors."
                    +"It is produced by splitting ${link(BRINE, "Brine")} into Sodium and ${link(CHLORINE, "Chlorine")} in an ${link(ELECTROLYTIC_SEPARATOR, "Electrolytic Separator")}"
                }

                SUPERHEATED_SODIUM {
                    +"${link(SODIUM, "Sodium")} that has been heated in a reactor. Remove the heat using a Boiler to return to normal Sodium"
                }

                SULFUR_DIOXIDE {
                    +"Sulfur dioxide is a step in ${link(SULFURIC_ACID, "Sulfuric Acid")} production."
                    +"It is created by putting sulfur in a ${link(CHEMICAL_OXIDIZER, "Chemical Oxidizer")} and used with ${link(OXYGEN, "Oxygen")} in the ${link(CHEMICAL_INFUSER, "Chemical Infuser")} to create ${link(SULFUR_TRIOXIDE, "Sulfur Trioxide")}."
                }

                SULFUR_TRIOXIDE {
                    name = "Sulfur Trioxide"
                    icon = ULTIMATE_CHEMICAL_TANK
                    +"Sulfur trioxide is another step in ${link(SULFURIC_ACID, "Sulfuric Acid")} production. It is produced by combining ${link(SULFUR_DIOXIDE, "Sulfur Dioxide")} and ${link(OXYGEN, "Oxygen")} in a ${link(CHEMICAL_INFUSER, "Chemical Infuser")}."
                    +"It is combined with ${link(WATER_VAPOR, "Water Vapor")} in a Chemical Infuser to create sulfuric acid."
                }

                SULFURIC_ACID {
                    +"Sulfuric acid is necesary for ${link(GuideEntry.ORE_QUINTUPLING, "Ore Quintupling")}.  It is used in the ${link(CHEMICAL_DISSOLUTION_CHAMBER, "Chemical Dissolution Chamber")} to dissolve ores into slurry."
                    +"It is created by combining ${link(SULFUR_TRIOXIDE, "Sulfur Trioxide")} and ${link(WATER_VAPOR, "Water Vapor")} in the ${link(CHEMICAL_INFUSER, "Chemical Infuser")}."
                }

                //todo move to generators
                GuideEntry.CHEMICAL_TRITIUM {
                    name = "Tritium"
                    icon = ULTIMATE_CHEMICAL_TANK
                    +"Tritium is another isotope of ${link(HYDROGEN, "Hydrogen")}, with two extra neturons.  It is used as fuel in the ${link(GuideEntry.GENERATORS_FUSION, "Fusion Reactor")}."
                    +"It can either be injected directly into the reactor along with ${link(GuideEntry.CHEMICAL_DEUTERIUM, "Deuterium")}, or combined into ${link(GuideEntry.CHEMICAL_DT_FUEL, "D-T Fuel")}.  The latter is only useful if production is very high, see ${link(GuideEntry.GENERATORS_FUSION, "Fusion Reactor")} for more information."
                }

                WATER_VAPOR {
                    +"Water vapor is created from water in the ${link(ROTARY_CONDENSENTRATOR, "Rotary Condenstrator")}. It is not as hot as Steam"
                    +"It is combined with ${link(SULFUR_TRIOXIDE, "Sulfur Trioxide")} in the ${link(CHEMICAL_INFUSER, "Chemical Infuser")} to create ${link(SULFURIC_ACID, "Sulfuric Acid")}.  It can also be combined with dirt in the ${link(CHEMICAL_INJECTION_CHAMBER, "Chemical Injection Chamber")} to make clay."
                }

                HYDROFLUORIC_ACID {
                    +"An acid make from ${link(SULFURIC_ACID, "Sulfuric Acid")} and ${link(FLUORITE_GEM, "Fluorite")}, used to make ${link(URANIUM_HEXAFLUORIDE, "Uranium Hexafluoride")}"
                }

                URANIUM_OXIDE {
                    +"Oxidized form of ${link(YELLOW_CAKE_URANIUM, "Yellow Cake Uranium")}, used to make ${link(URANIUM_HEXAFLUORIDE, "Uranium Hexafluoride")}"
                }

                URANIUM_HEXAFLUORIDE {
                    +"Used to make ${link(FISSILE_FUEL, "Fissile Fuel")} in the ${link(ISOTOPIC_CENTRIFUGE, "Isotopic Centrifuge")}."
                }

                LIQUID_OSMIUM {
                    +"Chemical form of Osmium used in the ${link(OSMIUM_COMPRESSOR, "Osmium Compressor")}."
                }

                FISSILE_FUEL {
                    +"Fuel source made from ${link(URANIUM_HEXAFLUORIDE, "Uranium Hexafluoride")} in the ${link(ISOTOPIC_CENTRIFUGE, "Isotopic Centrifuge")}."
                    text {
                        text = "Used in the Fission Reactor."
                        flag = "false"//todo check if generators installed
                    }
                }

                NUCLEAR_WASTE {
                    +"Waste product of used ${link(FISSILE_FUEL, "Fissile Fuel")}."
                }

                SPENT_NUCLEAR_WASTE {
                    +"Biproduct of reprocessed ${link(NUCLEAR_WASTE, "Nuclear Waste")}"
                }

                PLUTONIUM {
                    +"First stage of ${link(PLUTONIUM_PELLET, "Plutonium Pellet")} production."
                }

                POLONIUM {
                    +"First stage of ${link(POLONIUM_PELLET, "Polonium Pellet")} production."
                }

                ANTIMATTER {
                    +"Late-game chemical used for advanced processes."
                }

                NUTRITIONAL_PASTE {
                    +"Food, tasty paste form. Used with the ${link(CANTEEN, "Canteen")}, or in the ${link(Modules.NUTRITIONAL_INJECTION_UNIT, "Nutritional Injection Unit")}."
                }

            }
        }
    }

    companion object {
        private const val FORCED_ITEM_SORT_NUM = 98
        private const val FORCED_BLOCK_SORT_NUM = 99
        const val bookId = "mekanism"
    }
}