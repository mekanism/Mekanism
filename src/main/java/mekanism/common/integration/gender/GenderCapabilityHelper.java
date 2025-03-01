package mekanism.common.integration.gender;

import mekanism.common.Mekanism;
import mekanism.common.registries.MekanismItems;
import net.minecraft.world.item.ArmorItem;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;

public class GenderCapabilityHelper {

    public static void addGenderCapability(RegisterCapabilitiesEvent event, ArmorItem item) {
        //Validate the mod is loaded and that this is for the correct slot
        if (Mekanism.hooks.WildfireGenderModLoaded && item.getType() == ArmorItem.Type.CHESTPLATE) {
            if (MekanismItems.HAZMAT_GOWN.is(item)) {
                MekanismGenderArmor.HAZMAT.register(event, item);
            } else if (MekanismItems.JETPACK.is(item) || MekanismItems.SCUBA_TANK.is(item)) {
                MekanismGenderArmor.OPEN_FRONT.register(event, item);
            } else if (MekanismItems.ARMORED_JETPACK.is(item) || MekanismItems.MEKASUIT_BODYARMOR.is(item)) {
                MekanismGenderArmor.HIDES_BREASTS.register(event, item);
            }
        }
    }
}