package mekanism.tools.common.item;

import java.util.function.Consumer;
import mekanism.tools.common.material.MaterialCreator;
import mekanism.tools.common.util.ToolsUtils;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.item.equipment.ArmorType;
import net.minecraft.world.item.equipment.EquipmentAsset;
import net.minecraft.world.item.equipment.Equippable;

public class ItemMekanismArmor extends Item {

    private final ArmorType armorType;

    private final ResourceKey<EquipmentAsset> equipmentAssetId;

    public ItemMekanismArmor(MaterialCreator material, ArmorType armorType, Item.Properties properties) {
        super(applyArmorProps(properties, material, armorType));
        this.armorType = armorType;
        this.equipmentAssetId = material.equipmentAsset();
    }

    /// Copied and adapted from [Item.Properties#humanoidArmor]
    private static Properties applyArmorProps(Properties properties, MaterialCreator material, ArmorType armorType) {
        return properties.durability(material.getDurabilityForType(armorType))
              .attributes(material.createAttributes(armorType))
              .enchantable(material.getEnchantmentValue())
              .component(
                    DataComponents.EQUIPPABLE, Equippable.builder(armorType.getSlot())
                          .setEquipSound(material.equipSound())
                          .setAsset(material.equipmentAsset())
                          .build()
              )
              .repairable(material.getRepairItems());
    }

    @Override
    @Deprecated
    public void appendHoverText(ItemStack stack, Item.TooltipContext context, TooltipDisplay tooltipDisplay, Consumer<Component> tooltipAdder, TooltipFlag flag) {
        super.appendHoverText(stack, context, tooltipDisplay, tooltipAdder, flag);
        ToolsUtils.addDurability(tooltipAdder, stack);
    }

    public ArmorType getArmorType() {
        return armorType;
    }

    public ResourceKey<EquipmentAsset> getEquipmentAssetId() {
        return equipmentAssetId;
    }
}