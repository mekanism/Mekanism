package mekanism.common.item.gear;

import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;
import com.google.common.collect.Multimap;
import mcp.MethodsReturnNonnullByDefault;
import mekanism.api.math.FloatingLong;
import mekanism.common.capabilities.ItemCapabilityWrapper;
import mekanism.common.capabilities.energy.BasicEnergyContainer;
import mekanism.common.capabilities.energy.item.RateLimitEnergyHandler;
import mekanism.common.capabilities.radiation.item.RadiationShieldingHandler;
import mekanism.common.content.gear.IModuleContainerItem;
import mekanism.common.content.gear.Modules;
import mekanism.common.util.StorageUtils;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.item.ArmorItem;
import net.minecraft.item.IArmorMaterial;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.NonNullList;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.SoundEvents;
import net.minecraftforge.common.capabilities.ICapabilityProvider;

public class ItemMekaSuitArmor extends ArmorItem implements IModuleContainerItem /*, ISpecialGear*/ {

    private static final FloatingLong MAX_ENERGY = FloatingLong.createConst(1_000_000_000);
    private static final MekaSuitMaterial MEKASUIT_MATERIAL = new MekaSuitMaterial();

    public ItemMekaSuitArmor(EquipmentSlotType slot, Properties properties) {
        super(MEKASUIT_MATERIAL, slot, properties.setNoRepair().maxStackSize(1));
    }

    @Override
    public boolean showDurabilityBar(ItemStack stack) {
        return true;
    }

    @Override
    public double getDurabilityForDisplay(ItemStack stack) {
        return StorageUtils.getDurabilityForDisplay(stack);
    }

    @Override
    public void fillItemGroup(@Nonnull ItemGroup group, @Nonnull NonNullList<ItemStack> items) {
        super.fillItemGroup(group, items);
        if (isInGroup(group)) {
            items.add(StorageUtils.getFilledEnergyVariant(new ItemStack(this), MAX_ENERGY));
        }
    }

    @Override
    // TODO adjust values based on energy
    public Multimap<String, AttributeModifier> getAttributeModifiers(EquipmentSlotType equipmentSlot, ItemStack stack) {
        Multimap<String, AttributeModifier> multimap = super.getAttributeModifiers(equipmentSlot);
        if (equipmentSlot == slot) {
            multimap.put(SharedMonsterAttributes.ARMOR.getName(), new AttributeModifier(ARMOR_MODIFIERS[equipmentSlot.getIndex()], "Armor modifier", this.damageReduceAmount, AttributeModifier.Operation.ADDITION));
            multimap.put(SharedMonsterAttributes.ARMOR_TOUGHNESS.getName(), new AttributeModifier(ARMOR_MODIFIERS[equipmentSlot.getIndex()], "Armor toughness", this.toughness, AttributeModifier.Operation.ADDITION));
        }

        return multimap;
     }

    @Override
    public ICapabilityProvider initCapabilities(ItemStack stack, CompoundNBT nbt) {
        //Note: We interact with this capability using "manual" as the automation type, to ensure we can properly bypass the energy limit for extracting
        // Internal is used by the "null" side, which is what will get used for most items
        return new ItemCapabilityWrapper(stack, RateLimitEnergyHandler.create(() -> MAX_ENERGY, BasicEnergyContainer.notExternal, BasicEnergyContainer.alwaysTrue),
            RadiationShieldingHandler.create(item -> isModuleEnabled(item, Modules.RADIATION_SHIELDING_UNIT) ? ItemHazmatSuitArmor.getShieldingByArmor(slot) : 0));
    }

    @ParametersAreNonnullByDefault
    @MethodsReturnNonnullByDefault
    protected static class MekaSuitMaterial implements IArmorMaterial {

        @Override
        public int getDurability(EquipmentSlotType slotType) {
            return 0;
        }

        @Override
        public int getDamageReductionAmount(EquipmentSlotType slotType) {
            return 0;
        }

        @Override
        public int getEnchantability() {
            return 0;
        }

        @Override
        public SoundEvent getSoundEvent() {
            return SoundEvents.ITEM_ARMOR_EQUIP_GENERIC;
        }

        @Override
        public Ingredient getRepairMaterial() {
            return Ingredient.EMPTY;
        }

        @Override
        public String getName() {
            return "mekasuit";
        }

        @Override
        public float getToughness() {
            return 0;
        }
    }
}
