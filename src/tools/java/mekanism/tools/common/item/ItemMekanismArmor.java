package mekanism.tools.common.item;

import java.util.List;
import java.util.UUID;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import mekanism.common.registration.impl.ItemDeferredRegister;
import mekanism.tools.client.render.GlowArmor;
import mekanism.tools.common.IHasRepairType;
import mekanism.tools.common.ToolsLang;
import mekanism.tools.common.material.BaseMekanismMaterial;
import mekanism.tools.common.registries.ToolsItems;
import net.minecraft.client.renderer.entity.model.BipedModel;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.attributes.Attribute;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.ai.attributes.AttributeModifier.Operation;
import net.minecraft.entity.ai.attributes.Attributes;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.item.ArmorItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class ItemMekanismArmor extends ArmorItem implements IHasRepairType {

    private final BaseMekanismMaterial material;

    public ItemMekanismArmor(BaseMekanismMaterial material, EquipmentSlotType slot) {
        super(material, slot, ItemDeferredRegister.getMekBaseProperties());
        this.material = material;
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void addInformation(@Nonnull ItemStack stack, @Nullable World world, @Nonnull List<ITextComponent> tooltip, @Nonnull ITooltipFlag flag) {
        tooltip.add(ToolsLang.HP.translate(stack.getMaxDamage() - stack.getDamage()));
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public BipedModel getArmorModel(LivingEntity entityLiving, ItemStack itemStack, EquipmentSlotType armorSlot, BipedModel _default) {
        if (itemStack.getItem() == ToolsItems.REFINED_GLOWSTONE_HELMET.getItem() || itemStack.getItem() == ToolsItems.REFINED_GLOWSTONE_CHESTPLATE.getItem()
            || itemStack.getItem() == ToolsItems.REFINED_GLOWSTONE_LEGGINGS.getItem() || itemStack.getItem() == ToolsItems.REFINED_GLOWSTONE_BOOTS.getItem()) {
            return GlowArmor.getGlow(armorSlot);
        }
        return super.getArmorModel(entityLiving, itemStack, armorSlot, _default);
    }

    @Nonnull
    @Override
    public Ingredient getRepairMaterial() {
        return getArmorMaterial().getRepairMaterial();
    }

    @Override
    public int getDamageReduceAmount() {
        return getArmorMaterial().getDamageReductionAmount(getEquipmentSlot());
    }

    @Override
    public float getToughness() {
        return getArmorMaterial().getToughness();
    }

    @Override
    public int getMaxDamage(ItemStack stack) {
        return material.getDurability(getEquipmentSlot());
    }

    @Override
    public boolean isDamageable() {
        return material.getDurability(getEquipmentSlot()) > 0;
    }

    /**
     * {@inheritDoc}
     *
     * @implNote We bypass calling super to ensure we get added instead of not being able to add the proper values that {@link ArmorItem} tries to set
     */
    @Nonnull
    @Override
    public Multimap<Attribute, AttributeModifier> getAttributeModifiers(@Nonnull EquipmentSlotType slot, @Nonnull ItemStack stack) {
        //TODO - 1.16: Cache this, and update it when one of the values change
        Multimap<Attribute, AttributeModifier> attributes = HashMultimap.create();
        if (slot == getEquipmentSlot()) {
            UUID modifier = ARMOR_MODIFIERS[slot.getIndex()];
            attributes.put(Attributes.field_233826_i_, new AttributeModifier(modifier, "Armor modifier", getDamageReduceAmount(), Operation.ADDITION));
            attributes.put(Attributes.field_233827_j_, new AttributeModifier(modifier, "Armor toughness", getToughness(), Operation.ADDITION));
        }
        return attributes;
    }
}