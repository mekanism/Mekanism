package mekanism.common.registration.impl;

import javax.annotation.Nonnull;
import mekanism.common.registration.WrappedRegistryObject;
import net.minecraft.particles.IParticleData;
import net.minecraft.particles.ParticleType;
import net.minecraftforge.fml.RegistryObject;

public class ParticleTypeRegistryObject<PARTICLE extends IParticleData, TYPE extends ParticleType<PARTICLE>> extends WrappedRegistryObject<TYPE> {

    public ParticleTypeRegistryObject(RegistryObject<TYPE> registryObject) {
        super(registryObject);
    }

    @Nonnull
    public TYPE getParticleType() {
        return get();
    }
}