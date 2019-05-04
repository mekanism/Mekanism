package mekanism.common.util;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import mekanism.api.Coord4D;
import mekanism.common.Mekanism;
import net.minecraft.block.Block;
import net.minecraft.block.BlockShulkerBox;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityShulkerBox;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraftforge.event.ForgeEventFactory;
import net.minecraftforge.fml.relauncher.ReflectionHelper;
import net.minecraftforge.fml.relauncher.ReflectionHelper.UnableToFindMethodException;

public final class MinerUtils {

    public static final List<Block> specialSilkIDs = Arrays.asList(Blocks.ICE, Blocks.CHORUS_FLOWER);

    private static Method getSilkTouchDrop = null;

    static {
        try {
            getSilkTouchDrop = ReflectionHelper
                  .findMethod(Block.class, "getSilkTouchDrop", "func_180643_i", IBlockState.class);
        } catch (UnableToFindMethodException e) {
            Mekanism.logger.error("Unable to find method Block.getSilkTouchDrop");
        }
    }

    public static List<ItemStack> getDrops(World world, Coord4D coord, boolean silk, BlockPos minerPosition) {
        IBlockState state = coord.getBlockState(world);
        Block block = state.getBlock();
        EntityPlayer fakePlayer = Mekanism.proxy.getDummyPlayer((WorldServer) world, minerPosition).get();

        if (block.isAir(state, world, coord.getPos())) {
            return Collections.emptyList();
        }

        if (block instanceof BlockShulkerBox){
            //special case Shulker Boxes because bad Mojang code / no forge patch
            ItemStack shulkerBoxItem = new ItemStack(Item.getItemFromBlock(block));

            TileEntity tileentity = world.getTileEntity(coord.getPos());

            //copied from BlockShulkerBox.breakBlock
            if (tileentity instanceof TileEntityShulkerBox)
            {
                TileEntityShulkerBox tileentityshulkerbox = (TileEntityShulkerBox)tileentity;

                if (!tileentityshulkerbox.isCleared() && tileentityshulkerbox.shouldDrop())
                {
                    NBTTagCompound itemTag = new NBTTagCompound();
                    NBTTagCompound nbtBlockEntity = new NBTTagCompound();
                    itemTag.setTag("BlockEntityTag", ((TileEntityShulkerBox)tileentity).saveToNbt(nbtBlockEntity));
                    shulkerBoxItem.setTagCompound(itemTag);

                    if (tileentityshulkerbox.hasCustomName())
                    {
                        shulkerBoxItem.setStackDisplayName(tileentityshulkerbox.getName());
                    }
                }
            }
            return Collections.singletonList(shulkerBoxItem);
        } else if (silk && (block.canSilkHarvest(world, coord.getPos(), state, fakePlayer) || specialSilkIDs.contains(block))) {
            Object it = null;
            if (getSilkTouchDrop != null) {
                try {
                    it = getSilkTouchDrop.invoke(block, state);
                } catch (InvocationTargetException | IllegalAccessException e) {
                    Mekanism.logger.error("Block.getSilkTouchDrop errored", e);
                }
            }
            List<ItemStack> ret = new ArrayList<>();
            if (it instanceof ItemStack && !((ItemStack) it).isEmpty()) {
                ret.add((ItemStack) it);
            } else {
                //silk touch drop is empty or failed to call/find getSilkTouchDrop method
                // Fallback to grabbing an itemblock
                Item item = Item.getItemFromBlock(block);
                if (item != Items.AIR) {
                    ret.add(new ItemStack(item, 1, item.getHasSubtypes() ? block.getMetaFromState(state) : 0));
                }
            }
            if (ret.size() > 0) {
                ForgeEventFactory.fireBlockHarvesting(ret, world, coord.getPos(), state, 0, 1.0F, true, fakePlayer);
                return ret;
            }
        } else {
            @SuppressWarnings("deprecation")//needed for backwards compatibility
            List<ItemStack> blockDrops = block.getDrops(world, coord.getPos(), state, 0);
            if (blockDrops.size() > 0) {
                ForgeEventFactory.fireBlockHarvesting(blockDrops, world, coord.getPos(), state, 0, 1.0F, false, fakePlayer);
            } else if (block == Blocks.CHORUS_FLOWER) {
                //Chorus flower returns AIR for itemDropped... and for silkTouchDrop.
                blockDrops.add(new ItemStack(Blocks.CHORUS_FLOWER));
            }
            return blockDrops;
        }

        return Collections.emptyList();
    }
}