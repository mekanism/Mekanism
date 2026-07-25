package mekanism.common.integration.gender;

import mekanism.common.Mekanism;
import mekanism.common.integration.MekanismHooks;
import mekanism.common.registries.MekanismItems;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;

@Mod(value = Mekanism.MODID, depends = MekanismHooks.GENDER_MOD_ID)
public class MekanismFGM {

    public MekanismFGM(IEventBus modEventBus) {
        modEventBus.addListener(RegisterCapabilitiesEvent.class, event -> {
            MekanismGenderArmor.register(event, new MekanismGenderArmor(0.5F, 0.25F, false), MekanismItems.HAZMAT_GOWN);
            MekanismGenderArmor.register(event, new MekanismGenderArmor(false, false, 0, 0, false),
                  MekanismItems.JETPACK, MekanismItems.SCUBA_TANK);
            MekanismGenderArmor.register(event, new MekanismGenderArmor(true, true, 0, 0, false),
                  MekanismItems.ARMORED_JETPACK, MekanismItems.MEKASUIT_BODYARMOR);
        });
    }
}