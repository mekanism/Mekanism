package mekanism.common.block.attribute;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.function.Function;
import mekanism.common.lib.math.Pos3D;
import net.minecraft.core.particles.ParticleOptions;

public class AttributeParticleFX implements Attribute {

    private final List<Function<Random, Particle>> particleFunctions = new ArrayList<>();

    public List<Function<Random, Particle>> getParticleFunctions() {
        return particleFunctions;
    }

    public AttributeParticleFX add(ParticleOptions type, Function<Random, Pos3D> posSupplier) {
        particleFunctions.add(random -> new Particle(type, posSupplier.apply(random)));
        return this;
    }

    public static class Particle {

        private final ParticleOptions type;
        private final Pos3D pos;

        protected Particle(ParticleOptions type, Pos3D pos) {
            this.type = type;
            this.pos = pos;
        }

        public ParticleOptions getType() {
            return type;
        }

        public Pos3D getPos() {
            return pos;
        }
    }
}
