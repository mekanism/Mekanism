package mekanism.tools.common.integration.gender;

import mekanism.common.Mekanism;
import mekanism.common.integration.gender.MekanismGenderArmor;
import mekanism.tools.common.item.ItemMekanismArmor;
import mekanism.tools.common.registries.ToolsItems;
import net.minecraft.world.item.ArmorItem;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;

public class ToolsGenderCapabilityHelper {

    public static void addGenderCapability(ItemMekanismArmor item, RegisterCapabilitiesEvent event) {
        //Validate the mod is loaded and that this is for the correct slot
        if (Mekanism.hooks.WildfireGenderModLoaded && item.getType() == ArmorItem.Type.CHESTPLATE) {
            if (item == ToolsItems.BRONZE_CHESTPLATE.asItem()) {
                ArmorSettings.BRONZE.register(event, item);
            } else if (item == ToolsItems.LAPIS_LAZULI_CHESTPLATE.asItem()) {
                ArmorSettings.LAPIS_LAZULI.register(event, item);
            } else if (item == ToolsItems.OSMIUM_CHESTPLATE.asItem()) {
                ArmorSettings.OSMIUM.register(event, item);
            } else if (item == ToolsItems.REFINED_GLOWSTONE_CHESTPLATE.asItem()) {
                ArmorSettings.REFINED_GLOWSTONE.register(event, item);
            } else if (item == ToolsItems.REFINED_OBSIDIAN_CHESTPLATE.asItem()) {
                ArmorSettings.REFINED_OBSIDIAN.register(event, item);
            } else if (item == ToolsItems.STEEL_CHESTPLATE.asItem()) {
                ArmorSettings.STEEL.register(event, item);
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