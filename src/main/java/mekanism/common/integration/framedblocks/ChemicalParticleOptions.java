package mekanism.common.integration.framedblocks;

import com.mojang.serialization.MapCodec;
import mekanism.api.chemical.Chemical;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;

record ChemicalParticleOptions(Chemical chemical) implements ParticleOptions {

    static final MapCodec<ChemicalParticleOptions> CODEC = Chemical.CODEC
            .xmap(ChemicalParticleOptions::new, ChemicalParticleOptions::chemical)
            .fieldOf("chemical");
    static final StreamCodec<RegistryFriendlyByteBuf, ChemicalParticleOptions> STREAM_CODEC =
            Chemical.STREAM_CODEC.map(ChemicalParticleOptions::new, ChemicalParticleOptions::chemical);

    @Override
    public ParticleType<?> getType() {
        return FramedBlocksIntegration.CHEMICAL_PARTICLE.value();
    }

    static final class Type extends ParticleType<ChemicalParticleOptions> {

        Type() {
            super(false);
        }

        @Override
        public MapCodec<ChemicalParticleOptions> codec() {
            return CODEC;
        }

        @Override
        public StreamCodec<? super RegistryFriendlyByteBuf, ChemicalParticleOptions> streamCodec() {
            return STREAM_CODEC;
        }
    }
}
