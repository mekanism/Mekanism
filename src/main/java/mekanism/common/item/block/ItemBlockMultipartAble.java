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
    public ActionResultType useOn(ItemUseContext context) {
        PlayerEntity player = context.getPlayer();
        if (player == null) {
            return ActionResultType.PASS;
        }
        ItemStack stack = context.getItemInHand();
        if (stack.isEmpty()) {
            return ActionResultType.FAIL;//WTF
        }
        World world = context.getLevel();
        BlockPos pos = context.getClickedPos();
        if (!WorldUtils.isValidReplaceableBlock(world, pos)) {
            pos = pos.relative(context.getClickedFace());
        }
        if (player.mayUseItemAt(pos, context.getClickedFace(), stack)) {
            BlockItemUseContext blockItemUseContext = new BlockItemUseContext(context);
            BlockState state = getPlacementState(blockItemUseContext);
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
        if (WorldUtils.isValidReplaceableBlock(context.getLevel(), context.getClickedPos())) {
            return super.placeBlock(context, state);
        }
        return false;
    }
}