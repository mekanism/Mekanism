package mekanism.common.integration.crafttweaker;

import mekanism.common.Mekanism;
import mekanism.common.integration.MekanismHooks;
import mekanism.common.integration.crafttweaker.content.CrTContentUtils;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.data.loading.DatagenModLoader;

@Mod(value = Mekanism.MODID, depends = MekanismHooks.CRAFT_TWEAKER_MOD_ID)
public class MekanismCrTIntegration {

    public MekanismCrTIntegration(IEventBus modEventBus) {
        if (!DatagenModLoader.isRunningDataGen()) {
            //Register our CrT listener at lowest priority to try and ensure they get later ids than our normal registries
            modEventBus.addListener(EventPriority.LOWEST, CrTContentUtils::registerCrTContent);
        }
    }
}