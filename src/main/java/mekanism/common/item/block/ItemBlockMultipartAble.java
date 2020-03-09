package mekanism.common.item.block;

import javax.annotation.Nonnull;
import mekanism.common.util.MekanismUtils;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.SoundType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUseContext;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

/**
 * Created by Thiakil on 19/11/2017.
 */
//TODO: Cleanup/rename this class
public abstract class ItemBlockMultipartAble<BLOCK extends Block> extends ItemBlockMekanism<BLOCK> {

    public ItemBlockMultipartAble(BLOCK block) {
        super(block);
    }

    /**
     * Reimplementation of onItemUse that will divert to MCMultipart placement functions if applicable
     */
    @Nonnull
    @Override
    public ActionResultType onItemUse(ItemUseContext context) {
        PlayerEntity player = context.getPlayer();
        if (player == null) {
            return ActionResultType.PASS;
        }
        World world = context.getWorld();
        BlockPos pos = context.getPos();
        Hand hand = context.getHand();
        ItemStack stack = player.getHeldItem(hand);
        if (stack.isEmpty()) {
            return ActionResultType.FAIL;//WTF
        }
        Direction side = context.getFace();
        BlockItemUseContext blockItemUseContext = new BlockItemUseContext(context);
        if (!MekanismUtils.isValidReplaceableBlock(world, pos)) {
            pos = pos.offset(side);
        }
        if (player.canPlayerEdit(pos, side, stack)) {
            BlockState iblockstate1 = getStateForPlacement(blockItemUseContext);
            if (iblockstate1 == null) return ActionResultType.FAIL;
            boolean flag = placeBlock(blockItemUseContext, iblockstate1);
            if (flag) {
                iblockstate1 = world.getBlockState(pos);
                SoundType soundtype = iblockstate1.getBlock().getSoundType(iblockstate1, world, pos, player);
                world.playSound(player, pos, soundtype.getPlaceSound(), SoundCategory.BLOCKS, (soundtype.getVolume() + 1.0F) / 2.0F, soundtype.getPitch() * 0.8F);
                stack.shrink(1);
            }
            return ActionResultType.SUCCESS;
        }
        return ActionResultType.FAIL;
    }

    @Override
    public boolean placeBlock(@Nonnull BlockItemUseContext context, @Nonnull BlockState state) {
        if (!MekanismUtils.isValidReplaceableBlock(context.getWorld(), context.getPos())) {
            return false;
        }
        return super.placeBlock(context, state);
    }
}