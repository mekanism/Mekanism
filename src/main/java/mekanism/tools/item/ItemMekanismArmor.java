package mekanism.tools.item;

import java.util.List;
import java.util.Locale;
import javax.annotation.Nullable;
import mekanism.client.render.ModelCustomArmor;
import mekanism.common.util.LangUtils;
import mekanism.common.util.TextComponentUtil;
import mekanism.common.util.TextComponentUtil.Translation;
import mekanism.tools.common.IHasRepairType;
import mekanism.tools.common.Materials;
import mekanism.tools.common.MekanismTools;
import mekanism.tools.common.ToolsItem;
import net.minecraft.client.renderer.entity.model.BipedModel;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.item.ArmorItem;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class ItemMekanismArmor extends ArmorItem implements IHasRepairType {

    public ItemMekanismArmor(Materials material, int renderIndex, EquipmentSlotType slot) {
        super(material.getArmorMaterial(), renderIndex, slot);
        String name = null;
        if (slot == EquipmentSlotType.HEAD) {
            name = material.getMaterialName() + "_helmet";
        } else if (slot == EquipmentSlotType.CHEST) {
            name = material.getMaterialName() + "_chestplate";
        } else if (slot == EquipmentSlotType.LEGS) {
            name = material.getMaterialName() + "_leggings";
        } else if (slot == EquipmentSlotType.FEET) {
            name = material.getMaterialName() + "_boots";
        }
        if (name != null) {
            setRegistryName(new ResourceLocation(MekanismTools.MODID, name.toLowerCase(Locale.ROOT)));
        }
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void addInformation(ItemStack stack, @Nullable World world, List<ITextComponent> tooltip, ITooltipFlag flag) {
        tooltip.add(TextComponentUtil.build(Translation.of("mekanism.tooltip.hp"), ": " + (stack.getMaxDamage() - stack.getDamage())));
    }

    @Override
    public String getArmorTexture(ItemStack stack, Entity entity, EquipmentSlotType slot, String type) {
        int layer = slot == EquipmentSlotType.LEGS ? 2 : 1;
        return "mekanism:armor/" + getArmorMaterial().name().toLowerCase(Locale.ROOT) + "_" + layer + ".png";
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public BipedModel getArmorModel(LivingEntity entityLiving, ItemStack itemStack, EquipmentSlotType armorSlot, BipedModel _default) {
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