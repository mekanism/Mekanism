package mekanism.common.particle;

import java.util.ArrayList;
import java.util.List;
import mekanism.common.Mekanism;
import net.minecraft.particles.IParticleData;
import net.minecraft.particles.ParticleType;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.registries.IForgeRegistry;

public class MekanismParticleType {

    private static final List<ParticleType<?>> types = new ArrayList<>();

    public static final ParticleType<LaserParticleData> LASER = create("laser", new LaserParticleType());

    private static <T extends IParticleData> ParticleType<T> create(String name, ParticleType<T> type) {
        type.setRegistryName(new ResourceLocation(Mekanism.MODID, name));
        types.add(type);
        return type;
    }

    public static void registerParticles(IForgeRegistry<ParticleType<?>> registry) {
        types.forEach(registry::register);
        //TODO: Should the list be cleared afterwards as it isn't really needed anymore after registration
    }
}