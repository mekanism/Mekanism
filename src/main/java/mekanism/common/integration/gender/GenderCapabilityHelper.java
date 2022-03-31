package mekanism.common.integration.gender;

import mekanism.common.Mekanism;
import mekanism.common.capabilities.ItemCapabilityWrapper;
import mekanism.common.registries.MekanismItems;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ArmorItem;

public class GenderCapabilityHelper {

    public static void addGenderCapability(ArmorItem item, ItemCapabilityWrapper wrapper) {
        //Validate the mod is loaded and that this is for the correct slot
        if (Mekanism.hooks.WildfireGenderModLoaded && item.getSlot() == EquipmentSlot.CHEST) {
            MekanismGenderArmor genderArmor = null;
            if (item == MekanismItems.HAZMAT_GOWN.asItem()) {
                genderArmor = MekanismGenderArmor.HAZMAT;
            } else if (item == MekanismItems.JETPACK.asItem() || item == MekanismItems.SCUBA_TANK.asItem()) {
                genderArmor = MekanismGenderArmor.OPEN_FRONT;
            } else if (item == MekanismItems.ARMORED_JETPACK.asItem() || item == MekanismItems.MEKASUIT_BODYARMOR.asItem()) {
                genderArmor = MekanismGenderArmor.HIDES_BREASTS;
            }
            if (genderArmor != null) {
                wrapper.add(genderArmor);
            }
        }
    }
}