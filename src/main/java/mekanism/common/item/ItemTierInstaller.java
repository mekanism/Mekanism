package mekanism.common.item;

import javax.annotation.Nonnull;
import mekanism.common.base.ITierUpgradeable;
import mekanism.common.tier.BaseTier;
import mekanism.common.tile.base.TileEntityMekanism;
import mekanism.common.util.MekanismUtils;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUseContext;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ActionResultType;
import net.minecraft.world.World;

public class ItemTierInstaller extends ItemMekanismTiered {

    public ItemTierInstaller(BaseTier tier) {
        super(tier, "tier_installer", new Item.Properties().maxStackSize(1));
    }

    @Nonnull
    @Override
    public ActionResultType onItemUse(ItemUseContext context) {
        PlayerEntity player = context.getPlayer();
        World world = context.getWorld();
        if (world.isRemote || player == null) {
            return ActionResultType.PASS;
        }
        TileEntity tile = MekanismUtils.getTileEntity(world, context.getPos());
        if (tile instanceof ITierUpgradeable) {
            //TODO: Replace this?? Or will instance case still be true
            if (tile instanceof TileEntityMekanism && ((TileEntityMekanism) tile).playersUsing.size() > 0) {
                return ActionResultType.FAIL;
            }
            if (((ITierUpgradeable) tile).upgrade(getTier())) {
                if (!player.isCreative()) {
                    ItemStack stack = player.getHeldItem(context.getHand());
                    stack.shrink(1);
                }
                return ActionResultType.SUCCESS;
            }
            return ActionResultType.PASS;
        }
        return ActionResultType.PASS;
    }
}