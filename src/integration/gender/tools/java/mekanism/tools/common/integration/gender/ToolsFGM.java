package mekanism.tools.common.integration.gender;

import mekanism.common.integration.MekanismHooks;
import mekanism.common.integration.gender.MekanismGenderArmor;
import mekanism.tools.common.MekanismTools;
import mekanism.tools.common.registries.ToolsItems;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;

@Mod(value = MekanismTools.MODID, depends = MekanismHooks.GENDER_MOD_ID)
public class ToolsFGM {

    public ToolsFGM(IEventBus modEventBus) {
        modEventBus.addListener(RegisterCapabilitiesEvent.class, event -> {
            MekanismGenderArmor.register(event, new MekanismGenderArmor(0.9F), ToolsItems.BRONZE_ARMOR.chestplate());
            MekanismGenderArmor.register(event, new MekanismGenderArmor(0.6F, 0.1F), ToolsItems.LAPIS_LAZULI_ARMOR.chestplate());
            MekanismGenderArmor.register(event, new MekanismGenderArmor(1), ToolsItems.OSMIUM_ARMOR.chestplate());
            MekanismGenderArmor.register(event, new MekanismGenderArmor(0.95F), ToolsItems.REFINED_GLOWSTONE_ARMOR.chestplate());
            MekanismGenderArmor.register(event, new MekanismGenderArmor(1), ToolsItems.REFINED_OBSIDIAN_ARMOR.chestplate());
            MekanismGenderArmor.register(event, new MekanismGenderArmor(1), ToolsItems.STEEL_ARMOR.chestplate());
        });
    }
}