package mekanism.common.item.block;

import mekanism.api.security.IItemSecurityUtils;
import mekanism.common.block.attribute.Attribute;
import mekanism.common.block.attribute.Attributes.AttributeSecurity;
import mekanism.common.capabilities.ICapabilityAware;
import mekanism.common.capabilities.security.SecurityObject;
import mekanism.common.util.InventoryUtils;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;

public class MekanismBlockItem extends BlockItem implements ICapabilityAware {

    public MekanismBlockItem(Block block, Item.Properties properties) {
        super(block, properties);
    }

    @Override
    public void onDestroyed(ItemEntity item, DamageSource damageSource) {
        //Try to drop the inventory contents if we are a block item that persists our inventory
        InventoryUtils.dropItemContents(item, damageSource);
    }

    @Override
    public boolean shouldCauseBlockBreakReset(ItemStack oldStack, ItemStack newStack) {
        //Ignore NBT for energized items causing block break reset
        return !ItemStack.matchesIgnoringComponents(oldStack, newStack, DataComponentType::ignoreSwapAnimation);
    }

    @Override
    public void attachCapabilities(RegisterCapabilitiesEvent event) {
        if (Attribute.has(getBlock(), AttributeSecurity.class)) {
            event.registerItem(IItemSecurityUtils.INSTANCE.ownerCapability(), (_, itemAccess) -> new SecurityObject(itemAccess), this);
            event.registerItem(IItemSecurityUtils.INSTANCE.securityCapability(), (_, itemAccess) -> new SecurityObject(itemAccess), this);
        }
    }
}