package mekanism.patchouli.content

import mekanism.common.MekanismLang
import mekanism.common.registries.MekanismBlocks.*
import mekanism.patchouli.GuideCategory
import mekanism.patchouli.GuideEntry
import mekanism.patchouli.dsl.PatchouliBook
import net.minecraft.core.Direction

fun PatchouliBook.multiblocks() {
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
            +THERMAL_EVAPORATION_CONTROLLER
            +THERMAL_EVAPORATION_VALVE
            +THERMAL_EVAPORATION_BLOCK
        }
        GuideEntry.DYNAMIC_TANK {
            name = "Dynamic Tank"
            icon = DYNAMIC_VALVE
            +"The blocks Dynamic Tank, Dynamic Glass, and Dynamic Valve make up the multi-block that is the Dynamic Tank, a fluid storage structure that can hold a large amount of a single type of fluid.$(p)Dynamic Tanks can be made in any size from 3x3x3 to 18x18x18, and does not need to be a cube."
            +"A valid Dynamic Tank structure will flash with \"active redstone\" particles upon completion.$(p)Notes:$(li)All of the Dynamic Tank's borders must be made out of Dynamic Tank (not glass or valve)$(li)The tank's length, width, and height can be any number within the size limits - e.g. 3x4x5"
            +DYNAMIC_TANK
            +DYNAMIC_VALVE
        }
        GuideEntry.TELEPORTER {
            name = "Teleporter"
            icon = TELEPORTER
            +TELEPORTER_FRAME
        }
        GuideEntry.INDUCTION {
            name = "Induction Matrix"
            icon = BASIC_INDUCTION_CELL
            +INDUCTION_CASING
            +INDUCTION_PORT
            +BASIC_INDUCTION_CELL
            +ADVANCED_INDUCTION_CELL
            +ELITE_INDUCTION_CELL
            +ULTIMATE_INDUCTION_CELL
            +BASIC_INDUCTION_PROVIDER
            +ADVANCED_INDUCTION_PROVIDER
            +ELITE_INDUCTION_PROVIDER
            +ULTIMATE_INDUCTION_PROVIDER
        }
        GuideEntry.BOILER {
            name = MekanismLang.BOILER.translationKey
            icon = BOILER_VALVE
            +SUPERHEATING_ELEMENT
            +PRESSURE_DISPERSER
            +BOILER_CASING
            +BOILER_VALVE
        }
    }
}