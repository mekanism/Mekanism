package mekanism.common.integration.opencomputers;

import mekanism.common.Mekanism;
import mekanism.common.integration.MekanismHooks;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;

@Mod(value = Mekanism.MODID, depends = MekanismHooks.OPEN_COMPUTERS_MOD_ID)
public class MekanismOpenComputers {

    public MekanismOpenComputers(IEventBus modEventBus) {
        //If OpenComputers is loaded add the capability for it
        modEventBus.addListener(RegisterCapabilitiesEvent.class, OCEnvironmentHelper::addBoundingComputerCapabilities);
    }
}