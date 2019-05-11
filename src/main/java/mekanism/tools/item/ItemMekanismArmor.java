package mekanism.tools.item;

import java.util.List;
import java.util.Locale;
import javax.annotation.Nonnull;
import mekanism.client.render.ModelCustomArmor;
import mekanism.common.Mekanism;
import mekanism.common.MekanismItems;
import mekanism.common.util.LangUtils;
import mekanism.common.util.StackUtils;
import mekanism.tools.common.MekanismTools;
import mekanism.tools.common.ToolsItems;
import net.minecraft.client.model.ModelBiped;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.init.Items;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemArmor;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class ItemMekanismArmor extends ItemArmor {

    public ItemMekanismArmor(ArmorMaterial enumarmormaterial, int renderIndex, EntityEquipmentSlot slot) {
        super(enumarmormaterial, renderIndex, slot);
        setCreativeTab(Mekanism.tabMekanism);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void addInformation(ItemStack itemstack, World world, List<String> list, ITooltipFlag flag) {
        list.add(LangUtils.localize("tooltip.hp") + ": " + (itemstack.getMaxDamage() - itemstack.getItemDamage()));
    }

    @Override
    public String getArmorTexture(ItemStack stack, Entity entity, EntityEquipmentSlot slot, String type) {
        int layer = (slot == EntityEquipmentSlot.LEGS) ? 2 : 1;
        return "mekanism:armor/" + getArmorMaterial().name().toLowerCase(Locale.ROOT) + "_" + layer + ".png";
    }

    @Override
    public boolean getIsRepairable(ItemStack stack1, @Nonnull ItemStack stack2) {
        return StackUtils.equalsWildcard(getRepairStack(), stack2) || super.getIsRepairable(stack1, stack2);
    }

    private ItemStack getRepairStack() {
        if (getArmorMaterial() == MekanismTools.armorOBSIDIAN) {
            return new ItemStack(MekanismItems.Ingot, 1, 0);
        } else if (getArmorMaterial() == MekanismTools.armorLAZULI) {
            return new ItemStack(Items.DYE, 1, 4);
        } else if (getArmorMaterial() == MekanismTools.armorOSMIUM) {
            return new ItemStack(MekanismItems.Ingot, 1, 1);
        } else if (getArmorMaterial() == MekanismTools.armorBRONZE) {
            return new ItemStack(MekanismItems.Ingot, 1, 2);
        } else if (getArmorMaterial() == MekanismTools.armorGLOWSTONE) {
            return new ItemStack(MekanismItems.Ingot, 1, 3);
        } else if (getArmorMaterial() == MekanismTools.armorSTEEL) {
            return new ItemStack(MekanismItems.Ingot, 1, 4);
        }
        return new ItemStack(getArmorMaterial().getRepairItem());
    }

    @Override
    @SideOnly(Side.CLIENT)
    public ModelBiped getArmorModel(EntityLivingBase entityLiving, ItemStack itemStack, EntityEquipmentSlot armorSlot,
          ModelBiped _default) {
        if (itemStack.getItem() == ToolsItems.GlowstoneHelmet || itemStack.getItem() == ToolsItems.GlowstoneChestplate
            || itemStack.getItem() == ToolsItems.GlowstoneLeggings || itemStack.getItem() == ToolsItems.GlowstoneBoots) {
            return ModelCustomArmor.getGlow(armorSlot);
        }
        return super.getArmorModel(entityLiving, itemStack, armorSlot, _default);
    }
}