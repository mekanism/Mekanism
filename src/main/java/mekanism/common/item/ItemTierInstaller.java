package mekanism.common.item;

import javax.annotation.Nonnull;
import mekanism.common.base.ITierUpgradeable;
import mekanism.common.tier.BaseTier;
import mekanism.common.tile.prefab.TileEntityBasicBlock;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class ItemTierInstaller extends ItemMekanismTiered {

    public ItemTierInstaller(BaseTier tier) {
        super(tier, "tier_installer");
        setMaxStackSize(1);
    }

    @Nonnull
    @Override
    public EnumActionResult onItemUseFirst(EntityPlayer player, World world, BlockPos pos, EnumFacing side, float hitX, float hitY, float hitZ, EnumHand hand) {
        if (world.isRemote) {
            return EnumActionResult.PASS;
        }
        TileEntity tile = world.getTileEntity(pos);
        if (tile instanceof ITierUpgradeable) {
            //TODO: Replace this?? Or will instance case still be true
            if (tile instanceof TileEntityBasicBlock && ((TileEntityBasicBlock) tile).playersUsing.size() > 0) {
                return EnumActionResult.FAIL;
            }
            if (((ITierUpgradeable) tile).upgrade(getTier())) {
                if (!player.capabilities.isCreativeMode) {
                    ItemStack stack = player.getHeldItem(hand);
                    stack.shrink(1);
                }
                return EnumActionResult.SUCCESS;
            }
            return EnumActionResult.PASS;
        }
        return EnumActionResult.PASS;
    }
}