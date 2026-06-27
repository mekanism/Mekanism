package mekanism.common.integration.projecte;

import mekanism.common.Mekanism;
import mekanism.common.integration.MekanismHooks;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModList;
import net.neoforged.fml.common.Mod;

@Mod(Mekanism.MODID)
public class MekanismProjectECompat {

    public MekanismProjectECompat(IEventBus modEventBus) {
        if (ModList.get().isLoaded(MekanismHooks.PROJECTE_MOD_ID)) {
            MekanismNormalizedSimpleStacks.NSS_SERIALIZERS.register(modEventBus);
        }
    }
}