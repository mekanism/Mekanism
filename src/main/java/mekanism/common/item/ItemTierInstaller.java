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

public class ItemTierInstaller extends Item {

    private final BaseTier tier;

    public ItemTierInstaller(BaseTier tier, Properties properties) {
        super(properties.maxStackSize(1));
        this.tier = tier;
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
            if (((ITierUpgradeable) tile).upgrade(tier)) {
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