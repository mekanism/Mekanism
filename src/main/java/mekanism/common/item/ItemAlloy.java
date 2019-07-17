package mekanism.common.item;

import javax.annotation.Nonnull;
import mekanism.api.IAlloyInteraction;
import mekanism.common.MekanismItems;
import mekanism.common.capabilities.Capabilities;
import mekanism.common.config.MekanismConfig;
import mekanism.common.util.CapabilityUtils;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class ItemAlloy extends ItemMekanism {

    @Nonnull
    @Override
    public EnumActionResult onItemUse(EntityPlayer player, World world, BlockPos pos, EnumHand hand, EnumFacing side, float hitX, float hitY, float hitZ) {
        TileEntity tile = world.getTileEntity(pos);
        ItemStack stack = player.getHeldItem(hand);
        if (MekanismConfig.current().general.allowTransmitterAlloyUpgrade.val() && CapabilityUtils.hasCapability(tile, Capabilities.ALLOY_INTERACTION_CAPABILITY, side)) {
            if (!world.isRemote) {
                IAlloyInteraction interaction = CapabilityUtils.getCapability(tile, Capabilities.ALLOY_INTERACTION_CAPABILITY, side);
                //TODO: Modify this
                int ordinal = stack.getItem() == MekanismItems.EnrichedAlloy ? 1 : stack.getItem() == MekanismItems.ReinforcedAlloy ? 2 : 3;
                interaction.onAlloyInteraction(player, hand, stack, ordinal);
            }
            return EnumActionResult.SUCCESS;
        }
        return EnumActionResult.PASS;
    }
}