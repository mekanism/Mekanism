package mekanism.common.world.biome_modifier;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import mekanism.common.registration.impl.SetupFeatureDeferredRegister.MekFeature;
import mekanism.common.registries.MekanismBiomeModifierSerializers;
import mekanism.common.registries.MekanismFeatures;
import mekanism.common.resource.ore.OreType;
import mekanism.common.world.ResizableOreFeature;
import mekanism.common.world.ResizableOreFeatureConfig;
import net.minecraft.core.Holder;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.levelgen.GenerationStep;
import net.minecraftforge.common.world.BiomeGenerationSettingsBuilder;
import net.minecraftforge.common.world.BiomeModifier;
import net.minecraftforge.common.world.ModifiableBiomeInfo.BiomeInfo;

//TODO - 1.19: Generate our modifiers so that they are actually loaded/added from the datapack
public record MekanismOreBiomeModifier(OreType oreType) implements BiomeModifier {

    @Override
    public void modify(Holder<Biome> biome, Phase phase, BiomeInfo.Builder builder) {
        //TODO - 1.19: Figure out a way to represent all overworld biomes
        if (phase == Phase.ADD /*&& this.biomes.contains(biome)*/) {
            BiomeGenerationSettingsBuilder generation = builder.getGenerationSettings();
            for (MekFeature<ResizableOreFeatureConfig, ResizableOreFeature> feature : MekanismFeatures.ORES.get(oreType)) {
                generation.addFeature(GenerationStep.Decoration.UNDERGROUND_ORES, feature.placedFeature());
            }
        }
    }

    @Override
    public Codec<? extends BiomeModifier> codec() {
        return MekanismBiomeModifierSerializers.ORE_MODIFIER.get();
    }

    public static Codec<MekanismOreBiomeModifier> makeCodec() {
        return RecordCodecBuilder.create(builder -> builder.group(
              OreType.CODEC.fieldOf("oreType").forGetter(MekanismOreBiomeModifier::oreType)
        ).apply(builder, MekanismOreBiomeModifier::new));
    }
}