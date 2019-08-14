package mekanism.common.item.block;

import java.util.Optional;
import javax.annotation.Nonnull;
import mcmultipart.api.container.IMultipartContainer;
import mcmultipart.api.multipart.IMultipart;
import mcmultipart.api.multipart.MultipartHelper;
import mcmultipart.api.slot.IPartSlot;
import mekanism.common.Mekanism;
import mekanism.common.integration.MekanismHooks;
import mekanism.common.integration.multipart.MultipartMekanism;
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
        BlockState state = world.getBlockState(pos);
        Block block = state.getBlock();
        Hand hand = context.getHand();
        ItemStack itemstack = player.getHeldItem(hand);
        if (itemstack.isEmpty()) {
            return ActionResultType.FAIL;//WTF
        }
        Direction side = context.getFace();
        BlockItemUseContext blockItemUseContext = new BlockItemUseContext(context);
        if (Mekanism.hooks.MCMPLoaded) {
            if (!block.isReplaceable(state, blockItemUseContext) && !hasFreeMultiPartSpot(itemstack, world, pos, state, side)) {//free spot handles case of no container
                pos = pos.offset(side);
                state = world.getBlockState(pos);
            }
        } else if (!block.isReplaceable(state, blockItemUseContext)) {
            pos = pos.offset(side);
        }

        if (player.canPlayerEdit(pos, side, itemstack) && mayPlace(itemstack, world, pos, state, hand, side)) {
            BlockState iblockstate1 = this.getBlock().getStateForPlacement(blockItemUseContext);
            boolean flag;
            if (Mekanism.hooks.MCMPLoaded) {
                flag = MultipartMekanism.placeMultipartBlock(this.getBlock(), itemstack, player, world, pos, side, hitX, hitY, hitZ, iblockstate1);
            } else {
                flag = placeBlock(blockItemUseContext, iblockstate1);
            }
            if (flag) {
                iblockstate1 = world.getBlockState(pos);
                SoundType soundtype = iblockstate1.getBlock().getSoundType(iblockstate1, world, pos, player);
                world.playSound(player, pos, soundtype.getPlaceSound(), SoundCategory.BLOCKS, (soundtype.getVolume() + 1.0F) / 2.0F, soundtype.getPitch() * 0.8F);
                itemstack.shrink(1);
            }
            return ActionResultType.SUCCESS;
        }
        return ActionResultType.FAIL;
    }

    private boolean mayPlace(ItemStack itemstack, World worldIn, BlockPos pos, BlockState state, Hand hand, Direction facing) {
        if (!Mekanism.hooks.MCMPLoaded) {
            return worldIn.mayPlace(getBlock(), pos, false, facing, null);
        }
        return worldIn.mayPlace(getBlock(), pos, false, facing, null) || hasFreeMultiPartSpot(itemstack, worldIn, pos, state, facing);
    }

    private boolean hasFreeMultiPartSpot(ItemStack itemstack, World worldIn, BlockPos pos, BlockState state, Direction facing) {
        Optional<IMultipartContainer> container;
        if (!Mekanism.hooks.MCMPLoaded || !(container = MultipartHelper.getContainer(worldIn, pos)).isPresent()) {
            return false;
        }
        IMultipart multipart = getMultiPart();
        IPartSlot slot = multipart.getSlotForPlacement(worldIn, pos, state, facing, 0, 0, 0, null);
        return container.get().canAddPart(slot, this.block.getStateForPlacement(worldIn, pos, facing, 0, 0, 0, itemstack.getMetadata(), null, Hand.MAIN_HAND));
    }

    //FQ needed because it uses java optional interface elsewhere
    @net.minecraftforge.fml.common.Optional.Method(modid = MekanismHooks.MCMULTIPART_MOD_ID)
    protected abstract IMultipart getMultiPart();

    @Override
    public boolean canPlaceBlockOnSide(World worldIn, @Nonnull BlockPos pos, @Nonnull Direction side, @Nonnull PlayerEntity player, ItemStack stack) {
        return super.canPlaceBlockOnSide(worldIn, pos, side, player, stack) || (Mekanism.hooks.MCMPLoaded && MultipartHelper.getContainer(worldIn, pos).isPresent());
    }

    @Override
    public boolean placeBlock(@Nonnull BlockItemUseContext context, @Nonnull BlockState state) {
        World world = context.getWorld();
        BlockPos pos = context.getPos();
        if (!world.getBlockState(pos).getBlock().isReplaceable(state, context)) {
            return false;
        }
        return super.placeBlock(context, state);
    }
}