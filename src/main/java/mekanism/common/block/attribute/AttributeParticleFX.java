package mekanism.common.block.attribute;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.util.RandomSource;
import net.minecraft.world.phys.Vec3;

public class AttributeParticleFX implements Attribute {

    private final List<Function<RandomSource, Particle>> particleFunctions = new ArrayList<>();

    public List<Function<RandomSource, Particle>> getParticleFunctions() {
        return particleFunctions;
    }

    public AttributeParticleFX addDense(ParticleOptions type, int density, Function<RandomSource, Vec3> posSupplier) {
        Function<RandomSource, Particle> particleFunction = random -> new Particle(type, posSupplier.apply(random));
        for (int i = 0; i < density; i++) {
            particleFunctions.add(particleFunction);
        }
        return this;
    }

    public AttributeParticleFX add(ParticleOptions type, Function<RandomSource, Vec3> posSupplier) {
        particleFunctions.add(random -> new Particle(type, posSupplier.apply(random)));
        return this;
    }

    public static class Particle {

        private final ParticleOptions type;
        private final Vec3 pos;

        protected Particle(ParticleOptions type, Vec3 pos) {
            this.type = type;
            this.pos = pos;
        }

        public ParticleOptions getType() {
            return type;
        }

        public Vec3 getPos() {
            return pos;
        }
    }
}
