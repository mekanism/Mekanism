package mekanism.common.item.block;

import javax.annotation.Nonnull;
import mekanism.common.registration.impl.ItemDeferredRegister;
import mekanism.common.util.WorldUtils;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.SoundType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUseContext;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

/**
 * Created by Thiakil on 19/11/2017.
 */
//TODO: Re-evaluate this class
public abstract class ItemBlockMultipartAble<BLOCK extends Block> extends ItemBlockMekanism<BLOCK> {

    public ItemBlockMultipartAble(BLOCK block) {
        super(block, ItemDeferredRegister.getMekBaseProperties());
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
        ItemStack stack = context.getItem();
        if (stack.isEmpty()) {
            return ActionResultType.FAIL;//WTF
        }
        World world = context.getWorld();
        BlockPos pos = context.getPos();
        if (!WorldUtils.isValidReplaceableBlock(world, pos)) {
            pos = pos.offset(context.getFace());
        }
        if (player.canPlayerEdit(pos, context.getFace(), stack)) {
            BlockItemUseContext blockItemUseContext = new BlockItemUseContext(context);
            BlockState state = getStateForPlacement(blockItemUseContext);
            if (state == null) {
                return ActionResultType.FAIL;
            }
            if (placeBlock(blockItemUseContext, state)) {
                state = world.getBlockState(pos);
                SoundType soundtype = state.getSoundType(world, pos, player);
                world.playSound(player, pos, soundtype.getPlaceSound(), SoundCategory.BLOCKS, (soundtype.getVolume() + 1) / 2F, soundtype.getPitch() * 0.8F);
                stack.shrink(1);
            }
            return ActionResultType.SUCCESS;
        }
        return ActionResultType.FAIL;
    }

    @Override
    public boolean placeBlock(@Nonnull BlockItemUseContext context, @Nonnull BlockState state) {
        if (WorldUtils.isValidReplaceableBlock(context.getWorld(), context.getPos())) {
            return super.placeBlock(context, state);
        }
        return false;
    }
}