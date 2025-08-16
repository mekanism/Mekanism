package mekanism.tools.common.integration.gender;

import mekanism.common.Mekanism;
import mekanism.common.integration.gender.MekanismGenderArmor;
import mekanism.tools.common.MekanismTools;
import mekanism.tools.common.registries.ToolsItems;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;

@EventBusSubscriber(modid = MekanismTools.MODID)
public class ToolsGenderCapabilityHelper {

    @SubscribeEvent
    public static void addGenderCapability(RegisterCapabilitiesEvent event) {
        if (Mekanism.hooks.genderMod.isLoaded()) {
            ArmorSettings.BRONZE.register(event, ToolsItems.BRONZE_CHESTPLATE);
            ArmorSettings.LAPIS_LAZULI.register(event, ToolsItems.LAPIS_LAZULI_CHESTPLATE);
            ArmorSettings.OSMIUM.register(event, ToolsItems.OSMIUM_CHESTPLATE);
            ArmorSettings.REFINED_GLOWSTONE.register(event, ToolsItems.REFINED_GLOWSTONE_CHESTPLATE);
            ArmorSettings.REFINED_OBSIDIAN.register(event, ToolsItems.REFINED_OBSIDIAN_CHESTPLATE);
            ArmorSettings.STEEL.register(event, ToolsItems.STEEL_CHESTPLATE);
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