package mekanism.tools.item;

import java.util.List;
import java.util.Locale;
import mekanism.client.render.ModelCustomArmor;
import mekanism.common.util.LangUtils;
import mekanism.tools.common.IHasRepairType;
import mekanism.tools.common.Materials;
import mekanism.tools.common.MekanismTools;
import mekanism.tools.common.ToolsItem;
import net.minecraft.client.model.ModelBiped;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemArmor;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class ItemMekanismArmor extends ItemArmor implements IHasRepairType {

    public ItemMekanismArmor(Materials material, int renderIndex, EntityEquipmentSlot slot) {
        super(material.getArmorMaterial(), renderIndex, slot);
        String name = null;
        if (slot == EntityEquipmentSlot.HEAD) {
            name = material.getMaterialName() + "_helmet";
        } else if (slot == EntityEquipmentSlot.CHEST) {
            name = material.getMaterialName() + "_chestplate";
        } else if (slot == EntityEquipmentSlot.LEGS) {
            name = material.getMaterialName() + "_leggings";
        } else if (slot == EntityEquipmentSlot.FEET) {
            name = material.getMaterialName() + "_boots";
        }
        if (name != null) {
            setRegistryName(new ResourceLocation(MekanismTools.MODID, name.toLowerCase(Locale.ROOT)));
        }
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void addInformation(ItemStack itemstack, World world, List<String> list, ITooltipFlag flag) {
        list.add(LangUtils.localize("tooltip.hp") + ": " + (itemstack.getMaxDamage() - itemstack.getItemDamage()));
    }

    @Override
    public String getArmorTexture(ItemStack stack, Entity entity, EntityEquipmentSlot slot, String type) {
        int layer = slot == EntityEquipmentSlot.LEGS ? 2 : 1;
        return "mekanism:armor/" + getArmorMaterial().name().toLowerCase(Locale.ROOT) + "_" + layer + ".png";
    }

    @Override
    @SideOnly(Side.CLIENT)
    public ModelBiped getArmorModel(EntityLivingBase entityLiving, ItemStack itemStack, EntityEquipmentSlot armorSlot, ModelBiped _default) {
        if (itemStack.getItem() == ToolsItem.GLOWSTONE_HELMET.getItem() || itemStack.getItem() == ToolsItem.GLOWSTONE_CHESTPLATE.getItem()
            || itemStack.getItem() == ToolsItem.GLOWSTONE_LEGGINGS.getItem() || itemStack.getItem() == ToolsItem.GLOWSTONE_BOOTS.getItem()) {
            return ModelCustomArmor.getGlow(armorSlot);
        }
        return super.getArmorModel(entityLiving, itemStack, armorSlot, _default);
    }

    @Override
    public ItemStack getRepairStack() {
        return getArmorMaterial().getRepairItemStack();
    }
}