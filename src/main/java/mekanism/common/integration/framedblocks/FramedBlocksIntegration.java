package mekanism.common.integration.framedblocks;

import mekanism.common.Mekanism;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.neoforge.client.event.ModelEvent;
import net.neoforged.neoforge.client.event.RegisterParticleProvidersEvent;
import net.neoforged.neoforge.fluids.FluidType;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import xfacthd.framedblocks.api.camo.CamoContainerFactory;
import xfacthd.framedblocks.api.util.FramedConstants;

public final class FramedBlocksIntegration {

    private static final DeferredRegister<CamoContainerFactory<?>> CAMO_FACTORIES = DeferredRegister.create(
            FramedConstants.CAMO_CONTAINER_FACTORY_REGISTRY_KEY,
            Mekanism.MODID
    );
    private static final DeferredRegister<ParticleType<?>> PARTICLE_TYPES = DeferredRegister.create(Registries.PARTICLE_TYPE, Mekanism.MODID);

    static final DeferredHolder<CamoContainerFactory<?>, ChemicalCamoContainerFactory> CHEMICAL_FACTORY =
            CAMO_FACTORIES.register("chemical", ChemicalCamoContainerFactory::new);
    static final DeferredHolder<ParticleType<?>, ChemicalParticleOptions.Type> CHEMICAL_PARTICLE =
            PARTICLE_TYPES.register("chemical", ChemicalParticleOptions.Type::new);

    public static void init(IEventBus modBus) {
        CAMO_FACTORIES.register(modBus);
        PARTICLE_TYPES.register(modBus);

        if (FMLEnvironment.dist.isClient()) {
            ClientEvents.init(modBus);
        }
    }

    public static final class Constants {

        /**
         * The amount of a given chemical to consume when applying it to a framed block
         */
        public static final int CHEMICAL_AMOUNT = FluidType.BUCKET_VOLUME;
        /**
         * A dummy model used for generating a baked model from a given chemical's texture
         * when applying it as a camo to a framed block
         */
        public static final ResourceLocation CHEMICAL_DUMMY_MODEL = Mekanism.rl("chemical/dummy");
    }

    static final class ClientEvents {

        static void init(IEventBus modBus) {
            modBus.addListener(ClientEvents::onRegisterAdditionalModels);
            modBus.addListener(ClientEvents::onModelLoadingCompleted);
            modBus.addListener(ClientEvents::onRegisterParticleProviders);
        }

        private static void onRegisterAdditionalModels(ModelEvent.RegisterAdditional event) {
            event.register(ChemicalModel.BARE_MODEL);
        }

        private static void onModelLoadingCompleted(ModelEvent.BakingCompleted event) {
            ChemicalCamoClientHandler.clearModelCache();
        }

        private static void onRegisterParticleProviders(RegisterParticleProvidersEvent event) {
            event.registerSpecial(CHEMICAL_PARTICLE.get(), new ChemicalSpriteParticle.Provider());
        }
    }
}
