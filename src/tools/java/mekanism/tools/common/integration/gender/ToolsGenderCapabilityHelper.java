package mekanism.tools.common.integration.gender;

import mekanism.common.Mekanism;
import mekanism.common.capabilities.ItemCapabilityWrapper;
import mekanism.common.integration.gender.MekanismGenderArmor;
import mekanism.tools.common.item.ItemMekanismArmor;
import mekanism.tools.common.registries.ToolsItems;
import net.minecraft.world.entity.EquipmentSlot;

public class ToolsGenderCapabilityHelper {

    public static void addGenderCapability(ItemMekanismArmor item, ItemCapabilityWrapper wrapper) {
        //Validate the mod is loaded and that this is for the correct slot
        if (Mekanism.hooks.WildfireGenderModLoaded && item.getSlot() == EquipmentSlot.CHEST) {
            MekanismGenderArmor genderArmor = null;
            if (item == ToolsItems.BRONZE_CHESTPLATE.asItem()) {
                genderArmor = ArmorSettings.BRONZE;
            } else if (item == ToolsItems.LAPIS_LAZULI_CHESTPLATE.asItem()) {
                genderArmor = ArmorSettings.LAPIS_LAZULI;
            } else if (item == ToolsItems.OSMIUM_CHESTPLATE.asItem()) {
                genderArmor = ArmorSettings.OSMIUM;
            } else if (item == ToolsItems.REFINED_GLOWSTONE_CHESTPLATE.asItem()) {
                genderArmor = ArmorSettings.REFINED_GLOWSTONE;
            } else if (item == ToolsItems.REFINED_OBSIDIAN_CHESTPLATE.asItem()) {
                genderArmor = ArmorSettings.REFINED_OBSIDIAN;
            } else if (item == ToolsItems.STEEL_CHESTPLATE.asItem()) {
                genderArmor = ArmorSettings.STEEL;
            }
            if (genderArmor != null) {
                wrapper.add(genderArmor);
            }
        }
    }

    private static class ArmorSettings {

        private static final MekanismGenderArmor BRONZE = new MekanismGenderArmor(0.9F);
        private static final MekanismGenderArmor LAPIS_LAZULI = new MekanismGenderArmor(0.6F, 0.1F);
        private static final MekanismGenderArmor OSMIUM = new MekanismGenderArmor(1);
        private static final MekanismGenderArmor REFINED_GLOWSTONE = new MekanismGenderArmor(0.95F);
        private static final MekanismGenderArmor REFINED_OBSIDIAN = new MekanismGenderArmor(1);
        private static final MekanismGenderArmor STEEL = new MekanismGenderArmor(1);
    }
}