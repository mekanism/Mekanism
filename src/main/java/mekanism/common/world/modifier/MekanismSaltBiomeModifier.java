package mekanism.common.world.modifier;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import mekanism.common.registries.MekanismBiomeModifierSerializers;
import mekanism.common.registries.MekanismFeatures;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.levelgen.GenerationStep.Decoration;
import net.minecraftforge.common.world.BiomeGenerationSettingsBuilder;
import net.minecraftforge.common.world.BiomeModifier;
import net.minecraftforge.common.world.ModifiableBiomeInfo.BiomeInfo;

public record MekanismSaltBiomeModifier(HolderSet<Biome> biomes, Decoration step) implements BiomeModifier {

    @Override
    public void modify(Holder<Biome> biome, Phase phase, BiomeInfo.Builder builder) {
        if (phase == Phase.ADD && biomes.contains(biome)) {
            BiomeGenerationSettingsBuilder generation = builder.getGenerationSettings();
            generation.addFeature(step, MekanismFeatures.SALT.placedFeature());
        }
    }

    @Override
    public Codec<? extends BiomeModifier> codec() {
        return MekanismBiomeModifierSerializers.SALT_MODIFIER.get();
    }

    public static Codec<MekanismSaltBiomeModifier> makeCodec() {
        return RecordCodecBuilder.create(builder -> builder.group(
              Biome.LIST_CODEC.fieldOf("biomes").forGetter(MekanismSaltBiomeModifier::biomes),
              Decoration.CODEC.fieldOf("step").forGetter(MekanismSaltBiomeModifier::step)
        ).apply(builder, MekanismSaltBiomeModifier::new));
    }
}