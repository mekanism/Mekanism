package mekanism.common.item.block;

import java.util.List;
import javax.annotation.Nonnull;
import mekanism.api.EnumColor;
import mekanism.api.MekanismAPI;
import mekanism.common.MekanismBlock;
import mekanism.common.block.BlockCardboardBox.BlockData;
import mekanism.common.tile.TileEntityCardboardBox;
import mekanism.common.util.ItemDataUtils;
import mekanism.common.util.LangUtils;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class ItemBlockCardboardBox extends ItemBlockMekanism {

    private static boolean isMonitoring;

    public ItemBlockCardboardBox(Block block) {
        super(block);
        setMaxStackSize(16);
        MinecraftForge.EVENT_BUS.register(this);
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void addInformation(@Nonnull ItemStack itemstack, World world, @Nonnull List<String> list, @Nonnull ITooltipFlag flag) {
        list.add(EnumColor.INDIGO + LangUtils.localize("tooltip.blockData") + ": " + LangUtils.transYesNo(getBlockData(itemstack) != null));
        BlockData data = getBlockData(itemstack);
        if (data != null) {
            try {
                list.add(LangUtils.localize("tooltip.block") + ": " + new ItemStack(data.block, 1, data.meta).getDisplayName());
                list.add(LangUtils.localize("tooltip.meta") + ": " + data.meta);
                if (data.tileTag != null) {
                    list.add(LangUtils.localize("tooltip.tile") + ": " + data.tileTag.getString("id"));
                }
            } catch (Exception ignored) {
            }
        }
    }

    @Nonnull
    @Override
    public ActionResultType onItemUseFirst(PlayerEntity player, World world, BlockPos pos, Direction side, float hitX, float hitY, float hitZ, Hand hand) {
        ItemStack stack = player.getHeldItem(hand);
        if (!player.isSneaking() && !world.isAirBlock(pos) && stack.getItemDamage() == 0) {
            BlockState state = world.getBlockState(pos);
            Block block = state.getBlock();
            if (!world.isRemote && MekanismAPI.isBlockCompatible(block) && state.getBlockHardness(world, pos) != -1) {
                BlockData data = new BlockData();
                data.block = block;
                data.meta = meta;
                isMonitoring = true;
                if (world.getTileEntity(pos) != null) {
                    TileEntity tile = world.getTileEntity(pos);
                    CompoundNBT tag = new CompoundNBT();
                    tile.write(tag);
                    data.tileTag = tag;
                }
                if (!player.isCreative()) {
                    stack.shrink(1);
                }

                // First, set the block to air to give the underlying block a chance to process
                // any updates (esp. if it's a tile entity backed block). Ideally, we could avoid
                // double updates, but if the block we are wrapping has multiple stacked blocks,
                // we need to make sure it has a chance to update.
                world.removeBlock(pos, false);
                world.setBlockState(pos, MekanismBlock.CARDBOARD_BOX.getBlock().getStateFromMeta(1));
                isMonitoring = false;
                TileEntityCardboardBox tileEntity = (TileEntityCardboardBox) world.getTileEntity(pos);
                if (tileEntity != null) {
                    tileEntity.storedData = data;
                }
                return ActionResultType.SUCCESS;
            }
        }
        return ActionResultType.PASS;
    }

    @Override
    public boolean placeBlockAt(@Nonnull ItemStack stack, @Nonnull PlayerEntity player, World world, @Nonnull BlockPos pos, Direction side, float hitX, float hitY,
          float hitZ, @Nonnull BlockState state) {
        if (world.isRemote) {
            return true;
        }
        if (super.placeBlockAt(stack, player, world, pos, side, hitX, hitY, hitZ, state)) {
            TileEntityCardboardBox tileEntity = (TileEntityCardboardBox) world.getTileEntity(pos);
            if (tileEntity != null) {
                tileEntity.storedData = getBlockData(stack);
            }
            return true;
        }
        return false;
    }

    public void setBlockData(ItemStack itemstack, BlockData data) {
        ItemDataUtils.setCompound(itemstack, "blockData", data.write(new CompoundNBT()));
    }

    public BlockData getBlockData(ItemStack itemstack) {
        if (!ItemDataUtils.hasData(itemstack, "blockData")) {
            return null;
        }
        return BlockData.read(ItemDataUtils.getCompound(itemstack, "blockData"));
    }

    @SubscribeEvent
    public void onEntitySpawn(EntityJoinWorldEvent event) {
        if (event.getEntity() instanceof ItemEntity && isMonitoring) {
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