package mekanism.tools.common.registration;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableList.Builder;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
import mekanism.common.registration.impl.ItemDeferredRegister;
import mekanism.common.registration.impl.ItemRegistryObject;
import mekanism.tools.common.material.MaterialCreator;
import mekanism.tools.common.registries.ToolsItems;
import net.minecraft.core.component.DataComponents;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ShieldItem;
import net.minecraft.world.item.component.BlocksAttacks;
import net.minecraft.world.level.block.entity.BannerPatternLayers;

public record ToolCollection(ItemRegistryObject<Item> axe, ItemRegistryObject<Item> hoe, ItemRegistryObject<Item> paxel,
                             ItemRegistryObject<Item> pickaxe, ItemRegistryObject<ShieldItem> shield, ItemRegistryObject<Item> shovel,
                             ItemRegistryObject<Item> sword, ItemRegistryObject<Item> spear) {

    public static ToolCollection create(ItemDeferredRegister registry, MaterialCreator material) {
        return new ToolCollection(
              registry.registerSimple(material.getRegistryPrefix() + "_axe", properties -> ToolsItems.setCommonProperties(properties, material)
                    .axe(material.toToolMaterial(), material.getAxeDamage(), material.getAxeAtkSpeed())
              ),
              registry.registerSimple(material.getRegistryPrefix() + "_hoe", properties -> ToolsItems.setCommonProperties(properties, material)
                    .hoe(material.toToolMaterial(), material.getHoeDamage(), material.getHoeAtkSpeed())
              ),
              registry.registerSimple(material.getRegistryPrefix() + "_paxel", properties -> ToolsItems.paxel(ToolsItems.setCommonProperties(properties, material), material)),
              registry.registerSimple(material.getRegistryPrefix() + "_pickaxe", properties -> ToolsItems.setCommonProperties(properties, material)
                    .pickaxe(material.toToolMaterial(), material.getPickaxeDamage(), material.getPickaxeAtkSpeed())
              ),
              registry.registerItem(material.getRegistryPrefix() + "_shield", properties -> new ShieldItem(ToolsItems.setCommonProperties(properties, material)
                    .durability(material.getShieldDurability())
                    .component(DataComponents.BANNER_PATTERNS, BannerPatternLayers.EMPTY)
                    .repairable(material.getRepairItems())
                    .equippableUnswappable(EquipmentSlot.OFFHAND)
                    .delayedComponent(
                          DataComponents.BLOCKS_ATTACKS,
                          context -> new BlocksAttacks(
                                0.25F,
                                1.0F,
                                List.of(new BlocksAttacks.DamageReduction(90.0F, Optional.empty(), 0.0F, 1.0F)),
                                //TODO - 26.3: should different shields block more damage?
                                new BlocksAttacks.ItemDamageFunction(3.0F, 1.0F, 1.0F),
                                Optional.of(context.getOrThrow(DamageTypeTags.BYPASSES_SHIELD)),
                                Optional.of(SoundEvents.SHIELD_BLOCK),
                                Optional.of(SoundEvents.SHIELD_BREAK)
                          )
                    )
                    .component(DataComponents.BREAK_SOUND, SoundEvents.SHIELD_BREAK)
              )),
              registry.registerSimple(material.getRegistryPrefix() + "_shovel", properties -> ToolsItems.setCommonProperties(properties, material)
                    .shovel(material.toToolMaterial(), material.getShovelDamage(), material.getShovelAtkSpeed())
              ),
              registry.registerSimple(material.getRegistryPrefix() + "_sword", properties -> ToolsItems.setCommonProperties(properties, material)
                    .sword(material.toToolMaterial(), material.getSwordDamage(), material.getSwordAtkSpeed())
              ),
              registry.registerSimple(material.getRegistryPrefix() + "_spear", properties -> ToolsItems.setCommonProperties(properties, material)
                    .spear(material.toToolMaterial(), material.getSpearAttackDuration(), material.getSpearDamageMultiplier(), material.getSpearDelay(),
                          material.getSpearDismountTime(), material.getSpearDismountThreshold(), material.getSpearKnockbackTime(), material.getSpearKnockbackThreshold(),
                          material.getSpearDamageTime(), material.getSpearDamageThreshold())
              )
        );
    }

    public List<ItemRegistryObject<? extends Item>> asList() {
        Builder<ItemRegistryObject<? extends Item>> builder = ImmutableList.builderWithExpectedSize(7);
        forEach(builder::add);
        return builder.build();
    }

    public void forEach(Consumer<ItemRegistryObject<? extends Item>> consumer) {
        consumer.accept(this.axe);
        consumer.accept(this.hoe);
        consumer.accept(this.paxel);
        consumer.accept(this.pickaxe);
        consumer.accept(this.shield);
        consumer.accept(this.shovel);
        consumer.accept(this.sword);
        consumer.accept(this.spear);
    }
}