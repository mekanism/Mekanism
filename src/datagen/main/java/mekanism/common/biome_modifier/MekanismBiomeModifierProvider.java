package mekanism.common.biome_modifier;

import java.util.function.BiConsumer;
import mekanism.common.Mekanism;
import mekanism.common.resource.ore.OreType;
import mekanism.common.util.EnumUtils;
import mekanism.common.world.biome_modifier.MekanismOreBiomeModifier;
import mekanism.common.world.biome_modifier.MekanismSaltBiomeModifier;
import net.minecraft.core.HolderSet;
import net.minecraft.core.Registry;
import net.minecraft.data.DataGenerator;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BiomeTags;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.levelgen.GenerationStep;
import net.minecraftforge.common.world.BiomeModifier;

public class MekanismBiomeModifierProvider extends BaseBiomeModifierProvider {

    public MekanismBiomeModifierProvider(DataGenerator gen) {
        super(gen, Mekanism.MODID);
    }

    @Override
    protected void getModifiers(Registry<Biome> biomeRegistry, BiConsumer<BiomeModifier, ResourceLocation> consumer) {
        HolderSet.Named<Biome> isOverworldTag = new HolderSet.Named<>(biomeRegistry, BiomeTags.IS_OVERWORLD);
        for (OreType oreType : EnumUtils.ORE_TYPES) {
            consumer.accept(new MekanismOreBiomeModifier(isOverworldTag, GenerationStep.Decoration.UNDERGROUND_ORES, oreType), Mekanism.rl(oreType.getSerializedName()));
        }
        consumer.accept(new MekanismSaltBiomeModifier(isOverworldTag, GenerationStep.Decoration.UNDERGROUND_ORES), Mekanism.rl("salt"));
    }
}