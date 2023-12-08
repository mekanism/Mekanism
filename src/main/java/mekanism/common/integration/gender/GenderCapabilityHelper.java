package mekanism.common.integration.gender;

import mekanism.common.Mekanism;
import mekanism.common.registries.MekanismItems;
import net.minecraft.world.item.ArmorItem;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;

public class GenderCapabilityHelper {

    public static void addGenderCapability(RegisterCapabilitiesEvent event, ArmorItem item) {
        //Validate the mod is loaded and that this is for the correct slot
        if (Mekanism.hooks.WildfireGenderModLoaded && item.getType() == ArmorItem.Type.CHESTPLATE) {
            if (item == MekanismItems.HAZMAT_GOWN.asItem()) {
                MekanismGenderArmor.HAZMAT.register(event, item);
            } else if (item == MekanismItems.JETPACK.asItem() || item == MekanismItems.SCUBA_TANK.asItem()) {
                MekanismGenderArmor.OPEN_FRONT.register(event, item);
            } else if (item == MekanismItems.ARMORED_JETPACK.asItem() || item == MekanismItems.MEKASUIT_BODYARMOR.asItem()) {
                MekanismGenderArmor.HIDES_BREASTS.register(event, item);
            }
        }
    }
}