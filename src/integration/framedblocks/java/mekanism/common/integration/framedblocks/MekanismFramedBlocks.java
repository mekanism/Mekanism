package mekanism.common.integration.framedblocks;

import io.github.xfacthd.framedblocks.api.camo.CamoContainerFactory;
import io.github.xfacthd.framedblocks.api.util.FramedConstants;
import mekanism.common.Mekanism;
import mekanism.common.integration.MekanismHooks;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.registries.Registries;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

@Mod(value = Mekanism.MODID, depends = MekanismHooks.FRAMED_BLOCKS_MOD_ID)
public class MekanismFramedBlocks {

    private static final DeferredRegister<CamoContainerFactory<?>> CAMO_FACTORIES = DeferredRegister.create(FramedConstants.Registries.CAMO_CONTAINER_FACTORY_REGISTRY_KEY, Mekanism.MODID);
    private static final DeferredRegister<ParticleType<?>> PARTICLE_TYPES = DeferredRegister.create(Registries.PARTICLE_TYPE, Mekanism.MODID);

    static final DeferredHolder<CamoContainerFactory<?>, ChemicalCamoContainerFactory> CHEMICAL_FACTORY = CAMO_FACTORIES.register("chemical", ChemicalCamoContainerFactory::new);
    public static final DeferredHolder<ParticleType<?>, ChemicalParticleOptions.Type> CHEMICAL_PARTICLE = PARTICLE_TYPES.register("chemical", ChemicalParticleOptions.Type::new);

    public MekanismFramedBlocks(IEventBus modEventBus) {
        CAMO_FACTORIES.register(modEventBus);
        PARTICLE_TYPES.register(modEventBus);
    }
}