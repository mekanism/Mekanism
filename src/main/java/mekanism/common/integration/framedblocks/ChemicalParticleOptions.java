package mekanism.common.integration.framedblocks;

import com.mojang.serialization.MapCodec;
import mekanism.api.SerializationConstants;
import mekanism.api.chemical.Chemical;
import net.minecraft.core.Holder;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import org.jetbrains.annotations.NotNull;

record ChemicalParticleOptions(Holder<Chemical> chemical) implements ParticleOptions {

    private static final MapCodec<ChemicalParticleOptions> CODEC = Chemical.HOLDER_CODEC.xmap(
          ChemicalParticleOptions::new,
          ChemicalParticleOptions::chemical
    ).fieldOf(SerializationConstants.CHEMICAL);
    private static final StreamCodec<RegistryFriendlyByteBuf, ChemicalParticleOptions> STREAM_CODEC = Chemical.HOLDER_STREAM_CODEC.map(
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
