package mekanism.tools.item;

import java.util.List;
import java.util.Locale;
import mekanism.client.render.ModelCustomArmor;
import mekanism.common.Mekanism;
import mekanism.common.util.LangUtils;
import mekanism.tools.common.ToolsItems;
import net.minecraft.client.model.ModelBiped;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemArmor;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class ItemMekanismArmor extends ItemArmor {

    public ItemMekanismArmor(ArmorMaterial material, int renderIndex, EntityEquipmentSlot slot) {
        super(material, renderIndex, slot);
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
    @SideOnly(Side.CLIENT)
    public ModelBiped getArmorModel(EntityLivingBase entityLiving, ItemStack itemStack, EntityEquipmentSlot armorSlot, ModelBiped _default) {
        if (itemStack.getItem() == ToolsItems.GlowstoneHelmet || itemStack.getItem() == ToolsItems.GlowstoneChestplate
            || itemStack.getItem() == ToolsItems.GlowstoneLeggings || itemStack.getItem() == ToolsItems.GlowstoneBoots) {
            return ModelCustomArmor.getGlow(armorSlot);
        }
        return super.getArmorModel(entityLiving, itemStack, armorSlot, _default);
    }
}