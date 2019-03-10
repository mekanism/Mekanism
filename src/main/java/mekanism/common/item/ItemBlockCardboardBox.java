package mekanism.common.item;

import java.util.List;
import javax.annotation.Nonnull;
import mekanism.api.EnumColor;
import mekanism.api.MekanismAPI;
import mekanism.common.MekanismBlocks;
import mekanism.common.block.BlockCardboardBox.BlockData;
import mekanism.common.tile.TileEntityCardboardBox;
import mekanism.common.util.ItemDataUtils;
import mekanism.common.util.LangUtils;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class ItemBlockCardboardBox extends ItemBlock {

    private static boolean isMonitoring;

    public Block metaBlock;

    public ItemBlockCardboardBox(Block block) {
        super(block);
        setMaxStackSize(16);
        metaBlock = block;

        MinecraftForge.EVENT_BUS.register(this);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void addInformation(@Nonnull ItemStack itemstack, World world, @Nonnull List<String> list,
          @Nonnull ITooltipFlag flag) {
        list.add(EnumColor.INDIGO + LangUtils.localize("tooltip.blockData") + ": " + LangUtils
              .transYesNo(getBlockData(itemstack) != null));
        BlockData data = getBlockData(itemstack);

        if (data != null) {
            try {
                list.add(LangUtils.localize("tooltip.block") + ": " + new ItemStack(data.block, 1, data.meta)
                      .getDisplayName());
                list.add(LangUtils.localize("tooltip.meta") + ": " + data.meta);

                if (data.tileTag != null) {
                    list.add(LangUtils.localize("tooltip.tile") + ": " + data.tileTag.getString("id"));
                }
            } catch (Exception ignored) {
            }
        }
    }

    @Override
    public int getMetadata(int i) {
        return i;
    }

    @Nonnull
    @Override
    public EnumActionResult onItemUseFirst(EntityPlayer player, World world, BlockPos pos, EnumFacing side, float hitX,
          float hitY, float hitZ, EnumHand hand) {
        ItemStack stack = player.getHeldItem(hand);

        if (!player.isSneaking() && !world.isAirBlock(pos) && stack.getItemDamage() == 0) {
            IBlockState state = world.getBlockState(pos);
            Block block = state.getBlock();
            int meta = block.getMetaFromState(state);

            if (!world.isRemote && MekanismAPI.isBlockCompatible(block, meta)
                  && state.getBlockHardness(world, pos) != -1) {
                BlockData data = new BlockData();
                data.block = block;
                data.meta = meta;

                isMonitoring = true;

                if (world.getTileEntity(pos) != null) {
                    TileEntity tile = world.getTileEntity(pos);
                    NBTTagCompound tag = new NBTTagCompound();

                    tile.writeToNBT(tag);
                    data.tileTag = tag;
                }

                if (!player.capabilities.isCreativeMode) {
                    stack.shrink(1);
                }

                // First, set the block to air to give the underlying block a chance to process
                // any updates (esp. if it's a tile entity backed block). Ideally, we could avoid
                // double updates, but if the block we are wrapping has multiple stacked blocks,
                // we need to make sure it has a chance to update.
                world.setBlockToAir(pos);
                world.setBlockState(pos, MekanismBlocks.CardboardBox.getStateFromMeta(1));

                isMonitoring = false;

                TileEntityCardboardBox tileEntity = (TileEntityCardboardBox) world.getTileEntity(pos);

                if (tileEntity != null) {
                    tileEntity.storedData = data;
                }

                return EnumActionResult.SUCCESS;
            }
        }

        return EnumActionResult.PASS;
    }

    @Override
    public boolean placeBlockAt(@Nonnull ItemStack stack, @Nonnull EntityPlayer player, World world,
          @Nonnull BlockPos pos, EnumFacing side, float hitX, float hitY, float hitZ, @Nonnull IBlockState state) {
        if (world.isRemote) {
            return true;
        }

        boolean place = super.placeBlockAt(stack, player, world, pos, side, hitX, hitY, hitZ, state);

        if (place) {
            TileEntityCardboardBox tileEntity = (TileEntityCardboardBox) world.getTileEntity(pos);

            if (tileEntity != null) {
                tileEntity.storedData = getBlockData(stack);
            }
        }

        return place;
    }

    public void setBlockData(ItemStack itemstack, BlockData data) {
        ItemDataUtils.setCompound(itemstack, "blockData", data.write(new NBTTagCompound()));
    }

    public BlockData getBlockData(ItemStack itemstack) {
        if (!ItemDataUtils.hasData(itemstack, "blockData")) {
            return null;
        }

        return BlockData.read(ItemDataUtils.getCompound(itemstack, "blockData"));
    }

    @SubscribeEvent
    public void onEntitySpawn(EntityJoinWorldEvent event) {
        if (event.getEntity() instanceof EntityItem && isMonitoring) {
            event.setCanceled(true);
        }
    }

    @Override
    public int getItemStackLimit(ItemStack stack) {
        BlockData blockData = getBlockData(stack);
        if (blockData != null) {
            return 1;
        }
        return super.getItemStackLimit(stack);
    }
}
