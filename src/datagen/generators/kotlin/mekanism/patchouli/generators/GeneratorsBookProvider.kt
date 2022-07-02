package mekanism.patchouli.generators

import mekanism.common.Mekanism
import mekanism.generators.common.MekanismGenerators
import mekanism.generators.common.registries.GeneratorsBlocks
import mekanism.generators.common.registries.GeneratorsBlocks.*
import mekanism.generators.common.registries.GeneratorsItems
import mekanism.patchouli.BasePatchouliProvider
import mekanism.patchouli.generators.GeneratorsGuideCategory.GENERATORS
import mekanism.patchouli.generators.GeneratorsGuideEntry.FISSION
import mekanism.patchouli.generators.GeneratorsGuideEntry.FUSION
import mekanism.patchouli.GuideCategory
import mekanism.patchouli.MekanismMainBookProvider
import net.minecraft.data.CachedOutput
import net.minecraft.data.DataGenerator
import net.minecraft.data.HashCache
import net.minecraft.resources.ResourceLocation

/**
 * Created by Thiakil on 20/02/2021.
 */
class GeneratorsBookProvider(generator: DataGenerator): BasePatchouliProvider(generator, MekanismGenerators.MODID) {
    override fun run(output: CachedOutput) {
        output("generators") {
            extend(Mekanism.MODID, MekanismMainBookProvider.bookId)
            name = "Mekanism Generators"
            locale = "en_us"
            existingCategory(GuideCategory.MULTIBLOCKS, ResourceLocation(Mekanism.MODID, MekanismMainBookProvider.bookId)) {
                FUSION {
                    name = "Fusion Reactor"
                    icon = GeneratorsBlocks.FUSION_REACTOR_CONTROLLER
                }
                FISSION {
                    name = "Fission Reactor"
                    icon = GeneratorsBlocks.FISSION_FUEL_ASSEMBLY
                }
            }

            GENERATORS {
                name = "Generators"
                description = "Generate electricity with these single block generators."
                icon = GeneratorsItems.SOLAR_PANEL
                SOLAR_GENERATOR {

                }
            }
        }
    }
}