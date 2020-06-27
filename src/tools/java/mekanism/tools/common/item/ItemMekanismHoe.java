package mekanism.tools.common.item;

import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Multimap;
import java.util.List;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import mekanism.common.registration.impl.ItemDeferredRegister;
import mekanism.tools.common.IHasRepairType;
import mekanism.tools.common.ToolsLang;
import mekanism.tools.common.item.attribute.AttributeCache;
import mekanism.tools.common.item.attribute.IAttributeRefresher;
import mekanism.tools.common.material.MaterialCreator;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.ai.attributes.Attribute;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.ai.attributes.AttributeModifier.Operation;
import net.minecraft.entity.ai.attributes.Attributes;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.item.HoeItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.ToolType;

public class ItemMekanismHoe extends HoeItem implements IHasRepairType, IAttributeRefresher {

    private static final ToolType HOE_TOOL_TYPE = ToolType.get("hoe");
    private final MaterialCreator material;
    private final AttributeCache attributeCache;

    public ItemMekanismHoe(MaterialCreator material) {
        super(material, material.getHoeDamage(), material.getHoeAtkSpeed(), ItemDeferredRegister.getMekBaseProperties().addToolType(HOE_TOOL_TYPE, material.getHarvestLevel()));
        this.material = material;
        this.attributeCache = new AttributeCache(this, material.attackDamage, material.hoeDamage, material.hoeAtkSpeed);
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void addInformation(@Nonnull ItemStack stack, @Nullable World world, @Nonnull List<ITextComponent> tooltip, @Nonnull ITooltipFlag flag) {
        tooltip.add(ToolsLang.HP.translate(stack.getMaxDamage() - stack.getDamage()));
    }

    public float getAttackDamage() {
        return material.getHoeDamage() + getTier().getAttackDamage();
    }

    @Nonnull
    @Override
    public Ingredient getRepairMaterial() {
        return getTier().getRepairMaterial();
    }

    @Override
    public int getMaxDamage(ItemStack stack) {
        return getTier().getMaxUses();
    }

    @Override
    public boolean isDamageable() {
        return getTier().getMaxUses() > 0;
    }

    /**
     * {@inheritDoc}
     *
     * @implNote We bypass calling super to ensure we get added instead of not being able to add the proper values that {@link net.minecraft.item.HoeItem} tries to set
     */
    @Nonnull
    @Override
    public Multimap<Attribute, AttributeModifier> getAttributeModifiers(@Nonnull EquipmentSlotType slot, @Nonnull ItemStack stack) {
        return slot == EquipmentSlotType.MAINHAND ? attributeCache.getAttributes() : ImmutableMultimap.of();
    }

    @Override
    public void addToBuilder(ImmutableMultimap.Builder<Attribute, AttributeModifier> builder) {
        builder.put(Attributes.field_233823_f_, new AttributeModifier(ATTACK_DAMAGE_MODIFIER, "Weapon modifier", getAttackDamage(), Operation.ADDITION));
        builder.put(Attributes.field_233825_h_, new AttributeModifier(ATTACK_SPEED_MODIFIER, "Weapon modifier", material.getHoeAtkSpeed(), Operation.ADDITION));
    }
}