package mekanism.common.item.block.machine;

import java.util.List;
import javax.annotation.Nonnull;
import mekanism.common.block.basic.BlockLogisticalSorter;
import mekanism.common.item.block.ItemBlockTooltip;
import mekanism.common.item.interfaces.IItemSustainedInventory;
import mekanism.common.lib.security.ISecurityItem;
import mekanism.common.registration.impl.ItemDeferredRegister;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.SecurityUtils;
import net.minecraft.world.item.ItemStack;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.Level;

public class ItemBlockLogisticalSorter extends ItemBlockTooltip<BlockLogisticalSorter> implements IItemSustainedInventory, ISecurityItem {

    public ItemBlockLogisticalSorter(BlockLogisticalSorter block) {
        super(block, true, ItemDeferredRegister.getMekBaseProperties().stacksTo(1));
    }

    @Override
    protected void addDetails(@Nonnull ItemStack stack, Level world, @Nonnull List<Component> tooltip, boolean advanced) {
        SecurityUtils.addSecurityTooltip(stack, tooltip);
        MekanismUtils.addUpgradesToTooltip(stack, tooltip);
    }
}