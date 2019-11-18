package mekanism.common.registration.impl;

import javax.annotation.Nonnull;
import mekanism.common.registration.WrappedRegistryObject;
import net.minecraft.particles.IParticleData;
import net.minecraft.particles.ParticleType;
import net.minecraftforge.fml.RegistryObject;

public class ParticleTypeRegistryObject<PARTICLE extends IParticleData> extends WrappedRegistryObject<ParticleType<PARTICLE>> {

    public ParticleTypeRegistryObject(RegistryObject<ParticleType<PARTICLE>> registryObject) {
        super(registryObject);
    }

    @Nonnull
    public ParticleType<PARTICLE> getParticleType() {
        return get();
    }
}