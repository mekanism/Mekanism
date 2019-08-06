package mekanism.common.item;

import javax.annotation.Nonnull;
import mekanism.common.base.ITierUpgradeable;
import mekanism.common.tier.BaseTier;
import mekanism.common.tile.base.TileEntityMekanism;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class ItemTierInstaller extends ItemMekanismTiered {

    public ItemTierInstaller(BaseTier tier) {
        super(tier, "tier_installer");
        setMaxStackSize(1);
    }

    @Nonnull
    @Override
    public ActionResultType onItemUseFirst(PlayerEntity player, World world, BlockPos pos, Direction side, float hitX, float hitY, float hitZ, Hand hand) {
        if (world.isRemote) {
            return ActionResultType.PASS;
        }
        TileEntity tile = world.getTileEntity(pos);
        if (tile instanceof ITierUpgradeable) {
            //TODO: Replace this?? Or will instance case still be true
            if (tile instanceof TileEntityMekanism && ((TileEntityMekanism) tile).playersUsing.size() > 0) {
                return ActionResultType.FAIL;
            }
            if (((ITierUpgradeable) tile).upgrade(getTier())) {
                if (!player.capabilities.isCreativeMode) {
                    ItemStack stack = player.getHeldItem(hand);
                    stack.shrink(1);
                }
                return ActionResultType.SUCCESS;
            }
            return ActionResultType.PASS;
        }
        return ActionResultType.PASS;
    }
}