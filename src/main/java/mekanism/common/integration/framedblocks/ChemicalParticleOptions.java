package mekanism.common.integration.framedblocks;

import com.mojang.serialization.MapCodec;
import mekanism.api.SerializationConstants;
import mekanism.api.chemical.ChemicalResource;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;

record ChemicalParticleOptions(ChemicalResource chemical) implements ParticleOptions {

    private static final MapCodec<ChemicalParticleOptions> CODEC = ChemicalResource.OPTIONAL_CODEC.xmap(
          ChemicalParticleOptions::new,
          ChemicalParticleOptions::chemical
    ).fieldOf(SerializationConstants.CHEMICAL);
    private static final StreamCodec<RegistryFriendlyByteBuf, ChemicalParticleOptions> STREAM_CODEC = ChemicalResource.STREAM_CODEC.map(
          ChemicalParticleOptions::new,
          ChemicalParticleOptions::chemical
    );

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
