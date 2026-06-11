package mekanism.tools.common.item;

import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
import mekanism.tools.common.material.BaseMekanismMaterial;
import mekanism.tools.common.util.ToolsUtils;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ShieldItem;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.BlocksAttacks;
import net.minecraft.world.item.component.TooltipDisplay;
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
                          //TODO - 26.1: should different shields block more damage?
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

    @Override
    @Deprecated
    public void appendHoverText(ItemStack stack, Item.TooltipContext context, TooltipDisplay tooltipDisplay, Consumer<Component> tooltipAdder, TooltipFlag flag) {
        super.appendHoverText(stack, context, tooltipDisplay, tooltipAdder, flag);
        ToolsUtils.addDurability(tooltipAdder, stack);
    }
}