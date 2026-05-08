package mekanism.common.integration.framedblocks;

import com.mojang.serialization.MapCodec;
import mekanism.api.SerializationConstants;
import mekanism.api.chemical.ChemicalResource;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import org.jetbrains.annotations.NotNull;

record ChemicalParticleOptions(ChemicalResource chemical) implements ParticleOptions {

    //TODO - 26.1: Should this be an optional codec or not?
    private static final MapCodec<ChemicalParticleOptions> CODEC = ChemicalResource.OPTIONAL_CODEC.xmap(
          ChemicalParticleOptions::new,
          ChemicalParticleOptions::chemical
    ).fieldOf(SerializationConstants.CHEMICAL);
    private static final StreamCodec<RegistryFriendlyByteBuf, ChemicalParticleOptions> STREAM_CODEC = ChemicalResource.STREAM_CODEC.map(
          ChemicalParticleOptions::new,
          ChemicalParticleOptions::chemical
    );

    @NotNull
    @Override
    public ParticleType<?> getType() {
        return FramedBlocksIntegration.CHEMICAL_PARTICLE.value();
    }

    static final class Type extends ParticleType<ChemicalParticleOptions> {

        Type() {
            super(false);
        }

        @NotNull
        @Override
        public MapCodec<ChemicalParticleOptions> codec() {
            return CODEC;
        }

        @NotNull
        @Override
        public StreamCodec<? super RegistryFriendlyByteBuf, ChemicalParticleOptions> streamCodec() {
            return STREAM_CODEC;
        }
    }
}
