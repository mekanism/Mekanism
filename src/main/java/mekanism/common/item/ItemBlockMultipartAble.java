package mekanism.common.item;

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
import net.minecraft.block.SoundType;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

/**
 * Created by Thiakil on 19/11/2017.
 */
public abstract class ItemBlockMultipartAble extends ItemBlock {

    public ItemBlockMultipartAble(Block block) {
        super(block);
    }

    /**
     * Reimplementation of onItemUse that will divert to MCMultipart placement functions if applicable
     */
    @Nonnull
    @Override
    public EnumActionResult onItemUse(EntityPlayer player, World worldIn, @Nonnull BlockPos pos, @Nonnull EnumHand hand, @Nonnull EnumFacing facing, float hitX, float hitY, float hitZ) {
        IBlockState iblockstate = worldIn.getBlockState(pos);
        Block block = iblockstate.getBlock();
        ItemStack itemstack = player.getHeldItem(hand);
        if (itemstack.isEmpty()) {
            return EnumActionResult.FAIL;//WTF
        }
        if (Mekanism.hooks.MCMPLoaded) {
            if (!block.isReplaceable(worldIn, pos) && !hasFreeMultiPartSpot(itemstack, worldIn, pos, iblockstate, facing)) {//free spot handles case of no container
                pos = pos.offset(facing);
                iblockstate = worldIn.getBlockState(pos);
            }
        } else if (!block.isReplaceable(worldIn, pos)) {
            pos = pos.offset(facing);
        }

        if (player.canPlayerEdit(pos, facing, itemstack) && mayPlace(itemstack, worldIn, pos, iblockstate, hand, facing)) {
            int i = this.getMetadata(itemstack.getMetadata());
            IBlockState iblockstate1 = this.block.getStateForPlacement(worldIn, pos, facing, hitX, hitY, hitZ, i, player, hand);
            boolean flag;
            if (Mekanism.hooks.MCMPLoaded) {
                flag = MultipartMekanism.placeMultipartBlock(this.block, itemstack, player, worldIn, pos, facing, hitX, hitY, hitZ, iblockstate1);
            } else {
                flag = placeBlockAt(itemstack, player, worldIn, pos, facing, hitX, hitY, hitZ, iblockstate1);
            }
            if (flag) {
                iblockstate1 = worldIn.getBlockState(pos);
                SoundType soundtype = iblockstate1.getBlock().getSoundType(iblockstate1, worldIn, pos, player);
                worldIn.playSound(player, pos, soundtype.getPlaceSound(), SoundCategory.BLOCKS, (soundtype.getVolume() + 1.0F) / 2.0F, soundtype.getPitch() * 0.8F);
                itemstack.shrink(1);
            }
            return EnumActionResult.SUCCESS;
        }
        return EnumActionResult.FAIL;
    }

    private boolean mayPlace(ItemStack itemstack, World worldIn, BlockPos pos, IBlockState state, EnumHand hand, EnumFacing facing) {
        if (!Mekanism.hooks.MCMPLoaded) {
            return worldIn.mayPlace(this.block, pos, false, facing, null);
        }
        return worldIn.mayPlace(this.block, pos, false, facing, null) || hasFreeMultiPartSpot(itemstack, worldIn, pos, state, facing);
    }

    private boolean hasFreeMultiPartSpot(ItemStack itemstack, World worldIn, BlockPos pos, IBlockState state, EnumFacing facing) {
        Optional<IMultipartContainer> container;
        if (!Mekanism.hooks.MCMPLoaded || !(container = MultipartHelper.getContainer(worldIn, pos)).isPresent()) {
            return false;
        }
        IMultipart multipart = getMultiPart();
        IPartSlot slot = multipart.getSlotForPlacement(worldIn, pos, state, facing, 0, 0, 0, null);
        return container.get().canAddPart(slot, this.block.getStateForPlacement(worldIn, pos, facing, 0, 0, 0, itemstack.getMetadata(), null, EnumHand.MAIN_HAND));
    }

    //FQ needed because it uses java optional interface elsewhere
    @net.minecraftforge.fml.common.Optional.Method(modid = MekanismHooks.MCMULTIPART_MOD_ID)
    protected abstract IMultipart getMultiPart();

    @Override
    public boolean canPlaceBlockOnSide(World worldIn, @Nonnull BlockPos pos, @Nonnull EnumFacing side, @Nonnull EntityPlayer player, ItemStack stack) {
        return super.canPlaceBlockOnSide(worldIn, pos, side, player, stack) || (Mekanism.hooks.MCMPLoaded && MultipartHelper.getContainer(worldIn, pos).isPresent());
    }

    @Override
    public boolean placeBlockAt(@Nonnull ItemStack stack, @Nonnull EntityPlayer player, World world, @Nonnull BlockPos pos, EnumFacing side, float hitX, float hitY,
          float hitZ, @Nonnull IBlockState newState) {
        if (!world.getBlockState(pos).getBlock().isReplaceable(world, pos)) {
            return false;
        }
        return super.placeBlockAt(stack, player, world, pos, side, hitX, hitY, hitZ, newState);
    }
}