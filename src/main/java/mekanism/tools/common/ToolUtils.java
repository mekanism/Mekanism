package mekanism.tools.common;

import mekanism.common.MekanismItems;
import net.minecraft.init.Items;
import net.minecraft.item.Item.ToolMaterial;
import net.minecraft.item.ItemArmor.ArmorMaterial;
import net.minecraft.item.ItemStack;

public class ToolUtils {

    public static ItemStack getRepairStack(ToolMaterial material) {
        if (material == MekanismTools.toolOBSIDIAN || material == MekanismTools.toolOBSIDIAN2) {
            return new ItemStack(MekanismItems.Ingot, 1, 0);
        } else if (material == MekanismTools.toolLAZULI || material == MekanismTools.toolLAZULI2) {
            return new ItemStack(Items.DYE, 1, 4);
        } else if (material == MekanismTools.toolOSMIUM || material == MekanismTools.toolOSMIUM2) {
            return new ItemStack(MekanismItems.Ingot, 1, 1);
        } else if (material == MekanismTools.toolBRONZE || material == MekanismTools.toolBRONZE2) {
            return new ItemStack(MekanismItems.Ingot, 1, 2);
        } else if (material == MekanismTools.toolGLOWSTONE || material == MekanismTools.toolGLOWSTONE2) {
            return new ItemStack(MekanismItems.Ingot, 1, 3);
        } else if (material == MekanismTools.toolSTEEL || material == MekanismTools.toolSTEEL2) {
            return new ItemStack(MekanismItems.Ingot, 1, 4);
        }
        return material.getRepairItemStack();
    }

    public static ItemStack getRepairStack(ArmorMaterial material) {
        if (material == MekanismTools.armorOBSIDIAN) {
            return new ItemStack(MekanismItems.Ingot, 1, 0);
        } else if (material == MekanismTools.armorLAZULI) {
            return new ItemStack(Items.DYE, 1, 4);
        } else if (material == MekanismTools.armorOSMIUM) {
            return new ItemStack(MekanismItems.Ingot, 1, 1);
        } else if (material == MekanismTools.armorBRONZE) {
            return new ItemStack(MekanismItems.Ingot, 1, 2);
        } else if (material == MekanismTools.armorGLOWSTONE) {
            return new ItemStack(MekanismItems.Ingot, 1, 3);
        } else if (material == MekanismTools.armorSTEEL) {
            return new ItemStack(MekanismItems.Ingot, 1, 4);
        }
        return material.getRepairItemStack();
    }
}