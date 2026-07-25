package mekanism.client.integration.framedblocks;

import mekanism.common.Mekanism;
import mekanism.common.integration.MekanismHooks;
import mekanism.common.integration.framedblocks.MekanismFramedBlocks;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.client.event.RegisterParticleProvidersEvent;

@Mod(value = Mekanism.MODID, dist = Dist.CLIENT, depends = MekanismHooks.FRAMED_BLOCKS_MOD_ID)
public class MekanismFramedBlocksClient {

    public MekanismFramedBlocksClient(IEventBus modEventBus) {
        modEventBus.addListener(RegisterParticleProvidersEvent.class, MekanismFramedBlocksClient::onRegisterParticleProviders);
    }

    private static void onRegisterParticleProviders(RegisterParticleProvidersEvent event) {
        event.registerSpecial(MekanismFramedBlocks.CHEMICAL_PARTICLE.get(), new ChemicalSpriteParticle.Provider());
    }
}