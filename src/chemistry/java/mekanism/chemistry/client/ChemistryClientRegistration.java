package mekanism.chemistry.client;

import mekanism.chemistry.client.gui.GuiAirCompressor;
import mekanism.chemistry.client.gui.GuiFractionatingDistillerController;
import mekanism.chemistry.common.MekanismChemistry;
import mekanism.chemistry.common.registries.ChemistryContainerTypes;
import mekanism.chemistry.common.registries.ChemistryFluids;
import mekanism.client.ClientRegistrationUtil;
import mekanism.common.registration.impl.FluidRegistryObject;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.world.inventory.MenuType;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;

@Mod.EventBusSubscriber(modid = MekanismChemistry.MODID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.MOD)
public class ChemistryClientRegistration {

    private ChemistryClientRegistration() {
    }


    @SubscribeEvent
    public static void init(FMLClientSetupEvent event) {
        //Fluids (translucent)
        for (FluidRegistryObject<?, ?, ?, ?> fluidRO : ChemistryFluids.FLUIDS.getAllFluids()) {
            ClientRegistrationUtil.setRenderLayer(RenderType.translucent(), fluidRO);
        }
    }

    @SubscribeEvent
    public static void registerContainers(RegistryEvent.Register<MenuType<?>> event) {
        ClientRegistrationUtil.registerScreen(ChemistryContainerTypes.AIR_COMPRESSOR, GuiAirCompressor::new);
        ClientRegistrationUtil.registerScreen(ChemistryContainerTypes.FRACTIONATING_DISTILLER_CONTROLLER, GuiFractionatingDistillerController::new);
    }
}
