package mekanism.tools.common;

import mekanism.common.MekanismItems;
import mekanism.common.config.MekanismConfig;
import mekanism.common.config.ToolsConfig.ArmorBalance;
import mekanism.common.config.ToolsConfig.ToolBalance;
import net.minecraft.init.Items;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.Item.ToolMaterial;
import net.minecraft.item.ItemArmor.ArmorMaterial;
import net.minecraft.item.ItemStack;
import net.minecraft.util.SoundEvent;
import net.minecraftforge.common.util.EnumHelper;

public enum Materials {
    OBSIDIAN("OBSIDIAN", MekanismConfig.current().tools.toolOBSIDIAN, MekanismConfig.current().tools.toolOBSIDIAN2, MekanismConfig.current().tools.armorOBSIDIAN, SoundEvents.ITEM_ARMOR_EQUIP_IRON),
    LAZULI("LAZULI", MekanismConfig.current().tools.toolLAZULI, MekanismConfig.current().tools.toolLAZULI2, MekanismConfig.current().tools.armorLAZULI, SoundEvents.ITEM_ARMOR_EQUIP_DIAMOND),
    OSMIUM("OSMIUM", MekanismConfig.current().tools.toolOSMIUM, MekanismConfig.current().tools.toolOSMIUM2, MekanismConfig.current().tools.armorOSMIUM, SoundEvents.ITEM_ARMOR_EQUIP_IRON),
    BRONZE("BRONZE", MekanismConfig.current().tools.toolBRONZE, MekanismConfig.current().tools.toolBRONZE2, MekanismConfig.current().tools.armorBRONZE, SoundEvents.ITEM_ARMOR_EQUIP_IRON),
    GLOWSTONE("GLOWSTONE", MekanismConfig.current().tools.toolGLOWSTONE, MekanismConfig.current().tools.toolGLOWSTONE2, MekanismConfig.current().tools.armorGLOWSTONE, SoundEvents.ITEM_ARMOR_EQUIP_IRON),
    STEEL("STEEL", MekanismConfig.current().tools.toolSTEEL, MekanismConfig.current().tools.toolSTEEL2, MekanismConfig.current().tools.armorSTEEL, SoundEvents.ITEM_ARMOR_EQUIP_IRON);

    private final ToolMaterial material;
    private final ToolMaterial paxelMaterial;
    private final ArmorMaterial armorMaterial;
    private final float axeDamage;
    private final float axeSpeed;

    Materials(String materialName, ToolBalance material, ToolBalance paxelMaterial, ArmorBalance armorMaterial, SoundEvent equipSound) {
        this.material = getToolMaterial(materialName, material);
        this.paxelMaterial = getToolMaterial(materialName + "2", paxelMaterial);
        this.armorMaterial = EnumHelper.addArmorMaterial(materialName, "TODO", armorMaterial.durability.val(), new int[]{
              armorMaterial.feetProtection.val(), armorMaterial.legsProtection.val(), armorMaterial.chestProtection.val(), armorMaterial.headProtection.val(),
              }, armorMaterial.enchantability.val(), equipSound, armorMaterial.toughness.val());
        this.axeDamage = material.axeAttackDamage.val();
        this.axeSpeed = material.axeAttackSpeed.val();
    }

    public void setRepairItem(ItemStack repairStack) {
        material.setRepairItem(repairStack);
        paxelMaterial.setRepairItem(repairStack);
        armorMaterial.setRepairItem(repairStack);
    }

    public ToolMaterial getMaterial() {
        return material;
    }

    public ToolMaterial getPaxelMaterial() {
        return paxelMaterial;
    }

    public ArmorMaterial getArmorMaterial() {
        return armorMaterial;
    }

    public float getAxeDamage() {
        return axeDamage;
    }

    public float getAxeSpeed() {
        return axeSpeed;
    }

    private static ToolMaterial getToolMaterial(String enumName, ToolBalance config) {
        return EnumHelper.addToolMaterial(enumName, config.harvestLevel.val(), config.maxUses.val(), config.efficiency.val(), config.damage.val(), config.enchantability.val());
    }

    public static void addRepairItems() {
        OBSIDIAN.setRepairItem(new ItemStack(MekanismItems.Ingot, 1, 0));
        OSMIUM.setRepairItem(new ItemStack(MekanismItems.Ingot, 1, 1));
        BRONZE.setRepairItem(new ItemStack(MekanismItems.Ingot, 1, 2));
        GLOWSTONE.setRepairItem(new ItemStack(MekanismItems.Ingot, 1, 3));
        STEEL.setRepairItem(new ItemStack(MekanismItems.Ingot, 1, 4));
        LAZULI.setRepairItem(new ItemStack(Items.DYE, 1, 4));
    }
}