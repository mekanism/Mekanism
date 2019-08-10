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
public abstract class ItemBlockMultipartAble extends ItemBlockMekanism {

    public ItemBlockMultipartAble(Block block) {
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
        BlockState iblockstate = world.getBlockState(pos);
        Block block = iblockstate.getBlock();
        Hand hand = context.getHand();
        ItemStack itemstack = player.getHeldItem(hand);
        if (itemstack.isEmpty()) {
            return ActionResultType.FAIL;//WTF
        }
        Direction side = context.getFace();
        if (Mekanism.hooks.MCMPLoaded) {
            if (!block.isReplaceable(world, pos) && !hasFreeMultiPartSpot(itemstack, world, pos, iblockstate, side)) {//free spot handles case of no container
                pos = pos.offset(side);
                iblockstate = world.getBlockState(pos);
            }
        } else if (!block.isReplaceable(world, pos)) {
            pos = pos.offset(side);
        }

        if (player.canPlayerEdit(pos, side, itemstack) && mayPlace(itemstack, world, pos, iblockstate, hand, side)) {
            int i = this.getMetadata(itemstack.getMetadata());
            BlockState iblockstate1 = this.getBlock().getStateForPlacement(world, pos, side, hitX, hitY, hitZ, i, player, hand);
            boolean flag;
            if (Mekanism.hooks.MCMPLoaded) {
                flag = MultipartMekanism.placeMultipartBlock(this.getBlock(), itemstack, player, world, pos, side, hitX, hitY, hitZ, iblockstate1);
            } else {
                flag = placeBlockAt(itemstack, player, world, pos, side, hitX, hitY, hitZ, iblockstate1);
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
            return worldIn.mayPlace(this.block, pos, false, facing, null);
        }
        return worldIn.mayPlace(this.block, pos, false, facing, null) || hasFreeMultiPartSpot(itemstack, worldIn, pos, state, facing);
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
    public boolean placeBlockAt(@Nonnull ItemStack stack, @Nonnull PlayerEntity player, World world, @Nonnull BlockPos pos, Direction side, float hitX, float hitY,
          float hitZ, @Nonnull BlockState newState) {
        if (!world.getBlockState(pos).getBlock().isReplaceable(world, pos)) {
            return false;
        }
        return super.placeBlockAt(stack, player, world, pos, side, hitX, hitY, hitZ, newState);
    }
}