package mekanism.tools.common.item;

import java.util.List;
import java.util.Optional;
import mekanism.tools.common.material.BaseMekanismMaterial;
import net.minecraft.core.component.DataComponents;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ShieldItem;
import net.minecraft.world.item.component.BlocksAttacks;
import net.minecraft.world.level.block.entity.BannerPatternLayers;

public class ItemMekanismShield extends ShieldItem {

    private final BaseMekanismMaterial tier;

    public ItemMekanismShield(BaseMekanismMaterial material, Item.Properties properties) {
        super(properties
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
                          //TODO - 26.2: should different shields block more damage?
                          new BlocksAttacks.ItemDamageFunction(3.0F, 1.0F, 1.0F),
                          Optional.of(context.getOrThrow(DamageTypeTags.BYPASSES_SHIELD)),
                          Optional.of(SoundEvents.SHIELD_BLOCK),
                          Optional.of(SoundEvents.SHIELD_BREAK)
                    )
              )
              .component(DataComponents.BREAK_SOUND, SoundEvents.SHIELD_BREAK)
        );
        this.tier = material;
    }
}