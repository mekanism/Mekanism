package mekanism.patchouli

import mekanism.patchouli.dsl.IGuideCategory
import java.util.Locale
import mekanism.common.Mekanism
import net.minecraft.Util

enum class GeneratorsGuideCategory : IGuideCategory {
    GENERATORS;

    override val id: String = name.lowercase(Locale.ROOT)
    private val translationKey: String = Util.makeDescriptionId("guidebook", Mekanism.rl("category.$id"))
    override val translationKeyName: String = "$translationKey.name"
    override val translationKeyDescription: String = "$translationKey.description"

}