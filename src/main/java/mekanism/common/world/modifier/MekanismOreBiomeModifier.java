package mekanism.common.world.modifier;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import mekanism.common.registration.impl.SetupFeatureDeferredRegister.MekFeature;
import mekanism.common.registries.MekanismBiomeModifierSerializers;
import mekanism.common.registries.MekanismFeatures;
import mekanism.common.resource.ore.OreType;
import mekanism.common.world.ResizableOreFeature;
import mekanism.common.world.ResizableOreFeatureConfig;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.levelgen.GenerationStep.Decoration;
import net.minecraftforge.common.world.BiomeGenerationSettingsBuilder;
import net.minecraftforge.common.world.BiomeModifier;
import net.minecraftforge.common.world.ModifiableBiomeInfo.BiomeInfo;

public record MekanismOreBiomeModifier(HolderSet<Biome> biomes, Decoration step, OreType oreType) implements BiomeModifier {

    @Override
    public void modify(Holder<Biome> biome, Phase phase, BiomeInfo.Builder builder) {
        if (phase == Phase.ADD && biomes.contains(biome)) {
            BiomeGenerationSettingsBuilder generation = builder.getGenerationSettings();
            for (MekFeature<ResizableOreFeatureConfig, ResizableOreFeature> feature : MekanismFeatures.ORES.get(oreType)) {
                generation.addFeature(step, feature.placedFeature());
            }
        }
    }

    @Override
    public Codec<? extends BiomeModifier> codec() {
        return MekanismBiomeModifierSerializers.ORE_MODIFIER.get();
    }

    public static Codec<MekanismOreBiomeModifier> makeCodec() {
        return RecordCodecBuilder.create(builder -> builder.group(
              Biome.LIST_CODEC.fieldOf("biomes").forGetter(MekanismOreBiomeModifier::biomes),
              Decoration.CODEC.fieldOf("step").forGetter(MekanismOreBiomeModifier::step),
              OreType.CODEC.fieldOf("oreType").forGetter(MekanismOreBiomeModifier::oreType)
        ).apply(builder, MekanismOreBiomeModifier::new));
    }
}