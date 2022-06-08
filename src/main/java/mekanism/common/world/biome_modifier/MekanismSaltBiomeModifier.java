package mekanism.common.world.biome_modifier;

import com.mojang.serialization.Codec;
import mekanism.common.registries.MekanismBiomeModifierSerializers;
import mekanism.common.registries.MekanismFeatures;
import net.minecraft.core.Holder;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.levelgen.GenerationStep;
import net.minecraftforge.common.world.BiomeGenerationSettingsBuilder;
import net.minecraftforge.common.world.BiomeModifier;
import net.minecraftforge.common.world.ModifiableBiomeInfo.BiomeInfo;

public class MekanismSaltBiomeModifier implements BiomeModifier {

    private static final MekanismSaltBiomeModifier INSTANCE = new MekanismSaltBiomeModifier();

    private MekanismSaltBiomeModifier() {
    }

    @Override
    public void modify(Holder<Biome> biome, Phase phase, BiomeInfo.Builder builder) {
        //TODO - 1.19: Figure out a way to represent all overworld biomes
        if (phase == Phase.ADD /*&& this.biomes.contains(biome)*/) {
            BiomeGenerationSettingsBuilder generation = builder.getGenerationSettings();
            generation.addFeature(GenerationStep.Decoration.UNDERGROUND_ORES, MekanismFeatures.SALT.placedFeature());
        }
    }

    @Override
    public Codec<? extends BiomeModifier> codec() {
        return MekanismBiomeModifierSerializers.SALT_MODIFIER.get();
    }

    public static Codec<MekanismSaltBiomeModifier> makeCodec() {
        return Codec.unit(INSTANCE);
    }
}